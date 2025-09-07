<template>
  <div class="merchant-settings">
    <el-row :gutter="20">
      <!-- 店铺基本信息 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>店铺基本信息</span>
          </template>
          <el-form :model="shopInfo" label-width="100px" :rules="shopRules" ref="shopFormRef">
            <el-form-item label="店铺名称" prop="shopName">
              <el-input v-model="shopInfo.shopName" placeholder="请输入店铺名称" />
            </el-form-item>
            <el-form-item label="店铺描述" prop="description">
              <el-input 
                v-model="shopInfo.description" 
                type="textarea" 
                :rows="4" 
                placeholder="请输入店铺描述"
              />
            </el-form-item>
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="shopInfo.phone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="营业地址" prop="address">
              <el-input v-model="shopInfo.address" placeholder="请输入营业地址" />
            </el-form-item>
            <el-form-item label="营业时间">
              <el-row :gutter="10">
                <el-col :span="12">
                  <el-time-picker
                    v-model="shopInfo.openTime"
                    placeholder="开始时间"
                    format="HH:mm"
                    value-format="HH:mm"
                  />
                </el-col>
                <el-col :span="12">
                  <el-time-picker
                    v-model="shopInfo.closeTime"
                    placeholder="结束时间"
                    format="HH:mm"
                    value-format="HH:mm"
                  />
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item label="店铺状态">
              <el-switch 
                v-model="shopInfo.isOpen" 
                active-text="营业中" 
                inactive-text="暂停营业"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveShopInfo" :loading="saving">
                保存信息
              </el-button>
              <el-button @click="resetShopInfo">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 店铺Logo上传 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>店铺Logo</span>
          </template>
          <div class="logo-upload">
            <el-upload
              class="logo-uploader"
              :action="uploadAction"
              :show-file-list="false"
              :on-success="handleLogoSuccess"
              :before-upload="beforeLogoUpload"
              :headers="uploadHeaders"
            >
              <img v-if="shopInfo.logoUrl" :src="shopInfo.logoUrl" class="logo" />
              <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="logo-tips">
              <p>建议尺寸：200x200像素</p>
              <p>支持格式：JPG、PNG</p>
              <p>文件大小：不超过2MB</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 账户安全 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>账户安全</span>
          </template>
          <el-form :model="passwordForm" label-width="100px" :rules="passwordRules" ref="passwordFormRef">
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

      <!-- 通知设置 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>通知设置</span>
          </template>
          <el-form :model="notificationSettings" label-width="120px">
            <el-form-item label="新订单通知">
              <el-switch v-model="notificationSettings.newOrder" />
            </el-form-item>
            <el-form-item label="订单状态变更">
              <el-switch v-model="notificationSettings.orderStatus" />
            </el-form-item>
            <el-form-item label="库存预警">
              <el-switch v-model="notificationSettings.stockAlert" />
            </el-form-item>
            <el-form-item label="系统消息">
              <el-switch v-model="notificationSettings.systemMessage" />
            </el-form-item>
            <el-form-item label="邮件通知">
              <el-switch v-model="notificationSettings.emailNotification" />
            </el-form-item>
            <el-form-item label="短信通知">
              <el-switch v-model="notificationSettings.smsNotification" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveNotificationSettings" :loading="savingNotification">
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 营业设置 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>营业设置</span>
          </template>
          <el-form :model="businessSettings" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="自动接单">
                  <el-switch v-model="businessSettings.autoAcceptOrder" />
                  <span class="setting-desc">开启后新订单将自动接受</span>
                </el-form-item>
                <el-form-item label="预计制作时间">
                  <el-input-number 
                    v-model="businessSettings.preparationTime" 
                    :min="5" 
                    :max="120" 
                    :step="5"
                  />
                  <span class="setting-desc">分钟</span>
                </el-form-item>
                <el-form-item label="最大接单量">
                  <el-input-number 
                    v-model="businessSettings.maxOrdersPerHour" 
                    :min="1" 
                    :max="100"
                  />
                  <span class="setting-desc">每小时最多接单数量</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="起送金额">
                  <el-input-number 
                    v-model="businessSettings.minOrderAmount" 
                    :min="0" 
                    :precision="2"
                  />
                  <span class="setting-desc">元</span>
                </el-form-item>
                <el-form-item label="配送费">
                  <el-input-number 
                    v-model="businessSettings.deliveryFee" 
                    :min="0" 
                    :precision="2"
                  />
                  <span class="setting-desc">元</span>
                </el-form-item>
                <el-form-item label="包装费">
                  <el-input-number 
                    v-model="businessSettings.packagingFee" 
                    :min="0" 
                    :precision="2"
                  />
                  <span class="setting-desc">元</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="saveBusinessSettings" :loading="savingBusiness">
                保存设置
              </el-button>
              <el-button @click="resetBusinessSettings">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import merchantApi from '@/api/merchant.js'

const shopFormRef = ref(null)
const passwordFormRef = ref(null)
const saving = ref(false)
const changingPassword = ref(false)
const savingNotification = ref(false)
const savingBusiness = ref(false)

const uploadAction = ref('/api/upload/logo')
const uploadHeaders = ref({
  'Authorization': `Bearer ${localStorage.getItem('token')}`
})

const shopInfo = ref({
  shopName: '美味小厨',
  description: '提供新鲜美味的家常菜，用心做好每一道菜',
  phone: '13800138000',
  address: '学校食堂二楼A区',
  openTime: '08:00',
  closeTime: '20:00',
  isOpen: true,
  logoUrl: ''
})

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const notificationSettings = ref({
  newOrder: true,
  orderStatus: true,
  stockAlert: true,
  systemMessage: true,
  emailNotification: false,
  smsNotification: true
})

const businessSettings = ref({
  autoAcceptOrder: false,
  preparationTime: 15,
  maxOrdersPerHour: 30,
  minOrderAmount: 10.00,
  deliveryFee: 2.00,
  packagingFee: 1.00
})

const shopRules = {
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 20, message: '店铺名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入营业地址', trigger: 'blur' }
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
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loadShopInfo = async () => {
  try {
    const response = await merchantApi.getShopInfo()
    if (response.data) {
      shopInfo.value = { ...shopInfo.value, ...response.data }
    }
  } catch (error) {
    console.error('加载店铺信息失败:', error)
    ElMessage.warning('加载店铺信息失败，使用默认数据')
  }
}

const saveShopInfo = async () => {
  if (!shopFormRef.value) return
  
  try {
    await shopFormRef.value.validate()
    saving.value = true
    
    // 这里应该调用API保存店铺信息
    await merchantApi.updateShopInfo(shopInfo.value)
    
    ElMessage.success('店铺信息保存成功')
  } catch (error) {
    console.error('保存店铺信息失败:', error)
    ElMessage.error('保存店铺信息失败')
  } finally {
    saving.value = false
  }
}

const resetShopInfo = () => {
  loadShopInfo()
}

const handleLogoSuccess = (response, file) => {
  shopInfo.value.logoUrl = URL.createObjectURL(file.raw)
  ElMessage.success('Logo上传成功')
}

const beforeLogoUpload = (file) => {
  const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPGOrPNG) {
    ElMessage.error('Logo只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('Logo大小不能超过 2MB!')
    return false
  }
  return true
}

const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true
    
    await merchantApi.changePassword(passwordForm.value)
    
    ElMessage.success('密码修改成功')
    resetPasswordForm()
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改密码失败')
  } finally {
    changingPassword.value = false
  }
}

const resetPasswordForm = () => {
  passwordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate()
  }
}

const saveNotificationSettings = async () => {
  savingNotification.value = true
  try {
    await merchantApi.updateNotificationSettings(notificationSettings.value)
    
    ElMessage.success('通知设置保存成功')
  } catch (error) {
    console.error('保存通知设置失败:', error)
    ElMessage.error('保存通知设置失败')
  } finally {
    savingNotification.value = false
  }
}

const saveBusinessSettings = async () => {
  savingBusiness.value = true
  try {
    await merchantApi.updateBusinessSettings(businessSettings.value)
    
    ElMessage.success('营业设置保存成功')
  } catch (error) {
    console.error('保存营业设置失败:', error)
    ElMessage.error('保存营业设置失败')
  } finally {
    savingBusiness.value = false
  }
}

const resetBusinessSettings = () => {
  businessSettings.value = {
    autoAcceptOrder: false,
    preparationTime: 15,
    maxOrdersPerHour: 30,
    minOrderAmount: 10.00,
    deliveryFee: 2.00,
    packagingFee: 1.00
  }
}

onMounted(() => {
  loadShopInfo()
})
</script>

<style scoped>
.merchant-settings {
  padding: 20px;
}

.logo-upload {
  text-align: center;
}

.logo-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 200px;
  height: 200px;
  margin: 0 auto;
}

.logo-uploader:hover {
  border-color: #409eff;
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 200px;
  height: 200px;
  line-height: 200px;
  text-align: center;
}

.logo {
  width: 200px;
  height: 200px;
  display: block;
  object-fit: cover;
}

.logo-tips {
  margin-top: 10px;
  font-size: 12px;
  color: #666;
}

.logo-tips p {
  margin: 5px 0;
}

.setting-desc {
  margin-left: 10px;
  font-size: 12px;
  color: #666;
}
</style>
