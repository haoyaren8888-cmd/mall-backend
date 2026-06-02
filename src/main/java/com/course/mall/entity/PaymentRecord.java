package com.course.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;
    private String payNo;
    private BigDecimal amount;
    private String status;
    private String payType;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
