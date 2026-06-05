package com.course.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.course.mall.entity.Order;
import com.course.mall.entity.Product;
import com.course.mall.entity.User;
import com.course.mall.mapper.OrderMapper;
import com.course.mall.mapper.ProductMapper;
import com.course.mall.mapper.UserMapper;
import com.course.mall.vo.AdminStatsVO;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;

    public AdminDashboardService(UserMapper userMapper, ProductMapper productMapper, OrderMapper orderMapper) {
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
    }

    public AdminStatsVO stats() {
        return new AdminStatsVO(
                userMapper.selectCount(new LambdaQueryWrapper<User>()),
                userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, "ENABLED")),
                userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, "DISABLED")),
                productMapper.selectCount(new LambdaQueryWrapper<Product>()),
                orderMapper.selectCount(new LambdaQueryWrapper<Order>()),
                orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, "PENDING_PAY"))
        );
    }
}
