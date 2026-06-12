package com.contest.message.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.common.result.Result;
import com.contest.message.entity.OperationLog;
import com.contest.message.service.OperationLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 操作日志接口 */
@RestController
@RequestMapping("/api/log")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    /** 分页查询操作日志 */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('log:list')")
    public Result<IPage<OperationLog>> page(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) Long userId) {
        return Result.success(operationLogService.page(userId, page, size));
    }
}
