package com.course.mall.interceptor;

import com.course.mall.common.BusinessException;
import com.course.mall.common.SessionKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (request.getSession(false) == null ||
                request.getSession(false).getAttribute(SessionKeys.CURRENT_USER) == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return true;
    }
}
