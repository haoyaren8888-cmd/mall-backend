package com.course.mall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.Result;
import com.course.mall.service.ProductMessageService;
import com.course.mall.vo.AdminMessageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/messages")
public class AdminMessageController {
    private final ProductMessageService messageService;

    public AdminMessageController(ProductMessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public Result<Page<AdminMessageVO>> list(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false) Boolean replied) {
        return Result.ok(messageService.pageAdminMessages(page, size, keyword, status, replied));
    }

    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestParam String status) {
        messageService.updateAdminStatus(id, status);
        return Result.ok();
    }
}
