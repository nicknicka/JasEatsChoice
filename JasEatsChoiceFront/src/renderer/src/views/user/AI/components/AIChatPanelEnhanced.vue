<template>
  <ErrorBoundary
    :title="'AI聊天功能异常'"
    :error-message="'聊天功能遇到问题，请重试'"
    @error="handleChatError"
  >
    <div class="chat-content-wrapper">
      <!-- 聊天消息区域 - 使用虚拟滚动优化长列表 -->
      <div class="chat-messages" ref="chatContainerRef">
        <!-- 加载骨架屏 -->
        <ChatSkeleton v-if="isLoading && messages.length === 0" :count="5" />

        <!-- 使用虚拟滚动（消息数量超过50条时启用） -->
        <VirtualList
          v-else-if="messages.length > 50"
          :data="messages"
          :estimated-item-height="120"
          :key-field="'id'"
        >
          <template #default="{ item }">
            <ChatMessageEnhanced
              v-bind="item"
              :render-markdown="true"
            />
          </template>
        </VirtualList>

        <!-- 普通列表（消息数量较少时） -->
        <template v-else>
          <ChatMessageEnhanced
            v-for="message in messages"
            :key="message.id"
            v-bind="message"
            :render-markdown="true"
          />
        </template>

        <!-- 空状态 -->
        <div v-if="!isLoading && messages.length === 0" class="empty-state">
          <el-icon :size="64" color="#dcdfe6"><ChatDotRound /></el-icon>
          <p>暂无消息</p>
          <p class="empty-hint">开始对话吧！</p>
        </div>
      </div>

      <!-- 底部输入区域容器 -->
      <div class="bottom-input-container">
        <!-- 快捷提问区域 -->
        <QuickQuestions
          :show="showQuickQuestions"
          :questions="quickQuestions"
          @close="showQuickQuestions = false"
          @select="handleQuickQuestion"
        />

        <!-- 输入框区域 -->
        <MessageInput
          v-model="inputMessage"
          :show-emoji-picker="showEmojiPicker"
          :show-quick-questions="showQuickQuestions"
          :uploaded-images="uploadedImages"
          :emojis="commonEmojis"
          :is-streaming="isStreaming"
          :is-loading="isLoading"
          :personal-data-enabled="aiPersonalDataEnabled"
          @toggle-emoji="toggleEmoji"
          @clear-input="clearInput"
          @clear-chat="clearChat"
          @show-quick-questions="showQuickQuestions = true"
          @select-emoji="selectEmoji"
          @remove-image="removeUploadedImage"
          @send="sendMessage"
          @stop-streaming="stopStreaming"
          @toggle-personal-data="handlePersonalDataToggle"
          @upload-image="handleImageUpload"
        />
      </div>
    </div>
  </ErrorBoundary>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import ErrorBoundary from '../../../components/common/ErrorBoundary'
import VirtualList from '../../../components/common/VirtualList'
import ChatMessageEnhanced from './ChatMessageEnhanced.vue'
import ChatSkeleton from './ChatSkeleton.vue'
import QuickQuestions from './QuickQuestions.vue'
import MessageInput from './MessageInput.vue'
import { useAIChat } from '../../../composables/useAIChat'
import { useUserPreference } from '../../../composables/useUserPreference'
import { useImageUpload } from '../../../composables/useImageUpload'
import { QUICK_QUESTIONS, COMMON_EMOJIS, ERROR_MESSAGES, logger } from '../../../config/chatConfig'
import { rafThrottle } from '../../../utils/performanceUtils'

// 聊天功能
const {
  messages,
  isLoading,
  isStreaming,
  chatContainerRef,
  loadMessages,
  sendMessage: sendChatMessage,
  clearChat,
  stopStreaming,
  scrollToBottom
} = useAIChat()

// 用户偏好
const { aiPersonalDataEnabled, loadUserPreference, handlePersonalDataToggle } = useUserPreference()

// 图片上传
const {
  uploadedImages,
  handleImageUpload: uploadImage,
  removeUploadedImage
} = useImageUpload()

// 本地状态
const inputMessage = ref('')
const showQuickQuestions = ref(true)
const showEmojiPicker = ref(false)
const quickQuestions = ref(QUICK_QUESTIONS)
const commonEmojis = ref(COMMON_EMOJIS)

// 是否自动滚动到底部
const shouldAutoScroll = ref(true)

// 监听消息变化，自动滚动
watch(
  () => messages.value,
  () => {
    if (shouldAutoScroll.value) {
      scrollToBottom(true)
    }
  },
  { deep: true }
)

// 监听用户滚动，决定是否继续自动滚动
const handleUserScroll = rafThrottle(() => {
  if (!chatContainerRef.value) return

  const { scrollTop, scrollHeight, clientHeight } = chatContainerRef.value
  const distanceToBottom = scrollHeight - scrollTop - clientHeight

  // 如果距离底部小于100px，继续自动滚动
  shouldAutoScroll.value = distanceToBottom < 100
})

/**
 * 切换表情面板
 */
const toggleEmoji = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

/**
 * 选择表情
 */
const selectEmoji = (emoji) => {
  inputMessage.value += emoji
  showEmojiPicker.value = false
  nextTick(() => {
    const textarea = document.querySelector('.message-textarea textarea')
    if (textarea) textarea.focus()
  })
}

/**
 * 清空输入
 */
const clearInput = () => {
  inputMessage.value = ''
  ElMessage.success(ERROR_MESSAGES.CLEARED)
}

/**
 * 点击外部区域关闭表情面板
 */
const handleClickOutside = (event) => {
  const emojiPanel = document.querySelector('.emoji-panel')
  const emojiButton = document.querySelector('[class*="is-active"]')

  if (emojiPanel && !emojiPanel.contains(event.target) && !emojiButton?.contains(event.target)) {
    showEmojiPicker.value = false
  }
}

/**
 * 处理快捷提问
 */
const handleQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

/**
 * 处理图片上传
 */
const handleImageUpload = (file, event) => {
  uploadImage(file, null, (error) => {
    logger.error('图片上传失败:', error)
  })
  if (event && event.target) {
    event.target.value = ''
  }
}

/**
 * 发送消息
 */
const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message) {
    ElMessage.warning('请输入问题')
    return
  }

  inputMessage.value = ''
  shouldAutoScroll.value = true
  await sendChatMessage(message)
}

/**
 * 错误处理
 */
const handleChatError = (error) => {
  logger.error('AI聊天错误:', error)
  ElMessage.error('聊天功能异常，请刷新页面重试')
}

// 生命周期
onMounted(async () => {
  await loadMessages()
  await loadUserPreference()
  document.addEventListener('click', handleClickOutside)
  if (chatContainerRef.value) {
    chatContainerRef.value.addEventListener('scroll', handleUserScroll)
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  if (chatContainerRef.value) {
    chatContainerRef.value.removeEventListener('scroll', handleUserScroll)
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
  gap: 8px;
  overflow: hidden;
  min-height: 0;
  box-sizing: border-box;
}

.chat-messages {
  width: 100%;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  background-color: #fff;
  border-radius: 16px;
  padding: 24px;
  box-sizing: border-box;
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
      font-size: 16px;
    }

    .empty-hint {
      font-size: 14px;
      color: #c0c4cc;
    }
  }
}

/* 底部输入容器 */
.bottom-input-container {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
</style>
