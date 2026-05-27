package com.contest.team.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.team.entity.Team;
import com.contest.team.entity.TeamMember;

import java.util.List;

public interface TeamService extends IService<Team> {

    Team createTeam(Long userId, String teamName);

    String generateInviteCode(Long teamId, Long userId);

    Team joinByInviteCode(Long userId, String inviteCode);

    void approveMember(Long teamId, Long userId, Long memberId);

    void rejectMember(Long teamId, Long userId, Long memberId);

    void removeMember(Long teamId, Long userId, Long memberId);

    void dissolveTeam(Long teamId, Long userId);

    void leaveTeam(Long teamId, Long userId);

    void submitForReview(Long teamId, Long userId);

    List<TeamMember> listMembers(Long teamId);

    List<TeamMember> listPendingMembers(Long teamId);

    List<Team> getTeamsByLeader(Long userId);

    IPage<Team> pageTeams(Integer status, Integer page, Integer size);

    void adminApproveTeam(Long teamId);

    void adminRejectTeam(Long teamId, String reason);

    List<Team> listUserTeams(Long userId);
}
