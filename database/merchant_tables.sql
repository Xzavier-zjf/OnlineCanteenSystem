-- 商户相关数据表

USE canteen_system;

-- 商户店铺设置表
CREATE TABLE IF NOT EXISTS `merchant_shop_settings` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设置ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
    `auto_accept_order` TINYINT DEFAULT 0 COMMENT '自动接单（1=是，0=否）',
    `preparation_time` INT DEFAULT 15 COMMENT '备餐时间（分钟）',
    `max_orders_per_hour` INT DEFAULT 30 COMMENT '每小时最大订单数',
    `min_order_amount` DECIMAL(10,2) DEFAULT 10.00 COMMENT '最低起送金额',
    `delivery_fee` DECIMAL(10,2) DEFAULT 2.00 COMMENT '配送费',
    `packaging_fee` DECIMAL(10,2) DEFAULT 1.00 COMMENT '包装费',
    `business_start_time` TIME DEFAULT '08:00:00' COMMENT '营业开始时间',
    `business_end_time` TIME DEFAULT '22:00:00' COMMENT '营业结束时间',
    `is_open` TINYINT DEFAULT 1 COMMENT '是否营业（1=营业，0=休息）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_merchant_id` (`merchant_id`),
    FOREIGN KEY (`merchant_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户店铺设置表';

-- 商户收入明细表
CREATE TABLE IF NOT EXISTS `merchant_revenue_detail` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收入明细ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
    `order_id` BIGINT COMMENT '订单ID',
    `revenue_date` DATE NOT NULL COMMENT '收入日期',
    `order_count` INT DEFAULT 0 COMMENT '订单数量',
    `total_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '总金额',
    `avg_order_value` DECIMAL(10,2) DEFAULT 0.00 COMMENT '平均订单价值',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_merchant_date` (`merchant_id`, `revenue_date`),
    INDEX `idx_revenue_date` (`revenue_date`),
    FOREIGN KEY (`merchant_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户收入明细表';

-- 插入商户店铺设置测试数据
INSERT INTO `merchant_shop_settings` (`merchant_id`, `auto_accept_order`, `preparation_time`, `max_orders_per_hour`, `min_order_amount`, `delivery_fee`, `packaging_fee`, `business_start_time`, `business_end_time`, `is_open`) VALUES
(2, 1, 20, 25, 15.00, 3.00, 1.50, '07:30:00', '21:30:00', 1),
(15, 0, 15, 30, 10.00, 2.00, 1.00, '08:00:00', '22:00:00', 1),
(19, 0, 18, 35, 12.00, 2.50, 1.20, '08:30:00', '21:00:00', 1)
ON DUPLICATE KEY UPDATE 
    `auto_accept_order` = VALUES(`auto_accept_order`),
    `preparation_time` = VALUES(`preparation_time`),
    `max_orders_per_hour` = VALUES(`max_orders_per_hour`),
    `min_order_amount` = VALUES(`min_order_amount`),
    `delivery_fee` = VALUES(`delivery_fee`),
    `packaging_fee` = VALUES(`packaging_fee`),
    `business_start_time` = VALUES(`business_start_time`),
    `business_end_time` = VALUES(`business_end_time`),
    `is_open` = VALUES(`is_open`);

-- 插入商户收入明细测试数据（最近7天）
INSERT INTO `merchant_revenue_detail` (`merchant_id`, `revenue_date`, `order_count`, `total_amount`, `avg_order_value`) VALUES
-- 商户2的数据
(2, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 16, 420.00, 26.25),
(2, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 19, 494.00, 26.00),
(2, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 21, 546.00, 26.00),
(2, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 24, 624.00, 26.00),
(2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 18, 468.00, 26.00),
(2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 22, 572.00, 26.00),
(2, CURDATE(), 10, 260.00, 26.00),

-- 商户15的数据
(15, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 18, 450.00, 25.00),
(15, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 22, 580.50, 26.39),
(15, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 15, 375.00, 25.00),
(15, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 28, 742.00, 26.50),
(15, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 20, 520.00, 26.00),
(15, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 25, 650.00, 26.00),
(15, CURDATE(), 12, 312.00, 26.00),

-- 商户19的数据
(19, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 14, 364.00, 26.00),
(19, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 17, 442.00, 26.00),
(19, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 19, 494.00, 26.00),
(19, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 22, 572.00, 26.00),
(19, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 16, 416.00, 26.00),
(19, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 20, 520.00, 26.00),
(19, CURDATE(), 8, 208.00, 26.00)
ON DUPLICATE KEY UPDATE 
    `order_count` = VALUES(`order_count`),
    `total_amount` = VALUES(`total_amount`),
    `avg_order_value` = VALUES(`avg_order_value`);