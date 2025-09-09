-- 修复用户数据 - 确保测试账号存在
USE canteen_system;

-- 删除可能存在的旧数据
DELETE FROM user WHERE username IN ('admin', 'merchant', 'user1');

-- 插入测试用户（使用明文密码，因为代码中使用明文比较）
INSERT INTO user (username, password, real_name, role, status, create_time, update_time) VALUES
('admin', 'admin123', '系统管理员', 'ADMIN', 1, NOW(), NOW()),
('merchant', 'admin123', '食堂商户', 'MERCHANT', 1, NOW(), NOW()),
('user1', 'admin123', '测试用户', 'USER', 1, NOW(), NOW());

-- 验证插入结果
SELECT id, username, password, real_name, role, status FROM user WHERE username IN ('admin', 'merchant', 'user1');