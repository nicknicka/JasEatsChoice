<template>
  <view class="empty-state">
    <image
      class="empty-image"
      :src="emptyImage"
      mode="aspectFit"
    />
    <text class="empty-title">{{ title }}</text>
    <text class="empty-desc" v-if="description">{{ description }}</text>
    <slot name="action">
      <button
        v-if="showAction"
        class="empty-action"
        @click="handleAction"
      >
        {{ actionText }}
      </button>
    </slot>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'default'
  },
  title: {
    type: String,
    default: '暂无数据'
  },
  description: {
    type: String,
    default: ''
  },
  showAction: {
    type: Boolean,
    default: true
  },
  actionText: {
    type: String,
    default: '重新加载'
  }
})

const emit = defineEmits(['action'])

const emptyImage = computed(() => {
  const images = {
    default: '/static/images/empty-default.png',
    network: '/static/images/empty-network.png',
    search: '/static/images/empty-search.png',
    error: '/static/images/empty-error.png',
    dish: '/static/images/empty-dish.png',
    merchant: '/static/images/empty-merchant.png'
  }
  return images[props.type] || images.default
})

const handleAction = () => {
  emit('action')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.empty-state {
  padding: 120rpx $spacing-lg;
  @include flex-center-column;
  gap: $spacing-md;
}

.empty-image {
  width: 320rpx;
  height: 320rpx;
  opacity: 0.8;
}

.empty-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
  text-align: center;
}

.empty-desc {
  font-size: $font-size-base;
  color: $text-color-secondary;
  text-align: center;
  line-height: 1.5;
}

.empty-action {
  margin-top: $spacing-sm;
  width: 300rpx;
  height: 80rpx;
  line-height: 80rpx;
  background: linear-gradient(135deg, $primary-color, $primary-color-light);
  color: #fff;
  border-radius: 40rpx;
  font-size: $font-size-base;
  border: none;

  &::after {
    border: none;
  }
}
</style>
