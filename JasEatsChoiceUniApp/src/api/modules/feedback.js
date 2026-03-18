/**
 * 用户反馈相关API
 * 对接后端 FeedbackController
 * 基础路径: /v1/feedback
 */
import { get, post, put } from '@/utils/request'

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
   */
  submit: (data) => post('/v1/feedback', data),

  /**
   * 获取反馈列表
   * GET /v1/feedback
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.type - 反馈类型
   * @param {string} params.status - 状态(pending/processing/resolved/closed)
   */
  getList: (params) => get('/v1/feedback', params),

  /**
   * 获取反馈详情
   * GET /v1/feedback/{id}
   * @param {string} id - 反馈ID
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
   */
  append: (id, data) => post(`/v1/feedback/${id}/append`, data),

  /**
   * 上传反馈图片
   * POST /v1/feedback/images
   * @param {FormData} formData - 图片文件
   */
  uploadImage: (formData) => post('/v1/feedback/images', formData),

  /**
   * 获取反馈分类
   * GET /v1/feedback/categories
   */
  getCategories: () => get('/v1/feedback/categories'),

  /**
   * 获取常见问题
   * GET /v1/feedback/faq
   * @param {Object} params - 查询参数
   * @param {string} params.category - 分类
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
   */
  rate: (id, data) => post(`/v1/feedback/${id}/rate`, data),

  /**
   * 获取反馈模板
   * GET /v1/feedback/templates
   * @param {Object} params - 查询参数
   * @param {string} params.type - 反馈类型
   */
  getTemplates: (params) => get('/v1/feedback/templates', params),

  /**
   * 检查是否有未读回复
   * GET /v1/feedback/unread/reply
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   */
  hasUnreadReply: (params) => get('/v1/feedback/unread/reply', params),

  /**
   * 标记反馈回复已读
   * PUT /v1/feedback/{id}/read
   * @param {string} id - 反馈ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   */
  markReplyRead: (id, data) => put(`/v1/feedback/${id}/read`, data)
}

export default feedbackApi
