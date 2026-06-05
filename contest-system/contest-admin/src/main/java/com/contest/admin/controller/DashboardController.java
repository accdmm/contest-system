package com.contest.admin.controller;

import com.contest.admin.service.DashboardService;
import com.contest.common.dto.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Long>> getStatistics() {
        return Result.success(dashboardService.getStatistics());
    }

    @GetMapping("/contest-category")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Map<String, Object>>> getContestCategory() {
        return Result.success(dashboardService.getContestCategoryDistribution());
    }

    @GetMapping("/contest-level")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Map<String, Object>>> getContestLevel() {
        return Result.success(dashboardService.getContestLevelDistribution());
    }

    @GetMapping("/registration-trend")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Map<String, Object>>> getRegistrationTrend(
            @RequestParam(defaultValue = "30") int days) {
        return Result.success(dashboardService.getRegistrationTrend(days));
    }

    @GetMapping("/registration-status")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Map<String, Object>>> getRegistrationStatus() {
        return Result.success(dashboardService.getRegistrationStatusDistribution());
    }

    @GetMapping("/user-growth")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Map<String, Object>>> getUserGrowth(
            @RequestParam(defaultValue = "30") int days) {
        return Result.success(dashboardService.getUserGrowth(days));
    }

    @GetMapping("/top-contests")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Map<String, Object>>> getTopContests(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(dashboardService.getTopContests(limit));
    }
}
