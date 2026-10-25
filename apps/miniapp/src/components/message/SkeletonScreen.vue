<template>
  <view class="skeleton-container">
    <!-- 会话骨架屏 -->
    <view class="skeleton-item" v-for="n in count" :key="n">
      <!-- 头像骨架 -->
      <view class="skeleton-avatar">
        <view class="skeleton-shimmer"></view>
      </view>

      <!-- 内容骨架 -->
      <view class="skeleton-content">
        <view class="skeleton-title">
          <view class="skeleton-line title-line">
            <view class="skeleton-shimmer"></view>
          </view>
          <view class="skeleton-line time-line">
            <view class="skeleton-shimmer"></view>
          </view>
        </view>
        <view class="skeleton-line desc-line">
          <view class="skeleton-shimmer"></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
defineProps({
  count: {
    type: Number,
    default: 5
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.skeleton-container {
  padding: 20rpx 30rpx;
}

.skeleton-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 25rpx 0;
  border-bottom: 1rpx solid $border-color-lighter;
}

.skeleton-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: $border-radius-lg;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  overflow: hidden;
  position: relative;
}

.skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.skeleton-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20rpx;
}

.skeleton-line {
  height: 28rpx;
  border-radius: 6rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  position: relative;
  overflow: hidden;
}

.title-line {
  width: 200rpx;
}

.time-line {
  width: 100rpx;
}

.desc-line {
  width: 100%;
}

.skeleton-shimmer {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.6) 50%,
    transparent 100%
  );
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}
</style>
