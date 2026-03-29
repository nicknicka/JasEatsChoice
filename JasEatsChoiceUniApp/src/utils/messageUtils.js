/**
 * 消息工具类
 */
import { MessageType, MessageStatus, NotificationType } from '@/enums/message'

/**
 * 消息工具类
 */
export class MessageUtils {
  /**
   * 获取消息图标
   */
  static getMessageIcon(type) {
    const iconMap = {
      [MessageType.TEXT]: 'chatbubble',
      [MessageType.IMAGE]: 'image',
      [MessageType.VOICE]: 'mic',
      [MessageType.VIDEO]: 'videocam',
      [MessageType.LOCATION]: 'location',
      [MessageType.DISH]: 'shop',
      [MessageType.ORDER]: 'list',
      [MessageType.RECIPE]: 'book',
      [MessageType.SYSTEM]: 'sound',
      [MessageType.NOTIFICATION]: 'notification',
      [MessageType.CUSTOM]: 'help'
    }
    return iconMap[type] || 'help'
  }

  /**
   * 获取消息图标 emoji
   */
  static.getMessageIconEmoji(type) {
    const emojiMap = {
      [MessageType.TEXT]: '💬',
      [MessageType.IMAGE]: '🖼️',
      [MessageType.VOICE]: '🎤',
      [MessageType.VIDEO]: '📹',
      [MessageType.LOCATION]: '📍',
      [MessageType.DISH]: '🍽️',
      [MessageType.ORDER]: '📦',
      [MessageType.RECIPE]: '📖',
      [MessageType.SYSTEM]: '📢',
      [MessageType.NOTIFICATION]: '🔔',
      [MessageType.CUSTOM]: '📄'
    }
    return emojiMap[type] || '📄'
  }

  /**
   * 获取通知图标 emoji
   */
  static getNotificationIconEmoji(type) {
    const emojiMap = {
      [NotificationType.SYSTEM]: '📢',
      [NotificationType.ORDER]: '📦',
      [NotificationType.ACTIVITY]: '🎉',
      [NotificationType.CHAT]: '💬',
      [NotificationType.PROMOTION]: '🎁'
    }
    return emojiMap[type] || '📄'
  }

  /**
   * 获取通知颜色
   */
  static getNotificationColor(type) {
    const colorMap = {
      [NotificationType.SYSTEM]: '#FF9800',
      [NotificationType.ORDER]: '#2196F3',
      [NotificationType.ACTIVITY]: '#FF6B35',
      [NotificationType.CHAT]: '#52C41A',
      [NotificationType.PROMOTION]: '#9C27B0'
    }
    return colorMap[type] || '#999'
  }

  /**
   * 获取通知渐变背景
   */
  static getNotificationGradient(type) {
    const gradientMap = {
      [NotificationType.SYSTEM]: 'linear-gradient(135deg, #FFB74D, #FF9800)',
      [NotificationType.ORDER]: 'linear-gradient(135deg, #64B5F6, #2196F3)',
      [NotificationType.ACTIVITY]: 'linear-gradient(135deg, #FF6B35, #FF8F61)',
      [NotificationType.CHAT]: 'linear-gradient(135deg, #81C784, #4CAF50)',
      [NotificationType.PROMOTION]: 'linear-gradient(135deg, #BA68C8, #9C27B0)'
    }
    return gradientMap[type] || 'linear-gradient(135deg, #999, #666)'
  }

  /**
   * 格式化消息时间
   */
  static formatMessageTime(timestamp) {
    if (!timestamp) return ''

    const now = new Date()
    const target = new Date(timestamp)
    const diff = now - target

    // 小于1分钟
    if (diff < 60000) {
      return '刚刚'
    }

    // 小于1小时
    if (diff < 3600000) {
      return `${Math.floor(diff / 60000)}分钟前`
    }

    // 小于24小时
    if (diff < 86400000) {
      return `${Math.floor(diff / 3600000)}小时前`
    }

    // 小于7天
    if (diff < 604800000) {
      return `${Math.floor(diff / 86400000)}天前`
    }

    // 超过7天显示日期
    const month = target.getMonth() + 1
    const date = target.getDate()
    return `${month}月${date}日`
  }

  /**
   * 格式化聊天时间（显示时分）
   */
  static formatChatTime(timestamp) {
    if (!timestamp) return ''

    const now = new Date()
    const target = new Date(timestamp)
    const diff = now - target

    // 小于1分钟
    if (diff < 60000) {
      return '刚刚'
    }

    // 小于1小时
    if (diff < 3600000) {
      return `${Math.floor(diff / 60000)}分钟前`
    }

    // 今天
    if (target.toDateString() === now.toDateString()) {
      const hour = target.getHours().toString().padStart(2, '0')
      const minute = target.getMinutes().toString().padStart(2, '0')
      return `${hour}:${minute}`
    }

    // 昨天
    const yesterday = new Date(now)
    yesterday.setDate(yesterday.getDate() - 1)
    if (target.toDateString() === yesterday.toDateString()) {
      const hour = target.getHours().toString().padStart(2, '0')
      const minute = target.getMinutes().toString().padStart(2, '0')
      return `昨天 ${hour}:${minute}`
    }

    // 本周
    if (diff < 604800000) {
      const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      return weekdays[target.getDay()]
    }

    // 更早显示日期
    const month = target.getMonth() + 1
    const date = target.getDate()
    return `${month}/${date}`
  }

  /**
   * 截断消息文本
   */
  static truncateMessage(text, maxLength = 50) {
    if (!text) return ''
    if (text.length <= maxLength) return text
    return text.substring(0, maxLength) + '...'
  }

  /**
   * 检查消息是否包含关键词
   */
  static containsKeyword(message, keyword) {
    if (!message || !keyword) return false
    const lowerMessage = message.toLowerCase()
    const lowerKeyword = keyword.toLowerCase()
    return lowerMessage.includes(lowerKeyword)
  }

  /**
   * 高亮关键词
   */
  static highlightKeyword(text, keyword) {
    if (!text || !keyword) return text

    const regex = new RegExp(`(${keyword})`, 'gi')
    return text.replace(regex, '<mark>$1</mark>')
  }

  /**
   * 获取消息状态文本
   */
  static getMessageStatusText(status) {
    const statusMap = {
      [MessageStatus.SENDING]: '发送中...',
      [MessageStatus.SENT]: '已发送',
      [MessageStatus.DELIVERED]: '已送达',
      [MessageStatus.READ]: '已读',
      [MessageStatus.FAILED]: '发送失败'
    }
    return statusMap[status] || ''
  }

  /**
   * 计算未读数量
   */
  static calculateUnreadCount(conversations) {
    if (!conversations || !Array.isArray(conversations)) return 0
    return conversations.reduce((sum, conv) => sum + (conv.unread || 0), 0)
  }

  /**
   * 排序会话列表
   */
  static sortConversations(conversations) {
    if (!conversations || !Array.isArray(conversations)) return []

    return [...conversations].sort((a, b) => {
      // 置顶的在前
      if (a.isPinned && !b.isPinned) return -1
      if (!a.isPinned && b.isPinned) return 1

      // 按时间排序
      const timeA = new Date(a.lastTime || 0).getTime()
      const timeB = new Date(b.lastTime || 0).getTime()
      return timeB - timeA
    })
  }

  /**
   * 过滤会话列表
   */
  static filterConversations(conversations, filter) {
    if (!conversations || !Array.isArray(conversations)) return []

    switch (filter) {
      case 'unread':
        return conversations.filter(conv => conv.unread > 0)
      case 'pinned':
        return conversations.filter(conv => conv.isPinned)
      case 'group':
        return conversations.filter(conv => conv.isGroup)
      default:
        return conversations
    }
  }

  /**
   * 搜索会话
   */
  static searchConversations(conversations, keyword) {
    if (!conversations || !Array.isArray(conversations) || !keyword) return []

    const lowerKeyword = keyword.toLowerCase()
    return conversations.filter(conv => {
      return conv.name?.toLowerCase().includes(lowerKeyword) ||
             conv.lastMessage?.toLowerCase().includes(lowerKeyword)
    })
  }

  /**
   * 生成唯一ID
   */
  static generateId() {
    return `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  /**
   * 验证消息内容
   */
  static validateMessage(content) {
    if (!content || typeof content !== 'string') {
      return { valid: false, message: '消息内容不能为空' }
    }

    if (content.trim().length === 0) {
      return { valid: false, message: '消息内容不能为空' }
    }

    if (content.length > 500) {
      return { valid: false, message: '消息内容不能超过500字' }
    }

    return { valid: true }
  }

  /**
   * 清理消息内容
   */
  static sanitizeMessage(content) {
    if (!content || typeof content !== 'string') return ''

    // 移除HTML标签
    let cleaned = content.replace(/<[^>]*>/g, '')

    // 移除多余空格
    cleaned = cleaned.replace(/\s+/g, ' ').trim()

    return cleaned
  }

  /**
   * 防抖函数
   */
  static debounce(func, delay = 300) {
    let timer = null
    return function(...args) {
      if (timer) clearTimeout(timer)
      timer = setTimeout(() => {
        func.apply(this, args)
      }, delay)
    }
  }

  /**
   * 节流函数
   */
  static throttle(func, delay = 300) {
    let timer = null
    return function(...args) {
      if (!timer) {
        timer = setTimeout(() => {
          func.apply(this, args)
          timer = null
        }, delay)
      }
    }
  }
}

/**
 * 会话工具类
 */
export class ConversationUtils {
  /**
   * 获取会话名称
   */
  static getConversationName(conversation) {
    if (conversation.isGroup) {
      return conversation.name || '群聊'
    }
    return conversation.name || '未知用户'
  }

  /**
   * 获取会话头像
   */
  static getConversationAvatar(conversation) {
    return conversation.avatar || '/static/default-avatar.png'
  }

  /**
   * 检查是否在线
   */
  static isOnline(conversation) {
    return conversation.isOnline || false
  }

  /**
   * 获取最后一条消息预览
   */
  static getLastMessagePreview(conversation, maxLength = 30) {
    if (!conversation.lastMessage) return ''
    return MessageUtils.truncateMessage(conversation.lastMessage, maxLength)
  }
}

export default MessageUtils
