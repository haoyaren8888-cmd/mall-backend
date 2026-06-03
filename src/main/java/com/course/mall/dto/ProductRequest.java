package com.course.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotNull
    private Long categoryId;
    private Long sellerId;
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private String coverImage;
    private String status = "ON";
    private String conditionLevel;
    private String campus;
    private String tradePlace;
    private String tradeType;
    private String auditStatus;
    private String itemStatus;
    private String rejectReason;
}
