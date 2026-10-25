<template>
  <div class="chat-content-wrapper">
    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="chatContainerRef">
      <!-- 加载中 -->
      <div v-if="isLoading" class="loading-state">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p>正在加载聊天记录...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="messages.length === 0" class="empty-state">
        <el-icon :size="64"><ChatDotRound /></el-icon>
        <p>暂无消息</p>
        <p class="hint">开始对话吧！</p>
      </div>

      <!-- 消息列表 -->
      <template v-else>
        <div
          v-for="message in messages"
          :key="message.id"
          class="chat-message"
          :class="{
            'user-message': message.sender === 'user',
            'ai-message': message.sender === 'ai'
          }"
        >
          <div class="message-avatar">{{ message.avatar }}</div>
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>
      </template>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <el-input
        v-model="inputMessage"
        placeholder="请输入您的问题...（例如：推荐适合减肥的食谱）"
        :rows="2"
        type="textarea"
        :maxlength="500"
        :disabled="isLoading"
        @keydown="handleKeydown"
        class="message-input"
      />
      <el-button
        :type="isStreaming ? 'danger' : 'primary'"
        class="send-btn"
        @click="handleSendClick"
        :disabled="isLoading && !isStreaming"
        :loading="isLoading"
      >
        {{ isStreaming ? '停止' : '发送' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, ChatDotRound } from '@element-plus/icons-vue'
import { WELCOME_MESSAGE } from '../../../../../config/chatConfig'

// 状态
const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const isStreaming = ref(false)
const chatContainerRef = ref(null)

// 模拟加载聊天记录
const loadMessages = () => {
  isLoading.value = true
  setTimeout(() => {
    messages.value = [
      {
        id: 1,
        sender: 'ai',
        content: WELCOME_MESSAGE,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        avatar: '🤖'
      }
    ]
    isLoading.value = false
    scrollToBottom()
  }, 500)
}

// 发送消息
const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message) {
    ElMessage.warning('请输入问题')
    return
  }

  // 添加用户消息
  messages.value.push({
    id: messages.value.length + 1,
    sender: 'user',
    content: message,
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    avatar: '👤'
  })

  inputMessage.value = ''
  scrollToBottom()

  // 模拟AI回复
  isLoading.value = true
  isStreaming.value = true

  setTimeout(() => {
    messages.value.push({
      id: messages.value.length + 1,
      sender: 'ai',
      content: `这是对"${message}"的模拟回复。实际使用时将连接到后端API。`,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      avatar: '🤖'
    })
    isLoading.value = false
    isStreaming.value = false
    scrollToBottom()
  }, 1000)
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
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
    isStreaming.value = false
    ElMessage.info('已停止AI回复')
  } else {
    sendMessage()
  }
}

// 初始化
loadMessages()
</script>

<style scoped lang="less">
.chat-content-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  flex: 1;
  gap: 16px;
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

  .loading-state,
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

    .message-content {
      display: flex;
      flex-direction: column;
      gap: 6px;
      max-width: 75%;

      .message-text {
        padding: 14px 18px;
        border-radius: 20px;
        line-height: 1.7;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        font-size: 0.929rem /* 原值: 15px，调整为13px */;
        white-space: pre-wrap;
        word-break: break-word;

        &:hover {
          transform: translateY(-2px) scale(1.01);
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

.input-area {
  flex-shrink: 0;
  display: flex;
  gap: 12px;
  align-items: flex-end;
  background: linear-gradient(to bottom, #ffffff 0%, #fafbfc 100%);
  border: 1px solid #e8ecef;
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

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
