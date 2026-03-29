<template>
  <view class="conversation-item-wrapper">
    <view
      class="conversation-item"
      :class="{ 'pinned': conversation.isPinned }"
      :style="{ transform: `translateX(${translateX}px)` }"
      @touchstart="onTouchStart"
      @touchmove="onTouchMove"
      @touchend="onTouchEnd"
      @tap="onClick"
      @longpress="onLongPress"
    >
      <!-- 头像 -->
      <view class="avatar-wrapper">
        <image
          class="avatar"
          :src="conversation.avatar"
          mode="aspectFill"
          @error="handleAvatarError"
        ></image>
        <view class="online-badge" v-if="conversation.isOnline">
          <view class="online-pulse"></view>
        </view>
        <view class="unread-badge" v-if="conversation.unread > 0">
          <text class="unread-text">{{ conversation.unread > 99 ? '99+' : conversation.unread }}</text>
        </view>
      </view>

      <!-- 内容 -->
      <view class="conversation-content">
        <view class="content-header">
          <text class="name">{{ conversation.name }}</text>
          <text class="time">{{ formatTime(conversation.lastTime) }}</text>
        </view>
        <view class="content-body">
          <text class="last-message">{{ conversation.lastMessage }}</text>
          <view class="message-type" v-if="conversation.lastMessageType !== 'text'">
            <uni-icons
              :type="getMessageIcon(conversation.lastMessageType)"
              size="14"
              color="#999"
            ></uni-icons>
          </view>
        </view>
      </view>

      <!-- 置顶标识 -->
      <view class="pin-badge" v-if="conversation.isPinned">
        <uni-icons type="star-filled" size="16" color="#FFA500"></uni-icons>
      </view>
    </view>

    <!-- 滑动操作按钮 -->
    <view class="swipe-actions" v-if="showActions">
      <view class="swipe-btn top" @tap.stop="onPin" v-if="!conversation.isPinned">
        <text class="btn-icon">⭐</text>
        <text class="btn-text">置顶</text>
      </view>
      <view class="swipe-btn unpin" @tap.stop="onUnpin" v-else>
        <text class="btn-icon">⭐</text>
        <text class="btn-text">取消</text>
      </view>
      <view class="swipe-btn read" @tap.stop="onMarkRead" v-if="conversation.unread > 0">
        <text class="btn-icon">✓</text>
        <text class="btn-text">已读</text>
      </view>
      <view class="swipe-btn delete" @tap.stop="onDelete">
        <text class="btn-icon">🗑️</text>
        <text class="btn-text">删除</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { formatRelativeTime } from '@/utils/helper'

const props = defineProps({
  conversation: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click', 'pin', 'unpin', 'markRead', 'delete', 'longpress'])

// 滑动相关
const translateX = ref(0)
const startX = ref(0)
const startTime = ref(0)
const showActions = ref(false)
const isSwiping = ref(false)

const maxSwipeDistance = -280  // 最大滑动距离

/**
 * 触摸开始
 */
const onTouchStart = (e) => {
  startX.value = e.changedTouches[0].pageX
  startTime.value = Date.now()
  isSwiping.value = true
}

/**
 * 触摸移动
 */
const onTouchMove = (e) => {
  if (!isSwiping.value) return

  const currentX = e.changedTouches[0].pageX
  const deltaX = currentX - startX.value

  // 只允许向左滑动
  if (deltaX < 0) {
    // 平滑阻尼效果
    const resistance = deltaX > maxSwipeDistance ? 0.6 : 1
    translateX.value = Math.max(deltaX * resistance, maxSwipeDistance)
  } else {
    translateX.value = Math.min(deltaX, 0)
  }
}

/**
 * 触摸结束
 */
const onTouchEnd = (e) => {
  if (!isSwiping.value) return

  const endX = e.changedTouches[0].pageX
  const deltaX = endX - startX.value
  const deltaTime = Date.now() - startTime.value

  // 判断是否触发滑动操作
  const shouldTrigger = deltaX < -80 || (deltaX < -40 && deltaTime < 300)

  if (shouldTrigger) {
    // 展开操作按钮
    translateX.value = maxSwipeDistance
    showActions.value = true

    // 震动反馈
    uni.vibrateShort({
      type: 'light'
    })
  } else {
    // 回弹
    translateX.value = 0
    showActions.value = false
  }

  isSwiping.value = false
}

/**
 * 关闭滑动
 */
const closeSwipe = () => {
  translateX.value = 0
  showActions.value = false
}

/**
 * 点击事件
 */
const onClick = () => {
  if (showActions.value) {
    closeSwipe()
  } else {
    emit('click', props.conversation)
  }
}

/**
 * 长按事件
 */
const onLongPress = () => {
  if (!showActions.value) {
    emit('longpress', props.conversation)
  }
}

/**
 * 置顶
 */
const onPin = () => {
  closeSwipe()
  emit('pin', props.conversation)
}

/**
 * 取消置顶
 */
const onUnpin = () => {
  closeSwipe()
  emit('unpin', props.conversation)
}

/**
 * 标记已读
 */
const onMarkRead = () => {
  closeSwipe()
  emit('markRead', props.conversation)
}

/**
 * 删除
 */
const onDelete = () => {
  closeSwipe()
  emit('delete', props.conversation)
}

/**
 * 头像加载错误处理
 */
const handleAvatarError = () => {
  props.conversation.avatar = '/static/default-avatar.png'
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  return formatRelativeTime(time)
}

/**
 * 获取消息图标
 */
const getMessageIcon = (type) => {
  const iconMap = {
    image: 'image',
    dish: 'shop',
    order: 'list',
    voice: 'mic'
  }
  return iconMap[type] || 'chatbubble'
}

// 暴露方法
defineExpose({
  closeSwipe
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.conversation-item-wrapper {
  position: relative;
  overflow: hidden;
}

.conversation-item {
  position: relative;
  background: #fff;
  padding: 25rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  border-bottom: 1rpx solid $border-color-lighter;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 2;

  &.pinned {
    background: linear-gradient(to right, rgba(255, 165, 0, 0.05), transparent);

    .name {
      color: #FFA500;
    }
  }
}

.avatar-wrapper {
  position: relative;
  width: 100rpx;
  height: 100rpx;
  flex-shrink: 0;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: $border-radius-lg;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.conversation-item:active .avatar {
  transform: scale(0.95);
}

.online-badge {
  position: absolute;
  bottom: 2rpx;
  right: 2rpx;
  width: 22rpx;
  height: 22rpx;
  background: #52C41A;
  border: 3rpx solid #fff;
  border-radius: 50%;
  box-shadow: 0 2rpx 6rpx rgba(82, 196, 26, 0.4);
}

.online-pulse {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #52C41A;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(1.8);
    opacity: 0;
  }
}

.unread-badge {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 6rpx;
  background: linear-gradient(135deg, #ff6b6b, #ff5252);
  color: #fff;
  font-size: $font-size-xs;
  border-radius: $border-radius-round;
  @include flex-center;
  font-weight: $font-weight-bold;
  box-shadow: 0 2rpx 8rpx rgba(255, 82, 82, 0.4);
  border: 2rpx solid #fff;
  animation: badgeBounce 0.4s ease;
}

@keyframes badgeBounce {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.unread-text {
  font-size: 20rpx;
  font-weight: bold;
}

.conversation-content {
  flex: 1;
  min-width: 0;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.name {
  font-size: $font-size-lg;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
}

.time {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  flex-shrink: 0;
}

.content-body {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.last-message {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-color-secondary;
  @include text-ellipsis;
}

.message-type {
  flex-shrink: 0;
}

.pin-badge {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
}

/* 滑动操作按钮 */
.swipe-actions {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  display: flex;
  z-index: 1;
}

.swipe-btn {
  width: 90rpx;
  height: 100%;
  @include flex-center-column;
  gap: 8rpx;
  color: #fff;
  font-size: $font-size-xs;
  transition: all 0.3s ease;

  &.top {
    background: linear-gradient(135deg, #FFA500, #FF8F00);
  }

  &.unpin {
    background: linear-gradient(135deg, #999, #666);
  }

  &.read {
    background: linear-gradient(135deg, #52C41A, #389E0D);
  }

  &.delete {
    background: linear-gradient(135deg, #ff4d4f, #cf1322);
  }

  &:active {
    opacity: 0.8;
    transform: scale(0.95);
  }
}

.btn-icon {
  font-size: 36rpx;
  line-height: 1;
}

.btn-text {
  font-size: 20rpx;
  font-weight: $font-weight-medium;
}
</style>
