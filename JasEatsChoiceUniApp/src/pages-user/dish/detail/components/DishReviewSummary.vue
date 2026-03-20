<!--
组件名称：DishReviewSummary
用途：菜品评价汇总（评分概览、标签）
页面：菜品详情
创建时间：2026-03-20
-->
<template>
  <view class="dish-review-summary" v-if="reviewSummary">
    <view class="rating-overview">
      <text class="rating-score">{{ reviewSummary.averageRating }}</text>
      <view class="rating-stars">
        <text class="star" v-for="i in 5" :key="i">
          {{ i <= Math.floor(reviewSummary.averageRating) ? '⭐' : '☆' }}
        </text>
      </view>
      <text class="rating-total">{{ reviewCount }}条</text>
    </view>

    <view class="rating-tags" v-if="reviewSummary.tags && reviewSummary.tags.length > 0">
      <text
        class="rating-tag"
        v-for="(tag, index) in reviewSummary.tags"
        :key="index"
      >
        {{ tag.label }} {{ tag.percentage }}%
      </text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  reviewSummary: {
    type: Object,
    default: null
  },
  reviewCount: {
    type: Number,
    default: 0
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.dish-review-summary {
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  margin-bottom: $spacing-md;
}

.rating-overview {
  @include flex-center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;

  .rating-score {
    font-size: 48rpx;
    font-weight: $font-weight-bold;
    color: $text-color-primary;
  }

  .rating-stars {
    @include flex-center;
    gap: 4rpx;

    .star {
      font-size: $font-size-base;
      color: #f5a623;
    }
  }

  .rating-total {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }
}

.rating-tags {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;

  .rating-tag {
    font-size: $font-size-sm;
    color: $text-color-regular;
    background-color: $bg-color-white;
    padding: 8rpx 16rpx;
    border-radius: 8rpx;
  }
}
</style>
