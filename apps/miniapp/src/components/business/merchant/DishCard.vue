<!--
组件名称：DishCard
用途：菜品卡片展示
复用情况：商家详情、菜品列表、购物车
创建时间：2026-03-20
-->
<template>
  <view class="dish-card" @tap="$emit('tap', dish)">
    <image class="dish-image" :src="dish.image" mode="aspectFill" />

    <view class="dish-info">
      <view class="dish-name">{{ dish.name }}</view>
      <view class="dish-description" v-if="dish.description">{{ dish.description }}</view>

      <view class="dish-bottom">
        <view class="price-section">
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ dish.price }}</text>
          <text class="price-original" v-if="dish.originalPrice">¥{{ dish.originalPrice }}</text>
        </view>
        <view class="sales-info">月售{{ dish.monthlySales }}</view>
      </view>

      <view class="dish-tags" v-if="dish.tags && dish.tags.length > 0">
        <text class="tag" v-for="tag in dish.tags" :key="tag">{{ tag }}</text>
      </view>
    </view>

    <view class="dish-action" v-if="showAddBtn" @tap.stop="$emit('add', dish)">
      <view class="add-btn">+</view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  dish: {
    type: Object,
    required: true
  },
  showAddBtn: {
    type: Boolean,
    default: true
  }
})

defineEmits(['tap', 'add'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.dish-card {
  display: flex;
  gap: 20rpx;
  padding: 25rpx;
  background: #fff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}

.dish-image {
  width: 180rpx;
  height: 180rpx;
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
  font-weight: 500;
  color: #333;
  margin-bottom: 8rpx;
}

.dish-description {
  font-size: 22rpx;
  color: #999;
  margin-bottom: 15rpx;
  @include text-ellipsis-multiline(2);
}

.dish-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 5rpx;
}

.price-symbol {
  font-size: 20rpx;
  color: #FF6B35;
}

.price-value {
  font-size: 28rpx;
  font-weight: bold;
  color: #FF6B35;
}

.price-original {
  font-size: 20rpx;
  color: #999;
  text-decoration: line-through;
  margin-left: 8rpx;
}

.sales-info {
  font-size: 22rpx;
  color: #999;
}

.dish-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.tag {
  font-size: 20rpx;
  color: #FF6B35;
  background: #FFF7E6;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  border: 1rpx solid #FFE7CC;
}

.dish-action {
  display: flex;
  align-items: center;
}

.add-btn {
  width: 60rpx;
  height: 60rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 32rpx;
  border-radius: 50%;
  @include flex-center;
}
</style>
