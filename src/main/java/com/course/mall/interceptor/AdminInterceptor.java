package com.course.mall.interceptor;

import com.course.mall.common.BusinessException;
import com.course.mall.common.CurrentUser;
import com.course.mall.common.SessionKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        HttpSession session = request.getSession(false);
        CurrentUser user = session == null ? null : (CurrentUser) session.getAttribute(SessionKeys.CURRENT_USER);
        if (!isAdmin(user)) {
            throw BusinessException.forbidden("没有后台操作权限");
        }
        return true;
    }

    private boolean isAdmin(CurrentUser user) {
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}
