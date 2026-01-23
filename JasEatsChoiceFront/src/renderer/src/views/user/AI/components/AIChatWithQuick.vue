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

    <!-- 底部容器（包含快捷提问和输入区域） -->
    <div class="bottom-container">
      <!-- 快捷提问面板 -->
      <transition name="slide-down">
        <div v-if="showQuickQuestions" class="quick-questions-panel">
          <div class="quick-questions-header">
            <span class="quick-questions-title">💡 快捷提问</span>
            <el-button
              :icon="Close"
              circle
              size="small"
              text
              @click="showQuickQuestions = false"
            />
          </div>
          <div class="quick-questions-list">
            <el-tag
              v-for="question in quickQuestions"
              :key="question"
              @click="handleQuickQuestion(question)"
              class="question-tag"
              type="info"
              effect="plain"
            >
              {{ question }}
            </el-tag>
          </div>
        </div>
      </transition>

      <!-- 重新显示快捷提问按钮 -->
      <transition name="fade">
        <div v-if="!showQuickQuestions && messages.length > 0" class="show-questions-btn">
          <el-button
            link
            type="primary"
            @click="showQuickQuestions = true"
            size="small"
          >
            💡 显示快捷提问
          </el-button>
        </div>
      </transition>

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
          show-word-limit
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
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, ChatDotRound, Close } from '@element-plus/icons-vue'

// 状态
const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const isStreaming = ref(false)
const chatContainerRef = ref(null)
const showQuickQuestions = ref(true)

// 快捷问题列表
const quickQuestions = ref([
  "推荐适合减肥的食谱",
  "今日卡路里摄入建议",
  "如何搭配营养均衡的饮食",
  "推荐低卡路里零食",
  "适合运动后的食物"
])

// 模拟加载聊天记录
const loadMessages = () => {
  isLoading.value = true
  setTimeout(() => {
    messages.value = [
      {
        id: 1,
        sender: 'ai',
        content: '您好！我是您的AI饮食助手。有什么可以帮您的吗？',
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
      content: `这是对"${message}"的模拟回复。\n\n💡 实际使用时将连接到后端API，提供真实的AI饮食建议。`,
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

// 快捷提问点击
const handleQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
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
  gap: 12px;
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
      font-size: 16px;
    }

    .hint {
      font-size: 14px;
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
        font-size: 15px;
        white-space: pre-wrap;
        word-break: break-word;

        &:hover {
          transform: translateY(-2px) scale(1.01);
        }
      }

      .message-time {
        font-size: 12px;
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

.bottom-container {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

// 快捷提问面板
.quick-questions-panel {
  background: linear-gradient(135deg, #f0f9ff 0%, #e8f4fd 100%);
  border: 1px solid #d1e9ff;
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.08);

  .quick-questions-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    .quick-questions-title {
      font-size: 14px;
      font-weight: 600;
      color: #2c7be5;
    }
  }

  .quick-questions-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .question-tag {
      margin: 0;
      padding: 6px 14px;
      cursor: pointer;
      transition: all 0.3s ease;
      font-size: 13px;
      font-weight: 500;
      border-radius: 20px;
      background-color: #fff;
      border-color: #b3e0ff;
      color: #409eff;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.25);
        background: linear-gradient(135deg, #409eff 0%, #5dade2 100%);
        color: #fff;
        border-color: transparent;
      }
    }
  }
}

.show-questions-btn {
  text-align: center;
  padding: 4px 0;
}

// 快捷提问面板动画
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-12px);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
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
      font-size: 14px;
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
    font-size: 15px;
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
