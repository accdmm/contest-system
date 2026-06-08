package com.contest.common.result;

/**
 * 响应状态码枚举 — 集中管理所有业务码
 *
 * 分级规则：
 * - 200-299 — 通用成功/失败
 * - 300-399 — 用户/账号相关
 * - 400-499 — 业务逻辑相关（报名 450-459、团队 470-479、权限 490-499）
 * - 500-599 — 服务器错误
 *
 * 设计原则：code 供前端程序判断（如 if code === 451 提示"已报名"），
 * message 供直接展示给用户。状态码与 HTTP 状态码保持对应关系但独立控制。
 */
public enum ResultCodeEnum {

    // ==================== 通用 ====================
    /** 成功 */
    SUCCESS(200, "成功"),
    /** 请求失败 */
    FAIL(400, "失败"),
    /** 参数校验不通过 */
    PARAM_ERROR(400, "参数不正确"),
    /** 未登录或 Token 无效 */
    UNAUTHORIZED(401, "未登录"),
    /** 无操作权限 */
    FORBIDDEN(403, "无权限"),
    /** 数据不存在 */
    NOT_FOUND(404, "数据不存在"),
    /** 数据冲突（并发操作或唯一约束冲突） */
    CONFLICT(409, "操作冲突，请刷新后重试"),
    /** 服务器内部异常 */
    SERVER_ERROR(500, "服务器内部错误"),

    // ==================== 账号 (301-399) ====================
    /** 注册时学号/邮箱已存在 */
    ACCOUNT_EXIST(301, "账号已存在"),
    /** 登录时账号不存在 */
    ACCOUNT_NOT_EXIST(302, "账号不存在"),
    /** 登录密码错误 */
    PASSWORD_ERROR(303, "用户名或密码错误"),
    /** 账号被冻结 */
    ACCOUNT_DISABLED(304, "该用户已被禁用"),

    // ==================== 报名 (450-459) ====================
    /** 已报名同一竞赛 */
    REG_DUPLICATE(451, "已报名该竞赛"),
    /** 同时报名数超过上限 */
    REG_LIMIT(452, "报名数量已达上限"),
    /** 该竞赛仅限团队报名 */
    REG_TEAM_REQUIRED(453, "该竞赛仅限团队报名"),
    /** 该竞赛仅限个人报名 */
    REG_TEAM_ONLY(454, "该竞赛仅限个人报名"),
    /** 报名人数/队伍已满 */
    REG_FULL(455, "报名人数已满"),
    /** 竞赛当前未开放报名 */
    REG_NOT_OPEN(456, "竞赛当前未开放报名"),
    /** 报名尚未开始 */
    REG_NOT_STARTED(457, "报名尚未开始"),
    /** 报名已截止 */
    REG_CLOSED(458, "报名已截止"),

    // ==================== 团队 (470-479) ====================
    /** 团队不存在 */
    TEAM_NOT_FOUND(471, "团队不存在"),
    /** 非队长操作团队 */
    TEAM_NOT_LEADER(472, "仅队长可进行此操作"),
    /** 已经是团队成员 */
    TEAM_MEMBER_EXIST(473, "你已经是该团队成员"),
    /** 邀请码无效 */
    TEAM_INVITE_INVALID(474, "邀请码无效或已过期"),
    /** 团队人数未达报名最低要求 */
    TEAM_NOT_READY(475, "团队人数未达标"),
    /** 团队未通过审核 */
    TEAM_NOT_APPROVED(476, "团队审核未通过"),
    /** 队长不能退出团队 */
    TEAM_LEADER_CANNOT_LEAVE(477, "队长不能退出，请解散团队"),
    /** 团队人数已达上限 */
    TEAM_FULL(478, "团队人数已达上限"),

    // ==================== 权限 (490-499) ====================
    /** 无权执行该操作 */
    PERMISSION_DENIED(491, "无权操作"),
    /** 请求的数据不存在 */
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
