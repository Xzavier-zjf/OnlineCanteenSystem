import { createServiceRequest } from './request'
import { ElMessage } from 'element-plus'

const userRequest = createServiceRequest('users')
const productRequest = createServiceRequest('products')
const orderRequest = createServiceRequest('orders')

const setupInterceptors = (request) => {
  request.interceptors.request.use(
    config => {
      const token = localStorage.getItem('token')
      if (token && token !== 'undefined' && token !== 'null') {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    },
    error => Promise.reject(error)
  )

  request.interceptors.response.use(
    response => {
      const { data } = response
      if (data && typeof data === 'object' && 'code' in data) {
        if (data.code === 200) {
          return data
        }
        ElMessage.error(data.message || '请求失败')
        return Promise.reject(new Error(data.message || '请求失败'))
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

setupInterceptors(userRequest)
setupInterceptors(productRequest)
setupInterceptors(orderRequest)

export const userApi = {
  login: (data) => userRequest.post('/login', data),
  register: (data) => userRequest.post('/register', data),
  getUserInfo: (id) => userRequest.get(`/users/${id}`),
  getProfile: () => userRequest.get('/info'),
  updateProfile: (data) => userRequest.put('/info', data),
  changePassword: (data) => userRequest.post('/change-password', data),
  getUserStats: () => userRequest.get('/stats')
}

export const productApi = {
  getCategories: () => productRequest.get('/categories'),
  getProducts: (params) => productRequest.get('', { params }),
  getHotProducts: (limit = 6) => productRequest.get(`/hot?limit=${limit}`),
  getProductDetail: (id) => productRequest.get(`/${id}`),
  getSystemStats: () => productRequest.get('/stats')
}

export const orderApi = {
  createOrder: (data) => orderRequest.post('', data),
  getOrders: (userId) => userId ? orderRequest.get(`/user/${userId}`) : orderRequest.get(''),
  getOrderDetail: (id) => orderRequest.get(`/${id}`),
  updateOrderStatus: (id, status) => orderRequest.put(`/${id}/status`, { status }),
  payOrder: (id) => orderRequest.post(`/${id}/pay`),
  cancelOrder: (id) => orderRequest.post(`/${id}/cancel`),
  refundOrder: (id, reason) => orderRequest.post(`/${id}/refund`, { reason }),
  prepareOrder: (id) => orderRequest.post(`/${id}/prepare`),
  readyOrder: (id) => orderRequest.post(`/${id}/ready`)
}

export const systemApi = {
  getSystemStats: () => userRequest.get('/system/stats')
}

export { recommendApi } from './recommend'
