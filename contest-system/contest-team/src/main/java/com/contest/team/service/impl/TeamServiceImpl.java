package com.contest.team.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.competition.entity.Contest;
import com.contest.competition.service.ContestService;
import com.contest.message.service.NotificationService;
import com.contest.register.entity.Registration;
import com.contest.register.service.RegistrationService;
import com.contest.team.entity.Team;
import com.contest.team.entity.TeamMember;
import com.contest.team.mapper.TeamMapper;
import com.contest.team.mapper.TeamMemberMapper;
import com.contest.team.service.TeamService;
import com.contest.user.entity.User;
import com.contest.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    private final TeamMemberMapper teamMemberMapper;
    private final ContestService contestService;
    private final UserService userService;
    private final RegistrationService registrationService;
    private final NotificationService notificationService;

    public TeamServiceImpl(TeamMemberMapper teamMemberMapper, ContestService contestService, UserService userService, RegistrationService registrationService, NotificationService notificationService) {
        this.teamMemberMapper = teamMemberMapper;
        this.contestService = contestService;
        this.userService = userService;
        this.registrationService = registrationService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Team createTeam(Long userId, String teamName) {
        Team team = new Team();
        team.setLeaderId(userId);
        team.setTeamName(teamName);
        team.setTeamNo("T" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        team.setStatus(CommonConstants.TEAM_FORMING);
        team.setMemberCount(1);
        save(team);

        TeamMember leader = new TeamMember();
        leader.setTeamId(team.getId());
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

        TeamMember existing = teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getTeamId, team.getId()));
        if (existing != null && existing.getStatus() == CommonConstants.MEMBER_APPROVED) {
            throw new BusinessException("你已经是该团队成员");
        }
        if (existing != null) {
            existing.setTeamId(team.getId());
            existing.setRole(CommonConstants.MEMBER_NORMAL);
            existing.setStatus(CommonConstants.MEMBER_PENDING);
            existing.setApplyTime(LocalDateTime.now());
            existing.setHandleTime(null);
            teamMemberMapper.updateById(existing);
        } else {
            TeamMember member = new TeamMember();
            member.setTeamId(team.getId());
            member.setUserId(userId);
            member.setRole(CommonConstants.MEMBER_NORMAL);
            member.setStatus(CommonConstants.MEMBER_PENDING);
            member.setApplyTime(LocalDateTime.now());
            teamMemberMapper.insert(member);
        }

        User applicant = userService.getById(userId);
        String applicantName = applicant != null ? applicant.getName() : "用户" + userId;
        notificationService.sendNotification(team.getLeaderId(), CommonConstants.NOTIFY_TEAM_APPLY,
                "新成员申请", applicantName + " 申请加入你的团队「" + team.getTeamName() + "」，请及时处理。",
                team.getId(), "team");

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
        notificationService.sendNotification(member.getUserId(), CommonConstants.NOTIFY_TEAM_RESULT,
                "加入申请已通过", "你已被队长通过加入团队「" + team.getTeamName() + "」的申请。",
                teamId, "team");
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
        boolean wasApproved = member.getStatus() == CommonConstants.MEMBER_APPROVED;
        member.setStatus(CommonConstants.MEMBER_REJECTED);
        member.setHandleTime(LocalDateTime.now());
        teamMemberMapper.updateById(member);
        if (wasApproved) {
            team.setMemberCount(Math.max(0, team.getMemberCount() - 1));
            updateById(team);
        }
        notificationService.sendNotification(member.getUserId(), CommonConstants.NOTIFY_TEAM_RESULT,
                "加入申请未通过", "你加入团队「" + team.getTeamName() + "」的申请已被队长拒绝。",
                teamId, "team");
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

        removeById(teamId);

        List<TeamMember> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .in(TeamMember::getStatus, CommonConstants.MEMBER_PENDING, CommonConstants.MEMBER_APPROVED));
        for (TeamMember m : members) {
            m.setStatus(CommonConstants.MEMBER_REJECTED);
            teamMemberMapper.updateById(m);
            if (!m.getUserId().equals(userId)) {
                notificationService.sendNotification(m.getUserId(), CommonConstants.NOTIFY_SYSTEM,
                        "团队已解散", "你所在的团队「" + team.getTeamName() + "」已被队长解散。",
                        teamId, "team");
            }
        }

        Registration reg = registrationService.lambdaQuery()
                .eq(Registration::getTeamId, teamId)
                .ne(Registration::getStatus, CommonConstants.REG_CANCELLED)
                .one();
        if (reg != null) {
            boolean wasApproved = reg.getStatus() == CommonConstants.REG_APPROVED;
            reg.setStatus(CommonConstants.REG_CANCELLED);
            registrationService.updateById(reg);
            if (wasApproved) {
                Contest contest = contestService.getById(reg.getContestId());
                if (contest != null && contest.getCurrentCount() != null && contest.getCurrentCount() > 0) {
                    contest.setCurrentCount(contest.getCurrentCount() - 1);
                    contestService.updateById(contest);
                }
            }
            notificationService.sendNotification(userId, CommonConstants.NOTIFY_SYSTEM,
                    "报名已取消", "由于团队已解散，竞赛报名已自动取消。",
                    reg.getContestId(), "contest");
        }
    }

    @Override
    @Transactional
    public void leaveTeam(Long teamId, Long userId) {
        TeamMember member = teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId));
        if (member == null) {
            throw new BusinessException("你不是该团队成员");
        }
        if (member.getRole() == CommonConstants.MEMBER_LEADER) {
            throw new BusinessException("队长不能退出，请解散团队");
        }
        if (member.getStatus() != CommonConstants.MEMBER_APPROVED) {
            throw new BusinessException("你不是该团队的正式成员");
        }
        teamMemberMapper.deleteById(member.getId());
        Team team = getById(teamId);
        if (team != null) {
            team.setMemberCount(Math.max(0, team.getMemberCount() - 1));
            updateById(team);
            var user = userService.getById(userId);
            String userName = user != null ? user.getName() : "有成员";
            notificationService.sendNotification(team.getLeaderId(), CommonConstants.NOTIFY_SYSTEM,
                    "成员退出", userName + " 退出了团队「" + team.getTeamName() + "」。",
                    teamId, "team");
        }
    }

    private void notifyAdmins(Integer type, String title, String content, Long relatedId, String relatedType) {
        List<User> admins = userService.list(new LambdaQueryWrapper<User>().eq(User::getRole, 1));
        admins.forEach(admin -> notificationService.sendNotification(admin.getId(), type, title, content, relatedId, relatedType));
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
        notifyAdmins(CommonConstants.NOTIFY_SYSTEM, "团队提交审核",
                "团队「" + team.getTeamName() + "」已提交审核申请，请及时审批。",
                teamId, "team");
    }

    @Override
    public List<TeamMember> listMembers(Long teamId) {
        List<TeamMember> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getStatus, CommonConstants.MEMBER_APPROVED));
        return enrichWithUserName(members);
    }

    @Override
    public List<TeamMember> listPendingMembers(Long teamId) {
        List<TeamMember> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getStatus, CommonConstants.MEMBER_PENDING));
        return enrichWithUserName(members);
    }

    private List<TeamMember> enrichWithUserName(List<TeamMember> members) {
        return members.stream().peek(m -> {
            if (m.getUserId() != null) {
                var user = userService.getById(m.getUserId());
                m.setUserName(user != null ? user.getName() : null);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public List<Team> getTeamsByLeader(Long userId) {
        return list(new LambdaQueryWrapper<Team>()
                .eq(Team::getLeaderId, userId));
    }

    @Override
    public IPage<Team> pageTeams(Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Team::getStatus, status);
        }
        wrapper.orderByDesc(Team::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public void adminApproveTeam(Long teamId) {
        Team team = getById(teamId);
        if (team == null) throw new BusinessException("团队不存在");
        if (team.getStatus() != CommonConstants.TEAM_SUBMITTED) {
            throw new BusinessException("该团队未提交审核");
        }
        team.setStatus(CommonConstants.TEAM_APPROVED);
        updateById(team);
        Registration reg = registrationService.lambdaQuery()
                .eq(Registration::getTeamId, teamId)
                .eq(Registration::getStatus, CommonConstants.REG_PENDING)
                .one();
        if (reg != null) {
            registrationService.approveRegistration(reg.getId());
        }
        notificationService.sendNotification(team.getLeaderId(), CommonConstants.NOTIFY_TEAM_RESULT,
                "团队审核通过", "你的团队「" + team.getTeamName() + "」已通过管理员审核。",
                teamId, "team");
    }

    @Override
    @Transactional
    public void adminRejectTeam(Long teamId, String reason) {
        if (reason == null || reason.trim().length() < 5) {
            throw new BusinessException("驳回原因不少于5个字符");
        }
        Team team = getById(teamId);
        if (team == null) throw new BusinessException("团队不存在");
        team.setStatus(CommonConstants.TEAM_REJECTED);
        team.setMemberCount(0);
        updateById(team);

        List<TeamMember> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .in(TeamMember::getStatus, CommonConstants.MEMBER_PENDING, CommonConstants.MEMBER_APPROVED));
        for (TeamMember m : members) {
            m.setStatus(CommonConstants.MEMBER_REJECTED);
            teamMemberMapper.updateById(m);
        }
        String rejectMsg = "你的团队「" + team.getTeamName() + "」已被管理员驳回。原因：" + reason;
        notificationService.sendNotification(team.getLeaderId(), CommonConstants.NOTIFY_TEAM_RESULT,
                "团队审核未通过", rejectMsg, teamId, "team");
        for (TeamMember m : members) {
            if (!m.getUserId().equals(team.getLeaderId())) {
                notificationService.sendNotification(m.getUserId(), CommonConstants.NOTIFY_TEAM_RESULT,
                        "团队审核未通过", "你所在的团队「" + team.getTeamName() + "」已被管理员驳回。原因：" + reason,
                        teamId, "team");
            }
        }
    }

    @Override
    public List<Team> listUserTeams(Long userId) {
        List<TeamMember> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, userId)
                .in(TeamMember::getStatus, CommonConstants.MEMBER_PENDING, CommonConstants.MEMBER_APPROVED));
        if (members.isEmpty()) return List.of();
        List<Long> teamIds = members.stream().map(TeamMember::getTeamId).collect(Collectors.toList());
        return listByIds(teamIds);
    }
}
