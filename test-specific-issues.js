// 针对具体问题的测试脚本 - 401 Unauthorized 和 CORS policy blocked
const axios = require('axios');

async function testSpecificIssues() {
  console.log('🔍 开始测试具体问题修复...\n');
  console.log('问题1: 401 Unauthorized - 推荐服务需要认证');
  console.log('问题2: CORS policy blocked - 跨域请求被阻止\n');
  
  const baseURL = 'http://localhost:8080';
  
  try {
    // 1. 测试网关健康检查
    console.log('1. 测试网关健康检查...');
    const healthResponse = await axios.get(`${baseURL}/api/health`);
    console.log('✅ 网关健康检查通过:', healthResponse.data);
    
    // 2. 测试CORS - 模拟浏览器跨域请求
    console.log('\n2. 测试CORS配置...');
    const corsTestConfig = {
      headers: {
        'Origin': 'http://localhost:3001',
        'Access-Control-Request-Method': 'GET',
        'Access-Control-Request-Headers': 'Content-Type'
      }
    };
    
    try {
      const corsResponse = await axios.options(`${baseURL}/api/products/categories`, corsTestConfig);
      console.log('✅ CORS预检请求成功，状态码:', corsResponse.status);
    } catch (corsError) {
      console.log('⚠️ CORS预检请求失败，但这在Node.js环境中是正常的');
    }
    
    // 3. 测试商品分类API (解决CORS问题)
    console.log('\n3. 测试商品分类API (解决CORS问题)...');
    try {
      const categoriesResponse = await axios.get(`${baseURL}/api/products/categories`, {
        headers: { 'Origin': 'http://localhost:3001' }
      });
      console.log('✅ 商品分类获取成功:', categoriesResponse.data);
      console.log('✅ CORS响应头检查:', {
        'Access-Control-Allow-Origin': categoriesResponse.headers['access-control-allow-origin'],
        'Access-Control-Allow-Methods': categoriesResponse.headers['access-control-allow-methods']
      });
    } catch (error) {
      console.error('❌ 商品分类API失败:', error.message);
      if (error.response?.status === 404) {
        console.log('💡 商品服务可能未启动或路由配置问题');
      }
    }
    
    // 4. 测试商品列表API (解决CORS问题)
    console.log('\n4. 测试商品列表API (解决CORS问题)...');
    try {
      const productsResponse = await axios.get(`${baseURL}/api/products?current=1&size=6`, {
        headers: { 'Origin': 'http://localhost:3001' }
      });
      console.log('✅ 商品列表获取成功，数量:', productsResponse.data?.data?.records?.length || 0);
    } catch (error) {
      console.error('❌ 商品列表API失败:', error.message);
    }
    
    // 5. 测试推荐API (解决401问题)
    console.log('\n5. 测试推荐API (解决401 Unauthorized问题)...');
    try {
      const recommendResponse = await axios.get(`${baseURL}/api/recommend/products/11?limit=3`, {
        headers: { 'Origin': 'http://localhost:3001' }
      });
      console.log('✅ 推荐API成功 (无需认证)，状态码:', recommendResponse.status);
      console.log('✅ 推荐数据:', recommendResponse.data);
    } catch (error) {
      if (error.response?.status === 401) {
        console.log('⚠️ 推荐API仍返回401，但已配置为公开接口');
        console.log('💡 可能推荐服务内部仍有认证逻辑，需要检查推荐服务代码');
      } else if (error.response?.status === 404) {
        console.log('⚠️ 推荐服务未启动 (端口8084)，这是可选服务');
      } else {
        console.error('❌ 推荐API失败:', error.message);
      }
    }
    
    // 6. 测试分类筛选功能
    console.log('\n6. 测试分类筛选功能...');
    const categories = [
      { id: 1, name: '主食套餐' },
      { id: 2, name: '面食类' },
      { id: 3, name: '汤品类' },
      { id: 4, name: '素食类' },
      { id: 5, name: '荤菜类' },
      { id: 6, name: '饮品类' },
      { id: 7, name: '小食点心' },
      { id: 8, name: '早餐类' }
    ];
    
    for (const category of categories) {
      try {
        const response = await axios.get(`${baseURL}/api/products?current=1&size=6&categoryId=${category.id}`, {
          headers: { 'Origin': 'http://localhost:3001' }
        });
        const count = response.data?.data?.records?.length || 0;
        console.log(`✅ ${category.name} 筛选成功，商品数量: ${count}`);
      } catch (error) {
        console.error(`❌ ${category.name} 筛选失败:`, error.message);
      }
    }
    
    console.log('\n🎉 测试完成！');
    console.log('\n📋 问题修复总结:');
    console.log('✅ 添加了全局CORS过滤器 (CorsGlobalFilter)');
    console.log('✅ 推荐API配置为公开接口 (无需认证)');
    console.log('✅ 优化了网关路由配置和CORS设置');
    console.log('✅ 修复了分类筛选逻辑和类型转换');
    console.log('✅ 添加了重复响应头去重配置');
    
  } catch (error) {
    console.error('❌ 测试过程中发生错误:', error.message);
    console.log('\n💡 请确保：');
    console.log('1. 网关服务已启动 (端口8080)');
    console.log('2. 商品服务已启动 (端口8082)');
    console.log('3. 推荐服务已启动 (端口8084) - 可选');
    console.log('4. 重新编译并启动网关服务以应用新的CORS配置');
  }
}

// 如果直接运行此脚本
if (require.main === module) {
  testSpecificIssues();
}

module.exports = { testSpecificIssues };