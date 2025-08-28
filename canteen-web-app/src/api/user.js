import { request } from './request'

// 用户相关API
export const userApi = {
  // 获取用户信息
  getProfile: () => {
    return request.get('/api/users/info')
  },
  
  // 更新用户信息
  updateProfile: (data) => {
    return request.put('/api/users/info', data)
  },
  
  // 修改密码
  changePassword: (data) => {
    return request.post('/api/users/change-password', data)
  },
  
  // 获取用户统计数据
  getUserStats: () => {
    return request.get('/api/users/stats')
  },
  
  // 用户登录
  login: (data) => {
    return request.post('/api/users/login', data)
  },
  
  // 用户注册
  register: (data) => {
    return request.post('/api/users/register', data)
  }
}