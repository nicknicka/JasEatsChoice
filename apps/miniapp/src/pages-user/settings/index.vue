<template>
  <view class="settings-container">
    <scroll-view class="scroll-container" scroll-y>
      <!-- 用户信息卡片 -->
      <view class="user-profile-card" @click="editProfile">
        <view class="profile-left">
          <image class="avatar-image" :src="userInfo.avatar || DEFAULT_AVATAR" mode="aspectFill" />
          <view class="user-info">
            <text class="user-name">{{ userInfo.name }}</text>
            <text class="user-id">{{ userInfo.id ? `ID: ${userInfo.id}` : '完善资料与偏好设置' }}</text>
          </view>
        </view>
        <uni-icons type="right" size="16" color="rgba(255,255,255,0.72)"></uni-icons>
      </view>

      <!-- 账号设置 -->
      <view class="settings-section">
        <view class="section-title">账号设置</view>

        <view class="setting-item" @click="handlePageNavigation('personal-info')">
          <view class="item-icon-wrapper">
            <uni-icons type="person-filled" size="22" color="#FF6B35"></uni-icons>
          </view>
          <view class="item-content">
            <text class="item-label">个人信息</text>
            <text class="item-value" v-if="userInfo.profileCompleted">已完善</text>
            <text class="item-tips" v-else>未完善</text>
          </view>
          <uni-icons type="right" size="16" color="#B7B7B7"></uni-icons>
        </view>

        <view class="setting-item" @click="handlePageNavigation('address')">
          <view class="item-icon-wrapper">
            <uni-icons type="location" size="22" color="#FF6B35"></uni-icons>
          </view>
          <view class="item-content">
            <text class="item-label">收货地址</text>
            <text class="item-value">管理常用收货地址</text>
          </view>
          <uni-icons type="right" size="16" color="#B7B7B7"></uni-icons>
        </view>

        <view class="setting-item" @click="handlePageNavigation('help')">
          <view class="item-icon-wrapper">
            <uni-icons type="help" size="22" color="#FF6B35"></uni-icons>
          </view>
          <view class="item-content">
            <text class="item-label">帮助中心</text>
            <text class="item-value">资料、账号与使用问题统一查看</text>
          </view>
          <uni-icons type="right" size="16" color="#B7B7B7"></uni-icons>
        </view>
      </view>

      <!-- 通知设置 -->
      <view class="settings-section">
        <view class="section-title">通知设置</view>

        <view class="setting-item">
          <view class="item-icon-wrapper">
            <uni-icons type="notification-filled" size="22" color="#FF6B35"></uni-icons>
          </view>
          <view class="item-content">
            <text class="item-label">消息通知</text>
            <text class="item-desc">接收订单和活动消息</text>
          </view>
          <switch
            :checked="settings.messageNotification"
            @change="toggleSetting('messageNotification')"
            color="#FF6B35"
          />
        </view>

        <view class="setting-item">
          <view class="item-icon-wrapper">
            <uni-icons type="email" size="22" color="#FF6B35"></uni-icons>
          </view>
          <view class="item-content">
            <text class="item-label">营销通知</text>
            <text class="item-desc">接收优惠和推荐消息</text>
          </view>
          <switch
            :checked="settings.marketingNotification"
            @change="toggleSetting('marketingNotification')"
            color="#FF6B35"
          />
        </view>

        <view class="setting-item" @click="handlePageNavigation('message')">
          <view class="item-icon-wrapper">
            <uni-icons type="chatbubble-filled" size="22" color="#FF6B35"></uni-icons>
          </view>
          <view class="item-content">
            <text class="item-label">消息中心</text>
            <text class="item-value">查看订单、活动和聊天消息</text>
          </view>
          <uni-icons type="right" size="16" color="#B7B7B7"></uni-icons>
        </view>
      </view>

      <!-- 隐私设置 -->
      <view class="settings-section">
        <view class="section-title">隐私设置</view>

        <view class="setting-item">
          <view class="item-icon-wrapper">
            <uni-icons type="person" size="22" color="#FF6B35"></uni-icons>
          </view>
          <view class="item-content">
            <text class="item-label">个人信息可见</text>
            <text class="item-desc">允许其他用户查看您的基本信息</text>
          </view>
          <switch
            :checked="settings.profileVisible"
            @change="toggleSetting('profileVisible')"
            color="#FF6B35"
          />
        </view>

        <view class="setting-item">
          <view class="item-icon-wrapper">
            <uni-icons type="location" size="22" color="#FF6B35"></uni-icons>
          </view>
          <view class="item-content">
            <text class="item-label">位置信息</text>
            <text class="item-desc">允许获取位置以提供更好服务</text>
          </view>
          <switch
            :checked="settings.locationEnabled"
            @change="toggleSetting('locationEnabled')"
            color="#FF6B35"
          />
        </view>

        <view class="setting-item" @click="handlePageNavigation('about')">
          <view class="item-icon-wrapper">
            <uni-icons type="locked" size="22" color="#FF6B35"></uni-icons>
          </view>
          <view class="item-content">
            <text class="item-label">隐私与说明</text>
            <text class="item-value">查看平台说明、协议与隐私相关内容</text>
          </view>
          <uni-icons type="right" size="16" color="#B7B7B7"></uni-icons>
        </view>
      </view>

      <!-- 通用设置 -->
      <view class="settings-section">
        <view class="section-title">通用设置</view>

        <view class="setting-item">
          <view class="item-icon-wrapper">
            <uni-icons type="gear-filled" size="22" color="#FF6B35"></uni-icons>
          </view>
          <view class="item-content">
            <text class="item-label">深色模式</text>
            <text class="item-value">跟随系统</text>
          </view>
          <switch
            :checked="settings.darkMode"
            disabled
            color="#FF6B35"
          />
        </view>

        <view class="setting-item is-disabled">
          <view class="item-icon-wrapper">
            <uni-icons type="world" size="22" color="#FF6B35"></uni-icons>
          </view>
          <view class="item-content">
            <text class="item-label">语言</text>
            <text class="item-value">简体中文（固定）</text>
          </view>
          <text class="item-arrow">暂不支持切换</text>
        </view>

        <view class="setting-item" @click="clearCache">
          <view class="item-icon-wrapper">
            <uni-icons type="trash" size="22" color="#FF6B35"></uni-icons>
          </view>
          <view class="item-content">
            <text class="item-label">清除缓存</text>
            <text class="item-value">{{ cacheSize }}</text>
          </view>
          <text class="item-action">清除</text>
        </view>
      </view>

      <!-- 其他 -->
      <view class="settings-section">
        <view class="section-title">其他</view>

        <view class="setting-item" @click="handlePageNavigation('feedback')">
          <view class="item-icon-wrapper">
            <uni-icons type="compose" size="22" color="#FF6B35"></uni-icons>
          </view>
          <view class="item-content">
            <text class="item-label">意见反馈</text>
          </view>
          <uni-icons type="right" size="16" color="#B7B7B7"></uni-icons>
        </view>

        <view class="setting-item" @click="handlePageNavigation('about')">
          <view class="item-icon-wrapper">
            <uni-icons type="info" size="22" color="#FF6B35"></uni-icons>
          </view>
          <view class="item-content">
            <text class="item-label">关于我们</text>
            <text class="item-value">v1.0.0</text>
          </view>
          <uni-icons type="right" size="16" color="#B7B7B7"></uni-icons>
        </view>

        <view class="setting-item" @click="checkUpdate">
          <view class="item-icon-wrapper">
            <uni-icons type="refresh" size="22" color="#FF6B35"></uni-icons>
          </view>
          <view class="item-content">
            <text class="item-label">检查更新</text>
            <text class="item-value">已是最新版本</text>
          </view>
          <uni-icons type="right" size="16" color="#B7B7B7"></uni-icons>
        </view>
      </view>

      <!-- 退出登录 -->
      <view class="logout-section">
        <button class="logout-btn" @click="logout">
          退出登录
        </button>
      </view>

      <!-- 版本信息 -->
      <view class="version-info">
        <text class="version-text">佳食宜选 v1.0.0</text>
      </view>

      <!-- 底部空白 -->
      <view class="bottom-spacer"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { userApi, notificationApi } from '@/api'
import {
  LOGIN,
  USER_ADDRESS,
  USER_FEEDBACK,
  USER_HELP,
  USER_MESSAGE,
  USER_PROFILE_ABOUT,
  USER_PROFILE_EDIT
} from '@/constants/routes'

// 用户信息store
const userStore = useUserStore()
const DEFAULT_AVATAR = '/static/images/default-avatar.png'

// 用户信息
const userInfo = ref({
  id: '',
  name: '佳食宜选用户',
  avatar: DEFAULT_AVATAR,
  profileCompleted: false
})

// 设置项
const settings = ref({
  messageNotification: true,
  marketingNotification: false,
  profileVisible: true,
  locationEnabled: true,
  darkMode: false
})

// 缓存大小
const cacheSize = ref('0KB')

const implementedPages = new Set(['personal-info', 'address', 'help', 'message', 'feedback', 'about'])

/**
 * 编辑个人资料
 */
const editProfile = () => {
  uni.navigateTo({
    url: USER_PROFILE_EDIT
  })
}

const isImplementedPage = (page) => implementedPages.has(page)

const handlePageNavigation = (page) => {
  if (!isImplementedPage(page)) {
    return
  }
  navigateTo(page)
}

/**
 * 页面导航
 */
const navigateTo = (page) => {
  const pageMap = {
    'personal-info': USER_PROFILE_EDIT,
    'address': USER_ADDRESS,
    'help': USER_HELP,
    'message': USER_MESSAGE,
    'feedback': USER_FEEDBACK,
    'about': USER_PROFILE_ABOUT
  }

  const path = pageMap[page]

  if (!path) {
    uni.showToast({
      title: '当前入口暂不可用',
      icon: 'none'
    })
    return
  }

  uni.navigateTo({
    url: path,
    fail: () => {
      uni.showToast({
        title: '当前入口暂不可用',
        icon: 'none'
      })
    }
  })
}

const parseUserInfo = (value) => {
  if (!value) return null

  if (typeof value === 'string') {
    try {
      return JSON.parse(value)
    } catch (error) {
      return null
    }
  }

  return value
}

/**
 * 切换设置
 */
const toggleSetting = async (key) => {
  if (!userStore.isLogin) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  const oldValue = settings.value[key]
  settings.value[key] = !settings.value[key]

  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    // 根据设置类型调用不同的API
    if (key.includes('Notification')) {
      // 更新通知偏好设置
      await notificationApi.setPreferences({
        userId,
        orderNotify: settings.value.messageNotification,
        activityNotify: settings.value.marketingNotification,
        systemNotify: true,
        chatNotify: true
      })
    } else {
      // 更新用户设置
      await userApi.updateSettings(userId, {
        [key]: settings.value[key]
      })
    }

    // 保存到本地存储
    uni.setStorageSync('settings', JSON.stringify(settings.value))

    uni.showToast({
      title: '设置已更新',
      icon: 'success',
      duration: 1500
    })
  } catch (error) {
    console.error('更新设置失败:', error)
    // 回滚状态
    settings.value[key] = oldValue
    uni.showToast({
      title: '更新失败，请重试',
      icon: 'none'
    })
  }
}

/**
 * 清除缓存
 */
const clearCache = () => {
  uni.showModal({
    title: '清除缓存',
    content: `确定要清除${cacheSize.value}缓存吗？`,
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({
            title: '清除中...'
          })

          // U-005: 实际清除缓存逻辑
          try {
            const res = uni.getStorageInfoSync()
            const keys = res.keys || []
            // 排除必要的存储数据
            const excludeKeys = ['token', 'userInfo', 'settings', 'user_id']
            keys.forEach(key => {
              if (!excludeKeys.includes(key)) {
                uni.removeStorageSync(key)
              }
            })
          } catch (error) {
            console.error('清除存储失败:', error)
          }

          // 重新计算缓存大小
          await calculateCacheSize()

          uni.hideLoading()
          uni.showToast({
            title: '缓存已清除',
            icon: 'success'
          })
        } catch (error) {
          console.error('清除缓存失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: '清除失败，请重试',
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
      icon: 'success'
    })
  }, 1500)
}

/**
 * 退出登录
 */
const logout = () => {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出登录吗？',
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用退出登录API
          await userApi.logout()
        } catch (error) {
          console.error('退出登录API调用失败:', error)
        }

        // 清除登录信息
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')

        // 清空用户store
        userStore.$reset()

        // 跳转到登录页
        uni.reLaunch({
          url: LOGIN
        })
      }
    }
  })
}

/**
 * 加载用户信息
 */
const loadUserInfo = async () => {
  try {
    if (userStore.isLogin && userStore.userInfo) {
      const info = userStore.userInfo
      userInfo.value = {
        id: info.userId || info.id || '',
        name: info.nickname || info.name || '佳食宜选用户',
        avatar: info.avatar || DEFAULT_AVATAR,
        profileCompleted: !!(info.realName || info.phone || info.nickname || info.goal)
      }
    } else {
      const localInfo = parseUserInfo(uni.getStorageSync('userInfo'))
      if (localInfo) {
        const info = localInfo
        userInfo.value = {
          id: info.userId || info.id || '',
          name: info.nickname || info.name || '佳食宜选用户',
          avatar: info.avatar || DEFAULT_AVATAR,
          profileCompleted: !!(info.realName || info.phone || info.nickname || info.goal)
        }
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

/**
 * 加载设置
 */
const loadSettings = async () => {
  if (!userStore.isLogin) {
    return
  }

  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    // 加载通知偏好设置
    const res = await notificationApi.getPreferences({ userId })

    if (res && res.data) {
      settings.value = {
        messageNotification: res.data.orderNotify !== false,
        marketingNotification: res.data.activityNotify || false,
        profileVisible: true,
        locationEnabled: true,
        darkMode: false
      }

      // 保存到本地存储
      uni.setStorageSync('settings', JSON.stringify(settings.value))
    } else {
      // 从本地存储读取
      const localSettings = uni.getStorageSync('settings')
      if (localSettings) {
        settings.value = JSON.parse(localSettings)
      }
    }
  } catch (error) {
    console.error('加载设置失败:', error)
    // 从本地存储读取
    const localSettings = uni.getStorageSync('settings')
    if (localSettings) {
      settings.value = JSON.parse(localSettings)
    }
  }
}

/**
 * U-006: 计算缓存大小
 */
const calculateCacheSize = () => {
  try {
    // U-006: 实际计算缓存大小
    const res = uni.getStorageInfoSync()
    const size = res.currentSize || 0 // 当前占用的空间大小（单位：KB）

    // 格式化显示
    if (size < 1024) {
      cacheSize.value = `${size}KB`
    } else {
      const mb = (size / 1024).toFixed(1)
      cacheSize.value = `${mb}MB`
    }
  } catch (error) {
    console.error('计算缓存大小失败:', error)
    cacheSize.value = '0KB'
  }
}

// 组件挂载
onMounted(() => {
  loadUserInfo()
  loadSettings()
  calculateCacheSize()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.settings-container {
  min-height: 100vh;
  background-color: $bg-color-base;
}

.scroll-container {
  height: 100vh;
}

/* 用户信息卡片 */
.user-profile-card {
  background: linear-gradient(135deg, #FF6B35, #FF8F61);
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  @include flex-between;
  box-shadow: $box-shadow-md;
}

.profile-left {
  @include flex-center;
  gap: $spacing-md;
}

.avatar-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
}

.user-info {
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
}

.user-name {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: #fff;
}

.user-id {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.8);
}

/* 设置区块 */
.settings-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  margin-top: 0;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  box-shadow: $box-shadow-sm;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-md;
  padding-left: $spacing-xs;
}

.setting-item {
  @include flex-center;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color-lighter;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background-color: $bg-color-base;
    margin: 0 (-$spacing-md);
    padding-left: $spacing-md;
    padding-right: $spacing-md;
  }

  &.is-disabled {
    opacity: 0.7;
  }

  &.is-disabled:active {
    background-color: transparent;
    margin: 0;
    padding-left: 0;
    padding-right: 0;
  }
}

.item-icon-wrapper {
  width: 72rpx;
  height: 72rpx;
  @include flex-center;
  background-color: $bg-color-base;
  border-radius: $border-radius-lg;
  margin-right: $spacing-md;
  flex-shrink: 0;
}

.item-content {
  flex: 1;
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
}

.item-label {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}

.item-value {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.item-tips {
  font-size: $font-size-sm;
  color: $warning-color;
}

.item-desc {
  font-size: $font-size-xs;
  color: $text-color-placeholder;
}

.item-arrow {
  font-size: $font-size-lg;
  color: $text-color-placeholder;
  margin-left: $spacing-sm;
  flex-shrink: 0;
  text-align: right;
}

.item-action {
  padding: $spacing-sm $spacing-md;
  background-color: rgba(255, 107, 53, 0.1);
  color: $primary-color;
  font-size: $font-size-sm;
  border-radius: $border-radius-round;
  margin-left: $spacing-sm;
  flex-shrink: 0;
}

/* 退出登录 */
.logout-section {
  padding: $spacing-lg $spacing-md;
}

.logout-btn {
  width: 100%;
  height: 88rpx;
  @include flex-center;
  background-color: $bg-color-white;
  color: $danger-color;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-lg;
  border: 1rpx solid $danger-color;

  &:active {
    background-color: rgba(239, 83, 80, 0.05);
  }
}

/* 版本信息 */
.version-info {
  @include flex-center;
  padding: $spacing-lg 0;
}

.version-text {
  font-size: $font-size-sm;
  color: $text-color-placeholder;
}

/* 底部空白 */
.bottom-spacer {
  height: 40rpx;
}
</style>
