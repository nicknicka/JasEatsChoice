<template>
  <view class="role-select-container">
    <view class="header">
      <text class="title">选择您的身份</text>
      <text class="subtitle">请选择您在平台上的角色</text>
    </view>

    <view class="role-cards">
      <view class="role-card user" @tap="selectRole('user')">
        <view class="role-icon">👤</view>
        <text class="role-name">我是食客</text>
        <text class="role-desc">浏览美食，下单订餐</text>
        <view class="role-features">
          <text class="feature-tag">个性化推荐</text>
          <text class="feature-tag">在线订餐</text>
          <text class="feature-tag">健康管理</text>
        </view>
      </view>

      <view class="role-card merchant" @tap="selectRole('merchant')">
        <view class="role-icon">👨‍🍳</view>
        <text class="role-name">我是商家</text>
        <text class="role-desc">管理店铺，接单配送</text>
        <view class="role-features">
          <text class="feature-tag">订单管理</text>
          <text class="feature-tag">菜品管理</text>
          <text class="feature-tag">数据统计</text>
        </view>
      </view>
    </view>

    <view class="tips">
      <text class="tips-text">* 后续可在设置中切换角色</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { toUserHome, toMerchantHome } from '@/utils/router'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

const selectRole = async (role) => {
  try {
    uni.showLoading({ title: '设置中...' })
    
    // 更新用户角色
    await userStore.updateRole(role)
    
    uni.hideLoading()
    
    // 根据角色跳转
    if (role === 'merchant') {
      toMerchantHome()
    } else {
      toUserHome()
    }
  } catch (error) {
    uni.hideLoading()
    uni.showToast({
      title: '设置失败，请重试',
      icon: 'none'
    })
  }
}
</script>

<style lang="scss" scoped>
.role-select-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding: 60rpx 30rpx;
}

.header {
  text-align: center;
  margin-bottom: 80rpx;
}

.title {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.subtitle {
  display: block;
  font-size: 28rpx;
  color: #999;
}

.role-cards {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.role-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 8rpx;
  }

  &.user::before {
    background: linear-gradient(90deg, #FF6B35, #FF8F6B);
  }

  &.merchant::before {
    background: linear-gradient(90deg, #52C41A, #73D13D);
  }

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.12);
  }
}

.role-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.role-name {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.role-desc {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 40rpx;
}

.role-features {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;
  justify-content: center;
}

.feature-tag {
  padding: 12rpx 24rpx;
  background: #F5F5F5;
  border-radius: 40rpx;
  font-size: 24rpx;
  color: #666;
}

.user .feature-tag {
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
}

.merchant .feature-tag {
  background: rgba(82, 196, 26, 0.1);
  color: #52C41A;
}

.tips {
  margin-top: 60rpx;
  text-align: center;
}

.tips-text {
  font-size: 24rpx;
  color: #999;
}
</style>
