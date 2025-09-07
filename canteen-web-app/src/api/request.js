import axios from 'axios'
import { ElMessage } from 'element-plus'

// API基础URL配置 - 通过Vite代理连接Spring Boot后端
const API_BASE_URL = '/api'

// 创建axios实例
export const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: false
})

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
    console.error('请求拦截器错误:', error)
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

// 创建不同服务的请求实例 - 使用相对路径通过Vite代理
export const createServiceRequest = (service) => {
  // 使用相对路径，让Vite代理处理路由
  const serviceUrl = `/api/${service}`
  
  console.log(`创建服务实例: ${service}, URL: ${serviceUrl} (通过Vite代理)`)
  
  const serviceRequest = axios.create({
    baseURL: serviceUrl,
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json'
    },
    withCredentials: false
  })
  
  // 添加请求拦截器
  serviceRequest.interceptors.request.use(
    config => {
      const token = localStorage.getItem('token')
      if (token && token !== 'undefined' && token !== 'null') {
        config.headers.Authorization = `Bearer ${token}`
      }
      console.log(`${service}服务请求:`, config.method?.toUpperCase(), config.url)
      return config
    },
    error => {
      console.error(`${service}服务请求错误:`, error)
      return Promise.reject(error)
    }
  )
  
  // 添加响应拦截器
  serviceRequest.interceptors.response.use(
    response => {
      console.log(`${service}服务响应:`, response.status, response.config.url)
      return response
    },
    error => {
      console.error(`${service}服务响应错误:`, error.message)
      if (error.response?.status === 404) {
        ElMessage.error(`${service}服务不可用，请检查后端服务是否启动`)
      }
      return Promise.reject(error)
    }
  )
  
  return serviceRequest
}

// 默认导出request实例
export default request