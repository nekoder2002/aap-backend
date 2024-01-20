package com.dhu.interceptor;

import com.dhu.constants.ExceptionConstants;
import com.dhu.exception.NotLoginException;
import com.dhu.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.判斷是否需要拦截(Treadlocal是否用户)
        if (UserHolder.getUser() == null) {
            //没有，需要拦截，抛出异常
            throw new NotLoginException(ExceptionConstants.NOT_LOGIN_MSG);
        }
        return true;
    }
}
