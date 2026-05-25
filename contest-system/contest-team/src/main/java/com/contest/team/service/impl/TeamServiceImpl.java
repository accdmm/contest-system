package com.contest.team.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.team.entity.Team;
import com.contest.team.entity.TeamMember;
import com.contest.team.mapper.TeamMapper;
import com.contest.team.mapper.TeamMemberMapper;
import com.contest.team.service.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    private final TeamMemberMapper teamMemberMapper;

    public TeamServiceImpl(TeamMemberMapper teamMemberMapper) {
        this.teamMemberMapper = teamMemberMapper;
    }

    @Override
    @Transactional
    public Team createTeam(Long userId, Long contestId, String teamName) {
        Team team = new Team();
        team.setContestId(contestId);
        team.setLeaderId(userId);
        team.setTeamName(teamName);
        team.setTeamNo("T" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        team.setStatus(CommonConstants.TEAM_FORMING);
        team.setMemberCount(1);
        save(team);

        TeamMember leader = new TeamMember();
        leader.setTeamId(team.getId());
        leader.setContestId(contestId);
        leader.setUserId(userId);
        leader.setRole(CommonConstants.MEMBER_LEADER);
        leader.setStatus(CommonConstants.MEMBER_APPROVED);
        leader.setApplyTime(LocalDateTime.now());
        leader.setHandleTime(LocalDateTime.now());
        teamMemberMapper.insert(leader);

        return team;
    }

    @Override
    public String generateInviteCode(Long teamId, Long userId) {
        Team team = getById(teamId);
        if (team == null) {
            throw new BusinessException("团队不存在");
        }
        if (!team.getLeaderId().equals(userId)) {
            throw new BusinessException("仅队长可生成邀请码");
        }
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        team.setInviteCode(code);
        team.setInviteCodeExpire(LocalDateTime.now().plusDays(7));
        updateById(team);
        return code;
    }

    @Override
    @Transactional
    public Team joinByInviteCode(Long userId, String inviteCode) {
        Team team = getOne(new LambdaQueryWrapper<Team>()
                .eq(Team::getInviteCode, inviteCode));
        if (team == null) {
            throw new BusinessException("邀请码无效");
        }
        if (team.getInviteCodeExpire() != null && team.getInviteCodeExpire().isBefore(LocalDateTime.now())) {
            throw new BusinessException("邀请码已过期");
        }
        if (team.getStatus() != CommonConstants.TEAM_FORMING) {
            throw new BusinessException("该团队当前无法加入");
        }

        long count = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getContestId, team.getContestId()));
        if (count > 0) {
            throw new BusinessException("你已在同一竞赛的团队中");
        }

        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setContestId(team.getContestId());
        member.setUserId(userId);
        member.setRole(CommonConstants.MEMBER_NORMAL);
        member.setStatus(CommonConstants.MEMBER_PENDING);
        member.setApplyTime(LocalDateTime.now());
        teamMemberMapper.insert(member);

        return team;
    }

    @Override
    @Transactional
    public void approveMember(Long teamId, Long userId, Long memberId) {
        Team team = getById(teamId);
        if (team == null || !team.getLeaderId().equals(userId)) {
            throw new BusinessException("仅队长可审核成员");
        }
        TeamMember member = teamMemberMapper.selectById(memberId);
        if (member == null || !member.getTeamId().equals(teamId)) {
            throw new BusinessException("成员申请不存在");
        }
        member.setStatus(CommonConstants.MEMBER_APPROVED);
        member.setHandleTime(LocalDateTime.now());
        teamMemberMapper.updateById(member);
        team.setMemberCount(team.getMemberCount() + 1);
        updateById(team);
    }

    @Override
    @Transactional
    public void rejectMember(Long teamId, Long userId, Long memberId) {
        Team team = getById(teamId);
        if (team == null || !team.getLeaderId().equals(userId)) {
            throw new BusinessException("仅队长可审核成员");
        }
        TeamMember member = teamMemberMapper.selectById(memberId);
        if (member == null || !member.getTeamId().equals(teamId)) {
            throw new BusinessException("成员申请不存在");
        }
        member.setStatus(CommonConstants.MEMBER_REJECTED);
        member.setHandleTime(LocalDateTime.now());
        teamMemberMapper.updateById(member);
    }

    @Override
    @Transactional
    public void removeMember(Long teamId, Long userId, Long memberId) {
        Team team = getById(teamId);
        if (team == null || !team.getLeaderId().equals(userId)) {
            throw new BusinessException("仅队长可移除成员");
        }
        TeamMember member = teamMemberMapper.selectById(memberId);
        if (member == null || !member.getTeamId().equals(teamId)) {
            throw new BusinessException("成员不存在");
        }
        if (member.getRole() == CommonConstants.MEMBER_LEADER) {
            throw new BusinessException("不能移除队长");
        }
        member.setStatus(CommonConstants.MEMBER_REJECTED);
        member.setHandleTime(LocalDateTime.now());
        teamMemberMapper.updateById(member);
        team.setMemberCount(Math.max(0, team.getMemberCount() - 1));
        updateById(team);
    }

    @Override
    @Transactional
    public void dissolveTeam(Long teamId, Long userId) {
        Team team = getById(teamId);
        if (team == null || !team.getLeaderId().equals(userId)) {
            throw new BusinessException("仅队长可解散团队");
        }
        if (team.getStatus() == CommonConstants.TEAM_SUBMITTED) {
            throw new BusinessException("已提交报名的团队不可解散");
        }
        team.setStatus(CommonConstants.TEAM_DISSOLVED);
        updateById(team);
    }

    @Override
    @Transactional
    public void submitForReview(Long teamId, Long userId) {
        Team team = getById(teamId);
        if (team == null || !team.getLeaderId().equals(userId)) {
            throw new BusinessException("仅队长可提交报名");
        }
        team.setStatus(CommonConstants.TEAM_SUBMITTED);
        updateById(team);
    }

    @Override
    public List<TeamMember> listMembers(Long teamId) {
        return teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getStatus, CommonConstants.MEMBER_APPROVED));
    }

    @Override
    public List<TeamMember> listPendingMembers(Long teamId) {
        return teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getStatus, CommonConstants.MEMBER_PENDING));
    }
}
