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
    `is_delete` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`)
);
