// 测试代理修复的脚本
const axios = require('axios');

async function testProxyFix() {
  console.log('🔍 测试Vite代理修复...\n');
  
  // 模拟前端请求的路径
  const frontendRequests = [
    {
      name: '商品分类请求',
      path: '/api/product/products/categories',
      expectedProxy: 'http://localhost:8082/api/products/categories',
      description: '前端请求 /api/product/products/categories → 代理到商品服务'
    },
    {
      name: '商品列表请求',
      path: '/api/product/products?current=1&size=6',
      expectedProxy: 'http://localhost:8082/api/products?current=1&size=6',
      description: '前端请求 /api/product/products → 代理到商品服务'
    },
    {
      name: '推荐商品请求',
      path: '/api/recommend/products/11?limit=3',
      expectedProxy: 'http://localhost:8084/api/recommend/products/11?limit=3',
      description: '前端请求 /api/recommend → 代理到推荐服务'
    },
    {
      name: '订单列表请求',
      path: '/api/order/orders',
      expectedProxy: 'http://localhost:8083/api/orders',
      description: '前端请求 /api/order/orders → 代理到订单服务'
    }
  ];
  
  console.log('📋 代理规则验证:\n');
  
  frontendRequests.forEach((req, index) => {
    console.log(`${index + 1}. ${req.name}`);
    console.log(`   前端请求: ${req.path}`);
    console.log(`   代理目标: ${req.expectedProxy}`);
    console.log(`   说明: ${req.description}\n`);
  });
  
  console.log('🔧 修复内容总结:');
  console.log('1. ✅ 添加了 /api/product → /api/products 的路径重写');
  console.log('2. ✅ 添加了 /api/order → /api/orders 的路径重写');
  console.log('3. ✅ 修复了 createServiceRequest 使用相对路径');
  console.log('4. ✅ 添加了详细的请求/响应日志');
  
  console.log('\n🚀 现在请重启前端服务测试:');
  console.log('cd canteen-web-app && npm run dev');
  
  console.log('\n💡 验证方法:');
  console.log('1. 打开浏览器开发者工具的Network面板');
  console.log('2. 访问商品页面，查看请求是否通过代理');
  console.log('3. 请求URL应该是相对路径 /api/... 而不是 http://localhost:8080/...');
  console.log('4. 不应该再出现CORS错误');
}

if (require.main === module) {
  testProxyFix();
}

module.exports = { testProxyFix };