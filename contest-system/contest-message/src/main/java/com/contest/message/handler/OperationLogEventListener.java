package com.contest.message.handler;

import com.contest.common.event.OperationLogEvent;
import com.contest.message.entity.OperationLog;
import com.contest.message.mapper.OperationLogMapper;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** 操作日志事件监听器：异步处理操作日志持久化 */
@Component
public class OperationLogEventListener {

    private final OperationLogMapper operationLogMapper;

    public OperationLogEventListener(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @EventListener
    public void handleOperationLog(OperationLogEvent event) {
        OperationLog log = new OperationLog();
        log.setUserId(event.getUserId());
        log.setAction(event.getAction());
        log.setDetail(event.getDetail());
        log.setIpAddress(event.getIpAddress());
        operationLogMapper.insert(log);
    }
}
