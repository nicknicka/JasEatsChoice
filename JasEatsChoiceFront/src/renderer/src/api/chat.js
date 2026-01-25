// 聊天相关API
import api from '../utils/api'

export default {
  /**
   * 上传图片
   * @param {File} file - 图片文件
   * @returns {Promise} 上传结果
   */
  uploadChatImage(file) {
    const formData = new FormData()
    formData.append('file', file)

    return api.post('/v1/chat/upload-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 上传文件
   * @param {File} file - 文件
   * @returns {Promise} 上传结果
   */
  uploadChatFile(file) {
    const formData = new FormData()
    formData.append('file', file)

    return api.post('/v1/chat/upload-file', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 发送文本消息
   * @param {Object} message - 消息对象
   * @returns {Promise} 发送结果
   */
  sendTextMessage(message) {
    return api.post('/v1/chat/messages', {
      ...message,
      msgType: message.msgType || 'text'
    })
  },

  /**
   * 发送图片消息
   * @param {Object} message - 消息对象（包含fileUrl等信息）
   * @returns {Promise} 发送结果
   */
  sendImageMessage(message) {
    return api.post('/v1/chat/messages', {
      ...message,
      msgType: 'image',
      content: '[图片]'
    })
  },

  /**
   * 发送文件消息
   * @param {Object} message - 消息对象（包含fileUrl, fileName等信息）
   * @returns {Promise} 发送结果
   */
  sendFileMessage(message) {
    return api.post('/v1/chat/messages', {
      ...message,
      msgType: 'file',
      content: `[文件] ${message.fileName}`
    })
  },

  /**
   * 获取聊天记录
   * @param {String} sessionId - 会话ID
   * @param {Number} page - 页码
   * @param {Number} size - 每页数量
   * @returns {Promise} 聊天记录
   */
  getChatMessages(sessionId, page = 1, size = 20) {
    return api.get(`/v1/chat/${sessionId}/messages`, {
      params: { page, size }
    })
  },

  /**
   * 标记消息已读
   * @param {String} messageId - 消息ID
   * @returns {Promise} 标记结果
   */
  markMessageAsRead(messageId) {
    return api.put(`/v1/chat/messages/${messageId}/read`)
  },

  /**
   * 撤回消息
   * @param {String} messageId - 消息ID
   * @param {String} userId - 用户ID
   * @returns {Promise} 撤回结果
   */
  recallMessage(messageId, userId) {
    return api.post(`/v1/chat/messages/${messageId}/recall`, { userId })
  }
}
