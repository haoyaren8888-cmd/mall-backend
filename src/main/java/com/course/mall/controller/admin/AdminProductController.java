package com.course.mall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.Result;
import com.course.mall.dto.ProductRequest;
import com.course.mall.entity.Product;
import com.course.mall.service.ProductService;
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

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Result<Page<Product>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) Long categoryId,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) String sort) {
        return Result.ok(productService.pageProducts(page, size, categoryId, keyword, status, sort, true));
    }

    @PostMapping
    public Result<Product> create(@Valid @RequestBody ProductRequest request) {
        return Result.ok(productService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return Result.ok(productService.update(id, request));
    }

    @PutMapping("/{id}/on")
    public Result<Void> on(@PathVariable Long id) {
        productService.changeStatus(id, "ON");
        return Result.ok();
    }

    @PutMapping("/{id}/off")
    public Result<Void> off(@PathVariable Long id) {
        productService.changeStatus(id, "OFF");
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok();
    }
}
