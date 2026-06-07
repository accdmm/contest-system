package com.contest.common.enums;

public enum ResultCodeEnum {

    SUCCESS(200, "成功"),
    FAIL(400, "失败"),
    PARAM_ERROR(400, "参数不正确"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "数据不存在"),
    CONFLICT(409, "操作冲突，请刷新后重试"),
    SERVER_ERROR(500, "服务器内部错误"),

    ACCOUNT_EXIST(301, "账号已存在"),
    ACCOUNT_NOT_EXIST(302, "账号不存在"),
    PASSWORD_ERROR(303, "用户名或密码错误"),
    ACCOUNT_DISABLED(304, "该用户已被禁用"),

    REG_DUPLICATE(451, "已报名该竞赛"),
    REG_LIMIT(452, "报名数量已达上限"),
    REG_TEAM_REQUIRED(453, "该竞赛仅限团队报名"),
    REG_TEAM_ONLY(454, "该竞赛仅限个人报名"),
    REG_FULL(455, "报名人数已满"),
    REG_NOT_OPEN(456, "竞赛当前未开放报名"),
    REG_NOT_STARTED(457, "报名尚未开始"),
    REG_CLOSED(458, "报名已截止"),

    TEAM_NOT_FOUND(471, "团队不存在"),
    TEAM_NOT_LEADER(472, "仅队长可进行此操作"),
    TEAM_MEMBER_EXIST(473, "你已经是该团队成员"),
    TEAM_INVITE_INVALID(474, "邀请码无效或已过期"),
    TEAM_NOT_READY(475, "团队人数未达标"),
    TEAM_NOT_APPROVED(476, "团队审核未通过"),
    TEAM_LEADER_CANNOT_LEAVE(477, "队长不能退出，请解散团队"),
    TEAM_FULL(478, "团队人数已达上限"),

    PERMISSION_DENIED(491, "无权操作"),
    DATA_NOT_FOUND(492, "数据不存在");

    private final int code;
    private final String message;

    ResultCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
