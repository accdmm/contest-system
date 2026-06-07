package com.contest.team.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.team.entity.TeamDO;
import com.contest.team.entity.TeamMemberDO;

import java.util.List;

public interface TeamService extends IService<TeamDO> {

    TeamDO createTeam(Long userId, String teamName, Long teacherId);

    String generateInviteCode(Long teamId, Long userId);

    TeamDO joinByInviteCode(Long userId, String inviteCode);

    void approveMember(Long teamId, Long userId, Long memberId);

    void rejectMember(Long teamId, Long userId, Long memberId);

    void removeMember(Long teamId, Long userId, Long memberId);

    void dissolveTeam(Long teamId, Long userId);

    void leaveTeam(Long teamId, Long userId);

    void submitForReview(Long teamId, Long userId);

    List<TeamMemberDO> listMembers(Long teamId);

    List<TeamMemberDO> listPendingMembers(Long teamId);

    List<TeamDO> getTeamsByLeader(Long userId);

    IPage<TeamDO> pageTeams(Integer status, Integer page, Integer size);

    void adminApproveTeam(Long teamId);

    void adminRejectTeam(Long teamId, String reason);

    List<TeamDO> listUserTeams(Long userId);

    void setTeacher(Long teamId, Long teacherId, Long userId);

    List<TeamDO> getTeamsByTeacher(Long teacherId);
}
