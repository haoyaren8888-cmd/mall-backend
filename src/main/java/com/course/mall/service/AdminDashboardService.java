package com.course.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.course.mall.entity.Order;
import com.course.mall.entity.Product;
import com.course.mall.entity.ProductMessage;
import com.course.mall.entity.ProductReview;
import com.course.mall.entity.User;
import com.course.mall.mapper.OrderMapper;
import com.course.mall.mapper.ProductMapper;
import com.course.mall.mapper.ProductMessageMapper;
import com.course.mall.mapper.ProductReviewMapper;
import com.course.mall.mapper.UserMapper;
import com.course.mall.vo.AdminStatsVO;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final ProductMessageMapper messageMapper;
    private final ProductReviewMapper reviewMapper;

    public AdminDashboardService(UserMapper userMapper, ProductMapper productMapper, OrderMapper orderMapper,
                                 ProductMessageMapper messageMapper, ProductReviewMapper reviewMapper) {
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.messageMapper = messageMapper;
        this.reviewMapper = reviewMapper;
    }

    public AdminStatsVO stats() {
        Long hiddenMessageCount = messageMapper.selectCount(new LambdaQueryWrapper<ProductMessage>()
                .eq(ProductMessage::getStatus, "HIDDEN"));
        Long hiddenReviewCount = reviewMapper.selectCount(new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getStatus, "HIDDEN"));
        return new AdminStatsVO(
                userMapper.selectCount(new LambdaQueryWrapper<User>()),
                userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, "ENABLED")),
                userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, "DISABLED")),
                productMapper.selectCount(new LambdaQueryWrapper<Product>()),
                orderMapper.selectCount(new LambdaQueryWrapper<Order>()),
                orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, "PENDING_PAY")),
                messageMapper.selectCount(new LambdaQueryWrapper<ProductMessage>()),
                messageMapper.selectCount(new LambdaQueryWrapper<ProductMessage>()
                        .eq(ProductMessage::getStatus, "ON")
                        .and(w -> w.isNull(ProductMessage::getReplyContent)
                                .or()
                                .eq(ProductMessage::getReplyContent, ""))),
                reviewMapper.selectCount(new LambdaQueryWrapper<ProductReview>()),
                hiddenMessageCount + hiddenReviewCount
        );
    }
}
