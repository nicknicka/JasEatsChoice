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

    console.log('🔍 [preprocessMessages] 开始处理消息列表', {
      原始消息数量: messages.length,
      完整数据: messages.map(msg => ({
        id: msg.id || msg.msgId,
        msgType: msg.msgType,
        content: msg.content?.substring(0, 30),
        fileUrl: msg.fileUrl,
        fullUrl: msg.fullUrl,
        fileName: msg.fileName
      }))
    })

    const uniqueMessages = []
    const messageIds = new Set()

    messages.forEach((msg, index) => {
      // ⭐ 修复：使用 msgId 或 id 作为唯一标识
      const messageId = msg.msgId || msg.id
      console.log(`📝 [preprocessMessages] 处理第${index + 1}条消息`, {
        原始msgId: messageId,
        msgType: msg.msgType,
        content: msg.content,
        fileUrl: msg.fileUrl,
        fullUrl: msg.fullUrl,
        fileName: msg.fileName,
        fileType: msg.fileType
      })

      if (!messageIds.has(messageId)) {
        messageIds.add(messageId)

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

        // ⭐ 修复：先确保 fileUrl 和 fullUrl 字段存在，再创建 processedMsg
        // 这样可以确保这些字段被正确复制到新对象中
        let fileUrl = msg.fileUrl || ''
        let fullUrl = msg.fullUrl || ''

        // 如果后端只返回了相对路径 fileUrl，构建完整的 fullUrl
        if (fileUrl && !fullUrl && !fileUrl.startsWith('http')) {
          // 构建完整URL（与上传时的逻辑一致）
          const serverUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
          fullUrl = `${serverUrl}/api/uploads/${fileUrl}`
          console.log('🔗 [preprocessMessages] 构建完整URL:', {
            相对路径: fileUrl,
            完整URL: fullUrl
          })
        }

        // 确保 id 字段存在（优先使用 msgId）
        const normalizedId = messageId

        // 确定消息类型
        let msgType = msg.msgType
        if (msg.msgType === 'image' || msg.content === '[图片]' || fileUrl) {
          msgType = 'image'
        } else if (msg.msgType === 'file' || msg.content?.startsWith('[文件]') || msg.fileName) {
          msgType = 'file'
        }

        const processedMsg = {
          ...msg,
          id: normalizedId,  // ⭐ 标准化 id 字段
          msgType,
          formattedTime: formatMessageTime(msg.createTime || msg.time),
          fromId,
          senderName,
          // 显式确保 URL 字段存在
          fileUrl,
          fullUrl
        }

        // 打印调试信息
        if (msgType === 'image') {
          console.log('📸 [preprocessMessages] 处理图片消息', {
            msgId: normalizedId,
            fileUrl: processedMsg.fileUrl,
            fullUrl: processedMsg.fullUrl,
            最终URL: processedMsg.fullUrl || processedMsg.fileUrl
          })
        } else if (msgType === 'file') {
          console.log('📎 [preprocessMessages] 处理文件消息', {
            msgId: normalizedId,
            fileName: processedMsg.fileName,
            fileUrl: processedMsg.fileUrl
          })
        }

        // 如果有回复信息但没有回复者名称，则生成显示名称
        if (msg.replyTo && !msg.replyFromName) {
          processedMsg.replyFromName = getReplyDisplayName(msg.replyFromId)
        }

        uniqueMessages.push(processedMsg)
      }
    })

    // 🔍 调试：打印所有图片消息的详细信息
    const imageMessages = uniqueMessages.filter(msg => msg.msgType === 'image')
    if (imageMessages.length > 0) {
      console.log('🖼️ [preprocessMessages] 处理后的图片消息列表:', imageMessages.map(msg => ({
        id: msg.id,
        msgType: msg.msgType,
        content: msg.content,
        fileUrl: msg.fileUrl,
        fullUrl: msg.fullUrl,
        fileName: msg.fileName
      })))
    }

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
            // 🔍 调试：检查缓存中的图片消息
            const imageMessages = parsedHistory[sessionId].filter(msg => msg.msgType === 'image')
            if (imageMessages.length > 0) {
              console.log('📦 [缓存] 会话', sessionId, '中的图片消息:', imageMessages.map(msg => ({
                id: msg.id,
                msgType: msg.msgType,
                fileUrl: msg.fileUrl,
                fullUrl: msg.fullUrl
              })))
            }
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
    console.log('🚀 [loadChatMessages] 开始执行', {
      sessionId,
      loadMore,
      currentPage: msgPageNum.value,
      pageSize: MESSAGE_CONFIG.DEFAULT_PAGE_SIZE
    })

    if (!loadMore) {
      msgPageNum.value = 1
      hasMoreMessages.value = true
      console.log('🔄 [loadChatMessages] 重置分页为第1页')
    }

    // 先显示缓存（快速响应用户体验）
    if (!loadMore && chatHistory.value[sessionId]) {
      const cachedMessages = chatHistory.value[sessionId]
      chatMessages.value = cachedMessages
      scrollToBottom()

      console.log('💾 [loadChatMessages] 已从缓存加载消息', {
        sessionId,
        缓存消息数: cachedMessages.length,
        图片消息数: cachedMessages.filter(msg => msg.msgType === 'image').length,
        文件消息数: cachedMessages.filter(msg => msg.msgType === 'file').length
      })
    }

    if (isLoadingMessages.value || !hasMoreMessages.value) {
      console.log('⏸️ [loadChatMessages] 跳过加载', {
        reason: isLoadingMessages.value ? '正在加载中' : '没有更多消息',
        isLoading: isLoadingMessages.value,
        hasMore: hasMoreMessages.value
      })
      return
    }

    isLoadingMessages.value = true
    console.log('📡 [loadChatMessages] 准备请求后端API', {
      url: `/v1/chat/${sessionId}/messages`,
      params: {
        pageNum: msgPageNum.value,
        pageSize: MESSAGE_CONFIG.DEFAULT_PAGE_SIZE,
        userId: userId.value
      }
    })

    const startTime = Date.now()

    try {
      const response = await api.get(`/v1/chat/${sessionId}/messages`, {
        params: {
          pageNum: msgPageNum.value,
          pageSize: MESSAGE_CONFIG.DEFAULT_PAGE_SIZE,
          userId: userId.value
        }
      })

      const requestTime = Date.now() - startTime

      console.log('📥 [loadChatMessages] 收到API响应', {
        响应码: response.code,
        请求耗时: `${requestTime}ms`,
        总消息数: response.data?.total,
        当前页消息数: response.data?.records?.length,
        当前页码: response.data?.current,
        总页数: response.data?.pages
      })

      if (response.code === '200') {
        const data = response.data
        const messages = data.records || []

        console.log('🔍 [loadChatMessages] 开始处理消息', {
          原始消息数: messages.length,
          消息类型分布: {
            text: messages.filter(m => m.msgType === 'text').length,
            image: messages.filter(m => m.msgType === 'image').length,
            file: messages.filter(m => m.msgType === 'file').length
          }
        })

        const processedMessages = preprocessMessages(messages)

        console.log('✅ [loadChatMessages] 消息处理完成', {
          原始消息: messages.length,
          去重后: processedMessages.length,
          去重数量: messages.length - processedMessages.length
        })

        if (loadMore) {
          const previousCount = chatMessages.value.length
          chatMessages.value = [...processedMessages, ...chatMessages.value]
          const scrollTop = messagesContainerRef.value?.scrollTop || 0
          nextTick(() => {
            if (messagesContainerRef.value) {
              messagesContainerRef.value.scrollTop = scrollTop + 100
            }
          })
          console.log('📜 [loadChatMessages] 加载更多完成', {
            之前消息数: previousCount,
            新增消息数: processedMessages.length,
            总计消息数: chatMessages.value.length
          })
        } else {
          // 用后端数据覆盖缓存数据（保证数据准确性）
          chatHistory.value[sessionId] = processedMessages
          chatMessages.value = processedMessages
          scrollToBottom()
          console.log('📝 [loadChatMessages] 首次加载完成', {
            消息总数: processedMessages.length,
            已更新缓存: true,
            已滚动到底部: true
          })
        }

        totalMessages.value = data.total || 0
        hasMoreMessages.value =
          data.records && data.records.length >= MESSAGE_CONFIG.DEFAULT_PAGE_SIZE

        console.log('📊 [loadChatMessages] 分页状态', {
          总消息数: totalMessages.value,
          当前页: msgPageNum.value,
          每页大小: MESSAGE_CONFIG.DEFAULT_PAGE_SIZE,
          是否有更多: hasMoreMessages.value,
          是否最后一页: !hasMoreMessages.value
        })

        saveChatHistoryToLocal()
      } else {
        console.warn('⚠️ [loadChatMessages] API返回非200状态码', {
          code: response.code,
          message: response.message
        })
      }
    } catch (error) {
      console.error('❌ [loadChatMessages] 请求失败', {
        sessionId,
        pageNum: msgPageNum.value,
        错误信息: error.message,
        错误堆栈: error.stack,
        响应数据: error.response?.data
      })
      ElMessage.error('加载聊天记录失败，请稍后重试')
      if (!loadMore) {
        chatMessages.value = []
      }
    } finally {
      isLoadingMessages.value = false
      const totalTime = Date.now() - startTime
      console.log('✅ [loadChatMessages] 执行完成', {
        总耗时: `${totalTime}ms`,
        当前消息数: chatMessages.value.length,
        isLoading: isLoadingMessages.value
      })
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

      // ⭐ 修复：先确保 fileUrl 和 fullUrl 字段存在，再创建 processedMsg
      const fileUrl = message.fileUrl || message.fullUrl || ''
      const fullUrl = message.fullUrl || message.fileUrl || ''

      // 确定消息类型
      let msgType = message.msgType
      if (message.msgType === 'image' || message.content === '[图片]' || fileUrl) {
        msgType = 'image'
      } else if (message.msgType === 'file' || message.content?.startsWith('[文件]') || message.fileName) {
        msgType = 'file'
      }

      const processedMsg = {
        ...message,
        msgType,
        formattedTime: formatMessageTime(message.createTime || message.time),
        fromId,
        senderName,
        // 显式确保 URL 字段存在
        fileUrl,
        fullUrl
      }

      // 打印调试信息
      if (msgType === 'image') {
        console.log('📸 [Chat] WebSocket 处理图片消息 - msgId:', message.id, 'fileUrl:', processedMsg.fileUrl, 'fullUrl:', processedMsg.fullUrl)
      } else if (msgType === 'file') {
        console.log('📎 [Chat] WebSocket 处理文件消息 - msgId:', message.id, 'fileName:', processedMsg.fileName)
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
