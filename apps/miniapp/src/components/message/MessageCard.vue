<template>
  <view class="message-card" :class="`type-${type}`" @tap="onClick">
    <!-- 左侧图标 -->
    <view class="card-icon">
      <text class="icon-emoji">{{ iconEmoji }}</text>
      <view class="icon-badge" v-if="badge">
        <text class="badge-text">{{ badge > 99 ? '99+' : badge }}</text>
      </view>
    </view>

    <!-- 中间内容 -->
    <view class="card-content">
      <view class="content-header">
        <text class="title">{{ title }}</text>
        <text class="time">{{ time }}</text>
      </view>

      <text class="description">{{ description }}</text>

      <!-- 扩展内容 -->
      <view class="content-extra" v-if="$slots.extra">
        <slot name="extra"></slot>
      </view>

      <!-- 标签 -->
      <view class="content-tags" v-if="tags && tags.length > 0">
        <view class="tag" v-for="(tag, index) in tags" :key="index">
          <text class="tag-text">{{ tag }}</text>
        </view>
      </view>
    </view>

    <!-- 右侧箭头 -->
    <view class="card-arrow" v-if="showArrow">
      <text class="arrow-icon">›</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'default', // default, system, order, activity, warning, success
    validator: (value) => ['default', 'system', 'order', 'activity', 'warning', 'success'].includes(value)
  },
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  time: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: ''
  },
  badge: {
    type: [Number, String],
    default: 0
  },
  tags: {
    type: Array,
    default: () => []
  },
  showArrow: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['click'])

// 图标映射
const iconMap = {
  system: '📢',
  order: '📦',
  activity: '🎉',
  warning: '⚠️',
  success: '✅',
  default: '📄'
}

const iconEmoji = computed(() => {
  return props.icon || iconMap[props.type] || iconMap.default
})

const onClick = () => {
  emit('click')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.message-card {
  background: #fff;
  border-radius: $border-radius-lg;
  padding: 25rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6rpx;
    background: $text-color-placeholder;
    transition: background 0.3s ease;
  }

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.12);
  }

  &.type-system::before {
    background: linear-gradient(180deg, #FFB74D, #FF9800);
  }

  &.type-order::before {
    background: linear-gradient(180deg, #64B5F6, #2196F3);
  }

  &.type-activity::before {
    background: linear-gradient(180deg, #FF6B35, #FF8F61);
  }

  &.type-warning::before {
    background: linear-gradient(180deg, #FAAD14, #FA8C16);
  }

  &.type-success::before {
    background: linear-gradient(180deg, #52C41A, #389E0D);
  }
}

.card-icon {
  position: relative;
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  @include flex-center;
  flex-shrink: 0;
  background: $bg-color-base;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.icon-emoji {
  font-size: 48rpx;
  line-height: 1;
}

.icon-badge {
  position: absolute;
  top: -4rpx;
  right: -4rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 6rpx;
  background: linear-gradient(135deg, #ff6b6b, #ff5252);
  border-radius: $border-radius-round;
  @include flex-center;
  box-shadow: 0 2rpx 6rpx rgba(255, 82, 82, 0.4);
  border: 2rpx solid #fff;
}

.badge-text {
  font-size: 20rpx;
  color: #fff;
  font-weight: $font-weight-bold;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12rpx;
}

.title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  flex: 1;
  margin-right: $spacing-sm;
}

.time {
  font-size: $font-size-sm;
  color: $text-color-placeholder;
  flex-shrink: 0;
}

.description {
  display: block;
  font-size: $font-size-base;
  color: $text-color-regular;
  line-height: 1.6;
  margin-bottom: 12rpx;
  @include text-ellipsis-multiline(2);
}

.content-extra {
  margin-bottom: 12rpx;
}

.content-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.tag {
  padding: 6rpx 16rpx;
  background: rgba(255, 107, 53, 0.1);
  border-radius: $border-radius-round;
  border: 1rpx solid rgba(255, 107, 53, 0.2);
}

.tag-text {
  font-size: $font-size-xs;
  color: $primary-color;
  font-weight: $font-weight-medium;
}

.card-arrow {
  flex-shrink: 0;
  width: 40rpx;
  height: 100%;
  @include flex-center;
}

.arrow-icon {
  font-size: 48rpx;
  color: $text-color-placeholder;
  transition: transform 0.3s ease;
}

.message-card:active .arrow-icon {
  transform: translateX(6rpx);
}
</style>
