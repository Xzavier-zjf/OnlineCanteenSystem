# 高校食堂订餐系统

基于 Spring Boot 微服务、Vue 3 和 Java Swing 的高校食堂订餐系统。项目包含用户端、商户端、后台管理、商品管理、订单管理、推荐管理、文件上传和网关代理等功能。

## 技术栈

- 后端：Java 17、Spring Boot 2.7.18、Spring Cloud 2021.0.8
- 数据库：MySQL 8.x
- ORM：MyBatis Plus 3.5.2
- 认证：JWT
- 前端：Vue 3、Vite、Element Plus、Pinia、ECharts
- 商户桌面端：Java Swing

## 项目结构

```text
online-canteen-system/
├── pom.xml                         # Maven 父工程
├── canteen-common/                 # 公共模块
├── canteen-gateway/                # API 网关，默认端口 8080
├── canteen-user-service/           # 用户、商户、管理员与上传服务，默认端口 8081
├── canteen-product-service/        # 商品服务，默认端口 8082
├── canteen-order-service/          # 订单服务，默认端口 8083
├── canteen-recommend-service/      # 推荐服务，默认端口 8084
├── canteen-web-app/                # Vue 前端应用
├── canteen-merchant-client/        # Java Swing 商户客户端
├── database/                       # 数据库初始化与增量脚本
└── start-services.bat              # Windows 本地启动脚本
```

## 根目录 package 文件说明

根目录曾经有 `package.json` 和 `package-lock.json`，它们属于旧的 mock server 配置，入口指向已经不存在的 `mock-server.js`。当前真实前端依赖在 `canteen-web-app/package.json` 中维护，因此根目录不再保留 Node 包配置。

前端安装与构建请进入 `canteen-web-app/`：

```bash
cd canteen-web-app
npm install
npm run dev
npm run build
```

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.x
- Node.js 18+（推荐 20+）

## 数据库初始化

按实际环境创建数据库后执行脚本：

```bash
mysql -u root -p < database/init.sql
```

根据需要再执行增量脚本，例如：

```bash
mysql -u root -p canteen_system < database/settings_tables.sql
mysql -u root -p canteen_system < database/add_system_settings_config.sql
mysql -u root -p canteen_system < database/add_merchant_id_to_product.sql
```

服务的数据库连接配置位于各模块的 `src/main/resources/application.yml`。

## 后端启动

先编译后端：

```bash
mvn clean compile
```

分别启动服务：

```bash
cd canteen-user-service
mvn spring-boot:run

cd ../canteen-product-service
mvn spring-boot:run

cd ../canteen-order-service
mvn spring-boot:run

cd ../canteen-recommend-service
mvn spring-boot:run

cd ../canteen-gateway
mvn spring-boot:run
```

Windows 可直接运行：

```bat
start-services.bat
```

## 前端启动

```bash
cd canteen-web-app
npm install
npm run dev
```

默认访问地址：

- 前端开发服务：http://localhost:3001
- API 网关：http://localhost:8080

前端接口统一走相对路径 `/api/**`，开发环境由 Vite 代理到网关，生产环境由部署层或网关承接转发。不要在业务页面中直接请求 `http://localhost:8081`、`http://localhost:8082` 等单个服务地址，否则生产部署后会访问用户本机的 `localhost`。

图片与上传文件统一使用 `/uploads/**` 路径。开发环境通过 Vite 代理到用户服务，生产环境通过网关 `UploadsProxyController` 读取用户服务上传目录。

## 网关配置

网关默认监听 `8080`，并将请求转发到各后端服务：

```yaml
canteen:
  user-service:
    url: http://localhost:8081
  product-service:
    url: http://localhost:8082
  order-service:
    url: http://localhost:8083
  recommend-service:
    url: http://localhost:8084
```

本地默认值已内置在代码中，独立部署时可在 `canteen-gateway/src/main/resources/application-standalone.yml` 或外部配置中覆盖。网关会保留下游服务的错误码和响应体，避免把登录失败、参数错误、资源不存在等业务错误统一吞成 `500`。

## 商户客户端

```bash
cd canteen-merchant-client
mvn package
java -jar target/canteen-merchant-client-1.0.0.jar
```

## 功能状态

- 用户注册、登录、资料设置、偏好设置、通知设置
- 商品浏览、分类、图片上传、图片路径统一处理
- 下单、支付、取消订单
- 商户接单、备餐、完成订单、财务统计
- 管理员用户管理、商户审核、商品审核、系统设置
- 推荐商品、热门推荐
- 网关统一 API 转发与 `/uploads/**` 生产代理

以下功能仍在开发或未完整接入：

- 推荐策略配置后端接口暂未接入，前端会明确提示“未接入，未保存”
- 订单打印、退款、再来一单仍显示为功能开发中
- 前端生产构建仍存在较大的第三方依赖 chunk，后续可继续拆包优化
- `npm install` 后提示的依赖审计风险需单独评估和处理

## 默认账号

初始化脚本中通常包含以下测试账号，具体以数据库脚本为准：

- 管理员：`admin / admin123`
- 商户：`merchant / admin123`
- 用户：`user1 / admin123`

## 仓库维护约定

以下内容不提交到仓库：

- Maven 构建产物：`target/`
- 前端依赖与构建产物：`node_modules/`、`dist/`、`.vite/`
- 运行时上传文件：`uploads/`
- 本地环境变量：`.env*`
- IDE、系统和工具日志：`.idea/`、`.vscode/`、`.codebuddy/`
- 编辑器备份和临时文件：`*.backup`、`*.bak`、`*.tmp`
- Maven Shade 生成文件：`dependency-reduced-pom.xml`

如需提交示例上传文件，请放到专门的示例目录，并避免混入运行时上传目录。

## Git 远端

当前推荐远端命名：

- `origin`：GitHub，`https://github.com/Xzavier-zjf/OnlineCanteenSystem.git`
- `gitee`：Gitee，`git@gitee.com:fengqiqingpin/online-canteen-system.git`

日常提交到 GitHub：

```bash
git push origin main
```

如需同步到 Gitee：

```bash
git push gitee main
```
