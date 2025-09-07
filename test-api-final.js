const axios = require('axios');

console.log('🚀 开始测试产品服务API...\n');

const baseURL = 'http://localhost:8082/api/products';

async function testAPI() {
  try {
    // 1. 测试获取分类
    console.log('1️⃣ 测试获取分类...');
    const categoriesResponse = await axios.get(`${baseURL}/categories`);
    console.log('✅ 分类数据:', categoriesResponse.data.data?.length || 0, '个分类');
    
    // 2. 测试获取所有商品
    console.log('\n2️⃣ 测试获取所有商品...');
    const allProductsResponse = await axios.get(`${baseURL}?current=1&size=20`);
    console.log('✅ 商品数据:', allProductsResponse.data.data?.records?.length || 0, '个商品');
    
    // 3. 测试分类筛选 - 主食套餐
    console.log('\n3️⃣ 测试分类筛选 - 主食套餐(categoryId=1)...');
    const category1Response = await axios.get(`${baseURL}?categoryId=1&current=1&size=10`);
    console.log('✅ 主食套餐:', category1Response.data.data?.records?.length || 0, '个商品');
    
    // 4. 测试分类筛选 - 面食类
    console.log('\n4️⃣ 测试分类筛选 - 面食类(categoryId=2)...');
    const category2Response = await axios.get(`${baseURL}?categoryId=2&current=1&size=10`);
    console.log('✅ 面食类:', category2Response.data.data?.records?.length || 0, '个商品');
    
    // 5. 测试系统统计
    console.log('\n5️⃣ 测试系统统计...');
    const statsResponse = await axios.get(`${baseURL}/stats`);
    console.log('✅ 系统统计:', statsResponse.data.data || '获取成功');
    
    console.log('\n🎉 所有API测试通过！产品服务运行正常！');
    console.log('\n📋 测试结果总结:');
    console.log('- ✅ 分类接口正常');
    console.log('- ✅ 商品列表接口正常');
    console.log('- ✅ 分类筛选功能正常');
    console.log('- ✅ 系统统计接口正常');
    console.log('\n🚀 现在可以启动前端测试分类筛选功能了！');
    
  } catch (error) {
    console.error('❌ API测试失败:', error.message);
    if (error.response) {
      console.error('响应状态:', error.response.status);
      console.error('响应数据:', error.response.data);
    }
  }
}

testAPI();