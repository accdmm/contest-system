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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 团队服务实现
 *
 * 处理团队创建、成员管理、加入审批、团队报名等业务逻辑。
 * 队长创建团队时生成6位唯一邀请码，成员通过邀请码申请加入，
 * 队长审批成员申请，团队人数达标后方可提交报名审核。
 *
 * 事务说明：团队创建、成员管理、审批、解散等核心操作均标注 @Transactional，
 * 确保团队表、成员表、报名表之间的数据一致性（如解散团队时同步取消关联报名）。
 *
 * 安全性说明：关键操作（加入、审批、移出、解散等）均校验当前用户身份，
 * 确保只有队长可管理团队。
 */
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

    /**
     * 创建团队：初始化团队信息，队长自动成为已批准的成员
     *
     * 创建流程：
     * 1. 生成团队编号（T + 时间戳，精确到秒）
     * 2. 初始化状态为组建中（TEAM_FORMING），成员计数为 1
     * 3. 在 team_member 表中插入队长记录（角色=队长，状态=已通过）
     *
     * @param userId    队长用户 ID
     * @param teamName  团队名称
     * @param teacherId 指导教师 ID（可选）
     * @return 创建的团队对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 生成 6 位唯一邀请码
     *
     * 使用 UUID 的前 6 位并转大写，碰撞概率极低。有效期 7 天。
     * 邀请码存入 team 表的 invite_code 字段，通过唯一索引保证不重复。
     *
     * @param teamId 团队 ID
     * @param userId 请求用户 ID（需为队长）
     * @return 6 位大写邀请码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 通过邀请码加入团队
     *
     * 校验流程：邀请码有效性 → 过期时间 → 团队状态（仅 TEAM_FORMING 可加入）→
     * 重复校验（已通过成员不可再次加入）→ 更新或插入申请记录 → 通知队长。
     *
     * 已拒绝或已移除的成员可重新申请，此时更新原有记录的 status 为 MEMBER_PENDING。
     *
     * @param userId     申请用户 ID
     * @param inviteCode 6 位邀请码
     * @return 目标团队
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 批准成员加入
     *
     * 校验：仅队长可操作 → 团队人数上限检查 → 成员申请存在性 → 重复审批检测
     * → 更新成员状态为已通过 → 增加团队 memberCount → 发送通知。
     *
     * @param teamId   团队 ID
     * @param userId   队长用户 ID
     * @param memberId 成员申请记录 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 拒绝成员加入 / 移除已批准的成员
     *
     * 支持拒绝待审批和已批准的成员。若移除已批准的成员，递减 team.memberCount。
     *
     * @param teamId   团队 ID
     * @param userId   队长用户 ID
     * @param memberId 成员记录 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 移除团队成员
     *
     * 仅队长可操作，不能移除队长本人。移除后成员状态置为 MEMBER_REJECTED，
     * 递减团队 memberCount。
     *
     * @param teamId   团队 ID
     * @param userId   队长用户 ID
     * @param memberId 成员记录 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 解散团队
     *
     * 仅队长可操作。操作流程：
     * 1. 逻辑删除团队记录（@TableLogic 设为已删除）
     * 2. 将待审批和已通过的成员记录置为已拒绝
     * 3. 取消该团队的所有报名（已完成报名时递减竞赛 currentCount）
     * 4. 逐个发送通知给所有受影响的成员
     *
     * @param teamId 团队 ID
     * @param userId 队长用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

        // 批量查询关联竞赛，避免 N+1 问题
        List<Long> contestIds = regs.stream()
                .map(RegistrationDO::getContestId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, ContestDO> contestMap = contestIds.isEmpty() ?
                java.util.Collections.emptyMap() :
                contestService.listByIds(contestIds).stream()
                        .collect(Collectors.toMap(ContestDO::getId, c -> c));

        for (RegistrationDO reg : regs) {
            boolean wasApproved = reg.getStatus() == CommonConstants.REG_APPROVED;
            reg.setStatus(CommonConstants.REG_CANCELLED);
            registrationService.updateById(reg);
            if (wasApproved) {
                ContestDO contest = contestMap.get(reg.getContestId());
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

    /**
     * 成员主动退出团队
     *
     * 非队长成员可主动退出。退出的操作顺序：
     * 更新成员状态 → 递减团队 memberCount → 通知队长 → 取消该成员在团队关联的报名。
     *
     * @param teamId 团队 ID
     * @param userId 退出用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 提交团队报名审核
     *
     * 仅队长可操作。校验条件：
     * - 团队需已关联竞赛（通过报名记录反查）
     * - 队员数需达到竞赛设定的团队最少人数
     * - 至少有一名已通过的普通成员（队长不算）
     * 通过后将团队状态置为 TEAM_SUBMITTED，通知管理员审批。
     *
     * @param teamId 团队 ID
     * @param userId 队长用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 查询团队成员列表（仅已通过的成员）
     *
     * 自动填充用户名称（通过 userService.listByIds 批量查询）。
     *
     * @param teamId 团队 ID
     * @return 成员列表
     */
    @Override
    public List<TeamMemberDO> listMembers(Long teamId) {
        List<TeamMemberDO> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMemberDO>()
                .eq(TeamMemberDO::getTeamId, teamId)
                .eq(TeamMemberDO::getStatus, CommonConstants.MEMBER_APPROVED));
        return enrichWithUserName(members);
    }

    /**
     * 查询待审批的成员申请列表
     *
     * @param teamId 团队 ID
     * @return 待审批成员列表
     */
    @Override
    public List<TeamMemberDO> listPendingMembers(Long teamId) {
        List<TeamMemberDO> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMemberDO>()
                .eq(TeamMemberDO::getTeamId, teamId)
                .eq(TeamMemberDO::getStatus, CommonConstants.MEMBER_PENDING));
        return enrichWithUserName(members);
    }

    /**
     * 批量填充成员列表的用户名称
     *
     * 使用 selectBatchIds + Map 映射避免 N+1 查询。
     *
     * @param members 成员列表（不含 userName）
     * @return 含 userName 的成员列表
     */
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

    /**
     * 获取用户作为队长创建的团队列表
     *
     * @param userId 用户 ID
     * @return 团队列表
     */
    @Override
    public List<TeamDO> getTeamsByLeader(Long userId) {
        return list(new LambdaQueryWrapper<TeamDO>()
                .eq(TeamDO::getLeaderId, userId));
    }

    /**
     * 分页查询所有团队（管理员后台用）
     *
     * @param status 团队状态筛选（可选）
     * @param page   页码
     * @param size   每页条数
     * @return 团队分页数据
     */
    @Override
    public IPage<TeamDO> pageTeams(Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<TeamDO> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(TeamDO::getStatus, status);
        }
        wrapper.orderByDesc(TeamDO::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 管理员通过团队审核
     *
     * 校验团队已提交 → 更新状态为已通过 → 自动审批关联的报名记录
     * → 发送通知给队长。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 管理员驳回团队
     *
     * 校验驳回原因长度 → 更新团队状态为已驳回 → 取消关联的所有报名
     * （递减已通过报名的竞赛 currentCount）→ 拒绝所有成员 → 发送通知。
     *
     * @param teamId 团队 ID
     * @param reason 驳回原因（不少于 5 个字符）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

        // 批量查询关联竞赛，避免 N+1 问题
        List<Long> contestIds = regs.stream()
                .map(RegistrationDO::getContestId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, ContestDO> contestMap = contestIds.isEmpty() ?
                java.util.Collections.emptyMap() :
                contestService.listByIds(contestIds).stream()
                        .collect(Collectors.toMap(ContestDO::getId, c -> c));

        for (RegistrationDO reg : regs) {
            boolean wasApproved = reg.getStatus() == CommonConstants.REG_APPROVED;
            reg.setStatus(CommonConstants.REG_REJECTED);
            reg.setReviewReason(reason);
            registrationService.updateById(reg);
            if (wasApproved) {
                ContestDO contest = contestMap.get(reg.getContestId());
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

    /**
     * 解析团队关联竞赛的最大人数限制
     *
     * 通过该团队的所有非取消报名记录，反查竞赛的 teamMaxSize。
     * 若团队尚未报名则返回 null。
     */
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

    /**
     * 解析团队关联竞赛的最小人数限制
     *
     * 通过该团队的所有非取消报名记录，反查竞赛的 teamMinSize。
     * 若团队尚未报名则返回 null。
     */
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

    /**
     * 获取用户所在的所有团队（含待审批和已加入的）
     *
     * 先查询 team_member 表获取用户关联的团队 ID，
     * 再批量查询 team 表返回列表。
     *
     * @param userId 用户 ID
     * @return 团队列表（不包括被拒绝的）
     */
    @Override
    public List<TeamDO> listUserTeams(Long userId) {
        List<TeamMemberDO> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMemberDO>()
                .eq(TeamMemberDO::getUserId, userId)
                .in(TeamMemberDO::getStatus, CommonConstants.MEMBER_PENDING, CommonConstants.MEMBER_APPROVED));
        if (members.isEmpty()) return List.of();
        List<Long> teamIds = members.stream().map(TeamMemberDO::getTeamId).collect(Collectors.toList());
        return listByIds(teamIds);
    }

    /**
     * 设置或更换指导教师
     *
     * 仅队长可操作。校验教师用户是否存在且角色为教师（role=2）。
     * 传入 null 可清空指导教师。
     *
     * @param teamId    团队 ID
     * @param teacherId 新指导教师 ID（null 表示清空）
     * @param userId    操作人用户 ID（需为队长）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 获取教师指导的所有团队列表
     *
     * @param teacherId 教师用户 ID
     * @return 团队列表
     */
    @Override
    public List<TeamDO> getTeamsByTeacher(Long teacherId) {
        return list(new LambdaQueryWrapper<TeamDO>()
                .eq(TeamDO::getTeacherId, teacherId));
    }
}
