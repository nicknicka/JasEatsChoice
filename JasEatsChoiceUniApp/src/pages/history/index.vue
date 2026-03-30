<template>
  <view class="history-container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-title">浏览记录</view>
      <view class="nav-action" @click="showClearConfirm">
        <text class="action-text">清空</text>
      </view>
    </view>

    <!-- 浏览记录列表 -->
    <scroll-view
      class="history-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 空状态 -->
      <view class="empty-container" v-if="groupedHistory.length === 0 && !loading">
        <text class="empty-icon">👁️</text>
        <text class="empty-text">暂无浏览记录</text>
        <text class="empty-desc">浏览过的商家、菜品都会记录在这里</text>
        <button class="explore-btn" @click="goToExplore">去逛逛</button>
      </view>

      <!-- 浏览记录列表 -->
      <view class="history-list" v-else>
        <view
          class="history-group"
          v-for="group in groupedHistory"
          :key="group.date"
        >
          <!-- 分组标题 -->
          <view class="group-header">
            <text class="group-date">{{ group.dateLabel }}</text>
            <text class="group-count">{{ group.items.length }}条</text>
          </view>

          <!-- 记录列表 -->
          <view
            class="history-item"
            v-for="item in group.items"
            :key="item.id"
            @click="handleItemClick(item)"
          >
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
                  <text class="meta-tag merchant-tag">商家</text>
                  <text class="meta-item" v-if="item.rating">
                    <text class="star">⭐</text> {{ item.rating }}
                  </text>
                </template>

                <!-- 菜品类型 -->
                <template v-else-if="item.type === 'dish'">
                  <text class="meta-tag dish-tag">菜品</text>
                  <text class="meta-item" v-if="item.merchantName">{{ item.merchantName }}</text>
                  <text class="meta-item price" v-if="item.price">¥{{ item.price }}</text>
                </template>

                <!-- 文章类型 -->
                <template v-else-if="item.type === 'article'">
                  <text class="meta-tag article-tag">文章</text>
                  <text class="meta-item" v-if="item.readCount">{{ item.readCount }}阅读</text>
                </template>
              </view>

              <!-- 浏览时间 -->
              <text class="item-time">{{ formatTime(item.viewTime) }}</text>
            </view>

            <!-- 删除按钮 -->
            <view class="item-delete" @click.stop="deleteItem(item)">
              <text class="delete-icon">🗑️</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="groupedHistory.length > 0 && hasMore">
        <uni-load-more
          status="loading"
          content-text="{
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
import { historyApi } from '@/api'

// 数据状态
const historyList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)

// 组件挂载
onMounted(() => {
  loadHistory()
})

/**
 * 按日期分组的浏览记录
 */
const groupedHistory = computed(() => {
  const groups = {}

  historyList.value.forEach(item => {
    const date = getDateKey(item.viewTime)

    if (!groups[date]) {
      groups[date] = {
        date: date,
        dateLabel: getDateLabel(item.viewTime),
        items: []
      }
    }

    groups[date].items.push(item)
  })

  // 转换为数组并按日期排序
  return Object.values(groups).sort((a, b) => {
    return new Date(b.date) - new Date(a.date)
  })
})

/**
 * 获取日期键
 */
const getDateKey = (time) => {
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

/**
 * 获取日期标签
 */
const getDateLabel = (time) => {
  const now = new Date()
  const date = new Date(time)
  const diff = now - date

  // 今天
  if (diff < 1000 * 60 * 60 * 24 && date.getDate() === now.getDate()) {
    return '今天'
  }

  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.getDate() === yesterday.getDate() && date.getMonth() === yesterday.getMonth()) {
    return '昨天'
  }

  // 本周
  const weekAgo = new Date(now)
  weekAgo.setDate(weekAgo.getDate() - 7)
  if (date > weekAgo) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return weekdays[date.getDay()]
  }

  // 更早
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

/**
 * 加载浏览记录
 */
const loadHistory = async () => {
  if (loading.value) return

  loading.value = true

  try {
    // TODO: 调用真实API
    // const res = await historyApi.getList()

    // 模拟数据
    const now = Date.now()

    const mockData = [
      // 今天的记录
      {
        id: 1,
        type: 'merchant',
        name: '老王家常菜',
        image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=老王家常菜',
        rating: 4.8,
        viewTime: new Date(now - 1000 * 60 * 30).toISOString()
      },
      {
        id: 2,
        type: 'dish',
        name: '宫保鸡丁',
        image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=宫保鸡丁',
        merchantName: '老王家常菜',
        price: 38,
        viewTime: new Date(now - 1000 * 60 * 60 * 2).toISOString()
      },
      {
        id: 3,
        type: 'dish',
        name: '麻婆豆腐',
        image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=麻婆豆腐',
        merchantName: '川味轩',
        price: 28,
        viewTime: new Date(now - 1000 * 60 * 60 * 5).toISOString()
      },
      // 昨天的记录
      {
        id: 4,
        type: 'merchant',
        name: '粤式早茶',
        image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=粤式早茶',
        rating: 4.6,
        viewTime: new Date(now - 1000 * 60 * 60 * 24 - 1000 * 60 * 60 * 3).toISOString()
      },
      {
        id: 5,
        type: 'article',
        name: '春季养生饮食指南',
        image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=养生指南',
        readCount: 1234,
        viewTime: new Date(now - 1000 * 60 * 60 * 24 - 1000 * 60 * 60 * 8).toISOString()
      },
      // 更早的记录
      {
        id: 6,
        type: 'dish',
        name: '糖醋排骨',
        image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=糖醋排骨',
        merchantName: '老王家常菜',
        price: 48,
        viewTime: new Date(now - 1000 * 60 * 60 * 24 * 3 - 1000 * 60 * 60 * 2).toISOString()
      },
      {
        id: 7,
        type: 'merchant',
        name: '川味轩',
        image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=川味轩',
        rating: 4.7,
        viewTime: new Date(now - 1000 * 60 * 60 * 24 * 5 - 1000 * 60 * 60 * 4).toISOString()
      }
    ]

    historyList.value = mockData
    hasMore.value = false
  } catch (error) {
    console.error('加载浏览记录失败:', error)
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
  await loadHistory()
  refreshing.value = false
}

/**
 * 处理项目点击
 */
const handleItemClick = (item) => {
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
 * 删除单条记录
 */
const deleteItem = (item) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这条浏览记录吗？',
    success: (res) => {
      if (res.confirm) {
        const index = historyList.value.findIndex(h => h.id === item.id)
        if (index > -1) {
          historyList.value.splice(index, 1)
        }

        uni.showToast({
          title: '删除成功',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 显示清空确认
 */
const showClearConfirm = () => {
  if (historyList.value.length === 0) {
    uni.showToast({
      title: '暂无浏览记录',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '提示',
    content: '确定要清空所有浏览记录吗？',
    success: (res) => {
      if (res.confirm) {
        clearAllHistory()
      }
    }
  })
}

/**
 * 清空所有浏览记录
 */
const clearAllHistory = () => {
  historyList.value = []

  uni.showToast({
    title: '已清空',
    icon: 'success'
  })
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
  } else {
    const date = new Date(time)
    return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
  }
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

.history-container {
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

.action-text {
  font-size: $font-size-base;
  color: $danger-color;
}

/* 浏览记录列表 */
.history-scroll {
  flex: 1;
  margin-top: 108rpx;
  padding: $spacing-md;
  padding-bottom: env(safe-area-inset-bottom);
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

/* 浏览记录列表 */
.history-list {
  .history-group {
    margin-bottom: $spacing-lg;
  }
}

/* 分组标题 */
.group-header {
  @include flex-between;
  padding: $spacing-sm $spacing-md;
  margin-bottom: $spacing-sm;
  background-color: rgba($primary-color, 0.1);
  border-radius: $border-radius-base;
}

.group-date {
  font-size: $font-size-base;
  color: $primary-color;
  font-weight: $font-weight-bold;
}

.group-count {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 记录项 */
.history-item {
  display: flex;
  align-items: center;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;
  box-shadow: $box-shadow-sm;
  position: relative;
}

.item-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: $border-radius-base;
  flex-shrink: 0;
  margin-right: $spacing-md;
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
  white-space: nowrap;
}

.item-meta {
  @include flex-center;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.meta-tag {
  font-size: $font-size-xs;
  padding: 4rpx 8rpx;
  border-radius: 4rpx;
  font-weight: $font-weight-bold;

  &.merchant-tag {
    background-color: rgba($primary-color, 0.1);
    color: $primary-color;
  }

  &.dish-tag {
    background-color: rgba($success-color, 0.1);
    color: $success-color;
  }

  &.article-tag {
    background-color: rgba($warning-color, 0.1);
    color: $warning-color;
  }
}

.meta-item {
  font-size: $font-size-xs;
  color: $text-color-secondary;

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

.item-delete {
  flex-shrink: 0;
  width: 64rpx;
  height: 64rpx;
  @include flex-center;
  margin-left: $spacing-sm;
}

.delete-icon {
  font-size: 32rpx;
}
</style>
