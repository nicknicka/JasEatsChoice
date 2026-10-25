<template>
  <view class="message-search">
    <!-- 搜索栏 -->
    <view class="search-bar" :class="{ focused: isFocused }">
      <uni-icons type="search" size="18" :color="isFocused ? '#FF6B35' : '#999'"></uni-icons>
      <input
        class="search-input"
        type="text"
        :value="modelValue"
        :placeholder="placeholder"
        :focus="focus"
        @input="onInput"
        @focus="onFocus"
        @blur="onBlur"
        @confirm="onConfirm"
      />
      <view class="search-clear" v-if="showClear" @tap="onClear">
        <uni-icons type="clear" size="16" color="#999"></uni-icons>
      </view>
    </view>

    <!-- 搜索建议 -->
    <view class="search-suggestions" v-if="showSuggestions && suggestions.length > 0">
      <view class="suggestion-title">搜索历史</view>
      <view class="suggestion-list">
        <view
          class="suggestion-item"
          v-for="(item, index) in suggestions"
          :key="index"
          @tap="onSelectSuggestion(item)"
        >
          <uni-icons type="clock" size="14" color="#999"></uni-icons>
          <text class="suggestion-text">{{ item }}</text>
          <uni-icons
            type="close"
            size="14"
            color="#999"
            @tap.stop="onDeleteSuggestion(index)"
          ></uni-icons>
        </view>
      </view>
      <view class="suggestion-actions">
        <text class="action-btn" @tap="onClearHistory">清空历史</text>
      </view>
    </view>

    <!-- 快捷筛选 -->
    <view class="quick-filters" v-if="showFilters && !modelValue">
      <scroll-view class="filters-scroll" scroll-x>
        <view
          class="filter-chip"
          :class="{ active: activeFilter === item.value }"
          v-for="item in filters"
          :key="item.value"
          @tap="onFilter(item.value)"
        >
          <text class="filter-icon">{{ item.icon }}</text>
          <text class="filter-text">{{ item.label }}</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '搜索联系人、消息...'
  },
  focus: {
    type: Boolean,
    default: false
  },
  showFilters: {
    type: Boolean,
    default: true
  },
  suggestions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'clear', 'filter', 'deleteSuggestion', 'clearHistory'])

const isFocused = ref(false)
const activeFilter = ref('all')

// 快捷筛选选项
const filters = [
  { label: '全部', value: 'all', icon: '📋' },
  { label: '未读', value: 'unread', icon: '🔴' },
  { label: '置顶', value: 'pinned', icon: '⭐' },
  { label: '群聊', value: 'group', icon: '👥' }
]

// 是否显示清除按钮
const showClear = computed(() => {
  return props.modelValue && props.modelValue.length > 0
})

// 是否显示搜索建议
const showSuggestions = computed(() => {
  return isFocused.value && !props.modelValue && props.suggestions.length > 0
})

/**
 * 输入事件
 */
const onInput = (e) => {
  emit('update:modelValue', e.detail.value)
}

/**
 * 获取焦点
 */
const onFocus = () => {
  isFocused.value = true
}

/**
 * 失去焦点
 */
const onBlur = () => {
  // 延迟失焦，让点击事件先触发
  setTimeout(() => {
    isFocused.value = false
  }, 200)
}

/**
 * 确认搜索
 */
const onConfirm = () => {
  emit('search', props.modelValue)
  // 震动反馈
  uni.vibrateShort({
    type: 'light'
  })
}

/**
 * 清除输入
 */
const onClear = () => {
  emit('update:modelValue', '')
  emit('clear')
}

/**
 * 选择搜索建议
 */
const onSelectSuggestion = (item) => {
  emit('update:modelValue', item)
  emit('search', item)
}

/**
 * 删除搜索建议
 */
const onDeleteSuggestion = (index) => {
  emit('deleteSuggestion', index)
}

/**
 * 清空历史
 */
const onClearHistory = () => {
  uni.showModal({
    title: '清空历史',
    content: '确定清空所有搜索历史吗？',
    success: (res) => {
      if (res.confirm) {
        emit('clearHistory')
      }
    }
  })
}

/**
 * 筛选
 */
const onFilter = (value) => {
  activeFilter.value = value
  emit('filter', value)

  // 震动反馈
  uni.vibrateShort({
    type: 'light'
  })
}

// 监听输入变化，自动搜索
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    emit('search', newVal)
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.message-search {
  background: #fff;
  border-bottom: 1rpx solid $border-color-lighter;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 15rpx;
  padding: 20rpx 30rpx;
  background: $bg-color-base;
  border-radius: 30rpx;
  margin: 20rpx 30rpx;
  transition: all 0.3s ease;
  border: 2rpx solid transparent;

  &.focused {
    background: #fff;
    border-color: $primary-color;
    box-shadow: 0 2rpx 8rpx rgba(255, 107, 53, 0.2);
  }
}

.search-input {
  flex: 1;
  height: 60rpx;
  font-size: $font-size-base;
  color: $text-color-primary;
}

.search-clear {
  width: 40rpx;
  height: 40rpx;
  @include flex-center;
  border-radius: 50%;
  transition: background 0.3s ease;

  &:active {
    background: rgba(0, 0, 0, 0.05);
  }
}

/* 搜索建议 */
.search-suggestions {
  padding: 0 30rpx 20rpx;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.suggestion-title {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  margin-bottom: 20rpx;
  padding-left: 10rpx;
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 15rpx;
  padding: 20rpx;
  background: $bg-color-base;
  border-radius: $border-radius-base;
  transition: all 0.3s ease;

  &:active {
    background: rgba(255, 107, 53, 0.1);
    transform: scale(0.98);
  }
}

.suggestion-text {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
}

.suggestion-actions {
  margin-top: 20rpx;
  text-align: center;
}

.action-btn {
  font-size: $font-size-sm;
  color: $primary-color;
  padding: 10rpx 20rpx;

  &:active {
    opacity: 0.6;
  }
}

/* 快捷筛选 */
.quick-filters {
  padding: 20rpx 30rpx;
}

.filters-scroll {
  white-space: nowrap;
}

.filter-chip {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  margin-right: 15rpx;
  background: $bg-color-base;
  border-radius: $border-radius-round;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
  flex-shrink: 0;

  &.active {
    background: linear-gradient(135deg, rgba(255, 107, 53, 0.1), rgba(255, 107, 53, 0.15));
    border-color: $primary-color;
  }

  &:active {
    transform: scale(0.95);
  }
}

.filter-icon {
  font-size: 32rpx;
  line-height: 1;
}

.filter-text {
  font-size: $font-size-sm;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}
</style>
