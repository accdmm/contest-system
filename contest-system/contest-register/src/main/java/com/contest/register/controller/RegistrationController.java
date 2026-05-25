package com.contest.register.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.dto.Result;
import com.contest.register.entity.Registration;
import com.contest.register.service.RegistrationService;
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
    public Result<Registration> registerPersonal(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Long contestId = Long.valueOf(params.get("contestId").toString());
        String remark = (String) params.getOrDefault("remark", "");
        return Result.success(registrationService.registerPersonal(userId, contestId, remark));
    }

    @PostMapping("/team")
    public Result<Registration> registerTeam(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Long contestId = Long.valueOf(params.get("contestId").toString());
        Long teamId = Long.valueOf(params.get("teamId").toString());
        return Result.success(registrationService.registerTeam(userId, contestId, teamId));
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        registrationService.approveRegistration(id);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> params) {
        registrationService.rejectRegistration(id, params.get("reason"));
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam Long userId) {
        registrationService.cancelRegistration(id, userId);
        return Result.success();
    }

    @GetMapping("/user/{userId}")
    public Result<IPage<Registration>> byUser(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(registrationService.pageByUser(userId, page, size));
    }

    @GetMapping("/contest/{contestId}")
    public Result<IPage<Registration>> byContest(@PathVariable Long contestId,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) Integer status) {
        return Result.success(registrationService.pageByContest(contestId, page, size, status));
    }

    @GetMapping("/{id}")
    public Result<Registration> getById(@PathVariable Long id) {
        Registration reg = registrationService.getById(id);
        if (reg == null) {
            return Result.error("报名记录不存在");
        }
        return Result.success(reg);
    }
}
