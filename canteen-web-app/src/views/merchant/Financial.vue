<template>
  <div class="merchant-financial">
    <!-- 财务概览 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">¥{{ financialStats.todayRevenue || '0.00' }}</div>
            <div class="stat-label">今日营收</div>
          </div>
          <el-icon class="stat-icon revenue-icon"><Money /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">¥{{ financialStats.monthRevenue || '0.00' }}</div>
            <div class="stat-label">区间营收</div>
          </div>
          <el-icon class="stat-icon month-icon"><TrendCharts /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ financialStats.todayOrders || 0 }}</div>
            <div class="stat-label">今日订单</div>
          </div>
          <el-icon class="stat-icon order-icon"><ShoppingCart /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">¥{{ financialStats.avgOrderValue || '0.00' }}</div>
            <div class="stat-label">客单价</div>
          </div>
          <el-icon class="stat-icon avg-icon"><DataAnalysis /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 时间筛选和图表 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>营收趋势分析</span>
              <div class="date-picker">
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  @change="onDateRangeChange"
                />
                <el-button type="primary" @click="refreshData" style="margin-left: 10px;">
                  刷新数据
                </el-button>
              </div>
            </div>
          </template>
          
          <div class="chart-container" ref="revenueChart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细财务数据 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>收入明细</span>
          </template>
          <el-table :data="Array.isArray(revenueDetails) ? revenueDetails : []" style="width: 100%" max-height="400">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="orderCount" label="订单数" width="80" />
            <el-table-column prop="revenue" label="营收" width="100">
              <template #default="scope">
                ¥{{ scope.row.revenue }}
              </template>
            </el-table-column>
            <el-table-column prop="avgOrderValue" label="客单价">
              <template #default="scope">
                ¥{{ scope.row.avgOrderValue }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>商品销售排行</span>
          </template>
          <el-table :data="Array.isArray(topProducts) ? topProducts : []" style="width: 100%" max-height="400">
            <el-table-column prop="rank" label="排名" width="60" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="sales" label="销量" width="80" />
            <el-table-column prop="revenue" label="营收" width="100">
              <template #default="scope">
                ¥{{ scope.row.revenue }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 财务报表导出 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>财务报表导出</span>
          </template>
          <div class="export-section">
            <el-form :model="exportForm" inline>
              <el-form-item label="报表类型">
                <el-select v-model="exportForm.type" placeholder="请选择报表类型">
                  <el-option label="日报" value="daily" />
                  <el-option label="周报" value="weekly" />
                  <el-option label="月报" value="monthly" />
                  <el-option label="自定义" value="custom" />
                </el-select>
              </el-form-item>
              <el-form-item label="时间范围" v-if="exportForm.type === 'custom'">
                <el-date-picker
                  v-model="exportForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item label="导出格式">
                <el-select v-model="exportForm.format" placeholder="请选择格式">
                  <el-option label="CSV" value="csv" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="exportReport" :loading="exporting">
                  导出报表
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Money, TrendCharts, ShoppingCart, DataAnalysis } from '@element-plus/icons-vue'
import { echarts } from '@/utils/echarts'
import merchantApi from '@/api/merchant.js'

export default {
  name: 'MerchantFinancial',
  components: {
    Money, TrendCharts, ShoppingCart, DataAnalysis
  },
  setup() {
    const loading = ref(false)
    const exporting = ref(false)
    
    // 响应式数据
    const financialStats = reactive({
      todayRevenue: '0.00',
      monthRevenue: '0.00',
      todayOrders: 0,
      avgOrderValue: '0.00',
      monthlyRevenues: []
    })
    
    const dateRange = ref([])
    const revenueChart = ref(null)
    const revenueDetails = ref([]) // 确保是数组
    const topProducts = ref([]) // 确保是数组
    
    const exportForm = reactive({
      type: 'monthly',
      dateRange: [],
      format: 'csv'
    })
    
    const toMoney = (value) => {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    }

    const todayText = () => new Date().toISOString().split('T')[0]

    // 加载财务统计数据
    const loadFinancialStats = async () => {
      try {
        loading.value = true
        const response = await merchantApi.getFinancialStats({
          startDate: dateRange.value[0],
          endDate: dateRange.value[1]
        })
        
        if (response.data) {
          Object.assign(financialStats, {
            todayRevenue: toMoney(response.data.todayRevenue),
            monthRevenue: toMoney(response.data.totalRevenue),
            avgOrderValue: toMoney(response.data.avgOrderAmount),
            monthlyRevenues: Array.isArray(response.data.monthlyRevenues)
              ? response.data.monthlyRevenues
              : []
          })
        }
      } catch (error) {
        console.error('财务统计加载失败:', error)
        ElMessage.error('财务统计加载失败')
        Object.assign(financialStats, {
          todayRevenue: '0.00',
          monthRevenue: '0.00',
          todayOrders: 0,
          avgOrderValue: '0.00',
          monthlyRevenues: []
        })
      } finally {
        loading.value = false
      }
    }
    
    // 加载收入明细
    const loadRevenueDetails = async () => {
      try {
        const response = await merchantApi.getRevenueDetails({
          startDate: dateRange.value[0],
          endDate: dateRange.value[1]
        })
        
        const details = response.data?.list || []
        revenueDetails.value = details.map(item => ({
          ...item,
          revenue: toMoney(item.revenue),
          avgOrderValue: toMoney(item.avgOrderValue)
        }))

        const todayDetail = details.find(item => item.date === todayText())
        financialStats.todayOrders = Number(todayDetail?.orderCount || 0)
      } catch (error) {
        console.error('收入明细加载失败:', error)
        ElMessage.error('收入明细加载失败')
        revenueDetails.value = []
      }
    }
    
    // 加载热销商品
    const loadTopProducts = async () => {
      try {
        const response = await merchantApi.getTopProducts({ limit: 10 })
        
        const products = Array.isArray(response.data) ? response.data : []
        topProducts.value = products.map((item, index) => ({
          ...item,
          rank: item.rank || index + 1,
          productName: item.productName || item.name || '未命名商品',
          sales: item.sales || item.salesCount || 0,
          revenue: toMoney(item.revenue)
        }))
      } catch (error) {
        console.error('热销商品加载失败:', error)
        ElMessage.error('热销商品加载失败')
        topProducts.value = []
      }
    }
    
    // 初始化图表
    const initChart = async () => {
      await nextTick()
      if (revenueChart.value) {
        const chartInstance = echarts.init(revenueChart.value)
        const chartData = Array.isArray(financialStats.monthlyRevenues)
          ? financialStats.monthlyRevenues
          : []
        const xData = chartData.map(item => item.month || item.date || '')
        const yData = chartData.map(item => Number(item.revenue || 0))
        
        const option = {
          title: {
            text: '营收趋势',
            left: 'center'
          },
          tooltip: {
            trigger: 'axis',
            formatter: function(params) {
              return `${params[0].name}<br/>营收: ¥${params[0].value}`
            }
          },
          xAxis: {
            type: 'category',
            data: xData
          },
          yAxis: {
            type: 'value',
            axisLabel: {
              formatter: '¥{value}'
            }
          },
          series: [{
            data: yData,
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
          }]
        }
        
        chartInstance.setOption(option)
        
        // 响应式调整
        window.addEventListener('resize', () => {
          chartInstance.resize()
        })
      }
    }
    
    // 日期范围变化
    const onDateRangeChange = (dates) => {
      if (dates && dates.length === 2) {
        refreshData()
      }
    }
    
    // 刷新数据
    const refreshData = async () => {
      await Promise.all([
        loadFinancialStats(),
        loadRevenueDetails(),
        loadTopProducts()
      ])
      initChart()
    }
    
    // 导出报表
    const exportReport = async () => {
      try {
        exporting.value = true
        
        const params = {
          type: exportForm.type,
          format: exportForm.format
        }
        
        if (exportForm.type === 'custom' && exportForm.dateRange.length === 2) {
          params.startDate = exportForm.dateRange[0]
          params.endDate = exportForm.dateRange[1]
        }
        
        const blob = await merchantApi.exportFinancialReport(params)
        const downloadBlob = blob instanceof Blob
          ? blob
          : new Blob([blob], { type: 'text/csv;charset=utf-8' })
        const url = window.URL.createObjectURL(downloadBlob)
        const link = document.createElement('a')
        link.href = url
        link.download = `merchant-financial-report-${new Date().toISOString().slice(0, 10)}.csv`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        ElMessage.success('报表导出成功')
      } catch (error) {
        console.error('导出报表失败:', error)
        ElMessage.error('导出功能暂时不可用')
      } finally {
        exporting.value = false
      }
    }
    
    // 初始化
    onMounted(() => {
      // 设置默认日期范围
      const endDate = new Date()
      const startDate = new Date()
      startDate.setDate(endDate.getDate() - 29)
      
      dateRange.value = [
        startDate.toISOString().split('T')[0],
        endDate.toISOString().split('T')[0]
      ]
      
      // 加载数据
      refreshData()
    })
    
    return {
      loading,
      exporting,
      financialStats,
      dateRange,
      revenueChart,
      revenueDetails,
      topProducts,
      exportForm,
      onDateRangeChange,
      refreshData,
      exportReport
    }
  }
}
</script>

<style scoped>
.merchant-financial {
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
  font-size: 28px;
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
  font-size: 36px;
  opacity: 0.3;
}

.revenue-icon {
  color: #409eff;
}

.month-icon {
  color: #67c23a;
}

.order-icon {
  color: #e6a23c;
}

.avg-icon {
  color: #f56c6c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.date-picker {
  display: flex;
  align-items: center;
}

.chart-container {
  height: 400px;
}

.export-section {
  padding: 20px;
  background-color: #f5f5f5;
  border-radius: 4px;
}
</style>
