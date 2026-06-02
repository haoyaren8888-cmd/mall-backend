package com.course.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.Result;
import com.course.mall.entity.User;
import com.course.mall.mapper.UserMapper;
import com.course.mall.vo.UserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserMapper userMapper;

    public AdminUserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping
    public Result<Page<UserVO>> list(@RequestParam(defaultValue = "1") long page,
                                     @RequestParam(defaultValue = "10") long size) {
        Page<User> userPage = userMapper.selectPage(Page.of(page, size),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));
        Page<UserVO> voPage = Page.of(page, size, userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(UserVO::from).toList());
        return Result.ok(voPage);
    }

    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestParam String status) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
        return Result.ok();
    }
}
