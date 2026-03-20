<template>
  <view class="wish-list-container">
    <!-- 顶部搜索和筛选 -->
    <view class="header-actions">
      <view class="search-bar">
        <uni-icons type="search" size="18" color="#999"></uni-icons>
        <input
          class="search-input"
          v-model="searchKeyword"
          placeholder="搜索心愿"
          @input="onSearchInput"
        />
      </view>

      <!-- 分类筛选 -->
      <scroll-view scroll-x class="category-tabs">
        <view
          class="tab-item"
          :class="{ active: activeCategory === item.value }"
          v-for="item in categories"
          :key="item.value"
          @tap="changeCategory(item.value)"
        >
          {{ item.label }}
        </view>
      </scroll-view>
    </view>

    <!-- 心愿列表 - WISH-001 -->
    <scroll-view
      class="wish-list"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view
        class="wish-card"
        v-for="wish in wishList"
        :key="wish.id"
        @tap="goToDetail(wish.id)"
      >
        <!-- 状态标签 -->
        <view class="status-badge" :class="'status-' + wish.status">
          {{ getStatusText(wish.status) }}
        </view>

        <!-- 用户信息 -->
        <view class="user-info">
          <image class="user-avatar" :src="wish.user.avatar" mode="aspectFill"></image>
          <view class="user-details">
            <text class="user-name">{{ wish.user.name }}</text>
            <text class="submit-time">{{ wish.submitTime }}</text>
          </view>
        </view>

        <!-- 心愿内容 -->
        <view class="wish-content">
          <text class="wish-text">{{ wish.content }}</text>

          <!-- 期望菜品 -->
          <view class="expected-dishes" v-if="wish.dishes.length > 0">
            <text class="dish-label">期望菜品：</text>
            <view class="dish-tags">
              <text class="dish-tag" v-for="dish in wish.dishes" :key="dish">
                {{ dish }}
              </text>
            </view>
          </view>

          <!-- 图片 -->
          <view class="wish-images" v-if="wish.images.length > 0">
            <image
              class="wish-image"
              v-for="(img, index) in wish.images.slice(0, 3)"
              :key="index"
              :src="img"
              mode="aspectFill"
            />
          </view>

          <!-- 其他信息 -->
          <view class="wish-meta">
            <view class="meta-item" v-if="wish.budget">
              <text class="label">预算：</text>
              <text class="value">¥{{ wish.budget }}</text>
            </view>
            <view class="meta-item" v-if="wish.expectedTime">
              <text class="label">期望时间：</text>
              <text class="value">{{ wish.expectedTime }}</text>
            </view>
          </view>
        </view>

        <!-- 互动信息 -->
        <view class="wish-actions" @tap.stop>
          <view class="action-item" @tap="toggleLike(wish)">
            <uni-icons
              :type="wish.liked ? 'heart-filled' : 'heart'"
              :color="wish.liked ? '#FF6B35' : '#999'"
              size="20"
            />
            <text class="count">{{ wish.likeCount || 0 }}</text>
          </view>
          <view class="action-item" v-if="wish.status === 'pending'">
            <button class="action-btn" size="mini" @tap="editWish(wish)">编辑</button>
          </view>
          <view class="action-item" v-if="wish.status === 'pending'">
            <button class="action-btn danger" size="mini" @tap="deleteWish(wish)">删除</button>
          </view>
        </view>

        <!-- 商家响应 -->
        <view class="merchant-responses" v-if="wish.responses.length > 0">
          <text class="response-title">{{ wish.responses.length }}位商家响应</text>
          <view class="response-avatars">
            <image
              class="response-avatar"
              v-for="response in wish.responses.slice(0, 5)"
              :key="response.merchantId"
              :src="response.avatar"
              mode="aspectFill"
            />
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="wishList.length === 0 && !loading">
        <empty text="暂无心愿" icon="💝" buttonText="发布心愿" @button-click="goToCreate" />
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="loading">
        <uni-load-more :status="hasMore ? 'loading' : 'noMore'" />
      </view>
    </scroll-view>

    <!-- 创建按钮 -->
    <view class="create-btn" @tap="goToCreate">
      <uni-icons type="plus" size="20" color="#fff"></uni-icons>
      <text>发布心愿</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { wishApi } from '@/api/modules/wish.js'

const userId = ref('')

// 搜索
const searchKeyword = ref('')

// 分类
const activeCategory = ref('all')
const categories = ref([
  { label: '全部', value: 'all' },
  { label: '中餐', value: 'chinese' },
  { label: '西餐', value: 'western' },
  { label: '日韩料理', value: 'asian' },
  { label: '小吃快餐', value: 'snack' },
  { label: '甜品饮品', value: 'dessert' }
])

// 心愿列表
const wishList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

onMounted(() => {
  userId.value = uni.getStorageSync('userId') || ''

  // WISH-001: 加载心愿列表
  loadWishList()
})

/**
 * WISH-001: 加载心愿列表
 */
const loadWishList = async (isRefresh = false) => {
  if (loading.value) return

  try {
    loading.value = true

    if (isRefresh) {
      pageNum.value = 1
      hasMore.value = true
    }

    const params = {
      userId: userId.value,
      page: pageNum.value,
      size: pageSize.value
    }

    if (activeCategory.value !== 'all') {
      params.category = activeCategory.value
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    // WISH-001: 调用API获取心愿列表
    const res = await wishApi.getUserList(params)

    if (res.code === 200 && res.data) {
      const wishes = res.data.list || res.data || []

      // 转换数据格式
      const formattedWishes = wishes.map(wish => ({
        id: wish.id,
        content: wish.content,
        status: wish.status || 'pending',
        dishes: wish.dishes || [],
        images: wish.images || [],
        budget: wish.budget || '',
        expectedTime: wish.expectedTime || '',
        likeCount: wish.likeCount || 0,
        liked: wish.liked || false,
        submitTime: formatTime(wish.createdAt),
        user: {
          id: wish.userId,
          name: wish.userName || '匿名用户',
          avatar: wish.userAvatar || 'https://via.placeholder.com/100'
        },
        responses: wish.responses || []
      }))

      if (isRefresh) {
        wishList.value = formattedWishes
      } else {
        wishList.value.push(...formattedWishes)
      }

      hasMore.value = wishes.length >= pageSize.value
      pageNum.value++
    }
  } catch (error) {
    console.error('加载心愿列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
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
  if (!loading.value && hasMore.value) {
    loadWishList()
  }
}

/**
 * 切换分类
 */
const changeCategory = (category) => {
  activeCategory.value = category
  loadWishList(true)
}

/**
 * 搜索输入
 */
const onSearchInput = (e) => {
  searchKeyword.value = e.detail.value
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    loadWishList(true)
  }, 500)
}

let searchTimer = null

/**
 * WISH-003: 点赞/取消点赞
 */
const toggleLike = async (wish) => {
  try {
    if (wish.liked) {
      // 取消点赞
      await wishApi.unlike(wish.id, { userId: userId.value })
      wish.liked = false
      wish.likeCount = Math.max(0, (wish.likeCount || 0) - 1)
    } else {
      // 点赞
      await wishApi.like(wish.id, { userId: userId.value })
      wish.liked = true
      wish.likeCount = (wish.likeCount || 0) + 1
    }
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

/**
 * 编辑心愿
 */
const editWish = (wish) => {
  uni.navigateTo({
    url: `/pages-user/wish/add?id=${wish.id}&mode=edit`
  })
}

/**
 * 删除心愿
 */
const deleteWish = (wish) => {
  uni.showModal({
    title: '提示',
    content: '确定删除此心愿吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const apiRes = await wishApi.delete(wish.id, { userId: userId.value })

          if (apiRes.code === 200) {
            uni.showToast({
              title: '删除成功',
              icon: 'success'
            })

            // 刷新列表
            setTimeout(() => {
              loadWishList(true)
            }, 1500)
          }
        } catch (error) {
          uni.showToast({
            title: '删除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const texts = {
    pending: '待响应',
    accepted: '已接受',
    rejected: '已拒绝',
    completed: '已完成'
  }
  return texts[status] || '未知'
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return `${date.getMonth() + 1}-${date.getDate()}`
}

/**
 * 跳转到详情
 */
const goToDetail = (wishId) => {
  uni.navigateTo({
    url: `/pages-user/wish/detail?id=${wishId}`
  })
}

/**
 * 创建心愿
 */
const goToCreate = () => {
  uni.navigateTo({
    url: '/pages-user/wish/add'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.wish-list-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

/* 顶部操作 */
.header-actions {
  background: #fff;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 15rpx;
  padding: 15rpx 20rpx;
  background: #F5F5F5;
  border-radius: 30rpx;
  margin-bottom: 20rpx;
}

.search-input {
  flex: 1;
  font-size: 26rpx;
  color: #333;
}

.category-tabs {
  white-space: nowrap;
}

.tab-item {
  display: inline-block;
  padding: 12rpx 30rpx;
  margin-right: 20rpx;
  background: #F5F5F5;
  border-radius: 30rpx;
  font-size: 26rpx;
  color: #666;

  &.active {
    background: #FF6B35;
    color: #fff;
  }
}

/* 心愿列表 */
.wish-list {
  height: calc(100vh - 260rpx);
  padding: 0 20rpx;
}

.wish-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 25rpx;
  margin-bottom: 20rpx;
  position: relative;
}

.status-badge {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  font-size: 22rpx;

  &.status-pending {
    background: rgba(255, 165, 0, 0.1);
    color: #FFA500;
  }

  &.status-accepted {
    background: rgba(82, 196, 26, 0.1);
    color: #52C41A;
  }

  &.status-rejected {
    background: rgba(140, 140, 140, 0.1);
    color: #8C8C8C;
  }

  &.status-completed {
    background: rgba(24, 144, 255, 0.1);
    color: #1890FF;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 20rpx;
}

.user-avatar {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.user-details {
  flex: 1;
}

.user-name {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 5rpx;
}

.submit-time {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.wish-content {
  margin-bottom: 20rpx;
}

.wish-text {
  display: block;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  margin-bottom: 15rpx;
}

.expected-dishes {
  display: flex;
  align-items: center;
  margin-bottom: 15rpx;
  flex-wrap: wrap;
}

.dish-label {
  font-size: 26rpx;
  color: #666;
  margin-right: 10rpx;
}

.dish-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.dish-tag {
  padding: 4rpx 12rpx;
  background: #F5F5F5;
  border-radius: 4rpx;
  font-size: 24rpx;
  color: #FF6B35;
}

.wish-images {
  display: flex;
  gap: 10rpx;
  margin-bottom: 15rpx;
}

.wish-image {
  width: 150rpx;
  height: 150rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.wish-meta {
  display: flex;
  gap: 30rpx;
}

.meta-item {
  font-size: 24rpx;

  .label {
    color: #999;
  }

  .value {
    color: #333;
  }
}

.wish-actions {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding-top: 15rpx;
  border-top: 1rpx solid #eee;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.action-item .count {
  font-size: 24rpx;
  color: #666;
}

.action-btn {
  padding: 0 20rpx;
  height: 50rpx;
  line-height: 50rpx;
  border-radius: 25rpx;
  font-size: 24rpx;
  background: #F5F5F5;
  color: #666;
  border: none;

  &.danger {
    background: rgba(245, 34, 45, 0.1);
    color: #F5222D;
  }
}

.merchant-responses {
  margin-top: 15rpx;
  padding-top: 15rpx;
  border-top: 1rpx solid #eee;
}

.response-title {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.response-avatars {
  display: flex;
  gap: 10rpx;
}

.response-avatar {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  border: 2rpx solid #fff;
}

/* 创建按钮 */
.create-btn {
  position: fixed;
  bottom: 30rpx;
  right: 30rpx;
  padding: 20rpx 40rpx;
  background: #FF6B35;
  color: #fff;
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.4);
  font-size: 28rpx;
}
</style>
