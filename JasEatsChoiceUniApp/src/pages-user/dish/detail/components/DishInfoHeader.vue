<!--
组件名称：DishInfoHeader
用途：菜品信息头部（名称、描述、标签、价格、收藏）
页面：菜品详情
创建时间：2026-03-20
-->
<template>
  <view class="dish-info-header">
    <view class="dish-header">
      <view class="dish-name">{{ dish.name }}</view>
      <view class="dish-favorite" @tap="$emit('toggle-favorite')">
        <text class="favorite-icon">{{ isFavorite ? '❤️' : '🤍' }}</text>
      </view>
    </view>

    <view class="dish-description" v-if="dish.description">{{ dish.description }}</view>

    <view class="dish-tags" v-if="dish.tags && dish.tags.length > 0">
      <text class="tag" v-for="tag in dish.tags" :key="tag">{{ tag }}</text>
    </view>

    <view class="dish-bottom">
      <view class="price-section">
        <text class="price-symbol">¥</text>
        <text class="price-value">{{ dish.price }}</text>
        <text class="price-original" v-if="dish.originalPrice">¥{{ dish.originalPrice }}</text>
      </view>
      <view class="sales-info" v-if="dish.sales">已售 {{ dish.sales }}</view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  dish: {
    type: Object,
    required: true
  },
  isFavorite: {
    type: Boolean,
    default: false
  }
})

defineEmits(['toggle-favorite'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.dish-info-header {
  .dish-header {
    @include flex-between;
    margin-bottom: $spacing-md;
  }

  .dish-name {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $text-color-primary;
    flex: 1;
    padding-right: $spacing-md;
  }

  .dish-favorite {
    .favorite-icon {
      font-size: 48rpx;
    }
  }

  .dish-description {
    font-size: $font-size-base;
    color: $text-color-regular;
    line-height: $line-height-lg;
    margin-bottom: $spacing-md;
  }

  .dish-tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
    margin-bottom: $spacing-md;

    .tag {
      font-size: $font-size-sm;
      color: $primary-color;
      background-color: rgba(255, 107, 53, 0.1);
      padding: 8rpx 16rpx;
      border-radius: 8rpx;
    }
  }

  .dish-bottom {
    @include flex-between;
  }

  .price-section {
    @include flex-center;
    gap: 4rpx;
    color: $danger-color;

    .price-symbol {
      font-size: $font-size-base;
    }

    .price-value {
      font-size: $font-size-xxl;
      font-weight: $font-weight-bold;
    }

    .price-original {
      font-size: $font-size-sm;
      color: $text-color-secondary;
      text-decoration: line-through;
      margin-left: $spacing-xs;
    }
  }

  .sales-info {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }
}
</style>
