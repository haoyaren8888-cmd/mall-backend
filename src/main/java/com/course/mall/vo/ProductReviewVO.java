package com.course.mall.vo;

import com.course.mall.entity.ProductReview;
import com.course.mall.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductReviewVO {
    private Long id;
    private Long productId;
    private Long userId;
    private String nickname;
    private String campus;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;

    public static ProductReviewVO from(ProductReview review, User user) {
        ProductReviewVO vo = new ProductReviewVO();
        vo.setId(review.getId());
        vo.setProductId(review.getProductId());
        vo.setUserId(review.getUserId());
        vo.setNickname(user == null ? "同学" : user.getNickname());
        vo.setCampus(user == null ? null : user.getCampus());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setCreatedAt(review.getCreatedAt());
        return vo;
    }
}
