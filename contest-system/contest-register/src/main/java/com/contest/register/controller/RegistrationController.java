package com.contest.register.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.security.SecurityUtil;
import com.contest.common.dto.Result;
import com.contest.register.entity.Registration;
import com.contest.register.service.RegistrationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/personal")
    @PreAuthorize("isAuthenticated()")
    public Result<Registration> registerPersonal(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtil.getCurrentUserId();
        Object contestIdObj = params.get("contestId");
        if (contestIdObj == null) {
            return Result.error("缺少必要参数");
        }
        Long contestId = Long.valueOf(contestIdObj.toString());
        String remark = (String) params.getOrDefault("remark", "");
        return Result.success(registrationService.registerPersonal(userId, contestId, remark));
    }

    @PostMapping("/team")
    @PreAuthorize("isAuthenticated()")
    public Result<Registration> registerTeam(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtil.getCurrentUserId();
        Object contestIdObj = params.get("contestId");
        Object teamIdObj = params.get("teamId");
        if (contestIdObj == null || teamIdObj == null) {
            return Result.error("缺少必要参数");
        }
        Long contestId = Long.valueOf(contestIdObj.toString());
        Long teamId = Long.valueOf(teamIdObj.toString());
        return Result.success(registrationService.registerTeam(userId, contestId, teamId));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('registration:approve')")
    public Result<Void> approve(@PathVariable Long id) {
        registrationService.approveRegistration(id);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('registration:approve')")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> params) {
        registrationService.rejectRegistration(id, params.get("reason"));
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancel(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        registrationService.cancelRegistration(id, userId);
        return Result.success();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<Registration>> byUser(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(registrationService.pageByUser(userId, page, size));
    }

    @GetMapping("/contest/{contestId}")
    @PreAuthorize("hasAuthority('registration:list')")
    public Result<IPage<Registration>> byContest(@PathVariable Long contestId,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) Integer status) {
        return Result.success(registrationService.pageByContest(contestId, page, size, status));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('registration:list')")
    public Result<IPage<Registration>> page(@RequestParam(required = false) Long contestId,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(registrationService.pageAll(contestId, status, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Registration> getById(@PathVariable Long id) {
        Registration reg = registrationService.getById(id);
        if (reg == null) {
            return Result.error("报名记录不存在");
        }
        return Result.success(reg);
    }
}
