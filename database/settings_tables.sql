-- 系统设置相关表结构

USE canteen_system;

-- 1. 用户通知设置表
CREATE TABLE IF NOT EXISTS `user_notification_settings` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设置ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `email_notification` TINYINT DEFAULT 1 COMMENT '邮件通知（1=开启，0=关闭）',
    `sms_notification` TINYINT DEFAULT 0 COMMENT '短信通知（1=开启，0=关闭）',
    `order_notification` TINYINT DEFAULT 1 COMMENT '订单通知（1=开启，0=关闭）',
    `promotion_notification` TINYINT DEFAULT 1 COMMENT '促销通知（1=开启，0=关闭）',
    `system_notification` TINYINT DEFAULT 1 COMMENT '系统通知（1=开启，0=关闭）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_id` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知设置表';

-- 2. 用户偏好设置表
CREATE TABLE IF NOT EXISTS `user_preference_settings` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设置ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `language` VARCHAR(10) DEFAULT 'zh-CN' COMMENT '语言设置',
    `theme` VARCHAR(20) DEFAULT 'light' COMMENT '主题设置（light/dark）',
    `timezone` VARCHAR(50) DEFAULT 'Asia/Shanghai' COMMENT '时区设置',
    `auto_login` TINYINT DEFAULT 0 COMMENT '自动登录（1=开启，0=关闭）',
    `page_size` INT DEFAULT 20 COMMENT '分页大小',
    `default_payment` VARCHAR(20) DEFAULT 'alipay' COMMENT '默认支付方式',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_id` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好设置表';

-- 3. 用户登录记录表
CREATE TABLE IF NOT EXISTS `user_login_records` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `login_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `login_ip` VARCHAR(45) COMMENT '登录IP',
    `login_location` VARCHAR(100) COMMENT '登录地点',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `status` ENUM('SUCCESS','FAILED') DEFAULT 'SUCCESS' COMMENT '登录状态',
    `fail_reason` VARCHAR(200) COMMENT '失败原因',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_login_time` (`login_time` DESC),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录记录表';

-- 4. 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(20) DEFAULT 'STRING' COMMENT '配置类型（STRING/NUMBER/BOOLEAN/JSON）',
    `description` VARCHAR(200) COMMENT '配置描述',
    `is_public` TINYINT DEFAULT 0 COMMENT '是否公开（1=公开，0=私有）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_config_key` (`config_key`),
    INDEX `idx_is_public` (`is_public`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 5. 商户设置表
CREATE TABLE IF NOT EXISTS `merchant_settings` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设置ID',
    `user_id` BIGINT NOT NULL COMMENT '商户用户ID',
    `shop_name` VARCHAR(100) COMMENT '店铺名称',
    `shop_description` TEXT COMMENT '店铺描述',
    `shop_logo` VARCHAR(200) COMMENT '店铺Logo',
    `business_hours` JSON COMMENT '营业时间（JSON格式）',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `shop_address` VARCHAR(200) COMMENT '店铺地址',
    `delivery_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '配送费',
    `min_order_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '起送金额',
    `auto_accept_order` TINYINT DEFAULT 1 COMMENT '自动接单（1=开启，0=关闭）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_id` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户设置表';

-- 插入默认系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `is_public`) VALUES
('system.name', '高校食堂订餐系统', 'STRING', '系统名称', 1),
('system.version', '1.0.0', 'STRING', '系统版本', 1),
('system.logo', '/images/logo.png', 'STRING', '系统Logo', 1),
('system.contact_email', 'admin@canteen.com', 'STRING', '联系邮箱', 1),
('system.contact_phone', '400-123-4567', 'STRING', '联系电话', 1),
('order.auto_cancel_minutes', '30', 'NUMBER', '订单自动取消时间（分钟）', 0),
('payment.timeout_minutes', '15', 'NUMBER', '支付超时时间（分钟）', 0),
('user.max_login_attempts', '5', 'NUMBER', '最大登录尝试次数', 0),
('notification.email_enabled', 'true', 'BOOLEAN', '邮件通知是否启用', 0),
('notification.sms_enabled', 'false', 'BOOLEAN', '短信通知是否启用', 0);

-- 为现有用户创建默认设置
INSERT INTO `user_notification_settings` (`user_id`, `email_notification`, `sms_notification`, `order_notification`, `promotion_notification`, `system_notification`)
SELECT `id`, 1, 0, 1, 1, 1 FROM `user` WHERE NOT EXISTS (
    SELECT 1 FROM `user_notification_settings` WHERE `user_notification_settings`.`user_id` = `user`.`id`
);

INSERT INTO `user_preference_settings` (`user_id`, `language`, `theme`, `timezone`, `auto_login`, `page_size`, `default_payment`)
SELECT `id`, 'zh-CN', 'light', 'Asia/Shanghai', 0, 20, 'alipay' FROM `user` WHERE NOT EXISTS (
    SELECT 1 FROM `user_preference_settings` WHERE `user_preference_settings`.`user_id` = `user`.`id`
);

-- 为商户用户创建默认店铺设置
INSERT INTO `merchant_settings` (`user_id`, `shop_name`, `shop_description`, `business_hours`, `contact_phone`, `delivery_fee`, `min_order_amount`, `auto_accept_order`)
SELECT 
    `id`, 
    CONCAT(`real_name`, '的店铺'), 
    '欢迎来到我的店铺', 
    '{"monday": {"open": "08:00", "close": "20:00"}, "tuesday": {"open": "08:00", "close": "20:00"}, "wednesday": {"open": "08:00", "close": "20:00"}, "thursday": {"open": "08:00", "close": "20:00"}, "friday": {"open": "08:00", "close": "20:00"}, "saturday": {"open": "09:00", "close": "21:00"}, "sunday": {"open": "09:00", "close": "21:00"}}',
    `phone`,
    2.00,
    20.00,
    1
FROM `user` 
WHERE `role` = 'MERCHANT' AND NOT EXISTS (
    SELECT 1 FROM `merchant_settings` WHERE `merchant_settings`.`user_id` = `user`.`id`
);

-- 添加avatar字段到user表（如果不存在）
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `avatar` VARCHAR(200) COMMENT '头像URL' AFTER `address`;

-- 查看创建的表
SHOW TABLES LIKE '%settings%';
SHOW TABLES LIKE '%config%';
SHOW TABLES LIKE '%records%';