package com.course.mall.vo;

import com.course.mall.entity.ProductMessage;
import com.course.mall.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductMessageVO {
    private Long id;
    private Long productId;
    private Long userId;
    private String nickname;
    private String campus;
    private String content;
    private LocalDateTime createdAt;

    public static ProductMessageVO from(ProductMessage message, User user) {
        ProductMessageVO vo = new ProductMessageVO();
        vo.setId(message.getId());
        vo.setProductId(message.getProductId());
        vo.setUserId(message.getUserId());
        vo.setNickname(user == null ? "同学" : user.getNickname());
        vo.setCampus(user == null ? null : user.getCampus());
        vo.setContent(message.getContent());
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }
}
