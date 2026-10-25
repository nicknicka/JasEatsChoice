<!--
组件名称：QuantityControl
用途：数量选择器（+/-按钮）
复用情况：购物车、菜品详情等
创建时间：2026-03-20
-->
<template>
  <view class="quantity-control-wrapper">
    <view class="quantity-control">
      <view class="quantity-btn" @tap="$emit('decrease')">
        <text>-</text>
      </view>
      <view class="quantity-value">{{ modelValue }}</view>
      <view class="quantity-btn" @tap="$emit('increase')">
        <text>+</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: Number,
    default: 1
  }
})

defineEmits(['update:modelValue', 'increase', 'decrease'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.quantity-control-wrapper {
  display: inline-block;
}

.quantity-control {
  @include flex-center;
  gap: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  padding: $spacing-xs;

  .quantity-btn {
    width: 56rpx;
    height: 56rpx;
    @include flex-center;
    background-color: $bg-color-white;
    border-radius: $border-radius-sm;
    font-size: $font-size-lg;
    color: $text-color-primary;
  }

  .quantity-value {
    width: 80rpx;
    text-align: center;
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
    color: $text-color-primary;
  }
}
</style>
