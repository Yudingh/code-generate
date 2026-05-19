package com.ydh.aicodegenrate.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.ydh.aicodegenrate.annotation.AuthCheck;
import com.ydh.aicodegenrate.common.BaseResponse;
import com.ydh.aicodegenrate.common.DeleteRequest;
import com.ydh.aicodegenrate.common.ResultUtils;
import com.ydh.aicodegenrate.constant.AppConstant;
import com.ydh.aicodegenrate.constant.UserConstant;
import com.ydh.aicodegenrate.exception.BusinessException;
import com.ydh.aicodegenrate.exception.ErrorCode;
import com.ydh.aicodegenrate.exception.ThrowUtils;
import com.ydh.aicodegenrate.model.dto.app.*;
import com.ydh.aicodegenrate.model.entity.User;
import com.ydh.aicodegenrate.model.vo.AppVO;
import com.ydh.aicodegenrate.model.vo.UserVO;
import com.ydh.aicodegenrate.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.ydh.aicodegenrate.model.entity.App;
import com.ydh.aicodegenrate.service.AppService;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 应用 控制层。
 *
 * @author Nithti
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    /**
     * 用户创建应用
     * @param appAddRequest 创建应用请求
     * @param request 请求
     * @return 应用id
     */
     @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
         String initPrompt = appAddRequest.getInitPrompt();
         ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR,"输入不能为空");
         User user = userService.getLoginUser(request);
         Long appId = appService.createApp(appAddRequest, user);
         return ResultUtils.success(appId);
     }

    /**
     * 用户更新应用（只能更改自己的应用名称）
     * @param appUpdateRequest 更新请求
     * @param request session请求
     * @return 是否更新成功
     */
     @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
         ThrowUtils.throwIf(appUpdateRequest.getId() == null,ErrorCode.PARAMS_ERROR,"应用ID为空");
         ThrowUtils.throwIf(StrUtil.isBlank(appUpdateRequest.getAppName()),ErrorCode.PARAMS_ERROR,"更新应用名为空");
         // 用户当前用户
         User user = userService.getLoginUser(request);
         return ResultUtils.success(appService.updateApp(appUpdateRequest, user));
     }

    /**
     * 用户删除应用（只能删除自己的）
     * @param appDeleteRequest 前端删除请求
     * @param request session请求
     * @return 删除成功
     */
     @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody AppDeleteRequest appDeleteRequest, HttpServletRequest request) {
         ThrowUtils.throwIf(appDeleteRequest.getId() == null,ErrorCode.PARAMS_ERROR,"应用ID为空");
         User user = userService.getLoginUser(request);
         return ResultUtils.success(appService.deleteApp(appDeleteRequest, user));
     }

    /**
     * 查询应用信息
     * @param id 应用id
     * @return 应用信息
     */
     @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVoById(Long id){
         ThrowUtils.throwIf(id <= 0,ErrorCode.PARAMS_ERROR);
         App app = appService.getById(id);
         ThrowUtils.throwIf(app == null,ErrorCode.NOT_FOUND_ERROR);
         return ResultUtils.success(appService.getAppVO(app));
     }

    /**
     * 分页查询用户创建的应用列表
     * @param appQueryRequest 分页查询请求
     * @param request http请求
     * @return 分页信息
     */
     @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVoByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request){
         ThrowUtils.throwIf(appQueryRequest == null,ErrorCode.PARAMS_ERROR);
         int pageSize = appQueryRequest.getPageSize();
         ThrowUtils.throwIf(pageSize <= 0,ErrorCode.PARAMS_ERROR);
         ThrowUtils.throwIf(pageSize > 20,ErrorCode.PARAMS_ERROR,"每页最多查询20个应用");
         int pageNum = appQueryRequest.getPageNum();
         // 限制用户只能查自己的应用
         User user = userService.getLoginUser(request);
         appQueryRequest.setUserId(user.getId());
         QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
         Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
         // 数据封装
         Page<AppVO> appVOPage = new Page<>(pageNum, pageSize,appPage.getTotalRow());
         List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
         appVOPage.setRecords(appVOList);
         return ResultUtils.success(appVOPage);
     }

    /**
     * 分页查询精选应用
     * @param appQueryRequest 分页查询请求
     * @return 分页精选应用
     */
     @PostMapping("/good/list/page/vo")
    public BaseResponse<Page<AppVO>> listGoodAppVoByPage(@RequestBody AppQueryRequest appQueryRequest){
         ThrowUtils.throwIf(appQueryRequest == null,ErrorCode.PARAMS_ERROR);
         int pageSize = appQueryRequest.getPageSize();
         ThrowUtils.throwIf(pageSize <= 0,ErrorCode.PARAMS_ERROR);
         ThrowUtils.throwIf(pageSize > 20,ErrorCode.PARAMS_ERROR,"每页最多查询20个应用");
         int pageNum = appQueryRequest.getPageNum();
         // 只查询精选应用
         appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
         QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
         Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
         Page<AppVO> appVOPage = new Page<>(pageNum, pageSize,appPage.getTotalRow());
         List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
         appVOPage.setRecords(appVOList);
         return ResultUtils.success(appVOPage);
     }

    /**
     * 管理员删除应用
     * @param deleteRequest 删除请求
     * @return 是否成功
     */
     @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteByAdmin(@RequestBody DeleteRequest deleteRequest){
         if (deleteRequest == null || deleteRequest.getId() < 0){
             throw new BusinessException(ErrorCode.PARAMS_ERROR);
         }
         long id = deleteRequest.getId();
         // 判断是否存在
         App oldApp = appService.getById(id);
         ThrowUtils.throwIf(oldApp == null,ErrorCode.NOT_FOUND_ERROR);
         boolean result = appService.removeById(id);
         return ResultUtils.success(result);
     }

    /**
     * 管理员更新应用信息
     * @param appAdminUpdateRequest 更新应用请求
     * @return 更新结果
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest){
         if (appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null){
             throw new BusinessException(ErrorCode.PARAMS_ERROR);
         }
         long id = appAdminUpdateRequest.getId();
         App oldApp = appService.getById(id);
         ThrowUtils.throwIf(oldApp == null,ErrorCode.NOT_FOUND_ERROR);
         App app = new App();
         BeanUtil.copyProperties(appAdminUpdateRequest, app, CopyOptions.create().setIgnoreNullValue(true));
         app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result ,ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员分页查询应用
     * @param appQueryRequest 查询请求
     * @return 分页查询结果
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAdminAppVoByPage(@RequestBody AppQueryRequest appQueryRequest){
        ThrowUtils.throwIf(appQueryRequest == null,ErrorCode.PARAMS_ERROR);
        int pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize <= 0,ErrorCode.PARAMS_ERROR);
        int pageNum = appQueryRequest.getPageNum();
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize,appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 管理员查看应用信息
     * @param id 应用id
     * @return 应用信息
     */
    @GetMapping("admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> getAppVoByIdByAdmin(long id){
        ThrowUtils.throwIf(id <= 0,ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null,ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 流式返回生成结果
     * @param appId 应用id
     * @param message 用户消息
     * @param request http请求
     * @return 生成结果
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String, String>>> chatToGenCode(@RequestParam Long appId,@RequestParam String message,HttpServletRequest request){
        ThrowUtils.throwIf(appId == null || appId < 0,ErrorCode.PARAMS_ERROR,"应用ID错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message),ErrorCode.PARAMS_ERROR,"用户输入为空");
        User loginUser = userService.getLoginUser(request);
        Flux<ServerSentEvent<Map<String, String>>> dataStream = appService.chatToGenCode(appId, message, loginUser)
                .map(chunk -> ServerSentEvent.<Map<String, String>>builder()
                        .data(Collections.singletonMap("d", chunk))
                        .build());
        ServerSentEvent<Map<String, String>> doneEvent = ServerSentEvent.<Map<String, String>>builder()
                .event("done")
                .build();
        return dataStream.concatWith(Flux.just(doneEvent));
    }

    @PostMapping("/deploy")
    public BaseResponse<String> deploy(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request){
        ThrowUtils.throwIf(appDeployRequest == null,ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId < 0,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }
}
