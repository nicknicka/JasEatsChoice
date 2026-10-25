<!--
组件名称：OrderDishList
用途：订单菜品列表
复用情况：订单详情、订单确认
创建时间：2026-03-20
-->
<template>
  <view class="order-dish-list">
    <view class="section-title">订单商品</view>

    <view class="merchant-group" v-for="group in items" :key="group.merchantId">
      <view class="merchant-info">
        <image class="merchant-logo" :src="group.merchant.logo" mode="aspectFill" />
        <text class="merchant-name">{{ group.merchant.name }}</text>
      </view>

      <view class="dish-list">
        <view class="dish-item" v-for="item in group.items" :key="item.dish.id">
          <image class="dish-image" :src="item.dish.image" mode="aspectFill" />

          <view class="dish-info">
            <view class="dish-name">{{ item.dish.name }}</view>
            <view class="dish-spec" v-if="item.spec">{{ item.spec }}</view>
            <view class="dish-bottom">
              <view class="dish-price">
                <text class="price-symbol">¥</text>
                <text class="price-value">{{ item.dish.price }}</text>
              </view>
              <view class="dish-quantity">×{{ item.quantity }}</view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  items: {
    type: Array,
    default: () => []
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.order-dish-list {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.merchant-group {
  margin-bottom: 20rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.merchant-info {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 20rpx;
  padding-bottom: 15rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.merchant-logo {
  width: 60rpx;
  height: 60rpx;
  border-radius: 12rpx;
}

.merchant-name {
  font-size: 26rpx;
  font-weight: 500;
  color: #333;
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.dish-item {
  display: flex;
  gap: 20rpx;
}

.dish-image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.dish-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.dish-spec {
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
}

.dish-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-price {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 22rpx;
  color: #FF6B35;
}

.price-value {
  font-size: 28rpx;
  font-weight: bold;
  color: #FF6B35;
}

.dish-quantity {
  font-size: 24rpx;
  color: #999;
}
</style>
