/**
 * 用户反馈相关API
 * 对接后端 FeedbackController
 * 基础路径: /v1/feedback
 */
import { get, post, put } from '@/utils/request'
import { FEEDBACK_API, buildUrl } from '../urlEnum'

export const feedbackApi = {
  /**
   * 提交反馈
   * POST /v1/feedback
   * @param {Object} data - 反馈数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.type - 反馈类型(bug/suggestion/complaint/praise/other)
   * @param {string} data.content - 反馈内容
   * @param {Array} data.images - 图片列表
   * @param {string} data.contact - 联系方式
   * @param {Object} data.deviceInfo - 设备信息
   * @returns {Promise} 返回提交结果
   */
  submit: (data) => post(FEEDBACK_API.CREATE_FEEDBACK, data),

  /**
   * 提交反馈（别名）
   * @param {Object} data - 反馈数据
   * @returns {Promise} 返回提交结果
   */
  create: (data) => post(FEEDBACK_API.CREATE_FEEDBACK, data),

  /**
   * 获取反馈列表
   * GET /v1/feedback
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.type - 反馈类型
   * @param {string} params.status - 状态(pending/processing/resolved/closed)
   * @returns {Promise} 返回反馈列表
   */
  getFeedbacks: (params) => get(FEEDBACK_API.GET_FEEDBACKS, params),

  /**
   * 获取反馈列表（别名）
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回反馈列表
   */
  getList: (params) => get(FEEDBACK_API.GET_FEEDBACKS, params),

  /**
   * 获取反馈详情
   * GET /v1/feedback/{feedbackId}
   * @param {string} id - 反馈ID
   * @returns {Promise} 返回反馈详情
   */
  getFeedback: (id) => get(buildUrl('/v1/feedback/:feedbackId', { feedbackId: id })),

  /**
   * 获取反馈详情（别名）
   * @param {string} id - 反馈ID
   * @returns {Promise} 返回反馈详情
   */
  getDetail: (id) => get(`/v1/feedback/${id}`),

  /**
   * 追加反馈内容
   * POST /v1/feedback/{id}/append
   * @param {string} id - 反馈ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.content - 追加内容
   * @param {Array} data.images - 图片列表
   * @returns {Promise} 返回追加结果
   */
  append: (id, data) => post(`/v1/feedback/${id}/append`, data),

  /**
   * 上传反馈图片
   * POST /v1/feedback/images
   * @param {FormData} formData - 图片文件
   * @returns {Promise} 返回上传结果
   */
  uploadImage: (formData) => post('/v1/feedback/images', formData),

  /**
   * 获取反馈分类
   * GET /v1/feedback/categories
   * @returns {Promise} 返回分类列表
   */
  getCategories: () => get('/v1/feedback/categories'),

  /**
   * 获取常见问题
   * GET /v1/feedback/faq
   * @param {Object} params - 查询参数
   * @param {string} params.category - 分类
   * @returns {Promise} 返回常见问题列表
   */
  getFAQ: (params) => get('/v1/feedback/faq', params),

  /**
   * 评价反馈处理结果
   * POST /v1/feedback/{id}/rate
   * @param {string} id - 反馈ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {number} data.rating - 评分(1-5)
   * @param {string} data.comment - 评论
   * @returns {Promise} 返回评价结果
   */
  rate: (id, data) => post(`/v1/feedback/${id}/rate`, data),

  /**
   * 获取反馈模板
   * GET /v1/feedback/templates
   * @param {Object} params - 查询参数
   * @param {string} params.type - 反馈类型
   * @returns {Promise} 返回模板列表
   */
  getTemplates: (params) => get('/v1/feedback/templates', params),

  /**
   * 检查是否有未读回复
   * GET /v1/feedback/unread/reply
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回未读状态
   */
  hasUnreadReply: (params) => get('/v1/feedback/unread/reply', params),

  /**
   * 标记反馈回复已读
   * PUT /v1/feedback/{id}/read
   * @param {string} id - 反馈ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @returns {Promise} 返回标记结果
   */
  markReplyRead: (id, data) => put(`/v1/feedback/${id}/read`, data),

  /**
   * 删除反馈
   * DELETE /v1/feedback/{feedbackId}
   * @param {string} id - 反馈ID
   * @returns {Promise} 返回删除结果
   */
  delete: (id) => {
    // 由于 del 函数签名，这里需要特殊处理
    return put(`/v1/feedback/${id}/delete`, {})
  }
}

export default feedbackApi
