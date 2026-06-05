package com.course.mall.controller;

import com.course.mall.common.Result;
import com.course.mall.service.OrderService;
import com.course.mall.vo.OrderVO;
import com.course.mall.vo.PaymentRecordVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final OrderService orderService;

    public PaymentController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/mock/{orderNo}")
    public Result<OrderVO> mockPay(@PathVariable String orderNo) {
        return Result.ok(orderService.mockPay(orderNo));
    }

    @GetMapping("/{orderNo}")
    public Result<PaymentRecordVO> detail(@PathVariable String orderNo) {
        return Result.ok(orderService.paymentRecord(orderNo));
    }
}
