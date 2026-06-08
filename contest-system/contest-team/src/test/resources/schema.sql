SET NON_KEYWORDS USER;
DROP ALL OBJECTS;

CREATE TABLE IF NOT EXISTS `college` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
);

CREATE TABLE IF NOT EXISTS `major` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `college_id` INT NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_college_major` (`college_id`, `name`),
    KEY `idx_college` (`college_id`)
);

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(20) NOT NULL,
    `password` VARCHAR(100) NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `role` TINYINT NOT NULL DEFAULT 0,
    `email` VARCHAR(100) DEFAULT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `college_id` INT DEFAULT NULL,
    `major_id` INT DEFAULT NULL,
    `college` VARCHAR(50) DEFAULT NULL,
    `major` VARCHAR(50) DEFAULT NULL,
    `class_name` VARCHAR(50) DEFAULT NULL,
    `avatar_url` VARCHAR(255) DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 0,
    `email_notify` TINYINT NOT NULL DEFAULT 1,
    `sms_notify` TINYINT NOT NULL DEFAULT 1,
    `deadline_notify` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_user_college` (`college`),
    KEY `idx_status` (`status`)
);

CREATE TABLE IF NOT EXISTS `contest` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `category` VARCHAR(20) DEFAULT NULL,
    `level` VARCHAR(10) DEFAULT NULL,
    `organizer` VARCHAR(100) DEFAULT NULL,
    `contest_time` DATETIME DEFAULT NULL,
    `register_start_time` DATETIME DEFAULT NULL,
    `register_end_time` DATETIME DEFAULT NULL,
    `location` VARCHAR(200) DEFAULT NULL,
    `cover_image_url` VARCHAR(255) DEFAULT NULL,
    `description` TEXT,
    `attachment_urls` TEXT,
    `contest_type` TINYINT NOT NULL DEFAULT 0,
    `team_min_size` INT NOT NULL DEFAULT 0,
    `team_max_size` INT NOT NULL DEFAULT 0,
    `max_participants` INT NOT NULL DEFAULT 0,
    `create_by` BIGINT UNSIGNED DEFAULT NULL,
    `is_delete` TINYINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 0,
    `current_count` INT DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_status_type` (`status`, `contest_type`),
    KEY `idx_time` (`register_end_time`)
);

CREATE TABLE IF NOT EXISTS `team` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `leader_id` BIGINT UNSIGNED NOT NULL,
    `teacher_id` BIGINT UNSIGNED DEFAULT NULL,
    `team_name` VARCHAR(50) NOT NULL,
    `team_no` VARCHAR(20) NOT NULL,
    `invite_code` VARCHAR(10) DEFAULT NULL,
    `invite_code_expire` DATETIME DEFAULT NULL,
    `is_delete` TINYINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 0,
    `member_count` INT NOT NULL DEFAULT 1,
    `material_urls` TEXT,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_no` (`team_no`),
    UNIQUE KEY `uk_invite_code` (`invite_code`),
    KEY `idx_invite` (`invite_code`)
);

CREATE TABLE IF NOT EXISTS `team_member` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `team_id` BIGINT UNSIGNED NOT NULL,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `role` TINYINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 0,
    `apply_time` DATETIME DEFAULT NULL,
    `handle_time` DATETIME DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_team` (`team_id`),
    KEY `idx_member_user` (`user_id`)
);

CREATE TABLE IF NOT EXISTS `registration` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `contest_id` BIGINT UNSIGNED NOT NULL,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `team_id` BIGINT UNSIGNED DEFAULT NULL,
    `reg_type` TINYINT NOT NULL DEFAULT 0,
    `is_delete` TINYINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 0,
    `review_reason` VARCHAR(255) DEFAULT NULL,
    `material_urls` TEXT,
    `remark` VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_contest_status` (`contest_id`, `status`),
    KEY `idx_reg_user` (`user_id`)
);

CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `type` TINYINT NOT NULL DEFAULT 0,
    `title` VARCHAR(100) NOT NULL,
    `content` TEXT,
    `related_id` BIGINT DEFAULT NULL,
    `related_type` VARCHAR(20) DEFAULT NULL,
    `is_read` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_read` (`user_id`, `is_read`),
    KEY `idx_type` (`type`)
);
