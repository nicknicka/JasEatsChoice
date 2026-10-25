<template>
  <view class="dish-card" @click="handleClick">
    <!-- 菜品图片 -->
    <image class="dish-image" :src="dish.image" mode="aspectFill" />

    <!-- 收藏按钮 -->
    <view class="favorite-btn" @click.stop="toggleFavorite">
      <text class="favorite-icon">{{ dish.isFavorite ? '⭐' : '☆' }}</text>
    </view>

    <!-- 菜品信息 -->
    <view class="dish-info">
      <text class="dish-name">{{ dish.name }}</text>

      <!-- 标签 -->
      <view class="dish-tags" v-if="dish.tags && dish.tags.length">
        <text
          class="tag-item"
          v-for="(tag, index) in dish.tags.slice(0, 2)"
          :key="index"
        >
          {{ tag }}
        </text>
      </view>

      <!-- 元信息 -->
      <view class="dish-meta">
        <text class="dish-price">¥{{ dish.price }}</text>
        <text class="dish-sales" v-if="dish.monthlySales">
          月售{{ dish.monthlySales }}
        </text>
      </view>

      <!-- 商家信息（可选显示） -->
      <view class="merchant-info" v-if="showMerchant && dish.merchantName">
        <text class="merchant-name">{{ dish.merchantName }}</text>
      </view>
    </view>

    <!-- 加购按钮（可选） -->
    <view class="add-btn" v-if="showAddBtn" @click.stop="handleAdd">
      <text class="add-icon">+</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  // 菜品数据
  dish: {
    type: Object,
    required: true
  },
  // 是否显示商家信息
  showMerchant: {
    type: Boolean,
    default: false
  },
  // 是否显示加购按钮
  showAddBtn: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click', 'favorite', 'add'])

/**
 * 点击卡片
 */
const handleClick = () => {
  emit('click', props.dish)
}

/**
 * 切换收藏
 */
const toggleFavorite = () => {
  emit('favorite', props.dish)
}

/**
 * 加购
 */
const handleAdd = () => {
  emit('add', props.dish)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.dish-card {
  position: relative;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  overflow: hidden;
  box-shadow: $box-shadow-sm;

  &:active {
    transform: scale(0.98);
  }
}

.dish-image {
  width: 100%;
  height: 240rpx;
}

.favorite-btn {
  position: absolute;
  top: $spacing-sm;
  right: $spacing-sm;
  width: 56rpx;
  height: 56rpx;
  @include flex-center;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  z-index: 1;

  &:active {
    transform: scale(0.9);
  }
}

.favorite-icon {
  font-size: $font-size-xl;
  color: #fff;
}

.dish-info {
  padding: $spacing-md;
}

.dish-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
  @include text-ellipsis;
  display: block;
  margin-bottom: $spacing-xs;
}

.dish-tags {
  @include flex-center;
  gap: $spacing-xs;
  flex-wrap: wrap;
  margin-bottom: $spacing-xs;
}

.tag-item {
  padding: 4rpx 12rpx;
  background-color: rgba(255, 107, 53, 0.1);
  color: $primary-color;
  font-size: $font-size-xs;
  border-radius: $border-radius-round;
}

.dish-meta {
  @include flex-between;
  align-items: center;
}

.dish-price {
  font-size: $font-size-lg;
  color: $primary-color;
  font-weight: $font-weight-bold;
}

.dish-sales {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.merchant-info {
  margin-top: $spacing-xs;
  padding-top: $spacing-xs;
  border-top: 1rpx solid $border-color-lighter;
}

.merchant-name {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.add-btn {
  position: absolute;
  bottom: $spacing-md;
  right: $spacing-md;
  width: 56rpx;
  height: 56rpx;
  @include flex-center;
  background: linear-gradient(135deg, $primary-color, #FF8F61);
  border-radius: 50%;
  box-shadow: $box-shadow-md;

  &:active {
    transform: scale(0.9);
  }
}

.add-icon {
  font-size: $font-size-xl;
  color: #fff;
  line-height: 1;
}
</style>
