package com.contest.message.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.message.entity.OperationLogDO;

/** 操作日志服务接口 */
public interface OperationLogService {

    /** 分页查询操作日志 */
    IPage<OperationLogDO> page(Long userId, Integer page, Integer size);
}
