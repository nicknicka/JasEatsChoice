<template>
  <view class="nutrition-bar" :class="{ vertical: direction === 'vertical' }">
    <!-- 标签区域 -->
    <view class="bar-label">
      <text class="label-icon" v-if="icon">{{ icon }}</text>
      <text class="label-text">{{ label }}</text>
    </view>

    <!-- 进度条区域 -->
    <view class="bar-progress">
      <view class="progress-track">
        <view
          class="progress-fill"
          :style="{
            width: percent + '%',
            background: color
          }"
        >
          <view class="progress-shine" v-if="showShine && percent > 0"></view>
        </view>
      </view>

      <!-- 数值显示 -->
      <view class="bar-value" v-if="showValue">
        <text class="value-current">{{ current }}</text>
        <text class="value-separator" v-if="target">/</text>
        <text class="value-target" v-if="target">{{ target }}</text>
        <text class="value-unit">{{ unit }}</text>
      </view>
    </view>

    <!-- 百分比文字（可选） -->
    <view class="bar-percent" v-if="showPercent">
      <text class="percent-text" :class="percentClass">{{ Math.round(percent) }}%</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 标签
  label: {
    type: String,
    required: true
  },
  // 图标
  icon: {
    type: String,
    default: ''
  },
  // 当前值
  current: {
    type: [Number, String],
    required: true
  },
  // 目标值
  target: {
    type: [Number, String],
    default: 0
  },
  // 单位
  unit: {
    type: String,
    default: ''
  },
  // 颜色
  color: {
    type: String,
    default: '#FF6B35'
  },
  // 是否显示数值
  showValue: {
    type: Boolean,
    default: true
  },
  // 是否显示百分比
  showPercent: {
    type: Boolean,
    default: false
  },
  // 是否显示光泽效果
  showShine: {
    type: Boolean,
    default: true
  },
  // 方向：horizontal 横向 / vertical 纵向
  direction: {
    type: String,
    default: 'horizontal'
  }
})

/**
 * 百分比
 */
const percent = computed(() => {
  if (!props.target || props.target === 0) return 0
  const value = (parseFloat(props.current) / parseFloat(props.target)) * 100
  return Math.min(Math.max(value, 0), 100)
})

/**
 * 百分比样式类
 */
const percentClass = computed(() => {
  if (percent.value >= 100) return 'complete'
  if (percent.value >= 80) return 'high'
  if (percent.value >= 50) return 'medium'
  return 'low'
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.nutrition-bar {
  @include flex-center;
  width: 100%;
  gap: $spacing-sm;

  &.vertical {
    flex-direction: column;
    align-items: stretch;

    .bar-progress {
      flex-direction: column;
      gap: $spacing-xs;
    }

    .progress-track {
      width: 24rpx;
      height: 100%;
      min-height: 200rpx;
    }

    .progress-fill {
      width: 100% !important;
      height: v-bind('percent + "%');
      bottom: 0;
      top: auto;
    }

    .bar-value {
      text-align: center;
    }
  }
}

.bar-label {
  @include flex-center;
  gap: $spacing-xs;
  flex-shrink: 0;
  min-width: 120rpx;
}

.label-icon {
  font-size: $font-size-xl;
}

.label-text {
  font-size: $font-size-sm;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}

.bar-progress {
  flex: 1;
  @include flex-center;
  gap: $spacing-sm;
}

.progress-track {
  position: relative;
  flex: 1;
  height: 16rpx;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
  overflow: hidden;
}

.progress-fill {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  border-radius: $border-radius-round;
  transition: width 0.3s ease;
  overflow: hidden;
}

.progress-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  animation: shine 2s infinite;
}

@keyframes shine {
  0% {
    left: -100%;
  }
  100% {
    left: 200%;
  }
}

.bar-value {
  @include flex-center;
  gap: 4rpx;
  flex-shrink: 0;
  min-width: 120rpx;
  justify-content: flex-end;
}

.value-current {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.value-separator {
  font-size: $font-size-sm;
  color: $text-color-placeholder;
}

.value-target {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.value-unit {
  font-size: $font-size-xs;
  color: $text-color-placeholder;
  margin-left: 4rpx;
}

.bar-percent {
  flex-shrink: 0;
  min-width: 80rpx;
  text-align: right;
}

.percent-text {
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;

  &.low {
    color: $danger-color;
  }

  &.medium {
    color: $warning-color;
  }

  &.high {
    color: $primary-color;
  }

  &.complete {
    color: $success-color;
  }
}
</style>
