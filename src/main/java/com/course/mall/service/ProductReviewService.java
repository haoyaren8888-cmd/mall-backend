package com.course.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.BusinessException;
import com.course.mall.common.CurrentUser;
import com.course.mall.dto.ProductReviewRequest;
import com.course.mall.entity.Order;
import com.course.mall.entity.OrderItem;
import com.course.mall.entity.Product;
import com.course.mall.entity.ProductReview;
import com.course.mall.entity.User;
import com.course.mall.mapper.OrderItemMapper;
import com.course.mall.mapper.OrderMapper;
import com.course.mall.mapper.ProductMapper;
import com.course.mall.mapper.ProductReviewMapper;
import com.course.mall.mapper.UserMapper;
import com.course.mall.vo.ProductReviewVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProductReviewService {
    private final ProductReviewMapper reviewMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;

    public ProductReviewService(ProductReviewMapper reviewMapper, ProductMapper productMapper,
                                OrderMapper orderMapper, OrderItemMapper orderItemMapper, UserMapper userMapper) {
        this.reviewMapper = reviewMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.userMapper = userMapper;
    }

    public Page<ProductReviewVO> pageReviews(Long productId, long page, long size) {
        requireProductExists(productId);
        Page<ProductReview> reviewPage = reviewMapper.selectPage(Page.of(page, size),
                new LambdaQueryWrapper<ProductReview>()
                        .eq(ProductReview::getProductId, productId)
                        .eq(ProductReview::getStatus, "ON")
                        .orderByDesc(ProductReview::getCreatedAt));

        Page<ProductReviewVO> voPage = Page.of(page, size, reviewPage.getTotal());
        voPage.setRecords(reviewPage.getRecords().stream()
                .map(review -> ProductReviewVO.from(review, userMapper.selectById(review.getUserId())))
                .toList());
        return voPage;
    }

    @Transactional
    public ProductReviewVO create(CurrentUser currentUser, Long productId, ProductReviewRequest request) {
        requireProductExists(productId);
        Order order = requireFinishedBuyerOrder(currentUser.getId(), request.getOrderNo());
        OrderItem orderItem = requireOrderItem(order.getId(), productId);
        ensureNotReviewed(order.getId(), productId, currentUser.getId());

        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (!StringUtils.hasText(content)) {
            throw BusinessException.badRequest("评价内容不能为空");
        }

        ProductReview review = new ProductReview();
        review.setProductId(productId);
        review.setOrderId(order.getId());
        review.setOrderItemId(orderItem.getId());
        review.setUserId(currentUser.getId());
        review.setRating(request.getRating());
        review.setContent(content);
        review.setStatus("ON");
        reviewMapper.insert(review);

        User user = userMapper.selectById(currentUser.getId());
        return ProductReviewVO.from(review, user);
    }

    private void requireProductExists(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw BusinessException.notFound("商品不存在");
        }
    }

    private Order requireFinishedBuyerOrder(Long userId, String orderNo) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw BusinessException.notFound("订单不存在");
        }
        if (!"FINISHED".equals(order.getStatus())) {
            throw BusinessException.badRequest("交易完成后才能评价");
        }
        return order;
    }

    private OrderItem requireOrderItem(Long orderId, Long productId) {
        OrderItem orderItem = orderItemMapper.selectOne(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)
                .eq(OrderItem::getProductId, productId));
        if (orderItem == null) {
            throw BusinessException.badRequest("只能评价本订单购买过的商品");
        }
        return orderItem;
    }

    private void ensureNotReviewed(Long orderId, Long productId, Long userId) {
        ProductReview exists = reviewMapper.selectOne(new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getOrderId, orderId)
                .eq(ProductReview::getProductId, productId)
                .eq(ProductReview::getUserId, userId));
        if (exists != null) {
            throw BusinessException.badRequest("该商品已经评价过了");
        }
    }
}
