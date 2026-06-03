package com.course.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.course.mall.common.BusinessException;
import com.course.mall.common.CurrentUser;
import com.course.mall.common.SessionKeys;
import com.course.mall.dto.LoginRequest;
import com.course.mall.dto.RegisterRequest;
import com.course.mall.entity.User;
import com.course.mall.mapper.UserMapper;
import com.course.mall.vo.UserVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {
    private final UserMapper userMapper;

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserVO register(RegisterRequest request) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw BusinessException.badRequest("用户名已存在");
        }
        if (StringUtils.hasText(request.getStudentNo())) {
            Long studentCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getStudentNo, request.getStudentNo()));
            if (studentCount > 0) {
                throw BusinessException.badRequest("学号已被注册");
            }
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setStudentNo(request.getStudentNo());
        user.setCampus(request.getCampus());
        user.setCollege(request.getCollege());
        user.setDormitory(request.getDormitory());
        user.setRole("USER");
        user.setStatus("ENABLED");
        user.setCreditScore(100);
        user.setDealCount(0);
        userMapper.insert(user);
        return UserVO.from(user);
    }

    public UserVO login(LoginRequest request, HttpSession session) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getPassword, request.getPassword()));
        if (user == null) {
            throw BusinessException.badRequest("用户名或密码错误");
        }
        if (!"ENABLED".equals(user.getStatus())) {
            throw BusinessException.forbidden("账号已被禁用");
        }
        CurrentUser currentUser = new CurrentUser(user.getId(), user.getUsername(), user.getNickname(), user.getRole());
        session.setAttribute(SessionKeys.CURRENT_USER, currentUser);
        return UserVO.from(user);
    }

    public UserVO me(CurrentUser currentUser) {
        User user = userMapper.selectById(currentUser.getId());
        if (user == null) {
            throw BusinessException.unauthorized("登录状态失效");
        }
        return UserVO.from(user);
    }
}
