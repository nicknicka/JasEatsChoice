<template>
  <view class="settings-container">
    <!-- 账号与安全 -->
    <view class="settings-group">
      <view class="group-title">账号与安全</view>
      <view class="settings-list">
        <view class="settings-item" @tap="editPhone">
          <view class="item-left">
            <uni-icons type="phone" size="20" color="#FF6B35"></uni-icons>
            <text class="item-label">手机号</text>
          </view>
          <view class="item-right">
            <text class="item-value">{{ maskPhone(userInfo.phone) }}</text>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </view>
        <view class="settings-item" @tap="editPassword">
          <view class="item-left">
            <uni-icons type="locked" size="20" color="#FF6B35"></uni-icons>
            <text class="item-label">登录密码</text>
          </view>
          <view class="item-right">
            <text class="item-value">已设置</text>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </view>
        <view class="settings-item" @tap="editPayPassword">
          <view class="item-left">
            <uni-icons type="wallet" size="20" color="#FF6B35"></uni-icons>
            <text class="item-label">支付密码</text>
          </view>
          <view class="item-right">
            <text class="item-value" :class="{ unbound: !userInfo.hasPayPassword }">
              {{ userInfo.hasPayPassword ? '已设置' : '未设置' }}
            </text>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 消息通知 -->
    <view class="settings-group">
      <view class="group-title">消息通知</view>
      <view class="settings-list">
        <view class="settings-item">
          <view class="item-left">
            <uni-icons type="sound" size="20" color="#FF6B35"></uni-icons>
            <text class="item-label">新订单通知</text>
          </view>
          <view class="item-right">
            <switch
              :checked="notificationSettings.newOrder"
              color="#FF6B35"
              @change="toggleNotification('newOrder', $event)"
            />
          </view>
        </view>
        <view class="settings-item">
          <view class="item-left">
            <uni-icons type="chatbubble" size="20" color="#FF6B35"></uni-icons>
            <text class="item-label">新消息通知</text>
          </view>
          <view class="item-right">
            <switch
              :checked="notificationSettings.newMessage"
              color="#FF6B35"
              @change="toggleNotification('newMessage', $event)"
            />
          </view>
        </view>
        <view class="settings-item">
          <view class="item-left">
            <uni-icons type="notification" size="20" color="#FF6B35"></uni-icons>
            <text class="item-label">系统通知</text>
          </view>
          <view class="item-right">
            <switch
              :checked="notificationSettings.system"
              color="#FF6B35"
              @change="toggleNotification('system', $event)"
            />
          </view>
        </view>
        <view class="settings-item">
          <view class="item-left">
            <uni-icons type="star" size="20" color="#FF6B35"></uni-icons>
            <text class="item-label">评价通知</text>
          </view>
          <view class="item-right">
            <switch
              :checked="notificationSettings.review"
              color="#FF6B35"
              @change="toggleNotification('review', $event)"
            />
          </view>
        </view>
      </view>
    </view>

    <!-- 通用设置 -->
    <view class="settings-group">
      <view class="group-title">通用设置</view>
      <view class="settings-list">
        <view class="settings-item">
          <view class="item-left">
            <uni-icons type="eye" size="20" color="#FF6B35"></uni-icons>
            <text class="item-label">深色模式</text>
          </view>
          <view class="item-right">
            <switch
              :checked="generalSettings.darkMode"
              color="#FF6B35"
              @change="toggleDarkMode"
            />
          </view>
        </view>
        <view class="settings-item" @tap="selectLanguage">
          <view class="item-left">
            <uni-icons type="world" size="20" color="#FF6B35"></uni-icons>
            <text class="item-label">语言设置</text>
          </view>
          <view class="item-right">
            <text class="item-value">简体中文</text>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </view>
        <view class="settings-item">
          <view class="item-left">
            <uni-icons type="cloud-download" size="20" color="#FF6B35"></uni-icons>
            <text class="item-label">自动更新</text>
          </view>
          <view class="item-right">
            <switch
              :checked="generalSettings.autoUpdate"
              color="#FF6B35"
              @change="toggleAutoUpdate"
            />
          </view>
        </view>
        <view class="settings-item" @tap="clearCache">
          <view class="item-left">
            <uni-icons type="trash" size="20" color="#FF6B35"></uni-icons>
            <text class="item-label">清除缓存</text>
          </view>
          <view class="item-right">
            <text class="item-value">{{ cacheSize }}</text>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 其他 -->
    <view class="settings-group">
      <view class="group-title">其他</view>
      <view class="settings-list">
        <view class="settings-item" @tap="viewTutorials">
          <view class="item-left">
            <uni-icons type="help" size="20" color="#FF6B35"></uni-icons>
            <text class="item-label">使用教程</text>
          </view>
          <view class="item-right">
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </view>
        <view class="settings-item" @tap="contactSupport">
          <view class="item-left">
            <uni-icons type="chatboxes" size="20" color="#FF6B35"></uni-icons>
            <text class="item-label">联系客服</text>
          </view>
          <view class="item-right">
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </view>
        <view class="settings-item" @tap="viewAbout">
          <view class="item-left">
            <uni-icons type="info" size="20" color="#FF6B35"></uni-icons>
            <text class="item-label">关于我们</text>
          </view>
          <view class="item-right">
            <text class="item-value">v{{ appVersion }}</text>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </view>
        <view class="settings-item" @tap="checkUpdate">
          <view class="item-left">
            <uni-icons type="loop" size="20" color="#FF6B35"></uni-icons>
            <text class="item-label">检查更新</text>
          </view>
          <view class="item-right">
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-section">
      <button class="logout-button" @tap="logout">退出登录</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 用户信息
const userInfo = ref({
  phone: '13800138000',
  hasPayPassword: true
})

// 消息通知设置
const notificationSettings = ref({
  newOrder: true,
  newMessage: true,
  system: true,
  review: true
})

// 通用设置
const generalSettings = ref({
  darkMode: false,
  autoUpdate: true,
  language: 'zh-CN'
})

// 缓存大小
const cacheSize = ref('23.5MB')

// 应用版本
const appVersion = ref('1.0.0')

onMounted(() => {
  loadSettings()
})

/**
 * 加载设置
 */
const loadSettings = () => {
  // TODO: 调用API获取设置
  // const res = await merchantApi.getSettings()
  // userInfo.value = res.data.userInfo
  // notificationSettings.value = res.data.notificationSettings
  // generalSettings.value = res.data.generalSettings
}

/**
 * 遮罩手机号
 */
const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 编辑手机号
 */
const editPhone = () => {
  uni.navigateTo({
    url: '/pages-merchant/profile/edit-phone'
  })
}

/**
 * 修改登录密码
 */
const editPassword = () => {
  uni.navigateTo({
    url: '/pages-merchant/profile/edit-password?type=login'
  })
}

/**
 * 修改支付密码
 */
const editPayPassword = () => {
  if (userInfo.value.hasPayPassword) {
    uni.navigateTo({
      url: '/pages-merchant/profile/edit-password?type=pay'
    })
  } else {
    uni.navigateTo({
      url: '/pages-merchant/profile/set-pay-password'
    })
  }
}

/**
 * 切换通知
 */
const toggleNotification = (type, e) => {
  const value = e.detail.value
  notificationSettings.value[type] = value

  uni.showToast({
    title: value ? '已开启' : '已关闭',
    icon: 'success'
  })

  // TODO: 调用API保存设置
  saveSettings()
}

/**
 * 切换深色模式
 */
const toggleDarkMode = (e) => {
  const value = e.detail.value
  generalSettings.value.darkMode = value

  uni.showToast({
    title: value ? '已开启深色模式' : '已关闭深色模式',
    icon: 'success'
  })

  // TODO: 调用API保存设置
  saveSettings()
}

/**
 * 切换自动更新
 */
const toggleAutoUpdate = (e) => {
  const value = e.detail.value
  generalSettings.value.autoUpdate = value

  uni.showToast({
    title: value ? '已开启自动更新' : '已关闭自动更新',
    icon: 'success'
  })

  // TODO: 调用API保存设置
  saveSettings()
}

/**
 * 选择语言
 */
const selectLanguage = () => {
  const languages = ['简体中文', 'English', '日本語']
  uni.showActionSheet({
    itemList: languages,
    success: (res) => {
      const language = languages[res.tapIndex]
      uni.showToast({
        title: `已切换到${language}`,
        icon: 'success'
      })
      // TODO: 调用API保存语言设置
    }
  })
}

/**
 * 清除缓存
 */
const clearCache = () => {
  uni.showModal({
    title: '清除缓存',
    content: '确定清除所有缓存吗？清除后需要重新加载数据。',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '清除中...'
        })

        setTimeout(() => {
          uni.hideLoading()
          cacheSize.value = '0MB'
          uni.showToast({
            title: '清除成功',
            icon: 'success'
          })
          // TODO: 实际清除缓存逻辑
        }, 1500)
      }
    }
  })
}

/**
 * 查看教程
 */
const viewTutorials = () => {
  uni.navigateTo({
    url: '/pages-merchant/profile/tutorials'
  })
}

/**
 * 联系客服
 */
const contactSupport = () => {
  uni.showActionSheet({
    itemList: ['在线客服', '电话客服'],
    success: (res) => {
      if (res.tapIndex === 0) {
        // 打开在线客服
        uni.navigateTo({
          url: '/pages-merchant/chat/index?type=service'
        })
      } else {
        // 拨打客服电话
        uni.makePhoneCall({
          phoneNumber: '400-123-4567'
        })
      }
    }
  })
}

/**
 * 关于我们
 */
const viewAbout = () => {
  uni.navigateTo({
    url: '/pages-merchant/profile/about'
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
    uni.showModal({
      title: '提示',
      content: '当前已是最新版本',
      showCancel: false
    })
  }, 1500)
}

/**
 * 保存设置
 */
const saveSettings = () => {
  // TODO: 调用API保存设置
  // await merchantApi.updateSettings({
  //   notificationSettings: notificationSettings.value,
  //   generalSettings: generalSettings.value
  // })
}

/**
 * 退出登录
 */
const logout = () => {
  uni.showModal({
    title: '退出登录',
    content: '确定退出登录吗？',
    confirmColor: '#F5222D',
    success: (res) => {
      if (res.confirm) {
        // TODO: 清除登录状态
        // 清除token
        uni.removeStorageSync('token')

        uni.showToast({
          title: '已退出登录',
          icon: 'success'
        })

        setTimeout(() => {
          uni.reLaunch({
            url: '/pages-merchant/login/index'
          })
        }, 1500)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.settings-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

/* 设置分组 */
.settings-group {
  margin-bottom: 20rpx;
}

.group-title {
  font-size: 26rpx;
  color: #999;
  padding: 20rpx 30rpx 15rpx;
}

.settings-list {
  background: #fff;
}

.settings-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.item-label {
  font-size: 28rpx;
  color: #333;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.item-value {
  font-size: 26rpx;
  color: #999;

  &.unbound {
    color: #FAAD14;
  }
}

/* 退出登录 */
.logout-section {
  padding: 40rpx 30rpx;
}

.logout-button {
  width: 100%;
  height: 90rpx;
  background: #fff;
  color: #F5222D;
  font-size: 32rpx;
  border-radius: 45rpx;
  border: none;
  @include flex-center;
}
</style>
