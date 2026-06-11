package com.contest.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contest.message.entity.OperationLogDO;
import com.contest.message.mapper.OperationLogMapper;
import com.contest.message.service.OperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 操作日志服务实现 */
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<OperationLogDO> page(Long userId, Integer page, Integer size) {
        Page<OperationLogDO> p = new Page<>(page, size);
        LambdaQueryWrapper<OperationLogDO> wrapper = new LambdaQueryWrapper<OperationLogDO>()
                .orderByDesc(OperationLogDO::getCreateTime);
        if (userId != null) {
            wrapper.eq(OperationLogDO::getUserId, userId);
        }
        return operationLogMapper.selectPage(p, wrapper);
    }
}
