<template>
  <div class="profile">
    <el-card class="profile-card" shadow="hover">
      <template #header>
        <div class="profile-header">
          <el-icon size="24"><User /></el-icon>
          <span>个人信息</span>
        </div>
      </template>
      
      <div class="profile-content">
        <div class="avatar-section">
          <el-avatar :size="80" :icon="UserFilled" />
          <h3>{{ userInfo.realName || userInfo.username || '用户' }}</h3>
        </div>
        
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">
            {{ userInfo.username || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="真实姓名">
            {{ userInfo.realName || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ userInfo.email || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ userInfo.phone || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="学院">
            {{ userInfo.college || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="地址">
            {{ userInfo.address || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ formatDate(userInfo.createTime) || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="用户状态">
            <el-tag type="success">正常</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="profile-actions">
          <el-button type="primary" @click="editProfile">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
          <el-button @click="changePassword">
            <el-icon><Lock /></el-icon>
            修改密码
          </el-button>
        </div>
      </div>
    </el-card>
    
    <el-card class="stats-card" shadow="hover">
      <template #header>
        <div class="stats-header">
          <el-icon size="24"><DataAnalysis /></el-icon>
          <span>我的统计</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-number">{{ userStats.totalOrders || 0 }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-number">¥{{ userStats.totalAmount || '0.00' }}</div>
            <div class="stat-label">总消费金额</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-number">{{ userStats.favoriteCount || 0 }}</div>
            <div class="stat-label">收藏商品</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 编辑个人信息对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑个人信息" width="500px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="真实姓名" prop="realName">
          <el-input 
            v-model="editForm.realName" 
            placeholder="请输入真实姓名" 
            @keyup.enter="saveProfile"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input 
            v-model="editForm.email" 
            placeholder="请输入邮箱地址" 
            @keyup.enter="saveProfile"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="editForm.phone" 
            placeholder="请输入手机号" 
            @keyup.enter="saveProfile"
          />
        </el-form-item>
        <el-form-item label="学院" prop="college">
          <el-input 
            v-model="editForm.college" 
            placeholder="请输入学院名称" 
            @keyup.enter="saveProfile"
          />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input 
            v-model="editForm.address" 
            placeholder="请输入地址" 
            @keyup.enter="saveProfile"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            placeholder="请输入当前密码" 
            show-password 
            @keyup.enter="savePassword"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码" 
            show-password 
            @keyup.enter="savePassword"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码" 
            show-password 
            @keyup.enter="savePassword"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePassword" :loading="changingPassword">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, UserFilled, Edit, Lock, DataAnalysis } from '@element-plus/icons-vue'
import { userApi } from '@/api/index'

export default {
  name: 'Profile',
  components: {
    User, 
    UserFilled, 
    Edit, 
    Lock, 
    DataAnalysis
  },
  setup() {
    const userInfo = ref({})
    const userStats = ref({
      totalOrders: 0,
      totalAmount: '0.00',
      favoriteCount: 0
    })
    const editDialogVisible = ref(false)
    const passwordDialogVisible = ref(false)
    const saving = ref(false)
    const changingPassword = ref(false)
    const editFormRef = ref()
    const passwordFormRef = ref()
    
    const editForm = reactive({
      realName: '',
      email: '',
      phone: '',
      college: '',
      address: ''
    })
    
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    const editRules = {
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' }
      ],
      email: [
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      phone: [
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ]
    }
    
    const passwordRules = {
      oldPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== passwordForm.newPassword) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }
    
    const loadUserInfo = async () => {
      const token = localStorage.getItem('token')
      
      if (token && token !== 'undefined' && token !== 'null') {
        try {
          console.log('正在从服务器获取最新用户信息...')
          const result = await userApi.getProfile()
          
          console.log('获取用户信息API响应:', result)
          
          if (result && result.code === 200 && result.data) {
            userInfo.value = result.data
            // 更新本地存储
            localStorage.setItem('userInfo', JSON.stringify(result.data))
            console.log('用户信息已更新:', result.data)
          } else {
            console.error('获取用户信息失败:', result)
            // 如果API失败，尝试从localStorage获取
            const userInfoStr = localStorage.getItem('userInfo')
            if (userInfoStr && userInfoStr !== 'undefined' && userInfoStr !== 'null') {
              try {
                const parsed = JSON.parse(userInfoStr)
                if (parsed && typeof parsed === 'object') {
                  userInfo.value = parsed
                }
              } catch (e) {
                console.error('解析本地用户信息失败:', e)
              }
            }
          }
        } catch (error) {
          console.error('获取用户信息失败:', error)
          // 如果API失败，尝试从localStorage获取
          const userInfoStr = localStorage.getItem('userInfo')
          if (userInfoStr && userInfoStr !== 'undefined' && userInfoStr !== 'null') {
            try {
              const parsed = JSON.parse(userInfoStr)
              if (parsed && typeof parsed === 'object') {
                userInfo.value = parsed
              }
            } catch (e) {
              console.error('解析本地用户信息失败:', e)
            }
          }
        }
      } else {
        ElMessage.error('未登录，请先登录')
      }
    }

    const loadUserStats = async () => {
      const token = localStorage.getItem('token')
      
      if (token && token !== 'undefined' && token !== 'null') {
        try {
          console.log('正在获取用户统计数据...')
          const result = await userApi.getUserStats()
          
          console.log('获取用户统计数据API响应:', result)
          
          if (result && result.code === 200 && result.data) {
            userStats.value = {
              totalOrders: result.data.totalOrders || 0,
              totalAmount: result.data.totalAmount || '0.00',
              favoriteCount: result.data.favoriteCount || 0
            }
            console.log('用户统计数据已更新:', userStats.value)
          } else {
            console.error('获取用户统计数据失败:', result)
          }
        } catch (error) {
          console.error('获取用户统计数据失败:', error)
          // 保持默认值
          userStats.value = {
            totalOrders: 0,
            totalAmount: '0.00',
            favoriteCount: 0
          }
        }
      }
    }
    
    const formatDate = (dateStr) => {
      if (!dateStr) return ''
      try {
        let date
        // 处理不同的日期格式
        if (typeof dateStr === 'string') {
          // 如果是ISO格式或数组格式，直接解析
          date = new Date(dateStr)
        } else if (Array.isArray(dateStr) && dateStr.length >= 3) {
          // 处理数组格式: [2024, 12, 25, 10, 30, 0]
          const [year, month, day, hour = 0, minute = 0, second = 0] = dateStr
          date = new Date(year, month - 1, day, hour, minute, second)
        } else {
          date = new Date(dateStr)
        }
        
        if (isNaN(date.getTime())) {
          console.warn('日期格式无效:', dateStr)
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
        console.error('日期格式化错误:', error, '原始数据:', dateStr)
        return '日期解析失败'
      }
    }
    
    const editProfile = () => {
      // 填充编辑表单
      editForm.realName = userInfo.value.realName || ''
      editForm.email = userInfo.value.email || ''
      editForm.phone = userInfo.value.phone || ''
      editForm.college = userInfo.value.college || ''
      editForm.address = userInfo.value.address || ''
      editDialogVisible.value = true
    }
    
    const changePassword = () => {
      // 重置密码表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      passwordDialogVisible.value = true
    }
    
    const saveProfile = async () => {
      try {
        await editFormRef.value.validate()
        saving.value = true
        
        const token = localStorage.getItem('token')
        if (!token) {
          ElMessage.error('请先登录')
          return
        }
        
        const result = await userApi.updateProfile(editForm)
        
        console.log('更新个人信息API响应:', result)
        
        if (result && result.code === 200) {
          // 重新从服务器获取最新的用户信息
          await loadUserInfo()
          
          ElMessage.success('个人信息更新成功')
          editDialogVisible.value = false
        } else {
          console.error('更新失败:', result)
          ElMessage.error(result?.message || '更新失败')
        }
      } catch (error) {
        console.error('更新个人信息失败:', error)
        // 错误处理已在request.js中统一处理
      } finally {
        saving.value = false
      }
    }
    
    const savePassword = async () => {
      try {
        await passwordFormRef.value.validate()
        changingPassword.value = true
        
        const token = localStorage.getItem('token')
        if (!token) {
          ElMessage.error('请先登录')
          return
        }
        
        const result = await userApi.changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        
        if (result) {
          ElMessage.success('密码修改成功')
          passwordDialogVisible.value = false
          
          // 提示用户重新登录
          await ElMessageBox.alert('密码已修改，请重新登录', '提示', {
            confirmButtonText: '确定',
            type: 'success'
          })
          
          // 清除登录信息并跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
        }
      } catch (error) {
        console.error('修改密码失败:', error)
        // 错误处理已在request.js中统一处理
      } finally {
        changingPassword.value = false
      }
    }
    
    onMounted(() => {
      loadUserInfo()
      loadUserStats()
    })
    
    return {
      userInfo,
      userStats,
      editDialogVisible,
      passwordDialogVisible,
      saving,
      changingPassword,
      editFormRef,
      passwordFormRef,
      editForm,
      passwordForm,
      editRules,
      passwordRules,
      formatDate,
      editProfile,
      changePassword,
      saveProfile,
      savePassword
    }
  }
}
</script>

<style scoped>
.profile {
  max-width: 800px;
  margin: 0 auto;
}

.profile-card, .stats-card {
  margin-bottom: 20px;
}

.profile-header, .stats-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1em;
  font-weight: bold;
}

.profile-content {
  text-align: center;
}

.avatar-section {
  margin-bottom: 30px;
}

.avatar-section h3 {
  margin: 15px 0 0 0;
  color: #303133;
}

.profile-actions {
  margin-top: 30px;
  display: flex;
  gap: 15px;
  justify-content: center;
}

.stat-item {
  text-align: center;
  padding: 20px;
}

.stat-number {
  font-size: 2em;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  color: #606266;
  font-size: 0.9em;
}
</style>