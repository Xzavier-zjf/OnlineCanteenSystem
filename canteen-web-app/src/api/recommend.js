import { createServiceRequest } from './request'

const recommendRequest = createServiceRequest('recommend')

export const recommendApi = {
  getRecommendProducts: (userId, limit = 6) => {
    return recommendRequest.get(`/products/${userId}?limit=${limit}`)
  },

  getHotRecommendations: (limit = 6) => {
    return recommendRequest.get(`/hot?limit=${limit}`)
  },

  recordBehavior: (behaviorData) => {
    return recommendRequest.post('/behavior', behaviorData)
  },

  getUserPreferences: (userId) => {
    return recommendRequest.get(`/preferences/${userId}`)
  },

  getSimilarProducts: (productId, limit = 4) => {
    return recommendRequest.get(`/similar/${productId}?limit=${limit}`)
  },

  getCollaborativeRecommendations: (userId, limit = 6) => {
    return recommendRequest.get(`/collaborative/${userId}?limit=${limit}`)
  }
}

export default recommendApi
