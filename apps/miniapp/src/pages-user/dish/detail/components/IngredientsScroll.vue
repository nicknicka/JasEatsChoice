<!--
组件名称：IngredientsScroll
用途：食材横向滚动列表
页面：菜品详情
创建时间：2026-03-20
-->
<template>
  <view class="ingredients-scroll-wrapper" v-if="ingredients.length > 0">
    <view class="section-title">主要食材</view>
    <scroll-view class="ingredients-scroll" scroll-x show-scrollbar="false">
      <view class="ingredients-list">
        <view class="ingredient-item" v-for="item in ingredients" :key="item.name">
          <image class="ingredient-image" :src="item.image" mode="aspectFill" />
          <view class="ingredient-name">{{ item.name }}</view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
const props = defineProps({
  ingredients: {
    type: Array,
    default: () => []
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.ingredients-scroll-wrapper {
  .section-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $text-color-primary;
    margin-bottom: $spacing-md;
  }

  .ingredients-scroll {
    white-space: nowrap;
  }

  .ingredients-list {
    display: flex;
    gap: $spacing-md;
  }

  .ingredient-item {
    @include flex-center-column;
    gap: $spacing-xs;
    flex-shrink: 0;

    .ingredient-image {
      width: 120rpx;
      height: 120rpx;
      border-radius: $border-radius-base;
    }

    .ingredient-name {
      font-size: $font-size-sm;
      color: $text-color-regular;
    }
  }
}
</style>
