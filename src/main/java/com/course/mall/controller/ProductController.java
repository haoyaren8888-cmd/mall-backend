package com.course.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.Result;
import com.course.mall.entity.Product;
import com.course.mall.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
                                      @RequestParam(required = false) String keyword) {
        return Result.ok(productService.pageProducts(page, size, categoryId, keyword, false));
    }

    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id, false));
    }
}
