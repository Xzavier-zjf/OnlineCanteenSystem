import request from '@/utils/request.js'

export const adminApi = {
  // 仪表板相关
  getDashboardStats() {
    return request({
      url: '/api/admin/dashboard/stats',
      method: 'get'
    })
  },

  getRecentOrders() {
    return request({
      url: '/api/admin/orders/recent',
      method: 'get'
    })
  },

  // 用户管理
  getUserList(params) {
    return request({
      url: '/api/admin/users',
      method: 'get',
      params
    })
  },

  getUserDetail(userId) {
    return request({
      url: `/api/admin/users/${userId}`,
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
      url: '/api/admin/users/statistics',
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
      url: '/api/admin/users/merchants/pending',
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
      url: '/api/admin/orders',
      method: 'get',
      params
    })
  },

  getOrderStatistics() {
    return request({
      url: '/api/admin/orders/statistics',
      method: 'get'
    })
  },

  getSalesStatistics(startDate, endDate) {
    return request({
      url: '/api/admin/orders/sales/statistics',
      method: 'get',
      params: { startDate, endDate }
    })
  },

  // 商品管理
  getAllProducts(params) {
    return request({
      url: '/api/admin/products',
      method: 'get',
      params
    })
  },

  approveProduct(productId, approved, reason) {
    return request({
      url: `/api/admin/products/${productId}/approve`,
      method: 'put',
      data: { approved, reason }
    })
  },

  deleteProduct(productId) {
    return request({
      url: `/api/admin/products/${productId}`,
      method: 'delete'
    })
  },

  // 推荐管理
  getRecommendProducts() {
    return request({
      url: '/api/admin/recommend/products',
      method: 'get'
    })
  },

  setRecommendProducts(productIds) {
    return request({
      url: '/api/admin/recommend/products',
      method: 'post',
      data: { productIds }
    })
  },

  getHotProducts() {
    return request({
      url: '/api/admin/recommend/hot',
      method: 'get'
    })
  },

  setHotProducts(productIds) {
    return request({
      url: '/api/admin/recommend/hot',
      method: 'post',
      data: { productIds }
    })
  }
}