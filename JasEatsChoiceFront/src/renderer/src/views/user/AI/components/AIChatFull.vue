<template>
  <div class="chat-content-wrapper">
    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="chatContainerRef">
      <!-- 消息列表 -->
      <template v-if="messages.length > 0">
        <div
          v-for="message in messages"
          :key="message.id"
          class="chat-message"
          :class="{
            'user-message': message.sender === 'user',
            'ai-message': message.sender === 'ai'
          }"
        >
          <!-- 用户头像：使用真实头像 -->
          <CommonAvatar
            v-if="message.sender === 'user'"
            :avatar-url="message.avatar"
            :size="42"
            :fallback-text="userStore.userInfo?.nickname || '用'"
            class="message-avatar-custom"
          />
          <!-- AI头像：使用emoji -->
          <div v-else class="message-avatar">{{ message.avatar }}</div>
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
            <div class="message-text">
              <div
                class="message-text-content"
                :class="{ 'markdown-content': message.enableMarkdown }"
                v-html="renderContent(message.content, message.enableMarkdown)"
              ></div>
              <!-- 更多操作按钮 -->
              <el-dropdown trigger="click" @command="(cmd) => handleMessageAction(cmd, message.content)">
                <span class="more-btn">
                  <el-icon :size="12">
                    <More />
                  </el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="copy">
                      <el-icon><DocumentCopy /></el-icon>
                      <span>复制</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>
      </template>
    </div>

    <!-- 底部容器 -->
    <div class="bottom-container" ref="bottomContainerRef">
      <!-- 快捷提问悬浮面板 -->
      <transition name="slide-up">
        <div v-if="showQuickQuestions" class="quick-questions-panel">
          <div class="quick-questions-title">💡 快捷提问</div>
          <div class="quick-questions-categories">
            <div
              v-for="category in quickQuestionCategories"
              :key="category.id"
              class="question-category"
            >
              <!-- 分类标题（可点击展开/折叠） -->
              <div
                class="category-header"
                @click="toggleCategory(category.id)"
              >
                <span class="category-title">{{ category.title }}</span>
                <el-icon
                  class="category-arrow"
                  :class="{ 'is-expanded': category.expanded }"
                >
                  <ArrowRight />
                </el-icon>
              </div>

              <!-- 分类问题列表 -->
              <transition name="category-slide">
                <div v-show="category.expanded" class="category-questions">
                  <div
                    v-for="question in category.questions"
                    :key="question"
                    @click.stop="handleQuickQuestion(question)"
                    class="question-item"
                  >
                    {{ question }}
                  </div>
                </div>
              </transition>
            </div>
          </div>
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
                  :icon="Operation"
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

              <!-- 清空对话按钮 -->
              <el-tooltip content="清空对话" placement="top">
                <el-button
                  :icon="Delete"
                  circle
                  size="small"
                  @click="clearChat"
                />
              </el-tooltip>


              <!-- 快捷提问按钮 -->
              <el-tooltip content="快捷提问" placement="top">
                <el-button
                  :icon="QuestionFilled"
                  circle
                  size="small"
                  @click="toggleQuickQuestions"
                  :class="{ 'is-active': showQuickQuestions }"
                />
              </el-tooltip>

              <!-- AI个性化数据开关 -->
              <el-tooltip content="开启后AI将使用您的个人数据提供个性化建议" placement="bottom">
                <el-switch
                  v-model="aiPersonalDataEnabled"
                  @change="handlePersonalDataToggle"
                  size="small"
                />
              </el-tooltip>
            </div>

            <div class="toolbar-right">
            </div>
          </div>

          <!-- 文本输入框 -->
          <div class="input-with-counter">
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
            <!-- 字数统计 -->
            <div class="char-count-wrapper">
              <span
                class="char-count"
                :class="{
                  'near-limit': inputMessage.length >= 450 && inputMessage.length < 500,
                  'at-limit': inputMessage.length >= 500
                }"
              >
                {{ inputMessage.length }}/500
              </span>
            </div>
          </div>
        </div>

        <el-button
          :type="isStreaming ? 'danger' : 'primary'"
          class="send-btn"
          @click="handleSendClick"
          :disabled="isLoading && !isStreaming"
          :loading="isLoading && !isStreaming"
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
  ChatDotRound,
  Close,
  ChatDotRound as ChatDotRoundIcon,
  Delete,
  Picture,
  ChatLineRound,
  DocumentCopy,
  More,
  Operation,
  QuestionFilled,
  ArrowRight
} from '@element-plus/icons-vue'
import { parseMarkdown } from '../../../../utils/markdownParser'
import axios from 'axios'
import { API_CONFIG } from '../../../../config/index'
import { useAuthStore } from '../../../../store/authStore'
import { useUserStore } from '../../../../store/userStore'
import CommonAvatar from '@/components/CommonAvatar.vue'

// 获取认证store
const authStore = useAuthStore()
// 获取用户store
const userStore = useUserStore()

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
const showQuickQuestions = ref(false)
const showEmojiPicker = ref(false)
const uploadedImages = ref([])

// 用户手动滚动标记
const userHasScrolled = ref(false)
let isAutoScrolling = false // 防止滚动时触发滚动事件

// AI个性化数据开关状态（隐私保护原则：默认未授权）
const aiPersonalDataEnabled = ref(false)

// 快捷问题分类列表（与后端Function Calling功能对应）
const quickQuestionCategories = ref([
  {
    id: 'dish-exploration',
    title: '🍽️ 菜品探索',
    expanded: true,
    questions: [
      "帮我搜索一些主食菜品",
      "有什么推荐的甜点吗",
      "搜索包含鸡肉的菜肴",
      "查看汤品分类的菜品"
    ]
  },
  {
    id: 'nutrition-analysis',
    title: '📊 营养分析',
    expanded: false,
    questions: [
      "分析西红柿炒鸡蛋的营养成分",
      "宫保鸡丁的热量是多少",
      "这份菜的蛋白质含量高吗",
      "分析这碗米饭的营养价值"
    ]
  },
  {
    id: 'order-management',
    title: '🛒 订单管理',
    expanded: false,
    questions: [
      "我要下单宫保鸡丁和红烧肉",
      "查询我的订单状态",
      "创建一个新订单",
      "我的订单配送到了吗"
    ]
  },
  {
    id: 'personalized-recommendation',
    title: '👤 个性化推荐',
    expanded: false,
    questions: [
      "根据我的喜好推荐菜品",
      "查看我的饮食偏好",
      "我最近都点了什么菜",
      "有什么适合我的健康菜品推荐"
    ]
  }
])

// 切换分类展开/折叠状态
const toggleCategory = (categoryId) => {
  const category = quickQuestionCategories.value.find(c => c.id === categoryId)
  if (category) {
    category.expanded = !category.expanded
  }
}

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
        avatar: item.sender === 'ai' ? '🤖' : (userStore.userInfo?.avatar || ''),
        enableMarkdown: true
      }))
      console.log('✅ 成功加载聊天历史:', messages.value.length, '条消息')
    } else {
      // 没有历史记录，显示欢迎消息并保存到后端
      console.log('📭 没有历史记录，显示欢迎消息')
      const welcomeMessage = '您好！我是您的AI饮食助手。😊\n我可以帮助您：\n- 推荐健康食谱\n- 分析营养成分\n- 提供饮食建议\n有什么可以帮您的吗？'
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
          enableMarkdown: true
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
        content: '您好！我是您的AI饮食助手。😊\n\n我可以帮助您：\n\n- 推荐健康食谱\n- 分析营养成分\n- 提供饮食建议\n\n有什么可以帮您的吗？',
        time: new Date().toLocaleTimeString([], {
          hour: '2-digit',
          minute: '2-digit'
        }),
        avatar: '🤖',
        enableMarkdown: true
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
        // console.log('🔍 收到原始行:', trimmedLine)  // 【调试日志】原始行内容

        if (!trimmedLine.startsWith('data:')) continue

        const data = trimmedLine.substring(5).trim()
        // console.log('📦 提取的data内容:', data)  // 【调试日志】提取的data
        if (!data) continue

        try {
          // 解析SSE数据（可能是数组格式或直接的对象）
          let parsedData

          if (data.startsWith('[')) {
            // console.log('🔧 检测到数组格式，尝试解析数组')  // 【调试日志】
            // Spring Boot的SseEmitter数组格式：[{...}, {...}, {...}]
            const dataArray = JSON.parse(data)
            // console.log('📊 解析后的数组:', dataArray)  // 【调试日志】

            // 查找data字段是对象类型（包含done或content）的元素
            const actualDataItem = dataArray.find(
              (item) => {
                // 检查data字段是否存在且是对象类型
                const itemData = item.data
                return itemData &&
                       typeof itemData === 'object' &&
                       (itemData.hasOwnProperty('done') || itemData.hasOwnProperty('content'))
              }
            )
            // console.log('🎯 找到的目标元素:', actualDataItem)  // 【调试日志】

            if (actualDataItem && actualDataItem.data) {
              parsedData = actualDataItem.data
            }
          } else {
            // console.log('🔧 检测到对象格式，直接解析')  // 【调试日志】
            // 直接的对象格式：{ content: string, done: boolean }
            parsedData = JSON.parse(data)
            // console.log('📊 解析后的对象:', parsedData)  // 【调试日志】
          }

          // console.log('✅ 最终解析结果:', parsedData)  // 【调试日志】

          if (!parsedData) {
            // console.log('⚠️ 解析结果为空，跳过此数据')
            continue
          }

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
            // console.log('📝 收到内容片段:', parsedData.content)  // 【调试日志】
            messages.value[messageIndex].content += parsedData.content
            // console.log('📊 当前消息总长度:', messages.value[messageIndex].content.length)  // 【调试日志】
            await nextTick()
            // 流式传输时不自动滚动,让用户控制查看位置
          } else {
            // console.log('⚠️ 没有content字段，parsedData:', parsedData)  // 【调试日志】
          }
        } catch (error) {
          console.log('⚠️ 跳过无效数据:', data, '错误:', error.message)  // 【调试日志】
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
    avatar: userStore.userInfo?.avatar || '', // 使用用户真实头像
    images: hasImages ? [...uploadedImages.value] : undefined
  }

  messages.value.push(userMessage)
  const userInput = message

  // 清空输入
  inputMessage.value = ''
  uploadedImages.value = []

  // 保存用户消息到后端
  await saveMessageToBackend('user', message)

  // 用户发送新消息时,重置滚动标志并强制滚动到底部
  userHasScrolled.value = false
  scrollToBottom(true)

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
    enableMarkdown: true
  })

  // 不自动滚动,让用户控制查看位置

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
        Accept: 'text/event-stream',
        'Authorization': `Bearer ${authStore.token}` // 添加JWT token
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
// 处理滚动事件,检测用户是否手动滚动
const handleScroll = () => {
  // 如果是自动滚动,不处理
  if (isAutoScrolling) {
    return
  }

  const container = chatContainerRef.value
  if (!container) {
    return
  }

  // 检查是否接近底部(阈值100px)
  const isNearBottom =
    container.scrollHeight - container.scrollTop - container.clientHeight < 100

  // 如果不在底部100px范围内,标记用户已手动滚动
  if (!isNearBottom) {
    userHasScrolled.value = true
  } else {
    // 如果用户在底部100px范围内,重置标志(允许自动滚动)
    userHasScrolled.value = false
  }
}

const scrollToBottom = (force = false) => {
  // 只有在强制滚动或用户未手动滚动时才自动滚动
  if (force || !userHasScrolled.value) {
    isAutoScrolling = true
    nextTick(() => {
      if (chatContainerRef.value) {
        chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
      }
      // 延迟重置标志,确保滚动事件不会误触发
      setTimeout(() => {
        isAutoScrolling = false
      }, 100)
    })
  }
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
  // 关闭快捷提问面板
  if (showEmojiPicker.value) {
    showQuickQuestions.value = false
  }
}

// 切换快捷提问面板
const toggleQuickQuestions = () => {
  showQuickQuestions.value = !showQuickQuestions.value
  // 关闭表情面板
  if (showQuickQuestions.value) {
    showEmojiPicker.value = false
  }
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

// 点击外部关闭表情面板和快捷提问面板
const handleClickOutside = (event) => {
  if (bottomContainerRef.value && !bottomContainerRef.value.contains(event.target)) {
    showEmojiPicker.value = false
    showQuickQuestions.value = false
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

// 复制消息
const copyMessage = async (content) => {
  try {
    // 优先尝试使用现代剪贴板API
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(content)
      ElMessage.success('复制成功')
      return
    }

    // Fallback: 使用传统方法创建临时文本区域
    const textArea = document.createElement('textarea')
    textArea.value = content
    textArea.style.position = 'fixed'
    textArea.style.left = '-999999px'
    textArea.style.top = '-999999px'
    document.body.appendChild(textArea)
    textArea.focus()
    textArea.select()

    try {
      const successful = document.execCommand('copy')
      if (successful) {
        ElMessage.success('复制成功')
      } else {
        throw new Error('execCommand failed')
      }
    } finally {
      document.body.removeChild(textArea)
    }
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败,请手动复制')
  }
}

// 处理消息操作菜单命令
const handleMessageAction = async (command, content) => {
  switch (command) {
    case 'copy':
      await copyMessage(content)
      break
    // 可以在这里添加更多操作
  }
}

// 生命周期
onMounted(async () => {
  document.addEventListener('click', handleClickOutside)

  // 添加滚动事件监听器
  if (chatContainerRef.value) {
    chatContainerRef.value.addEventListener('scroll', handleScroll)
  }

  // 加载聊天历史记录
  await loadMessages()
  // 加载用户偏好设置
  await loadUserPreference()
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)

  // 移除滚动事件监听器
  if (chatContainerRef.value) {
    chatContainerRef.value.removeEventListener('scroll', handleScroll)
  }
})
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

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #909399;

    p {
      margin: 12px 0 0 0;
      font-size: 1.143rem /* 原值: 16px */;
    }

    .hint {
      font-size: 1rem /* 原值: 14px */;
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

    // 自定义头像组件样式
    .message-avatar-custom {
      flex-shrink: 0;
      filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.15));

      :deep(.avatar-container) {
        padding: 4px; /* 减小padding以匹配42px尺寸 */
      }

      :deep(.user-avatar) {
        border-width: 2px; /* 减小边框宽度 */
      }
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
        line-height: 1.37; /* 进一步减小行高，让换行更紧凑 */
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        font-size: 0.929rem /* 原值: 15px，调整为13px */;
        white-space: pre-wrap;
        word-break: break-word;
        display: inline;
        position: relative;

        .message-text-content {
          display: inline;
        }

        // 更多操作按钮（内联在文本末尾）
        :deep(.el-dropdown) {
          display: inline;
          vertical-align: middle;
        }

        .more-btn {
          display: inline;
          cursor: pointer;
          opacity: 0;
          transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1);
          color: inherit;
          vertical-align: middle;
          margin-left: 2px;
          user-select: none;

          &:active {
            transform: scale(0.95);
          }
        }

        &:hover .more-btn {
          opacity: 0.5;
        }

        .more-btn:hover {
          opacity: 1 !important;
        }

        // 下拉菜单样式
        :deep(.el-dropdown-menu__item) {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px 16px;

          .el-icon {
            font-size: 1.143rem /* 原值: 16px */;
          }
        }

        &:hover {
          transform: translateY(-2px) scale(1.01);
        }

        // Markdown样式
        &.markdown-content {
          // 极简换行符间距
          :deep(br) {
            display: block;
            content: '';
            margin: 0; /* 完全去除<br>的额外间距 */
          }

          // 标题
          :deep(h1), :deep(h2), :deep(h3) {
            margin: 8px 0 6px 0; /* 减小标题间距 */
            font-weight: 600;
            line-height: 1.5;
          }

          :deep(h1) { font-size: 1.286rem ; }
          :deep(h2) { font-size: 1.143rem ; }
          :deep(h3) { font-size: 1rem ; }

          &:first-child {
            margin-top: 0;
          }

          // 段落
          :deep(p) {
            margin: 2px 0; /* 进一步减小段落间距 */
            line-height: 1.3; /* 与基础行高保持一致 */
          }
        }

        // 代码块
        :deep(pre) {
          background: rgba(0, 0, 0, 0.05);
          border-radius: 8px;
          padding: 8px; /* 减小内边距 */
          margin: 6px 0; /* 减小外边距 */
          overflow-x: auto;

          code {
            font-family: 'Courier New', monospace;
            font-size: 0.857rem /* 原值: 13px，改为12px */;
            line-height: 1.4; /* 减小行高 */
            color: #333;
          }
        }

        // 行内代码
        :deep(code:not(pre code)) {
          background: rgba(0, 0, 0, 0.05);
          padding: 2px 5px; /* 稍微减小内边距 */
          border-radius: 4px;
          font-family: 'Courier New', monospace;
          font-size: 0.85em; /* 稍微减小行内代码 */
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
          margin: 2px 0; /* 进一步减小列表间距 */
          padding-left: 18px; /* 稍微减小缩进 */
          line-height: 1.25; /* 与基础行高保持一致 */
        }

        :deep(li) {
          margin: 1px 0; /* 进一步减小列表项间距 */
        }

        // 换行
        :deep(br) {
          line-height: 1.2; /* 减小空行高度 */
        }
      }

      .message-time {
        font-size: 0.857rem /* 原值: 12px */;
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
  position: relative; /* 为绝对定位的 emoji panel 提供定位上下文 */
}

// 快捷提问悬浮面板
.quick-questions-panel {
  position: absolute;
  bottom: 100%;
  right: 0;
  margin-bottom: 8px;
  background: #ffffff;
  border: 1px solid #e8ecef;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  max-width: 320px;
  max-height: 400px;
  overflow-y: auto;
  z-index: 1000; /* 与 emoji panel 相同层级 */

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: #dee2e6;
    border-radius: 2px;

    &:hover {
      background: #adb5bd;
    }
  }

  .quick-questions-title {
    font-size: 0.929rem /* 原值: 13px */;
    font-weight: 600;
    color: #606266;
    margin-bottom: 10px;
    padding-bottom: 8px;
    border-bottom: 1px solid #e8ecef;
  }

  .quick-questions-categories {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .question-category {
      border: 1px solid #e8ecef;
      border-radius: 6px;
      overflow: hidden;
      transition: all 0.2s ease;

      &:hover {
        border-color: #ff6b6b;
      }

      .category-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 8px 12px;
        background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
        cursor: pointer;
        user-select: none;
        transition: all 0.2s ease;

        &:hover {
          background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
        }

        .category-title {
          font-size: 0.857rem /* 原值: 12px */;
          font-weight: 600;
          color: #606266;
        }

        .category-arrow {
          font-size: 0.857rem /* 原值: 12px */;
          color: #909399;
          transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

          &.is-expanded {
            transform: rotate(90deg);
          }
        }
      }

      .category-questions {
        padding: 6px;
        background: #ffffff;
        display: flex;
        flex-direction: column;
        gap: 4px;

        .question-item {
          padding: 8px 10px;
          font-size: 0.786rem /* 原值: 11px */;
          color: #606266;
          background: #f5f7fa;
          border-radius: 4px;
          cursor: pointer;
          transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
          text-align: left;
          line-height: 1.4;

          &:hover {
            background: linear-gradient(135deg, #409eff 0%, #5dade2 100%);
            color: #ffffff;
            transform: translateX(2px);
          }

          &:active {
            transform: translateX(1px);
          }
        }
      }
    }
  }
}

// 分类展开/折叠动画
.category-slide-enter-active,
.category-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.category-slide-enter-from,
.category-slide-leave-to {
  max-height: 0;
  opacity: 0;
}

.category-slide-enter-to,
.category-slide-leave-from {
  max-height: 300px;
  opacity: 1;
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
  position: absolute;
  bottom: 100%;
  left: 0;
  margin-bottom: 8px;
  background: #ffffff;
  border: 1px solid #e8ecef;
  border-radius: 8px;
  padding: 10px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  max-height: 180px;
  overflow-y: auto;
  z-index: 1000; /* 提高 z-index 确保浮在最上层 */

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
      font-size: 1.429rem /* 原值: 20px */;
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
  border-radius: 16px;
  padding: 16px 18px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
    border-color: #e0e4e8;
  }

  .input-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 2px;
    margin-bottom: 6px;

    .toolbar-left {
      display: flex;
      gap: 8px;
      align-items: center;
    }

    .input-with-counter {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-top: 8px;
    }

    .toolbar-divider {
      width: 1px;
      height: 20px;
      background: linear-gradient(to bottom, transparent, #e8ecef, transparent);
      margin: 0 2px;
    }

    // 开关样式优化
    :deep(.el-switch) {
      --el-switch-on-color: #ff6b6b;
      --el-switch-off-color: #dcdfe6;

      &.el-switch--small {
        height: 20px;

        .el-switch__core {
          height: 20px;
          min-width: 40px;
          border-radius: 10px;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

          &::after {
            width: 16px;
            height: 16px;
            top: 1px;
            left: 1px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          }

          &.is-checked::after {
            left: calc(100% - 17px);
          }
        }

        &:hover .el-switch__core {
          transform: scale(1.02);
        }
      }

      .el-switch__action {
        background-color: #fff;
      }

      .el-switch__label {
        font-size: 0.857rem /* 原值: 12px */;
        font-weight: 600;
        color: #606266;

        &.is-active {
          color: #ff6b6b;
        }
      }
    }

    :deep(.el-button) {
      border: 1px solid #e8ecef;
      background: #ffffff;
      color: #5a6c7d;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      font-weight: 500;
      width: 32px;
      height: 32px;
      padding: 0;

      &:hover {
        border-color: #ff6b6b;
        color: #ff6b6b;
        transform: translateY(-2px) scale(1.05);
        box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
        background: #fff;
      }

      &:active {
        transform: translateY(0) scale(1);
      }

      &.is-active {
        border-color: #ff6b6b;
        color: #ff6b6b;
        background: linear-gradient(
          135deg,
          rgba(255, 107, 107, 0.1) 0%,
          rgba(255, 82, 82, 0.1) 100%
        );
        box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.15),
          0 2px 8px rgba(255, 107, 107, 0.2);

        &:hover {
          background: linear-gradient(
            135deg,
            rgba(255, 107, 107, 0.15) 0%,
            rgba(255, 82, 82, 0.15) 100%
          );
        }
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
      font-size: 1rem /* 原值: 14px */;
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

  // 字数统计包装器
  .char-count-wrapper {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    padding: 0 2px;
    margin-top: 4px;

    .char-count {
      font-size: 0.857rem /* 原值: 12px */;
      color: #909399;
      padding: 4px 12px;
      background: linear-gradient(135deg, #f5f7fa 0%, #eef1f6 100%);
      border-radius: 12px;
      font-weight: 600;
      border: 1px solid #e8ecef;
      transition: all 0.3s ease;
      display: inline-block;
      user-select: none;

      &:hover {
        background: linear-gradient(135deg, #eef1f6 0%, #e8ebf1 100%);
        border-color: #d0d7de;
        transform: translateY(-1px);
      }

      // 接近上限时的样式
      &.near-limit {
        color: #e6a23c;
        background: linear-gradient(135deg, #fef0e6 0%, #fde6d3 100%);
        border-color: #f5dab1;
      }

      // 达到上限时的样式
      &.at-limit {
        color: #f56c6c;
        background: linear-gradient(135deg, #fee 0%, #fecaca 100%);
        border-color: #fbc4c4;
        animation: pulse 1.5s ease-in-out infinite;
      }
    }
  }

  // 字数接近上限时的脉冲动画
  @keyframes pulse {
    0%, 100% {
      opacity: 1;
    }
    50% {
      opacity: 0.7;
    }
  }

  .send-btn {
    flex-shrink: 0;
    background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
    border: none;
    padding: 10px 28px;
    font-size: 1.071rem /* 原值: 15px */;
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
