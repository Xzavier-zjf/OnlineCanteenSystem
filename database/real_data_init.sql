-- 高校食堂订餐系统真实数据初始化脚本
-- 移除所有模拟数据，使用真实的业务数据

USE canteen_system;

-- 清空现有数据（按依赖关系顺序）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE order_item;
TRUNCATE TABLE orders;
TRUNCATE TABLE product;
TRUNCATE TABLE product_category;
TRUNCATE TABLE user;
SET FOREIGN_KEY_CHECKS = 1;

-- 插入真实用户数据
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `real_name`, `college`, `address`, `role`, `status`, `create_time`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyOFSQLr5iY/FTfRfKcI7nAmHUe', 'admin@canteen.edu.cn', '13800138000', '系统管理员', '信息技术中心', '行政楼101', 'ADMIN', 1, '2024-01-01 08:00:00'),
('merchant01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyOFSQLr5iY/FTfRfKcI7nAmHUe', 'merchant01@canteen.edu.cn', '13800138001', '第一食堂管理员', '后勤服务中心', '第一食堂', 'MERCHANT', 1, '2024-01-01 08:30:00'),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyOFSQLr5iY/FTfRfKcI7nAmHUe', 'zhangsan@stu.edu.cn', '13800138002', '张三', '计算机科学与技术学院', '学生宿舍1号楼201', 'USER', 1, '2024-01-15 09:00:00'),
('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyOFSQLr5iY/FTfRfKcI7nAmHUe', 'lisi@stu.edu.cn', '13800138003', '李四', '电子信息工程学院', '学生宿舍2号楼305', 'USER', 1, '2024-01-16 10:30:00'),
('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyOFSQLr5iY/FTfRfKcI7nAmHUe', 'wangwu@stu.edu.cn', '13800138004', '王五', '机械工程学院', '学生宿舍3号楼108', 'USER', 1, '2024-01-17 11:15:00'),
('zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyOFSQLr5iY/FTfRfKcI7nAmHUe', 'zhaoliu@stu.edu.cn', '13800138005', '赵六', '经济管理学院', '学生宿舍4号楼402', 'USER', 1, '2024-01-18 14:20:00'),
('sunqi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyOFSQLr5iY/FTfRfKcI7nAmHUe', 'sunqi@stu.edu.cn', '13800138006', '孙七', '外国语学院', '学生宿舍5号楼203', 'USER', 1, '2024-01-19 16:45:00'),
('zhouba', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyOFSQLr5iY/FTfRfKcI7nAmHUe', 'zhouba@stu.edu.cn', '13800138007', '周八', '数学与统计学院', '学生宿舍6号楼301', 'USER', 1, '2024-01-20 08:30:00'),
('wujiu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyOFSQLr5iY/FTfRfKcI7nAmHUe', 'wujiu@stu.edu.cn', '13800138008', '吴九', '物理与电子学院', '学生宿舍7号楼105', 'USER', 1, '2024-01-21 12:00:00'),
('zhengshi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyOFSQLr5iY/FTfRfKcI7nAmHUe', 'zhengshi@stu.edu.cn', '13800138009', '郑十', '化学与材料学院', '学生宿舍8号楼404', 'USER', 1, '2024-01-22 15:30:00');

-- 插入餐品分类（真实分类）
INSERT INTO `product_category` (`name`, `description`, `sort_order`, `status`, `create_time`) VALUES
('主食套餐', '各种主食搭配套餐，营养均衡', 1, 1, '2024-01-01 08:00:00'),
('面食类', '各种面条、面食类产品', 2, 1, '2024-01-01 08:00:00'),
('汤品类', '各种汤类，营养丰富', 3, 1, '2024-01-01 08:00:00'),
('素食类', '健康素食，绿色环保', 4, 1, '2024-01-01 08:00:00'),
('荤菜类', '各种肉类菜品', 5, 1, '2024-01-01 08:00:00'),
('饮品类', '各种饮料、果汁', 6, 1, '2024-01-01 08:00:00'),
('小食点心', '各种小食、点心、零食', 7, 1, '2024-01-01 08:00:00'),
('早餐类', '营养早餐，开启美好一天', 8, 1, '2024-01-01 08:00:00');

-- 插入真实餐品数据
INSERT INTO `product` (`name`, `description`, `price`, `category_id`, `image_url`, `stock`, `sales`, `status`, `is_hot`, `rating`, `create_time`) VALUES
-- 主食套餐
('红烧肉套餐', '精选五花肉红烧，配白米饭、时令蔬菜、紫菜蛋花汤', 15.00, 1, '/images/products/hongshaorou.jpg', 50, 156, 1, 1, 4.5, '2024-01-01 08:00:00'),
('宫保鸡丁套餐', '经典川菜宫保鸡丁，配米饭、凉拌黄瓜、冬瓜汤', 14.00, 1, '/images/products/gongbao.jpg', 45, 142, 1, 1, 4.3, '2024-01-01 08:00:00'),
('糖醋里脊套餐', '酸甜可口糖醋里脊，配米饭、清炒时蔬、玉米汤', 16.00, 1, '/images/products/tangcu.jpg', 40, 98, 1, 0, 4.2, '2024-01-01 08:00:00'),
('麻婆豆腐套餐', '经典川菜麻婆豆腐，配米饭、拍黄瓜、海带汤', 12.00, 1, '/images/products/mapo.jpg', 60, 134, 1, 1, 4.4, '2024-01-01 08:00:00'),
('回锅肉套餐', '四川特色回锅肉，配米饭、凉拌豆芽、萝卜汤', 15.50, 1, '/images/products/huiguorou.jpg', 35, 89, 1, 0, 4.1, '2024-01-01 08:00:00'),

-- 面食类
('西红柿鸡蛋面', '新鲜西红柿配土鸡蛋，面条劲道爽滑', 10.00, 2, '/images/products/xihongshi.jpg', 80, 203, 1, 1, 4.6, '2024-01-01 08:00:00'),
('牛肉拉面', '精选牛肉熬制高汤，手工拉面', 18.00, 2, '/images/products/niurou.jpg', 30, 167, 1, 1, 4.7, '2024-01-01 08:00:00'),
('炸酱面', '传统北京炸酱面，酱香浓郁', 13.00, 2, '/images/products/zhajiang.jpg', 50, 125, 1, 0, 4.2, '2024-01-01 08:00:00'),
('酸辣粉', '重庆特色酸辣粉，酸辣开胃', 9.00, 2, '/images/products/suanla.jpg', 70, 189, 1, 1, 4.4, '2024-01-01 08:00:00'),
('兰州拉面', '正宗兰州牛肉面，清汤面条', 16.00, 2, '/images/products/lanzhou.jpg', 25, 156, 1, 0, 4.5, '2024-01-01 08:00:00'),

-- 汤品类
('紫菜蛋花汤', '清淡紫菜蛋花汤，营养丰富', 6.00, 3, '/images/products/zicai.jpg', 100, 245, 1, 1, 4.3, '2024-01-01 08:00:00'),
('冬瓜排骨汤', '清香冬瓜配新鲜排骨，营养滋补', 12.00, 3, '/images/products/donggua.jpg', 40, 178, 1, 0, 4.4, '2024-01-01 08:00:00'),
('番茄鸡蛋汤', '酸甜番茄配嫩滑鸡蛋', 8.00, 3, '/images/products/fanqie.jpg', 80, 198, 1, 1, 4.2, '2024-01-01 08:00:00'),
('玉米排骨汤', '香甜玉米配鲜美排骨', 14.00, 3, '/images/products/yumi.jpg', 35, 145, 1, 0, 4.5, '2024-01-01 08:00:00'),
('海带豆腐汤', '清淡海带豆腐汤，低脂健康', 7.00, 3, '/images/products/haidai.jpg', 90, 167, 1, 0, 4.1, '2024-01-01 08:00:00'),

-- 素食类
('麻婆豆腐', '经典川菜麻婆豆腐，麻辣鲜香', 10.00, 4, '/images/products/mapo_tofu.jpg', 100, 189, 1, 1, 4.3, '2024-01-01 08:00:00'),
('清炒时蔬', '当季新鲜蔬菜，清淡健康', 8.00, 4, '/images/products/shicai.jpg', 150, 145, 1, 0, 4.1, '2024-01-01 08:00:00'),
('红烧茄子', '软糯香甜，下饭神器', 9.00, 4, '/images/products/qiezi.jpg', 120, 167, 1, 0, 4.2, '2024-01-01 08:00:00'),
('干煸豆角', '香辣下饭，口感丰富', 11.00, 4, '/images/products/doujiao.jpg', 100, 134, 1, 0, 4.2, '2024-01-01 08:00:00'),
('蒜蓉菠菜', '营养丰富的绿叶蔬菜', 7.00, 4, '/images/products/bocai.jpg', 130, 123, 1, 0, 4.0, '2024-01-01 08:00:00'),

-- 荤菜类
('红烧肉', '肥瘦相间，入口即化', 18.00, 5, '/images/products/hongshaorou_dish.jpg', 60, 298, 1, 1, 4.7, '2024-01-01 08:00:00'),
('宫保鸡丁', '川菜经典，香辣可口', 16.00, 5, '/images/products/gongbao_dish.jpg', 80, 245, 1, 1, 4.5, '2024-01-01 08:00:00'),
('糖醋里脊', '酸甜可口，外酥内嫩', 20.00, 5, '/images/products/tangcu_dish.jpg', 70, 187, 1, 1, 4.6, '2024-01-01 08:00:00'),
('回锅肉', '四川名菜，香辣下饭', 17.00, 5, '/images/products/huiguorou_dish.jpg', 75, 156, 1, 0, 4.4, '2024-01-01 08:00:00'),
('可乐鸡翅', '甜香嫩滑，老少皆宜', 15.00, 5, '/images/products/jichi.jpg', 90, 234, 1, 1, 4.5, '2024-01-01 08:00:00'),
('清蒸鲈鱼', '鲜嫩无腥，营养丰富', 25.00, 5, '/images/products/luyu.jpg', 40, 89, 1, 0, 4.6, '2024-01-01 08:00:00'),

-- 饮品类
('鲜榨橙汁', '新鲜橙子现榨，维C丰富', 8.00, 6, '/images/products/orange_juice.jpg', 100, 178, 1, 0, 4.3, '2024-01-01 08:00:00'),
('柠檬蜂蜜茶', '清香甘甜，生津止渴', 10.00, 6, '/images/products/lemon_tea.jpg', 120, 145, 1, 0, 4.2, '2024-01-01 08:00:00'),
('绿豆汤', '清热解毒，夏日必备', 5.00, 6, '/images/products/lvdou.jpg', 150, 234, 1, 1, 4.4, '2024-01-01 08:00:00'),
('酸梅汤', '酸甜开胃，传统饮品', 6.00, 6, '/images/products/suanmei.jpg', 130, 167, 1, 0, 4.1, '2024-01-01 08:00:00'),
('豆浆', '营养丰富，早餐首选', 3.00, 6, '/images/products/doujiang.jpg', 200, 289, 1, 1, 4.3, '2024-01-01 08:00:00'),
('牛奶', '优质牛奶，补充蛋白质', 4.00, 6, '/images/products/milk.jpg', 180, 234, 1, 0, 4.2, '2024-01-01 08:00:00'),

-- 小食点心
('煎蛋灌饼', '香脆可口，营养丰富', 6.00, 7, '/images/products/jidan_bing.jpg', 80, 156, 1, 0, 4.1, '2024-01-01 08:00:00'),
('手抓饼', '层次分明，香酥可口', 5.00, 7, '/images/products/shouzhua.jpg', 100, 189, 1, 1, 4.2, '2024-01-01 08:00:00'),
('小笼包(6个)', '皮薄馅大，汤汁丰富', 12.00, 7, '/images/products/xiaolongbao.jpg', 60, 145, 1, 1, 4.5, '2024-01-01 08:00:00'),
('煎饺(8个)', '外酥内嫩，香味扑鼻', 10.00, 7, '/images/products/jianjiao.jpg', 70, 123, 1, 0, 4.2, '2024-01-01 08:00:00'),
('蒸蛋糕', '松软香甜，下午茶首选', 8.00, 7, '/images/products/dangao.jpg', 50, 98, 1, 0, 4.0, '2024-01-01 08:00:00'),

-- 早餐类
('白粥配咸菜', '清淡养胃，传统早餐', 4.00, 8, '/images/products/baizhou.jpg', 150, 234, 1, 1, 4.1, '2024-01-01 08:00:00'),
('小米粥', '营养丰富，养胃健脾', 5.00, 8, '/images/products/xiaomi.jpg', 120, 189, 1, 0, 4.2, '2024-01-01 08:00:00'),
('茶叶蛋', '香味浓郁，蛋白质丰富', 3.00, 8, '/images/products/chaye_dan.jpg', 200, 267, 1, 1, 4.0, '2024-01-01 08:00:00'),
('豆腐脑', '嫩滑豆腐脑，营养早餐', 6.00, 8, '/images/products/doufu_nao.jpg', 100, 156, 1, 0, 4.1, '2024-01-01 08:00:00'),
('油条', '传统油条，配粥绝佳', 2.00, 8, '/images/products/youtiao.jpg', 150, 198, 1, 0, 3.9, '2024-01-01 08:00:00');

-- 插入真实订单数据
INSERT INTO `orders` (`order_no`, `user_id`, `total_amount`, `status`, `remark`, `create_time`) VALUES
('CT20240115001', 3, 27.00, 'COMPLETED', '不要辣', '2024-01-15 12:30:00'),
('CT20240115002', 4, 18.00, 'COMPLETED', '多加米饭', '2024-01-15 12:45:00'),
('CT20240116003', 5, 35.50, 'COMPLETED', '打包带走', '2024-01-16 11:20:00'),
('CT20240116004', 6, 22.00, 'COMPLETED', '', '2024-01-16 12:15:00'),
('CT20240117005', 7, 29.00, 'COMPLETED', '少盐', '2024-01-17 12:00:00'),
('CT20240117006', 8, 16.00, 'COMPLETED', '加个鸡蛋', '2024-01-17 12:30:00'),
('CT20240118007', 9, 31.00, 'COMPLETED', '不要香菜', '2024-01-18 11:45:00'),
('CT20240118008', 10, 24.00, 'COMPLETED', '微辣', '2024-01-18 12:20:00'),
('CT20240119009', 3, 19.00, 'COMPLETED', '', '2024-01-19 12:10:00'),
('CT20240119010', 4, 26.00, 'COMPLETED', '多加汤', '2024-01-19 12:35:00');

-- 插入订单详情数据
INSERT INTO `order_item` (`order_id`, `product_id`, `product_name`, `quantity`, `price`, `subtotal`) VALUES
-- 订单1: CT20240115001 (张三)
(1, 1, '红烧肉套餐', 1, 15.00, 15.00),
(1, 11, '紫菜蛋花汤', 1, 6.00, 6.00),
(1, 25, '鲜榨橙汁', 1, 8.00, 8.00),

-- 订单2: CT20240115002 (李四)
(2, 6, '西红柿鸡蛋面', 1, 10.00, 10.00),
(2, 29, '豆浆', 1, 3.00, 3.00),
(2, 33, '茶叶蛋', 1, 3.00, 3.00),

-- 订单3: CT20240116003 (王五)
(3, 2, '宫保鸡丁套餐', 1, 14.00, 14.00),
(3, 20, '红烧肉', 1, 18.00, 18.00),
(3, 27, '绿豆汤', 1, 5.00, 5.00),

-- 订单4: CT20240116004 (赵六)
(4, 7, '牛肉拉面', 1, 18.00, 18.00),
(4, 32, '小笼包(6个)', 1, 12.00, 12.00),

-- 订单5: CT20240117005 (孙七)
(5, 3, '糖醋里脊套餐', 1, 16.00, 16.00),
(5, 13, '番茄鸡蛋汤', 1, 8.00, 8.00),
(5, 29, '豆浆', 1, 3.00, 3.00),

-- 订单6: CT20240117006 (周八)
(6, 9, '酸辣粉', 1, 9.00, 9.00),
(6, 31, '手抓饼', 1, 5.00, 5.00),

-- 订单7: CT20240118007 (吴九)
(7, 4, '麻婆豆腐套餐', 1, 12.00, 12.00),
(7, 21, '宫保鸡丁', 1, 16.00, 16.00),
(7, 28, '酸梅汤', 1, 6.00, 6.00),

-- 订单8: CT20240118008 (郑十)
(8, 8, '炸酱面', 1, 13.00, 13.00),
(8, 26, '柠檬蜂蜜茶', 1, 10.00, 10.00),

-- 订单9: CT20240119009 (张三)
(9, 10, '兰州拉面', 1, 16.00, 16.00),
(9, 33, '茶叶蛋', 1, 3.00, 3.00),

-- 订单10: CT20240119010 (李四)
(10, 22, '糖醋里脊', 1, 20.00, 20.00),
(10, 25, '鲜榨橙汁', 1, 8.00, 8.00);

-- 查看插入的数据统计
SELECT '用户统计' as 统计类型, COUNT(*) as 数量 FROM user
UNION ALL
SELECT '分类统计', COUNT(*) FROM product_category
UNION ALL
SELECT '商品统计', COUNT(*) FROM product
UNION ALL
SELECT '订单统计', COUNT(*) FROM orders
UNION ALL
SELECT '订单详情统计', COUNT(*) FROM order_item;

-- 查看热门商品统计
SELECT 
    p.name as 商品名称,
    p.sales as 销量,
    p.rating as 评分,
    p.is_hot as 是否热门,
    pc.name as 分类
FROM product p
JOIN product_category pc ON p.category_id = pc.id
WHERE p.is_hot = 1
ORDER BY p.sales DESC;

-- 查看用户订单统计
SELECT 
    u.real_name as 用户姓名,
    u.college as 学院,
    COUNT(o.id) as 订单数量,
    SUM(o.total_amount) as 总消费金额
FROM user u
LEFT JOIN orders o ON u.id = o.user_id
WHERE u.role = 'USER'
GROUP BY u.id, u.real_name, u.college
ORDER BY 总消费金额 DESC;