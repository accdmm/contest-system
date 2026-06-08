package com.contest.competition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.competition.entity.ContestDO;
import com.contest.competition.mapper.ContestMapper;
import com.contest.competition.service.ContestService;
import com.contest.user.entity.UserDO;
import com.contest.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 竞赛服务实现
 *
 * 包含竞赛创建、修改、发布、下架、删除等核心业务逻辑。
 * 创建时校验时间顺序（报名开始 < 报名截止 < 竞赛时间），
 * 下架时校验是否存在已通过报名。
 */
@Service
public class ContestServiceImpl extends ServiceImpl<ContestMapper, ContestDO> implements ContestService {

    private final UserMapper userMapper;

    public ContestServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 创建竞赛：先校验时间顺序（报名开始 < 报名截止 < 竞赛时间），再初始化为草稿状态，设置当前报名人数为0
     */
    @Override
    public ContestDO createContest(ContestDO contest) {
        LocalDateTime now = LocalDateTime.now();
        if (contest.getRegisterStartTime() != null && contest.getRegisterStartTime().isBefore(now)) {
            throw new BusinessException("报名开始时间不能早于当前时间");
        }
        if (contest.getRegisterEndTime() != null && contest.getRegisterEndTime().isBefore(now)) {
            throw new BusinessException("报名截止时间不能早于当前时间");
        }
        if (contest.getContestTime() != null && contest.getContestTime().isBefore(now)) {
            throw new BusinessException("竞赛时间不能早于当前时间");
        }
        if (contest.getRegisterStartTime() != null && contest.getRegisterEndTime() != null
                && contest.getRegisterEndTime().isBefore(contest.getRegisterStartTime())) {
            throw new BusinessException("报名截止时间不能早于开始时间");
        }
        if (contest.getRegisterEndTime() != null && contest.getContestTime() != null
                && contest.getContestTime().isBefore(contest.getRegisterEndTime())) {
            throw new BusinessException("竞赛时间不能早于报名截止时间");
        }
        contest.setStatus(CommonConstants.CONTEST_DRAFT);
        contest.setCurrentCount(0);
        save(contest);
        populateCreatorName(contest);
        return contest;
    }

    /**
     * 修改竞赛：非草稿状态且有已通过报名时禁止修改；设置 status/currentCount 为 null 避免覆盖数据库值
     */
    @Override
    @Transactional
    public ContestDO updateContest(ContestDO contest) {
        ContestDO existing = getById(contest.getId());
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

    /** 上架竞赛：将状态置为已开放 */
    @Override
    @Transactional
    public void publishContest(Long id) {
        ContestDO contest = getById(id);
        if (contest == null) {
            throw new BusinessException("竞赛不存在");
        }
        contest.setStatus(CommonConstants.CONTEST_OPEN);
        updateById(contest);
    }

    /** 下架竞赛：有已通过报名时不能下架 */
    @Override
    @Transactional
    public void unpublishContest(Long id) {
        ContestDO contest = getById(id);
        if (contest == null) {
            throw new BusinessException("竞赛不存在");
        }
        if (contest.getCurrentCount() != null && contest.getCurrentCount() > 0) {
            throw new BusinessException("已有报名的竞赛不可下架");
        }
        contest.setStatus(CommonConstants.CONTEST_DRAFT);
        updateById(contest);
    }

    /** 删除竞赛：仅草稿状态且无报名时允许删除 */
    @Override
    @Transactional
    public void deleteContest(Long id) {
        ContestDO contest = getById(id);
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
    public ContestDO getById(Serializable id) {
        ContestDO contest = super.getById(id);
        if (contest != null) {
            populateCreatorName(contest);
        }
        return contest;
    }

    @Override
    public IPage<ContestDO> pageContests(Integer page, Integer size, String keyword, String category, Integer status, String sortBy) {
        return pageContests(page, size, keyword, category, status, null, sortBy);
    }

    @Override
    public IPage<ContestDO> pageContests(Integer page, Integer size, String keyword, String category, Integer status, Integer contestType, String sortBy) {
        LambdaQueryWrapper<ContestDO> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ContestDO::getName, keyword);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(ContestDO::getCategory, category);
        }
        LocalDateTime now = LocalDateTime.now();
        if (status != null) {
            if (status == 0) {
                wrapper.eq(ContestDO::getStatus, CommonConstants.CONTEST_DRAFT);
            } else if (status == 1) {
                wrapper.eq(ContestDO::getStatus, CommonConstants.CONTEST_OPEN)
                        .le(ContestDO::getRegisterStartTime, now)
                        .gt(ContestDO::getRegisterEndTime, now);
            } else if (status == 2) {
                wrapper.eq(ContestDO::getStatus, CommonConstants.CONTEST_OPEN)
                        .le(ContestDO::getRegisterEndTime, now);
            }
        } else {
            // 默认只展示报名未截止的上架竞赛，排除草稿和已过期的
            wrapper.eq(ContestDO::getStatus, CommonConstants.CONTEST_OPEN)
                    .gt(ContestDO::getRegisterEndTime, now);
        }
        if (contestType != null) {
            wrapper.eq(ContestDO::getContestType, contestType);
        }
        if ("hot".equals(sortBy)) {
            wrapper.orderByDesc(ContestDO::getCurrentCount);
        } else if ("deadline".equals(sortBy)) {
            wrapper.orderByAsc(ContestDO::getRegisterEndTime);
        } else {
            wrapper.orderByDesc(ContestDO::getUpdateTime);
        }
        IPage<ContestDO> result = page(new Page<>(page, size), wrapper);
        populateCreatorNames(result.getRecords());
        return result;
    }

    /**
     * 管理员分页查询所有竞赛（包含草稿、已发布、已截止，不按状态过滤）
     *
     * 与前台 pageContests 不同：不默认过滤 status=1（已发布），
     * 管理员可见全部状态的竞赛，便于管理草稿和已截止的竞赛。
     * 同时支持按状态筛选，status 为 null 时展示全部。
     */
    @Override
    public IPage<ContestDO> pageAdminContests(Integer page, Integer size, String keyword, String category, Integer status) {
        LambdaQueryWrapper<ContestDO> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ContestDO::getName, keyword);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(ContestDO::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(ContestDO::getStatus, status);
        }
        wrapper.orderByDesc(ContestDO::getCreateTime);
        IPage<ContestDO> result = page(new Page<>(page, size), wrapper);
        populateCreatorNames(result.getRecords());
        return result;
    }

    @Override
    public List<ContestDO> listHotContests(int limit) {
        LambdaQueryWrapper<ContestDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContestDO::getStatus, CommonConstants.CONTEST_OPEN)
               .gt(ContestDO::getRegisterEndTime, LocalDateTime.now());
        wrapper.orderByDesc(ContestDO::getCurrentCount);
        List<ContestDO> list = page(new Page<>(1, limit), wrapper).getRecords();
        populateCreatorNames(list);
        return list;
    }

    @Override
    public List<ContestDO> listLatestContests(int limit) {
        LambdaQueryWrapper<ContestDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContestDO::getStatus, CommonConstants.CONTEST_OPEN)
               .gt(ContestDO::getRegisterEndTime, LocalDateTime.now());
        wrapper.orderByDesc(ContestDO::getCreateTime);
        List<ContestDO> list = page(new Page<>(1, limit), wrapper).getRecords();
        populateCreatorNames(list);
        return list;
    }

    private void populateCreatorName(ContestDO contest) {
        if (contest == null || contest.getCreateBy() == null) return;
        UserDO user = userMapper.selectById(contest.getCreateBy());
        if (user != null) {
            contest.setCreatorName(user.getName());
        }
    }

    private void populateCreatorNames(List<ContestDO> contests) {
        Set<Long> userIds = contests.stream()
                .map(ContestDO::getCreateBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        List<UserDO> users = userMapper.selectBatchIds(userIds);
        Map<Long, String> nameMap = users.stream()
                .collect(Collectors.toMap(UserDO::getId, UserDO::getName));
        for (ContestDO c : contests) {
            if (c.getCreateBy() != null) {
                c.setCreatorName(nameMap.get(c.getCreateBy()));
            }
        }
    }
}
