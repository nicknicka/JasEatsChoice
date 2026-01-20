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

const emit = defineEmits(['send', 'cancel-reply'])

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

const handleImageSelected = (event) => {
  const file = event.target.files?.[0]
  if (file) {
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('图片大小不能超过5MB')
      return
    }

    // 这里可以添加上传图片的逻辑
    ElMessage.success('图片已选择，准备上传')
    console.log('选择的图片:', file.name)
  }
  // 重置input，以便可以重复选择同一文件
  event.target.value = ''
}

const handleFileSelected = (event) => {
  const file = event.target.files?.[0]
  if (file) {
    if (file.size > 10 * 1024 * 1024) {
      ElMessage.error('文件大小不能超过10MB')
      return
    }

    // 这里可以添加上传文件的逻辑
    ElMessage.success('文件已选择，准备上传')
    console.log('选择的文件:', file.name)
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
.message-input-container {
  padding: 8px 12px;
  background: linear-gradient(to bottom, #ffffff 0%, #fafbfc 100%);
  border-top: 1px solid #e8ecef;
  display: flex;
  flex-direction: column;
  gap: 8px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);

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
          background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
          box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15),
                      0 2px 8px rgba(102, 126, 234, 0.2);

          &:hover {
            background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
          }
        }
      }
    }

    .textarea-wrapper {
      position: relative;

      &.focused {
        .message-textarea :deep(.el-textarea__inner) {
          border-color: #667eea;
          box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12),
                      0 0 12px rgba(102, 126, 234, 0.08);
        }
      }

      .message-textarea {
        :deep(.el-textarea__inner) {
          border-radius: 10px;
          border: 2px solid #e8ecef;
          background: #ffffff;
          padding: 8px 12px;
          padding-right: 80px; // 为字符计数留空间
          font-size: 14px;
          line-height: 1.6;
          min-height: 32px;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          resize: none;

          &:focus {
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12),
                        0 0 12px rgba(102, 126, 234, 0.08);
            background: #ffffff;
          }

          &:hover:not(:focus) {
            border-color: #d0d7de;
          }

          &::placeholder {
            color: #adb5bd;
            font-size: 13px;
          }
        }
      }

      .char-counter {
        position: absolute;
        right: 12px;
        bottom: 8px;
        font-size: 12px;
        color: #adb5bd;
        background: #ffffff;
        padding: 2px 6px;
        border-radius: 4px;
        transition: all 0.3s ease;
        pointer-events: none;

        &.warning {
          color: #f59e0b;
          font-weight: 500;
        }

        &.danger {
          color: #ef4444;
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
      background: #ffffff;
      border: 1px solid #e8ecef;
      border-radius: 8px;
      padding: 10px;
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
      margin-bottom: 6px;
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
        grid-template-columns: repeat(10, 1fr);
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

    .send-wrapper {
      display: flex;
      justify-content: flex-end;

      .send-button {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        padding: 6px 20px;
        font-size: 14px;
        font-weight: 500;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 6px 16px rgba(102, 126, 234, 0.35);
        }

        &:active:not(:disabled) {
          transform: translateY(0);
        }

        &:disabled {
          background: #e9ecef;
          box-shadow: none;
          color: #adb5bd;
        }

        &.send-success {
          background: linear-gradient(135deg, #10b981 0%, #059669 100%);
          box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
        }
      }

      :deep(.el-button--primary) {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        padding: 6px 20px;
        font-size: 14px;
        font-weight: 500;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 6px 16px rgba(102, 126, 234, 0.35);
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
