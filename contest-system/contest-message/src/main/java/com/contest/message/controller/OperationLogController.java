package com.contest.message.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contest.common.dto.Result;
import com.contest.message.entity.OperationLog;
import com.contest.message.mapper.OperationLogMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/log")
public class OperationLogController {

    private final OperationLogMapper operationLogMapper;

    public OperationLogController(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('log:list')")
    public Result<IPage<OperationLog>> page(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) Long userId) {
        Page<OperationLog> p = new Page<>(page, size);
        if (userId != null) {
            return Result.success(operationLogMapper.selectPage(p,
                    new LambdaQueryWrapper<OperationLog>()
                            .eq(OperationLog::getUserId, userId)
                            .orderByDesc(OperationLog::getCreateTime)));
        }
        return Result.success(operationLogMapper.selectPage(p,
                new LambdaQueryWrapper<OperationLog>()
                        .orderByDesc(OperationLog::getCreateTime)));
    }
}
