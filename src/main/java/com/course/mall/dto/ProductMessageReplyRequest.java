package com.course.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductMessageReplyRequest {
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 300, message = "回复内容不能超过300字")
    private String replyContent;
}
