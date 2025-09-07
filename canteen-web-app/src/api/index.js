import { createServiceRequest } from './request'
import { ElMessage } from 'element-plus'

// 创建不同服务的请求实例 - 使用复数形式匹配vite.config.js代理
const userRequest = createServiceRequest('users')
const productRequest = createServiceRequest('products')
const orderRequest = createServiceRequest('orders')
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
    return userRequest.post('/login', data)
  },
  
  // 用户注册
  register: (data) => {
    return userRequest.post('/register', data)
  },
  
  // 获取用户信息
  getUserInfo: (id) => {
    return userRequest.get(`/users/${id}`)
  },
  
  // 获取用户资料
  getProfile: () => {
    return userRequest.get('/info')
  },
  
  // 更新用户资料
  updateProfile: (data) => {
    return userRequest.put('/info', data)
  },
  
  // 修改密码
  changePassword: (data) => {
    return userRequest.post('/change-password', data)
  },
  
  // 获取用户统计数据
  getUserStats: () => {
    return userRequest.get('/stats')
  }
}

// 商品API
export const productApi = {
  // 获取商品分类
  getCategories: () => {
    return productRequest.get('/products/categories').catch(() => {
      // 降级到模拟数据
      return {
        code: 200,
        data: [
          { id: 1, name: '主食套餐', description: '各种主食套餐', icon: '🍱' },
          { id: 2, name: '面食类', description: '各种面条类食品', icon: '🍜' },
          { id: 3, name: '汤品类', description: '各种汤品', icon: '🍲' },
          { id: 4, name: '素食类', description: '素食菜品', icon: '🥗' },
          { id: 5, name: '荤菜类', description: '荤菜类食品', icon: '🍖' },
          { id: 6, name: '饮品类', description: '各种饮品', icon: '🥤' },
          { id: 7, name: '小食点心', description: '小食和点心', icon: '🧁' },
          { id: 8, name: '早餐类', description: '早餐食品', icon: '🥐' }
        ]
      }
    })
  },
  
  // 获取商品列表
  getProducts: (params) => {
    return productRequest.get('/products', { params }).catch(() => {
      // 降级到模拟数据
      const mockProducts = [
        // 主食套餐
        {
          id: 1,
          name: '红烧肉套餐',
          description: '香喷喷的红烧肉配米饭，营养丰富',
          price: 18.00,
          originalPrice: 22.00,
          imageUrl: '/images/products/hongshaorou_rice.jpg',
          categoryId: 1,
          categoryName: '主食套餐',
          sales: 156,
          rating: 4.8,
          stock: 25,
          isHot: true,
          isNew: false
        },
        {
          id: 2,
          name: '宫保鸡丁套餐',
          description: '经典川菜宫保鸡丁配米饭',
          price: 16.00,
          imageUrl: '/images/products/gongbao_dish.jpg',
          categoryId: 1,
          categoryName: '主食套餐',
          sales: 89,
          rating: 4.6,
          stock: 18,
          isHot: false,
          isNew: true
        },
        {
          id: 3,
          name: '糖醋里脊套餐',
          description: '酸甜可口的糖醋里脊配米饭',
          price: 17.00,
          imageUrl: '/images/products/tangcu_liji.jpg',
          categoryId: 1,
          categoryName: '主食套餐',
          sales: 124,
          rating: 4.7,
          stock: 30,
          isHot: true,
          isNew: false
        },
        
        // 面食类
        {
          id: 4,
          name: '兰州拉面',
          description: '正宗兰州牛肉拉面，汤清面劲',
          price: 15.00,
          imageUrl: '/images/products/lanzhou_noodle.jpg',
          categoryId: 2,
          categoryName: '面食类',
          sales: 203,
          rating: 4.9,
          stock: 40,
          isHot: true,
          isNew: false
        },
        {
          id: 5,
          name: '炸酱面',
          description: '北京风味炸酱面，酱香浓郁',
          price: 12.00,
          imageUrl: '/images/products/zhajiang_noodle.jpg',
          categoryId: 2,
          categoryName: '面食类',
          sales: 78,
          rating: 4.4,
          stock: 22,
          isHot: false,
          isNew: false
        },
        {
          id: 6,
          name: '酸辣粉',
          description: '重庆特色酸辣粉，酸辣开胃',
          price: 10.00,
          imageUrl: '/images/products/suanla_noodle.jpg',
          categoryId: 2,
          categoryName: '面食类',
          sales: 145,
          rating: 4.5,
          stock: 35,
          isHot: false,
          isNew: true
        },
        
        // 汤品类
        {
          id: 7,
          name: '西红柿鸡蛋汤',
          description: '清淡营养的西红柿鸡蛋汤',
          price: 8.00,
          imageUrl: '/images/products/fanqie.jpg',
          categoryId: 3,
          categoryName: '汤品类',
          sales: 67,
          rating: 4.3,
          stock: 50,
          isHot: false,
          isNew: false
        },
        {
          id: 8,
          name: '冬瓜排骨汤',
          description: '清香的冬瓜排骨汤，营养丰富',
          price: 12.00,
          imageUrl: '/images/products/donggua_soup.jpg',
          categoryId: 3,
          categoryName: '汤品类',
          sales: 89,
          rating: 4.6,
          stock: 28,
          isHot: false,
          isNew: false
        },
        
        // 素食类
        {
          id: 9,
          name: '麻婆豆腐',
          description: '经典川菜麻婆豆腐，麻辣鲜香',
          price: 14.00,
          imageUrl: '/images/products/mapo_tofu.jpg',
          categoryId: 4,
          categoryName: '素食类',
          sales: 112,
          rating: 4.7,
          stock: 32,
          isHot: true,
          isNew: false
        },
        {
          id: 10,
          name: '清炒白菜',
          description: '清淡爽口的清炒白菜',
          price: 8.00,
          imageUrl: '/images/products/baicai.jpg',
          categoryId: 4,
          categoryName: '素食类',
          sales: 45,
          rating: 4.2,
          stock: 40,
          isHot: false,
          isNew: false
        },
        
        // 荤菜类
        {
          id: 11,
          name: '回锅肉',
          description: '四川经典回锅肉，肥而不腻',
          price: 20.00,
          imageUrl: '/images/products/huiguorou_dish.jpg',
          categoryId: 5,
          categoryName: '荤菜类',
          sales: 134,
          rating: 4.8,
          stock: 15,
          isHot: true,
          isNew: false
        },
        {
          id: 12,
          name: '红烧牛肉',
          description: '软烂香甜的红烧牛肉',
          price: 25.00,
          imageUrl: '/images/products/beef_noodle.jpg',
          categoryId: 5,
          categoryName: '荤菜类',
          sales: 98,
          rating: 4.6,
          stock: 12,
          isHot: false,
          isNew: true
        },
        
        // 饮品类
        {
          id: 13,
          name: '柠檬蜂蜜茶',
          description: '清香的柠檬蜂蜜茶，生津止渴',
          price: 8.00,
          imageUrl: '/images/products/lemon_tea.jpg',
          categoryId: 6,
          categoryName: '饮品类',
          sales: 167,
          rating: 4.5,
          stock: 60,
          isHot: false,
          isNew: false
        },
        {
          id: 14,
          name: '鲜榨橙汁',
          description: '新鲜橙子榨制，维C丰富',
          price: 12.00,
          imageUrl: '/images/products/orange_juice.jpg',
          categoryId: 6,
          categoryName: '饮品类',
          sales: 89,
          rating: 4.4,
          stock: 45,
          isHot: false,
          isNew: true
        },
        
        // 小食点心
        {
          id: 15,
          name: '小笼包',
          description: '皮薄馅大的小笼包，汁多味美',
          price: 15.00,
          imageUrl: '/images/products/xiaolongbao.jpg',
          categoryId: 7,
          categoryName: '小食点心',
          sales: 178,
          rating: 4.9,
          stock: 38,
          isHot: true,
          isNew: false
        },
        {
          id: 16,
          name: '煎饺',
          description: '外酥内嫩的煎饺，香气扑鼻',
          price: 12.00,
          imageUrl: '/images/products/jianjiao.jpg',
          categoryId: 7,
          categoryName: '小食点心',
          sales: 95,
          rating: 4.3,
          stock: 25,
          isHot: false,
          isNew: false
        },
        
        // 早餐类
        {
          id: 17,
          name: '白粥配咸菜',
          description: '清淡的白粥配爽口咸菜',
          price: 6.00,
          imageUrl: '/images/products/baizhou.jpg',
          categoryId: 8,
          categoryName: '早餐类',
          sales: 234,
          rating: 4.2,
          stock: 80,
          isHot: false,
          isNew: false
        },
        {
          id: 18,
          name: '豆浆油条',
          description: '经典早餐搭配豆浆油条',
          price: 8.00,
          imageUrl: '/images/products/doujiang_youtiao.jpg',
          categoryId: 8,
          categoryName: '早餐类',
          sales: 189,
          rating: 4.4,
          stock: 55,
          isHot: true,
          isNew: false
        }
      ]
      
      // 根据参数筛选商品
      let filteredProducts = [...mockProducts]
      
      // 分类筛选 - 支持字符串和数字类型的categoryId
      if (params?.categoryId && params.categoryId !== 'all') {
        const categoryId = typeof params.categoryId === 'string' ? parseInt(params.categoryId) : params.categoryId
        console.log('筛选分类ID:', categoryId, '类型:', typeof categoryId)
        filteredProducts = filteredProducts.filter(p => {
          console.log('商品分类ID:', p.categoryId, '匹配结果:', p.categoryId === categoryId)
          return p.categoryId === categoryId
        })
        console.log('分类筛选后商品数量:', filteredProducts.length)
      }
      
      // 关键词搜索
      if (params?.keyword) {
        const keyword = params.keyword.toLowerCase()
        filteredProducts = filteredProducts.filter(p => 
          p.name.toLowerCase().includes(keyword) || 
          p.description.toLowerCase().includes(keyword)
        )
      }
      
      // 价格区间筛选
      if (params?.priceRange) {
        const [min, max] = params.priceRange.split('-').map(p => p.replace('+', ''))
        filteredProducts = filteredProducts.filter(p => {
          if (max === '') {
            return p.price >= parseFloat(min)
          }
          return p.price >= parseFloat(min) && p.price <= parseFloat(max)
        })
      }
      
      // 库存筛选
      if (params?.stockFilter) {
        switch (params.stockFilter) {
          case 'in_stock':
            filteredProducts = filteredProducts.filter(p => p.stock > 10)
            break
          case 'low_stock':
            filteredProducts = filteredProducts.filter(p => p.stock > 0 && p.stock <= 10)
            break
          case 'out_of_stock':
            filteredProducts = filteredProducts.filter(p => p.stock === 0)
            break
        }
      }
      
      // 排序
      if (params?.sortBy) {
        switch (params.sortBy) {
          case 'price_asc':
            filteredProducts.sort((a, b) => a.price - b.price)
            break
          case 'price_desc':
            filteredProducts.sort((a, b) => b.price - a.price)
            break
          case 'sales_desc':
            filteredProducts.sort((a, b) => b.sales - a.sales)
            break
          case 'rating_desc':
            filteredProducts.sort((a, b) => b.rating - a.rating)
            break
          case 'create_time_desc':
            filteredProducts.sort((a, b) => b.isNew - a.isNew)
            break
        }
      }
      
      // 分页
      const current = params?.current || 1
      const size = params?.size || 6
      const start = (current - 1) * size
      const end = start + size
      const records = filteredProducts.slice(start, end)
      
      return {
        code: 200,
        data: {
          records,
          total: filteredProducts.length,
          current,
          size
        }
      }
    })
  },
  
  // 获取热门商品
  getHotProducts: (limit = 6) => {
    return productRequest.get(`/products/hot?limit=${limit}`)
  },
  
  // 获取商品详情
  getProductDetail: (id) => {
    return productRequest.get(`/products/${id}`)
  },
  
  // 获取系统统计数据
  getSystemStats: () => {
    return productRequest.get('/products/stats')
  }
}

// 订单API
export const orderApi = {
  // 创建订单
  createOrder: (data) => {
    return orderRequest.post('/orders', data)
  },
  
  // 获取订单列表
  getOrders: (userId) => {
    if (userId) {
      return orderRequest.get(`/orders/user/${userId}`)
    } else {
      return orderRequest.get('/orders')
    }
  },
  
  // 获取订单详情
  getOrderDetail: (id) => {
    return orderRequest.get(`/orders/${id}`)
  },
  
  // 更新订单状态
  updateOrderStatus: (id, status) => {
    return orderRequest.put(`/orders/${id}/status`, { status })
  },
  
  // 支付订单
  payOrder: (id) => {
    return orderRequest.post(`/orders/${id}/pay`)
  },
  
  // 取消订单
  cancelOrder: (id) => {
    return orderRequest.post(`/orders/${id}/cancel`)
  },
  
  // 商家接单（开始制作）
  prepareOrder: (id) => {
    return orderRequest.post(`/orders/${id}/prepare`)
  },
  
  // 订单制作完成（待取餐）
  readyOrder: (id) => {
    return orderRequest.post(`/orders/${id}/ready`)
  }
}

// 系统统计API
export const systemApi = {
  // 获取系统统计数据
  getSystemStats: () => {
    return userRequest.get('/system/stats')
  }
}

// 导入并重新导出推荐API
export { recommendApi } from './recommend'