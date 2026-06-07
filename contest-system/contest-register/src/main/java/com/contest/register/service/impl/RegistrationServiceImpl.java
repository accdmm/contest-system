package com.contest.register.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.common.service.TeamValidator;
import com.contest.competition.entity.ContestDO;
import com.contest.competition.service.ContestService;
import com.contest.message.service.NotificationService;
import com.contest.register.entity.RegistrationDO;
import com.contest.register.mapper.RegistrationMapper;
import com.contest.register.service.AdminNotifyService;
import com.contest.register.service.RegistrationService;
import com.contest.user.entity.UserDO;
import com.contest.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 报名服务实现，包含个人/团队报名、审核、取消及报名校验等核心业务逻辑 */
@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, RegistrationDO> implements RegistrationService {

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

    private ContestDO validateContest(Long contestId, Integer requiredRegType) {
        ContestDO contest = contestService.getById(contestId);
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

    private void checkMaxParticipants(Long contestId, Integer maxParticipants) {
        if (maxParticipants == null || maxParticipants <= 0) return;
        long currentTotal = count(new LambdaQueryWrapper<RegistrationDO>()
                .eq(RegistrationDO::getContestId, contestId)
                .ne(RegistrationDO::getStatus, CommonConstants.REG_CANCELLED));
        if (currentTotal >= maxParticipants) {
            throw new BusinessException("报名人数已满，无法继续报名");
        }
    }

    private void checkMaxParticipantsForApproval(ContestDO contest) {
        Integer max = contest.getMaxParticipants();
        if (max != null && max > 0 && contest.getCurrentCount() != null && contest.getCurrentCount() >= max) {
            throw new BusinessException("报名人数已满，无法继续报名");
        }
    }

    /**
     * 个人赛报名：校验竞赛开放状态和时间 → 校验人数上限 → 校验重复报名（同一竞赛不可重复报名）→
     * 校验每人最多3个活跃报名 → 创建待审核记录 → 发送管理员通知
     */
    @Override
    @Transactional
    public RegistrationDO registerPersonal(Long userId, Long contestId, String remark) {
        ContestDO contest = validateContest(contestId, CommonConstants.REG_PERSONAL);
        checkMaxParticipants(contestId, contest.getMaxParticipants());

        long dupCount = count(new LambdaQueryWrapper<RegistrationDO>()
                .eq(RegistrationDO::getUserId, userId)
                .eq(RegistrationDO::getContestId, contestId)
                .ne(RegistrationDO::getStatus, CommonConstants.REG_CANCELLED));
        if (dupCount > 0) {
            throw new BusinessException("您已报名该竞赛");
        }

        long activeCount = count(new LambdaQueryWrapper<RegistrationDO>()
                .eq(RegistrationDO::getUserId, userId)
                .ne(RegistrationDO::getStatus, CommonConstants.REG_CANCELLED));
        if (activeCount >= 3) {
            throw new BusinessException("每人同时最多报名3个竞赛");
        }

        RegistrationDO reg = new RegistrationDO();
        reg.setContestId(contestId);
        reg.setUserId(userId);
        reg.setRegType(CommonConstants.REG_PERSONAL);
        reg.setStatus(CommonConstants.REG_PENDING);
        reg.setRemark(remark);
        save(reg);
        UserDO user = userService.getById(userId);
        String userName = user != null ? user.getName() : "用户" + userId;
        adminNotifyService.notifyAdmins(CommonConstants.NOTIFY_SYSTEM, "新报名申请",
                userName + " 提交了竞赛「" + contest.getName() + "」的个人报名，请及时审核。",
                contestId, "contest");
        return reg;
    }

    /**
     * 团队赛报名：验证团队有效性和队长身份 → 校验竞赛状态和时间 → 校验人数上限 →
     * 校验重复报名（同一团队不可重复报名同一竞赛）→ 校验每人最多3个活跃报名 → 创建待审核记录 → 发送管理员通知
     */
    @Override
    @Transactional
    public RegistrationDO registerTeam(Long userId, Long contestId, Long teamId) {
        teamValidator.validateForRegistration(teamId);
        teamValidator.validateTeamLeader(teamId, userId);
        ContestDO contest = validateContest(contestId, CommonConstants.REG_TEAM);
        checkMaxParticipants(contestId, contest.getMaxParticipants());

        long dupCount = count(new LambdaQueryWrapper<RegistrationDO>()
                .eq(RegistrationDO::getTeamId, teamId)
                .eq(RegistrationDO::getContestId, contestId)
                .ne(RegistrationDO::getStatus, CommonConstants.REG_CANCELLED));
        if (dupCount > 0) {
            throw new BusinessException("该团队已报名该竞赛");
        }

        long activeCount = count(new LambdaQueryWrapper<RegistrationDO>()
                .eq(RegistrationDO::getUserId, userId)
                .ne(RegistrationDO::getStatus, CommonConstants.REG_CANCELLED));
        if (activeCount >= 3) {
            throw new BusinessException("每人同时最多报名3个竞赛");
        }

        RegistrationDO reg = new RegistrationDO();
        reg.setContestId(contestId);
        reg.setUserId(userId);
        reg.setTeamId(teamId);
        reg.setRegType(CommonConstants.REG_TEAM);
        reg.setStatus(CommonConstants.REG_PENDING);
        save(reg);
        UserDO user = userService.getById(userId);
        String userName = user != null ? user.getName() : "用户" + userId;
        adminNotifyService.notifyAdmins(CommonConstants.NOTIFY_SYSTEM, "新团队报名申请",
                userName + " 提交了竞赛「" + contest.getName() + "」的团队报名，请及时审核。",
                contestId, "contest");
        return reg;
    }

    /**
     * 审核通过报名：仅待审核状态可批准 → 检查人数上限 → 递增竞赛 currentCount →
     * 状态流转为已通过 → 发送审核通过通知
     */
    @Override
    @Transactional
    public void approveRegistration(Long id) {
        RegistrationDO reg = getById(id);
        if (reg == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (reg.getStatus() != CommonConstants.REG_PENDING) {
            throw new BusinessException("仅待审核状态的报名可批准");
        }
        ContestDO contest = contestService.getById(reg.getContestId());
        if (contest != null) {
            checkMaxParticipantsForApproval(contest);
            contest.setCurrentCount(contest.getCurrentCount() == null ? 1 : contest.getCurrentCount() + 1);
            contestService.updateById(contest);
        }
        reg.setStatus(CommonConstants.REG_APPROVED);
        updateById(reg);

        notificationService.sendNotification(reg.getUserId(), CommonConstants.NOTIFY_REVIEW_RESULT,
                "报名审核通过", "你的竞赛报名已通过审核，请及时查看详情。",
                reg.getContestId(), "contest");
    }

    /**
     * 驳回报名：校验驳回原因长度 → 仅待审核或已通过状态可驳回 → 若之前是已通过状态则递减竞赛 currentCount →
     * 状态流转为已驳回 → 发送驳回通知
     */
    @Override
    @Transactional
    public void rejectRegistration(Long id, String reason) {
        if (reason == null || reason.trim().length() < 5) {
            throw new BusinessException("驳回原因不少于5个字符");
        }
        RegistrationDO reg = getById(id);
        if (reg == null) {
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
            ContestDO contest = contestService.getById(reg.getContestId());
            if (contest != null && contest.getCurrentCount() != null && contest.getCurrentCount() > 0) {
                contest.setCurrentCount(contest.getCurrentCount() - 1);
                contestService.updateById(contest);
            }
        }

        notificationService.sendNotification(reg.getUserId(), CommonConstants.NOTIFY_REVIEW_RESULT,
                "报名审核未通过", "你的竞赛报名已被驳回，原因：" + reason,
                reg.getContestId(), "contest");
    }

    @Override
    @Transactional
    public void cancelRegistration(Long id, Long userId) {
        RegistrationDO reg = getById(id);
        if (reg == null) {
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
            ContestDO contest = contestService.getById(reg.getContestId());
            if (contest != null && contest.getCurrentCount() != null && contest.getCurrentCount() > 0) {
                contest.setCurrentCount(contest.getCurrentCount() - 1);
                contestService.updateById(contest);
            }

        }
        ContestDO c = contestService.getById(reg.getContestId());
        UserDO user = userService.getById(userId);
        String userName = user != null ? user.getName() : "用户" + userId;
        String contestName = c != null ? c.getName() : "未知竞赛";
        adminNotifyService.notifyAdmins(CommonConstants.NOTIFY_SYSTEM, "报名已取消",
                userName + " 取消了竞赛「" + contestName + "」的报名。",
                reg.getContestId(), "contest");
    }

    @Override
    public IPage<RegistrationDO> pageByUser(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<RegistrationDO> wrapper = new LambdaQueryWrapper<RegistrationDO>()
                .eq(RegistrationDO::getUserId, userId)
                .orderByDesc(RegistrationDO::getCreateTime);
        IPage<RegistrationDO> result = page(new Page<>(page, size), wrapper);
        List<RegistrationDO> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Long> contestIds = records.stream()
                    .map(RegistrationDO::getContestId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!contestIds.isEmpty()) {
                List<ContestDO> contests = contestService.listByIds(contestIds);
                Map<Long, String> contestNameMap = contests.stream()
                        .collect(Collectors.toMap(ContestDO::getId, ContestDO::getName));
                records.forEach(reg -> {
                    if (reg.getContestId() != null) {
                        reg.setContestName(contestNameMap.get(reg.getContestId()));
                    }
                });
            }
        }
        return result;
    }

    @Override
    public IPage<RegistrationDO> pageByContest(Long contestId, Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<RegistrationDO> wrapper = new LambdaQueryWrapper<RegistrationDO>()
                .eq(RegistrationDO::getContestId, contestId);
        if (status != null) {
            wrapper.eq(RegistrationDO::getStatus, status);
        }
        wrapper.orderByDesc(RegistrationDO::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public IPage<RegistrationDO> pageAll(Long contestId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<RegistrationDO> wrapper = new LambdaQueryWrapper<>();
        if (contestId != null) {
            wrapper.eq(RegistrationDO::getContestId, contestId);
        }
        if (status != null) {
            wrapper.eq(RegistrationDO::getStatus, status);
        }
        wrapper.orderByDesc(RegistrationDO::getCreateTime);
        IPage<RegistrationDO> result = page(new Page<>(page, size), wrapper);
        List<RegistrationDO> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Long> contestIds = records.stream()
                    .map(RegistrationDO::getContestId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, String> contestNameMap = java.util.Collections.emptyMap();
            if (!contestIds.isEmpty()) {
                List<ContestDO> contests = contestService.listByIds(contestIds);
                contestNameMap = contests.stream()
                        .collect(Collectors.toMap(ContestDO::getId, ContestDO::getName));
            }
            List<Long> userIds = records.stream()
                    .map(RegistrationDO::getUserId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, String> userNameMap = java.util.Collections.emptyMap();
            if (!userIds.isEmpty()) {
                List<UserDO> users = userService.listByIds(userIds);
                userNameMap = users.stream()
                        .collect(Collectors.toMap(UserDO::getId, UserDO::getName));
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
