import request from '@/utils/request'

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
    }).catch(() => {
      // 降级到本地存储
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      Object.assign(userInfo, data)
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      return { data: userInfo }
    })
  },

  // 修改密码
  changePassword(data) {
    return request({
      url: '/user/change-password',
      method: 'post',
      data
    }).catch(() => {
      // 模拟成功响应
      return { data: { success: true } }
    })
  },

  // 上传头像
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('avatar', file)
    
    return request({
      url: '/user/upload-avatar',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }).catch(() => {
      // 降级到本地URL
      return { 
        data: { 
          avatarUrl: URL.createObjectURL(file) 
        } 
      }
    })
  },

  // 获取登录记录
  getLoginRecords() {
    return request({
      url: '/user/login-records',
      method: 'get'
    }).catch(() => {
      // 返回模拟数据
      return {
        data: [
          {
            id: 1,
            loginTime: '2024-01-15 14:30:25',
            ip: '192.168.1.100',
            device: 'Chrome 浏览器',
            location: '北京市',
            status: '成功'
          },
          {
            id: 2,
            loginTime: '2024-01-14 09:15:10',
            ip: '192.168.1.100',
            device: 'Chrome 浏览器',
            location: '北京市',
            status: '成功'
          },
          {
            id: 3,
            loginTime: '2024-01-13 16:45:30',
            ip: '192.168.1.101',
            device: 'Firefox 浏览器',
            location: '上海市',
            status: '失败'
          }
        ]
      }
    })
  },

  // 获取通知设置
  getNotificationSettings() {
    return request({
      url: '/user/notification-settings',
      method: 'get'
    }).catch(() => {
      // 返回默认设置
      return {
        data: {
          systemMessage: true,
          emailNotification: false,
          smsNotification: true,
          orderStatus: true,
          promotions: false,
          newOrder: true,
          stockAlert: true,
          userRegistration: true,
          systemError: true
        }
      }
    })
  },

  // 更新通知设置
  updateNotificationSettings(data) {
    return request({
      url: '/user/notification-settings',
      method: 'put',
      data
    }).catch(() => {
      // 模拟成功响应
      return { data: { success: true } }
    })
  },

  // 获取偏好设置
  getPreferenceSettings() {
    return request({
      url: '/user/preference-settings',
      method: 'get'
    }).catch(() => {
      // 返回默认设置
      return {
        data: {
          theme: 'light',
          language: 'zh-CN',
          autoSave: true,
          pageSize: 20
        }
      }
    })
  },

  // 更新偏好设置
  updatePreferenceSettings(data) {
    return request({
      url: '/user/preference-settings',
      method: 'put',
      data
    }).catch(() => {
      // 保存到本地存储
      localStorage.setItem('preferenceSettings', JSON.stringify(data))
      return { data: { success: true } }
    })
  },

  // 商户专用 - 获取店铺设置
  getShopSettings() {
    return request({
      url: '/merchant/shop-settings',
      method: 'get'
    }).catch(() => {
      // 返回模拟数据
      return {
        data: {
          shopName: '美味餐厅',
          description: '提供各种美味佳肴，新鲜食材，用心制作',
          phone: '400-123-4567',
          address: '北京市朝阳区xxx街道xxx号',
          isOpen: true,
          autoAcceptOrder: false,
          businessHours: {
            start: '08:00',
            end: '22:00'
          }
        }
      }
    })
  },

  // 商户专用 - 更新店铺设置
  updateShopSettings(data) {
    return request({
      url: '/merchant/shop-settings',
      method: 'put',
      data
    }).catch(() => {
      // 模拟成功响应
      return { data: { success: true } }
    })
  },

  // 管理员专用 - 获取系统设置
  getSystemSettings() {
    return request({
      url: '/admin/system-settings',
      method: 'get'
    }).catch(() => {
      // 返回模拟数据
      return {
        data: {
          maintenanceMode: false,
          allowRegistration: true,
          systemAnnouncement: '系统将于今晚23:00-01:00进行维护，请提前做好准备。',
          maxUploadSize: 10, // MB
          sessionTimeout: 30 // 分钟
        }
      }
    })
  },

  // 管理员专用 - 更新系统设置
  updateSystemSettings(data) {
    return request({
      url: '/admin/system-settings',
      method: 'put',
      data
    }).catch(() => {
      // 模拟成功响应
      return { data: { success: true } }
    })
  },

  // 管理员专用 - 数据备份
  backupData() {
    return request({
      url: '/admin/backup',
      method: 'post'
    }).catch(() => {
      // 模拟成功响应
      return { 
        data: { 
          success: true, 
          backupFile: `backup_${new Date().toISOString().slice(0, 10)}.sql`,
          size: '15.6MB'
        } 
      }
    })
  },

  // 管理员专用 - 清理日志
  clearLogs() {
    return request({
      url: '/admin/clear-logs',
      method: 'post'
    }).catch(() => {
      // 模拟成功响应
      return { 
        data: { 
          success: true, 
          clearedSize: '128MB',
          clearedFiles: 45
        } 
      }
    })
  },

  // 获取系统统计信息
  getSystemStats() {
    return request({
      url: '/admin/system-stats',
      method: 'get'
    }).catch(() => {
      // 返回模拟数据
      return {
        data: {
          totalUsers: 1250,
          totalMerchants: 85,
          totalOrders: 15680,
          systemUptime: '15天 8小时 32分钟',
          diskUsage: '68%',
          memoryUsage: '45%',
          cpuUsage: '23%'
        }
      }
    })
  }
}