<!--
组件名称：DishBottomBar
用途：菜品详情底部操作栏
页面：菜品详情
创建时间：2026-03-20
-->
<template>
  <view class="dish-bottom-bar">
    <view class="bar-left">
      <view class="bar-icon" @tap="$emit('cart')">
        <text class="icon">🛒</text>
        <view class="badge" v-if="cartCount > 0">{{ cartCount }}</view>
      </view>
      <view class="bar-icon" @tap="$emit('chat')">
        <text class="icon">💬</text>
      </view>
    </view>

    <view class="bar-right">
      <QuantityControl
        :model-value="quantity"
        @increase="$emit('increase-quantity')"
        @decrease="$emit('decrease-quantity')"
      />
      <view class="add-cart-btn" @tap="$emit('add-cart')">
        加入购物车
      </view>
    </view>
  </view>
</template>

<script setup>
import QuantityControl from '@/components/common/QuantityControl.vue'

const props = defineProps({
  quantity: {
    type: Number,
    default: 1
  },
  cartCount: {
    type: Number,
    default: 0
  }
})

defineEmits(['cart', 'chat', 'increase-quantity', 'decrease-quantity', 'add-cart'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.dish-bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  @include flex-between;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: $z-index-fixed;
}

.bar-left {
  @include flex-center;
  gap: $spacing-lg;
}

.bar-icon {
  position: relative;
  @include flex-center-column;
  gap: 4rpx;

  .icon {
    font-size: 48rpx;
  }

  .badge {
    position: absolute;
    top: 0;
    right: -8rpx;
    min-width: 32rpx;
    height: 32rpx;
    padding: 0 8rpx;
    background-color: $danger-color;
    color: #fff;
    font-size: $font-size-xs;
    line-height: 32rpx;
    text-align: center;
    border-radius: 16rpx;
  }
}

.bar-right {
  @include flex-center;
  gap: $spacing-md;
  flex: 1;
}

.add-cart-btn {
  flex: 1;
  height: 72rpx;
  @include flex-center;
  background-color: $primary-color;
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-base;
}
</style>
