<!--
组件名称：NutritionGrid
用途：营养信息网格展示
复用情况：菜品详情、AI分析等
创建时间：2026-03-20
-->
<template>
  <view class="nutrition-grid-wrapper">
    <view class="grid-header" v-if="title">
      <text class="title-text">{{ title }}</text>
      <text class="title-unit" v-if="unit">{{ unit }}</text>
    </view>
    <view class="nutrition-grid" :class="'grid-' + columns">
      <view class="nutrition-item" v-for="(item, index) in nutritionList" :key="index">
        <view class="nutrition-icon" v-if="item.icon">{{ item.icon }}</view>
        <view class="nutrition-value">{{ item.value }}</view>
        <view class="nutrition-label">{{ item.label }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  unit: {
    type: String,
    default: ''
  },
  nutritionList: {
    type: Array,
    default: () => []
  },
  columns: {
    type: Number,
    default: 5
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.nutrition-grid-wrapper {
  .grid-header {
    @include flex-between;
    margin-bottom: $spacing-md;
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $text-color-primary;

    .title-unit {
      font-size: $font-size-sm;
      color: $text-color-secondary;
      font-weight: $font-weight-normal;
    }
  }

  .nutrition-grid {
    display: grid;
    gap: $spacing-md;

    &.grid-3 {
      grid-template-columns: repeat(3, 1fr);
    }

    &.grid-4 {
      grid-template-columns: repeat(4, 1fr);
    }

    &.grid-5 {
      grid-template-columns: repeat(5, 1fr);
    }
  }

  .nutrition-item {
    @include flex-center-column;
    gap: $spacing-xs;

    .nutrition-icon {
      font-size: 48rpx;
    }

    .nutrition-value {
      font-size: $font-size-base;
      font-weight: $font-weight-medium;
      color: $text-color-primary;
    }

    .nutrition-label {
      font-size: $font-size-xs;
      color: $text-color-secondary;
    }
  }
}
</style>
