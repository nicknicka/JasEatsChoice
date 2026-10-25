/**
 * 聊天消息工具函数
 */

/**
 * XSS防护 - 转义HTML特殊字符
 * @param {string} content - 消息内容
 * @returns {string} 转义后的内容
 */
export const sanitizeMessage = (content) => {
  if (!content || typeof content !== 'string') return ''

  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#x27;',
    '/': '&#x2F;'
  }

  return content.replace(/[&<>"'/]/g, (char) => map[char])
}

/**
 * 过滤敏感信息（手机号、身份证等）
 * @param {string} content - 消息内容
 * @returns {string} 过滤后的内容
 */
export const filterSensitiveInfo = (content) => {
  if (!content || typeof content !== 'string') return ''

  // 过滤手机号
  content = content.replace(/(\d{3})\d{4}(\d{4})/g, '$1****$2')

  // 过滤身份证号
  content = content.replace(/(\d{6})\d{8}(\d{4})/g, '$1********$2')

  // 过滤银行卡号
  content = content.replace(/(\d{4})\d{8,12}(\d{4})/g, '$1********$2')

  return content
}

/**
 * 格式化消息时间
 * @param {string|Date} time - 时间
 * @returns {string} 格式化后的时间
 */
export const formatMessageTime = (time) => {
  if (!time) return ''

  const date = typeof time === 'string' ? new Date(time) : time
  if (isNaN(date.getTime())) return ''

  const now = new Date()
  const diff = now - date

  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }

  // 小于1小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }

  // 今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }

  // 本周
  const weekAgo = new Date(now)
  weekAgo.setDate(weekAgo.getDate() - 7)
  if (date > weekAgo) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const weekday = weekdays[date.getDay()]
    return `${weekday} ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }

  // 本年
  if (date.getFullYear() === now.getFullYear()) {
    return date.toLocaleDateString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  // 更早
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 格式化会话列表时间
 * @param {string|Date} time - 时间
 * @returns {string} 格式化后的时间
 */
export const formatConversationTime = (time) => {
  if (!time) return ''

  const date = typeof time === 'string' ? new Date(time) : time
  if (isNaN(date.getTime())) return ''

  const now = new Date()
  const diff = now - date

  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }

  // 小于1小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }

  // 今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }

  // 本周
  const weekAgo = new Date(now)
  weekAgo.setDate(weekAgo.getDate() - 7)
  if (date > weekAgo) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return weekdays[date.getDay()]
  }

  // 本年
  if (date.getFullYear() === now.getFullYear()) {
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  }

  // 更早
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

/**
 * 截断消息内容
 * @param {string} content - 消息内容
 * @param {number} maxLength - 最大长度
 * @returns {string} 截断后的内容
 */
export const truncateMessage = (content, maxLength = 50) => {
  if (!content || typeof content !== 'string') return ''

  if (content.length <= maxLength) return content

  return content.substring(0, maxLength) + '...'
}

/**
 * 清理消息内容（XSS防护 + 敏感信息过滤）
 * @param {string} content - 消息内容
 * @returns {string} 清理后的内容
 */
export const cleanMessage = (content) => {
  if (!content || typeof content !== 'string') return ''

  // 先过滤敏感信息
  let cleaned = filterSensitiveInfo(content)

  // 再进行XSS防护
  cleaned = sanitizeMessage(cleaned)

  return cleaned
}

/**
 * 生成唯一消息ID
 * @returns {string} 消息ID
 */
export const generateMessageId = () => {
  return `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

/**
 * 判断消息是否为图片消息
 * @param {object} message - 消息对象
 * @returns {boolean} 是否为图片消息
 */
export const isImageMessage = (message) => {
  return (
    message.messageType === 'image' ||
    (message.content && message.content.match(/^https?:.*\.(jpg|jpeg|png|gif|webp)$/i))
  )
}

/**
 * 判断消息是否为文件消息
 * @param {object} message - 消息对象
 * @returns {boolean} 是否为文件消息
 */
export const isFileMessage = (message) => {
  return message.messageType === 'file'
}

/**
 * 计算未读消息数量
 * @param {Array} conversations - 会话列表
 * @returns {number} 未读消息总数
 */
export const calculateUnreadCount = (conversations) => {
  if (!Array.isArray(conversations)) return 0

  return conversations.reduce((total, conv) => total + (conv.unreadCount || 0), 0)
}

/**
 * 按未读消息排序会话列表
 * @param {Array} conversations - 会话列表
 * @returns {Array} 排序后的会话列表
 */
export const sortConversationsByUnread = (conversations) => {
  if (!Array.isArray(conversations)) return []

  return [...conversations].sort((a, b) => {
    // 有未读消息的排在前面
    if (a.unreadCount > 0 && b.unreadCount === 0) return -1
    if (a.unreadCount === 0 && b.unreadCount > 0) return 1

    // 都有未读消息或都没有，按最后消息时间排序
    const timeA = new Date(a.time || 0).getTime()
    const timeB = new Date(b.time || 0).getTime()
    return timeB - timeA
  })
}
