package com.course.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.BusinessException;
import com.course.mall.common.CurrentUser;
import com.course.mall.common.Result;
import com.course.mall.common.SessionContext;
import com.course.mall.entity.User;
import com.course.mall.mapper.UserMapper;
import com.course.mall.vo.UserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import java.util.Set;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private static final Set<String> ALLOWED_STATUS = Set.of("ENABLED", "DISABLED");

    private final UserMapper userMapper;

    public AdminUserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping
    public Result<Page<UserVO>> list(@RequestParam(defaultValue = "1") long page,
                                     @RequestParam(defaultValue = "10") long size,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String searchText = keyword.trim();
            wrapper.and(query -> query.like(User::getUsername, searchText)
                    .or().like(User::getNickname, searchText)
                    .or().like(User::getPhone, searchText)
                    .or().like(User::getStudentNo, searchText));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(User::getStatus, status.trim());
        }
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> userPage = userMapper.selectPage(Page.of(page, size), wrapper);
        Page<UserVO> voPage = Page.of(page, size, userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(UserVO::from).toList());
        return Result.ok(voPage);
    }

    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestParam String status) {
        if (!ALLOWED_STATUS.contains(status)) {
            throw BusinessException.badRequest("用户状态不正确");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        CurrentUser currentUser = SessionContext.requireUser();
        if (currentUser.getId().equals(id) && !"ENABLED".equals(status)) {
            throw BusinessException.badRequest("不能禁用当前登录账号");
        }

        user.setStatus(status);
        userMapper.updateById(user);
        return Result.ok();
    }
}
