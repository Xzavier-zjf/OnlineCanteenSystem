-- 为商品表添加merchant_id字段

USE canteen_system;

-- 添加merchant_id字段
ALTER TABLE `product` 
ADD COLUMN `merchant_id` BIGINT DEFAULT 2 COMMENT '商户ID' AFTER `category_id`,
ADD INDEX `idx_merchant_id` (`merchant_id`);

-- 更新现有商品的商户ID
UPDATE `product` SET `merchant_id` = 2 WHERE `merchant_id` IS NULL;