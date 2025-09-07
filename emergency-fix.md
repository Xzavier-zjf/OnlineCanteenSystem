# 紧急修复指南 - 502错误解决方案

## 🚨 问题诊断

所有服务都返回**502 Bad Gateway**错误，这表明：
1. 服务进程在运行（端口被占用）
2. 但服务内部有严重错误，无法正常响应请求

## 🔧 紧急修复步骤

### 1. 停止所有服务
```bash
# 找到并停止所有Java进程
tasklist | findstr java
# 记下PID，然后强制停止
taskkill /PID <PID号> /F

# 或者直接停止所有Java进程（谨慎使用）
taskkill /IM java.exe /F
```

### 2. 清理并重新编译
```bash
# 清理网关服务
cd canteen-gateway
mvn clean
mvn compile

# 清理商品服务
cd ../canteen-product-service
mvn clean
mvn compile
```

### 3. 检查依赖问题
网关服务可能缺少必要的依赖。让我们添加缺失的依赖：

```xml
<!-- 在canteen-gateway/pom.xml中添加 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 4. 简化配置启动
先用最简单的配置启动：

**临时application.yml** (canteen-gateway/src/main/resources/application-simple.yml):
```yaml
server:
  port: 8080

spring:
  application:
    name: canteen-gateway

logging:
  level:
    root: INFO
```

### 5. 重新启动服务

```bash
# 1. 启动网关（使用简化配置）
cd canteen-gateway
mvn spring-boot:run -Dspring.profiles.active=simple

# 2. 在新终端启动商品服务
cd canteen-product-service
mvn spring-boot:run

# 3. 在新终端启动前端
cd canteen-web-app
npm run dev
```

## 🔍 如果仍然失败

### 检查Java版本兼容性
```bash
java -version
mvn -version
```

### 查看详细错误日志
```bash
# 启动时添加详细日志
mvn spring-boot:run -X
```

### 使用IDE启动
1. 用IDEA打开项目
2. 直接运行 `GatewayApplication.main()`
3. 查看控制台的详细错误信息

## 🚀 备用方案：直接访问后端

如果网关问题持续，可以临时修改前端配置直接访问后端：

**临时修改 canteen-web-app/vite.config.js**:
```javascript
proxy: {
  '/api/products': {
    target: 'http://localhost:8082',
    changeOrigin: true
  },
  '/api/users': {
    target: 'http://localhost:8081', 
    changeOrigin: true
  }
}
```

## 📋 常见502错误原因

1. **依赖冲突**：Spring Boot版本不兼容
2. **端口冲突**：多个服务抢占同一端口
3. **配置错误**：application.yml语法错误
4. **内存不足**：JVM内存不够
5. **数据库连接**：数据库连接失败导致服务启动异常

## 💡 快速验证

修复后运行：
```bash
# 验证网关
curl http://localhost:8080/status

# 验证商品服务  
curl http://localhost:8082/api/products/health

# 验证前端
访问 http://localhost:3001
```

如果以上步骤都无效，请提供启动时的完整错误日志。