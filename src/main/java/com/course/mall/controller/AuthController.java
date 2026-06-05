package com.course.mall.controller;

import com.course.mall.common.CurrentUser;
import com.course.mall.common.Result;
import com.course.mall.common.SessionContext;
import com.course.mall.dto.LoginRequest;
import com.course.mall.dto.PasswordUpdateRequest;
import com.course.mall.dto.ProfileUpdateRequest;
import com.course.mall.dto.RegisterRequest;
import com.course.mall.service.AuthService;
import com.course.mall.vo.UserVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return Result.ok(authService.login(request, session));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<UserVO> me() {
        CurrentUser user = SessionContext.requireUser();
        return Result.ok(authService.me(user));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateRequest request, HttpSession session) {
        CurrentUser user = SessionContext.requireUser();
        return Result.ok(authService.updateProfile(user, request, session));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        CurrentUser user = SessionContext.requireUser();
        authService.updatePassword(user, request);
        return Result.ok();
    }
}
