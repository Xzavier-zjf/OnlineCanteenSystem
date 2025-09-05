<template>
  <el-container class="admin-layout">
    <el-aside width="200px" class="admin-sidebar">
      <div class="logo">
        <h2>管理后台</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="admin-menu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表板</span>
        </el-menu-item>
        
        <el-sub-menu index="users">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/admin/users">用户列表</el-menu-item>
          <el-menu-item index="/admin/merchants">商户管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="products">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </template>
          <el-menu-item index="/admin/products">商品列表</el-menu-item>
          <el-menu-item index="/admin/products/audit">商品审核</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="orders">
          <template #title>
            <el-icon><ShoppingCart /></el-icon>
            <span>订单管理</span>
          </template>
          <el-menu-item index="/admin/orders">订单列表</el-menu-item>
          <el-menu-item index="/admin/orders/statistics">订单统计</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="recommend">
          <template #title>
            <el-icon><Star /></el-icon>
            <span>推荐管理</span>
          </template>
          <el-menu-item index="/admin/recommend/products">推荐商品</el-menu-item>
          <el-menu-item index="/admin/recommend/hot">热销榜单</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
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

      <el-main class="admin-main">
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
  User, 
  Goods, 
  ShoppingCart, 
  Star, 
  ArrowDown 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userInfo = ref({
  username: '管理员',
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
  // 获取用户信息
  if (userStore.userInfo) {
    userInfo.value = userStore.userInfo
  }
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      // 跳转到个人资料页面
      break
    case 'logout':
      // 退出登录
      userStore.logout()
      router.push('/login')
      break
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.admin-sidebar {
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

.admin-menu {
  border-right: none;
}

.admin-header {
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

.admin-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>