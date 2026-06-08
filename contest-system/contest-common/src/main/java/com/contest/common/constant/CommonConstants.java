package com.contest.common.constant;

/**
 * 通用常量定义 — 集中管理所有业务状态值和枚举
 *
 * <p>所有常量按业务模块分组，Service 层和 Controller 层通过引用此接口的
 * 常量而非魔法值来判断状态，提高代码可读性和可维护性。
 */
public interface CommonConstants {

    // ==================== 日期格式 ====================
    /** 日期时间格式（与 JacksonConfig 一致） */
    String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /** 日期格式 */
    String DATE_PATTERN = "yyyy-MM-dd";

    // ==================== 用户角色 ====================
    /** 学生 */
    int ROLE_STUDENT = 0;
    /** 管理员 */
    int ROLE_ADMIN = 1;
    /** 教师 */
    int ROLE_TEACHER = 2;

    // ==================== 用户状态 ====================
    /** 正常 */
    int STATUS_NORMAL = 0;
    /** 冻结（登录时拦截，管理员可冻结/解冻） */
    int STATUS_FROZEN = 1;

    // ==================== 通知开关 ====================
    int NOTIFY_OFF = 0;
    int NOTIFY_ON = 1;

    // ==================== 竞赛状态 ====================
    /** 草稿（刚创建或已下架，仅管理员可见） */
    int CONTEST_DRAFT = 0;
    /** 已开放（报名中，前台可见并可报名） */
    int CONTEST_OPEN = 1;
    /** 已截止（报名时间已过，前台可见但不可报名） */
    int CONTEST_CLOSED = 2;

    // ==================== 竞赛类型 ====================
    /** 仅个人赛 */
    int CONTEST_PERSONAL = 0;
    /** 仅团队赛 */
    int CONTEST_TEAM = 1;
    /** 两者皆可 */
    int CONTEST_BOTH = 2;

    // ==================== 团队状态 ====================
    /** 组建中（队长创建后，成员未满或未提交报名） */
    int TEAM_FORMING = 0;
    /** 已提交报名（队长提交审核，等待管理员审批） */
    int TEAM_SUBMITTED = 1;
    /** 报名已通过（管理员审批通过） */
    int TEAM_APPROVED = 2;
    /** 报名已驳回（管理员拒绝） */
    int TEAM_REJECTED = 3;

    // ==================== 成员类型 ====================
    /** 普通成员 */
    int MEMBER_NORMAL = 0;
    /** 队长 */
    int MEMBER_LEADER = 1;

    // ==================== 成员审核状态 ====================
    int MEMBER_PENDING = 0;
    int MEMBER_APPROVED = 1;
    int MEMBER_REJECTED = 2;

    // ==================== 报名状态 ====================
    /** 待审核 */
    int REG_PENDING = 0;
    /** 已通过 */
    int REG_APPROVED = 1;
    /** 已驳回 */
    int REG_REJECTED = 2;
    /** 已取消（用户主动取消） */
    int REG_CANCELLED = 3;

    // ==================== 报名类型 ====================
    int REG_PERSONAL = 0;
    int REG_TEAM = 1;

    // ==================== 通知类型 ====================
    /** 审核结果通知 */
    int NOTIFY_REVIEW_RESULT = 0;
    /** 入队申请通知 */
    int NOTIFY_TEAM_APPLY = 1;
    /** 入队结果通知 */
    int NOTIFY_TEAM_RESULT = 2;
    /** 竞赛变更通知 */
    int NOTIFY_CONTEST_CHANGE = 3;
    /** 系统公告 */
    int NOTIFY_SYSTEM = 4;

    // ==================== 通知阅读状态 ====================
    int NOTIFY_UNREAD = 0;
    int NOTIFY_READ = 1;

    // ==================== 内容管理类型 ====================
    /** 轮播图 */
    int CMS_BANNER = 0;
    /** 公告 */
    int CMS_ANNOUNCEMENT = 1;

    // ==================== 内容可见性 ====================
    int CMS_HIDDEN = 0;
    int CMS_VISIBLE = 1;

    // ==================== 排序方式 ====================
    String SORT_HOT = "hot";
    String SORT_DEADLINE = "deadline";

    // ==================== 业务限制 ====================
    int MAX_ACTIVE_REGISTRATIONS = 3;
    int MIN_REJECT_REASON_LENGTH = 5;
    int INVITE_CODE_LENGTH = 6;
    int INVITE_CODE_EXPIRE_DAYS = 7;
    int AI_TITLE_MAX_LENGTH = 80;
    int AI_QUESTION_TRUNCATE_LENGTH = 30;
}
