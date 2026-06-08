package com.contest.competition.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.security.SecurityUtil;
import com.contest.common.annotation.OperationLog;
import com.contest.common.result.Result;
import com.contest.competition.entity.ContestDO;
import com.contest.competition.param.ContestCreateParam;
import com.contest.competition.param.ContestUpdateParam;
import com.contest.competition.service.ContestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/** 竞赛管理接口 */
@RestController
@RequestMapping("/api/contest")
public class ContestController {

    private static final Logger log = LoggerFactory.getLogger(ContestController.class);
    private final ContestService contestService;

    public ContestController(ContestService contestService) {
        this.contestService = contestService;
    }

    /** 创建竞赛 */
    @PostMapping
    @PreAuthorize("hasAuthority('contest:create')")
    public Result<ContestDO> create(@RequestBody @Valid ContestCreateParam param) {
        ContestDO contest = new ContestDO();
        contest.setName(param.getName());
        contest.setCategory(param.getCategory());
        contest.setLevel(param.getLevel());
        contest.setOrganizer(param.getOrganizer());
        contest.setContestTime(param.getContestTime());
        contest.setRegisterStartTime(param.getRegisterStartTime());
        contest.setRegisterEndTime(param.getRegisterEndTime());
        contest.setLocation(param.getLocation());
        contest.setCoverImageUrl(param.getCoverImageUrl());
        contest.setDescription(param.getDescription());
        contest.setAttachmentUrls(param.getAttachmentUrls());
        contest.setContestType(param.getContestType());
        contest.setTeamMinSize(param.getTeamMinSize());
        contest.setTeamMaxSize(param.getTeamMaxSize());
        contest.setMaxParticipants(param.getMaxParticipants());
        Long userId = SecurityUtil.getCurrentUserId();
        contest.setCreateBy(userId);
        log.info("用户 {} 创建竞赛: {}", userId, param.getName());
        return Result.success(contestService.createContest(contest));
    }

    /** 修改竞赛 */
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('contest:update')")
    public Result<ContestDO> update(@RequestBody @Valid ContestUpdateParam param) {
        ContestDO contest = new ContestDO();
        contest.setId(param.getId());
        contest.setName(param.getName());
        contest.setCategory(param.getCategory());
        contest.setLevel(param.getLevel());
        contest.setOrganizer(param.getOrganizer());
        contest.setContestTime(param.getContestTime());
        contest.setRegisterStartTime(param.getRegisterStartTime());
        contest.setRegisterEndTime(param.getRegisterEndTime());
        contest.setLocation(param.getLocation());
        contest.setCoverImageUrl(param.getCoverImageUrl());
        contest.setDescription(param.getDescription());
        contest.setAttachmentUrls(param.getAttachmentUrls());
        contest.setContestType(param.getContestType());
        contest.setTeamMinSize(param.getTeamMinSize());
        contest.setTeamMaxSize(param.getTeamMaxSize());
        contest.setMaxParticipants(param.getMaxParticipants());
        return Result.success(contestService.updateContest(contest));
    }

    /** 删除竞赛（仅草稿状态可删） */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('contest:delete')")
    @OperationLog(action = "删除竞赛")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除竞赛: {}", id);
        contestService.deleteContest(id);
        return Result.success();
    }

    /** 上架竞赛 */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('contest:publish')")
    @OperationLog(action = "上架竞赛")
    public Result<Void> publish(@PathVariable Long id) {
        log.info("上架竞赛: {}", id);
        contestService.publishContest(id);
        return Result.success();
    }

    /** 下架竞赛（有已通过报名不可下架） */
    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('contest:publish')")
    @OperationLog(action = "下架竞赛")
    public Result<Void> unpublish(@PathVariable Long id) {
        log.info("下架竞赛: {}", id);
        contestService.unpublishContest(id);
        return Result.success();
    }

    /** 查询竞赛详情 */
    @GetMapping("/{id}")
    public Result<ContestDO> getById(@PathVariable Long id) {
        ContestDO contest = contestService.getById(id);
        if (contest == null) {
            return Result.error("竞赛不存在");
        }
        return Result.success(contest);
    }

    /** 分页查询竞赛列表 */
    @GetMapping("/page")
    public Result<IPage<ContestDO>> page(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String category,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) Integer contestType,
                                       @RequestParam(required = false) String sortBy) {
        return Result.success(contestService.pageContests(page, size, keyword, category, status, contestType, sortBy));
    }

    /** 获取热门竞赛（按报名人数倒序） */
    @GetMapping("/hot")
    public Result<List<ContestDO>> hot(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(contestService.listHotContests(limit));
    }

    /** 获取最新竞赛（按创建时间倒序） */
    @GetMapping("/latest")
    public Result<List<ContestDO>> latest(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(contestService.listLatestContests(limit));
    }
}
