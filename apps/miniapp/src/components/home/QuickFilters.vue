<template>
  <view class="quick-filters" v-if="showFilters">
    <scroll-view class="filter-scroll" scroll-x show-scrollbar="false">
      <view class="filter-list">
        <view
          class="filter-item"
          :class="{ active: activeFilter === 'all' }"
          @click="handleFilter('all')"
        >
          <text class="filter-icon">🍽️</text>
          <text class="filter-name">全部</text>
        </view>

        <view
          class="filter-item"
          :class="{ active: activeFilter === 'low-calorie' }"
          @click="handleFilter('low-calorie')"
        >
          <text class="filter-icon">🥗</text>
          <text class="filter-name">低卡</text>
        </view>

        <view
          class="filter-item"
          :class="{ active: activeFilter === 'high-rating' }"
          @click="handleFilter('high-rating')"
        >
          <text class="filter-icon">⭐</text>
          <text class="filter-name">高分</text>
        </view>

        <view
          class="filter-item"
          :class="{ active: activeFilter === 'nearby' }"
          @click="handleFilter('nearby')"
        >
          <text class="filter-icon">📍</text>
          <text class="filter-name">附近</text>
        </view>

        <view
          class="filter-item"
          :class="{ active: activeFilter === 'discount' }"
          @click="handleFilter('discount')"
        >
          <text class="filter-icon">💰</text>
          <text class="filter-name">优惠</text>
        </view>

        <view
          class="filter-item"
          :class="{ active: activeFilter === 'spicy' }"
          @click="handleFilter('spicy')"
        >
          <text class="filter-icon">🌶️</text>
          <text class="filter-name">辣味</text>
        </view>

        <view
          class="filter-item"
          :class="{ active: activeFilter === 'sweet' }"
          @click="handleFilter('sweet')"
        >
          <text class="filter-icon">🍬</text>
          <text class="filter-name">甜食</text>
        </view>
      </view>
    </scroll-view>

    <view class="filter-more" @click="showMoreFilters">
      <text class="more-icon">⚙️</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: 'all'
  },
  showFilters: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'filter-change', 'more-filters'])

const activeFilter = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

/**
 * 处理筛选点击
 */
const handleFilter = (filterType) => {
  activeFilter.value = filterType
  emit('filter-change', filterType)
}

/**
 * 显示更多筛选
 */
const showMoreFilters = () => {
  emit('more-filters')
  uni.navigateTo({
    url: '/pages-user/filter/index'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.quick-filters {
  @include flex-between;
  padding: $spacing-md;
  background-color: $bg-color-white;
  margin-bottom: $spacing-md;
}

.filter-scroll {
  flex: 1;
  white-space: nowrap;
}

.filter-list {
  @include flex-center;
  gap: $spacing-md;
}

.filter-item {
  @include flex-center-column;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  transition: all 0.3s ease;
  flex-shrink: 0;

  &.active {
    background: linear-gradient(135deg, $primary-color, $primary-color-light);
    color: #fff;
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);

    .filter-name {
      color: #fff;
      font-weight: $font-weight-medium;
    }
  }

  &:active {
    transform: scale(0.95);
  }

  .filter-icon {
    font-size: 40rpx;
  }

  .filter-name {
    font-size: $font-size-sm;
    color: $text-color-regular;
  }
}

.filter-more {
  margin-left: $spacing-sm;
  padding: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.9);
    background-color: darken($bg-color-base, 5%);
  }

  .more-icon {
    font-size: 40rpx;
  }
}
</style>
