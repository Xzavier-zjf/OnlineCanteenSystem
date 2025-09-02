@echo off
echo 启动高校食堂订餐系统所有服务...

echo.
echo 1. 启动用户服务 (端口: 8081)
start "用户服务" cmd /k "cd canteen-user-service && mvn spring-boot:run"

timeout /t 5

echo.
echo 2. 启动产品服务 (端口: 8082)
start "产品服务" cmd /k "cd canteen-product-service && mvn spring-boot:run"

timeout /t 5

echo.
echo 3. 启动订单服务 (端口: 8083)
start "订单服务" cmd /k "cd canteen-order-service && mvn spring-boot:run"

timeout /t 5

echo.
echo 4. 启动推荐服务 (端口: 8084)
start "推荐服务" cmd /k "cd canteen-recommend-service && mvn spring-boot:run"

timeout /t 5

echo.
echo 5. 启动网关服务 (端口: 8080)
start "网关服务" cmd /k "cd canteen-gateway && mvn spring-boot:run"

timeout /t 5

echo.
echo 6. 启动前端应用 (端口: 5173)
start "前端应用" cmd /k "cd canteen-web-app && npm run dev"

echo.
echo 所有服务启动完成！
echo 前端访问地址: http://localhost:5173
echo 网关访问地址: http://localhost:8080
echo.
pause