<template>
  <view class="about-container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-title">关于我们</view>
      <view class="nav-placeholder"></view>
    </view>

    <scroll-view class="content-scroll" scroll-y>
      <!-- 应用信息 -->
      <view class="app-info-section">
        <image class="app-logo" src="/static/logo.png" mode="aspectFit" />
        <text class="app-name">佳食宜选</text>
        <text class="app-version">Version 1.0.0</text>
        <text class="app-slogan">智能饮食，健康生活</text>
      </view>

      <!-- 功能介绍 -->
      <view class="section-card">
        <view class="section-title">
          <text class="title-icon">✨</text>
          <text class="title-text">产品介绍</text>
        </view>

        <view class="feature-list">
          <view class="feature-item">
            <text class="feature-icon">🤖</text>
            <view class="feature-content">
              <text class="feature-name">AI智能推荐</text>
              <text class="feature-desc">根据您的口味和健康需求，智能推荐适合的菜品</text>
            </view>
          </view>

          <view class="feature-item">
            <text class="feature-icon">📊</text>
            <view class="feature-content">
              <text class="feature-name">营养分析</text>
              <text class="feature-desc">详细记录每日饮食摄入，科学管理卡路里</text>
            </view>
          </view>

          <view class="feature-item">
            <text class="feature-icon">🍽️</text>
            <view class="feature-content">
              <text class="feature-name">美食探索</text>
              <text class="feature-desc">发现周边优质商家，品尝地道美食</text>
            </view>
          </view>

          <view class="feature-item">
            <text class="feature-icon">💬</text>
            <view class="feature-content">
              <text class="feature-name">社交互动</text>
              <text class="feature-desc">分享美食体验，结识美食爱好者</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 联系我们 -->
      <view class="section-card">
        <view class="section-title">
          <text class="title-icon">📞</text>
          <text class="title-text">联系我们</text>
        </view>

        <view class="contact-list">
          <view class="contact-item" @click="contactService('phone')">
            <view class="contact-icon">📱</view>
            <view class="contact-info">
              <text class="contact-label">客服电话</text>
              <text class="contact-value">400-123-4567</text>
            </view>
            <text class="contact-arrow">→</text>
          </view>

          <view class="contact-item" @click="contactService('email')">
            <view class="contact-icon">📧</view>
            <view class="contact-info">
              <text class="contact-label">邮箱</text>
              <text class="contact-value">service@jiashiyixuan.com</text>
            </view>
            <text class="contact-arrow">→</text>
          </view>

          <view class="contact-item" @click="contactService('wechat')">
            <view class="contact-icon">💬</view>
            <view class="contact-info">
              <text class="contact-label">微信公众号</text>
              <text class="contact-value">佳食宜选</text>
            </view>
            <text class="contact-arrow">→</text>
          </view>

          <view class="contact-item" @click="contactService('website')">
            <view class="contact-icon">🌐</view>
            <view class="contact-info">
              <text class="contact-label">官方网站</text>
              <text class="contact-value">www.jiashiyixuan.com</text>
            </view>
            <text class="contact-arrow">→</text>
          </view>
        </view>
      </view>

      <!-- 法律信息 -->
      <view class="section-card">
        <view class="section-title">
          <text class="title-icon">📄</text>
          <text class="title-text">法律信息</text>
        </view>

        <view class="legal-list">
          <view class="legal-item" @click="viewLegal('privacy')">
            <text class="legal-text">隐私政策</text>
            <text class="legal-arrow">→</text>
          </view>

          <view class="legal-item" @click="viewLegal('agreement')">
            <text class="legal-text">用户协议</text>
            <text class="legal-arrow">→</text>
          </view>

          <view class="legal-item" @click="viewLegal('license')">
            <text class="legal-text">开源许可</text>
            <text class="legal-arrow">→</text>
          </view>
        </view>
      </view>

      <!-- 版权信息 -->
      <view class="copyright-section">
        <text class="copyright-text">Copyright © 2024 佳食宜选</text>
        <text class="copyright-text">All Rights Reserved</text>
        <text class="icp-text">ICP备案号：粤ICP备2024000000号</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}

/**
 * 联系客服
 */
const contactService = (type) => {
  switch (type) {
    case 'phone':
      uni.makePhoneCall({
        phoneNumber: '400-123-4567'
      })
      break
    case 'email':
      uni.setClipboardData({
        data: 'service@jiashiyixuan.com',
        success: () => {
          uni.showToast({
            title: '邮箱已复制',
            icon: 'success'
          })
        }
      })
      break
    case 'wechat':
      uni.showToast({
        title: '请微信搜索"佳食宜选"',
        icon: 'none'
      })
      break
    case 'website':
      uni.showToast({
        title: '即将开放访问',
        icon: 'none'
      })
      break
  }
}

/**
 * 查看法律信息
 */
const viewLegal = (type) => {
  uni.showToast({
    title: '页面开发中...',
    icon: 'none'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.about-container {
  min-height: 100vh;
  background-color: $bg-color-base;
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

.nav-back,
.nav-placeholder {
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

/* 内容滚动 */
.content-scroll {
  height: 100vh;
  padding-top: 108rpx;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 应用信息 */
.app-info-section {
  background: linear-gradient(135deg, $primary-color, #FF8C61);
  padding: 80rpx $spacing-xl;
  text-align: center;
  margin-bottom: $spacing-md;
}

.app-logo {
  width: 160rpx;
  height: 160rpx;
  border-radius: 32rpx;
  background-color: #fff;
  margin-bottom: $spacing-lg;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
}

.app-name {
  display: block;
  font-size: 48rpx;
  font-weight: $font-weight-bold;
  color: #fff;
  margin-bottom: $spacing-sm;
}

.app-version {
  display: block;
  font-size: $font-size-base;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: $spacing-xs;
}

.app-slogan {
  display: block;
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.9);
}

/* 区块卡片 */
.section-card {
  background-color: $bg-color-white;
  margin: 0 $spacing-md $spacing-md;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  box-shadow: $box-shadow-sm;
}

.section-title {
  @include flex-center;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;
}

.title-icon {
  font-size: 36rpx;
}

.title-text {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

/* 功能列表 */
.feature-list {
  .feature-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: $spacing-lg;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.feature-icon {
  font-size: 48rpx;
  margin-right: $spacing-md;
}

.feature-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.feature-name {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.feature-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  line-height: 1.5;
}

/* 联系列表 */
.contact-list {
  .contact-item {
    display: flex;
    align-items: center;
    padding: $spacing-md 0;
    border-bottom: 1rpx solid $border-color-lighter;

    &:last-child {
      border-bottom: none;
    }

    &:active {
      background-color: $bg-color-base;
    }
  }
}

.contact-icon {
  width: 64rpx;
  height: 64rpx;
  @include flex-center;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  font-size: 32rpx;
  margin-right: $spacing-md;
}

.contact-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.contact-label {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.contact-value {
  font-size: $font-size-base;
  color: $text-color-primary;
}

.contact-arrow {
  font-size: $font-size-base;
  color: $text-color-secondary;
}

/* 法律列表 */
.legal-list {
  .legal-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-md 0;
    border-bottom: 1rpx solid $border-color-lighter;

    &:last-child {
      border-bottom: none;
    }

    &:active {
      background-color: $bg-color-base;
    }
  }
}

.legal-text {
  font-size: $font-size-base;
  color: $text-color-primary;
}

.legal-arrow {
  font-size: $font-size-base;
  color: $text-color-secondary;
}

/* 版权信息 */
.copyright-section {
  padding: $spacing-xl;
  text-align: center;
}

.copyright-text {
  display: block;
  font-size: $font-size-sm;
  color: $text-color-secondary;
  margin-bottom: $spacing-xs;
}

.icp-text {
  display: block;
  font-size: $font-size-xs;
  color: $text-color-placeholder;
}
</style>
