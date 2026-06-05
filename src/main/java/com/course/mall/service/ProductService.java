package com.course.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.BusinessException;
import com.course.mall.common.CurrentUser;
import com.course.mall.dto.ProductRequest;
import com.course.mall.entity.Product;
import com.course.mall.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class ProductService {
    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public Page<Product> pageProducts(long page, long size, Long categoryId, String keyword, String status,
                                      String sort, Boolean admin, String campus, String conditionLevel,
                                      BigDecimal minPrice, BigDecimal maxPrice, String auditStatus,
                                      String itemStatus) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(!Boolean.TRUE.equals(admin), Product::getStatus, "ON")
                .eq(!Boolean.TRUE.equals(admin), Product::getAuditStatus, "APPROVED")
                .eq(!Boolean.TRUE.equals(admin), Product::getItemStatus, "ON_SALE")
                .eq(Boolean.TRUE.equals(admin) && StringUtils.hasText(status), Product::getStatus, status)
                .eq(Boolean.TRUE.equals(admin) && StringUtils.hasText(auditStatus), Product::getAuditStatus, auditStatus)
                .eq(Boolean.TRUE.equals(admin) && StringUtils.hasText(itemStatus), Product::getItemStatus, itemStatus)
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .eq(StringUtils.hasText(campus), Product::getCampus, campus)
                .eq(StringUtils.hasText(conditionLevel), Product::getConditionLevel, conditionLevel)
                .ge(minPrice != null, Product::getPrice, minPrice)
                .le(maxPrice != null, Product::getPrice, maxPrice)
                .like(StringUtils.hasText(keyword), Product::getName, keyword);
        applySort(wrapper, sort);
        return productMapper.selectPage(Page.of(page, size), wrapper);
    }

    private void applySort(LambdaQueryWrapper<Product> wrapper, String sort) {
        if (!StringUtils.hasText(sort) || "newest".equals(sort)) {
            wrapper.orderByDesc(Product::getCreatedAt);
            return;
        }
        switch (sort) {
            case "priceAsc" -> wrapper.orderByAsc(Product::getPrice);
            case "priceDesc" -> wrapper.orderByDesc(Product::getPrice);
            case "salesDesc" -> wrapper.orderByDesc(Product::getSales);
            case "stockAsc" -> wrapper.orderByAsc(Product::getStock);
            case "stockDesc" -> wrapper.orderByDesc(Product::getStock);
            case "viewsDesc" -> wrapper.orderByDesc(Product::getViewCount);
            default -> wrapper.orderByDesc(Product::getCreatedAt);
        }
        wrapper.orderByDesc(Product::getCreatedAt);
    }

    public Product detail(Long id, boolean admin) {
        Product product = productMapper.selectById(id);
        if (product == null || (!admin && !canShowPublic(product))) {
            throw BusinessException.notFound("闲置商品不存在或暂未通过审核");
        }
        product.setViewCount((product.getViewCount() == null ? 0 : product.getViewCount()) + 1);
        productMapper.updateById(product);
        return product;
    }

    public Product create(ProductRequest request) {
        Product product = new Product();
        fill(product, request);
        product.setSales(0);
        product.setViewCount(0);
        product.setFavoriteCount(0);
        productMapper.insert(product);
        return product;
    }

    public Product publish(CurrentUser currentUser, ProductRequest request) {
        Product product = new Product();
        fill(product, request);
        product.setSellerId(currentUser.getId());
        product.setStock(request.getStock() == null ? 1 : request.getStock());
        product.setSales(0);
        product.setStatus("ON");
        product.setAuditStatus("PENDING");
        product.setItemStatus("ON_SALE");
        product.setViewCount(0);
        product.setFavoriteCount(0);
        productMapper.insert(product);
        return product;
    }

    public Page<Product> pageMine(CurrentUser currentUser, long page, long size, String itemStatus) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getSellerId, currentUser.getId())
                .eq(StringUtils.hasText(itemStatus), Product::getItemStatus, itemStatus)
                .orderByDesc(Product::getCreatedAt);
        return productMapper.selectPage(Page.of(page, size), wrapper);
    }

    public Product detailMine(CurrentUser currentUser, Long id) {
        Product product = requireProduct(id);
        if (!currentUser.getId().equals(product.getSellerId())) {
            throw BusinessException.forbidden("只能查看自己发布的闲置商品");
        }
        return product;
    }

    public Product updateMine(CurrentUser currentUser, Long id, ProductRequest request) {
        Product product = requireProduct(id);
        if (!currentUser.getId().equals(product.getSellerId())) {
            throw BusinessException.forbidden("只能修改自己发布的闲置商品");
        }
        if ("SOLD".equals(product.getItemStatus())) {
            throw BusinessException.badRequest("已成交商品不能修改");
        }
        fill(product, request);
        product.setSellerId(currentUser.getId());
        product.setAuditStatus("PENDING");
        product.setRejectReason(null);
        product.setStatus("ON");
        product.setItemStatus("ON_SALE");
        productMapper.updateById(product);
        return product;
    }

    public Product update(Long id, ProductRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw BusinessException.notFound("闲置商品不存在");
        }
        fill(product, request);
        productMapper.updateById(product);
        return product;
    }

    public void changeStatus(Long id, String status) {
        Product product = requireProduct(id);
        product.setStatus(status);
        if ("OFF".equals(status)) {
            product.setItemStatus("OFF_SHELF");
        }
        productMapper.updateById(product);
    }

    public void approve(Long id) {
        Product product = requireProduct(id);
        product.setAuditStatus("APPROVED");
        product.setRejectReason(null);
        product.setStatus("ON");
        if (!StringUtils.hasText(product.getItemStatus())) {
            product.setItemStatus("ON_SALE");
        }
        productMapper.updateById(product);
    }

    public void reject(Long id, String reason) {
        Product product = requireProduct(id);
        product.setAuditStatus("REJECTED");
        product.setRejectReason(StringUtils.hasText(reason) ? reason : "信息不完整，请修改后重新发布");
        product.setStatus("OFF");
        product.setItemStatus("OFF_SHELF");
        productMapper.updateById(product);
    }

    public void offShelfMine(CurrentUser currentUser, Long id) {
        Product product = requireProduct(id);
        if (!currentUser.getId().equals(product.getSellerId())) {
            throw BusinessException.forbidden("只能下架自己发布的闲置");
        }
        product.setStatus("OFF");
        product.setItemStatus("OFF_SHELF");
        productMapper.updateById(product);
    }

    public void delete(Long id) {
        productMapper.deleteById(id);
    }

    private void fill(Product product, ProductRequest request) {
        validatePrice(request.getPrice());
        product.setCategoryId(request.getCategoryId());
        if (request.getSellerId() != null) {
            product.setSellerId(request.getSellerId());
        }
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStock(request.getStock() == null ? 1 : request.getStock());
        product.setCoverImage(request.getCoverImage());
        product.setStatus(defaultText(request.getStatus(), "ON"));
        product.setConditionLevel(defaultText(request.getConditionLevel(), "九成新"));
        product.setCampus(defaultText(request.getCampus(), "明向校区"));
        product.setTradePlace(defaultText(request.getTradePlace(), "图书馆门口"));
        product.setTradeType(defaultText(request.getTradeType(), "线下面交"));
        product.setAuditStatus(defaultText(request.getAuditStatus(), "APPROVED"));
        product.setItemStatus(defaultText(request.getItemStatus(), "ON_SALE"));
        product.setRejectReason(request.getRejectReason());
    }

    private boolean canShowPublic(Product product) {
        return "ON".equals(product.getStatus()) &&
                "APPROVED".equals(product.getAuditStatus()) &&
                "ON_SALE".equals(product.getItemStatus());
    }

    private Product requireProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw BusinessException.notFound("闲置商品不存在");
        }
        return product;
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("转让价不能小于 0");
        }
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
