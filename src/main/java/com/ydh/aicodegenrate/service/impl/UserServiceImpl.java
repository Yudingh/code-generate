package com.ydh.aicodegenrate.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ydh.aicodegenrate.exception.BusinessException;
import com.ydh.aicodegenrate.exception.ErrorCode;
import com.ydh.aicodegenrate.model.dto.UserQueryRequest;
import com.ydh.aicodegenrate.model.entity.User;
import com.ydh.aicodegenrate.mapper.UserMapper;
import com.ydh.aicodegenrate.model.enums.UserRoleEnum;
import com.ydh.aicodegenrate.model.vo.LoginUserVO;
import com.ydh.aicodegenrate.model.vo.UserVO;
import com.ydh.aicodegenrate.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ydh.aicodegenrate.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author Nithti
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            // 如果参数为空则抛出异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (userAccount.length() < 6 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号过短");
        }
        if (userPassword.length() < 8  || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度需大于8位");
        }
        if (!(userPassword.equals(checkPassword))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入密码不一致");
        }
        // 2. 查询用户是否已经存在
        QueryWrapper account = new QueryWrapper().eq("userAccount", userAccount);
        long accountCount = this.mapper.selectCountByQuery(account);
        if (accountCount > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已存在，无法重复注册");
        }else {
            // 3. 加密密码
            String encryptPassword = getEncryptPassword(userPassword);
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserName("默认用户");
            user.setUserRole(UserRoleEnum.USER.getValue());

            boolean isSave = this.save(user);
            if (!isSave) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败，数据库错误");
            }
            return user.getId();
        }

        // 4. 创建参数
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码为空");
        }
        if (userAccount.length() < 6 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度需大于6位");
        }
        if (userPassword.length() < 8 ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度需大于8位");
        }
        // 2. 解密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 根据账号和密码查询用户
        QueryWrapper queryWrapper = new QueryWrapper().eq("userAccount", userAccount).eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }
        // 4.数据脱敏
        LoginUserVO userVO = getLoginUserVO(user);
        // 5. 更新Session
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return userVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        }
        return user;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"当前用户未登录");
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "ydh";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> users) {
        if (CollUtil.isEmpty(users)) {
            return new ArrayList<UserVO>();
        }
        return users.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("userRole", userRole)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

}
