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
        <div
          v-else
          class="image-wrapper"
          @mouseenter="!isMobile && (showImageInfo = true)"
          @mouseleave="!isMobile && (showImageInfo = false)"
          :class="{ 'is-mobile': isMobile }"
        >
          <el-image
            :src="message.fullUrl || message.fileUrl"
            :preview-src-list="[message.fullUrl || message.fileUrl]"
            :fit="imageFit"
            :class="['message-image', imageOrientation]"
            lazy
            @load="handleImageLoad"
            @error="handleImageError"
          >
            <template #placeholder>
              <div class="image-placeholder">
                <el-icon class="is-loading"><Loading /></el-icon>
              </div>
            </template>
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
                <span>图片加载失败</span>
              </div>
            </template>
          </el-image>

          <!-- 图片信息遮罩 -->
          <transition name="fade">
            <div v-if="showImageInfo || isMobile" class="image-info-overlay" :class="{ 'always-show': isMobile }">
              <div class="image-info">
                <span v-if="imageDimensions" class="image-dimensions">{{ imageDimensions }}</span>
                <span v-if="message.fileSize" class="file-size">{{ formatFileSize(message.fileSize) }}</span>
              </div>
              <el-button
                type="primary"
                size="small"
                circle
                @click.stop="downloadImage"
                class="download-btn"
                title="下载图片"
              >
                <el-icon><Download /></el-icon>
              </el-button>
            </div>
          </transition>
        </div>
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
import { computed, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, Document, Download, Loading } from '@element-plus/icons-vue'

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

// ========== 移动端检测 ==========
const isMobile = ref(false)

// ========== 初始化和调试日志 ==========
onMounted(() => {
  // 检测是否为移动端
  isMobile.value = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)

  // 调试日志：打印图片消息信息
  if (props.message.msgType === 'image') {
    console.log('🖼️ [MessageItem] 渲染图片消息', {
      msgId: props.message.id,
      msgType: props.message.msgType,
      content: props.message.content,
      fileUrl: props.message.fileUrl,
      fullUrl: props.message.fullUrl,
      fileName: props.message.fileName,
      最终URL: props.message.fullUrl || props.message.fileUrl
    })
  }
})

// ========== 文件扩展名获取 ==========
const getFileExtension = (url) => {
  if (!url) return 'jpg'
  const match = url.match(/\.([^.?]+)(?:\?|$)/)
  return match ? match[1].toLowerCase() : 'jpg'
}

// 图片相关响应式变量
const showImageInfo = ref(false)
const imageDimensions = ref(null)
const imageOrientation = ref('landscape')
const imageFit = ref('cover')

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

// 处理图片加载完成
const handleImageLoad = (e) => {
  const img = e.target || e
  const width = img.naturalWidth || img.width
  const height = img.naturalHeight || img.height

  // 设置图片尺寸信息
  imageDimensions.value = `${width} x ${height}`

  // 根据图片比例确定方向和适配方式
  const ratio = width / height

  if (ratio > 1.5) {
    // 横图：宽度优先
    imageOrientation.value = 'landscape'
    imageFit.value = 'cover'
  } else if (ratio < 0.67) {
    // 竖图：高度优先
    imageOrientation.value = 'portrait'
    imageFit.value = 'cover'
  } else {
    // 方图或接近方形
    imageOrientation.value = 'square'
    imageFit.value = 'cover'
  }
}

// 处理图片加载错误
const handleImageError = () => {
  console.error('图片加载失败:', props.message.fileUrl)
  imageDimensions.value = null
}

// 下载图片（改进版 - 支持跨域）
const downloadImage = async () => {
  const imageUrl = props.message.fullUrl || props.message.fileUrl
  if (!imageUrl) {
    ElMessage.error('图片地址无效')
    return
  }

  const extension = getFileExtension(imageUrl)
  const fileName = props.message.fileName || `image_${Date.now()}.${extension}`

  ElMessage.info({
    message: '正在下载图片...',
    duration: 1000
  })

  try {
    // 使用 fetch API 下载（支持跨域）
    const response = await fetch(imageUrl, {
      mode: 'cors',
      credentials: 'include'
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)

    // 创建临时下载链接
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    link.style.display = 'none'
    document.body.appendChild(link)

    // 触发下载
    link.click()

    // 清理
    setTimeout(() => {
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    }, 100)

    ElMessage.success({
      message: '下载成功',
      duration: 2000
    })
  } catch (error) {
    console.error('下载图片失败:', error)

    // 降级方案：在新标签页打开
    ElMessage.warning({
      message: '自动下载失败，正在打开图片...',
      duration: 2000
    })

    setTimeout(() => {
      window.open(imageUrl, '_blank')
      ElMessage.info('请右键点击图片选择"图片另存为"')
    }, 500)
  }
}

// 处理文件下载（改进版 - 支持跨域）
const handleDownloadFile = async () => {
  const fileUrl = props.message.fileUrl || props.message.fullUrl
  if (!fileUrl) {
    ElMessage.error('文件地址无效')
    return
  }

  const fileName = props.message.fileName || '下载文件'

  ElMessage.info({
    message: '正在下载文件...',
    duration: 1000
  })

  try {
    // 使用 fetch API 下载
    const response = await fetch(fileUrl, {
      mode: 'cors',
      credentials: 'include'
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    link.style.display = 'none'
    document.body.appendChild(link)
    link.click()

    setTimeout(() => {
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    }, 100)

    ElMessage.success({
      message: '下载成功',
      duration: 2000
    })
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.warning('自动下载失败，正在打开文件...')
    setTimeout(() => {
      window.open(fileUrl, '_blank')
    }, 500)
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
    font-size: 0.857rem /* 原值: 12px */;
    color: #999;
    display: flex;
    align-items: center;
    gap: 8px;

    .sender-name {
      font-weight: 500;
    }

    .message-status {
      font-size: 0.75rem /* 原值: 11px */;

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
          font-size: 0.857rem /* 原值: 12px */;
          color: #909399;
        }

        :deep(.el-skeleton) {
          background-color: #f5f7fa;
          border-radius: 8px;
          padding: 8px;
        }
      }

      .image-wrapper {
        position: relative;
        display: inline-block;
        border-radius: 12px;
        overflow: hidden;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover {
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
          transform: translateY(-2px);

          .message-image {
            transform: scale(1.02);
          }
        }

        // 移动端样式
        &.is-mobile {
          .image-info-overlay {
            opacity: 1 !important;
            background: linear-gradient(
              to top,
              rgba(0, 0, 0, 0.75) 0%,
              rgba(0, 0, 0, 0.6) 50%,
              transparent 100%
            );
            pointer-events: auto;

            &.always-show {
              position: absolute;
              bottom: 0;
              left: 0;
              right: 0;
              padding: 10px 12px;
            }
          }
        }

        .message-image {
          display: block;
          border-radius: 12px;
          cursor: pointer;
          transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

          // 根据图片方向调整尺寸
          &.landscape {
            max-width: 400px;
            max-height: 300px;
          }

          &.portrait {
            max-width: 250px;
            max-height: 400px;
          }

          &.square {
            max-width: 300px;
            max-height: 300px;
          }

          :deep(.el-image__inner) {
            width: 100%;
            height: 100%;
            object-fit: cover;
            border-radius: 12px;
            // 添加渐显动画
            animation: image-fadein 0.4s ease-out;
          }
        }

        // 图片加载占位符
        .image-placeholder {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 200px;
          height: 150px;
          background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
          border-radius: 12px;
          color: #909399;

          .el-icon {
            font-size: 2.286rem /* 原值: 32px */;
            animation: spin 1s linear infinite;
          }
        }

        // 图片信息遮罩层
        .image-info-overlay {
          position: absolute;
          bottom: 0;
          left: 0;
          right: 0;
          background: linear-gradient(to top, rgba(0, 0, 0, 0.7) 0%, transparent 100%);
          padding: 12px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 8px;
          border-radius: 0 0 12px 12px;

          .image-info {
            display: flex;
            flex-direction: column;
            gap: 4px;
            color: #fff;
            font-size: 0.857rem /* 原值: 12px */;

            .image-dimensions {
              font-weight: 500;
            }

            .file-size {
              opacity: 0.8;
              font-size: 0.75rem /* 原值: 11px */;
            }
          }

          .download-btn {
            flex-shrink: 0;
            background: rgba(255, 255, 255, 0.2);
            border: 1px solid rgba(255, 255, 255, 0.3);
            backdrop-filter: blur(10px);
            transition: all 0.3s ease;
            min-width: 36px;
            min-height: 36px;

            &:hover {
              background: rgba(255, 255, 255, 0.3);
              border-color: rgba(255, 255, 255, 0.5);
              transform: scale(1.1);
            }

            &:active {
              transform: scale(0.95);
            }

            // 移动端下载按钮样式优化
            .image-wrapper.is-mobile & {
              min-width: 40px;
              min-height: 40px;

              &:active {
                transform: scale(0.9);
                background: rgba(255, 255, 255, 0.4);
              }
            }
          }
        }

        // 渐显动画
        .fade-enter-active,
        .fade-leave-active {
          transition: opacity 0.3s ease;
        }

        .fade-enter-from,
        .fade-leave-to {
          opacity: 0;
        }
      }

      .image-error {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        width: 200px;
        height: 150px;
        background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
        border-radius: 12px;
        color: #f56c6c;
        gap: 8px;
        box-shadow: 0 2px 8px rgba(245, 108, 108, 0.1);

        .el-icon {
          font-size: 2.286rem /* 原值: 32px */;
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
            font-size: 1rem /* 原值: 14px */;
            font-weight: 500;
            color: #303133;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .file-size {
            font-size: 0.857rem /* 原值: 12px */;
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
          font-size: 0.857rem /* 原值: 12px */;
          font-weight: 500;
          color: #409eff;
          margin-bottom: 4px;
        }

        .quote-text {
          font-size: 0.929rem /* 原值: 13px */;
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
        font-size: 0.75rem /* 原值: 11px */;
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
        font-size: 1.143rem /* 原值: 16px */;
        padding: 0;
        width: 20px;
        height: 20px;
        flex-shrink: 0;
      }

      .resend-btn {
        font-size: 0.857rem /* 原值: 12px */;
        flex-shrink: 0;
      }
    }

    &:hover .message-footer .msg-action-btn {
      opacity: 1;
    }
  }
}

// 全局动画关键帧
@keyframes image-fadein {
  from {
    opacity: 0;
    filter: blur(10px);
  }
  to {
    opacity: 1;
    filter: blur(0);
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
