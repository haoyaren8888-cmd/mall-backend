package com.course.mall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminStatsVO {
    private Long userCount;
    private Long enabledUserCount;
    private Long disabledUserCount;
    private Long productCount;
    private Long orderCount;
    private Long pendingOrderCount;
    private Long messageCount;
    private Long unrepliedMessageCount;
    private Long reviewCount;
    private Long hiddenContentCount;
}
