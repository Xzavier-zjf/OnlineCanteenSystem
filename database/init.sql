-- 高校食堂订餐系统数据库建表脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS canteen_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE canteen_system;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（加密后）',
    `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `real_name` VARCHAR(100) COMMENT '真实姓名',
    `college` VARCHAR(100) COMMENT '学院',
    `address` VARCHAR(200) COMMENT '地址',
    `role` ENUM('USER','MERCHANT','ADMIN') DEFAULT 'USER' COMMENT '角色（USER/MERCHANT/ADMIN）',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1=正常，0=禁用）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`),
    INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 餐品分类表
CREATE TABLE IF NOT EXISTS `product_category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(200) COMMENT '分类描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1=启用，0=禁用）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_status` (`status`),
    INDEX `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='餐品分类表';

-- 3. 餐品表
CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '餐品ID',
    `name` VARCHAR(100) NOT NULL COMMENT '餐品名称',
    `description` TEXT COMMENT '餐品描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `image_url` VARCHAR(200) COMMENT '图片URL',
    `stock` INT DEFAULT 0 COMMENT '库存数量',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1=上架，0=下架）',
    `is_hot` TINYINT DEFAULT 0 COMMENT '是否热门（1=是，0=否）',
    `rating` DECIMAL(3,2) DEFAULT 0.00 COMMENT '评分（0.00-5.00）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_hot` (`is_hot`),
    INDEX `idx_rating` (`rating` DESC),
    INDEX `idx_sales` (`sales` DESC),
    INDEX `idx_create_time` (`create_time` DESC),
    FOREIGN KEY (`category_id`) REFERENCES `product_category`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='餐品表';

-- 4. 订单表
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(32) UNIQUE NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `status` ENUM('PENDING','PAID','PREPARING','COMPLETED','CANCELLED') DEFAULT 'PENDING' COMMENT '订单状态',
    `remark` VARCHAR(200) COMMENT '备注',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time` DESC),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 5. 订单详情表
CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '详情ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '餐品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '餐品名称（冗余存储）',
    `quantity` INT NOT NULL COMMENT '数量',
    `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计',
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_product_id` (`product_id`),
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单详情表';

-- 插入初始数据

-- 插入管理员用户 (密码: admin123，实际使用时需要加密)
INSERT INTO `user` (`username`, `password`, `real_name`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyOFSQLr5iY/FTfRfKcI7nAmHUe', '系统管理员', 'ADMIN'),
('merchant', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyOFSQLr5iY/FTfRfKcI7nAmHUe', '食堂商户', 'MERCHANT'),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyOFSQLr5iY/FTfRfKcI7nAmHUe', '测试用户', 'USER');

-- 插入餐品分类
INSERT INTO `product_category` (`name`, `description`, `sort_order`) VALUES
('主食', '米饭、面条等主食类', 1),
('汤类', '各种汤品', 2),
('素菜', '素食菜品', 3),
('荤菜', '肉类菜品', 4),
('饮品', '各种饮品', 5),
('小食', '小食零食', 6);

-- 插入示例餐品
INSERT INTO `product` (`name`, `description`, `price`, `category_id`, `stock`, `sales`, `image_url`) VALUES
('红烧肉盖饭', '经典红烧肉配米饭', 15.00, 1, 50, 120, '/images/hongshaorou.jpg'),
('西红柿鸡蛋面', '新鲜西红柿配鸡蛋面条', 12.00, 1, 30, 85, '/images/xihongshi.jpg'),
('紫菜蛋花汤', '清淡紫菜蛋花汤', 8.00, 2, 100, 200, '/images/zicai.jpg'),
('麻婆豆腐', '经典川菜麻婆豆腐', 10.00, 3, 40, 95, '/images/mapo.jpg'),
('糖醋里脊', '酸甜可口糖醋里脊', 18.00, 4, 25, 75, '/images/tangcu.jpg'),
('鲜橙汁', '新鲜橙子榨汁', 6.00, 5, 80, 150, '/images/orange.jpg'),
('小笼包', '上海风味小笼包', 8.00, 6, 60, 110, '/images/xiaolongbao.jpg'),
('宫保鸡丁', '经典川菜宫保鸡丁', 16.00, 4, 35, 90, '/images/gongbao.jpg');

-- 创建订单号生成函数（可选）
DELIMITER //
CREATE FUNCTION generate_order_no() RETURNS VARCHAR(32)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE order_no VARCHAR(32);
    SET order_no = CONCAT('CT', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND() * 999999), 6, '0'));
    RETURN order_no;
END //
DELIMITER ;

-- 查看表结构
SHOW TABLES;