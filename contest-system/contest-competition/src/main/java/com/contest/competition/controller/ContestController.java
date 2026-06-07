package com.contest.competition.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.security.SecurityUtil;
import com.contest.common.dto.Result;
import com.contest.competition.entity.Contest;
import com.contest.competition.param.ContestCreateParam;
import com.contest.competition.param.ContestUpdateParam;
import com.contest.competition.service.ContestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/contest")
public class ContestController {

    private static final Logger log = LoggerFactory.getLogger(ContestController.class);
    private final ContestService contestService;

    public ContestController(ContestService contestService) {
        this.contestService = contestService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('contest:create')")
    public Result<Contest> create(@RequestBody @Valid ContestCreateParam param) {
        Contest contest = new Contest();
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

    @PutMapping
    @PreAuthorize("hasAuthority('contest:update')")
    public Result<Contest> update(@RequestBody @Valid ContestUpdateParam param) {
        Contest contest = new Contest();
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('contest:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除竞赛: {}", id);
        contestService.deleteContest(id);
        return Result.success();
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('contest:publish')")
    public Result<Void> publish(@PathVariable Long id) {
        log.info("上架竞赛: {}", id);
        contestService.publishContest(id);
        return Result.success();
    }

    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('contest:publish')")
    public Result<Void> unpublish(@PathVariable Long id) {
        log.info("下架竞赛: {}", id);
        contestService.unpublishContest(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Contest> getById(@PathVariable Long id) {
        Contest contest = contestService.getById(id);
        if (contest == null) {
            return Result.error("竞赛不存在");
        }
        return Result.success(contest);
    }

    @GetMapping("/page")
    public Result<IPage<Contest>> page(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String category,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) Integer contestType,
                                       @RequestParam(required = false) String sortBy) {
        return Result.success(contestService.pageContests(page, size, keyword, category, status, contestType, sortBy));
    }

    @GetMapping("/hot")
    public Result<List<Contest>> hot(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(contestService.listHotContests(limit));
    }

    @GetMapping("/latest")
    public Result<List<Contest>> latest(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(contestService.listLatestContests(limit));
    }
}
