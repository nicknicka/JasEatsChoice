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

      <!-- 文本消息 -->
      <div v-if="message.msgType !== 'image' && message.msgType !== 'file'" class="text-content">
        {{ message.content }}
      </div>

      <!-- 图片消息 -->
      <div v-if="message.msgType === 'image'" class="image-content">
        <!-- 骨架屏加载中 -->
        <div v-if="message.isLoading" class="image-skeleton">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="width: 200px; height: 150px" />
            </template>
          </el-skeleton>
          <div class="loading-text">正在上传...</div>
        </div>
        <!-- 正常显示图片 -->
        <el-image
          v-else
          :src="message.fullUrl || message.fileUrl"
          :preview-src-list="[message.fullUrl || message.fileUrl]"
          fit="cover"
          class="message-image"
          lazy
        >
          <template #error>
            <div class="image-error">
              <el-icon><Picture /></el-icon>
              <span>图片加载失败</span>
            </div>
          </template>
        </el-image>
      </div>

      <!-- 文件消息 -->
      <div v-else-if="message.msgType === 'file'" class="file-content">
        <div class="file-info" @click="handleDownloadFile">
          <div class="file-icon">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="file-details">
            <div class="file-name">{{ message.fileName || message.content }}</div>
            <div class="file-size">{{ formatFileSize(message.fileSize) }}</div>
          </div>
          <el-button type="primary" size="small" text>
            <el-icon><Download /></el-icon>
            下载
          </el-button>
        </div>
      </div>

      <!-- 时间和操作区域 -->
      <div class="message-footer">
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
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Picture, Document, Download } from '@element-plus/icons-vue'

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
  if (isMyMessage.value) {
    return '我'
  }
  // 优先使用消息中的 senderName 或 fromName 字段
  return props.message.senderName || props.message.fromName || props.message.fromId
})

const formattedTime = computed(() => {
  return props.message.formattedTime || props.formatMessageTime(props.message.createTime || props.message.time)
})

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '未知大小'
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

// 处理文件下载
const handleDownloadFile = () => {
  const fileUrl = props.message.fileUrl || props.message.fullUrl
  const fileName = props.message.fileName || '下载文件'

  if (fileUrl) {
    // 创建一个隐藏的a标签来下载文件
    const link = document.createElement('a')
    link.href = fileUrl
    link.download = fileName
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
}
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

    .text-content {
      white-space: pre-wrap;
    }

    .image-content {
      .image-skeleton {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;

        .loading-text {
          font-size: 12px;
          color: #909399;
        }

        :deep(.el-skeleton) {
          background-color: #f5f7fa;
          border-radius: 8px;
          padding: 8px;
        }
      }

      .message-image {
        max-width: 300px;
        max-height: 300px;
        border-radius: 8px;
        cursor: pointer;

        :deep(.el-image__inner) {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .image-error {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        width: 200px;
        height: 150px;
        background-color: #f5f7fa;
        border-radius: 8px;
        color: #909399;
        gap: 8px;

        .el-icon {
          font-size: 32px;
        }
      }
    }

    .file-content {
      .file-info {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px;
        background-color: #f5f7fa;
        border-radius: 8px;
        cursor: pointer;
        transition: background-color 0.2s;

        &:hover {
          background-color: #e4e7ed;
        }

        .file-icon {
          color: #409eff;
          flex-shrink: 0;
        }

        .file-details {
          flex: 1;
          min-width: 0;

          .file-name {
            font-size: 14px;
            font-weight: 500;
            color: #303133;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .file-size {
            font-size: 12px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }

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

    .message-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
      margin-top: 4px;
      flex-wrap: wrap;

      .message-time {
        font-size: 11px;
        color: #999;
        display: block;
        line-height: 1.4;
        word-break: break-word;
        flex: 1;
        min-width: 0;
      }

      .msg-action-btn {
        opacity: 0;
        transition: opacity 0.2s;
        font-size: 16px;
        padding: 0;
        width: 20px;
        height: 20px;
        flex-shrink: 0;
      }

      .resend-btn {
        font-size: 12px;
        flex-shrink: 0;
      }
    }

    &:hover .message-footer .msg-action-btn {
      opacity: 1;
    }
  }
}
</style>
