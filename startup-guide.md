# 餐厅系统启动指南 - CORS和分类筛选修复版

## 🔧 修复内容

### 1. 401 Unauthorized 问题修复
- ✅ 推荐服务API配置为公开接口，无需认证
- ✅ 更新了网关认证白名单，包含 `/api/recommend/` 路径
- ✅ 优化了前端推荐API调用的错误处理

### 2. CORS Policy Blocked 问题修复
- ✅ 创建了全局CORS过滤器 (`CorsGlobalFilter`)
- ✅ 更新了Spring Cloud Gateway的CORS配置
- ✅ 添加了重复响应头去重配置
- ✅ 优化了前端axios配置

### 3. 分类筛选功能修复
- ✅ 修复了分类ID类型转换问题
- ✅ 添加了详细的筛选日志
- ✅ 优化了API调用逻辑
- ✅ 确保所有分类都能正确筛选

## 🚀 启动步骤

### 1. 启动后端服务

```bash
# 1. 启动网关服务 (必须)
cd canteen-gateway
mvn spring-boot:run
# 或者
java -jar target/canteen-gateway-1.0-SNAPSHOT.jar

# 2. 启动商品服务 (必须)
cd canteen-product-service
mvn spring-boot:run

# 3. 启动用户服务 (可选)
cd canteen-user-service
mvn spring-boot:run

# 4. 启动订单服务 (可选)
cd canteen-order-service
mvn spring-boot:run

# 5. 启动推荐服务 (可选)
cd canteen-recommend-service
mvn spring-boot:run
```

### 2. 启动前端服务

```bash
cd canteen-web-app
npm install
npm run dev
```

## 🧪 测试验证

### 方法1: 使用测试脚本
```bash
# 安装axios (如果没有)
npm install axios

# 运行针对具体问题的测试脚本
node test-specific-issues.js

# 或运行原始测试脚本
node test-cors-fix.js
```

### 方法2: 手动测试
1. 打开浏览器访问 `http://localhost:3001`
2. 进入商品页面
3. 测试分类筛选功能：
   - 点击"全部" - 应显示所有商品
   - 点击"主食套餐" - 应只显示主食套餐类商品
   - 点击"面食类" - 应只显示面食类商品
   - 依此类推...

## 🔍 问题排查

### 如果仍然出现 "401 Unauthorized" 错误：
1. 检查网关服务是否正常启动 (端口8080)
2. 确认推荐服务是否启动 (端口8084) - 可选服务
3. 检查网关日志，确认推荐API已配置为公开接口
4. 如果推荐服务未启动，前端会自动降级到模拟数据

### 如果仍然出现 "CORS policy blocked" 错误：
1. 重新编译并启动网关服务以应用新的CORS配置
2. 检查浏览器控制台的网络请求响应头
3. 确认前端代理配置正确 (Vite开发服务器)
4. 检查网关日志中的CORS相关信息

### 如果分类筛选不工作：
1. 打开浏览器开发者工具
2. 查看控制台日志，应该看到筛选参数和结果
3. 检查网络请求是否包含正确的categoryId参数
4. 确认商品服务正常运行 (端口8082)

## 📋 服务端口说明

| 服务 | 端口 | 状态 |
|------|------|------|
| 前端开发服务器 | 3001 | 必须启动 |
| API网关 | 8080 | 必须启动 |
| 用户服务 | 8081 | 可选 |
| 商品服务 | 8082 | 必须启动 |
| 订单服务 | 8083 | 可选 |
| 推荐服务 | 8084 | 可选 |

## 🎯 功能验证清单

- [ ] 网关服务启动成功
- [ ] 商品服务启动成功  
- [ ] 前端服务启动成功
- [ ] 可以访问商品页面
- [ ] "全部"分类显示所有商品
- [ ] "主食套餐"分类筛选正常
- [ ] "面食类"分类筛选正常
- [ ] "汤品类"分类筛选正常
- [ ] "素食类"分类筛选正常
- [ ] "荤菜类"分类筛选正常
- [ ] "饮品类"分类筛选正常
- [ ] "小食点心"分类筛选正常
- [ ] "早餐类"分类筛选正常
- [ ] 没有CORS错误提示
- [ ] 网络请求正常响应

## 🔧 技术细节

### CORS配置改进：
- 使用`allowedOriginPatterns`替代`allowedOrigins`
- 添加了`allowCredentials: true`
- 设置了合适的`maxAge`
- 配置了完整的HTTP方法支持

### 分类筛选改进：
- 修复了字符串/数字类型转换
- 添加了详细的调试日志
- 优化了筛选逻辑
- 确保API参数正确传递

如果遇到问题，请检查控制台日志获取详细错误信息。