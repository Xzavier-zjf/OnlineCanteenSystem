import { createServiceRequest } from './request'
import { ElMessage } from 'element-plus'

// 创建不同服务的请求实例
const userRequest = createServiceRequest('user')
const productRequest = createServiceRequest('product')
const orderRequest = createServiceRequest('order')
const recommendRequest = createServiceRequest('recommend')

// 为每个请求实例添加拦截器
const setupInterceptors = (request) => {
  // 请求拦截器
  request.interceptors.request.use(
    config => {
      const token = localStorage.getItem('token')
      if (token && token !== 'undefined' && token !== 'null') {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    },
    error => {
      return Promise.reject(error)
    }
  )

  // 响应拦截器
  request.interceptors.response.use(
    response => {
      const { data } = response
      
      // 如果返回的是标准格式 {code, data, message}
      if (data && typeof data === 'object' && 'code' in data) {
        if (data.code === 200) {
          return data
        } else {
          ElMessage.error(data.message || '请求失败')
          return Promise.reject(new Error(data.message || '请求失败'))
        }
      }
      
      return data
    },
    error => {
      console.error('API请求失败:', error)
      
      if (error.response) {
        const { status, data } = error.response
        
        switch (status) {
          case 401:
            ElMessage.error('登录已过期，请重新登录')
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            window.location.href = '/login'
            break
          case 404:
            ElMessage.error('服务不可用，请检查后端服务是否启动')
            break
          case 500:
            ElMessage.error('服务器内部错误')
            break
          default:
            ElMessage.error(data?.message || `请求失败: ${status}`)
        }
      } else if (error.request) {
        ElMessage.error('网络错误，请检查网络连接')
      } else {
        ElMessage.error('请求配置错误')
      }
      
      return Promise.reject(error)
    }
  )
}

// 为所有请求实例设置拦截器
setupInterceptors(userRequest)
setupInterceptors(productRequest)
setupInterceptors(orderRequest)
setupInterceptors(recommendRequest)

// 用户API
export const userApi = {
  // 用户登录
  login: (data) => {
    return userRequest.post('/api/users/login', data)
  },
  
  // 用户注册
  register: (data) => {
    return userRequest.post('/api/users/register', data)
  },
  
  // 获取用户信息
  getUserInfo: (id) => {
    return userRequest.get(`/api/users/${id}`)
  },
  
  // 获取用户资料
  getProfile: () => {
    return userRequest.get('/api/users/info')
  },
  
  // 更新用户资料
  updateProfile: (data) => {
    return userRequest.put('/api/users/info', data)
  },
  
  // 修改密码
  changePassword: (data) => {
    return userRequest.post('/api/users/change-password', data)
  },
  
  // 获取用户统计数据
  getUserStats: () => {
    return userRequest.get('/api/users/stats')
  }
}

// 商品API
export const productApi = {
  // 获取商品分类
  getCategories: () => {
    return productRequest.get('/api/products/categories')
  },
  
  // 获取商品列表
  getProducts: (params) => {
    return productRequest.get('/api/products', { params })
  },
  
  // 获取热门商品
  getHotProducts: (limit = 6) => {
    return productRequest.get(`/api/products/hot?limit=${limit}`)
  },
  
  // 获取商品详情
  getProductDetail: (id) => {
    return productRequest.get(`/api/products/${id}`)
  }
}

// 订单API
export const orderApi = {
  // 创建订单
  createOrder: (data) => {
    return orderRequest.post('/api/orders', data)
  },
  
  // 获取订单列表
  getOrders: (userId) => {
    return orderRequest.get(`/api/orders/user/${userId}`)
  },
  
  // 获取订单详情
  getOrderDetail: (id) => {
    return orderRequest.get(`/api/orders/${id}`)
  },
  
  // 更新订单状态
  updateOrderStatus: (id, status) => {
    return orderRequest.put(`/api/orders/${id}/status`, { status })
  },
  
  // 支付订单
  payOrder: (id) => {
    return orderRequest.post(`/api/orders/${id}/pay`)
  },
  
  // 取消订单
  cancelOrder: (id) => {
    return orderRequest.post(`/api/orders/${id}/cancel`)
  },
  
  // 商家接单（开始制作）
  prepareOrder: (id) => {
    return orderRequest.post(`/api/orders/${id}/prepare`)
  },
  
  // 订单制作完成（待取餐）
  readyOrder: (id) => {
    return orderRequest.post(`/api/orders/${id}/ready`)
  }
}

// 推荐API
export const recommendApi = {
  // 获取个性化推荐
  getRecommendProducts: (userId, limit = 6) => {
    return recommendRequest.get(`/api/recommend/products/${userId}?limit=${limit}`)
  },
  
  // 获取热门推荐
  getHotRecommendations: (limit = 6) => {
    return recommendRequest.get(`/api/recommend/hot?limit=${limit}`)
  },
  
  // 获取相似商品推荐
  getSimilarProducts: (productId, limit = 6) => {
    return recommendRequest.get(`/api/recommend/similar/${productId}?limit=${limit}`)
  },
  
  // 记录用户行为
  recordBehavior: (data) => {
    return recommendRequest.post('/api/recommend/behavior', data)
  }
}