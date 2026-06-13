package com.contest.register.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.common.service.TeamValidator;
import com.contest.competition.entity.Contest;
import com.contest.competition.service.ContestService;
import com.contest.message.service.NotificationService;
import com.contest.register.entity.Registration;
import com.contest.register.mapper.RegistrationMapper;
import com.contest.register.service.AdminNotifyService;
import com.contest.register.service.RegistrationService;
import com.contest.user.entity.User;
import com.contest.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * 报名服务实现，包含个人/团队报名、审核、取消及报名校验等核心业务逻辑
 *
 * 事务说明：报名、审核、驳回、取消等操作均标注 @Transactional，
 * 确保报名记录、竞赛人数、通知发送等操作的数据一致性。
 *
 * 并发安全性说明：
 * - 重复报名校验通过 count 查询 + 数据库唯一约束（业务层面无唯一约束，通过查询保证）
 * - 人数上限校验：报名时检查 count，审核时再次检查 currentCount，双层校验防止超限
 * - 活跃报名数校验：每人同时最多 3 个非取消状态报名
 */
@Slf4j
@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService {

    private static final String UNKNOWN_CONTEST = "未知竞赛";

    private final ContestService contestService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final TeamValidator teamValidator;
    private final AdminNotifyService adminNotifyService;

    public RegistrationServiceImpl(ContestService contestService, NotificationService notificationService, UserService userService, TeamValidator teamValidator, AdminNotifyService adminNotifyService) {
        this.contestService = contestService;
        this.notificationService = notificationService;
        this.userService = userService;
        this.teamValidator = teamValidator;
        this.adminNotifyService = adminNotifyService;
    }

    /**
     * 校验竞赛是否可报名
     *
     * 校验顺序：竞赛是否存在 → 是否开放报名 → 报名开始时间 → 报名截止时间 → 报名类型匹配。
     * 任一条件不满足立即抛出 BusinessException，避免无效请求继续执行。
     *
     * @param contestId       竞赛 ID
     * @param requiredRegType 报名类型（个人/团队），与竞赛类型匹配检查
     * @return 校验通过的竞赛对象
     */
    private Contest validateContest(Long contestId, Integer requiredRegType) {
        Contest contest = contestService.getById(contestId);
        if (contest == null) {
            throw new BusinessException("竞赛不存在");
        }
        if (contest.getStatus() != CommonConstants.CONTEST_OPEN) {
            throw new BusinessException("竞赛当前未开放报名");
        }
        LocalDateTime now = LocalDateTime.now();
        if (contest.getRegisterStartTime() != null && now.isBefore(contest.getRegisterStartTime())) {
            throw new BusinessException("报名尚未开始");
        }
        if (contest.getRegisterEndTime() != null && now.isAfter(contest.getRegisterEndTime())) {
            throw new BusinessException("报名已截止");
        }
        if (contest.getContestType() == CommonConstants.CONTEST_PERSONAL && requiredRegType == CommonConstants.REG_TEAM) {
            throw new BusinessException("该竞赛仅限个人报名");
        }
        if (contest.getContestType() == CommonConstants.CONTEST_TEAM && requiredRegType == CommonConstants.REG_PERSONAL) {
            throw new BusinessException("该竞赛仅限团队报名");
        }
        return contest;
    }

    /**
     * 校验报名人数是否已达上限
     *
     * 查询数据库中该竞赛所有非取消状态的报名记录数，与竞赛设定的上限比较。
     * MyBatis-Plus 的 @TableLogic 自动过滤 is_delete=1 的记录。
     *
     * @param contestId       竞赛 ID
     * @param maxParticipants 人数上限（0 或 null 表示不限）
     */
    private void checkMaxParticipants(Long contestId, Integer maxParticipants) {
        if (maxParticipants == null || maxParticipants <= 0) return;
        long currentTotal = count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getContestId, contestId)
                .ne(Registration::getStatus, CommonConstants.REG_CANCELLED));
        if (currentTotal >= maxParticipants) {
            throw new BusinessException("报名人数已满，无法继续报名");
        }
    }

    /**
     * 审核时再次校验人数上限
     *
     * 与 checkMaxParticipants 的区别：此方法读取 Contest 中的 currentCount 字段
     * （已通过审核的计数），而非数据库中报名记录总数。用于审批通过时确保不会超出上限。
     *
     * @param contest 竞赛实体
     */
    private void checkMaxParticipantsForApproval(Contest contest) {
        Integer max = contest.getMaxParticipants();
        if (max != null && max > 0 && contest.getCurrentCount() != null && contest.getCurrentCount() >= max) {
            throw new BusinessException("报名人数已满，无法继续报名");
        }
    }

    /**
     * 个人赛报名
     *
     * 校验流程：竞赛开放状态和时间 → 人数上限 → 重复报名（同一竞赛不可重复报名）→
     * 每人最多 3 个活跃报名 → 创建待审核记录 → 发送管理员通知。
     *
     * @param userId    报名用户 ID
     * @param contestId 目标竞赛 ID
     * @param remark    报名备注（可选）
     * @return 创建的报名记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Registration registerPersonal(Long userId, Long contestId, String remark) {
        log.info("registerPersonal: userId={}, contestId={}", userId, contestId);
        Contest contest = validateContest(contestId, CommonConstants.REG_PERSONAL);
        checkMaxParticipants(contestId, contest.getMaxParticipants());

        long dupCount = count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .eq(Registration::getContestId, contestId)
                .ne(Registration::getStatus, CommonConstants.REG_CANCELLED));
        if (dupCount > 0) {
            log.warn("registerPersonal failed: duplicate registration, userId={}, contestId={}", userId, contestId);
            throw new BusinessException("您已报名该竞赛");
        }

        long activeCount = count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .ne(Registration::getStatus, CommonConstants.REG_CANCELLED));
        if (activeCount >= CommonConstants.MAX_ACTIVE_REGISTRATIONS) {
            throw new BusinessException("每人同时最多报名3个竞赛");
        }

        Registration reg = new Registration();
        reg.setContestId(contestId);
        reg.setUserId(userId);
        reg.setRegType(CommonConstants.REG_PERSONAL);
        reg.setStatus(CommonConstants.REG_PENDING);
        reg.setRemark(remark);
        save(reg);
        User user = userService.getById(userId);
        String userName = user != null ? user.getName() : String.format("用户%d", userId);
        adminNotifyService.notifyAdmins(CommonConstants.NOTIFY_SYSTEM, "新报名申请",
                userName + " 提交了竞赛「" + contest.getName() + "」的个人报名，请及时审核。",
                contestId, CommonConstants.RELATED_TYPE_CONTEST);
        log.info("registerPersonal success: id={}, userId={}, contestId={}", reg.getId(), userId, contestId);
        return reg;
    }

    /**
     * 团队赛报名
     *
     * 校验流程：团队有效性和队长身份 → 竞赛状态和时间 → 人数上限 →
     * 重复报名（同一团队不可重复报名同一竞赛）→ 每人最多 3 个活跃报名 → 创建待审核记录。
     *
     * @param userId    队长用户 ID
     * @param contestId 目标竞赛 ID
     * @param teamId    团队 ID
     * @return 创建的报名记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Registration registerTeam(Long userId, Long contestId, Long teamId) {
        log.info("registerTeam: userId={}, contestId={}, teamId={}", userId, contestId, teamId);
        teamValidator.validateForRegistration(teamId);
        teamValidator.validateTeamLeader(teamId, userId);
        Contest contest = validateContest(contestId, CommonConstants.REG_TEAM);
        checkMaxParticipants(contestId, contest.getMaxParticipants());

        long dupCount = count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getTeamId, teamId)
                .eq(Registration::getContestId, contestId)
                .ne(Registration::getStatus, CommonConstants.REG_CANCELLED));
        if (dupCount > 0) {
            log.warn("registerTeam failed: team already registered, teamId={}, contestId={}", teamId, contestId);
            throw new BusinessException("该团队已报名该竞赛");
        }

        long activeCount = count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .ne(Registration::getStatus, CommonConstants.REG_CANCELLED));
        if (activeCount >= CommonConstants.MAX_ACTIVE_REGISTRATIONS) {
            throw new BusinessException("每人同时最多报名3个竞赛");
        }

        Registration reg = new Registration();
        reg.setContestId(contestId);
        reg.setUserId(userId);
        reg.setTeamId(teamId);
        reg.setRegType(CommonConstants.REG_TEAM);
        reg.setStatus(CommonConstants.REG_PENDING);
        save(reg);
        User user = userService.getById(userId);
        String userName = user != null ? user.getName() : String.format("用户%d", userId);
        adminNotifyService.notifyAdmins(CommonConstants.NOTIFY_SYSTEM, "新团队报名申请",
                userName + " 提交了竞赛「" + contest.getName() + "」的团队报名，请及时审核。",
                contestId, CommonConstants.RELATED_TYPE_CONTEST);
        log.info("registerTeam success: id={}, teamId={}, contestId={}", reg.getId(), teamId, contestId);
        return reg;
    }

    /**
     * 审核通过报名
     *
     * 校验：仅待审核状态可批准 → 检查人数上限 → 递增竞赛 currentCount → 状态流转为已通过 → 发送通知。
     * 通过后不可撤销（如需撤销请使用驳回流程）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRegistration(Long id) {
        log.info("approveRegistration: id={}", id);
        Registration reg = getById(id);
        if (reg == null) {
            log.warn("approveRegistration failed: registration not found, id={}", id);
            throw new BusinessException("报名记录不存在");
        }
        if (reg.getStatus() != CommonConstants.REG_PENDING) {
            throw new BusinessException("仅待审核状态的报名可批准");
        }
        Contest contest = contestService.getById(reg.getContestId());
        if (contest == null) {
            log.warn("approveRegistration failed: contest not found, contestId={}", reg.getContestId());
            throw new BusinessException("关联竞赛不存在，无法审核通过");
        }
        checkMaxParticipantsForApproval(contest);
        contest.setCurrentCount(contest.getCurrentCount() == null ? 1 : contest.getCurrentCount() + 1);
        contestService.updateById(contest);
        reg.setStatus(CommonConstants.REG_APPROVED);
        updateById(reg);

        notificationService.sendNotification(reg.getUserId(), CommonConstants.NOTIFY_REVIEW_RESULT,
                "报名审核通过", "你的竞赛报名已通过审核，请及时查看详情。",
                reg.getContestId(), CommonConstants.RELATED_TYPE_CONTEST);
        log.info("approveRegistration success: id={}, contestId={}, userId={}", id, reg.getContestId(), reg.getUserId());
    }

    /**
     * 驳回报名
     *
     * 校验：驳回原因不少于 5 个字符 → 仅待审核或已通过状态可驳回 → 若之前是已通过状态则递减
     * 竞赛 currentCount → 状态流转为已驳回 → 发送驳回通知（含原因）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRegistration(Long id, String reason) {
        log.info("rejectRegistration: id={}", id);
        if (reason == null || reason.trim().length() < CommonConstants.MIN_REJECT_REASON_LENGTH) {
            log.warn("rejectRegistration failed: reason too short, id={}", id);
            throw new BusinessException("驳回原因不少于5个字符");
        }
        Registration reg = getById(id);
        if (reg == null) {
            log.warn("rejectRegistration failed: registration not found, id={}", id);
            throw new BusinessException("报名记录不存在");
        }
        if (reg.getStatus() != CommonConstants.REG_PENDING && reg.getStatus() != CommonConstants.REG_APPROVED) {
            throw new BusinessException("该报名记录当前状态不允许驳回");
        }
        boolean wasApproved = reg.getStatus() == CommonConstants.REG_APPROVED;
        reg.setStatus(CommonConstants.REG_REJECTED);
        reg.setReviewReason(reason);
        updateById(reg);

        if (wasApproved) {
            Contest contest = contestService.getById(reg.getContestId());
            if (contest == null) {
                log.warn("rejectRegistration: contest not found, contestId={}, skipping count decrement", reg.getContestId());
            } else if (contest.getCurrentCount() != null && contest.getCurrentCount() > 0) {
                contest.setCurrentCount(contest.getCurrentCount() - 1);
                contestService.updateById(contest);
            }
        }

        notificationService.sendNotification(reg.getUserId(), CommonConstants.NOTIFY_REVIEW_RESULT,
                "报名审核未通过", "你的竞赛报名已被驳回，原因：" + reason,
                reg.getContestId(), CommonConstants.RELATED_TYPE_CONTEST);
        log.info("rejectRegistration success: id={}, contestId={}", id, reg.getContestId());
    }

    /**
     * 取消报名（用户主动操作）
     *
     * 校验：仅本人可取消 → 仅待审核或已通过状态可取消 → 若之前是已通过则递减竞赛
     * currentCount → 状态流转为已取消 → 发送管理员通知。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRegistration(Long id, Long userId) {
        log.info("cancelRegistration: id={}, userId={}", id, userId);
        Registration reg = getById(id);
        if (reg == null) {
            log.warn("cancelRegistration failed: registration not found, id={}", id);
            throw new BusinessException("报名记录不存在");
        }
        if (!reg.getUserId().equals(userId)) {
            throw new BusinessException("只能取消自己的报名");
        }
        if (reg.getStatus() != CommonConstants.REG_PENDING && reg.getStatus() != CommonConstants.REG_APPROVED) {
            throw new BusinessException("该报名记录当前状态不允许取消");
        }

        boolean wasApproved = reg.getStatus() == CommonConstants.REG_APPROVED;
        reg.setStatus(CommonConstants.REG_CANCELLED);
        updateById(reg);

        if (wasApproved) {
            Contest contest = contestService.getById(reg.getContestId());
            if (contest != null && contest.getCurrentCount() != null && contest.getCurrentCount() > 0) {
                contest.setCurrentCount(contest.getCurrentCount() - 1);
                contestService.updateById(contest);
            }

        }
        Contest c = contestService.getById(reg.getContestId());
        User user = userService.getById(userId);
        String userName = user != null ? user.getName() : String.format("用户%d", userId);
        String contestName = c != null ? c.getName() : UNKNOWN_CONTEST;
        adminNotifyService.notifyAdmins(CommonConstants.NOTIFY_SYSTEM, "报名已取消",
                userName + " 取消了竞赛「" + contestName + "」的报名。",
                reg.getContestId(), CommonConstants.RELATED_TYPE_CONTEST);
        log.info("cancelRegistration success: id={}, contestId={}", id, reg.getContestId());
    }

    /**
     * 查询用户的报名列表
     *
     * 按创建时间倒序，同时填充竞赛名称（通过 contestService.listByIds 批量查询）。
     *
     * @param userId 用户 ID
     * @param page   页码
     * @param size   每页条数
     * @return 报名记录分页数据
     */
    @Override
    public IPage<Registration> pageByUser(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .orderByDesc(Registration::getCreateTime);
        IPage<Registration> result = page(new Page<>(page, size), wrapper);
        List<Registration> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Long> contestIds = records.stream()
                    .map(Registration::getContestId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!contestIds.isEmpty()) {
                List<Contest> contests = contestService.listByIds(contestIds);
                Map<Long, String> contestNameMap = contests.stream()
                        .collect(Collectors.toMap(Contest::getId, Contest::getName));
                records.forEach(reg -> {
                    if (reg.getContestId() != null) {
                        reg.setContestName(contestNameMap.get(reg.getContestId()));
                    }
                });
            }
        }
        return result;
    }

    /**
     * 按竞赛查询报名列表（管理员后台用）
     *
     * @param contestId 竞赛 ID
     * @param page      页码
     * @param size      每页条数
     * @param status    报名状态筛选（可选）
     * @return 报名记录分页数据
     */
    @Override
    public IPage<Registration> pageByContest(Long contestId, Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<Registration>()
                .eq(Registration::getContestId, contestId);
        if (status != null) {
            wrapper.eq(Registration::getStatus, status);
        }
        wrapper.orderByDesc(Registration::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 查询全部报名记录（管理员后台用，支持按竞赛和状态筛选）
     *
     * 批量填充竞赛名称和用户名称（通过 listByIds + Map 映射避免 N+1 问题）。
     *
     * @param contestId 竞赛 ID（可选）
     * @param status    报名状态（可选）
     * @param page      页码
     * @param size      每页条数
     * @return 报名记录分页数据
     */
    @Override
    public IPage<Registration> pageAll(Long contestId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        if (contestId != null) {
            wrapper.eq(Registration::getContestId, contestId);
        }
        if (status != null) {
            wrapper.eq(Registration::getStatus, status);
        }
        wrapper.orderByDesc(Registration::getCreateTime);
        IPage<Registration> result = page(new Page<>(page, size), wrapper);
        List<Registration> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Long> contestIds = records.stream()
                    .map(Registration::getContestId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, String> contestNameMap = java.util.Collections.emptyMap();
            if (!contestIds.isEmpty()) {
                List<Contest> contests = contestService.listByIds(contestIds);
                contestNameMap = contests.stream()
                        .collect(Collectors.toMap(Contest::getId, Contest::getName));
            }
            List<Long> userIds = records.stream()
                    .map(Registration::getUserId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, String> userNameMap = java.util.Collections.emptyMap();
            if (!userIds.isEmpty()) {
                List<User> users = userService.listByIds(userIds);
                userNameMap = users.stream()
                        .collect(Collectors.toMap(User::getId, User::getName));
            }
            Map<Long, String> finalContestNameMap = contestNameMap;
            Map<Long, String> finalUserNameMap = userNameMap;
            records.forEach(reg -> {
                if (reg.getContestId() != null) {
                    reg.setContestName(finalContestNameMap.get(reg.getContestId()));
                }
                if (reg.getUserId() != null) {
                    reg.setUserName(finalUserNameMap.get(reg.getUserId()));
                }
            });
        }
        return result;
    }
}
