<!--
组件名称：MerchantMiniCard
用途：商家迷你卡片（紧凑版）
复用情况：菜品详情、订单详情等
创建时间：2026-03-20
-->
<template>
  <view class="merchant-mini-card" @tap="$emit('tap')">
    <view class="merchant-info">
      <image class="merchant-logo" :src="merchant.logo" mode="aspectFill" />
      <view class="merchant-detail">
        <view class="merchant-name">{{ merchant.name }}</view>
        <view class="merchant-rating">
          <text class="star">⭐</text>
          <text>{{ merchant.rating }}</text>
          <text class="merchant-sales" v-if="merchant.monthlySales">月售{{ merchant.monthlySales }}</text>
        </view>
      </view>
    </view>
    <view class="merchant-arrow">›</view>
  </view>
</template>

<script setup>
const props = defineProps({
  merchant: {
    type: Object,
    required: true
  }
})

defineEmits(['tap'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.merchant-mini-card {
  @include flex-between;
  align-items: center;
}

.merchant-info {
  @include flex-center;
  gap: $spacing-md;
  flex: 1;
}

.merchant-logo {
  width: 100rpx;
  height: 100rpx;
  border-radius: $border-radius-base;
}

.merchant-detail {
  flex: 1;
}

.merchant-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
  margin-bottom: $spacing-xs;
}

.merchant-rating {
  @include flex-center;
  gap: $spacing-xs;
  font-size: $font-size-sm;

  .star {
    color: #f5a623;
  }

  .merchant-sales {
    color: $text-color-secondary;
  }
}

.merchant-arrow {
  font-size: 48rpx;
  color: $text-color-secondary;
}
</style>
