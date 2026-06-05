package com.course.mall.service;

import com.course.mall.common.BusinessException;
import com.course.mall.common.CurrentUser;
import com.course.mall.common.SessionKeys;
import com.course.mall.dto.PasswordUpdateRequest;
import com.course.mall.dto.ProfileUpdateRequest;
import com.course.mall.entity.User;
import com.course.mall.mapper.UserMapper;
import com.course.mall.vo.UserVO;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserMapper userMapper;
    @Mock
    private HttpSession session;

    @Test
    void updatePasswordSavesNewPassword() {
        AuthService service = new AuthService(userMapper);
        User user = user();
        user.setPassword("123456");
        when(userMapper.selectById(10L)).thenReturn(user);

        service.updatePassword(currentUser(), passwordRequest("123456", "abcdef"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("abcdef");
    }

    @Test
    void updatePasswordRejectsWrongOldPassword() {
        AuthService service = new AuthService(userMapper);
        User user = user();
        user.setPassword("123456");
        when(userMapper.selectById(10L)).thenReturn(user);

        assertThatThrownBy(() -> service.updatePassword(currentUser(), passwordRequest("wrong", "abcdef")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("原密码不正确");
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void updatePasswordRejectsSamePassword() {
        AuthService service = new AuthService(userMapper);
        User user = user();
        user.setPassword("123456");
        when(userMapper.selectById(10L)).thenReturn(user);

        assertThatThrownBy(() -> service.updatePassword(currentUser(), passwordRequest("123456", "123456")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("新密码不能和原密码相同");
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void updateProfileSavesUserAndRefreshesSession() {
        AuthService service = new AuthService(userMapper);
        when(userMapper.selectById(10L)).thenReturn(user());
        when(userMapper.selectCount(any())).thenReturn(0L);

        UserVO result = service.updateProfile(currentUser(), updateRequest(), session);

        assertThat(result.getNickname()).isEqualTo("任亚浩");
        assertThat(result.getPhone()).isEqualTo("13800000000");
        assertThat(result.getStudentNo()).isEqualTo("20260001");
        assertThat(result.getDormitory()).isEqualTo("明向1号楼");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(userCaptor.capture());
        assertThat(userCaptor.getValue().getCollege()).isEqualTo("计算机科学与技术学院");

        ArgumentCaptor<CurrentUser> sessionUserCaptor = ArgumentCaptor.forClass(CurrentUser.class);
        verify(session).setAttribute(org.mockito.ArgumentMatchers.eq(SessionKeys.CURRENT_USER), sessionUserCaptor.capture());
        assertThat(sessionUserCaptor.getValue().getNickname()).isEqualTo("任亚浩");
    }

    @Test
    void updateProfileRejectsDuplicateStudentNo() {
        AuthService service = new AuthService(userMapper);
        when(userMapper.selectById(10L)).thenReturn(user());
        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.updateProfile(currentUser(), updateRequest(), session))
                .isInstanceOf(BusinessException.class)
                .hasMessage("学号已被其他用户使用");
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void updateProfileRequiresValidLoginUser() {
        AuthService service = new AuthService(userMapper);
        when(userMapper.selectById(10L)).thenReturn(null);

        assertThatThrownBy(() -> service.updateProfile(currentUser(), updateRequest(), session))
                .isInstanceOf(BusinessException.class)
                .hasMessage("登录状态失效");
        verify(userMapper, never()).updateById(any(User.class));
    }

    private CurrentUser currentUser() {
        return new CurrentUser(10L, "leader", "leader", "USER");
    }

    private ProfileUpdateRequest updateRequest() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setNickname(" 任亚浩 ");
        request.setPhone(" 13800000000 ");
        request.setStudentNo(" 20260001 ");
        request.setCampus(" 明向校区 ");
        request.setCollege(" 计算机科学与技术学院 ");
        request.setDormitory(" 明向1号楼 ");
        return request;
    }

    private PasswordUpdateRequest passwordRequest(String oldPassword, String newPassword) {
        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setOldPassword(oldPassword);
        request.setNewPassword(newPassword);
        return request;
    }

    private User user() {
        User user = new User();
        user.setId(10L);
        user.setUsername("leader");
        user.setNickname("leader");
        user.setRole("USER");
        user.setStatus("ENABLED");
        return user;
    }
}
