/**
 * 推荐系统API
 * 与后端推荐系统接口对接
 */

import axios from 'axios'
import { API_CONFIG } from '../config/index.js'

const BASE_URL = API_CONFIG.baseURL

/**
 * 推荐系统API
 */
export const recommendationAPI = {
  /**
   * 获取个性化推荐
   * @param {string|number} userId - 用户ID
   * @param {Object} options - 推荐选项
   * @param {string} options.scene - 推荐场景: home/personal/cart/dish_detail
   * @param {number} options.limit - 返回数量
   * @param {string} options.timePeriod - 时段: 早餐/午餐/晚餐/宵夜
   * @param {string} options.weather - 天气: sunny/rainy/hot/cold
   */
  getRecommendations(userId, options = {}) {
    const params = {
      scene: options.scene || 'home',
      limit: options.limit || 20,
      ...(options.timePeriod && { timePeriod: options.timePeriod }),
      ...(options.weather && { weather: options.weather })
    }

    return axios.get(`${BASE_URL}/v1/recommend/recommend/${userId}`, { params })
  },

  /**
   * 刷新推荐
   * @param {string|number} userId - 用户ID
   */
  refreshRecommendations(userId) {
    return axios.post(`${BASE_URL}/v1/recommend/recommend/${userId}/refresh`)
  },

  /**
   * 记录推荐反馈
   * @param {Object} feedbackData - 反馈数据
   * @param {string} feedbackData.userId - 用户ID
   * @param {string} feedbackData.dishId - 菜品ID
   * @param {string} feedbackData.recommendationId - 推荐ID
   * @param {boolean} feedbackData.isClicked - 是否点击
   * @param {boolean} feedbackData.isOrdered - 是否下单
   */
  recordFeedback(feedbackData) {
    return axios.post(`${BASE_URL}/v1/recommend/feedback`, feedbackData)
  },

  /**
   * 拒绝推荐
   * @param {string|number} userId - 用户ID
   * @param {Object} data - 拒绝数据
   * @param {string} data.dishId - 菜品ID
   * @param {string} data.reason - 拒绝原因
   */
  rejectRecommendation(userId, data) {
    return axios.post(`${BASE_URL}/v1/recommend/recommend/${userId}/reject`, data)
  },

  /**
   * 替换推荐菜品
   * @param {string|number} userId - 用户ID
   * @param {string[]} replaceDishIds - 要替换的菜品ID列表
   */
  replaceRecommendations(userId, replaceDishIds) {
    return axios.post(`${BASE_URL}/v1/recommend/recommend/${userId}/replace`, {
      replaceDishIds
    })
  },

  /**
   * 筛选推荐菜品
   * @param {string|number} userId - 用户ID
   * @param {Object} filters - 筛选条件
   * @param {string} filters.category - 分类
   * @param {number} filters.minCalorie - 最小卡路里
   * @param {number} filters.maxCalorie - 最大卡路里
   * @param {number} filters.minPrice - 最小价格
   * @param {number} filters.maxPrice - 最大价格
   */
  filterRecommendations(userId, filters = {}) {
    return axios.post(`${BASE_URL}/v1/recommend/recommend/${userId}/filter`, filters)
  },

  /**
   * 获取推荐理由
   * @param {string|number} userId - 用户ID
   * @param {string} dishId - 菜品ID
   */
  getRecommendationReason(userId, dishId) {
    return axios.get(`${BASE_URL}/v1/recommend/recommend/${userId}/reason/${dishId}`)
  },

  /**
   * 获取用户画像
   * @param {string|number} userId - 用户ID
   */
  getUserProfile(userId) {
    return axios.get(`${BASE_URL}/v1/recommend/profile/${userId}`)
  },

  /**
   * 设置推荐偏好
   * @param {string|number} userId - 用户ID
   * @param {Object} preferences - 偏好设置
   * @param {string} preferences.dietGoal - 饮食目标: low_calorie/high_protein/balanced
   */
  setUserPreference(userId, preferences) {
    return axios.put(`${BASE_URL}/v1/recommend/users/${userId}/prefer`, preferences)
  },

  /**
   * 获取用户行为历史
   * @param {string|number} userId - 用户ID
   * @param {number} limit - 返回数量
   */
  getUserBehaviors(userId, limit = 50) {
    return axios.get(`${BASE_URL}/v1/recommend/behavior/${userId}`, {
      params: { limit }
    })
  },

  /**
   * 记录用户行为
   * @param {Object} behaviorData - 行为数据
   * @param {string} behaviorData.userId - 用户ID
   * @param {string} behaviorData.behaviorType - 行为类型
   * @param {string} behaviorData.itemType - 项目类型
   * @param {string} behaviorData.itemId - 项目ID
   * @param {Object} behaviorData.context - 上下文信息
   */
  recordBehavior(behaviorData) {
    return axios.post(`${BASE_URL}/v1/recommend/behavior`, behaviorData)
  },

  /**
   * 生成购物清单
   * @param {string|number} userId - 用户ID
   * @param {string} date - 日期
   */
  generateShoppingList(userId, date) {
    const params = date ? { date } : {}
    return axios.get(`${BASE_URL}/v1/recommend/recipe/${userId}/shopping-list`, {
      params
    })
  }
}

export default recommendationAPI
