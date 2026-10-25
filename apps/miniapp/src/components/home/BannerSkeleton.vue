<template>
  <view class="banner-skeleton">
    <view class="skeleton-swiper">
      <view class="skeleton-image"></view>
      <view class="skeleton-indicators">
        <view
          class="skeleton-dot"
          :class="{ active: index === 0 }"
          v-for="index in 3"
          :key="index"
        ></view>
      </view>
    </view>
  </view>
</template>

<script setup>
/**
 * 轮播图骨架屏组件
 */
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.banner-skeleton {
  padding: $spacing-md 0;
  background-color: $bg-color-white;
  margin-bottom: $spacing-md;
}

.skeleton-swiper {
  width: 100%;
  height: 300rpx;
  border-radius: $border-radius-lg;
  overflow: hidden;
  position: relative;
}

.skeleton-image {
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-indicators {
  position: absolute;
  bottom: 20rpx;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12rpx;
}

.skeleton-dot {
  width: 16rpx;
  height: 16rpx;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 50%;

  &.active {
    width: 24rpx;
    background: rgba(255, 255, 255, 0.8);
  }
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}
</style>
