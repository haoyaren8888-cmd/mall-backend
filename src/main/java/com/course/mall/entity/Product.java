package com.course.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private Long sellerId;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private String coverImage;
    private Integer sales;
    private String status;
    private String conditionLevel;
    private String campus;
    private String tradePlace;
    private String tradeType;
    private String auditStatus;
    private String itemStatus;
    private Integer viewCount;
    private Integer favoriteCount;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
