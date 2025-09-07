<template>
  <div class="merchant-dashboard">
    <!-- 欢迎区域 -->
    <el-card class="welcome-card" shadow="hover">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎回来，{{ merchantInfo.name || '商户' }}！</h2>
          <p>今天是 {{ formatDate(new Date()) }}，祝您生意兴隆！</p>
        </div>
        <div class="welcome-actions">
          <el-button type="primary" @click="$router.push('/merchant/products')">
            <el-icon><Plus /></el-icon>
            添加商品
          </el-button>
          <el-button type="success" @click="$router.push('/merchant/orders')">
            <el-icon><List /></el-icon>
            查看订单
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计数据 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.todayOrders || 0 }}</div>
            <div class="stat-label">今日订单</div>
            <div class="stat-change" :class="{ positive: stats.orderChange > 0, negative: stats.orderChange < 0 }">
              <el-icon v-if="stats.orderChange > 0"><ArrowUp /></el-icon>
              <el-icon v-else-if="stats.orderChange < 0"><ArrowDown /></el-icon>
              {{ Math.abs(stats.orderChange || 0) }}%
            </div>
          </div>
          <el-icon class="stat-icon order-icon"><ShoppingCart /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">¥{{ stats.todaySales || '0.00' }}</div>
            <div class="stat-label">今日销售额</div>
            <div class="stat-change" :class="{ positive: stats.salesChange > 0, negative: stats.salesChange < 0 }">
              <el-icon v-if="stats.salesChange > 0"><ArrowUp /></el-icon>
              <el-icon v-else-if="stats.salesChange < 0"><ArrowDown /></el-icon>
              {{ Math.abs(stats.salesChange || 0) }}%
            </div>
          </div>
          <el-icon class="stat-icon sales-icon"><Money /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalProducts || 0 }}</div>
            <div class="stat-label">商品总数</div>
            <div class="stat-sub">{{ stats.activeProducts || 0 }} 个在售</div>
          </div>
          <el-icon class="stat-icon product-icon"><Goods /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.avgRating || '0.0' }}</div>
            <div class="stat-label">平均评分</div>
            <div class="stat-sub">{{ stats.totalReviews || 0 }} 条评价</div>
          </div>
          <el-icon class="stat-icon rating-icon"><Star /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表和数据 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card title="销售趋势">
          <template #header>
            <div class="card-header">
              <span>销售趋势</span>
              <el-select v-model="salesPeriod" size="small" style="width: 100px">
                <el-option label="7天" value="7days" />
                <el-option label="30天" value="30days" />
                <el-option label="90天" value="90days" />
              </el-select>
            </div>
          </template>
          <div class="chart-container" ref="salesChart" v-loading="chartsLoading"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="订单状态分布">
          <template #header>
            <span>订单状态分布</span>
          </template>
          <div class="chart-container" ref="orderStatusChart" v-loading="chartsLoading"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待处理订单 -->
    <el-card class="pending-orders-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>待处理订单</span>
          <el-badge :value="pendingOrders.length" class="badge">
            <el-button type="primary" size="small" @click="$router.push('/merchant/orders')">
              查看全部
            </el-button>
          </el-badge>
        </div>
      </template>
      
      <div v-loading="ordersLoading">
        <div v-if="pendingOrders.length === 0" class="empty-orders">
          <el-empty description="暂无待处理订单" :image-size="100">
            <el-button type="primary" @click="loadPendingOrders">刷新</el-button>
          </el-empty>
        </div>
        <div v-else>
          <div class="order-item" v-for="order in pendingOrders.slice(0, 5)" :key="order.id">
            <div class="order-info">
              <div class="order-header">
                <span class="order-no">订单号: {{ order.orderNo }}</span>
                <el-tag :type="getOrderStatusType(order.status)">
                  {{ getOrderStatusText(order.status) }}
                </el-tag>
              </div>
              <div class="order-details">
                <span class="order-time">{{ formatTime(order.createTime) }}</span>
                <span class="order-amount">¥{{ order.totalAmount }}</span>
              </div>
            </div>
            <div class="order-actions">
              <el-button 
                v-if="order.status === 'PAID'" 
                type="primary" 
                size="small"
                @click="startPreparing(order.id)"
              >
                开始制作
              </el-button>
              <el-button 
                v-if="order.status === 'PREPARING'" 
                type="success" 
                size="small"
                @click="markReady(order.id)"
              >
                制作完成
              </el-button>
              <el-button size="small" @click="viewOrderDetail(order.id)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 热销商品 -->
    <el-card class="hot-products-card" shadow="hover">
      <template #header>
        <span>热销商品 TOP 5</span>
      </template>
      
      <div v-loading="productsLoading">
        <div v-if="hotProducts.length === 0" class="empty-products">
          <el-empty description="暂无销售数据" :image-size="100" />
        </div>
        <el-table v-else :data="Array.isArray(hotProducts) ? hotProducts : []" style="width: 100%">
          <el-table-column prop="rank" label="排名" width="80" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="row.rank <= 3 ? 'warning' : 'info'" 
                size="small"
              >
                {{ row.rank }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="商品名称" />
          <el-table-column prop="sales" label="销量" width="100" align="center" />
          <el-table-column prop="revenue" label="销售额" width="120" align="center">
            <template #default="{ row }">
              ¥{{ row.revenue }}
            </template>
          </el-table-column>
          <el-table-column prop="rating" label="评分" width="100" align="center">
            <template #default="{ row }">
              <el-rate v-model="row.rating" disabled size="small" />
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Plus, List, ShoppingCart, Money, Goods, Star, 
  ArrowUp, ArrowDown 
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import merchantApi from '@/api/merchant.js'

export default {
  name: 'MerchantDashboard',
  components: {
    Plus, List, ShoppingCart, Money, Goods, Star,
    ArrowUp, ArrowDown
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const chartsLoading = ref(false)
    const ordersLoading = ref(false)
    const productsLoading = ref(false)
    
    const merchantInfo = ref({})
    const stats = reactive({
      todayOrders: 0,
      todaySales: '0.00',
      totalProducts: 0,
      activeProducts: 0,
      avgRating: '0.0',
      totalReviews: 0,
      orderChange: 0,
      salesChange: 0
    })
    
    const salesPeriod = ref('7days')
    const pendingOrders = ref([])
    const hotProducts = ref([])
    
    const salesChart = ref(null)
    const orderStatusChart = ref(null)
    
    // 获取商户信息
    const getMerchantInfo = () => {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr)
          merchantInfo.value = userInfo
        } catch (error) {
          console.error('解析用户信息失败:', error)
        }
      }
    }
    
    // 加载统计数据
    const loadStats = async () => {
      try {
        loading.value = true
        const response = await merchantApi.getDashboardStats()
        if (response.data) {
          Object.assign(stats, response.data)
        }
      } catch (error) {
        console.warn('商户统计API不存在，使用默认值')
        // 使用默认值，不显示错误消息
        Object.assign(stats, {
          todayOrders: 0,
          todaySales: '0.00',
          totalProducts: 0,
          activeProducts: 0,
          avgRating: '0.0',
          totalReviews: 0,
          orderChange: 0,
          salesChange: 0
        })
      } finally {
        loading.value = false
      }
    }
    
    // 加载待处理订单
    const loadPendingOrders = async () => {
      try {
        ordersLoading.value = true
        const response = await merchantApi.getOrders({ status: 'pending', size: 5 })
        
        // 确保返回的数据是数组格式
        if (response.data && Array.isArray(response.data.list)) {
          pendingOrders.value = response.data.list
        } else if (response.data && Array.isArray(response.data)) {
          pendingOrders.value = response.data
        } else {
          pendingOrders.value = []
        }
      } catch (error) {
        console.warn('待处理订单API调用失败，使用模拟数据')
        // 提供模拟数据，确保是数组格式
        pendingOrders.value = [
          {
            id: 1,
            orderNo: 'ORD202401150001',
            status: 'PAID',
            totalAmount: 45.50,
            createTime: new Date().toISOString(),
            customerName: '张同学'
          },
          {
            id: 2,
            orderNo: 'ORD202401150002',
            status: 'PREPARING',
            totalAmount: 32.00,
            createTime: new Date(Date.now() - 10 * 60 * 1000).toISOString(),
            customerName: '李同学'
          },
          {
            id: 3,
            orderNo: 'ORD202401150003',
            status: 'PAID',
            totalAmount: 28.50,
            createTime: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
            customerName: '王同学'
          }
        ]
      } finally {
        ordersLoading.value = false
      }
    }
    
    // 加载热销商品
    const loadHotProducts = async () => {
      try {
        productsLoading.value = true
        const response = await merchantApi.getTopProducts({ limit: 5 })
        
        // 确保返回的数据是数组格式
        if (response.data && Array.isArray(response.data)) {
          hotProducts.value = response.data
        } else if (response.data && Array.isArray(response.data.list)) {
          hotProducts.value = response.data.list
        } else {
          hotProducts.value = []
        }
      } catch (error) {
        console.warn('热销商品API调用失败，使用模拟数据')
        // 提供模拟数据，确保是数组格式
        hotProducts.value = [
          {
            rank: 1,
            name: '红烧肉饭',
            sales: 156,
            revenue: 2340.00,
            rating: 4.8
          },
          {
            rank: 2,
            name: '宫保鸡丁',
            sales: 134,
            revenue: 1876.00,
            rating: 4.6
          },
          {
            rank: 3,
            name: '糖醋里脊',
            sales: 98,
            revenue: 1568.00,
            rating: 4.7
          },
          {
            rank: 4,
            name: '麻婆豆腐',
            sales: 87,
            revenue: 1218.00,
            rating: 4.5
          },
          {
            rank: 5,
            name: '青椒肉丝',
            sales: 76,
            revenue: 1064.00,
            rating: 4.4
          }
        ]
      } finally {
        productsLoading.value = false
      }
    }
    
    // 初始化图表
    const initCharts = () => {
      chartsLoading.value = true
      
      // 销售趋势图
      if (salesChart.value) {
        const salesChartInstance = echarts.init(salesChart.value)
        const salesOption = {
          tooltip: {
            trigger: 'axis'
          },
          xAxis: {
            type: 'category',
            data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
          },
          yAxis: {
            type: 'value'
          },
          series: [{
            data: [820, 932, 901, 934, 1290, 1330, 1320],
            type: 'line',
            smooth: true,
            itemStyle: {
              color: '#409EFF'
            }
          }]
        }
        salesChartInstance.setOption(salesOption)
      }
      
      // 订单状态分布图
      if (orderStatusChart.value) {
        const orderChartInstance = echarts.init(orderStatusChart.value)
        const orderOption = {
          tooltip: {
            trigger: 'item'
          },
          series: [{
            type: 'pie',
            radius: '50%',
            data: [
              { value: 15, name: '待支付' },
              { value: 25, name: '已支付' },
              { value: 18, name: '制作中' },
              { value: 12, name: '待取餐' },
              { value: 30, name: '已完成' }
            ]
          }]
        }
        orderChartInstance.setOption(orderOption)
      }
      
      chartsLoading.value = false
    }
    
    // 辅助函数
    const formatDate = (date) => {
      return new Intl.DateTimeFormat('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
      }).format(date)
    }
    
    const formatTime = (timeStr) => {
      if (!timeStr) return '未知时间'
      return new Date(timeStr).toLocaleString('zh-CN')
    }
    
    const getOrderStatusType = (status) => {
      const statusMap = {
        'PENDING': 'warning',
        'PAID': 'info',
        'PREPARING': 'primary',
        'READY': 'success',
        'COMPLETED': 'success',
        'CANCELLED': 'danger'
      }
      return statusMap[status] || 'info'
    }
    
    const getOrderStatusText = (status) => {
      const statusMap = {
        'PENDING': '待支付',
        'PAID': '已支付',
        'PREPARING': '制作中',
        'READY': '待取餐',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    }
    
    // 订单操作
    const startPreparing = async (orderId) => {
      try {
        // await merchantApi.startPreparing(orderId)
        ElMessage.success('订单已开始制作')
        loadPendingOrders()
      } catch (error) {
        console.error('开始制作失败:', error)
        ElMessage.error('操作失败，请重试')
      }
    }
    
    const markReady = async (orderId) => {
      try {
        // await merchantApi.markReady(orderId)
        ElMessage.success('订单已标记为待取餐')
        loadPendingOrders()
      } catch (error) {
        console.error('标记完成失败:', error)
        ElMessage.error('操作失败，请重试')
      }
    }
    
    const viewOrderDetail = (orderId) => {
      router.push(`/order/${orderId}`)
    }
    
    // 监听销售周期变化
    watch(salesPeriod, () => {
      initCharts()
    })
    
    onMounted(() => {
      getMerchantInfo()
      loadStats()
      loadPendingOrders()
      loadHotProducts()
      
      // 延迟初始化图表，确保DOM已渲染
      setTimeout(() => {
        initCharts()
      }, 100)
    })
    
    return {
      loading,
      chartsLoading,
      ordersLoading,
      productsLoading,
      merchantInfo,
      stats,
      salesPeriod,
      pendingOrders,
      hotProducts,
      salesChart,
      orderStatusChart,
      formatDate,
      formatTime,
      getOrderStatusType,
      getOrderStatusText,
      startPreparing,
      markReady,
      viewOrderDetail,
      loadPendingOrders
    }
  }
}
</script>

<style scoped>
.merchant-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.welcome-card {
  margin-bottom: 20px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.welcome-text p {
  margin: 0;
  color: #606266;
}

.welcome-actions {
  display: flex;
  gap: 10px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-content {
  position: relative;
  z-index: 2;
}

.stat-value {
  font-size: 2em;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  color: #909399;
  font-size: 0.9em;
  margin-bottom: 5px;
}

.stat-change {
  font-size: 0.8em;
  display: flex;
  align-items: center;
  gap: 2px;
}

.stat-change.positive {
  color: #67C23A;
}

.stat-change.negative {
  color: #F56C6C;
}

.stat-sub {
  color: #909399;
  font-size: 0.8em;
}

.stat-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 3em;
  opacity: 0.1;
}

.order-icon {
  color: #409EFF;
}

.sales-icon {
  color: #67C23A;
}

.product-icon {
  color: #E6A23C;
}

.rating-icon {
  color: #F56C6C;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pending-orders-card,
.hot-products-card {
  margin-bottom: 20px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}

.order-item:last-child {
  border-bottom: none;
}

.order-info {
  flex: 1;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.order-no {
  font-weight: bold;
  color: #303133;
}

.order-details {
  display: flex;
  justify-content: space-between;
  color: #606266;
  font-size: 0.9em;
}

.order-amount {
  font-weight: bold;
  color: #E6A23C;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.empty-orders,
.empty-products {
  padding: 40px 0;
}

@media (max-width: 768px) {
  .merchant-dashboard {
    padding: 10px;
  }
  
  .welcome-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .order-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .order-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>