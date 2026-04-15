/**
 * 节日推荐相关API
 */
import api from '../utils/api'

export default {
  /**
   * 获取节日列表
   */
  getFestivals() {
    return api.get('/v1/festival/active')
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
    return api.post('/v1/festival/feedback', {
      recommendHistoryId: recommendId,
      ...data
    })
  },

  /**
   * 获取用户自定义事件
   */
  getCustomEvents() {
    return api.get('/v1/festival/custom-events')
  },

  /**
   * 创建用户自定义事件
   */
  createCustomEvent(data) {
    return api.post('/v1/festival/custom-event', data)
  },

  /**
   * 更新用户自定义事件
   */
  updateCustomEvent(eventId, data) {
    return Promise.reject(new Error('后端暂未提供自定义事件更新接口'))
  },

  /**
   * 删除用户自定义事件
   */
  deleteCustomEvent(eventId) {
    return Promise.reject(new Error('后端暂未提供自定义事件删除接口'))
  }
}
