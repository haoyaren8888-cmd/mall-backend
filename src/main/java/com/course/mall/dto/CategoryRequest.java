package com.course.mall.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    private Long parentId = 0L;
    @NotBlank
    private String name;
    private Integer sort = 0;
    private String status = "ON";
}
