/**
 * 聊天消息管理
 */
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api.js'
import { MESSAGE_CONFIG, STORAGE_CONFIG, MESSAGE_STATUS } from '@/constants/chatConstants'

export function useChatMessages({ userId, selectedConversation }) {
  // 聊天记录
  const chatHistory = ref({})
  const chatMessages = ref([])

  // 分页相关
  const msgPageNum = ref(1)
  const totalMessages = ref(0)
  const hasMoreMessages = ref(true)
  const isLoadingMessages = ref(false)

  // 消息容器引用
  const messagesContainerRef = ref(null)

  /**
   * 格式化消息时间
   */
  const formatMessageTime = (time) => {
    if (!time) return ''

    const date = new Date(time)
    const now = new Date()
    const diffMs = now - date
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMs / 3600000)
    const diffDays = Math.floor(diffMs / 86400000)

    // 今天
    if (diffDays === 0) {
      if (diffMins < 1) return '刚刚'
      if (diffMins < 60) return `${diffMins}分钟前`
      if (diffHours < 24)
        return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
    // 昨天
    else if (diffDays === 1) {
      return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
    }
    // 更早
    else if (diffDays < 7) {
      return `${diffDays}天前`
    }

    // 超过一周显示完整日期
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  /**
   * 预处理消息
   */
  const preprocessMessages = (messages) => {
    if (!Array.isArray(messages)) return []

    const uniqueMessages = []
    const messageIds = new Set()

    messages.forEach((msg) => {
      if (!messageIds.has(msg.id)) {
        messageIds.add(msg.id)
        uniqueMessages.push({
          ...msg,
          formattedTime: formatMessageTime(msg.createTime || msg.time),
          fromId: msg.fromId || msg.sender || '未知'
        })
      }
    })

    return uniqueMessages
  }

  /**
   * 滚动到底部
   */
  const scrollToBottom = () => {
    nextTick(() => {
      if (messagesContainerRef.value) {
        messagesContainerRef.value.scrollTop = messagesContainerRef.value.scrollHeight
      }
    })
  }

  /**
   * 保存聊天历史到本地
   */
  const saveChatHistoryToLocal = () => {
    try {
      const storageDays =
        parseInt(localStorage.getItem(STORAGE_CONFIG.CHAT_STORAGE_DAYS_KEY)) ||
        STORAGE_CONFIG.DEFAULT_STORAGE_DAYS
      const maxMessagesPerSession =
        parseInt(localStorage.getItem(STORAGE_CONFIG.CHAT_MAX_MESSAGES_KEY)) ||
        STORAGE_CONFIG.MAX_MESSAGES_PER_SESSION

      const cutoffTime = Date.now() - storageDays * 24 * 60 * 60 * 1000
      const filteredHistory = {}

      Object.keys(chatHistory.value).forEach((sessionId) => {
        const recentMessages = chatHistory.value[sessionId].filter((msg) => {
          const msgTime = new Date(msg.createTime || msg.time).getTime()
          return msgTime > cutoffTime
        })

        if (recentMessages.length > 0) {
          filteredHistory[sessionId] = recentMessages.slice(-maxMessagesPerSession)
        }
      })

      localStorage.setItem(STORAGE_CONFIG.CHAT_HISTORY_KEY, JSON.stringify(filteredHistory))
    } catch (error) {
      console.error('保存聊天历史失败:', error)
    }
  }

  /**
   * 从本地加载聊天历史
   */
  const loadChatHistoryFromLocal = () => {
    try {
      const saved = localStorage.getItem(STORAGE_CONFIG.CHAT_HISTORY_KEY)
      if (saved) {
        const parsedHistory = JSON.parse(saved)
        Object.keys(parsedHistory).forEach((sessionId) => {
          if (!chatHistory.value[sessionId]) {
            chatHistory.value[sessionId] = parsedHistory[sessionId]
          }
        })
      }
    } catch (error) {
      console.error('加载聊天历史失败:', error)
    }
  }

  /**
   * 加载聊天记录
   */
  const loadChatMessages = async (sessionId, loadMore = false) => {
    if (!loadMore) {
      msgPageNum.value = 1
      hasMoreMessages.value = true
    }

    // 使用缓存
    if (!loadMore && chatHistory.value[sessionId]) {
      chatMessages.value = chatHistory.value[sessionId]
      scrollToBottom()
      return
    }

    if (isLoadingMessages.value || !hasMoreMessages.value) {
      return
    }

    isLoadingMessages.value = true

    try {
      const response = await api.get(`/v1/chat/${sessionId}/messages`, {
        params: {
          pageNum: msgPageNum.value,
          pageSize: MESSAGE_CONFIG.DEFAULT_PAGE_SIZE,
          userId: userId.value
        }
      })

      if (response.code === '200') {
        const data = response.data
        const messages = data.records || []
        const processedMessages = preprocessMessages(messages)

        if (loadMore) {
          chatMessages.value = [...processedMessages, ...chatMessages.value]
          const scrollTop = messagesContainerRef.value?.scrollTop || 0
          nextTick(() => {
            if (messagesContainerRef.value) {
              messagesContainerRef.value.scrollTop = scrollTop + 100
            }
          })
        } else {
          chatHistory.value[sessionId] = processedMessages
          chatMessages.value = processedMessages
          scrollToBottom()
        }

        totalMessages.value = data.total || 0
        hasMoreMessages.value =
          data.records && data.records.length >= MESSAGE_CONFIG.DEFAULT_PAGE_SIZE

        saveChatHistoryToLocal()
      }
    } catch (error) {
      console.error('加载聊天记录失败:', error)
      ElMessage.error('加载聊天记录失败，请稍后重试')
      if (!loadMore) {
        chatMessages.value = []
      }
    } finally {
      isLoadingMessages.value = false
    }
  }

  /**
   * 加载更多消息
   */
  const loadMoreMessages = async () => {
    if (!selectedConversation.value || isLoadingMessages.value || !hasMoreMessages.value) {
      return
    }
    msgPageNum.value++
    await loadChatMessages(selectedConversation.value.id, true)
  }

  /**
   * 添加新消息
   */
  const addMessage = (message, sessionId) => {
    const exists = chatMessages.value.some((msg) => msg.id === message.id)
    if (!exists) {
      const processedMsg = {
        ...message,
        formattedTime: formatMessageTime(message.createTime || message.time),
        fromId: message.fromId || message.sender || '未知'
      }
      chatMessages.value.push(processedMsg)
      chatHistory.value[sessionId] = chatMessages.value
      scrollToBottom()
      saveChatHistoryToLocal()
    }
  }

  return {
    chatHistory,
    chatMessages,
    msgPageNum,
    totalMessages,
    hasMoreMessages,
    isLoadingMessages,
    messagesContainerRef,
    formatMessageTime,
    loadChatMessages,
    loadMoreMessages,
    addMessage,
    saveChatHistoryToLocal,
    loadChatHistoryFromLocal,
    scrollToBottom
  }
}
