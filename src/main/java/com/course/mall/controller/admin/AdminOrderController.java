package com.course.mall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.Result;
import com.course.mall.service.OrderService;
import com.course.mall.vo.OrderVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Result<Page<OrderVO>> list(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) String orderNo) {
        return Result.ok(orderService.adminList(page, size, status, orderNo));
    }

    @PutMapping("/{orderNo}/ship")
    public Result<OrderVO> ship(@PathVariable String orderNo) {
        return Result.ok(orderService.adminShip(orderNo));
    }
}
