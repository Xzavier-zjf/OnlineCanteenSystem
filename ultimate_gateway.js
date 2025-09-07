const express = require('express');
const cors = require('cors');
const { createProxyMiddleware } = require('http-proxy-middleware');

const app = express();
const PORT = 8080;

// 启用CORS
app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization'],
  credentials: true
}));

// 解析JSON
app.use(express.json());

// 请求日志
app.use((req, res, next) => {
  console.log(`🔄 ${new Date().toISOString()} ${req.method} ${req.originalUrl}`);
  next();
});

// 健康检查
app.get('/api/health', (req, res) => {
  console.log('✅ 健康检查请求');
  res.json({
    code: 200,
    message: '终极网关运行正常',
    data: {
      timestamp: new Date().toISOString(),
      services: {
        users: 'http://localhost:8081',
        products: 'http://localhost:8082', 
        orders: 'http://localhost:8083',
        recommend: 'http://localhost:8084'
      }
    }
  });
});

// 用户服务代理 - 处理双重路径
app.use('/api/users', createProxyMiddleware({
  target: 'http://localhost:8081',
  changeOrigin: true,
  pathRewrite: {
    '^/api/users/users(.*)': '/api/users$1', // 重写双重路径，保留路径参数
    '^/api/users': '/api/users'
  },
  onProxyReq: (proxyReq, req, res) => {
    console.log(`👤 用户服务代理: ${req.method} ${req.originalUrl} -> ${proxyReq.path}`);
  },
  onProxyRes: (proxyRes, req, res) => {
    console.log(`✅ 用户服务响应: ${proxyRes.statusCode}`);
  },
  onError: (err, req, res) => {
    console.error(`❌ 用户服务错误: ${err.message}`);
    res.status(502).json({
      code: 502,
      message: '用户服务不可用',
      data: null
    });
  }
}));

// 商品服务代理 - 处理双重路径
app.use('/api/products', createProxyMiddleware({
  target: 'http://localhost:8082',
  changeOrigin: true,
  pathRewrite: {
    '^/api/products/products(.*)': '/api/products$1', // 重写双重路径，保留查询参数
    '^/api/products': '/api/products'
  },
  onProxyReq: (proxyReq, req, res) => {
    console.log(`🛍️ 商品服务代理: ${req.method} ${req.originalUrl} -> ${proxyReq.path}`);
  },
  onProxyRes: (proxyRes, req, res) => {
    console.log(`✅ 商品服务响应: ${proxyRes.statusCode}`);
  },
  onError: (err, req, res) => {
    console.error(`❌ 商品服务错误: ${err.message}`);
    res.status(502).json({
      code: 502,
      message: '商品服务不可用',
      data: null
    });
  }
}));

// 订单服务代理 - 处理双重路径
app.use('/api/orders', createProxyMiddleware({
  target: 'http://localhost:8083',
  changeOrigin: true,
  pathRewrite: {
    '^/api/orders/orders(.*)': '/api/orders$1', // 重写双重路径，保留查询参数
    '^/api/orders': '/api/orders'
  },
  onProxyReq: (proxyReq, req, res) => {
    console.log(`📋 订单服务代理: ${req.method} ${req.originalUrl} -> ${proxyReq.path}`);
  },
  onProxyRes: (proxyRes, req, res) => {
    console.log(`✅ 订单服务响应: ${proxyRes.statusCode}`);
  },
  onError: (err, req, res) => {
    console.error(`❌ 订单服务错误: ${err.message}`);
    res.status(502).json({
      code: 502,
      message: '订单服务不可用',
      data: null
    });
  }
}));

// 推荐服务代理
app.use('/api/recommend', createProxyMiddleware({
  target: 'http://localhost:8084',
  changeOrigin: true,
  onProxyReq: (proxyReq, req, res) => {
    console.log(`⭐ 推荐服务代理: ${req.method} ${req.originalUrl} -> ${proxyReq.path}`);
  },
  onProxyRes: (proxyRes, req, res) => {
    console.log(`✅ 推荐服务响应: ${proxyRes.statusCode}`);
  },
  onError: (err, req, res) => {
    console.error(`❌ 推荐服务错误: ${err.message}`);
    res.status(502).json({
      code: 502,
      message: '推荐服务不可用',
      data: null
    });
  }
}));

// 管理员和商户路由
app.use('/api/admin', createProxyMiddleware({
  target: 'http://localhost:8081',
  changeOrigin: true,
  onProxyReq: (proxyReq, req, res) => {
    console.log(`👨‍💼 管理员服务代理: ${req.method} ${req.originalUrl}`);
  }
}));

app.use('/api/merchant', createProxyMiddleware({
  target: 'http://localhost:8081',
  changeOrigin: true,
  onProxyReq: (proxyReq, req, res) => {
    console.log(`🏪 商户服务代理: ${req.method} ${req.originalUrl}`);
  }
}));

// 错误处理
app.use((err, req, res, next) => {
  console.error('🚨 网关错误:', err);
  res.status(500).json({
    code: 500,
    message: '网关内部错误',
    data: null
  });
});

// 启动服务器
app.listen(PORT, () => {
  console.log(`
🚀 ===== 终极食堂订餐系统网关 =====
📍 端口: ${PORT}
🕒 启动时间: ${new Date().toLocaleString()}

🔗 服务映射:
   👤 /api/users -> http://localhost:8081
   🛍️ /api/products -> http://localhost:8082  
   📋 /api/orders -> http://localhost:8083
   ⭐ /api/recommend -> http://localhost:8084

🌐 访问地址:
   🖥️ 网关: http://localhost:${PORT}
   🌍 前端: http://localhost:3001

✨ 特殊处理:
   🔄 自动路径重写 (/api/users/users -> /api/users)
   🛡️ CORS 完全开放
   📝 详细请求日志

🎯 网关已就绪，等待前端请求...
==========================================
`);
});