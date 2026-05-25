<template>
  <div class="orders-statistics">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="总订单数" :value="statistics.totalOrders" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="今日订单" :value="statistics.todayOrders" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="总销售额" :value="statistics.totalSales" prefix="¥" :precision="2" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="今日销售额" :value="statistics.todaySales" prefix="¥" :precision="2" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 订单状态分布 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>订单状态分布</span>
          </template>
          <div ref="statusChart" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <!-- 销售趋势 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>销售趋势</span>
            <el-select v-model="trendDays" @change="loadSalesTrend" style="float: right; width: 120px;">
              <el-option label="7天" :value="7" />
              <el-option label="30天" :value="30" />
              <el-option label="90天" :value="90" />
            </el-select>
          </template>
          <div ref="trendChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 热门商品排行 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>热门商品排行</span>
          </template>
          <el-table :data="hotProducts" style="width: 100%">
            <el-table-column prop="rank" label="排名" width="80" />
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="sales" label="销量" width="100" />
            <el-table-column prop="revenue" label="销售额" width="120">
              <template #default="scope">
                ¥{{ scope.row.revenue.toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 商户销售排行 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>商户销售排行</span>
          </template>
          <el-table :data="topMerchants" style="width: 100%">
            <el-table-column prop="rank" label="排名" width="80" />
            <el-table-column prop="name" label="商户名称" />
            <el-table-column prop="orders" label="订单数" width="100" />
            <el-table-column prop="revenue" label="销售额" width="120">
              <template #default="scope">
                ¥{{ scope.row.revenue.toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin.js'
import { echarts } from '@/utils/echarts'

const statistics = ref({
  totalOrders: 0,
  todayOrders: 0,
  totalSales: 0,
  todaySales: 0
})

const trendDays = ref(7)
const statusChart = ref(null)
const trendChart = ref(null)
const hotProducts = ref([])
const topMerchants = ref([])

let statusChartInstance = null
let trendChartInstance = null

const loadStatistics = async () => {
  try {
    const [orderStats, salesStats, todaySalesStats] = await Promise.all([
      adminApi.getOrderStatistics(),
      adminApi.getSalesStatistics(365), // 全年
      adminApi.getSalesStatistics(1) // 今日
    ])

    statistics.value = {
      totalOrders: orderStats.data?.totalOrders || 0,
      todayOrders: orderStats.data?.todayOrders || 0,
      totalSales: salesStats.data?.totalSales || 0,
      todaySales: todaySalesStats.data?.totalSales || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

const loadOrderStatusChart = async () => {
  try {
    const response = await adminApi.getOrderStatistics()
    const statusData = response.data?.statusDistribution || []

    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '订单状态',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '18',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: statusData
        }
      ]
    }

    if (statusChartInstance) {
      statusChartInstance.setOption(option)
    }
  } catch (error) {
    console.error('加载订单状态图表失败:', error)
    ElMessage.error('加载订单状态图表失败')
  }
}

const loadSalesTrend = async () => {
  try {
    const response = await adminApi.getSalesStatistics(trendDays.value)
    const trendData = response.data?.dailyTrend || generateDefaultTrendData()

    const dates = trendData.map(item => item.date)
    const sales = trendData.map(item => item.sales)
    const orders = trendData.map(item => item.orders)

    const option = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['销售额', '订单数']
      },
      xAxis: {
        type: 'category',
        data: dates
      },
      yAxis: [
        {
          type: 'value',
          name: '销售额(元)',
          position: 'left'
        },
        {
          type: 'value',
          name: '订单数',
          position: 'right'
        }
      ],
      series: [
        {
          name: '销售额',
          type: 'line',
          data: sales,
          smooth: true,
          itemStyle: { color: '#409EFF' }
        },
        {
          name: '订单数',
          type: 'bar',
          yAxisIndex: 1,
          data: orders,
          itemStyle: { color: '#67C23A' }
        }
      ]
    }

    if (trendChartInstance) {
      trendChartInstance.setOption(option)
    }
  } catch (error) {
    console.error('加载销售趋势图表失败:', error)
  }
}

const generateDefaultTrendData = () => {
  const data = []
  const today = new Date()
  
  for (let i = trendDays.value - 1; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(date.getDate() - i)
    data.push({
      date: date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }),
      sales: 0,
      orders: 0
    })
  }
  
  return data
}

const loadHotProducts = async () => {
  try {
    const response = await adminApi.getHotProductsStats()
    hotProducts.value = response.data || []
  } catch (error) {
    console.error('加载热门商品失败:', error)
    ElMessage.error('加载热门商品失败')
    hotProducts.value = []
  }
}

const loadTopMerchants = async () => {
  try {
    const response = await adminApi.getMerchantStats()
    topMerchants.value = response.data || []
  } catch (error) {
    console.error('加载商户排行失败:', error)
    ElMessage.error('加载商户排行失败')
    topMerchants.value = []
  }
}

const initCharts = async () => {
  await nextTick()
  
  if (statusChart.value) {
    statusChartInstance = echarts.init(statusChart.value)
    await loadOrderStatusChart()
  }
  
  if (trendChart.value) {
    trendChartInstance = echarts.init(trendChart.value)
    await loadSalesTrend()
  }
}

onMounted(async () => {
  await Promise.all([
    loadStatistics(),
    loadHotProducts(),
    loadTopMerchants()
  ])
  
  await initCharts()
})
</script>

<style scoped>
.orders-statistics {
  padding: 20px;
}

.stat-card {
  text-align: center;
}
</style>
