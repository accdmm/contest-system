package com.contest.team.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.dto.Result;
import com.contest.team.entity.Team;
import com.contest.team.entity.TeamMember;
import com.contest.team.service.TeamService;
import org.springframework.web.bind.annotation.*;

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
    public Result<Team> create(@RequestBody Map<String, Object> params) {
        Object userIdObj = params.get("userId");
        Object contestIdObj = params.get("contestId");
        Object teamNameObj = params.get("teamName");
        if (userIdObj == null || contestIdObj == null || teamNameObj == null) {
            return Result.error("缺少必要参数");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        Long contestId = Long.valueOf(contestIdObj.toString());
        String teamName = teamNameObj.toString();
        return Result.success(teamService.createTeam(userId, contestId, teamName));
    }

    @PostMapping("/{teamId}/invite")
    public Result<String> generateInvite(@PathVariable Long teamId, @RequestBody Map<String, Long> params) {
        Long userId = params.get("userId");
        if (userId == null) {
            return Result.error("缺少用户ID");
        }
        return Result.success(teamService.generateInviteCode(teamId, userId));
    }

    @PostMapping("/join")
    public Result<Team> join(@RequestBody Map<String, String> params) {
        String userIdStr = params.get("userId");
        String code = params.get("inviteCode");
        if (userIdStr == null || code == null) {
            return Result.error("缺少必要参数");
        }
        Long userId = Long.valueOf(userIdStr);
        return Result.success(teamService.joinByInviteCode(userId, code));
    }

    @PutMapping("/{teamId}/members/{memberId}/approve")
    public Result<Void> approveMember(@PathVariable Long teamId, @RequestParam Long userId, @PathVariable Long memberId) {
        teamService.approveMember(teamId, userId, memberId);
        return Result.success();
    }

    @PutMapping("/{teamId}/members/{memberId}/reject")
    public Result<Void> rejectMember(@PathVariable Long teamId, @RequestParam Long userId, @PathVariable Long memberId) {
        teamService.rejectMember(teamId, userId, memberId);
        return Result.success();
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    public Result<Void> removeMember(@PathVariable Long teamId, @RequestParam Long userId, @PathVariable Long memberId) {
        teamService.removeMember(teamId, userId, memberId);
        return Result.success();
    }

    @PutMapping("/{teamId}/dissolve")
    public Result<Void> dissolve(@PathVariable Long teamId, @RequestParam Long userId) {
        teamService.dissolveTeam(teamId, userId);
        return Result.success();
    }

    @PutMapping("/{teamId}/leave")
    public Result<Void> leave(@PathVariable Long teamId, @RequestParam Long userId) {
        teamService.leaveTeam(teamId, userId);
        return Result.success();
    }

    @PutMapping("/{teamId}/submit")
    public Result<Void> submitReview(@PathVariable Long teamId, @RequestParam Long userId) {
        teamService.submitForReview(teamId, userId);
        return Result.success();
    }

    @GetMapping("/{teamId}/members")
    public Result<List<TeamMember>> members(@PathVariable Long teamId) {
        return Result.success(teamService.listMembers(teamId));
    }

    @GetMapping("/{teamId}/pending")
    public Result<List<TeamMember>> pendingMembers(@PathVariable Long teamId) {
        return Result.success(teamService.listPendingMembers(teamId));
    }

    @GetMapping("/{id}/detail")
    public Result<Team> getById(@PathVariable Long id) {
        Team team = teamService.getById(id);
        if (team == null) {
            return Result.error("团队不存在");
        }
        return Result.success(team);
    }

    @GetMapping("/leader")
    public Result<Team> getByLeader(@RequestParam Long userId, @RequestParam Long contestId) {
        Team team = teamService.getByLeaderAndContest(userId, contestId);
        return Result.success(team);
    }

    @GetMapping("/page")
    public Result<IPage<Team>> page(@RequestParam(required = false) Integer status,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(teamService.pageTeams(status, page, size));
    }

    @GetMapping("/user/{userId}")
    public Result<List<Team>> userTeams(@PathVariable Long userId) {
        return Result.success(teamService.listUserTeams(userId));
    }

    @PutMapping("/{teamId}/admin-approve")
    public Result<Void> adminApprove(@PathVariable Long teamId) {
        teamService.adminApproveTeam(teamId);
        return Result.success();
    }

    @PutMapping("/{teamId}/admin-reject")
    public Result<Void> adminReject(@PathVariable Long teamId, @RequestBody Map<String, String> params) {
        teamService.adminRejectTeam(teamId, params.get("reason"));
        return Result.success();
    }
}
