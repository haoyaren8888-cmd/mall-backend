package com.course.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.CurrentUser;
import com.course.mall.common.Result;
import com.course.mall.common.SessionContext;
import com.course.mall.dto.ProductMessageRequest;
import com.course.mall.dto.ProductRequest;
import com.course.mall.dto.ProductReviewRequest;
import com.course.mall.entity.Product;
import com.course.mall.service.ProductFavoriteService;
import com.course.mall.service.ProductMessageService;
import com.course.mall.service.ProductReviewService;
import com.course.mall.service.ProductService;
import com.course.mall.vo.ProductMessageVO;
import com.course.mall.vo.ProductReviewVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final ProductFavoriteService favoriteService;
    private final ProductMessageService messageService;
    private final ProductReviewService reviewService;

    public ProductController(ProductService productService, ProductFavoriteService favoriteService,
                             ProductMessageService messageService, ProductReviewService reviewService) {
        this.productService = productService;
        this.favoriteService = favoriteService;
        this.messageService = messageService;
        this.reviewService = reviewService;
    }

    @GetMapping
    public Result<Page<Product>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "8") long size,
                                      @RequestParam(required = false) Long categoryId,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String sort,
                                      @RequestParam(required = false) String campus,
                                      @RequestParam(required = false) String conditionLevel,
                                      @RequestParam(required = false) BigDecimal minPrice,
                                      @RequestParam(required = false) BigDecimal maxPrice) {
        return Result.ok(productService.pageProducts(page, size, categoryId, keyword, null, sort, false,
                campus, conditionLevel, minPrice, maxPrice, null, null));
    }

    @GetMapping("/mine")
    public Result<Page<Product>> mine(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "8") long size,
                                      @RequestParam(required = false) String itemStatus) {
        CurrentUser currentUser = SessionContext.requireUser();
        return Result.ok(productService.pageMine(currentUser, page, size, itemStatus));
    }

    @GetMapping("/favorites")
    public Result<Page<Product>> favorites(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "8") long size) {
        CurrentUser currentUser = SessionContext.requireUser();
        return Result.ok(favoriteService.pageFavorites(currentUser, page, size));
    }

    @GetMapping("/mine/{id}")
    public Result<Product> mineDetail(@PathVariable Long id) {
        CurrentUser currentUser = SessionContext.requireUser();
        return Result.ok(productService.detailMine(currentUser, id));
    }

    @PostMapping
    public Result<Product> publish(@Valid @RequestBody ProductRequest request) {
        CurrentUser currentUser = SessionContext.requireUser();
        return Result.ok(productService.publish(currentUser, request));
    }

    @PutMapping("/{id}")
    public Result<Product> updateMine(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        CurrentUser currentUser = SessionContext.requireUser();
        return Result.ok(productService.updateMine(currentUser, id, request));
    }

    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id, false));
    }

    @GetMapping("/{id}/messages")
    public Result<Page<ProductMessageVO>> messages(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "5") long size) {
        return Result.ok(messageService.pageMessages(id, page, size));
    }

    @PostMapping("/{id}/messages")
    public Result<ProductMessageVO> leaveMessage(@PathVariable Long id,
                                                 @Valid @RequestBody ProductMessageRequest request) {
        CurrentUser currentUser = SessionContext.requireUser();
        return Result.ok(messageService.create(currentUser, id, request));
    }

    @GetMapping("/{id}/reviews")
    public Result<Page<ProductReviewVO>> reviews(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "5") long size) {
        return Result.ok(reviewService.pageReviews(id, page, size));
    }

    @PostMapping("/{id}/reviews")
    public Result<ProductReviewVO> createReview(@PathVariable Long id,
                                                @Valid @RequestBody ProductReviewRequest request) {
        CurrentUser currentUser = SessionContext.requireUser();
        return Result.ok(reviewService.create(currentUser, id, request));
    }

    @GetMapping("/{id}/favorite")
    public Result<Boolean> favoriteStatus(@PathVariable Long id) {
        CurrentUser currentUser = SessionContext.requireUser();
        return Result.ok(favoriteService.isFavorite(currentUser, id));
    }

    @PostMapping("/{id}/favorite")
    public Result<Void> favorite(@PathVariable Long id) {
        CurrentUser currentUser = SessionContext.requireUser();
        favoriteService.favorite(currentUser, id);
        return Result.ok();
    }

    @DeleteMapping("/{id}/favorite")
    public Result<Void> cancelFavorite(@PathVariable Long id) {
        CurrentUser currentUser = SessionContext.requireUser();
        favoriteService.cancel(currentUser, id);
        return Result.ok();
    }

    @PutMapping("/{id}/off-shelf")
    public Result<Void> offShelf(@PathVariable Long id) {
        CurrentUser currentUser = SessionContext.requireUser();
        productService.offShelfMine(currentUser, id);
        return Result.ok();
    }
}
