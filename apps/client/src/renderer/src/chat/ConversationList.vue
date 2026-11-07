<template>
  <div class="conversation-list">
    <!-- 会话列表 -->
    <div v-if="conversations.length > 0" class="conversation-list-scroll">
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
          <div v-if="getImageAvatar(conversation.avatar)">
            <img :src="getImageAvatar(conversation.avatar)" alt="" />
          </div>
          <div
            v-else
            class="emoji-avatar"
            :class="conversation.type === 'group' ? 'group-avatar' : 'single-avatar'"
          >
            {{ conversation.type === 'group' ? '👥' : '💬' }}
          </div>
          <div v-if="conversation.unreadCount > 0" class="unread-count">
            {{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}
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
    <div v-else class="empty-conversations" @click="$emit('create-new')">
      <div class="empty-icon">📭</div>
      <p class="empty-title">暂无会话</p>
      <p class="empty-tip">点击此处或上方"新建"按钮开始对话</p>
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

import { getAvatarUrl } from '@/utils/avatar'

defineEmits(['select', 'contextmenu', 'toggle-pin', 'create-new'])

const normalizeImageAvatar = (avatar) => {
  if (!avatar) return ''

  const normalizedAvatar = String(avatar).replace(/\\/g, '/').trim()
  if (/^https?:/.test(normalizedAvatar) || /^data:image/.test(normalizedAvatar)) {
    return normalizedAvatar
  }

  return getAvatarUrl(normalizedAvatar)
}

const getImageAvatar = (avatar) => normalizeImageAvatar(avatar)
</script>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.conversation-list {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  .conversation-list-scroll {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: @nordic-divider;
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: darken(@nordic-border, 8%);
      border-radius: 3px;

      &:hover {
        background: darken(@nordic-border, 18%);
      }
    }

    .conversation-item {
      display: flex;
      align-items: center;
      padding: 12px 14px;
      cursor: pointer;
      border-bottom: 1px solid @nordic-divider;
      transition: all @nordic-transition-base ease;
      position: relative;
      flex-shrink: 0;

      &:hover {
        background:
          linear-gradient(90deg, #fbf7f2 0%, rgba(255, 255, 255, 0.98) 88%);
        box-shadow: inset 3px 0 0 fade(@nordic-accent, 35%);
      }

      &:active {
        transform: scale(0.995);
      }

      &.active {
        background:
          linear-gradient(90deg, #f7efe7 0%, rgba(255, 255, 255, 0.98) 90%);
        border-left: 3px solid @nordic-accent;
        box-shadow: inset 0 0 0 1px fade(@nordic-accent, 18%);
      }

      &.pinned-conversation {
        background:
          linear-gradient(90deg, #fbf6ea 0%, rgba(255, 255, 255, 0.97) 88%);
        border-left: 3px solid @nordic-yellow;

        &:hover {
          background:
            linear-gradient(90deg, #f8f1df 0%, rgba(255, 255, 255, 0.98) 88%);
        }
      }

      .pin-btn {
        position: absolute;
        top: 10px;
        right: 10px;
        width: 24px;
        height: 24px;
        border-radius: 50%;
        background: fade(@nordic-surface, 92%);
        border: 1px solid @nordic-border;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 11px;
        cursor: pointer;
        opacity: 0;
        color: @nordic-text-muted;
        transition: all @nordic-transition-fast ease;

        &:hover {
          opacity: 1;
          color: @nordic-accent-dark;
          border-color: fade(@nordic-accent, 35%);
          background: #fff8f3;
          transform: scale(1.05);
        }
      }

      &:hover .pin-btn {
        opacity: 1;
      }

      .conversation-avatar {
        margin-right: 12px;
        position: relative;

        img {
          width: 42px;
          height: 42px;
          border-radius: 14px;
          object-fit: contain;
          aspect-ratio: 1 / 1;
          background: #f7f2ec;
          border: 1px solid fade(@nordic-border, 85%);
          box-shadow: 0 8px 18px rgba(80, 58, 42, 0.08);
        }

        .emoji-avatar {
          width: 42px;
          height: 42px;
          border-radius: 14px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 18px;
          text-align: center;
          border: 1px solid fade(@nordic-border, 80%);
          box-shadow: 0 8px 18px rgba(80, 58, 42, 0.08);
        }

        .single-avatar {
          background:
            radial-gradient(circle at 30% 30%, #fffdfa 0%, #f5ece4 65%, #edd9ca 100%);
        }

        .group-avatar {
          background:
            radial-gradient(circle at 35% 30%, #f5fbf4 0%, #e6f0e7 58%, #cfe1d1 100%);
        }

        .unread-count {
          background: @nordic-red;
          color: #fff;
          border-radius: 10px;
          padding: 2px 6px;
          font-size: 10px;
          position: absolute;
          top: -2px;
          right: -3px;
          transform: translate(0, 0);
          z-index: 1;
          min-width: 16px;
          text-align: center;
          font-weight: 600;
          border: 2px solid @nordic-surface;
          box-shadow: 0 4px 12px fade(@nordic-red, 24%);
        }

        .group-tag {
          background: fade(@nordic-green, 14%);
          color: @nordic-green-dark;
          font-size: 8px;
          padding: 2px 5px;
          border-radius: 999px;
          position: absolute;
          bottom: -4px;
          right: -4px;
          z-index: 2;
          font-weight: 600;
          border: 1px solid fade(@nordic-green, 28%);
          box-shadow: 0 4px 10px rgba(76, 122, 77, 0.12);
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
          font-size: @nordic-text-sm;

          .name {
            font-weight: 600;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            flex: 1;
            margin-right: 6px;
            color: @nordic-text;

            .member-count {
              font-size: 10px;
              color: @nordic-text-muted;
              font-weight: 500;
            }
          }

          .time {
            font-size: 10px;
            white-space: nowrap;
            color: @nordic-text-muted;
            font-weight: 400;
          }
        }

        .last-message {
          font-size: @nordic-text-xs;
          color: @nordic-text-secondary;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          line-height: 1.4;
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
    min-height: 400px;
    cursor: pointer;
    user-select: none;
    transition: all @nordic-transition-base ease;
    background:
      radial-gradient(circle at top, rgba(212, 132, 90, 0.08) 0%, transparent 42%);

    &:hover {
      background:
        radial-gradient(circle at top, rgba(212, 132, 90, 0.12) 0%, transparent 45%),
        linear-gradient(180deg, #fdfaf6 0%, #f7f1ea 100%);

      .empty-icon {
        transform: translateY(-2px) scale(1.04);
      }

      .empty-title {
        color: @nordic-accent-dark;
      }

      .empty-tip {
        color: @nordic-text-secondary;
      }
    }

    &:active {
      transform: scale(0.98);
    }

    .empty-icon {
      width: 92px;
      height: 92px;
      margin-bottom: 22px;
      border-radius: 28px;
      background:
        radial-gradient(circle at 30% 30%, #fffefb 0%, #f2e6da 68%, #ead3bf 100%);
      color: @nordic-accent-dark;
      box-shadow:
        inset 0 1px 0 rgba(255, 255, 255, 0.75),
        0 14px 30px rgba(114, 82, 58, 0.08);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 44px;
      transition: transform @nordic-transition-base ease;
    }

    .empty-title {
      font-size: @nordic-text-lg;
      font-weight: 600;
      color: @nordic-text;
      margin: 0 0 8px 0;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: color 0.3s ease;
    }

    .empty-tip {
      max-width: 220px;
      font-size: @nordic-text-base;
      color: @nordic-text-secondary;
      margin: 0;
      line-height: 1.6;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: color 0.3s ease;
    }
  }
}
</style>
