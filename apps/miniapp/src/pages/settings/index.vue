<template>
  <view class="settings-container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-title">设置</view>
      <view class="nav-placeholder"></view>
    </view>

    <scroll-view class="scroll-content" scroll-y>
      <!-- 账号安全 -->
      <view class="section-card">
        <view class="section-title">账号安全</view>

        <view class="menu-list">
          <view class="menu-item" @click="navigateTo('change-phone')">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">📱</text>
            </view>
            <view class="menu-content">
              <text class="menu-label">手机号</text>
              <text class="menu-value">{{ maskedPhone }}</text>
            </view>
            <text class="menu-arrow">→</text>
          </view>

          <view class="menu-item" @click="navigateTo('change-password')">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">🔒</text>
            </view>
            <view class="menu-content">
              <text class="menu-label">修改密码</text>
            </view>
            <text class="menu-arrow">→</text>
          </view>
        </view>
      </view>

      <!-- 隐私设置 -->
      <view class="section-card">
        <view class="section-title">隐私设置</view>

        <view class="menu-list">
          <view class="menu-item switch-item">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">👁️</text>
            </view>
            <view class="menu-content">
              <text class="menu-label">公开个人资料</text>
              <text class="menu-desc">允许其他用户查看您的基本信息</text>
            </view>
            <switch
              :checked="settings.publicProfile"
              @change="settings.publicProfile = $event.detail.value"
              color="#FF6B35"
            />
          </view>

          <view class="menu-item switch-item">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">📍</text>
            </view>
            <view class="menu-content">
              <text class="menu-label">位置信息</text>
              <text class="menu-desc">允许获取您的位置以提供附近推荐</text>
            </view>
            <switch
              :checked="settings.locationEnabled"
              @change="settings.locationEnabled = $event.detail.value"
              color="#FF6B35"
            />
          </view>
        </view>
      </view>

      <!-- 通知设置 -->
      <view class="section-card">
        <view class="section-title">通知设置</view>

        <view class="menu-list">
          <view class="menu-item switch-item">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">🔔</text>
            </view>
            <view class="menu-content">
              <text class="menu-label">订单通知</text>
            </view>
            <switch
              :checked="settings.orderNotification"
              @change="settings.orderNotification = $event.detail.value"
              color="#FF6B35"
            />
          </view>

          <view class="menu-item switch-item">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">💬</text>
            </view>
            <view class="menu-content">
              <text class="menu-label">消息通知</text>
            </view>
            <switch
              :checked="settings.messageNotification"
              @change="settings.messageNotification = $event.detail.value"
              color="#FF6B35"
            />
          </view>

          <view class="menu-item switch-item">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">📢</text>
            </view>
            <view class="menu-content">
              <text class="menu-label">营销通知</text>
            </view>
            <switch
              :checked="settings.marketingNotification"
              @change="settings.marketingNotification = $event.detail.value"
              color="#FF6B35"
            />
          </view>
        </view>
      </view>

      <!-- 通用设置 -->
      <view class="section-card">
        <view class="section-title">通用</view>

        <view class="menu-list">
          <view class="menu-item" @click="clearCache">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">🗑️</text>
            </view>
            <view class="menu-content">
              <text class="menu-label">清除缓存</text>
              <text class="menu-value">{{ cacheSize }}</text>
            </view>
          </view>

          <view class="menu-item" @click="checkUpdate">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">🔄</text>
            </view>
            <view class="menu-content">
              <text class="menu-label">检查更新</text>
              <text class="menu-value">v1.0.0</text>
            </view>
            <text class="menu-arrow">→</text>
          </view>

          <view class="menu-item" @click="navigateTo('about')">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">ℹ️</text>
            </view>
            <view class="menu-content">
              <text class="menu-label">关于我们</text>
            </view>
            <text class="menu-arrow">→</text>
          </view>

          <view class="menu-item" @click="navigateTo('privacy')">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">📄</text>
            </view>
            <view class="menu-content">
              <text class="menu-label">隐私政策</text>
            </view>
            <text class="menu-arrow">→</text>
          </view>

          <view class="menu-item" @click="navigateTo('agreement')">
            <view class="menu-icon-wrapper">
              <text class="menu-icon">📋</text>
            </view>
            <view class="menu-content">
              <text class="menu-label">用户协议</text>
            </view>
            <text class="menu-arrow">→</text>
          </view>
        </view>
      </view>

      <!-- 账号操作 -->
      <view class="section-card">
        <view class="section-title">账号操作</view>

        <view class="menu-list">
          <view class="menu-item danger-item" @click="handleLogout">
            <view class="menu-icon-wrapper danger-icon">
              <text class="menu-icon">🚪</text>
            </view>
            <view class="menu-content">
              <text class="menu-label danger-text">退出登录</text>
            </view>
          </view>

          <view class="menu-item danger-item" @click="deleteAccount">
            <view class="menu-icon-wrapper danger-icon">
              <text class="menu-icon">⚠️</text>
            </view>
            <view class="menu-content">
              <text class="menu-label danger-text">注销账号</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 底部安全区 -->
      <view class="bottom-safe-area"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'

const userStore = useUserStore()

// 设置项
const settings = ref({
  publicProfile: true,
  locationEnabled: true,
  orderNotification: true,
  messageNotification: true,
  marketingNotification: false
})

// 缓存大小
const cacheSize = ref('0MB')

// 脱敏手机号
const maskedPhone = computed(() => {
  const phone = userStore.userInfo?.phone
  if (!phone) return '未绑定'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
})

// 加载设置
onMounted(() => {
  loadSettings()
  calculateCacheSize()
})

/**
 * 加载设置
 */
const loadSettings = () => {
  try {
    const savedSettings = uni.getStorageSync('userSettings')
    if (savedSettings) {
      settings.value = { ...settings.value, ...savedSettings }
    }
  } catch (error) {
    console.error('加载设置失败:', error)
  }
}

/**
 * 保存设置
 */
const saveSettings = () => {
  try {
    uni.setStorageSync('userSettings', settings.value)
  } catch (error) {
    console.error('保存设置失败:', error)
  }
}

// 监听设置变化
const unwatchSettings = () => {
  saveSettings()
}

/**
 * 计算缓存大小
 */
const calculateCacheSize = () => {
  try {
    const res = uni.getStorageInfoSync()
    const size = res.currentSize
    cacheSize.value = size < 1 ? `${Math.round(size * 1024)}KB` : `${size.toFixed(2)}MB`
  } catch (error) {
    console.error('计算缓存大小失败:', error)
  }
}

/**
 * 清除缓存
 */
const clearCache = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清除缓存吗？这将删除所有临时文件。',
    success: (res) => {
      if (res.confirm) {
        try {
          // 清除图片缓存
          uni.clearStorageSync()

          // 保留必要的数据
          const token = uni.getStorageSync('token')
          const userInfo = uni.getStorageSync('userInfo')
          const userId = uni.getStorageSync('userId')

          if (token) uni.setStorageSync('token', token)
          if (userInfo) uni.setStorageSync('userInfo', userInfo)
          if (userId) uni.setStorageSync('userId', userId)

          cacheSize.value = '0MB'

          uni.showToast({
            title: '清除成功',
            icon: 'success'
          })
        } catch (error) {
          console.error('清除缓存失败:', error)
          uni.showToast({
            title: '清除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 检查更新
 */
const checkUpdate = () => {
  uni.showLoading({
    title: '检查中...'
  })

  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '已是最新版本',
      icon: 'none'
    })
  }, 1000)
}

/**
 * 退出登录
 */
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        // 清除用户信息
        userStore.logout()
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        uni.removeStorageSync('userId')
        uni.removeStorageSync('role')

        uni.showToast({
          title: '已退出登录',
          icon: 'success'
        })

        // 延迟跳转到登录页
        setTimeout(() => {
          uni.reLaunch({
            url: '/pages/login/index'
          })
        }, 1500)
      }
    }
  })
}

/**
 * 注销账号
 */
const deleteAccount = () => {
  uni.showModal({
    title: '警告',
    content: '注销账号后将无法恢复，所有数据将被永久删除。确定要注销吗？',
    confirmText: '确定注销',
    confirmColor: '#FF6B35',
    success: (res) => {
      if (res.confirm) {
        // 二次确认
        uni.showModal({
          title: '最后确认',
          content: '注销操作不可撤销，请再次确认',
          confirmText: '仍要注销',
          confirmColor: '#FF6B35',
          success: async (res2) => {
            if (res2.confirm) {
              try {
                const userId = userStore.userInfo?.userId || userStore.userInfo?.id
                // await userApi.deleteUser(userId)

                // 清除所有数据
                uni.clearStorageSync()

                uni.showToast({
                  title: '账号已注销',
                  icon: 'success'
                })

                setTimeout(() => {
                  uni.reLaunch({
                    url: '/pages/login/index'
                  })
                }, 1500)
              } catch (error) {
                console.error('注销账号失败:', error)
                uni.showToast({
                  title: '注销失败',
                  icon: 'none'
                })
              }
            }
          }
        })
      }
    }
  })
}

/**
 * 页面导航
 */
const navigateTo = (page) => {
  uni.showToast({
    title: '页面开发中...',
    icon: 'none'
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

.settings-container {
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

/* 滚动内容 */
.scroll-content {
  height: 100vh;
  padding-top: 108rpx;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 区块卡片 */
.section-card {
  background-color: $bg-color-white;
  margin: 0 $spacing-md $spacing-md;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  box-shadow: $box-shadow-sm;
}

.section-title {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  margin-bottom: $spacing-md;
  padding-left: $spacing-sm;
}

/* 菜单列表 */
.menu-list {
  .menu-item {
    display: flex;
    align-items: center;
    padding: $spacing-md 0;
    border-bottom: 1rpx solid $border-color-lighter;

    &:last-child {
      border-bottom: none;
    }

    &.switch-item {
      padding: $spacing-lg 0;
    }

    &:active {
      background-color: $bg-color-base;
    }
  }
}

.menu-icon-wrapper {
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

.menu-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.menu-label {
  font-size: $font-size-base;
  color: $text-color-primary;
}

.menu-desc {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

.menu-value {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  margin-right: $spacing-sm;
}

.menu-arrow {
  font-size: $font-size-base;
  color: $text-color-secondary;
}

/* 危险操作 */
.danger-item {
  .danger-icon {
    background-color: rgba($danger-color, 0.1);
  }

  .danger-text {
    color: $danger-color;
    font-weight: $font-weight-bold;
  }
}

/* 底部安全区 */
.bottom-safe-area {
  height: 40rpx;
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
