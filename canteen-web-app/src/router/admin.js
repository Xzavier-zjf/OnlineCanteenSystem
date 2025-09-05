export default [
  {
    path: '/admin',
    component: () => import('@/views/admin/Layout.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '仪表板' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'merchants',
        name: 'AdminMerchants',
        component: () => import('@/views/admin/Merchants.vue'),
        meta: { title: '商户管理' }
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/Products.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'products/audit',
        name: 'AdminProductsAudit',
        component: () => import('@/views/admin/ProductsAudit.vue'),
        meta: { title: '商品审核' }
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/Orders.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'orders/statistics',
        name: 'AdminOrdersStatistics',
        component: () => import('@/views/admin/OrdersStatistics.vue'),
        meta: { title: '订单统计' }
      },
      {
        path: 'recommend/products',
        name: 'AdminRecommendProducts',
        component: () => import('@/views/admin/RecommendProducts.vue'),
        meta: { title: '推荐商品' }
      },
      {
        path: 'recommend/hot',
        name: 'AdminRecommendHot',
        component: () => import('@/views/admin/RecommendHot.vue'),
        meta: { title: '热销榜单' }
      },
      {
        path: 'system',
        name: 'AdminSystem',
        component: () => import('@/views/admin/System.vue'),
        meta: { title: '系统设置' }
      }
    ]
  }
]