<template>
  <view class="search-container">
    <!-- 搜索头部 -->
    <view class="search-header">
      <view class="search-input-wrapper">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          type="text"
          v-model="searchKeyword"
          placeholder="搜索菜品、商家或食谱..."
          :focus="inputFocused"
          @input="onSearchInput"
          @confirm="onSearch"
        />
        <text class="clear-btn" v-if="searchKeyword" @click="clearSearch">×</text>
      </view>
      <text class="cancel-btn" @click="cancelSearch" v-if="inputFocused || searchKeyword">取消</text>
    </view>

    <scroll-view class="scroll-container" scroll-y>
      <!-- 搜索建议/自动补全 -->
      <view class="suggestions-section card" v-if="searchSuggestions.length > 0 && searchKeyword">
        <view class="suggestion-list">
          <view
            class="suggestion-item"
            v-for="(item, index) in searchSuggestions"
            :key="index"
            @click="selectSuggestion(item)"
          >
            <text class="suggestion-icon">🔍</text>
            <text class="suggestion-text">{{ item }}</text>
          </view>
        </view>
      </view>

      <!-- 搜索状态：未输入时显示历史和热门 -->
      <view v-if="!searchKeyword">
        <!-- 搜索历史 -->
        <view class="history-section card" v-if="searchHistory.length > 0">
          <view class="section-header">
            <text class="section-title">搜索历史</text>
            <text class="clear-btn" @click="clearHistory">清空</text>
          </view>
          <view class="history-list">
            <view
              class="history-item"
              v-for="(item, index) in searchHistory"
              :key="index"
              @click="searchHistoryItem(item)"
            >
              <text class="history-icon">🕐</text>
              <text class="history-text">{{ item }}</text>
              <text class="delete-btn" @click.stop="deleteHistoryItem(index)">×</text>
            </view>
          </view>
        </view>

        <!-- 热门搜索 -->
        <view class="hot-section card">
          <view class="section-title">热门搜索</view>
          <view class="hot-list">
            <view
              class="hot-item"
              v-for="(item, index) in hotSearches"
              :key="index"
              @click="searchHotItem(item)"
            >
              <text class="hot-rank" :class="{ top3: index < 3 }">{{ index + 1 }}</text>
              <text class="hot-text">{{ item }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 搜索结果 -->
      <view v-if="searchKeyword && !loading">
        <!-- 筛选Tab -->
        <view class="filter-tabs">
          <scroll-view class="tabs-scroll" scroll-x show-scrollbar="false">
            <view class="tabs-list">
              <view
                class="tab-item"
                :class="{ active: activeTab === tab.value }"
                v-for="tab in searchTabs"
                :key="tab.value"
                @click="switchTab(tab.value)"
              >
                {{ tab.label }}
              </view>
            </view>
          </scroll-view>
        </view>

        <!-- 搜索结果列表 -->
        <view class="results-section">
          <!-- 商家结果 -->
          <view v-if="activeTab === 'merchant' && merchantResults.length > 0">
            <view class="section-header">
              <text class="section-title">商家</text>
              <text class="result-count">共{{ merchantResults.length }}家</text>
            </view>
            <view class="merchant-list">
              <view
                class="merchant-item card"
                v-for="merchant in merchantResults"
                :key="merchant.id"
                @click="toMerchantDetail(merchant.id)"
              >
                <image class="merchant-logo" :src="merchant.logo" mode="aspectFill" />
                <view class="merchant-info">
                  <view class="merchant-name">{{ merchant.name }}</view>
                  <view class="merchant-rating">
                    <text class="star">⭐</text>
                    <text>{{ merchant.rating }}</text>
                    <text class="sales">月售{{ merchant.monthlySales }}</text>
                  </view>
                  <view class="merchant-tags">
                    <text class="tag" v-for="tag in merchant.tags" :key="tag">{{ tag }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <!-- 菜品结果 -->
          <view v-if="activeTab === 'dish' && dishResults.length > 0">
            <view class="section-header">
              <text class="section-title">菜品</text>
              <text class="result-count">共{{ dishResults.length }}道</text>
            </view>
            <view class="dish-list">
              <view
                class="dish-item card"
                v-for="dish in dishResults"
                :key="dish.id"
                @click="toDishDetail(dish.id)"
              >
                <image class="dish-image" :src="dish.image" mode="aspectFill" />
                <view class="dish-info">
                  <view class="dish-name">{{ dish.name }}</view>
                  <view class="dish-desc">{{ dish.description }}</view>
                  <view class="dish-bottom">
                    <view class="dish-price">
                      <text class="price-symbol">¥</text>
                      <text class="price-value">{{ dish.price }}</text>
                    </view>
                    <view class="dish-sales">已售{{ dish.sales }}</view>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <!-- 食谱结果 -->
          <view v-if="activeTab === 'recipe' && recipeResults.length > 0">
            <view class="section-header">
              <text class="section-title">食谱</text>
              <text class="result-count">共{{ recipeResults.length }}个</text>
            </view>
            <view class="recipe-list">
              <view
                class="recipe-item card"
                v-for="recipe in recipeResults"
                :key="recipe.id"
                @click="toRecipeDetail(recipe.id)"
              >
                <image class="recipe-image" :src="recipe.image" mode="aspectFill" />
                <view class="recipe-info">
                  <view class="recipe-name">{{ recipe.name }}</view>
                  <view class="recipe-meta">
                    <text class="meta-item">🔥 {{ recipe.calories }}卡</text>
                    <text class="meta-item">⏱️ {{ recipe.cookTime }}分钟</text>
                  </view>
                  <view class="recipe-tags">
                    <text class="tag" v-for="tag in recipe.tags" :key="tag">{{ tag }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <!-- 空结果 -->
          <view class="empty-result" v-if="allResultsEmpty">
            <view class="empty-icon">🔍</view>
            <view class="empty-text">没有找到相关内容</view>
            <view class="empty-tips">换个关键词试试吧</view>
          </view>
        </view>
      </view>

      <!-- 加载中 -->
      <view class="loading-state" v-if="loading">
        <uni-load-more status="loading" contentText="搜索中..." />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 状态
const searchKeyword = ref('')
const inputFocused = ref(false)
const loading = ref(false)
const activeTab = ref('all')

// 搜索历史
const searchHistory = ref([
  '宫保鸡丁',
  '老王家常菜',
  '川菜'
])

// 热门搜索
const hotSearches = ref([
  '宫保鸡丁',
  '麻婆豆腐',
  '鱼香肉丝',
  '回锅肉',
  '水煮鱼',
  '酸菜鱼',
  '红烧肉',
  '糖醋排骨'
])

// 搜索建议
const searchSuggestions = ref([])

// 搜索Tab
const searchTabs = ref([
  { label: '全部', value: 'all' },
  { label: '商家', value: 'merchant' },
  { label: '菜品', value: 'dish' },
  { label: '食谱', value: 'recipe' }
])

// 搜索结果
const merchantResults = ref([])
const dishResults = ref([])
const recipeResults = ref([])

// 计算属性
const allResultsEmpty = computed(() => {
  return merchantResults.value.length === 0 &&
         dishResults.value.length === 0 &&
         recipeResults.value.length === 0
})

/**
 * 搜索输入
 */
const onSearchInput = (e) => {
  const keyword = e.detail.value.trim()

  if (keyword.length > 0) {
    // 模拟搜索建议
    searchSuggestions.value = [
      keyword,
      keyword + ' 做法',
      keyword + ' 做法',
      keyword + ' 营养价值'
    ].slice(0, 5)
  } else {
    searchSuggestions.value = []
  }
}

/**
 * 执行搜索
 */
const onSearch = async () => {
  const keyword = searchKeyword.value.trim()

  if (!keyword) {
    return
  }

  // 添加到搜索历史
  if (!searchHistory.value.includes(keyword)) {
    searchHistory.value.unshift(keyword)
    if (searchHistory.value.length > 10) {
      searchHistory.value.pop()
    }
  }

  loading.value = true

  try {
    // TODO: 调用后端搜索API
    // const res = await searchApi.search({
    //   keyword,
    //   tab: activeTab.value
    // })
    //
    // merchantResults.value = res.data.merchants || []
    // dishResults.value = res.data.dishes || []
    // recipeResults.value = res.data.recipes || []

    // 模拟搜索结果
    await new Promise(resolve => setTimeout(resolve, 800))

    // 模拟商家结果
    merchantResults.value = [
      {
        id: 1,
        name: '老王家常菜',
        logo: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=老王',
        rating: 4.8,
        monthlySales: 999,
        tags: ['川菜', '配送快']
      }
    ]

    // 模拟菜品结果
    dishResults.value = [
      {
        id: 1,
        name: '宫保鸡丁',
        description: '经典川菜，酸甜可口',
        price: '28',
        sales: 999,
        image: 'https://via.placeholder.com/300x300/FF6B35/FFFFFF?text=宫保鸡丁'
      },
      {
        id: 4,
        name: '麻婆豆腐',
        description: '麻辣鲜香，嫩滑爽口',
        price: '18',
        sales: 666,
        image: 'https://via.placeholder.com/300x300/faad14/FFFFFF?text=麻婆豆腐'
      }
    ]

    // 模拟食谱结果
    recipeResults.value = []

    // 清空建议
    searchSuggestions.value = []
  } catch (error) {
    console.error('搜索失败:', error)
    uni.showToast({
      title: '搜索失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 选择搜索建议
 */
const selectSuggestion = (suggestion) => {
  searchKeyword.value = suggestion
  searchSuggestions.value = []
  onSearch()
}

/**
 * 切换Tab
 */
const switchTab = (tab) => {
  activeTab.value = tab
}

/**
 * 清除搜索
 */
const clearSearch = () => {
  searchKeyword.value = ''
  searchSuggestions.value = []
  merchantResults.value = []
  dishResults.value = []
  recipeResults.value = []
}

/**
 * 取消搜索
 */
const cancelSearch = () => {
  inputFocused.value = false
  clearSearch()
}

/**
 * 清空历史
 */
const clearHistory = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清空搜索历史吗？',
    success: (res) => {
      if (res.confirm) {
        searchHistory.value = []
      }
    }
  })
}

/**
 * 删除历史记录项
 */
const deleteHistoryItem = (index) => {
  searchHistory.value.splice(index, 1)
}

/**
 * 搜索历史项
 */
const searchHistoryItem = (keyword) => {
  searchKeyword.value = keyword
  onSearch()
}

/**
 * 搜索热门项
 */
const searchHotItem = (keyword) => {
  searchKeyword.value = keyword
  onSearch()
}

/**
 * 跳转到商家详情
 */
const toMerchantDetail = (merchantId) => {
  uni.navigateTo({
    url: `/pages/merchant/detail/index?id=${merchantId}`
  })
}

/**
 * 跳转到菜品详情
 */
const toDishDetail = (dishId) => {
  uni.navigateTo({
    url: `/pages/dish/detail/index?id=${dishId}`
  })
}

/**
 * 跳转到食谱详情
 */
const toRecipeDetail = (recipeId) => {
  uni.showToast({
    title: '食谱详情页开发中',
    icon: 'none'
  })
  // TODO: 跳转到食谱详情页
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.search-container {
  min-height: 100vh;
  background-color: $bg-color-base;
}

/* 搜索头部 */
.search-header {
  @include flex-center;
  gap: $spacing-md;
  padding: $spacing-md;
  background-color: $bg-color-white;
}

.search-input-wrapper {
  flex: 1;
  @include flex-center;
  gap: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
  padding: $spacing-sm $spacing-md;
}

.search-icon {
  font-size: $font-size-lg;
  color: $text-color-secondary;
}

.search-input {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
}

.clear-btn {
  font-size: $font-size-xl;
  color: $text-color-secondary;
  padding: 0 $spacing-xs;
}

.cancel-btn {
  font-size: $font-size-base;
  color: $primary-color;
  padding: $spacing-sm;
}

.scroll-container {
  height: calc(100vh - 100rpx);
}

.card {
  background-color: $bg-color-white;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
}

/* 搜索建议 */
.suggestions-section {
  .suggestion-list {
    .suggestion-item {
      @include flex-center;
      gap: $spacing-sm;
      padding: $spacing-sm 0;
      border-bottom: 1rpx solid $border-color-light;

      &:last-child {
        border-bottom: none;
      }
    }
  }

  .suggestion-icon {
    font-size: $font-size-base;
    color: $text-color-secondary;
  }

  .suggestion-text {
    flex: 1;
    font-size: $font-size-base;
    color: $text-color-regular;
  }
}

/* 搜索历史 */
.history-section {
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

  .clear-btn {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }

  .history-list {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  .history-item {
    @include flex-center;
    gap: $spacing-xs;
    padding: $spacing-sm $spacing-md;
    background-color: $bg-color-base;
    border-radius: $border-radius-base;
  }

  .history-icon {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }

  .history-text {
    font-size: $font-size-sm;
    color: $text-color-regular;
    max-width: 400rpx;
    @include text-ellipsis;
  }

  .delete-btn {
    font-size: $font-size-xl;
    color: $text-color-placeholder;
    margin-left: $spacing-xs;
  }
}

/* 热门搜索 */
.hot-section {
  .section-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $text-color-primary;
    margin-bottom: $spacing-md;
  }

  .hot-list {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  .hot-item {
    @include flex-center;
    gap: $spacing-xs;
  }

  .hot-rank {
    width: 40rpx;
    height: 40rpx;
    @include flex-center;
    font-size: $font-size-sm;
    color: #999;
    background-color: $bg-color-base;
    border-radius: $border-radius-sm;

    &.top3 {
      background-color: #f5a623;
      color: #fff;
      font-weight: $font-weight-bold;
    }
  }

  .hot-text {
    font-size: $font-size-sm;
    color: $text-color-regular;
  }
}

/* 筛选Tab */
.filter-tabs {
  background-color: $bg-color-white;
  position: sticky;
  top: 0;
  z-index: $z-index-normal;
}

.tabs-scroll {
  white-space: nowrap;
}

.tabs-list {
  display: flex;
  padding: $spacing-sm $spacing-md;
}

.tab-item {
  flex-shrink: 0;
  padding: $spacing-sm $spacing-md;
  margin-right: $spacing-sm;
  font-size: $font-size-base;
  color: $text-color-regular;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;

  &.active {
    color: #fff;
    background-color: $primary-color;
    font-weight: $font-weight-medium;
  }
}

/* 搜索结果 */
.results-section {
  padding: 0 $spacing-md $spacing-md;

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

  .result-count {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }

  /* 商家列表 */
  .merchant-list {
    .merchant-item {
      display: flex;
      margin-bottom: $spacing-md;
    }

    .merchant-logo {
      width: 120rpx;
      height: 120rpx;
      border-radius: $border-radius-base;
      flex-shrink: 0;
    }

    .merchant-info {
      flex: 1;
      margin-left: $spacing-sm;
    }

    .merchant-name {
      font-size: $font-size-base;
      font-weight: $font-weight-medium;
      color: $text-color-primary;
      margin-bottom: $spacing-xs;
    }

    .merchant-rating {
      @include flex-center;
      gap: $spacing-xs;
      font-size: $font-size-sm;
      margin-bottom: $spacing-xs;

      .star {
        color: #f5a623;
      }

      .sales {
        color: $text-color-secondary;
      }
    }

    .merchant-tags {
      display: flex;
      flex-wrap: wrap;
      gap: $spacing-xs;

      .tag {
        font-size: $font-size-xs;
        color: $primary-color;
        background-color: rgba(255, 107, 53, 0.1);
        padding: 4rpx 8rpx;
        border-radius: 4rpx;
      }
    }
  }

  /* 菜品列表 */
  .dish-list {
    .dish-item {
      display: flex;
      margin-bottom: $spacing-md;
    }

    .dish-image {
      width: 160rpx;
      height: 160rpx;
      border-radius: $border-radius-base;
      flex-shrink: 0;
    }

    .dish-info {
      flex: 1;
      margin-left: $spacing-sm;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }

    .dish-name {
      font-size: $font-size-base;
      font-weight: $font-weight-medium;
      color: $text-color-primary;
    }

    .dish-desc {
      font-size: $font-size-sm;
      color: $text-color-secondary;
      margin-top: $spacing-xs;
      @include text-ellipsis;
    }

    .dish-bottom {
      @include flex-between;
      align-items: center;
    }

    .dish-price {
      @include flex-center;
      gap: 2rpx;
      color: $danger-color;
      font-weight: $font-weight-bold;

      .price-symbol {
        font-size: $font-size-sm;
      }

      .price-value {
        font-size: $font-size-lg;
      }
    }

    .dish-sales {
      font-size: $font-size-sm;
      color: $text-color-secondary;
    }
  }

  /* 食谱列表 */
  .recipe-list {
    .recipe-item {
      display: flex;
      margin-bottom: $spacing-md;
    }

    .recipe-image {
      width: 160rpx;
      height: 160rpx;
      border-radius: $border-radius-base;
      flex-shrink: 0;
    }

    .recipe-info {
      flex: 1;
      margin-left: $spacing-sm;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }

    .recipe-name {
      font-size: $font-size-base;
      font-weight: $font-weight-medium;
      color: $text-color-primary;
    }

    .recipe-meta {
      @include flex-center;
      gap: $spacing-md;
      margin-top: $spacing-xs;

      .meta-item {
        font-size: $font-size-xs;
        color: $text-color-secondary;
      }
    }

    .recipe-tags {
      display: flex;
      flex-wrap: wrap;
      gap: $spacing-xs;

      .tag {
        font-size: $font-size-xs;
        color: $primary-color;
        background-color: rgba(255, 107, 53, 0.1);
        padding: 4rpx 8rpx;
        border-radius: 4rpx;
      }
    }
  }

  /* 空结果 */
  .empty-result {
    @include flex-center-column;
    gap: $spacing-md;
    padding: 200rpx 0;
  }

  .empty-icon {
    font-size: 160rpx;
  }

  .empty-text {
    font-size: $font-size-lg;
    color: $text-color-primary;
  }

  .empty-tips {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }
}

/* 加载状态 */
.loading-state {
  padding: 200rpx 0;
  text-align: center;
}
</style>
