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
  console.log(`${new Date().toISOString()} ${req.method} ${req.originalUrl}`);
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
async function proxyRequest(serviceUrl, originalPath, targetPath, req, res) {
  try {
    const fullUrl = serviceUrl + targetPath;
    console.log(`代理: ${originalPath} -> ${fullUrl}`);
    
    const config = {
      method: req.method,
      url: fullUrl,
      headers: {
        'Content-Type': 'application/json',
        ...(req.headers.authorization && { 'Authorization': req.headers.authorization })
      },
      timeout: 10000,
      validateStatus: function (status) {
        return status < 500; // 接受所有小于500的状态码
      }
    };
    
    if (req.body && Object.keys(req.body).length > 0) {
      config.data = req.body;
    }
    
    const response = await axios(config);
    console.log(`✅ 代理成功: ${response.status}`);
    res.status(response.status).json(response.data);
    
  } catch (error) {
    console.error(`❌ 代理失败: ${originalPath} - ${error.message}`);
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res.status(502).json({
        code: 502,
        message: '服务不可用: ' + error.message,
        data: null
      });
    }
  }
}

// 用户服务路由 - 修复双重路径
app.all('/api/users/users/login', (req, res) => {
  proxyRequest('http://localhost:8081', req.originalUrl, '/api/users/login', req, res);
});

app.all('/api/users/users/*', (req, res) => {
  const targetPath = req.originalUrl.replace('/api/users/users', '/api/users');
  proxyRequest('http://localhost:8081', req.originalUrl, targetPath, req, res);
});

app.all('/api/users/*', (req, res) => {
  proxyRequest('http://localhost:8081', req.originalUrl, req.originalUrl, req, res);
});

// 商品服务路由 - 修复双重路径
app.all('/api/products/products', (req, res) => {
  const targetPath = '/api/products' + (req.url.includes('?') ? req.url.substring(req.url.indexOf('?')) : '');
  proxyRequest('http://localhost:8082', req.originalUrl, targetPath, req, res);
});

app.all('/api/products/products/*', (req, res) => {
  const targetPath = req.originalUrl.replace('/api/products/products', '/api/products');
  proxyRequest('http://localhost:8082', req.originalUrl, targetPath, req, res);
});

app.all('/api/products/*', (req, res) => {
  proxyRequest('http://localhost:8082', req.originalUrl, req.originalUrl, req, res);
});

// 订单服务路由 - 修复双重路径
app.all('/api/orders/orders', (req, res) => {
  const targetPath = '/api/orders' + (req.url.includes('?') ? req.url.substring(req.url.indexOf('?')) : '');
  proxyRequest('http://localhost:8083', req.originalUrl, targetPath, req, res);
});

app.all('/api/orders/orders/*', (req, res) => {
  const targetPath = req.originalUrl.replace('/api/orders/orders', '/api/orders');
  proxyRequest('http://localhost:8083', req.originalUrl, targetPath, req, res);
});

app.all('/api/orders/*', (req, res) => {
  proxyRequest('http://localhost:8083', req.originalUrl, req.originalUrl, req, res);
});

// 推荐服务路由
app.all('/api/recommend/*', (req, res) => {
  proxyRequest('http://localhost:8084', req.originalUrl, req.originalUrl, req, res);
});

// 管理员和商户路由
app.all('/api/admin/*', (req, res) => {
  proxyRequest('http://localhost:8081', req.originalUrl, req.originalUrl, req, res);
});

app.all('/api/merchant/*', (req, res) => {
  proxyRequest('http://localhost:8081', req.originalUrl, req.originalUrl, req, res);
});

// 启动服务器
app.listen(PORT, () => {
  console.log(`\n🚀 食堂订餐系统网关启动成功！`);
  console.log(`📍 端口: ${PORT}`);
  console.log(`🕒 时间: ${new Date().toLocaleString()}`);
  console.log(`\n🔗 路由配置:`);
  console.log(`   /api/users -> http://localhost:8081`);
  console.log(`   /api/products -> http://localhost:8082`);
  console.log(`   /api/orders -> http://localhost:8083`);
  console.log(`   /api/recommend -> http://localhost:8084`);
  console.log(`\n🌐 访问地址:`);
  console.log(`   网关: http://localhost:${PORT}`);
  console.log(`   前端: http://localhost:3001`);
  console.log(`\n✅ 网关已就绪，等待请求...\n`);
});