package com.contest.admin.controller;

import com.contest.admin.service.DashboardService;
import com.contest.common.result.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 数据可视化大屏接口 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /** 获取概览统计数据 */
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Long>> getStatistics() {
        return Result.success(dashboardService.getStatistics());
    }

    /** 获取竞赛类别分布 */
    @GetMapping("/contest-category")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Map<String, Object>>> getContestCategory() {
        return Result.success(dashboardService.getContestCategoryDistribution());
    }

    /** 获取竞赛级别分布 */
    @GetMapping("/contest-level")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Map<String, Object>>> getContestLevel() {
        return Result.success(dashboardService.getContestLevelDistribution());
    }

    /** 获取报名趋势数据 */
    @GetMapping("/registration-trend")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Map<String, Object>>> getRegistrationTrend(
            @RequestParam(defaultValue = "30") int days) {
        return Result.success(dashboardService.getRegistrationTrend(days));
    }

    /** 获取报名状态分布 */
    @GetMapping("/registration-status")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Map<String, Object>>> getRegistrationStatus() {
        return Result.success(dashboardService.getRegistrationStatusDistribution());
    }

    /** 获取用户增长趋势 */
    @GetMapping("/user-growth")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Map<String, Object>>> getUserGrowth(
            @RequestParam(defaultValue = "30") int days) {
        return Result.success(dashboardService.getUserGrowth(days));
    }

    /** 获取热门竞赛排行 */
    @GetMapping("/top-contests")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Map<String, Object>>> getTopContests(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(dashboardService.getTopContests(limit));
    }
}
