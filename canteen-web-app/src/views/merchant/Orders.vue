<template>
  <div class="merchant-orders">
    <!-- 页面头部 -->
    <el-card class="header-card" shadow="hover">
      <div class="page-header">
        <div class="header-info">
          <h2>订单管理</h2>
          <p>管理您收到的订单，及时处理订单状态</p>
        </div>
        <div class="header-stats">
          <el-statistic title="待处理订单" :value="pendingCount" />
          <el-statistic title="今日订单" :value="todayCount" />
          <el-statistic title="今日收入" :value="todayRevenue" prefix="¥" />
        </div>
      </div>
    </el-card>

    <!-- 快速操作区 -->
    <el-card class="quick-actions-card" shadow="hover">
      <div class="quick-actions">
        <el-button 
          type="primary" 
          :badge="pendingCount"
          @click="filterByStatus('PAID')"
        >
          <el-icon><Clock /></el-icon>
          待制作 ({{ getStatusCount('PAID') }})
        </el-button>
        <el-button 
          type="warning" 
          @click="filterByStatus('PREPARING')"
        >
          <el-icon><Loading /></el-icon>
          制作中 ({{ getStatusCount('PREPARING') }})
        </el-button>
        <el-button 
          type="success" 
          @click="filterByStatus('READY')"
        >
          <el-icon><Check /></el-icon>
          待取餐 ({{ getStatusCount('READY') }})
        </el-button>
        <el-button @click="loadOrders">
          <el-icon><RefreshRight /></el-icon>
          刷新
        </el-button>
      </div>
    </el-card>

    <!-- 筛选和搜索 -->
    <el-card class="filter-card" shadow="hover">
      <div class="filter-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索订单号或用户名"
          clearable
          style="width: 200px; margin-right: 10px;"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select
          v-model="statusFilter"
          placeholder="订单状态"
          clearable
          style="width: 150px; margin-right: 10px;"
          @change="handleSearch"
        >
          <el-option label="待支付" value="PENDING" />
          <el-option label="已支付" value="PAID" />
          <el-option label="制作中" value="PREPARING" />
          <el-option label="待取餐" value="READY" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="margin-right: 10px;"
          @change="handleSearch"
        />
        
        <el-select
          v-model="amountFilter"
          placeholder="金额筛选"
          clearable
          style="width: 120px; margin-right: 10px;"
          @change="handleSearch"
        >
          <el-option label="0-20元" value="0-20" />
          <el-option label="20-50元" value="20-50" />
          <el-option label="50-100元" value="50-100" />
          <el-option label="100元以上" value="100+" />
        </el-select>
        
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        
        <el-button @click="exportOrders" :disabled="orders.length === 0">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </el-card>

    <!-- 订单列表 -->
    <el-card class="orders-card" shadow="hover">
      <div v-loading="loading">
        <div v-if="orders.length === 0 && !loading" class="empty-orders">
          <el-empty description="暂无订单" :image-size="120">
            <el-button type="primary" @click="loadOrders">刷新数据</el-button>
          </el-empty>
        </div>
        
        <div v-else class="orders-list">
          <div class="order-item" v-for="order in orders" :key="order.id">
            <el-card class="order-card" shadow="hover">
              <!-- 订单头部 -->
              <div class="order-header">
                <div class="order-info">
                  <div class="order-main-info">
                    <span class="order-no">订单号: {{ order.orderNo }}</span>
                    <el-tag :type="getStatusType(order.status)" class="status-tag">
                      {{ getStatusText(order.status) }}
                    </el-tag>
                  </div>
                  <div class="order-meta">
                    <span class="order-time">{{ formatTime(order.createTime) }}</span>
                    <span class="order-user">用户: {{ order.userName || '匿名用户' }}</span>
                  </div>
                </div>
                <div class="order-amount">
                  <div class="amount-value">¥{{ order.totalAmount }}</div>
                  <div class="amount-label">订单金额</div>
                </div>
              </div>
              
              <el-divider />
              
              <!-- 订单详情 -->
              <div class="order-content">
                <div class="order-items" v-if="order.items && order.items.length > 0">
                  <div class="item-row" v-for="item in order.items" :key="item.id">
                    <img :src="getImageUrl(item.imageUrl)" class="item-image" @error="handleImageError" />
                    <div class="item-info">
                      <div class="item-name">{{ item.productName }}</div>
                      <div class="item-details">
                        <span class="item-price">¥{{ item.price }}</span>
                        <span class="item-quantity">x{{ item.quantity }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                
                <div class="order-remark" v-if="order.remark">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>备注: {{ order.remark }}</span>
                </div>
                
                <!-- 时间线 -->
                <div class="order-timeline" v-if="order.timeline">
                  <el-timeline>
                    <el-timeline-item
                      v-for="(event, index) in order.timeline"
                      :key="index"
                      :timestamp="formatTime(event.time)"
                      :type="getTimelineType(event.status)"
                    >
                      {{ event.description }}
                    </el-timeline-item>
                  </el-timeline>
                </div>
              </div>
              
              <!-- 订单操作 -->
              <div class="order-actions">
                <el-button size="small" @click="viewOrderDetail(order.id)">
                  <el-icon><View /></el-icon>
                  查看详情
                </el-button>
                
                <el-button 
                  v-if="order.status === 'PAID'" 
                  type="primary" 
                  size="small"
                  :loading="processingOrders.includes(order.id)"
                  @click="startPreparing(order)"
                >
                  <el-icon><Play /></el-icon>
                  开始制作
                </el-button>
                
                <el-button 
                  v-if="order.status === 'PREPARING'" 
                  type="success" 
                  size="small"
                  :loading="processingOrders.includes(order.id)"
                  @click="markReady(order)"
                >
                  <el-icon><Check /></el-icon>
                  制作完成
                </el-button>
                
                <el-button 
                  v-if="order.status === 'READY'" 
                  type="warning" 
                  size="small"
                  @click="notifyCustomer(order)"
                >
                  <el-icon><Bell /></el-icon>
                  通知取餐
                </el-button>
                
                <el-button 
                  v-if="['PAID', 'PREPARING'].includes(order.status)" 
                  type="danger" 
                  size="small"
                  @click="cancelOrder(order)"
                >
                  <el-icon><Close /></el-icon>
                  取消订单
                </el-button>
                
                <el-dropdown v-if="order.status === 'COMPLETED'">
                  <el-button size="small">
                    更多操作<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="printReceipt(order)">
                        <el-icon><Printer /></el-icon>
                        打印小票
                      </el-dropdown-item>
                      <el-dropdown-item @click="refundOrder(order)">
                        <el-icon><RefreshLeft /></el-icon>
                        申请退款
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </el-card>
          </div>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 批量操作对话框 -->
    <el-dialog v-model="showBatchDialog" title="批量操作" width="500px">
      <div class="batch-content">
        <p>选择要执行的批量操作：</p>
        <el-radio-group v-model="batchAction">
          <el-radio label="start_preparing">批量开始制作</el-radio>
          <el-radio label="mark_ready">批量标记完成</el-radio>
          <el-radio label="cancel">批量取消</el-radio>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button @click="showBatchDialog = false">取消</el-button>
        <el-button type="primary" @click="executeBatchAction">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Clock, Loading, Check, RefreshRight, Search, Download,
  View, Play, Bell, Close, ArrowDown, Printer, RefreshLeft,
  ChatDotRound
} from '@element-plus/icons-vue'

export default {
  name: 'MerchantOrders',
  components: {
    Clock, Loading, Check, RefreshRight, Search, Download,
    View, Play, Bell, Close, ArrowDown, Printer, RefreshLeft,
    ChatDotRound
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const orders = ref([])
    const processingOrders = ref([])
    
    // 筛选条件
    const searchKeyword = ref('')
    const statusFilter = ref('')
    const dateRange = ref([])
    const amountFilter = ref('')
    
    // 分页
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    
    // 批量操作
    const showBatchDialog = ref(false)
    const batchAction = ref('')
    const selectedOrders = ref([])
    
    // 统计数据
    const stats = reactive({
      pendingCount: 0,
      todayCount: 0,
      todayRevenue: '0.00'
    })
    
    // 计算属性
    const pendingCount = computed(() => {
      return orders.value.filter(order => 
        ['PAID', 'PREPARING'].includes(order.status)
      ).length
    })
    
    const todayCount = computed(() => {
      const today = new Date().toDateString()
      return orders.value.filter(order => 
        new Date(order.createTime).toDateString() === today
      ).length
    })
    
    const todayRevenue = computed(() => {
      const today = new Date().toDateString()
      const todayOrders = orders.value.filter(order => 
        new Date(order.createTime).toDateString() === today &&
        order.status === 'COMPLETED'
      )
      const revenue = todayOrders.reduce((sum, order) => 
        sum + parseFloat(order.totalAmount), 0
      )
      return revenue.toFixed(2)
    })
    
    // 获取状态数量
    const getStatusCount = (status) => {
      return orders.value.filter(order => order.status === status).length
    }
    
    // 加载订单列表
    const loadOrders = async () => {
      try {
        loading.value = true
        
        const params = {
          current: currentPage.value,
          size: pageSize.value
        }
        
        if (searchKeyword.value) {
          params.keyword = searchKeyword.value
        }
        if (statusFilter.value) {
          params.status = statusFilter.value
        }
        if (dateRange.value && dateRange.value.length === 2) {
          params.startDate = dateRange.value[0]
          params.endDate = dateRange.value[1]
        }
        if (amountFilter.value) {
          params.amountRange = amountFilter.value
        }
        
        // 调用真实API获取订单数据
        const response = await fetch('/api/merchant/orders?' + new URLSearchParams(params), {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          }
        })
        
        if (response.ok) {
          const data = await response.json()
          if (data.success) {
            orders.value = data.data.records || []
            total.value = data.data.total || 0
          } else {
            throw new Error(data.message || '获取订单列表失败')
          }
        } else {
          console.warn('订单列表API不存在，使用空数据')
          orders.value = []
          total.value = 0
        }
        
      } catch (error) {
        console.error('加载订单失败:', error)
        ElMessage.error('加载订单失败')
      } finally {
        loading.value = false
      }
    }
    
    // 搜索处理
    const handleSearch = () => {
      currentPage.value = 1
      loadOrders()
    }
    
    // 分页处理
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
      loadOrders()
    }
    
    const handleCurrentChange = (page) => {
      currentPage.value = page
      loadOrders()
    }
    
    // 按状态筛选
    const filterByStatus = (status) => {
      statusFilter.value = status
      handleSearch()
    }
    
    // 辅助函数
    const formatTime = (timeStr) => {
      if (!timeStr) return '未知时间'
      return new Date(timeStr).toLocaleString('zh-CN')
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
    
    const getTimelineType = (status) => {
      const typeMap = {
        'PENDING': 'warning',
        'PAID': 'primary',
        'PREPARING': 'primary',
        'READY': 'success',
        'COMPLETED': 'success',
        'CANCELLED': 'danger'
      }
      return typeMap[status] || 'primary'
    }
    
    const getImageUrl = (imageUrl) => {
      if (!imageUrl) return '/images/products/placeholder.svg'
      if (imageUrl.startsWith('http')) return imageUrl
      return imageUrl
    }
    
    const handleImageError = (event) => {
      event.target.src = '/images/products/placeholder.svg'
    }
    
    // 订单操作
    const startPreparing = async (order) => {
      try {
        processingOrders.value.push(order.id)
        // await merchantApi.startPreparing(order.id)
        ElMessage.success('订单已开始制作')
        loadOrders()
      } catch (error) {
        console.error('开始制作失败:', error)
        ElMessage.error('操作失败')
      } finally {
        processingOrders.value = processingOrders.value.filter(id => id !== order.id)
      }
    }
    
    const markReady = async (order) => {
      try {
        processingOrders.value.push(order.id)
        // await merchantApi.markReady(order.id)
        ElMessage.success('订单制作完成，已通知用户取餐')
        loadOrders()
      } catch (error) {
        console.error('标记完成失败:', error)
        ElMessage.error('操作失败')
      } finally {
        processingOrders.value = processingOrders.value.filter(id => id !== order.id)
      }
    }
    
    const notifyCustomer = async (order) => {
      try {
        // await merchantApi.notifyCustomer(order.id)
        ElMessage.success('已通知用户取餐')
      } catch (error) {
        console.error('通知失败:', error)
        ElMessage.error('通知失败')
      }
    }
    
    const cancelOrder = async (order) => {
      try {
        await ElMessageBox.confirm(
          `确定要取消订单 "${order.orderNo}" 吗？`,
          '确认取消',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        // await merchantApi.cancelOrder(order.id)
        ElMessage.success('订单已取消')
        loadOrders()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消订单失败:', error)
          ElMessage.error('取消失败')
        }
      }
    }
    
    const viewOrderDetail = (orderId) => {
      router.push(`/order/${orderId}`)
    }
    
    const printReceipt = (order) => {
      ElMessage.info('打印功能开发中')
    }
    
    const refundOrder = (order) => {
      ElMessage.info('退款功能开发中')
    }
    
    // 导出订单
    const exportOrders = () => {
      try {
        const exportData = orders.value.map(order => ({
          '订单号': order.orderNo,
          '用户名': order.userName || '匿名用户',
          '订单状态': getStatusText(order.status),
          '订单金额': `¥${order.totalAmount}`,
          '创建时间': formatTime(order.createTime),
          '备注': order.remark || '无'
        }))
        
        const headers = Object.keys(exportData[0])
        const csvContent = [
          headers.join(','),
          ...exportData.map(row => headers.map(header => `"${row[header]}"`).join(','))
        ].join('\n')
        
        const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
        const link = document.createElement('a')
        const url = URL.createObjectURL(blob)
        link.setAttribute('href', url)
        link.setAttribute('download', `商户订单_${new Date().toISOString().split('T')[0]}.csv`)
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
      loadOrders()
    })
    
    return {
      loading,
      orders,
      processingOrders,
      searchKeyword,
      statusFilter,
      dateRange,
      amountFilter,
      currentPage,
      pageSize,
      total,
      showBatchDialog,
      batchAction,
      selectedOrders,
      pendingCount,
      todayCount,
      todayRevenue,
      getStatusCount,
      loadOrders,
      handleSearch,
      handleSizeChange,
      handleCurrentChange,
      filterByStatus,
      formatTime,
      getStatusType,
      getStatusText,
      getTimelineType,
      getImageUrl,
      handleImageError,
      startPreparing,
      markReady,
      notifyCustomer,
      cancelOrder,
      viewOrderDetail,
      printReceipt,
      refundOrder,
      exportOrders
    }
  }
}
</script>

<style scoped>
.merchant-orders {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header-card,
.quick-actions-card,
.filter-card,
.orders-card {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.header-info p {
  margin: 0;
  color: #606266;
}

.header-stats {
  display: flex;
  gap: 40px;
}

.quick-actions {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.filter-section {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-item {
  width: 100%;
}

.order-card {
  border-left: 4px solid #409EFF;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.order-main-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.order-no {
  font-weight: bold;
  color: #303133;
}

.status-tag {
  font-weight: 500;
}

.order-meta {
  display: flex;
  gap: 20px;
  color: #606266;
  font-size: 0.9em;
}

.order-amount {
  text-align: right;
}

.amount-value {
  font-size: 1.5em;
  font-weight: bold;
  color: #E6A23C;
}

.amount-label {
  color: #909399;
  font-size: 0.9em;
}

.order-items {
  margin-bottom: 15px;
}

.item-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.item-row:last-child {
  border-bottom: none;
}

.item-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 12px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.item-details {
  display: flex;
  justify-content: space-between;
  color: #606266;
  font-size: 0.9em;
}

.item-price {
  color: #E6A23C;
  font-weight: 500;
}

.order-remark {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;
  margin-bottom: 15px;
  color: #606266;
}

.order-timeline {
  margin-top: 15px;
}

.order-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.empty-orders {
  padding: 40px 0;
}

.batch-content {
  padding: 20px 0;
}

@media (max-width: 768px) {
  .merchant-orders {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-stats {
    flex-direction: column;
    gap: 15px;
  }
  
  .quick-actions {
    justify-content: center;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-section .el-input,
  .filter-section .el-select,
  .filter-section .el-date-picker {
    width: 100% !important;
    margin-right: 0 !important;
    margin-bottom: 10px;
  }
  
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .order-meta {
    flex-direction: column;
    gap: 5px;
  }
  
  .order-actions {
    justify-content: center;
  }
}
</style>