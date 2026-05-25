package com.contest.register.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.register.entity.Registration;
import com.contest.register.mapper.RegistrationMapper;
import com.contest.register.service.RegistrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService {

    @Override
    @Transactional
    public Registration registerPersonal(Long userId, Long contestId, String remark) {
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
        return reg;
    }

    @Override
    @Transactional
    public Registration registerTeam(Long userId, Long contestId, Long teamId) {
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
        return reg;
    }

    @Override
    public void approveRegistration(Long id) {
        Registration reg = getById(id);
        if (reg == null) {
            throw new BusinessException("报名记录不存在");
        }
        reg.setStatus(CommonConstants.REG_APPROVED);
        updateById(reg);
    }

    @Override
    public void rejectRegistration(Long id, String reason) {
        if (reason == null || reason.trim().length() < 5) {
            throw new BusinessException("驳回原因不少于5个字符");
        }
        Registration reg = getById(id);
        if (reg == null) {
            throw new BusinessException("报名记录不存在");
        }
        reg.setStatus(CommonConstants.REG_REJECTED);
        reg.setReviewReason(reason);
        updateById(reg);
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
        reg.setStatus(CommonConstants.REG_CANCELLED);
        updateById(reg);
    }

    @Override
    public IPage<Registration> pageByUser(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .orderByDesc(Registration::getCreateTime);
        return page(new Page<>(page, size), wrapper);
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
}
