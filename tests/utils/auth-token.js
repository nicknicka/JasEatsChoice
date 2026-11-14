// 测试用令牌获取：SecurityConfig 默认拒绝后，E2E 里直连后端的接口调用
// 需要带登录态。这里用固定验证码走 API 登录（dev 栈 CAPTCHA_TEST_FIXED_CODE=8888），
// 进程内缓存令牌，避免每个请求都登录一次。
//
// 用法：
//   const { authedFetch, userToken, adminToken } = require('../utils/auth-token')
//   const response = await authedFetch('/v1/wallet/info/123')
const { TEST_API_BASE } = require('./api')

const API_BASE = TEST_API_BASE.replace(/\/$/, '')
const FIXED_CAPTCHA = process.env.TEST_CAPTCHA_FIXED_CODE || '8888'

const USER = {
  phone: process.env.TEST_USER_PHONE || process.env.TEST_USER || '17322222222',
  password: process.env.TEST_USER_PWD || 'asdasd'
}
const ADMIN = {
  username: process.env.TEST_ADMIN_USER || process.env.TEST_ADMIN || 'admin',
  password: process.env.TEST_ADMIN_PWD || 'admin123'
}

let userTokenCache = null
let adminTokenCache = null

async function postJson(path, body) {
  const response = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify(body)
  })
  const result = await response.json().catch(() => null)
  if (!response.ok || !result || result.success !== true) {
    throw new Error(`POST ${path} 失败（${response.status}）：${result?.message || response.statusText}`)
  }
  return result
}

async function userToken() {
  if (userTokenCache) return userTokenCache
  const result = await postJson('/v1/users/login', {
    phone: USER.phone,
    password: USER.password,
    captcha: FIXED_CAPTCHA,
    // 固定验证码模式下 checkCodeKey 只需非空
    checkCodeKey: 'e2e-fixed-captcha'
  })
  userTokenCache = result.data?.token
  if (!userTokenCache) throw new Error('用户登录未返回 token')
  return userTokenCache
}

async function adminToken() {
  if (adminTokenCache) return adminTokenCache
  const result = await postJson('/admin/login', { username: ADMIN.username, password: ADMIN.password })
  adminTokenCache = result.token || result.data?.token
  if (!adminTokenCache) throw new Error('管理员登录未返回 token')
  return adminTokenCache
}

// 带登录态请求受保护接口；scope='admin' 用管理员令牌。
async function authedFetch(path, options = {}, scope = 'user') {
  const token = scope === 'admin' ? await adminToken() : await userToken()
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      Accept: 'application/json',
      ...(options.headers || {}),
      Authorization: `Bearer ${token}`
    }
  })
  return response
}

async function authedJson(path, options = {}, scope = 'user') {
  const response = await authedFetch(path, options, scope)
  const result = await response.json().catch(() => null)
  if (!response.ok || !result || result.success !== true) {
    throw new Error(`请求 ${path} 失败（${response.status}）：${result?.message || response.statusText}`)
  }
  return result.data
}

module.exports = { authedFetch, authedJson, userToken, adminToken, API_BASE }
