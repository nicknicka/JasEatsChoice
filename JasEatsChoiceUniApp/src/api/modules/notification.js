/**
 * 通知相关API
 * 对接后端 NotificationController
 * 基础路径: /v1/notifications
 */
import { get, post, put, del } from '@/utils/request'
import { NOTIFICATION_API, buildUrl } from '../urlEnum'

export const notificationApi = {
  /**
   * 获取通知列表
   * GET /v1/notifications
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.type - 类型(system/order/chat/activity/all)
   * @param {string} params.status - 状态(read/unread/all)
   * @returns {Promise} 返回通知列表
   */
  getNotifications: (params) => get(NOTIFICATION_API.GET_NOTIFICATIONS, params),

  /**
   * 获取通知列表（别名）
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回通知列表
   */
  getList: (params) => get(NOTIFICATION_API.GET_NOTIFICATIONS, params),

  /**
   * 获取未读通知数量
   * GET /v1/notifications/unread/count
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回未读数量
   */
  getUnreadCount: (params) => get('/v1/notifications/unread/count', params),

  /**
   * 获取通知详情
   * GET /v1/notifications/{notificationId}
   * @param {string} id - 通知ID
   * @returns {Promise} 返回通知详情
   */
  getNotification: (id) => get(buildUrl(NOTIFICATION_API.GET_NOTIFICATION, { notificationId: id })),

  /**
   * 获取通知详情（别名）
   * @param {string} id - 通知ID
   * @returns {Promise} 返回通知详情
   */
  getDetail: (id) => get(`/v1/notifications/${id}`),

  /**
   * 标记为已读
   * PUT /v1/notifications/{notificationId}/read
   * @param {string} id - 通知ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @returns {Promise} 返回标记结果
   */
  markAsRead: (id, data) => put(buildUrl(NOTIFICATION_API.MARK_READ, { notificationId: id }), data),

  /**
   * 批量标记为已读
   * PUT /v1/notifications/batch/read
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {Array} data.ids - 通知ID数组
   * @returns {Promise} 返回标记结果
   */
  batchMarkAsRead: (data) => put('/v1/notifications/batch/read', data),

  /**
   * 标记全部为已读
   * PUT /v1/notifications/read-all
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @returns {Promise} 返回标记结果
   */
  markAllAsRead: (data) => put(NOTIFICATION_API.MARK_ALL_READ, data),

  /**
   * 删除通知
   * DELETE /v1/notifications/{notificationId}
   * @param {string} id - 通知ID
   * @param {Object} params - 参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回删除结果
   */
  delete: (id, params) => del(buildUrl(NOTIFICATION_API.DELETE_NOTIFICATION, { notificationId: id }), params),

  /**
   * 批量删除通知
   * DELETE /v1/notifications/batch
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {Array} data.ids - 通知ID数组
   * @returns {Promise} 返回删除结果
   */
  batchDelete: (data) => del('/v1/notifications/batch', data),

  /**
   * 清空所有通知
   * DELETE /v1/notifications/clear
   * @param {Object} params - 参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回清空结果
   */
  clear: (params) => del('/v1/notifications/clear', params),

  /**
   * 获取系统通知
   * GET /v1/notifications/system
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回系统通知列表
   */
  getSystemNotifications: (params) => get('/v1/notifications/system', params),

  /**
   * 获取活动通知
   * GET /v1/notifications/activity
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回活动通知列表
   */
  getActivityNotifications: (params) => get('/v1/notifications/activity', params),

  /**
   * 订阅推送通知
   * POST /v1/notifications/subscribe
   * @param {Object} data - 订阅数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.deviceToken - 设备Token
   * @param {string} data.platform - 平台(iOS/Android/Web)
   * @returns {Promise} 返回订阅结果
   */
  subscribe: (data) => post('/v1/notifications/subscribe', data),

  /**
   * 取消订阅推送通知
   * POST /v1/notifications/unsubscribe
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.deviceToken - 设备Token
   * @returns {Promise} 返回取消订阅结果
   */
  unsubscribe: (data) => post('/v1/notifications/unsubscribe', data),

  /**
   * 设置通知偏好
   * PUT /v1/notifications/preferences
   * @param {Object} data - 偏好设置
   * @param {string} data.userId - 用户ID
   * @param {boolean} data.orderNotify - 订单通知开关
   * @param {boolean} data.chatNotify - 聊天通知开关
   * @param {boolean} data.activityNotify - 活动通知开关
   * @param {boolean} data.systemNotify - 系统通知开关
   * @returns {Promise} 返回设置结果
   */
  setPreferences: (data) => put('/v1/notifications/preferences', data),

  /**
   * 获取通知偏好设置
   * GET /v1/notifications/preferences
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回偏好设置
   */
  getPreferences: (params) => get('/v1/notifications/preferences', params)
}

export default notificationApi
