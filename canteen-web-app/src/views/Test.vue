<template>
  <div class="test-page">
    <el-card class="test-card" shadow="hover">
      <template #header>
        <div class="test-header">
          <el-icon><Tools /></el-icon>
          <span>系统功能测试</span>
        </div>
      </template>
      
      <div class="test-content">
        <h3>1. 用户信息测试</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="Token状态">
            <el-tag :type="tokenStatus.type">{{ tokenStatus.text }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">
            {{ userInfo.id || '未获取' }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ userInfo.username || '未获取' }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ userInfo.email || '未获取' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        
        <h3>2. API连接测试</h3>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-button @click="testProductApi" :loading="testing.product">
              测试商品API
            </el-button>
          </el-col>
          <el-col :span="6">
            <el-button @click="testOrderApi" :loading="testing.order">
              测试订单API
            </el-button>
          </el-col>
          <el-col :span="6">
            <el-button @click="testRecommendApi" :loading="testing.recommend">
              测试推荐API
            </el-button>
          </el-col>
          <el-col :span="6">
            <el-button @click="testUserApi" :loading="testing.user">
              测试用户API
            </el-button>
          </el-col>
        </el-row>
        
        <el-divider />
        
        <h3>3. 图片资源测试</h3>
        <el-row :gutter="20">
          <el-col :span="4" v-for="image in testImages" :key="image.name">
            <el-card class="image-test-card">
              <img :src="image.url" @load="onImageLoad(image)" @error="onImageError(image)" class="test-image" />
              <div class="image-status">
                <span>{{ image.name }}</span>
                <el-tag :type="image.status === 'loaded' ? 'success' : image.status === 'error' ? 'danger' : 'info'">
                  {{ image.status }}
                </el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-divider />
        
        <h3>4. 搜索功能测试</h3>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input 
              v-model="searchKeyword" 
              placeholder="输入搜索关键词" 
              @keyup.enter="testSearch"
            />
          </el-col>
          <el-col :span="4">
            <el-button @click="testSearch" :loading="testing.search">
              测试搜索
            </el-button>
          </el-col>
          <el-col :span="12">
            <div v-if="searchResult">
              搜索结果: {{ searchResult.total }} 个商品
            </div>
          </el-col>
        </el-row>
        
        <el-divider />
        
        <h3>5. 测试结果</h3>
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
import { Tools } from '@element-plus/icons-vue'
import { productApi, orderApi, recommendApi, userApi } from '../api'

export default {
  name: 'Test',
  components: {
    Tools
  },
  setup() {
    const userInfo = ref({})
    const testLogs = ref([])
    const searchKeyword = ref('米饭')
    const searchResult = ref(null)
    
    const testing = reactive({
      product: false,
      order: false,
      recommend: false,
      user: false,
      search: false
    })
    
    const testImages = ref([
      { name: '米饭', url: '/images/products/rice.jpg', status: 'loading' },
      { name: '牛肉面', url: '/images/products/beef_noodle.jpg', status: 'loading' },
      { name: '小笼包', url: '/images/products/xiaolongbao.jpg', status: 'loading' },
      { name: '占位图', url: '/images/products/placeholder.svg', status: 'loading' }
    ])
    
    const tokenStatus = computed(() => {
      const token = localStorage.getItem('token')
      if (!token || token === 'null' || token === 'undefined') {
        return { type: 'danger', text: '未登录' }
      }
      return { type: 'success', text: '已登录' }
    })
    
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
    
    const testProductApi = async () => {
      testing.product = true
      try {
        const response = await productApi.getProducts({ current: 1, size: 5 })
        addLog(`商品API测试成功，获取到 ${response.data.records.length} 个商品`, 'success')
      } catch (error) {
        addLog('商品API测试失败: ' + error.message, 'danger')
      } finally {
        testing.product = false
      }
    }
    
    const testOrderApi = async () => {
      testing.order = true
      try {
        if (!userInfo.value.id) {
          addLog('订单API测试跳过：用户未登录', 'warning')
          return
        }
        const response = await orderApi.getOrders(userInfo.value.id)
        addLog(`订单API测试成功，获取到 ${response.data.length} 个订单`, 'success')
      } catch (error) {
        addLog('订单API测试失败: ' + error.message, 'danger')
      } finally {
        testing.order = false
      }
    }
    
    const testRecommendApi = async () => {
      testing.recommend = true
      try {
        const response = await recommendApi.getHotRecommendations(4)
        addLog(`推荐API测试成功，获取到 ${response.data.length} 个推荐商品`, 'success')
      } catch (error) {
        addLog('推荐API测试失败: ' + error.message, 'danger')
      } finally {
        testing.recommend = false
      }
    }
    
    const testUserApi = async () => {
      testing.user = true
      try {
        if (!userInfo.value.id) {
          addLog('用户API测试跳过：用户未登录', 'warning')
          return
        }
        const response = await userApi.getUserInfo()
        addLog('用户API测试成功', 'success')
      } catch (error) {
        addLog('用户API测试失败: ' + error.message, 'danger')
      } finally {
        testing.user = false
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
    
    const onImageLoad = (image) => {
      image.status = 'loaded'
      addLog(`图片加载成功: ${image.name}`, 'success')
    }
    
    const onImageError = (image) => {
      image.status = 'error'
      addLog(`图片加载失败: ${image.name}`, 'danger')
    }
    
    onMounted(() => {
      loadUserInfo()
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
      testProductApi,
      testOrderApi,
      testRecommendApi,
      testUserApi,
      testSearch,
      onImageLoad,
      onImageError
    }
  }
}
</script>

<style scoped>
.test-page {
  max-width: 1000px;
  margin: 0 auto;
}

.test-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.2em;
  font-weight: bold;
}

.test-content h3 {
  margin: 20px 0 15px 0;
  color: #303133;
}

.image-test-card {
  margin-bottom: 10px;
}

.test-image {
  width: 100%;
  height: 100px;
  object-fit: cover;
}

.image-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  font-size: 0.9em;
}
</style>