<!--
组件名称：DishRecommendGrid
用途：相关推荐菜品网格
页面：菜品详情
创建时间：2026-03-20
-->
<template>
  <view class="dish-recommend-grid" v-if="dishes.length > 0">
    <view class="section-title">相关推荐</view>
    <view class="dish-grid">
      <view
        class="dish-card"
        v-for="dish in dishes"
        :key="dish.id"
        @tap="$emit('tap', dish.id)"
      >
        <image class="dish-image" :src="dish.image" mode="aspectFill" />
        <view class="dish-info">
          <view class="dish-name">{{ dish.name }}</view>
          <view class="dish-bottom">
            <view class="dish-price">
              <text class="price-symbol">¥</text>
              <text class="price-value">{{ dish.price }}</text>
            </view>
            <view class="dish-sales" v-if="dish.sales">已售{{ dish.sales }}</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  dishes: {
    type: Array,
    default: () => []
  }
})

defineEmits(['tap'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.dish-recommend-grid {
  padding: $spacing-md;

  .section-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $text-color-primary;
    margin-bottom: $spacing-md;
  }

  .dish-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: $spacing-md;
  }

  .dish-card {
    background-color: $bg-color-white;
    border-radius: $border-radius-base;
    overflow: hidden;
    box-shadow: $box-shadow-light;
  }

  .dish-image {
    width: 100%;
    height: 200rpx;
  }

  .dish-info {
    padding: $spacing-sm;
  }

  .dish-name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $text-color-primary;
    @include text-ellipsis;
  }

  .dish-bottom {
    @include flex-between;
    margin-top: $spacing-sm;
  }

  .dish-price {
    @include flex-center;
    gap: 2rpx;
    color: $danger-color;
    font-weight: $font-weight-bold;

    .price-symbol {
      font-size: $font-size-sm;
    }

    .price-value {
      font-size: $font-size-lg;
    }
  }

  .dish-sales {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }
}
</style>
