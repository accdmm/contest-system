package com.contest.common.event;

/**
 * 操作日志事件
 */
public class OperationLogEvent {

    private final Long userId;
    private final String action;
    private final String detail;
    private final String ipAddress;

    public OperationLogEvent(Long userId, String action, String detail, String ipAddress) {
        this.userId = userId;
        this.action = action;
        this.detail = detail;
        this.ipAddress = ipAddress;
    }

    public Long getUserId() { return userId; }
    public String getAction() { return action; }
    public String getDetail() { return detail; }
    public String getIpAddress() { return ipAddress; }
}
