<template>
  <view
    class="custom-tag"
    :class="[
      `tag-${type}`,
      `tag-${size}`,
      { 'is-plain': plain, 'is-round': round }
    ]"
    :style="customStyle"
    @click="handleClick"
  >
    <!-- 标签图标 -->
    <text class="tag-icon" v-if="icon">{{ icon }}</text>

    <!-- 标签内容 -->
    <text class="tag-text">
      <slot></slot>
    </text>

    <!-- 关闭按钮 -->
    <text class="tag-close" v-if="closable" @click.stop="handleClose">×</text>
  </view>
</template>

<script setup>
const props = defineProps({
  // 标签类型
  type: {
    type: String,
    default: 'default'
  },
  // 标签大小
  size: {
    type: String,
    default: 'medium'
  },
  // 是否朴素样式
  plain: {
    type: Boolean,
    default: false
  },
  // 是否圆角
  round: {
    type: Boolean,
    default: false
  },
  // 是否可关闭
  closable: {
    type: Boolean,
    default: false
  },
  // 图标
  icon: String,
  // 自定义样式
  customStyle: [String, Object]
})

const emit = defineEmits(['click', 'close'])

/**
 * 点击事件
 */
const handleClick = () => {
  emit('click')
}

/**
 * 关闭事件
 */
const handleClose = () => {
  emit('close')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.custom-tag {
  @include flex-center;
  border: 1rpx solid transparent;
  transition: all 0.3s;

  /* 尺寸 */
  &.tag-mini {
    height: 40rpx;
    padding: 0 $spacing-xs;
    font-size: $font-size-xs;
  }

  &.tag-small {
    height: 48rpx;
    padding: 0 $spacing-sm;
    font-size: $font-size-sm;
  }

  &.tag-medium {
    height: 56rpx;
    padding: 0 $spacing-md;
    font-size: $font-size-base;
  }

  &.tag-large {
    height: 64rpx;
    padding: 0 $spacing-lg;
    font-size: $font-size-lg;
  }

  /* 圆角 */
  &.is-round {
    border-radius: $border-radius-round;
  }

  /* 类型 - 默认 */
  &.tag-default {
    background-color: $bg-color-base;
    color: $text-color-regular;
    border-color: $border-color-base;

    &.is-plain {
      background-color: transparent;
      color: $text-color-regular;
    }
  }

  /* 类型 - 主要 */
  &.tag-primary {
    background-color: rgba(255, 107, 53, 0.1);
    color: $primary-color;
    border-color: $primary-color;

    &.is-plain {
      background-color: transparent;
      color: $primary-color;
    }
  }

  /* 类型 - 成功 */
  &.tag-success {
    background-color: rgba(76, 217, 100, 0.1);
    color: $success-color;
    border-color: $success-color;

    &.is-plain {
      background-color: transparent;
      color: $success-color;
    }
  }

  /* 类型 - 警告 */
  &.tag-warning {
    background-color: rgba(255, 183, 77, 0.1);
    color: $warning-color;
    border-color: $warning-color;

    &.is-plain {
      background-color: transparent;
      color: $warning-color;
    }
  }

  /* 类型 - 危险 */
  &.tag-danger {
    background-color: rgba(239, 83, 80, 0.1);
    color: $danger-color;
    border-color: $danger-color;

    &.is-plain {
      background-color: transparent;
      color: $danger-color;
    }
  }
}

.tag-icon {
  margin-right: $spacing-xs;
}

.tag-text {
  font-weight: $font-weight-medium;
}

.tag-close {
  margin-left: $spacing-xs;
  font-size: $font-size-xl;
  color: inherit;
  opacity: 0.6;

  &:active {
    opacity: 1;
  }
}
</style>
