package com.course.mall.service;

import com.course.mall.common.CurrentUser;
import com.course.mall.common.SessionKeys;
import com.course.mall.entity.Order;
import com.course.mall.entity.OrderItem;
import com.course.mall.entity.PaymentRecord;
import com.course.mall.entity.Product;
import com.course.mall.mapper.AddressMapper;
import com.course.mall.mapper.CartItemMapper;
import com.course.mall.mapper.OrderItemMapper;
import com.course.mall.mapper.OrderMapper;
import com.course.mall.mapper.PaymentRecordMapper;
import com.course.mall.mapper.ProductMapper;
import com.course.mall.vo.OrderVO;
import com.course.mall.vo.PaymentRecordVO;
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

    @Test
    void finishChangesShippedOrderToFinishedAndMarksSoldProduct() {
        loginAsUser(10L);
        OrderService orderService = new OrderService(
                orderMapper, orderItemMapper, cartItemMapper, productMapper, addressMapper, paymentRecordMapper);
        Order order = shippedOrder();
        OrderItem item = new OrderItem();
        item.setProductId(88L);
        Product product = new Product();
        product.setId(88L);
        product.setStock(0);
        product.setStatus("ON");
        product.setItemStatus("ON_SALE");

        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderItemMapper.selectList(any())).thenReturn(List.of(item));
        when(productMapper.selectById(88L)).thenReturn(product);

        OrderVO result = orderService.finish("M202606020002");

        assertThat(result.getStatus()).isEqualTo("FINISHED");
        assertThat(order.getStatus()).isEqualTo("FINISHED");
        assertThat(product.getStatus()).isEqualTo("OFF");
        assertThat(product.getItemStatus()).isEqualTo("SOLD");
        verify(orderMapper).updateById(order);
        verify(productMapper).updateById(product);
    }

    @Test
    void paymentRecordOnlyReturnsCurrentUserOrderPayment() {
        loginAsUser(10L);
        OrderService orderService = new OrderService(
                orderMapper, orderItemMapper, cartItemMapper, productMapper, addressMapper, paymentRecordMapper);
        Order order = paidOrder();
        PaymentRecord record = new PaymentRecord();
        record.setOrderNo("M202606020003");
        record.setPayNo("PAY202606020003");
        record.setPayType("MOCK");
        record.setStatus("SUCCESS");
        record.setAmount(new BigDecimal("199.80"));

        when(orderMapper.selectOne(any())).thenReturn(order);
        when(paymentRecordMapper.selectOne(any())).thenReturn(record);

        PaymentRecordVO result = orderService.paymentRecord("M202606020003");

        assertThat(result.getOrderNo()).isEqualTo("M202606020003");
        assertThat(result.getPayNo()).isEqualTo("PAY202606020003");
        assertThat(result.getPayType()).isEqualTo("MOCK");
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getAmount()).isEqualByComparingTo("199.80");
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

    private Order shippedOrder() {
        Order order = new Order();
        order.setId(100L);
        order.setUserId(10L);
        order.setOrderNo("M202606020002");
        order.setStatus("SHIPPED");
        order.setTotalAmount(new BigDecimal("199.80"));
        return order;
    }

    private Order paidOrder() {
        Order order = new Order();
        order.setId(101L);
        order.setUserId(10L);
        order.setOrderNo("M202606020003");
        order.setStatus("PAID");
        order.setTotalAmount(new BigDecimal("199.80"));
        return order;
    }
}
