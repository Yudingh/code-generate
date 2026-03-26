package com.ydh.aicodegenrate.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.ydh.aicodegenrate.annotation.AuthCheck;
import com.ydh.aicodegenrate.common.BaseResponse;
import com.ydh.aicodegenrate.common.ResultUtils;
import com.ydh.aicodegenrate.constant.UserConstant;
import com.ydh.aicodegenrate.exception.BusinessException;
import com.ydh.aicodegenrate.exception.ErrorCode;
import com.ydh.aicodegenrate.exception.ThrowUtils;
import com.ydh.aicodegenrate.model.dto.*;
import com.ydh.aicodegenrate.model.enums.UserRoleEnum;
import com.ydh.aicodegenrate.model.vo.LoginUserVO;
import com.ydh.aicodegenrate.model.vo.UserVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.ydh.aicodegenrate.model.entity.User;
import com.ydh.aicodegenrate.service.UserService;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 用户 控制层。
 *
 * @author Nithti
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest 前端发送请求
     * @return 用户id
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 判断请求参数是否正确
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR,"请求参数为空或");
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param httpServletRequest http请求
     * @return 返回登录用户脱敏后信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest) {
        // 判断参数是否正确
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR,"请求参数为空");
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, httpServletRequest);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 从Session中获取脱敏后登录用户信息
     * @param httpServletRequest 请求
     * @return 脱敏后用户信息
     */
    @PostMapping("/get/login")
    public BaseResponse<LoginUserVO> getLogin(HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(httpServletRequest == null,ErrorCode.PARAMS_ERROR,"参数为空");
        User loginUser = userService.getLoginUser(httpServletRequest);
        LoginUserVO userVO = userService.getLoginUserVO(loginUser);
        return ResultUtils.success(userVO);
    }

    /**
     * 用户退出（从Session中删除当前用户）
     * @param httpServletRequest 请求
     * @return 是否退出登录成功
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(httpServletRequest == null,ErrorCode.PARAMS_ERROR,"参数为空");
        boolean isLogout = userService.userLogout(httpServletRequest);
        return ResultUtils.success(isLogout);
    }

    /**
     * 增加用户（仅管理员）
     * @param userAddRequest 请求
     * @return 新增用户id
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> add(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest==null,ErrorCode.PARAMS_ERROR,"参数为空");
        // 将请求中的数据填入User中
        User user = new User();
        BeanUtil.copyProperties(userAddRequest,user);
        // 设置默认密码
        final String defaultPassWord = "123456789";
        String userPassword = userService.getEncryptPassword(defaultPassWord);
        user.setUserPassword(userPassword);
        boolean save = userService.save(user);
        ThrowUtils.throwIf(!save,ErrorCode.OPERATION_ERROR,"添加用户失败");
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据用户id查询用户
     * @param userId 用户id
     * @return 用户
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(Long userId) {
        ThrowUtils.throwIf(userId==null,ErrorCode.PARAMS_ERROR,"参数为空");
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user==null,ErrorCode.NOT_FOUND_ERROR,"请求数据不存在");
        return ResultUtils.success(user);
    }

    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(Long userId) {
        BaseResponse<User> userBaseResponse = getUserById(userId);
        User user = userBaseResponse.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 根据id删除用户
     * @param userId 用户id
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> delete(Long userId, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(userId == null,ErrorCode.PARAMS_ERROR,"参数为空");
        // 不能自己删除自己
        Long logId = userService.getLoginUser(httpServletRequest).getId();
        ThrowUtils.throwIf(logId.equals(userId),ErrorCode.PARAMS_ERROR,"无法删除自己");
        boolean removedById = userService.removeById(userId);
        return ResultUtils.success(removedById);
    }

    /**
     * 根据用户ID更新用户信息
     * @param userUpdateRequest 用户更新请求
     * @return true
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> update(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest==null || userUpdateRequest.getId()==null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest,user);
        boolean updateById = userService.updateById(user);
        ThrowUtils.throwIf(!updateById,ErrorCode.OPERATION_ERROR,"更新用户信息失败");
        return ResultUtils.success(true);
    }

    /**
     * 根据请求分页查询脱敏后用户信息
     * @param userQueryRequest 用户查询请求
     * @return 分页查询结果
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

}
