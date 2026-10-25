<template>
  <div class="message-input-container" ref="containerRef">
    <!-- 引用预览 -->
    <ReplyPreview :replying-to="replyingTo" @cancel="$emit('cancel-reply')" />

    <div class="input-wrapper">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-tooltip content="表情" placement="top">
            <el-button
              :icon="ChatDotRound"
              circle
              size="small"
              @click="toggleEmoji"
              :class="{ 'is-active': showEmoji }"
            />
          </el-tooltip>
          <el-tooltip content="上传图片" placement="top">
            <el-button :icon="Picture" circle size="small" @click="handleImageUpload" />
          </el-tooltip>
          <el-tooltip content="上传文件" placement="top">
            <el-button :icon="FolderOpened" circle size="small" @click="handleFileUpload" />
          </el-tooltip>
          <div class="toolbar-divider"></div>
          <el-tooltip content="@提醒" placement="top">
            <el-button
              circle
              size="small"
              @click="handleMention"
              :class="{ 'is-active': showMentionPanel }"
            >@</el-button>
          </el-tooltip>
        </div>
        <div class="toolbar-right">
          <el-tooltip content="清空" placement="top">
            <el-button :icon="Delete" circle size="small" @click="clearInput" />
          </el-tooltip>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="textarea-wrapper" :class="{ 'focused': isFocused }">
        <el-input
          ref="textareaRef"
          v-model="inputValue"
          type="textarea"
          placeholder="输入消息... (Enter发送，Shift+Enter换行)"
          :autosize="{ minRows: 1, maxRows: 5 }"
          @keydown.enter.exact.prevent="handleSend"
          :disabled="disabled"
          @focus="isFocused = true"
          @blur="isFocused = false"
          class="message-textarea"
        />

        <!-- 字符计数 -->
        <div class="char-counter" :class="{ 'warning': isNearLimit, 'danger': isAtLimit }">
          {{ inputValue.length }}/{{ maxLength }}
        </div>
      </div>

      <!-- 表情面板 -->
      <transition name="slide-up">
        <div v-if="showEmoji" class="emoji-panel">
          <div class="emoji-grid">
            <span
              v-for="emoji in commonEmojis"
              :key="emoji"
              class="emoji-item"
              @click="insertEmoji(emoji)"
            >
              {{ emoji }}
            </span>
          </div>
        </div>
      </transition>

      <!-- 发送按钮 -->
      <div class="send-wrapper">
        <el-button
          type="primary"
          @click="handleSend"
          :disabled="disabled || !inputValue.trim() || isSending"
          :loading="isSending"
          :icon="isSending ? undefined : Promotion"
          size="default"
          class="send-button"
          :class="{ 'send-success': showSuccessAnimation }"
        >
          {{ isSending ? '发送中' : showSuccessAnimation ? '已发送' : '发送' }}
        </el-button>
      </div>
    </div>

    <!-- 隐藏的文件输入 -->
    <input
      ref="imageInputRef"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleImageSelected"
    />
    <input
      ref="fileInputRef"
      type="file"
      style="display: none"
      @change="handleFileSelected"
    />
  </div>
</template>

<script setup>
import { ref, nextTick, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  Picture,
  FolderOpened,
  Delete,
  Promotion
} from '@element-plus/icons-vue'
import ReplyPreview from './ReplyPreview.vue'
import chatApi from '@/api/chat'

const props = defineProps({
  replyingTo: {
    type: Object,
    default: null
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['send', 'cancel-reply', 'send-image', 'send-file'])

const inputValue = ref('')
const showEmoji = ref(false)
const showMentionPanel = ref(false)
const textareaRef = ref(null)
const imageInputRef = ref(null)
const fileInputRef = ref(null)
const containerRef = ref(null)
const isFocused = ref(false)
const isSending = ref(false)
const showSuccessAnimation = ref(false)

// 字符计数相关
const maxLength = 500 // 最大字符数

const isNearLimit = computed(() => {
  return inputValue.value.length >= maxLength * 0.9 && inputValue.value.length < maxLength
})

const isAtLimit = computed(() => {
  return inputValue.value.length >= maxLength
})

// 监听输入，超过最大长度时截断
watch(inputValue, (newVal) => {
  if (newVal.length > maxLength) {
    inputValue.value = newVal.slice(0, maxLength)
    ElMessage.warning(`消息长度不能超过${maxLength}个字符`)
  }
})

// 常用表情
const commonEmojis = [
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂',
  '🙂', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗',
  '😚', '😙', '🥲', '😋', '😛', '😜', '🤪', '😝',
  '🤗', '🤭', '🫢', '🫣', '🤔', '🫡', '😶', '😐',
  '👍', '👎', '👌', '✌️', '🤞', '🤝', '🙏', '💪',
  '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍',
  '🎉', '🎊', '🎈', '🎁', '🏆', '⭐', '✨', '💫'
]

const handleSend = async () => {
  if (!inputValue.value.trim() || isSending.value) return

  isSending.value = true
  try {
    emit('send', inputValue.value.trim())
    inputValue.value = ''
    closeAllPanels() // 关闭所有面板

    // 显示成功动画
    showSuccessAnimation.value = true
    setTimeout(() => {
      showSuccessAnimation.value = false
    }, 1500)
  } finally {
    isSending.value = false
  }
}

const clearInput = () => {
  inputValue.value = ''
  closeAllPanels()
  ElMessage.info('已清空输入内容')
}

// 关闭所有面板
const closeAllPanels = () => {
  showEmoji.value = false
  showMentionPanel.value = false
}

const toggleEmoji = () => {
  // 如果当前表情面板已打开，则关闭它
  // 如果当前是其他面板打开，先关闭其他面板，再打开表情面板
  if (showEmoji.value) {
    showEmoji.value = false
  } else {
    closeAllPanels()
    showEmoji.value = true
  }
  nextTick(() => {
    textareaRef.value?.focus()
  })
}

const insertEmoji = (emoji) => {
  if (inputValue.value.length >= maxLength) {
    ElMessage.warning(`已达到最大字符限制（${maxLength}个字符）`)
    return
  }
  inputValue.value += emoji
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.focus()
    }
  })
}

const handleImageUpload = () => {
  closeAllPanels() // 关闭所有工具栏面板
  imageInputRef.value?.click()
}

const handleFileUpload = () => {
  closeAllPanels() // 关闭所有工具栏面板
  fileInputRef.value?.click()
}

const handleImageSelected = async (event) => {
  const file = event.target.files?.[0]
  if (file) {
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('图片大小不能超过5MB')
      return
    }

    try {
      ElMessage.info('正在上传图片...')
      const response = await chatApi.uploadChatImage(file)

      if (response && response.code === '200') {
        const fileInfo = {
          fileUrl: response.data.fileUrl,
          fileName: response.data.fileName,
          fileSize: response.data.fileSize,
          fileType: response.data.fileType,
          fullUrl: response.data.fullUrl
        }
        emit('send-image', fileInfo)
      } else {
        ElMessage.error(response?.message || '图片上传失败')
      }
    } catch (error) {
      console.error('上传图片失败:', error)
      ElMessage.error('图片上传失败，请重试')
    }
  }
  // 重置input，以便可以重复选择同一文件
  event.target.value = ''
}

const handleFileSelected = async (event) => {
  const file = event.target.files?.[0]
  if (file) {
    if (file.size > 10 * 1024 * 1024) {
      ElMessage.error('文件大小不能超过10MB')
      return
    }

    try {
      ElMessage.info('正在上传文件...')
      const response = await chatApi.uploadChatFile(file)

      if (response && response.code === '200') {
        const fileInfo = {
          fileUrl: response.data.fileUrl,
          fileName: response.data.fileName,
          fileSize: response.data.fileSize,
          fileType: response.data.fileType,
          fullUrl: response.data.fullUrl
        }
        emit('send-file', fileInfo)
      } else {
        ElMessage.error(response?.message || '文件上传失败')
      }
    } catch (error) {
      console.error('上传文件失败:', error)
      ElMessage.error('文件上传失败，请重试')
    }
  }
  event.target.value = ''
}

const handleMention = () => {
  // @提醒功能 - 与其他面板互斥
  if (showMentionPanel.value) {
    showMentionPanel.value = false
  } else {
    closeAllPanels()
    showMentionPanel.value = true
  }
  // 这里可以扩展@提醒面板的逻辑
  ElMessage.info('@提醒功能开发中')
}

// 点击外部区域关闭所有面板
const handleClickOutside = (event) => {
  if (containerRef.value && !containerRef.value.contains(event.target)) {
    closeAllPanels()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

// 暴露方法供父组件调用
defineExpose({
  focus: () => {
    nextTick(() => {
      textareaRef.value?.focus()
    })
  },
  clear: () => {
    inputValue.value = ''
  }
})
</script>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.message-input-container {
  padding: 10px 14px 14px;
  background: linear-gradient(180deg, #fffdfb 0%, #f9f5ef 100%);
  border-top: 1px solid @nordic-border;
  display: flex;
  flex-direction: column;
  gap: 8px;
  box-shadow: 0 -10px 24px rgba(105, 78, 57, 0.05);

  .input-wrapper {
    display: flex;
    flex-direction: column;
    gap: 6px;
    position: relative;

    .toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 2px;

      .toolbar-left,
      .toolbar-right {
        display: flex;
        gap: 6px;
        align-items: center;
      }

      .toolbar-divider {
        width: 1px;
        height: 16px;
        background: @nordic-border;
        margin: 0 4px;
      }

      :deep(.el-button) {
        border: 1px solid fade(@nordic-border, 92%);
        background: rgba(255, 255, 255, 0.9);
        color: @nordic-text-secondary;
        transition: all @nordic-transition-fast ease;
        font-weight: 500;
        box-shadow: 0 6px 14px rgba(117, 82, 55, 0.05);

        &:hover {
          border-color: fade(@nordic-accent, 34%);
          color: @nordic-accent-dark;
          transform: translateY(-1px);
          box-shadow: 0 10px 18px fade(@nordic-accent, 12%);
          background: #fff9f4;
        }

        &:active {
          transform: translateY(0);
        }

        &.is-active {
          border-color: fade(@nordic-accent, 38%);
          color: @nordic-accent-dark;
          background: #f8ece2;
          box-shadow:
            0 0 0 3px fade(@nordic-accent-light, 38%),
            0 8px 16px fade(@nordic-accent, 12%);

          &:hover {
            background: #f5e6da;
          }
        }
      }
    }

    .textarea-wrapper {
      position: relative;

      &.focused {
        .message-textarea :deep(.el-textarea__inner) {
          border-color: fade(@nordic-accent, 42%);
          box-shadow:
            0 0 0 3px fade(@nordic-accent-light, 42%),
            0 10px 18px rgba(117, 82, 55, 0.07);
        }
      }

      .message-textarea {
        :deep(.el-textarea__inner) {
          border-radius: 16px;
          border: 1px solid @nordic-border;
          background: #ffffff;
          padding: 12px 14px;
          padding-right: 80px; // 为字符计数留空间
          font-size: @nordic-text-base;
          line-height: 1.6;
          min-height: 42px;
          transition: all @nordic-transition-fast ease;
          resize: none;
          box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.82);

          &:focus {
            border-color: fade(@nordic-accent, 42%);
            box-shadow:
              0 0 0 3px fade(@nordic-accent-light, 42%),
              0 10px 18px rgba(117, 82, 55, 0.07);
            background: #ffffff;
          }

          &:hover:not(:focus) {
            border-color: darken(@nordic-border, 8%);
          }

          &::placeholder {
            color: @nordic-text-muted;
            font-size: @nordic-text-sm;
          }
        }
      }

      .char-counter {
        position: absolute;
        right: 12px;
        bottom: 8px;
        font-size: @nordic-text-xs;
        color: @nordic-text-muted;
        background: #ffffff;
        padding: 2px 8px;
        border-radius: 999px;
        transition: all @nordic-transition-fast ease;
        pointer-events: none;
        border: 1px solid fade(@nordic-border, 85%);

        &.warning {
          color: @nordic-yellow-dark;
          font-weight: 500;
        }

        &.danger {
          color: @nordic-red;
          font-weight: 600;
          animation: pulse 1s ease-in-out infinite;
        }
      }
    }

    .emoji-panel {
      position: absolute;
      bottom: 100%;
      left: 0;
      right: 0;
      background: #fffdfa;
      border: 1px solid @nordic-border;
      border-radius: 16px;
      padding: 10px;
      box-shadow: 0 18px 32px rgba(105, 78, 57, 0.12);
      margin-bottom: 6px;
      max-height: 180px;
      overflow-y: auto;
      z-index: 100;

      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-thumb {
        background: darken(@nordic-border, 10%);
        border-radius: 3px;

        &:hover {
          background: darken(@nordic-border, 20%);
        }
      }

      .emoji-grid {
        display: grid;
        grid-template-columns: repeat(10, 1fr);
        gap: 4px;

        .emoji-item {
          font-size: 1.429rem /* 原值: 20px */;
          text-align: center;
          padding: 6px 4px;
          border-radius: 10px;
          cursor: pointer;
          transition: all @nordic-transition-fast ease;
          user-select: none;

          &:hover {
            background: #f8ede3;
            transform: scale(1.1);
          }

          &:active {
            transform: scale(1.05);
          }
        }
      }
    }

    .send-wrapper {
      display: flex;
      justify-content: flex-end;

      .send-button {
        background: linear-gradient(180deg, @nordic-accent 0%, @nordic-accent-dark 100%);
        border: none;
        padding: 8px 22px;
        font-size: @nordic-text-base;
        font-weight: 600;
        border-radius: 14px;
        box-shadow: 0 14px 24px fade(@nordic-accent, 18%);
        transition: all @nordic-transition-fast ease;

        &:hover:not(:disabled) {
          transform: translateY(-1px);
          box-shadow: 0 18px 28px fade(@nordic-accent, 24%);
        }

        &:active:not(:disabled) {
          transform: translateY(0);
        }

        &:disabled {
          background: #e9ecef;
          box-shadow: none;
          color: @nordic-text-muted;
        }

        &.send-success {
          background: linear-gradient(180deg, @nordic-green 0%, @nordic-green-dark 100%);
          box-shadow: 0 12px 22px fade(@nordic-green, 20%);
        }
      }

      :deep(.el-button--primary) {
        background: linear-gradient(180deg, @nordic-accent 0%, @nordic-accent-dark 100%);
        border: none;
        padding: 8px 22px;
        font-size: @nordic-text-base;
        font-weight: 600;
        border-radius: 14px;
        box-shadow: 0 14px 24px fade(@nordic-accent, 18%);
        transition: all @nordic-transition-fast ease;

        &:hover:not(:disabled) {
          transform: translateY(-1px);
          box-shadow: 0 18px 28px fade(@nordic-accent, 24%);
          background: linear-gradient(180deg, lighten(@nordic-accent, 3%) 0%, @nordic-accent-dark 100%);
        }

        &:active:not(:disabled) {
          transform: translateY(0);
        }

        &:disabled {
          background: #e9ecef;
          box-shadow: none;
        }
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

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
</style>
