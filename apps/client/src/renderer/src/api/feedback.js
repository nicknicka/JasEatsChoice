/**
 * 用户反馈 API（统一契约 /v1/feedback）。
 *
 * 归属只由后端从可信 JWT 推导，请求体不要携带 userId。
 */
import api from '../utils/api'
import { API_CONFIG } from '../config/index.js'

/**
 * 提交反馈
 * @param {Object} data - { type, content, contact?, deviceInfo?, orderId?, merchantId?, images? }
 * @returns {Promise}
 */
export const createFeedback = (data) => api.post(API_CONFIG.feedback.create, data)

/**
 * 查看自己的反馈历史
 * @param {Object} params - { page?, size?, type?, status? }
 * @returns {Promise}
 */
export const listFeedbacks = (params = {}) => api.get(API_CONFIG.feedback.list, { params })

/**
 * 查看自己的反馈详情
 * @param {string} feedbackId
 * @returns {Promise}
 */
export const getFeedback = (feedbackId) =>
  api.get(API_CONFIG.feedback.detail.replace('{feedbackId}', encodeURIComponent(feedbackId)))

export default { createFeedback, listFeedbacks, getFeedback }
