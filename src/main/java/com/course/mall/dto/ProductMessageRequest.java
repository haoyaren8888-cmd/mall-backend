package com.course.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductMessageRequest {
    @NotBlank(message = "留言内容不能为空")
    @Size(max = 300, message = "留言内容不能超过300字")
    private String content;
}
