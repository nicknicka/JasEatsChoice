<template>
  <view class="empty-container">
    <view class="empty-icon-wrapper">
      <uni-icons
        :type="resolvedIcon.type"
        :size="40"
        :color="resolvedIcon.color"
      ></uni-icons>
    </view>

    <text class="empty-text">{{ text }}</text>

    <text class="empty-desc" v-if="description">{{ description }}</text>

    <button class="empty-btn" v-if="buttonText" @click="handleButtonClick">
      {{ buttonText }}
    </button>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  icon: {
    type: String,
    default: 'info-filled'
  },
  text: {
    type: String,
    default: '暂无数据'
  },
  description: String,
  buttonText: String
})

const emit = defineEmits(['button-click'])

const iconMap = {
  '📦': { type: 'list', color: '#FF6B35' },
  '🍽️': { type: 'shop', color: '#FF6B35' },
  '💭': { type: 'chatbubble', color: '#FF6B35' },
  '🍜': { type: 'shop', color: '#FF6B35' },
  '👥': { type: 'staff', color: '#FF6B35' },
  '🔍': { type: 'search', color: '#FF6B35' },
  '📬': { type: 'email', color: '#FF6B35' },
  '💰': { type: 'wallet-filled', color: '#FF6B35' },
  '💝': { type: 'gift-filled', color: '#FF6B35' },
  '📋': { type: 'list', color: '#FF6B35' },
  '💬': { type: 'chatbubble-filled', color: '#FF6B35' },
  '📝': { type: 'compose', color: '#FF6B35' },
  '🍳': { type: 'compose', color: '#FF6B35' },
  '⭐': { type: 'star-filled', color: '#FF6B35' },
  '🏪': { type: 'shop', color: '#FF6B35' },
  '📖': { type: 'list', color: '#FF6B35' },
  '📢': { type: 'notification-filled', color: '#FF6B35' },
  '💳': { type: 'wallet-filled', color: '#FF6B35' }
}

const resolvedIcon = computed(() => {
  return iconMap[props.icon] || {
    type: props.icon || 'info-filled',
    color: '#FF6B35'
  }
})

/**
 * 按钮点击
 */
const handleButtonClick = () => {
  emit('button-click')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.empty-container {
  @include flex-center-column;
  padding: 120rpx $spacing-lg;
  text-align: center;
}

.empty-icon-wrapper {
  width: 104rpx;
  height: 104rpx;
  border-radius: 28rpx;
  background-color: #FFF3ED;
  @include flex-center;
  margin-bottom: $spacing-lg;
}

.empty-text {
  font-size: $font-size-lg;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
  margin-bottom: $spacing-sm;
}

.empty-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  margin-bottom: $spacing-xl;
  line-height: $line-height-lg;
}

.empty-btn {
  min-width: 240rpx;
  height: 72rpx;
  @include flex-center;
  background: linear-gradient(135deg, $primary-color, #FF8F61);
  color: #fff;
  font-size: $font-size-base;
  border-radius: $border-radius-round;
  border: none;
  padding: 0 $spacing-xl;

  &:active {
    opacity: 0.8;
  }
}
</style>
