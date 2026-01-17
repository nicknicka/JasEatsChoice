<template>
  <div class="conversation-list">
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

    <!-- 会话列表空数据提示 -->
    <div v-if="conversations.length === 0" class="empty-conversations">
      <el-empty description="暂无会话"></el-empty>
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
  width: 37%;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;

  .conversation-item {
    display: flex;
    align-items: center;
    padding: 16px;
    cursor: pointer;
    border-bottom: 1px solid #e4e7ed;
    transition: background-color 0.3s;
    position: relative;

    &:hover {
      background-color: #f5f7fa;
    }

    &.active {
      background-color: #ecf5ff;
    }

    &.pinned-conversation {
      background-color: #fffbe6;
      border-left: 3px solid #ffd591;
    }

    .pin-btn {
      position: absolute;
      top: 8px;
      right: 8px;
      font-size: 14px;
      cursor: pointer;
      opacity: 0;
      transition: opacity 0.2s;

      &:hover {
        opacity: 1;
      }
    }

    &:hover .pin-btn {
      opacity: 0.5;
    }

    .conversation-avatar {
      margin-right: 11px;
      position: relative;

      img {
        width: 35px;
        height: 35px;
        border-radius: 7px;
        object-fit: contain;
        aspect-ratio: 1 / 1;
      }

      .emoji-avatar {
        width: 35px;
        height: 35px;
        border-radius: 7px;
        background-color: #f0f0f0;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        text-align: center;
      }

      .unread-count {
        background-color: #f56c6c;
        width: 10px;
        height: 10px;
        color: #fff;
        border-radius: 50%;
        padding: 2px;
        font-size: 7px;
        position: absolute;
        top: 0;
        right: 0;
        transform: translate(50%, -50%);
        z-index: 1;
        min-height: 7px;
        min-width: 7px;
        text-align: center;
      }

      .group-tag {
        background-color: #409eff;
        color: #fff;
        font-size: 8px;
        padding: 1px 4px;
        border-radius: 3px;
        position: absolute;
        top: 0;
        right: 0;
        transform: translate(0, 0);
        z-index: 2;
      }
    }

    .conversation-info {
      flex: 1;
      min-width: 0;

      .name-time {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 4px;
        font-size: 14px;

        .name {
          font-weight: 500;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          flex: 1;
          margin-right: 8px;

          .member-count {
            font-size: 8px;
            color: #909399;
          }
        }

        .time {
          font-size: 8px;
          white-space: nowrap;
          color: #909399;
        }
      }

      .last-message {
        font-size: 10px;
        color: #606266;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }

  .empty-conversations {
    text-align: center;
    margin-top: 50px;
  }
}
</style>
