/**
 * 拼单相关API
 * 对接后端 GroupOrderController
 * 基础路径: /v1/group-orders
 */
import { get, post, put, del } from '@/utils/request'
import { GROUP_ORDER_API, buildUrl } from '../urlEnum'

export const groupOrderApi = {
  /**
   * 创建拼单
   * POST /v1/group-orders
   * @param {Object} data - 拼单数据
   * @param {string} data.userId - 发起人ID
   * @param {string} data.merchantId - 商家ID
   * @param {string} data.dishId - 菜品ID
   * @param {number} data.targetPeople - 目标人数
   * @param {number} data.minPeople - 最少人数
   * @param {number} data.maxPeople - 最多人数
   * @param {string} data.deadline - 截止时间
   * @param {string} data.description - 拼单说明
   * @param {number} data.discountRate - 折扣率
   * @returns {Promise} 返回创建结果
   */
  createGroupOrder: (data) => post(GROUP_ORDER_API.CREATE_GROUP_ORDER, data),

  /**
   * 创建拼单（别名）
   * @param {Object} data - 拼单数据
   * @returns {Promise} 返回创建结果
   */
  create: (data) => post(GROUP_ORDER_API.CREATE_GROUP_ORDER, data),

  /**
   * 获取拼单列表
   * GET /v1/group-orders
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.status - 状态(recruiting/in_progress/completed/cancelled)
   * @param {string} params.merchantId - 商家ID
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回拼单列表
   */
  getGroupOrders: (params) => get(GROUP_ORDER_API.GET_GROUP_ORDERS, params),

  /**
   * 获取拼单列表（别名）
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回拼单列表
   */
  getList: (params) => get(GROUP_ORDER_API.GET_GROUP_ORDERS, params),

  /**
   * 获取附近拼单
   * GET /v1/group-orders/nearby
   * @param {Object} params - 查询参数
   * @param {number} params.latitude - 纬度（Double类型）
   * @param {number} params.longitude - 经度（Double类型）
   * @param {number} params.radius - 半径(km)
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回附近拼单列表
   */
  getNearby: (params) => {
    // 确保数值类型正确
    const processedParams = {}
    if (params) {
      if (params.latitude !== undefined && params.latitude !== null) {
        processedParams.latitude = Number(params.latitude)
      }
      if (params.longitude !== undefined && params.longitude !== null) {
        processedParams.longitude = Number(params.longitude)
      }
      if (params.radius !== undefined && params.radius !== null) {
        processedParams.radius = Number(params.radius)
      }
      if (params.page !== undefined) {
        processedParams.page = Number(params.page)
      }
      if (params.size !== undefined) {
        processedParams.size = Number(params.size)
      }
    }
    return get('/v1/group-orders/nearby', processedParams)
  },

  /**
   * 获取拼单详情
   * GET /v1/group-orders/{groupOrderId}
   * @param {string} id - 拼单ID
   * @returns {Promise} 返回拼单详情
   */
  getGroupOrder: (id) => get(buildUrl(GROUP_ORDER_API.GET_GROUP_ORDER, { groupOrderId: id })),

  /**
   * 获取拼单详情（别名）
   * @param {string} id - 拼单ID
   * @returns {Promise} 返回拼单详情
   */
  getDetail: (id) => get(`/v1/group-orders/${id}`),

  /**
   * 加入拼单
   * POST /v1/group-orders/{groupOrderId}/join
   * @param {string} id - 拼单ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {number} data.peopleCount - 参与人数
   * @param {string} data.remark - 备注
   * @returns {Promise} 返回加入结果
   */
  joinGroupOrder: (id, data) => post(buildUrl(GROUP_ORDER_API.JOIN_GROUP_ORDER, { groupOrderId: id }), data),

  /**
   * 加入拼单（别名）
   * @param {string} id - 拼单ID
   * @param {Object} data - 数据
   * @returns {Promise} 返回加入结果
   */
  join: (id, data) => post(`/v1/group-orders/${id}/join`, data),

  /**
   * 离开拼单
   * POST /v1/group-orders/{groupOrderId}/leave
   * @param {string} id - 拼单ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.reason - 离开原因
   * @returns {Promise} 返回离开结果
   */
  leaveGroupOrder: (id, data) => post(buildUrl(GROUP_ORDER_API.LEAVE_GROUP_ORDER, { groupOrderId: id }), data),

  /**
   * 离开拼单（别名）
   * @param {string} id - 拼单ID
   * @param {Object} data - 数据
   * @returns {Promise} 返回离开结果
   */
  quit: (id, data) => post(`/v1/group-orders/${id}/quit`, data),

  /**
   * 更新拼单
   * PUT /v1/group-orders/{groupOrderId}
   * @param {string} id - 拼单ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {number} data.targetPeople - 目标人数
   * @param {string} data.deadline - 截止时间
   * @param {string} data.description - 说明
   * @returns {Promise} 返回更新结果
   */
  updateGroupOrder: (id, data) => put(buildUrl(GROUP_ORDER_API.UPDATE_GROUP_ORDER, { groupOrderId: id }), data),

  /**
   * 更新拼单（别名）
   * @param {string} id - 拼单ID
   * @param {Object} data - 数据
   * @returns {Promise} 返回更新结果
   */
  update: (id, data) => put(`/v1/group-orders/${id}`, data),

  /**
   * 删除拼单
   * DELETE /v1/group-orders/{groupOrderId}
   * @param {string} id - 拼单ID
   * @param {Object} params - 参数
   * @param {string} params.userId - 用户ID
   * @param {string} params.reason - 取消原因
   * @returns {Promise} 返回删除结果
   */
  deleteGroupOrder: (id, params) => del(buildUrl(GROUP_ORDER_API.DELETE_GROUP_ORDER, { groupOrderId: id }), params),

  /**
   * 删除拼单（别名）
   * @param {string} id - 拼单ID
   * @param {Object} params - 参数
   * @returns {Promise} 返回删除结果
   */
  delete: (id, params) => del(`/v1/group-orders/${id}`, params),

  /**
   * 获取拼单成员列表
   * GET /v1/group-orders/{id}/members
   * @param {string} id - 拼单ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回成员列表
   */
  getMembers: (id, params) => get(`/v1/group-orders/${id}/members`, params),

  /**
   * 邀请好友加入拼单
   * POST /v1/group-orders/{id}/invite
   * @param {string} id - 拼单ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 邀请人ID
   * @param {Array} data.friendIds - 好友ID数组
   * @returns {Promise} 返回邀请结果
   */
  invite: (id, data) => post(`/v1/group-orders/${id}/invite`, data),

  /**
   * 确认拼单成团
   * POST /v1/group-orders/{id}/confirm
   * @param {string} id - 拼单ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @returns {Promise} 返回确认结果
   */
  confirm: (id, data) => post(`/v1/group-orders/${id}/confirm`, data),

  /**
   * 获取拼单订单
   * GET /v1/group-orders/{id}/orders
   * @param {string} id - 拼单ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回订单列表
   */
  getOrders: (id, params) => get(`/v1/group-orders/${id}/orders`, params),

  /**
   * 分享拼单
   * POST /v1/group-orders/{id}/share
   * @param {string} id - 拼单ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.platform - 分享平台(wechat/friends/moment)
   * @returns {Promise} 返回分享结果
   */
  share: (id, data) => post(`/v1/group-orders/${id}/share`, data),

  /**
   * 获取拼单统计
   * GET /v1/group-orders/statistics
   * @param {Object} params - 查询参数
   * @param {string} params.groupOrderId - 拼单ID
   * @returns {Promise} 返回统计数据
   */
  getStatistics: (params) => get('/v1/group-orders/statistics', params),

  /**
   * 获取用户参与的拼单
   * GET /v1/group-orders/user/{userId}
   * @param {string} userId - 用户ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.status - 状态
   * @returns {Promise} 返回拼单列表
   */
  getUserGroupOrders: (userId, params) => get(`/v1/group-orders/user/${userId}`, params)
}

export default groupOrderApi
