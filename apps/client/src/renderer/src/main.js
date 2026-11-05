import './platform/webShim' // 必须最先执行：浏览器环境下补齐 window.api
import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/css/tokens.css'
import './assets/css/element-plus-overrides.css'
import './assets/css/styles.less'
import './assets/css/component-animations.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import pinia from './store' // 引入 Pinia 实例
import permission from './directives/permission' // 引入权限指令
import { cleanExpiredTokens } from './utils/tokenCleaner' // 引入 token 清理工具
import {
  configureAuthSession,
  handleAuthExpired,
  installGlobalAuthInterceptors
} from './utils/authSession'

const app = createApp(App)

// Register all Element Plus icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用 Pinia
app.use(pinia)
configureAuthSession(router)
installGlobalAuthInterceptors()

// 注册权限指令
app.directive('permission', permission)

// 忽略百分比超出范围的Vue警告
app.config.warnHandler = (msg, vm, trace) => {
  if (!msg.includes('Invalid prop: custom validator check failed for prop "percentage"')) {
    console.warn(msg, vm, trace)
  }
}

// 禁用所有Element Plus相关的控制台警告，仅保留页面上的表单验证提示
const originalWarn = console.warn
console.warn = function (warning, ...args) {
  // 检查是否是Element Plus相关的警告或百分比超出范围的警告
  const isElementPlusWarning =
    // 情况1: ElementPlusError实例 (所有Element Plus错误警告)
    (warning instanceof Error &&
      (warning.name === 'ElementPlusError' || warning.message.includes('ElementPlus'))) ||
    // 情况2: 字符串警告包含Element Plus关键词
    (typeof warning === 'string' &&
      (warning.includes('ElementPlus') || warning.includes('Unexpected mutation of'))) ||
    // 情况3: 对象类型的表单验证警告
    (typeof warning === 'object' &&
      warning !== null &&
      JSON.stringify(warning).match(/(phone|password|captcha|height|weight): Array/))

  // 仅显示非Element Plus的警告
  if (!isElementPlusWarning) {
    originalWarn.apply(console, arguments)
  }
}

app
  .use(ElementPlus, {
    size: 'small',
    zIndex: 3000
  })
  .use(router)
  .mount('#app')

// ==========================================
// 应用启动时的 token 清理（Electron 特殊处理）
// ==========================================

// 等待 DOM 加载完成后执行清理
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initTokenCleanup)
} else {
  // DOM 已经加载完成
  setTimeout(initTokenCleanup, 0)
}

function initTokenCleanup() {
  console.log('[TokenCleanup] 开始检查 token 状态...')

  // 清理过期的 token
  const cleanResult = cleanExpiredTokens()

  if (cleanResult.cleaned) {
    console.log('[TokenCleanup] 已清理过期的 token:', cleanResult.tokens)

    const currentPath = router.currentRoute.value.path
    const expiredUserToken = cleanResult.tokens.includes('auth_token')
    const expiredAdminToken = cleanResult.tokens.includes('admin_token')

    if (currentPath.startsWith('/admin') && expiredAdminToken) {
      console.log('[TokenCleanup] 管理员登录态已过期，跳转到管理员登录页')
      handleAuthExpired('admin')
    } else if (
      (currentPath.startsWith('/user') || currentPath.startsWith('/merchant')) &&
      expiredUserToken
    ) {
      console.log('[TokenCleanup] 用户登录态已过期，跳转到用户登录页')
      handleAuthExpired('user')
    }
  } else {
    console.log('[TokenCleanup] 所有 token 均有效，无需清理')
  }

  // 定期检查 token 状态（每 5 分钟检查一次），发现登录态过期时兜底跳转登录页
  setInterval(
    () => {
      const result = cleanExpiredTokens()
      if (!result.cleaned) return

      console.log('[TokenCleanup] 定期清理发现并清理了过期 token:', result.tokens)

      const currentPath = router.currentRoute.value.path
      if (
        currentPath.startsWith('/admin') &&
        currentPath !== '/admin/login' &&
        result.tokens.includes('admin_token')
      ) {
        console.log('[TokenCleanup] 管理员登录态已过期，跳转到管理员登录页')
        handleAuthExpired('admin')
      } else if (
        (currentPath.startsWith('/user') || currentPath.startsWith('/merchant')) &&
        result.tokens.includes('auth_token')
      ) {
        console.log('[TokenCleanup] 用户登录态已过期，跳转到用户登录页')
        handleAuthExpired('user')
      }
    },
    5 * 60 * 1000
  )
}
