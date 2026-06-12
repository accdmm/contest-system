package com.contest.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.result.Result;
import com.contest.competition.entity.Contest;
import com.contest.competition.service.ContestService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员竞赛管理接口
 *
 * 与前台竞赛接口分离，管理员可查看全部状态的竞赛（含草稿），
 * 不走前台 pageContests 的默认状态过滤逻辑。
 */
@RestController
@RequestMapping("/api/admin/contest")
public class AdminContestController {

    private final ContestService contestService;

    public AdminContestController(ContestService contestService) {
        this.contestService = contestService;
    }

    /**
     * 管理员分页查询所有竞赛
     *
     * status 为可选参数，不传时展示全部状态的竞赛（草稿/已发布/已截止），
     * 按创建时间倒序排列。
     *
     * @param page     页码
     * @param size     每页条数
     * @param keyword  关键词（按名称模糊搜索）
     * @param category 竞赛类别
     * @param status   状态（0=草稿，1=已发布，传 null 表示全部）
     * @return 分页结果
     */
    @GetMapping("/page")
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<Contest>> page(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String category,
                                       @RequestParam(required = false) Integer status) {
        return Result.success(contestService.pageAdminContests(page, size, keyword, category, status));
    }
}
