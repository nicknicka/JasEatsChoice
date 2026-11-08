/**
 * 商家端AI聊天 Composable
 */
import { ref, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { useAuthStore } from '../../../../store/authStore'
import { API_CONFIG } from '../../../../config/index'
import { formatTime } from '../../../../utils/dateFormatter'
import { handleApiError } from '../../../../utils/errorHandler'
import { useStreamResponse } from '../../../../composables/useStreamResponse'
import { createMerchantAIStreamingCollector } from './merchantAIStreamingService.js'

import {
  createMerchantAIHistoryService,
  mapMerchantHistoryMessage
} from './merchantAIHistoryService.js'
import {
  MERCHANT_CHAT_CONFIG,
  MERCHANT_WELCOME_MESSAGE,
  MERCHANT_ERROR_MESSAGES,
  merchantLogger
} from '../../../../config/merchantChatConfig'

const messages = ref([])
const isLoading = ref(false)

export function useMerchantAIChat() {
  const authStore = useAuthStore()
  const { isStreaming, processStream } = useStreamResponse()
  const chatContainerRef = ref(null)
  const abortController = ref(null)

  const getUserId = () => String(authStore.userId || localStorage.getItem('auth_userId') || '1')

  const scrollToBottom = (smooth = true) => {
    nextTick(() => {
      if (!chatContainerRef.value) {
        return
      }

      if (smooth) {
        chatContainerRef.value.scrollTo({
          top: chatContainerRef.value.scrollHeight,
          behavior: 'smooth'
        })
        return
      }

      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    })
  }

  const createWelcomeMessage = () => ({
    id: 1,
    sender: 'ai',
    content: MERCHANT_WELCOME_MESSAGE,
    time: formatTime(),
    avatar: '🤖'
  })

  const validateMessage = (content) => {
    const trimmed = content.trim()

    if (!trimmed) {
      return MERCHANT_ERROR_MESSAGES.INPUT_EMPTY
    }

    if (trimmed.length > MERCHANT_CHAT_CONFIG.MAX_MESSAGE_LENGTH) {
      return `消息不能超过 ${MERCHANT_CHAT_CONFIG.MAX_MESSAGE_LENGTH} 个字符`
    }

    return ''
  }

  const historyService = createMerchantAIHistoryService({
    request: axios,
    getUserId,
    getToken: () => authStore.token
  })

  /**
   * 保存单条聊天记录，失败时保留待重试状态
   */
  const saveHistoryMessage = async (message) => {
    if (!message || message.saveStatus === 'saved' || message.saveStatus === 'saving') {
      return
    }

    message.saveStatus = 'saving'
    const result = await historyService.saveMessage(message)
    message.saveStatus = result.status

    if (!result.ok) {
      merchantLogger.warn('保存商家AI聊天记录失败:', result.error)
    }
  }

  /**
   * 重试保存聊天记录，继续复用原来的 clientMessageId
   */
  const retrySaveMessage = (message) => {
    return saveHistoryMessage(message)
  }

  /**
   * 加载聊天历史
   */
  const loadMessages = async () => {
    try {
      const userId = getUserId()
      merchantLogger.log('开始加载商家AI聊天记录', userId)

      const response = await axios.get(API_CONFIG.baseURL + API_CONFIG.ai.history, {
        params: { userId }
      })

      const historyList = response.data?.data
      if (response.data?.code === 200 && Array.isArray(historyList) && historyList.length > 0) {
        messages.value = historyList.map((item, index) =>
          mapMerchantHistoryMessage(item, index, formatTime)
        )
      } else {
        messages.value = [createWelcomeMessage()]
      }
    } catch (error) {
      merchantLogger.error('加载商家AI聊天记录失败:', error)
      messages.value = [createWelcomeMessage()]
    } finally {
      // 首屏停在消息顶部：第一张卡片完整可见，不被 scrollToBottom 顶出上方裁切
      nextTick(() => {
        if (chatContainerRef.value) {
          chatContainerRef.value.scrollTop = 0
        }
      })
    }
  }

  /**
   * 发送消息
   */
  const sendMessage = async (content) => {
    const trimmedContent = content.trim()
    const validationError = validateMessage(trimmedContent)
    if (validationError) {
      ElMessage.warning(validationError)
      return
    }

    const userMessage = {
      id: messages.value.length + 1,
      sender: 'user',
      content: trimmedContent,
      clientMessageId: `merchant-user-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`,
      time: formatTime(),
      avatar: '👤',
      saveStatus: 'pending'
    }
    messages.value.push(userMessage)
    saveHistoryMessage(userMessage)
    scrollToBottom(true)

    const aiMessageIndex = messages.value.length
    const aiClientMessageId = `merchant-ai-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
    messages.value.push({
      id: aiMessageIndex + 1,
      sender: 'ai',
      content: '',
      messageType: null,
      cardData: null,
      clientMessageId: aiClientMessageId,
      saveStatus: 'pending',
      time: formatTime(),
      avatar: '🤖',
      isThinking: true,
      progress: false,
      thinkingText: '正在为您分析需求...'
    })
    scrollToBottom(false)

    abortController.value = new AbortController()
    isLoading.value = true
    const streamingCollector = createMerchantAIStreamingCollector()

    try {
      const response = await fetch(API_CONFIG.baseURL + API_CONFIG.ai.chat, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'text/event-stream'
        },
        body: JSON.stringify({
          message: trimmedContent,
          userId: getUserId()
        }),
        signal: abortController.value.signal
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const reader = response.body?.getReader()
      if (!reader) {
        throw new Error('未获取到流式响应')
      }

      await processStream(
        reader,
        (chunk) => {
          const message = messages.value[aiMessageIndex]
          const extracted = streamingCollector.appendContent(chunk)

          message.isThinking = false
          message.progress = false
          message.thinkingText = ''
          if (extracted.content) message.content = extracted.content

          if (extracted.cardData) {
            message.cardData = extracted.cardData
            message.messageType = 'uni_card'
          }

          scrollToBottom(false)
        },
        () => {
          const message = messages.value[aiMessageIndex]
          message.isThinking = false
          message.progress = false
          message.thinkingText = ''
        },
        (error) => {
          const message = messages.value[aiMessageIndex]
          message.isThinking = false
          message.progress = false
          message.thinkingText = ''
          message.isError = true
          if (!message.content) {
            message.content = handleApiError(error)
          }
        },
        (progressText) => {
          const message = messages.value[aiMessageIndex]
          message.isThinking = false
          message.progress = true
          message.thinkingText = progressText || '正在思考中...'
          scrollToBottom(false)
        },
        (cardData) => {
          const message = messages.value[aiMessageIndex]
          if (!message || !cardData) return

          streamingCollector.appendCardData(cardData)
          const extracted = streamingCollector.getSnapshot()

          message.cardData = extracted.cardData
          message.messageType = extracted.messageType
          message.isThinking = false
          message.progress = false
          message.thinkingText = ''
          scrollToBottom(false)
        }
      )

      await saveHistoryMessage(messages.value[aiMessageIndex])
    } catch (error) {
      if (error.name === 'AbortError') {
        merchantLogger.warn('商家AI回复被主动中断')
        return
      }

      merchantLogger.error('发送商家AI消息失败:', error)
      const message = messages.value[aiMessageIndex]
      message.isThinking = false
      message.progress = false
      message.thinkingText = ''
      message.isError = true
      message.content = message.content || handleApiError(error) || MERCHANT_ERROR_MESSAGES.SERVER_ERROR
    } finally {
      isLoading.value = false
      abortController.value = null
    }
  }

  /**
   * 清空聊天
   */
  const clearChat = async () => {
    try {
      await ElMessageBox.confirm('确定要清空当前经营助手聊天记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      const response = await axios.delete(API_CONFIG.baseURL + API_CONFIG.ai.clear, {
        params: { userId: getUserId() }
      })

      if (response.data?.code !== 200) {
        throw new Error(response.data?.message || '清空失败')
      }

      messages.value = [createWelcomeMessage()]
      ElMessage.success('聊天已清空')
      nextTick(() => {
        if (chatContainerRef.value) {
          chatContainerRef.value.scrollTop = 0
        }
      })
    } catch (error) {
      if (error === 'cancel' || error === 'close') {
        return
      }

      merchantLogger.error('清空商家AI聊天记录失败:', error)
      ElMessage.error(handleApiError(error))
    }
  }

  return {
    messages,
    isLoading,
    isStreaming,
    chatContainerRef,
    loadMessages,
    sendMessage,
    retrySaveMessage,
    clearChat,
    scrollToBottom
  }
}
