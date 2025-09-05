<template>
  <el-container class="merchant-layout">
    <el-aside width="200px" class="merchant-sidebar">
      <div class="logo">
        <h2>商户后台</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="merchant-menu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/merchant/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表板</span>
        </el-menu-item>
        <el-menu-item index="/merchant/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/merchant/orders">
          <el-icon><ShoppingCart /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/merchant/financial">
          <el-icon><Money /></el-icon>
          <span>财务统计</span>
        </el-menu-item>
        <el-menu-item index="/merchant/settings">
          <el-icon><Setting /></el-icon>
          <span>店铺设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="merchant-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/merchant' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userInfo.avatar" />
              <span class="username">{{ userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="merchant-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import { 
  Odometer, 
  Goods, 
  ShoppingCart, 
  Money,
  Setting,
  ArrowDown 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userInfo = ref({
  username: '商户',
  avatar: ''
})

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta.title
  }))
})

onMounted(() => {
  if (userStore.userInfo) {
    userInfo.value = userStore.userInfo
  }
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      break
    case 'logout':
      userStore.logout()
      router.push('/login')
      break
  }
}
</script>

<style scoped>
.merchant-layout {
  height: 100vh;
}

.merchant-sidebar {
  background-color: #304156;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4b;
}

.logo h2 {
  color: #fff;
  margin: 0;
  font-size: 18px;
}

.merchant-menu {
  border-right: none;
}

.merchant-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 12px;
}

.username {
  margin: 0 8px;
  color: #606266;
}

.merchant-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>