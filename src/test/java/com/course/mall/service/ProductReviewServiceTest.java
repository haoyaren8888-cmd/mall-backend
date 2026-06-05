package com.course.mall.service;

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
import com.course.mall.vo.AdminReviewVO;
import com.course.mall.vo.ProductReviewVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductReviewServiceTest {
    @Mock
    private ProductReviewMapper reviewMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private UserMapper userMapper;

    @Test
    void createRequiresFinishedOrderAndSavesReview() {
        ProductReviewService service = productReviewService();
        ProductReviewRequest request = reviewRequest();

        when(productMapper.selectById(30L)).thenReturn(product(30L));
        when(orderMapper.selectOne(any())).thenReturn(order("FINISHED"));
        when(orderItemMapper.selectOne(any())).thenReturn(orderItem(77L, 30L));
        when(reviewMapper.selectOne(any())).thenReturn(null);
        when(userMapper.selectById(10L)).thenReturn(user());
        doAnswer(invocation -> {
            ProductReview review = invocation.getArgument(0);
            review.setId(88L);
            return 1;
        }).when(reviewMapper).insert(any(ProductReview.class));

        ProductReviewVO result = service.create(currentUser(), 30L, request);

        assertThat(result.getId()).isEqualTo(88L);
        assertThat(result.getProductId()).isEqualTo(30L);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getContent()).isEqualTo("成色很好，交易也很顺利");
        assertThat(result.getNickname()).isEqualTo("王嘉毅");

        ArgumentCaptor<ProductReview> captor = ArgumentCaptor.forClass(ProductReview.class);
        verify(reviewMapper).insert(captor.capture());
        ProductReview saved = captor.getValue();
        assertThat(saved.getOrderId()).isEqualTo(99L);
        assertThat(saved.getOrderItemId()).isEqualTo(77L);
        assertThat(saved.getUserId()).isEqualTo(10L);
        assertThat(saved.getStatus()).isEqualTo("ON");
    }

    @Test
    void createRejectsUnfinishedOrder() {
        ProductReviewService service = productReviewService();

        when(productMapper.selectById(30L)).thenReturn(product(30L));
        when(orderMapper.selectOne(any())).thenReturn(order("SHIPPED"));

        assertThatThrownBy(() -> service.create(currentUser(), 30L, reviewRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("交易完成后才能评价");
        verify(reviewMapper, never()).insert(any(ProductReview.class));
    }

    @Test
    void createRejectsDuplicateReview() {
        ProductReviewService service = productReviewService();

        when(productMapper.selectById(30L)).thenReturn(product(30L));
        when(orderMapper.selectOne(any())).thenReturn(order("FINISHED"));
        when(orderItemMapper.selectOne(any())).thenReturn(orderItem(77L, 30L));
        when(reviewMapper.selectOne(any())).thenReturn(existingReview());

        assertThatThrownBy(() -> service.create(currentUser(), 30L, reviewRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("该商品已经评价过了");
        verify(reviewMapper, never()).insert(any(ProductReview.class));
    }

    @Test
    void pageReviewsLoadsReviewerInfo() {
        ProductReviewService service = productReviewService();
        ProductReview review = existingReview();
        review.setCreatedAt(LocalDateTime.now());
        Page<ProductReview> reviewPage = Page.of(1, 5, 1);
        reviewPage.setRecords(List.of(review));

        when(productMapper.selectById(30L)).thenReturn(product(30L));
        when(reviewMapper.selectPage(any(), any())).thenReturn(reviewPage);
        when(userMapper.selectById(10L)).thenReturn(user());

        Page<ProductReviewVO> result = service.pageReviews(30L, 1, 5);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getRating()).isEqualTo(5);
        assertThat(result.getRecords().get(0).getNickname()).isEqualTo("王嘉毅");
    }

    @Test
    void pageAdminReviewsLoadsProductOrderAndUserInfo() {
        ProductReviewService service = productReviewService();
        ProductReview review = existingReview();
        Page<ProductReview> reviewPage = Page.of(1, 10, 1);
        reviewPage.setRecords(List.of(review));

        when(reviewMapper.selectPage(any(), any())).thenReturn(reviewPage);
        when(productMapper.selectById(30L)).thenReturn(product(30L));
        when(orderMapper.selectById(99L)).thenReturn(order("FINISHED"));
        when(userMapper.selectById(10L)).thenReturn(user());

        Page<AdminReviewVO> result = service.pageAdminReviews(1, 10, "成色", "ON", 5);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        AdminReviewVO reviewVO = result.getRecords().get(0);
        assertThat(reviewVO.getProductName()).isEqualTo("Java Web notes");
        assertThat(reviewVO.getOrderNo()).isEqualTo("M1001");
        assertThat(reviewVO.getUsername()).isEqualTo("buyer");
        assertThat(reviewVO.getStatus()).isEqualTo("ON");
    }

    @Test
    void updateAdminStatusCanHideReview() {
        ProductReviewService service = productReviewService();
        ProductReview review = existingReview();

        when(reviewMapper.selectById(66L)).thenReturn(review);

        service.updateAdminStatus(66L, "HIDDEN");

        assertThat(review.getStatus()).isEqualTo("HIDDEN");
        verify(reviewMapper).updateById(review);
    }

    @Test
    void updateAdminStatusRejectsInvalidStatus() {
        ProductReviewService service = productReviewService();

        assertThatThrownBy(() -> service.updateAdminStatus(66L, "DELETED"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("评价状态不正确");
        verify(reviewMapper, never()).updateById(any(ProductReview.class));
    }

    private ProductReviewService productReviewService() {
        return new ProductReviewService(reviewMapper, productMapper, orderMapper, orderItemMapper, userMapper);
    }

    private CurrentUser currentUser() {
        return new CurrentUser(10L, "buyer", "buyer", "USER");
    }

    private ProductReviewRequest reviewRequest() {
        ProductReviewRequest request = new ProductReviewRequest();
        request.setOrderNo("M1001");
        request.setRating(5);
        request.setContent(" 成色很好，交易也很顺利 ");
        return request;
    }

    private Product product(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Java Web notes");
        return product;
    }

    private Order order(String status) {
        Order order = new Order();
        order.setId(99L);
        order.setOrderNo("M1001");
        order.setUserId(10L);
        order.setStatus(status);
        return order;
    }

    private OrderItem orderItem(Long id, Long productId) {
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setOrderId(99L);
        item.setProductId(productId);
        return item;
    }

    private ProductReview existingReview() {
        ProductReview review = new ProductReview();
        review.setId(66L);
        review.setProductId(30L);
        review.setOrderId(99L);
        review.setOrderItemId(77L);
        review.setUserId(10L);
        review.setRating(5);
        review.setContent("成色很好，交易也很顺利");
        review.setStatus("ON");
        return review;
    }

    private User user() {
        User user = new User();
        user.setId(10L);
        user.setUsername("buyer");
        user.setNickname("王嘉毅");
        user.setCampus("明向校区");
        return user;
    }
}
