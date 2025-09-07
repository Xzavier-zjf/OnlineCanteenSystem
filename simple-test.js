// 简单测试脚本 - 检查基本连通性
const axios = require('axios');

async function simpleTest() {
  console.log('🔍 简单连通性测试...\n');
  
  const tests = [
    { name: '网关根路径', url: 'http://localhost:8080/' },
    { name: '网关状态', url: 'http://localhost:8080/status' },
    { name: '网关健康检查', url: 'http://localhost:8080/api/health' },
    { name: '商品服务健康检查', url: 'http://localhost:8082/api/products/health' },
    { name: '商品服务测试', url: 'http://localhost:8082/api/products/test' }
  ];
  
  for (const test of tests) {
    try {
      const response = await axios.get(test.url, { timeout: 3000 });
      console.log(`✅ ${test.name}: ${response.status} - ${response.data}`);
    } catch (error) {
      if (error.code === 'ECONNREFUSED') {
        console.log(`❌ ${test.name}: 服务未启动`);
      } else {
        console.log(`❌ ${test.name}: ${error.response?.status || error.message}`);
      }
    }
  }
  
  console.log('\n💡 如果所有测试都失败，请：');
  console.log('1. 停止所有Java进程：taskkill /IM java.exe /F');
  console.log('2. 重新编译：mvn clean compile');
  console.log('3. 使用简化配置启动：mvn spring-boot:run -Dspring.profiles.active=simple');
}

if (require.main === module) {
  simpleTest();
}

module.exports = { simpleTest };