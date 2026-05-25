package com.contest.team.controller;

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
        Long userId = Long.valueOf(params.get("userId").toString());
        Long contestId = Long.valueOf(params.get("contestId").toString());
        String teamName = params.get("teamName").toString();
        return Result.success(teamService.createTeam(userId, contestId, teamName));
    }

    @PostMapping("/{teamId}/invite")
    public Result<String> generateInvite(@PathVariable Long teamId, @RequestBody Map<String, Long> params) {
        return Result.success(teamService.generateInviteCode(teamId, params.get("userId")));
    }

    @PostMapping("/join")
    public Result<Team> join(@RequestBody Map<String, String> params) {
        Long userId = Long.valueOf(params.get("userId"));
        String code = params.get("inviteCode");
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

    @GetMapping("/{id}")
    public Result<Team> getById(@PathVariable Long id) {
        Team team = teamService.getById(id);
        if (team == null) {
            return Result.error("团队不存在");
        }
        return Result.success(team);
    }
}
