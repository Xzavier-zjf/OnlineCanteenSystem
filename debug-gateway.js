// 调试网关问题的脚本
const axios = require('axios');

async function debugGateway() {
  console.log('🔍 调试网关问题...\n');
  
  // 1. 检查网关是否真的在运行
  console.log('1. 检查网关端口状态...');
  try {
    const response = await axios.get('http://localhost:8080', { timeout: 5000 });
    console.log('✅ 网关端口响应:', response.status);
  } catch (error) {
    console.log('❌ 网关端口问题:', error.message);
    if (error.code === 'ECONNREFUSED') {
      console.log('💡 网关服务未启动或端口被占用');
    } else if (error.response?.status === 502) {
      console.log('💡 网关启动了但内部有错误');
    } else if (error.response?.status === 404) {
      console.log('💡 网关启动了但没有根路径处理器');
    }
  }
  
  // 2. 尝试访问网关的健康检查
  console.log('\n2. 测试网关健康检查...');
  try {
    const healthResponse = await axios.get('http://localhost:8080/api/health', { timeout: 5000 });
    console.log('✅ 网关健康检查成功:', healthResponse.data);
  } catch (error) {
    console.log('❌ 网关健康检查失败:', error.message);
    console.log('状态码:', error.response?.status);
    console.log('响应数据:', error.response?.data);
  }
  
  // 3. 直接测试商品服务
  console.log('\n3. 直接测试商品服务...');
  try {
    const productResponse = await axios.get('http://localhost:8082/api/products/health', { timeout: 5000 });
    console.log('✅ 商品服务直接访问成功:', productResponse.data);
  } catch (error) {
    if (error.code === 'ECONNREFUSED') {
      console.log('❌ 商品服务未启动 (端口8082)');
    } else {
      console.log('❌ 商品服务问题:', error.message);
    }
  }
  
  // 4. 测试通过网关访问商品服务
  console.log('\n4. 测试通过网关访问商品服务...');
  try {
    const gatewayProductResponse = await axios.get('http://localhost:8080/api/products/health', { timeout: 5000 });
    console.log('✅ 通过网关访问商品服务成功:', gatewayProductResponse.data);
  } catch (error) {
    console.log('❌ 通过网关访问商品服务失败:', error.message);
    console.log('状态码:', error.response?.status);
  }
  
  console.log('\n📋 诊断建议:');
  console.log('1. 如果网关端口响应404，说明网关启动了但路由配置有问题');
  console.log('2. 如果网关端口响应502，说明网关内部有错误');
  console.log('3. 如果商品服务直接访问失败，需要先启动商品服务');
  console.log('4. 如果商品服务直接访问成功但通过网关失败，说明网关路由有问题');
}

if (require.main === module) {
  debugGateway();
}

module.exports = { debugGateway };