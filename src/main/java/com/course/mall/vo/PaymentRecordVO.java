package com.course.mall.vo;

import com.course.mall.entity.PaymentRecord;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentRecordVO {
    private String orderNo;
    private String payNo;
    private BigDecimal amount;
    private String status;
    private String payType;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public static PaymentRecordVO from(PaymentRecord record) {
        if (record == null) {
            return null;
        }
        PaymentRecordVO vo = new PaymentRecordVO();
        vo.setOrderNo(record.getOrderNo());
        vo.setPayNo(record.getPayNo());
        vo.setAmount(record.getAmount());
        vo.setStatus(record.getStatus());
        vo.setPayType(record.getPayType());
        vo.setPaidAt(record.getPaidAt());
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }
}
