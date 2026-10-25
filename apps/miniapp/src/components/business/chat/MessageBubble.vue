<!--
组件名称：MessageBubble
用途：展示单条聊天消息
复用情况：群聊、私聊、客服聊天
创建时间：2026-03-20
-->
<template>
  <view
    class="message-bubble"
    :class="{ self: message.isSelf }"
    @longpress="onLongPress"
  >
    <!-- 时间戳 -->
    <view class="message-time" v-if="message.showTime">
      {{ formatRelativeTime(message.time) }}
    </view>

    <!-- 消息内容 -->
    <view class="message-wrapper">
      <!-- 对方头像 -->
      <image
        class="message-avatar"
        :src="message.avatar"
        mode="aspectFill"
        v-if="!message.isSelf && showAvatar"
        @tap="onAvatarTap"
      />

      <!-- 昵称 -->
      <text class="member-name" v-if="!message.isSelf && message.isGroup && showNickname">
        {{ message.nickname }}
      </text>

      <!-- 消息气泡 -->
      <view class="message-content" :class="{ self: message.isSelf, card: isCardMessage }">
        <!-- 文本消息 -->
        <view class="text-message" v-if="message.type === 'text'">
          {{ message.content }}
        </view>

        <!-- 图片消息 -->
        <image
          class="image-message"
          :src="message.content"
          mode="widthFix"
          v-if="message.type === 'image'"
          @tap="onImageTap"
        />

        <!-- 菜品卡片 -->
        <view class="dish-card" v-if="message.type === 'dish'" @tap="onDishTap">
          <image class="dish-image" :src="message.dishImage" mode="aspectFill" />
          <view class="dish-info">
            <text class="dish-name">{{ message.dishName }}</text>
            <text class="dish-price">¥{{ message.dishPrice }}</text>
          </view>
        </view>

        <!-- 订单卡片 -->
        <view class="order-card" v-if="message.type === 'order'" @tap="onOrderTap">
          <view class="order-header">
            <text class="order-title">{{ message.orderTitle }}</text>
            <text class="order-status">{{ message.orderStatusText }}</text>
          </view>
          <view class="order-content">
            <text class="order-desc">{{ message.orderDesc }}</text>
            <text class="order-amount">¥{{ message.orderAmount }}</text>
          </view>
        </view>

        <!-- 群订单卡片 -->
        <view class="group-order-card" v-if="message.type === 'groupOrder'" @tap="onGroupOrderTap">
          <view class="group-order-header">
            <uni-icons type="shop" size="20" color="#FF6B35" />
            <text class="group-order-title">群订单</text>
          </view>
          <view class="group-order-content">
            <view class="group-order-avatars">
              <image
                class="avatar-item"
                :src="avatar"
                mode="aspectFill"
                v-for="(avatar, index) in message.avatars"
                :key="index"
              />
              <view class="avatar-count">+{{ message.joinCount }}</view>
            </view>
            <text class="group-order-amount">¥{{ message.totalAmount }}</text>
          </view>
        </view>

        <!-- 引用消息 -->
        <view class="quote-message" v-if="message.quote">
          <view class="quote-header">
            <text class="quote-author">{{ message.quote.author }}</text>
          </view>
          <text class="quote-content">{{ message.quote.content }}</text>
        </view>

        <!-- 消息状态 -->
        <view class="message-status" v-if="message.isSelf && showMessageStatus">
          <uni-icons
            v-if="message.status === 'sending'"
            type="spinner-cycle"
            size="14"
            color="#fff"
          />
          <uni-icons
            v-else-if="message.status === 'success'"
            type="checkmarkempty"
            size="14"
            color="#fff"
          />
          <uni-icons
            v-else-if="message.status === 'fail'"
            type="close"
            size="14"
            color="#fff"
          />
        </view>
      </view>

      <!-- 自己头像 -->
      <image
        class="message-avatar"
        :src="message.avatar"
        mode="aspectFill"
        v-if="message.isSelf && showAvatar"
      />
    </view>
  </view>
</template>

<script setup>
import { computed, defineProps, defineEmits } from 'vue'
import { formatRelativeTime } from '@/utils/helper'

const props = defineProps({
  message: {
    type: Object,
    required: true
  },
  showAvatar: {
    type: Boolean,
    default: true
  },
  showNickname: {
    type: Boolean,
    default: true
  },
  showMessageStatus: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits([
  'avatar-tap',
  'image-tap',
  'dish-tap',
  'order-tap',
  'group-order-tap',
  'longpress'
])

const isCardMessage = computed(() => {
  return ['image', 'dish', 'order', 'groupOrder'].includes(props.message.type)
})

/**
 * 点击头像
 */
const onAvatarTap = () => {
  emit('avatar-tap', props.message.userId)
}

/**
 * 点击图片
 */
const onImageTap = () => {
  emit('image-tap', props.message.content)
}

/**
 * 点击菜品
 */
const onDishTap = () => {
  emit('dish-tap', props.message.dishId)
}

/**
 * 点击订单
 */
const onOrderTap = () => {
  emit('order-tap', props.message.orderId)
}

/**
 * 点击群订单
 */
const onGroupOrderTap = () => {
  emit('group-order-tap', props.message.orderId)
}

/**
 * 长按消息
 */
const onLongPress = () => {
  emit('longpress', props.message)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.message-bubble {
  margin-bottom: 28rpx;

  &.self .message-wrapper {
    flex-direction: row-reverse;
  }
}

.message-time {
  text-align: center;
  font-size: 24rpx;
  color: #9A877A;
  margin-bottom: 18rpx;
}

.message-wrapper {
  display: flex;
  width: 100%;
  gap: 16rpx;
  align-items: flex-start;
  box-sizing: border-box;
}

.message-avatar {
  width: 68rpx;
  height: 68rpx;
  border-radius: 24rpx;
  flex-shrink: 0;
  margin-top: 4rpx;
  box-shadow: 0 10rpx 20rpx rgba(123, 86, 53, 0.08);
}

.member-name {
  font-size: 22rpx;
  color: #99867A;
  margin-bottom: 8rpx;
  align-self: flex-start;
}

.message-content {
  max-width: calc(100% - 92rpx);
  position: relative;
  box-sizing: border-box;
  overflow: visible;
  border-radius: 26rpx;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 12rpx 26rpx rgba(123, 86, 53, 0.05);
  border: 1rpx solid rgba(255, 255, 255, 0.9);

  &.self {
    background: linear-gradient(135deg, #FF6B35 0%, #FF8755 100%);
    color: #fff;
    border-top-right-radius: 10rpx;
    box-shadow: 0 14rpx 28rpx rgba(255, 107, 53, 0.22);
    border-color: transparent;
    margin-left: auto;
  }

  &.card {
    background: transparent;
    box-shadow: none;
    border: none;
  }
}

.text-message {
  padding: 22rpx 24rpx;
  font-size: 28rpx;
  line-height: 1.6;
  word-break: break-all;
  overflow-wrap: break-word;
  color: $text-color-primary;
}

.self .text-message {
  color: #fff;
  text-align: left;
}

.image-message {
  max-width: 400rpx;
  border-radius: 24rpx;
  box-shadow: 0 10rpx 24rpx rgba(123, 86, 53, 0.12);
}

.dish-card {
  width: 380rpx;
  background: #fffdfb;
  border-radius: 28rpx;
  overflow: hidden;
  box-shadow: 0 12rpx 28rpx rgba(123, 86, 53, 0.08);
}

.dish-image {
  width: 100%;
  height: 216rpx;
}

.dish-info {
  padding: 18rpx 20rpx 22rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
}

.dish-name {
  font-size: 26rpx;
  color: $text-color-primary;
  font-weight: 600;
}

.dish-price {
  font-size: 30rpx;
  color: #FF6B35;
  font-weight: bold;
}

.order-card {
  width: 400rpx;
  background: #fffdfb;
  border-radius: 28rpx;
  padding: 22rpx;
  box-shadow: 0 12rpx 28rpx rgba(123, 86, 53, 0.08);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14rpx;
  gap: 20rpx;
}

.order-title {
  font-size: 26rpx;
  color: $text-color-primary;
  font-weight: 600;
}

.order-status {
  font-size: 22rpx;
  color: #52C41A;
  background: rgba(82, 196, 26, 0.12);
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20rpx;
}

.order-desc {
  font-size: 24rpx;
  color: $text-color-regular;
  flex: 1;
}

.order-amount {
  font-size: 30rpx;
  color: #FF6B35;
  font-weight: bold;
}

.group-order-card {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  border-radius: 28rpx;
  padding: 22rpx;
  box-shadow: 0 14rpx 28rpx rgba(255, 107, 53, 0.22);
}

.group-order-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 15rpx;
}

.group-order-title {
  font-size: 26rpx;
  color: #fff;
  font-weight: 500;
}

.group-order-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.group-order-avatars {
  display: flex;
  align-items: center;
}

.avatar-item {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  border: 2rpx solid #fff;
  margin-left: -10rpx;

  &:first-child {
    margin-left: 0;
  }
}

.avatar-count {
  width: 50rpx;
  height: 50rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  font-size: 20rpx;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: -10rpx;
}

.group-order-amount {
  font-size: 32rpx;
  color: #fff;
  font-weight: bold;
}

.quote-message {
  margin: 16rpx 16rpx 0;
  padding: 16rpx 18rpx;
  border-radius: 20rpx;
  background: rgba(86, 66, 53, 0.07);
}

.quote-author {
  font-size: 22rpx;
  color: #8A7568;
  display: block;
  margin-bottom: 6rpx;
}

.quote-content {
  font-size: 24rpx;
  color: #6B5A50;
}

.message-status {
  position: absolute;
  right: 16rpx;
  bottom: -36rpx;
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  background: rgba(255, 107, 53, 0.16);
  @include flex-center;
}
</style>
