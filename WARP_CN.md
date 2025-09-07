# WARP.md

该文件为WARP (warp.dev) 在此仓库中工作时提供指导。

## 项目概述

这是一个基于微服务架构的高校食堂在线订餐系统，使用Spring Boot和Vue.js构建。系统支持学生订餐、商户管理和系统管理功能。

## 技术栈

- **后端**: Java 17, Spring Boot 2.7.18, Spring Cloud 2021.0.8, MyBatis Plus
- **数据库**: MySQL 8.0.33
- **认证**: JWT 0.11.5
- **前端**: Vue 3, Element Plus, Vite
- **商户端**: Java Swing 桌面应用
- **构建工具**: Maven, npm

## 架构概览

### 微服务结构
系统采用微服务架构，包含以下组件：

- **canteen-common**: 共享工具类、DTOs、异常处理、JWT工具和公共控制器
- **canteen-user-service** (端口 8081): 用户管理、身份验证、管理员/商户/客户角色
- **canteen-product-service** (端口 8082): 餐品管理、分类管理、库存管理
- **canteen-order-service** (端口 8083): 订单处理、支付处理
- **canteen-recommend-service** (端口 8084): 推荐算法和热门商品
- **canteen-gateway** (端口 8080): API网关，负责路由和负载均衡
- **canteen-web-app**: Vue.js前端应用（客户端）
- **canteen-merchant-client**: Java Swing桌面应用（商户端）

### 核心架构模式
- **领域驱动设计**: 每个服务有自己的领域，包含控制器、服务、DTOs和映射器
- **JWT身份验证**: 通过公共模块实现共享身份验证
- **REST API**: RESTful端点，使用标准化响应格式（Result包装器）
- **服务发现**: 使用Spring Cloud服务发现（配置为Nacos）
- **跨域支持**: 每个服务中配置CORS

### 数据库结构
- 所有微服务共享单一MySQL数据库 `canteen_system`
- 使用MyBatis Plus作为ORM，包含映射器接口
- 通过 `database/` 文件夹中的SQL脚本进行测试数据初始化

## 常用开发命令

### 后端开发

#### 构建和测试
```bash
# 清理并构建整个项目
mvn clean install

# 构建特定服务
cd canteen-user-service && mvn clean package

# 运行整个项目的测试
mvn test

# 运行特定服务的测试
cd canteen-user-service && mvn test
```

#### 运行服务
服务必须按依赖顺序启动：

```bash
# 使用Maven启动单个服务
cd canteen-user-service && mvn spring-boot:run

# 启动所有后端服务（每个在单独的终端中运行）
cd canteen-user-service && mvn spring-boot:run
cd canteen-product-service && mvn spring-boot:run  
cd canteen-order-service && mvn spring-boot:run
cd canteen-recommend-service && mvn spring-boot:run
cd canteen-gateway && mvn spring-boot:run
```

#### JAR包方式执行
```bash
# 构建并运行JAR包
mvn package
java -jar target/canteen-user-service-1.0.0.jar
```

### 前端开发

#### Web应用 (Vue.js)
```bash
cd canteen-web-app

# 安装依赖
npm install

# 启动开发服务器（运行在 http://localhost:5173）
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

#### 商户桌面客户端
```bash
cd canteen-merchant-client

# 构建桌面应用
mvn package

# 运行桌面应用
java -jar target/canteen-merchant-client-1.0.0.jar
```

### 数据库设置
```bash
# 初始化数据库（在MySQL中执行）
mysql -u root -p < database/init.sql

# 加载测试数据
mysql -u root -p < database/test_users.sql
```

### API测试
```bash
# 测试用户注册
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "123456", "email": "test@example.com", "realName": "测试用户"}'

# 测试用户登录
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "123456"}'

# 获取餐品列表
curl "http://localhost:8082/api/products?current=1&size=10"

# 获取餐品分类
curl http://localhost:8082/api/products/categories
```

## 开发注意事项

### 服务端口
- 网关: 8080 (主要API入口)
- 用户服务: 8081
- 餐品服务: 8082  
- 订单服务: 8083
- 推荐服务: 8084
- 前端: 5173 (开发环境)

### 默认测试账号
- 管理员: `admin` / `admin123`
- 商户: `merchant` / `admin123`
- 用户: `user1` / `admin123`

### 配置要求
每个服务需要在 `application.yml` 中配置数据库连接：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/canteen_system?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

### 代码结构模式
- **控制器(Controllers)**: 处理HTTP请求和响应，使用Result包装器
- **服务(Services)**: 业务逻辑实现，使用@Service注解
- **数据传输对象(DTOs)**: API通信的数据传输对象
- **实体(Entities)**: MyBatis Plus数据库实体，带注解
- **映射器(Mappers)**: MyBatis Plus映射器接口，继承BaseMapper

### 常见问题
- 确保MySQL正在运行且可访问
- 检查所需端口(8080-8084, 5173)未被占用
- 验证使用的是Java 17
- 服务有相互依赖关系，需按正确顺序启动
- 前端API调用在生产环境中通过网关(端口8080)路由

### 身份验证流程
- JWT令牌在user-service中生成
- canteen-common中的JwtUtils处理令牌验证
- 每个服务中的JwtAuthenticationFilter验证请求
- 令牌包含用户角色信息(ADMIN, MERCHANT, USER)

### 单独测试服务的方法
```bash
# 只启动用户服务进行测试
cd canteen-user-service && mvn spring-boot:run

# 测试用户服务的健康状态
curl http://localhost:8081/api/users/health

# 运行单个服务的单元测试
cd canteen-user-service && mvn test -Dtest=UserServiceTest
```

### 数据库管理
```bash
# 查看数据库状态
mysql -u root -p -e "USE canteen_system; SHOW TABLES;"

# 重置测试数据
mysql -u root -p canteen_system < database/test_users.sql

# 备份数据库
mysqldump -u root -p canteen_system > backup.sql
```

### 开发环境热部署
- Spring Boot支持热重载，修改Java文件后自动重启
- Vue.js开发服务器支持热模块替换(HMR)
- 修改配置文件需要手动重启服务

### 日志查看
```bash
# 查看服务启动日志（在服务目录中）
tail -f logs/spring.log

# 查看特定级别的日志
grep "ERROR" logs/spring.log

# 实时监控API请求
tail -f logs/access.log
```
