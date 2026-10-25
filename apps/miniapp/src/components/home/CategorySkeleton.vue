<template>
  <view class="category-skeleton">
    <view class="section-header">
      <view class="skeleton-title"></view>
      <view class="skeleton-more"></view>
    </view>
    <scroll-view class="category-scroll" scroll-x show-scrollbar="false">
      <view class="category-list">
        <view class="category-item" v-for="n in 8" :key="n">
          <view class="skeleton-icon"></view>
          <view class="skeleton-name"></view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
/**
 * 分类导航骨架屏组件
 */
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.category-skeleton {
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
}

.section-header {
  @include flex-between;
  margin-bottom: $spacing-md;
}

.skeleton-title {
  width: 150rpx;
  height: 36rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;
}

.skeleton-more {
  width: 100rpx;
  height: 32rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;
}

.category-scroll {
  white-space: nowrap;
}

.category-list {
  @include flex-center;
  gap: $spacing-lg;
}

.category-item {
  @include flex-center-column;
  gap: $spacing-xs;
  padding: $spacing-md;
  flex-shrink: 0;
}

.skeleton-icon {
  width: 100rpx;
  height: 100rpx;
  @include flex-center;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: $border-radius-lg;
}

.skeleton-name {
  width: 80rpx;
  height: 28rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;
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
