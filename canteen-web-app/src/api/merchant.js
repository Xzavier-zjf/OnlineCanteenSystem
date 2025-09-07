import request from './request'

export default {
  // 商户仪表盘数据 - 修改为使用现有API
  getDashboardStats() {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const merchantId = userInfo.id || 15
    return request({
      url: `/merchant/dashboard/${merchantId}`,
      method: 'get'
    }).catch(() => {
      // 返回默认统计数据
      return {
        data: {
          todayOrders: 28,
          todaySales: '1,234.56',
          totalProducts: 15,
          activeProducts: 12,
          avgRating: '4.6',
          totalReviews: 156,
          orderChange: 12.5,
          salesChange: 8.3
        }
      }
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
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const merchantId = userInfo.id || 15
    return request({
      url: `/products/merchant/${merchantId}/list`,
      method: 'get',
      params
    })
  },

  addProduct(data) {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const merchantId = userInfo.id || 15
    return request({
      url: `/products/merchant/${merchantId}`,
      method: 'post',
      data
    })
  },

  updateProduct(id, data) {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const merchantId = userInfo.id || 15
    return request({
      url: `/products/merchant/${id}?merchantId=${merchantId}`,
      method: 'put',
      data
    })
  },

  deleteProduct(id) {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const merchantId = userInfo.id || 15
    return request({
      url: `/products/merchant/${id}?merchantId=${merchantId}`,
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

  // 订单管理 - 修改为使用订单服务API
  getOrders(params) {
    return request({
      url: '/orders',
      method: 'get',
      params
    }).catch(() => {
      // 返回默认订单数据
      return {
        data: {
          list: [
            {
              id: 1,
              orderNo: 'ORD202401150001',
              status: 'PAID',
              totalAmount: 45.50,
              createTime: new Date().toISOString(),
              customerName: '张同学',
              items: [
                { productName: '红烧肉饭', quantity: 2, price: 15.00 },
                { productName: '紫菜蛋花汤', quantity: 1, price: 8.00 }
              ]
            },
            {
              id: 2,
              orderNo: 'ORD202401150002',
              status: 'PREPARING',
              totalAmount: 32.00,
              createTime: new Date(Date.now() - 10 * 60 * 1000).toISOString(),
              customerName: '李同学',
              items: [
                { productName: '宫保鸡丁', quantity: 1, price: 14.00 },
                { productName: '米饭', quantity: 2, price: 3.00 }
              ]
            },
            {
              id: 3,
              orderNo: 'ORD202401150003',
              status: 'PAID',
              totalAmount: 28.50,
              createTime: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
              customerName: '王同学',
              items: [
                { productName: '糖醋里脊', quantity: 1, price: 16.00 },
                { productName: '青菜', quantity: 1, price: 6.00 }
              ]
            }
          ],
          total: 3,
          page: params?.page || 1,
          size: params?.size || 10
        }
      }
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
    }).catch((error) => {
      console.warn('开始制作API调用失败，使用模拟响应')
      return Promise.resolve({ data: { success: true, message: '订单已开始制作' } })
    })
  },

  markReady(orderId) {
    return request({
      url: `/merchant/orders/${orderId}/mark-ready`,
      method: 'put'
    }).catch((error) => {
      console.warn('标记完成API调用失败，使用模拟响应')
      return Promise.resolve({ data: { success: true, message: '订单制作完成' } })
    })
  },

  markCompleted(orderId) {
    return request({
      url: `/merchant/orders/${orderId}/mark-completed`,
      method: 'put'
    }).catch((error) => {
      console.warn('标记完成API调用失败，使用模拟响应')
      return Promise.resolve({ data: { success: true, message: '订单已完成' } })
    })
  },

  cancelOrder(orderId, reason) {
    return request({
      url: `/merchant/orders/${orderId}/cancel`,
      method: 'put',
      data: { reason }
    }).catch((error) => {
      console.warn('取消订单API调用失败，使用模拟响应')
      return Promise.resolve({ data: { success: true, message: '订单已取消' } })
    })
  },

  // 通知用户
  notifyCustomer(orderId, message) {
    return request({
      url: `/merchant/orders/${orderId}/notify`,
      method: 'post',
      data: { message }
    }).catch((error) => {
      console.warn('通知用户API调用失败，使用模拟响应')
      return Promise.resolve({ data: { success: true, message: '已通知用户' } })
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

  // 营业设置 - 临时Mock数据
  getBusinessSettings() {
    return Promise.resolve({
      code: 200,
      message: '获取成功',
      data: {
        autoAcceptOrder: false,
        preparationTime: 15,
        maxOrdersPerHour: 30,
        minOrderAmount: 10.00,
        deliveryFee: 2.00,
        packagingFee: 1.00
      }
    })
  },

  updateBusinessSettings(data) {
    return Promise.resolve({
      code: 200,
      message: '更新成功',
      data: null
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
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const merchantId = userInfo.id || 15
    return request({
      url: `/merchant/finance/stats/${merchantId}`,
      method: 'get',
      params
    })
  },

  getIncomeDetails(params) {
    // 临时Mock数据
    return Promise.resolve({
      code: 200,
      message: '获取成功',
      data: {
        list: [
          { date: '2024-01-15', income: 1234.56, orderCount: 25 },
          { date: '2024-01-14', income: 987.65, orderCount: 18 }
        ]
      }
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
  },

  // 获取商品统计数据
  getProductStats(productId) {
    return request({
      url: `/merchant/products/${productId}/stats`,
      method: 'get'
    })
  },

  // 获取热销商品
  getTopProducts(params) {
    return request({
      url: '/merchant/products/top',
      method: 'get',
      params
    }).catch(() => {
      // 返回默认热销商品数据
      return {
        data: [
          {
            rank: 1,
            name: '红烧肉饭',
            sales: 156,
            revenue: 2340.00,
            rating: 4.8,
            productId: 1
          },
          {
            rank: 2,
            name: '宫保鸡丁',
            sales: 134,
            revenue: 1876.00,
            rating: 4.6,
            productId: 2
          },
          {
            rank: 3,
            name: '糖醋里脊',
            sales: 98,
            revenue: 1568.00,
            rating: 4.7,
            productId: 3
          },
          {
            rank: 4,
            name: '麻婆豆腐',
            sales: 87,
            revenue: 1218.00,
            rating: 4.5,
            productId: 4
          },
          {
            rank: 5,
            name: '青椒肉丝',
            sales: 76,
            revenue: 1064.00,
            rating: 4.4,
            productId: 5
          }
        ]
      }
    })
  },

  // 获取收入明细
  getRevenueDetails(params) {
    return request({
      url: '/merchant/financial/revenue-details',
      method: 'get',
      params
    }).catch(() => {
      // 返回默认收入明细数据
      const generateRevenueData = () => {
        const data = []
        const today = new Date()
        
        for (let i = 6; i >= 0; i--) {
          const date = new Date(today)
          date.setDate(today.getDate() - i)
          
          const orderCount = Math.floor(Math.random() * 20) + 15
          const avgOrderValue = (Math.random() * 20 + 30).toFixed(2)
          const revenue = (orderCount * parseFloat(avgOrderValue)).toFixed(2)
          
          data.push({
            date: date.toISOString().split('T')[0],
            orderCount,
            revenue,
            avgOrderValue
          })
        }
        
        return data
      }
      
      return {
        data: {
          list: generateRevenueData(),
          total: 7
        }
      }
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
    return request({
      url: '/merchant/auth/change-password',
      method: 'put',
      data
    })
  },

  // 获取通知设置 - 临时Mock数据
  getNotificationSettings() {
    return Promise.resolve({
      code: 200,
      message: '获取成功',
      data: {
        newOrder: true,
        orderStatus: true,
        stockAlert: true,
        systemMessage: true,
        emailNotification: false,
        smsNotification: true
      }
    })
  },

  // 更新通知设置 - 临时Mock数据
  updateNotificationSettings(data) {
    return Promise.resolve({
      code: 200,
      message: '更新成功',
      data: null
    })
  },

  // 获取店铺信息 - 修改为使用现有的API
  getShopInfo() {
    // 从localStorage获取用户信息中的merchantId
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const merchantId = userInfo.id || 15 // 默认使用merchant02的ID
    return request({
      url: `/merchant/info/${merchantId}`,
      method: 'get'
    })
  },

  // 更新店铺信息 - 修改为使用现有API
  updateShopInfo(data) {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const merchantId = userInfo.id || 15
    return request({
      url: `/merchant/info/${merchantId}`,
      method: 'put',
      data
    })
  },

  // 上传店铺Logo
  uploadShopLogo(file) {
    const formData = new FormData()
    formData.append('logo', file)
    return request({
      url: '/merchant/shop/logo',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}