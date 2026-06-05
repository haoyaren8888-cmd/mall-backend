package com.course.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.CurrentUser;
import com.course.mall.common.SessionKeys;
import com.course.mall.entity.Order;
import com.course.mall.entity.OrderItem;
import com.course.mall.entity.Product;
import com.course.mall.mapper.AddressMapper;
import com.course.mall.mapper.CartItemMapper;
import com.course.mall.mapper.OrderItemMapper;
import com.course.mall.mapper.OrderMapper;
import com.course.mall.mapper.PaymentRecordMapper;
import com.course.mall.mapper.ProductMapper;
import com.course.mall.vo.OrderVO;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderSellerListTest {
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private AddressMapper addressMapper;
    @Mock
    private PaymentRecordMapper paymentRecordMapper;

    @AfterEach
    void resetRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void sellerListOnlyReturnsSellerOrderItems() {
        loginAsUser(10L);
        OrderService orderService = orderService();
        Product product = product(30L, 10L);
        Order order = order(99L, "M1001");
        OrderItem sellerItem = orderItem(99L, 30L, "keyboard", "89.00", 2);
        Page<Order> orderPage = Page.of(1, 10, 1);
        orderPage.setRecords(List.of(order));

        when(productMapper.selectList(any())).thenReturn(List.of(product));
        when(orderItemMapper.selectList(any())).thenReturn(List.of(sellerItem), List.of(sellerItem));
        when(orderMapper.selectPage(any(), any())).thenReturn(orderPage);

        Page<OrderVO> result = orderService.sellerList(1, 10, "PAID");

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getOrderNo()).isEqualTo("M1001");
        assertThat(result.getRecords().get(0).getTotalAmount()).isEqualByComparingTo("178.00");
        assertThat(result.getRecords().get(0).getItems()).hasSize(1);
        assertThat(result.getRecords().get(0).getItems().get(0).getProductId()).isEqualTo(30L);
    }

    @Test
    void sellerListReturnsEmptyWhenSellerHasNoProducts() {
        loginAsUser(10L);
        OrderService orderService = orderService();
        when(productMapper.selectList(any())).thenReturn(List.of());

        Page<OrderVO> result = orderService.sellerList(1, 10, null);

        assertThat(result.getTotal()).isZero();
        assertThat(result.getRecords()).isEmpty();
        verifyNoInteractions(orderItemMapper, orderMapper);
    }

    private OrderService orderService() {
        return new OrderService(orderMapper, orderItemMapper, cartItemMapper,
                productMapper, addressMapper, paymentRecordMapper);
    }

    private void loginAsUser(Long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionKeys.CURRENT_USER, new CurrentUser(userId, "seller", "seller", "USER"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private Product product(Long id, Long sellerId) {
        Product product = new Product();
        product.setId(id);
        product.setSellerId(sellerId);
        return product;
    }

    private Order order(Long id, String orderNo) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNo(orderNo);
        order.setStatus("PAID");
        order.setTotalAmount(new BigDecimal("220.00"));
        order.setReceiverSnapshot("user 13800000000 campus");
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    private OrderItem orderItem(Long orderId, Long productId, String name, String price, int quantity) {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setProductName(name);
        item.setProductImage("/images/item.jpg");
        item.setPrice(new BigDecimal(price));
        item.setQuantity(quantity);
        item.setSubtotal(new BigDecimal(price).multiply(BigDecimal.valueOf(quantity)));
        return item;
    }
}
