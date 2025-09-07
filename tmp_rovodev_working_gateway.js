const express = require('express');
const cors = require('cors');
const axios = require('axios');

const app = express();
const PORT = 8080;

// 启用CORS
app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));

// 解析JSON
app.use(express.json());

// 日志中间件
app.use((req, res, next) => {
  console.log(`${new Date().toISOString()} ${req.method} ${req.url}`);
  next();
});

// 健康检查
app.get('/api/health', (req, res) => {
  res.json({
    code: 200,
    message: '网关运行正常',
    data: null
  });
});

// 通用代理函数
async function proxyRequest(serviceUrl, req, res) {
  try {
    const targetUrl = serviceUrl + req.url;
    console.log(`代理请求: ${req.method} ${req.url} -> ${targetUrl}`);
    
    const config = {
      method: req.method,
      url: targetUrl,
      headers: {
        'Content-Type': 'application/json',
        ...(req.headers.authorization && { 'Authorization': req.headers.authorization })
      },
      timeout: 10000
    };
    
    if (req.body && Object.keys(req.body).length > 0) {
      config.data = req.body;
    }
    
    const response = await axios(config);
    console.log(`代理成功: ${response.status} ${req.url}`);
    res.status(response.status).json(response.data);
    
  } catch (error) {
    console.error(`代理失败: ${req.url} - ${error.message}`);
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res.status(500).json({
        code: 500,
        message: '服务不可用: ' + error.message,
        data: null
      });
    }
  }
}

// 用户服务路由 - 处理双重路径问题
app.all('/api/users/users/*', (req, res) => {
  // 移除重复的 /users
  req.url = req.url.replace('/api/users/users', '/api/users');
  proxyRequest('http://localhost:8081', req, res);
});

app.all('/api/users/*', (req, res) => {
  proxyRequest('http://localhost:8081', req, res);
});

// 商品服务路由 - 处理双重路径问题
app.all('/api/products/products/*', (req, res) => {
  // 移除重复的 /products
  req.url = req.url.replace('/api/products/products', '/api/products');
  proxyRequest('http://localhost:8082', req, res);
});

app.all('/api/products/*', (req, res) => {
  proxyRequest('http://localhost:8082', req, res);
});

// 订单服务路由 - 处理双重路径问题
app.all('/api/orders/orders/*', (req, res) => {
  // 移除重复的 /orders
  req.url = req.url.replace('/api/orders/orders', '/api/orders');
  proxyRequest('http://localhost:8083', req, res);
});

app.all('/api/orders/*', (req, res) => {
  proxyRequest('http://localhost:8083', req, res);
});

// 推荐服务路由
app.all('/api/recommend/*', (req, res) => {
  proxyRequest('http://localhost:8084', req, res);
});

// 管理员路由
app.all('/api/admin/*', (req, res) => {
  proxyRequest('http://localhost:8081', req, res);
});

// 商户路由
app.all('/api/merchant/*', (req, res) => {
  proxyRequest('http://localhost:8081', req, res);
});

// 错误处理
app.use((err, req, res, next) => {
  console.error('网关错误:', err);
  res.status(500).json({
    code: 500,
    message: '网关内部错误: ' + err.message,
    data: null
  });
});

// 启动服务器
app.listen(PORT, () => {
  console.log(`\n=== 食堂订餐系统网关启动成功 ===`);
  console.log(`端口: ${PORT}`);
  console.log(`时间: ${new Date().toLocaleString()}`);
  console.log(`\n路由配置:`);
  console.log(`- /api/users -> http://localhost:8081`);
  console.log(`- /api/products -> http://localhost:8082`);
  console.log(`- /api/orders -> http://localhost:8083`);
  console.log(`- /api/recommend -> http://localhost:8084`);
  console.log(`\n网关地址: http://localhost:${PORT}`);
  console.log(`前端地址: http://localhost:3001`);
  console.log(`================================\n`);
});