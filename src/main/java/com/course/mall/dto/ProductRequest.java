package com.course.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotNull
    private Long categoryId;
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private BigDecimal price;
    @NotNull
    private Integer stock;
    private String coverImage;
    private String status = "ON";
}
