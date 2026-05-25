package com.contest.competition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.competition.entity.Contest;
import com.contest.competition.mapper.ContestMapper;
import com.contest.competition.service.ContestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContestServiceImpl extends ServiceImpl<ContestMapper, Contest> implements ContestService {

    @Override
    public Contest createContest(Contest contest) {
        contest.setStatus(CommonConstants.CONTEST_DRAFT);
        contest.setCurrentCount(0);
        save(contest);
        return contest;
    }

    @Override
    @Transactional
    public Contest updateContest(Contest contest) {
        Contest existing = getById(contest.getId());
        if (existing == null) {
            throw new BusinessException("竞赛不存在");
        }
        if (existing.getStatus() != CommonConstants.CONTEST_DRAFT
                && existing.getCurrentCount() != null && existing.getCurrentCount() > 0) {
            throw new BusinessException("已有报名的竞赛不可修改类型或截止时间");
        }
        if (contest.getRegisterStartTime() != null && contest.getRegisterEndTime() != null
                && contest.getRegisterEndTime().isBefore(contest.getRegisterStartTime())) {
            throw new BusinessException("报名截止时间不能早于开始时间");
        }
        if (contest.getRegisterEndTime() != null && contest.getContestTime() != null
                && contest.getContestTime().isBefore(contest.getRegisterEndTime())) {
            throw new BusinessException("竞赛时间不能早于报名截止时间");
        }
        contest.setStatus(null);
        contest.setCurrentCount(null);
        updateById(contest);
        return contest;
    }

    @Override
    public void publishContest(Long id) {
        Contest contest = getById(id);
        if (contest == null) {
            throw new BusinessException("竞赛不存在");
        }
        contest.setStatus(CommonConstants.CONTEST_OPEN);
        updateById(contest);
    }

    @Override
    public void unpublishContest(Long id) {
        Contest contest = getById(id);
        if (contest == null) {
            throw new BusinessException("竞赛不存在");
        }
        contest.setStatus(CommonConstants.CONTEST_DRAFT);
        updateById(contest);
    }

    @Override
    @Transactional
    public void deleteContest(Long id) {
        Contest contest = getById(id);
        if (contest == null) {
            throw new BusinessException("竞赛不存在");
        }
        if (contest.getStatus() != CommonConstants.CONTEST_DRAFT) {
            throw new BusinessException("仅下架的竞赛可删除");
        }
        if (contest.getCurrentCount() != null && contest.getCurrentCount() > 0) {
            throw new BusinessException("已有报名的竞赛不可删除");
        }
        removeById(id);
    }

    @Override
    public IPage<Contest> pageContests(Integer page, Integer size, String keyword, String category, Integer status, Integer notStatus) {
        LambdaQueryWrapper<Contest> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Contest::getName, keyword);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Contest::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Contest::getStatus, status);
        }
        if (notStatus != null) {
            wrapper.ne(Contest::getStatus, notStatus);
        }
        wrapper.orderByDesc(Contest::getUpdateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<Contest> listHotContests(int limit) {
        LambdaQueryWrapper<Contest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contest::getStatus, CommonConstants.CONTEST_OPEN);
        wrapper.orderByDesc(Contest::getCurrentCount);
        wrapper.last("LIMIT " + limit);
        return list(wrapper);
    }

    @Override
    public List<Contest> listLatestContests(int limit) {
        LambdaQueryWrapper<Contest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contest::getStatus, CommonConstants.CONTEST_OPEN);
        wrapper.orderByDesc(Contest::getCreateTime);
        wrapper.last("LIMIT " + limit);
        return list(wrapper);
    }
}
