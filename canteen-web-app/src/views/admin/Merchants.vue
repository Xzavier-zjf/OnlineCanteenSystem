<template>
  <div class="admin-merchants">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商户管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :model="searchForm" inline>
          <el-form-item label="商户名称">
            <el-input v-model="searchForm.shopName" placeholder="请输入商户名称" clearable />
          </el-form-item>
          <el-form-item label="联系人">
            <el-input v-model="searchForm.contactName" placeholder="请输入联系人" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
              <el-option label="已禁用" value="DISABLED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchMerchants">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 商户统计 -->
      <div class="stats-bar">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="总商户数" :value="stats.totalMerchants || 0" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="待审核" :value="stats.pendingMerchants || 0" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="已通过" :value="stats.approvedMerchants || 0" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="活跃商户" :value="stats.activeMerchants || 0" />
          </el-col>
        </el-row>
      </div>

      <!-- 商户列表 -->
      <el-table :data="merchantList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="shopName" label="店铺名称" width="200" />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="address" label="地址" width="250" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewMerchant(scope.row)">
              详情
            </el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING'"
              size="small" 
              type="success" 
              @click="approveMerchant(scope.row, true)"
            >
              通过
            </el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING'"
              size="small" 
              type="danger" 
              @click="approveMerchant(scope.row, false)"
            >
              拒绝
            </el-button>
            <el-button 
              v-if="scope.row.status === 'APPROVED'"
              size="small" 
              type="warning" 
              @click="toggleMerchantStatus(scope.row)"
            >
              禁用
            </el-button>
            <el-button 
              v-if="scope.row.status === 'DISABLED'"
              size="small" 
              type="success" 
              @click="toggleMerchantStatus(scope.row)"
            >
              启用
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

    <!-- 商户详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="商户详情" width="800px">
      <div v-if="selectedMerchant">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商户ID">{{ selectedMerchant.id }}</el-descriptions-item>
          <el-descriptions-item label="店铺名称">{{ selectedMerchant.shopName }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ selectedMerchant.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ selectedMerchant.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedMerchant.email }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedMerchant.status)">
              {{ getStatusText(selectedMerchant.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ selectedMerchant.createTime }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ selectedMerchant.approveTime || '未审核' }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ selectedMerchant.address }}</el-descriptions-item>
          <el-descriptions-item label="店铺描述" :span="2">{{ selectedMerchant.description || '暂无描述' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 经营数据 -->
        <div style="margin-top: 20px;" v-if="merchantStats">
          <h4>经营数据</h4>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-statistic title="商品数量" :value="merchantStats.productCount || 0" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="订单数量" :value="merchantStats.orderCount || 0" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="总销售额" :value="merchantStats.totalSales || 0" prefix="¥" />
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="approveDialogVisible" title="商户审核" width="500px">
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="approveForm.approved">
            <el-radio :label="true">通过</el-radio>
            <el-radio :label="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input 
            v-model="approveForm.reason" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApprove">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const merchantList = ref([])
const stats = ref({
  totalMerchants: 0,
  pendingMerchants: 0,
  approvedMerchants: 0,
  activeMerchants: 0
})
const searchForm = ref({
  shopName: '',
  contactName: '',
  status: ''
})
const pagination = ref({
  page: 1,
  size: 20,
  total: 0
})

const detailDialogVisible = ref(false)
const selectedMerchant = ref(null)
const merchantStats = ref(null)

const approveDialogVisible = ref(false)
const approveForm = ref({
  approved: true,
  reason: ''
})
const currentMerchant = ref(null)

const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'DISABLED': 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'DISABLED': '已禁用'
  }
  return statusMap[status] || status
}

const loadMerchants = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      ...searchForm.value
    }
    const response = await adminApi.getUserList({
      ...params,
      role: 'MERCHANT'
    })
    merchantList.value = response.data.records || []
    pagination.value.total = response.data.total || 0
  } catch (error) {
    console.error('加载商户列表失败:', error)
    ElMessage.error('加载商户列表失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await adminApi.getUserStatistics()
    if (response && response.data) {
      stats.value = { ...stats.value, ...response.data }
    }
  } catch (error) {
    console.error('加载商户统计失败:', error)
  }
}

const searchMerchants = () => {
  pagination.value.page = 1
  loadMerchants()
}

const resetSearch = () => {
  searchForm.value = {
    shopName: '',
    contactName: '',
    status: ''
  }
  pagination.value.page = 1
  loadMerchants()
}

const viewMerchant = async (merchant) => {
  selectedMerchant.value = merchant
  detailDialogVisible.value = true
  
  // 加载商户统计数据
  try {
    const response = await adminApi.getUserOrderStats(merchant.id)
    merchantStats.value = response.data
  } catch (error) {
    console.error('加载商户统计失败:', error)
  }
}

const approveMerchant = (merchant, approved) => {
  currentMerchant.value = merchant
  approveForm.value.approved = approved
  approveForm.value.reason = ''
  approveDialogVisible.value = true
}

const submitApprove = async () => {
  try {
    await adminApi.approveMerchant(
      currentMerchant.value.id,
      approveForm.value.approved,
      approveForm.value.reason
    )
    ElMessage.success('审核完成')
    approveDialogVisible.value = false
    loadMerchants()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

const toggleMerchantStatus = async (merchant) => {
  const action = merchant.status === 'APPROVED' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}商户 ${merchant.shopName} 吗？`, '确认操作')
    
    await adminApi.toggleUserStatus(merchant.id)
    ElMessage.success(`${action}成功`)
    loadMerchants()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}商户失败:`, error)
      ElMessage.error(`${action}商户失败`)
    }
  }
}

const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  loadMerchants()
}

const handleCurrentChange = (page) => {
  pagination.value.page = page
  loadMerchants()
}

onMounted(() => {
  loadMerchants()
  loadStats()
})
</script>

<style scoped>
.admin-merchants {
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
