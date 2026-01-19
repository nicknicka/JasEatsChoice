<template>
  <div
    class="message-item"
    :class="{
      'others-message': isOtherMessage,
      'my-message': isMyMessage,
      'message-sending': isSending,
      'message-failed': isFailed
    }"
  >
    <div class="message-header">
      <span class="sender-name">{{ senderName }}</span>
      <span v-if="isSending" class="message-status">发送中...</span>
      <span v-else-if="isFailed" class="message-status failed">发送失败</span>
    </div>

    <div class="message-content">
      <!-- 引用引用 -->
      <div v-if="message.replyTo" class="message-reply-quote">
        <div class="quote-bar"></div>
        <div class="quote-content">
          <div class="quote-author">{{ message.replyFromName || message.replyFromId }}</div>
          <div class="quote-text">{{ message.replyContent }}</div>
        </div>
      </div>

      {{ message.content }}

      <div class="message-time">
        {{ formattedTime }}
      </div>

      <!-- 消息操作按钮 -->
      <el-dropdown trigger="click" @command="(cmd) => $emit('command', cmd, message)">
        <el-button type="text" size="small" class="msg-action-btn">⋯</el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="reply">引用</el-dropdown-item>
            <el-dropdown-item command="forward">转发</el-dropdown-item>
            <el-dropdown-item v-if="canRecall" command="recall">撤回消息</el-dropdown-item>
            <el-dropdown-item command="copy">复制</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 重发按钮 -->
      <el-button
        v-if="isFailed && message.canResend"
        type="warning"
        size="small"
        text
        @click="$emit('resend', message)"
        class="resend-btn"
      >
        点击重发
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  message: {
    type: Object,
    required: true
  },
  userId: {
    type: [String, Number],
    required: true
  },
  formatMessageTime: {
    type: Function,
    default: (time) => time || ''
  },
  canRecallMessage: {
    type: Function,
    default: () => false
  }
})

const emit = defineEmits(['command', 'resend'])

const isMyMessage = computed(
  () => props.message.fromId === props.userId.toString()
)

const isOtherMessage = computed(
  () => props.message.fromId !== props.userId.toString()
)

const isSending = computed(() => props.message.status === 'sending')

const isFailed = computed(() => props.message.status === 'failed')

const canRecall = computed(() => {
  return isMyMessage.value && props.canRecallMessage(props.message)
})

const senderName = computed(() => {
  return isMyMessage.value ? '我' : props.message.fromId
})

const formattedTime = computed(() => {
  return props.message.formattedTime || props.formatMessageTime(props.message.createTime || props.message.time)
})
</script>

<style scoped lang="less">
.message-item {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;

  &.others-message {
    align-items: flex-start;

    .message-content {
      background-color: #fff;
      border: 1px solid #e4e7ed;
      border-radius: 0 12px 12px 12px;
    }
  }

  &.my-message {
    align-items: flex-end;

    .message-content {
      background-color: #95ec69;
      border: 1px solid #86d35e;
      border-radius: 12px 0 12px 12px;
    }
  }

  &.message-sending {
    opacity: 0.6;
  }

  &.message-failed {
    .message-content {
      background-color: #fef0f0;
      border-color: #fbc4c4;
    }
  }

  .message-header {
    margin-bottom: 4px;
    font-size: 12px;
    color: #999;
    display: flex;
    align-items: center;
    gap: 8px;

    .sender-name {
      font-weight: 500;
    }

    .message-status {
      font-size: 11px;

      &.failed {
        color: #f56c6c;
      }
    }
  }

  .message-content {
    max-width: 70%;
    padding: 10px 14px;
    position: relative;
    word-break: break-word;
    white-space: pre-wrap;

    .message-reply-quote {
      background-color: #f5f7fa;
      padding: 8px;
      border-radius: 4px;
      margin-bottom: 8px;
      display: flex;
      gap: 8px;

      .quote-bar {
        width: 3px;
        background-color: #409eff;
        border-radius: 2px;
      }

      .quote-content {
        flex: 1;

        .quote-author {
          font-size: 12px;
          font-weight: 500;
          color: #409eff;
          margin-bottom: 4px;
        }

        .quote-text {
          font-size: 13px;
          color: #606266;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }
      }
    }

    .message-time {
      font-size: 11px;
      color: #999;
      margin-top: 4px;
      display: inline-block;
    }

    .msg-action-btn {
      margin-left: 8px;
      opacity: 0;
      transition: opacity 0.2s;
      font-size: 16px;
      padding: 0;
      width: 20px;
      height: 20px;
    }

    .resend-btn {
      margin-left: 8px;
      font-size: 12px;
    }

    &:hover .msg-action-btn {
      opacity: 1;
    }
  }
}
</style>
