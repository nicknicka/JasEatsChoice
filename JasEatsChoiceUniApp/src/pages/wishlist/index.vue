<template>
  <view class="wishlist-container">
    <!-- 顶部统计卡片 -->
    <view class="stats-card">
      <view class="stat-item">
        <text class="stat-value">{{ myWishCount }}</text>
        <text class="stat-label">我的发布</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ completedCount }}</text>
        <text class="stat-label">已完成</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ pendingCount }}</text>
        <text class="stat-label">进行中</text>
      </view>
    </view>

    <!-- 分类标签 -->
    <view class="filter-tabs">
      <view
        class="tab-item"
        :class="{ active: activeTab === item.value }"
        v-for="item in filterTabs"
        :key="item.value"
        @tap="changeTab(item.value)"
      >
        {{ item.label }}
      </view>
    </view>

    <!-- 心愿列表 -->
    <scroll-view
      class="wishlist-list"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view
        class="wish-card"
        v-for="item in wishList"
        :key="item.id"
        @tap="viewWishDetail(item)"
      >
        <!-- 用户信息 -->
        <view class="user-header">
          <image class="user-avatar" :src="item.user.avatar" mode="aspectFill"></image>
          <view class="user-info">
            <text class="user-name">{{ item.user.name }}</text>
            <text class="publish-time">{{ item.time }}</text>
          </view>
          <view class="wish-status" :class="'status-' + item.status">
            {{ item.statusText }}
          </view>
        </view>

        <!-- 心愿内容 -->
        <text class="wish-content">{{ item.content }}</text>

        <!-- 期望菜品标签 -->
        <view class="dish-tags" v-if="item.dishes.length > 0">
          <text
            class="dish-tag"
            v-for="dish in item.dishes.slice(0, 3)"
            :key="dish"
          >
            {{ dish }}
          </text>
          <text class="dish-tag more" v-if="item.dishes.length > 3">
            +{{ item.dishes.length - 3 }}
          </text>
        </view>

        <!-- 其他信息 -->
        <view class="wish-meta">
          <view class="meta-item" v-if="item.budget">
            <uni-icons type="wallet" size="14" color="#999"></uni-icons>
            <text class="meta-text">预算：¥{{ item.budget }}</text>
          </view>
          <view class="meta-item" v-if="item.expectTime">
            <uni-icons type="calendar" size="14" color="#999"></uni-icons>
            <text class="meta-text">{{ item.expectTime }}</text>
          </view>
        </view>

        <!-- 互动数据 -->
        <view class="interaction-bar">
          <view class="interaction-item">
            <uni-icons type="hand-up" size="16" color="#999"></uni-icons>
            <text class="interaction-count">{{ item.likeCount }}</text>
          </view>
          <view class="interaction-item">
            <uni-icons type="chatbubble" size="16" color="#999"></uni-icons>
            <text class="interaction-count">{{ item.replyCount }}</text>
          </view>
          <view class="interaction-item">
            <uni-icons type="eye" size="16" color="#999"></uni-icons>
            <text class="interaction-count">{{ item.viewCount }}</text>
          </view>
        </view>

        <!-- 商家回复数 -->
        <view class="merchant-replies" v-if="item.merchantReplyCount > 0">
          <text class="reply-count">{{ item.merchantReplyCount }}位商家已回复</text>
          <uni-icons type="arrowright" size="14" color="#FF6B35"></uni-icons>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="wishList.length > 0">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="wishList.length === 0 && !loading">
        <empty text="暂无心愿单" icon="🍽️" buttonText="发布心愿" @button-click="publishWish" />
      </view>
    </scroll-view>

    <!-- 发布按钮 -->
    <view class="publish-btn-container">
      <button class="publish-btn" @tap="publishWish">
        <uni-icons type="plus" size="20" color="#fff"></uni-icons>
        <text>发布心愿</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 筛选标签
const filterTabs = ref([
  { label: '全部', value: 'all' },
  { label: '我的', value: 'my' },
  { label: '待响应', value: 'pending' },
  { label: '进行中', value: 'processing' },
  { label: '已完成', value: 'completed' }
])

const activeTab = ref('all')

// 统计数据
const myWishCount = ref(5)
const completedCount = ref(3)
const pendingCount = ref(2)

// 心愿列表
const wishList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 20

onMounted(() => {
  loadWishList()
})

/**
 * 切换标签
 */
const changeTab = (tab) => {
  activeTab.value = tab
  loadWishList(true)
}

/**
 * 加载心愿列表
 */
const loadWishList = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true
  if (isRefresh) {
    page.value = 1
    noMore.value = false
  }

  try {
    // TODO: 调用API获取心愿列表
    // const res = await userApi.getWishList({
    //   type: activeTab.value,
    //   page: page.value,
    //   size: pageSize
    // })

    // 模拟数据
    setTimeout(() => {
      const mockData = generateMockWishes()
      if (isRefresh) {
        wishList.value = mockData
      } else {
        wishList.value = [...wishList.value, ...mockData]
      }

      if (mockData.length < pageSize) {
        noMore.value = true
      }

      loading.value = false
      refreshing.value = false
    }, 500)
  } catch (error) {
    console.error('加载心愿列表失败:', error)
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 生成模拟心愿数据
 */
const generateMockWishes = () => {
  const wishes = []
  const count = Math.floor(Math.random() * 5) + 5

  const contents = [
    '想吃正宗的川菜，有没有推荐的商家？最好是麻辣鲜香的口感。',
    '最近想吃火锅，有没有性价比高的火锅店推荐？',
    '想吃家乡的味道，有没有做湖南菜的商家？',
    '有没有适合聚会的餐厅？需要包厢，大概8-10人。',
    '想吃日料，有没有新鲜的刺身和寿司推荐？'
  ]

  for (let i = 0; i < count; i++) {
    const statusList = ['pending', 'processing', 'completed']
    wishes.push({
      id: page.value * 20 + i,
      user: {
        id: 1,
        name: '张同学',
        avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=张'
      },
      content: contents[Math.floor(Math.random() * contents.length)],
      dishes: ['宫保鸡丁', '水煮鱼', '麻婆豆腐'].slice(0, Math.floor(Math.random() * 3) + 1),
      budget: '50-80',
      expectTime: '本周五',
      time: '2小时前',
      status: statusList[Math.floor(Math.random() * statusList.length)],
      statusText: ['待响应', '进行中', '已完成'][Math.floor(Math.random() * 3)],
      likeCount: Math.floor(Math.random() * 20),
      replyCount: Math.floor(Math.random() * 10),
      viewCount: Math.floor(Math.random() * 100),
      merchantReplyCount: Math.floor(Math.random() * 5)
    })
  }

  return wishes
}

/**
 * 查看心愿详情
 */
const viewWishDetail = (wish) => {
  uni.navigateTo({
    url: `/pages/wishlist/detail?id=${wish.id}`
  })
}

/**
 * 发布心愿
 */
const publishWish = () => {
  uni.navigateTo({
    url: '/pages/wishlist/add'
  })
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadWishList(true)
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    page.value++
    loadWishList()
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.wishlist-container {
  min-height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
}

/* 统计卡片 */
.stats-card {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  padding: 40rpx 30rpx;
  display: flex;
  justify-content: space-around;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.stat-value {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
}

.stat-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.stat-divider {
  width: 1rpx;
  background: rgba(255, 255, 255, 0.3);
}

/* 筛选标签 */
.filter-tabs {
  background: #fff;
  display: flex;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #eee;
}

.tab-item {
  padding: 10rpx 24rpx;
  margin-right: 30rpx;
  font-size: 28rpx;
  color: #666;
  border-radius: 20rpx;

  &.active {
    background: #FF6B35;
    color: #fff;
  }
}

/* 心愿列表 */
.wishlist-list {
  flex: 1;
  padding: 20rpx;
}

.wish-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 25rpx;
  margin-bottom: 20rpx;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 15rpx;
}

.user-avatar {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.user-name {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.publish-time {
  font-size: 24rpx;
  color: #999;
}

.wish-status {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;

  &.status-pending {
    background: #FFF7E6;
    color: #FAAD14;
  }

  &.status-processing {
    background: #E6F7FF;
    color: #1890FF;
  }

  &.status-completed {
    background: #F6FFED;
    color: #52C41A;
  }
}

.wish-content {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  margin-bottom: 15rpx;
  display: block;
}

.dish-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-bottom: 15rpx;
}

.dish-tag {
  padding: 8rpx 16rpx;
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
  font-size: 24rpx;
  border-radius: 20rpx;

  &.more {
    background: #F5F5F5;
    color: #999;
  }
}

.wish-meta {
  display: flex;
  gap: 20rpx;
  margin-bottom: 15rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5rpx;
}

.meta-text {
  font-size: 24rpx;
  color: #999;
}

.interaction-bar {
  display: flex;
  gap: 30rpx;
  padding-top: 15rpx;
  border-top: 1rpx solid #eee;
}

.interaction-item {
  display: flex;
  align-items: center;
  gap: 5rpx;
}

.interaction-count {
  font-size: 24rpx;
  color: #999;
}

.merchant-replies {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 15rpx;
  border-top: 1rpx solid #eee;
}

.reply-count {
  font-size: 26rpx;
  color: #FF6B35;
}

/* 加载状态 */
.load-status {
  text-align: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 26rpx;
}

/* 空状态 */
.empty-state {
  padding-top: 200rpx;
}

/* 发布按钮 */
.publish-btn-container {
  background: #fff;
  padding: 20rpx;
  border-top: 1rpx solid #eee;
}

.publish-btn {
  width: 100%;
  height: 90rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 45rpx;
  border: none;
  @include flex-center;
  gap: 10rpx;
}
</style>
