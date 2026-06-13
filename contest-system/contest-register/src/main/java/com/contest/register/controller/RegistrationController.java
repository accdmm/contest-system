package com.contest.register.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.security.SecurityUtil;
import com.contest.common.annotation.OperationLog;
import com.contest.common.result.Result;
import com.contest.register.entity.Registration;
import com.contest.register.param.RegPersonalRequest;
import com.contest.register.param.RegTeamRequest;
import com.contest.register.param.RejectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.contest.register.service.RegistrationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/** 报名管理接口 */
@RestController
@RequestMapping("/api/registration")
@Slf4j
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /** 个人赛报名 */
    @PostMapping("/personal")
    @PreAuthorize("isAuthenticated()")
    public Result<Registration> registerPersonal(@RequestBody @Valid RegPersonalRequest param) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("用户 {} 报名个人赛: contestId={}", userId, param.getContestId());
        return Result.success(registrationService.registerPersonal(userId, param.getContestId(), param.getRemark()));
    }

    /** 团队赛报名 */
    @PostMapping("/team")
    @PreAuthorize("isAuthenticated()")
    public Result<Registration> registerTeam(@RequestBody @Valid RegTeamRequest param) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("用户 {} 报名团队赛: contestId={}, teamId={}", userId, param.getContestId(), param.getTeamId());
        return Result.success(registrationService.registerTeam(userId, param.getContestId(), param.getTeamId()));
    }

    /** 审核通过报名 */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('registration:approve')")
    @OperationLog(action = "通过报名")
    public Result<Void> approve(@PathVariable Long id) {
        registrationService.approveRegistration(id);
        return Result.success();
    }

    /** 驳回报名 */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('registration:approve')")
    @OperationLog(action = "驳回报名")
    public Result<Void> reject(@PathVariable Long id, @RequestBody @Valid RejectRequest param) {
        registrationService.rejectRegistration(id, param.getReason());
        return Result.success();
    }

    /** 取消报名 */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancel(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        registrationService.cancelRegistration(id, userId);
        return Result.success();
    }

    /** 查询用户的报名记录 */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<Registration>> byUser(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!currentUserId.equals(userId) && !isAdmin) {
            return Result.error("无权查看其他用户的报名记录");
        }
        return Result.success(registrationService.pageByUser(userId, page, size));
    }

    /** 查询竞赛的报名记录 */
    @GetMapping("/contest/{contestId}")
    @PreAuthorize("hasAuthority('registration:list')")
    public Result<IPage<Registration>> byContest(@PathVariable Long contestId,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) Integer status) {
        return Result.success(registrationService.pageByContest(contestId, page, size, status));
    }

    /** 分页查询报名列表 */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('registration:list')")
    public Result<IPage<Registration>> page(@RequestParam(required = false) Long contestId,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(registrationService.pageAll(contestId, status, page, size));
    }

    /** 查询报名详情 */
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
