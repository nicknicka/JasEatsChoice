import { ref, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../config/index'
import { useAuthStore } from '../store/authStore'
import { formatTime } from '../utils/dateFormatter'
import { handleApiError } from '../utils/errorHandler'
import { validateMessage } from '../utils/imageValidator'
import { CHAT_CONFIG, ERROR_MESSAGES, WELCOME_MESSAGE, logger } from '../config/chatConfig'
import { useStreamResponse } from './useStreamResponse'

/**
 * AI聊天功能
 * @returns {Object} 聊天相关方法和状态
 */
export function useAIChat() {
  const authStore = useAuthStore()
  const { isStreaming, processStream } = useStreamResponse()

  // 状态
  const messages = ref([])
  const isLoading = ref(false)
  const abortController = ref(null)
  const chatContainerRef = ref(null)

  // 获取用户ID
  const getUserId = () => {
    return String(authStore.userId || '1')
  }

  /**
   * 滚动到底部
   * @param {boolean} smooth - 是否平滑滚动
   */
  const scrollToBottom = (smooth = true) => {
    nextTick(() => {
      if (chatContainerRef.value) {
        if (smooth) {
          chatContainerRef.value.scrollTo({
            top: chatContainerRef.value.scrollHeight,
            behavior: 'smooth'
          })
        } else {
          chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
        }
      }
    })
  }

  /**
   * 加载聊天历史记录
   * @returns {Promise<void>}
   */
  const loadMessages = async () => {
    try {
      const userId = getUserId()
      logger.log('📥 开始加载聊天记录，userId:', userId)

      const historyResponse = await axios.get(API_CONFIG.baseURL + API_CONFIG.ai.history, {
        params: { userId }
      })

      logger.log('📡 后端响应:', historyResponse.data)

      if (
        historyResponse.data.code === 200 &&
        historyResponse.data.data &&
        historyResponse.data.data.length > 0
      ) {
        // 转换为前端格式
        messages.value = historyResponse.data.data.map((item, index) => ({
          id: index + 1,
          sender: item.sender,
          content: item.content,
          time: formatTime(new Date(item.createTime)),
          avatar: item.sender === 'ai' ? '🤖' : '👤'
        }))
        logger.log('✅ 成功加载聊天历史:', messages.value.length, '条消息')
      } else {
        // 没有历史记录，显示欢迎消息
        logger.log('📭 没有历史记录，显示欢迎消息')
        await addMessage('ai', WELCOME_MESSAGE, false)
      }

      // 滚动到底部
      scrollToBottom(false)
    } catch (error) {
      logger.error('❌ 加载聊天记录失败:', error)

      // 加载失败时显示欢迎消息
      messages.value = [
        {
          id: 1,
          sender: 'ai',
          content: WELCOME_MESSAGE,
          time: formatTime(),
          avatar: '🤖'
        }
      ]
    }
  }

  /**
   * 添加消息到列表
   * @param {string} sender - 发送者（user/ai）
   * @param {string} content - 消息内容
   * @param {boolean} saveToBackend - 是否保存到后端
   * @returns {Promise<void>}
   */
  const addMessage = async (sender, content, saveToBackend = true) => {
    const message = {
      id: messages.value.length + 1,
      sender,
      content,
      time: formatTime(),
      avatar: sender === 'ai' ? '🤖' : '👤'
    }

    messages.value.push(message)

    if (saveToBackend) {
      await saveMessageToBackend(sender, content)
    }
  }

  /**
   * 保存消息到后端
   * @param {string} sender - 发送者
   * @param {string} content - 消息内容
   * @returns {Promise<void>}
   */
  const saveMessageToBackend = async (sender, content) => {
    try {
      const userId = getUserId()
      await axios.post(API_CONFIG.baseURL + API_CONFIG.ai.save, {
        userId,
        sender,
        content
      })
      logger.log('✅ 消息已保存到后端:', sender)
    } catch (error) {
      logger.error('❌ 保存消息到后端失败:', error)
    }
  }

  /**
   * 清空聊天记录
   * @returns {Promise<void>}
   */
  const clearChat = async () => {
    try {
      await ElMessageBox.confirm('确定要清空所有聊天记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      const userId = getUserId()
      logger.log('🗑️ 开始清空聊天记录，userId:', userId)

      // 调用后端API清空聊天记录
      const clearResponse = await axios.delete(API_CONFIG.baseURL + API_CONFIG.ai.clear, {
        params: { userId }
      })

      if (clearResponse.data.code === 200) {
        logger.log('✅ 后端清空成功')

        // 清空前端显示
        messages.value = []

        // 重新加载消息（会显示欢迎消息）
        await loadMessages()

        ElMessage.success(ERROR_MESSAGES.CLEARED_SUCCESS)

        // 滚动到顶部
        nextTick(() => {
          if (chatContainerRef.value) {
            chatContainerRef.value.scrollTop = 0
          }
        })
      } else {
        ElMessage.error(clearResponse.data.message || '清空失败，请稍后重试')
      }
    } catch (error) {
      if (error !== 'cancel') {
        logger.error('❌ 清空聊天记录失败:', error)
        ElMessage.error(handleApiError(error))
      }
    }
  }

  /**
   * 停止流式传输
   */
  const stopStreaming = () => {
    if (abortController.value) {
      logger.log('🛑 用户主动停止流式传输')
      abortController.value.abort()
      ElMessage.info('已停止AI回复')
    }
  }

  /**
   * 发送消息
   * @param {string} message - 消息内容
   * @returns {Promise<void>}
   */
  const sendMessage = async (message) => {
    // 验证消息
    const validation = validateMessage(message)
    if (!validation.valid) {
      ElMessage.warning(validation.error)
      return
    }

    const requestStartTime = Date.now()
    logger.log('==================== AI聊天请求开始 ====================')
    logger.log('⏰ 请求时间:', new Date().toLocaleString())
    logger.log('📝 用户消息:', message)

    // 添加用户消息
    await addMessage('user', message)
    scrollToBottom(true)

    // 创建AI消息对象（初始为空）
    const aiMessageIndex = messages.value.length
    messages.value.push({
      id: aiMessageIndex + 1,
      sender: 'ai',
      content: '',
      time: formatTime(),
      avatar: '🤖'
    })

    scrollToBottom(false)

    // 创建AbortController
    abortController.value = new AbortController()
    isLoading.value = true

    try {
      const apiUrl = API_CONFIG.baseURL + API_CONFIG.ai.chat
      logger.log('🌐 发送流式API请求到:', apiUrl)

      // 使用fetch API发起流式请求
      const response = await fetch(apiUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'text/event-stream'
        },
        body: JSON.stringify({ message }),
        signal: abortController.value.signal
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const responseTime = Date.now() - requestStartTime
      logger.log('✅ 连接成功，耗时:', responseTime, 'ms')

      // 获取流式读取器
      const reader = response.body.getReader()

      // 处理流式响应
      await processStream(
        reader,
        // onContent - 追加内容
        (content) => {
          messages.value[aiMessageIndex].content += content
          nextTick(() => scrollToBottom(false))
        },
        // onComplete - 完成后保存
        async () => {
          const aiContent = messages.value[aiMessageIndex].content
          if (aiContent) {
            await saveMessageToBackend('ai', aiContent)
          }
        },
        // onError - 错误处理
        (error) => {
          if (!messages.value[aiMessageIndex].content) {
            messages.value[aiMessageIndex].content = handleApiError(error)
          }
        }
      )

      const totalTime = Date.now() - requestStartTime
      logger.log('✨ 整体请求完成，总耗时:', totalTime, 'ms')
      logger.log('📝 AI回复最终内容长度:', messages.value[aiMessageIndex].content.length, '字符')
      logger.log('==================== AI聊天请求完成 ====================\n')
    } catch (error) {
      // 用户主动取消
      if (error.name === 'AbortError') {
        logger.log('ℹ️ 用户主动取消AI回复')
        logger.log('==================== AI聊天请求已取消 ====================\n')
        return
      }

      // 其他错误
      logger.error('❌ API请求失败:', error)

      if (!messages.value[aiMessageIndex].content) {
        messages.value[aiMessageIndex].content = handleApiError(error)
      }

      logger.log('==================== AI聊天请求失败 ====================\n')
    } finally {
      isLoading.value = false
      abortController.value = null
    }
  }

  return {
    // 状态
    messages,
    isLoading,
    isStreaming,
    chatContainerRef,

    // 方法
    loadMessages,
    sendMessage,
    clearChat,
    stopStreaming,
    scrollToBottom
  }
}
