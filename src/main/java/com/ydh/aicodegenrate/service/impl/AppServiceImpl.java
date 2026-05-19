package com.ydh.aicodegenrate.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ydh.aicodegenrate.constant.UserConstant;
import com.ydh.aicodegenrate.exception.BusinessException;
import com.ydh.aicodegenrate.exception.ErrorCode;
import com.ydh.aicodegenrate.exception.ThrowUtils;
import com.ydh.aicodegenrate.model.dto.app.AppAddRequest;
import com.ydh.aicodegenrate.model.dto.app.AppDeleteRequest;
import com.ydh.aicodegenrate.model.dto.app.AppQueryRequest;
import com.ydh.aicodegenrate.model.dto.app.AppUpdateRequest;
import com.ydh.aicodegenrate.model.entity.App;
import com.ydh.aicodegenrate.mapper.AppMapper;
import com.ydh.aicodegenrate.model.entity.User;
import com.ydh.aicodegenrate.model.enums.CodeGenTypeEnum;
import com.ydh.aicodegenrate.model.vo.AppVO;
import com.ydh.aicodegenrate.model.vo.UserVO;
import com.ydh.aicodegenrate.service.AppService;
import com.ydh.aicodegenrate.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

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
    public Boolean deleteApp(AppDeleteRequest appDeleteRequest, User user) {
        Long id = appDeleteRequest.getId();
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp==null,ErrorCode.NOT_FOUND_ERROR);
        if (!oldApp.getUserId().equals(user.getId()) && !UserConstant.ADMIN_ROLE.equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return this.removeById(id);
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
        Set<Long> userIds = appList.stream().map(App::getId).collect(Collectors.toSet());
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


}
