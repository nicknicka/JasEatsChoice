/**
 * 用户相关API
 * 对接后端 UserController
 * 基础路径: /v1/user
 */
import { get, post, put, del } from '@/utils/request'

export const userApi = {
  /**
   * 用户登录（验证码）
   * POST /v1/user/login
   * @param {Object} data - 登录数据
   * @param {string} data.phone - 手机号
   * @param {string} data.code - 验证码
   */
  login: (data) => post('/v1/user/login', data),

  /**
   * 用户注册
   * POST /v1/user/register
   * @param {Object} data - 注册数据
   * @param {string} data.phone - 手机号
   * @param {string} data.code - 验证码
   * @param {string} data.password - 密码（可选）
   * @param {string} data.nickname - 昵称
   * @param {string} data.avatar - 头像URL
   */
  register: (data) => post('/v1/user/register', data),

  /**
   * 发送验证码
   * POST /v1/user/send-code
   * @param {string} phone - 手机号
   */
  sendCode: (phone) => post('/v1/user/send-code', { phone }),

  /**
   * 微信授权登录
   * POST /v1/user/wechat-login
   * @param {Object} data - 微信登录数据
   * @param {string} data.code - 微信code
   * @param {string} data.encryptedData - 加密数据
   * @param {string} data.iv - 加密算法的初始向量
   */
  wechatLogin: (data) => post('/v1/user/wechat-login', data),

  /**
   * 获取用户信息
   * GET /v1/users/{userId}
   * @param {string} userId - 用户ID
   */
  getUserInfo: (userId) => get(`/v1/users/${userId}`),

  /**
   * 更新用户信息
   * PUT /v1/users/{userId}
   * @param {string} userId - 用户ID
   * @param {Object} data - 用户信息
   */
  updateUserInfo: (userId, data) => put(`/v1/users/${userId}`, data),

  /**
   * 修改密码
   * POST /v1/user/change-password
   * @param {Object} data - 密码数据
   * @param {string} data.oldPassword - 旧密码
   * @param {string} data.newPassword - 新密码
   */
  changePassword: (data) => post('/v1/user/change-password', data),

  /**
   * 重置密码
   * POST /v1/user/reset-password
   * @param {Object} data - 重置数据
   * @param {string} data.phone - 手机号
   * @param {string} data.code - 验证码
   * @param {string} data.newPassword - 新密码
   */
  resetPassword: (data) => post('/v1/user/reset-password', data),

  /**
   * 上传头像
   * POST /v1/user/avatar
   * @param {FormData} formData - 头像文件
   */
  uploadAvatar: (formData) => post('/v1/user/avatar', formData),

  /**
   * 获取用户统计数据
   * GET /v1/user/stats/{userId}
   * @param {string} userId - 用户ID
   */
  getUserStats: (userId) => get(`/v1/user/stats/${userId}`),

  /**
   * 完善身体数据
   * POST /v1/user/profile
   * @param {Object} data - 身体数据
   * @param {string} data.userId - 用户ID
   * @param {number} data.height - 身高(cm)
   * @param {number} data.weight - 体重(kg)
   * @param {string} data.goal - 饮食目标(lose_weight/maintain/gain_muscle)
   * @param {string} data.activityLevel - 活动水平
   * @param {Array} data.preferences - 饮食偏好标签
   */
  completeProfile: (data) => post('/v1/user/profile', data),

  /**
   * 获取用户饮食目标
   * GET /v1/user/goals/{userId}
   * @param {string} userId - 用户ID
   */
  getUserGoals: (userId) => get(`/v1/user/goals/${userId}`),

  /**
   * 删除用户
   * DELETE /v1/users/{userId}
   * @param {string} userId - 用户ID
   */
  deleteUser: (userId) => del(`/v1/users/${userId}`)
}

export default userApi
