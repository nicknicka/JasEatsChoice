<template>
  <div class="message-input-container" ref="inputContainerRef">
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
              @click="$emit('toggle-emoji')"
              :class="{ 'is-active': showEmojiPicker }"
            />
          </el-tooltip>

          <!-- 图片上传按钮 -->
          <input
            type="file"
            accept="image/*"
            @change="handleImageChange"
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

          <!-- 清空输入按钮 -->
          <el-tooltip content="清空输入" placement="top">
            <el-button
              :icon="Delete"
              circle
              size="small"
              @click="$emit('clear-input')"
            />
          </el-tooltip>
        </div>
        <div class="toolbar-right">
          <!-- 个性化数据开关 -->
          <el-tooltip
            content="开启后AI将使用您的个人数据提供个性化建议"
            placement="bottom"
          >
            <el-switch
              :model-value="personalDataEnabled"
              active-text="个性化"
              inactive-text="通用"
              @change="$emit('toggle-personal-data', $event)"
              size="small"
              style="margin-right: 8px"
            />
          </el-tooltip>

          <!-- 清空对话记录按钮 -->
          <el-button
            link
            type="danger"
            @click="$emit('clear-chat')"
          >
            🗑️ 清空对话
          </el-button>

          <el-button
            v-if="!showQuickQuestions"
            link
            type="primary"
            @click="$emit('show-quick-questions')"
          >
            💡 快捷提问
          </el-button>
        </div>
      </div>

      <!-- 表情面板 -->
      <transition name="slide-up">
        <div v-if="showEmojiPicker" class="emoji-panel">
          <div class="emoji-grid">
            <span
              v-for="emoji in emojis"
              :key="emoji"
              class="emoji-item"
              @click="$emit('select-emoji', emoji)"
            >
              {{ emoji }}
            </span>
          </div>
        </div>
      </transition>

      <!-- 已上传图片预览 -->
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
            @click="$emit('remove-image', img.id)"
          />
        </div>
      </div>

      <!-- 输入框和发送按钮 -->
      <div class="input-area">
        <el-input
          :model-value="modelValue"
          @update:model-value="$emit('update:modelValue', $event)"
          placeholder="请输入您的问题...（例如：推荐适合减肥的食谱）"
          clearable
          resize="none"
          :rows="2"
          type="textarea"
          @keydown="handleKeydown"
          maxlength="500"
          show-word-limit
          class="message-textarea"
        />
        <el-button
          :type="isStreaming ? 'danger' : 'primary'"
          class="send-btn"
          :class="{ 'is-loading': isLoading && !isStreaming }"
          @click="handleSendClick"
          :disabled="isLoading && !isStreaming"
          :icon="getSendButtonIcon"
        >
          <template v-if="isLoading && !isStreaming">
            <span class="loading-text">思考中</span>
          </template>
          <template v-else>
            {{ isStreaming ? '停止' : '发送' }}
          </template>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import {
  ChatDotRound,
  Picture,
  Delete,
  ChatRound,
  Close,
  Loading
} from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  showEmojiPicker: {
    type: Boolean,
    default: false
  },
  showQuickQuestions: {
    type: Boolean,
    default: true
  },
  uploadedImages: {
    type: Array,
    default: () => []
  },
  emojis: {
    type: Array,
    default: () => []
  },
  isStreaming: {
    type: Boolean,
    default: false
  },
  isLoading: {
    type: Boolean,
    default: false
  },
  personalDataEnabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:modelValue',
  'toggle-emoji',
  'clear-input',
  'clear-chat',
  'show-quick-questions',
  'select-emoji',
  'remove-image',
  'send',
  'stop-streaming',
  'stop-streaming',
  'toggle-personal-data',
  'upload-image'
])

const imageInputRef = ref(null)
const inputContainerRef = ref(null)

const triggerImageUpload = () => {
  imageInputRef.value?.click()
}

const handleImageChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    emit('upload-image', file, event)
  }
}

const handleKeydown = (event) => {
  // Shift+Enter换行，Enter发送
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    if (!isStreaming.value) {
      emit('send')
    } else {
      emit('stop-streaming')
    }
  }
}

const handleSendClick = () => {
  if (!isStreaming.value) {
    emit('send')
  } else {
    emit('stop-streaming')
  }
}

// 获取发送按钮图标
const getSendButtonIcon = computed(() => {
  if (props.isLoading && !props.isStreaming) {
    return Loading
  }
  return props.isStreaming ? Close : ChatRound
})

defineExpose({
  inputContainerRef
})
</script>

<style scoped lang="less">
.message-input-container {
  background: linear-gradient(to bottom, #ffffff 0%, #fafbfc 100%);
  border: 1px solid #e8ecef;
  border-radius: 12px;
  padding: 10px 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .input-wrapper {
    display: flex;
    flex-direction: column;
    gap: 8px;
    position: relative;
  }

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
        background: linear-gradient(
          135deg,
          rgba(102, 126, 234, 0.1) 0%,
          rgba(118, 75, 162, 0.1) 100%
        );
        box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15),
          0 2px 8px rgba(102, 126, 234, 0.2);

        &:hover {
          background: linear-gradient(
            135deg,
            rgba(102, 126, 234, 0.15) 0%,
            rgba(118, 75, 162, 0.15) 100%
          );
        }
      }
    }
  }

  .input-area {
    display: flex;
    gap: 10px;
    align-items: flex-end;

    .message-textarea {
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

    .send-btn {
      flex-shrink: 0;
      background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
      border: none;
      padding: 8px 24px;
      font-size: 1rem /* 原值: 14px */;
      font-weight: 600;
      border-radius: 10px;
      box-shadow: 0 2px 8px rgba(255, 107, 107, 0.25);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      height: 60px;
      position: relative;
      overflow: hidden;

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

      &.is-loading {
        animation: pulse 1.5s ease-in-out infinite;

        :deep(.el-icon) {
          animation: rotate 1s linear infinite;
        }

        .loading-text {
          display: inline-flex;
          align-items: center;
          gap: 6px;
        }
      }

      :deep(.el-icon) {
        font-size: 18px;
      }
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(255, 107, 107, 0.25);
  }
  50% {
    box-shadow: 0 4px 16px rgba(255, 107, 107, 0.5);
  }
}

/* 表情面板样式 */
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

/* 表情面板滑入滑出动画 */
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

/* 上传图片预览样式 */
.uploaded-images-preview {
  display: flex;
  gap: 12px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;

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
</style>
