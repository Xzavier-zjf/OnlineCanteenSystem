<template>
  <div class="admin-orders">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :model="searchForm" inline>
          <el-form-item label="订单号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="searchForm.userName" placeholder="请输入用户名" clearable />
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="待支付" value="PENDING" />
              <el-option label="已支付" value="PAID" />
              <el-option label="准备中" value="PREPARING" />
              <el-option label="待取餐" value="READY" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchOrders">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 订单统计 -->
      <div class="stats-bar">
        <el-row :gutter="20">
          <el-col :span="4">
            <el-statistic title="总订单数" :value="stats.totalOrders || 0" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="待支付" :value="stats.pendingOrders || 0" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="已完成" :value="stats.completedOrders || 0" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="已取消" :value="stats.cancelledOrders || 0" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="今日销售额" :value="stats.todaySales || 0" prefix="¥" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="总销售额" :value="stats.totalSales || 0" prefix="¥" />
          </el-col>
        </el-row>
      </div>

      <!-- 订单列表 -->
      <el-table :data="orderList" style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="merchantName" label="商户" width="150" />
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
        <el-table-column prop="paymentMethod" label="支付方式" width="120" />
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewOrderDetail(scope.row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="800px">
      <div v-if="selectedOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ selectedOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="用户">{{ selectedOrder.userName }}</el-descriptions-item>
          <el-descriptions-item label="商户">{{ selectedOrder.merchantName }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ selectedOrder.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ selectedOrder.paymentMethod }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(selectedOrder.status)">
              {{ getStatusText(selectedOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ selectedOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ selectedOrder.paidTime || '未支付' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ selectedOrder.completedTime || '未完成' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ selectedOrder.remark || '无' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 订单商品 -->
        <div style="margin-top: 20px;">
          <h4>订单商品</h4>
          <el-table :data="selectedOrder.items || []" style="width: 100%">
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="price" label="单价">
              <template #default="scope">
                ¥{{ scope.row.price }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" />
            <el-table-column prop="subtotal" label="小计">
              <template #default="scope">
                ¥{{ scope.row.subtotal }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const orderList = ref([])
const stats = ref({
  totalOrders: 0,
  pendingOrders: 0,
  processingOrders: 0,
  completedOrders: 0,
  cancelledOrders: 0,
  todayOrders: 0,
  todaySales: 0,
  totalSales: 0
})
const searchForm = ref({
  orderNo: '',
  userName: '',
  status: ''
})
const dateRange = ref([])
const pagination = ref({
  page: 1,
  size: 20,
  total: 0
})

const detailDialogVisible = ref(false)
const selectedOrder = ref(null)

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

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      ...searchForm.value
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const response = await adminApi.getAllOrders(params)
    orderList.value = response.data.records || []
    pagination.value.total = response.data.total || 0
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await adminApi.getOrderStatistics()
    if (response && response.data) {
      stats.value = { ...stats.value, ...response.data }
    }
  } catch (error) {
    console.error('加载订单统计失败:', error)
    // 保持默认值，不覆盖stats.value
  }
}

const searchOrders = () => {
  pagination.value.page = 1
  loadOrders()
}

const resetSearch = () => {
  searchForm.value = {
    orderNo: '',
    userName: '',
    status: ''
  }
  dateRange.value = []
  pagination.value.page = 1
  loadOrders()
}

const viewOrderDetail = (order) => {
  selectedOrder.value = order
  detailDialogVisible.value = true
}

const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  loadOrders()
}

const handleCurrentChange = (page) => {
  pagination.value.page = page
  loadOrders()
}

onMounted(() => {
  loadOrders()
  loadStats()
})
</script>

<style scoped>
.admin-orders {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.stats-bar {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
