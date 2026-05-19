package com.ydh.aicodegenrate.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ydh.aicodegenrate.model.dto.app.AppAddRequest;
import com.ydh.aicodegenrate.model.dto.app.AppDeleteRequest;
import com.ydh.aicodegenrate.model.dto.app.AppQueryRequest;
import com.ydh.aicodegenrate.model.dto.app.AppUpdateRequest;
import com.ydh.aicodegenrate.model.entity.App;
import com.ydh.aicodegenrate.model.entity.User;
import com.ydh.aicodegenrate.model.vo.AppVO;
import com.ydh.aicodegenrate.model.vo.UserVO;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author Nithti
 */
public interface AppService extends IService<App> {
    /**
     * 创建应用接口
     * @param appAddRequest 创建应用提示词
     * @param user 当前登录用户
     * @return 返回创建的应用id
     */
    Long createApp(AppAddRequest appAddRequest, User user);

    /**
     * 更新应用接口
     * @param appAddRequest 更新应用请求
     * @param user 当前登录用户
     * @return 更新结果
     */
    Boolean updateApp(AppUpdateRequest appAddRequest, User user);

    /**
     * 删除应用
     * @param appDeleteRequest 删除应用请求
     * @param user 当前登录用户
     * @return 是否删除成功
     */
    Boolean deleteApp(AppDeleteRequest appDeleteRequest, User user);

    /**
     * 获取封装App信息
     * @param app  app信息
     * @return 封装app信息
     */
    AppVO getAppVO(App app);

    /**
     * 构造查询对象
     * @param appQueryRequest 查询请求
     * @return 查询对象
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    List<AppVO> getAppVOList(List<App> appList);
}
