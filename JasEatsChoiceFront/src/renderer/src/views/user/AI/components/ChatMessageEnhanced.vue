<template>
  <div
    class="chat-message"
    :class="{
      'user-message': sender === 'user',
      'ai-message': sender === 'ai'
    }"
  >
    <div class="message-avatar">{{ avatar }}</div>
    <div class="message-content">
      <div class="message-header">
        <span class="message-sender">{{ senderName }}</span>
        <el-dropdown trigger="click" @command="handleCommand">
          <el-icon class="more-icon"><MoreFilled /></el-icon>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="copy">复制消息</el-dropdown-item>
              <el-dropdown-item command="copyPlain" v-if="isMarkdown">复制纯文本</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <div
        class="message-text"
        :class="{ 'markdown-content': isMarkdown && renderMarkdown }"
        v-html="displayContent"
      ></div>

      <div class="message-time">{{ time }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { MoreFilled } from '@element-plus/icons-vue'
import { parseMarkdown, stripMarkdown, hasMarkdownSyntax } from '../../../utils/markdownParser'

const props = defineProps({
  id: {
    type: Number,
    required: true
  },
  sender: {
    type: String,
    required: true,
    validator: (value) => ['user', 'ai'].includes(value)
  },
  content: {
    type: String,
    required: true
  },
  time: {
    type: String,
    required: true
  },
  avatar: {
    type: String,
    required: true
  },
  renderMarkdown: {
    type: Boolean,
    default: true
  }
})

const senderName = computed(() => {
  return props.sender === 'ai' ? 'AI助手' : '我'
})

const isMarkdown = computed(() => {
  return hasMarkdownSyntax(props.content)
})

const displayContent = computed(() => {
  if (props.renderMarkdown && isMarkdown.value) {
    return parseMarkdown(props.content)
  }
  return props.content
})

const handleCommand = (command) => {
  switch (command) {
    case 'copy':
      copyToClipboard(props.content)
      break
    case 'copyPlain':
      copyToClipboard(stripMarkdown(props.content))
      break
  }
}

const copyToClipboard = async (text) => {
  try {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(text)
      ElMessage.success('已复制到剪贴板')
    } else {
      // 降级方案
      const textArea = document.createElement('textarea')
      textArea.value = text
      textArea.style.position = 'fixed'
      textArea.style.opacity = '0'
      document.body.appendChild(textArea)
      textArea.select()
      try {
        document.execCommand('copy')
        ElMessage.success('已复制到剪贴板')
      } catch (err) {
        ElMessage.error('复制失败')
      }
      document.body.removeChild(textArea)
    }
  } catch (err) {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped lang="less">
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

    .message-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;

      .message-sender {
        font-size: 12px;
        color: #909399;
        font-weight: 500;
      }

      .more-icon {
        cursor: pointer;
        font-size: 16px;
        color: #909399;
        transition: all 0.2s ease;
        padding: 4px;
        border-radius: 4px;

        &:hover {
          color: #409eff;
          background: rgba(64, 158, 255, 0.1);
        }
      }
    }

    .message-text {
      padding: 14px 18px;
      border-radius: 20px;
      line-height: 1.7;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      font-size: 15px;
      white-space: pre-wrap;
      word-break: break-word;
      overflow-x: auto;

      &:hover {
        transform: translateY(-2px) scale(1.01);
      }

      // Markdown内容样式
      &.markdown-content {
        // 标题
        :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
          margin: 16px 0 8px 0;
          font-weight: 600;
          line-height: 1.4;
        }

        :deep(h1) { font-size: 24px; }
        :deep(h2) { font-size: 20px; }
        :deep(h3) { font-size: 18px; }
        :deep(h4) { font-size: 16px; }

        // 段落
        :deep(p) {
          margin: 8px 0;
        }

        // 代码块
        :deep(pre) {
          background: rgba(0, 0, 0, 0.05);
          border-radius: 8px;
          padding: 12px;
          margin: 8px 0;
          overflow-x: auto;

          code {
            font-family: 'Courier New', monospace;
            font-size: 13px;
            line-height: 1.6;
            color: #333;
          }
        }

        // 行内代码
        :deep(code:not(pre code)) {
          background: rgba(0, 0, 0, 0.05);
          padding: 2px 6px;
          border-radius: 4px;
          font-family: 'Courier New', monospace;
          font-size: 0.9em;
        }

        // 粗体和斜体
        :deep(strong) {
          font-weight: 600;
        }

        :deep(em) {
          font-style: italic;
        }

        // 删除线
        :deep(del) {
          text-decoration: line-through;
          opacity: 0.7;
        }

        // 链接
        :deep(a) {
          color: inherit;
          text-decoration: underline;
          opacity: 0.8;
          transition: opacity 0.2s;

          &:hover {
            opacity: 1;
          }
        }

        // 列表
        :deep(ul), :deep(ol) {
          margin: 8px 0;
          padding-left: 24px;
        }

        :deep(li) {
          margin: 4px 0;
        }

        // 引用
        :deep(blockquote) {
          border-left: 4px solid rgba(0, 0, 0, 0.1);
          padding-left: 12px;
          margin: 8px 0;
          opacity: 0.8;
        }

        // 水平线
        :deep(hr) {
          border: none;
          border-top: 1px solid rgba(0, 0, 0, 0.1);
          margin: 12px 0;
        }

        // 图片
        :deep(img) {
          max-width: 100%;
          border-radius: 8px;
          margin: 8px 0;
        }
      }
    }

    .message-time {
      font-size: 12px;
      color: #a8abb2;
      margin-top: 2px;
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
</style>
