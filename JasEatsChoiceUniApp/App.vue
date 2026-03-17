<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useUserStore } from '@/store'

// 获取用户store
const userStore = useUserStore()

/**
 * 应用启动（只触发一次）
 */
onLaunch(() => {
  console.log('App Launch')

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
  console.log('App Show')

  // 刷新用户信息
  if (userStore.isLogin) {
    userStore.fetchUserInfo().catch(err => {
      console.error('刷新用户信息失败:', err)
    })
  }
})

/**
 * 应用隐藏（从前台进入后台）
 */
onHide(() => {
  console.log('App Hide')
})

/**
 * 检查登录状态
 */
const checkLoginStatus = () => {
  try {
    // 从本地存储读取token
    const token = uni.getStorageSync('token')

    if (token) {
      userStore.setToken(token)
      console.log('检测到已登录用户')

      // 获取用户信息
      userStore.fetchUserInfo().catch(err => {
        console.error('获取用户信息失败:', err)
        // Token可能已过期，清除登录状态
        userStore.logout()
      })
    } else {
      console.log('未登录')
    }
  } catch (error) {
    console.error('检查登录状态失败:', error)
  }
}

/**
 * 获取系统信息
 */
const getSystemInfo = () => {
  try {
    const systemInfo = uni.getSystemInfoSync()

    console.log('系统信息:', systemInfo)

    // 存储系统信息到本地
    uni.setStorageSync('systemInfo', systemInfo)

    // 存储状态栏高度和导航栏高度供全局使用
    const { statusBarHeight, platform } = systemInfo
    uni.setStorageSync('statusBarHeight', statusBarHeight)
    uni.setStorageSync('platform', platform)

  } catch (error) {
    console.error('获取系统信息失败:', error)
  }
}

/**
 * 初始化配置
 */
const initConfig = () => {
  // 可以在这里初始化一些全局配置
  console.log('初始化配置')
}
</script>

<style lang="scss">
/* 引入全局样式 */
@import '@/styles/common.scss';

/* 全局样式重置 */
page {
  background-color: #f5f5f5;
  font-size: 28rpx;
  line-height: 1.6;
  color: #333;
}

/* 全局链接样式 */
.link {
  color: #FF6B35;
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
  caret-color: #FF6B35;
}

/* 全局滚动条隐藏 */
::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
  color: transparent;
}
</style>
