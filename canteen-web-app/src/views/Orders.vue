<template>
  <div class="orders">
    <el-card class="orders-card" shadow="hover">
      <template #header>
        <div class="orders-header">
          <el-icon><List /></el-icon>
          <span>我的订单</span>
        </div>
      </template>
      
      <!-- 订单筛选和操作栏 -->
      <div class="order-toolbar" v-if="orders.length > 0">
        <div class="filter-section">
          <el-select v-model="statusFilter" placeholder="筛选状态" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="待支付" value="PENDING" />
            <el-option label="已支付" value="PAID" />
            <el-option label="制作中" value="PREPARING" />
            <el-option label="待取餐" value="READY" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </div>
        <div class="action-section">
          <el-button type="primary" :icon="RefreshRight" @click="loadOrders" :loading="loading">
            刷新
          </el-button>
        </div>
      </div>
      
      <div v-loading="loading">
        <div v-if="orders.length === 0 && !loading" class="empty-orders">
          <el-empty description="暂无订单" :image-size="120">
            <template #image>
              <el-icon size="120" color="#c0c4cc"><Document /></el-icon>
            </template>
            <template #description>
              <p>您还没有任何订单</p>
              <p style="color: #909399; font-size: 14px;">快去选购您喜欢的美食吧</p>
            </template>
            <el-button type="primary" size="large" @click="$router.push('/products')">
              <el-icon><Plus /></el-icon>
              立即点餐
            </el-button>
          </el-empty>
        </div>
        
        <div v-else>
          <div class="order-item" v-for="order in pagedOrders" :key="order.id">
            <el-card shadow="hover" class="order-card">
              <div class="order-header">
                <div class="order-info">
                  <h3>订单号: {{ order.orderNo }}</h3>
                  <p class="order-time">下单时间: {{ formatTime(order.createTime) }}</p>
                </div>
                <div class="order-status">
                  <el-tag :type="getStatusType(order.status)">
                    {{ getStatusText(order.status) }}
                  </el-tag>
                </div>
              </div>
              
              <el-divider />
              
              <div class="order-content">
                <div class="order-amount">
                  <span class="amount-label">订单金额:</span>
                  <span class="amount-value">¥{{ order.totalAmount }}</span>
                </div>
                
                <div class="order-remark" v-if="order.remark">
                  <span class="remark-label">备注:</span>
                  <span class="remark-value">{{ order.remark }}</span>
                </div>
                
                <!-- 订单更新时间 -->
                <div class="order-update-time" v-if="order.updateTime && order.updateTime !== order.createTime">
                  <span class="update-label">更新时间:</span>
                  <span class="update-value">{{ formatTime(order.updateTime) }}</span>
                </div>
              </div>
              
              <div class="order-actions">
                <el-button size="small" @click="viewOrderDetail(order.id)">
                  <el-icon><View /></el-icon>
                  查看详情
                </el-button>
                <el-button 
                  v-if="order.status === 'PENDING'" 
                  type="danger" 
                  size="small" 
                  :loading="cancelingId === order.id"
                  :disabled="cancelingId === order.id || payingId === order.id"
                  @click="cancelOrder(order.id)"
                >
                  <el-icon v-if="cancelingId !== order.id"><Close /></el-icon>
                  {{ cancelingId === order.id ? '取消中...' : '取消订单' }}
                </el-button>
                <el-button 
                  v-if="order.status === 'PENDING'" 
                  type="primary" 
                  size="small" 
                  :loading="payingId === order.id"
                  :disabled="cancelingId === order.id || payingId === order.id"
                  @click="payOrder(order.id)"
                >
                  <el-icon v-if="payingId !== order.id"><CreditCard /></el-icon>
                  {{ payingId === order.id ? '支付中...' : '立即支付' }}
                </el-button>
                <el-button 
                  v-if="order.status === 'COMPLETED'" 
                  type="success" 
                  size="small" 
                  @click="reorder(order)"
                >
                  <el-icon><RefreshRight /></el-icon>
                  再来一单
                </el-button>
                <el-button 
                  v-if="order.status === 'READY'" 
                  type="warning" 
                  size="small" 
                  @click="confirmReceive(order.id)"
                >
                  <el-icon><Check /></el-icon>
                  确认取餐
                </el-button>
              </div>
            </el-card>
          </div>
        </div>
        <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: center;" v-if="filteredOrders.length > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="filteredOrders.length"
            :page-sizes="[5, 10, 15]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '../api'
import { List, Document, Plus, View, Close, CreditCard, RefreshRight, Check } from '@element-plus/icons-vue'

export default {
  name: 'Orders',
  components: {
    List, Document, Plus, View, Close, CreditCard, RefreshRight, Check
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const orders = ref([])
    const statusFilter = ref('')  // 状态筛选
    const currentPage = ref(1)
    const pageSize = ref(5)
    const cancelingId = ref(null)
    const payingId = ref(null)
    
    const getUserInfo = () => {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr && userInfoStr !== 'undefined' && userInfoStr !== 'null') {
        try {
          const parsed = JSON.parse(userInfoStr)
          if (parsed && typeof parsed === 'object' && parsed.id) {
            return parsed
          } else {
            console.warn('用户信息格式无效:', parsed)
            return {}
          }
        } catch (e) {
          console.error('解析用户信息失败:', e)
          return {}
        }
      }
      return {}
    }
    
    const userInfo = getUserInfo()
    
    const loadOrders = async () => {
      try {
        loading.value = true
        
        // 重新获取用户信息
        const userInfoStr = localStorage.getItem('userInfo')
        if (!userInfoStr) {
          throw new Error('用户信息不存在')
        }
        
        const currentUserInfo = JSON.parse(userInfoStr)
        const userId = currentUserInfo.id || currentUserInfo.userId || Date.now()
        
        console.log('加载订单，用户ID:', userId)
        const response = await orderApi.getOrders(userId)
        orders.value = response.data
        console.log('订单加载成功:', response.data)
      } catch (error) {
        console.error('加载订单失败:', error)
        ElMessage.error('加载订单失败: ' + (error.message || '请重试'))
      } finally {
        loading.value = false
      }
    }
    
    const getStatusType = (status) => {
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
    
    const getStatusText = (status) => {
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
    
    const formatTime = (timeStr) => {
      if (!timeStr) return '未知时间'
      try {
        let date
        // 处理不同的日期格式
        if (typeof timeStr === 'string') {
          // 如果是ISO格式或数组格式，直接解析
          date = new Date(timeStr)
        } else if (Array.isArray(timeStr) && timeStr.length >= 3) {
          // 处理数组格式: [2024, 12, 25, 10, 30, 0]
          const [year, month, day, hour = 0, minute = 0, second = 0] = timeStr
          date = new Date(year, month - 1, day, hour, minute, second)
        } else {
          date = new Date(timeStr)
        }
        
        if (isNaN(date.getTime())) {
          console.warn('日期格式无效:', timeStr)
          return '日期格式错误'
        }
        
        // 返回格式化的日期和时间
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        })
      } catch (error) {
        console.error('日期格式化错误:', error, '原始数据:', timeStr)
        return '日期解析失败'
      }
    }
    
    // 筛选后的订单列表
    const filteredOrders = computed(() => {
      if (!statusFilter.value) {
        return orders.value
      }
      return orders.value.filter(order => order.status === statusFilter.value)
    })
    
    const pagedOrders = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value
      return filteredOrders.value.slice(start, start + pageSize.value)
    })
    
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
    }
    
    const handleCurrentChange = (page) => {
      currentPage.value = page
    }
    
    const viewOrderDetail = (orderId) => {
      router.push(`/order/${orderId}`)
    }
    
    const cancelOrder = async (orderId) => {
      try {
        await ElMessageBox.confirm('确定要取消这个订单吗？', '确认取消', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        cancelingId.value = orderId
        await orderApi.cancelOrder(orderId)
        ElMessage.success('订单已取消')
        loadOrders()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消订单失败:', error)
          ElMessage.error('取消订单失败: ' + (error.message || '请重试'))
        }
      } finally {
        cancelingId.value = null
      }
    }
    
    const payOrder = async (orderId) => {
      try {
        // 模拟支付过程
        await ElMessageBox.confirm('确定要支付这个订单吗？', '确认支付', {
          confirmButtonText: '确定支付',
          cancelButtonText: '取消',
          type: 'info'
        })
        payingId.value = orderId
        await orderApi.payOrder(orderId)
        ElMessage.success('支付成功')
        loadOrders()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('支付失败:', error)
          ElMessage.error('支付失败: ' + (error.message || '请重试'))
        }
      } finally {
        payingId.value = null
      }
    }
    
    const confirmReceive = async (orderId) => {
      try {
        await ElMessageBox.confirm('确认已取餐？', '确认取餐', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'info'
        })
        await orderApi.readyOrder(orderId)
        ElMessage.success('订单已标记为待取餐，请及时取餐！')
        loadOrders()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('确认取餐失败:', error)
          ElMessage.error('确认取餐失败: ' + (error.message || '请重试'))
        }
      }
    }
    
    const reorder = (order) => {
      ElMessage.info('功能开发中，敬请期待')
      // TODO: 实现再来一单功能
    }
    
    onMounted(() => {
      // 重新获取最新的用户信息和token
      const token = localStorage.getItem('token')
      const userInfoStr = localStorage.getItem('userInfo')
      
      console.log('Orders页面加载 - token:', token)
      console.log('Orders页面加载 - userInfo:', userInfoStr)
      
      if (!token || token === 'null' || token === 'undefined' || !userInfoStr || userInfoStr === 'null') {
        console.log('用户未登录，跳转到登录页')
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }
      
      try {
        const currentUserInfo = JSON.parse(userInfoStr)
        if (!currentUserInfo.id) {
          currentUserInfo.id = currentUserInfo.userId || Date.now()
        }
        console.log('用户已登录，用户信息:', currentUserInfo)
        loadOrders()
      } catch (error) {
        console.error('解析用户信息失败:', error)
        ElMessage.warning('用户信息异常，请重新登录')
        router.push('/login')
      }
    })
    
    return {
      loading,
      orders,
      statusFilter,
      filteredOrders,
      currentPage,
      pageSize,
      pagedOrders,
      getStatusType,
      getStatusText,
      formatTime,
      viewOrderDetail,
      cancelOrder,
      payOrder,
      confirmReceive,
      reorder,
      handleSizeChange,
      handleCurrentChange,
      cancelingId,
      payingId
    }
  }
}
</script>

<style scoped>
.orders {
  max-width: 800px;
  margin: 0 auto;
}

.orders-header {
  display: flex;
  align-items: center;
  font-size: 1.2em;
  font-weight: bold;
}

.orders-header .el-icon {
  margin-right: 8px;
}

.order-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.filter-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.action-section {
  display: flex;
  gap: 10px;
}

.empty-orders {
  text-align: center;
  padding: 40px 0;
}

.order-item {
  margin-bottom: 20px;
}

.order-card {
  border-radius: 8px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.order-info h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 1.1em;
}

.order-time {
  color: #909399;
  font-size: 0.9em;
  margin: 0;
}

.order-content {
  margin: 15px 0;
}

.order-amount {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.amount-label {
  color: #606266;
}

.amount-value {
  color: #E6A23C;
  font-size: 1.2em;
  font-weight: bold;
}

.order-remark {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.remark-label {
  color: #606266;
  flex-shrink: 0;
}

.remark-value {
  color: #303133;
  word-break: break-all;
}

.order-update-time {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-top: 10px;
}

.update-label {
  color: #909399;
  flex-shrink: 0;
  font-size: 0.9em;
}

.update-value {
  color: #606266;
  font-size: 0.9em;
}

.order-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 15px;
}
</style>