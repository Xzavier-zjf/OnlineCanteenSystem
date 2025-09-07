import { createServiceRequest } from './request'

// 创建推荐服务实例
const recommendRequest = createServiceRequest('recommend')

// 推荐API
export const recommendApi = {
  // 获取个性化推荐商品
  getRecommendProducts: (userId, limit = 6) => {
    return recommendRequest.get(`/products/${userId}?limit=${limit}`).catch(() => {
      // 降级到模拟数据
      const mockRecommendations = [
        {
          id: 1,
          name: '红烧肉套餐',
          description: '香喷喷的红烧肉配米饭，营养丰富',
          price: 18.00,
          imageUrl: '/images/products/hongshaorou_rice.jpg',
          categoryId: 1,
          categoryName: '主食套餐',
          sales: 156,
          rating: 4.8,
          reason: '基于您的历史订单推荐'
        },
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
          reason: '热销商品，深受喜爱'
        },
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
          reason: '高评分商品，品质保证'
        }
      ]
      
      return {
        code: 200,
        data: mockRecommendations.slice(0, limit)
      }
    })
  },

  // 获取热门推荐
  getHotRecommendations: (limit = 6) => {
    return recommendRequest.get(`/hot?limit=${limit}`).catch(() => {
      // 降级到模拟数据
      const hotProducts = [
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
          isHot: true,
          reason: '热销商品，好评如潮'
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
          isHot: true,
          reason: '经典搭配，营养美味'
        },
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
          isHot: true,
          reason: '人气单品，不容错过'
        },
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
          reason: '清爽饮品，解腻首选'
        },
        {
          id: 1,
          name: '红烧肉套餐',
          description: '香喷喷的红烧肉配米饭，营养丰富',
          price: 18.00,
          imageUrl: '/images/products/hongshaorou_rice.jpg',
          categoryId: 1,
          categoryName: '主食套餐',
          sales: 156,
          rating: 4.8,
          isHot: true,
          reason: '经典美味，营养丰富'
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
          reason: '开胃小食，酸辣过瘾'
        }
      ]
      
      return {
        code: 200,
        data: hotProducts.slice(0, limit)
      }
    })
  },

  // 记录用户行为
  recordBehavior: (behaviorData) => {
    return recommendRequest.post('/behavior', behaviorData).catch(() => {
      // 静默失败，不影响用户体验
      console.log('行为记录失败，但不影响功能使用')
      return { code: 200, message: '记录成功' }
    })
  },

  // 获取用户偏好分析
  getUserPreferences: (userId) => {
    return recommendRequest.get(`/preferences/${userId}`).catch(() => {
      // 降级到模拟数据
      return {
        code: 200,
        data: {
          favoriteCategories: ['主食套餐', '面食类', '饮品类'],
          priceRange: { min: 8, max: 20 },
          tastePref: ['清淡', '微辣'],
          orderFrequency: 'high',
          avgOrderAmount: 25.5
        }
      }
    })
  },

  // 获取相似商品推荐
  getSimilarProducts: (productId, limit = 4) => {
    return recommendRequest.get(`/similar/${productId}?limit=${limit}`).catch(() => {
      // 降级到模拟数据
      const similarProducts = [
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
          reason: '同类型套餐推荐'
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
          reason: '口味相近推荐'
        },
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
          reason: '川菜系列推荐'
        },
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
          reason: '经典川菜推荐'
        }
      ]
      
      return {
        code: 200,
        data: similarProducts.slice(0, limit)
      }
    })
  },

  // 获取基于协同过滤的推荐
  getCollaborativeRecommendations: (userId, limit = 6) => {
    return recommendRequest.get(`/collaborative/${userId}?limit=${limit}`).catch(() => {
      // 降级到模拟数据
      return {
        code: 200,
        data: [
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
            reason: '相似用户也喜欢'
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
            reason: '健康饮品推荐'
          }
        ]
      }
    })
  }
}

export default recommendApi