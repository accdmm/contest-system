package com.contest.competition.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.dto.Result;
import com.contest.competition.entity.Contest;
import com.contest.competition.service.ContestService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contest")
public class ContestController {

    private final ContestService contestService;

    public ContestController(ContestService contestService) {
        this.contestService = contestService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('contest:create')")
    public Result<Contest> create(@RequestBody Contest contest) {
        return Result.success(contestService.createContest(contest));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('contest:update')")
    public Result<Contest> update(@RequestBody Contest contest) {
        return Result.success(contestService.updateContest(contest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('contest:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        contestService.deleteContest(id);
        return Result.success();
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('contest:publish')")
    public Result<Void> publish(@PathVariable Long id) {
        contestService.publishContest(id);
        return Result.success();
    }

    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('contest:publish')")
    public Result<Void> unpublish(@PathVariable Long id) {
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
        if (status != null && status == 0) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.isAuthenticated()
                    && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                status = null;
            }
        }
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
