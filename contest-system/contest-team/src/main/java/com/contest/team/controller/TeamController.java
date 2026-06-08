package com.contest.team.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.security.SecurityUtil;
import com.contest.common.annotation.OperationLog;
import com.contest.common.result.Result;
import com.contest.team.entity.TeamDO;
import com.contest.team.entity.TeamMemberDO;
import com.contest.team.param.TeamCreateParam;
import com.contest.team.param.TeamJoinParam;
import com.contest.team.param.RejectParam;
import com.contest.team.param.SetTeacherParam;
import com.contest.team.service.TeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/** 团队管理接口 */
@RestController
@RequestMapping("/api/team")
public class TeamController {

    private static final Logger log = LoggerFactory.getLogger(TeamController.class);
    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /** 创建团队 */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<TeamDO> create(@RequestBody @Valid TeamCreateParam param) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("用户 {} 创建团队: {}", userId, param.getTeamName());
        return Result.success(teamService.createTeam(userId, param.getTeamName(), param.getTeacherId()));
    }

    /** 生成团队邀请码 */
    @PostMapping("/{teamId}/invite")
    @PreAuthorize("isAuthenticated()")
    public Result<String> generateInvite(@PathVariable Long teamId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("用户 {} 生成团队 {} 邀请码", userId, teamId);
        return Result.success(teamService.generateInviteCode(teamId, userId));
    }

    /** 通过邀请码加入团队 */
    @PostMapping("/join")
    @PreAuthorize("isAuthenticated()")
    public Result<TeamDO> join(@RequestBody @Valid TeamJoinParam param) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("用户 {} 通过邀请码加入团队", userId);
        return Result.success(teamService.joinByInviteCode(userId, param.getInviteCode()));
    }

    /** 批准成员加入 */
    @PostMapping("/{teamId}/members/{memberId}/approve")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> approveMember(@PathVariable Long teamId, @PathVariable Long memberId) {
        Long userId = SecurityUtil.getCurrentUserId();
        teamService.approveMember(teamId, userId, memberId);
        return Result.success();
    }

    /** 拒绝成员加入 */
    @PostMapping("/{teamId}/members/{memberId}/reject")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> rejectMember(@PathVariable Long teamId, @PathVariable Long memberId) {
        Long userId = SecurityUtil.getCurrentUserId();
        teamService.rejectMember(teamId, userId, memberId);
        return Result.success();
    }

    /** 移除团队成员 */
    @PostMapping("/{teamId}/members/{memberId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> removeMember(@PathVariable Long teamId, @PathVariable Long memberId) {
        Long userId = SecurityUtil.getCurrentUserId();
        teamService.removeMember(teamId, userId, memberId);
        return Result.success();
    }

    /** 解散团队 */
    @PostMapping("/{teamId}/dissolve")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> dissolve(@PathVariable Long teamId) {
        Long userId = SecurityUtil.getCurrentUserId();
        teamService.dissolveTeam(teamId, userId);
        return Result.success();
    }

    /** 退出团队 */
    @PostMapping("/{teamId}/leave")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> leave(@PathVariable Long teamId) {
        Long userId = SecurityUtil.getCurrentUserId();
        teamService.leaveTeam(teamId, userId);
        return Result.success();
    }

    /** 提交团队审核 */
    @PostMapping("/{teamId}/submit")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> submitReview(@PathVariable Long teamId) {
        Long userId = SecurityUtil.getCurrentUserId();
        teamService.submitForReview(teamId, userId);
        return Result.success();
    }

    /** 获取团队成员列表 */
    @GetMapping("/{teamId}/members")
    @PreAuthorize("isAuthenticated()")
    public Result<List<TeamMemberDO>> members(@PathVariable Long teamId) {
        return Result.success(teamService.listMembers(teamId));
    }

    /** 获取待审批成员列表 */
    @GetMapping("/{teamId}/pending")
    @PreAuthorize("isAuthenticated()")
    public Result<List<TeamMemberDO>> pendingMembers(@PathVariable Long teamId) {
        return Result.success(teamService.listPendingMembers(teamId));
    }

    /** 根据ID获取团队详情 */
    @GetMapping("/{id}/detail")
    @PreAuthorize("isAuthenticated()")
    public Result<TeamDO> getById(@PathVariable Long id) {
        TeamDO team = teamService.getById(id);
        if (team == null) {
            return Result.error("团队不存在");
        }
        return Result.success(team);
    }

    /** 获取当前用户创建的团队列表 */
    @GetMapping("/leader")
    @PreAuthorize("isAuthenticated()")
    public Result<List<TeamDO>> getByLeader() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(teamService.getTeamsByLeader(userId));
    }

    /** 分页查询团队（管理员） */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('team:list')")
    public Result<IPage<TeamDO>> page(@RequestParam(required = false) Integer status,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(teamService.pageTeams(status, page, size));
    }

    /** 获取用户参与的团队列表 */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public Result<List<TeamDO>> userTeams(@PathVariable Long userId) {
        return Result.success(teamService.listUserTeams(userId));
    }

    /** 管理员通过团队审核 */
    @PostMapping("/{teamId}/admin-approve")
    @PreAuthorize("hasAuthority('team:approve')")
    @OperationLog(action = "通过团队")
    public Result<Void> adminApprove(@PathVariable Long teamId) {
        teamService.adminApproveTeam(teamId);
        return Result.success();
    }

    /** 管理员驳回团队审核 */
    @PostMapping("/{teamId}/admin-reject")
    @PreAuthorize("hasAuthority('team:approve')")
    @OperationLog(action = "驳回团队")
    public Result<Void> adminReject(@PathVariable Long teamId, @RequestBody @Valid RejectParam param) {
        teamService.adminRejectTeam(teamId, param.getReason());
        return Result.success();
    }

    /** 设置团队指导教师 */
    @PostMapping("/{teamId}/teacher")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> setTeacher(@PathVariable Long teamId, @RequestBody @Valid SetTeacherParam param) {
        Long userId = SecurityUtil.getCurrentUserId();
        teamService.setTeacher(teamId, param.getTeacherId(), userId);
        return Result.success();
    }

    /** 获取当前教师指导的团队列表 */
    @GetMapping("/teacher")
    @PreAuthorize("isAuthenticated()")
    public Result<List<TeamDO>> getTeacherTeams() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(teamService.getTeamsByTeacher(userId));
    }
}
