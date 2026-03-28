<template>
  <view class="loading-container" :class="{ 'is-full': fullScreen }">
    <!-- 加载动画 -->
    <view class="loading-spinner" v-if="type === 'spinner'">
      <view class="spinner-dot" v-for="i in 3" :key="i"></view>
    </view>

    <!-- 加载圈 -->
    <view class="loading-ring" v-else-if="type === 'ring'">
      <view class="ring-circle"></view>
    </view>

    <!-- 加载文字 -->
    <text class="loading-text" v-if="text">{{ text }}</text>
  </view>
</template>

<script setup>
const props = defineProps({
  // 加载类型：spinner | ring
  type: {
    type: String,
    default: 'spinner'
  },
  // 加载文字
  text: String,
  // 是否全屏
  fullScreen: {
    type: Boolean,
    default: false
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.loading-container {
  @include flex-center-column;
  gap: $spacing-md;
  padding: $spacing-lg;

  &.is-full {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.3);
    z-index: $z-index-max;
  }
}

/* 加载动画 - 点 */
.loading-spinner {
  @include flex-center;
  gap: 12rpx;
}

.spinner-dot {
  width: 16rpx;
  height: 16rpx;
  background-color: $primary-color;
  border-radius: 50%;
  animation: spinner-bounce 1.4s infinite ease-in-out both;

  &:nth-child(1) {
    animation-delay: -0.32s;
  }

  &:nth-child(2) {
    animation-delay: -0.16s;
  }

  &:nth-child(3) {
    animation-delay: 0s;
  }
}

@keyframes spinner-bounce {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 加载动画 - 圈 */
.loading-ring {
  width: 60rpx;
  height: 60rpx;
}

.ring-circle {
  width: 100%;
  height: 100%;
  border: 4rpx solid rgba(255, 107, 53, 0.2);
  border-top-color: $primary-color;
  border-radius: 50%;
  animation: ring-rotate 1s linear infinite;
}

@keyframes ring-rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}
</style>
