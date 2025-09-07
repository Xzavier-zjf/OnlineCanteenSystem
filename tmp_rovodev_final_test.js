const http = require('http');

// 测试前端会调用的具体API
const frontendTests = [
  { name: '登录测试', method: 'POST', path: '/api/users/users/login', body: '{"username":"admin","password":"admin123"}' },
  { name: '商品列表', method: 'GET', path: '/api/products/products?current=1&size=1' },
  { name: '商品统计', method: 'GET', path: '/api/products/products/stats' },
  { name: '订单列表', method: 'GET', path: '/api/orders/orders' },
  { name: '网关健康检查', method: 'GET', path: '/api/health' }
];

function testAPI(test) {
  return new Promise((resolve) => {
    const options = {
      hostname: 'localhost',
      port: 8080,
      path: test.path,
      method: test.method,
      timeout: 5000,
      headers: {
        'Content-Type': 'application/json'
      }
    };

    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => data += chunk);
      res.on('end', () => {
        resolve({
          name: test.name,
          status: res.statusCode,
          success: res.statusCode < 400,
          response: data.substring(0, 300)
        });
      });
    });

    req.on('error', (err) => {
      resolve({
        name: test.name,
        status: 'ERROR',
        success: false,
        response: err.message
      });
    });

    req.on('timeout', () => {
      req.destroy();
      resolve({
        name: test.name,
        status: 'TIMEOUT',
        success: false,
        response: 'Request timeout'
      });
    });

    if (test.body) {
      req.write(test.body);
    }
    req.end();
  });
}

async function runFinalTest() {
  console.log('=== 最终测试 - 前端API调用 ===\n');
  
  let successCount = 0;
  
  for (const test of frontendTests) {
    const result = await testAPI(test);
    const status = result.success ? '✅ 成功' : '❌ 失败';
    console.log(`${status} ${result.name}: ${result.status}`);
    
    if (result.success) {
      successCount++;
      console.log(`   ✓ 响应正常`);
    } else {
      console.log(`   ✗ 错误: ${result.response}`);
    }
    console.log('');
  }
  
  console.log(`=== 测试结果: ${successCount}/${frontendTests.length} 成功 ===`);
  
  if (successCount === frontendTests.length) {
    console.log('🎉 所有API测试通过！前端应用现在可以正常工作了。');
  } else {
    console.log('⚠️  部分API仍有问题，需要进一步检查。');
  }
}

runFinalTest().catch(console.error);