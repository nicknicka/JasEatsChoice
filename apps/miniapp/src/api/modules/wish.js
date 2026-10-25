/**
 * 心愿单管理 API
 * 对接后端 WishController
 * 基础路径: /v1/wishes
 */
import { get, post, put, del } from '@/utils/request'

export const wishApi = {
  /**
   * WISH-001: 获取用户心愿列表
   * GET /v1/wishes
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {string} params.status - 状态（pending/accepted/rejected/completed）
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getUserList: (params) => get('/v1/wishes', params),

  /**
   * WISH-002: 获取心愿详情
   * GET /v1/wishes/{wishId}
   * @param {string} wishId - 心愿ID
   */
  getDetail: (wishId) => get(`/v1/wishes/${wishId}`),

  /**
   * WISH-003: 点赞心愿
   * POST /v1/wishes/{wishId}/like
   * @param {string} wishId - 心愿ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   */
  like: (wishId, data) => post(`/v1/wishes/${wishId}/like`, data),

  /**
   * 取消点赞
   * POST /v1/wishes/{wishId}/unlike
   * @param {string} wishId - 心愿ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   */
  unlike: (wishId, data) => post(`/v1/wishes/${wishId}/unlike`, data),

  /**
   * WISH-004: 发布心愿
   * POST /v1/wishes
   * @param {Object} data - 心愿数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.content - 心愿内容
   * @param {string} data.category - 分类
   * @param {Array} data.dishes - 期望菜品列表
   * @param {Array} data.images - 图片列表
   * @param {number} data.budget - 预算
   * @param {string} data.expectedTime - 期望时间
   */
  create: (data) => post('/v1/wishes', data),

  /**
   * 更新心愿
   * PUT /v1/wishes/{wishId}
   * @param {string} wishId - 心愿ID
   * @param {Object} data - 心愿数据
   */
  update: (wishId, data) => put(`/v1/wishes/${wishId}`, data),

  /**
   * 删除心愿
   * DELETE /v1/wishes/{wishId}
   * @param {string} wishId - 心愿ID
   * @param {Object} params - 参数
   * @param {string} params.userId - 用户ID
   */
  delete: (wishId, params) => del(`/v1/wishes/${wishId}`, params),

  /**
   * WISH-005: 获取商家心愿单列表
   * GET /v1/wishes/merchant
   * @param {Object} params - 查询参数
   * @param {string} params.merchantId - 商家ID
   * @param {string} params.status - 状态筛选
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getMerchantList: (params) => get('/v1/wishes/merchant', params),

  /**
   * WISH-006: 商家接受心愿
   * POST /v1/wishes/{wishId}/accept
   * @param {string} wishId - 心愿ID
   * @param {Object} data - 数据
   * @param {string} data.merchantId - 商家ID
   * @param {string} data.quote - 报价
   * @param {string} data.estimatedTime - 预计时间
   * @param {string} data.remark - 备注
   */
  accept: (wishId, data) => post(`/v1/wishes/${wishId}/accept`, data),

  /**
   * WISH-007: 商家拒绝心愿
   * POST /v1/wishes/{wishId}/reject
   * @param {string} wishId - 心愿ID
   * @param {Object} data - 数据
   * @param {string} data.merchantId - 商家ID
   * @param {string} data.reason - 拒绝原因
   */
  reject: (wishId, data) => post(`/v1/wishes/${wishId}/reject`, data),

  /**
   * WISH-008: 获取商家端心愿详情（审核）
   * GET /v1/wishes/{wishId}/audit
   * @param {string} wishId - 心愿ID
   * @param {Object} params - 参数
   * @param {string} params.merchantId - 商家ID
   */
  getAuditDetail: (wishId, params) => get(`/v1/wishes/${wishId}/audit`, params),

  /**
   * 用户选择商家
   * POST /v1/wishes/{wishId}/select-merchant
   * @param {string} wishId - 心愿ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.merchantId - 商家ID
   */
  selectMerchant: (wishId, data) => post(`/v1/wishes/${wishId}/select-merchant`, data),

  /**
   * 获取心愿分类
   * GET /v1/wishes/categories
   */
  getCategories: () => get('/v1/wishes/categories'),

  /**
   * 搜索心愿
   * GET /v1/wishes/search
   * @param {Object} params - 查询参数
   * @param {string} params.keyword - 关键词
   * @param {string} params.category - 分类
   * @param {number} params.page - 页码
   */
  search: (params) => get('/v1/wishes/search', params)
}

export default wishApi
