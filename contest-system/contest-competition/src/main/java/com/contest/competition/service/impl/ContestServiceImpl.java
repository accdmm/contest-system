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
import com.contest.user.entity.User;
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
 * 创建时校验时间顺序（报名开始 &lt; 报名截止 &lt; 竞赛时间），
 * 下架时校验是否存在已通过报名。
 *
 * 安全性说明：
 * - 所有数据库操作通过 MyBatis-Plus 参数化查询（PreparedStatement），防 SQL 注入
 * - 时间校验防止前端传入异常时间戳导致竞态条件
 * - 删除操作校验 draft 状态，防止误删已发布竞赛
 *
 * 性能说明：
 * - 分页查询使用 MyBatis-Plus 分页插件，自动生成 LIMIT + COUNT SQL
 * - contest 表有 idx_status_type、idx_time 索引，覆盖常用查询条件
 * - 热门/最新竞赛均限制返回条数（limit 参数），避免全表扫描
 * - creatorName 通过批量查询（selectBatchIds）+ Map 映射填充，避免 N+1 问题
 */
@Service
public class ContestServiceImpl extends ServiceImpl<ContestMapper, Contest> implements ContestService {

    private final UserMapper userMapper;

    public ContestServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 创建竞赛：校验时间顺序 → 初始化为草稿状态 → 设置报名人数为0
     *
     * 校验时间约束（按顺序）：
     * 1. 报名开始时间不能早于当前时间
     * 2. 报名截止时间不能早于当前时间
     * 3. 竞赛时间不能早于当前时间
     * 4. 报名截止不能早于报名开始
     * 5. 竞赛时间不能早于报名截止
     *
     * 创建后自动填充 creatorName（通过 createBy 查询用户表）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Contest createContest(Contest contest) {
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
     * 修改竞赛：非草稿状态且有已通过报名时禁止修改
     *
     * 设置 status 和 currentCount 为 null，避免 MyBatis-Plus 的 updateById
     * 将这两个字段覆盖为 null。仅修改前端传入的字段。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 上架竞赛：将状态从草稿（0）置为已开放（1），前台可见并开放报名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishContest(Long id) {
        Contest contest = getById(id);
        if (contest == null) {
            throw new BusinessException("竞赛不存在");
        }
        if (contest.getStatus() != CommonConstants.CONTEST_DRAFT) {
            throw new BusinessException("仅草稿状态的竞赛可上架");
        }
        if (contest.getRegisterEndTime() != null
                && contest.getRegisterEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("报名截止时间已过，不可上架");
        }
        contest.setStatus(CommonConstants.CONTEST_OPEN);
        updateById(contest);
    }

    /**
     * 下架竞赛：仅有已通过报名时不允许下架（保护已报名用户权益）
     *
     * 下架后状态恢复为草稿（0），前台不可见。若 currentCount = 0，
     * 说明无任何已通过报名，允许下架。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublishContest(Long id) {
        Contest contest = getById(id);
        if (contest == null) {
            throw new BusinessException("竞赛不存在");
        }
        if (contest.getCurrentCount() != null && contest.getCurrentCount() > 0) {
            throw new BusinessException("已有报名的竞赛不可下架");
        }
        contest.setStatus(CommonConstants.CONTEST_DRAFT);
        updateById(contest);
    }

    /**
     * 删除竞赛：仅草稿状态且无报名时允许删除
     *
     * 使用 removeById（逻辑删除，由于 @TableLogic 注解自动转为 UPDATE SET is_delete=1）。
     * 物理删除仅发生在 is_delete 字段不存在时，当前已添加该字段，故为逻辑删除。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 重写 getById：查询后自动填充创建人名称
     *
     * 由于 MyBatis-Plus 的 @TableLogic 自动过滤 is_delete=1 的记录，
     * 已逻辑删除的竞赛会返回 null。
     */
    @Override
    public Contest getById(Serializable id) {
        Contest contest = super.getById(id);
        if (contest != null) {
            populateCreatorName(contest);
        }
        return contest;
    }

    @Override
    public IPage<Contest> pageContests(Integer page, Integer size, String keyword, String category, Integer status, String sortBy) {
        return pageContests(page, size, keyword, category, status, null, sortBy);
    }

    /**
     * 前台竞赛列表分页查询
     *
     * 默认只展示 status=1（已发布）且报名未截止的竞赛。
     * 可选按类别、竞赛类型筛选，支持按热度（currentCount）和截止时间（registerEndTime）排序。
     *
     * 性能说明：查询使用了 contest 表的 idx_status_type（status + contest_type 复合索引）
     * 和 idx_time（register_end_time 索引），确保在大量竞赛数据下的查询性能。
     */
    @Override
    public IPage<Contest> pageContests(Integer page, Integer size, String keyword, String category, Integer status, Integer contestType, String sortBy) {
        LambdaQueryWrapper<Contest> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Contest::getName, keyword);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Contest::getCategory, category);
        }
        LocalDateTime now = LocalDateTime.now();
        if (status != null) {
            if (status == CommonConstants.CONTEST_DRAFT) {
                wrapper.eq(Contest::getStatus, CommonConstants.CONTEST_DRAFT);
            } else if (status == CommonConstants.CONTEST_OPEN) {
                wrapper.eq(Contest::getStatus, CommonConstants.CONTEST_OPEN)
                        .le(Contest::getRegisterStartTime, now)
                        .gt(Contest::getRegisterEndTime, now);
            } else if (status == CommonConstants.CONTEST_CLOSED) {
                wrapper.eq(Contest::getStatus, CommonConstants.CONTEST_OPEN)
                        .le(Contest::getRegisterEndTime, now);
            }
        } else {
            wrapper.eq(Contest::getStatus, CommonConstants.CONTEST_OPEN)
                    .gt(Contest::getRegisterEndTime, now);
        }
        if (contestType != null) {
            wrapper.eq(Contest::getContestType, contestType);
        }
        if (CommonConstants.SORT_HOT.equals(sortBy)) {
            wrapper.orderByDesc(Contest::getCurrentCount);
        } else if (CommonConstants.SORT_DEADLINE.equals(sortBy)) {
            wrapper.orderByAsc(Contest::getRegisterEndTime);
        } else {
            wrapper.orderByDesc(Contest::getUpdateTime);
        }
        IPage<Contest> result = page(new Page<>(page, size), wrapper);
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
    public IPage<Contest> pageAdminContests(Integer page, Integer size, String keyword, String category, Integer status) {
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
        wrapper.orderByDesc(Contest::getCreateTime);
        IPage<Contest> result = page(new Page<>(page, size), wrapper);
        populateCreatorNames(result.getRecords());
        return result;
    }

    /**
     * 获取热门竞赛（按报名人数倒序）
     *
     * 仅返回已发布且报名未截止的竞赛。限制返回条数（默认 5 条）。
     */
    @Override
    public List<Contest> listHotContests(int limit) {
        LambdaQueryWrapper<Contest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contest::getStatus, CommonConstants.CONTEST_OPEN)
               .gt(Contest::getRegisterEndTime, LocalDateTime.now());
        wrapper.orderByDesc(Contest::getCurrentCount);
        List<Contest> list = page(new Page<>(1, limit), wrapper).getRecords();
        populateCreatorNames(list);
        return list;
    }

    /**
     * 获取最新竞赛（按创建时间倒序）
     *
     * 仅返回已发布且报名未截止的竞赛。限制返回条数（默认 5 条）。
     */
    @Override
    public List<Contest> listLatestContests(int limit) {
        LambdaQueryWrapper<Contest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contest::getStatus, CommonConstants.CONTEST_OPEN)
               .gt(Contest::getRegisterEndTime, LocalDateTime.now());
        wrapper.orderByDesc(Contest::getCreateTime);
        List<Contest> list = page(new Page<>(1, limit), wrapper).getRecords();
        populateCreatorNames(list);
        return list;
    }

    /**
     * 填充单个竞赛的创建人名称
     *
     * 通过 createBy 字段查询 user 表获取 name。
     * 适用于详情页等单条查询场景。
     */
    private void populateCreatorName(Contest contest) {
        if (contest == null || contest.getCreateBy() == null) return;
        User user = userMapper.selectById(contest.getCreateBy());
        if (user != null) {
            contest.setCreatorName(user.getName());
        }
    }

    /**
     * 批量填充竞赛列表的创建人名称
     *
     * 使用 selectBatchIds + Map 映射，避免逐条查询的 N+1 问题。
     * 适用于列表页等批量查询场景。
     */
    private void populateCreatorNames(List<Contest> contests) {
        Set<Long> userIds = contests.stream()
                .map(Contest::getCreateBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, String> nameMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        for (Contest c : contests) {
            if (c.getCreateBy() != null) {
                c.setCreatorName(nameMap.get(c.getCreateBy()));
            }
        }
    }
}
