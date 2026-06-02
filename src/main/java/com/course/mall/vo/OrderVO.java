package com.course.mall.vo;

import com.course.mall.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private BigDecimal totalAmount;
    private String status;
    private String receiverSnapshot;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private List<OrderItemVO> items = new ArrayList<>();

    public static OrderVO from(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setReceiverSnapshot(order.getReceiverSnapshot());
        vo.setPaidAt(order.getPaidAt());
        vo.setCreatedAt(order.getCreatedAt());
        return vo;
    }
}
