/**
 * 商家相关API
 * 对接后端 MerchantController 和 MerchantsController
 * 基础路径: /v1/merchants, /v1/merchant
 */
import { get, post, put, del } from '@/utils/request'
import { MERCHANT_API, buildUrl } from '../urlEnum'

export const merchantApi = {
  // ==================== 商家认证 ====================

  /**
   * 商家登录
   * POST /v1/merchant/login
   * @param {Object} data - 登录数据
   * @param {string} data.username - 用户名/手机号
   * @param {string} data.password - 密码
   * @returns {Promise} 返回登录结果
   */
  login: (data) => post(MERCHANT_API.MERCHANT_LOGIN, data),

  /**
   * 商家注册
   * POST /v1/merchant/register
   * @param {Object} data - 注册数据
   * @returns {Promise} 返回注册结果
   */
  register: (data) => post(MERCHANT_API.MERCHANT_REGISTER, data),

  /**
   * 商家退出登录
   * POST /v1/merchant/logout
   * @returns {Promise} 返回退出结果
   */
  logout: () => post(MERCHANT_API.MERCHANT_LOGOUT),

  // ==================== 商家列表 ====================

  /**
   * 获取商家列表
   * GET /v1/merchants
   * @param {Object} params - 查询参数
   * @param {string} params.keyword - 搜索关键词
   * @param {string} params.category - 分类
   * @param {string} params.sort - 排序方式(distance/rating/sales)
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回商家列表
   */
  getList: (params) => get(MERCHANT_API.GET_MERCHANTS, params),

  /**
   * 获取商家详情
   * GET /v1/merchants/{merchantId}
   * @param {string} merchantId - 商家ID
   * @returns {Promise} 返回商家详情
   */
  getDetail: (merchantId) => get(buildUrl(MERCHANT_API.GET_MERCHANT_DETAIL, { merchantId })),

  /**
   * 获取附近商家
   * GET /v1/merchants/nearby
   * @param {Object} params - 查询参数
   * @param {number} params.latitude - 纬度（必须是 Double 类型）
   * @param {number} params.longitude - 经度（必须是 Double 类型）
   * @param {number} params.radius - 半径(米)
   * @param {number} params.limit - 数量限制
   * @returns {Promise} 返回附近商家列表
   */
  getNearby: (params) => {
    // 确保数值类型正确（后端要求 Double 类型）
    const processedParams = {}
    if (params) {
      if (params.latitude !== undefined && params.latitude !== null) {
        processedParams.latitude = Number(params.latitude)
      }
      if (params.longitude !== undefined && params.longitude !== null) {
        processedParams.longitude = Number(params.longitude)
      }
      if (params.radius !== undefined && params.radius !== null) {
        processedParams.radius = Number(params.radius)
      }
      if (params.limit !== undefined && params.limit !== null) {
        processedParams.limit = Number(params.limit)
      }
    }
    return get(MERCHANT_API.GET_NEARBY_MERCHANTS, processedParams)
  },

  // ==================== 商家信息 ====================

  /**
   * 获取商家信息（当前登录商家）
   * GET /v1/merchant/info
   * @returns {Promise} 返回商家信息
   */
  getInfo: () => get(MERCHANT_API.GET_MERCHANT_INFO),

  /**
   * M-001: 获取商家资料
   * GET /v1/merchant/profile
   * @returns {Promise} 返回商家资料
   */
  getProfile: () => get(MERCHANT_API.GET_MERCHANT_PROFILE),

  /**
   * M-002: 保存商家资料
   * PUT /v1/merchant/profile
   * @param {Object} data - 商家资料
   * @param {string} data.avatar - 头像
   * @param {string} data.contactName - 联系人姓名
   * @param {string} data.phone - 联系电话
   * @param {string} data.wechat - 微信号
   * @param {string} data.merchantType - 商户类型
   * @param {string} data.licenseNo - 营业执照号
   * @param {string} data.shopName - 店铺名称
   * @param {string} data.licenseImage - 营业执照图片
   * @param {string} data.bankName - 开户银行
   * @param {string} data.bankAccount - 银行卡号
   * @param {string} data.accountName - 开户人姓名
   * @returns {Promise} 返回保存结果
   */
  updateProfile: (data) => put(MERCHANT_API.UPDATE_MERCHANT_PROFILE, data),

  /**
   * 更新商家信息
   * PUT /v1/merchants/{merchantId}
   * @param {string} merchantId - 商家ID
   * @param {Object} data - 商家信息
   * @returns {Promise} 返回更新结果
   */
  updateInfo: (merchantId, data) => put(buildUrl(MERCHANT_API.UPDATE_MERCHANT_INFO, { merchantId }), data),

  // ==================== 店铺管理 ====================

  /**
   * M-003: 获取店铺信息
   * GET /v1/merchant/shop
   * @returns {Promise} 返回店铺信息
   */
  getShopInfo: () => get(MERCHANT_API.GET_SHOP_INFO),

  /**
   * M-004: 保存店铺信息
   * PUT /v1/merchant/shop
   * @param {Object} data - 店铺信息
   * @param {string} data.avatar - 店铺头像
   * @param {string} data.name - 店铺名称
   * @param {boolean} data.isOpen - 营业状态
   * @param {Array} data.businessHours - 营业时间
   * @param {string} data.phone - 联系电话
   * @param {string} data.address - 店铺地址
   * @param {number} data.latitude - 纬度
   * @param {number} data.longitude - 经度
   * @param {string} data.deliveryRange - 配送范围
   * @param {string} data.deliveryFee - 配送费
   * @param {string} data.minOrderAmount - 起送金额
   * @param {string} data.description - 店铺简介
   * @param {Array} data.images - 店铺图片
   * @returns {Promise} 返回保存结果
   */
  updateShopInfo: (data) => put(MERCHANT_API.UPDATE_SHOP_INFO, data),

  // ==================== 商家设置 ====================

  /**
   * M-005: 获取商家设置
   * GET /v1/merchant/settings
   * @returns {Promise} 返回商家设置
   */
  getSettings: () => get(MERCHANT_API.GET_SETTINGS),

  /**
   * M-006/M-007/M-008: 保存商家设置
   * PUT /v1/merchant/settings
   * @param {Object} data - 设置数据
   * @param {Object} data.notificationSettings - 通知设置
   * @param {Object} data.generalSettings - 通用设置
   * @param {boolean} data.autoAccept - 自动接单
   * @param {Array} data.businessHours - 营业时间
   * @param {Object} data.autoReply - 自动回复
   * @returns {Promise} 返回保存结果
   */
  updateSettings: (data) => put(MERCHANT_API.UPDATE_SETTINGS, data),

  /**
   * M-009: 保存语言设置
   * PUT /v1/merchant/settings/language
   * @param {string} language - 语言代码(zh-CN/en-US/ja-JP)
   * @returns {Promise} 返回保存结果
   */
  updateLanguage: (language) => put(MERCHANT_API.UPDATE_LANGUAGE, { language }),

  // ==================== 商家收藏 ====================

  /**
   * 收藏商家
   * POST /v1/users/{userId}/favorites/merchants
   * @param {string} userId - 用户ID
   * @param {string} merchantId - 商家ID
   * @returns {Promise} 返回收藏结果
   */
  favorite: (userId, merchantId) => post(buildUrl(MERCHANT_API.FAVORITE_MERCHANT, { userId }), { merchantId }),

  /**
   * 取消收藏商家
   * DELETE /v1/users/{userId}/favorites/merchants/{merchantId}
   * @param {string} userId - 用户ID
   * @param {string} merchantId - 商家ID
   * @returns {Promise} 返回取消收藏结果
   */
  unfavorite: (userId, merchantId) => del(buildUrl(MERCHANT_API.UNFAVORITE_MERCHANT, { userId, merchantId })),

  /**
   * 检查是否收藏商家
   * GET /v1/users/{userId}/favorites/merchants/{merchantId}/check
   * @param {string} userId - 用户ID
   * @param {string} merchantId - 商家ID
   * @returns {Promise} 返回收藏状态
   */
  checkFavorite: (userId, merchantId) => get(buildUrl(MERCHANT_API.CHECK_FAVORITE_MERCHANT, { userId, merchantId })),

  // ==================== 商家优惠券 ====================

  /**
   * 获取商家优惠券列表
   * GET /v1/merchants/{merchantId}/coupons
   * @param {string} merchantId - 商家ID
   * @returns {Promise} 返回优惠券列表
   */
  getCoupons: () => Promise.resolve([]),

  // ==================== 商家评价 ====================

  /**
   * 获取商家评价列表
   * GET /v1/merchants/{merchantId}/reviews
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回评价列表
   */
  getReviews: (merchantId, params) => get(buildUrl(MERCHANT_API.GET_MERCHANT_REVIEWS, { merchantId }), params),

  // ==================== 商家统计 ====================

  /**
   * 获取商家统计数据
   * GET /v1/merchants/{merchantId}/statistics
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @param {string} params.timeRange - 时间范围(today/week/month)
   * @returns {Promise} 返回统计数据
   */
  getStatistics: (merchantId, params) => get(buildUrl(MERCHANT_API.GET_MERCHANT_STATISTICS, { merchantId }), params),

  /**
   * 获取商家财务数据
   * GET /v1/merchants/{merchantId}/finance
   * @param {string} merchantId - 商家ID
   * @returns {Promise} 返回财务数据
   */
  getFinance: (merchantId) => get(buildUrl(MERCHANT_API.GET_MERCHANT_FINANCE, { merchantId })),

  /**
   * 商家提现申请
   * POST /v1/merchants/{merchantId}/withdraw
   * @param {string} merchantId - 商家ID
   * @param {Object} data - 提现数据
   * @param {number} data.amount - 提现金额
   * @param {string} data.account - 提现账户
   * @returns {Promise} 返回提现结果
   */
  withdraw: (merchantId, data) => post(buildUrl(MERCHANT_API.MERCHANT_WITHDRAW, { merchantId }), data),

  // ==================== 商家财务（新接口） ====================

  /**
   * M-012: 获取商家财务数据
   * GET /v1/merchant/finance
   * @param {Object} params - 查询参数
   * @param {string} params.timeRange - 时间范围(week/month/year)
   * @returns {Promise} 返回财务数据
   */
  getFinanceData: (params) => get(MERCHANT_API.GET_FINANCE_DATA, params),

  /**
   * M-013: 获取交易记录
   * GET /v1/merchant/finance/transactions
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.type - 类型(income/withdraw)
   * @returns {Promise} 返回交易记录
   */
  getTransactions: (params) => get(MERCHANT_API.GET_TRANSACTIONS, params),

  /**
   * M-015: 获取提现数据
   * GET /v1/merchant/withdraw
   * @returns {Promise} 返回提现数据
   */
  getWithdrawData: () => get(MERCHANT_API.GET_WITHDRAW_DATA),

  /**
   * M-016: 提交提现申请
   * POST /v1/merchant/withdraw
   * @param {Object} data - 提现数据
   * @param {number} data.amount - 提现金额
   * @param {string} data.bankName - 银行名称
   * @param {string} data.bankAccount - 银行卡号
   * @param {string} data.accountName - 开户人姓名
   * @returns {Promise} 返回提现结果
   */
  submitWithdraw: (data) => post(MERCHANT_API.SUBMIT_WITHDRAW, data),

  // ==================== 商家教程 ====================

  /**
   * M-017: 获取教程数据
   * GET /v1/merchant/tutorials
   * @param {Object} params - 查询参数
   * @param {string} params.category - 分类
   * @returns {Promise} 返回教程数据
   */
  getTutorials: (params) => get(MERCHANT_API.GET_TUTORIALS, params),

  /**
   * M-019: 记录反馈
   * POST /v1/merchant/tutorials/{tutorialId}/feedback
   * @param {string} tutorialId - 教程ID
   * @param {Object} data - 反馈数据
   * @param {boolean} data.helpful - 是否有帮助
   * @param {string} data.comment - 评论内容
   * @returns {Promise} 返回反馈结果
   */
  submitTutorialFeedback: (tutorialId, data) => post(buildUrl(MERCHANT_API.SUBMIT_TUTORIAL_FEEDBACK, { tutorialId }), data),

  // ==================== 订单管理相关API ====================

  /**
   * 获取商家订单列表
   * GET /v1/orders/merchant/{merchantId}
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @param {boolean} params.today - 是否只查询今日订单，默认true
   * @returns {Promise} 返回订单列表
   */
  getOrders: (merchantId, params) => get(buildUrl('/v1/orders/merchant/:merchantId', { merchantId }), params),

  /**
   * 获取今日订单
   * GET /v1/orders/merchant/{merchantId}?today=true
   * @param {string} merchantId - 商家ID
   * @returns {Promise} 返回今日订单列表
   */
  getTodayOrders: (merchantId) => get(buildUrl('/v1/orders/merchant/:merchantId', { merchantId }), { today: true }),

  /**
   * 获取订单详情
   * GET /v1/orders/{orderId}
   * @param {string} orderId - 订单ID
   * @returns {Promise} 返回订单详情
   */
  getOrderDetail: (orderId) => get(buildUrl('/v1/orders/:orderId', { orderId })),

  /**
   * 获取订单菜品列表
   * GET /v1/orders/{orderId}/dishes
   * @param {string} orderId - 订单ID
   * @returns {Promise} 返回菜品列表
   */
  getOrderDishes: (orderId) => get(buildUrl('/v1/orders/:orderId/dishes', { orderId })),

  /**
   * 更新订单状态
   * PUT /v1/orders/{orderId}/status?status={status}
   * @param {string} orderId - 订单ID
   * @param {number} status - 订单状态(0-待支付,1-待接单,2-制作中,3-已完成,4-已取消)
   * @returns {Promise} 返回更新结果
   */
  updateOrderStatus: (orderId, status) => put(buildUrl('/v1/orders/:orderId/status', { orderId }), null, { params: { status } }),

  /**
   * 接单 - 将状态更新为2(制作中)
   * PUT /v1/orders/{orderId}/status?status=2
   * @param {string} orderId - 订单ID
   * @returns {Promise} 返回接单结果
   */
  acceptOrder: (orderId) => put(buildUrl('/v1/orders/:orderId/status', { orderId }), null, { params: { status: 2 } }),

  /**
   * 拒单 - 将状态更新为4(已取消)
   * PUT /v1/orders/{orderId}/cancel?reason={reason}
   * @param {string} orderId - 订单ID
   * @param {string} reason - 拒单原因
   * @returns {Promise} 返回拒单结果
   */
  rejectOrder: (orderId, reason) => put(buildUrl('/v1/orders/:orderId/cancel', { orderId }), null, { params: { reason } }),

  /**
   * 完成订单 - 将状态更新为3(已完成)
   * PUT /v1/orders/{orderId}/status?status=3
   * @param {string} orderId - 订单ID
   * @returns {Promise} 返回完成订单结果
   */
  completeOrder: (orderId) => put(buildUrl('/v1/orders/:orderId/status', { orderId }), null, { params: { status: 3 } })
}

export default merchantApi
