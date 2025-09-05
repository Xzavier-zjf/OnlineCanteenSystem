import axios from 'axios'
import { useUserStore } from '../stores/user'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // api的base_url
  timeout: 5000 // 请求超时时间
})

// request拦截器
service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    Promise.reject(error)
  }
)

// response拦截器
service.interceptors.response.use(
  response => response,
  error => {
    console.error('err' + error) // for debug
    return Promise.reject(error)
  }
)

export default service
