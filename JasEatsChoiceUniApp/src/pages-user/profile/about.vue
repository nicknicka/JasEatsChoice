<template>
  <view class="about-container">
    <!-- 头部LOGO -->
    <view class="header-section">
      <image class="logo" src="/static/logo.png" mode="aspectFit"></image>
      <text class="app-name">佳食宜选</text>
      <text class="app-version">版本 1.0.0</text>
    </view>

    <!-- 功能介绍 -->
    <view class="section">
      <view class="section-title">产品介绍</view>
      <view class="intro-text">
        佳食宜选是一款专注于校园场景的智能订餐与健康管理应用。我们通过AI技术为您提供个性化的饮食推荐，帮助您合理规划膳食，养成健康的饮食习惯。
      </view>
    </view>

    <!-- 核心功能 -->
    <view class="section">
      <view class="section-title">核心功能</view>
      <view class="feature-grid">
        <view class="feature-item">
          <uni-icons type="calendar" size="40" color="#FF6B35"></uni-icons>
          <text class="feature-name">个性化推荐</text>
          <text class="feature-desc">根据您的喜好和健康目标推荐菜品</text>
        </view>
        <view class="feature-item">
          <uni-icons type="chartbar" size="40" color="#FF6B35"></uni-icons>
          <text class="feature-name">卡路里管理</text>
          <text class="feature-desc">精准记录和分析每日热量摄入</text>
        </view>
        <view class="feature-item">
          <uni-icons type="shop" size="40" color="#FF6B35"></uni-icons>
          <text class="feature-name">在线订餐</text>
          <text class="feature-desc">轻松浏览周边美食，快速下单</text>
        </view>
        <view class="feature-item">
          <uni-icons type="chatbubble" size="40" color="#FF6B35"></uni-icons>
          <text class="feature-name">AI助手</text>
          <text class="feature-desc">智能问答，解答饮食相关问题</text>
        </view>
      </view>
    </view>

    <!-- 联系我们 -->
    <view class="section">
      <view class="section-title">联系我们</view>
      <view class="contact-list">
        <view class="contact-item" @tap="makePhoneCall">
          <uni-icons type="phone" size="20" color="#666"></uni-icons>
          <text class="contact-label">客服电话</text>
          <text class="contact-value">400-123-4567</text>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
        <view class="contact-item" @tap="copyEmail">
          <uni-icons type="email" size="20" color="#666"></uni-icons>
          <text class="contact-label">邮箱</text>
          <text class="contact-value">support@jiashiyixuan.com</text>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
        <view class="contact-item" @tap="openWebsite">
          <uni-icons type="link" size="20" color="#666"></uni-icons>
          <text class="contact-label">官网</text>
          <text class="contact-value">www.jiashiyixuan.com</text>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 法律信息 -->
    <view class="section">
      <view class="section-title">法律信息</view>
      <view class="legal-list">
        <view class="legal-item" @tap="viewAgreement('user')">
          <text>用户协议</text>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
        <view class="legal-item" @tap="viewAgreement('privacy')">
          <text>隐私政策</text>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
        <view class="legal-item" @tap="viewAgreement('permission')">
          <text>权限说明</text>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 底部信息 -->
    <view class="footer">
      <text class="copyright">© 2026 佳食宜选 All Rights Reserved</text>
      <text class="icp">ICP备案号：京ICP备12345678号</text>
    </view>
  </view>
</template>

<script setup>
const makePhoneCall = () => {
  uni.makePhoneCall({
    phoneNumber: '400-123-4567'
  })
}

const copyEmail = () => {
  uni.setClipboardData({
    data: 'support@jiashiyixuan.com',
    success: () => {
      uni.showToast({
        title: '邮箱已复制',
        icon: 'success'
      })
    }
  })
}

const openWebsite = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const viewAgreement = (type) => {
  const titleMap = {
    user: '用户协议',
    privacy: '隐私政策',
    permission: '权限说明'
  }

  uni.showToast({
    title: `${titleMap[type]}功能开发中`,
    icon: 'none'
  })
}
</script>

<style lang="scss" scoped>
.about-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

.header-section {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  padding: 80rpx 30rpx 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo {
  width: 160rpx;
  height: 160rpx;
  border-radius: 32rpx;
  background: #fff;
  margin-bottom: 30rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
}

.app-name {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 15rpx;
}

.app-version {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.section {
  background: #fff;
  margin-top: 20rpx;
  padding: 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.intro-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30rpx;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30rpx 20rpx;
  background: #F5F5F5;
  border-radius: 16rpx;
}

.feature-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin: 20rpx 0 10rpx;
}

.feature-desc {
  font-size: 24rpx;
  color: #999;
  text-align: center;
  line-height: 1.5;
}

.contact-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 25rpx 0;
  border-bottom: 1rpx solid #eee;
}

.contact-label {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.contact-value {
  font-size: 26rpx;
  color: #666;
}

.legal-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.legal-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25rpx 0;
  border-bottom: 1rpx solid #eee;
  font-size: 28rpx;
  color: #333;
}

.footer {
  padding: 40rpx 30rpx;
  text-align: center;
}

.copyright {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-bottom: 10rpx;
}

.icp {
  font-size: 24rpx;
  color: #CCC;
}
</style>
