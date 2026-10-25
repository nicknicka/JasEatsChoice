<template>
  <view class="favorites-container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-title">我的收藏</view>
      <view class="nav-action" @click="refreshCollections">
        <text class="action-icon">🔄</text>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-card">
      <text class="stats-text">共 {{ filteredCollections.length }} 个收藏</text>
    </view>

    <!-- 筛选工具栏 -->
    <view class="filter-bar">
      <!-- 搜索框 -->
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="searchKeyword"
          placeholder="搜索收藏名称..."
          @input="handleSearch"
        />
        <text class="clear-icon" v-if="searchKeyword" @click="searchKeyword = ''; handleSearch()">×</text>
      </view>

      <!-- 类型筛选 -->
      <scroll-view class="type-tabs" scroll-x>
        <view
          class="type-tab"
          v-for="type in typeTabs"
          :key="type.value"
          :class="{ active: filterType === type.value }"
          @click="switchType(type.value)"
        >
          <text class="tab-icon">{{ type.icon }}</text>
          <text class="tab-text">{{ type.label }}</text>
        </view>
      </scroll-view>

      <!-- 排序选择 -->
      <view class="sort-bar">
        <view class="sort-item" @click="toggleSort">
          <text class="sort-text">{{ getSortText() }}</text>
          <text class="sort-icon">{{ sortOrder === 'desc' ? '↓' : '↑' }}</text>
        </view>

        <view class="sort-item" @click="toggleBatchMode">
          <text class="sort-text">{{ batchMode ? '完成' : '管理' }}</text>
        </view>
      </view>
    </view>

    <!-- 批量操作栏 -->
    <view class="batch-bar" v-if="batchMode">
      <view class="batch-select-all" @click="toggleSelectAll">
        <text class="checkbox">{{ selectedAll ? '☑️' : '☐' }}</text>
        <text class="select-all-text">全选</text>
      </view>
      <view class="batch-actions">
        <text class="selected-count">已选 {{ selectedCollections.length }} 项</text>
        <button class="batch-delete-btn" @click="batchDelete">删除</button>
      </view>
    </view>

    <!-- 收藏列表 -->
    <scroll-view
      class="collections-scroll"
      scroll-y
      :refresher-enabled="!batchMode"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <!-- 空状态 -->
      <view class="empty-container" v-if="filteredCollections.length === 0 && !loading">
        <text class="empty-icon">{{ getEmptyIcon() }}</text>
        <text class="empty-text">{{ getEmptyText() }}</text>
        <text class="empty-desc">{{ getEmptyDesc() }}</text>
        <button class="explore-btn" @click="goToExplore">去逛逛</button>
      </view>

      <!-- 收藏列表 -->
      <view class="collections-list" v-else>
        <view
          class="collection-item"
          v-for="item in filteredCollections"
          :key="item.id"
          @click="handleItemClick(item)"
        >
          <!-- 批选模式复选框 -->
          <view class="item-checkbox" v-if="batchMode" @click.stop="toggleSelect(item.id)">
            <text class="checkbox-icon">{{ selectedCollections.includes(item.id) ? '☑️' : '☐' }}</text>
          </view>

          <!-- 收藏内容 -->
          <view class="item-content">
            <!-- 类型图标 -->
            <view class="item-type-badge" :class="'type-' + item.type">
              <text class="type-icon">{{ getTypeIcon(item.type) }}</text>
            </view>

            <!-- 封面图 -->
            <image
              class="item-image"
              :src="item.image || '/static/placeholder.png'"
              mode="aspectFill"
            />

            <!-- 信息区域 -->
            <view class="item-info">
              <text class="item-name">{{ item.name }}</text>

              <view class="item-meta">
                <!-- 商家类型 -->
                <template v-if="item.type === 'merchant'">
                  <text class="meta-item" v-if="item.rating">
                    <text class="star">⭐</text> {{ item.rating }}
                  </text>
                  <text class="meta-item" v-if="item.distance">{{ item.distance }}</text>
                  <text class="meta-item" v-if="item.deliveryTime">{{ item.deliveryTime }}</text>
                </template>

                <!-- 菜品类型 -->
                <template v-else-if="item.type === 'dish'">
                  <text class="meta-item" v-if="item.merchantName">{{ item.merchantName }}</text>
                  <text class="meta-item price" v-if="item.price">¥{{ item.price }}</text>
                  <text class="meta-item" v-if="item.calories">{{ item.calories }}kcal</text>
                </template>

                <!-- 文章类型 -->
                <template v-else-if="item.type === 'article'">
                  <text class="meta-item" v-if="item.author">{{ item.author }}</text>
                  <text class="meta-item" v-if="item.readCount">{{ item.readCount }}阅读</text>
                  <text class="meta-item" v-if="item.likeCount">{{ item.likeCount }}赞</text>
                </template>
              </view>

              <!-- 收藏时间 -->
              <text class="item-time">{{ formatTime(item.collectTime) }}</text>
            </view>

            <!-- 快速操作 -->
            <view class="item-actions" v-if="!batchMode" @click.stop>
              <view class="action-btn" @click="toggleCollect(item)">
                <text class="action-icon">❤️</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="filteredCollections.length > 0">
        <uni-load-more
          :status="loadMoreStatus"
          :content-text="{
            contentdown: '上拉加载更多',
            contentrefresh: '加载中...',
            contentnomore: '没有更多了'
          }"
        ></uni-load-more>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { favoriteApi } from '@/api'

// 数据状态
const collections = ref([])
const loading = ref(false)
const refreshing = ref(false)
const loadMoreStatus = ref('more')

// 筛选和排序
const searchKeyword = ref('')
const filterType = ref('all')
const sortBy = ref('date')
const sortOrder = ref('desc')

// 批量操作
const batchMode = ref(false)
const selectedCollections = ref([])

// 类型标签
const typeTabs = [
  { label: '全部', value: 'all', icon: '📦' },
  { label: '商家', value: 'merchant', icon: '🏪' },
  { label: '菜品', value: 'dish', icon: '🍲' },
  { label: '文章', value: 'article', icon: '📝' }
]

// 分页
const page = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

// 计算属性：过滤后的收藏列表
const filteredCollections = computed(() => {
  let result = [...collections.value]

  // 类型筛选
  if (filterType.value !== 'all') {
    result = result.filter(item => item.type === filterType.value)
  }

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(item =>
      item.name.toLowerCase().includes(keyword) ||
      (item.merchantName && item.merchantName.toLowerCase().includes(keyword))
    )
  }

  // 排序
  result.sort((a, b) => {
    let compareValue = 0

    switch (sortBy.value) {
      case 'date':
        compareValue = new Date(a.collectTime) - new Date(b.collectTime)
        break
      case 'type':
        compareValue = a.type.localeCompare(b.type)
        break
      case 'name':
        compareValue = a.name.localeCompare(b.name, 'zh-CN')
        break
    }

    return sortOrder.value === 'asc' ? compareValue : -compareValue
  })

  return result
})

// 是否全选
const selectedAll = computed(() => {
  return filteredCollections.value.length > 0 &&
    selectedCollections.value.length === filteredCollections.value.length
})

// 组件挂载
onMounted(() => {
  loadCollections()
})

/**
 * 加载收藏列表
 */
const loadCollections = async () => {
  if (loading.value) return

  loading.value = true

  try {
    // TODO: 调用真实API
    // const res = await favoriteApi.getList({
    //   page: page.value,
    //   size: pageSize.value
    // })

    // 模拟数据
    const mockData = [
      {
        id: 1,
        type: 'merchant',
        name: '老王家常菜',
        image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=老王家常菜',
        rating: 4.8,
        distance: '1.2km',
        deliveryTime: '30分钟',
        collectTime: new Date(Date.now() - 1000 * 60 * 30).toISOString()
      },
      {
        id: 2,
        type: 'dish',
        name: '宫保鸡丁',
        image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=宫保鸡丁',
        merchantName: '老王家常菜',
        price: 38,
        calories: 320,
        collectTime: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString()
      },
      {
        id: 3,
        type: 'dish',
        name: '麻婆豆腐',
        image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=麻婆豆腐',
        merchantName: '川味轩',
        price: 28,
        calories: 280,
        collectTime: new Date(Date.now() - 1000 * 60 * 60 * 5).toISOString()
      },
      {
        id: 4,
        type: 'article',
        name: '春季养生饮食指南',
        image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=养生指南',
        author: '营养师小王',
        readCount: 1234,
        likeCount: 89,
        collectTime: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString()
      },
      {
        id: 5,
        type: 'merchant',
        name: '粤式早茶',
        image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=粤式早茶',
        rating: 4.6,
        distance: '2.5km',
        deliveryTime: '45分钟',
        collectTime: new Date(Date.now() - 1000 * 60 * 60 * 24 * 2).toISOString()
      }
    ]

    if (page.value === 1) {
      collections.value = mockData
    } else {
      collections.value = [...collections.value, ...mockData]
    }

    hasMore.value = collections.value.length < 50 // 假设总共50条
    if (!hasMore.value) {
      loadMoreStatus.value = 'noMore'
    }
  } catch (error) {
    console.error('加载收藏失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 下拉刷新
 */
const onRefresh = async () => {
  refreshing.value = true
  page.value = 1
  hasMore.value = true
  loadMoreStatus.value = 'more'

  await loadCollections()
  refreshing.value = false
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (hasMore.value && !loading.value) {
    page.value++
    loadMoreStatus.value = 'loading'
    loadCollections()
  }
}

/**
 * 刷新收藏列表
 */
const refreshCollections = () => {
  onRefresh()
}

/**
 * 切换类型筛选
 */
const switchType = (type) => {
  filterType.value = type
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  // 搜索通过 computed 自动处理
}

/**
 * 切换排序
 */
const toggleSort = () => {
  const sortOptions = ['date', 'type', 'name']
  const currentIndex = sortOptions.indexOf(sortBy.value)
  const nextIndex = (currentIndex + 1) % sortOptions.length
  sortBy.value = sortOptions[nextIndex]
}

/**
 * 获取排序文本
 */
const getSortText = () => {
  const sortMap = {
    'date': '按时间',
    'type': '按类型',
    'name': '按名称'
  }
  return sortMap[sortBy.value] || '排序'
}

/**
 * 切换批量操作模式
 */
const toggleBatchMode = () => {
  batchMode.value = !batchMode.value
  if (!batchMode.value) {
    selectedCollections.value = []
  }
}

/**
 * 切换全选
 */
const toggleSelectAll = () => {
  if (selectedAll.value) {
    selectedCollections.value = []
  } else {
    selectedCollections.value = filteredCollections.value.map(item => item.id)
  }
}

/**
 * 切换单项选择
 */
const toggleSelect = (id) => {
  const index = selectedCollections.value.indexOf(id)
  if (index > -1) {
    selectedCollections.value.splice(index, 1)
  } else {
    selectedCollections.value.push(id)
  }
}

/**
 * 批量删除
 */
const batchDelete = () => {
  if (selectedCollections.value.length === 0) {
    uni.showToast({
      title: '请先选择要删除的项',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '提示',
    content: `确定要删除选中的 ${selectedCollections.value.length} 个收藏吗？`,
    success: (res) => {
      if (res.confirm) {
        // 从列表中移除
        collections.value = collections.value.filter(
          item => !selectedCollections.value.includes(item.id)
        )

        selectedCollections.value = []
        batchMode.value = false

        uni.showToast({
          title: '删除成功',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 处理项目点击
 */
const handleItemClick = (item) => {
  if (batchMode.value) {
    toggleSelect(item.id)
    return
  }

  // 根据类型跳转到不同页面
  switch (item.type) {
    case 'merchant':
      uni.navigateTo({
        url: `/pages/merchant/detail?id=${item.id}`
      })
      break
    case 'dish':
      uni.navigateTo({
        url: `/pages/dish/detail?id=${item.id}`
      })
      break
    case 'article':
      uni.navigateTo({
        url: `/pages/article/detail?id=${item.id}`
      })
      break
  }
}

/**
 * 取消收藏
 */
const toggleCollect = (item) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消收藏吗？',
    success: (res) => {
      if (res.confirm) {
        const index = collections.value.findIndex(c => c.id === item.id)
        if (index > -1) {
          collections.value.splice(index, 1)
        }

        uni.showToast({
          title: '已取消收藏',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 获取类型图标
 */
const getTypeIcon = (type) => {
  const iconMap = {
    'merchant': '🏪',
    'dish': '🍲',
    'article': '📝'
  }
  return iconMap[type] || '📦'
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''

  const now = Date.now()
  const itemTime = new Date(time).getTime()
  const diff = now - itemTime

  if (diff < 1000 * 60 * 60) {
    return `${Math.floor(diff / (1000 * 60))}分钟前`
  } else if (diff < 1000 * 60 * 60 * 24) {
    return `${Math.floor(diff / (1000 * 60 * 60))}小时前`
  } else if (diff < 1000 * 60 * 60 * 24 * 7) {
    return `${Math.floor(diff / (1000 * 60 * 60 * 24))}天前`
  } else {
    const date = new Date(time)
    return `${date.getMonth() + 1}-${date.getDate()}`
  }
}

/**
 * 获取空状态图标
 */
const getEmptyIcon = () => {
  if (searchKeyword.value) return '🔍'
  if (filterType.value !== 'all') return getTypeIcon(filterType.value)
  return '💝'
}

/**
 * 获取空状态文本
 */
const getEmptyText = () => {
  if (searchKeyword.value) return '没有找到相关收藏'
  if (filterType.value === 'merchant') return '暂无收藏的商家'
  if (filterType.value === 'dish') return '暂无收藏的菜品'
  if (filterType.value === 'article') return '暂无收藏的文章'
  return '暂无收藏'
}

/**
 * 获取空状态描述
 */
const getEmptyDesc = () => {
  if (searchKeyword.value) return '试试搜索其他关键词'
  return '收藏喜欢的商家、菜品和文章吧~'
}

/**
 * 去逛逛
 */
const goToExplore = () => {
  uni.switchTab({
    url: '/pages/home/index'
  })
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.favorites-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  display: flex;
  flex-direction: column;
}

/* 导航栏 */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  background-color: $bg-color-white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-md;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.nav-back {
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
}

.back-icon {
  font-size: 48rpx;
  color: $text-color-primary;
  font-weight: bold;
}

.nav-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.nav-action {
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
}

.action-icon {
  font-size: 40rpx;
}

/* 统计卡片 */
.stats-card {
  position: fixed;
  top: 88rpx;
  left: 0;
  right: 0;
  background-color: $primary-color;
  padding: $spacing-md $spacing-lg;
  z-index: 99;
}

.stats-text {
  font-size: $font-size-base;
  color: #fff;
  font-weight: $font-weight-bold;
}

/* 筛选工具栏 */
.filter-bar {
  position: fixed;
  top: 144rpx;
  left: 0;
  right: 0;
  background-color: $bg-color-white;
  z-index: 98;
  padding-bottom: $spacing-sm;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.search-box {
  display: flex;
  align-items: center;
  margin: $spacing-md $spacing-md $spacing-sm;
  padding: $spacing-sm $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
}

.search-icon {
  font-size: 32rpx;
  margin-right: $spacing-sm;
}

.search-input {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
}

.clear-icon {
  font-size: 36rpx;
  color: $text-color-secondary;
  padding: 0 $spacing-xs;
}

.type-tabs {
  white-space: nowrap;
  padding: 0 $spacing-sm;
}

.type-tab {
  display: inline-block;
  padding: $spacing-sm $spacing-md;
  margin: 0 $spacing-xs;
  border-radius: $border-radius-round;
  background-color: $bg-color-base;
  transition: all 0.3s;

  &.active {
    background-color: $primary-color;

    .tab-text {
      color: #fff;
      font-weight: $font-weight-bold;
    }
  }
}

.tab-icon {
  font-size: 28rpx;
  margin-right: 4rpx;
}

.tab-text {
  font-size: $font-size-sm;
  color: $text-color-primary;
}

.sort-bar {
  display: flex;
  justify-content: space-between;
  padding: $spacing-sm $spacing-md;
  border-top: 1rpx solid $border-color-lighter;
}

.sort-item {
  @include flex-center;
  gap: 4rpx;
  padding: $spacing-xs $spacing-md;
  border-radius: $border-radius-base;
  background-color: $bg-color-base;
}

.sort-text {
  font-size: $font-size-sm;
  color: $text-color-primary;
}

.sort-icon {
  font-size: 24rpx;
  color: $text-color-secondary;
}

/* 批量操作栏 */
.batch-bar {
  position: fixed;
  top: 280rpx;
  left: 0;
  right: 0;
  background-color: $warning-color;
  padding: $spacing-md;
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 97;
}

.batch-select-all {
  @include flex-center;
  gap: $spacing-sm;
}

.checkbox {
  font-size: 36rpx;
}

.select-all-text {
  font-size: $font-size-base;
  color: #fff;
  font-weight: $font-weight-bold;
}

.batch-actions {
  @include flex-center;
  gap: $spacing-md;
}

.selected-count {
  font-size: $font-size-sm;
  color: #fff;
}

.batch-delete-btn {
  padding: $spacing-xs $spacing-md;
  background-color: #fff;
  color: $danger-color;
  border-radius: $border-radius-round;
  font-size: $font-size-sm;
  border: none;
}

/* 收藏列表 */
.collections-scroll {
  flex: 1;
  margin-top: 300rpx;
  padding: $spacing-md;
  padding-bottom: env(safe-area-inset-bottom);
}

.batch-mode .collections-scroll {
  margin-top: 350rpx;
}

/* 空状态 */
.empty-container {
  @include flex-center-column;
  padding: 200rpx $spacing-xl;
  text-align: center;
}

.empty-icon {
  font-size: 160rpx;
  margin-bottom: $spacing-lg;
}

.empty-text {
  font-size: $font-size-xl;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-sm;
}

.empty-desc {
  font-size: $font-size-base;
  color: $text-color-secondary;
  margin-bottom: $spacing-xl;
}

.explore-btn {
  padding: $spacing-md $spacing-xl;
  background-color: $primary-color;
  color: #fff;
  border-radius: $border-radius-round;
  font-size: $font-size-base;
  border: none;
}

/* 收藏列表 */
.collections-list {
  .collection-item {
    background-color: $bg-color-white;
    border-radius: $border-radius-lg;
    padding: $spacing-md;
    margin-bottom: $spacing-md;
    box-shadow: $box-shadow-sm;
    position: relative;
  }
}

.item-checkbox {
  position: absolute;
  top: $spacing-md;
  left: $spacing-md;
  z-index: 10;
}

.checkbox-icon {
  font-size: 40rpx;
}

.item-content {
  display: flex;
  align-items: flex-start;
  gap: $spacing-md;
  padding-left: batchMode.value ? '60rpx' : '0';
}

.item-type-badge {
  position: absolute;
  top: $spacing-md;
  right: $spacing-md;
  width: 48rpx;
  height: 48rpx;
  @include flex-center;
  border-radius: 50%;
  z-index: 5;

  &.type-merchant {
    background-color: rgba($primary-color, 0.1);
  }

  &.type-dish {
    background-color: rgba($success-color, 0.1);
  }

  &.type-article {
    background-color: rgba($warning-color, 0.1);
  }
}

.type-icon {
  font-size: 28rpx;
}

.item-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: $border-radius-base;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.item-name {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-meta {
  @include flex-center;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.meta-item {
  font-size: $font-size-xs;
  color: $text-color-secondary;
  padding: 4rpx 8rpx;
  background-color: $bg-color-base;
  border-radius: 4rpx;

  &.price {
    color: $danger-color;
    font-weight: $font-weight-bold;
  }

  .star {
    color: #FFD700;
  }
}

.item-time {
  font-size: $font-size-xs;
  color: $text-color-placeholder;
}

.item-actions {
  flex-shrink: 0;
}

.action-btn {
  width: 64rpx;
  height: 64rpx;
  @include flex-center;
  background-color: $bg-color-base;
  border-radius: 50%;
}

.action-icon {
  font-size: 32rpx;
}
</style>
