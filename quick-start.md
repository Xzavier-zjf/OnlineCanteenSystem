# 快速启动指南 - 路由问题修复版

## 🚨 问题诊断

你遇到的502/404错误是因为**路由路径不匹配**问题：

- 前端请求：`/api/products/categories`
- 网关转发：`/api/products/categories` → `http://localhost:8082/api/products/categories`
- 商品服务实际路径：`/api/products/categories` ✅

## ✅ 已修复的路由问题

### 修复内容：
1. **路径映射修复**：网关现在正确处理`/api`前缀
2. **日志增强**：添加详细的路由转发日志
3. **错误处理优化**：更好的错误信息提示

## 🚀 启动步骤

### 1. 启动后端服务（按顺序）

```bash
# 1. 启动网关服务 (必须)
cd canteen-gateway
mvn clean compile
mvn spring-boot:run

# 2. 启动商品服务 (必须)
cd canteen-product-service
mvn spring-boot:run

# 3. 启动用户服务 (可选)
cd canteen-user-service
mvn spring-boot:run

# 4. 启动订单服务 (可选)
cd canteen-order-service
mvn spring-boot:run
```

### 2. 启动前端服务

```bash
cd canteen-web-app
npm run dev
```

### 3. 验证修复效果

```bash
# 测试路由修复
node test-routing-fix.js

# 检查服务状态
node test-routing-fix.js --check-services
```

## 🔍 故障排除

### 如果仍然出现502错误：

1. **检查服务启动顺序**：
   ```bash
   # 先启动网关
   cd canteen-gateway && mvn spring-boot:run
   
   # 再启动商品服务
   cd canteen-product-service && mvn spring-boot:run
   ```

2. **检查端口占用**：
   ```bash
   # Windows
   netstat -ano | findstr :8080
   netstat -ano | findstr :8082
   
   # 如果端口被占用，结束进程或更换端口
   ```

3. **查看网关日志**：
   启动网关时应该看到类似信息：
   ```
   API网关启动成功！
   端口: 8080
   ```

### 如果仍然出现404错误：

1. **验证商品服务**：
   ```bash
   # 直接访问商品服务
   curl http://localhost:8082/api/products/health
   ```

2. **检查网关路由**：
   ```bash
   # 通过网关访问
   curl http://localhost:8080/api/products/health
   ```

## 📋 服务检查清单

- [ ] 网关服务启动 (端口8080)
- [ ] 商品服务启动 (端口8082)
- [ ] 前端服务启动 (端口3001)
- [ ] 网关日志显示正常
- [ ] 商品服务日志显示正常
- [ ] 可以访问 `http://localhost:8080/api/health`
- [ ] 可以访问 `http://localhost:8080/api/products/health`

## 🎯 预期结果

修复后应该看到：
- ✅ 网关健康检查通过
- ✅ 商品服务健康检查通过
- ✅ 商品分类API正常返回数据
- ✅ 商品列表API正常返回数据
- ✅ 分类筛选功能正常工作
- ✅ 前端页面正常显示商品

## 💡 调试技巧

1. **查看网关日志**：网关会输出详细的路由转发信息
2. **使用浏览器开发者工具**：检查网络请求的状态码和响应
3. **逐步测试**：先测试网关健康检查，再测试具体API

如果按照以上步骤操作后仍有问题，请提供具体的错误日志信息。