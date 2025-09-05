<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :model="searchForm" inline>
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
              <el-option label="普通用户" value="USER" />
              <el-option label="商户" value="MERCHANT" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="启用" value="ACTIVE" />
              <el-option label="禁用" value="DISABLED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchUsers">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 用户列表 -->
      <el-table :data="userList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">
              {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ scope.row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button 
              size="small" 
              :type="scope.row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="toggleUserStatus(scope.row)"
            >
              {{ scope.row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="primary" @click="resetPassword(scope.row)">
              重置密码
            </el-button>
            <el-button size="small" type="info" @click="viewUserDetail(scope.row)">
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

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="用户详情" width="600px">
      <div v-if="selectedUser">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ selectedUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ selectedUser.username }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedUser.email }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ selectedUser.phone }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ getRoleText(selectedUser.role) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedUser.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ selectedUser.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ selectedUser.createTime }}</el-descriptions-item>
          <el-descriptions-item label="最后登录">{{ selectedUser.lastLoginTime || '未登录' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 用户订单统计 -->
        <div style="margin-top: 20px;">
          <h4>订单统计</h4>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-statistic title="总订单数" :value="userStats.totalOrders || 0" />
            </el-col>
            <el-col :span="12">
              <el-statistic title="总消费金额" :value="userStats.totalSpent || 0" prefix="¥" />
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const userList = ref([])
const searchForm = ref({
  username: '',
  role: '',
  status: ''
})
const pagination = ref({
  page: 1,
  size: 20,
  total: 0
})

const detailDialogVisible = ref(false)
const selectedUser = ref(null)
const userStats = ref({})

const getRoleType = (role) => {
  const roleMap = {
    'USER': 'primary',
    'MERCHANT': 'success',
    'ADMIN': 'danger'
  }
  return roleMap[role] || 'info'
}

const getRoleText = (role) => {
  const roleMap = {
    'USER': '普通用户',
    'MERCHANT': '商户',
    'ADMIN': '管理员'
  }
  return roleMap[role] || role
}

const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      ...searchForm.value
    }
    const response = await adminApi.getUserList(params)
    userList.value = response.data.records || []
    pagination.value.total = response.data.total || 0
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const searchUsers = () => {
  pagination.value.page = 1
  loadUsers()
}

const resetSearch = () => {
  searchForm.value = {
    username: '',
    role: '',
    status: ''
  }
  pagination.value.page = 1
  loadUsers()
}

const toggleUserStatus = async (user) => {
  const action = user.status === 'ACTIVE' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户 ${user.username} 吗？`, '确认操作')
    
    await adminApi.toggleUserStatus(user.id)
    ElMessage.success(`${action}成功`)
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}用户失败:`, error)
      ElMessage.error(`${action}用户失败`)
    }
  }
}

const resetPassword = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要重置用户 ${user.username} 的密码吗？`, '确认操作')
    
    const response = await adminApi.resetUserPassword(user.id)
    ElMessage.success(`密码重置成功，新密码：${response.data.newPassword}`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败')
    }
  }
}

const viewUserDetail = async (user) => {
  selectedUser.value = user
  detailDialogVisible.value = true
  
  // 加载用户统计数据
  try {
    const response = await adminApi.getUserOrderStats(user.id)
    userStats.value = response.data
  } catch (error) {
    console.error('加载用户统计失败:', error)
  }
}

const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  loadUsers()
}

const handleCurrentChange = (page) => {
  pagination.value.page = page
  loadUsers()
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>