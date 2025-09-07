@echo off
echo ================================
echo 重启所有服务 - 解决502错误
echo ================================

echo 1. 停止所有进程...
taskkill /f /im java.exe 2>nul
taskkill /f /im node.exe 2>nul
timeout /t 3

echo.
echo 2. 启动微服务（按顺序）...

echo 启动用户服务...
start "用户服务" /min cmd /c "cd canteen-user-service && mvn spring-boot:run"
timeout /t 20

echo 启动商品服务...
start "商品服务" /min cmd /c "cd canteen-product-service && mvn spring-boot:run"
timeout /t 20

echo 启动订单服务...
start "订单服务" /min cmd /c "cd canteen-order-service && mvn spring-boot:run"
timeout /t 20

echo 启动推荐服务...
start "推荐服务" /min cmd /c "cd canteen-recommend-service && mvn spring-boot:run"
timeout /t 15

echo.
echo 3. 启动Node.js网关...
start "网关服务" cmd /c "node tmp_rovodev_working_gateway.js"
timeout /t 10

echo.
echo ================================
echo 所有服务启动完成！
echo 前端地址: http://localhost:3001
echo 网关地址: http://localhost:8080
echo ================================
pause