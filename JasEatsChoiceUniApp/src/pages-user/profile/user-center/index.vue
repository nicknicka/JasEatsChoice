<template>
  <view class="user-center-container">
    <scroll-view class="scroll-container" scroll-y>
      <!-- 用户信息卡片 -->
      <view class="user-info-card">
        <view class="user-avatar">
          <image class="avatar-image" :src="userInfo.avatar" mode="aspectFill" />
          <view class="avatar-badge" v-if="userInfo.vipLevel">
            <text class="badge-text">VIP</text>
          </view>
        </view>

        <view class="user-detail">
          <view class="user-name-row">
            <text class="user-name">{{ userInfo.name }}</text>
            <view class="gender-icon">{{ userInfo.gender === 'male' ? '👨' : '👩' }}</view>
          </view>
          <view class="user-id">ID: {{ userInfo.id }}</view>
          <view class="user-tags">
            <text class="tag-item" v-for="tag in userInfo.tags" :key="tag">{{ tag }}</text>
          </view>
        </view>

        <view class="edit-btn" @click="editProfile">
          <text>编辑</text>
        </view>
      </view>

      <!-- 数据统计 -->
      <view class="stats-card">
        <view class="stat-item" @click="navigateTo('orders')">
          <text class="stat-value">{{ stats.orders }}</text>
          <text class="stat-label">订单</text>
        </view>
        <view class="stat-item" @click="navigateTo('favorites')">
          <text class="stat-value">{{ stats.favorites }}</text>
          <text class="stat-label">收藏</text>
        </view>
        <view class="stat-item" @click="navigateTo('history')">
          <text class="stat-value">{{ stats.history }}</text>
          <text class="stat-label">浏览</text>
        </view>
        <view class="stat-item" @click="navigateTo('coupons')">
          <text class="stat-value">{{ stats.coupons }}</text>
          <text class="stat-label">优惠券</text>
        </view>
      </view>

      <!-- 订单管理 -->
      <view class="section-card">
        <view class="section-header" @click="navigateTo('orders')">
          <view class="section-title">我的订单</view>
          <view class="section-more">
            <text>查看全部</text>
            <text class="more-arrow">→</text>
          </view>
        </view>

        <view class="order-status-grid">
          <view class="status-item" @click="navigateTo('orders', 'pending')">
            <view class="status-icon-wrapper">
              <text class="status-icon">⏰</text>
              <view class="status-badge" v-if="orderCounts.pending > 0">
                {{ orderCounts.pending }}
              </view>
            </view>
            <text class="status-text">待支付</text>
          </view>

          <view class="status-item" @click="navigateTo('orders', 'processing')">
            <view class="status-icon-wrapper">
              <text class="status-icon">👨🍳</text>
              <view class="status-badge" v-if="orderCounts.processing > 0">
                {{ orderCounts.processing }}
              </view>
            </view>
            <text class="status-text">处理中</text>
          </view>

          <view class="status-item" @click="navigateTo('orders', 'delivering')">
            <view class="status-icon-wrapper">
              <text class="status-icon">🚴</text>
              <view class="status-badge" v-if="orderCounts.delivering > 0">
                {{ orderCounts.delivering }}
              </view>
            </view>
            <text class="status-text">配送中</text>
          </view>

          <view class="status-item" @click="navigateTo('orders', 'completed')">
            <view class="status-icon-wrapper">
              <text class="status-icon">⭐</text>
            </view>
            <text class="status-text">待评价</text>
          </view>
        </view>
      </view>

      <!-- 我的资产 -->
      <view class="section-card">
        <view class="section-header" @click="navigateTo('wallet')">
          <view class="section-title">我的资产</view>
          <view class="section-more">
            <text>查看全部</text>
            <text class="more-arrow">→</text>
          </view>
        </view>

        <view class="assets-list">
          <view class="asset-item">
            <text class="asset-value">¥{{ wallet.balance }}</text>
            <text class="asset-label">余额</text>
          </view>
          <view class="asset-item">
            <text class="asset-value">{{ wallet.points }}</text>
            <text class="asset-label">积分</text>
          </view>
          <view class="asset-item">
            <text class="asset-value">{{ wallet.redEnvelopes }}</text>
            <text class="asset-label">红包</text>
          </view>
        </view>
      </view>

      <!-- 功能列表 -->
      <view class="section-card">
        <view class="section-title">常用功能</view>

        <view class="menu-list">
          <view class="menu-item" @click="navigateTo('address')">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">📍</text>
            </view>
            <text class="menu-label">收货地址</text>
            <text class="menu-arrow">→</text>
          </view>

          <view class="menu-item" @click="navigateTo('calorie')">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">📊</text>
            </view>
            <text class="menu-label">卡路里统计</text>
            <text class="menu-arrow">→</text>
          </view>

          <view class="menu-item" @click="navigateTo('recipe')">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">📖</text>
            </view>
            <text class="menu-label">我的食谱</text>
            <text class="menu-arrow">→</text>
          </view>

          <view class="menu-item" @click="navigateTo('health-report')">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">📋</text>
            </view>
            <text class="menu-label">健康报告</text>
            <text class="menu-arrow">→</text>
          </view>
        </view>
      </view>

      <!-- 服务与帮助 -->
      <view class="section-card">
        <view class="section-title">服务与帮助</view>

        <view class="menu-list">
          <view class="menu-item" @click="navigateTo('message')">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">💬</text>
              <view class="menu-badge" v-if="unreadCount > 0">
                {{ unreadCount > 99 ? '99+' : unreadCount }}
              </view>
            </view>
            <text class="menu-label">消息中心</text>
            <text class="menu-arrow">→</text>
          </view>

          <view class="menu-item" @click="navigateTo('customer-service')">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">🎧</text>
            </view>
            <text class="menu-label">联系客服</text>
            <text class="menu-arrow">→</text>
          </view>

          <view class="menu-item" @click="navigateTo('feedback')">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">✉️</text>
            </view>
            <text class="menu-label">意见反馈</text>
            <text class="menu-arrow">→</text>
          </view>

          <view class="menu-item" @click="navigateTo('about')">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">ℹ️</text>
            </view>
            <text class="menu-label">关于我们</text>
            <text class="menu-arrow">→</text>
          </view>

          <view class="menu-item" @click="navigateTo('settings')">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">⚙️</text>
            </view>
            <text class="menu-label">设置</text>
            <text class="menu-arrow">→</text>
          </view>
        </view>
      </view>

      <!-- 底部空白 -->
      <view class="bottom-spacer"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { userApi, chatApi, orderApi } from '@/api'

// Pinia store
const userStore = useUserStore()

// 用户信息
const userInfo = ref({
  id: '',
  name: '佳食宜选用户',
  avatar: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=用户',
  gender: 'female',
  tags: [],
  vipLevel: 0
})

// 统计数据
const stats = ref({
  orders: 28,
  favorites: 56,
  history: 128,
  coupons: 5
})

// 订单数量
const orderCounts = ref({
  pending: 2,
  processing: 1,
  delivering: 3,
  completed: 0
})

// 钱包信息
const wallet = ref({
  balance: '128.50',
  points: 2580,
  redEnvelopes: 3
})

// 未读消息数
const unreadCount = ref(5)

/**
 * 编辑个人资料
 */
const editProfile = () => {
  uni.navigateTo({
    url: '/pages/user-center/edit'
  })
}

/**
 * 页面导航
 */
const navigateTo = (page, params = {}) => {
  const pageMap = {
    'orders': '/pages/orders/index',
    'favorites': '/pages/favorites/index',
    'history': '/pages/history/index',
    'coupons': '/pages/coupons/index',
    'wallet': '/pages/wallet/index',
    'address': '/pages/address/index',
    'calorie': '/pages/calorie/index',
    'recipe': '/pages/recipe/my',
    'health-report': '/pages/health-report/index',
    'message': '/pages/message/index',
    'customer-service': '/pages/customer-service/index',
    'feedback': '/pages/feedback/index',
    'about': '/pages/about/index',
    'settings': '/pages/settings/index'
  }

  const path = pageMap[page]

  if (!path) {
    uni.showToast({
      title: '页面开发中...',
      icon: 'none'
    })
    return
  }

  let url = path
  if (Object.keys(params).length > 0) {
    const query = Object.keys(params)
      .map(key => `${key}=${params[key]}`)
      .join('&')
    url = `${path}?${query}`
  }

  uni.navigateTo({
    url: url,
    fail: () => {
      // 如果页面不存在，提示开发中
      uni.showToast({
        title: '页面开发中...',
        icon: 'none'
      })
    }
  })
}

/**
 * 加载用户信息
 */
const loadUserInfo = async () => {
  try {
    // 检查登录状态
    if (!userStore.isLogin) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages/login/index'
        })
      }, 1500)
      return
    }

    // 从store获取用户信息
    if (userStore.userInfo) {
      userInfo.value = {
        id: userStore.userInfo.userId || userStore.userInfo.id || '',
        name: userStore.userInfo.nickname || userStore.userInfo.name || '佳食宜选用户',
        avatar: userStore.userInfo.avatar || 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=用户',
        gender: userStore.userInfo.gender || 'female',
        tags: userStore.userInfo.tags || [],
        vipLevel: userStore.userInfo.vipLevel || userStore.userInfo.memberLevel || 0
      }
    }

    // 调用后端API获取最新用户信息
    const res = await userApi.getUserInfo(userStore.userInfo.userId || userStore.userInfo.id)
    if (res) {
      userInfo.value = {
        id: res.userId || res.id || '',
        name: res.nickname || res.name || '佳食宜选用户',
        avatar: res.avatar || 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=用户',
        gender: res.gender || 'female',
        tags: res.tags || [],
        vipLevel: res.vipLevel || res.memberLevel || 0
      }

      // 更新store中的用户信息
      userStore.setUserInfo(res)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    // 如果API调用失败，尝试从本地存储获取
    const localUserInfo = uni.getStorageSync('userInfo')
    if (localUserInfo) {
      userInfo.value = {
        id: localUserInfo.userId || localUserInfo.id || '',
        name: localUserInfo.nickname || localUserInfo.name || '佳食宜选用户',
        avatar: localUserInfo.avatar || 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=用户',
        gender: localUserInfo.gender || 'female',
        tags: localUserInfo.tags || [],
        vipLevel: localUserInfo.vipLevel || localUserInfo.memberLevel || 0
      }
    }
  }
}

/**
 * 加载统计数据
 */
const loadStats = async () => {
  try {
    // 调用后端API获取用户统计数据
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    if (!userId) {
      console.warn('用户ID不存在，跳过加载统计数据')
      return
    }

    const res = await userApi.getUserStats(userId)
    if (res && res.data) {
      const data = res.data
      stats.value = {
        orders: data.dietRecords?.totalOrders || data.dietRecords?.completed || 0,
        favorites: data.favorites?.totalFavorites || 0,
        history: 0, // 后端暂未提供浏览历史统计
        coupons: 0  // 后端暂未提供优惠券统计
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 使用默认值
    stats.value = {
      orders: 0,
      favorites: 0,
      history: 0,
      coupons: 0
    }
  }
}

/**
 * 加载订单各状态数量
 * 从后端API获取真实的订单统计数据
 */
const loadOrderCounts = async () => {
  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    if (!userId) {
      console.warn('用户ID不存在，无法加载订单数量')
      return
    }

    const res = await orderApi.getCount({ userId })

    if (res.success && res.data) {
      orderCounts.value = {
        pending: res.data.pending || 0,
        processing: res.data.processing || 0,
        delivering: res.data.delivering || 0,
        completed: res.data.completed || 0
      }
    } else {
      console.warn('获取订单数量失败，使用默认值')
      orderCounts.value = {
        pending: 0,
        processing: 0,
        delivering: 0,
        completed: 0
      }
    }
  } catch (error) {
    console.error('加载订单数量失败:', error)
    // 使用默认值
    orderCounts.value = {
      pending: 0,
      processing: 0,
      delivering: 0,
      completed: 0
    }
  }
}

/**
 * 加载未读消息数
 */
const loadUnreadCount = async () => {
  try {
    // 调用后端API获取未读消息数
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    if (!userId) {
      console.warn('用户ID不存在，跳过加载未读消息数')
      return
    }

    const res = await chatApi.getUnreadCount(userId)
    if (res !== undefined && res !== null) {
      unreadCount.value = res.count || res.total || res.data || res || 0
    }
  } catch (error) {
    console.error('加载未读消息数失败:', error)
    // 使用默认值
    unreadCount.value = 0
  }
}

// 组件挂载
onMounted(() => {
  loadUserInfo()
  loadStats()
  loadOrderCounts()
  loadUnreadCount()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.user-center-container {
  min-height: 100vh;
  background-color: $bg-color-base;
}

.scroll-container {
  height: 100vh;
}

/* 用户信息卡片 */
.user-info-card {
  background: linear-gradient(135deg, #FF6B35, #FF8F61);
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  box-shadow: $box-shadow-md;
}

.user-avatar {
  position: relative;
  width: 120rpx;
  height: 120rpx;
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
}

.avatar-badge {
  position: absolute;
  bottom: -4rpx;
  right: -4rpx;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  padding: 4rpx 8rpx;
  border-radius: $border-radius-round;
  border: 2rpx solid #fff;

  .badge-text {
    font-size: $font-size-xs;
    color: #fff;
    font-weight: $font-weight-bold;
  }
}

.user-detail {
  flex: 1;
}

.user-name-row {
  @include flex-center;
  gap: $spacing-sm;
  margin-bottom: $spacing-xs;
}

.user-name {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: #fff;
}

.gender-icon {
  font-size: $font-size-lg;
}

.user-id {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: $spacing-xs;
}

.user-tags {
  @include flex-center;
  gap: $spacing-xs;
  flex-wrap: wrap;
}

.tag-item {
  padding: 4rpx 12rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: $border-radius-round;
  font-size: $font-size-xs;
  color: #fff;
}

.edit-btn {
  padding: $spacing-sm $spacing-md;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: $border-radius-round;
  color: #fff;
  font-size: $font-size-sm;
}

/* 数据统计卡片 */
.stats-card {
  background-color: $bg-color-white;
  margin: 0 $spacing-md $spacing-md;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  @include flex-around;
  box-shadow: $box-shadow-sm;
}

.stat-item {
  @include flex-center-column;
  gap: $spacing-xs;
  cursor: pointer;

  &:active {
    opacity: 0.6;
  }
}

.stat-value {
  font-size: 40rpx;
  font-weight: $font-weight-bold;
  color: $primary-color;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 通用区块卡片 */
.section-card {
  background-color: $bg-color-white;
  margin: 0 $spacing-md $spacing-md;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  box-shadow: $box-shadow-sm;
}

.section-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-md;
  cursor: pointer;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.section-more {
  @include flex-center;
  gap: $spacing-xs;
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.more-arrow {
  font-size: $font-size-base;
}

/* 订单状态 */
.order-status-grid {
  @include flex-around;
}

.status-item {
  @include flex-center-column;
  gap: $spacing-sm;
  padding: $spacing-md;
  cursor: pointer;

  &:active {
    opacity: 0.6;
  }
}

.status-icon-wrapper {
  position: relative;
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
  background-color: $bg-color-base;
  border-radius: $border-radius-lg;
}

.status-icon {
  font-size: 48rpx;
}

.status-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  @include flex-center;
  padding: 0 8rpx;
  background-color: $danger-color;
  border-radius: $border-radius-round;
  border: 2rpx solid $bg-color-white;
  font-size: $font-size-xs;
  color: #fff;
  font-weight: $font-weight-bold;
}

.status-text {
  font-size: $font-size-sm;
  color: $text-color-regular;
}

/* 资产列表 */
.assets-list {
  @include flex-around;
}

.asset-item {
  @include flex-center-column;
  gap: $spacing-xs;
  padding: $spacing-md;
  flex: 1;
  cursor: pointer;

  &:active {
    opacity: 0.6;
  }

  &:not(:last-child) {
    border-right: 1rpx solid $border-color-lighter;
  }
}

.asset-value {
  font-size: 36rpx;
  font-weight: $font-weight-bold;
  color: $primary-color;
}

.asset-label {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 菜单列表 */
.menu-list {
  .menu-item {
    @include flex-center;
    padding: $spacing-md 0;
    border-bottom: 1rpx solid $border-color-lighter;
    cursor: pointer;

    &:last-child {
      border-bottom: none;
    }

    &:active {
      background-color: $bg-color-base;
    }
  }
}

.menu-icon-wrapper {
  position: relative;
  width: 64rpx;
  height: 64rpx;
  @include flex-center;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  margin-right: $spacing-md;
}

.menu-icon {
  font-size: 36rpx;
}

.menu-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  @include flex-center;
  padding: 0 8rpx;
  background-color: $danger-color;
  border-radius: $border-radius-round;
  font-size: $font-size-xs;
  color: #fff;
  font-weight: $font-weight-bold;
}

.menu-label {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
}

.menu-arrow {
  font-size: $font-size-base;
  color: $text-color-secondary;
}

/* 底部空白 */
.bottom-spacer {
  height: 40rpx;
}
</style>
