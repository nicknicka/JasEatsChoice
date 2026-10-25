<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useUserStore, useLocationStore, useCartStore } from '@/store'

const isDev = process.env.NODE_ENV !== 'production'

const logInfo = (...args) => {
  if (isDev) {
    console.log(...args)
  }
}

const createStoreSafely = (factory, storeName) => {
  if (typeof factory !== 'function') {
    console.error(`❌ ${storeName} store 工厂不可用`)
    return null
  }

  try {
    return factory()
  } catch (error) {
    console.error(`❌ ${storeName} store 创建失败:`, error)
    return null
  }
}

// 获取所有 stores
const userStore = createStoreSafely(useUserStore, 'User')
const locationStore = createStoreSafely(useLocationStore, 'Location')
const cartStore = createStoreSafely(useCartStore, 'Cart')

/**
 * 应用启动（只触发一次）
 */
onLaunch(() => {
  logInfo('App Launch')

  // 初始化所有 stores（必须在 uni 对象就绪后调用）
  initStores()

  // 检查登录状态
  checkLoginStatus()

  // 获取系统信息
  getSystemInfo()

  // 初始化配置
  initConfig()
})

/**
 * 应用显示（从后台进入前台）
 */
onShow(() => {
  logInfo('App Show')

  // 刷新用户信息
  if (userStore?.isLogin) {
    userStore.fetchUserInfo().catch(err => {
      console.error('刷新用户信息失败:', err)
    })
  }
})

/**
 * 应用隐藏（从前台进入后台）
 */
onHide(() => {
  logInfo('App Hide')
})

/**
 * 初始化所有 stores
 * 必须在 uni 对象就绪后调用
 */
const initStores = () => {
  try {
    logInfo('🔄 初始化所有 stores...')

    // 初始化 user store
    userStore?.initialize?.()

    // 初始化 location store
    locationStore?.initialize?.()

    // 初始化 cart store
    cartStore?.initialize?.()

    logInfo('✅ 所有 stores 初始化完成')
  } catch (error) {
    console.error('❌ Stores 初始化失败:', error)
  }
}

/**
 * 检查登录状态
 */
const checkLoginStatus = () => {
  try {
    if (!userStore) {
      console.warn('User store 不可用，跳过登录态检查')
      return
    }

    // token 已经在 userStore.initialize() 中恢复
    if (userStore.isLogin && userStore.token) {
      logInfo('检测到已登录用户')

      // 获取用户信息
      userStore.fetchUserInfo().catch(err => {
        console.error('获取用户信息失败:', err)
        // Token可能已过期，清除登录状态
        userStore.logout()
      })
    } else {
      logInfo('未登录')
    }
  } catch (error) {
    console.error('检查登录状态失败:', error)
  }
}

/**
 * 获取系统信息
 * 注意：微信小程序已废弃 wx.getSystemInfoSync，改用以下新 API：
 * - wx.getWindowInfo() - 获取窗口信息
 * - wx.getSystemSetting() - 获取系统设置
 * - wx.getDeviceInfo() - 获取设备信息
 * - wx.getAppBaseInfo() - 获取应用基础信息
 */
const getSystemInfo = () => {
  try {
    let systemInfo

    // #ifdef MP-WEIXIN
    // 微信小程序使用新 API
    const windowInfo = uni.getWindowInfo()
    const deviceInfo = uni.getDeviceInfo()
    const appBaseInfo = uni.getAppBaseInfo()

    // 合并信息
    systemInfo = {
      ...windowInfo,
      ...deviceInfo,
      ...appBaseInfo
    }
    // #endif

    // #ifndef MP-WEIXIN
    // 其他平台继续使用旧 API
    systemInfo = uni.getSystemInfoSync()
    // #endif

    logInfo('系统信息:', systemInfo)

    // 存储系统信息到本地
    uni.setStorageSync('systemInfo', systemInfo)

    // 存储状态栏高度和平台信息供全局使用
    const { statusBarHeight, platform } = systemInfo
    uni.setStorageSync('statusBarHeight', statusBarHeight)
    uni.setStorageSync('platform', platform)

  } catch (error) {
    console.error('获取系统信息失败:', error)

    // 如果新 API 失败，降级使用旧 API
    try {
      const systemInfo = uni.getSystemInfoSync()
      logInfo('使用降级方案获取系统信息:', systemInfo)
      uni.setStorageSync('systemInfo', systemInfo)
      uni.setStorageSync('statusBarHeight', systemInfo.statusBarHeight)
      uni.setStorageSync('platform', systemInfo.platform)
    } catch (fallbackError) {
      console.error('降级方案也失败了:', fallbackError)
    }
  }
}

/**
 * 初始化配置
 */
const initConfig = () => {
  // 可以在这里初始化一些全局配置
  logInfo('初始化配置')
}

</script>

<style lang="scss">
/* 引入全局样式 */
@import '@/styles/common.scss';
@import '@/styles/variables.scss';

/* 全局样式重置 */
page {
  background-color: $bg-color-base;
  font-size: $font-size-base;
  line-height: $line-height-base;
  color: $text-color-primary;
}

/* 全局链接样式 */
.link {
  color: $primary-color;
  text-decoration: underline;
}

/* 全局按钮样式重置 */
button {
  &::after {
    border: none;
  }
}

/* 全局输入框样式重置 */
input,
textarea {
  caret-color: $primary-color;
}

/* 全局滚动条隐藏 */
::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
  color: transparent;
}
</style>
