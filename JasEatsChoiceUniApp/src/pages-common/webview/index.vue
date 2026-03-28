<template>
  <view class="webview-container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar" :class="{ transparent: isTransparent, solid: isSolid }">
      <view class="nav-back" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <view class="nav-title" :class="{ show: showTitle }">{{ pageTitle || '加载中...' }}</view>
      <view class="nav-actions">
        <view class="action-btn" @click="refreshPage" v-if="loaded">
          <text class="icon">🔄</text>
        </view>
        <view class="action-btn" @click="showMoreMenu">
          <text class="icon">⋮</text>
        </view>
      </view>
    </view>

    <!-- 加载进度条 -->
    <view class="progress-bar" v-if="loading">
      <view class="progress" :style="{ width: progress + '%' }"></view>
    </view>

    <!-- WebView容器 -->
    <web-view
      class="webview"
      :src="webviewUrl"
      @message="handleMessage"
      @load="handleLoad"
      @error="handleError"
    ></web-view>

    <!-- 更多菜单 -->
    <uni-popup ref="morePopup" type="bottom">
      <view class="more-menu">
        <view class="menu-list">
          <view class="menu-item" @click="copyLink">
            <text class="menu-icon">🔗</text>
            <text class="menu-text">复制链接</text>
          </view>
          <view class="menu-item" @click="openInBrowser">
            <text class="menu-icon">🌐</text>
            <text class="menu-text">在浏览器打开</text>
          </view>
          <view class="menu-item" @click="sharePage">
            <text class="menu-icon">📤</text>
            <text class="menu-text">分享页面</text>
          </view>
          <view class="menu-item" @click="showPageInfo">
            <text class="menu-icon">ℹ️</text>
            <text class="menu-text">页面信息</text>
          </view>
          <view class="menu-item danger" @click="clearCache">
            <text class="menu-icon">🗑️</text>
            <text class="menu-text">清除缓存</text>
          </view>
        </view>
        <view class="menu-cancel" @click="closeMoreMenu">
          <text class="cancel-text">取消</text>
        </view>
      </view>
    </uni-popup>

    <!-- 页面信息弹窗 -->
    <uni-popup ref="infoPopup" type="center">
      <view class="page-info">
        <view class="info-header">
          <text class="info-title">页面信息</text>
          <view class="close-btn" @click="closeInfoPopup">✕</view>
        </view>
        <view class="info-content">
          <view class="info-item">
            <text class="info-label">页面标题</text>
            <text class="info-value">{{ pageTitle }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">页面URL</text>
            <text class="info-value url">{{ webviewUrl }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">加载时间</text>
            <text class="info-value">{{ loadTime }}ms</text>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 错误提示 -->
    <view class="error-page" v-if="hasError">
      <view class="error-content">
        <text class="error-icon">❌</text>
        <text class="error-title">页面加载失败</text>
        <text class="error-desc">{{ errorMessage }}</text>
        <view class="error-actions">
          <button class="btn btn-outline" @click="goBack">返回</button>
          <button class="btn btn-primary" @click="refreshPage">重试</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useRouter } from 'uni-mini-router'

const router = useRouter()

// WebView相关
const webviewUrl = ref('')
const pageTitle = ref('')
const loading = ref(true)
const loaded = ref(false)
const progress = ref(0)
const hasError = ref(false)
const errorMessage = ref('')

// 导航栏样式
const isTransparent = ref(false)
const isSolid = ref(false)
const showTitle = ref(false)

// 性能数据
const loadStartTime = ref(0)
const loadTime = ref(0)

const morePopup = ref(null)
const infoPopup = ref(null)

onLoad((options) => {
  // 获取传入的URL
  if (options.url) {
    webviewUrl.value = decodeURIComponent(options.url)
  } else {
    hasError.value = true
    errorMessage.value = '缺少页面URL参数'
  }

  // 获取页面标题（可选）
  if (options.title) {
    pageTitle.value = decodeURIComponent(options.title)
  }

  // 是否透明导航栏
  if (options.transparent === 'true') {
    isTransparent.value = true
  }

  // 开始加载计时
  loadStartTime.value = Date.now()
  simulateProgress()
})

// 模拟加载进度
const simulateProgress = () => {
  let currentProgress = 0
  const interval = setInterval(() => {
    currentProgress += Math.random() * 20
    if (currentProgress >= 90) {
      clearInterval(interval)
      currentProgress = 90
    }
    progress.value = currentProgress
  }, 200)
}

// 处理WebView加载完成
const handleLoad = (e) => {
  loading.value = false
  loaded.value = true
  progress.value = 100
  loadTime.value = Date.now() - loadStartTime.value

  // 更新页面标题
  if (e.detail && e.detail.title) {
    pageTitle.value = e.detail.title
  }

  setTimeout(() => {
    showTitle.value = true
  }, 300)
}

// 处理WebView加载错误
const handleError = (e) => {
  loading.value = false
  hasError.value = true
  errorMessage.value = e.detail?.errorMessage || '网络连接失败，请检查网络设置'
}

// 处理WebView消息
const handleMessage = (e) => {
  console.log('WebView message:', e.detail)
  const { data } = e.detail

  // 处理来自H5页面的消息
  if (data && data.action) {
    switch (data.action) {
      case 'setTitle':
        pageTitle.value = data.title
        break
      case 'close':
        goBack()
        break
      case 'share':
        sharePage()
        break
      default:
        break
    }
  }
}

// 返回上一页
const goBack = () => {
  if (loaded.value) {
    // 检查WebView是否可以后退
    // 这里需要H5页面配合，通过postMessage通信
    // 暂时直接关闭当前页面
    uni.navigateBack({
      delta: 1
    })
  } else {
    uni.navigateBack({
      delta: 1
    })
  }
}

// 刷新页面
const refreshPage = () => {
  hasError.value = false
  loading.value = true
  loaded.value = false
  progress.value = 0
  loadStartTime.value = Date.now()

  // 添加时间戳避免缓存
  const separator = webviewUrl.value.includes('?') ? '&' : '?'
  webviewUrl.value = webviewUrl.value + separator + '_t=' + Date.now()

  simulateProgress()
}

// 显示更多菜单
const showMoreMenu = () => {
  morePopup.value?.open()
}

// 关闭更多菜单
const closeMoreMenu = () => {
  morePopup.value?.close()
}

// 复制链接
const copyLink = () => {
  closeMoreMenu()

  uni.setClipboardData({
    data: webviewUrl.value,
    success: () => {
      uni.showToast({
        title: '链接已复制',
        icon: 'success'
      })
    }
  })
}

// 在浏览器打开
const openInBrowser = () => {
  closeMoreMenu()

  // 复制链接，提示用户在浏览器中打开
  uni.setClipboardData({
    data: webviewUrl.value,
    success: () => {
      uni.showModal({
        title: '提示',
        content: '链接已复制，请在浏览器中粘贴打开',
        showCancel: false
      })
    }
  })
}

// 分享页面
const sharePage = () => {
  closeMoreMenu()

  uni.share({
    provider: 'weixin',
    type: 0,
    title: pageTitle.value || '佳食宜选',
    href: webviewUrl.value,
    success: () => {
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    },
    fail: () => {
      uni.showToast({
        title: '分享失败',
        icon: 'error'
      })
    }
  })
}

// 显示页面信息
const showPageInfo = () => {
  closeMoreMenu()
  infoPopup.value?.open()
}

// 关闭页面信息弹窗
const closeInfoPopup = () => {
  infoPopup.value?.close()
}

// 清除缓存
const clearCache = () => {
  closeMoreMenu()

  uni.showModal({
    title: '清除缓存',
    content: '确定要清除WebView缓存吗？',
    success: (res) => {
      if (res.confirm) {
        // 这里可以调用清除缓存的API
        uni.showToast({
          title: '缓存已清除',
          icon: 'success'
        })
        refreshPage()
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.webview-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.nav-bar {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  position: relative;
  z-index: 100;
  transition: all 0.3s;

  &.transparent {
    background: transparent;
    border-bottom: none;
  }

  &.solid {
    background: #ffffff;
    border-bottom: 1rpx solid #f0f0f0;
  }

  .nav-back {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .icon {
      font-size: 48rpx;
      color: #333333;
    }
  }

  .nav-title {
    flex: 1;
    text-align: center;
    font-size: 32rpx;
    font-weight: bold;
    color: #333333;
    opacity: 0;
    transition: opacity 0.3s;

    &.show {
      opacity: 1;
    }
  }

  .nav-actions {
    display: flex;
    gap: 16rpx;

    .action-btn {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      .icon {
        font-size: 36rpx;
        color: #333333;
      }

      &:active {
        opacity: 0.7;
      }
    }
  }
}

.progress-bar {
  height: 4rpx;
  background: #f0f0f0;
  position: relative;
  overflow: hidden;

  .progress {
    height: 100%;
    background: linear-gradient(90deg, #ff6b6b 0%, #ee5a6f 100%);
    transition: width 0.3s;
  }
}

.webview {
  flex: 1;
  width: 100%;
}

.error-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;

  .error-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 24rpx;
    padding: 48rpx 32rpx;

    .error-icon {
      font-size: 120rpx;
    }

    .error-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333333;
    }

    .error-desc {
      font-size: 26rpx;
      color: #999999;
      text-align: center;
    }

    .error-actions {
      display: flex;
      gap: 24rpx;
      margin-top: 32rpx;

      .btn {
        width: 200rpx;
        height: 72rpx;
        border-radius: 36rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28rpx;
        font-weight: 500;
        border: none;

        &.btn-primary {
          background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
          color: #ffffff;
        }

        &.btn-outline {
          background: #ffffff;
          color: #ff6b6b;
          border: 2rpx solid #ff6b6b;
        }

        &:active {
          opacity: 0.8;
        }
      }
    }
  }
}

.more-menu {
  background: #f5f5f5;

  .menu-list {
    background: #ffffff;
    border-radius: 24rpx 24rpx 0 0;
    padding: 16rpx 0;

    .menu-item {
      display: flex;
      align-items: center;
      gap: 24rpx;
      padding: 28rpx 32rpx;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      &.danger {
        .menu-icon,
        .menu-text {
          color: #ff4d4f;
        }
      }

      .menu-icon {
        font-size: 40rpx;
      }

      .menu-text {
        flex: 1;
        font-size: 30rpx;
        color: #333333;
      }

      &:active {
        background: #f5f5f5;
      }
    }
  }

  .menu-cancel {
    margin-top: 16rpx;
    background: #ffffff;
    padding: 28rpx;
    text-align: center;
    border-radius: 0 0 24rpx 24rpx;

    .cancel-text {
      font-size: 32rpx;
      color: #333333;
    }

    &:active {
      background: #f5f5f5;
    }
  }
}

.page-info {
  width: 560rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;

  .info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32rpx;

    .info-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333333;
    }

    .close-btn {
      width: 48rpx;
      height: 48rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36rpx;
      color: #999999;
    }
  }

  .info-content {
    .info-item {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 16rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .info-label {
        font-size: 26rpx;
        color: #999999;
        flex-shrink: 0;
      }

      .info-value {
        flex: 1;
        font-size: 26rpx;
        color: #333333;
        text-align: right;
        word-break: break-all;

        &.url {
          font-size: 22rpx;
          color: #666666;
        }
      }
    }
  }
}
</style>
