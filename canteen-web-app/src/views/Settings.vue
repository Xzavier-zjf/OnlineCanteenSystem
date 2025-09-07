<template>
  <div class="settings-container">
    <el-card class="settings-card">
      <template #header>
        <div class="settings-header">
          <el-icon size="24"><Setting /></el-icon>
          <span>系统设置</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card">
        <!-- 个人信息设置 -->
        <el-tab-pane label="个人信息" name="profile">
          <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="profileForm.username" disabled />
                </el-form-item>
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
                </el-form-item>
                <el-form-item label="手机号码" prop="phone">
                  <el-input v-model="profileForm.phone" placeholder="请输入手机号码" />
                </el-form-item>
                <el-form-item label="邮箱地址" prop="email">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱地址" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="头像上传">
                  <el-upload
                    class="avatar-uploader"
                    action="/api/upload/avatar"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess"
                    :before-upload="beforeAvatarUpload"
                  >
                    <img v-if="profileForm.avatar" :src="profileForm.avatar" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                </el-form-item>
                <el-form-item label="个人简介">
                  <el-input 
                    v-model="profileForm.bio" 
                    type="textarea" 
                    :rows="4" 
                    placeholder="请输入个人简介"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="savingProfile">
                保存个人信息
              </el-button>
              <el-button @click="resetProfile">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 账户安全 -->
        <el-tab-pane label="账户安全" name="security">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>修改密码</span>
                </template>
                <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
                  <el-form-item label="当前密码" prop="currentPassword">
                    <el-input 
                      v-model="passwordForm.currentPassword" 
                      type="password" 
                      placeholder="请输入当前密码"
                      show-password
                    />
                  </el-form-item>
                  <el-form-item label="新密码" prop="newPassword">
                    <el-input 
                      v-model="passwordForm.newPassword" 
                      type="password" 
                      placeholder="请输入新密码"
                      show-password
                    />
                  </el-form-item>
                  <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input 
                      v-model="passwordForm.confirmPassword" 
                      type="password" 
                      placeholder="请再次输入新密码"
                      show-password
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="changePassword" :loading="changingPassword">
                      修改密码
                    </el-button>
                    <el-button @click="resetPasswordForm">重置</el-button>
                  </el-form-item>
                </el-form>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>登录记录</span>
                </template>
                <el-table :data="Array.isArray(loginRecords) ? loginRecords : []" style="width: 100%" max-height="300">
                  <el-table-column prop="loginTime" label="登录时间" width="150" />
                  <el-table-column prop="ip" label="IP地址" width="120" />
                  <el-table-column prop="device" label="设备" width="120" />
                  <el-table-column prop="location" label="地点" width="100" />
                  <el-table-column prop="status" label="状态" width="80">
                    <template #default="scope">
                      <el-tag :type="scope.row.status === '成功' ? 'success' : 'danger'">
                        {{ scope.row.status }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 通知设置 -->
        <el-tab-pane label="通知设置" name="notification">
          <el-form :model="notificationSettings" label-width="150px">
            <el-card>
              <template #header>
                <span>消息通知</span>
              </template>
              <el-form-item label="系统消息通知">
                <el-switch v-model="notificationSettings.systemMessage" />
                <span class="setting-desc">接收系统重要消息通知</span>
              </el-form-item>
              <el-form-item label="邮件通知">
                <el-switch v-model="notificationSettings.emailNotification" />
                <span class="setting-desc">通过邮件接收通知</span>
              </el-form-item>
              <el-form-item label="短信通知">
                <el-switch v-model="notificationSettings.smsNotification" />
                <span class="setting-desc">通过短信接收重要通知</span>
              </el-form-item>
              
              <!-- 角色特定通知设置 -->
              <template v-if="userRole === 'USER'">
                <el-form-item label="订单状态通知">
                  <el-switch v-model="notificationSettings.orderStatus" />
                  <span class="setting-desc">订单状态变更时通知</span>
                </el-form-item>
                <el-form-item label="优惠活动通知">
                  <el-switch v-model="notificationSettings.promotions" />
                  <span class="setting-desc">接收优惠活动和折扣信息</span>
                </el-form-item>
              </template>

              <template v-if="userRole === 'MERCHANT'">
                <el-form-item label="新订单通知">
                  <el-switch v-model="notificationSettings.newOrder" />
                  <span class="setting-desc">有新订单时立即通知</span>
                </el-form-item>
                <el-form-item label="库存预警通知">
                  <el-switch v-model="notificationSettings.stockAlert" />
                  <span class="setting-desc">商品库存不足时通知</span>
                </el-form-item>
              </template>

              <template v-if="userRole === 'ADMIN'">
                <el-form-item label="用户注册通知">
                  <el-switch v-model="notificationSettings.userRegistration" />
                  <span class="setting-desc">有新用户注册时通知</span>
                </el-form-item>
                <el-form-item label="系统异常通知">
                  <el-switch v-model="notificationSettings.systemError" />
                  <span class="setting-desc">系统出现异常时通知</span>
                </el-form-item>
              </template>

              <el-form-item>
                <el-button type="primary" @click="saveNotificationSettings" :loading="savingNotification">
                  保存通知设置
                </el-button>
              </el-form-item>
            </el-card>
          </el-form>
        </el-tab-pane>

        <!-- 偏好设置 -->
        <el-tab-pane label="偏好设置" name="preferences">
          <el-form :model="preferenceSettings" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-card>
                  <template #header>
                    <span>界面设置</span>
                  </template>
                  <el-form-item label="主题模式">
                    <el-radio-group v-model="preferenceSettings.theme">
                      <el-radio label="light">浅色模式</el-radio>
                      <el-radio label="dark">深色模式</el-radio>
                      <el-radio label="auto">跟随系统</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item label="语言设置">
                    <el-select v-model="preferenceSettings.language" placeholder="选择语言">
                      <el-option label="简体中文" value="zh-CN" />
                      <el-option label="English" value="en-US" />
                    </el-select>
                  </el-form-item>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card>
                  <template #header>
                    <span>功能设置</span>
                  </template>
                  <el-form-item label="自动保存">
                    <el-switch v-model="preferenceSettings.autoSave" />
                    <span class="setting-desc">自动保存表单数据</span>
                  </el-form-item>
                  <el-form-item label="页面大小">
                    <el-input-number 
                      v-model="preferenceSettings.pageSize" 
                      :min="10" 
                      :max="100" 
                      :step="10"
                    />
                    <span class="setting-desc">每页显示条数</span>
                  </el-form-item>
                </el-card>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="savePreferenceSettings" :loading="savingPreference">
                保存偏好设置
              </el-button>
              <el-button @click="resetPreferenceSettings">恢复默认</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 商户专属设置 -->
        <el-tab-pane v-if="userRole === 'MERCHANT'" label="店铺设置" name="shop">
          <el-form :model="shopSettings" :rules="shopRules" ref="shopFormRef" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-card>
                  <template #header>
                    <span>基本信息</span>
                  </template>
                  <el-form-item label="店铺名称" prop="shopName">
                    <el-input v-model="shopSettings.shopName" placeholder="请输入店铺名称" />
                  </el-form-item>
                  <el-form-item label="店铺描述">
                    <el-input 
                      v-model="shopSettings.description" 
                      type="textarea" 
                      :rows="3" 
                      placeholder="请输入店铺描述"
                    />
                  </el-form-item>
                  <el-form-item label="联系电话" prop="phone">
                    <el-input v-model="shopSettings.phone" placeholder="请输入联系电话" />
                  </el-form-item>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card>
                  <template #header>
                    <span>营业设置</span>
                  </template>
                  <el-form-item label="店铺状态">
                    <el-switch 
                      v-model="shopSettings.isOpen" 
                      active-text="营业中" 
                      inactive-text="暂停营业"
                    />
                  </el-form-item>
                  <el-form-item label="自动接单">
                    <el-switch v-model="shopSettings.autoAcceptOrder" />
                    <span class="setting-desc">开启后新订单将自动接受</span>
                  </el-form-item>
                </el-card>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="saveShopSettings" :loading="savingShop">
                保存店铺设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 管理员专属设置 -->
        <el-tab-pane v-if="userRole === 'ADMIN'" label="系统管理" name="system">
          <!-- 系统统计信息 -->
          <el-row :gutter="20" style="margin-bottom: 20px;">
            <el-col :span="24">
              <el-card>
                <template #header>
                  <span>系统统计信息</span>
                </template>
                <el-row :gutter="20">
                  <el-col :span="6">
                    <div class="stat-item">
                      <div class="stat-value">{{ systemStats.totalUsers }}</div>
                      <div class="stat-label">总用户数</div>
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="stat-item">
                      <div class="stat-value">{{ systemStats.totalMerchants }}</div>
                      <div class="stat-label">商户数量</div>
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="stat-item">
                      <div class="stat-value">{{ systemStats.totalOrders }}</div>
                      <div class="stat-label">总订单数</div>
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="stat-item">
                      <div class="stat-value">{{ systemStats.systemUptime }}</div>
                      <div class="stat-label">系统运行时间</div>
                    </div>
                  </el-col>
                </el-row>
                <el-row :gutter="20" style="margin-top: 20px;">
                  <el-col :span="8">
                    <div class="stat-item">
                      <div class="stat-value">{{ systemStats.diskUsage }}</div>
                      <div class="stat-label">磁盘使用率</div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="stat-item">
                      <div class="stat-value">{{ systemStats.memoryUsage }}</div>
                      <div class="stat-label">内存使用率</div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="stat-item">
                      <div class="stat-value">{{ systemStats.cpuUsage }}</div>
                      <div class="stat-label">CPU使用率</div>
                    </div>
                  </el-col>
                </el-row>
              </el-card>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>系统配置</span>
                </template>
                <el-form :model="systemSettings" label-width="150px">
                  <el-form-item label="系统维护模式">
                    <el-switch v-model="systemSettings.maintenanceMode" />
                    <span class="setting-desc">开启后系统将进入维护模式</span>
                  </el-form-item>
                  <el-form-item label="用户注册开关">
                    <el-switch v-model="systemSettings.allowRegistration" />
                    <span class="setting-desc">是否允许新用户注册</span>
                  </el-form-item>
                  <el-form-item label="系统公告">
                    <el-input 
                      v-model="systemSettings.systemAnnouncement" 
                      type="textarea" 
                      :rows="3" 
                      placeholder="请输入系统公告"
                    />
                  </el-form-item>
                </el-form>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>数据管理</span>
                </template>
                <el-form label-width="120px">
                  <el-form-item label="数据备份">
                    <el-button type="primary" @click="backupData" :loading="backingUp">
                      立即备份
                    </el-button>
                  </el-form-item>
                  <el-form-item label="清理日志">
                    <el-button type="warning" @click="clearLogs" :loading="clearingLogs">
                      清理日志
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-card>
            </el-col>
          </el-row>
          <el-form-item>
            <el-button type="primary" @click="saveSystemSettings" :loading="savingSystem">
              保存系统设置
            </el-button>
          </el-form-item>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Plus } from '@element-plus/icons-vue'
import settingsApi from '@/api/settings'

// 获取用户角色
const userRole = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.role || 'USER'
})

const activeTab = ref('profile')
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const shopFormRef = ref(null)

// 加载状态
const savingProfile = ref(false)
const changingPassword = ref(false)
const savingNotification = ref(false)
const savingPreference = ref(false)
const savingShop = ref(false)
const savingSystem = ref(false)
const backingUp = ref(false)
const clearingLogs = ref(false)

// 个人信息表单
const profileForm = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  avatar: '',
  bio: ''
})

// 密码修改表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 通知设置
const notificationSettings = reactive({
  systemMessage: true,
  emailNotification: false,
  smsNotification: true,
  // 用户特有
  orderStatus: true,
  promotions: false,
  // 商户特有
  newOrder: true,
  stockAlert: true,
  // 管理员特有
  userRegistration: true,
  systemError: true
})

// 偏好设置
const preferenceSettings = reactive({
  theme: 'light',
  language: 'zh-CN',
  autoSave: true,
  pageSize: 20
})

// 店铺设置（商户专用）
const shopSettings = reactive({
  shopName: '',
  description: '',
  phone: '',
  isOpen: true,
  autoAcceptOrder: false
})

// 系统设置（管理员专用）
const systemSettings = reactive({
  maintenanceMode: false,
  allowRegistration: true,
  systemAnnouncement: ''
})

// 系统统计信息（管理员专用）
const systemStats = reactive({
  totalUsers: 0,
  totalMerchants: 0,
  totalOrders: 0,
  systemUptime: '0天 0小时 0分钟',
  diskUsage: '0%',
  memoryUsage: '0%',
  cpuUsage: '0%'
})

// 加载系统统计信息（管理员专用）
const loadSystemStats = async () => {
  if (userRole.value !== 'ADMIN') return
  
  try {
    const response = await settingsApi.getSystemStats()
    Object.assign(systemStats, response.data)
  } catch (error) {
    console.error('加载系统统计信息失败:', error)
  }
}

// 登录记录
const loginRecords = ref([])

// 加载登录记录
const loadLoginRecords = async () => {
  try {
    const response = await settingsApi.getLoginRecords()
    loginRecords.value = response.data
  } catch (error) {
    console.error('加载登录记录失败:', error)
  }
}

// 表单验证规则
const profileRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const shopRules = {
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ]
}

// 加载用户信息
const loadUserProfile = async () => {
  try {
    const response = await settingsApi.getUserProfile()
    const userInfo = response.data
    Object.assign(profileForm, {
      username: userInfo.username || '',
      realName: userInfo.realName || '',
      phone: userInfo.phone || '',
      email: userInfo.email || '',
      avatar: userInfo.avatar || '',
      bio: userInfo.bio || ''
    })
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  }
}

// 保存个人信息
const saveProfile = async () => {
  if (!profileFormRef.value) return
  
  try {
    await profileFormRef.value.validate()
    savingProfile.value = true
    
    // 调用API更新用户信息
    await settingsApi.updateUserProfile(profileForm)
    
    // 更新本地存储
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    Object.assign(userInfo, profileForm)
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
    
    ElMessage.success('个人信息保存成功')
  } catch (error) {
    console.error('保存个人信息失败:', error)
    ElMessage.error('保存个人信息失败')
  } finally {
    savingProfile.value = false
  }
}

// 重置个人信息
const resetProfile = () => {
  loadUserProfile()
}

// 头像上传成功
const handleAvatarSuccess = async (response, file) => {
  try {
    const uploadResponse = await settingsApi.uploadAvatar(file.raw)
    profileForm.avatar = uploadResponse.data.avatarUrl
    ElMessage.success('头像上传成功')
  } catch (error) {
    console.error('头像上传失败:', error)
    // 降级到本地URL
    profileForm.avatar = URL.createObjectURL(file.raw)
    ElMessage.success('头像上传成功')
  }
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPGOrPNG) {
    ElMessage.error('头像只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true
    
    // 调用API修改密码
    await settingsApi.changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    
    ElMessage.success('密码修改成功')
    resetPasswordForm()
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改密码失败，请检查当前密码是否正确')
  } finally {
    changingPassword.value = false
  }
}

// 重置密码表单
const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate()
  }
}

// 保存通知设置
const saveNotificationSettings = async () => {
  savingNotification.value = true
  try {
    await settingsApi.updateNotificationSettings(notificationSettings)
    ElMessage.success('通知设置保存成功')
  } catch (error) {
    console.error('保存通知设置失败:', error)
    ElMessage.error('保存通知设置失败')
  } finally {
    savingNotification.value = false
  }
}

// 加载通知设置
const loadNotificationSettings = async () => {
  try {
    const response = await settingsApi.getNotificationSettings()
    Object.assign(notificationSettings, response.data)
  } catch (error) {
    console.error('加载通知设置失败:', error)
  }
}

// 保存偏好设置
const savePreferenceSettings = async () => {
  savingPreference.value = true
  try {
    await settingsApi.updatePreferenceSettings(preferenceSettings)
    ElMessage.success('偏好设置保存成功')
    
    // 应用主题设置
    applyThemeSettings()
  } catch (error) {
    console.error('保存偏好设置失败:', error)
    ElMessage.error('保存偏好设置失败')
  } finally {
    savingPreference.value = false
  }
}

// 加载偏好设置
const loadPreferenceSettings = async () => {
  try {
    const response = await settingsApi.getPreferenceSettings()
    Object.assign(preferenceSettings, response.data)
    applyThemeSettings()
  } catch (error) {
    console.error('加载偏好设置失败:', error)
  }
}

// 应用主题设置
const applyThemeSettings = () => {
  const html = document.documentElement
  if (preferenceSettings.theme === 'dark') {
    html.classList.add('dark')
  } else if (preferenceSettings.theme === 'light') {
    html.classList.remove('dark')
  } else {
    // 跟随系统
    const isDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    html.classList.toggle('dark', isDark)
  }
}

// 重置偏好设置
const resetPreferenceSettings = () => {
  Object.assign(preferenceSettings, {
    theme: 'light',
    language: 'zh-CN',
    autoSave: true,
    pageSize: 20
  })
}

// 保存店铺设置（商户专用）
const saveShopSettings = async () => {
  if (!shopFormRef.value) return
  
  try {
    await shopFormRef.value.validate()
    savingShop.value = true
    
    await settingsApi.updateShopSettings(shopSettings)
    ElMessage.success('店铺设置保存成功')
  } catch (error) {
    console.error('保存店铺设置失败:', error)
    ElMessage.error('保存店铺设置失败')
  } finally {
    savingShop.value = false
  }
}

// 加载店铺设置（商户专用）
const loadShopSettings = async () => {
  if (userRole.value !== 'MERCHANT') return
  
  try {
    const response = await settingsApi.getShopSettings()
    Object.assign(shopSettings, response.data)
  } catch (error) {
    console.error('加载店铺设置失败:', error)
  }
}

// 保存系统设置（管理员专用）
const saveSystemSettings = async () => {
  savingSystem.value = true
  try {
    await settingsApi.updateSystemSettings(systemSettings)
    ElMessage.success('系统设置保存成功')
  } catch (error) {
    console.error('保存系统设置失败:', error)
    ElMessage.error('保存系统设置失败')
  } finally {
    savingSystem.value = false
  }
}

// 加载系统设置（管理员专用）
const loadSystemSettings = async () => {
  if (userRole.value !== 'ADMIN') return
  
  try {
    const response = await settingsApi.getSystemSettings()
    Object.assign(systemSettings, response.data)
  } catch (error) {
    console.error('加载系统设置失败:', error)
  }
}

// 数据备份
const backupData = async () => {
  try {
    await ElMessageBox.confirm(
      '数据备份将创建系统完整备份文件，包含所有用户数据、订单信息等。确定要进行数据备份吗？', 
      '确认数据备份', 
      {
        confirmButtonText: '开始备份',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    backingUp.value = true
    const response = await settingsApi.backupData()
    
    ElMessage.success(`数据备份成功！备份文件：${response.data.backupFile}，大小：${response.data.size}`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('数据备份失败:', error)
      ElMessage.error('数据备份失败，请稍后重试')
    }
  } finally {
    backingUp.value = false
  }
}

// 清理日志
const clearLogs = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将清理30天前的系统日志文件，包括访问日志、错误日志等。清理后无法恢复，确定要继续吗？', 
      '确认清理日志', 
      {
        confirmButtonText: '确定清理',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    clearingLogs.value = true
    const response = await settingsApi.clearLogs()
    
    ElMessage.success(`日志清理成功！清理了 ${response.data.clearedFiles} 个文件，释放空间 ${response.data.clearedSize}`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('日志清理失败:', error)
      ElMessage.error('日志清理失败，请稍后重试')
    }
  } finally {
    clearingLogs.value = false
  }
}

onMounted(async () => {
  // 加载基础数据
  await loadUserProfile()
  await loadLoginRecords()
  await loadNotificationSettings()
  await loadPreferenceSettings()
  
  // 根据角色加载特定数据
  if (userRole.value === 'MERCHANT') {
    await loadShopSettings()
  } else if (userRole.value === 'ADMIN') {
    await loadSystemSettings()
    await loadSystemStats()
  }
})
</script>

<style scoped>
.settings-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.settings-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.settings-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.setting-desc {
  margin-left: 10px;
  font-size: 12px;
  color: #666;
}

:deep(.el-tabs__content) {
  padding: 20px;
}

:deep(.el-card) {
  margin-bottom: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 深色主题支持 */
.dark .stat-value {
  color: #79bbff;
}

.dark .stat-label {
  color: #ccc;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .settings-container {
    padding: 10px;
  }
  
  .stat-item {
    margin-bottom: 15px;
  }
  
  .stat-value {
    font-size: 20px;
  }
}
</style>