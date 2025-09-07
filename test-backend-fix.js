// 测试后端修复的脚本
const axios = require('axios');

async function testBackendFix() {
  console.log('🔍 测试后端接口修复...\n');
  
  const baseUrl = 'http://localhost:8082';
  
  const tests = [
    {
      name: '商品服务健康检查',
      url: `${baseUrl}/api/product/health`,
      description: '检查兼容控制器是否正常'
    },
    {
      name: '商品分类列表',
      url: `${baseUrl}/api/product/products/categories`,
      description: '测试前端请求的分类接口'
    },
    {
      name: '商品列表（基础）',
      url: `${baseUrl}/api/product/products?current=1&size=6`,
      description: '测试基础商品列表'
    },
    {
      name: '商品列表（带排序）',
      url: `${baseUrl}/api/product/products?current=1&size=3&sortBy=price_desc`,
      description: '测试排序功能，解决500错误'
    },
    {
      name: '分类筛选（主食套餐）',
      url: `${baseUrl}/api/product/products?categoryId=1&current=1&size=10`,
      description: '测试分类筛选功能'
    },
    {
      name: '分类筛选（面食类）',
      url: `${baseUrl}/api/product/products?categoryId=2&current=1&size=10`,
      description: '测试面食类筛选'
    }
  ];
  
  let successCount = 0;
  
  for (const test of tests) {
    try {
      console.log(`🔄 ${test.name}...`);
      const response = await axios.get(test.url, { timeout: 5000 });
      
      if (response.status === 200) {
        console.log(`✅ ${test.name}: 成功`);
        
        // 显示返回数据的简要信息
        if (response.data && response.data.data) {
          const data = response.data.data;
          if (Array.isArray(data)) {
            console.log(`   返回 ${data.length} 条记录`);
          } else if (data.records) {
            console.log(`   返回 ${data.records.length} 条记录，总数: ${data.total}`);
          } else {
            console.log(`   返回数据: ${JSON.stringify(data).substring(0, 50)}...`);
          }
        }
        successCount++;
      } else {
        console.log(`❌ ${test.name}: HTTP ${response.status}`);
      }
    } catch (error) {
      if (error.code === 'ECONNREFUSED') {
        console.log(`❌ ${test.name}: 服务未启动 (端口8082)`);
        console.log(`   💡 请先启动: cd canteen-product-service && mvn spring-boot:run -Dspring-boot.run.profiles=standalone`);
      } else if (error.response) {
        console.log(`❌ ${test.name}: HTTP ${error.response.status}`);
        if (error.response.status === 500) {
          console.log(`   💡 500错误: ${error.response.data?.message || '内部服务器错误'}`);
        } else if (error.response.status === 404) {
          console.log(`   💡 404错误: 接口不存在或路径错误`);
        }
      } else {
        console.log(`❌ ${test.name}: ${error.message}`);
      }
    }
    console.log('');
  }
  
  console.log(`📊 测试结果: ${successCount}/${tests.length} 个接口正常\n`);
  
  if (successCount === tests.length) {
    console.log('🎉 所有后端接口修复成功！');
    console.log('现在可以启动前端测试分类筛选功能：');
    console.log('cd canteen-web-app && npm run dev');
  } else if (successCount === 0) {
    console.log('🚨 后端服务未启动，请先启动商品服务：');
    console.log('cd canteen-product-service && mvn spring-boot:run -Dspring-boot.run.profiles=standalone');
  } else {
    console.log('⚠️ 部分接口有问题，请检查后端日志');
  }
}

if (require.main === module) {
  testBackendFix();
}

module.exports = { testBackendFix };