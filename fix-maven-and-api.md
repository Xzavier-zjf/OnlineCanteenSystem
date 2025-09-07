# 修复Maven命令和API问题

## 🚨 问题1: Maven命令语法错误

**错误命令:**
```bash
mvn spring-boot:run -Dspring.profiles.active=simple
```

**正确命令:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=simple
```

## 🚨 问题2: 前端仍在请求404的API

前端请求的API路径不存在：
- `/api/product/products` → 404
- `/api/order/orders` → 404

## 🔧 立即修复步骤

### 1. 停止所有服务
```bash
taskkill /IM java.exe /F
```

### 2. 正确启动网关
```bash
cd canteen-gateway
mvn spring-boot:run -Dspring-boot.run.profiles=simple
```

### 3. 启动商品服务
```bash
cd canteen-product-service
mvn spring-boot:run
```

### 4. 测试基本连通性
```bash
node simple-test.js
```

## 🎯 如果网关仍然有问题

### 方案A: 使用默认配置启动
```bash
cd canteen-gateway
mvn spring-boot:run
```

### 方案B: 直接用IDE启动
1. 用IDEA打开 `canteen-gateway`
2. 运行 `GatewayApplication.main()`
3. 查看控制台错误信息

### 方案C: 临时绕过网关
修改前端直接访问后端服务，跳过有问题的网关。

## 💡 快速验证命令

```bash
# 检查端口占用
netstat -ano | findstr :8080
netstat -ano | findstr :8082

# 测试网关
curl http://localhost:8080/
curl http://localhost:8080/status

# 测试商品服务
curl http://localhost:8082/api/products/health
```

## 🚀 下一步

1. 先用正确的Maven命令启动服务
2. 运行 `simple-test.js` 查看哪些服务正常
3. 根据测试结果决定是修复网关还是绕过网关