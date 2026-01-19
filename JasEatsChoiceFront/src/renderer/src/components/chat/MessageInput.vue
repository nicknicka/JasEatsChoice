<template>
  <div class="message-input-container">
    <!-- 引用预览 -->
    <ReplyPreview :replying-to="replyingTo" @cancel="$emit('cancel-reply')" />

    <div class="input-wrapper">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-tooltip content="表情" placement="top">
            <el-button :icon="ChatDotRound" circle size="small" @click="toggleEmoji" />
          </el-tooltip>
          <el-tooltip content="上传图片" placement="top">
            <el-button :icon="Picture" circle size="small" @click="handleImageUpload" />
          </el-tooltip>
          <el-tooltip content="上传文件" placement="top">
            <el-button :icon="FolderOpened" circle size="small" @click="handleFileUpload" />
          </el-tooltip>
        </div>
        <div class="toolbar-right">
          <el-tooltip content="清空" placement="top">
            <el-button :icon="Delete" circle size="small" @click="clearInput" />
          </el-tooltip>
        </div>
      </div>

      <!-- 输入框 -->
      <el-input
        ref="textareaRef"
        v-model="inputValue"
        type="textarea"
        placeholder="输入消息... (Enter发送，Shift+Enter换行)"
        :autosize="{ minRows: 1, maxRows: 3 }"
        @keydown.enter.exact.prevent="handleSend"
        :disabled="disabled"
        class="message-textarea"
      />

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
          :disabled="disabled || !inputValue.trim()"
          :icon="Promotion"
          size="default"
        >
          发送
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
import { ref, nextTick } from 'vue'
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
const textareaRef = ref(null)
const imageInputRef = ref(null)
const fileInputRef = ref(null)

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

const handleSend = () => {
  if (inputValue.value.trim()) {
    emit('send', inputValue.value.trim())
    inputValue.value = ''
    showEmoji.value = false
  }
}

const clearInput = () => {
  inputValue.value = ''
  showEmoji.value = false
  ElMessage.info('已清空输入内容')
}

const toggleEmoji = () => {
  showEmoji.value = !showEmoji.value
}

const insertEmoji = (emoji) => {
  inputValue.value += emoji
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.focus()
    }
  })
}

const handleImageUpload = () => {
  imageInputRef.value?.click()
}

const handleFileUpload = () => {
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
  padding: 6px 10px;
  background: linear-gradient(to bottom, #ffffff 0%, #fafbfc 100%);
  border-top: 1px solid #e8ecef;
  display: flex;
  flex-direction: column;
  gap: 6px;
  box-shadow: 0 -2px 6px rgba(0, 0, 0, 0.03);

  .input-wrapper {
    display: flex;
    flex-direction: column;
    gap: 5px;
    position: relative;

    .toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 2px;

      .toolbar-left,
      .toolbar-right {
        display: flex;
        gap: 5px;
      }

      :deep(.el-button) {
        border: 1px solid #e8ecef;
        background: #ffffff;
        color: #5a6c7d;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover {
          border-color: #667eea;
          color: #667eea;
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
        }
      }
    }

    .message-textarea {
      flex: 1;

      :deep(.el-textarea__inner) {
        border-radius: 8px;
        border: 2px solid #e8ecef;
        background: #ffffff;
        padding: 6px 10px;
        font-size: 13px;
        line-height: 1.5;
        min-height: 29px;
        transition: all 0.3s ease;
        resize: none;

        &:focus {
          border-color: #667eea;
          box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        &::placeholder {
          color: #adb5bd;
          font-size: 12px;
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
      border-radius: 6px;
      padding: 8px;
      box-shadow: 0 4px 11px rgba(0, 0, 0, 0.1);
      margin-bottom: 5px;
      max-height: 160px;
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
        gap: 3px;

        .emoji-item {
          font-size: 18px;
          text-align: center;
          padding: 5px 3px;
          border-radius: 5px;
          cursor: pointer;
          transition: all 0.2s ease;
          user-select: none;

          &:hover {
            background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
            transform: scale(1.15);
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

      :deep(.el-button--primary) {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        padding: 5px 16px;
        font-size: 13px;
        font-weight: 500;
        border-radius: 6px;
        box-shadow: 0 2px 6px rgba(102, 126, 234, 0.2);
        transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover:not(:disabled) {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
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

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(5px);
}
</style>
