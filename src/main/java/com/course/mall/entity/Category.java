package com.course.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String name;
    private Integer sort;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
