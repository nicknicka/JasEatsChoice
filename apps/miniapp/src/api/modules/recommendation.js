/**
 * 推荐系统API
 * 对接后端推荐系统接口（与桌面端一致）
 */
import { get, post } from '@/utils/request'

const RECOMMEND_API = {
  // 获取个性化推荐
  GET_RECOMMENDATIONS: (userId) => `/v1/recommendations/${userId}`,

  // 刷新推荐
  REFRESH: (userId) => `/v1/recommendations/${userId}/refresh`,

  // 记录反馈
  FEEDBACK: '/v1/recommendations/feedback',

  // 拒绝推荐
  REJECT: (userId) => `/v1/recommendations/${userId}/reject`,

  // 记录用户行为
  BEHAVIOR: '/v1/recommendations/behavior'
}

export const recommendationApi = {
  /**
   * 获取个性化推荐
   * @param {string} userId - 用户ID
   * @param {Object} options - 推荐选项
   * @param {string} options.scene - 推荐场景: home/personal/cart/dish_detail
   * @param {number} options.limit - 返回数量
   * @param {string} options.timePeriod - 时段: 早餐/午餐/晚餐/宵夜
   * @param {string} options.weather - 天气: sunny/rainy/hot/cold
   * @returns {Promise} 返回推荐菜品列表
   */
  getRecommendations(userId, options = {}) {
    const params = {
      scene: options.scene || 'home',
      limit: options.limit || 20,
      ...(options.timePeriod && { timePeriod: options.timePeriod }),
      ...(options.weather && { weather: options.weather })
    }

    return get(RECOMMEND_API.GET_RECOMMENDATIONS(userId), params)
  },

  /**
   * 刷新推荐
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回刷新结果
   */
  refreshRecommendations(userId) {
    return post(RECOMMEND_API.REFRESH(userId))
  },

  /**
   * 记录推荐反馈
   * @param {Object} feedbackData - 反馈数据
   * @param {string} feedbackData.userId - 用户ID
   * @param {string} feedbackData.dishId - 菜品ID
   * @param {string} feedbackData.recommendationId - 推荐ID
   * @param {boolean} feedbackData.isClicked - 是否点击
   * @param {boolean} feedbackData.isOrdered - 是否下单
   * @returns {Promise} 返回记录结果
   */
  recordFeedback(feedbackData) {
    return post(RECOMMEND_API.FEEDBACK, feedbackData)
  },

  /**
   * 拒绝推荐
   * @param {string} userId - 用户ID
   * @param {Object} data - 拒绝数据
   * @param {string} data.dishId - 菜品ID
   * @param {string} data.reason - 拒绝原因
   * @returns {Promise} 返回拒绝结果
   */
  rejectRecommendation(userId, data) {
    return post(RECOMMEND_API.REJECT(userId), data)
  },

  /**
   * 记录用户行为
   * @param {Object} behaviorData - 行为数据
   * @param {string} behaviorData.userId - 用户ID
   * @param {string} behaviorData.behaviorType - 行为类型
   * @param {string} behaviorData.itemType - 项目类型
   * @param {string} behaviorData.itemId - 项目ID
   * @param {Object} behaviorData.context - 上下文信息
   * @returns {Promise} 返回记录结果
   */
  recordBehavior(behaviorData) {
    return post(RECOMMEND_API.BEHAVIOR, behaviorData)
  }
}

export default recommendationApi
