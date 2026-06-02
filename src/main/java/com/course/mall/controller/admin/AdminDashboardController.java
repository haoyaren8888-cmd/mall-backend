package com.course.mall.controller.admin;

import com.course.mall.common.Result;
import com.course.mall.service.AdminDashboardService;
import com.course.mall.vo.AdminStatsVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/stats")
    public Result<AdminStatsVO> stats() {
        return Result.ok(adminDashboardService.stats());
    }
}
