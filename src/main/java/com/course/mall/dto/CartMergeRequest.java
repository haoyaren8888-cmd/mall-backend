package com.course.mall.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CartMergeRequest {
    private List<CartItemRequest> items = new ArrayList<>();
}
