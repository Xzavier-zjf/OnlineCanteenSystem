# 高校食堂订餐系统 - 项目启动指南

## 项目概述
本项目是一个基于Spring Boot微服务架构的高校食堂订餐系统，包含用户管理、餐品管理、订单管理等核心功能。

## 技术栈
- **后端**: Java 17, Spring Boot 2.7.18, Spring Cloud 2021.0.8
- **数据库**: MySQL 8.0.33
- **ORM**: MyBatis Plus 3.5.2
- **认证**: JWT 0.11.5
- **前端**: Vue 3 + Element Plus + Vite
- **商户端**: Java Swing

## 项目结构
```
OnlineCanteenSystem/
├── pom.xml                    # 父项目POM
├── canteen-common/            # 公共模块
├── canteen-user-service/      # 用户服务 (端口:8081)
├── canteen-product-service/   # 餐品服务 (端口:8082)
├── canteen-order-service/     # 订单服务 (端口:8083)
├── canteen-recommend-service/ # 推荐服务 (端口:8084)
├── canteen-gateway/          # API网关 (端口:8080)
├── canteen-web-app/          # 前端Vue应用
├── canteen-merchant-client/  # 商户端Swing应用
└── database/                 # 数据库脚本
    ├── init.sql              # 建表脚本
    └── test_users.sql        # 测试用户数据
```

## 环境要求
1. **JDK 17** 或更高版本
2. **Maven 3.6+**
3. **MySQL 8.0**
4. **Node.js 16+** (用于前端开发)
5. **IDE**: IntelliJ IDEA 推荐

## 快速启动

### 1. 数据库准备
```sql
-- 执行数据库初始化脚本
mysql -u root -p < database/init.sql
```

### 2. 修改数据库配置
在各服务的 `application.yml` 中修改数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/canteen_system?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root      # 修改为你的用户名
    password: 123456    # 修改为你的密码
```

### 3. 编译项目
```bash
mvn clean install
```

### 4. 启动服务
按照以下顺序启动服务：

#### 启动用户服务
```bash
cd canteen-user-service
mvn spring-boot:run
```
或在IDE中运行 `UserServiceApplication.java`

#### 启动餐品服务
```bash
cd canteen-product-service
mvn spring-boot:run
```
或在IDE中运行 `ProductServiceApplication.java`

#### 启动订单服务
```bash
cd canteen-order-service
mvn spring-boot:run
```
或在IDE中运行 `OrderServiceApplication.java`

#### 启动推荐服务
```bash
cd canteen-recommend-service
mvn spring-boot:run
```
或在IDE中运行 `RecommendServiceApplication.java`

#### 启动API网关
```bash
cd canteen-gateway
mvn spring-boot:run
```
或在IDE中运行 `GatewayApplication.java`

### 5. 启动前端应用
```bash
cd canteen-web-app
npm install
npm run dev
```

### 6. 启动商户端应用
```bash
cd canteen-merchant-client
mvn package
java -jar target/canteen-merchant-client-1.0.0.jar
```

## API 测试示例

### 用户注册
```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "email": "test@example.com",
    "realName": "测试用户"
  }'
```

### 用户登录
```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

### 获取餐品列表
```bash
curl "http://localhost:8082/api/products?current=1&size=10"
```

### 获取餐品分类
```bash
curl http://localhost:8082/api/products/categories
```

### 获取热门推荐
```bash
curl http://localhost:8082/api/products/hot
```

## 默认测试数据
系统初始化时会创建以下测试账号：
- **管理员**: username=`admin`, password=`admin123`
- **商户**: username=`merchant`, password=`admin123`  
- **用户**: username=`user1`, password=`admin123`

## 开发计划
- [x] 项目基础架构搭建
- [x] 公共模块开发
- [x] 用户服务开发
- [x] 餐品服务开发
- [x] 订单服务开发
- [x] 推荐服务开发
- [x] API网关开发
- [x] Vue前端开发
- [x] Java Swing商户端开发
- [x] 系统集成测试

## 注意事项
1. 确保MySQL服务已启动
2. 数据库连接配置正确
3. 确保端口8080-8084没有被占用
4. 推荐使用Postman进行API测试
5. 前端应用默认运行在 http://localhost:5173

## 问题排查
如果遇到启动问题：
1. 检查Java版本是否为17+
2. 检查Maven配置是否正确
3. 检查数据库连接是否正常
4. 查看控制台错误日志

项目当前已实现完整的用户注册、登录、餐品浏览、订单管理等功能，可以作为生产环境使用的基础。