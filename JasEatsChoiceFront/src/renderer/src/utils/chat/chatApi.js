/**
 * 聊天API工具函数
 */
import api, { decodeJwt } from '../api'
import { ElMessage } from 'element-plus'
import router from '../../router'

/**
 * 获取当前用户ID
 * @returns {string|number} 用户ID
 */
export const getCurrentUserId = () => {
  const authStore = require('../../store/authStore').useAuthStore()
  const token = authStore.token

  if (token) {
    const decodedToken = decodeJwt(token)
    if (decodedToken && decodedToken.userId) {
      return decodedToken.userId
    }
  }

  return '1' // 默认值
}

/**
 * 处理认证错误
 * @param {Error} error - 错误对象
 */
export const handleAuthError = (error) => {
  if (error.response?.status === 401) {
    ElMessage.error('登录已过期，请重新登录')

    // 清除认证信息
    const authStore = require('../../store/authStore').useAuthStore()
    authStore.clearAuth()

    // 跳转到登录页
    router.push('/login')
  }
}

/**
 * 处理API错误
 * @param {Error} error - 错误对象
 * @param {string} defaultMessage - 默认错误消息
 * @returns {string} 错误消息
 */
export const handleApiError = (error, defaultMessage = '操作失败，请稍后重试') => {
  // 处理认证错误
  if (error.response?.status === 401) {
    handleAuthError(error)
    return '登录已过期'
  }

  // 获取错误信息
  const errorMessage = error.response?.data?.message || error.message || defaultMessage

  // 显示错误提示
  ElMessage.error(errorMessage)

  return errorMessage
}

/**
 * 获取会话列表
 * @param {string|number} userId - 用户ID
 * @returns {Promise} API响应
 */
export const getChatSessions = async (userId) => {
  try {
    const response = await api.get(`/v1/chat/users/${userId}/chat-sessions`)

    if (response.data && response.data.success) {
      // 转换数据格式
      const sessions = response.data.data.map((session) => {
        const isGroupChat = session.msgType === 'group'

        return {
          id: isGroupChat
            ? session.toId
            : session.fromId === userId
              ? session.toId
              : session.fromId,
          type: isGroupChat ? 'group' : 'private',
          name: isGroupChat
            ? session.toId
            : `用户${session.fromId === userId ? session.toId : session.fromId}`,
          avatar: isGroupChat ? '👥' : '👤',
          lastMessage: session.content,
          time: session.createTime,
          unreadCount: 0,
          memberCount: isGroupChat ? Math.floor(Math.random() * 50) + 10 : undefined,
          userId: isGroupChat
            ? undefined
            : session.fromId === userId
              ? session.toId
              : session.fromId
        }
      })

      // 按时间排序
      sessions.sort((a, b) => {
        return new Date(b.time) - new Date(a.time)
      })

      return sessions
    }

    return []
  } catch (error) {
    console.error('获取会话列表失败:', error)
    handleApiError(error, '获取会话列表失败')
    return []
  }
}

/**
 * 获取聊天记录
 * @param {string} sessionId - 会话ID
 * @param {string|number} userId - 当前用户ID
 * @returns {Promise} API响应
 */
export const getChatMessages = async (sessionId, userId) => {
  try {
    const response = await api.get(`/v1/chat/${sessionId}/messages`)

    if (response.data && response.data.success) {
      // 转换数据格式
      const messages = response.data.data.records.map((message) => ({
        id: message.id,
        sender: message.fromId === userId ? 'merchant' : message.fromId,
        content: message.content,
        time: message.createTime,
        isRead: message.readStatus
      }))

      return messages
    }

    return []
  } catch (error) {
    console.error('获取聊天记录失败:', error)
    handleApiError(error, '获取聊天记录失败')
    return []
  }
}

/**
 * 发送消息
 * @param {object} messageData - 消息数据
 * @returns {Promise} API响应
 */
export const sendMessage = async (messageData) => {
  try {
    const response = await api.post('/api/v1/chat/messages', messageData)

    if (response.data && response.data.success) {
      return {
        success: true,
        data: response.data.data
      }
    }

    return {
      success: false,
      message: '发送失败'
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    handleApiError(error, '发送消息失败')
    return {
      success: false,
      message: error.message
    }
  }
}

/**
 * 标记消息为已读
 * @param {string} sessionId - 会话ID
 * @returns {Promise} API响应
 */
export const markMessagesAsRead = async (sessionId) => {
  try {
    const response = await api.put(`/v1/chat/sessions/${sessionId}/read`)

    return {
      success: response.data?.success || false
    }
  } catch (error) {
    console.error('标记已读失败:', error)
    // 静默失败，不显示错误提示
    return {
      success: false
    }
  }
}

/**
 * 构建会话ID
 * @param {string|number} fromId - 发送者ID
 * @param {string|number} toId - 接收者ID
 * @param {string} type - 会话类型 ('private' | 'group')
 * @returns {string} 会话ID
 */
export const buildSessionId = (fromId, toId, type) => {
  if (type === 'group') {
    return String(toId)
  }

  // 单聊：按字典序排列确保唯一性
  const ids = [String(fromId), String(toId)]
  ids.sort()
  return ids.join('_')
}

/**
 * 格式化消息数据用于发送
 * @param {string} content - 消息内容
 * @param {string|number} fromId - 发送者ID
 * @param {string|number} toId - 接收者ID
 * @param {string} type - 会话类型 ('private' | 'group')
 * @returns {object} 格式化后的消息数据
 */
export const formatMessageForSend = (content, fromId, toId, type) => {
  return {
    fromId: String(fromId),
    toId: String(toId),
    content: content.trim(),
    msgType: type === 'group' ? 'group' : 'private'
  }
}

/**
 * 创建前端消息对象
 * @param {string} content - 消息内容
 * @param {string} type - 会话类型 ('private' | 'group')
 * @returns {object} 消息对象
 */
export const createLocalMessage = (content, type) => {
  return {
    id: Date.now(),
    sender: type === 'private' ? 'merchant' : '我',
    content: content.trim(),
    time: new Date().toISOString().slice(0, 19).replace('T', ' '),
    isRead: true
  }
}
