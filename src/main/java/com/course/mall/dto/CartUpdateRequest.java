package com.course.mall.dto;

import lombok.Data;

@Data
public class CartUpdateRequest {
    private Integer quantity;
    private Boolean checked;
}
