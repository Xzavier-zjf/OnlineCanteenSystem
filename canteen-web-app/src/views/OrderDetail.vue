<template>
  <div class="order-detail">
    <el-card class="detail-card" shadow="hover" v-loading="loading">
      <template #header>
        <div class="detail-header">
          <el-button @click="$router.back()" type="text">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span>订单详情</span>
        </div>
      </template>
      
      <div v-if="orderDetail">
        <!-- 订单基本信息 -->
        <div class="order-basic-info">
          <h2>订单号: {{ orderDetail.orderNo }}</h2>
          <div class="basic-info-grid">
            <div class="info-item">
              <span class="info-label">订单状态:</span>
              <el-tag :type="getStatusType(orderDetail.status)">
                {{ getStatusText(orderDetail.status) }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="info-label">下单时间:</span>
              <span>{{ formatTime(orderDetail.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">订单金额:</span>
              <span class="amount">¥{{ orderDetail.totalAmount }}</span>
            </div>
            <div class="info-item" v-if="orderDetail.remark">
              <span class="info-label">备注:</span>
              <span>{{ orderDetail.remark }}</span>
            </div>
          </div>
        </div>
        
        <el-divider />
        
        <!-- 订单商品列表 -->
        <div class="order-items">
          <h3>商品清单</h3>
          <div class="items-list">
            <div class="item-row" v-for="item in orderDetail.items" :key="item.id">
              <div class="item-info">
                <img :src="item.imageUrl || '/images/placeholder.jpg'" class="item-image" alt="商品图片" />
                <div class="item-details">
                  <h4>{{ item.productName }}</h4>
                  <p class="item-description">{{ item.description || '暂无描述' }}</p>
                  <p class="item-price">单价: ¥{{ item.price }}</p>
                </div>
              </div>
              <div class="item-quantity">
                <span>x{{ item.quantity }}</span>
              </div>
              <div class="item-subtotal">
                <span>¥{{ (item.price * item.quantity).toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <el-divider />
        
        <!-- 订单状态时间线 -->
        <div class="order-timeline">
          <h3>订单进度</h3>
          <el-timeline>
            <el-timeline-item
              v-for="(step, index) in getOrderSteps(orderDetail.status)"
              :key="index"
              :timestamp="step.time"
              :type="step.completed ? step.type : 'info'"
              :hollow="!step.completed"
              :size="step.current ? 'large' : 'normal'"
            >
              <template #dot>
                <el-icon 
                  :size="step.current ? 20 : 16" 
                  :color="step.completed ? getStepColor(step.type) : '#c0c4cc'"
                >
                  <Clock v-if="step.icon === 'Clock'" />
                  <Check v-else-if="step.icon === 'Check'" />
                  <Warning v-else-if="step.icon === 'Warning'" />
                  <Close v-else-if="step.icon === 'Close'" />
                </el-icon>
              </template>
              <div class="timeline-content">
                <h4 :class="{ 'current-step': step.current, 'completed-step': step.completed }">
                  {{ step.title }}
                </h4>
                <p class="step-description">{{ step.description }}</p>
                <div v-if="step.current && orderDetail.status === 'PREPARING'" class="progress-indicator">
                  <el-progress :percentage="60" :show-text="false" :stroke-width="4" />
                  <span class="progress-text">预计还需 10-15 分钟</span>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
        
        <!-- 操作按钮 -->
        <div class="order-actions">
          <el-button 
            v-if="orderDetail.status === 'PENDING'" 
            type="danger" 
            :loading="canceling"
            :disabled="canceling || paying"
            @click="cancelOrder"
          >
            取消订单
          </el-button>
          <el-button 
            v-if="orderDetail.status === 'PENDING'" 
            type="primary" 
            :loading="paying"
            :disabled="canceling || paying"
            @click="payOrder"
          >
            立即支付
          </el-button>
          <el-button 
            v-if="orderDetail.status === 'COMPLETED'" 
            type="success" 
            @click="reorder"
          >
            再来一单
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '../api'
import { ArrowLeft, Check, Clock, Warning, Close } from '@element-plus/icons-vue'

export default {
  name: 'OrderDetail',
  components: {
    ArrowLeft, Check, Clock, Warning, Close
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(false)
    const orderDetail = ref(null)
    const canceling = ref(false)
    const paying = ref(false)
    
    const loadOrderDetail = async () => {
      try {
        loading.value = true
        const response = await orderApi.getOrderDetail(route.params.id)
        orderDetail.value = response.data
      } catch (error) {
        console.error('加载订单详情失败:', error)
        ElMessage.error('订单不存在或已删除')
        router.back()
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
        const date = new Date(timeStr)
        if (isNaN(date.getTime())) {
          return '时间格式错误'
        }
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        })
      } catch (error) {
        console.error('时间格式化错误:', error)
        return '时间解析失败'
      }
    }
    
    const getOrderSteps = (currentStatus) => {
      const order = orderDetail.value
      if (!order) return []
      
      const allSteps = [
        { 
          title: '订单已创建', 
          status: 'PENDING', 
          type: 'primary', 
          icon: 'Clock',
          time: formatTime(order.createTime),
          description: '订单已提交，等待支付'
        },
        { 
          title: '支付完成', 
          status: 'PAID', 
          type: 'success', 
          icon: 'Check',
          time: order.paidTime ? formatTime(order.paidTime) : '',
          description: '支付成功，商家开始准备'
        },
        { 
          title: '开始制作', 
          status: 'PREPARING', 
          type: 'primary', 
          icon: 'Clock',
          time: order.preparingTime ? formatTime(order.preparingTime) : '',
          description: '商家正在精心制作您的美食'
        },
        { 
          title: '制作完成', 
          status: 'READY', 
          type: 'warning', 
          icon: 'Warning',
          time: order.readyTime ? formatTime(order.readyTime) : '',
          description: '美食已制作完成，请及时取餐'
        },
        { 
          title: '订单完成', 
          status: 'COMPLETED', 
          type: 'success', 
          icon: 'Check',
          time: order.completedTime ? formatTime(order.completedTime) : '',
          description: '感谢您的光临，期待下次再见'
        }
      ]
      
      const statusOrder = ['PENDING', 'PAID', 'PREPARING', 'READY', 'COMPLETED']
      const currentIndex = statusOrder.indexOf(currentStatus)
      
      if (currentStatus === 'CANCELLED') {
        return [
          { 
            title: '订单已创建', 
            type: 'info', 
            icon: 'Clock',
            time: formatTime(order.createTime),
            description: '订单已提交',
            completed: true,
            current: false
          },
          { 
            title: '订单已取消', 
            type: 'danger', 
            icon: 'Close',
            time: order.cancelledTime ? formatTime(order.cancelledTime) : formatTime(order.updateTime),
            description: '订单已被取消',
            completed: true,
            current: true
          }
        ]
      }
      
      return allSteps.map((step, index) => ({
        ...step,
        completed: index <= currentIndex,
        current: index === currentIndex
      })).filter((step, index) => index <= currentIndex || index === currentIndex + 1)
    }
    

    
    const getStepColor = (type) => {
      const colorMap = {
        'primary': '#409eff',
        'success': '#67c23a',
        'warning': '#e6a23c',
        'danger': '#f56c6c',
        'info': '#909399'
      }
      return colorMap[type] || '#909399'
    }
    
    const cancelOrder = async () => {
      try {
        await ElMessageBox.confirm('确定要取消这个订单吗？', '确认取消', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        canceling.value = true
        await orderApi.updateOrderStatus(route.params.id, 'CANCELLED')
        ElMessage.success('订单已取消')
        loadOrderDetail()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消订单失败:', error)
          ElMessage.error('取消订单失败')
        }
      } finally {
        canceling.value = false
      }
    }
    
    const payOrder = async () => {
      try {
        await ElMessageBox.confirm('确定要支付这个订单吗？', '确认支付', {
          confirmButtonText: '确定支付',
          cancelButtonText: '取消',
          type: 'info'
        })
        paying.value = true
        await orderApi.updateOrderStatus(route.params.id, 'PAID')
        ElMessage.success('支付成功')
        loadOrderDetail()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('支付失败:', error)
          ElMessage.error('支付失败')
        }
      } finally {
        paying.value = false
      }
    }
    
    const reorder = () => {
      ElMessage.info('功能开发中，敬请期待')
    }
    
    onMounted(() => {
      loadOrderDetail()
    })
    
    return {
      loading,
      orderDetail,
      getStatusType,
      getStatusText,
      formatTime,
      getOrderSteps,
      getStepColor,
      cancelOrder,
      payOrder,
      reorder,
      canceling,
      paying
    }
  }
}
</script>

<style scoped>
.order-detail {
  max-width: 800px;
  margin: 0 auto;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.2em;
  font-weight: bold;
}

.order-basic-info h2 {
  margin: 0 0 20px 0;
  color: #303133;
}

.basic-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-label {
  color: #606266;
  font-weight: 500;
  min-width: 80px;
}

.amount {
  color: #E6A23C;
  font-size: 1.1em;
  font-weight: bold;
}

.order-items h3 {
  margin: 0 0 15px 0;
  color: #303133;
}

.items-list {
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.item-row {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
}

.item-row:last-child {
  border-bottom: none;
}

.item-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.item-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  margin-right: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.item-details h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.item-description {
  color: #909399;
  font-size: 0.9em;
  margin: 0 0 5px 0;
  line-height: 1.4;
}

.item-price {
  color: #606266;
  font-size: 0.9em;
  margin: 0;
  font-weight: 500;
}

.item-quantity {
  min-width: 60px;
  text-align: center;
  color: #606266;
}

.item-subtotal {
  min-width: 80px;
  text-align: right;
  color: #E6A23C;
  font-weight: bold;
}

.order-timeline h3 {
  margin: 0 0 15px 0;
  color: #303133;
}

.timeline-content {
  padding-left: 10px;
}

.timeline-content h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #303133;
  transition: all 0.3s;
}

.timeline-content h4.current-step {
  color: #409eff;
  font-weight: bold;
}

.timeline-content h4.completed-step {
  color: #67c23a;
}

.step-description {
  margin: 0 0 10px 0;
  color: #909399;
  font-size: 14px;
  line-height: 1.4;
}

.progress-indicator {
  margin-top: 10px;
}

.progress-text {
  display: block;
  margin-top: 5px;
  color: #409eff;
  font-size: 12px;
}

.order-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 30px;
  padding: 20px;
}
</style>