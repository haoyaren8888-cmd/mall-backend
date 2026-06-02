package com.course.mall.interceptor;

import com.course.mall.common.BusinessException;
import com.course.mall.common.CurrentUser;
import com.course.mall.common.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminInterceptorTest {
    private final AdminInterceptor interceptor = new AdminInterceptor();

    @Test
    void allowsAdminUser() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/products");
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionKeys.CURRENT_USER, new CurrentUser(1L, "admin", "admin", "ADMIN"));

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), null);

        assertThat(result).isTrue();
    }

    @Test
    void rejectsNormalUser() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/products");
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionKeys.CURRENT_USER, new CurrentUser(2L, "user", "user", "USER"));

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), null))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(403);
    }

    @Test
    void rejectsAnonymousRequestWithoutCreatingSession() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/products");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), null))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(403);
        assertThat(request.getSession(false)).isNull();
    }

    @Test
    void allowsOptionsPreflight() {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/admin/products");

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), null);

        assertThat(result).isTrue();
    }
}
