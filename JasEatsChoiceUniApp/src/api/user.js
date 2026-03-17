/**
 * 用户相关API
 */
import request from '@/utils/request'

/**
 * 用户登录
 */
export const login = (data) => {
  return request({
    url: '/api/user/login',
    method: 'POST',
    data
  })
}

/**
 * 手机号登录
 */
export const loginByPhone = (data) => {
  return request({
    url: '/api/user/login/phone',
    method: 'POST',
    data
  })
}

/**
 * 微信授权登录
 */
export const loginByWechat = (data) => {
  return request({
    url: '/api/user/login/wechat',
    method: 'POST',
    data
  })
}

/**
 * 用户注册
 */
export const register = (data) => {
  return request({
    url: '/api/user/register',
    method: 'POST',
    data
  })
}

/**
 * 发送验证码
 */
export const sendCode = (data) => {
  return request({
    url: '/api/user/sms/code',
    method: 'POST',
    data
  })
}

/**
 * 获取用户信息
 */
export const getUserInfo = () => {
  return request({
    url: '/api/user/info',
    method: 'GET'
  })
}

/**
 * 更新用户信息
 */
export const updateUserInfo = (data) => {
  return request({
    url: '/api/user/info',
    method: 'PUT',
    data
  })
}

/**
 * 上传头像
 */
export const uploadAvatar = (data) => {
  return request({
    url: '/api/user/avatar',
    method: 'POST',
    data,
    header: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取用户统计数据
 */
export const getUserStats = () => {
  return request({
    url: '/api/user/stats',
    method: 'GET'
  })
}

/**
 * 实名认证
 */
export const realNameAuth = (data) => {
  return request({
    url: '/api/user/auth',
    method: 'POST',
    data
  })
}

/**
 * 退出登录
 */
export const logout = () => {
  return request({
    url: '/api/user/logout',
    method: 'POST'
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
  logout
}
