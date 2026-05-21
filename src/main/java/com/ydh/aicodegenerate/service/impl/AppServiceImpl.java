package com.ydh.aicodegenerate.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ydh.aicodegenerate.constant.AppConstant;
import com.ydh.aicodegenerate.constant.UserConstant;
import com.ydh.aicodegenerate.core.AiCodeGeneratorFacade;
import com.ydh.aicodegenerate.exception.BusinessException;
import com.ydh.aicodegenerate.exception.ErrorCode;
import com.ydh.aicodegenerate.exception.ThrowUtils;
import com.ydh.aicodegenerate.model.dto.app.AppAddRequest;
import com.ydh.aicodegenerate.model.dto.app.AppDeleteRequest;
import com.ydh.aicodegenerate.model.dto.app.AppQueryRequest;
import com.ydh.aicodegenerate.model.dto.app.AppUpdateRequest;
import com.ydh.aicodegenerate.model.entity.App;
import com.ydh.aicodegenerate.mapper.AppMapper;
import com.ydh.aicodegenerate.model.entity.User;
import com.ydh.aicodegenerate.model.enums.ChatHistoryMessageTypeEnum;
import com.ydh.aicodegenerate.model.enums.CodeGenTypeEnum;
import com.ydh.aicodegenerate.model.vo.AppVO;
import com.ydh.aicodegenerate.model.vo.UserVO;
import com.ydh.aicodegenerate.service.AppService;
import com.ydh.aicodegenerate.service.ChatHistoryService;
import com.ydh.aicodegenerate.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author Nithti
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Resource
    private ChatHistoryService chatHistoryService;

    @Override
    public Long createApp(AppAddRequest appAddRequest, User user) {
        // 安全校验
        ThrowUtils.throwIf(StrUtil.isBlank(appAddRequest.getInitPrompt()), ErrorCode.PARAMS_ERROR,"prompt参数为空");
        ThrowUtils.throwIf((user == null),ErrorCode.PARAMS_ERROR,"用户未登陆");
        // 创建APP实例
        App app = new App();
        // 将请求里的值赋给APP：为了简化开发，如果请求里有10个属性，不用写10次set
        BeanUtil.copyProperties(appAddRequest,app);
        // 赋值
        app.setUserId(user.getId());
        app.setAppName(user.getId() + appAddRequest.getInitPrompt());
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        // 插入数据库
        boolean save = this.save(app);
        ThrowUtils.throwIf(!save,ErrorCode.OPERATION_ERROR,"数据库插入失败");
        return app.getId();
    }

    @Override
    public Boolean updateApp(AppUpdateRequest appAddRequest, User user) {
        Long id = appAddRequest.getId();
        // 判断当前应用的userId是否等于登录用户id
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null,ErrorCode.NOT_FOUND_ERROR,"当前应用不存在");
        Long userId = oldApp.getUserId();
        ThrowUtils.throwIf(!userId.equals(user.getId()),ErrorCode.NO_AUTH_ERROR,"无法修改他人应用");
        // 更新数据
        App app = new App();
        app.setId(id);
        app.setAppName(appAddRequest.getAppName());
        app.setEditTime(LocalDateTime.now());
        boolean isUpdate = this.updateById(app);
        ThrowUtils.throwIf(!isUpdate,ErrorCode.OPERATION_ERROR,"数据更新失败");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteApp(AppDeleteRequest appDeleteRequest, User user) {
        Long id = appDeleteRequest.getId();
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp==null,ErrorCode.NOT_FOUND_ERROR);
        if (!oldApp.getUserId().equals(user.getId()) && !UserConstant.ADMIN_ROLE.equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 先清理应用下历史对话，再删除应用本身
        boolean deleteChatHistory = chatHistoryService.deleteByAppId(id);

        boolean isRemove = this.removeById(id);
        if (!isRemove) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"删除应用失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAppByAdmin(AppDeleteRequest appDeleteRequest, User user) {
        Long id = appDeleteRequest.getId();
        App app = this.getById(id);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        if (!UserConstant.ADMIN_ROLE.equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无管理员权限");
        }
        boolean deleteChatHistory = chatHistoryService.deleteByAppId(id);
        boolean isRemove = this.removeById(id);
        if (!isRemove) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"删除应用失败");
        }
        return true;
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户ID
        Set<Long> userIds = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        // 构建Map映射关系 userId->userVo
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        // 一次性组装全部的AppVo
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).toList();
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User user) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId < 0,ErrorCode.PARAMS_ERROR,"应用ID错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message),ErrorCode.PARAMS_ERROR,"用户消息为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null,ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        // 3. 验证用户使用权限
        if (!app.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum enumByValue = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (enumByValue == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持当前生成类型");
        }
        // 5. 落库用户消息
        boolean saveUserMsg = chatHistoryService.addChatMessage(
                appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), user.getId()
        );
        ThrowUtils.throwIf(!saveUserMsg, ErrorCode.OPERATION_ERROR, "保存用户历史消息失败");

        // 6. 流式生成并在结束后落库完整 AI 回复
        StringBuilder aiMessageBuilder = new StringBuilder();
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message,enumByValue,appId)
                .doOnNext(aiMessageBuilder::append)
                .doOnComplete(() -> {
                    String aiMessage = aiMessageBuilder.toString();
                    if (StrUtil.isNotBlank(aiMessage)) {
                        boolean saveAiMsg = chatHistoryService.addChatMessage(
                                appId, aiMessage, ChatHistoryMessageTypeEnum.AI.getValue(), user.getId()
                        );
                        ThrowUtils.throwIf(!saveAiMsg, ErrorCode.OPERATION_ERROR, "保存AI历史消息失败");
                    }
                })
                .doOnError(error -> {
                    String errorMessage = StrUtil.blankToDefault(error.getMessage(), error.toString());
                    chatHistoryService.addChatMessage(
                            appId,
                            "AI回复失败：" + errorMessage,
                            ChatHistoryMessageTypeEnum.AI.getValue(),
                            user.getId()
                    );
                });
    }

    @Override
    public String deployApp(Long appId, User user) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR,"应用ID错误");
        ThrowUtils.throwIf(user==null,ErrorCode.NOT_FOUND_ERROR,"用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可部署
        if (!app.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限部署该应用");
        }
        // 4. 检查是否已有deploy
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用代码不存在，请先生成应用代码");
        }
        // 7. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir,new File(deployDirPath),true);
        }catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"部署失败" + e.getMessage());
        }
        // 8. 更新deployKey和部署时间
        App updatedApp = new App();
        updatedApp.setId(appId);
        updatedApp.setDeployKey(deployKey);
        updatedApp.setDeployedTime(LocalDateTime.now());
        boolean update = this.updateById(updatedApp);
        ThrowUtils.throwIf(!update,ErrorCode.SYSTEM_ERROR,"更新应用部署信息失败");
        // 9. 返回可访问的url
        return String.format("%s/%s/",AppConstant.CODE_DEPLOY_HOST,deployKey);
    }


}
