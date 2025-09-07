import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Products from '../views/Products.vue'
import Orders from '../views/Orders.vue'
import OrderDetail from '../views/OrderDetail.vue'
import Profile from '../views/Profile.vue'
import Test from '../views/Test.vue'
import ServiceStatus from '../views/ServiceStatus.vue'
import adminRoutes from './admin'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/products',
    name: 'Products',
    component: Products
  },
  {
    path: '/orders',
    name: 'Orders',
    component: Orders,
    meta: { requiresAuth: true }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: OrderDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { requiresAuth: true }
  },
  {
    path: '/test',
    name: 'Test',
    component: Test
  },
  {
    path: '/status',
    name: 'ServiceStatus',
    component: ServiceStatus
  },
  // 商户路由
  {
    path: '/merchant/dashboard',
    name: 'MerchantDashboard',
    component: () => import('../views/merchant/Dashboard.vue'),
    meta: { requiresAuth: true, role: 'MERCHANT' }
  },
  {
    path: '/merchant/products',
    name: 'MerchantProducts',
    component: () => import('../views/merchant/Products.vue'),
    meta: { requiresAuth: true, role: 'MERCHANT' }
  },
  {
    path: '/merchant/orders',
    name: 'MerchantOrders',
    component: () => import('../views/merchant/Orders.vue'),
    meta: { requiresAuth: true, role: 'MERCHANT' }
  },
  {
    path: '/merchant/financial',
    name: 'MerchantFinancial',
    component: () => import('../views/merchant/Financial.vue'),
    meta: { requiresAuth: true, role: 'MERCHANT' }
  },
  {
    path: '/merchant/settings',
    name: 'MerchantSettings',
    component: () => import('../views/merchant/Settings.vue'),
    meta: { requiresAuth: true, role: 'MERCHANT' }
  },
  // 管理员路由
  ...adminRoutes,
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('../views/Settings.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 获取用户角色首页
const getRoleHomePage = (role) => {
  switch (role) {
    case 'ADMIN':
      return '/admin/dashboard'
    case 'MERCHANT':
      return '/merchant/dashboard'
    case 'USER':
    default:
      return '/'
  }
}

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  
  // 如果已登录且访问登录页，重定向到对应首页
  if (to.path === '/login' && token && userInfo.role) {
    next(getRoleHomePage(userInfo.role))
    return
  }
  
  // 如果已登录且访问根路径，重定向到对应首页
  if (to.path === '/' && token && userInfo.role && userInfo.role !== 'USER') {
    next(getRoleHomePage(userInfo.role))
    return
  }
  
  // 检查是否需要认证
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }
  
  // 检查角色权限
  if (to.meta.role && userInfo.role !== to.meta.role) {
    // 如果角色不匹配，重定向到对应角色的首页
    next(getRoleHomePage(userInfo.role))
    return
  }
  
  next()
})

export default router