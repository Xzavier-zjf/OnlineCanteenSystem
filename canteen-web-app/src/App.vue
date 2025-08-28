<template>
  <div id="app">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-content">
          <div class="logo">
            <el-icon size="24" color="#409EFF"><Shop /></el-icon>
            <span class="logo-text">高校食堂订餐系统</span>
          </div>
          
          <div class="nav-menu">
            <el-menu
              :default-active="activeIndex"
              mode="horizontal"
              @select="handleSelect"
              background-color="transparent"
              text-color="#333"
              active-text-color="#409EFF"
            >
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/products">餐品浏览</el-menu-item>
              <el-menu-item index="/orders" v-if="isLoggedIn">我的订单</el-menu-item>
              <el-menu-item index="/status">服务状态</el-menu-item>
              <el-menu-item index="/test">系统测试</el-menu-item>
            </el-menu>
          </div>
          
          <div class="user-actions">
            <template v-if="!isLoggedIn">
              <el-button type="primary" @click="$router.push('/login')">登录</el-button>
              <el-button @click="$router.push('/register')">注册</el-button>
            </template>
            <template v-else>
              <el-dropdown @command="handleCommand">
                <span class="user-info">
                  <el-icon><User /></el-icon>
                  {{ userInfo.username }}
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                    <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </div>
        </div>
      </el-header>
      
      <!-- 主要内容区域 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
      
      <!-- 底部 -->
      <el-footer class="footer">
        <div class="footer-content">
          <p>&copy; 2025 高校食堂订餐系统. All rights reserved.</p>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Shop, User, ArrowDown, Food } from '@element-plus/icons-vue'

export default {
  name: 'App',
  components: {
    Shop,
    User,
    ArrowDown,
    Food
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    const userInfo = ref({})
    const token = ref(localStorage.getItem('token'))
    const loginStatus = ref(false)
    
    const isLoggedIn = computed(() => {
      // 使用响应式变量而不是直接读取localStorage
      return loginStatus.value
    })
    
    const checkLoginStatus = () => {
      const currentToken = localStorage.getItem('token')
      const currentUserInfo = localStorage.getItem('userInfo')
      const isValid = !!(currentToken && currentToken !== 'null' && currentToken !== 'undefined' && 
                        currentUserInfo && currentUserInfo !== 'null' && currentUserInfo !== 'undefined')
      
      console.log('检查登录状态:', {
        token: currentToken,
        userInfo: currentUserInfo,
        isValid: isValid
      })
      
      loginStatus.value = isValid
      return isValid
    }
    
    const activeIndex = computed(() => {
      return route.path
    })
    
    const handleSelect = (key) => {
      router.push(key)
    }
    
    const handleCommand = (command) => {
      if (command === 'logout') {
        logout()
      } else if (command === 'profile') {
        // 跳转到个人信息页面
        router.push('/profile')
      }
    }
    
    const logout = () => {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      token.value = null
      userInfo.value = {}
      ElMessage.success('退出登录成功')
      router.push('/login')
    }
    
    const loadUserInfo = () => {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr && userInfoStr !== 'undefined') {
        try {
          userInfo.value = JSON.parse(userInfoStr)
        } catch (e) {
          console.error('解析用户信息失败:', e)
          // 清除无效的用户信息
          localStorage.removeItem('userInfo')
          localStorage.removeItem('token')
          userInfo.value = {}
        }
      } else {
        userInfo.value = {}
      }
    }
    
    // 监听存储变化
    // 监听存储变化
    const updateLoginStatus = () => {
      token.value = localStorage.getItem('token')
      loadUserInfo()
      checkLoginStatus()
      console.log('登录状态已更新')
    }
    
    onMounted(() => {
      loadUserInfo()
      checkLoginStatus()
      // 监听存储变化事件
      window.addEventListener('storage', updateLoginStatus)
      // 监听自定义登录事件
      window.addEventListener('userLogin', updateLoginStatus)
    })
    
    // 暴露更新方法给全局使用
    window.updateAppLoginStatus = updateLoginStatus
    
    return {
      isLoggedIn,
      userInfo,
      activeIndex,
      handleSelect,
      handleCommand
    }
  }
}
</script>

<style scoped>
.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.logo-text {
  margin-left: 8px;
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.main-content {
  min-height: calc(100vh - 120px);
  padding: 20px;
  background-color: #f5f7fa;
}

.footer {
  background-color: #fff;
  border-top: 1px solid #e6e6e6;
  padding: 0;
}

.footer-content {
  text-align: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  color: #666;
}

/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}
</style>