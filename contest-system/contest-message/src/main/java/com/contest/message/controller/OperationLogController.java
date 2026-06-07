package com.contest.message.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contest.common.result.Result;
import com.contest.message.entity.OperationLogDO;
import com.contest.message.mapper.OperationLogMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 操作日志接口 */
@RestController
@RequestMapping("/api/log")
public class OperationLogController {

    private final OperationLogMapper operationLogMapper;

    public OperationLogController(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    /** 分页查询操作日志 */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('log:list')")
    public Result<IPage<OperationLogDO>> page(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) Long userId) {
        Page<OperationLogDO> p = new Page<>(page, size);
        if (userId != null) {
            return Result.success(operationLogMapper.selectPage(p,
                    new LambdaQueryWrapper<OperationLogDO>()
                            .eq(OperationLogDO::getUserId, userId)
                            .orderByDesc(OperationLogDO::getCreateTime)));
        }
        return Result.success(operationLogMapper.selectPage(p,
                new LambdaQueryWrapper<OperationLogDO>()
                        .orderByDesc(OperationLogDO::getCreateTime)));
    }
}
