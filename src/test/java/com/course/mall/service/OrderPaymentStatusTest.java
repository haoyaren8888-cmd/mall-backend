package com.course.mall.service;

import com.course.mall.common.CurrentUser;
import com.course.mall.common.SessionKeys;
import com.course.mall.entity.Order;
import com.course.mall.entity.PaymentRecord;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPaymentStatusTest {
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
    void mockPayChangesPendingOrderToPaidAndCreatesPaymentRecord() {
        loginAsUser(10L);
        OrderService orderService = new OrderService(
                orderMapper, orderItemMapper, cartItemMapper, productMapper, addressMapper, paymentRecordMapper);
        Order order = pendingOrder();

        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderItemMapper.selectList(any())).thenReturn(List.of());

        OrderVO result = orderService.mockPay("M202606020001");

        assertThat(result.getStatus()).isEqualTo("PAID");
        assertThat(order.getStatus()).isEqualTo("PAID");
        assertThat(order.getPaidAt()).isNotNull();
        verify(orderMapper).updateById(order);

        ArgumentCaptor<PaymentRecord> recordCaptor = ArgumentCaptor.forClass(PaymentRecord.class);
        verify(paymentRecordMapper).insert(recordCaptor.capture());
        PaymentRecord record = recordCaptor.getValue();
        assertThat(record.getOrderNo()).isEqualTo("M202606020001");
        assertThat(record.getAmount()).isEqualByComparingTo("199.80");
        assertThat(record.getPayType()).isEqualTo("MOCK");
        assertThat(record.getStatus()).isEqualTo("SUCCESS");
        assertThat(record.getPaidAt()).isNotNull();
    }

    private void loginAsUser(Long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionKeys.CURRENT_USER, new CurrentUser(userId, "user", "normal user", "USER"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private Order pendingOrder() {
        Order order = new Order();
        order.setId(99L);
        order.setUserId(10L);
        order.setOrderNo("M202606020001");
        order.setStatus("PENDING_PAY");
        order.setTotalAmount(new BigDecimal("199.80"));
        return order;
    }
}
