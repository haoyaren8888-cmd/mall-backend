package com.course.mall.controller;

import com.course.mall.common.Result;
import com.course.mall.dto.CartItemRequest;
import com.course.mall.dto.CartMergeRequest;
import com.course.mall.dto.CartUpdateRequest;
import com.course.mall.service.CartService;
import com.course.mall.vo.CartItemVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Result<List<CartItemVO>> list() {
        return Result.ok(cartService.list());
    }

    @PostMapping("/items")
    public Result<Void> add(@Valid @RequestBody CartItemRequest request) {
        cartService.add(request);
        return Result.ok();
    }

    @PutMapping("/items/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CartUpdateRequest request) {
        cartService.update(id, request);
        return Result.ok();
    }

    @DeleteMapping("/items/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cartService.delete(id);
        return Result.ok();
    }

    @PostMapping("/merge")
    public Result<List<CartItemVO>> merge(@RequestBody CartMergeRequest request) {
        return Result.ok(cartService.merge(request));
    }
}
