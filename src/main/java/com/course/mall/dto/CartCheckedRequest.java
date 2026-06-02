package com.course.mall.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartCheckedRequest {
    private List<Long> ids;
    private Boolean checked;
}
