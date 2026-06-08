-- 数据库迁移脚本：统一逻辑删除字段（is_delete）
-- 适用于已存在旧表（contest/registration 缺少 is_delete，team 使用 deleted）的数据库

ALTER TABLE `contest`
    ADD COLUMN `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除' AFTER `contest_type`;

ALTER TABLE `registration`
    ADD COLUMN `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除' AFTER `status`;

ALTER TABLE `team`
    CHANGE COLUMN `deleted` `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除';
