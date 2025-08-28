-- 测试用户数据（使用明文密码便于测试）
USE canteen_system;

-- 清空已有测试用户数据（如果存在）
DELETE FROM `user` WHERE username IN ('testuser1', 'testuser2', 'admin', 'merchant', 'user1');

-- 插入测试用户（密码使用明文存储，仅用于开发测试）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `real_name`, `college`, `address`, `role`, `status`) VALUES
('admin', 'admin123', 'admin@canteen.com', '13800000001', '系统管理员', '管理学院', '行政办公楼101', 'ADMIN', 1),
('merchant', 'merchant123', 'merchant@canteen.com', '13800000002', '食堂商户', '后勤服务中心', '食堂管理办公室', 'MERCHANT', 1),
('testuser1', 'password123', 'test1@example.com', '13800000003', '张三', '计算机学院', '学生宿舍1号楼202', 'USER', 1),
('testuser2', 'password123', 'test2@example.com', '13800000004', '李四', '电子信息学院', '学生宿舍2号楼301', 'USER', 1);

-- 查看插入的用户数据
SELECT id, username, email, phone, real_name, college, address, role, status, create_time FROM `user`;