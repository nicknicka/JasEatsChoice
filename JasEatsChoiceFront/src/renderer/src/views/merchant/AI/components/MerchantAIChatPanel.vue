<template>
  <div class="chat-panel">
    <!-- 消息列表 -->
    <div class="messages-container" ref="chatContainerRef">
      <div
        v-for="(msg, index) in messages"
        :key="msg.id"
        :class="['message', msg.sender, { error: msg.isError }]"
        :style="{ animationDelay: `${index * 0.03}s` }"
      >
        <div class="avatar" :class="msg.sender">
          <span class="avatar-emoji">{{ msg.avatar }}</span>
        </div>
        <div class="bubble">
          <div class="text" v-html="formatMarkdown(msg.content)"></div>
          <div class="time">{{ msg.time }}</div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="isLoading" class="message ai loading">
        <div class="avatar ai">
          <span class="avatar-emoji">🤖</span>
        </div>
        <div class="bubble loading-bubble">
          <div class="typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷提问 -->
    <Transition name="quick-fade">
      <div class="quick-questions" v-if="showQuickQuestions && messages.length <= 1">
        <div class="quick-header">
          <span class="quick-icon">💡</span>
          <span class="quick-title">快速开始对话</span>
        </div>
        <div class="quick-list">
          <button
            v-for="q in quickQuestions"
            :key="q"
            class="quick-btn"
            @click="handleQuickQuestion(q)"
          >
            {{ q }}
          </button>
        </div>
      </div>
    </Transition>

    <!-- 输入区域 -->
    <div class="input-area">
      <div class="input-wrapper">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          :disabled="isLoading"
          resize="none"
          placeholder="输入您的经营问题，按 Ctrl+Enter 发送..."
          @keydown.enter.ctrl="sendMessage"
        />
      </div>
      <div class="input-actions">
        <button class="action-btn clear-btn" @click="clearChat" :disabled="isLoading">
          <el-icon><Delete /></el-icon>
          <span>清空</span>
        </button>
        <button
          class="action-btn send-btn"
          :disabled="isLoading || !inputMessage.trim()"
          @click="sendMessage"
        >
          <el-icon v-if="!isLoading"><Position /></el-icon>
          <el-icon v-else class="is-loading"><Loading /></el-icon>
          <span>{{ isLoading ? '思考中...' : '发送' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Delete, Position, Loading } from '@element-plus/icons-vue'
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
@import '../../../../assets/css/nordic-theme.less';
@import '../../../../assets/css/merchant-theme.less';

.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px;
  gap: 16px;
}

// --- 消息列表 ---
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: linear-gradient(180deg, rgba(250, 248, 245, 0.4), rgba(240, 237, 232, 0.3));
  border-radius: 16px;
  border: 1px solid rgba(226, 222, 216, 0.5);

  &::-webkit-scrollbar {
    width: 5px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: @merchant-border;
    border-radius: 3px;

    &:hover {
      background: @merchant-text-muted;
    }
  }
}

// --- 消息气泡 ---
.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  animation: msgSlideIn 0.4s cubic-bezier(0.22, 1, 0.36, 1) both;

  &.ai {
    .avatar.ai {
      background: linear-gradient(135deg, @merchant-primary, darken(@merchant-primary, 5%));
      box-shadow: 0 3px 10px rgba(74, 122, 77, 0.2);
    }
    .bubble {
      background: @merchant-surface;
      border: 1px solid @merchant-border;
      border-top-left-radius: 6px;
    }
  }

  &.user {
    flex-direction: row-reverse;
    .avatar.user {
      background: linear-gradient(135deg, @merchant-secondary, darken(@merchant-secondary, 5%));
      box-shadow: 0 3px 10px rgba(181, 106, 74, 0.2);
    }
    .bubble {
      background: linear-gradient(135deg, @merchant-secondary-light, rgba(244, 230, 222, 0.7));
      border: 1px solid rgba(181, 106, 74, 0.12);
      border-top-right-radius: 6px;
    }
  }

  &.error {
    .bubble {
      border-color: rgba(196, 91, 91, 0.2);
      background: linear-gradient(135deg, @merchant-error-light, rgba(246, 224, 224, 0.5));
    }
  }

  .avatar {
    width: 38px;
    height: 38px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    position: relative;

    .avatar-emoji {
      font-size: 18px;
      line-height: 1;
    }
  }

  .bubble {
    max-width: 72%;
    min-width: 0;
    padding: 14px 18px;
    border-radius: 18px;
    position: relative;

    .text {
      font-size: 14px;
      line-height: 1.7;
      color: @merchant-text;
      white-space: pre-wrap;
      word-break: break-word;
      overflow-wrap: anywhere;

      :deep(h2) {
        font-size: 17px;
        font-weight: 700;
        margin: 10px 0 6px;
        color: @merchant-primary-dark;
      }

      :deep(h3) {
        font-size: 15px;
        font-weight: 600;
        margin: 8px 0 4px;
        color: @merchant-primary-dark;
      }

      :deep(h4) {
        font-size: 14px;
        font-weight: 600;
        margin: 6px 0 3px;
        color: @merchant-text;
      }

      :deep(strong) {
        color: @merchant-secondary;
        font-weight: 600;
      }

      :deep(em) {
        color: @merchant-text-sec;
        font-style: italic;
      }

      :deep(code) {
        background: rgba(240, 237, 232, 0.8);
        padding: 2px 6px;
        border-radius: 4px;
        font-family: 'Menlo', 'Consolas', monospace;
        font-size: 13px;
      }

      :deep(pre) {
        background: rgba(45, 42, 38, 0.04);
        padding: 14px;
        border-radius: 10px;
        overflow-x: auto;
        margin: 10px 0;
        border: 1px solid @merchant-border;
      }

      :deep(li) {
        margin-left: 16px;
        margin-bottom: 4px;
        line-height: 1.6;
      }

      :deep(.table-row) {
        display: flex;
        gap: 12px;
        padding: 5px 0;
        border-bottom: 1px solid @merchant-divider;

        .cell {
          flex: 1;
          font-size: 13px;
        }
      }
    }

    .time {
      font-size: 11px;
      color: @merchant-text-muted;
      margin-top: 6px;
      text-align: right;
      opacity: 0.7;
    }
  }
}

// --- 加载状态 ---
.loading-bubble {
  background: @merchant-surface;
  border: 1px solid @merchant-border;
  border-top-left-radius: 6px;
}

.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 10px 4px;

  span {
    width: 7px;
    height: 7px;
    background: @merchant-primary;
    border-radius: 50%;
    animation: typingWave 1.4s ease-in-out infinite;

    &:nth-child(1) { animation-delay: 0s; }
    &:nth-child(2) { animation-delay: 0.15s; }
    &:nth-child(3) { animation-delay: 0.3s; }
  }
}

// --- 快捷提问 ---
.quick-questions {
  background: linear-gradient(135deg, rgba(255,255,255,0.75), rgba(250,248,245,0.6));
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(226, 222, 216, 0.5);
  border-radius: 16px;
  padding: 18px;

  .quick-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 14px;

    .quick-icon {
      font-size: 16px;
    }

    .quick-title {
      font-size: 13px;
      font-weight: 600;
      color: @merchant-text-sec;
      letter-spacing: 0.3px;
    }
  }

  .quick-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .quick-btn {
    padding: 9px 16px;
    background: @merchant-surface;
    border: 1px solid @merchant-border;
    border-radius: 24px;
    font-size: 13px;
    color: @merchant-text-sec;
    cursor: pointer;
    transition: all 0.25s cubic-bezier(0.22, 1, 0.36, 1);
    font-family: inherit;
    line-height: 1.3;

    &:hover {
      background: @merchant-primary-light;
      border-color: @merchant-primary;
      color: @merchant-primary-dark;
      transform: translateY(-2px);
      box-shadow: 0 4px 14px rgba(74, 122, 77, 0.12);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

// --- 输入区域 ---
.input-area {
  display: flex;
  flex-direction: column;
  gap: 10px;

  .input-wrapper {
    background: @merchant-surface;
    border: 1.5px solid @merchant-border;
    border-radius: 14px;
    overflow: hidden;
    transition: border-color 0.25s ease, box-shadow 0.25s ease;

    &:focus-within {
      border-color: @merchant-primary;
      box-shadow: 0 0 0 3px rgba(74, 122, 77, 0.08);
    }

    :deep(.el-textarea__inner) {
      border: none;
      box-shadow: none;
      padding: 14px 16px;
      font-size: 14px;
      line-height: 1.6;
      background: transparent;
      color: @merchant-text;

      &::placeholder {
        color: @merchant-text-muted;
      }

      &:focus {
        box-shadow: none;
      }
    }
  }

  .input-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }

  .action-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 9px 18px;
    border: none;
    border-radius: 10px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.25s cubic-bezier(0.22, 1, 0.36, 1);
    font-family: inherit;

    &.clear-btn {
      background: @merchant-surface-alt;
      color: @merchant-text-sec;
      border: 1px solid @merchant-border;

      &:hover:not(:disabled) {
        background: @merchant-surface;
        border-color: @merchant-text-muted;
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }

    &.send-btn {
      background: linear-gradient(135deg, @merchant-primary, darken(@merchant-primary, 4%));
      color: #fff;
      box-shadow: 0 2px 8px rgba(74, 122, 77, 0.2);

      &:hover:not(:disabled) {
        transform: translateY(-1px);
        box-shadow: 0 4px 14px rgba(74, 122, 77, 0.3);
      }

      &:active:not(:disabled) {
        transform: translateY(0);
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
        transform: none;
      }
    }
  }
}

// --- 动画 ---
@keyframes msgSlideIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes typingWave {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

// --- 快捷提问过渡 ---
.quick-fade-enter-active {
  transition: all 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}

.quick-fade-leave-active {
  transition: all 0.25s ease;
}

.quick-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.quick-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
