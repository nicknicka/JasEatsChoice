/**
 * 消息相关API
 */
import request from '@/utils/request'

/**
 * 获取消息列表
 */
export const getMessageList = (params) => {
  return request({
    url: '/api/message/list',
    method: 'GET',
    params
  })
}

/**
 * 获取消息详情
 */
export const getMessageDetail = (messageId) => {
  return request({
    url: `/api/message/${messageId}`,
    method: 'GET'
  })
}

/**
 * 标记消息为已读
 */
export const markMessageRead = (messageId) => {
  return request({
    url: `/api/message/${messageId}/read`,
    method: 'POST'
  })
}

/**
 * 全部标记为已读
 */
export const markAllRead = () => {
  return request({
    url: '/api/message/read-all',
    method: 'POST'
  })
}

/**
 * 删除消息
 */
export const deleteMessage = (messageId) => {
  return request({
    url: `/api/message/${messageId}`,
    method: 'DELETE'
  })
}

/**
 * 删除已读消息
 */
export const deleteReadMessages = () => {
  return request({
    url: '/api/message/delete-read',
    method: 'POST'
  })
}

/**
 * 获取未读消息数
 */
export const getUnreadCount = () => {
  return request({
    url: '/api/message/unread-count',
    method: 'GET'
  })
}

/**
 * 获取系统通知
 */
export const getSystemNotifications = (params) => {
  return request({
    url: '/api/message/system',
    method: 'GET',
    params
  })
}

export default {
  getMessageList,
  getMessageDetail,
  markMessageRead,
  markAllRead,
  deleteMessage,
  deleteReadMessages,
  getUnreadCount,
  getSystemNotifications
}
