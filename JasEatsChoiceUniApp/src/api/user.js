/**
 * 用户相关API
 */
import request from '@/utils/request'

/**
 * 用户登录
 */
export const login = (data) => {
  return request({
    url: '/v1/users/login',
    method: 'POST',
    data
  })
}

/**
 * 手机号登录（验证码登录）
 */
export const loginByPhone = (data) => {
  return request({
    url: '/v1/users/login',
    method: 'POST',
    data
  })
}

/**
 * 微信授权登录
 */
export const loginByWechat = (data) => {
  return request({
    url: '/v1/users/wechat-login',
    method: 'POST',
    data
  })
}

/**
 * 用户注册
 */
export const register = (data) => {
  return request({
    url: '/v1/users/register',
    method: 'POST',
    data
  })
}

/**
 * 发送验证码
 */
export const sendCode = (data) => {
  return request({
    url: '/v1/users/send-sms-code',
    method: 'POST',
    data
  })
}

/**
 * 获取用户信息
 */
export const getUserInfo = (userId) => {
  return request({
    url: `/v1/users/${userId}`,
    method: 'GET'
  })
}

/**
 * 更新用户信息
 */
export const updateUserInfo = (userId, data) => {
  return request({
    url: `/v1/users/${userId}`,
    method: 'PUT',
    data
  })
}

/**
 * 上传头像
 */
export const uploadAvatar = (userId, data) => {
  return request({
    url: `/v1/users/${userId}/avatar/base64`,
    method: 'POST',
    data
  })
}

/**
 * 获取用户统计数据
 */
export const getUserStats = (userId) => {
  return request({
    url: `/v1/users/${userId}/stats`,
    method: 'GET'
  })
}

/**
 * 实名认证
 */
export const realNameAuth = (userId, data) => {
  return request({
    url: `/v1/users/${userId}/auth`,
    method: 'POST',
    data
  })
}

/**
 * 退出登录
 */
export const logout = () => {
  return request({
    url: '/v1/users/logout',
    method: 'POST'
  })
}

/**
 * 获取验证码
 */
export const getCaptcha = () => {
  return request({
    url: '/v1/captcha',
    method: 'GET'
  })
}

export default {
  login,
  loginByPhone,
  loginByWechat,
  register,
  sendCode,
  getUserInfo,
  updateUserInfo,
  uploadAvatar,
  getUserStats,
  realNameAuth,
  logout,
  getCaptcha
}
