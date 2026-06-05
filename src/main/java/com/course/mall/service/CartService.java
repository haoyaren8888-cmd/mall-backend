package com.course.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.course.mall.common.BusinessException;
import com.course.mall.common.CurrentUser;
import com.course.mall.common.SessionContext;
import com.course.mall.dto.CartCheckedRequest;
import com.course.mall.dto.CartItemRequest;
import com.course.mall.dto.CartMergeRequest;
import com.course.mall.dto.CartUpdateRequest;
import com.course.mall.entity.CartItem;
import com.course.mall.entity.Product;
import com.course.mall.mapper.CartItemMapper;
import com.course.mall.mapper.ProductMapper;
import com.course.mall.vo.CartItemVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    public CartService(CartItemMapper cartItemMapper, ProductMapper productMapper) {
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
    }

    public List<CartItemVO> list() {
        Long userId = SessionContext.requireUser().getId();
        List<CartItem> items = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getUpdatedAt));
        if (items.isEmpty()) {
            return List.of();
        }
        List<Long> productIds = items.stream().map(CartItem::getProductId).toList();
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<CartItemVO> result = new ArrayList<>();
        for (CartItem item : items) {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                continue;
            }
            CartItemVO vo = new CartItemVO();
            vo.setId(item.getId());
            vo.setProductId(product.getId());
            vo.setProductName(product.getName());
            vo.setProductImage(product.getCoverImage());
            vo.setPrice(product.getPrice());
            vo.setStock(product.getStock());
            vo.setQuantity(item.getQuantity());
            vo.setChecked(item.getChecked());
            vo.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            result.add(vo);
        }
        return result;
    }

    public void add(CartItemRequest request) {
        CurrentUser user = SessionContext.requireUser();
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw BusinessException.badRequest("商品数量必须大于 0");
        }
        Product product = requireSaleProduct(request.getProductId());
        ensureNotOwnProduct(product, user.getId());
        CartItem existed = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, user.getId())
                .eq(CartItem::getProductId, request.getProductId()));
        int nextQuantity = request.getQuantity();
        if (existed != null) {
            nextQuantity += existed.getQuantity();
        }
        ensureStock(product, nextQuantity);
        if (existed == null) {
            CartItem item = new CartItem();
            item.setUserId(user.getId());
            item.setProductId(request.getProductId());
            item.setQuantity(request.getQuantity());
            item.setChecked(request.getChecked() == null || request.getChecked());
            cartItemMapper.insert(item);
        } else {
            existed.setQuantity(existed.getQuantity() + request.getQuantity());
            existed.setChecked(request.getChecked() == null || request.getChecked());
            cartItemMapper.updateById(existed);
        }
    }

    public void update(Long id, CartUpdateRequest request) {
        Long userId = SessionContext.requireUser().getId();
        CartItem item = requireOwnItem(id);
        Product product = null;
        if (request.getQuantity() != null) {
            if (request.getQuantity() <= 0) {
                throw BusinessException.badRequest("商品数量必须大于 0");
            }
            product = requireSaleProduct(item.getProductId());
            ensureNotOwnProduct(product, userId);
            ensureStock(product, request.getQuantity());
            item.setQuantity(request.getQuantity());
        }
        if (request.getChecked() != null) {
            if (request.getChecked()) {
                product = product == null ? requireSaleProduct(item.getProductId()) : product;
                ensureNotOwnProduct(product, userId);
                ensureStock(product, item.getQuantity());
            }
            item.setChecked(request.getChecked());
        }
        cartItemMapper.updateById(item);
    }

    public void checked(CartCheckedRequest request) {
        if (request.getChecked() == null) {
            throw BusinessException.badRequest("请选择意向清单勾选状态");
        }
        Long userId = SessionContext.requireUser().getId();
        List<Long> ids = request.getIds() == null ? List.of() : request.getIds();
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId);
        if (!ids.isEmpty()) {
            wrapper.in(CartItem::getId, ids);
        }
        List<CartItem> items = cartItemMapper.selectList(wrapper);
        if (!ids.isEmpty()) {
            Set<Long> uniqueIds = new HashSet<>(ids);
            if (items.size() != uniqueIds.size()) {
                throw BusinessException.notFound("意向清单商品不存在");
            }
        }
        if (request.getChecked()) {
            validateItemsStock(items, userId);
        }
        for (CartItem item : items) {
            item.setChecked(request.getChecked());
            cartItemMapper.updateById(item);
        }
    }

    public void delete(Long id) {
        requireOwnItem(id);
        cartItemMapper.deleteById(id);
    }

    public List<CartItemVO> merge(CartMergeRequest request) {
        if (request.getItems() != null) {
            for (CartItemRequest item : request.getItems()) {
                add(item);
            }
        }
        return list();
    }

    private CartItem requireOwnItem(Long id) {
        Long userId = SessionContext.requireUser().getId();
        CartItem item = cartItemMapper.selectById(id);
        if (item == null || !userId.equals(item.getUserId())) {
            throw BusinessException.notFound("意向清单商品不存在");
        }
        return item;
    }

    private Product requireSaleProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || !"ON".equals(product.getStatus())) {
            throw BusinessException.notFound("商品不存在或已下架");
        }
        return product;
    }

    private void validateItemsStock(List<CartItem> items, Long userId) {
        if (items.isEmpty()) {
            return;
        }
        List<Long> productIds = items.stream().map(CartItem::getProductId).distinct().toList();
        Map<Long, Product> products = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        for (CartItem item : items) {
            Product product = products.get(item.getProductId());
            if (product == null || !"ON".equals(product.getStatus())) {
                throw BusinessException.notFound("商品不存在或已下架");
            }
            ensureNotOwnProduct(product, userId);
            ensureStock(product, item.getQuantity());
        }
    }

    private void ensureNotOwnProduct(Product product, Long userId) {
        if (userId != null && userId.equals(product.getSellerId())) {
            throw BusinessException.badRequest("不能购买自己发布的闲置商品");
        }
    }

    private void ensureStock(Product product, int quantity) {
        int stock = product.getStock() == null ? 0 : product.getStock();
        if (quantity > stock) {
            throw BusinessException.badRequest(product.getName() + " 库存不足");
        }
    }
}
