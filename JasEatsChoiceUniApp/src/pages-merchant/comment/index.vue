<template>
  <view class="comment-center-container">
    <!-- 评分概览 -->
    <view class="rating-overview">
      <view class="rating-main">
        <text class="rating-score">{{ ratingStats.avgRating }}</text>
        <uni-rate :value="ratingStats.avgRating" size="18" readonly></uni-rate>
        <text class="rating-count">{{ ratingStats.totalReviews }}条评价</text>
      </view>
      <view class="rating-distribution">
        <view
          class="distribution-item"
          v-for="item in ratingStats.distribution"
          :key="item.star"
        >
          <text class="star-label">{{ item.star }}星</text>
          <view class="progress-bar">
            <view
              class="progress-fill"
              :style="{ width: item.percent + '%' }"
            ></view>
          </view>
          <text class="count">{{ item.count }}</text>
        </view>
      </view>
    </view>

    <!-- 统计数据 -->
    <view class="stats-grid">
      <view class="stat-card">
        <text class="stat-value">{{ ratingStats.totalReviews }}</text>
        <text class="stat-label">总评价数</text>
      </view>
      <view class="stat-card">
        <text class="stat-value positive">{{ ratingStats.positiveRate }}%</text>
        <text class="stat-label">好评率</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ ratingStats.replyCount }}</text>
        <text class="stat-label">已回复</text>
      </view>
      <view class="stat-card">
        <text class="stat-value warning">{{ ratingStats.pendingReply }}</text>
        <text class="stat-label">待回复</text>
      </view>
    </view>

    <!-- 筛选Tab -->
    <view class="filter-tabs">
      <scroll-view scroll-x class="tabs-scroll">
        <view
          class="tab-item"
          :class="{ active: activeFilter === item.value }"
          v-for="item in filterTabs"
          :key="item.value"
          @tap="changeFilter(item.value)"
        >
          {{ item.label }}
          <view class="tab-count" v-if="item.count > 0">{{ item.count }}</view>
        </view>
      </scroll-view>
    </view>

    <!-- 标签筛选 -->
    <view class="tag-filter">
      <scroll-view scroll-x class="tags-scroll">
        <view
          class="tag-item"
          :class="{ active: activeTag === item.value }"
          v-for="item in tagFilters"
          :key="item.value"
          @tap="changeTag(item.value)"
        >
          {{ item.label }}
        </view>
      </scroll-view>
    </view>

    <!-- 评价列表 -->
    <scroll-view
      class="comment-list"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view
        class="comment-card"
        v-for="comment in commentList"
        :key="comment.id"
      >
        <!-- 用户信息 -->
        <view class="user-header">
          <image class="user-avatar" :src="comment.user.avatar" mode="aspectFill"></image>
          <view class="user-info">
            <text class="user-name">{{ comment.user.name }}</text>
            <view class="comment-rating">
              <uni-rate :value="comment.rating" size="12" readonly></uni-rate>
            </view>
          </view>
          <text class="comment-time">{{ comment.time }}</text>
        </view>

        <!-- 评价内容 -->
        <text class="comment-content">{{ comment.content }}</text>

        <!-- 评价图片 -->
        <view class="comment-images" v-if="comment.images.length > 0">
          <image
            class="comment-image"
            v-for="(img, index) in comment.images"
            :key="index"
            :src="img"
            mode="aspectFill"
            @tap="previewImage(comment.images, index)"
          ></image>
        </view>

        <!-- 菜品信息 -->
        <view class="dish-info" v-if="comment.dish">
          <image class="dish-thumb" :src="comment.dish.image" mode="aspectFill"></image>
          <text class="dish-name">{{ comment.dish.name }}</text>
        </view>

        <!-- 标签 -->
        <view class="comment-tags" v-if="comment.tags.length > 0">
          <text
            class="tag"
            v-for="tag in comment.tags"
            :key="tag"
          >
            {{ tag }}
          </text>
        </view>

        <!-- 商家回复 -->
        <view class="merchant-reply" v-if="comment.reply">
          <view class="reply-header">
            <uni-icons type="chatbubble" size="16" color="#FF6B35"></uni-icons>
            <text class="reply-label">商家回复</text>
          </view>
          <text class="reply-content">{{ comment.reply.content }}</text>
          <text class="reply-time">{{ comment.reply.time }}</text>
        </view>

        <!-- 操作按钮 -->
        <view class="action-buttons" v-if="!comment.reply">
          <button class="action-btn" @tap="replyComment(comment)">
            回复评价
          </button>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="commentList.length > 0">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
        <text v-else @tap="loadMore">上拉加载更多</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="commentList.length === 0 && !loading">
        <empty text="暂无评价" icon="💬" buttonText="刷新列表" @button-click="onRefresh" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 评分统计
const ratingStats = ref({
  avgRating: 4.8,
  totalReviews: 156,
  positiveRate: 96,
  replyCount: 142,
  pendingReply: 14,
  distribution: [
    { star: 5, count: 120, percent: 77 },
    { star: 4, count: 25, percent: 16 },
    { star: 3, count: 8, percent: 5 },
    { star: 2, count: 2, percent: 1 },
    { star: 1, count: 1, percent: 1 }
  ]
})

// 筛选Tab
const filterTabs = ref([
  { label: '全部', value: 'all', count: 156 },
  { label: '待回复', value: 'pending', count: 14 },
  { label: '已回复', value: 'replied', count: 142 },
  { label: '有图', value: 'hasImage', count: 89 },
  { label: '差评', value: 'bad', count: 3 }
])

const activeFilter = ref('all')

// 标签筛选
const tagFilters = ref([
  { label: '全部', value: 'all' },
  { label: '口味好', value: 'taste' },
  { label: '分量足', value: 'portion' },
  { label: '配送快', value: 'delivery' },
  { label: '服务好', value: 'service' },
  { label: '性价比高', value: 'value' }
])

const activeTag = ref('all')

const commentList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 20

onMounted(() => {
  loadComments()
})

/**
 * 切换筛选
 */
const changeFilter = (filter) => {
  activeFilter.value = filter
  page.value = 1
  noMore.value = false
  loadComments()
}

/**
 * 切换标签
 */
const changeTag = (tag) => {
  activeTag.value = tag
  page.value = 1
  noMore.value = false
  loadComments()
}

/**
 * 加载评价列表
 */
const loadComments = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true
  if (isRefresh) {
    page.value = 1
    noMore.value = false
  }

  try {
    // TODO: 调用API获取评价列表
    // const res = await merchantApi.getComments({
    //   filter: activeFilter.value,
    //   tag: activeTag.value,
    //   page: page.value,
    //   size: pageSize
    // })

    // 模拟数据
    setTimeout(() => {
      const mockData = generateMockComments()
      if (isRefresh) {
        commentList.value = mockData
      } else {
        commentList.value = [...commentList.value, ...mockData]
      }

      if (mockData.length < pageSize) {
        noMore.value = true
      }

      loading.value = false
      refreshing.value = false
    }, 500)
  } catch (error) {
    console.error('加载评价失败:', error)
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 生成模拟评价数据
 */
const generateMockComments = () => {
  const comments = []
  const count = Math.floor(Math.random() * 5) + 5

  const users = [
    { name: '张同学', avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=张' },
    { name: '李同学', avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=李' },
    { name: '王同学', avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=王' }
  ]

  const contents = [
    '味道非常不错，宫保鸡丁很正宗，分量也足，下次还会再来的！',
    '配送速度快，菜品新鲜，包装也很好，就是有点辣，下次要微辣就好。',
    '性价比很高，菜品味道好，价格实惠，强烈推荐给大家！',
    '服务态度很好，老板很热情，菜品味道也OK，就是等待时间有点长。',
    '非常满意的一次用餐体验，菜品丰富多样，味道正宗，值得推荐！'
  ]

  for (let i = 0; i < count; i++) {
    const user = users[Math.floor(Math.random() * users.length)]
    const hasReply = Math.random() > 0.3

    comments.push({
      id: page.value * 20 + i,
      user: user,
      rating: Math.floor(Math.random() * 2) + 4,
      time: '2天前',
      content: contents[Math.floor(Math.random() * contents.length)],
      images: Math.random() > 0.7 ? ['https://via.placeholder.com/200/FF6B35/FFFFFF?text=1'] : [],
      dish: {
        id: 1,
        name: '宫保鸡丁',
        image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=1'
      },
      tags: ['口味好', '分量足'],
      reply: hasReply ? {
        content: '感谢您的好评！我们会继续努力提供更优质的服务和菜品。',
        time: '1天前'
      } : null
    })
  }

  return comments
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    page.value++
    loadComments()
  }
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadComments(true)
}

/**
 * 预览图片
 */
const previewImage = (images, current) => {
  uni.previewImage({
    urls: images,
    current: current
  })
}

/**
 * 回复评价
 */
const replyComment = (comment) => {
  uni.navigateTo({
    url: `/pages-merchant/comment/reply?id=${comment.id}`
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.comment-center-container {
  min-height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
}

/* 评分概览 */
.rating-overview {
  background: #fff;
  padding: 30rpx;
  display: flex;
  gap: 30rpx;
}

.rating-main {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding-right: 30rpx;
  border-right: 1rpx solid #eee;
}

.rating-score {
  font-size: 72rpx;
  font-weight: bold;
  color: #FF6B35;
}

.rating-count {
  font-size: 24rpx;
  color: #999;
}

.rating-distribution {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.distribution-item {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.star-label {
  width: 60rpx;
  font-size: 24rpx;
  color: #666;
}

.progress-bar {
  flex: 1;
  height: 12rpx;
  background: #F5F5F5;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #FF6B35;
  border-radius: 6rpx;
}

.count {
  width: 50rpx;
  text-align: right;
  font-size: 24rpx;
  color: #999;
}

/* 统计数据 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  padding: 20rpx;
}

.stat-card {
  background: #fff;
  padding: 25rpx;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.stat-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #FF6B35;

  &.positive {
    color: #52C41A;
  }

  &.warning {
    color: #FAAD14;
  }
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

/* 筛选Tab */
.filter-tabs,
.tag-filter {
  background: #fff;
  border-bottom: 1rpx solid #eee;
}

.tabs-scroll,
.tags-scroll {
  white-space: nowrap;
  padding: 20rpx;
}

.tab-item,
.tag-item {
  display: inline-block;
  padding: 10rpx 24rpx;
  margin-right: 20rpx;
  font-size: 26rpx;
  color: #666;
  border-radius: 20rpx;
  background: #F5F5F5;
  position: relative;

  &.active {
    background: #FF6B35;
    color: #fff;
  }

  &:last-child {
    margin-right: 0;
  }
}

.tab-count {
  position: absolute;
  top: -5rpx;
  right: -5rpx;
  min-width: 28rpx;
  height: 28rpx;
  padding: 0 6rpx;
  background: #F5222D;
  color: #fff;
  font-size: 18rpx;
  border-radius: 14rpx;
  @include flex-center;
}

/* 评价列表 */
.comment-list {
  flex: 1;
  padding: 20rpx;
}

.comment-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

/* 用户头部 */
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
  font-weight: bold;
  color: #333;
}

.comment-rating {
  display: flex;
  align-items: center;
}

.comment-time {
  font-size: 24rpx;
  color: #999;
}

/* 评价内容 */
.comment-content {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  margin-bottom: 15rpx;
}

.comment-images {
  display: flex;
  gap: 10rpx;
  margin-bottom: 15rpx;
}

.comment-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 8rpx;
}

/* 菜品信息 */
.dish-info {
  display: flex;
  align-items: center;
  gap: 15rpx;
  padding: 15rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
  margin-bottom: 15rpx;
}

.dish-thumb {
  width: 80rpx;
  height: 80rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.dish-name {
  font-size: 26rpx;
  color: #666;
}

/* 标签 */
.comment-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-bottom: 15rpx;
}

.tag {
  padding: 6rpx 16rpx;
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
  font-size: 24rpx;
  border-radius: 20rpx;
}

/* 商家回复 */
.merchant-reply {
  padding: 15rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
  margin-bottom: 15rpx;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 10rpx;
}

.reply-label {
  font-size: 24rpx;
  font-weight: bold;
  color: #FF6B35;
}

.reply-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 10rpx;
}

.reply-time {
  font-size: 22rpx;
  color: #999;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 15rpx;
}

.action-btn {
  flex: 1;
  height: 70rpx;
  border-radius: 35rpx;
  font-size: 26rpx;
  background: #FF6B35;
  color: #fff;
  border: none;
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
</style>
