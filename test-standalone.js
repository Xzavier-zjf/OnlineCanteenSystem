// 测试独立启动的服务
const axios = require('axios');

async function testStandalone() {
  console.log('🔍 测试独立启动的服务...\n');
  
  const tests = [
    { 
      name: '商品服务健康检查', 
      url: 'http://localhost:8082/api/products/health',
      description: '检查商品服务是否正常启动'
    },
    { 
      name: '网关服务状态', 
      url: 'http://localhost:8080/status',
      description: '检查网关服务是否正常启动'
    },
    { 
      name: '通过网关访问商品服务', 
      url: 'http://localhost:8080/api/products/health',
      description: '检查网关路由是否正常'
    },
    { 
      name: '商品列表API', 
      url: 'http://localhost:8082/api/products',
      description: '检查商品数据是否可用'
    }
  ];
  
  let successCount = 0;
  
  for (const test of tests) {
    try {
      console.log(`🔄 ${test.name}...`);
      const response = await axios.get(test.url, { timeout: 5000 });
      console.log(`✅ ${test.name}: ${response.status}`);
      if (response.data) {
        console.log(`   数据: ${JSON.stringify(response.data).substring(0, 100)}...`);
      }
      successCount++;
    } catch (error) {
      if (error.code === 'ECONNREFUSED') {
        console.log(`❌ ${test.name}: 服务未启动`);
        console.log(`   💡 ${test.description}`);
      } else {
        console.log(`❌ ${test.name}: ${error.response?.status || error.message}`);
      }
    }
    console.log('');
  }
  
  console.log(`📊 测试结果: ${successCount}/${tests.length} 个服务正常\n`);
  
  if (successCount === 0) {
    console.log('🚨 所有服务都未启动，请执行：');
    console.log('1. cd canteen-product-service');
    console.log('2. mvn spring-boot:run -Dspring-boot.run.profiles=standalone');
    console.log('3. 在新终端: cd canteen-gateway');
    console.log('4. mvn spring-boot:run -Dspring-boot.run.profiles=standalone');
  } else if (successCount < tests.length) {
    console.log('⚠️ 部分服务启动成功，请检查未启动的服务');
  } else {
    console.log('🎉 所有服务启动成功！现在可以启动前端：');
    console.log('cd canteen-web-app && npm run dev');
  }
}

if (require.main === module) {
  testStandalone();
}

module.exports = { testStandalone };