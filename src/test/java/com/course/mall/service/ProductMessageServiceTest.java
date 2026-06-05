package com.course.mall.service;

import com.course.mall.common.BusinessException;
import com.course.mall.common.CurrentUser;
import com.course.mall.dto.ProductMessageReplyRequest;
import com.course.mall.entity.Product;
import com.course.mall.entity.ProductMessage;
import com.course.mall.entity.User;
import com.course.mall.mapper.ProductMapper;
import com.course.mall.mapper.ProductMessageMapper;
import com.course.mall.mapper.UserMapper;
import com.course.mall.vo.ProductMessageVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductMessageServiceTest {
    @Mock
    private ProductMessageMapper messageMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private UserMapper userMapper;

    @Test
    void sellerCanReplyMessage() {
        ProductMessageService service = productMessageService();
        when(productMapper.selectById(20L)).thenReturn(product(20L, 2L));
        when(messageMapper.selectOne(any())).thenReturn(message());
        when(userMapper.selectById(3L)).thenReturn(user());

        ProductMessageVO result = service.reply(seller(), 20L, 66L, replyRequest());

        assertThat(result.getReplyContent()).isEqualTo("可以，充电器和包装盒都在。");
        assertThat(result.getNickname()).isEqualTo("赵奎");
        assertThat(result.getReplyAt()).isNotNull();

        ArgumentCaptor<ProductMessage> captor = ArgumentCaptor.forClass(ProductMessage.class);
        verify(messageMapper).updateById(captor.capture());
        assertThat(captor.getValue().getReplyContent()).isEqualTo("可以，充电器和包装盒都在。");
        assertThat(captor.getValue().getReplyAt()).isNotNull();
    }

    @Test
    void replyRequiresProductSeller() {
        ProductMessageService service = productMessageService();
        when(productMapper.selectById(20L)).thenReturn(product(20L, 9L));

        assertThatThrownBy(() -> service.reply(seller(), 20L, 66L, replyRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("只有卖家可以回复留言");
        verify(messageMapper, never()).updateById(any(ProductMessage.class));
    }

    @Test
    void replyRequiresContent() {
        ProductMessageService service = productMessageService();
        ProductMessageReplyRequest request = new ProductMessageReplyRequest();
        request.setReplyContent("   ");

        when(productMapper.selectById(20L)).thenReturn(product(20L, 2L));
        when(messageMapper.selectOne(any())).thenReturn(message());

        assertThatThrownBy(() -> service.reply(seller(), 20L, 66L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("回复内容不能为空");
        verify(messageMapper, never()).updateById(any(ProductMessage.class));
    }

    private ProductMessageService productMessageService() {
        return new ProductMessageService(messageMapper, productMapper, userMapper);
    }

    private CurrentUser seller() {
        return new CurrentUser(2L, "seller", "卖家", "USER");
    }

    private ProductMessageReplyRequest replyRequest() {
        ProductMessageReplyRequest request = new ProductMessageReplyRequest();
        request.setReplyContent(" 可以，充电器和包装盒都在。 ");
        return request;
    }

    private Product product(Long id, Long sellerId) {
        Product product = new Product();
        product.setId(id);
        product.setSellerId(sellerId);
        product.setStatus("ON");
        product.setAuditStatus("APPROVED");
        product.setItemStatus("ON_SALE");
        return product;
    }

    private ProductMessage message() {
        ProductMessage message = new ProductMessage();
        message.setId(66L);
        message.setProductId(20L);
        message.setUserId(3L);
        message.setContent("请问配件还在吗？");
        message.setStatus("ON");
        return message;
    }

    private User user() {
        User user = new User();
        user.setId(3L);
        user.setNickname("赵奎");
        user.setCampus("明向校区");
        return user;
    }
}
