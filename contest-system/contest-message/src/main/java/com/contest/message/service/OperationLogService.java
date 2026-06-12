package com.contest.message.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.message.entity.OperationLog;

/** 操作日志服务接口 */
public interface OperationLogService {

    /** 分页查询操作日志 */
    IPage<OperationLog> page(Long userId, Integer page, Integer size);
}
