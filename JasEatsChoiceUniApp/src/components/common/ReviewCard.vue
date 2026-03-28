<!--
组件名称：ReviewCard
用途：评价卡片展示
复用情况：菜品详情、评价列表等
创建时间：2026-03-20
-->
<template>
  <view class="review-card">
    <view class="review-header">
      <image class="user-avatar" :src="review.user.avatar" mode="aspectFill" />
      <view class="user-info">
        <view class="user-name">{{ review.user.name }}</view>
        <view class="review-stars">
          <text class="star" v-for="i in 5" :key="i">
            {{ i <= review.rating ? '⭐' : '☆' }}
          </text>
        </view>
      </view>
      <view class="review-date">{{ review.date }}</view>
    </view>

    <view class="review-content">{{ review.content }}</view>

    <view class="review-images" v-if="review.images && review.images.length > 0">
      <image
        class="review-image"
        v-for="(image, index) in review.images"
        :key="index"
        :src="image"
        mode="aspectFill"
        @tap="$emit('preview-image', review.images, index)"
      />
    </view>

    <view class="review-merchant" v-if="review.merchantReply">
      <text class="merchant-label">商家回复：</text>
      <text class="merchant-reply">{{ review.merchantReply }}</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  review: {
    type: Object,
    required: true
  }
})

defineEmits(['preview-image'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.review-card {
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color-light;

  &:last-child {
    border-bottom: none;
  }
}

.review-header {
  @include flex-between;
  margin-bottom: $spacing-sm;
}

.user-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
}

.user-info {
  flex: 1;
  margin-left: $spacing-sm;
}

.user-name {
  font-size: $font-size-base;
  color: $text-color-primary;
  margin-bottom: $spacing-xs;
}

.review-stars {
  @include flex-center;
  gap: 4rpx;

  .star {
    font-size: $font-size-sm;
    color: #f5a623;
  }
}

.review-date {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.review-content {
  font-size: $font-size-base;
  color: $text-color-regular;
  line-height: $line-height-lg;
  margin-bottom: $spacing-sm;
}

.review-images {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}

.review-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: $border-radius-base;
}

.review-merchant {
  padding: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  font-size: $font-size-sm;

  .merchant-label {
    color: $text-color-secondary;
  }

  .merchant-reply {
    color: $text-color-regular;
  }
}
</style>
