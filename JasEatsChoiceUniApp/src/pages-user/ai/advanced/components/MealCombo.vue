<!--
组件名称：MealCombo
用途：营养搭配建议
页面：AI高级分析页面
创建时间：2026-03-20
-->
<template>
  <view class="meal-combo">
    <view class="combo-item" v-for="combo in combos" :key="combo.type">
      <view class="combo-header">
        <text class="combo-type">{{ combo.type }}</text>
        <text class="combo-calories">{{ combo.totalCalories }}kcal</text>
      </view>
      <view class="combo-dishes">
        <view class="combo-dish" v-for="dish in combo.dishes" :key="dish">
          <text class="dish-text">{{ dish }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  combos: {
    type: Array,
    default: () => []
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.meal-combo {
  .combo-item {
    padding: 24rpx;
    background: #F8F9FA;
    border-radius: 12rpx;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .combo-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;

    .combo-type {
      font-size: 28rpx;
      font-weight: 500;
      color: #333;
    }

    .combo-calories {
      font-size: 24rpx;
      color: #FF6B35;
      font-weight: bold;
    }
  }

  .combo-dishes {
    .combo-dish {
      padding: 12rpx 0;

      .dish-text {
        font-size: 26rpx;
        color: #666;
        line-height: 1.6;
      }
    }
  }
}
</style>
