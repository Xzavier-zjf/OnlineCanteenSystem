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
          <el-select v-model="statusFilter" placeholder="筛选状态" clearable style="width: 150px; margin-right: 10px;">
            <el-option label="全部" value="" />
            <el-option label="待支付" value="PENDING" />
            <el-option label="已支付" value="PAID" />
            <el-option label="制作中" value="PREPARING" />
            <el-option label="待取餐" value="READY" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
          
          <el-select v-model="timeFilter" placeholder="时间筛选" clearable style="width: 150px; margin-right: 10px;">
            <el-option label="全部时间" value="" />
            <el-option label="今天" value="today" />
            <el-option label="最近3天" value="3days" />
            <el-option label="最近一周" value="week" />
            <el-option label="最近一月" value="month" />
          </el-select>
          
          <el-select v-model="amountFilter" placeholder="金额筛选" clearable style="width: 150px; margin-right: 10px;">
            <el-option label="全部金额" value="" />
            <el-option label="0-20元" value="0-20" />
            <el-option label="20-50元" value="20-50" />
            <el-option label="50-100元" value="50-100" />
            <el-option label="100元以上" value="100+" />
          </el-select>
          
          <el-select v-model="sortBy" placeholder="排序方式" style="width: 150px; margin-right: 10px;">
            <el-option label="创建时间降序" value="create_desc" />
            <el-option label="创建时间升序" value="create_asc" />
            <el-option label="金额降序" value="amount_desc" />
            <el-option label="金额升序" value="amount_asc" />
            <el-option label="状态排序" value="status" />
          </el-select>
        </div>
        <div class="action-section">
          <el-button type="primary" :icon="RefreshRight" @click="loadOrders" :loading="loading">
            刷新
          </el-button>
          <el-button type="info" @click="exportOrders" :disabled="filteredOrders.length === 0">
            <el-icon><Download /></el-icon>
            导出
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
import { List, Document, Plus, View, Close, CreditCard, RefreshRight, Check, Download } from '@element-plus/icons-vue'

export default {
  name: 'Orders',
  components: {
    List, Document, Plus, View, Close, CreditCard, RefreshRight, Check, Download
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const orders = ref([])
    const statusFilter = ref('')  // 状态筛选
    const timeFilter = ref('')    // 时间筛选
    const amountFilter = ref('')  // 金额筛选
    const sortBy = ref('create_desc') // 排序方式
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
      let filtered = [...orders.value]
      
      // 状态筛选
      if (statusFilter.value) {
        filtered = filtered.filter(order => order.status === statusFilter.value)
      }
      
      // 时间筛选
      if (timeFilter.value) {
        const now = new Date()
        const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
        
        filtered = filtered.filter(order => {
          const orderDate = new Date(order.createTime)
          
          switch (timeFilter.value) {
            case 'today':
              return orderDate >= today
            case '3days':
              const threeDaysAgo = new Date(today.getTime() - 3 * 24 * 60 * 60 * 1000)
              return orderDate >= threeDaysAgo
            case 'week':
              const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
              return orderDate >= weekAgo
            case 'month':
              const monthAgo = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000)
              return orderDate >= monthAgo
            default:
              return true
          }
        })
      }
      
      // 金额筛选
      if (amountFilter.value) {
        filtered = filtered.filter(order => {
          const amount = parseFloat(order.totalAmount)
          
          switch (amountFilter.value) {
            case '0-20':
              return amount >= 0 && amount <= 20
            case '20-50':
              return amount > 20 && amount <= 50
            case '50-100':
              return amount > 50 && amount <= 100
            case '100+':
              return amount > 100
            default:
              return true
          }
        })
      }
      
      // 排序
      filtered.sort((a, b) => {
        switch (sortBy.value) {
          case 'create_asc':
            return new Date(a.createTime) - new Date(b.createTime)
          case 'create_desc':
            return new Date(b.createTime) - new Date(a.createTime)
          case 'amount_asc':
            return parseFloat(a.totalAmount) - parseFloat(b.totalAmount)
          case 'amount_desc':
            return parseFloat(b.totalAmount) - parseFloat(a.totalAmount)
          case 'status':
            const statusOrder = ['PENDING', 'PAID', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED']
            return statusOrder.indexOf(a.status) - statusOrder.indexOf(b.status)
          default:
            return new Date(b.createTime) - new Date(a.createTime)
        }
      })
      
      return filtered
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
    
    const exportOrders = () => {
      try {
        const exportData = filteredOrders.value.map(order => ({
          '订单号': order.orderNo,
          '订单状态': getStatusText(order.status),
          '订单金额': `¥${order.totalAmount}`,
          '创建时间': formatTime(order.createTime),
          '更新时间': formatTime(order.updateTime),
          '备注': order.remark || '无'
        }))
        
        // 转换为CSV格式
        const headers = Object.keys(exportData[0])
        const csvContent = [
          headers.join(','),
          ...exportData.map(row => headers.map(header => `"${row[header]}"`).join(','))
        ].join('\n')
        
        // 创建下载链接
        const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
        const link = document.createElement('a')
        const url = URL.createObjectURL(blob)
        link.setAttribute('href', url)
        link.setAttribute('download', `订单记录_${new Date().toISOString().split('T')[0]}.csv`)
        link.style.visibility = 'hidden'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        
        ElMessage.success(`已导出 ${exportData.length} 条订单记录`)
      } catch (error) {
        console.error('导出订单失败:', error)
        ElMessage.error('导出失败，请重试')
      }
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
      timeFilter,
      amountFilter,
      sortBy,
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
      exportOrders,
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

/* 订单工具栏样式 */
.order-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-section {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.action-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-section {
    justify-content: center;
  }
  
  .filter-section .el-select {
    width: 100% !important;
    margin-right: 0 !important;
    margin-bottom: 8px;
  }
  
  .action-section {
    justify-content: center;
  }
  
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .order-actions {
    flex-wrap: wrap;
    justify-content: center;
  }
}

/* 订单状态标签样式增强 */
.el-tag {
  font-weight: 500;
}

/* 金额显示样式 */
.amount-value {
  color: #E6A23C;
  font-weight: bold;
  font-size: 1.1em;
}
</style>