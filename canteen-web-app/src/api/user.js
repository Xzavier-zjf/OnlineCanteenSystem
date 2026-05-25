import { createServiceRequest } from './request'

const userRequest = createServiceRequest('users')

const unwrapResult = async (requestPromise) => {
  const response = await requestPromise
  const result = response.data
  if (result && typeof result === 'object' && 'code' in result && result.code !== 200) {
    const error = new Error(result.message || '请求失败')
    error.isBusinessError = true
    throw error
  }
  return result
}

// 用户相关API
export const userApi = {
  // 获取用户信息
  getProfile: () => {
    return unwrapResult(userRequest.get('/info'))
  },
  
  // 更新用户信息
  updateProfile: (data) => {
    return unwrapResult(userRequest.put('/info', data))
  },
  
  // 修改密码
  changePassword: (data) => {
    return unwrapResult(userRequest.post('/change-password', data))
  },
  
  // 获取用户统计数据
  getUserStats: () => {
    return unwrapResult(userRequest.get('/stats'))
  },
  
  // 用户登录
  login: (data) => {
    return unwrapResult(userRequest.post('/login', data))
  },
  
  // 用户注册
  register: (data) => {
    return unwrapResult(userRequest.post('/register', data))
  }
}
