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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService {

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

    private Contest validateContest(Long contestId, Integer requiredRegType) {
        Contest contest = contestService.getById(contestId);
        if (contest == null) {
            throw new BusinessException("竞赛不存在");
        }
        if (contest.getStatus() != CommonConstants.CONTEST_OPEN) {
            throw new BusinessException("竞赛当前未开放报名");
        }
        if (contest.getContestType() == CommonConstants.CONTEST_PERSONAL && requiredRegType == CommonConstants.REG_TEAM) {
            throw new BusinessException("该竞赛仅限个人报名");
        }
        if (contest.getContestType() == CommonConstants.CONTEST_TEAM && requiredRegType == CommonConstants.REG_PERSONAL) {
            throw new BusinessException("该竞赛仅限团队报名");
        }
        return contest;
    }

    private void checkMaxParticipants(Contest contest) {
        Integer max = contest.getMaxParticipants();
        if (max != null && max > 0 && contest.getCurrentCount() != null && contest.getCurrentCount() >= max) {
            throw new BusinessException("报名人数已满，无法继续报名");
        }
    }

    @Override
    @Transactional
    public Registration registerPersonal(Long userId, Long contestId, String remark) {
        Contest contest = validateContest(contestId, CommonConstants.REG_PERSONAL);
        checkMaxParticipants(contest);

        long count = count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .eq(Registration::getContestId, contestId)
                .ne(Registration::getStatus, CommonConstants.REG_CANCELLED));
        if (count > 0) {
            throw new BusinessException("您已报名该竞赛");
        }

        Registration reg = new Registration();
        reg.setContestId(contestId);
        reg.setUserId(userId);
        reg.setRegType(CommonConstants.REG_PERSONAL);
        reg.setStatus(CommonConstants.REG_PENDING);
        reg.setRemark(remark);
        save(reg);
        User user = userService.getById(userId);
        String userName = user != null ? user.getName() : "用户" + userId;
        adminNotifyService.notifyAdmins(CommonConstants.NOTIFY_SYSTEM, "新报名申请",
                userName + " 提交了竞赛「" + contest.getName() + "」的个人报名，请及时审核。",
                contestId, "contest");
        return reg;
    }

    @Override
    @Transactional
    public Registration registerTeam(Long userId, Long contestId, Long teamId) {
        teamValidator.validateForRegistration(teamId);
        Contest contest = validateContest(contestId, CommonConstants.REG_TEAM);
        checkMaxParticipants(contest);

        long count = count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .eq(Registration::getContestId, contestId)
                .ne(Registration::getStatus, CommonConstants.REG_CANCELLED));
        if (count > 0) {
            throw new BusinessException("您已报名该竞赛");
        }

        Registration reg = new Registration();
        reg.setContestId(contestId);
        reg.setUserId(userId);
        reg.setTeamId(teamId);
        reg.setRegType(CommonConstants.REG_TEAM);
        reg.setStatus(CommonConstants.REG_PENDING);
        save(reg);
        User user = userService.getById(userId);
        String userName = user != null ? user.getName() : "用户" + userId;
        adminNotifyService.notifyAdmins(CommonConstants.NOTIFY_SYSTEM, "新团队报名申请",
                userName + " 提交了竞赛「" + contest.getName() + "」的团队报名，请及时审核。",
                contestId, "contest");
        return reg;
    }

    @Override
    @Transactional
    public void approveRegistration(Long id) {
        Registration reg = getById(id);
        if (reg == null) {
            throw new BusinessException("报名记录不存在");
        }
        Contest contest = contestService.getById(reg.getContestId());
        if (contest != null) {
            checkMaxParticipants(contest);
            contest.setCurrentCount(contest.getCurrentCount() == null ? 1 : contest.getCurrentCount() + 1);
            contestService.updateById(contest);
        }
        reg.setStatus(CommonConstants.REG_APPROVED);
        updateById(reg);

        notificationService.sendNotification(reg.getUserId(), CommonConstants.NOTIFY_REVIEW_RESULT,
                "报名审核通过", "你的竞赛报名已通过审核，请及时查看详情。",
                reg.getContestId(), "contest");
    }

    @Override
    @Transactional
    public void rejectRegistration(Long id, String reason) {
        if (reason == null || reason.trim().length() < 5) {
            throw new BusinessException("驳回原因不少于5个字符");
        }
        Registration reg = getById(id);
        if (reg == null) {
            throw new BusinessException("报名记录不存在");
        }
        boolean wasApproved = reg.getStatus() == CommonConstants.REG_APPROVED;
        reg.setStatus(CommonConstants.REG_REJECTED);
        reg.setReviewReason(reason);
        updateById(reg);

        if (wasApproved) {
            Contest contest = contestService.getById(reg.getContestId());
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
        Registration reg = getById(id);
        if (reg == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (!reg.getUserId().equals(userId)) {
            throw new BusinessException("只能取消自己的报名");
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
        String userName = user != null ? user.getName() : "用户" + userId;
        String contestName = c != null ? c.getName() : "未知竞赛";
        adminNotifyService.notifyAdmins(CommonConstants.NOTIFY_SYSTEM, "报名已取消",
                userName + " 取消了竞赛「" + contestName + "」的报名。",
                reg.getContestId(), "contest");
    }

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
