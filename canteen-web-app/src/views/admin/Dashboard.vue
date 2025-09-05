<template>
  <div class="admin-dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </div>
          <el-icon class="stat-icon user-icon"><User /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalOrders || 0 }}</div>
            <div class="stat-label">总订单数</div>
          </div>
          <el-icon class="stat-icon order-icon"><ShoppingCart /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalMerchants || 0 }}</div>
            <div class="stat-label">商户数量</div>
          </div>
          <el-icon class="stat-icon merchant-icon"><Shop /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">¥{{ stats.todaySales || '0.00' }}</div>
            <div class="stat-label">今日销量</div>
          </div>
          <el-icon class="stat-icon sales-icon"><Money /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card title="订单状态统计">
          <template #header>
            <span>订单状态统计</span>
          </template>
          <div class="chart-container" ref="orderStatusChart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="销售趋势">
          <template #header>
            <span>销售趋势</span>
          </template>
          <div class="chart-container" ref="salesTrendChart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="table-row">
      <el-col :span="24">
        <el-card title="最近订单">
          <template #header>
            <span>最近订单</span>
            <el-button type="primary" size="small" @click="viewAllOrders">查看全部</el-button>
          </template>
          <el-table :data="recentOrders" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="userName" label="用户" width="120" />
            <el-table-column prop="totalAmount" label="金额" width="100">
              <template #default="scope">
                ¥{{ scope.row.totalAmount }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, ShoppingCart, Shop, Money } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { adminApi } from '@/api/admin'

const stats = ref({})
const recentOrders = ref([])
const orderStatusChart = ref(null)
const salesTrendChart = ref(null)

onMounted(() => {
  loadDashboardData()
  initCharts()
})

const loadDashboardData = async () => {
  try {
    // 加载统计数据
    const statsResponse = await adminApi.getDashboardStats()
    stats.value = statsResponse.data

    // 加载最近订单
    const ordersResponse = await adminApi.getRecentOrders()
    recentOrders.value = ordersResponse.data
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
  }
}

const initCharts = async () => {
  try {
    // 获取订单状态统计数据
    const orderStatsResponse = await adminApi.getOrderStatistics()
    const orderStatsData = orderStatsResponse.data || {}
    
    // 初始化订单状态图表
    const orderChart = echarts.init(orderStatusChart.value)
    const orderOption = {
      title: {
        text: '订单状态分布',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      series: [
        {
          name: '订单状态',
          type: 'pie',
          radius: '50%',
          data: [
            { value: orderStatsData.pending || 0, name: '待支付' },
            { value: orderStatsData.paid || 0, name: '已支付' },
            { value: orderStatsData.preparing || 0, name: '制作中' },
            { value: orderStatsData.ready || 0, name: '待取餐' },
            { value: orderStatsData.completed || 0, name: '已完成' },
            { value: orderStatsData.cancelled || 0, name: '已取消' }
          ].filter(item => item.value > 0), // 只显示有数据的状态
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
    orderChart.setOption(orderOption)

    // 获取销售趋势数据
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(endDate.getDate() - 6) // 最近7天
    
    const salesResponse = await adminApi.getSalesStatistics(
      startDate.toISOString().split('T')[0],
      endDate.toISOString().split('T')[0]
    )
    const salesData = salesResponse.data || {}
    
    // 生成最近7天的日期标签
    const dateLabels = []
    const salesValues = []
    for (let i = 6; i >= 0; i--) {
      const date = new Date()
      date.setDate(date.getDate() - i)
      const dateStr = date.toISOString().split('T')[0]
      const dayName = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()]
      dateLabels.push(dayName)
      salesValues.push(salesData[dateStr] || 0)
    }
    
    // 初始化销售趋势图表
    const salesChart = echarts.init(salesTrendChart.value)
    const salesOption = {
      title: {
        text: '最近7天销售趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis',
        formatter: function(params) {
          return `${params[0].name}<br/>销售额: ¥${params[0].value}`
        }
      },
      xAxis: {
        type: 'category',
        data: dateLabels
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '¥{value}'
        }
      },
      series: [
        {
          name: '销售额',
          data: salesValues,
          type: 'line',
          smooth: true,
          itemStyle: {
            color: '#409EFF'
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [{
                offset: 0, color: 'rgba(64, 158, 255, 0.3)'
              }, {
                offset: 1, color: 'rgba(64, 158, 255, 0.1)'
              }]
            }
          }
        }
      ]
    }
    salesChart.setOption(salesOption)
  } catch (error) {
    console.error('初始化图表失败:', error)
    // 如果获取数据失败，显示空图表
    const orderChart = echarts.init(orderStatusChart.value)
    orderChart.setOption({
      title: { text: '订单状态分布', left: 'center' },
      series: [{ type: 'pie', data: [] }]
    })
    
    const salesChart = echarts.init(salesTrendChart.value)
    salesChart.setOption({
      title: { text: '最近7天销售趋势', left: 'center' },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: [{ type: 'line', data: [] }]
    })
  }
}

const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'PAID': 'success',
    'PREPARING': 'info',
    'READY': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'PREPARING': '准备中',
    'READY': '待取餐',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const viewAllOrders = () => {
  // 跳转到订单管理页面
  console.log('跳转到订单管理页面')
}
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-content {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 40px;
  opacity: 0.3;
}

.user-icon {
  color: #409eff;
}

.order-icon {
  color: #67c23a;
}

.merchant-icon {
  color: #e6a23c;
}

.sales-icon {
  color: #f56c6c;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}

.table-row .el-card :deep(.el-card__header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>