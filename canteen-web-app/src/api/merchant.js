import request from './request'

export default {
  // 商户仪表盘数据
  getDashboardStats() {
    return request({
      url: '/merchant/dashboard/stats',
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

  // 商品管理
  getProducts(params) {
    return request({
      url: '/merchant/products',
      method: 'get',
      params
    })
  },

  addProduct(data) {
    return request({
      url: '/merchant/products',
      method: 'post',
      data
    })
  },

  updateProduct(id, data) {
    return request({
      url: `/merchant/products/${id}`,
      method: 'put',
      data
    })
  },

  deleteProduct(id) {
    return request({
      url: `/merchant/products/${id}`,
      method: 'delete'
    })
  },

  // 批量操作商品
  batchUpdateProducts(data) {
    return request({
      url: '/merchant/products/batch',
      method: 'put',
      data
    })
  },

  // 上架/下架商品
  toggleProductStatus(id, status) {
    return request({
      url: `/merchant/products/${id}/status`,
      method: 'put',
      data: { status }
    })
  },

  // 更新商品库存
  updateProductStock(id, stock) {
    return request({
      url: `/merchant/products/${id}/stock`,
      method: 'put',
      data: { stock }
    })
  },

  // 订单管理
  getOrders(params) {
    return request({
      url: '/merchant/orders',
      method: 'get',
      params
    })
  },

  getOrderDetail(id) {
    return request({
      url: `/merchant/orders/${id}`,
      method: 'get'
    })
  },

  // 订单状态操作
  startPreparing(orderId) {
    return request({
      url: `/merchant/orders/${orderId}/start-preparing`,
      method: 'put'
    })
  },

  markReady(orderId) {
    return request({
      url: `/merchant/orders/${orderId}/mark-ready`,
      method: 'put'
    })
  },

  markCompleted(orderId) {
    return request({
      url: `/merchant/orders/${orderId}/mark-completed`,
      method: 'put'
    })
  },

  cancelOrder(orderId, reason) {
    return request({
      url: `/merchant/orders/${orderId}/cancel`,
      method: 'put',
      data: { reason }
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
      url: '/merchant/business-settings',
      method: 'get'
    })
  },

  updateBusinessSettings(data) {
    return request({
      url: '/merchant/business-settings',
      method: 'put',
      data
    })
  },

  // 分类管理
  getCategories() {
    return request({
      url: '/merchant/categories',
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

  // 财务统计
  getFinancialStats(params) {
    return request({
      url: '/merchant/financial/stats',
      method: 'get',
      params
    })
  },

  getIncomeDetails(params) {
    return request({
      url: '/merchant/financial/income',
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
      url: '/merchant/upload/image',
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
      url: '/merchant/upload/images',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}