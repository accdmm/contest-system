-- 高校竞赛报名管理系统 数据库初始化脚本
-- MySQL 5.7+ / InnoDB / UTF8MB4

CREATE DATABASE IF NOT EXISTS contest_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE contest_system;

-- 1. college 学院表
CREATE TABLE `college` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(50) NOT NULL COMMENT '学院名称',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院表';

-- 2. major 专业表
CREATE TABLE `major` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `college_id` INT NOT NULL COMMENT '所属学院ID',
    `name` VARCHAR(50) NOT NULL COMMENT '专业名称',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_college_major` (`college_id`, `name`),
    KEY `idx_college` (`college_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业表';

-- 3. user 用户表
CREATE TABLE `user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(20) NOT NULL COMMENT '学号或管理员登录账号',
    `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码',
    `name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '用户角色 0=学生 1=管理员 2=教师',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '绑定邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `college_id` INT DEFAULT NULL COMMENT '学院ID',
    `major_id` INT DEFAULT NULL COMMENT '专业ID',
    `college` VARCHAR(50) DEFAULT NULL COMMENT '学院名称(冗余)',
    `major` VARCHAR(50) DEFAULT NULL COMMENT '专业名称(冗余)',
    `class_name` VARCHAR(50) DEFAULT NULL COMMENT '班级',
    `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像MinIO访问地址',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '账号状态 0=正常 1=冻结',
    `email_notify` TINYINT NOT NULL DEFAULT 1 COMMENT '邮件通知 0=关闭 1=开启',
    `sms_notify` TINYINT NOT NULL DEFAULT 1 COMMENT '短信通知 0=关闭 1=开启',
    `deadline_notify` TINYINT NOT NULL DEFAULT 1 COMMENT '截止提醒 0=关闭 1=开启',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_college` (`college`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. contest 竞赛表
CREATE TABLE `contest` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(100) NOT NULL COMMENT '竞赛名称',
    `category` VARCHAR(20) DEFAULT NULL COMMENT '竞赛类别',
    `level` VARCHAR(10) DEFAULT NULL COMMENT '竞赛级别 校级/省级/国家级',
    `organizer` VARCHAR(100) DEFAULT NULL COMMENT '主办方',
    `contest_time` DATETIME DEFAULT NULL COMMENT '正式竞赛时间',
    `register_start_time` DATETIME DEFAULT NULL COMMENT '报名开始时间',
    `register_end_time` DATETIME DEFAULT NULL COMMENT '报名截止时间',
    `location` VARCHAR(200) DEFAULT NULL COMMENT '竞赛地点',
    `cover_image_url` VARCHAR(255) DEFAULT NULL COMMENT '封面图MinIO地址',
    `description` TEXT COMMENT '详细介绍（富文本HTML）',
    `attachment_urls` TEXT COMMENT '附件URL列表 JSON字符串',
    `contest_type` TINYINT NOT NULL DEFAULT 0 COMMENT '参赛形式 0=个人赛 1=团队赛 2=两者皆可',
    `team_min_size` INT NOT NULL DEFAULT 0 COMMENT '团队最少人数',
    `team_max_size` INT NOT NULL DEFAULT 0 COMMENT '团队最多人数',
    `max_participants` INT NOT NULL DEFAULT 0 COMMENT '人数/队伍上限 0=不限',
    `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人用户ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '竞赛状态 0=草稿/下架 1=上架报名中 2=报名结束',
    `current_count` INT NOT NULL DEFAULT 0 COMMENT '当前已报名人数/队伍数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status_type` (`status`, `contest_type`),
    KEY `idx_time` (`register_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛表';

-- 3. team 团队表
CREATE TABLE `team` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `leader_id` BIGINT UNSIGNED NOT NULL COMMENT '队长用户ID',
    `teacher_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '指导教师用户ID',
    `team_name` VARCHAR(50) NOT NULL COMMENT '团队名称',
    `team_no` VARCHAR(20) NOT NULL COMMENT '唯一团队编号',
    `invite_code` VARCHAR(10) DEFAULT NULL COMMENT '6位邀请码',
    `invite_code_expire` DATETIME DEFAULT NULL COMMENT '邀请码过期时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '团队状态 0=组建中 1=已提交报名(待审核) 2=已通过 3=已驳回',
    `member_count` INT NOT NULL DEFAULT 1 COMMENT '当前成员数',
    `material_urls` TEXT COMMENT '团队报名材料URL JSON字符串',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_no` (`team_no`),
    UNIQUE KEY `uk_invite_code` (`invite_code`),
    KEY `idx_invite` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队表';

-- 4. team_member 团队成员关系表
CREATE TABLE `team_member` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `team_id` BIGINT UNSIGNED NOT NULL COMMENT '所属团队ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '成员用户ID',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '成员角色 0=普通成员 1=队长',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '入队审核状态 0=待审核 1=已通过 2=已拒绝/已移除',
    `apply_time` DATETIME DEFAULT NULL COMMENT '申请入队时间',
    `handle_time` DATETIME DEFAULT NULL COMMENT '队长处理时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_team` (`team_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员关系表';

-- 5. registration 报名记录表
CREATE TABLE `registration` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `contest_id` BIGINT UNSIGNED NOT NULL COMMENT '报名竞赛ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '报名者ID/队长ID',
    `team_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '团队ID(团队赛时必填)',
    `reg_type` TINYINT NOT NULL DEFAULT 0 COMMENT '报名类型 0=个人赛 1=团队赛',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态 0=待审核 1=已通过 2=已驳回 3=已取消',
    `review_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
    `material_urls` TEXT COMMENT '个人赛材料URL JSON字符串',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '报名备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_contest_status` (`contest_id`, `status`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名记录表';

-- 6. notification 消息通知表
CREATE TABLE `notification` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '接收人ID 0=全员广播',
    `type` TINYINT NOT NULL DEFAULT 0 COMMENT '通知类型 0=审核结果 1=入队申请 2=入队结果 3=竞赛变更 4=系统公告',
    `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content` TEXT COMMENT '通知内容',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
    `related_type` VARCHAR(20) DEFAULT NULL COMMENT '关联业务类型 registration/team/contest',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读 0=未读 1=已读',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_read` (`user_id`, `is_read`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 7. cms_content 内容管理表
CREATE TABLE `cms_content` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `content_type` TINYINT NOT NULL DEFAULT 0 COMMENT '内容类型 0=轮播图 1=公告',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '标题',
    `content` TEXT COMMENT '公告富文本内容',
    `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片地址',
    `link_url` VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    `position` VARCHAR(20) DEFAULT NULL COMMENT '展示位置 home_scroll/message_center/popup',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '展示状态 0=隐藏 1=展示',
    `publish_time` DATETIME DEFAULT NULL COMMENT '定时发布时间 NULL=立即发布',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type_status` (`content_type`, `status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容管理表';

-- 8. operation_log 操作日志表
CREATE TABLE `operation_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作管理员ID',
    `action` VARCHAR(50) DEFAULT NULL COMMENT '操作类型',
    `detail` TEXT COMMENT '操作详情',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '操作人IP',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 9. ai_conversation AI对话会话表
CREATE TABLE `ai_conversation` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '对话标题',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话会话表';

-- 11. permission 权限表
CREATE TABLE IF NOT EXISTS `permission` (
    `id`       INT AUTO_INCREMENT PRIMARY KEY,
    `code`     VARCHAR(50) NOT NULL COMMENT '权限编码',
    `name`     VARCHAR(50) NOT NULL COMMENT '权限名称',
    `module`   VARCHAR(50) NOT NULL COMMENT '所属模块',
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 12. role_permission 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permission` (
    `id`            INT AUTO_INCREMENT PRIMARY KEY,
    `role`          TINYINT NOT NULL COMMENT '角色 0=学生 1=管理员 2=教师',
    `permission_id` INT NOT NULL,
    UNIQUE KEY `uk_role_perm` (`role`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 13. user_permission 用户权限关联表
CREATE TABLE IF NOT EXISTS `user_permission` (
    `id`            INT AUTO_INCREMENT PRIMARY KEY,
    `user_id`       BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `permission_id` INT NOT NULL COMMENT '权限ID',
    UNIQUE KEY `uk_user_perm` (`user_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户权限关联表';

-- 10. ai_message AI对话消息表
CREATE TABLE `ai_message` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `conversation_id` BIGINT UNSIGNED NOT NULL COMMENT '所属会话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色 user/assistant',
    `content` TEXT COMMENT '消息内容',
    `tokens` INT DEFAULT NULL COMMENT 'Token数量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息表';
