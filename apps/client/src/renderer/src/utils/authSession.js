import axios from 'axios'
import { API_CONFIG } from '../config'
import pinia from '../store'
import { useAuthStore } from '../store/authStore'
import { useUserStore } from '../store/userStore'

const USER_STORAGE_KEYS = [
  'auth_token',
  'auth_userId',
  'auth_phone',
  'auth_merchantId',
  'auth_currentRole',
  'token',
  'userId',
  'phone',
  'userInfo',
  'userAvatar',
  'merchantInfo'
]

const ADMIN_STORAGE_KEYS = ['admin_token', 'admin_info']

const PUBLIC_API_PATHS = [
  '/admin/login',
  '/v1/users/login',
  '/v1/merchant/login',
  '/v1/users/register',
  '/v1/users/send-sms-code',
  '/v1/users/send-email-code',
  '/v1/users/reset-password',
  '/v1/users/wechat-login',
  '/v1/captcha/checkCode',
  '/v1/captcha/verify'
]

const ADMIN_API_PATH = /^\/(?:admin|v1\/admin|v1\/tutorial\/admin)(?:\/|$)/

let router = null
let redirecting = false
let interceptorsInstalled = false

export const configureAuthSession = (nextRouter) => {
  router = nextRouter
}

const normalizeUrl = (url) => {
  if (!url) return ''

  try {
    return new URL(url, window.location.href)
  } catch {
    return null
  }
}

const normalizeAuthPath = (path) => {
  const apiBasePath = normalizeUrl(API_CONFIG.baseURL)?.pathname.replace(/\/$/, '')
  if (apiBasePath && (path === apiBasePath || path.startsWith(`${apiBasePath}/`))) {
    return path.slice(apiBasePath.length) || '/'
  }
  return path
}

export const resolveAuthScope = (url = '') => {
  const path = normalizeUrl(url)?.pathname || String(url).split('?')[0]
  return ADMIN_API_PATH.test(normalizeAuthPath(path)) ? 'admin' : 'user'
}

export const isPublicApi = (url = '') => {
  const path = normalizeUrl(url)?.pathname || String(url).split('?')[0]
  return PUBLIC_API_PATHS.some((publicPath) => path.endsWith(publicPath))
}

const isBackendApiUrl = (url) => {
  const parsed = normalizeUrl(url)
  if (!parsed) return false

  const apiBase = normalizeUrl(API_CONFIG.baseURL)
  if (!apiBase) return false

  return parsed.origin === apiBase.origin && parsed.pathname.startsWith(`${apiBase.pathname.replace(/\/$/, '')}/`)
}

const getTokenForRequest = (url) => {
  return resolveAuthScope(url) === 'admin'
    ? localStorage.getItem('admin_token')
    : localStorage.getItem('auth_token')
}

const resolveRequestUrl = (config) => {
  const url = String(config?.url || '')
  const baseURL = String(config?.baseURL || '')

  if (!url || /^[a-z][a-z\d+\-.]*:/i.test(url) || url.startsWith('//')) {
    return url || baseURL
  }
  if (!baseURL) return url

  return `${baseURL.replace(/\/$/, '')}/${url.replace(/^\//, '')}`
}

const getUserIdFromToken = (token) => {
  try {
    if (!token || typeof token !== 'string') return ''
    const payload = token.split('.')[1]
    if (!payload) return ''
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
    return JSON.parse(atob(padded))?.userId || ''
  } catch {
    return ''
  }
}

export const addAuthorization = (config) => {
  if (!config || isPublicApi(config.url) || config.headers?.Authorization) {
    return config
  }

  const token = getTokenForRequest(config.url)
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
    const userId = getUserIdFromToken(token)
    if (userId) config.headers['X-User-Id'] = userId
  }
  return config
}

export const clearAuthState = (scope = 'user') => {
  if (scope === 'admin') {
    ADMIN_STORAGE_KEYS.forEach((key) => localStorage.removeItem(key))
    return
  }

  const authStore = useAuthStore(pinia)
  const userStore = useUserStore(pinia)

  authStore.clearAuth()
  userStore.clearUserInfo()

  USER_STORAGE_KEYS.forEach((key) => localStorage.removeItem(key))
}

export const safeRedirect = (value, allowedPrefix) => {
  if (
    typeof value !== 'string' ||
    !value.startsWith('/') ||
    value.startsWith('//') ||
    /\\|%2f|%5c/i.test(value)
  ) {
    return ''
  }

  let url
  try {
    url = new URL(value, window.location.href)
  } catch {
    return ''
  }

  const path = url.pathname
  if (path !== allowedPrefix && !path.startsWith(`${allowedPrefix}/`)) {
    return ''
  }

  return `${url.pathname}${url.search}${url.hash}`
}

const getLoginPath = (scope) => {
  return scope === 'admin' ? '/admin/login' : '/login'
}

export const handleAuthExpired = (scope = 'user') => {
  const authScope = scope === 'admin' ? 'admin' : 'user'
  clearAuthState(authScope)

  if (redirecting) return

  const loginPath = getLoginPath(authScope)
  if (!router || router.currentRoute.value.path === loginPath) return

  const currentPath = router.currentRoute.value.fullPath || '/'
  redirecting = true
  router
    .replace({
      path: loginPath,
      query: currentPath === loginPath ? {} : { redirect: currentPath }
    })
    .finally(() => {
      redirecting = false
    })
}

export const installGlobalAuthInterceptors = () => {
  if (interceptorsInstalled) return
  interceptorsInstalled = true

  axios.interceptors.request.use((config) => {
    if (!isBackendApiUrl(resolveRequestUrl(config))) return config
    return addAuthorization(config)
  })

  axios.interceptors.response.use(
    (response) => response,
    (error) => {
      const requestUrl = resolveRequestUrl(error.config)
      if (error.response?.status === 401 && isBackendApiUrl(requestUrl) && !isPublicApi(error.config?.url)) {
        handleAuthExpired(resolveAuthScope(requestUrl))
      }
      return Promise.reject(error)
    }
  )

  const originalFetch = window.fetch.bind(window)
  window.fetch = async (...args) => {
    const requestUrl = typeof args[0] === 'string' ? args[0] : args[0]?.url || ''
    const response = await originalFetch(...args)
    if (response.status === 401 && isBackendApiUrl(requestUrl) && !isPublicApi(requestUrl)) {
      handleAuthExpired(resolveAuthScope(requestUrl))
    }
    return response
  }
}
