package com.ydh.aicodegenrate.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ydh.aicodegenrate.model.dto.UserQueryRequest;
import com.ydh.aicodegenrate.model.entity.User;
import com.ydh.aicodegenrate.model.vo.LoginUserVO;
import com.ydh.aicodegenrate.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author Nithti
 */
public interface UserService extends IService<User> {
    /**
     *  用户注册功能
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 登录功能
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request 请求（用于更新Session）
     * @return 返回脱敏后用户数据
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 根据Session获得用户信息
     * @param request http请求
     * @return 返回用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户退出登录
     * @param request http请求
     * @return 是否退出登录成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     *  用户密码加密
     * @param userPassword 用户密码
     * @return 加密后用户密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 将完整用户信息转为脱敏后用户信息
     * @param user 用户完整信息
     * @return 返回脱敏后用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 将完整用户信息转为脱敏后用户信息（查询脱敏）
     * @param user 完整用户信息
     * @return 脱敏用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 将完整用户信息列表转为脱敏后用户信息列表（查询脱敏）
     * @param users 完整用户信息列表
     * @return 脱敏后用户信息列表（查询）
     */
    List<UserVO> getUserVOList(List<User> users);

    /**
     * 根据用户查询构造QueryWrapper
     * @param userQueryRequest 用户查询
     * @return 查询
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);
}
