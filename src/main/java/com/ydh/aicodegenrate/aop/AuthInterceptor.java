package com.ydh.aicodegenrate.aop;

import com.ydh.aicodegenrate.annotation.AuthCheck;
import com.ydh.aicodegenrate.exception.BusinessException;
import com.ydh.aicodegenrate.exception.ErrorCode;
import com.ydh.aicodegenrate.model.entity.User;
import com.ydh.aicodegenrate.model.enums.UserRoleEnum;
import com.ydh.aicodegenrate.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doIntercept(ProceedingJoinPoint point, AuthCheck authCheck) throws Throwable {
        // 获得要求的权限
        String mustRole = authCheck.mustRole();
        UserRoleEnum mostRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // 如果要求权限为空，即不需要权限，直接放行
        if (mostRoleEnum == null) {
            return point.proceed();
        }
        // 要求权限不为空
        // 获得当前用户
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(httpServletRequest);
        // 获得用户权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 如果用户权限为空，拦截
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 如果要求权限为管理员，但是用户权限不是，拦截
        if (UserRoleEnum.ADMIN.equals(mostRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"需要管理员权限");
        }
        return point.proceed();
    }
}
