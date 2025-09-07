-- 为用户表添加avatar字段
USE canteen_system;

-- 添加avatar字段
ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(200) COMMENT '头像URL' AFTER `address`;

-- 查看表结构确认
DESCRIBE `user`;