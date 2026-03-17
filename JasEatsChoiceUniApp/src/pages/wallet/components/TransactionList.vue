<template>
  <view class="transactions-section">
    <view class="section-header">
      <text class="section-title">交易记录</text>
      <text class="section-more" @click="handleViewAll">查看全部 →</text>
    </view>

    <!-- 筛选Tab -->
    <view class="filter-tabs">
      <view
        class="tab-item"
        :class="{ active: activeTab === 'all' }"
        @click="handleTabChange('all')"
      >
        <text class="tab-text">全部</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'income' }"
        @click="handleTabChange('income')"
      >
        <text class="tab-text">收入</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'expense' }"
        @click="handleTabChange('expense')"
      >
        <text class="tab-text">支出</text>
      </view>
    </view>

    <!-- 交易列表 -->
    <scroll-view
      class="scroll-container"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
      @scrolltolower="handleLoadMore"
    >
      <!-- 空状态 -->
      <view class="empty-state" v-if="transactions.length === 0 && !loading">
        <Empty
          icon="💳"
          text="还没有交易记录"
          description="充值或消费后会显示在这里"
        />
      </view>

      <!-- 交易列表 -->
      <view class="transaction-list" v-else>
        <!-- 日期分组 -->
        <view
          class="date-group"
          v-for="group in groupedTransactions"
          :key="group.date"
        >
          <!-- 日期标题 -->
          <view class="date-title">
            <text class="date-text">{{ group.dateText }}</text>
            <text class="date-amount">
              {{ group.income > 0 ? '+' : '' }}{{ group.income }}
              {{ group.expense > 0 ? '-' : '' }}{{ group.expense }}
            </text>
          </view>

          <!-- 交易项列表 -->
          <view class="transaction-items">
            <TransactionItem
              v-for="item in group.items"
              :key="item.id"
              :transaction="item"
              @click="handleItemClick"
            />
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-more" v-if="hasMore && transactions.length > 0">
        <view class="load-text" v-if="loading">加载中...</view>
        <view class="load-text" v-else>上拉加载更多</view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import Empty from '@/components/common/Empty.vue'
import TransactionItem from './TransactionItem.vue'

const props = defineProps({
  // 交易记录列表
  transactions: {
    type: Array,
    default: () => []
  },
  // 当前激活的Tab
  activeTab: {
    type: String,
    default: 'all'
  },
  // 是否在加载
  loading: {
    type: Boolean,
    default: false
  },
  // 是否正在刷新
  refreshing: {
    type: Boolean,
    default: false
  },
  // 是否有更多数据
  hasMore: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits([
  'tabChange',
  'refresh',
  'loadMore',
  'viewAll',
  'itemClick'
])

/**
 * 按日期分组交易记录
 */
const groupedTransactions = computed(() => {
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  const groups = {}

  props.transactions.forEach(item => {
    const date = new Date(item.time)
    const dateKey = `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`

    if (!groups[dateKey]) {
      const dateText = formatDateText(date)
      groups[dateKey] = {
        date: dateKey,
        dateText,
        items: [],
        income: 0,
        expense: 0
      }
    }

    groups[dateKey].items.push(item)
    if (item.type === 'income') {
      groups[dateKey].income += item.amount
    } else {
      groups[dateKey].expense += item.amount
    }
  })

  return Object.values(groups).sort((a, b) => {
    return new Date(b.date) - new Date(a.date)
  })
})

/**
 * 格式化日期文本
 */
function formatDateText(date) {
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  const todayStr = `${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`
  const yesterdayStr = `${yesterday.getFullYear()}-${yesterday.getMonth() + 1}-${yesterday.getDate()}`
  const dateStr = `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`

  if (dateStr === todayStr) {
    return '今天'
  } else if (dateStr === yesterdayStr) {
    return '昨天'
  } else {
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}

/**
 * 切换Tab
 */
const handleTabChange = (tab) => {
  emit('tabChange', tab)
}

/**
 * 下拉刷新
 */
const handleRefresh = () => {
  emit('refresh')
}

/**
 * 上拉加载更多
 */
const handleLoadMore = () => {
  emit('loadMore')
}

/**
 * 查看全部
 */
const handleViewAll = () => {
  emit('viewAll')
}

/**
 * 点击交易项
 */
const handleItemClick = (item) => {
  emit('itemClick', item)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.transactions-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.section-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.section-more {
  font-size: $font-size-sm;
  color: $primary-color;

  &:active {
    opacity: 0.6;
  }
}

.filter-tabs {
  @include flex-center;
  background-color: $bg-color-base;
  padding: $spacing-xs;
  border-radius: $border-radius-lg;
  margin-bottom: $spacing-md;
}

.tab-item {
  flex: 1;
  @include flex-center;
  padding: $spacing-sm;
  border-radius: $border-radius-base;
  transition: all 0.3s;

  &.active {
    background-color: #fff;
    box-shadow: $box-shadow-sm;

    .tab-text {
      color: $primary-color;
      font-weight: $font-weight-bold;
    }
  }
}

.tab-text {
  font-size: $font-size-sm;
  color: $text-color-regular;
}

.scroll-container {
  max-height: 800rpx;
}

.empty-state {
  padding: 80rpx 0;
}

.transaction-list {
  @include flex-center-column;
  gap: $spacing-md;
}

.date-group {
  margin-bottom: $spacing-sm;
}

.date-title {
  @include flex-between;
  padding: $spacing-sm $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
}

.date-text {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
}

.date-amount {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.transaction-items {
  background-color: $bg-color-white;
  border-radius: $border-radius-base;
  overflow: hidden;
}

.load-more {
  @include flex-center;
  padding: $spacing-lg 0;
}

.load-text {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}
</style>
