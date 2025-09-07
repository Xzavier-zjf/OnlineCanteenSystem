// 测试路由修复的脚本
const axios = require('axios');

async function testRoutingFix() {
  console.log('🔍 测试路由修复...\n');
  
  const baseURL = 'http://localhost:8080';
  
  try {
    // 1. 测试网关健康检查
    console.log('1. 测试网关健康检查...');
    try {
      const healthResponse = await axios.get(`${baseURL}/api/health`);
      console.log('✅ 网关健康检查通过:', healthResponse.data);
    } catch (error) {
      console.error('❌ 网关健康检查失败:', error.message);
      console.log('💡 请确保网关服务已启动 (端口8080)');
      return;
    }
    
    // 2. 测试商品服务健康检查
    console.log('\n2. 测试商品服务健康检查...');
    try {
      const productHealthResponse = await axios.get(`${baseURL}/api/products/health`);
      console.log('✅ 商品服务健康检查通过:', productHealthResponse.data);
    } catch (error) {
      console.error('❌ 商品服务健康检查失败:', error.message);
      if (error.response?.status === 404) {
        console.log('💡 路由配置可能有问题，或商品服务未启动 (端口8082)');
      }
    }
    
    // 3. 测试商品分类API
    console.log('\n3. 测试商品分类API...');
    try {
      const categoriesResponse = await axios.get(`${baseURL}/api/products/categories`);
      console.log('✅ 商品分类获取成功:', categoriesResponse.data);
    } catch (error) {
      console.error('❌ 商品分类API失败:', error.message);
      console.log('状态码:', error.response?.status);
    }
    
    // 4. 测试商品列表API
    console.log('\n4. 测试商品列表API...');
    try {
      const productsResponse = await axios.get(`${baseURL}/api/products?current=1&size=6`);
      console.log('✅ 商品列表获取成功，数量:', productsResponse.data?.data?.records?.length || 0);
    } catch (error) {
      console.error('❌ 商品列表API失败:', error.message);
      console.log('状态码:', error.response?.status);
    }
    
    // 5. 测试分类筛选
    console.log('\n5. 测试分类筛选...');
    try {
      const categoryResponse = await axios.get(`${baseURL}/api/products?current=1&size=6&categoryId=1`);
      console.log('✅ 分类筛选成功，数量:', categoryResponse.data?.data?.records?.length || 0);
    } catch (error) {
      console.error('❌ 分类筛选失败:', error.message);
    }
    
    // 6. 测试推荐API (可选)
    console.log('\n6. 测试推荐API...');
    try {
      const recommendResponse = await axios.get(`${baseURL}/api/recommend/products/11?limit=3`);
      console.log('✅ 推荐API成功:', recommendResponse.status);
    } catch (error) {
      if (error.response?.status === 404) {
        console.log('⚠️ 推荐服务未启动 (端口8084) - 这是可选服务');
      } else {
        console.error('❌ 推荐API失败:', error.message);
      }
    }
    
    console.log('\n🎉 路由测试完成！');
    
  } catch (error) {
    console.error('❌ 测试过程中发生错误:', error.message);
  }
}

// 检查服务状态的函数
async function checkServiceStatus() {
  console.log('🔍 检查各服务状态...\n');
  
  const services = [
    { name: '网关服务', url: 'http://localhost:8080/api/health' },
    { name: '商品服务', url: 'http://localhost:8082/api/products/health' },
    { name: '用户服务', url: 'http://localhost:8081/api/users/health' },
    { name: '订单服务', url: 'http://localhost:8083/api/orders/health' },
    { name: '推荐服务', url: 'http://localhost:8084/api/recommend/health' }
  ];
  
  for (const service of services) {
    try {
      const response = await axios.get(service.url, { timeout: 3000 });
      console.log(`✅ ${service.name} 运行正常`);
    } catch (error) {
      if (error.code === 'ECONNREFUSED') {
        console.log(`❌ ${service.name} 未启动`);
      } else {
        console.log(`⚠️ ${service.name} 状态未知: ${error.message}`);
      }
    }
  }
}

// 如果直接运行此脚本
if (require.main === module) {
  const args = process.argv.slice(2);
  if (args.includes('--check-services')) {
    checkServiceStatus();
  } else {
    testRoutingFix();
  }
}

module.exports = { testRoutingFix, checkServiceStatus };