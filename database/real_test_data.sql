-- 高校食堂订餐系统真实测试数据
-- 注意：此脚本会清空现有数据并插入新的测试数据

USE canteen_system;

-- 清空现有数据（按外键依赖顺序）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `order_item`;
TRUNCATE TABLE `orders`;
TRUNCATE TABLE `product`;
TRUNCATE TABLE `product_category`;
TRUNCATE TABLE `user`;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 插入真实用户数据
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `real_name`, `college`, `address`, `role`, `status`) VALUES
-- 管理员
('admin', 'admin123', 'admin@university.edu.cn', '13800138001', '王管理', '后勤管理处', '行政楼201室', 'ADMIN', 1),
-- 商户
('canteen_manager', 'manager123', 'manager@university.edu.cn', '13800138002', '李经理', '后勤服务中心', '第一食堂管理办公室', 'MERCHANT', 1),
('canteen_chef', 'chef123', 'chef@university.edu.cn', '13800138003', '张师傅', '后勤服务中心', '第一食堂厨房', 'MERCHANT', 1),
-- 学生用户
('student001', 'password123', 'zhangsan@stu.university.edu.cn', '13900139001', '张三', '计算机科学与技术学院', '学生公寓1号楼202室', 'USER', 1),
('student002', 'password123', 'lisi@stu.university.edu.cn', '13900139002', '李四', '电子信息工程学院', '学生公寓2号楼305室', 'USER', 1),
('student003', 'password123', 'wangwu@stu.university.edu.cn', '13900139003', '王五', '机械工程学院', '学生公寓3号楼108室', 'USER', 1),
('student004', 'password123', 'zhaoliu@stu.university.edu.cn', '13900139004', '赵六', '经济管理学院', '学生公寓4号楼401室', 'USER', 1),
('student005', 'password123', 'sunqi@stu.university.edu.cn', '13900139005', '孙七', '外国语学院', '学生公寓5号楼203室', 'USER', 1),
-- 教师用户
('teacher001', 'teacher123', 'prof.chen@university.edu.cn', '13700137001', '陈教授', '计算机科学与技术学院', '教师公寓A栋301室', 'USER', 1),
('teacher002', 'teacher123', 'dr.liu@university.edu.cn', '13700137002', '刘博士', '电子信息工程学院', '教师公寓B栋205室', 'USER', 1);

-- 2. 插入餐品分类
INSERT INTO `product_category` (`name`, `description`, `sort_order`) VALUES
('主食类', '米饭、面条、包子等主食', 1),
('汤羹类', '各种汤品和羹类', 2),
('素食类', '蔬菜类菜品', 3),
('荤菜类', '肉类菜品', 4),
('饮品类', '各种饮料和果汁', 5),
('小食类', '小食、点心、零食', 6),
('套餐类', '搭配好的套餐', 7),
('早餐类', '早餐专用食品', 8);

-- 3. 插入真实餐品数据
INSERT INTO `product` (`name`, `description`, `price`, `category_id`, `stock`, `sales`, `image_url`, `status`) VALUES
-- 主食类
('白米饭', '优质东北大米蒸制，粒粒饱满', 2.00, 1, 200, 1500, '/images/products/rice.jpg', 1),
('红烧肉盖饭', '精选五花肉红烧，配白米饭', 15.00, 1, 50, 320, '/images/products/hongshaorou_rice.jpg', 1),
('宫保鸡丁盖饭', '经典川菜宫保鸡丁配米饭', 14.00, 1, 45, 280, '/images/products/gongbao_rice.jpg', 1),
('西红柿鸡蛋面', '新鲜西红柿炒鸡蛋配手工面条', 12.00, 1, 60, 450, '/images/products/tomato_noodle.jpg', 1),
('牛肉拉面', '清汤牛肉配手拉面', 18.00, 1, 30, 180, '/images/products/beef_noodle.jpg', 1),
('小笼包(8个)', '上海风味小笼包，皮薄馅大', 12.00, 1, 80, 520, '/images/products/xiaolongbao.jpg', 1),
('韭菜盒子(2个)', '韭菜鸡蛋馅，外酥内嫩', 8.00, 1, 60, 240, '/images/products/jiucai_box.jpg', 1),

-- 汤羹类
('紫菜蛋花汤', '清淡紫菜配鸡蛋花', 6.00, 2, 100, 680, '/images/products/zicai_soup.jpg', 1),
('冬瓜排骨汤', '清香冬瓜配新鲜排骨', 15.00, 2, 40, 220, '/images/products/donggua_soup.jpg', 1),
('西红柿鸡蛋汤', '酸甜西红柿配嫩滑鸡蛋', 8.00, 2, 80, 380, '/images/products/tomato_soup.jpg', 1),
('玉米排骨汤', '甜玉米配排骨慢炖', 16.00, 2, 35, 150, '/images/products/corn_soup.jpg', 1),
('银耳莲子汤', '滋补银耳配莲子', 10.00, 2, 50, 120, '/images/products/yiner_soup.jpg', 1),

-- 素食类
('麻婆豆腐', '经典川菜，嫩滑豆腐配麻辣汁', 10.00, 3, 70, 420, '/images/products/mapo_tofu.jpg', 1),
('地三鲜', '茄子、土豆、青椒经典搭配', 12.00, 3, 50, 280, '/images/products/disanxian.jpg', 1),
('清炒小白菜', '新鲜小白菜清炒', 8.00, 3, 80, 350, '/images/products/baicai.jpg', 1),
('蒜蓉菠菜', '菠菜配蒜蓉爆炒', 9.00, 3, 60, 290, '/images/products/bocai.jpg', 1),
('干煸豆角', '四川风味干煸豆角', 11.00, 3, 45, 180, '/images/products/doujiao.jpg', 1),
('醋溜白菜', '酸甜白菜丝', 7.00, 3, 70, 320, '/images/products/culiu_baicai.jpg', 1),

-- 荤菜类
('糖醋里脊', '酸甜可口的糖醋里脊肉', 18.00, 4, 40, 260, '/images/products/tangcu_liji.jpg', 1),
('红烧狮子头', '传统红烧狮子头', 20.00, 4, 25, 150, '/images/products/shizitou.jpg', 1),
('可乐鸡翅', '可乐炖制鸡翅，香甜可口', 16.00, 4, 50, 380, '/images/products/cola_chicken.jpg', 1),
('回锅肉', '四川经典回锅肉', 19.00, 4, 35, 200, '/images/products/huiguorou.jpg', 1),
('红烧鱼块', '新鲜鱼块红烧制作', 22.00, 4, 30, 120, '/images/products/hongshao_fish.jpg', 1),
('口水鸡', '四川口水鸡，麻辣鲜香', 17.00, 4, 40, 180, '/images/products/koushuiji.jpg', 1),

-- 饮品类
('鲜榨橙汁', '新鲜橙子现榨，维C丰富', 8.00, 5, 100, 450, '/images/products/orange_juice.jpg', 1),
('柠檬蜂蜜茶', '柠檬片配蜂蜜，清香怡人', 10.00, 5, 80, 320, '/images/products/lemon_tea.jpg', 1),
('绿豆汤', '清热解毒绿豆汤', 6.00, 5, 120, 280, '/images/products/mung_bean.jpg', 1),
('酸梅汤', '传统酸梅汤，生津止渴', 7.00, 5, 90, 380, '/images/products/suanmei.jpg', 1),
('豆浆', '新鲜黄豆磨制豆浆', 3.00, 5, 150, 680, '/images/products/doujiang.jpg', 1),
('牛奶', '纯牛奶，营养丰富', 5.00, 5, 100, 420, '/images/products/milk.jpg', 1),

-- 小食类
('煎饺(6个)', '猪肉韭菜煎饺，外焦内嫩', 10.00, 6, 60, 340, '/images/products/jianjiao.jpg', 1),
('锅贴(6个)', '传统锅贴，底部金黄', 12.00, 6, 50, 280, '/images/products/guotie.jpg', 1),
('春卷(4个)', '蔬菜春卷，清爽不腻', 8.00, 6, 70, 220, '/images/products/chunjuan.jpg', 1),
('烧饼', '传统烧饼，香酥可口', 4.00, 6, 80, 380, '/images/products/shaobing.jpg', 1),
('茶叶蛋', '五香茶叶蛋', 3.00, 6, 100, 520, '/images/products/chaye_egg.jpg', 1),

-- 套餐类
('经济套餐A', '一荤一素+米饭+汤', 18.00, 7, 50, 420, '/images/products/set_a.jpg', 1),
('经济套餐B', '两荤一素+米饭+汤', 25.00, 7, 40, 280, '/images/products/set_b.jpg', 1),
('营养套餐', '荤素搭配+粗粮+汤+水果', 28.00, 7, 30, 180, '/images/products/nutrition_set.jpg', 1),
('学生特惠套餐', '一荤一素+米饭', 15.00, 7, 60, 520, '/images/products/student_set.jpg', 1),

-- 早餐类
('豆浆油条', '经典早餐搭配', 6.00, 8, 80, 680, '/images/products/doujiang_youtiao.jpg', 1),
('小米粥', '营养小米粥', 4.00, 8, 100, 450, '/images/products/xiaomi_porridge.jpg', 1),
('包子(2个)', '猪肉大葱包子', 6.00, 8, 90, 520, '/images/products/baozi.jpg', 1),
('煎蛋', '煎鸡蛋，嫩滑可口', 3.00, 8, 120, 380, '/images/products/jiandan.jpg', 1),
('咸菜', '开胃咸菜', 2.00, 8, 150, 280, '/images/products/xiancai.jpg', 1);

-- 4. 插入订单数据（模拟真实订单）
INSERT INTO `orders` (`order_no`, `user_id`, `total_amount`, `status`, `remark`, `create_time`) VALUES
('CT20241201001001', 4, 27.00, 'COMPLETED', '不要太辣', '2024-12-01 12:30:00'),
('CT20241201001002', 5, 35.00, 'COMPLETED', '多加米饭', '2024-12-01 12:45:00'),
('CT20241201001003', 6, 18.00, 'PREPARING', '', '2024-12-01 13:15:00'),
('CT20241201001004', 7, 42.00, 'PAID', '打包带走', '2024-12-01 13:30:00'),
('CT20241201001005', 8, 25.00, 'COMPLETED', '', '2024-12-01 11:45:00'),
('CT20241202001006', 4, 22.00, 'COMPLETED', '少盐', '2024-12-02 12:00:00'),
('CT20241202001007', 9, 38.00, 'COMPLETED', '', '2024-12-02 12:20:00'),
('CT20241202001008', 10, 28.00, 'PREPARING', '加个鸡蛋', '2024-12-02 13:00:00'),
('CT20241203001009', 5, 15.00, 'PENDING', '', '2024-12-03 12:10:00'),
('CT20241203001010', 6, 33.00, 'PAID', '不要香菜', '2024-12-03 12:25:00');

-- 5. 插入订单详情数据
INSERT INTO `order_item` (`order_id`, `product_id`, `product_name`, `quantity`, `price`, `subtotal`) VALUES
-- 订单1: 张三的订单
(1, 2, '红烧肉盖饭', 1, 15.00, 15.00),
(1, 8, '紫菜蛋花汤', 1, 6.00, 6.00),
(1, 25, '鲜榨橙汁', 1, 8.00, 8.00),

-- 订单2: 李四的订单
(2, 7, '营养套餐', 1, 28.00, 28.00),
(2, 29, '豆浆', 1, 3.00, 3.00),
(2, 33, '茶叶蛋', 1, 3.00, 3.00),

-- 订单3: 王五的订单
(3, 36, '学生特惠套餐', 1, 15.00, 15.00),
(3, 27, '绿豆汤', 1, 6.00, 6.00),

-- 订单4: 赵六的订单
(4, 3, '宫保鸡丁盖饭', 1, 14.00, 14.00),
(4, 16, '麻婆豆腐', 1, 10.00, 10.00),
(4, 1, '白米饭', 1, 2.00, 2.00),
(4, 9, '冬瓜排骨汤', 1, 15.00, 15.00),

-- 订单5: 孙七的订单
(5, 34, '经济套餐A', 1, 18.00, 18.00),
(5, 26, '柠檬蜂蜜茶', 1, 10.00, 10.00),

-- 订单6: 张三的第二个订单
(6, 4, '西红柿鸡蛋面', 1, 12.00, 12.00),
(6, 18, '清炒小白菜', 1, 8.00, 8.00),
(6, 28, '酸梅汤', 1, 7.00, 7.00),

-- 订单7: 陈教授的订单
(7, 35, '经济套餐B', 1, 25.00, 25.00),
(7, 11, '玉米排骨汤', 1, 16.00, 16.00),

-- 订单8: 刘博士的订单
(8, 21, '糖醋里脊', 1, 18.00, 18.00),
(8, 1, '白米饭', 1, 2.00, 2.00),
(8, 8, '紫菜蛋花汤', 1, 6.00, 6.00),
(8, 29, '豆浆', 1, 3.00, 3.00),

-- 订单9: 李四的第二个订单
(9, 37, '学生特惠套餐', 1, 15.00, 15.00),

-- 订单10: 王五的第二个订单
(10, 5, '牛肉拉面', 1, 18.00, 18.00),
(10, 31, '煎饺(6个)', 1, 10.00, 10.00),
(10, 30, '牛奶', 1, 5.00, 5.00);

-- 查看插入的数据统计
SELECT '用户数据' as '数据类型', COUNT(*) as '记录数' FROM `user`
UNION ALL
SELECT '餐品分类', COUNT(*) FROM `product_category`
UNION ALL
SELECT '餐品数据', COUNT(*) FROM `product`
UNION ALL
SELECT '订单数据', COUNT(*) FROM `orders`
UNION ALL
SELECT '订单详情', COUNT(*) FROM `order_item`;

-- 显示一些示例数据
SELECT '=== 用户示例数据 ===' as info;
SELECT id, username, real_name, college, role FROM `user` LIMIT 5;

SELECT '=== 餐品示例数据 ===' as info;
SELECT p.id, p.name, p.price, pc.name as category_name, p.stock, p.sales 
FROM `product` p 
JOIN `product_category` pc ON p.category_id = pc.id 
LIMIT 10;

SELECT '=== 订单示例数据 ===' as info;
SELECT o.order_no, u.real_name, o.total_amount, o.status, o.create_time
FROM `orders` o
JOIN `user` u ON o.user_id = u.id
LIMIT 5;