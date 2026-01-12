<script setup>
import { ref, watch, nextTick, computed } from 'vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  conversationType: {
    type: String,
    default: 'private', // 'private' or 'group'
    validator: (value) => ['private', 'group'].includes(value)
  },
  currentUserId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits(['scroll-to-bottom'])

const messagesContainer = ref(null)

// 判断消息是否是自己发送的
const isMyMessage = (message) => {
  if (props.conversationType === 'group') {
    return message.sender === '我'
  } else {
    return message.sender === 'merchant'
  }
}

// 格式化消息时间
const formatMessageTime = (time) => {
  if (!time) return ''

  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }

  // 小于1小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }

  // 今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }

  // 更早
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 滚动到底部
const scrollToBottom = (smooth = true) => {
  nextTick(() => {
    if (messagesContainer.value) {
      const scrollOptions = smooth ? { behavior: 'smooth' } : {}
      messagesContainer.value.scrollTo({
        top: messagesContainer.value.scrollHeight,
        ...scrollOptions
      })
    }
  })
}

// 监听消息变化，自动滚动到底部
watch(() => props.messages, () => {
  scrollToBottom()
}, { deep: true })

// 暴露方法给父组件
defineExpose({
  scrollToBottom
})
</script>

<template>
  <div ref="messagesContainer" class="chat-message-list">
    <!-- 消息列表 -->
    <div v-if="messages.length > 0" class="messages-wrapper">
      <div
        v-for="message in messages"
        :key="message.id"
        class="message-item"
        :class="{ 'my-message': isMyMessage(message) }"
      >
        <!-- 群聊显示发送者名称 -->
        <div v-if="conversationType === 'group' && !isMyMessage(message)" class="message-header">
          <span class="sender-name">{{ message.sender }}</span>
        </div>

        <!-- 消息内容 -->
        <div class="message-content">
          <div class="message-text">{{ message.content }}</div>
          <div class="message-time">{{ formatMessageTime(message.time) }}</div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-messages">
      <el-empty description="暂无聊天记录" />
    </div>
  </div>
</template>

<style scoped lang="less">
.chat-message-list {
  flex: 1;
  padding: 18px 20px;
  overflow-y: auto;
  background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);

  .messages-wrapper {
    display: flex;
    flex-direction: column;

    .message-item {
      margin-bottom: 18px;
      max-width: 70%;
      animation: messageSlideIn 0.3s ease-out;

      &.my-message {
        align-self: flex-end;
        margin-left: auto;

        .message-content {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: #ffffff;
          box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

          .message-text {
            font-weight: 500;
          }

          .message-time {
            opacity: 0.85;
            color: #ffffff;
          }
        }
      }

      &:not(.my-message) {
        align-self: flex-start;

        .message-content {
          background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%);
          border: 1px solid #e5e7eb;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        }
      }

      .message-header {
        margin-bottom: 6px;

        .sender-name {
          font-size: 12px;
          color: #666;
          font-weight: 500;
        }
      }

      .message-content {
        border-radius: 14px;
        padding: 12px 16px;
        transition: all 0.2s ease;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        .message-text {
          font-size: 14px;
          line-height: 1.6;
          word-wrap: break-word;
          white-space: pre-wrap;
        }

        .message-time {
          text-align: right;
          font-size: 11px;
          margin-top: 6px;
          font-weight: 500;
          color: #9ca3af;
        }
      }
    }
  }

  .empty-messages {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
  }
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 滚动条样式
.chat-message-list::-webkit-scrollbar {
  width: 6px;
}

.chat-message-list::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.chat-message-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.chat-message-list::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
