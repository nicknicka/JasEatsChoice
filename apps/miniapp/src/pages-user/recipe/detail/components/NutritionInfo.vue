<template>
  <view class="nutrition-info">
    <view class="nutrition-cards">
      <view
        class="nutrition-item"
        v-for="item in nutritionList"
        :key="item.name"
      >
        <text class="nutrition-icon">{{ item.icon }}</text>
        <view class="nutrition-detail">
          <text class="nutrition-value">{{ item.value }}</text>
          <text class="nutrition-name">{{ item.name }}</text>
        </view>
      </view>
    </view>

    <!-- 食谱元信息 -->
    <view class="recipe-meta">
      <view class="meta-item">
        <text class="meta-icon">⏱️</text>
        <text class="meta-text">{{ recipe.cookTime }}分钟</text>
      </view>
      <view class="meta-item">
        <text class="meta-icon">👥</text>
        <text class="meta-text">{{ recipe.servings }}人份</text>
      </view>
      <view class="meta-item">
        <text class="meta-icon">📊</text>
        <text class="meta-text">{{ recipe.difficulty }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import NutritionBar from '@/components/business/NutritionBar.vue'

const props = defineProps({
  // 食谱数据
  recipe: {
    type: Object,
    required: true
  }
})

/**
 * 营养成分列表
 */
const nutritionList = computed(() => {
  if (!props.recipe) return []
  return [
    {
      icon: '🔥',
      name: '卡路里',
      value: props.recipe.calories + 'kcal'
    },
    {
      icon: '🥩',
      name: '蛋白质',
      value: props.recipe.protein + 'g'
    },
    {
      icon: '🍚',
      name: '碳水',
      value: props.recipe.carbs + 'g'
    },
    {
      icon: '🥑',
      name: '脂肪',
      value: props.recipe.fat + 'g'
    }
  ]
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.nutrition-info {
  @include flex-center-column;
  gap: $spacing-md;
}

.nutrition-cards {
  @include flex-center;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.nutrition-item {
  flex: 1;
  min-width: calc(50% - #{$spacing-sm});
  background-color: $bg-color-base;
  padding: $spacing-md;
  border-radius: $border-radius-lg;
  @include flex-center;
  gap: $spacing-sm;
}

.nutrition-icon {
  font-size: $font-size-xl;
}

.nutrition-detail {
  flex: 1;
  @include flex-center-column;
  gap: $spacing-xs;
}

.nutrition-value {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.nutrition-name {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

.recipe-meta {
  @include flex-center;
  gap: $spacing-lg;
  padding: $spacing-md 0;
  border-top: 1rpx solid $border-color-lighter;
  border-bottom: 1rpx solid $border-color-lighter;
}

.meta-item {
  @include flex-center;
  gap: $spacing-xs;
}

.meta-icon {
  font-size: $font-size-lg;
}

.meta-text {
  font-size: $font-size-sm;
  color: $text-color-regular;
}
</style>
