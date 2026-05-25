import request from './request'

const getCurrentMerchantId = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const merchantId = userInfo.id || userInfo.userId
  if (!merchantId) {
    throw new Error('当前商户信息缺失，请重新登录')
  }
  return merchantId
}

export default {
  // 商户仪表盘数据 - 修改为使用现有API
  getDashboardStats() {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/merchant/dashboard/${merchantId}`,
      method: 'get'
    })
  },

  // 获取销售统计
  getSalesStatistics(params) {
    return request({
      url: '/merchant/dashboard/sales',
      method: 'get',
      params
    })
  },

  // 获取订单统计
  getOrderStatistics(params) {
    return request({
      url: '/merchant/dashboard/orders',
      method: 'get',
      params
    })
  },

  // 商品管理 - 使用商户商品API
  getProducts(params) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/products/merchant/${merchantId}/list`,
      method: 'get',
      params
    })
  },

  addProduct(data) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/products/merchant/${merchantId}`,
      method: 'post',
      data
    })
  },

  createProduct(data) {
    return this.addProduct(data)
  },

  updateProduct(id, data) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/products/merchant/${id}?merchantId=${merchantId}`,
      method: 'put',
      data
    })
  },

  deleteProduct(id) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/products/merchant/${id}?merchantId=${merchantId}`,
      method: 'delete'
    })
  },

  // 批量操作商品
  batchUpdateProducts(ids, status) {
    return Promise.all(ids.map(id => this.updateProductStatus(id, status)))
  },

  // 上架/下架商品
  updateProductStatus(id, status) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/products/merchant/${id}/status`,
      method: 'put',
      params: { merchantId, status }
    })
  },

  toggleProductStatus(id, status) {
    return this.updateProductStatus(id, status)
  },

  // 更新商品库存
  updateProductStock(id, stock) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/products/merchant/${id}/stock`,
      method: 'put',
      params: { merchantId, stock }
    })
  },

  // 订单管理 - 修改为使用订单服务API
  getOrders(params) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/orders/merchant/${merchantId}/list`,
      method: 'get',
      params
    })
  },

  getOrderDetail(id) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/orders/merchant/${id}/detail`,
      method: 'get',
      params: { merchantId }
    })
  },

  // 订单状态操作
  startPreparing(orderId) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/orders/merchant/${orderId}/accept`,
      method: 'put',
      params: { merchantId }
    })
  },

  markReady(orderId) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/orders/merchant/${orderId}/status`,
      method: 'put',
      data: { merchantId, status: 'READY' }
    })
  },

  markCompleted(orderId) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/orders/merchant/${orderId}/status`,
      method: 'put',
      data: { merchantId, status: 'COMPLETED' }
    })
  },

  cancelOrder(orderId, reason) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/orders/merchant/${orderId}/reject`,
      method: 'put',
      params: { merchantId, reason }
    })
  },

  refundOrder(orderId, reason) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/orders/merchant/${orderId}/refund`,
      method: 'put',
      data: { merchantId, reason }
    })
  },

  // 通知用户
  notifyCustomer(orderId, message) {
    return request({
      url: `/merchant/orders/${orderId}/notify`,
      method: 'post',
      data: { message }
    })
  },

  // 批量操作订单
  batchUpdateOrders(data) {
    return request({
      url: '/merchant/orders/batch',
      method: 'put',
      data
    })
  },

  // 商户信息管理
  getMerchantInfo() {
    return request({
      url: '/merchant/info',
      method: 'get'
    })
  },

  updateMerchantInfo(data) {
    return request({
      url: '/merchant/info',
      method: 'put',
      data
    })
  },

  // 营业设置
  getBusinessSettings() {
    return request({
      url: '/merchant/shop-settings',
      method: 'get'
    })
  },

  batchUpdateStatus(ids, status) {
    return this.batchUpdateProducts(ids, status)
  },

  batchDelete(ids) {
    return Promise.all(ids.map(id => this.deleteProduct(id)))
  },

  updateBusinessSettings(data) {
    return request({
      url: '/merchant/shop-settings',
      method: 'put',
      data
    })
  },

  // 分类管理 - 修改为使用商品服务API
  getCategories() {
    return request({
      url: '/products/categories',
      method: 'get'
    })
  },

  addCategory(data) {
    return request({
      url: '/merchant/categories',
      method: 'post',
      data
    })
  },

  updateCategory(id, data) {
    return request({
      url: `/merchant/categories/${id}`,
      method: 'put',
      data
    })
  },

  deleteCategory(id) {
    return request({
      url: `/merchant/categories/${id}`,
      method: 'delete'
    })
  },

  // 财务统计 - 使用现有API
  getFinancialStats(params) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/merchant/finance/stats/${merchantId}`,
      method: 'get',
      params
    })
  },

  getIncomeDetails(params) {
    return request({
      url: '/merchant/financial/revenue-details',
      method: 'get',
      params
    })
  },

  // 评价管理
  getReviews(params) {
    return request({
      url: '/merchant/reviews',
      method: 'get',
      params
    })
  },

  replyReview(reviewId, reply) {
    return request({
      url: `/merchant/reviews/${reviewId}/reply`,
      method: 'post',
      data: { reply }
    })
  },

  // 数据导出
  exportOrders(params) {
    return request({
      url: '/merchant/export/orders',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },

  exportProducts(params) {
    return request({
      url: '/merchant/export/products',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },

  exportFinancial(params) {
    return request({
      url: '/merchant/export/financial',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },

  // 图片上传
  uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/upload/image',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 批量上传图片
  batchUploadImages(files) {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    return request({
      url: '/upload/images',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取商品统计数据
  getProductStats(productId) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/products/merchant/${productId}/detail`,
      method: 'get',
      params: { merchantId }
    })
  },

  // 获取热销商品
  getTopProducts(params) {
    return request({
      url: '/merchant/products/top',
      method: 'get',
      params
    })
  },

  // 获取收入明细
  getRevenueDetails(params) {
    return request({
      url: '/merchant/financial/revenue-details',
      method: 'get',
      params
    })
  },

  // 导出财务报表
  exportFinancialReport(params) {
    return request({
      url: '/merchant/financial/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },

  // 修改密码
  changePassword(data) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/merchant/password/${merchantId}`,
      method: 'put',
      data: {
        oldPassword: data.oldPassword || data.currentPassword,
        newPassword: data.newPassword
      }
    })
  },

  // 获取通知设置
  getNotificationSettings() {
    return request({
      url: '/users/notification-settings',
      method: 'get'
    })
  },

  // 更新通知设置
  updateNotificationSettings(data) {
    return request({
      url: '/users/notification-settings',
      method: 'put',
      data
    })
  },

  // 获取店铺信息 - 修改为使用现有的API
  getShopInfo() {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/merchant/info/${merchantId}`,
      method: 'get'
    })
  },

  // 更新店铺信息 - 修改为使用现有API
  updateShopInfo(data) {
    const merchantId = getCurrentMerchantId()
    return request({
      url: `/merchant/info/${merchantId}`,
      method: 'put',
      data
    })
  },

  // 上传店铺Logo
  uploadShopLogo(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/upload/image',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}
