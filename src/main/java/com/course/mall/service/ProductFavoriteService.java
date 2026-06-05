package com.course.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.BusinessException;
import com.course.mall.common.CurrentUser;
import com.course.mall.entity.Product;
import com.course.mall.entity.ProductFavorite;
import com.course.mall.mapper.ProductFavoriteMapper;
import com.course.mall.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductFavoriteService {
    private final ProductFavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    public ProductFavoriteService(ProductFavoriteMapper favoriteMapper, ProductMapper productMapper) {
        this.favoriteMapper = favoriteMapper;
        this.productMapper = productMapper;
    }

    public Page<Product> pageFavorites(CurrentUser currentUser, long page, long size) {
        Page<ProductFavorite> favoritePage = favoriteMapper.selectPage(Page.of(page, size),
                new LambdaQueryWrapper<ProductFavorite>()
                        .eq(ProductFavorite::getUserId, currentUser.getId())
                        .orderByDesc(ProductFavorite::getCreatedAt));

        List<Product> products = favoritePage.getRecords().stream()
                .map(favorite -> productMapper.selectById(favorite.getProductId()))
                .filter(product -> product != null && canShowPublic(product))
                .toList();

        Page<Product> productPage = Page.of(page, size, favoritePage.getTotal());
        productPage.setRecords(products);
        return productPage;
    }

    public boolean isFavorite(CurrentUser currentUser, Long productId) {
        return favoriteMapper.selectCount(new LambdaQueryWrapper<ProductFavorite>()
                .eq(ProductFavorite::getUserId, currentUser.getId())
                .eq(ProductFavorite::getProductId, productId)) > 0;
    }

    @Transactional
    public void favorite(CurrentUser currentUser, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || !canShowPublic(product)) {
            throw BusinessException.notFound("闲置商品不存在或暂未通过审核");
        }
        if (isFavorite(currentUser, productId)) {
            return;
        }

        ProductFavorite favorite = new ProductFavorite();
        favorite.setUserId(currentUser.getId());
        favorite.setProductId(productId);
        favoriteMapper.insert(favorite);
        syncFavoriteCount(productId);
    }

    @Transactional
    public void cancel(CurrentUser currentUser, Long productId) {
        favoriteMapper.delete(new LambdaQueryWrapper<ProductFavorite>()
                .eq(ProductFavorite::getUserId, currentUser.getId())
                .eq(ProductFavorite::getProductId, productId));
        syncFavoriteCount(productId);
    }

    private void syncFavoriteCount(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return;
        }
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<ProductFavorite>()
                .eq(ProductFavorite::getProductId, productId));
        product.setFavoriteCount(count.intValue());
        productMapper.updateById(product);
    }

    private boolean canShowPublic(Product product) {
        return "ON".equals(product.getStatus()) &&
                "APPROVED".equals(product.getAuditStatus()) &&
                "ON_SALE".equals(product.getItemStatus());
    }
}
