-- 角色功能升级脚本
-- 为支持管理员和商户角色功能，需要对现有表结构进行升级

USE canteen_system;

-- 1. 为商品表添加商户ID字段
ALTER TABLE `product` ADD COLUMN `merchant_id` BIGINT COMMENT '商户ID' AFTER `category_id`;
ALTER TABLE `product` ADD INDEX `idx_merchant_id` (`merchant_id`);

-- 2. 为订单表添加商户ID字段
ALTER TABLE `orders` ADD COLUMN `merchant_id` BIGINT COMMENT '商户ID' AFTER `user_id`;
ALTER TABLE `orders` ADD INDEX `idx_merchant_id` (`merchant_id`);

-- 3. 创建商户信息扩展表
CREATE TABLE IF NOT EXISTS `merchant_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT UNIQUE NOT NULL COMMENT '用户ID',
    `shop_name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
    `shop_logo` VARCHAR(200) COMMENT '店铺Logo',
    `shop_description` TEXT COMMENT '店铺描述',
    `business_license` VARCHAR(100) COMMENT '营业执照号',
    `contact_person` VARCHAR(50) COMMENT '联系人',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `business_hours` VARCHAR(100) COMMENT '营业时间',
    `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态（0=待审核，1=已通过，2=已拒绝）',
    `audit_reason` VARCHAR(200) COMMENT '审核原因',
    `audit_time` TIMESTAMP NULL COMMENT '审核时间',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_audit_status` (`audit_status`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户信息表';

-- 4. 创建商品审核记录表
CREATE TABLE IF NOT EXISTS `product_audit` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态（0=待审核，1=已通过，2=已拒绝）',
    `audit_reason` VARCHAR(200) COMMENT '审核原因',
    `auditor_id` BIGINT COMMENT '审核员ID',
    `audit_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_audit_status` (`audit_status`),
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`auditor_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品审核记录表';

-- 5. 创建推荐商品表
CREATE TABLE IF NOT EXISTS `recommend_product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `recommend_type` VARCHAR(20) NOT NULL COMMENT '推荐类型（HOT=热门推荐，NEW=新品推荐，FEATURED=精选推荐）',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1=启用，0=禁用）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_recommend_type` (`recommend_type`),
    INDEX `idx_sort_order` (`sort_order`),
    UNIQUE KEY `uk_product_type` (`product_id`, `recommend_type`),
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐商品表';

-- 6. 插入默认管理员账号
INSERT IGNORE INTO `user` (`username`, `password`, `email`, `real_name`, `role`, `status`, `create_time`, `update_time`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyZOzx8Qb6.1JCnhKI2Ov.Cq6Ey', 'admin@canteen.com', '系统管理员', 'ADMIN', 1, NOW(), NOW());
-- 默认密码：123456

-- 7. 为现有商品分配默认商户（如果有数据的话）
-- 这里假设用户ID为1的是默认商户，实际使用时需要根据具体情况调整
UPDATE `product` SET `merchant_id` = 1 WHERE `merchant_id` IS NULL AND EXISTS (SELECT 1 FROM `user` WHERE `id` = 1 AND `role` = 'MERCHANT');

-- 8. 为现有订单分配默认商户（如果有数据的话）
UPDATE `orders` SET `merchant_id` = 1 WHERE `merchant_id` IS NULL AND EXISTS (SELECT 1 FROM `user` WHERE `id` = 1 AND `role` = 'MERCHANT');

-- 9. 创建系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `config_key` VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_desc` VARCHAR(200) COMMENT '配置描述',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 10. 插入默认系统配置
INSERT IGNORE INTO `system_config` (`config_key`, `config_value`, `config_desc`) VALUES
('merchant_auto_approve', 'false', '商户注册是否自动审核通过'),
('product_auto_approve', 'false', '商品发布是否自动审核通过'),
('recommend_product_limit', '10', '首页推荐商品数量限制');

COMMIT;