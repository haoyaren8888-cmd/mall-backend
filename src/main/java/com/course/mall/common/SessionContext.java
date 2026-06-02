package com.course.mall.common;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class SessionContext {
    private SessionContext() {
    }

    public static HttpSession session() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attributes.getRequest().getSession(true);
    }

    public static CurrentUser currentUser() {
        HttpSession session = session();
        return (CurrentUser) session.getAttribute(SessionKeys.CURRENT_USER);
    }

    public static CurrentUser requireUser() {
        CurrentUser user = currentUser();
        if (user == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return user;
    }
}
