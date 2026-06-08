package com.contest.common.constant;

/**
 * 权限编码常量 — 定义系统中所有权限的唯一标识符
 *
 * <p>格式约定：{模块}:{操作}，如 contest:create 表示"竞赛：创建"。
 * 与 permission 表中的 code 字段对应，用于 @PreAuthorize("hasAuthority(...)") 注解。
 *
 * <p>权限模型：基于 RBAC（Role-Based Access Control），
 * 角色（Role）→ 权限编码（Permission）→ 接口（@PreAuthorize）。
 * 管理员角色拥有全部权限（见 PermissionService），
 * 学生/教师角色通过 role_permission 表分配权限。
 */
public interface PermissionConstants {

    // ==================== 竞赛管理 ====================
    String CONTEST_CREATE   = "contest:create";
    String CONTEST_UPDATE   = "contest:update";
    String CONTEST_DELETE   = "contest:delete";
    String CONTEST_PUBLISH  = "contest:publish";

    // ==================== 用户管理 ====================
    String USER_LIST        = "user:list";
    String USER_CREATE      = "user:create";
    String USER_FREEZE      = "user:freeze";

    // ==================== 报名管理 ====================
    String REG_APPROVE      = "registration:approve";
    String REG_LIST         = "registration:list";
    String REG_CANCEL       = "registration:cancel";

    // ==================== 团队管理 ====================
    String TEAM_APPROVE     = "team:approve";
    String TEAM_LIST        = "team:list";

    // ==================== 消息通知 ====================
    String NOTIFY_SEND      = "notification:send";
    String NOTIFY_BROADCAST = "notification:broadcast";

    // ==================== 内容管理 ====================
    String CMS_CREATE       = "cms:create";
    String CMS_UPDATE       = "cms:update";
    String CMS_DELETE       = "cms:delete";

    // ==================== 系统管理 ====================
    String LOG_LIST         = "log:list";
    String FILE_UPLOAD      = "file:upload";
    String PERMISSION_ASSIGN = "permission:assign";
}
