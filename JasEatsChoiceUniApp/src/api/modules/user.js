/**
 * 用户相关API
 * 对接后端 UserController
 * 基础路径: /v1/users
 */
import { get, post, put, del } from '@/utils/request'
import { USER_API, CAPTCHA_API, buildUrl } from '../urlEnum'

export const userApi = {
  // ==================== 认证相关 ====================

  /**
   * 获取图形验证码
   * GET /v1/captcha/checkCode
   * @returns {Promise} 返回验证码图片和key
   */
  getCaptcha: () => get(CAPTCHA_API.GET_CAPTCHA),

  /**
   * 用户登录（支持验证码和密码两种方式）
   * POST /v1/users/login
   * @param {Object} data - 登录数据
   * @param {string} data.phone - 手机号
   * @param {string} data.code - 验证码（验证码登录时使用）
   * @param {string} data.password - 密码（密码登录时使用）
   * @param {string} data.captcha - 图形验证码
   * @param {string} data.checkCodeKey - 验证码key
   * @returns {Promise} 返回登录结果，包含 token 和用户信息
   */
  login: (data) => post(USER_API.LOGIN, data),

  /**
   * 用户注册
   * POST /v1/users/register
   * @param {Object} data - 注册数据
   * @param {string} data.phone - 手机号
   * @param {string} data.code - 验证码
   * @param {string} data.password - 密码（可选）
   * @param {string} data.nickname - 昵称
   * @param {string} data.avatar - 头像URL
   * @returns {Promise} 返回注册结果
   */
  register: (data) => post(USER_API.REGISTER, data),

  /**
   * 发送验证码
   * POST /v1/users/send-sms-code
   * @param {string} phone - 手机号
   * @returns {Promise} 返回发送结果
   */
  sendCode: (phone) => post(USER_API.SEND_SMS_CODE, { phone }),

  /**
   * 微信授权登录
   * POST /v1/users/wechat-login
   * @param {Object} data - 微信登录数据
   * @param {string} data.code - 微信code
   * @param {string} data.encryptedData - 加密数据
   * @param {string} data.iv - 加密算法的初始向量
   * @returns {Promise} 返回登录结果
   */
  wechatLogin: (data) => post(USER_API.WECHAT_LOGIN, data),

  /**
   * 重置密码
   * POST /v1/users/reset-password
   * @param {Object} data - 重置数据
   * @param {string} data.phone - 手机号
   * @param {string} data.code - 验证码
   * @param {string} data.newPassword - 新密码
   * @returns {Promise} 返回重置结果
   */
  resetPassword: (data) => post(USER_API.RESET_PASSWORD, data),

  // ==================== 用户信息 ====================

  /**
   * 获取用户信息
   * GET /v1/users/{userId}
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回用户信息
   */
  getUserInfo: (userId) => get(buildUrl(USER_API.GET_USER_INFO, { userId })),

  /**
   * 更新用户信息
   * PUT /v1/users/{userId}
   * @param {string} userId - 用户ID
   * @param {Object} data - 用户信息
   * @param {string} data.nickname - 昵称
   * @param {string} data.avatar - 头像URL
   * @param {string} data.gender - 性别
   * @param {string} data.birthday - 生日
   * @param {string} data.signature - 个性签名
   * @returns {Promise} 返回更新结果
   */
  updateUserInfo: (userId, data) => put(buildUrl(USER_API.UPDATE_USER_INFO, { userId }), data),

  /**
   * 修改密码
   * POST /v1/users/{userId}/password
   * @param {string} userId - 用户ID
   * @param {Object} data - 密码数据
   * @param {string} data.oldPassword - 旧密码
   * @param {string} data.newPassword - 新密码
   * @returns {Promise} 返回修改结果
   */
  changePassword: (userId, data) => post(buildUrl(USER_API.CHANGE_PASSWORD, { userId }), data),

  /**
   * 上传头像
   * POST /v1/users/{userId}/avatar/base64
   * @param {string} userId - 用户ID
   * @param {Object} data - 头像数据
   * @param {string} data.avatarBase64 - base64编码的头像
   * @returns {Promise} 返回上传结果
   */
  uploadAvatar: (userId, data) => post(buildUrl(USER_API.UPLOAD_AVATAR, { userId }), data),

  /**
   * 删除用户
   * DELETE /v1/users/{userId}
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回删除结果
   */
  deleteUser: (userId) => del(buildUrl(USER_API.DELETE_USER, { userId })),

  // ==================== 用户统计和目标 ====================

  /**
   * 获取用户统计数据
   * GET /v1/user-statistics/{userId}/overview
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回统计数据
   */
  getUserStats: (userId) => get(`/v1/user-statistics/${userId}/overview`),

  /**
   * 获取用户钱包信息
   * GET /v1/users/{userId}/wallet
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回钱包信息
   */
  getWalletInfo: (userId) => get(`/v1/users/${userId}/wallet`),

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
   * @returns {Promise} 返回保存结果
   */
  completeProfile: (data) => post(USER_API.COMPLETE_PROFILE, data),

  /**
   * 获取用户饮食目标
   * GET /v1/user/goals/{userId}
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回饮食目标
   */
  getUserGoals: (userId) => get(buildUrl(USER_API.GET_USER_GOALS, { userId })),

  // ==================== 收藏相关 ====================

  /**
   * 获取收藏列表
   * GET /v1/users/{userId}/favorites
   * @param {string} userId - 用户ID
   * @param {Object} params - 查询参数
   * @param {string} params.type - 收藏类型(dish/merchant)
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回收藏列表
   */
  getFavorites: (userId, params) => get(buildUrl(USER_API.GET_FAVORITES, { userId }), params),

  /**
   * 添加收藏
   * POST /v1/users/{userId}/favorites
   * @param {string} userId - 用户ID
   * @param {Object} data - 收藏数据
   * @param {string} data.targetType - 目标类型(dish/merchant)
   * @param {string} data.targetId - 目标ID
   * @returns {Promise} 返回添加结果
   */
  addFavorite: (userId, data) => post(buildUrl(USER_API.ADD_FAVORITE, { userId }), data),

  /**
   * 取消收藏
   * DELETE /v1/users/{userId}/favorites/{targetType}/{targetId}
   * @param {string} userId - 用户ID
   * @param {string} targetType - 目标类型(dish/merchant)
   * @param {string} targetId - 目标ID
   * @returns {Promise} 返回取消收藏结果
   */
  deleteFavorite: (userId, targetType, targetId) => del(buildUrl(USER_API.DELETE_FAVORITE, { userId, targetType, targetId })),

  /**
   * 检查是否收藏
   * GET /v1/users/{userId}/favorites/check
   * @param {string} userId - 用户ID
   * @param {Object} params - 查询参数
   * @param {string} params.targetType - 目标类型(dish/merchant)
   * @param {string} params.targetId - 目标ID
   * @returns {Promise} 返回收藏状态
   */
  checkFavorite: (userId, params) => get(buildUrl(USER_API.CHECK_FAVORITE, { userId }), params)
}

export default userApi
