package com.course.mall.service;

import com.course.mall.common.BusinessException;
import com.course.mall.common.CurrentUser;
import com.course.mall.common.SessionKeys;
import com.course.mall.dto.CartItemRequest;
import com.course.mall.entity.CartItem;
import com.course.mall.entity.Product;
import com.course.mall.mapper.CartItemMapper;
import com.course.mall.mapper.ProductMapper;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartSelfPurchaseTest {
    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private ProductMapper productMapper;

    @AfterEach
    void resetRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void addRejectsOwnProduct() {
        loginAsUser(10L);
        CartService cartService = new CartService(cartItemMapper, productMapper);
        CartItemRequest request = new CartItemRequest();
        request.setProductId(30L);
        request.setQuantity(1);

        when(productMapper.selectById(30L)).thenReturn(product(30L, 10L));

        assertThatThrownBy(() -> cartService.add(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("不能购买自己发布的闲置商品");
    }

    @Test
    void deleteCheckedRemovesSelectedItems() {
        loginAsUser(10L);
        CartService cartService = new CartService(cartItemMapper, productMapper);
        when(cartItemMapper.selectList(any())).thenReturn(List.of(cartItem(1L), cartItem(2L)));

        cartService.deleteChecked();

        verify(cartItemMapper).deleteBatchIds(List.of(1L, 2L));
    }

    @Test
    void deleteCheckedSkipsWhenNothingSelected() {
        loginAsUser(10L);
        CartService cartService = new CartService(cartItemMapper, productMapper);
        when(cartItemMapper.selectList(any())).thenReturn(List.of());

        cartService.deleteChecked();

        verify(cartItemMapper, never()).deleteBatchIds(any());
    }

    private void loginAsUser(Long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionKeys.CURRENT_USER, new CurrentUser(userId, "user", "normal user", "USER"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private CartItem cartItem(Long id) {
        CartItem cartItem = new CartItem();
        cartItem.setId(id);
        cartItem.setUserId(10L);
        cartItem.setChecked(true);
        return cartItem;
    }

    private Product product(Long id, Long sellerId) {
        Product product = new Product();
        product.setId(id);
        product.setSellerId(sellerId);
        product.setName("二手键盘");
        product.setPrice(new BigDecimal("59.00"));
        product.setStock(1);
        product.setStatus("ON");
        return product;
    }
}
