package com.course.mall.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String nickname;
    private String phone;
    private String studentNo;
    private String campus;
    private String college;
    private String dormitory;
}
