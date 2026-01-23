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
      <div class="message-text">{{ content }}</div>
      <div class="message-time">{{ time }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
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
  }
})
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

    .message-text {
      max-width: 75%;
      padding: 14px 18px;
      border-radius: 20px;
      line-height: 1.7;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      font-size: 15px;
      white-space: pre-wrap;
      word-break: break-word;

      &:hover {
        transform: translateY(-2px) scale(1.01);
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
