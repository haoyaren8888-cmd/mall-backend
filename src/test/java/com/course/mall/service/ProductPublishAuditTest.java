package com.course.mall.service;

import com.course.mall.common.BusinessException;
import com.course.mall.common.CurrentUser;
import com.course.mall.dto.ProductRequest;
import com.course.mall.entity.Product;
import com.course.mall.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductPublishAuditTest {
    @Mock
    private ProductMapper productMapper;

    @Test
    void publishUsesCurrentUserAndWaitsForAudit() {
        ProductService productService = new ProductService(productMapper);
        ProductRequest request = idleRequest();
        request.setSellerId(99L);
        request.setStock(null);

        Product product = productService.publish(new CurrentUser(8L, "student", "student", "USER"), request);

        assertThat(product.getSellerId()).isEqualTo(8L);
        assertThat(product.getStock()).isEqualTo(1);
        assertThat(product.getStatus()).isEqualTo("ON");
        assertThat(product.getAuditStatus()).isEqualTo("PENDING");
        assertThat(product.getItemStatus()).isEqualTo("ON_SALE");
        assertThat(product.getViewCount()).isZero();
        assertThat(product.getFavoriteCount()).isZero();
        verify(productMapper).insert(product);
    }

    @Test
    void updateKeepsSellerWhenRequestOmitsSeller() {
        ProductService productService = new ProductService(productMapper);
        Product product = existingProduct();
        ProductRequest request = idleRequest();
        request.setSellerId(null);

        when(productMapper.selectById(5L)).thenReturn(product);

        Product updated = productService.update(5L, request);

        assertThat(updated.getSellerId()).isEqualTo(18L);
        assertThat(updated.getName()).isEqualTo("Java Web notes");
        assertThat(updated.getPrice()).isEqualByComparingTo("26.00");
        verify(productMapper).updateById(product);
    }

    @Test
    void sellerUpdateResubmitsProductForAudit() {
        ProductService productService = new ProductService(productMapper);
        Product product = existingProduct();
        product.setAuditStatus("REJECTED");
        product.setRejectReason("image is not clear");
        ProductRequest request = idleRequest();
        request.setSellerId(99L);

        when(productMapper.selectById(5L)).thenReturn(product);

        Product updated = productService.updateMine(new CurrentUser(18L, "seller", "seller", "USER"), 5L, request);

        assertThat(updated.getSellerId()).isEqualTo(18L);
        assertThat(updated.getName()).isEqualTo("Java Web notes");
        assertThat(updated.getAuditStatus()).isEqualTo("PENDING");
        assertThat(updated.getRejectReason()).isNull();
        assertThat(updated.getStatus()).isEqualTo("ON");
        assertThat(updated.getItemStatus()).isEqualTo("ON_SALE");
        verify(productMapper).updateById(product);
    }

    @Test
    void sellerUpdateRejectsOtherSellerProduct() {
        ProductService productService = new ProductService(productMapper);
        Product product = existingProduct();

        when(productMapper.selectById(5L)).thenReturn(product);

        assertThatThrownBy(() ->
                productService.updateMine(new CurrentUser(99L, "other", "other", "USER"), 5L, idleRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("只能修改自己发布的闲置商品");
    }

    @Test
    void approveClearsRejectReasonAndMakesProductVisible() {
        ProductService productService = new ProductService(productMapper);
        Product product = existingProduct();
        product.setAuditStatus("REJECTED");
        product.setStatus("OFF");
        product.setItemStatus(null);
        product.setRejectReason("description is not clear");

        when(productMapper.selectById(5L)).thenReturn(product);

        productService.approve(5L);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).updateById(productCaptor.capture());
        Product approved = productCaptor.getValue();
        assertThat(approved.getAuditStatus()).isEqualTo("APPROVED");
        assertThat(approved.getStatus()).isEqualTo("ON");
        assertThat(approved.getItemStatus()).isEqualTo("ON_SALE");
        assertThat(approved.getRejectReason()).isNull();
    }

    private ProductRequest idleRequest() {
        ProductRequest request = new ProductRequest();
        request.setCategoryId(22L);
        request.setName("Java Web notes");
        request.setDescription("course design notes");
        request.setPrice(new BigDecimal("26.00"));
        request.setOriginalPrice(new BigDecimal("59.00"));
        request.setConditionLevel("nine out of ten");
        request.setCampus("Mingxiang campus");
        request.setTradePlace("library gate");
        request.setTradeType("offline");
        return request;
    }

    private Product existingProduct() {
        Product product = new Product();
        product.setId(5L);
        product.setSellerId(18L);
        product.setCategoryId(11L);
        product.setName("old product");
        product.setPrice(new BigDecimal("30.00"));
        product.setStatus("ON");
        product.setAuditStatus("APPROVED");
        product.setItemStatus("ON_SALE");
        return product;
    }
}
