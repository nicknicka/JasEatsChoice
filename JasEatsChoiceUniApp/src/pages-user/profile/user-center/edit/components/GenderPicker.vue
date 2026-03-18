<template>
  <picker
    mode="selector"
    :range="options"
    :value="selectedIndex"
    @change="handleChange"
  >
    <view class="form-item" @click.stop>
      <text class="form-label">性别</text>
      <view class="form-value">
        <text class="value-text">{{ selectedText }}</text>
        <text class="value-arrow">›</text>
      </view>
    </view>
  </picker>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 当前选中的值
  modelValue: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:modelValue'])

// 性别选项
const options = ['保密', '男', '女']

/**
 * 当前选中的索引
 */
const selectedIndex = computed(() => {
  return props.modelValue || 0
})

/**
 * 选中的文本
 */
const selectedText = computed(() => {
  return options[props.modelValue] || '保密'
})

/**
 * 处理选择变化
 */
const handleChange = (e) => {
  emit('update:modelValue', parseInt(e.detail.value))
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.form-item {
  @include flex-between;
  align-items: center;
  padding: $spacing-lg 0;
  border-bottom: 1rpx solid $border-color-lighter;
}

.form-label {
  width: 160rpx;
  font-size: $font-size-base;
  color: $text-color-primary;
  flex-shrink: 0;
}

.form-value {
  flex: 1;
  @include flex-center;
  justify-content: flex-end;
  gap: $spacing-sm;
}

.value-text {
  font-size: $font-size-base;
  color: $text-color-primary;
}

.value-arrow {
  font-size: $font-size-xl;
  color: $text-color-placeholder;
}
</style>
