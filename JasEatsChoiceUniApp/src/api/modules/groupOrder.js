/**
 * 团购相关API
 * 对接后端 GroupOrderController
 * 基础路径: /v1/group-orders
 */
import { get, post, put, del } from '@/utils/request'

export const groupOrderApi = {
  /**
   * 创建团购
   * POST /v1/group-orders
   * @param {Object} data - 团购数据
   * @param {string} data.userId - 发起人ID
   * @param {string} data.merchantId - 商家ID
   * @param {string} data.dishId - 菜品ID
   * @param {number} data.targetPeople - 目标人数
   * @param {number} data.minPeople - 最少人数
   * @param {number} data.maxPeople - 最多人数
   * @param {string} data.deadline - 截止时间
   * @param {string} data.description - 团购说明
   * @param {number} data.discountRate - 折扣率
   */
  create: (data) => post('/v1/group-orders', data),

  /**
   * 获取团购列表
   * GET /v1/group-orders
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.status - 状态(recruiting/in_progress/completed/cancelled)
   * @param {string} params.merchantId - 商家ID
   * @param {string} params.userId - 用户ID
   */
  getList: (params) => get('/v1/group-orders', params),

  /**
   * 获取附近团购
   * GET /v1/group-orders/nearby
   * @param {Object} params - 查询参数
   * @param {number} params.latitude - 纬度
   * @param {number} params.longitude - 经度
   * @param {number} params.radius - 半径(km)
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getNearby: (params) => get('/v1/group-orders/nearby', params),

  /**
   * 获取团购详情
   * GET /v1/group-orders/{id}
   * @param {string} id - 团购ID
   */
  getDetail: (id) => get(`/v1/group-orders/${id}`),

  /**
   * 加入团购
   * POST /v1/group-orders/{id}/join
   * @param {string} id - 团购ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {number} data.peopleCount - 参与人数
   * @param {string} data.remark - 备注
   */
  join: (id, data) => post(`/v1/group-orders/${id}/join`, data),

  /**
   * 退出团购
   * POST /v1/group-orders/{id}/quit
   * @param {string} id - 团购ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.reason - 退出原因
   */
  quit: (id, data) => post(`/v1/group-orders/${id}/quit`, data),

  /**
   * 获取团购成员列表
   * GET /v1/group-orders/{id}/members
   * @param {string} id - 团购ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getMembers: (id, params) => get(`/v1/group-orders/${id}/members`, params),

  /**
   * 邀请好友加入团购
   * POST /v1/group-orders/{id}/invite
   * @param {string} id - 团购ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 邀请人ID
   * @param {Array} data.friendIds - 好友ID数组
   */
  invite: (id, data) => post(`/v1/group-orders/${id}/invite`, data),

  /**
   * 更新团购信息
   * PUT /v1/group-orders/{id}
   * @param {string} id - 团购ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {number} data.targetPeople - 目标人数
   * @param {string} data.deadline - 截止时间
   * @param {string} data.description - 说明
   */
  update: (id, data) => put(`/v1/group-orders/${id}`, data),

  /**
   * 取消团购
   * DELETE /v1/group-orders/{id}
   * @param {string} id - 团购ID
   * @param {Object} params - 参数
   * @param {string} params.userId - 用户ID
   * @param {string} params.reason - 取消原因
   */
  delete: (id, params) => del(`/v1/group-orders/${id}`, params),

  /**
   * 确认团购成团
   * POST /v1/group-orders/{id}/confirm
   * @param {string} id - 团购ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   */
  confirm: (id, data) => post(`/v1/group-orders/${id}/confirm`, data),

  /**
   * 获取团购订单
   * GET /v1/group-orders/{id}/orders
   * @param {string} id - 团购ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getOrders: (id, params) => get(`/v1/group-orders/${id}/orders`, params),

  /**
   * 分享团购
   * POST /v1/group-orders/{id}/share
   * @param {string} id - 团购ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.platform - 分享平台(wechat/friends/moment)
   */
  share: (id, data) => post(`/v1/group-orders/${id}/share`, data),

  /**
   * 获取团购统计
   * GET /v1/group-orders/statistics
   * @param {Object} params - 查询参数
   * @param {string} params.groupOrderId - 团购ID
   */
  getStatistics: (params) => get('/v1/group-orders/statistics', params),

  /**
   * 获取用户参与的团购
   * GET /v1/group-orders/user/{userId}
   * @param {string} userId - 用户ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.status - 状态
   */
  getUserGroupOrders: (userId, params) => get(`/v1/group-orders/user/${userId}`, params)
}

export default groupOrderApi
