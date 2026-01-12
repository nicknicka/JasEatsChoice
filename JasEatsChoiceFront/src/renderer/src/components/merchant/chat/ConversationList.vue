<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'

const props = defineProps({
  conversations: {
    type: Array,
    default: () => []
  },
  selectedConversation: {
    type: Object,
    default: null
  },
  searchKeyword: {
    type: String,
    default: ''
  },
  showUnreadOnly: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select', 'search', 'filter'])

// 过滤后的会话列表
const filteredConversations = computed(() => {
  let result = props.conversations

  // 搜索过滤
  if (props.searchKeyword) {
    const keyword = props.searchKeyword.toLowerCase()
    result = result.filter(conv =>
      conv.name.toLowerCase().includes(keyword) ||
      conv.lastMessage.toLowerCase().includes(keyword)
    )
  }

  // 仅显示未读
  if (props.showUnreadOnly) {
    result = result.filter(conv => conv.unreadCount > 0)
  }

  // 按未读消息排序
  return result.sort((a, b) => {
    if (a.unreadCount > 0 && b.unreadCount === 0) return -1
    if (a.unreadCount === 0 && b.unreadCount > 0) return 1
    return 0
  })
})

// 选择会话
const selectConversation = (conversation) => {
  emit('select', conversation)
  // 清空未读消息
  if (conversation.unreadCount > 0) {
    conversation.unreadCount = 0
    ElMessage.success('消息已标记为已读')
  }
}

// 获取头像显示内容
const getAvatarContent = (avatar) => {
  if (avatar.match(/^https?:/)) {
    return { type: 'image', content: avatar }
  }
  return { type: 'emoji', content: avatar }
}
</script>

<template>
  <div class="conversation-list-wrapper">
    <!-- 会话列表头部 -->
    <div class="list-header">
      <div class="header-title">
        <el-icon :size="18"><ChatDotRound /></el-icon>
        <span>会话列表</span>
        <el-badge v-if="filteredConversations.length > 0" :value="filteredConversations.length" class="item" />
      </div>
    </div>

    <!-- 会话列表 -->
    <div class="conversation-list">
      <div
        v-for="conversation in filteredConversations"
        :key="conversation.id"
        class="conversation-item"
        :class="{
          active: selectedConversation?.id === conversation.id,
          'has-unread': conversation.unreadCount > 0,
          'has-order': conversation.relatedOrder
        }"
        @click="selectConversation(conversation)"
      >
        <div class="conversation-avatar">
          <img
            v-if="getAvatarContent(conversation.avatar).type === 'image'"
            :src="conversation.avatar"
            :alt="conversation.name"
            class="avatar-image"
          />
          <div v-else class="emoji-avatar">
            {{ conversation.avatar }}
          </div>

          <!-- 未读消息徽章 -->
          <div v-if="conversation.unreadCount > 0" class="unread-badge">
            {{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}
          </div>

          <!-- 订单指示器 -->
          <div v-if="conversation.relatedOrder" class="order-indicator" title="有关联订单">
            📋
          </div>
        </div>

        <div class="conversation-info">
          <div class="name-time">
            <span class="name">{{ conversation.name }}</span>
            <span class="time">{{ conversation.time }}</span>
          </div>
          <div class="last-message">{{ conversation.lastMessage }}</div>
          <div v-if="conversation.type === 'group' && conversation.memberCount" class="member-count">
            {{ conversation.memberCount }}人
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredConversations.length === 0" class="empty-conversations">
        <el-empty :description="searchKeyword ? '未找到匹配的会话' : '暂无会话'" />
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.conversation-list-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #e8eef5;

  .list-header {
    padding: 18px 20px;
    border-bottom: 1px solid #e8eef5;
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    flex-shrink: 0;

    .header-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 15px;
      font-weight: 600;
      color: #1f2937;

      .el-badge {
        margin-left: auto;
      }
    }
  }

  .conversation-list {
    flex: 1;
    overflow-y: auto;

    .conversation-item {
      display: flex;
      align-items: center;
      padding: 14px 16px;
      cursor: pointer;
      border-bottom: 1px solid #f3f4f6;
      transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;

      &:last-child {
        border-bottom: none;
      }

      &:hover {
        background-color: #f8f9fa;
        transform: translateX(2px);
      }

      &.active {
        background: linear-gradient(135deg, #ecf5ff 0%, #e3f2fd 100%);
        border-left: 3px solid #667eea;
        padding-left: 13px;
      }

      &.has-unread {
        background-color: #fef2f2;
      }

      .conversation-avatar {
        position: relative;
        margin-right: 14px;
        flex-shrink: 0;

        .avatar-image {
          width: 50px;
          height: 50px;
          border-radius: 50%;
          object-fit: cover;
        }

        .emoji-avatar {
          width: 50px;
          height: 50px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
          border-radius: 50%;
          transition: transform 0.2s ease;
        }

        .unread-badge {
          position: absolute;
          top: -4px;
          right: -4px;
          background: linear-gradient(135deg, #f56c6c 0%, #ff8787 100%);
          color: #ffffff;
          border-radius: 12px;
          padding: 2px 7px;
          font-size: 11px;
          font-weight: 600;
          box-shadow: 0 2px 8px rgba(245, 108, 108, 0.4);
          border: 2px solid #fff;
          min-width: 18px;
          text-align: center;
        }

        .order-indicator {
          position: absolute;
          bottom: -2px;
          right: -2px;
          width: 20px;
          height: 20px;
          background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 10px;
          box-shadow: 0 2px 6px rgba(64, 158, 255, 0.4);
          border: 2px solid #fff;
        }
      }

      .conversation-info {
        flex: 1;
        min-width: 0;

        .name-time {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 5px;

          .name {
            font-weight: 600;
            font-size: 14px;
            color: #1f2937;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .time {
            font-size: 11px;
            color: #9ca3af;
            font-weight: 500;
            flex-shrink: 0;
            margin-left: 8px;
          }
        }

        .last-message {
          font-size: 13px;
          color: #6b7280;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          line-height: 1.4;
        }

        .member-count {
          font-size: 11px;
          color: #909399;
          margin-top: 2px;
        }
      }
    }

    .empty-conversations {
      padding: 40px 20px;
      text-align: center;
    }
  }
}

// 滚动条样式
.conversation-list::-webkit-scrollbar {
  width: 6px;
}

.conversation-list::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.conversation-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.conversation-list::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
