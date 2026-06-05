package com.course.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.Result;
import com.course.mall.dto.OrderCreateRequest;
import com.course.mall.service.OrderService;
import com.course.mall.vo.OrderVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateRequest request) {
        return Result.ok(orderService.create(request));
    }

    @GetMapping
    public Result<Page<OrderVO>> list(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String status) {
        return Result.ok(orderService.list(page, size, status));
    }

    @GetMapping("/sales")
    public Result<Page<OrderVO>> sellerList(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String status) {
        return Result.ok(orderService.sellerList(page, size, status));
    }

    @PutMapping("/sales/{orderNo}/ship")
    public Result<OrderVO> sellerShip(@PathVariable String orderNo) {
        return Result.ok(orderService.sellerShip(orderNo));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        return Result.ok(orderService.detail(orderNo));
    }

    @PutMapping("/{orderNo}/cancel")
    public Result<OrderVO> cancel(@PathVariable String orderNo) {
        return Result.ok(orderService.cancel(orderNo));
    }

    @PutMapping("/{orderNo}/finish")
    public Result<OrderVO> finish(@PathVariable String orderNo) {
        return Result.ok(orderService.finish(orderNo));
    }
}
