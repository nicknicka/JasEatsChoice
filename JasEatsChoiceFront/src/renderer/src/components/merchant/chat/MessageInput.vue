<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Paperclip, Mic } from '@element-plus/icons-vue'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  },
  sending: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '输入消息内容...'
  },
  showSyncToggle: {
    type: Boolean,
    default: false
  },
  syncToGroup: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['send', 'update:syncToGroup', 'upload-file', 'upload-image'])

const messageContent = ref('')
const showUploadMenu = ref(false)

// 是否可以发送
const canSend = computed(() => {
  return messageContent.value.trim().length > 0 && !props.disabled && !props.sending
})

// 处理键盘事件
const handleKeydown = (e) => {
  // Enter 发送，Shift+Enter 换行
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

// 发送消息
const sendMessage = () => {
  if (!canSend.value) return

  const content = messageContent.value.trim()
  if (!content) return

  emit('send', content)
  messageContent.value = ''
}

// 上传文件
const handleFileUpload = (file) => {
  emit('upload-file', file)
  showUploadMenu.value = false
}

// 上传图片
const handleImageUpload = (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  emit('upload-image', file)
  showUploadMenu.value = false
}

// 清空输入
const clearInput = () => {
  messageContent.value = ''
}

// 聚焦输入框
const inputRef = ref(null)
const focus = () => {
  inputRef.value?.focus()
}

// 暴露方法给父组件
defineExpose({
  focus,
  clearInput
})
</script>

<template>
  <div class="message-input-wrapper">
    <!-- 同步至群聊开关 -->
    <div v-if="showSyncToggle" class="sync-toggle">
      <el-checkbox v-model="syncToGroup" @change="emit('update:syncToGroup', $event)">
        <span class="sync-label">同步至群聊</span>
      </el-checkbox>
    </div>

    <!-- 工具栏和输入框 -->
    <div class="input-container">
      <div class="toolbar">
        <el-dropdown trigger="click" v-model="showUploadMenu">
          <el-button circle size="small" :icon="Paperclip" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="() => $refs.fileInput?.click()">
                <el-icon><Paperclip /></el-icon>
                <span>上传文件</span>
              </el-dropdown-item>
              <el-dropdown-item @click="() => $refs.imageInput?.click()">
                <el-icon><Picture /></el-icon>
                <span>上传图片</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 隐藏的文件输入 -->
        <input
          ref="fileInput"
          type="file"
          style="display: none"
          @change="(e) => handleFileUpload(e.target.files[0])"
        />
        <input
          ref="imageInput"
          type="file"
          accept="image/*"
          style="display: none"
          @change="(e) => handleImageUpload(e.target.files[0])"
        />
      </div>

      <el-input
        ref="inputRef"
        v-model="messageContent"
        type="textarea"
        :placeholder="placeholder"
        :rows="2"
        :disabled="disabled || sending"
        @keydown="handleKeydown"
        class="message-textarea"
      />

      <el-button
        type="primary"
        :icon="ChatDotRound"
        :disabled="!canSend"
        :loading="sending"
        @click="sendMessage"
        class="send-button"
      >
        发送
      </el-button>
    </div>

    <!-- 输入提示 -->
    <div class="input-hint">
      <span class="hint-text">按 Enter 发送，Shift + Enter 换行</span>
    </div>
  </div>
</template>

<style scoped lang="less">
.message-input-wrapper {
  padding: 16px 20px;
  border-top: 1px solid #e8eef5;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);

  .sync-toggle {
    margin-bottom: 10px;
    padding: 8px 12px;
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
    border-radius: 8px;
    border: 1px solid #bae6fd;

    .sync-label {
      font-size: 12px;
      color: #0369a1;
      font-weight: 500;
    }

    :deep(.el-checkbox__label) {
      color: #0369a1;
    }
  }

  .input-container {
    display: flex;
    gap: 12px;
    align-items: flex-end;

    .toolbar {
      display: flex;
      flex-direction: column;
      gap: 8px;
      padding-bottom: 2px;

      :deep(.el-button) {
        transition: all 0.2s ease;

        &:hover {
          transform: scale(1.1);
          background-color: #667eea;
          border-color: #667eea;
          color: #ffffff;
        }
      }
    }

    .message-textarea {
      flex: 1;

      :deep(.el-textarea__inner) {
        border-radius: 12px;
        border: 1px solid #e5e7eb;
        background: #ffffff;
        padding: 12px 14px;
        font-size: 14px;
        line-height: 1.6;
        transition: all 0.3s ease;
        resize: none;

        &:focus {
          border-color: #667eea;
          box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        &:disabled {
          background-color: #f5f7fa;
          cursor: not-allowed;
        }
      }
    }

    .send-button {
      align-self: flex-end;
      border-radius: 10px;
      font-weight: 600;
      padding: 10px 24px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
      transition: all 0.2s ease;
      height: auto;

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
      }

      &:active:not(:disabled) {
        transform: translateY(0);
      }

      &:disabled {
        background: #d1d5db;
        box-shadow: none;
      }
    }
  }

  .input-hint {
    margin-top: 8px;
    text-align: right;

    .hint-text {
      font-size: 11px;
      color: #9ca3af;
      font-weight: 500;
    }
  }
}

// 下拉菜单样式
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;

  .el-icon {
    font-size: 16px;
  }

  span {
    font-size: 13px;
  }

  &:hover {
    background-color: #f0f9ff;
    color: #0369a1;
  }
}
</style>
