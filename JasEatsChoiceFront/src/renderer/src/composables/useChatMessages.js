/**
 * 聊天消息管理
 */
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api.js'
import { MESSAGE_CONFIG, STORAGE_CONFIG } from '@/constants/chatConstants'

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
   * 获取回复消息的显示名称
   */
  const getReplyDisplayName = (replyFromId) => {
    if (!replyFromId) return '未知'

    if (replyFromId === userId.value.toString()) {
      return '我'
    }

    // 返回会话名称（对于单聊是对手的名字，对于群聊是群名称）
    return selectedConversation.value?.name || replyFromId
  }

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

    // 获取时间部分
    const timeStr = date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })

    // 今天
    if (diffDays === 0) {
      if (diffMins < 1) return '刚刚'
      if (diffMins < 60) return `${diffMins}分钟前`
      if (diffHours < 24) {
        // 今天的消息：只显示时间
        return timeStr
      }
    }
    // 昨天
    else if (diffDays === 1) {
      return `昨天 ${timeStr}`
    }
    // 更早但在一周内
    else if (diffDays < 7) {
      return `${diffDays}天前 ${timeStr}`
    }

    // 超过一周显示完整日期和时间
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

        // 确定发送者显示名称
        let senderName = null
        const fromId = msg.fromId || msg.sender || '未知'

        if (fromId === userId.value.toString()) {
          // 自己的消息，不需要显示名称（在UI中会显示"我"）
          senderName = null
        } else if (msg.senderName) {
          // 后端返回的发送者名称
          senderName = msg.senderName
        } else if (selectedConversation.value?.type === 'single') {
          // 单聊：使用会话名称
          senderName = selectedConversation.value.name
        } else if (selectedConversation.value?.type === 'group') {
          // 群聊：尝试使用后端返回的名称，如果没有则显示ID
          senderName = msg.username || msg.nickname || fromId
        }

        const processedMsg = {
          ...msg,
          formattedTime: formatMessageTime(msg.createTime || msg.time),
          fromId,
          senderName
        }

        // 如果有回复信息但没有回复者名称，则生成显示名称
        if (msg.replyTo && !msg.replyFromName) {
          processedMsg.replyFromName = getReplyDisplayName(msg.replyFromId)
        }

        uniqueMessages.push(processedMsg)
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

    // 先显示缓存（快速响应用户体验）
    if (!loadMore && chatHistory.value[sessionId]) {
      chatMessages.value = chatHistory.value[sessionId]
      scrollToBottom()
      console.log(`💾 [Chat] 已加载缓存消息 - sessionId: ${sessionId}, 消息数: ${chatMessages.value.length}条`)
    }

    if (isLoadingMessages.value || !hasMoreMessages.value) {
      console.log(`⏸️ [Chat] 跳过加载 - isLoading: ${isLoadingMessages.value}, hasMore: ${hasMoreMessages.value}`)
      return
    }

    isLoadingMessages.value = true
    console.log(`🔄 [Chat] 开始加载会话消息 - sessionId: ${sessionId}, pageNum: ${msgPageNum.value}, loadMore: ${loadMore}`)

    try {
      const response = await api.get(`/v1/chat/${sessionId}/messages`, {
        params: {
          pageNum: msgPageNum.value,
          pageSize: MESSAGE_CONFIG.DEFAULT_PAGE_SIZE,
          userId: userId.value
        }
      })

      console.log(`📡 [Chat] API响应 -`, {
        code: response.code,
        total: response.data?.total,
        recordsCount: response.data?.records?.length,
        pageNum: msgPageNum.value
      })

      if (response.code === '200') {
        const data = response.data
        const messages = data.records || []
        const processedMessages = preprocessMessages(messages)

        console.log(`✅ [Chat] 消息处理完成 - 原始: ${messages.length}条, 去重后: ${processedMessages.length}条`)

        if (loadMore) {
          const previousCount = chatMessages.value.length
          chatMessages.value = [...processedMessages, ...chatMessages.value]
          const scrollTop = messagesContainerRef.value?.scrollTop || 0
          nextTick(() => {
            if (messagesContainerRef.value) {
              messagesContainerRef.value.scrollTop = scrollTop + 100
            }
          })
          console.log(`📜 [Chat] 加载更多消息 - 之前: ${previousCount}条, 新增: ${processedMessages.length}条, 总计: ${chatMessages.value.length}条`)
        } else {
          // 用后端数据覆盖缓存数据（保证数据准确性）
          chatHistory.value[sessionId] = processedMessages
          chatMessages.value = processedMessages
          scrollToBottom()
          console.log(`📝 [Chat] 从服务器加载消息 - 共: ${processedMessages.length}条, 已更新缓存和显示`)
        }

        totalMessages.value = data.total || 0
        hasMoreMessages.value =
          data.records && data.records.length >= MESSAGE_CONFIG.DEFAULT_PAGE_SIZE

        console.log(`📊 [Chat] 分页信息 - 总消息数: ${totalMessages.value}, 是否有更多: ${hasMoreMessages.value}`)

        saveChatHistoryToLocal()
      }
    } catch (error) {
      console.error('❌ [Chat] 加载聊天记录失败 -', {
        sessionId,
        error: error.message,
        stack: error.stack
      })
      ElMessage.error('加载聊天记录失败，请稍后重试')
      if (!loadMore) {
        chatMessages.value = []
      }
    } finally {
      isLoadingMessages.value = false
      console.log(`✅ [Chat] 加载完成 - isLoading: ${isLoadingMessages.value}`)
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
      const fromId = message.fromId || message.sender || '未知'

      // 确定发送者显示名称（如果消息中没有）
      let senderName = message.senderName
      if (!senderName && fromId !== userId.value.toString()) {
        if (selectedConversation.value?.type === 'single') {
          senderName = selectedConversation.value.name
        } else if (selectedConversation.value?.type === 'group') {
          senderName = message.username || message.nickname || fromId
        }
      }

      const processedMsg = {
        ...message,
        formattedTime: formatMessageTime(message.createTime || message.time),
        fromId,
        senderName
      }

      // 如果有回复信息但没有回复者名称，则生成显示名称
      if (message.replyTo && !message.replyFromName) {
        processedMsg.replyFromName = getReplyDisplayName(message.replyFromId)
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
