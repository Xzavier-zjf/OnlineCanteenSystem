const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const cors = require('cors');

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

// 健康检查
app.get('/api/health', (req, res) => {
  res.json({
    code: 200,
    message: 'Mock网关运行正常',
    data: null
  });
});

// 用户服务代理
app.use('/api/users', createProxyMiddleware({
  target: 'http://localhost:8081',
  changeOrigin: true,
  pathRewrite: {
    '^/api/users/users': '/api/users',  // 修复双重users路径
    '^/api/users': '/api/users'
  },
  onError: (err, req, res) => {
    console.error('用户服务代理错误:', err.message);
    res.status(500).json({
      code: 500,
      message: '用户服务不可用: ' + err.message,
      data: null
    });
  },
  onProxyReq: (proxyReq, req, res) => {
    console.log('代理到用户服务:', req.method, req.originalUrl, '->', req.url);
  }
}));

// 商品服务代理 - 修复路径映射
app.use('/api/products', createProxyMiddleware({
  target: 'http://localhost:8082',
  changeOrigin: true,
  pathRewrite: {
    '^/api/products/products': '/api/products',  // 修复双重products路径
    '^/api/products': '/api/products'
  },
  onError: (err, req, res) => {
    console.error('商品服务代理错误:', err.message);
    res.status(500).json({
      code: 500,
      message: '商品服务不可用: ' + err.message,
      data: null
    });
  },
  onProxyReq: (proxyReq, req, res) => {
    console.log('代理到商品服务:', req.method, req.originalUrl, '->', req.url);
  }
}));

// 订单服务代理 - 修复路径映射
app.use('/api/orders', createProxyMiddleware({
  target: 'http://localhost:8083',
  changeOrigin: true,
  pathRewrite: {
    '^/api/orders/orders': '/api/orders',  // 修复双重orders路径
    '^/api/orders': '/api/orders'
  },
  onError: (err, req, res) => {
    console.error('订单服务代理错误:', err.message);
    res.status(500).json({
      code: 500,
      message: '订单服务不可用: ' + err.message,
      data: null
    });
  },
  onProxyReq: (proxyReq, req, res) => {
    console.log('代理到订单服务:', req.method, req.originalUrl, '->', req.url);
  }
}));

// 推荐服务代理
app.use('/api/recommend', createProxyMiddleware({
  target: 'http://localhost:8084',
  changeOrigin: true,
  onError: (err, req, res) => {
    console.error('推荐服务代理错误:', err.message);
    res.status(500).json({
      code: 500,
      message: '推荐服务不可用: ' + err.message,
      data: null
    });
  },
  onProxyReq: (proxyReq, req, res) => {
    console.log('代理到推荐服务:', req.method, req.url);
  }
}));

// 启动服务器
app.listen(PORT, () => {
  console.log(`Mock网关启动成功！端口: ${PORT}`);
  console.log('代理配置:');
  console.log('- /api/users -> http://localhost:8081');
  console.log('- /api/products -> http://localhost:8082');
  console.log('- /api/orders -> http://localhost:8083');
  console.log('- /api/recommend -> http://localhost:8084');
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