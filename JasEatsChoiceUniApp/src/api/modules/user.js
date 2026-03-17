import { get, post, put, del } from '@/utils/request'

/**
 * 用户相关API
 */
export const userApi = {
  /**
   * 用户登录
   * @param {Object} data - 登录数据
   * @param {string} data.phone - 手机号
   * @param {string} data.code - 验证码
   * @param {string} data.password - 密码
   */
  login: (data) => post('/api/user/login', data),

  /**
   * 用户注册
   * @param {Object} data - 注册数据
   * @param {string} data.phone - 手机号
   * @param {string} data.code - 验证码
   * @param {string} data.password - 密码
   * @param {Object} data.profile - 用户资料
   */
  register: (data) => post('/api/user/register', data),

  /**
   * 发送验证码
   * @param {string} phone - 手机号
   */
  sendCode: (phone) => post('/api/user/send-code', { phone }),

  /**
   * 微信授权登录
   * @param {Object} data - 微信登录数据
   * @param {string} data.code - 微信code
   * @param {string} data.encryptedData - 加密数据
   * @param {string} data.iv - 加密算法的初始向量
   */
  wechatLogin: (data) => post('/api/user/wechat-login', data),

  /**
   * 获取用户信息
   */
  getUserInfo: () => get('/api/user/info'),

  /**
   * 更新用户信息
   * @param {Object} data - 用户信息
   */
  updateUserInfo: (data) => put('/api/user/info', data),

  /**
   * 修改密码
   * @param {Object} data - 密码数据
   * @param {string} data.oldPassword - 旧密码
   * @param {string} data.newPassword - 新密码
   */
  changePassword: (data) => post('/api/user/change-password', data),

  /**
   * 重置密码
   * @param {Object} data - 重置数据
   * @param {string} data.phone - 手机号
   * @param {string} data.code - 验证码
   * @param {string} data.newPassword - 新密码
   */
  resetPassword: (data) => post('/api/user/reset-password', data),

  /**
   * 上传头像
   * @param {FormData} formData - 头像文件
   */
  uploadAvatar: (formData) => post('/api/user/upload-avatar', formData),

  /**
   * 获取用户统计数据
   */
  getUserStats: () => get('/api/user/stats'),

  /**
   * 完善身体数据
   * @param {Object} data - 身体数据
   * @param {number} data.height - 身高(cm)
   * @param {number} data.weight - 体重(kg)
   * @param {string} data.goal - 饮食目标
   * @param {Array} data.preferences - 饮食偏好
   */
  completeProfile: (data) => post('/api/user/complete-profile', data)
}
