<template>
  <div class="chat-content-wrapper">
    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="chatContainerRef">
      <!-- 加载中 -->
      <div v-if="isLoading" class="loading-state">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p>正在加载聊天记录...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="messages.length === 0" class="empty-state">
        <el-icon :size="64"><ChatDotRound /></el-icon>
        <p>暂无消息</p>
        <p class="hint">开始对话吧！</p>
      </div>

      <!-- 消息列表 -->
      <template v-else>
        <div
          v-for="message in messages"
          :key="message.id"
          class="chat-message"
          :class="{
            'user-message': message.sender === 'user',
            'ai-message': message.sender === 'ai'
          }"
        >
          <div class="message-avatar">{{ message.avatar }}</div>
          <div class="message-content">
            <!-- 图片附件 -->
            <div v-if="message.images && message.images.length > 0" class="message-images">
              <div
                v-for="(img, idx) in message.images"
                :key="idx"
                class="message-image"
              >
                <img :src="img.url" :alt="`图片${idx + 1}`" />
              </div>
            </div>

            <!-- 消息文本（支持Markdown或纯文本） -->
            <div
              class="message-text"
              :class="{ 'markdown-content': message.enableMarkdown }"
              v-html="renderContent(message.content, message.enableMarkdown)"
            ></div>
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>
      </template>
    </div>

    <!-- 底部容器 -->
    <div class="bottom-container" ref="bottomContainerRef">
      <!-- 快捷提问面板 -->
      <transition name="slide-down">
        <div v-if="showQuickQuestions" class="quick-questions-panel">
          <div class="quick-questions-header">
            <span class="quick-questions-title">💡 快捷提问</span>
            <el-button
              :icon="Close"
              circle
              size="small"
              text
              @click="showQuickQuestions = false"
            />
          </div>
          <div class="quick-questions-list">
            <el-tag
              v-for="question in quickQuestions"
              :key="question"
              @click="handleQuickQuestion(question)"
              class="question-tag"
              type="info"
              effect="plain"
            >
              {{ question }}
            </el-tag>
          </div>
        </div>
      </transition>

      <!-- 重新显示快捷提问按钮 -->
      <transition name="fade">
        <div v-if="!showQuickQuestions && messages.length > 1" class="show-questions-btn">
          <el-button
            link
            type="primary"
            @click="showQuickQuestions = true"
            size="small"
          >
            💡 显示快捷提问
          </el-button>
        </div>
      </transition>

      <!-- 已上传图片预览 -->
      <transition name="slide-up">
        <div v-if="uploadedImages.length > 0" class="uploaded-images-preview">
          <div
            v-for="img in uploadedImages"
            :key="img.id"
            class="uploaded-image-item"
          >
            <img :src="img.url" alt="上传的图片" />
            <el-button
              :icon="Delete"
              circle
              size="small"
              class="remove-image-btn"
              @click="removeUploadedImage(img.id)"
            />
          </div>
        </div>
      </transition>

      <!-- 表情面板 -->
      <transition name="slide-up">
        <div v-if="showEmojiPicker" class="emoji-panel">
          <div class="emoji-grid">
            <span
              v-for="emoji in commonEmojis"
              :key="emoji"
              class="emoji-item"
              @click="selectEmoji(emoji)"
              :title="emoji"
            >
              {{ emoji }}
            </span>
          </div>
        </div>
      </transition>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-wrapper">
          <!-- 工具栏 -->
          <div class="toolbar">
            <div class="toolbar-left">
              <!-- 表情按钮 -->
              <el-tooltip content="表情" placement="top">
                <el-button
                  :icon="ChatDotRound"
                  circle
                  size="small"
                  @click="toggleEmoji"
                  :class="{ 'is-active': showEmojiPicker }"
                />
              </el-tooltip>

              <!-- 图片上传按钮 -->
              <input
                type="file"
                accept="image/*"
                @change="handleImageUpload"
                style="display: none"
                ref="imageInputRef"
              />
              <el-tooltip content="上传图片" placement="top">
                <el-button
                  :icon="Picture"
                  circle
                  size="small"
                  @click="triggerImageUpload"
                />
              </el-tooltip>

              <div class="toolbar-divider"></div>

              <!-- 清空按钮 -->
              <el-tooltip content="清空输入" placement="top">
                <el-button
                  :icon="Delete"
                  circle
                  size="small"
                  @click="clearInput"
                />
              </el-tooltip>

              <!-- 清空对话按钮 -->
              <el-tooltip content="清空对话" placement="top">
                <el-button
                  :icon="ChatLineRound"
                  circle
                  size="small"
                  @click="clearChat"
                />
              </el-tooltip>
            </div>
            <div class="toolbar-right">
              <!-- Markdown开关 -->
              <el-switch
                v-model="enableMarkdown"
                active-text="Markdown"
                inactive-text="纯文本"
                size="small"
                style="margin-right: 12px"
              />

              <!-- 字数统计 -->
              <span class="char-count">{{ inputMessage.length }}/500</span>
            </div>
          </div>

          <!-- 文本输入框 -->
          <el-input
            v-model="inputMessage"
            placeholder="请输入您的问题...（支持Markdown格式）"
            :rows="2"
            type="textarea"
            :maxlength="500"
            :disabled="isLoading"
            @keydown="handleKeydown"
            class="message-input"
          />
        </div>

        <el-button
          :type="isStreaming ? 'danger' : 'primary'"
          class="send-btn"
          @click="handleSendClick"
          :disabled="isLoading && !isStreaming"
          :loading="isLoading"
        >
          {{ isStreaming ? '停止' : '发送' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Loading,
  ChatDotRound,
  Close,
  ChatDotRound as ChatDotRoundIcon,
  Delete,
  Picture,
  ChatLineRound
} from '@element-plus/icons-vue'
import { parseMarkdown } from '../../../../utils/markdownParser'
import axios from 'axios'
import { API_CONFIG } from '../../../../config/index'
import { useAuthStore } from '../../../../store/authStore'

// 获取认证store
const authStore = useAuthStore()

// 获取用户ID
const getUserId = () => {
  return String(authStore.userId || '1')
}

// 状态
const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const isStreaming = ref(false)
const abortController = ref(null)
const chatContainerRef = ref(null)
const bottomContainerRef = ref(null)
const showQuickQuestions = ref(true)
const showEmojiPicker = ref(false)
const uploadedImages = ref([])
const enableMarkdown = ref(true)

// AI个性化数据开关状态（隐私保护原则：默认未授权）
const aiPersonalDataEnabled = ref(false)

// 快捷问题列表
const quickQuestions = ref([
  "推荐适合减肥的食谱",
  "今日卡路里摄入建议",
  "如何搭配营养均衡的饮食",
  "推荐低卡路里零食",
  "适合运动后的食物"
])

// 常用表情列表
const commonEmojis = ref([
  "😊", "😂", "🤔", "👍", "👎", "❤️", "🔥", "✨",
  "🍎", "🥗", "🍲", "🍜", "🍕", "🍰", "☕", "🥤",
  "💪", "🏃", "🧘", "😋", "🤤", "😌", "🤗", "😎"
])

// 加载用户偏好设置
const loadUserPreference = async () => {
  try {
    const userId = getUserId()
    console.log('📥 加载用户偏好设置，userId:', userId)

    const response = await axios.get(`${API_CONFIG.baseURL}/v1/users/${userId}/preferences`)

    if (response.data && response.data.data) {
      // 只有明确设置为 true 时才启用（隐私保护原则）
      aiPersonalDataEnabled.value = response.data.data.enableAiPersonalData === true
      console.log('✅ 用户偏好加载成功:', aiPersonalDataEnabled.value)
    }
  } catch (error) {
    console.error('❌ 加载用户偏好失败:', error)
    // 失败时使用默认值（隐私保护原则：默认未授权）
    aiPersonalDataEnabled.value = false
  }
}

// 从后端加载聊天记录
const loadMessages = async () => {
  try {
    const userId = getUserId()
    console.log('📥 开始加载聊天记录，userId:', userId)

    const response = await axios.get(API_CONFIG.baseURL + API_CONFIG.ai.history, {
      params: { userId }
    })

    console.log('📡 后端响应:', response.data)
    console.log('📊 响应数据:', response.data.data)
    console.log('📏 数据长度:', response.data.data ? response.data.data.length : 0)

    if (
      response.data.code === 200 &&
      response.data.data &&
      response.data.data.length > 0
    ) {
      // 将后端数据转换为前端格式
      const historyData = response.data.data
      messages.value = historyData.map((item, index) => ({
        id: index + 1,
        sender: item.sender, // 'user' 或 'ai'
        content: item.content,
        time: new Date(item.createTime).toLocaleTimeString([], {
          hour: '2-digit',
          minute: '2-digit'
        }),
        avatar: item.sender === 'ai' ? '🤖' : '👤',
        enableMarkdown: enableMarkdown.value
      }))
      console.log('✅ 成功加载聊天历史:', messages.value.length, '条消息')
    } else {
      // 没有历史记录，显示欢迎消息并保存到后端
      console.log('📭 没有历史记录，显示欢迎消息')
      const welcomeMessage = '您好！我是您的AI饮食助手。😊\n\n我可以帮助您：\n- 推荐健康食谱\n- 分析营养成分\n- 提供饮食建议\n\n有什么可以帮您的吗？'
      messages.value = [
        {
          id: 1,
          sender: 'ai',
          content: welcomeMessage,
          time: new Date().toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit'
          }),
          avatar: '🤖',
          enableMarkdown: enableMarkdown.value
        }
      ]
      console.log('💾 保存欢迎消息到后端')
      // 保存欢迎消息到后端
      await saveMessageToBackend('ai', welcomeMessage)
    }
    isLoading.value = false
    scrollToBottom()
  } catch (error) {
    console.error('❌ 加载聊天记录失败:', error)
    console.error('❌ 错误详情:', error.response?.data || error.message)
    // 加载失败时，显示欢迎消息
    messages.value = [
      {
        id: 1,
        sender: 'ai',
        content: '您好！我是您的AI饮食助手。😊\n\n我可以帮助您：\n- 推荐健康食谱\n- 分析营养成分\n- 提供饮食建议\n\n有什么可以帮您的吗？',
        time: new Date().toLocaleTimeString([], {
          hour: '2-digit',
          minute: '2-digit'
        }),
        avatar: '🤖',
        enableMarkdown: enableMarkdown.value
      }
    ]
    isLoading.value = false
    scrollToBottom()
  }
}

// 保存消息到后端
const saveMessageToBackend = async (sender, content) => {
  try {
    const userId = getUserId()
    await axios.post(API_CONFIG.baseURL + API_CONFIG.ai.save, {
      userId,
      sender, // 'user' 或 'ai'
      content
    })
    console.log('✅ 消息已保存到后端:', sender)
  } catch (error) {
    console.error('❌ 保存消息到后端失败:', error)
  }
}

// 渲染内容（支持Markdown或纯文本）
const renderContent = (content, useMarkdown) => {
  if (useMarkdown) {
    return parseMarkdown(content)
  }
  return content.replace(/\n/g, '<br>')
}

// 流式传输：逐块读取AI回复
const streamResponse = async (messageIndex, reader) => {
  isStreaming.value = true
  messages.value[messageIndex].content = ''

  const decoder = new TextDecoder()
  let buffer = ''

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      const chunk = decoder.decode(value, { stream: true })
      buffer += chunk
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        const trimmedLine = line.trim()
        if (!trimmedLine.startsWith('data:')) continue

        const data = trimmedLine.substring(5).trim()
        if (!data) continue

        try {
          // 解析SSE数据（可能是数组格式或直接的对象）
          let parsedData

          if (data.startsWith('[')) {
            // Spring Boot的SseEmitter数组格式：[{...}, {...}, {...}]
            const dataArray = JSON.parse(data)

            // 查找mediaType为null的元素（包含实际数据）
            const actualDataItem = dataArray.find(
              (item) => item.mediaType === null
            )

            if (actualDataItem && actualDataItem.data) {
              parsedData = actualDataItem.data
            }
          } else {
            // 直接的对象格式：{ content: string, done: boolean }
            parsedData = JSON.parse(data)
          }

          if (!parsedData) continue

          // 接收 done 字段：检查是否结束
          if (parsedData.done === true) {
            console.log('✅ 接收完成')

            // 保存AI的完整回复到后端
            const aiContent = messages.value[messageIndex].content
            if (aiContent) {
              await saveMessageToBackend('ai', aiContent)
            }

            return
          }

          // 接收 content 字段：追加文本
          if (parsedData.content) {
            messages.value[messageIndex].content += parsedData.content
            await nextTick()
            scrollToBottom()
          }
        } catch (error) {
          console.log('⚠️ 跳过无效数据:', data)
        }
      }
    }
  } catch (error) {
    // 用户主动取消，不显示错误日志
    if (error.name === 'AbortError') {
      console.log('ℹ️ 用户主动停止流式传输')
      return
    }
    // 其他错误正常处理
    console.error('❌ 流式传输错误:', error)
    throw error
  } finally {
    isStreaming.value = false
  }
}

// 发送消息
const sendMessage = async () => {
  const message = inputMessage.value.trim()
  const hasImages = uploadedImages.value.length > 0

  if (!message && !hasImages) {
    ElMessage.warning('请输入问题或上传图片')
    return
  }

  if (message.length > 500) {
    ElMessage.warning('消息长度不能超过500个字符')
    return
  }

  // ========== 日志记录：请求开始 ==========
  const requestStartTime = Date.now()
  console.log('==================== AI聊天请求开始 ====================')
  console.log('⏰ 请求时间:', new Date().toLocaleString())
  console.log('📝 用户消息:', message)
  console.log('📏 消息长度:', message.length, '字符')
  console.log('📊 当前消息数量:', messages.value.length)

  // 创建用户消息
  const userMessage = {
    id: messages.value.length + 1,
    sender: 'user',
    content: message,
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    avatar: '👤',
    images: hasImages ? [...uploadedImages.value] : undefined
  }

  messages.value.push(userMessage)
  const userInput = message

  // 清空输入
  inputMessage.value = ''
  uploadedImages.value = []

  // 保存用户消息到后端
  await saveMessageToBackend('user', message)

  // 滚动到底部（用户消息发送后）
  scrollToBottom()

  // Call backend AI API
  isLoading.value = true

  // 创建AI消息占位
  const aiMessageIndex = messages.value.length
  messages.value.push({
    id: aiMessageIndex,
    sender: 'ai',
    content: '',
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    avatar: '🤖',
    enableMarkdown: enableMarkdown.value
  })

  // 再次滚动到底部，确保AI消息气泡可见
  scrollToBottom()

  // ========== 日志记录：API调用 ==========
  const apiUrl = API_CONFIG.baseURL + API_CONFIG.ai.chat
  console.log('🌐 发送流式API请求到:', apiUrl)
  console.log('📦 请求体:', { message: userInput })

  // 创建新的AbortController用于取消请求
  abortController.value = new AbortController()

  try {
    // 使用fetch API发起流式请求
    const response = await fetch(apiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'text/event-stream'
      },
      body: JSON.stringify({ message: userInput }),
      signal: abortController.value.signal
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    // ========== 日志记录：响应接收 ==========
    const responseTime = Date.now() - requestStartTime
    console.log('✅ 连接成功，耗时:', responseTime, 'ms')

    // 获取流式读取器
    const reader = response.body.getReader()

    // 使用流式传输处理响应（传入消息索引而不是消息对象）
    await streamResponse(aiMessageIndex, reader)

    const totalTime = Date.now() - requestStartTime
    console.log('✨ 整体请求完成，总耗时:', totalTime, 'ms')
    console.log(
      '📝 AI回复最终内容长度:',
      messages.value[aiMessageIndex].content.length,
      '字符'
    )
    console.log('==================== AI聊天请求完成 ====================\n')
  } catch (error) {
    // ========== 日志记录：错误处理 ==========

    // 用户主动取消，静默处理，不显示错误日志
    if (error.name === 'AbortError') {
      console.log('ℹ️ 用户主动取消AI回复')
      console.log('==================== AI聊天请求已取消 ====================\n')
      return  // 直接返回，不执行后续错误处理
    }

    // 其他错误的处理
    const errorTime = Date.now() - requestStartTime
    console.error('❌ API请求失败，耗时:', errorTime, 'ms')
    console.error('📋 错误对象:', error)
    console.error('❌ 错误消息:', error.message)

    let errorMsg = '对不起，暂时无法获取AI回复，请稍后重试。'

    // Add more specific error messages
    if (error.message.includes('HTTP error')) {
      const statusCode = parseInt(error.message.match(/\d+/)?.[0] || '500')
      console.error('🔴 服务器错误状态码:', statusCode)

      if (statusCode === 404) {
        errorMsg = 'AI聊天服务暂时不可用，请稍后重试。'
      } else if (statusCode === 500) {
        errorMsg = '服务器内部错误，请稍后重试。'
      } else {
        errorMsg = `服务器错误(${statusCode})，请稍后重试。`
      }
    } else if (error.message.includes('fetch')) {
      // Network error
      console.error('🔴 网络错误，无响应')
      errorMsg = '网络连接超时，请检查网络设置。'
    }

    // 只有当内容为空时才显示错误消息
    if (!messages.value[aiMessageIndex].content) {
      messages.value[aiMessageIndex].content = errorMsg
    }

    const totalTime = Date.now() - requestStartTime
    console.log('==================== AI聊天请求失败 ====================\n')
  } finally {
    isLoading.value = false
    abortController.value = null
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

// 键盘事件
const handleKeydown = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

// 发送按钮点击
const handleSendClick = () => {
  if (isStreaming.value) {
    stopStreaming()
  } else {
    sendMessage()
  }
}

// 停止流式传输
const stopStreaming = () => {
  if (abortController.value) {
    console.log('🛑 用户主动停止流式传输')
    abortController.value.abort()
    ElMessage.info('已停止AI回复')
  }
}

// 快捷提问点击
const handleQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

// 切换表情面板
const toggleEmoji = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

// 选择表情
const selectEmoji = (emoji) => {
  inputMessage.value += emoji
  showEmojiPicker.value = false
  nextTick(() => {
    const textarea = bottomContainerRef.value?.querySelector('textarea')
    if (textarea) {
      textarea.focus()
      textarea.selectionStart = textarea.selectionEnd = textarea.value.length
    }
  })
}

// 触发图片上传
const triggerImageUpload = () => {
  const input = bottomContainerRef.value?.querySelector('input[type="file"]')
  if (input) {
    input.click()
  }
}

// 处理图片上传
const handleImageUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  // 验证文件大小（10MB）
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过10MB')
    return
  }

  // 创建预览
  const reader = new FileReader()
  reader.onload = (e) => {
    uploadedImages.value.push({
      id: Date.now(),
      url: e.target.result,
      file: file
    })
    ElMessage.success('图片上传成功')
  }
  reader.readAsDataURL(file)

  // 清空input，允许重复上传
  event.target.value = ''
}

// 移除已上传的图片
const removeUploadedImage = (imageId) => {
  const index = uploadedImages.value.findIndex(img => img.id === imageId)
  if (index > -1) {
    uploadedImages.value.splice(index, 1)
  }
}

// 清空输入
const clearInput = () => {
  inputMessage.value = ''
  uploadedImages.value = []
  ElMessage.success('已清空')
}

// 清空对话
const clearChat = () => {
  ElMessageBox.confirm('确定要清空所有聊天记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        const userId = getUserId()
        console.log('🗑️ 开始清空聊天记录，userId:', userId)

        // 调用后端API清空聊天记录
        const clearResponse = await axios.delete(
          API_CONFIG.baseURL + API_CONFIG.ai.clear,
          {
            params: { userId }
          }
        )
        console.log('📡 后端清空响应:', clearResponse.data)

        // 检查后端是否成功清空
        if (clearResponse.data.code === 200) {
          console.log('✅ 后端清空成功')

          // 清空前端显示
          messages.value = []

          // 重新加载消息（会显示欢迎消息并保存到后端）
          await loadMessages()

          console.log('✅ 前端已重新加载消息')
          ElMessage.success('聊天记录已清空')

          // 清空后滚动到顶部
          nextTick(() => {
            if (chatContainerRef.value) {
              chatContainerRef.value.scrollTop = 0
            }
          })
        } else {
          // 后端返回错误码
          console.error('❌ 后端清空失败，响应码:', clearResponse.data.code)
          console.error('❌ 错误信息:', clearResponse.data.message)
          ElMessage.error(clearResponse.data.message || '清空失败，请稍后重试')
        }
      } catch (error) {
        console.error('❌ 清空聊天记录失败:', error)
        console.error('❌ 错误详情:', error.response?.data || error.message)

        // 根据错误类型显示不同提示
        let errorMsg = '清空失败，请稍后重试'
        if (error.response) {
          // 服务器返回了错误响应
          if (error.response.status === 404) {
            errorMsg = '清空服务暂时不可用'
          } else if (error.response.status === 500) {
            errorMsg = '服务器内部错误'
          } else if (error.response.data?.message) {
            errorMsg = error.response.data.message
          }
        } else if (error.message) {
          errorMsg = `网络错误：${error.message}`
        }

        ElMessage.error(errorMsg)
      }
    })
    .catch(() => {
      console.log('ℹ️ 用户取消清空操作')
    })
}

// 点击外部关闭表情面板
const handleClickOutside = (event) => {
  if (bottomContainerRef.value && !bottomContainerRef.value.contains(event.target)) {
    showEmojiPicker.value = false
  }
}

// 切换个性化数据开关
const handlePersonalDataToggle = async (value) => {
  try {
    const userId = getUserId()
    console.log('🔄 切换AI个性化数据:', value)

    await axios.put(`${API_CONFIG.baseURL}/v1/users/${userId}/preferences`, {
      enableAiPersonalData: value
    })

    ElMessage.success(value ? '已开启个性化建议' : '已关闭个性化建议')
    console.log('✅ 用户偏好更新成功')
  } catch (error) {
    console.error('❌ 更新偏好设置失败:', error)
    ElMessage.error('设置保存失败')

    // 恢复原状态
    aiPersonalDataEnabled.value = !value
  }
}

// 生命周期
onMounted(async () => {
  document.addEventListener('click', handleClickOutside)
  // 加载聊天历史记录
  await loadMessages()
  // 加载用户偏好设置
  await loadUserPreference()
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

// 初始化
loadMessages()
</script>

<style scoped lang="less">
.chat-content-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  flex: 1;
  gap: 12px;
  overflow: hidden;
  box-sizing: border-box;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  background-color: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 16px 0 rgba(0, 0, 0, 0.06);

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #dee2e6;
    border-radius: 3px;

    &:hover {
      background: #adb5bd;
    }
  }

  .loading-state,
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #909399;

    p {
      margin: 12px 0 0 0;
      font-size: 16px;
    }

    .hint {
      font-size: 14px;
      color: #c0c4cc;
    }
  }

  .chat-message {
    display: flex;
    gap: 12px;
    margin-bottom: 24px;
    animation: messageFadeIn 0.4s ease-out;

    &.user-message {
      flex-direction: row-reverse;
      justify-content: flex-start;

      .message-content {
        align-items: flex-end;

        .message-text {
          background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
          color: #fff;
          border-radius: 20px 20px 4px 20px;
          box-shadow: 0 4px 12px rgba(255, 107, 107, 0.25);
          font-weight: 500;
        }
      }
    }

    &.ai-message {
      flex-direction: row;
      justify-content: flex-start;

      .message-content {
        align-items: flex-start;

        .message-text {
          background: linear-gradient(135deg, #fff9fa 0%, #fff3f4 100%);
          color: #c8232c;
          border-radius: 20px 20px 20px 4px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
          border: 1px solid #ffe0e3;
        }
      }
    }

    .message-avatar {
      font-size: 42px;
      flex-shrink: 0;
      filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.15));
      line-height: 1;
    }

    .message-content {
      display: flex;
      flex-direction: column;
      gap: 8px;
      max-width: 75%;

      .message-images {
        display: flex;
        gap: 8px;
        margin-bottom: 4px;

        .message-image {
          border-radius: 8px;
          overflow: hidden;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

          img {
            max-width: 150px;
            max-height: 150px;
            object-fit: cover;
            display: block;
          }
        }
      }

      .message-text {
        padding: 14px 18px;
        border-radius: 20px;
        line-height: 1.7;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        font-size: 15px;
        white-space: pre-wrap;
        word-break: break-word;

        &:hover {
          transform: translateY(-2px) scale(1.01);
        }

        // Markdown样式
        &.markdown-content {
          // 标题
          :deep(h1), :deep(h2), :deep(h3) {
            margin: 12px 0 8px 0;
            font-weight: 600;
            line-height: 1.4;
          }

          :deep(h1) { font-size: 20px; }
          :deep(h2) { font-size: 18px; }
          :deep(h3) { font-size: 16px; }

          &:first-child {
            margin-top: 0;
          }
        }

        // 代码块
        :deep(pre) {
          background: rgba(0, 0, 0, 0.05);
          border-radius: 8px;
          padding: 10px;
          margin: 8px 0;
          overflow-x: auto;

          code {
            font-family: 'Courier New', monospace;
            font-size: 13px;
            line-height: 1.5;
            color: #333;
          }
        }

        // 行内代码
        :deep(code:not(pre code)) {
          background: rgba(0, 0, 0, 0.05);
          padding: 2px 6px;
          border-radius: 4px;
          font-family: 'Courier New', monospace;
          font-size: 0.9em;
        }

        // 粗体和斜体
        :deep(strong) {
          font-weight: 600;
        }

        :deep(em) {
          font-style: italic;
        }

        // 链接
        :deep(a) {
          color: inherit;
          text-decoration: underline;
          opacity: 0.8;

          &:hover {
            opacity: 1;
          }
        }

        // 列表
        :deep(ul), :deep(ol) {
          margin: 8px 0;
          padding-left: 20px;
        }

        :deep(li) {
          margin: 4px 0;
        }

        // 换行
        :deep(br) {
          line-height: 1.5;
        }
      }

      .message-time {
        font-size: 12px;
        color: #a8abb2;
        margin-top: 2px;
      }
    }
  }
}

@keyframes messageFadeIn {
  from {
    opacity: 0;
    transform: translateY(15px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.bottom-container {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

// 快捷提问面板
.quick-questions-panel {
  background: linear-gradient(135deg, #f0f9ff 0%, #e8f4fd 100%);
  border: 1px solid #d1e9ff;
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.08);

  .quick-questions-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    .quick-questions-title {
      font-size: 14px;
      font-weight: 600;
      color: #2c7be5;
    }
  }

  .quick-questions-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .question-tag {
      margin: 0;
      padding: 6px 14px;
      cursor: pointer;
      transition: all 0.3s ease;
      font-size: 13px;
      font-weight: 500;
      border-radius: 20px;
      background-color: #fff;
      border-color: #b3e0ff;
      color: #409eff;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.25);
        background: linear-gradient(135deg, #409eff 0%, #5dade2 100%);
        color: #fff;
        border-color: transparent;
      }
    }
  }
}

.show-questions-btn {
  text-align: center;
  padding: 4px 0;
}

// 已上传图片预览
.uploaded-images-preview {
  display: flex;
  gap: 12px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 12px;

  .uploaded-image-item {
    position: relative;
    width: 100px;
    height: 100px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .remove-image-btn {
      position: absolute;
      top: 4px;
      right: 4px;
      width: 24px;
      height: 24px;
      min-height: 24px;
      padding: 0;
      background-color: rgba(0, 0, 0, 0.6);
      border: none;
      color: #fff;
      opacity: 0;
      transition: all 0.2s ease;

      &:hover {
        background-color: rgba(255, 107, 107, 0.9);
        transform: scale(1.1);
      }
    }

    &:hover .remove-image-btn {
      opacity: 1;
    }
  }
}

// 表情面板
.emoji-panel {
  position: relative;
  background: #ffffff;
  border: 1px solid #e8ecef;
  border-radius: 8px;
  padding: 10px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  margin-bottom: 0;
  max-height: 180px;
  overflow-y: auto;
  z-index: 100;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #dee2e6;
    border-radius: 3px;

    &:hover {
      background: #adb5bd;
    }
  }

  .emoji-grid {
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 4px;

    .emoji-item {
      font-size: 20px;
      text-align: center;
      padding: 6px 4px;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      user-select: none;

      &:hover {
        background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
        transform: scale(1.2);
      }

      &:active {
        transform: scale(1.05);
      }
    }
  }
}

// 表情面板动画
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

// 快捷提问面板动画
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-12px);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.input-area {
  flex-shrink: 0;
  display: flex;
  gap: 12px;
  align-items: flex-end;
  background: linear-gradient(to bottom, #ffffff 0%, #fafbfc 100%);
  border: 1px solid #e8ecef;
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .input-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 2px;

    .toolbar-left {
      display: flex;
      gap: 6px;
      align-items: center;
    }

    .toolbar-right {
      display: flex;
      gap: 12px;
      align-items: center;

      .char-count {
        font-size: 12px;
        color: #909399;
      }
    }

    .toolbar-divider {
      width: 1px;
      height: 16px;
      background: #e8ecef;
      margin: 0 4px;
    }

    :deep(.el-button) {
      border: 1px solid #e8ecef;
      background: #ffffff;
      color: #5a6c7d;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      font-weight: 500;

      &:hover {
        border-color: #667eea;
        color: #667eea;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
        background: #ffffff;
      }

      &:active {
        transform: translateY(0);
      }

      &.is-active {
        border-color: #667eea;
        color: #667eea;
        background: linear-gradient(
          135deg,
          rgba(102, 126, 234, 0.1) 0%,
          rgba(118, 75, 162, 0.1) 100%
        );
        box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15),
          0 2px 8px rgba(102, 126, 234, 0.2);
      }
    }
  }

  .message-input {
    flex: 1;

    :deep(.el-textarea__inner) {
      border-radius: 10px;
      border: 2px solid #e8ecef;
      background: #ffffff;
      padding: 8px 12px;
      font-size: 14px;
      line-height: 1.6;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      resize: none;

      &:focus {
        border-color: #ff6b6b;
        box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.12);
        background: #ffffff;
      }

      &:hover:not(:focus) {
        border-color: #d0d7de;
      }
    }
  }

  .send-btn {
    flex-shrink: 0;
    background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
    border: none;
    padding: 10px 28px;
    font-size: 15px;
    font-weight: 600;
    border-radius: 10px;
    box-shadow: 0 2px 8px rgba(255, 107, 107, 0.25);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    height: 56px;

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(255, 107, 107, 0.35);
    }

    &:active:not(:disabled) {
      transform: translateY(0);
    }

    &:disabled {
      background: #e9ecef;
      box-shadow: none;
      color: #adb5bd;
    }
  }
}
</style>
