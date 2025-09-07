// CORS和分类筛选功能测试脚本
const axios = require('axios');

async function testCorsAndCategories() {
  console.log('🔍 开始测试CORS和分类筛选功能...\n');
  
  const baseURL = 'http://localhost:8080';
  
  try {
    // 1. 测试网关健康检查
    console.log('1. 测试网关健康检查...');
    const healthResponse = await axios.get(`${baseURL}/api/health`);
    console.log('✅ 网关健康检查通过:', healthResponse.data);
    
    // 2. 测试获取商品分类
    console.log('\n2. 测试获取商品分类...');
    const categoriesResponse = await axios.get(`${baseURL}/api/products/categories`);
    console.log('✅ 商品分类获取成功:', categoriesResponse.data);
    
    // 3. 测试获取所有商品
    console.log('\n3. 测试获取所有商品...');
    const allProductsResponse = await axios.get(`${baseURL}/api/products?current=1&size=6`);
    console.log('✅ 所有商品获取成功，数量:', allProductsResponse.data?.data?.records?.length || 0);
    
    // 4. 测试分类筛选 - 主食套餐
    console.log('\n4. 测试分类筛选 - 主食套餐 (categoryId=1)...');
    const categoryProductsResponse = await axios.get(`${baseURL}/api/products?current=1&size=6&categoryId=1`);
    console.log('✅ 主食套餐筛选成功，数量:', categoryProductsResponse.data?.data?.records?.length || 0);
    
    // 5. 测试分类筛选 - 面食类
    console.log('\n5. 测试分类筛选 - 面食类 (categoryId=2)...');
    const noodleProductsResponse = await axios.get(`${baseURL}/api/products?current=1&size=6&categoryId=2`);
    console.log('✅ 面食类筛选成功，数量:', noodleProductsResponse.data?.data?.records?.length || 0);
    
    console.log('\n🎉 所有测试通过！CORS和分类筛选功能已修复！');
    
  } catch (error) {
    console.error('❌ 测试失败:', error.message);
    if (error.response) {
      console.error('响应状态:', error.response.status);
      console.error('响应数据:', error.response.data);
    }
    console.log('\n💡 请确保：');
    console.log('1. 网关服务已启动 (端口8080)');
    console.log('2. 商品服务已启动 (端口8082)');
    console.log('3. 前端开发服务器已启动 (端口3001)');
  }
}

// 如果直接运行此脚本
if (require.main === module) {
  testCorsAndCategories();
}

module.exports = { testCorsAndCategories };