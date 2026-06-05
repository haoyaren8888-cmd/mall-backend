package com.course.mall.vo;

import com.course.mall.entity.Order;
import com.course.mall.entity.Product;
import com.course.mall.entity.ProductReview;
import com.course.mall.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminReviewVO {
    private Long id;
    private Long productId;
    private String productName;
    private String orderNo;
    private Long userId;
    private String username;
    private String nickname;
    private Integer rating;
    private String content;
    private String status;
    private LocalDateTime createdAt;

    public static AdminReviewVO from(ProductReview review, Product product, Order order, User user) {
        AdminReviewVO vo = new AdminReviewVO();
        vo.setId(review.getId());
        vo.setProductId(review.getProductId());
        vo.setProductName(product == null ? "-" : product.getName());
        vo.setOrderNo(order == null ? "-" : order.getOrderNo());
        vo.setUserId(review.getUserId());
        vo.setUsername(user == null ? "-" : user.getUsername());
        vo.setNickname(user == null ? "同学" : user.getNickname());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setStatus(review.getStatus());
        vo.setCreatedAt(review.getCreatedAt());
        return vo;
    }
}
