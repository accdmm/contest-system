package com.contest.team.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.security.SecurityUtil;
import com.contest.common.dto.Result;
import com.contest.team.entity.Team;
import com.contest.team.entity.TeamMember;
import com.contest.team.param.TeamCreateParam;
import com.contest.team.param.TeamJoinParam;
import com.contest.team.service.TeamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/team")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Team> create(@RequestBody @Valid TeamCreateParam param) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(teamService.createTeam(userId, param.getTeamName(), param.getTeacherId()));
    }

    @PostMapping("/{teamId}/invite")
    @PreAuthorize("isAuthenticated()")
    public Result<String> generateInvite(@PathVariable Long teamId) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(teamService.generateInviteCode(teamId, userId));
    }

    @PostMapping("/join")
    @PreAuthorize("isAuthenticated()")
    public Result<Team> join(@RequestBody @Valid TeamJoinParam param) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(teamService.joinByInviteCode(userId, param.getInviteCode()));
    }

    @PutMapping("/{teamId}/members/{memberId}/approve")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> approveMember(@PathVariable Long teamId, @PathVariable Long memberId) {
        Long userId = SecurityUtil.getCurrentUserId();
        teamService.approveMember(teamId, userId, memberId);
        return Result.success();
    }

    @PutMapping("/{teamId}/members/{memberId}/reject")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> rejectMember(@PathVariable Long teamId, @PathVariable Long memberId) {
        Long userId = SecurityUtil.getCurrentUserId();
        teamService.rejectMember(teamId, userId, memberId);
        return Result.success();
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> removeMember(@PathVariable Long teamId, @PathVariable Long memberId) {
        Long userId = SecurityUtil.getCurrentUserId();
        teamService.removeMember(teamId, userId, memberId);
        return Result.success();
    }

    @PutMapping("/{teamId}/dissolve")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> dissolve(@PathVariable Long teamId) {
        Long userId = SecurityUtil.getCurrentUserId();
        teamService.dissolveTeam(teamId, userId);
        return Result.success();
    }

    @PutMapping("/{teamId}/leave")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> leave(@PathVariable Long teamId) {
        Long userId = SecurityUtil.getCurrentUserId();
        teamService.leaveTeam(teamId, userId);
        return Result.success();
    }

    @PutMapping("/{teamId}/submit")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> submitReview(@PathVariable Long teamId) {
        Long userId = SecurityUtil.getCurrentUserId();
        teamService.submitForReview(teamId, userId);
        return Result.success();
    }

    @GetMapping("/{teamId}/members")
    @PreAuthorize("isAuthenticated()")
    public Result<List<TeamMember>> members(@PathVariable Long teamId) {
        return Result.success(teamService.listMembers(teamId));
    }

    @GetMapping("/{teamId}/pending")
    @PreAuthorize("isAuthenticated()")
    public Result<List<TeamMember>> pendingMembers(@PathVariable Long teamId) {
        return Result.success(teamService.listPendingMembers(teamId));
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("isAuthenticated()")
    public Result<Team> getById(@PathVariable Long id) {
        Team team = teamService.getById(id);
        if (team == null) {
            return Result.error("团队不存在");
        }
        return Result.success(team);
    }

    @GetMapping("/leader")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Team>> getByLeader() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(teamService.getTeamsByLeader(userId));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('team:list')")
    public Result<IPage<Team>> page(@RequestParam(required = false) Integer status,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(teamService.pageTeams(status, page, size));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Team>> userTeams(@PathVariable Long userId) {
        return Result.success(teamService.listUserTeams(userId));
    }

    @PutMapping("/{teamId}/admin-approve")
    @PreAuthorize("hasAuthority('team:approve')")
    public Result<Void> adminApprove(@PathVariable Long teamId) {
        teamService.adminApproveTeam(teamId);
        return Result.success();
    }

    @PutMapping("/{teamId}/admin-reject")
    @PreAuthorize("hasAuthority('team:approve')")
    public Result<Void> adminReject(@PathVariable Long teamId, @RequestBody Map<String, String> params) {
        teamService.adminRejectTeam(teamId, params.get("reason"));
        return Result.success();
    }

    @PutMapping("/{teamId}/teacher")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> setTeacher(@PathVariable Long teamId, @RequestBody Map<String, Long> params) {
        Long userId = SecurityUtil.getCurrentUserId();
        teamService.setTeacher(teamId, params.get("teacherId"), userId);
        return Result.success();
    }

    @GetMapping("/teacher")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Team>> getTeacherTeams() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(teamService.getTeamsByTeacher(userId));
    }
}
