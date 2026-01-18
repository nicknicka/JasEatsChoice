<template>
  <div class="conversation-list">
    <div class="conversation-list-scroll">
      <div
        v-for="conversation in conversations"
        :key="conversation.id"
        class="conversation-item"
        :class="{
          active: modelValue?.id === conversation.id,
          'pinned-conversation': conversation.pinned
        }"
        @click="$emit('select', conversation)"
        @contextmenu.prevent="$emit('contextmenu', conversation, $event)"
      >
        <div class="conversation-avatar">
          <div v-if="conversation.avatar && conversation.avatar.match(/^https?:/)">
            <img :src="conversation.avatar" alt="" />
          </div>
          <div v-else class="emoji-avatar">
            {{ conversation.avatar || (conversation.type === 'group' ? '👥' : '💬') }}
          </div>
          <div v-if="conversation.unreadCount > 0" class="unread-count">
            {{ conversation.unreadCount }}
          </div>
          <!-- 群聊标签 -->
          <div v-if="conversation.type === 'group'" class="group-tag">群聊</div>
        </div>

        <!-- 置顶按钮 - 仅支持私聊 -->
        <div
          v-if="conversation.type !== 'group'"
          class="pin-btn"
          @click.stop="$emit('toggle-pin', conversation)"
          :title="conversation.pinned ? '取消置顶' : '置顶会话'"
        >
          {{ conversation.pinned ? '📌' : '📌' }}
        </div>

        <div class="conversation-info">
          <div class="name-time">
            <span class="name">
              {{ conversation.name }}
              <span v-if="conversation.type === 'group'" class="member-count">
                ({{ conversation.memberCount || '0' }}人)</span
              >
            </span>
            <span class="time">{{ conversation.time }}</span>
          </div>
          <div class="last-message">{{ conversation.lastMessage || '暂无消息' }}</div>
        </div>
      </div>
    </div>

    <!-- 会话列表空数据提示 -->
    <div v-if="conversations.length === 0" class="empty-conversations">
      <div class="empty-icon">📭</div>
      <p class="empty-title">暂无会话</p>
      <p class="empty-tip">点击上方"新建聊天"按钮开始对话</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  conversations: {
    type: Array,
    default: () => []
  },
  modelValue: {
    type: Object,
    default: null
  }
})

defineEmits(['select', 'contextmenu', 'toggle-pin'])
</script>

<style scoped lang="less">
.conversation-list {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .conversation-list-scroll {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: #f1f1f1;
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: #c1c1c1;
      border-radius: 3px;

      &:hover {
        background: #a8a8a8;
      }
    }

    .conversation-item {
      display: flex;
      align-items: center;
      padding: 16px;
      cursor: pointer;
      border-bottom: 1px solid #f0f2f5;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;
      flex-shrink: 0;

      &:hover {
        background-color: #f5f7fa;
        transform: translateX(2px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      }

      &:active {
        transform: translateX(1px) scale(0.99);
      }

      &.active {
        background: linear-gradient(90deg, #ecf5ff 0%, #f0f7ff 100%);
        border-left: 3px solid #409eff;
        box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
      }

      &.pinned-conversation {
        background: linear-gradient(90deg, #fffbe6 0%, #fffcf5 100%);
        border-left: 3px solid #ffd591;

        &:hover {
          background: linear-gradient(90deg, #fff7e6 0%, #fffaf0 100%);
        }
      }

      .pin-btn {
        position: absolute;
        top: 8px;
        right: 8px;
        font-size: 14px;
        cursor: pointer;
        opacity: 0;
        transition: all 0.2s;
        padding: 4px;

        &:hover {
          opacity: 1;
          transform: scale(1.2);
        }
      }

      &:hover .pin-btn {
        opacity: 0.6;
      }

      .conversation-avatar {
        margin-right: 12px;
        position: relative;

        img {
          width: 45px;
          height: 45px;
          border-radius: 8px;
          object-fit: contain;
          aspect-ratio: 1 / 1;
          box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
        }

        .emoji-avatar {
          width: 45px;
          height: 45px;
          border-radius: 8px;
          background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          text-align: center;
          box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
        }

        .unread-count {
          background: linear-gradient(135deg, #f56c6c 0%, #e85a5a 100%);
          color: #fff;
          border-radius: 10px;
          padding: 2px 6px;
          font-size: 11px;
          position: absolute;
          top: -4px;
          right: -4px;
          transform: translate(0, 0);
          z-index: 1;
          min-width: 18px;
          text-align: center;
          font-weight: 600;
          box-shadow: 0 2px 4px rgba(245, 108, 108, 0.4);
          animation: pulse 2s infinite;
        }

        .group-tag {
          background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
          color: #fff;
          font-size: 9px;
          padding: 2px 6px;
          border-radius: 4px;
          position: absolute;
          bottom: -4px;
          right: -4px;
          z-index: 2;
          font-weight: 500;
          box-shadow: 0 2px 4px rgba(64, 158, 255, 0.3);
        }
      }

      .conversation-info {
        flex: 1;
        min-width: 0;

        .name-time {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 6px;
          font-size: 14px;

          .name {
            font-weight: 600;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            flex: 1;
            margin-right: 8px;
            color: #303133;

            .member-count {
              font-size: 11px;
              color: #909399;
              font-weight: 400;
            }
          }

          .time {
            font-size: 11px;
            white-space: nowrap;
            color: #909399;
            font-weight: 400;
          }
        }

        .last-message {
          font-size: 12px;
          color: #606266;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          line-height: 1.5;
        }
      }
    }
  }

  .empty-conversations {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    padding: 60px 20px;

    .empty-icon {
      font-size: 64px;
      margin-bottom: 16px;
      opacity: 0.6;
    }

    .empty-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .empty-tip {
      font-size: 13px;
      color: #909399;
      margin: 0;
      line-height: 1.6;
    }
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}
</style>
