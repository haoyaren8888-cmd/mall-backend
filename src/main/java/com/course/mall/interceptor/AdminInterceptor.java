package com.course.mall.interceptor;

import com.course.mall.common.BusinessException;
import com.course.mall.common.CurrentUser;
import com.course.mall.common.SessionKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        CurrentUser user = (CurrentUser) request.getSession().getAttribute(SessionKeys.CURRENT_USER);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw BusinessException.forbidden("没有后台操作权限");
        }
        return true;
    }
}
