package com.course.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String studentNo;
    private String campus;
    private String college;
    private String dormitory;
    private String role;
    private String status;
    private Integer creditScore;
    private Integer dealCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
