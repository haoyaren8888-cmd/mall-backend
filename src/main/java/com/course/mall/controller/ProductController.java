package com.course.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.CurrentUser;
import com.course.mall.common.Result;
import com.course.mall.common.SessionContext;
import com.course.mall.dto.ProductRequest;
import com.course.mall.entity.Product;
import com.course.mall.service.ProductService;
import jakarta.validation.Valid;
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

    public ProductController(ProductService productService) {
        this.productService = productService;
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

    @PostMapping
    public Result<Product> publish(@Valid @RequestBody ProductRequest request) {
        CurrentUser currentUser = SessionContext.requireUser();
        return Result.ok(productService.publish(currentUser, request));
    }

    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id, false));
    }

    @PutMapping("/{id}/off-shelf")
    public Result<Void> offShelf(@PathVariable Long id) {
        CurrentUser currentUser = SessionContext.requireUser();
        productService.offShelfMine(currentUser, id);
        return Result.ok();
    }
}
