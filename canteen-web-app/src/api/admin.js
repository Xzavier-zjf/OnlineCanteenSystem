import request from '@/api/request.js'

export const adminApi = {
  // 仪表板
  getDashboardStats() {
    return request({
      url: '/admin/dashboard',
      method: 'get'
    })
  },

  getRecentOrders() {
    return request({
      url: '/orders/admin/list',
      method: 'get',
      params: { page: 1, size: 5 }
    })
  },

  // 用户管理
  getUserList(params) {
    return request({
      url: '/admin/users',
      method: 'get',
      params
    })
  },

  getUserDetail(userId) {
    return request({
      url: `/admin/users/${userId}`,
      method: 'get'
    })
  },

  updateUserStatus(userId, status) {
    return request({
      url: `/api/admin/users/${userId}/status`,
      method: 'put',
      params: { status }
    })
  },

  toggleUserStatus(userId) {
    return request({
      url: `/api/admin/users/${userId}/toggle-status`,
      method: 'put'
    })
  },

  resetUserPassword(userId) {
    return request({
      url: `/api/admin/users/${userId}/password/reset`,
      method: 'put'
    })
  },

  getUserStatistics() {
    return request({
      url: '/admin/users/statistics',
      method: 'get'
    })
  },

  getUserOrderStats(userId) {
    return request({
      url: `/api/admin/users/${userId}/order-stats`,
      method: 'get'
    })
  },

  // 商户管理
  getPendingMerchants(params) {
    return request({
      url: '/admin/users/merchants/pending',
      method: 'get',
      params
    })
  },

  approveMerchant(userId, approved, reason) {
    return request({
      url: `/api/admin/users/merchants/${userId}/approve`,
      method: 'put',
      params: { approved, reason }
    })
  },

  // 订单管理
  getAllOrders(params) {
    return request({
      url: '/orders/admin/list',
      method: 'get',
      params
    })
  },

  getOrderStatistics() {
    return request({
      url: '/orders/stats/status',
      method: 'get'
    })
  },

  getSalesStatistics(days = 7) {
    return request({
      url: '/orders/stats/sales',
      method: 'get',
      params: { days }
    })
  },

  // 商品管理
  getCategories() {
    return request({
      url: '/products/categories',
      method: 'get'
    })
  },

  getAllProducts(params) {
    return request({
      url: '/products/admin/list',
      method: 'get',
      params
    })
  },

  approveProduct(productId, approved, reason) {
    return request({
      url: `/api/products/${productId}/audit`,
      method: 'put',
      params: { approved, reason }
    })
  },

  deleteProduct(productId, reason) {
    return request({
      url: `/api/products/${productId}`,
      method: 'delete',
      params: { reason }
    })
  },

  // 推荐管理
  getRecommendProducts() {
    return request({
      url: '/admin/recommend/products',
      method: 'get'
    })
  },

  setRecommendProducts(productIds) {
    return request({
      url: '/admin/recommend/products',
      method: 'post',
      data: { productIds }
    })
  },

  getRecommendConfig() {
    return request({
      url: '/admin/recommend-config',
      method: 'get'
    })
  },

  saveRecommendConfig(config) {
    return request({
      url: '/admin/recommend-config',
      method: 'put',
      data: config
    })
  },

  getHotProducts() {
    return request({
      url: '/admin/recommend/hot',
      method: 'get'
    })
  },

  setHotProducts(productIds) {
    return request({
      url: '/admin/recommend/hot',
      method: 'post',
      data: { productIds }
    })
  },

  // 系统统计数据
  getSystemStats() {
    return Promise.all([
      // 获取订单总数
      request({ url: '/orders/count', method: 'get' }),
      // 获取商品总数  
      request({ url: '/products/count', method: 'get' }),
      // 获取今日销售额
      request({ url: '/orders/sales/today', method: 'get' }),
      // 获取总销售额
      request({ url: '/orders/sales/total', method: 'get' })
    ]).then(([orderCount, productCount, todaySales, totalSales]) => ({
      orderCount: orderCount.data || orderCount,
      productCount: productCount.data || productCount,
      todaySales: todaySales.data || todaySales,
      totalSales: totalSales.data || totalSales
    }))
  },

  // 获取系统健康状态
  getSystemHealth() {
    return request({
      url: '/health',
      method: 'get'
    })
  },

  // 获取热门商品统计数据
  getHotProductsStats() {
    return request({
      url: '/admin/recommend/hot/stats',
      method: 'get'
    })
  },

  // 获取商户销售排行
  getMerchantStats() {
    return request({
      url: '/admin/merchants/stats',
      method: 'get'
    })
  },

  // 获取系统配置
  getSystemConfig() {
    return request({
      url: '/admin/system-settings',
      method: 'get'
    })
  },

  // 保存系统配置
  saveSystemConfig(config) {
    return request({
      url: '/admin/system-settings',
      method: 'put',
      data: config
    })
  },

  // 获取系统日志
  getSystemLogs(params = {}) {
    return request({
      url: '/admin/system-logs',
      method: 'get',
      params
    })
  },

  // 获取热门商品趋势
  getHotProductsTrend(params) {
    return request({
      url: '/admin/recommend/hot/trend',
      method: 'get',
      params
    })
  },

  // 获取推荐效果趋势
  getRecommendEffectTrend(params) {
    return request({
      url: '/admin/recommend/effect/trend',
      method: 'get',
      params
    })
  }
}
