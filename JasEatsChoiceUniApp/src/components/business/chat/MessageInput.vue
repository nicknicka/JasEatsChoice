<!--
组件名称：MessageInput
用途：聊天消息输入框
复用情况：群聊、私聊、客服聊天
创建时间：2026-03-20
-->
<template>
  <view class="message-input-container">
    <!-- 回复预览 -->
    <view class="reply-preview" v-if="replyMessage" @tap="cancelReply">
      <view class="reply-content">
        <text class="reply-author">{{ replyMessage.author }}</text>
        <text class="reply-text">{{ replyMessage.content }}</text>
      </view>
      <view class="reply-close">
        <uni-icons type="close" size="16" color="#999" />
      </view>
    </view>

    <!-- 输入区域 -->
    <view class="input-area">
      <view class="input-tools" v-if="showTools">
        <button class="tool-btn" @tap="onChooseImage">
          <uni-icons type="image" size="22" color="#666" />
        </button>
        <button class="tool-btn" @tap="onChooseDish" v-if="showDishButton">
          <uni-icons type="shop" size="22" color="#666" />
        </button>
        <button class="tool-btn" @tap="onCreateGroupOrder" v-if="showGroupOrderButton">
          <uni-icons type="cart" size="22" color="#666" />
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
  background: #fff;
  border-top: 1rpx solid #eee;
}

.reply-preview {
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 15rpx;
  border-bottom: 1rpx solid #eee;
}

.reply-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.reply-author {
  font-size: 22rpx;
  color: #999;
}

.reply-text {
  font-size: 24rpx;
  color: #666;
  @include text-ellipsis;
}

.reply-close {
  width: 40rpx;
  height: 40rpx;
  @include flex-center;
}

.input-area {
  padding: 20rpx 30rpx;
  display: flex;
  align-items: flex-end;
  gap: 20rpx;
}

.input-tools {
  display: flex;
  gap: 15rpx;
  align-items: flex-end;
}

.tool-btn {
  width: 60rpx;
  height: 60rpx;
  background: #F5F5F5;
  border-radius: 50%;
  border: none;
  @include flex-center;
}

.input-wrapper {
  flex: 1;
}

.input-field {
  min-height: 70rpx;
  max-height: 200rpx;
  padding: 15rpx 20rpx;
  background: #F5F5F5;
  border-radius: 20rpx;
  font-size: 28rpx;
}

.send-btn {
  width: 120rpx;
  height: 70rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 28rpx;
  border-radius: 35rpx;
  border: none;
  @include flex-center;

  &[disabled] {
    background: #E8E8E8;
    color: #999;
  }
}
</style>
