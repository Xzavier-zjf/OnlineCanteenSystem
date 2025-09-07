<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="login-header">
          <el-icon size="32" color="#409EFF"><User /></el-icon>
          <h2>用户登录</h2>
        </div>
      </template>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名" 
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password 
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%;">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>还没有账号？<el-button type="text" @click="$router.push('/register')">立即注册</el-button></p>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { userApi } from '@/api/index'

export default {
  name: 'Login',
  components: {
    User
  },
  setup() {
    const router = useRouter()
    const loginFormRef = ref()
    const loading = ref(false)
    
    const loginForm = reactive({
      username: '',
      password: ''
    })
    
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ]
    }
    
    const handleLogin = async () => {
      try {
        await loginFormRef.value.validate()
        loading.value = true
        
        // 调用登录API
        const response = await userApi.login({
          username: loginForm.username,
          password: loginForm.password
        })
        
        console.log('登录API响应:', response)
        
        // 检查响应格式 - 后端返回的是 {code: 200, message: "登录成功", data: {...}}
        if (response && response.code === 200 && response.data) {
          const loginData = response.data
          
          // 清除旧的缓存数据
          localStorage.removeItem('userInfo')
          localStorage.removeItem('token')
          
          // 保存token
          localStorage.setItem('token', loginData.token)
          
          // 登录成功后，重新获取完整的用户信息
          try {
            const userInfoResponse = await userApi.getProfile()
            if (userInfoResponse && userInfoResponse.code === 200) {
              localStorage.setItem('userInfo', JSON.stringify(userInfoResponse.data))
              console.log('登录成功，获取到完整用户信息:', userInfoResponse.data)
            } else {
              // 如果获取用户信息失败，使用登录返回的基本信息
              localStorage.setItem('userInfo', JSON.stringify({
                userId: loginData.userId,
                username: loginData.username,
                realName: loginData.realName,
                role: loginData.role
              }))
            }
          } catch (error) {
            console.warn('获取用户信息失败，使用登录返回的基本信息:', error)
            localStorage.setItem('userInfo', JSON.stringify({
              userId: loginData.userId,
              username: loginData.username,
              realName: loginData.realName,
              role: loginData.role
            }))
          }
          
          // 触发App组件更新登录状态
          if (window.updateAppLoginStatus) {
            window.updateAppLoginStatus()
          }
          
          // 触发存储事件
          window.dispatchEvent(new Event('storage'))
          
          ElMessage.success('登录成功')
          
          // 根据用户角色跳转到对应首页
          const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
          const role = userInfo.role || loginData.role
          
          switch (role) {
            case 'ADMIN':
              router.push('/admin/dashboard')
              break
            case 'MERCHANT':
              router.push('/merchant/dashboard')
              break
            case 'USER':
            default:
              router.push('/')
              break
          }
        } else {
          const errorMsg = response?.message || '登录失败: 用户名或密码错误'
          ElMessage.error(errorMsg)
        }
        
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error(error.message || '登录失败，请检查用户名和密码')
      } finally {
        loading.value = false
      }
    }
    
    return {
      loginForm,
      rules,
      loginFormRef,
      loading,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.login-header h2 {
  margin: 0;
  color: #303133;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
}

.login-footer p {
  margin: 10px 0;
  color: #606266;
}
</style>