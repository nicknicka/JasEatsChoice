<template>
  <view class="search-bar-container">
    <!-- 搜索栏 -->
    <view class="search-bar" @click="toSearch">
      <view class="search-icon">🔍</view>
      <view class="search-input">{{ placeholder }}</view>
      <view class="search-scan" @click.stop="handleScan">
        📷
      </view>
    </view>

    <!-- 热门搜索 -->
    <view class="hot-search" v-if="hotSearches.length > 0 && !showHistory">
      <view class="hot-header">
        <text class="hot-label">🔥 热门搜索</text>
      </view>
      <view class="hot-tags">
        <text
          class="hot-tag"
          :class="{ 'top-tag': index < 3 }"
          v-for="(tag, index) in hotSearches"
          :key="index"
          @click.stop="handleHotSearch(tag)"
        >
          <text v-if="index < 3" class="rank-num">{{ index + 1 }}</text>
          {{ tag }}
        </text>
      </view>
    </view>

    <!-- 搜索历史 -->
    <view class="search-history" v-if="searchHistory.length > 0 && showHistory">
      <view class="history-header">
        <text class="history-title">搜索历史</text>
        <view class="history-actions">
          <text class="history-clear" @click.stop="clearHistory">清空</text>
        </view>
      </view>
      <view class="history-list">
        <text
          class="history-item"
          v-for="(item, index) in searchHistory"
          :key="index"
          @click.stop="handleHistoryClick(item)"
        >
          {{ item }}
        </text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useSearchHistory } from '@/composables/useSearchHistory'

const props = defineProps({
  placeholder: {
    type: String,
    default: '搜索菜品、商家或食谱...'
  }
})

const { searchHistory, addHistory, clearHistory } = useSearchHistory()

const hotSearches = ref([])
const showHistory = ref(false)

/**
 * 加载热门搜索
 */
const loadHotSearches = async () => {
  try {
    const res = await uni.request({
      url: '/api/v1/search/hot',
      method: 'GET'
    })

    if (res.data && res.data.data) {
      hotSearches.value = res.data.data.slice(0, 10)
    }
  } catch (error) {
    console.error('加载热门搜索失败:', error)
    // 降级：使用默认热门搜索
    hotSearches.value = ['番茄炒蛋', '宫保鸡丁', '红烧肉', '鱼香肉丝', '麻婆豆腐']
  }
}

/**
 * 处理热门搜索点击
 */
const handleHotSearch = (tag) => {
  addHistory(tag)
  uni.navigateTo({
    url: `/pages-user/search/result?keyword=${encodeURIComponent(tag)}`
  })
}

/**
 * 处理历史搜索点击
 */
const handleHistoryClick = (item) => {
  uni.navigateTo({
    url: `/pages-user/search/result?keyword=${encodeURIComponent(item)}`
  })
}

/**
 * 扫码搜索
 */
const handleScan = () => {
  uni.scanCode({
    success: (res) => {
      console.log('扫码结果:', res.result)
      // 跳转到搜索结果
      uni.navigateTo({
        url: `/pages-user/search/result?keyword=${encodeURIComponent(res.result)}`
      })
    },
    fail: () => {
      uni.showToast({
        title: '扫码失败',
        icon: 'none'
      })
    }
  })
}

/**
 * 跳转到搜索页面
 */
const toSearch = () => {
  uni.navigateTo({
    url: '/pages-user/search/index'
  })
}

onMounted(() => {
  loadHotSearches()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.search-bar-container {
  padding: $spacing-md;
  background-color: $bg-color-white;
}

.search-bar {
  @include flex-center;
  gap: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
  padding: $spacing-sm $spacing-md;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.98);
    background-color: darken($bg-color-base, 5%);
  }

  .search-icon {
    font-size: $font-size-lg;
  }

  .search-input {
    flex: 1;
    font-size: $font-size-base;
    color: $text-color-secondary;
  }

  .search-scan {
    font-size: $font-size-lg;
    padding: $spacing-xs;
    transition: transform 0.2s ease;

    &:active {
      transform: scale(0.9);
    }
  }
}

.hot-search {
  margin-top: $spacing-md;

  .hot-header {
    margin-bottom: $spacing-sm;

    .hot-label {
      font-size: $font-size-sm;
      font-weight: $font-weight-medium;
      color: $text-color-primary;
    }
  }

  .hot-tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  .hot-tag {
    padding: 8rpx 20rpx;
    background-color: $bg-color-base;
    border-radius: $border-radius-round;
    font-size: $font-size-sm;
    color: $text-color-regular;
    transition: all 0.2s ease;

    &:active {
      transform: scale(0.95);
    }

    &.top-tag {
      background: linear-gradient(135deg, rgba(255, 107, 53, 0.1), rgba(255, 107, 53, 0.15));
      color: $primary-color;
      font-weight: $font-weight-medium;
      border: 1px solid rgba(255, 107, 53, 0.2);

      .rank-num {
        display: inline-block;
        width: 28rpx;
        height: 28rpx;
        line-height: 28rpx;
        text-align: center;
        background: linear-gradient(135deg, $primary-color, $primary-color-light);
        color: #fff;
        border-radius: 50%;
        font-size: $font-size-xs - 2rpx;
        margin-right: 4rpx;
      }
    }
  }
}

.search-history {
  margin-top: $spacing-md;

  .history-header {
    @include flex-between;
    margin-bottom: $spacing-sm;

    .history-title {
      font-size: $font-size-sm;
      font-weight: $font-weight-medium;
      color: $text-color-primary;
    }

    .history-actions {
      .history-clear {
        font-size: $font-size-sm;
        color: $text-color-secondary;
        padding: $spacing-xs;

        &:active {
          color: $primary-color;
        }
      }
    }
  }

  .history-list {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  .history-item {
    padding: 8rpx 20rpx;
    background-color: $bg-color-base;
    border-radius: $border-radius-round;
    font-size: $font-size-sm;
    color: $text-color-regular;
    transition: all 0.2s ease;

    &:active {
      transform: scale(0.95);
      background-color: darken($bg-color-base, 5%);
    }
  }
}
</style>
