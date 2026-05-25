<template>
  <div class="test-page">
    <el-card class="test-card" shadow="hover">
      <template #header>
        <div class="test-header">
          <el-icon><Tools /></el-icon>
          <span>食堂订餐系统测试中心</span>
          <el-button @click="runAllTests" :loading="runningAllTests" type="primary" size="small" style="margin-left: auto;">
            一键测试所有功能
          </el-button>
        </div>
      </template>
      
      <div class="test-content">
        <!-- 系统状态概览 -->
        <h3>🏥 系统状态概览</h3>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-item">
                <el-icon class="status-icon" :class="systemStatus.frontend.status"><Monitor /></el-icon>
                <div>
                  <div class="status-title">前端服务</div>
                  <div class="status-value">{{ systemStatus.frontend.text }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-item">
                <el-icon class="status-icon" :class="systemStatus.gateway.status"><Link /></el-icon>
                <div>
                  <div class="status-title">网关服务</div>
                  <div class="status-value">{{ systemStatus.gateway.text }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-item">
                <el-icon class="status-icon" :class="systemStatus.backend.status"><Setting /></el-icon>
                <div>
                  <div class="status-title">后端服务</div>
                  <div class="status-value">{{ systemStatus.backend.text }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-item">
                <el-icon class="status-icon" :class="systemStatus.database.status"><Coin /></el-icon>
                <div>
                  <div class="status-title">数据库</div>
                  <div class="status-value">{{ systemStatus.database.text }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-divider />
        
        <!-- 用户认证测试 -->
        <h3>👤 用户认证测试</h3>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="登录状态">
            <el-tag :type="tokenStatus.type">{{ tokenStatus.text }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">
            {{ userInfo.id || '未获取' }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ userInfo.username || '未获取' }}
          </el-descriptions-item>
          <el-descriptions-item label="用户角色">
            <el-tag v-if="userInfo.role" :type="getRoleType(userInfo.role)">{{ userInfo.role }}</el-tag>
            <span v-else>未获取</span>
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ userInfo.email || '未获取' }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ userInfo.createTime || '未获取' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        
        <!-- 核心API测试 -->
        <h3>🔌 核心API测试</h3>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-button @click="testUserApi" :loading="testing.user" type="primary" style="width: 100%;">
              <el-icon><User /></el-icon>
              用户服务API
            </el-button>
          </el-col>
          <el-col :span="6">
            <el-button @click="testProductApi" :loading="testing.product" type="success" style="width: 100%;">
              <el-icon><ShoppingBag /></el-icon>
              商品服务API
            </el-button>
          </el-col>
          <el-col :span="6">
            <el-button @click="testOrderApi" :loading="testing.order" type="warning" style="width: 100%;">
              <el-icon><Document /></el-icon>
              订单服务API
            </el-button>
          </el-col>
          <el-col :span="6">
            <el-button @click="testRecommendApi" :loading="testing.recommend" type="info" style="width: 100%;">
              <el-icon><Star /></el-icon>
              推荐服务API
            </el-button>
          </el-col>
        </el-row>
        
        <el-divider />
        
        <!-- 管理员功能测试 -->
        <h3>⚙️ 管理员功能测试</h3>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-button @click="testAdminApi" :loading="testing.admin" type="danger" style="width: 100%;">
              <el-icon><Setting /></el-icon>
              系统设置API
            </el-button>
          </el-col>
          <el-col :span="6">
            <el-button @click="testSystemStats" :loading="testing.stats" type="danger" style="width: 100%;">
              <el-icon><PieChart /></el-icon>
              系统统计API
            </el-button>
          </el-col>
          <el-col :span="6">
            <el-button @click="testBackupApi" :loading="testing.backup" type="danger" style="width: 100%;">
              <el-icon><Download /></el-icon>
              数据备份API
            </el-button>
          </el-col>
          <el-col :span="6">
            <el-button @click="testLogsApi" :loading="testing.logs" type="danger" style="width: 100%;">
              <el-icon><Document /></el-icon>
              系统日志API
            </el-button>
          </el-col>
        </el-row>
        
        <el-divider />
        
        <!-- 业务功能测试 -->
        <h3>🍽️ 业务功能测试</h3>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="business-test-card">
              <h4>商品搜索测试</h4>
              <el-input 
                v-model="searchKeyword" 
                placeholder="输入搜索关键词（如：米饭、面条）" 
                @keyup.enter="testSearch"
              />
              <el-button @click="testSearch" :loading="testing.search" type="primary" style="margin-top: 10px; width: 100%;">
                测试搜索功能
              </el-button>
              <div v-if="searchResult" class="search-result">
                找到 {{ searchResult.total }} 个商品
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="business-test-card">
              <h4>分类筛选测试</h4>
              <el-select v-model="selectedCategory" placeholder="选择商品分类" style="width: 100%;">
                <el-option label="全部分类" value="all"></el-option>
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id"></el-option>
              </el-select>
              <el-button @click="testCategoryFilter" :loading="testing.category" type="success" style="margin-top: 10px; width: 100%;">
                测试分类筛选
              </el-button>
              <div v-if="categoryResult" class="search-result">
                该分类下有 {{ categoryResult.total }} 个商品
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="business-test-card">
              <h4>订单流程测试</h4>
              <el-button @click="testOrderFlow" :loading="testing.orderFlow" type="warning" style="width: 100%;">
                模拟完整订单流程
              </el-button>
              <div v-if="orderFlowResult" class="search-result">
                {{ orderFlowResult }}
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-divider />
        
        <!-- 图片资源测试 -->
        <h3>🖼️ 图片资源测试</h3>
        <div class="image-test-section">
          <div class="image-test-controls">
            <el-button @click="testAllImages" :loading="testing.images" type="primary" size="small">
              <el-icon><PieChart /></el-icon>
              测试所有图片
            </el-button>
            <el-button @click="resetImageStatus" size="small">重置状态</el-button>
            <div class="image-stats">
              <el-tag type="success">成功: {{ imageStats.loaded }}</el-tag>
              <el-tag type="danger">失败: {{ imageStats.error }}</el-tag>
              <el-tag type="info">待测: {{ imageStats.loading }}</el-tag>
            </div>
          </div>
          <el-row :gutter="15">
            <el-col :span="3" v-for="image in testImages" :key="image.name">
              <el-card class="image-test-card">
                <img :src="image.url" @load="onImageLoad(image)" @error="onImageError(image)" class="test-image" />
                <div class="image-status">
                  <div class="image-name">{{ image.name }}</div>
                  <el-tag size="small" :type="image.status === 'loaded' ? 'success' : image.status === 'error' ? 'danger' : 'info'">
                    {{ getImageStatusText(image.status) }}
                  </el-tag>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <el-divider />
        
        <!-- 性能测试 -->
        <h3>⚡ 性能测试</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <h4>API响应时间测试</h4>
              <el-button @click="testApiPerformance" :loading="testing.performance" type="primary">
                开始性能测试
              </el-button>
              <div v-if="performanceResult" class="performance-result">
                <div v-for="(time, api) in performanceResult" :key="api">
                  {{ api }}: {{ time }}ms
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <h4>并发测试</h4>
              <el-button @click="testConcurrency" :loading="testing.concurrency" type="warning">
                测试并发请求
              </el-button>
              <div v-if="concurrencyResult" class="performance-result">
                {{ concurrencyResult }}
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-divider />
        
        <!-- 测试日志 -->
        <h3>📋 测试日志</h3>
        <div class="log-controls">
          <el-button @click="clearLogs" size="small">清空日志</el-button>
          <el-button @click="exportLogs" size="small" type="primary">导出日志</el-button>
        </div>
        <el-timeline>
          <el-timeline-item
            v-for="log in testLogs"
            :key="log.id"
            :timestamp="log.timestamp"
            :type="log.type"
          >
            {{ log.message }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Tools, Monitor, Link, Coin, User, ShoppingBag, 
  Document, Star, Setting, PieChart, Download 
} from '@element-plus/icons-vue'
import { productApi, orderApi, recommendApi, userApi } from '../api'
import settingsApi from '../api/settings'

export default {
  name: 'Test',
  components: {
    Tools, Monitor, Link, Coin, User, ShoppingBag, 
    Document, Star, Setting, PieChart, Download
  },
  setup() {
    const userInfo = ref({})
    const testLogs = ref([])
    const searchKeyword = ref('米饭')
    const searchResult = ref(null)
    const selectedCategory = ref('all')
    const categories = ref([])
    const categoryResult = ref(null)
    const orderFlowResult = ref('')
    const performanceResult = ref(null)
    const concurrencyResult = ref('')
    const runningAllTests = ref(false)
    
    const testing = reactive({
      product: false,
      order: false,
      recommend: false,
      user: false,
      search: false,
      admin: false,
      stats: false,
      backup: false,
      logs: false,
      category: false,
      orderFlow: false,
      performance: false,
      concurrency: false,
      images: false
    })
    
    const systemStatus = reactive({
      frontend: { status: 'success', text: '运行中' },
      gateway: { status: 'info', text: '检测中' },
      backend: { status: 'info', text: '检测中' },
      database: { status: 'info', text: '检测中' }
    })
    
    // 获取基础URL
    const getImageUrl = (filename) => {
      // 构建完整的绝对URL，避免路径混淆
      return `${window.location.origin}/images/products/${filename}`
    }
    
    const testImages = ref([
      { name: '红烧肉套餐', url: getImageUrl('hongshaorou_rice.jpg'), status: 'loading' },
      { name: '牛肉面', url: getImageUrl('beef_noodle.jpg'), status: 'loading' },
      { name: '小笼包', url: getImageUrl('xiaolongbao.jpg'), status: 'loading' },
      { name: '宫保鸡丁', url: getImageUrl('gongbao_dish.jpg'), status: 'loading' },
      { name: '兰州拉面', url: getImageUrl('lanzhou_noodle.jpg'), status: 'loading' },
      { name: '麻婆豆腐', url: getImageUrl('mapo_tofu.jpg'), status: 'loading' },
      { name: '糖醋里脊', url: getImageUrl('tangcu_liji.jpg'), status: 'loading' },
      { name: '回锅肉', url: getImageUrl('huiguorou_dish.jpg'), status: 'loading' },
      { name: '豆浆油条', url: getImageUrl('doujiang_youtiao.jpg'), status: 'loading' },
      { name: '学生套餐', url: getImageUrl('student_set.jpg'), status: 'loading' },
      { name: '柠檬茶', url: getImageUrl('lemon_tea.jpg'), status: 'loading' },
      { name: '占位图', url: getImageUrl('placeholder.jpg'), status: 'loading' }
    ])
    
    const tokenStatus = computed(() => {
      const token = localStorage.getItem('token')
      if (!token || token === 'null' || token === 'undefined') {
        return { type: 'danger', text: '未登录' }
      }
      return { type: 'success', text: '已登录' }
    })
    
    const getRoleType = (role) => {
      const roleMap = {
        'admin': 'danger',
        'merchant': 'warning', 
        'student': 'primary',
        'teacher': 'success'
      }
      return roleMap[role] || 'info'
    }
    
    const imageStats = computed(() => {
      const stats = { loaded: 0, error: 0, loading: 0 }
      testImages.value.forEach(image => {
        stats[image.status]++
      })
      return stats
    })
    
    const getImageStatusText = (status) => {
      const statusMap = {
        'loaded': '成功',
        'error': '失败',
        'loading': '待测'
      }
      return statusMap[status] || '未知'
    }
    
    const addLog = (message, type = 'primary') => {
      testLogs.value.unshift({
        id: Date.now(),
        message,
        type,
        timestamp: new Date().toLocaleTimeString()
      })
    }
    
    const loadUserInfo = () => {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr && userInfoStr !== 'undefined' && userInfoStr !== 'null') {
        try {
          const parsed = JSON.parse(userInfoStr)
          if (parsed && typeof parsed === 'object') {
            userInfo.value = parsed
            addLog('用户信息加载成功', 'success')
          } else {
            addLog('用户信息格式无效', 'warning')
          }
        } catch (e) {
          addLog('用户信息解析失败: ' + e.message, 'danger')
        }
      } else {
        addLog('未找到用户信息', 'warning')
      }
    }
    
    // 检查系统状态
    const checkSystemStatus = async () => {
      // 检查网关
      try {
        const response = await fetch('/api/health')
        systemStatus.gateway = response.ok ? 
          { status: 'success', text: '运行正常' } : 
          { status: 'danger', text: '连接失败' }
      } catch {
        systemStatus.gateway = { status: 'danger', text: '连接失败' }
      }
      
      // 检查后端服务
      try {
        await userApi.getProfile()
        systemStatus.backend = { status: 'success', text: '运行正常' }
        systemStatus.database = { status: 'success', text: '连接正常' }
      } catch {
        systemStatus.backend = { status: 'warning', text: '部分异常' }
        systemStatus.database = { status: 'warning', text: '连接异常' }
      }
    }

    const testUserApi = async () => {
      testing.user = true
      const startTime = Date.now()
      try {
        const response = await userApi.getProfile()
        const responseTime = Date.now() - startTime
        addLog(`用户API测试成功 (${responseTime}ms)`, 'success')
        return responseTime
      } catch (error) {
        addLog('用户API测试失败: ' + error.message, 'danger')
        return null
      } finally {
        testing.user = false
      }
    }
    
    const testProductApi = async () => {
      testing.product = true
      const startTime = Date.now()
      try {
        const response = await productApi.getProducts({ current: 1, size: 5 })
        const responseTime = Date.now() - startTime
        addLog(`商品API测试成功，获取到 ${response.data.records.length} 个商品 (${responseTime}ms)`, 'success')
        return responseTime
      } catch (error) {
        addLog('商品API测试失败: ' + error.message, 'danger')
        return null
      } finally {
        testing.product = false
      }
    }
    
    const testOrderApi = async () => {
      testing.order = true
      const startTime = Date.now()
      try {
        const response = await orderApi.getOrders()
        const responseTime = Date.now() - startTime
        addLog(`订单API测试成功，获取到 ${response.data?.length || 0} 个订单 (${responseTime}ms)`, 'success')
        return responseTime
      } catch (error) {
        addLog('订单API测试失败: ' + error.message, 'danger')
        return null
      } finally {
        testing.order = false
      }
    }
    
    const testRecommendApi = async () => {
      testing.recommend = true
      const startTime = Date.now()
      try {
        const response = await recommendApi.getHotRecommendations(4)
        const responseTime = Date.now() - startTime
        addLog(`推荐API测试成功，获取到 ${response.data.length} 个推荐商品 (${responseTime}ms)`, 'success')
        return responseTime
      } catch (error) {
        addLog('推荐API测试失败: ' + error.message, 'danger')
        return null
      } finally {
        testing.recommend = false
      }
    }
    
    const testAdminApi = async () => {
      testing.admin = true
      try {
        const response = await settingsApi.getSystemSettings()
        addLog('管理员系统设置API测试成功', 'success')
      } catch (error) {
        addLog('管理员系统设置API测试失败: ' + error.message, 'danger')
      } finally {
        testing.admin = false
      }
    }
    
    const testSystemStats = async () => {
      testing.stats = true
      try {
        const response = await settingsApi.getSystemStats()
        addLog('系统统计API测试成功', 'success')
      } catch (error) {
        addLog('系统统计API测试失败: ' + error.message, 'danger')
      } finally {
        testing.stats = false
      }
    }
    
    const testBackupApi = async () => {
      testing.backup = true
      try {
        const response = await settingsApi.backupData()
        addLog('数据备份API测试成功', 'success')
      } catch (error) {
        addLog('数据备份API测试失败: ' + error.message, 'danger')
      } finally {
        testing.backup = false
      }
    }
    
    const testLogsApi = async () => {
      testing.logs = true
      try {
        const response = await settingsApi.clearLogs()
        addLog('系统日志API测试成功', 'success')
      } catch (error) {
        addLog('系统日志API测试失败: ' + error.message, 'danger')
      } finally {
        testing.logs = false
      }
    }
    
    const testSearch = async () => {
      testing.search = true
      try {
        const response = await productApi.getProducts({
          keyword: searchKeyword.value,
          current: 1,
          size: 10
        })
        searchResult.value = response.data
        addLog(`搜索功能测试成功，关键词"${searchKeyword.value}"找到 ${response.data.total} 个结果`, 'success')
      } catch (error) {
        addLog('搜索功能测试失败: ' + error.message, 'danger')
      } finally {
        testing.search = false
      }
    }
    
    const testCategoryFilter = async () => {
      testing.category = true
      try {
        const response = await productApi.getProducts({
          categoryId: selectedCategory.value,
          current: 1,
          size: 20
        })
        categoryResult.value = response.data
        addLog(`分类筛选测试成功，分类"${selectedCategory.value}"下有 ${response.data.total} 个商品`, 'success')
      } catch (error) {
        addLog('分类筛选测试失败: ' + error.message, 'danger')
      } finally {
        testing.category = false
      }
    }
    
    const testOrderFlow = async () => {
      testing.orderFlow = true
      try {
        // 模拟订单流程：获取商品 -> 创建订单 -> 查看订单
        addLog('开始模拟订单流程...', 'info')
        
        // 1. 获取商品
        const products = await productApi.getProducts({ current: 1, size: 1 })
        if (products.data.records.length === 0) {
          throw new Error('没有可用商品')
        }
        
        addLog('✓ 获取商品成功', 'success')
        
        // 2. 模拟创建订单（这里只是模拟，不真正创建）
        const mockOrder = {
          productId: products.data.records[0].id,
          quantity: 1,
          totalAmount: products.data.records[0].price
        }
        
        addLog('✓ 模拟订单创建成功', 'success')
        
        // 3. 获取订单列表
        await orderApi.getOrders()
        addLog('✓ 订单查询成功', 'success')
        
        orderFlowResult.value = '订单流程测试完成'
        addLog('订单流程测试成功完成', 'success')
      } catch (error) {
        orderFlowResult.value = '订单流程测试失败'
        addLog('订单流程测试失败: ' + error.message, 'danger')
      } finally {
        testing.orderFlow = false
      }
    }
    
    const testApiPerformance = async () => {
      testing.performance = true
      try {
        addLog('开始API性能测试...', 'info')
        const results = {}
        
        // 测试各个API的响应时间
        const userTime = await testUserApi()
        if (userTime) results['用户API'] = userTime
        
        const productTime = await testProductApi()
        if (productTime) results['商品API'] = productTime
        
        const orderTime = await testOrderApi()
        if (orderTime) results['订单API'] = orderTime
        
        const recommendTime = await testRecommendApi()
        if (recommendTime) results['推荐API'] = recommendTime
        
        performanceResult.value = results
        addLog('API性能测试完成', 'success')
      } catch (error) {
        addLog('API性能测试失败: ' + error.message, 'danger')
      } finally {
        testing.performance = false
      }
    }
    
    const testConcurrency = async () => {
      testing.concurrency = true
      try {
        addLog('开始并发测试...', 'info')
        const startTime = Date.now()
        
        // 同时发起10个商品API请求
        const promises = Array(10).fill().map(() => 
          productApi.getProducts({ current: 1, size: 5 })
        )
        
        const results = await Promise.allSettled(promises)
        const successCount = results.filter(r => r.status === 'fulfilled').length
        const totalTime = Date.now() - startTime
        
        concurrencyResult.value = `10个并发请求，${successCount}个成功，总耗时${totalTime}ms`
        addLog(`并发测试完成：${successCount}/10 成功`, 'success')
      } catch (error) {
        addLog('并发测试失败: ' + error.message, 'danger')
      } finally {
        testing.concurrency = false
      }
    }
    
    const runAllTests = async () => {
      runningAllTests.value = true
      try {
        addLog('开始执行全面系统测试...', 'info')
        
        // 检查系统状态
        await checkSystemStatus()
        
        // 依次执行各项测试
        await testUserApi()
        await testProductApi()
        await testOrderApi()
        await testRecommendApi()
        await testAdminApi()
        await testSystemStats()
        await testSearch()
        
        addLog('全面系统测试完成', 'success')
        ElMessage.success('所有测试执行完成，请查看测试日志')
      } catch (error) {
        addLog('全面测试过程中出现错误: ' + error.message, 'danger')
      } finally {
        runningAllTests.value = false
      }
    }
    
    const onImageLoad = (image) => {
      image.status = 'loaded'
      addLog(`图片加载成功: ${image.name} (${image.url})`, 'success')
    }
    
    const onImageError = (image) => {
      image.status = 'error'
      const errorMsg = `图片加载失败: ${image.name} - URL: ${image.url}. 请确认该文件位于 'canteen-web-app/public/images/products/' 目录下。`
      addLog(errorMsg, 'danger')
      console.error(errorMsg)
    }
    
    const testAllImages = async () => {
      testing.images = true
      addLog('开始测试所有图片资源...', 'info')
      
      try {
        // 重置所有图片状态
        testImages.value.forEach(image => {
          image.status = 'loading'
        })
        
        // 创建图片加载Promise
        const imagePromises = testImages.value.map(image => {
          return new Promise((resolve) => {
            const img = new Image()
            img.onload = () => {
              image.status = 'loaded'
              addLog(`✓ ${image.name} 加载成功`, 'success')
              resolve({ name: image.name, status: 'loaded' })
            }
            img.onerror = () => {
              image.status = 'error'
              addLog(`✗ ${image.name} 加载失败`, 'danger')
              resolve({ name: image.name, status: 'error' })
            }
            img.src = image.url
          })
        })
        
        // 等待所有图片测试完成
        const results = await Promise.all(imagePromises)
        const successCount = results.filter(r => r.status === 'loaded').length
        const totalCount = results.length
        
        addLog(`图片测试完成: ${successCount}/${totalCount} 成功`, successCount === totalCount ? 'success' : 'warning')
      } catch (error) {
        addLog('图片测试过程中出现错误: ' + error.message, 'danger')
      } finally {
        testing.images = false
      }
    }
    
    const resetImageStatus = () => {
      testImages.value.forEach(image => {
        image.status = 'loading'
      })
      addLog('图片状态已重置', 'info')
    }
    
    const clearLogs = () => {
      testLogs.value = []
      addLog('测试日志已清空', 'info')
    }
    
    const exportLogs = () => {
      const logData = testLogs.value.map(log => 
        `[${log.timestamp}] ${log.type.toUpperCase()}: ${log.message}`
      ).join('\n')
      
      const blob = new Blob([logData], { type: 'text/plain' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `test-logs-${new Date().toISOString().slice(0, 10)}.txt`
      a.click()
      URL.revokeObjectURL(url)
      
      addLog('测试日志已导出', 'success')
    }
    
    const loadCategories = async () => {
      try {
        const response = await productApi.getCategories()
        categories.value = response.data
      } catch (error) {
        addLog('加载商品分类失败: ' + error.message, 'warning')
      }
    }
    
    onMounted(async () => {
      loadUserInfo()
      await loadCategories()
      await checkSystemStatus()
      addLog('测试页面初始化完成', 'success')
    })
    
    return {
      userInfo,
      tokenStatus,
      testLogs,
      testing,
      testImages,
      searchKeyword,
      searchResult,
      selectedCategory,
      categories,
      categoryResult,
      orderFlowResult,
      performanceResult,
      concurrencyResult,
      runningAllTests,
      systemStatus,
      imageStats,
      getRoleType,
      getImageStatusText,
      checkSystemStatus,
      testProductApi,
      testOrderApi,
      testRecommendApi,
      testUserApi,
      testAdminApi,
      testSystemStats,
      testBackupApi,
      testLogsApi,
      testSearch,
      testCategoryFilter,
      testOrderFlow,
      testApiPerformance,
      testConcurrency,
      runAllTests,
      onImageLoad,
      onImageError,
      testAllImages,
      resetImageStatus,
      clearLogs,
      exportLogs
    }
  }
}
</script>

<style scoped>
.test-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.test-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.3em;
  font-weight: bold;
  color: #409EFF;
}

.test-content h3 {
  margin: 25px 0 20px 0;
  color: #303133;
  font-size: 1.1em;
  border-left: 4px solid #409EFF;
  padding-left: 12px;
}

/* 系统状态卡片 */
.status-card {
  height: 100px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.status-item {
  display: flex;
  align-items: center;
  gap: 15px;
  height: 100%;
  padding: 15px;
}

.status-icon {
  font-size: 2em;
  transition: all 0.3s ease;
}

.status-icon.success {
  color: #67C23A;
}

.status-icon.warning {
  color: #E6A23C;
}

.status-icon.danger {
  color: #F56C6C;
}

.status-icon.info {
  color: #909399;
}

.status-title {
  font-size: 0.9em;
  color: #909399;
  margin-bottom: 5px;
}

.status-value {
  font-size: 1.1em;
  font-weight: bold;
  color: #303133;
}

/* 业务测试卡片 */
.business-test-card {
  height: 180px;
  padding: 20px;
}

.business-test-card h4 {
  margin: 0 0 15px 0;
  color: #409EFF;
  font-size: 1em;
}

.search-result {
  margin-top: 10px;
  padding: 8px 12px;
  background: #f0f9ff;
  border: 1px solid #409EFF;
  border-radius: 4px;
  color: #409EFF;
  font-size: 0.9em;
}

/* 图片测试区域 */
.image-test-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.image-test-controls {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.image-stats {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

/* 图片测试卡片 */
.image-test-card {
  margin-bottom: 15px;
  transition: all 0.3s ease;
  height: 180px;
}

.image-test-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.test-image {
  width: 100%;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.test-image:hover {
  transform: scale(1.05);
}

.image-status {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
  text-align: center;
}

.image-name {
  font-size: 0.85em;
  color: #303133;
  font-weight: 500;
  line-height: 1.2;
  min-height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 性能测试结果 */
.performance-result {
  margin-top: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.performance-result div {
  margin-bottom: 8px;
  padding: 5px 10px;
  background: white;
  border-radius: 4px;
  border-left: 3px solid #409EFF;
}

/* 日志控制按钮 */
.log-controls {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .test-page {
    padding: 10px;
  }
  
  .test-header {
    font-size: 1.1em;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .status-item {
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }
  
  .status-icon {
    font-size: 1.5em;
  }
  
  .business-test-card {
    height: auto;
    min-height: 150px;
  }
}

/* 动画效果 */
@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
  100% {
    opacity: 1;
  }
}

.el-button.is-loading {
  animation: pulse 1.5s infinite;
}

/* 测试结果标签样式 */
.el-tag {
  font-weight: 500;
}

/* 时间线样式优化 */
.el-timeline {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 10px;
}

.el-timeline::-webkit-scrollbar {
  width: 6px;
}

.el-timeline::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.el-timeline::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.el-timeline::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 描述列表样式优化 */
.el-descriptions {
  margin-bottom: 20px;
}

/* 按钮组样式 */
.el-row .el-col .el-button {
  transition: all 0.3s ease;
}

.el-row .el-col .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}
</style>