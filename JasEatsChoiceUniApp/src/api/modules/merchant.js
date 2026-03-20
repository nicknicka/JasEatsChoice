/**
 * 商家相关API
 * 对接后端 MerchantController
 * 基础路径: /v1/merchants
 */
import { get, post, put, del } from '@/utils/request'

export const merchantApi = {
  /**
   * 获取商家列表
   * GET /v1/merchants
   * @param {Object} params - 查询参数
   * @param {string} params.keyword - 搜索关键词
   * @param {string} params.category - 分类
   * @param {string} params.sort - 排序方式(distance/rating/sales)
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getList: (params) => get('/v1/merchants', params),

  /**
   * 获取商家详情
   * GET /v1/merchants/{merchantId}
   * @param {string} merchantId - 商家ID
   */
  getDetail: (merchantId) => get(`/v1/merchants/${merchantId}`),

  /**
   * 获取附近商家
   * GET /v1/merchants/nearby
   * @param {Object} params - 查询参数
   * @param {number} params.latitude - 纬度
   * @param {number} params.longitude - 经度
   * @param {number} params.radius - 半径(米)
   * @param {number} params.limit - 数量限制
   */
  getNearby: (params) => get('/v1/merchants/nearby', params),

  /**
   * 商家登录
   * POST /v1/merchant/login
   * @param {Object} data - 登录数据
   * @param {string} data.username - 用户名/手机号
   * @param {string} data.password - 密码
   */
  login: (data) => post('/v1/merchant/login', data),

  /**
   * 获取商家信息（当前登录商家）
   * GET /v1/merchant/info
   */
  getInfo: () => get('/v1/merchant/info'),

  /**
   * 更新商家信息
   * PUT /v1/merchants/{merchantId}
   * @param {string} merchantId - 商家ID
   * @param {Object} data - 商家信息
   */
  updateInfo: (merchantId, data) => put(`/v1/merchants/${merchantId}`, data),

  /**
   * 获取商家优惠券列表
   * GET /v1/merchants/{merchantId}/coupons
   * @param {string} merchantId - 商家ID
   */
  getCoupons: (merchantId) => get(`/v1/merchants/${merchantId}/coupons`),

  /**
   * 获取商家评价列表
   * GET /v1/merchants/{merchantId}/reviews
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getReviews: (merchantId, params) => get(`/v1/merchants/${merchantId}/reviews`, params),

  /**
   * 获取商家统计数据
   * GET /v1/merchants/{merchantId}/statistics
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @param {string} params.timeRange - 时间范围(today/week/month)
   */
  getStatistics: (merchantId, params) => get(`/v1/merchants/${merchantId}/statistics`, params),

  /**
   * 获取商家财务数据
   * GET /v1/merchants/{merchantId}/finance
   * @param {string} merchantId - 商家ID
   */
  getFinance: (merchantId) => get(`/v1/merchants/${merchantId}/finance`),

  /**
   * 商家提现申请
   * POST /v1/merchants/{merchantId}/withdraw
   * @param {string} merchantId - 商家ID
   * @param {Object} data - 提现数据
   * @param {number} data.amount - 提现金额
   * @param {string} data.account - 提现账户
   */
  withdraw: (merchantId, data) => post(`/v1/merchants/${merchantId}/withdraw`, data),

  /**
   * 收藏商家
   * POST /v1/users/{userId}/favorites/merchants
   * @param {string} userId - 用户ID
   * @param {string} merchantId - 商家ID
   */
  favorite: (userId, merchantId) => post(`/v1/users/${userId}/favorites/merchants`, { merchantId }),

  /**
   * 取消收藏商家
   * DELETE /v1/users/{userId}/favorites/merchants/{merchantId}
   * @param {string} userId - 用户ID
   * @param {string} merchantId - 商家ID
   */
  unfavorite: (userId, merchantId) => del(`/v1/users/${userId}/favorites/merchants/${merchantId}`),

  /**
   * 检查是否收藏商家
   * GET /v1/users/{userId}/favorites/merchants/{merchantId}/check
   * @param {string} userId - 用户ID
   * @param {string} merchantId - 商家ID
   */
  checkFavorite: (userId, merchantId) => get(`/v1/users/${userId}/favorites/merchants/${merchantId}/check`),

  // ============= 订单管理相关API =============

  /**
   * 获取商家订单列表
   * GET /v1/orders/merchant/{merchantId}
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @param {boolean} params.today - 是否只查询今日订单，默认true
   */
  getOrders: (merchantId, params) => get(`/v1/orders/merchant/${merchantId}`, params),

  /**
   * 获取今日订单
   * GET /v1/orders/merchant/{merchantId}?today=true
   * @param {string} merchantId - 商家ID
   */
  getTodayOrders: (merchantId) => get(`/v1/orders/merchant/${merchantId}`, { today: true }),

  /**
   * 获取订单详情
   * GET /v1/orders/{orderId}
   * @param {string} orderId - 订单ID
   */
  getOrderDetail: (orderId) => get(`/v1/orders/${orderId}`),

  /**
   * 获取订单菜品列表
   * GET /v1/orders/{orderId}/dishes
   * @param {string} orderId - 订单ID
   */
  getOrderDishes: (orderId) => get(`/v1/orders/${orderId}/dishes`),

  /**
   * 更新订单状态
   * PUT /v1/orders/{orderId}/status?status={status}
   * @param {string} orderId - 订单ID
   * @param {number} status - 订单状态(0-待支付,1-待接单,2-制作中,3-已完成,4-已取消)
   */
  updateOrderStatus: (orderId, status) => put(`/v1/orders/${orderId}/status`, null, { params: { status } }),

  /**
   * 接单 - 将状态更新为2(制作中)
   * PUT /v1/orders/{orderId}/status?status=2
   * @param {string} orderId - 订单ID
   */
  acceptOrder: (orderId) => put(`/v1/orders/${orderId}/status`, null, { params: { status: 2 } }),

  /**
   * 拒单 - 将状态更新为4(已取消)
   * PUT /v1/orders/{orderId}/cancel?reason={reason}
   * @param {string} orderId - 订单ID
   * @param {string} reason - 拒单原因
   */
  rejectOrder: (orderId, reason) => put(`/v1/orders/${orderId}/cancel`, null, { params: { reason } }),

  /**
   * 完成订单 - 将状态更新为3(已完成)
   * PUT /v1/orders/{orderId}/status?status=3
   * @param {string} orderId - 订单ID
   */
  completeOrder: (orderId) => put(`/v1/orders/${orderId}/status`, null, { params: { status: 3 } }),

  // ============= 商家个人中心相关API =============

  /**
   * M-001: 获取商家资料
   * GET /v1/merchant/profile
   */
  getProfile: () => get('/v1/merchant/profile'),

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
   */
  updateProfile: (data) => put('/v1/merchant/profile', data),

  /**
   * M-003: 获取店铺信息
   * GET /v1/merchant/shop
   */
  getShopInfo: () => get('/v1/merchant/shop'),

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
   */
  updateShopInfo: (data) => put('/v1/merchant/shop', data),

  /**
   * M-005: 获取商家设置
   * GET /v1/merchant/settings
   */
  getSettings: () => get('/v1/merchant/settings'),

  /**
   * M-006/M-007/M-008: 保存商家设置
   * PUT /v1/merchant/settings
   * @param {Object} data - 设置数据
   * @param {Object} data.notificationSettings - 通知设置
   * @param {Object} data.generalSettings - 通用设置
   * @param {boolean} data.autoAccept - 自动接单
   * @param {Array} data.businessHours - 营业时间
   * @param {Object} data.autoReply - 自动回复
   */
  updateSettings: (data) => put('/v1/merchant/settings', data),

  /**
   * M-009: 保存语言设置
   * PUT /v1/merchant/settings/language
   * @param {string} language - 语言代码(zh-CN/en-US/ja-JP)
   */
  updateLanguage: (language) => put('/v1/merchant/settings/language', { language }),

  /**
   * M-022: 商家退出登录
   * POST /v1/merchant/logout
   */
  logout: () => post('/v1/merchant/logout'),

  /**
   * M-012: 获取商家财务数据
   * GET /v1/merchant/finance
   * @param {Object} params - 查询参数
   * @param {string} params.timeRange - 时间范围(week/month/year)
   */
  getFinanceData: (params) => get('/v1/merchant/finance', params),

  /**
   * M-013: 获取交易记录
   * GET /v1/merchant/finance/transactions
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.type - 类型(income/withdraw)
   */
  getTransactions: (params) => get('/v1/merchant/finance/transactions', params),

  /**
   * M-015: 获取提现数据
   * GET /v1/merchant/withdraw
   */
  getWithdrawData: () => get('/v1/merchant/withdraw'),

  /**
   * M-016: 提交提现申请
   * POST /v1/merchant/withdraw
   * @param {Object} data - 提现数据
   * @param {number} data.amount - 提现金额
   * @param {string} data.bankName - 银行名称
   * @param {string} data.bankAccount - 银行卡号
   * @param {string} data.accountName - 开户人姓名
   */
  submitWithdraw: (data) => post('/v1/merchant/withdraw', data),

  /**
   * M-017: 获取教程数据
   * GET /v1/merchant/tutorials
   * @param {Object} params - 查询参数
   * @param {string} params.category - 分类
   */
  getTutorials: (params) => get('/v1/merchant/tutorials', params),

  /**
   * M-019: 记录反馈
   * POST /v1/merchant/tutorials/{tutorialId}/feedback
   * @param {string} tutorialId - 教程ID
   * @param {Object} data - 反馈数据
   * @param {boolean} data.helpful - 是否有帮助
   * @param {string} data.comment - 评论内容
   */
  submitTutorialFeedback: (tutorialId, data) => post(`/v1/merchant/tutorials/${tutorialId}/feedback`, data)
}

export default merchantApi
