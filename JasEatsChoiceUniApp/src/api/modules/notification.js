/**
 * 通知相关API
 * 对接后端 NotificationController
 * 基础路径: /notifications
 */
import { get, put, del } from '@/utils/request'
import { NOTIFICATION_API, buildUrl } from '../urlEnum'

const getCurrentUserId = () => {
  const userInfo = uni.getStorageSync('userInfo') || {}
  return userInfo.userId || userInfo.id || ''
}

const ensureUserId = (params = {}) => ({
  ...params,
  userId: params.userId || getCurrentUserId()
})

const buildQueryUrl = (url, params = {}) => {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&')
  return query ? `${url}?${query}` : url
}

const localPreferenceKey = (userId) => `notification-preferences:${userId || 'anonymous'}`

const successResponse = (data = null, message = '成功') => ({
  success: true,
  code: 200,
  message,
  data
})

export const notificationApi = {
  /**
   * 获取通知列表
   * GET /notifications/user/{userId}
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.type - 类型(system/order/chat/activity/all)
   * @param {string} params.status - 状态(read/unread/all)
   * @returns {Promise} 返回通知列表
   */
  getNotifications: (params = {}) => {
    const resolved = ensureUserId(params)
    return get(buildUrl(NOTIFICATION_API.GET_NOTIFICATIONS, { userId: resolved.userId }))
  },

  /**
   * 获取通知列表（别名）
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回通知列表
   */
  getList: (params = {}) => {
    const resolved = ensureUserId(params)
    return get(buildUrl(NOTIFICATION_API.GET_LIST, { userId: resolved.userId }))
  },

  /**
   * 获取未读通知数量
   * GET /notifications/unread-count
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回未读数量
   */
  getUnreadCount: (params = {}) => get('/notifications/unread-count', ensureUserId(params)),

  /**
   * 获取通知详情
   * GET /notifications/{notificationId}
   * @param {string} id - 通知ID
   * @returns {Promise} 返回通知详情
   */
  getNotification: (id) => get(buildUrl(NOTIFICATION_API.GET_NOTIFICATION, { notificationId: id })),

  /**
   * 获取通知详情（别名）
   * @param {string} id - 通知ID
   * @returns {Promise} 返回通知详情
   */
  getDetail: (id) => get(buildUrl(NOTIFICATION_API.GET_NOTIFICATION, { notificationId: id })),

  /**
   * 标记为已读
   * PUT /notifications/{notificationId}/read
   * @param {string} id - 通知ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @returns {Promise} 返回标记结果
   */
  markAsRead: (id) => put(buildUrl(NOTIFICATION_API.MARK_READ, { notificationId: id })),

  /**
   * 批量标记为已读
   * 前端批量标记已读，后端逐条执行
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {Array} data.ids - 通知ID数组
   * @returns {Promise} 返回标记结果
   */
  batchMarkAsRead: async (data = {}) => {
    const ids = data.ids || []
    await Promise.all(ids.map((id) => notificationApi.markAsRead(id)))
    return successResponse(true, '已批量标记为已读')
  },

  /**
   * 标记全部为已读
   * PUT /notifications/all-read
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @returns {Promise} 返回标记结果
   */
  markAllAsRead: (data = {}) => put(
    buildQueryUrl(NOTIFICATION_API.MARK_ALL_READ, ensureUserId(data))
  ),

  /**
   * 删除通知
   * DELETE /notifications/{notificationId}
   * @param {string} id - 通知ID
   * @param {Object} params - 参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回删除结果
   */
  delete: (id) => del(buildUrl(NOTIFICATION_API.DELETE_NOTIFICATION, { notificationId: id })),

  /**
   * 批量删除通知
   * DELETE /notifications/batch
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {Array} data.ids - 通知ID数组
   * @returns {Promise} 返回删除结果
   */
  batchDelete: (data = {}) => del('/notifications/batch', data.ids || []),

  /**
   * 清空所有通知
   * 前端清空通知，后端通过批量删除实现
   * @param {Object} params - 参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回清空结果
   */
  clear: async (params = {}) => {
    const listRes = await notificationApi.getList(params)
    const notifications = listRes?.data || listRes || []
    const ids = Array.isArray(notifications) ? notifications.map(item => item.id).filter(Boolean) : []

    if (ids.length === 0) {
      return successResponse(true, '没有可清空的通知')
    }

    return notificationApi.batchDelete({ ids })
  },

  /**
   * 获取系统通知
   * GET /v1/notifications/system
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回系统通知列表
   */
  getSystemNotifications: async (params = {}) => {
    const res = await notificationApi.getList(params)
    const notifications = res?.data || res || []
    const data = Array.isArray(notifications)
      ? notifications.filter(item => item.type === 'system')
      : []
    return successResponse(data)
  },

  /**
   * 获取活动通知
   * GET /v1/notifications/activity
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回活动通知列表
   */
  getActivityNotifications: async (params = {}) => {
    const res = await notificationApi.getList(params)
    const notifications = res?.data || res || []
    const data = Array.isArray(notifications)
      ? notifications.filter(item => item.type === 'activity')
      : []
    return successResponse(data)
  },

  /**
   * 订阅推送通知
   * POST /v1/notifications/subscribe
   * @param {Object} data - 订阅数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.deviceToken - 设备Token
   * @param {string} data.platform - 平台(iOS/Android/Web)
   * @returns {Promise} 返回订阅结果
   */
  subscribe: (data) => Promise.resolve(successResponse(data, '当前环境未接入推送订阅')),

  /**
   * 取消订阅推送通知
   * POST /v1/notifications/unsubscribe
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.deviceToken - 设备Token
   * @returns {Promise} 返回取消订阅结果
   */
  unsubscribe: (data) => Promise.resolve(successResponse(data, '当前环境未接入推送订阅')),

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
  setPreferences: (data = {}) => {
    const resolved = ensureUserId(data)
    uni.setStorageSync(localPreferenceKey(resolved.userId), resolved)
    return Promise.resolve(successResponse(resolved, '通知偏好已保存'))
  },

  /**
   * 获取通知偏好设置
   * GET /v1/notifications/preferences
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回偏好设置
   */
  getPreferences: (params = {}) => {
    const resolved = ensureUserId(params)
    const localValue = uni.getStorageSync(localPreferenceKey(resolved.userId))
    return Promise.resolve(successResponse(localValue || {
      userId: resolved.userId,
      orderNotify: true,
      chatNotify: true,
      activityNotify: true,
      systemNotify: true
    }))
  }
}

export default notificationApi
