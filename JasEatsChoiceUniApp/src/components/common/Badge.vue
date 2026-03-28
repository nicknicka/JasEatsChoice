<template>
  <view class="badge-wrapper" :class="{ 'is-dot': isDot }">
    <!-- 内容徽章 -->
    <view class="badge-content" v-if="!isDot">
      <slot>{{ value }}</slot>
    </view>

    <!-- 点状徽章 -->
    <view class="badge-dot" v-else></view>
  </view>
</template>

<script setup>
const props = defineProps({
  // 徽章值
  value: {
    type: [String, Number],
    default: ''
  },
  // 是否为点状
  isDot: {
    type: Boolean,
    default: false
  },
  // 最大值（超出显示max+）
  max: {
    type: Number,
    default: 99
  }
})

/**
 * 格式化显示值
 */
const formatValue = (val) => {
  if (typeof val === 'number') {
    return val > props.max ? `${props.max}+` : val
  }
  return val
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.badge-wrapper {
  position: relative;
  display: inline-block;
}

.badge-content {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  @include flex-center;
  padding: 0 8rpx;
  background-color: $danger-color;
  border-radius: $border-radius-round;
  border: 2rpx solid $bg-color-white;
  font-size: $font-size-xs;
  color: #fff;
  font-weight: $font-weight-bold;
  white-space: nowrap;
}

.badge-dot {
  position: absolute;
  top: -4rpx;
  right: -4rpx;
  width: 16rpx;
  height: 16rpx;
  background-color: $danger-color;
  border-radius: 50%;
  border: 2rpx solid $bg-color-white;
}
</style>
