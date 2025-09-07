// 绕过网关的临时解决方案
const fs = require('fs');
const path = require('path');

function createBypassConfig() {
  console.log('🔧 创建绕过网关的临时配置...\n');
  
  // 修改Vite配置，直接代理到后端服务
  const viteConfigPath = path.join('canteen-web-app', 'vite.config.js');
  
  const newViteConfig = `import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3001,
    open: true,
    proxy: {
      // 直接代理到商品服务
      '/api/products': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\\/api\\/products/, '/api/products')
      },
      // 直接代理到用户服务
      '/api/users': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\\/api\\/users/, '/api/users')
      },
      // 直接代理到订单服务
      '/api/orders': {
        target: 'http://localhost:8083',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\\/api\\/orders/, '/api/orders')
      },
      // 直接代理到推荐服务
      '/api/recommend': {
        target: 'http://localhost:8084',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\\/api\\/recommend/, '/api/recommend')
      }
    }
  }
})`;

  try {
    // 备份原配置
    if (fs.existsSync(viteConfigPath)) {
      fs.copyFileSync(viteConfigPath, viteConfigPath + '.backup');
      console.log('✅ 已备份原始vite.config.js');
    }
    
    // 写入新配置
    fs.writeFileSync(viteConfigPath, newViteConfig);
    console.log('✅ 已创建绕过网关的vite.config.js');
    
    console.log('\n🚀 现在可以启动服务：');
    console.log('1. cd canteen-product-service && mvn spring-boot:run');
    console.log('2. cd canteen-web-app && npm run dev');
    console.log('\n💡 这样前端会直接访问后端服务，跳过有问题的网关');
    
  } catch (error) {
    console.error('❌ 创建配置失败:', error.message);
  }
}

function restoreOriginalConfig() {
  console.log('🔄 恢复原始配置...\n');
  
  const viteConfigPath = path.join('canteen-web-app', 'vite.config.js');
  const backupPath = viteConfigPath + '.backup';
  
  try {
    if (fs.existsSync(backupPath)) {
      fs.copyFileSync(backupPath, viteConfigPath);
      fs.unlinkSync(backupPath);
      console.log('✅ 已恢复原始vite.config.js');
    } else {
      console.log('❌ 未找到备份文件');
    }
  } catch (error) {
    console.error('❌ 恢复配置失败:', error.message);
  }
}

// 命令行参数处理
const args = process.argv.slice(2);
if (args.includes('--restore')) {
  restoreOriginalConfig();
} else {
  createBypassConfig();
}

module.exports = { createBypassConfig, restoreOriginalConfig };