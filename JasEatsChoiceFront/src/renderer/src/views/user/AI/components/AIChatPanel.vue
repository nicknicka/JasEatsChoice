<template>
  <div class="chat-content-wrapper">
    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="chatContainerRef">
      <ChatMessage
        v-for="message in messages"
        :key="message.id"
        v-bind="message"
      />
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
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import ChatMessage from './ChatMessage.vue'
import QuickQuestions from './QuickQuestions.vue'
import MessageInput from './MessageInput.vue'
import { useAIChat } from '../../../composables/useAIChat'
import { useUserPreference } from '../../../composables/useUserPreference'
import { useImageUpload } from '../../../composables/useImageUpload'
import { QUICK_QUESTIONS, COMMON_EMOJIS, ERROR_MESSAGES, logger } from '../../../config/chatConfig'

// 聊天功能
const {
  messages,
  isLoading,
  isStreaming,
  chatContainerRef,
  loadMessages,
  sendMessage: sendChatMessage,
  clearChat,
  stopStreaming
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
const inputContainerRef = ref(null)
const quickQuestions = ref(QUICK_QUESTIONS)
const commonEmojis = ref(COMMON_EMOJIS)

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
    // 聚焦回输入框
    const textarea = document.querySelector('.message-textarea textarea')
    if (textarea) textarea.focus()
  })
}

/**
 * 清空输入
 */
const clearInput = () => {
  inputMessage.value = ''
  // 注意：不清空图片，只清空文本
  ElMessage.success(ERROR_MESSAGES.CLEARED)
}

/**
 * 点击外部区域关闭表情面板
 */
const handleClickOutside = (event) => {
  // 简单的点击外部关闭逻辑
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
  // 自动发送
  sendMessage()
}

/**
 * 处理图片上传
 */
const handleImageUpload = (file, event) => {
  uploadImage(file, null, (error) => {
    logger.error('图片上传失败:', error)
  })
  // 清空input，允许重复上传
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

  // 清空输入
  inputMessage.value = ''

  // 发送消息
  await sendChatMessage(message)
}

// 生命周期
onMounted(async () => {
  // 加载聊天历史
  await loadMessages()
  // 加载用户偏好
  await loadUserPreference()
  // 添加点击外部监听
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
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
}

/* 底部输入容器 */
.bottom-input-container {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
</style>
