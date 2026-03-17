<template>
  <view
    class="custom-button"
    :class="[
      `button-${type}`,
      `button-${size}`,
      { 'is-disabled': disabled, 'is-plain': plain, 'is-round': round, 'is-circle': circle }
    ]"
    :style="customStyle"
    @click="handleClick"
  >
    <!-- 加载图标 -->
    <text class="button-loading" v-if="loading">⟳</text>

    <!-- 图标 -->
    <text class="button-icon" v-if="icon && !loading">{{ icon }}</text>

    <!-- 按钮文字 -->
    <text class="button-text" v-if="!circle">
      <slot></slot>
    </text>
  </view>
</template>

<script setup>
const props = defineProps({
  // 按钮类型
  type: {
    type: String,
    default: 'default'
  },
  // 按钮大小
  size: {
    type: String,
    default: 'medium'
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否朴素按钮
  plain: {
    type: Boolean,
    default: false
  },
  // 是否圆角
  round: {
    type: Boolean,
    default: false
  },
  // 是否圆形
  circle: {
    type: Boolean,
    default: false
  },
  // 是否加载中
  loading: {
    type: Boolean,
    default: false
  },
  // 图标
  icon: String,
  // 自定义样式
  customStyle: [String, Object]
})

const emit = defineEmits(['click'])

/**
 * 点击事件
 */
const handleClick = () => {
  if (!props.disabled && !props.loading) {
    emit('click')
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.custom-button {
  @include flex-center;
  border: none;
  transition: all 0.3s;

  &.is-disabled {
    opacity: 0.6;
  }

  &:active:not(.is-disabled) {
    transform: scale(0.95);
  }

  /* 尺寸 */
  &.button-mini {
    height: 56rpx;
    padding: 0 $spacing-sm;
    font-size: $font-size-xs;
  }

  &.button-small {
    height: 64rpx;
    padding: 0 $spacing-md;
    font-size: $font-size-sm;
  }

  &.button-medium {
    height: 72rpx;
    padding: 0 $spacing-lg;
    font-size: $font-size-base;
  }

  &.button-large {
    height: 88rpx;
    padding: 0 $spacing-xl;
    font-size: $font-size-lg;
  }

  /* 圆形 */
  &.is-circle {
    width: 72rpx;
    height: 72rpx;
    padding: 0;
    border-radius: 50%;
  }

  /* 圆角 */
  &.is-round {
    border-radius: $border-radius-round;
  }

  /* 类型 - 默认 */
  &.button-default {
    background-color: $bg-color-white;
    color: $text-color-primary;
    border: 1rpx solid $border-color-base;

    &.is-plain {
      background-color: transparent;
    }
  }

  /* 类型 - 主要 */
  &.button-primary {
    background: linear-gradient(135deg, $primary-color, #FF8F61);
    color: #fff;

    &.is-plain {
      background-color: transparent;
      color: $primary-color;
      border: 1rpx solid $primary-color;
    }
  }

  /* 类型 - 成功 */
  &.button-success {
    background-color: $success-color;
    color: #fff;

    &.is-plain {
      background-color: transparent;
      color: $success-color;
      border: 1rpx solid $success-color;
    }
  }

  /* 类型 - 警告 */
  &.button-warning {
    background-color: $warning-color;
    color: #fff;

    &.is-plain {
      background-color: transparent;
      color: $warning-color;
      border: 1rpx solid $warning-color;
    }
  }

  /* 类型 - 危险 */
  &.button-danger {
    background-color: $danger-color;
    color: #fff;

    &.is-plain {
      background-color: transparent;
      color: $danger-color;
      border: 1rpx solid $danger-color;
    }
  }

  /* 类型 - 文本 */
  &.button-text {
    background-color: transparent;
    color: $primary-color;
    border: none;

    &.is-plain {
      background-color: transparent;
      border: none;
    }
  }
}

.button-loading {
  margin-right: $spacing-xs;
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.button-icon {
  margin-right: $spacing-xs;
}

.button-text {
  font-weight: $font-weight-medium;
}
</style>
