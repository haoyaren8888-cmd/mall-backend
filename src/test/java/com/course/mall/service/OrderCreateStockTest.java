package com.course.mall.service;

import com.course.mall.common.CurrentUser;
import com.course.mall.common.SessionKeys;
import com.course.mall.dto.OrderCreateRequest;
import com.course.mall.entity.Address;
import com.course.mall.entity.CartItem;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCreateStockTest {
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
    void createDeductsStockAndClearsCheckedCartItems() {
        loginAsUser(10L);
        OrderService orderService = new OrderService(
                orderMapper, orderItemMapper, cartItemMapper, productMapper, addressMapper, paymentRecordMapper);

        Address address = address(20L, 10L);
        CartItem cartItem = cartItem(11L, 30L, 2);
        Product product = product(30L, 5, 2);
        AtomicReference<Order> savedOrder = new AtomicReference<>();
        List<OrderItem> savedItems = new ArrayList<>();

        when(addressMapper.selectById(20L)).thenReturn(address);
        when(cartItemMapper.selectList(any())).thenReturn(List.of(cartItem));
        when(productMapper.selectById(30L)).thenReturn(product);
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(99L);
            savedOrder.set(order);
            return 1;
        }).when(orderMapper).insert(any(Order.class));
        doAnswer(invocation -> {
            savedItems.add(invocation.getArgument(0));
            return 1;
        }).when(orderItemMapper).insert(any(OrderItem.class));
        when(orderMapper.selectOne(any())).thenAnswer(invocation -> savedOrder.get());
        when(orderItemMapper.selectList(any())).thenAnswer(invocation -> savedItems);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(20L);
        OrderVO order = orderService.create(request);

        assertThat(order.getStatus()).isEqualTo("PENDING_PAY");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("199.80");
        assertThat(product.getStock()).isEqualTo(3);
        assertThat(product.getSales()).isEqualTo(4);
        assertThat(savedItems).hasSize(1);
        assertThat(savedItems.get(0).getSubtotal()).isEqualByComparingTo("199.80");
        verify(productMapper).updateById(product);
        verify(cartItemMapper).deleteBatchIds(List.of(11L));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).updateById(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getTotalAmount()).isEqualByComparingTo("199.80");
    }

    private void loginAsUser(Long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionKeys.CURRENT_USER, new CurrentUser(userId, "user", "normal user", "USER"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private Address address(Long id, Long userId) {
        Address address = new Address();
        address.setId(id);
        address.setUserId(userId);
        address.setReceiverName("user");
        address.setReceiverPhone("13800000000");
        address.setProvince("广东省");
        address.setCity("广州市");
        address.setDistrict("天河区");
        address.setDetail("体育西路 1 号");
        return address;
    }

    private CartItem cartItem(Long id, Long productId, Integer quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setId(id);
        cartItem.setProductId(productId);
        cartItem.setQuantity(quantity);
        cartItem.setChecked(true);
        return cartItem;
    }

    private Product product(Long id, Integer stock, Integer sales) {
        Product product = new Product();
        product.setId(id);
        product.setName("无线鼠标");
        product.setCoverImage("/images/mouse.jpg");
        product.setPrice(new BigDecimal("99.90"));
        product.setStock(stock);
        product.setSales(sales);
        product.setStatus("ON");
        return product;
    }
}
