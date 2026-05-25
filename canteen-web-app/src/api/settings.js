import request from '@/api/request'

export default {
  // 获取用户个人信息
  getUserProfile() {
    return request({
      url: '/users/profile',
      method: 'get'
    })
  },

  // 更新用户个人信息
  updateUserProfile(data) {
    return request({
      url: '/users/profile',
      method: 'put',
      data
    })
  },

  // 修改密码
  changePassword(data) {
    return request({
      url: '/users/change-password',
      method: 'post',
      data: {
        oldPassword: data.oldPassword || data.currentPassword,
        newPassword: data.newPassword
      }
    })
  },

  // 上传头像
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    
    return request({
      url: '/upload/avatar',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取登录记录
  getLoginRecords() {
    return request({
      url: '/users/login-records',
      method: 'get'
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

  // 获取偏好设置
  getPreferenceSettings() {
    return request({
      url: '/users/preference-settings',
      method: 'get'
    })
  },

  // 更新偏好设置
  updatePreferenceSettings(data) {
    return request({
      url: '/users/preference-settings',
      method: 'put',
      data
    })
  },

  // 商户专用 - 获取店铺设置
  getShopSettings() {
    return request({
      url: '/merchant/shop-settings',
      method: 'get'
    })
  },

  // 商户专用 - 更新店铺设置
  updateShopSettings(data) {
    return request({
      url: '/merchant/shop-settings',
      method: 'put',
      data
    })
  },

  // 管理员专用 - 获取系统设置
  getSystemSettings() {
    return request({
      url: '/admin/system-settings',
      method: 'get'
    })
  },

  // 管理员专用 - 更新系统设置
  updateSystemSettings(data) {
    return request({
      url: '/admin/system-settings',
      method: 'put',
      data
    })
  },

  // 管理员专用 - 数据备份
  backupData() {
    return request({
      url: '/admin/backup',
      method: 'post'
    })
  },

  // 管理员专用 - 清理日志
  clearLogs() {
    return request({
      url: '/admin/clear-logs',
      method: 'post'
    })
  },

  // 获取系统统计信息
  getSystemStats() {
    return request({
      url: '/admin/system-stats',
      method: 'get'
    })
  }
}
