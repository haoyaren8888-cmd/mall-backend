package com.course.mall.vo;

import com.course.mall.entity.Product;
import com.course.mall.entity.ProductMessage;
import com.course.mall.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminMessageVO {
    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private String username;
    private String nickname;
    private String campus;
    private String content;
    private String replyContent;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime replyAt;

    public static AdminMessageVO from(ProductMessage message, Product product, User user) {
        AdminMessageVO vo = new AdminMessageVO();
        vo.setId(message.getId());
        vo.setProductId(message.getProductId());
        vo.setProductName(product == null ? "-" : product.getName());
        vo.setUserId(message.getUserId());
        vo.setUsername(user == null ? "-" : user.getUsername());
        vo.setNickname(user == null ? "同学" : user.getNickname());
        vo.setCampus(user == null ? "-" : user.getCampus());
        vo.setContent(message.getContent());
        vo.setReplyContent(message.getReplyContent());
        vo.setStatus(message.getStatus());
        vo.setCreatedAt(message.getCreatedAt());
        vo.setReplyAt(message.getReplyAt());
        return vo;
    }
}
