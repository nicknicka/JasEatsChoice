<template>
  <div class="messages-container" ref="containerRef">
    <!-- 加载更多提示 -->
    <div
      v-if="msgPageNum > 1 || totalMessages > msgPageSize"
      class="load-more-tip"
      @click="hasMoreMessages && !isLoadingMessages && loadMoreMessages()"
    >
      <span v-if="isLoadingMessages" class="loading-text">
        <el-icon class="is-loading"><Loading /></el-icon>
        加载中...
      </span>
      <span v-else-if="hasMoreMessages" class="clickable-text">点击加载更多消息</span>
      <span v-else class="no-more-text">没有更多消息了</span>
    </div>

    <!-- 消息列表 -->
    <div
      v-for="message in messages"
      :key="message.id"
      class="message-item"
      :class="{
        'others-message': message.fromId !== currentUserId,
        'my-message': message.fromId === currentUserId,
        'message-sending': message.status === 'sending',
        'message-failed': message.status === 'failed'
      }"
      :data-message-id="message.id"
    >
      <div class="message-header">
        <span class="sender-name">{{
          message.fromId === currentUserId ? '我' : message.fromId
        }}</span>
        <span v-if="message.status === 'sending'" class="message-status">发送中...</span>
        <span v-else-if="message.status === 'failed'" class="message-status failed"
          >发送失败</span
        >
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
          {{ message.formattedTime || formatMessageTime(message.createTime || message.time) }}
        </div>

        <!-- 消息操作按钮 -->
        <el-dropdown
          trigger="click"
          @command="(cmd) => handleCommand(cmd, message)"
        >
          <el-button type="text" size="small" class="msg-action-btn">⋯</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="reply">引用</el-dropdown-item>
              <el-dropdown-item command="forward">转发</el-dropdown-item>
              <el-dropdown-item
                v-if="message.fromId === currentUserId && canRecall(message)"
                command="recall"
              >
                撤回消息
              </el-dropdown-item>
              <el-dropdown-item command="copy">复制</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 重发按钮 -->
        <el-button
          v-if="message.status === 'failed' && message.canResend"
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

    <!-- 空数据提示 -->
    <div v-if="messages.length === 0" class="empty-chat">
      <el-empty description="暂无聊天记录"></el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Loading } from '@element-plus/icons-vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  currentUserId: {
    type: [String, Number],
    required: true
  },
  msgPageNum: Number,
  msgPageSize: Number,
  totalMessages: Number,
  hasMoreMessages: Boolean,
  isLoadingMessages: Boolean,
  canRecall: Function,
  formatMessageTime: Function
})

const emit = defineEmits(['load-more', 'command', 'resend'])

const containerRef = ref(null)

const loadMoreMessages = () => {
  emit('load-more')
}

const handleCommand = (cmd, message) => {
  emit('command', cmd, message)
}

defineExpose({
  containerRef
})
</script>

<style scoped lang="less">
.messages-container {
  flex: 1;
  padding: 11px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;

  .load-more-tip {
    text-align: center;
    padding: 10px;
    margin-bottom: 10px;
    font-size: 12px;
    color: #909399;
    cursor: pointer;
    user-select: none;
    background-color: #f5f7fa;
    border-radius: 4px;

    .loading-text {
      color: #409eff;
    }

    .clickable-text {
      color: #409eff;
      &:hover {
        text-decoration: underline;
      }
    }

    .no-more-text {
      color: #c0c4cc;
    }
  }

  .message-item {
    margin-bottom: 16px;
    max-width: 70%;

    .message-header {
      margin-bottom: 4px;
      .sender-name {
        font-size: 12px;
        color: #666;
      }

      .message-status {
        font-size: 11px;
        margin-left: 8px;
        color: #909399;

        &.failed {
          color: #f56c6c;
        }
      }
    }

    .message-content {
      border-radius: 10px;
      padding: 7px;
      font-size: 12px;
      position: relative;

      .message-reply-quote {
        display: flex;
        gap: 8px;
        padding: 8px;
        margin-bottom: 8px;
        background-color: rgba(0, 0, 0, 0.05);
        border-radius: 6px;
        border-left: 3px solid #ddd;

        .quote-bar {
          width: 3px;
          background-color: #ddd;
          border-radius: 2px;
        }

        .quote-content {
          flex: 1;
          min-width: 0;

          .quote-author {
            font-size: 11px;
            font-weight: 500;
            color: #606266;
            margin-bottom: 4px;
          }

          .quote-text {
            font-size: 11px;
            color: #909399;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }

      .message-time {
        text-align: right;
        font-size: 10px;
        margin-top: 4px;
        opacity: 0.8;
      }

      .msg-action-btn {
        position: absolute;
        top: 5px;
        right: 5px;
        opacity: 0;
        transition: opacity 0.2s;
        padding: 2px 8px;
        font-size: 14px;
      }

      .resend-btn {
        margin-top: 4px;
        padding: 2px 8px;
        font-size: 11px;
      }

      &:hover .msg-action-btn {
        opacity: 0.6;
      }
    }

    &.others-message {
      align-self: flex-start;

      .message-content {
        background-color: #fff;
        border: 1px solid #ddd;

        .message-time {
          color: #909399;
        }
      }
    }

    &.my-message {
      align-self: flex-end;

      .message-content {
        background-color: #67c23a;
        color: #fff;

        .message-time {
          opacity: 0.8;
        }
      }
    }

    &.message-sending {
      opacity: 0.6;

      .message-content {
        background-color: #e0e0e0;
        color: #666;
      }
    }

    &.message-failed {
      .message-content {
        background-color: #fef0f0;
        border: 1px solid #fbc4c4;
        color: #f56c6c;
      }
    }
  }

  .empty-chat {
    text-align: center;
    margin-top: 50px;
  }
}
</style>
