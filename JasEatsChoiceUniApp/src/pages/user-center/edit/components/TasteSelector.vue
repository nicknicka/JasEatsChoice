<template>
  <view class="taste-selector">
    <view class="form-item" @click="showSelector">
      <text class="form-label">口味偏好</text>
      <view class="form-value">
        <text class="value-text">{{ selectedText || '请选择' }}</text>
        <text class="value-arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 选中的口味值列表
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

// 口味选项
const options = [
  { value: 'spicy', label: '辣味' },
  { value: 'sweet', label: '甜味' },
  { value: 'sour', label: '酸味' },
  { value: 'salty', label: '咸鲜' },
  { value: 'light', label: '清淡' }
]

/**
 * 选中的文本
 */
const selectedText = computed(() => {
  if (!props.modelValue || props.modelValue.length === 0) return ''
  return props.modelValue.map(value => {
    const option = options.find(opt => opt.value === value)
    return option ? option.label : value
  }).join('、')
})

/**
 * 显示选择器
 */
const showSelector = () => {
  const items = options.map(opt => opt.label)

  uni.showActionSheet({
    itemList: items,
    success: (res) => {
      const selected = options[res.tapIndex]
      toggleTaste(selected.value)
    }
  })
}

/**
 * 切换口味
 */
const toggleTaste = (value) => {
  const newList = [...(props.modelValue || [])]
  const index = newList.indexOf(value)

  if (index > -1) {
    newList.splice(index, 1)
  } else {
    newList.push(value)
  }

  emit('update:modelValue', newList)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.taste-selector {
  width: 100%;
}

.form-item {
  @include flex-between;
  align-items: center;
  padding: $spacing-lg 0;
  border-bottom: 1rpx solid $border-color-lighter;

  &:active {
    background-color: $bg-color-base;
    margin: 0 (-$spacing-md);
    padding-left: $spacing-md;
    padding-right: $spacing-md;
  }
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
  text-align: right;
}

.value-arrow {
  font-size: $font-size-xl;
  color: $text-color-placeholder;
}
</style>
