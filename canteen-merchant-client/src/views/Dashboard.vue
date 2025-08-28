<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32" color="#409EFF"><ShoppingBag /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ todayOrders }}</h3>
              <p>今日订单</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32" color="#67C23A"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <h3>¥{{ todayRevenue }}</h3>
              <p>今日营收</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32" color="#E6A23C"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ pendingOrders }}</h3>
              <p>待处理订单</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32" color="#F56C6C"><Star /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ totalProducts }}</h3>
              <p>商品总数</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 待处理订单 -->
      <el-col :span="12">
        <el-card class="order-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>待处理订单</span>
              <el-button type="text" @click="$router.push('/orders')">查看全部</el-button>
            </div>
          </template>
          
          <div v-loading="loading">
            <div v-if="recentOrders.length === 0" class="empty-content">
              <el-empty description="暂无待处理订单" />
            </div>
            <div v-else>
              <div class="order-item" v-for="order in recentOrders" :key="order.id">
                <div class="order-info">
                  <h4>{{ order.orderNo }}</h4>
                  <p>{{ formatTime(order.createTime) }}</p>
                </div>
                <div class="order-amount">
                  ¥{{ order.totalAmount }}
                </div>
                <div class="order-actions">
                  <el-button type="primary" size="small" @click="processOrder(order.id)">
                    处理
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 热门商品 -->
      <el-col :span="12">
        <el-card class="product-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>热门商品</span>
              <el-button type="text" @click="$router.push('/products')">管理商品</el-button>
            </div>
          </template>
          
          <div v-loading="loading">
            <div v-if="hotProducts.length === 0" class="empty-content">
              <el-empty description="暂无商品数据" />
            </div>
            <div v-else>
              <div class="product-item" v-for="product in hotProducts" :key="product.id">
                <img :src="product.imageUrl || '/placeholder.jpg'" class="product-image" />
                <div class="product-info">
                  <h4>{{ product.name }}</h4>
                  <p class="product-price">¥{{ product.price }}</p>
                </div>
                <div class="product-sales">
                  销量: {{ product.orderCount || 0 }}
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingBag, Money, Clock, Star } from '@element-plus/icons-vue'

export default {
  name: 'Dashboard',
  components: {
    ShoppingBag, Money, Clock, Star
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const todayOrders = ref(0)
    const todayRevenue = ref(0)
    const pendingOrders = ref(0)
    const totalProducts = ref(0)
    const recentOrders = ref([])
    const hotProducts = ref([])
    
    const loadDashboardData = async () => {
      try {
        loading.value = true
        
        // 模拟数据加载
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 设置模拟数据
        todayOrders.value = 28
        todayRevenue.value = 1256.50
        pendingOrders.value = 5
        totalProducts.value = 45
        
        recentOrders.value = [
          {
            id: 1,
            orderNo: 'CT20250827001',
            createTime: new Date().toISOString(),
            totalAmount: 35.00
          },
          {
            id: 2,
            orderNo: 'CT20250827002',
            createTime: new Date(Date.now() - 300000).toISOString(),
            totalAmount: 28.50
          },
          {
            id: 3,
            orderNo: 'CT20250827003',
            createTime: new Date(Date.now() - 600000).toISOString(),
            totalAmount: 42.00
          }
        ]
        
        hotProducts.value = [
          {
            id: 1,
            name: '红烧肉盖饭',
            price: 15.00,
            imageUrl: 'https://example.com/hongshaorou.jpg',
            orderCount: 156
          },
          {
            id: 3,
            name: '宫保鸡丁',
            price: 18.00,
            imageUrl: 'https://example.com/gongbaojiding.jpg',
            orderCount: 142
          },
          {
            id: 5,
            name: '麻婆豆腐',
            price: 12.00,
            imageUrl: 'https://example.com/mapodoufu.jpg',
            orderCount: 128
          }
        ]
        
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        loading.value = false
      }
    }
    
    const formatTime = (timeStr) => {
      return new Date(timeStr).toLocaleString('zh-CN')
    }
    
    const processOrder = (orderId) => {
      router.push(`/orders/${orderId}`)
    }
    
    onMounted(() => {
      loadDashboardData()
    })
    
    return {
      loading,
      todayOrders,
      todayRevenue,
      pendingOrders,
      totalProducts,
      recentOrders,
      hotProducts,
      formatTime,
      processOrder
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  margin-right: 15px;
}

.stat-info h3 {
  margin: 0 0 5px 0;
  font-size: 1.8em;
  color: #303133;
}

.stat-info p {
  margin: 0;
  color: #909399;
  font-size: 0.9em;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.empty-content {
  text-align: center;
  padding: 20px 0;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.order-item:last-child {
  border-bottom: none;
}

.order-info {
  flex: 1;
}

.order-info h4 {
  margin: 0 0 5px 0;
  color: #303133;
}

.order-info p {
  margin: 0;
  color: #909399;
  font-size: 0.9em;
}

.order-amount {
  color: #E6A23C;
  font-weight: bold;
  margin-right: 15px;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.product-item:last-child {
  border-bottom: none;
}

.product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
}

.product-info {
  flex: 1;
}

.product-info h4 {
  margin: 0 0 5px 0;
  color: #303133;
}

.product-price {
  margin: 0;
  color: #E6A23C;
  font-weight: bold;
}

.product-sales {
  color: #909399;
  font-size: 0.9em;
}
</style>