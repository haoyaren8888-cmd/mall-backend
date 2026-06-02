package com.course.mall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull
    private Long productId;
    @NotNull
    private Integer quantity;
    private Boolean checked = true;
}
