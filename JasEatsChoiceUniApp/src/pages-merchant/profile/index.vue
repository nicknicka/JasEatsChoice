<template>
  <view class="merchant-profile-container">
    <!-- 商家信息卡片 -->
    <view class="profile-header">
      <image class="shop-logo" :src="merchantInfo.logo" mode="aspectFill"></image>
      <view class="shop-details">
        <text class="shop-name">{{ merchantInfo.name }}</text>
        <view class="shop-rating">
          <uni-rate :value="merchantInfo.rating" size="14" readonly></uni-rate>
          <text class="rating-text">{{ merchantInfo.rating }}</text>
          <text class="rating-count">({{ merchantInfo.reviewCount }}条评价)</text>
        </view>
        <text class="shop-desc">{{ merchantInfo.description }}</text>
      </view>
      <view class="edit-btn" @tap="toEditShop">
        <uni-icons type="compose" size="20" color="#FF6B35"></uni-icons>
      </view>
    </view>

    <!-- 数据统计 -->
    <view class="stats-section">
      <view class="stats-grid">
        <view class="stat-item" @tap="toFinance">
          <text class="stat-value">¥{{ statistics.balance }}</text>
          <text class="stat-label">账户余额</text>
        </view>
        <view class="stat-item" @tap="toFinance">
          <text class="stat-value">¥{{ statistics.todayRevenue }}</text>
          <text class="stat-label">今日收入</text>
        </view>
        <view class="stat-item" @tap="toComment">
          <text class="stat-value">{{ statistics.pendingReviews }}</text>
          <text class="stat-label">待回复评价</text>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section">
      <view class="menu-group">
        <view class="menu-item" @tap="toShopSettings">
          <view class="item-left">
            <uni-icons type="shop" size="22" color="#FF6B35"></uni-icons>
            <text class="item-name">店铺设置</text>
          </view>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
        <view class="menu-item" @tap="toMenuManage">
          <view class="item-left">
            <uni-icons type="list" size="22" color="#FF6B35"></uni-icons>
            <text class="item-name">菜单管理</text>
          </view>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
        <view class="menu-item" @tap="toTutorials">
          <view class="item-left">
            <uni-icons type="video" size="22" color="#FF6B35"></uni-icons>
            <text class="item-name">教程管理</text>
          </view>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
      </view>

      <view class="menu-group">
        <view class="menu-item" @tap="toFinance">
          <view class="item-left">
            <uni-icons type="wallet" size="22" color="#52C41A"></uni-icons>
            <text class="item-name">财务管理</text>
          </view>
          <view class="item-right">
            <text class="item-desc">收入、提现</text>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
        </view>
        <view class="menu-item" @tap="toWithdraw">
          <view class="item-left">
            <uni-icons type="money" size="22" color="#52C41A"></uni-icons>
            <text class="item-name">提现</text>
          </view>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
      </view>

      <view class="menu-group">
        <view class="menu-item" @tap="toEditProfile">
          <view class="item-left">
            <uni-icons type="person" size="22" color="#1890FF"></uni-icons>
            <text class="item-name">个人资料</text>
          </view>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
        <view class="menu-item" @tap="toSettings">
          <view class="item-left">
            <uni-icons type="gear" size="22" color="#1890FF"></uni-icons>
            <text class="item-name">设置</text>
          </view>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
      </view>

      <view class="menu-group">
        <view class="menu-item" @tap="toHelp">
          <view class="item-left">
            <uni-icons type="help" size="22" color="#999"></uni-icons>
            <text class="item-name">帮助中心</text>
          </view>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
        <view class="menu-item" @tap="toAbout">
          <view class="item-left">
            <uni-icons type="info" size="22" color="#999"></uni-icons>
            <text class="item-name">关于我们</text>
          </view>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-section">
      <button class="logout-btn" @tap="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showConfirm } from '@/utils/helper'
import { merchantApi } from '@/api'

// 商家信息
const merchantInfo = ref({
  id: 1,
  name: '老王家常菜',
  logo: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=店',
  rating: 4.8,
  reviewCount: 156,
  description: '主营川菜，口味正宗，分量足'
})

// 统计数据
const statistics = ref({
  balance: '5,280.00',
  todayRevenue: '1,680',
  pendingReviews: 5
})

onMounted(async () => {
  await loadMerchantInfo()
  await loadStatistics()
})

/**
 * M-020: 加载商家信息
 */
const loadMerchantInfo = async () => {
  try {
    const res = await merchantApi.getInfo()
    if (res.code === 200 && res.data) {
      merchantInfo.value = {
        id: res.data.id,
        name: res.data.name,
        logo: res.data.logo || res.data.avatar,
        rating: res.data.rating || 4.8,
        reviewCount: res.data.reviewCount || 0,
        description: res.data.description || ''
      }
    }
  } catch (error) {
    console.error('加载商家信息失败:', error)
    // 保持默认数据
  }
}

/**
 * M-021: 加载统计数据
 */
const loadStatistics = async () => {
  try {
    const merchantId = uni.getStorageSync('merchantId') || merchantInfo.value.id
    const res = await merchantApi.getStatistics(merchantId, { timeRange: 'today' })
    if (res.code === 200 && res.data) {
      statistics.value = {
        balance: formatMoney(res.data.balance || 0),
        todayRevenue: formatMoney(res.data.todayRevenue || 0),
        pendingReviews: res.data.pendingReviews || 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 保持默认数据
  }
}

/**
 * 格式化金额
 */
const formatMoney = (amount) => {
  if (typeof amount === 'number') {
    return amount.toFixed(2)
  }
  return amount
}

/**
 * 编辑店铺
 */
const toEditShop = () => {
  uni.navigateTo({
    url: '/profile/shop'
  })
}

/**
 * 店铺设置
 */
const toShopSettings = () => {
  uni.navigateTo({
    url: '/profile/shop'
  })
}

/**
 * 菜单管理
 */
const toMenuManage = () => {
  uni.navigateTo({
    url: '/menu/index'
  })
}

/**
 * 教程管理
 */
const toTutorials = () => {
  uni.navigateTo({
    url: '/profile/tutorials'
  })
}

/**
 * 财务管理
 */
const toFinance = () => {
  uni.navigateTo({
    url: '/profile/finance'
  })
}

/**
 * 提现
 */
const toWithdraw = () => {
  uni.navigateTo({
    url: '/profile/withdraw'
  })
}

/**
 * 编辑个人资料
 */
const toEditProfile = () => {
  uni.navigateTo({
    url: '/profile/edit'
  })
}

/**
 * 设置
 */
const toSettings = () => {
  uni.navigateTo({
    url: '/profile/settings'
  })
}

/**
 * 评价中心
 */
const toComment = () => {
  uni.navigateTo({
    url: '/comment/index'
  })
}

/**
 * 帮助中心
 */
const toHelp = () => {
  uni.showToast({
    title: '帮助中心开发中',
    icon: 'none'
  })
}

/**
 * 关于我们
 */
const toAbout = () => {
  uni.showToast({
    title: '关于我们开发中',
    icon: 'none'
  })
}

/**
 * M-022: 退出登录
 */
const handleLogout = async () => {
  const confirmed = await showConfirm('确定要退出登录吗？')

  if (confirmed) {
    try {
      // 调用API退出登录
      await merchantApi.logout()
    } catch (error) {
      console.error('退出登录失败:', error)
    } finally {
      // 无论API调用是否成功，都清除本地登录状态
      uni.removeStorageSync('token')
      uni.removeStorageSync('merchantInfo')
      uni.removeStorageSync('merchantId')

      uni.showToast({
        title: '已退出登录',
        icon: 'success'
      })

      setTimeout(() => {
        uni.reLaunch({
          url: '/pages/login/index'
        })
      }, 1000)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.merchant-profile-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

/* 商家信息卡片 */
.profile-header {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  padding: 40rpx 30rpx;
  display: flex;
  gap: 20rpx;
  position: relative;
}

.shop-logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  background: #fff;
  flex-shrink: 0;
}

.shop-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.shop-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}

.shop-rating {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.rating-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #fff;
}

.rating-count {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.shop-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  @include text-ellipsis;
}

.edit-btn {
  position: absolute;
  top: 30rpx;
  right: 30rpx;
  width: 60rpx;
  height: 60rpx;
  @include flex-center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  backdrop-filter: blur(10px);
}

/* 数据统计 */
.stats-section {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.stat-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #FF6B35;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

/* 功能菜单 */
.menu-section {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding: 0 20rpx;
}

.menu-group {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;

  &:last-child {
    border-bottom: none;
  }
}

.item-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.item-name {
  font-size: 30rpx;
  color: #333;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.item-desc {
  font-size: 26rpx;
  color: #999;
}

/* 退出登录 */
.logout-section {
  padding: 40rpx 20rpx 0;
}

.logout-btn {
  width: 100%;
  height: 90rpx;
  background: #fff;
  border: 2rpx solid #F5222D;
  border-radius: 45rpx;
  @include flex-center;
  font-size: 28rpx;
  color: #F5222D;
}
</style>
