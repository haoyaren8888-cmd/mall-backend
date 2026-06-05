package com.course.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.BusinessException;
import com.course.mall.common.CurrentUser;
import com.course.mall.dto.ProductMessageRequest;
import com.course.mall.dto.ProductMessageReplyRequest;
import com.course.mall.entity.Product;
import com.course.mall.entity.ProductMessage;
import com.course.mall.entity.User;
import com.course.mall.mapper.ProductMapper;
import com.course.mall.mapper.ProductMessageMapper;
import com.course.mall.mapper.UserMapper;
import com.course.mall.vo.ProductMessageVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class ProductMessageService {
    private final ProductMessageMapper messageMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    public ProductMessageService(ProductMessageMapper messageMapper, ProductMapper productMapper, UserMapper userMapper) {
        this.messageMapper = messageMapper;
        this.productMapper = productMapper;
        this.userMapper = userMapper;
    }

    public Page<ProductMessageVO> pageMessages(Long productId, long page, long size) {
        requirePublicProduct(productId);
        Page<ProductMessage> messagePage = messageMapper.selectPage(Page.of(page, size),
                new LambdaQueryWrapper<ProductMessage>()
                        .eq(ProductMessage::getProductId, productId)
                        .eq(ProductMessage::getStatus, "ON")
                        .orderByDesc(ProductMessage::getCreatedAt));

        Page<ProductMessageVO> voPage = Page.of(page, size, messagePage.getTotal());
        voPage.setRecords(messagePage.getRecords().stream()
                .map(message -> {
                    User user = userMapper.selectById(message.getUserId());
                    return ProductMessageVO.from(message, user);
                })
                .toList());
        return voPage;
    }

    public ProductMessageVO create(CurrentUser currentUser, Long productId, ProductMessageRequest request) {
        requirePublicProduct(productId);
        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (!StringUtils.hasText(content)) {
            throw BusinessException.badRequest("留言内容不能为空");
        }

        ProductMessage message = new ProductMessage();
        message.setProductId(productId);
        message.setUserId(currentUser.getId());
        message.setContent(content);
        message.setStatus("ON");
        messageMapper.insert(message);

        User user = userMapper.selectById(currentUser.getId());
        return ProductMessageVO.from(message, user);
    }

    public ProductMessageVO reply(CurrentUser currentUser, Long productId, Long messageId,
                                  ProductMessageReplyRequest request) {
        Product product = requirePublicProduct(productId);
        if (!currentUser.getId().equals(product.getSellerId())) {
            throw BusinessException.forbidden("只有卖家可以回复留言");
        }

        ProductMessage message = messageMapper.selectOne(new LambdaQueryWrapper<ProductMessage>()
                .eq(ProductMessage::getId, messageId)
                .eq(ProductMessage::getProductId, productId)
                .eq(ProductMessage::getStatus, "ON"));
        if (message == null) {
            throw BusinessException.notFound("留言不存在");
        }

        String content = request.getReplyContent() == null ? "" : request.getReplyContent().trim();
        if (!StringUtils.hasText(content)) {
            throw BusinessException.badRequest("回复内容不能为空");
        }

        message.setReplyContent(content);
        message.setReplyAt(LocalDateTime.now());
        messageMapper.updateById(message);

        User user = userMapper.selectById(message.getUserId());
        return ProductMessageVO.from(message, user);
    }

    private Product requirePublicProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || !"ON".equals(product.getStatus()) ||
                !"APPROVED".equals(product.getAuditStatus()) ||
                !"ON_SALE".equals(product.getItemStatus())) {
            throw BusinessException.notFound("闲置商品不存在或暂未通过审核");
        }
        return product;
    }
}
