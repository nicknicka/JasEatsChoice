<template>
  <div class="chat-panel">
    <!-- 消息列表 -->
    <div class="messages-container" ref="chatContainerRef">
      <div
        v-for="msg in messages"
        :key="msg.id"
        :class="['message', msg.sender, { error: msg.isError }]"
      >
        <div class="avatar">{{ msg.avatar }}</div>
        <div class="content">
          <div class="text" v-html="formatMarkdown(msg.content)"></div>
          <div class="time">{{ msg.time }}</div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="isLoading" class="message ai loading">
        <div class="avatar">🤖</div>
        <div class="content">
          <div class="typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷提问 -->
    <div class="quick-questions" v-if="showQuickQuestions && messages.length <= 1">
      <div class="quick-title">快捷提问</div>
      <div class="quick-list">
        <div
          v-for="q in quickQuestions"
          :key="q"
          class="quick-btn"
          @click="handleQuickQuestion(q)"
        >
          {{ q }}
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="2"
        :disabled="isLoading"
        placeholder="输入您的经营问题... (Ctrl+Enter发送)"
        @keydown.enter.ctrl="sendMessage"
      />
      <div class="input-actions">
        <el-button @click="clearChat" :disabled="isLoading">
          <el-icon><Delete /></el-icon>
          清空
        </el-button>
        <el-button
          type="primary"
          :loading="isLoading"
          @click="sendMessage"
        >
          <el-icon><Position /></el-icon>
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Delete, Position } from '@element-plus/icons-vue'
import { useMerchantAIChat } from '../composables/useMerchantAIChat'
import { MERCHANT_QUICK_QUESTIONS } from '../../../../config/merchantChatConfig'

// 聊天功能
const {
  messages,
  isLoading,
  chatContainerRef,
  loadMessages,
  sendMessage: sendChatMessage,
  clearChat
} = useMerchantAIChat()

// 本地状态
const inputMessage = ref('')
const showQuickQuestions = ref(true)

// 快捷提问列表
const quickQuestions = ref(MERCHANT_QUICK_QUESTIONS)

/**
 * 处理快捷提问
 */
const handleQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

/**
 * 发送消息
 */
const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message) return

  showQuickQuestions.value = false
  inputMessage.value = ''
  await sendChatMessage(message)
}

/**
 * 简单的Markdown格式化
 */
const formatMarkdown = (text) => {
  if (!text) return ''

  // 转义HTML
  let result = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // 标题
  result = result.replace(/^### (.*$)/gm, '<h4>$1</h4>')
  result = result.replace(/^## (.*$)/gm, '<h3>$1</h3>')
  result = result.replace(/^# (.*$)/gm, '<h2>$1</h2>')

  // 粗体
  result = result.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')

  // 斜体
  result = result.replace(/\*(.*?)\*/g, '<em>$1</em>')

  // 代码块
  result = result.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')

  // 行内代码
  result = result.replace(/`(.*?)`/g, '<code>$1</code>')

  // 表格（简单处理）
  result = result.replace(/\|(.+)\|/g, (match) => {
    const cells = match.split('|').filter(c => c.trim())
    if (cells.some(c => c.trim().match(/^-+$/))) {
      return '' // 跳过分隔行
    }
    return `<div class="table-row">${cells.map(c => `<span class="cell">${c.trim()}</span>`).join('')}</div>`
  })

  // 列表
  result = result.replace(/^- (.*$)/gm, '<li>$1</li>')
  result = result.replace(/^(\d+)\. (.*$)/gm, '<li>$2</li>')

  // 换行
  result = result.replace(/\n/g, '<br>')

  return result
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped lang="less">
@import '../../../../../assets/css/nordic-theme.less';
@import '../../../../../assets/css/merchant-theme.less';

.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 12px;
  padding: 16px;
  background: @merchant-surface;
  border-radius: 12px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: @merchant-surface-alt;
  border-radius: 12px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: @merchant-border;
    border-radius: 3px;

    &:hover {
      background: @merchant-text-muted;
    }
  }
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;

  &.ai {
    .avatar {
      background: @merchant-secondary;
    }
    .content {
      background: @merchant-secondary-light;
      border: 1px solid @merchant-border;
    }
  }

  &.user {
    flex-direction: row-reverse;
    .avatar {
      background: @merchant-primary-light;
    }
    .content {
      background: @merchant-secondary-light;
      border: 1px solid @merchant-border;
    }
  }

  &.error {
    .content {
      background: @merchant-secondary-light;
      border-color: @merchant-error;
    }
  }

  .avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    flex-shrink: 0;
  }

  .content {
    max-width: 70%;
    padding: 12px 16px;
    border-radius: 16px;

    .text {
      font-size: 14px;
      line-height: 1.6;
      color: @merchant-text;

      :deep(h2) {
        font-size: 18px;
        font-weight: 700;
        margin: 8px 0;
        color: @merchant-secondary;
      }

      :deep(h3) {
        font-size: 16px;
        font-weight: 600;
        margin: 6px 0;
        color: @merchant-secondary;
      }

      :deep(h4) {
        font-size: 15px;
        font-weight: 600;
        margin: 4px 0;
        color: @merchant-secondary;
      }

      :deep(strong) {
        color: @merchant-secondary;
      }

      :deep(code) {
        background: @merchant-surface-alt;
        padding: 2px 6px;
        border-radius: 4px;
        font-family: monospace;
      }

      :deep(pre) {
        background: @merchant-surface-alt;
        padding: 12px;
        border-radius: 8px;
        overflow-x: auto;
        margin: 8px 0;
      }

      :deep(li) {
        margin-left: 16px;
        margin-bottom: 4px;
      }

      :deep(.table-row) {
        display: flex;
        gap: 12px;
        padding: 4px 0;
        border-bottom: 1px solid @merchant-border;

        .cell {
          flex: 1;
          font-size: 13px;
        }
      }
    }

    .time {
      font-size: 12px;
      color: @merchant-text-muted;
      margin-top: 4px;
      text-align: right;
    }
  }
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 8px 0;

  span {
    width: 8px;
    height: 8px;
    background: @merchant-secondary;
    border-radius: 50%;
    animation: bounce 1.4s infinite ease-in-out;

    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.quick-questions {
  background: @merchant-secondary-light;
  border-radius: 12px;
  padding: 16px;

  .quick-title {
    font-size: 14px;
    font-weight: 600;
    color: @merchant-secondary;
    margin-bottom: 12px;
  }

  .quick-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .quick-btn {
    padding: 8px 16px;
    background: @merchant-surface;
    border: 1px solid @merchant-border;
    border-radius: 20px;
    font-size: 13px;
    color: @merchant-secondary;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background: @merchant-secondary-light;
      border-color: @merchant-secondary;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(181, 106, 74, 0.15);
    }
  }
}

.input-area {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .input-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>
