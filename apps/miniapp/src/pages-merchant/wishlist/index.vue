<template>
  <view class="wishlist-container">
    <!-- 筛选Tab -->
    <view class="filter-tabs">
      <view
        class="tab-item"
        :class="{ active: activeStatus === item.value }"
        v-for="item in statusTabs"
        :key="item.value"
        @tap="changeStatus(item.value)"
      >
        {{ item.label }}
        <view class="tab-count" v-if="item.count > 0">{{ item.count }}</view>
      </view>
    </view>

    <!-- 列表 -->
    <scroll-view
      class="wishlist-scroll"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view
        class="wish-card"
        v-for="wish in wishlist"
        :key="wish.id"
        @tap="toDetail(wish)"
      >
        <!-- 用户信息 -->
        <view class="user-header">
          <image class="user-avatar" :src="wish.user.avatar" mode="aspectFill"></image>
          <view class="user-info">
            <text class="user-name">{{ wish.user.name }}</text>
            <text class="submit-time">{{ wish.submitTime }}</text>
          </view>
          <view class="wish-status" :class="'status-' + wish.status">
            {{ wish.statusText }}
          </view>
        </view>

        <!-- 需求内容 -->
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

          <!-- 预算范围 -->
          <view class="budget-info" v-if="wish.budget">
            <uni-icons type="wallet" size="16" color="#52C41A"></uni-icons>
            <text class="budget-text">预算：¥{{ wish.budget }}</text>
          </view>

          <!-- 期望时间 -->
          <view class="time-info" v-if="wish.expectTime">
            <uni-icons type="calendar" size="16" color="#1890FF"></uni-icons>
            <text class="time-text">期望时间：{{ wish.expectTime }}</text>
          </view>
        </view>

        <!-- 互动信息 -->
        <view class="interaction-info" v-if="wish.status !== 'pending'">
          <view class="info-item" v-if="wish.replyCount > 0">
            <text class="info-label">回复数</text>
            <text class="info-value">{{ wish.replyCount }}</text>
          </view>
          <view class="info-item" v-if="wish.likeCount > 0">
            <text class="info-label">点赞数</text>
            <text class="info-value">{{ wish.likeCount }}</text>
          </view>
        </view>

        <!-- 快捷操作 -->
        <view class="quick-actions" v-if="wish.status === 'pending'" @tap.stop>
          <button class="action-btn reject" @tap="rejectWish(wish)">
            暂无法满足
          </button>
          <button class="action-btn accept" @tap="acceptWish(wish)">
            接受需求
          </button>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="wishlist.length > 0">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
        <text v-else @tap="loadMore">上拉加载更多</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="wishlist.length === 0 && !loading">
        <empty text="暂无需求" icon="💭" buttonText="刷新列表" @button-click="onRefresh" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { wishApi } from '@/api/modules/wish.js'
import { formatRelativeTime } from '@/utils/helper'

const merchantId = ref('')

// 状态Tab
const statusTabs = ref([
  { label: '待审核', value: 'pending', count: 0 },
  { label: '已接受', value: 'accepted', count: 0 },
  { label: '已拒绝', value: 'rejected', count: 0 },
  { label: '已完成', value: 'completed', count: 0 }
])

const activeStatus = ref('pending')
const wishlist = ref([])
const loading = ref(false)
const refreshing = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 20

onMounted(() => {
  merchantId.value = uni.getStorageSync('merchantId') || ''
  // WISH-005: 加载商家心愿单列表
  loadWishlist()
})

/**
 * 切换状态
 */
const changeStatus = (status) => {
  activeStatus.value = status
  page.value = 1
  noMore.value = false
  loadWishlist()
}

/**
 * WISH-005: 加载心愿单列表
 */
const loadWishlist = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true
  if (isRefresh) {
    page.value = 1
    noMore.value = false
  }

  try {
    // WISH-005: 调用API获取商家心愿单列表
    const res = await wishApi.getMerchantList({
      merchantId: merchantId.value,
      status: activeStatus.value,
      page: page.value,
      size: pageSize
    })

    if (res.code === 200 && res.data) {
      const wishes = res.data.list || res.data || []

      // 转换数据格式
      const formattedWishes = wishes.map(wish => ({
        id: wish.id,
        user: {
          id: wish.userId,
          name: wish.userName || '匿名用户',
          avatar: wish.userAvatar || 'https://via.placeholder.com/100'
        },
        content: wish.content,
        dishes: wish.dishes || [],
        budget: wish.budget || '',
        expectTime: wish.expectedTime || '',
        submitTime: formatRelativeTime(wish.createdAt),
        status: wish.status || 'pending',
        statusText: getStatusText(wish.status || 'pending'),
        replyCount: wish.replyCount || 0,
        likeCount: wish.likeCount || 0
      }))

      if (isRefresh) {
        wishlist.value = formattedWishes
      } else {
        wishlist.value = [...wishlist.value, ...formattedWishes]
      }

      // 更新Tab计数
      if (res.data.counts) {
        statusTabs.value.forEach(tab => {
          tab.count = res.data.counts[tab.value] || 0
        })
      }

      if (wishes.length < pageSize) {
        noMore.value = true
      }
    }

    loading.value = false
    refreshing.value = false
  } catch (error) {
    console.error('加载心愿单失败:', error)

    // API失败时使用模拟数据
    // TODO: 生产环境应移除此模拟数据逻辑
    const mockData = generateMockWishlist()
    if (isRefresh) {
      wishlist.value = mockData
    } else {
      wishlist.value = [...wishlist.value, ...mockData]
    }
    if (mockData.length < pageSize) {
      noMore.value = true
    }

    loading.value = false
    refreshing.value = false
  }
}

/**
 * 生成模拟数据
 */
const generateMockWishlist = () => {
  const wishes = []
  const count = Math.floor(Math.random() * 5) + 3

  const users = [
    { name: '张同学', avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=张' },
    { name: '李同学', avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=李' },
    { name: '王同学', avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=王' }
  ]

  const contents = [
    '想吃家乡的味道，有没有正宗的川菜推荐？最好是那种麻辣鲜香的。',
    '希望能有低脂低卡的菜品，正在减肥中，但想吃点好吃的。',
    '想吃点开胃的菜品，最近没什么食欲，需要酸辣口味的刺激一下。',
    '有没有适合聚餐的菜品？需要分量大一点，价格实惠的。',
    '想吃点养生的汤品，最好是清热降火的。'
  ]

  for (let i = 0; i < count; i++) {
    const user = users[Math.floor(Math.random() * users.length)]
    wishes.push({
      id: page.value * 20 + i,
      user: user,
      content: contents[Math.floor(Math.random() * contents.length)],
      dishes: i % 2 === 0 ? ['宫保鸡丁', '水煮鱼'] : [],
      budget: i % 3 === 0 ? '50-80' : '',
      expectTime: i % 2 === 0 ? '本周五午餐' : '',
      submitTime: '2小时前',
      status: activeStatus.value,
      statusText: getStatusText(activeStatus.value),
      replyCount: Math.floor(Math.random() * 5),
      likeCount: Math.floor(Math.random() * 20)
    })
  }

  return wishes
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const statusMap = {
    pending: '待审核',
    accepted: '已接受',
    rejected: '已拒绝',
    completed: '已完成'
  }
  return statusMap[status] || status
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    page.value++
    loadWishlist()
  }
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadWishlist(true)
}

/**
 * 跳转到详情
 */
const toDetail = (wish) => {
  uni.navigateTo({
    url: `/pages-merchant/wishlist/audit?id=${wish.id}`
  })
}

/**
 * WISH-006: 商家接受需求
 */
const acceptWish = (wish) => {
  uni.showModal({
    title: '接受需求',
    content: '接受后将通知用户，并可以添加菜品。确认接受吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '处理中...' })

          // WISH-006: 调用API接受需求
          const apiRes = await wishApi.accept(wish.id, {
            merchantId: merchantId.value,
            quote: '', // 可选：报价
            estimatedTime: '', // 可选：预计时间
            remark: '商家已接受您的需求'
          })

          uni.hideLoading()

          if (apiRes.code === 200) {
            uni.showToast({
              title: '已接受',
              icon: 'success'
            })

            // 刷新列表
            setTimeout(() => {
              loadWishlist(true)
            }, 1500)
          } else {
            throw new Error(apiRes.message || '接受失败')
          }
        } catch (error) {
          console.error('接受需求失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: error.message || '接受失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * WISH-007: 商家拒绝需求
 */
const rejectWish = (wish) => {
  uni.showModal({
    title: '无法满足',
    content: '请输入拒绝原因（可选）',
    editable: true,
    placeholderText: '如：暂时缺少食材',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '处理中...' })

          // WISH-007: 调用API拒绝需求
          const apiRes = await wishApi.reject(wish.id, {
            merchantId: merchantId.value,
            reason: res.content || '商家暂时无法满足此需求'
          })

          uni.hideLoading()

          if (apiRes.code === 200) {
            uni.showToast({
              title: '已拒绝',
              icon: 'success'
            })

            // 刷新列表
            setTimeout(() => {
              loadWishlist(true)
            }, 1500)
          } else {
            throw new Error(apiRes.message || '拒绝失败')
          }
        } catch (error) {
          console.error('拒绝需求失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: error.message || '拒绝失败',
            icon: 'none'
          })
        }
      }
    }
  })
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

/* 筛选Tab */
.filter-tabs {
  background: #fff;
  padding: 20rpx;
  display: flex;
  gap: 15rpx;
  border-bottom: 1rpx solid #eee;
}

.tab-item {
  flex: 1;
  height: 70rpx;
  border-radius: 35rpx;
  background: #F5F5F5;
  @include flex-center;
  font-size: 28rpx;
  color: #666;
  position: relative;

  &.active {
    background: #FF6B35;
    color: #fff;
    font-weight: bold;
  }
}

.tab-count {
  position: absolute;
  top: -5rpx;
  right: -5rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background: #F5222D;
  color: #fff;
  font-size: 20rpx;
  border-radius: 16rpx;
  @include flex-center;
}

/* 列表 */
.wishlist-scroll {
  flex: 1;
  padding: 20rpx;
}

.wish-card {
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
  margin-bottom: 20rpx;
  padding-bottom: 15rpx;
  border-bottom: 1rpx solid #eee;
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
  gap: 6rpx;
}

.user-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.submit-time {
  font-size: 24rpx;
  color: #999;
}

.wish-status {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: bold;

  &.status-pending {
    background: #FFF7E6;
    color: #FAAD14;
  }

  &.status-accepted {
    background: #E6F7FF;
    color: #1890FF;
  }

  &.status-rejected {
    background: #FFF1F0;
    color: #F5222D;
  }

  &.status-completed {
    background: #F6FFED;
    color: #52C41A;
  }
}

/* 需求内容 */
.wish-content {
  margin-bottom: 15rpx;
}

.wish-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  margin-bottom: 15rpx;
}

.expected-dishes {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;
}

.dish-label {
  font-size: 26rpx;
  color: #999;
  flex-shrink: 0;
}

.dish-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  flex: 1;
}

.dish-tag {
  padding: 6rpx 16rpx;
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
  font-size: 24rpx;
  border-radius: 20rpx;
}

.budget-info,
.time-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 10rpx;
}

.budget-text,
.time-text {
  font-size: 26rpx;
  color: #666;
}

/* 互动信息 */
.interaction-info {
  display: flex;
  gap: 30rpx;
  padding: 15rpx 0;
  border-top: 1rpx solid #eee;
  margin-bottom: 15rpx;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.info-label {
  font-size: 24rpx;
  color: #999;
}

.info-value {
  font-size: 26rpx;
  color: #333;
  font-weight: bold;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  gap: 15rpx;
}

.action-btn {
  flex: 1;
  height: 70rpx;
  border-radius: 35rpx;
  font-size: 28rpx;
  border: none;

  &.accept {
    background: #FF6B35;
    color: #fff;
  }

  &.reject {
    background: #F5F5F5;
    color: #666;
  }
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
