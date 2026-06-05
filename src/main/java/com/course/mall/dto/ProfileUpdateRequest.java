package com.course.mall.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String nickname;
    private String phone;
    private String studentNo;
    private String campus;
    private String college;
    private String dormitory;
}
