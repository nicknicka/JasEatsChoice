/**
 * 节日推荐相关API
 */
import api from '../utils/api'

export default {
  /**
   * 获取节日列表
   */
  getFestivals() {
    return api.get('/v1/festival/festivals')
  },

  /**
   * 获取当前节日推荐
   */
  getCurrentRecommendations() {
    return api.get('/v1/festival/recommendations/current')
  },

  /**
   * 根据节日ID获取推荐
   */
  getRecommendationsByFestival(festivalId) {
    return api.get(`/v1/festival/recommendations/festival/${festivalId}`)
  },

  /**
   * 提交反馈
   */
  submitFeedback(recommendId, data) {
    return api.post(`/v1/festival/recommendations/${recommendId}/feedback`, data)
  },

  /**
   * 获取用户自定义事件
   */
  getCustomEvents() {
    return api.get('/v1/festival/events/custom')
  },

  /**
   * 创建用户自定义事件
   */
  createCustomEvent(data) {
    return api.post('/v1/festival/events/custom', data)
  },

  /**
   * 更新用户自定义事件
   */
  updateCustomEvent(eventId, data) {
    return api.put(`/v1/festival/events/custom/${eventId}`, data)
  },

  /**
   * 删除用户自定义事件
   */
  deleteCustomEvent(eventId) {
    return api.delete(`/v1/festival/events/custom/${eventId}`)
  }
}
