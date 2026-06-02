package com.course.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.BusinessException;
import com.course.mall.dto.ProductRequest;
import com.course.mall.entity.Product;
import com.course.mall.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductService {
    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public Page<Product> pageProducts(long page, long size, Long categoryId, String keyword, String status,
                                      String sort, Boolean admin) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(!Boolean.TRUE.equals(admin), Product::getStatus, "ON")
                .eq(Boolean.TRUE.equals(admin) && StringUtils.hasText(status), Product::getStatus, status)
                .eq(categoryId != null, Product::getCategoryId, categoryId)
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
            default -> wrapper.orderByDesc(Product::getCreatedAt);
        }
        wrapper.orderByDesc(Product::getCreatedAt);
    }

    public Product detail(Long id, boolean admin) {
        Product product = productMapper.selectById(id);
        if (product == null || (!admin && !"ON".equals(product.getStatus()))) {
            throw BusinessException.notFound("商品不存在或已下架");
        }
        return product;
    }

    public Product create(ProductRequest request) {
        Product product = new Product();
        fill(product, request);
        product.setSales(0);
        productMapper.insert(product);
        return product;
    }

    public Product update(Long id, ProductRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw BusinessException.notFound("商品不存在");
        }
        fill(product, request);
        productMapper.updateById(product);
        return product;
    }

    public void changeStatus(Long id, String status) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw BusinessException.notFound("商品不存在");
        }
        product.setStatus(status);
        productMapper.updateById(product);
    }

    public void delete(Long id) {
        productMapper.deleteById(id);
    }

    private void fill(Product product, ProductRequest request) {
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCoverImage(request.getCoverImage());
        product.setStatus(request.getStatus() == null ? "ON" : request.getStatus());
    }
}
