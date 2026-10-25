<!--
组件名称：RecommendDishCard
用途：推荐菜品卡片（含匹配度）
页面：AI高级分析页面
创建时间：2026-03-20
-->
<template>
  <view class="dish-card" @tap="$emit('tap', dish.id)">
    <image class="dish-image" :src="dish.image" mode="aspectFill"></image>
    <view class="dish-info">
      <text class="dish-name">{{ dish.name }}</text>
      <view class="dish-tags">
        <text class="tag" v-for="tag in dish.tags" :key="tag">{{ tag }}</text>
      </view>
      <view class="dish-nutrition">
        <text class="nutrition-item">{{ dish.calories }}kcal</text>
        <text class="nutrition-item">蛋白质{{ dish.protein }}g</text>
      </view>
    </view>
    <view class="match-score">
      <text class="score-label">匹配度</text>
      <text class="score-value">{{ dish.matchScore }}%</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  dish: {
    type: Object,
    required: true
  }
})

defineEmits(['tap'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.dish-card {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  background: #F8F9FA;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  position: relative;

  &:last-child {
    margin-bottom: 0;
  }

  .dish-image {
    width: 160rpx;
    height: 120rpx;
    border-radius: 12rpx;
    flex-shrink: 0;
  }

  .dish-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;

    .dish-name {
      font-size: 28rpx;
      font-weight: 500;
      color: #333;
    }

    .dish-tags {
      display: flex;
      gap: 10rpx;
      flex-wrap: wrap;

      .tag {
        padding: 4rpx 12rpx;
        background: #E8F5E9;
        color: #4CAF50;
        font-size: 22rpx;
        border-radius: 4rpx;
      }
    }

    .dish-nutrition {
      display: flex;
      gap: 20rpx;

      .nutrition-item {
        font-size: 24rpx;
        color: #666;
      }
    }
  }

  .match-score {
    position: absolute;
    top: 20rpx;
    right: 20rpx;
    text-align: center;

    .score-label {
      display: block;
      font-size: 20rpx;
      color: #999;
      margin-bottom: 5rpx;
    }

    .score-value {
      font-size: 28rpx;
      font-weight: bold;
      color: #FF6B35;
    }
  }
}
</style>
