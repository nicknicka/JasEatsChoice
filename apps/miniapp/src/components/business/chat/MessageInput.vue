<!--
组件名称：MessageInput
用途：聊天消息输入框
复用情况：群聊、私聊、客服聊天
创建时间：2026-03-20
-->
<template>
  <view class="message-input-container">
    <view class="reply-preview" v-if="replyMessage" @tap="cancelReply">
      <view class="reply-accent"></view>
      <view class="reply-content">
        <text class="reply-author">{{ replyMessage.author }}</text>
        <text class="reply-text">{{ replyMessage.content }}</text>
      </view>
      <view class="reply-close">
        <uni-icons type="close" size="16" color="#999" />
      </view>
    </view>

    <view class="input-area">
      <view class="input-tools" v-if="showTools">
        <button class="tool-btn" @tap="onChooseImage">
          <uni-icons type="image" size="20" color="#8A7568" />
        </button>
        <button class="tool-btn" @tap="onChooseDish" v-if="showDishButton">
          <uni-icons type="shop" size="20" color="#8A7568" />
        </button>
        <button class="tool-btn" @tap="onCreateGroupOrder" v-if="showGroupOrderButton">
          <uni-icons type="cart" size="20" color="#8A7568" />
        </button>
      </view>
      <view class="input-wrapper">
        <textarea
          class="input-field"
          v-model="inputContent"
          :placeholder="placeholder"
          :auto-height="true"
          :maxlength="maxLength"
          @focus="onFocus"
        />
      </view>
      <button class="send-btn" @tap="onSend" :disabled="!canSend">
        {{ sendButtonText }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  replyMessage: {
    type: Object,
    default: null
  },
  showTools: {
    type: Boolean,
    default: true
  },
  showDishButton: {
    type: Boolean,
    default: true
  },
  showGroupOrderButton: {
    type: Boolean,
    default: true
  },
  maxLength: {
    type: Number,
    default: 500
  },
  sendButtonText: {
    type: String,
    default: '发送'
  }
})

const emit = defineEmits([
  'send',
  'cancel-reply',
  'choose-image',
  'choose-dish',
  'create-group-order',
  'focus'
])

const inputContent = ref('')

const placeholder = computed(() => {
  return props.replyMessage ? '回复消息...' : '输入消息...'
})

const canSend = computed(() => {
  return inputContent.value.trim().length > 0
})

/**
 * 发送消息
 */
const onSend = () => {
  if (!canSend.value) return

  const content = inputContent.value.trim()
  inputContent.value = ''

  emit('send', {
    content,
    quote: props.replyMessage
  })
}

/**
 * 取消回复
 */
const cancelReply = () => {
  emit('cancel-reply')
}

/**
 * 选择图片
 */
const onChooseImage = () => {
  emit('choose-image')
}

/**
 * 选择菜品
 */
const onChooseDish = () => {
  emit('choose-dish')
}

/**
 * 创建群订单
 */
const onCreateGroupOrder = () => {
  emit('create-group-order')
}

/**
 * 输入框聚焦
 */
const onFocus = () => {
  emit('focus')
}

/**
 * 清空输入框
 */
const clear = () => {
  inputContent.value = ''
}

/**
 * 设置输入框内容
 */
const setContent = (content) => {
  inputContent.value = content
}

// 暴露方法给父组件
defineExpose({
  clear,
  setContent
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.message-input-container {
  position: relative;
  background: rgba(255, 252, 248, 0.94);
  border-top: 1rpx solid rgba(255, 107, 53, 0.08);
  padding-bottom: env(safe-area-inset-bottom);
  box-shadow: 0 -12rpx 28rpx rgba(123, 86, 53, 0.05);
}

.reply-preview {
  margin: 18rpx 24rpx 0;
  padding: 18rpx 20rpx;
  display: flex;
  align-items: center;
  gap: 14rpx;
  border-radius: 24rpx;
  background: rgba(255, 107, 53, 0.08);
}

.reply-accent {
  width: 8rpx;
  align-self: stretch;
  border-radius: 999rpx;
  background: $primary-color;
}

.reply-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.reply-author {
  font-size: 22rpx;
  color: #A05F44;
  font-weight: 600;
}

.reply-text {
  font-size: 24rpx;
  color: #7A685C;
  @include text-ellipsis;
}

.reply-close {
  width: 40rpx;
  height: 40rpx;
  @include flex-center;
}

.input-area {
  padding: 18rpx 24rpx 22rpx;
  display: flex;
  align-items: flex-end;
  gap: 16rpx;
}

.input-tools {
  display: flex;
  gap: 12rpx;
  align-items: flex-end;
}

.tool-btn {
  width: 72rpx;
  height: 72rpx;
  background: rgba(255, 255, 255, 0.84);
  border-radius: 24rpx;
  border: none;
  box-shadow: 0 8rpx 18rpx rgba(123, 86, 53, 0.06);
  @include flex-center;
}

.input-wrapper {
  flex: 1;
}

.input-field {
  min-height: 84rpx;
  max-height: 220rpx;
  padding: 20rpx 24rpx;
  background: rgba(255, 255, 255, 0.88);
  border: 1rpx solid rgba(255, 107, 53, 0.08);
  border-radius: 28rpx;
  font-size: 28rpx;
  line-height: 1.5;
  box-sizing: border-box;
}

.send-btn {
  width: 132rpx;
  height: 84rpx;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8755 100%);
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
  border-radius: 28rpx;
  border: none;
  box-shadow: 0 12rpx 24rpx rgba(255, 107, 53, 0.22);
  @include flex-center;

  &[disabled] {
    background: #EDE6E1;
    color: #B6AAA2;
    box-shadow: none;
  }
}
</style>
