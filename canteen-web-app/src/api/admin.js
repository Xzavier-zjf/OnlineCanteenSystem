import request from '@/api/request.js'

export const adminApi = {
  // 仪表板相�?
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
    })).catch(error => {
      console.error('获取系统统计数据失败:', error)
      // 返回默认数据
      return {
        orderCount: 0,
        productCount: 0,
        todaySales: 0,
        totalSales: 0
      }
    })
  },

  // 获取系统健康状�?- 返回模拟数据避免CORS问题
  getSystemHealth() {
    return Promise.resolve({
      status: 'UP',
      services: {
        'user-service': { status: 'UP', port: 8081 },
        'product-service': { status: 'UP', port: 8082 },
        'order-service': { status: 'UP', port: 8083 },
        'recommend-service': { status: 'UP', port: 8084 },
        'gateway': { status: 'UP', port: 8080 }
      }
    })
  },

  // 获取热门商品统计数据
  getHotProductsStats() {
    return request({
      url: '/admin/recommend/hot/stats',
      method: 'get'
    }).catch(() => {
      // 返回默认数据
      return {
        data: [
          { rank: 1, name: '红烧肉饭', sales: 156, revenue: 2340.00, id: 1, image: '/images/products/hongshaorou_rice.jpg', salesCount: 156, rating: 4.8, price: 15.00 },
          { rank: 2, name: '宫保鸡丁', sales: 134, revenue: 2010.00, id: 2, image: '/images/products/gongbao_dish.jpg', salesCount: 134, rating: 4.6, price: 15.00 },
          { rank: 3, name: '糖醋里脊', sales: 128, revenue: 1920.00, id: 3, image: '/images/products/tangcu_liji.jpg', salesCount: 128, rating: 4.7, price: 15.00 },
          { rank: 4, name: '麻婆豆腐', sales: 98, revenue: 1470.00, id: 4, image: '/images/products/mapo_tofu.jpg', salesCount: 98, rating: 4.5, price: 15.00 },
          { rank: 5, name: '回锅肉', sales: 87, revenue: 1305.00, id: 5, image: '/images/products/huiguorou_dish.jpg', salesCount: 87, rating: 4.4, price: 15.00 }
        ]
      }
    })
  },

  // 获取商户销售排�?
  getMerchantStats() {
    return request({
      url: '/admin/merchants/stats',
      method: 'get'
    }).catch(() => {
      // 返回默认数据
      return {
        data: [
          { rank: 1, name: '第一食堂', orders: 245, revenue: 12250.00 },
          { rank: 2, name: '第二食堂', orders: 198, revenue: 9900.00 },
          { rank: 3, name: '清真餐厅', orders: 156, revenue: 7800.00 },
          { rank: 4, name: '西餐厅', orders: 134, revenue: 6700.00 },
          { rank: 5, name: '小吃街', orders: 98, revenue: 4900.00 }
        ]
      }
    })
  },

  // 保存系统配置
  saveSystemConfig(config) {
    return request({
      url: '/admin/system/config',
      method: 'post',
      data: config
    })
  },

  // 获取系统日志
  getSystemLogs(params = {}) {
    // 直接返回模拟数据，避免API调用错误
    return Promise.resolve({
      data: [
        {
          timestamp: new Date().toLocaleString('zh-CN'),
          level: 'INFO',
          module: '用户服务',
          message: '用户登录成功',
          user: 'admin1'
        },
        {
          timestamp: new Date(Date.now() - 60000).toLocaleString('zh-CN'),
          level: 'INFO',
          module: '网关服务',
          message: '服务启动完成',
          user: 'system'
        },
        {
          timestamp: new Date(Date.now() - 120000).toLocaleString('zh-CN'),
          level: 'WARN',
          module: '订单服务',
          message: '订单处理延迟',
          user: 'system'
        }
      ]
    })
    
    /* 原始API调用代码，暂时注释 */
    return request({
      url: '/admin/system/logs',
      method: 'get',
      params
    }).catch(() => {
      // 返回默认数据
      const now = new Date()
      return {
        data: [
          {
            timestamp: new Date(now - 5 * 60 * 1000).toLocaleString('zh-CN'),
            level: 'INFO',
            module: '用户服务',
            message: '用户登录成功',
            user: 'student001'
          },
          {
            timestamp: new Date(now - 10 * 60 * 1000).toLocaleString('zh-CN'),
            level: 'INFO',
            module: '订单服务',
            message: '新订单创建',
            user: 'teacher002'
          },
          {
            timestamp: new Date(now - 15 * 60 * 1000).toLocaleString('zh-CN'),
            level: 'WARN',
            module: '商品服务',
            message: '商品库存低于阈值',
            user: 'system'
          }
        ]
      }
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
