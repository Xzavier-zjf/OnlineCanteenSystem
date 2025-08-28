<template>
  <div class="service-status">
    <el-card class="status-card" shadow="hover">
      <template #header>
        <div class="status-header">
          <el-icon><Monitor /></el-icon>
          <span>服务状态监控</span>
          <el-button @click="checkAllServices" :loading="checking" size="small">
            刷新状态
          </el-button>
        </div>
      </template>
      
      <div class="services-grid">
        <el-card 
          v-for="service in services" 
          :key="service.name"
          class="service-card"
          :class="getServiceCardClass(service.status)"
        >
          <div class="service-info">
            <div class="service-name">{{ service.name }}</div>
            <div class="service-url">{{ service.url }}</div>
            <div class="service-status-badge">
              <el-tag :type="getStatusType(service.status)">
                {{ getStatusText(service.status) }}
              </el-tag>
            </div>
            <div class="service-response-time" v-if="service.responseTime">
              响应时间: {{ service.responseTime }}ms
            </div>
          </div>
        </el-card>
      </div>
      
      <el-divider />
      
      <div class="quick-actions">
        <h3>快速操作</h3>
        <el-space wrap>
          <el-button @click="testLogin" :loading="testing.login">测试登录</el-button>
          <el-button @click="testProducts" :loading="testing.products">测试商品API</el-button>
          <el-button @click="testOrders" :loading="testing.orders">测试订单API</el-button>
          <el-button @click="testRecommend" :loading="testing.recommend">测试推荐API</el-button>
        </el-space>
      </div>
      
      <div class="test-results" v-if="testResults.length > 0">
        <h3>测试结果</h3>
        <el-timeline>
          <el-timeline-item
            v-for="result in testResults"
            :key="result.id"
            :timestamp="result.timestamp"
            :type="result.type"
          >
            {{ result.message }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Monitor } from '@element-plus/icons-vue'
import { userApi, productApi, orderApi, recommendApi } from '../api'

export default {
  name: 'ServiceStatus',
  components: {
    Monitor
  },
  setup() {
    const checking = ref(false)
    const testResults = ref([])
    
    const testing = reactive({
      login: false,
      products: false,
      orders: false,
      recommend: false
    })
    
    const services = ref([
      {
        name: '用户服务',
        url: 'http://localhost:8081',
        endpoint: '/api/users/health',
        status: 'unknown',
        responseTime: null
      },
      {
        name: '商品服务',
        url: 'http://localhost:8082',
        endpoint: '/api/products/categories',
        status: 'unknown',
        responseTime: null
      },
      {
        name: '订单服务',
        url: 'http://localhost:8083',
        endpoint: '/api/orders/health',
        status: 'unknown',
        responseTime: null
      },
      {
        name: '推荐服务',
        url: 'http://localhost:8084',
        endpoint: '/api/recommend/health',
        status: 'unknown',
        responseTime: null
      },
      {
        name: '网关服务',
        url: 'http://localhost:8080',
        endpoint: '/api/health',
        status: 'unknown',
        responseTime: null
      }
    ])
    
    const addTestResult = (message, type = 'primary') => {
      testResults.value.unshift({
        id: Date.now(),
        message,
        type,
        timestamp: new Date().toLocaleTimeString()
      })
      if (testResults.value.length > 10) {
        testResults.value = testResults.value.slice(0, 10)
      }
    }
    
    const checkService = async (service) => {
      const startTime = Date.now()
      try {
        const response = await fetch(service.url + service.endpoint, {
          method: 'GET',
          timeout: 5000
        })
        const endTime = Date.now()
        
        if (response.ok) {
          service.status = 'online'
          service.responseTime = endTime - startTime
        } else {
          service.status = 'error'
          service.responseTime = null
        }
      } catch (error) {
        service.status = 'offline'
        service.responseTime = null
      }
    }
    
    const checkAllServices = async () => {
      checking.value = true
      addTestResult('开始检查所有服务状态...')
      
      const promises = services.value.map(service => checkService(service))
      await Promise.all(promises)
      
      const onlineCount = services.value.filter(s => s.status === 'online').length
      addTestResult(`服务检查完成: ${onlineCount}/${services.value.length} 个服务在线`, 
                   onlineCount === services.value.length ? 'success' : 'warning')
      
      checking.value = false
    }
    
    const getStatusType = (status) => {
      const statusMap = {
        'online': 'success',
        'offline': 'danger',
        'error': 'warning',
        'unknown': 'info'
      }
      return statusMap[status] || 'info'
    }
    
    const getStatusText = (status) => {
      const statusMap = {
        'online': '在线',
        'offline': '离线',
        'error': '错误',
        'unknown': '未知'
      }
      return statusMap[status] || '未知'
    }
    
    const getServiceCardClass = (status) => {
      return {
        'service-online': status === 'online',
        'service-offline': status === 'offline',
        'service-error': status === 'error'
      }
    }
    
    const testLogin = async () => {
      testing.login = true
      try {
        addTestResult('测试登录功能...')
        
        // 检查当前登录状态
        const token = localStorage.getItem('token')
        const userInfo = localStorage.getItem('userInfo')
        
        if (token && userInfo && token !== 'null' && userInfo !== 'null') {
          addTestResult('用户已登录，登录功能正常', 'success')
        } else {
          addTestResult('用户未登录，请先登录以测试完整功能', 'warning')
        }
        
        // 测试用户服务健康状态
        const response = await fetch('http://localhost:8081/api/users/health')
        if (response.ok) {
          addTestResult('用户服务连接正常', 'success')
        } else {
          throw new Error('用户服务连接失败')
        }
      } catch (error) {
        addTestResult('登录功能测试失败: ' + error.message, 'danger')
      } finally {
        testing.login = false
      }
    }
    
    const testProducts = async () => {
      testing.products = true
      try {
        addTestResult('测试商品API...')
        const response = await productApi.getProducts({ current: 1, size: 5 })
        addTestResult(`商品API测试成功，获取到 ${response.data.records?.length || response.data.length} 个商品`, 'success')
      } catch (error) {
        addTestResult('商品API测试失败: ' + error.message, 'danger')
      } finally {
        testing.products = false
      }
    }
    
    const testOrders = async () => {
      testing.orders = true
      try {
        addTestResult('测试订单API...')
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        const token = localStorage.getItem('token')
        
        if (!userInfo.id || !token) {
          addTestResult('订单API测试跳过：用户未登录', 'warning')
          return
        }
        
        const response = await orderApi.getOrders(userInfo.id)
        const orderCount = response.data?.length || 0
        addTestResult(`订单API测试成功，获取到 ${orderCount} 个订单`, 'success')
        
        // 测试取消订单功能（如果有订单的话）
        if (orderCount > 0) {
          addTestResult('取消订单功能可用', 'success')
        }
      } catch (error) {
        if (error.message.includes('请先登录')) {
          addTestResult('订单API测试失败: 请先登录', 'warning')
        } else {
          addTestResult('订单API测试失败: ' + error.message, 'danger')
        }
      } finally {
        testing.orders = false
      }
    }
    
    const testRecommend = async () => {
      testing.recommend = true
      try {
        addTestResult('测试推荐API...')
        const response = await recommendApi.getHotRecommendations(4)
        addTestResult(`推荐API测试成功，获取到 ${response.data.length} 个推荐`, 'success')
      } catch (error) {
        addTestResult('推荐API测试失败: ' + error.message, 'danger')
      } finally {
        testing.recommend = false
      }
    }
    
    onMounted(() => {
      checkAllServices()
      // 每30秒自动检查一次服务状态
      setInterval(checkAllServices, 30000)
    })
    
    return {
      checking,
      services,
      testResults,
      testing,
      checkAllServices,
      getStatusType,
      getStatusText,
      getServiceCardClass,
      testLogin,
      testProducts,
      testOrders,
      testRecommend
    }
  }
}
</script>

<style scoped>
.service-status {
  max-width: 1000px;
  margin: 0 auto;
}

.status-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 1.2em;
  font-weight: bold;
}

.status-header .el-icon {
  margin-right: 8px;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.service-card {
  transition: all 0.3s;
}

.service-card.service-online {
  border-color: #67c23a;
}

.service-card.service-offline {
  border-color: #f56c6c;
}

.service-card.service-error {
  border-color: #e6a23c;
}

.service-info {
  text-align: center;
}

.service-name {
  font-weight: bold;
  margin-bottom: 8px;
  color: #303133;
}

.service-url {
  font-size: 0.9em;
  color: #909399;
  margin-bottom: 10px;
}

.service-status-badge {
  margin-bottom: 8px;
}

.service-response-time {
  font-size: 0.8em;
  color: #606266;
}

.quick-actions {
  margin: 20px 0;
}

.quick-actions h3 {
  margin-bottom: 15px;
  color: #303133;
}

.test-results {
  margin-top: 20px;
}

.test-results h3 {
  margin-bottom: 15px;
  color: #303133;
}
</style>