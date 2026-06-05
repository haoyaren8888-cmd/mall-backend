package com.course.mall.service;

import com.course.mall.mapper.OrderMapper;
import com.course.mall.mapper.ProductMapper;
import com.course.mall.mapper.ProductMessageMapper;
import com.course.mall.mapper.ProductReviewMapper;
import com.course.mall.mapper.UserMapper;
import com.course.mall.vo.AdminStatsVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {
    @Mock
    private UserMapper userMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private ProductMessageMapper messageMapper;
    @Mock
    private ProductReviewMapper reviewMapper;

    @Test
    void statsIncludesContentOperationCounts() {
        AdminDashboardService service = new AdminDashboardService(
                userMapper, productMapper, orderMapper, messageMapper, reviewMapper);

        when(messageMapper.selectCount(any())).thenReturn(1L, 8L, 3L);
        when(reviewMapper.selectCount(any())).thenReturn(2L, 6L);
        when(userMapper.selectCount(any())).thenReturn(10L, 9L, 1L);
        when(productMapper.selectCount(any())).thenReturn(12L);
        when(orderMapper.selectCount(any())).thenReturn(20L, 4L);

        AdminStatsVO stats = service.stats();

        assertThat(stats.getUserCount()).isEqualTo(10L);
        assertThat(stats.getProductCount()).isEqualTo(12L);
        assertThat(stats.getOrderCount()).isEqualTo(20L);
        assertThat(stats.getMessageCount()).isEqualTo(8L);
        assertThat(stats.getUnrepliedMessageCount()).isEqualTo(3L);
        assertThat(stats.getReviewCount()).isEqualTo(6L);
        assertThat(stats.getHiddenContentCount()).isEqualTo(3L);
    }
}
