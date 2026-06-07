package com.contest.team.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.competition.entity.ContestDO;
import com.contest.competition.service.ContestService;
import com.contest.message.service.NotificationService;
import com.contest.register.entity.RegistrationDO;
import com.contest.register.service.AdminNotifyService;
import com.contest.register.service.RegistrationService;
import com.contest.team.entity.TeamDO;
import com.contest.team.entity.TeamMemberDO;
import com.contest.team.mapper.TeamMapper;
import com.contest.team.mapper.TeamMemberMapper;
import com.contest.team.service.TeamService;
import com.contest.user.entity.UserDO;
import com.contest.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** 团队服务实现 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, TeamDO> implements TeamService {

    private final TeamMemberMapper teamMemberMapper;
    private final ContestService contestService;
    private final UserService userService;
    private final RegistrationService registrationService;
    private final NotificationService notificationService;
    private final AdminNotifyService adminNotifyService;

    public TeamServiceImpl(TeamMemberMapper teamMemberMapper, ContestService contestService, UserService userService, RegistrationService registrationService, NotificationService notificationService, AdminNotifyService adminNotifyService) {
        this.teamMemberMapper = teamMemberMapper;
        this.contestService = contestService;
        this.userService = userService;
        this.registrationService = registrationService;
        this.notificationService = notificationService;
        this.adminNotifyService = adminNotifyService;
    }

    /** 创建团队：初始化团队信息，队长自动成为已批准的成员 */
    @Override
    @Transactional
    public TeamDO createTeam(Long userId, String teamName, Long teacherId) {
        TeamDO team = new TeamDO();
        team.setLeaderId(userId);
        team.setTeamName(teamName);
        team.setTeacherId(teacherId);
        team.setTeamNo("T" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        team.setStatus(CommonConstants.TEAM_FORMING);
        team.setMemberCount(1);
        save(team);

        TeamMemberDO leader = new TeamMemberDO();
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
    @Transactional
    public String generateInviteCode(Long teamId, Long userId) {
        TeamDO team = getById(teamId);
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

    /** 通过邀请码加入团队：校验邀请码有效性、过期时间、团队状态；去重校验后插入或更新申请记录；通知队长 */
    @Override
    @Transactional
    public TeamDO joinByInviteCode(Long userId, String inviteCode) {
        TeamDO team = getOne(new LambdaQueryWrapper<TeamDO>()
                .eq(TeamDO::getInviteCode, inviteCode));
        if (team == null) {
            throw new BusinessException("邀请码无效");
        }
        if (team.getInviteCodeExpire() != null && team.getInviteCodeExpire().isBefore(LocalDateTime.now())) {
            throw new BusinessException("邀请码已过期");
        }
        if (team.getStatus() != CommonConstants.TEAM_FORMING) {
            throw new BusinessException("该团队当前无法加入");
        }

        TeamMemberDO existing = teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMemberDO>()
                .eq(TeamMemberDO::getUserId, userId)
                .eq(TeamMemberDO::getTeamId, team.getId()));
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
            TeamMemberDO member = new TeamMemberDO();
            member.setTeamId(team.getId());
            member.setUserId(userId);
            member.setRole(CommonConstants.MEMBER_NORMAL);
            member.setStatus(CommonConstants.MEMBER_PENDING);
            member.setApplyTime(LocalDateTime.now());
            teamMemberMapper.insert(member);
        }

        UserDO applicant = userService.getById(userId);
        String applicantName = applicant != null ? applicant.getName() : "用户" + userId;
        notificationService.sendNotification(team.getLeaderId(), CommonConstants.NOTIFY_TEAM_APPLY,
                "新成员申请", applicantName + " 申请加入你的团队「" + team.getTeamName() + "」，请及时处理。",
                team.getId(), "team");

        return team;
    }

    /** 批准成员加入：队长校验 → 人数上限校验 → 状态前置校验 → 更新成员状态并增加团队人数 → 发送通知 */
    @Override
    @Transactional
    public void approveMember(Long teamId, Long userId, Long memberId) {
        TeamDO team = getById(teamId);
        if (team == null || !team.getLeaderId().equals(userId)) {
            throw new BusinessException("仅队长可审核成员");
        }
        Integer maxSize = resolveTeamMaxSize(teamId);
        int currentCount = team.getMemberCount() != null ? team.getMemberCount() : 0;
        if (maxSize != null && currentCount + 1 > maxSize) {
            throw new BusinessException("团队人数已达上限（最多" + maxSize + "人），无法添加更多成员");
        }
        TeamMemberDO member = teamMemberMapper.selectById(memberId);
        if (member == null || !member.getTeamId().equals(teamId)) {
            throw new BusinessException("成员申请不存在");
        }
        if (member.getStatus() != CommonConstants.MEMBER_PENDING) {
            throw new BusinessException("仅待处理的申请可批准");
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
        TeamDO team = getById(teamId);
        if (team == null || !team.getLeaderId().equals(userId)) {
            throw new BusinessException("仅队长可审核成员");
        }
        TeamMemberDO member = teamMemberMapper.selectById(memberId);
        if (member == null || !member.getTeamId().equals(teamId)) {
            throw new BusinessException("成员申请不存在");
        }
        if (member.getStatus() != CommonConstants.MEMBER_PENDING && member.getStatus() != CommonConstants.MEMBER_APPROVED) {
            throw new BusinessException("该成员当前状态不允许此操作");
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
        TeamDO team = getById(teamId);
        if (team == null || !team.getLeaderId().equals(userId)) {
            throw new BusinessException("仅队长可移除成员");
        }
        TeamMemberDO member = teamMemberMapper.selectById(memberId);
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
        TeamDO team = getById(teamId);
        if (team == null || !team.getLeaderId().equals(userId)) {
            throw new BusinessException("仅队长可解散团队");
        }

        removeById(teamId);

        List<TeamMemberDO> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMemberDO>()
                .eq(TeamMemberDO::getTeamId, teamId)
                .in(TeamMemberDO::getStatus, CommonConstants.MEMBER_PENDING, CommonConstants.MEMBER_APPROVED));
        for (TeamMemberDO m : members) {
            m.setStatus(CommonConstants.MEMBER_REJECTED);
            teamMemberMapper.updateById(m);
            if (!m.getUserId().equals(userId)) {
                notificationService.sendNotification(m.getUserId(), CommonConstants.NOTIFY_SYSTEM,
                        "团队已解散", "你所在的团队「" + team.getTeamName() + "」已被队长解散。",
                        teamId, "team");
            }
        }

        List<RegistrationDO> regs = registrationService.lambdaQuery()
                .eq(RegistrationDO::getTeamId, teamId)
                .ne(RegistrationDO::getStatus, CommonConstants.REG_CANCELLED)
                .list();
        for (RegistrationDO reg : regs) {
            boolean wasApproved = reg.getStatus() == CommonConstants.REG_APPROVED;
            reg.setStatus(CommonConstants.REG_CANCELLED);
            registrationService.updateById(reg);
            if (wasApproved) {
                ContestDO contest = contestService.getById(reg.getContestId());
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
        TeamMemberDO member = teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMemberDO>()
                .eq(TeamMemberDO::getTeamId, teamId)
                .eq(TeamMemberDO::getUserId, userId));
        if (member == null) {
            throw new BusinessException("你不是该团队成员");
        }
        if (member.getRole() == CommonConstants.MEMBER_LEADER) {
            throw new BusinessException("队长不能退出，请解散团队");
        }
        if (member.getStatus() != CommonConstants.MEMBER_APPROVED) {
            throw new BusinessException("你不是该团队的正式成员");
        }
        member.setStatus(CommonConstants.MEMBER_REJECTED);
        member.setHandleTime(LocalDateTime.now());
        teamMemberMapper.updateById(member);
        TeamDO team = getById(teamId);
        if (team != null) {
            team.setMemberCount(Math.max(0, team.getMemberCount() - 1));
            updateById(team);
            var user = userService.getById(userId);
            String userName = user != null ? user.getName() : "有成员";
            notificationService.sendNotification(team.getLeaderId(), CommonConstants.NOTIFY_SYSTEM,
                    "成员退出", userName + " 退出了团队「" + team.getTeamName() + "」。",
                    teamId, "team");
        }
        List<RegistrationDO> regs = registrationService.lambdaQuery()
                .eq(RegistrationDO::getTeamId, teamId)
                .eq(RegistrationDO::getUserId, userId)
                .ne(RegistrationDO::getStatus, CommonConstants.REG_CANCELLED)
                .list();
        for (RegistrationDO reg : regs) {
            reg.setStatus(CommonConstants.REG_CANCELLED);
            registrationService.updateById(reg);
        }
    }

    @Override
    @Transactional
    public void submitForReview(Long teamId, Long userId) {
        TeamDO team = getById(teamId);
        if (team == null || !team.getLeaderId().equals(userId)) {
            throw new BusinessException("仅队长可提交报名");
        }
        Integer minSize = resolveTeamMinSize(teamId);
        if (minSize == null) {
            throw new BusinessException("团队尚未关联竞赛，无法确定最少人数要求");
        }
        int currentCount = team.getMemberCount() != null ? team.getMemberCount() : 0;
        if (currentCount < minSize) {
            throw new BusinessException("团队人数不足最低要求（至少" + minSize + "人），无法提交审核");
        }
        long approvedNonLeaderCount = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMemberDO>()
                .eq(TeamMemberDO::getTeamId, teamId)
                .eq(TeamMemberDO::getStatus, CommonConstants.MEMBER_APPROVED)
                .ne(TeamMemberDO::getRole, CommonConstants.MEMBER_LEADER));
        if (approvedNonLeaderCount == 0) {
            throw new BusinessException("团队中暂无已通过的普通成员，无法提交审核");
        }
        team.setStatus(CommonConstants.TEAM_SUBMITTED);
        updateById(team);
        adminNotifyService.notifyAdmins(CommonConstants.NOTIFY_SYSTEM, "团队提交审核",
                "团队「" + team.getTeamName() + "」已提交审核申请，请及时审批。",
                teamId, "team");
    }

    @Override
    public List<TeamMemberDO> listMembers(Long teamId) {
        List<TeamMemberDO> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMemberDO>()
                .eq(TeamMemberDO::getTeamId, teamId)
                .eq(TeamMemberDO::getStatus, CommonConstants.MEMBER_APPROVED));
        return enrichWithUserName(members);
    }

    @Override
    public List<TeamMemberDO> listPendingMembers(Long teamId) {
        List<TeamMemberDO> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMemberDO>()
                .eq(TeamMemberDO::getTeamId, teamId)
                .eq(TeamMemberDO::getStatus, CommonConstants.MEMBER_PENDING));
        return enrichWithUserName(members);
    }

    private List<TeamMemberDO> enrichWithUserName(List<TeamMemberDO> members) {
        List<Long> userIds = members.stream()
                .map(TeamMemberDO::getUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return members;
        }
        List<UserDO> users = userService.listByIds(userIds);
        java.util.Map<Long, UserDO> userMap = users.stream()
                .collect(Collectors.toMap(UserDO::getId, u -> u));
        return members.stream().peek(m -> {
            if (m.getUserId() != null) {
                UserDO user = userMap.get(m.getUserId());
                m.setUserName(user != null ? user.getName() : null);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public List<TeamDO> getTeamsByLeader(Long userId) {
        return list(new LambdaQueryWrapper<TeamDO>()
                .eq(TeamDO::getLeaderId, userId));
    }

    @Override
    public IPage<TeamDO> pageTeams(Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<TeamDO> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(TeamDO::getStatus, status);
        }
        wrapper.orderByDesc(TeamDO::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /** 管理员通过团队审核：校验团队已提交 → 更新状态 → 自动通过关联的报名记录 → 发送通知 */
    @Override
    @Transactional
    public void adminApproveTeam(Long teamId) {
        TeamDO team = getById(teamId);
        if (team == null) throw new BusinessException("团队不存在");
        if (team.getStatus() != CommonConstants.TEAM_SUBMITTED) {
            throw new BusinessException("该团队未提交审核");
        }
        team.setStatus(CommonConstants.TEAM_APPROVED);
        updateById(team);
        RegistrationDO reg = registrationService.lambdaQuery()
                .eq(RegistrationDO::getTeamId, teamId)
                .eq(RegistrationDO::getStatus, CommonConstants.REG_PENDING)
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
        TeamDO team = getById(teamId);
        if (team == null) throw new BusinessException("团队不存在");
        team.setStatus(CommonConstants.TEAM_REJECTED);
        team.setMemberCount(0);
        updateById(team);

        List<RegistrationDO> regs = registrationService.lambdaQuery()
                .eq(RegistrationDO::getTeamId, teamId)
                .ne(RegistrationDO::getStatus, CommonConstants.REG_CANCELLED)
                .list();
        for (RegistrationDO reg : regs) {
            boolean wasApproved = reg.getStatus() == CommonConstants.REG_APPROVED;
            reg.setStatus(CommonConstants.REG_REJECTED);
            reg.setReviewReason(reason);
            registrationService.updateById(reg);
            if (wasApproved) {
                ContestDO contest = contestService.getById(reg.getContestId());
                if (contest != null && contest.getCurrentCount() != null && contest.getCurrentCount() > 0) {
                    contest.setCurrentCount(contest.getCurrentCount() - 1);
                    contestService.updateById(contest);
                }
            }
        }

        List<TeamMemberDO> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMemberDO>()
                .eq(TeamMemberDO::getTeamId, teamId)
                .in(TeamMemberDO::getStatus, CommonConstants.MEMBER_PENDING, CommonConstants.MEMBER_APPROVED));
        for (TeamMemberDO m : members) {
            m.setStatus(CommonConstants.MEMBER_REJECTED);
            teamMemberMapper.updateById(m);
        }
        String rejectMsg = "你的团队「" + team.getTeamName() + "」已被管理员驳回。原因：" + reason;
        notificationService.sendNotification(team.getLeaderId(), CommonConstants.NOTIFY_TEAM_RESULT,
                "团队审核未通过", rejectMsg, teamId, "team");
        for (TeamMemberDO m : members) {
            if (!m.getUserId().equals(team.getLeaderId())) {
                notificationService.sendNotification(m.getUserId(), CommonConstants.NOTIFY_TEAM_RESULT,
                        "团队审核未通过", "你所在的团队「" + team.getTeamName() + "」已被管理员驳回。原因：" + reason,
                        teamId, "team");
            }
        }
    }

    private Integer resolveTeamMaxSize(Long teamId) {
        List<RegistrationDO> regs = registrationService.lambdaQuery()
                .eq(RegistrationDO::getTeamId, teamId)
                .ne(RegistrationDO::getStatus, CommonConstants.REG_CANCELLED)
                .list();
        if (!regs.isEmpty()) {
            ContestDO contest = contestService.getById(regs.get(0).getContestId());
            if (contest != null && contest.getTeamMaxSize() != null && contest.getTeamMaxSize() > 0) {
                return contest.getTeamMaxSize();
            }
        }
        return null;
    }

    private Integer resolveTeamMinSize(Long teamId) {
        List<RegistrationDO> regs = registrationService.lambdaQuery()
                .eq(RegistrationDO::getTeamId, teamId)
                .ne(RegistrationDO::getStatus, CommonConstants.REG_CANCELLED)
                .list();
        if (!regs.isEmpty()) {
            ContestDO contest = contestService.getById(regs.get(0).getContestId());
            if (contest != null && contest.getTeamMinSize() != null && contest.getTeamMinSize() > 0) {
                return contest.getTeamMinSize();
            }
        }
        return null;
    }

    @Override
    public List<TeamDO> listUserTeams(Long userId) {
        List<TeamMemberDO> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMemberDO>()
                .eq(TeamMemberDO::getUserId, userId)
                .in(TeamMemberDO::getStatus, CommonConstants.MEMBER_PENDING, CommonConstants.MEMBER_APPROVED));
        if (members.isEmpty()) return List.of();
        List<Long> teamIds = members.stream().map(TeamMemberDO::getTeamId).collect(Collectors.toList());
        return listByIds(teamIds);
    }

    @Override
    @Transactional
    public void setTeacher(Long teamId, Long teacherId, Long userId) {
        TeamDO team = getById(teamId);
        if (team == null) {
            throw new BusinessException("团队不存在");
        }
        if (!team.getLeaderId().equals(userId)) {
            throw new BusinessException("仅队长可设置指导教师");
        }
        if (teacherId != null) {
            UserDO teacher = userService.getById(teacherId);
            if (teacher == null) {
                throw new BusinessException("教师用户不存在");
            }
            if (teacher.getRole() != CommonConstants.ROLE_TEACHER) {
                throw new BusinessException("选择的用户不是教师角色");
            }
        }
        team.setTeacherId(teacherId);
        updateById(team);
    }

    @Override
    public List<TeamDO> getTeamsByTeacher(Long teacherId) {
        return list(new LambdaQueryWrapper<TeamDO>()
                .eq(TeamDO::getTeacherId, teacherId));
    }
}
