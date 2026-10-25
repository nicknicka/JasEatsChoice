<template>
  <view class="order-detail-section">
    <view class="section-header">
      <text class="section-title">订单详情</text>
      <text class="order-no">订单号：{{ order.orderNo }}</text>
    </view>

    <view class="dish-list">
      <view
        class="dish-item"
        v-for="(item, index) in order.items"
        :key="index"
      >
        <image class="dish-image" :src="item.image" mode="aspectFill" />
        <view class="dish-info">
          <text class="dish-name">{{ item.name }}</text>
          <text class="dish-spec" v-if="item.spec">{{ item.spec }}</text>
        </view>
        <view class="dish-price">
          <text class="price-text">¥{{ item.price }}</text>
          <text class="quantity-text">x{{ item.quantity }}</text>
        </view>
      </view>
    </view>

    <view class="order-summary">
      <view class="summary-item">
        <text class="summary-label">商品小计</text>
        <text class="summary-value">¥{{ order.subtotal }}</text>
      </view>
      <view class="summary-item" v-if="order.deliveryFee">
        <text class="summary-label">配送费</text>
        <text class="summary-value">¥{{ order.deliveryFee }}</text>
      </view>
      <view class="summary-item" v-if="order.discount">
        <text class="summary-label">优惠</text>
        <text class="summary-value discount">-¥{{ order.discount }}</text>
      </view>
      <view class="summary-item total">
        <text class="summary-label">实付金额</text>
        <text class="summary-value">¥{{ order.totalAmount }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  // 订单数据
  order: {
    type: Object,
    required: true
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.order-detail-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.section-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.order-no {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.dish-list {
  @include flex-center-column;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.dish-item {
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
}

.dish-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: $border-radius-base;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  margin-left: $spacing-md;
  @include flex-center-column;
  align-items: flex-start;
  gap: 4rpx;
}

.dish-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
}

.dish-spec {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.dish-price {
  @include flex-center-column;
  align-items: flex-end;
  gap: 4rpx;
}

.price-text {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $primary-color;
}

.quantity-text {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.order-summary {
  padding-top: $spacing-md;
  border-top: 1rpx solid $border-color-lighter;
}

.summary-item {
  @include flex-between;
  margin-bottom: $spacing-sm;

  &.total {
    padding-top: $spacing-sm;
    border-top: 1rpx solid $border-color-lighter;
    margin-top: $spacing-sm;
  }
}

.summary-label {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.summary-value {
  font-size: $font-size-sm;
  color: $text-color-primary;

  &.discount {
    color: $success-color;
  }
}

.summary-item.total .summary-value {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $primary-color;
}
</style>
