<script setup>
import { ref, computed } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'

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

// 聚焦输入框
const inputRef = ref(null)
const focus = () => {
  inputRef.value?.focus()
}

// 清空输入
const clearInput = () => {
  messageContent.value = ''
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
      <el-checkbox :model-value="syncToGroup" @change="emit('update:syncToGroup', $event)">
        <span class="sync-label">同步至群聊</span>
      </el-checkbox>
    </div>

    <!-- 工具栏和输入框 -->
    <div class="input-container">
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
@import '../../../assets/css/nordic-theme.less';
@import '../../../assets/css/merchant-theme.less';

.message-input-wrapper {
  padding: 16px 20px;
  border-top: 1px solid @merchant-border;
  background: linear-gradient(135deg, @merchant-surface-alt 0%, @merchant-surface 100%);
  box-shadow: 0 -2px 8px @merchant-shadow;

  .sync-toggle {
    margin-bottom: 10px;
    padding: 8px 12px;
    background: linear-gradient(135deg, @merchant-primary-light 0%, @merchant-surface-alt 100%);
    border-radius: @nordic-radius-sm;
    border: 1px solid @merchant-border;

    .sync-label {
      font-size: @nordic-text-xs;
      color: @merchant-primary-dark;
      font-weight: 500;
    }

    :deep(.el-checkbox__label) {
      color: @merchant-primary-dark;
    }
  }

  .input-container {
    display: flex;
    gap: 12px;
    align-items: flex-end;

    .message-textarea {
      flex: 1;

      :deep(.el-textarea__inner) {
        border-radius: @nordic-radius-lg;
        border: 1px solid @merchant-border;
        background: @merchant-surface;
        padding: 12px 14px;
        font-size: @nordic-text-base;
        line-height: 1.6;
        transition: all 0.3s ease;
        resize: none;

        &:focus {
          border-color: @merchant-primary;
          box-shadow: 0 0 0 3px @merchant-shadow-hover;
        }

        &:disabled {
          background-color: @merchant-surface-alt;
          cursor: not-allowed;
        }
      }
    }

    .send-button {
      align-self: flex-end;
      border-radius: @nordic-radius-md;
      font-weight: 600;
      padding: 10px 24px;
      background: linear-gradient(135deg, @merchant-primary 0%, @merchant-primary-dark 100%);
      border: none;
      box-shadow: 0 4px 12px @merchant-shadow-hover;
      transition: all 0.2s ease;
      height: auto;

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px @merchant-shadow-hover;
      }

      &:active:not(:disabled) {
        transform: translateY(0);
      }

      &:disabled {
        background: @merchant-text-muted;
        box-shadow: none;
      }
    }
  }

  .input-hint {
    margin-top: 8px;
    text-align: right;

    .hint-text {
      font-size: 0.75rem /* 原值: 11px */;
      color: @merchant-text-muted;
      font-weight: 500;
    }
  }
}
</style>
