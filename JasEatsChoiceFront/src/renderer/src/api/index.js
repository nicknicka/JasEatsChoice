/**
 * API 接口地址统一管理
 *
 * 使用说明：
 * 1. 所有接口地址在此文件中统一定义
 * 2. 使用 :param 表示路径参数，如 :userId
 * 3. 使用 buildUrl() 函数构建带参数的 URL
 * 4. 按功能模块分组，便于维护
 *
 * 示例：
 * // 基础用法
 * api.get(API.USER.LOGIN, data)
 *
 * // 带路径参数
 * const url = buildUrl(API.USER.PROFILE, { userId: '123' })
 * api.get(url)
 *
 * // 带查询参数
 * const url = buildQueryUrl(API.DISH.LIST, { page: 1, size: 10 })
 * api.get(url)
 */

// ==================== 服务器配置 ====================
// 从集中配置导入，避免硬编码（部署 Nginx 时只需修改 config/index.js）
import { ENV_CONFIG } from '@/config'

export const SERVER_CONFIG = {
  BASE_URL: ENV_CONFIG.baseURL,
  WS_URL: ENV_CONFIG.wsURL,
  WS_CHAT_URL: ENV_CONFIG.wsChatURL,
  TIMEOUT: 30000
}

// ==================== 用户相关 ====================
export const USER_API = {
  // 认证
  LOGIN: '/v1/users/login',
  REGISTER: '/v1/users/register',
  LOGOUT: '/v1/users/logout',
  SEND_SMS_CODE: '/v1/users/send-sms-code',
  SEND_EMAIL_CODE: '/v1/users/send-email-code',
  RESET_PASSWORD: '/v1/users/reset-password',
  WECHAT_LOGIN: '/v1/users/wechat-login',

  // 用户信息
  PROFILE: '/v1/users/:userId',
  UPDATE: '/v1/users/:userId',
  UPDATE_PASSWORD: '/v1/users/:userId/password',
  UPLOAD_AVATAR: '/v1/users/:userId/avatar/base64',
  DELETE: '/v1/users/:userId',

  // 用户偏好
  PREFERENCES: '/v1/users/:userId/preferences',
  FEEDBACK: '/v1/users/feedback',

  // 用户统计
  STATS: '/v1/user-statistics/:userId/overview',
  COMPLETE_PROFILE: '/v1/user/profile',
  GOALS: '/v1/user/goals/:userId'
}

// ==================== 商家相关 ====================
export const MERCHANT_API = {
  // 认证
  LOGIN: '/v1/merchant/login',
  REGISTER: '/v1/merchant/register',
  LOGOUT: '/v1/merchant/logout',

  // 商家列表
  LIST: '/v1/merchants',
  DETAIL: '/v1/merchants/:merchantId',
  NEARBY: '/v1/merchants/nearby',

  // 商家信息
  INFO: '/v1/merchant/info',
  PROFILE: '/v1/merchant/profile',
  UPDATE: '/v1/merchants/:merchantId',

  // 店铺管理
  SHOP: '/v1/merchant/shop',
  MENU: '/v1/menus/merchants/:merchantId/menu',
  COMMENTS: '/v1/merchant/:merchantId/comments',
  ALBUM: '/v1/merchant/:merchantId/album',
  DISCOUNTS: '/v1/merchant/:merchantId/discounts',
  ANNOUNCEMENTS: '/v1/merchant/:merchantId/announcements',
  AVATAR: '/v1/merchant/:merchantId/avatar',
  BUSINESS_OVERVIEW: '/v1/merchant/:merchantId/business-overview',

  // 商家设置
  SETTINGS: '/v1/merchant/settings',
  LANGUAGE: '/v1/merchant/settings/language',

  // 商家财务
  FINANCE: '/v1/merchant/finance',
  TRANSACTIONS: '/v1/merchant/finance/transactions',
  WITHDRAW: '/v1/merchant/withdraw',

  // 商家教程
  TUTORIALS: '/v1/merchant/tutorials',
  TUTORIAL_FEEDBACK: '/v1/merchant/tutorials/:tutorialId/feedback'
}

// ==================== 菜品相关 ====================
export const DISH_API = {
  LIST: '/v1/dishes',
  DETAIL: '/v1/dishes/:dishId',
  RECOMMEND: '/v1/dishes/recommended',
  SEARCH: '/v1/dishes/search',
  CATEGORIES: '/v1/dishes/categories',

  // 菜品管理
  CREATE: '/v1/dishes',
  UPDATE: '/v1/dishes/:dishId',
  DELETE: '/v1/dishes/:dishId',
  UPDATE_STATUS: '/v1/dishes/:dishId/status',
  BATCH_STATUS: '/v1/dishes/batch/status',
  BATCH_DELETE: '/v1/dishes/batch',

  // 步骤模板
  STEP_TEMPLATES: '/v1/dish-step-templates',
  STEP_TEMPLATE_DETAIL: '/v1/dish-step-templates/:id'
}

// ==================== 订单相关 ====================
export const ORDER_API = {
  // 用户订单
  USER_ORDERS: '/v1/orders/user/:userId',
  DETAIL: '/v1/orders/:orderId',
  CREATE: '/v1/orders',
  CANCEL: '/v1/orders/:orderId/cancel',
  UPDATE_STATUS: '/v1/orders/:orderId/status',
  DISHES: '/v1/orders/:orderId/dishes',

  // 商家订单
  MERCHANT_ORDERS: '/v1/orders/merchant/:merchantId',
  ACCEPT: '/v1/orders/:orderId/accept',
  REJECT: '/v1/orders/:orderId/reject',
  COMPLETE: '/v1/orders/:orderId/complete'
}

// ==================== 购物车相关 ====================
export const CART_API = {
  GET: '/v1/cart/:userId',
  ADD_ITEM: '/v1/cart/items',
  UPDATE_ITEM: '/v1/cart/items/:itemId',
  DELETE_ITEM: '/v1/cart/items/:itemId',
  CLEAR: '/v1/cart/:userId/clear'
}

// ==================== 评价相关 ====================
export const REVIEW_API = {
  LIST: '/v1/reviews',
  USER_REVIEWS: '/v1/reviews/user/:userId',
  MERCHANT_REVIEWS: '/v1/reviews/merchant/:merchantId',
  DISH_REVIEWS: '/v1/reviews/dish/:dishId',
  CREATE: '/v1/reviews',
  DELETE: '/v1/reviews/:reviewId'
}

// ==================== 优惠券相关 ====================
export const COUPON_API = {
  USER_COUPONS: '/v1/coupons/user/:userId',
  MERCHANT_COUPONS: '/v1/coupons/merchant/:merchantId',
  AVAILABLE: '/v1/coupons/available',
  CLAIM: '/v1/coupons/:couponId/claim',
  USE: '/v1/coupons/:couponId/use'
}

// ==================== 钱包相关 ====================
export const WALLET_API = {
  INFO: '/v1/wallet/:userId',
  BALANCE: '/v1/wallet/:userId/balance',
  RECHARGE: '/v1/wallet/:userId/recharge',
  WITHDRAW: '/v1/wallet/:userId/withdraw',
  TRANSACTIONS: '/v1/wallet/:userId/transactions'
}

// ==================== 地址相关 ====================
export const ADDRESS_API = {
  LIST: '/v1/addresses',
  DETAIL: '/v1/addresses/:addressId',
  CREATE: '/v1/addresses',
  UPDATE: '/v1/addresses/:addressId',
  DELETE: '/v1/addresses/:addressId',
  SET_DEFAULT: '/v1/addresses/:addressId/default',
  DEFAULT: '/v1/addresses/default'
}

// ==================== 食谱相关 ====================
export const RECIPE_API = {
  TODAY: '/v1/recipe/today',
  MY: '/v1/recipe/my',
  DETAIL: '/v1/recipe/:recipeId',
  CREATE: '/v1/recipe/create',
  UPDATE: '/v1/recipe/:recipeId',
  DELETE: '/v1/recipe/:recipeId',
  RECOMMEND: '/v1/recipe/recommend',
  ALL: '/v1/recipe/all',
  STEPS: '/v1/recipe/:recipeId/steps',
  FAVORITE: '/v1/recipe/:recipeId/favorite',
  SHARE: '/v1/recipe/:recipeId/share',
  NUTRITION: '/v1/recipe/nutrition',
  TOGGLE_FAVORITE: '/v1/recipe/toggle-favorite/:id',
  BATCH_TOGGLE_FAVORITE: '/v1/recipe/batch-toggle-favorite',
  SET_TODAY: '/v1/recipe/:id/set-today',
  UNSET_TODAY: '/v1/recipe/:id/unset-today'
}

// ==================== AI 相关 ====================
export const AI_API = {
  // 对话
  CHAT: '/agent/supervisor/chat',
  STREAM_CHAT: '/agent/supervisor-sse/chat',
  CHAT_LEGACY: '/v1/ai/stream/chat',

  // 聊天历史
  HISTORY: '/v1/ai/chat/history',
  SAVE: '/v1/ai/chat/save',
  CLEAR: '/v1/ai/chat/clear',
  HAS_HISTORY: '/v1/ai/chat/has-history',

  // AI 功能
  RECIPE: '/v1/ai/recipe',
  NUTRIENT: '/v1/ai/nutrient',
  RECOGNIZE_DISH: '/v1/ai/dish-recognize',
  RECOMMEND: '/v1/ai/recommend',
  ANALYZE: '/v1/ai/analyze',
  DISH_DESCRIPTION: '/v1/ai/dish-description'
}

// ==================== 商家AI助手 ====================
export const MERCHANT_AI_API = {
  // 经营洞察
  INSIGHT_METRICS: '/v1/merchant/insight/:merchantId/metrics',
  INSIGHT_TREND: '/v1/merchant/insight/:merchantId/trend',
  INSIGHT_TOP_DISHES: '/v1/merchant/insight/:merchantId/top-dishes',
  INSIGHT_RATING: '/v1/merchant/insight/:merchantId/rating-distribution',
  INSIGHT_AI_SUGGESTIONS: '/v1/merchant/insight/:merchantId/ai-suggestions',
  INSIGHT_FULL: '/v1/merchant/insight/:merchantId/full',

  // 评价回复
  REVIEW_PENDING: '/v1/merchant/review/:merchantId/pending',
  REVIEW_GENERATE_REPLY: '/v1/merchant/review/generate-reply',
  REVIEW_SUBMIT_REPLY: '/v1/merchant/review/submit-reply'
}

// ==================== 聊天相关 ====================
export const CHAT_API = {
  CONVERSATIONS: '/v1/conversations',
  CONVERSATION: '/v1/conversations/:conversationId',
  CREATE_CONVERSATION: '/v1/conversations',
  DELETE_CONVERSATION: '/v1/conversations/:conversationId',

  MESSAGES: '/v1/messages',
  SEND_MESSAGE: '/v1/messages/send',
  DELETE_MESSAGE: '/v1/messages/:messageId',
  MARK_READ: '/v1/messages/:messageId/read'
}

// ==================== 群组相关 ====================
export const GROUP_API = {
  LIST: '/v1/groups/my',
  DETAIL: '/v1/groups/:groupId',
  CREATE: '/v1/groups',
  UPDATE: '/v1/groups/:groupId',
  DELETE: '/v1/groups/:groupId',
  LEAVE: '/v1/groups/:groupId/leave',
  MEMBERS: '/v1/groups/:groupId/members',
  ADD_MEMBER: '/v1/groups/:groupId/members',
  REMOVE_MEMBER: '/v1/groups/:groupId/members/:userId',
  CHECK_MEMBER: '/v1/groups/:groupId/members/:userId/check',
  USER_ROLE: '/v1/groups/:groupId/members/:userId/role'
}

// ==================== 收藏相关 ====================
export const COLLECTION_API = {
  LIST: '/v1/collections',
  LIST_BY_TYPE: '/v1/collections/type',
  ADD: '/v1/collections',
  REMOVE: '/v1/collections',
  CHECK: '/v1/collections/check',
  CLEAR: '/v1/collections/user/:userId',

  // 用户收藏（新版）
  FAVORITES: '/v1/users/:userId/favorites',
  ADD_FAVORITE: '/v1/users/:userId/favorites',
  DELETE_FAVORITE: '/v1/users/:userId/favorites/:targetType/:targetId',
  CHECK_FAVORITE: '/v1/users/:userId/favorites/check'
}

// ==================== 消息通知相关 ====================
export const MESSAGE_API = {
  LIST: '/notifications/user',
  SEND: '/v1/message/send'
}

export const NOTIFICATION_API = {
  LIST: '/v1/notifications',
  DETAIL: '/v1/notifications/:notificationId',
  MARK_READ: '/v1/notifications/:notificationId/read',
  MARK_ALL_READ: '/v1/notifications/read-all',
  DELETE: '/v1/notifications/:notificationId'
}

// ==================== 教程相关 ====================
export const TUTORIAL_API = {
  // 公开接口
  FEATURED: '/v1/tutorial/featured',
  LIST: '/v1/tutorial/list',
  DETAIL: '/v1/tutorial/:id',
  PAGE: '/v1/tutorial/page',

  // 用户接口
  USER_CREATE: '/v1/tutorial/user/create',
  USER_MY: '/v1/tutorial/user/my',
  USER_UPDATE: '/v1/tutorial/user/:id',
  USER_SUBMIT: '/v1/tutorial/user/:id/submit',
  USER_DELETE: '/v1/tutorial/user/:id',

  // 管理员接口
  ADMIN_LIST: '/v1/tutorial/admin/list',
  ADMIN_CREATE: '/v1/tutorial/admin/create',
  ADMIN_PENDING: '/v1/tutorial/admin/pending',
  ADMIN_APPROVE: '/v1/tutorial/admin/:id/approve',
  ADMIN_REJECT: '/v1/tutorial/admin/:id/reject',
  ADMIN_FEATURED: '/v1/tutorial/admin/:id/featured',
  ADMIN_DELETE: '/v1/tutorial/admin/:id',

  // 商家接口
  MERCHANT_CREATE: '/v1/tutorial/merchant/create',
  MERCHANT_MY: '/v1/tutorial/merchant/my',
  MERCHANT_UPDATE: '/v1/tutorial/merchant/:id',
  MERCHANT_SUBMIT: '/v1/tutorial/merchant/:id/submit',
  MERCHANT_DELETE: '/v1/tutorial/merchant/:id'
}

// ==================== 饮食记录相关 ====================
export const DIET_API = {
  LIST: '/calorie-records',
  USER: '/calorie-records/user/:userId',
  DATE: '/calorie-records/user/:userId/date/:date',
  WEEK: '/calorie-records/user/:userId/week',
  CREATE: '/calorie-records',
  UPDATE: '/calorie-records/:id',
  DELETE: '/calorie-records/:id'
}

// ==================== 推荐拒绝相关 ====================
export const RECOMMEND_REJECT_API = {
  ADD: '/v1/recommendations/rejects',
  COUNT: '/v1/recommendations/rejects/count',
  LIST: '/v1/recommendations/rejects/list',
  FREQUENT: '/v1/recommendations/rejects/frequent',
  CLEAR: '/v1/recommendations/rejects'
}

// ==================== 首页相关 ====================
export const HOME_API = {
  HOT_TOPIC: '/v1/home/hot-topic',
  HOT_TOPIC_CLICK: '/v1/home/hot-topic/click',
  HOT_TOPIC_SHARE: '/v1/home/hot-topic/share'
}

// ==================== 天气位置相关 ====================
export const WEATHER_API = {
  CURRENT: '/v1/weather'
}

export const LOCATION_API = {
  CURRENT: '/v1/location',
  CASCADER: '/v1/location/cascader',
  SEARCH: '/v1/location/search',
  GEOCODE: '/v1/location/geocode',
  REVERSE_GEOCODE: '/v1/location/reverse-geocode'
}

// ==================== 品类相关 ====================
export const CATEGORY_API = {
  LIST: '/v1/category/list',
  COMMON: '/v1/category/common'
}

// ==================== 文件上传相关 ====================
export const UPLOAD_API = {
  IMAGE: '/v1/chat/upload-image',
  FILE: '/v1/chat/upload-file'
}

// ==================== 验证码相关 ====================
export const CAPTCHA_API = {
  CHECK_CODE: '/v1/captcha/checkCode',
  VERIFY: '/v1/captcha/verify'
}

// ==================== 支付相关 ====================
export const PAYMENT_API = {
  CREATE: '/v1/payment/create',
  QUERY: '/v1/payment/query',
  REFUND: '/v1/payment/refund',
  CALLBACK: '/v1/payment/callback'
}

// ==================== 管理员相关 ====================
export const ADMIN_API = {
  // 认证
  LOGIN: '/admin/login',
  CURRENT: '/admin/current',
  LIST: '/admin/list',
  CREATE: '/admin/create',
  UPDATE_STATUS: '/admin/:adminId/status',
  RESET_PASSWORD: '/admin/:adminId/password',

  // 统计
  DASHBOARD: '/admin/statistics/dashboard',
  USER_STATS: '/admin/statistics/users',
  ORDER_STATS: '/admin/statistics/orders',
  REVENUE_STATS: '/admin/statistics/revenue',

  // 用户管理
  USER_LIST: '/admin/users',
  USER_DETAIL: '/admin/users/:userId',
  UPDATE_USER_STATUS: '/admin/users/:userId/status',
  DELETE_USER: '/admin/users/:userId',

  // 商家管理
  MERCHANT_LIST: '/admin/merchants',
  MERCHANT_DETAIL: '/admin/merchants/:merchantId',
  AUDIT_MERCHANT: '/admin/merchants/:merchantId/audit',
  UPDATE_MERCHANT_STATUS: '/admin/merchants/:merchantId/status',
  PENDING_MERCHANTS: '/admin/merchants/pending',

  // 订单管理
  ORDER_LIST: '/admin/orders',
  ORDER_DETAIL: '/admin/orders/:orderId',
  UPDATE_ORDER_STATUS: '/admin/orders/:orderId/status',
  ORDER_STATISTICS: '/admin/orders/statistics',
  BATCH_UPDATE_ORDER_STATUS: '/admin/orders/batch/status',

  // 菜品管理
  DISH_LIST: '/admin/dishes',
  DISH_DETAIL: '/admin/dishes/:dishId',
  DISH_AUDIT_LIST: '/admin/dishes/audit',
  DISH_AUDIT_DETAIL: '/admin/dishes/audit/:dishId',
  AUDIT_DISH: '/admin/dishes/:dishId/audit',
  UPDATE_DISH_STATUS: '/admin/dishes/:dishId/status',

  // 财务管理 - 提现
  WITHDRAWAL_LIST: '/admin/finance/withdrawals',
  WITHDRAWAL_DETAIL: '/admin/finance/withdrawals/:id',
  AUDIT_WITHDRAWAL: '/admin/finance/withdrawals/:id/process',
  BATCH_PROCESS_WITHDRAWAL: '/admin/finance/withdrawals/batch/process',
  COMPLETE_WITHDRAWAL: '/admin/finance/withdrawals/:id/complete',
  FAIL_WITHDRAWAL: '/admin/finance/withdrawals/:id/fail',
  WITHDRAWAL_STATISTICS: '/admin/finance/withdrawals/statistics',
  WITHDRAWAL_TREND: '/admin/finance/withdrawals/trend',

  // 财务管理 - 充值
  RECHARGE_LIST: '/admin/finance/recharges',
  RECHARGE_STATS: '/admin/finance/recharges/stats',
  RECHARGE_DETAIL: '/admin/finance/recharges/:rechargeId',

  // 财务管理 - 退款
  REFUND_LIST: '/admin/finance/refunds',
  REFUND_STATS: '/admin/finance/refunds/stats',
  REFUND_DETAIL: '/admin/finance/refunds/:refundId',
  PROCESS_REFUND: '/admin/finance/refunds/:refundId/process',

  // 系统日志
  OPERATION_LOGS: '/admin/logs/operations',
  SYSTEM_LOGS: '/admin/logs/system',
  LOGIN_LOGS: '/admin/logs/login',
  LOG_LIST: '/admin/system/logs',
  LOG_STATISTICS: '/admin/system/logs/statistics',
  CLEAN_LOGS: '/admin/system/logs/clean',

  // 角色管理
  ROLE_LIST: '/admin/roles',
  ALL_ROLES: '/admin/roles/all',
  ROLE_DETAIL: '/admin/roles/:roleId',
  CREATE_ROLE: '/admin/roles',
  UPDATE_ROLE: '/admin/roles/:roleId',
  DELETE_ROLE: '/admin/roles/:roleId',
  ROLE_PERMISSIONS: '/admin/roles/:roleId/permissions',
  ASSIGN_PERMISSIONS: '/admin/roles/:roleId/permissions',

  // 权限管理
  PERMISSION_LIST: '/admin/permissions',
  PERMISSION_TREE: '/admin/permissions/tree',
  TOP_PERMISSIONS: '/admin/permissions/top',
  CHILD_PERMISSIONS: '/admin/permissions/children/:parentId',
  PERMISSION_DETAIL: '/admin/permissions/:permissionId',
  CREATE_PERMISSION: '/admin/permissions',
  UPDATE_PERMISSION: '/admin/permissions/:permissionId',
  DELETE_PERMISSION: '/admin/permissions/:permissionId',

  // 热点管理
  HOT_TOPICS: '/v1/admin/hot-topic',
  HOT_TOPIC_DETAIL: '/v1/admin/hot-topic/detail',
  HOT_TOPIC_CREATE: '/v1/admin/hot-topic/create',
  HOT_TOPIC_UPDATE: '/v1/admin/hot-topic/update',
  HOT_TOPIC_DELETE: '/v1/admin/hot-topic/delete',
  HOT_TOPIC_REVIEW: '/v1/admin/hot-topic/review',
  HOT_TOPIC_BATCH_DELETE: '/v1/admin/hot-topic/batch-delete',
  HOT_TOPIC_STATISTICS: '/v1/admin/hot-topic/statistics',

  // 公告管理
  ANNOUNCEMENTS: '/admin/announcements',
  ANNOUNCEMENT_DETAIL: '/admin/announcements/:id',
  ANNOUNCEMENT_CREATE: '/admin/announcements',
  ANNOUNCEMENT_UPDATE: '/admin/announcements/:id',
  ANNOUNCEMENT_DELETE: '/admin/announcements/:id',
  ANNOUNCEMENT_BATCH_DELETE: '/admin/announcements/batch',
  ANNOUNCEMENT_UPDATE_STATUS: '/admin/announcements/:id/status',
  ANNOUNCEMENT_STATISTICS: '/admin/announcements/statistics',

  // 系统配置
  CONFIG_LIST: '/admin/settings/config',
  CONFIG_GROUPS: '/admin/settings/config/groups',
  CONFIG_BY_GROUP: '/admin/settings/config/group/:configGroup',
  CONFIG_DETAIL: '/admin/settings/config/:configId',
  CREATE_CONFIG: '/admin/settings/config',
  UPDATE_CONFIG: '/admin/settings/config/:configId',
  DELETE_CONFIG: '/admin/settings/config/:configId',
  BATCH_UPDATE_CONFIG: '/admin/settings/config/batch',
  REFRESH_CONFIG_CACHE: '/admin/settings/config/refresh'
}

// ==================== 轮播图相关 ====================
export const BANNER_API = {
  LIST: '/v1/banners',
  DETAIL: '/v1/banners/:bannerId',
  CREATE: '/v1/banners',
  UPDATE: '/v1/banners/:bannerId',
  DELETE: '/v1/banners/:bannerId',
  UPDATE_STATUS: '/v1/banners/:bannerId/status'
}

// ==================== 历史记录相关 ====================
export const HISTORY_API = {
  BROWSE: '/v1/history/browse',
  SEARCH: '/v1/history/search',
  CLEAR: '/v1/history/clear'
}

// ==================== 愿望清单相关 ====================
export const WISHLIST_API = {
  LIST: '/v1/wishlist',
  ADD: '/v1/wishlist',
  DELETE: '/v1/wishlist/:wishId',
  COMPLETE: '/v1/wishlist/:wishId/complete'
}

// ==================== 拼单相关 ====================
export const GROUP_ORDER_API = {
  LIST: '/v1/group-orders',
  DETAIL: '/v1/group-orders/:groupOrderId',
  CREATE: '/v1/group-orders',
  JOIN: '/v1/group-orders/:groupOrderId/join',
  LEAVE: '/v1/group-orders/:groupOrderId/leave',
  UPDATE: '/v1/group-orders/:groupOrderId',
  DELETE: '/v1/group-orders/:groupOrderId'
}

// ==================== 反馈相关 ====================
export const FEEDBACK_API = {
  LIST: '/v1/feedback',
  CREATE: '/v1/feedback',
  DELETE: '/v1/feedback/:feedbackId'
}

// ==================== 辅助函数 ====================

/**
 * 构建带路径参数的 URL
 * @param {string} url - URL 模板，如 '/v1/users/:userId'
 * @param {Object} params - 路径参数对象，如 { userId: '123' }
 * @returns {string} 完整 URL，如 '/v1/users/123'
 *
 * @example
 * buildUrl(API.USER.PROFILE, { userId: '123' })
 * // 返回: '/v1/users/123'
 *
 * buildUrl(API.ORDER.DETAIL, { orderId: '456' })
 * // 返回: '/v1/orders/456'
 */
export const buildUrl = (url, params = {}) => {
  let result = url
  Object.keys(params).forEach(key => {
    result = result.replace(`:${key}`, params[key])
  })
  return result
}

/**
 * 构建带查询参数的 URL
 * @param {string} url - 基础 URL
 * @param {Object} params - 查询参数对象
 * @returns {string} 带查询参数的 URL
 *
 * @example
 * buildQueryUrl(API.DISH.LIST, { page: 1, size: 10 })
 * // 返回: '/v1/dishes?page=1&size=10'
 */
export const buildQueryUrl = (url, params = {}) => {
  const query = Object.keys(params)
    .filter(key => params[key] !== undefined && params[key] !== null)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
  return query ? `${url}?${query}` : url
}

/**
 * 构建完整的请求 URL（带路径参数和查询参数）
 * @param {string} url - URL 模板
 * @param {Object} pathParams - 路径参数
 * @param {Object} queryParams - 查询参数
 * @returns {string} 完整 URL
 *
 * @example
 * buildFullUrl(API.ORDER.DETAIL, { orderId: '123' }, { includeDishes: true })
 * // 返回: '/v1/orders/123?includeDishes=true'
 */
export const buildFullUrl = (url, pathParams = {}, queryParams = {}) => {
  const urlWithPathParams = buildUrl(url, pathParams)
  return buildQueryUrl(urlWithPathParams, queryParams)
}

// ==================== 统一导出 ====================

/**
 * API 接口地址统一枚举
 * 所有接口地址都在此对象中定义，便于维护和管理
 */
const API = {
  // 服务器配置
  SERVER: SERVER_CONFIG,

  // 业务接口
  USER: USER_API,
  MERCHANT: MERCHANT_API,
  DISH: DISH_API,
  ORDER: ORDER_API,
  CART: CART_API,
  REVIEW: REVIEW_API,
  COUPON: COUPON_API,
  WALLET: WALLET_API,
  ADDRESS: ADDRESS_API,
  RECIPE: RECIPE_API,
  AI: AI_API,
  MERCHANT_AI: MERCHANT_AI_API,
  CHAT: CHAT_API,
  GROUP: GROUP_API,
  COLLECTION: COLLECTION_API,
  MESSAGE: MESSAGE_API,
  NOTIFICATION: NOTIFICATION_API,
  TUTORIAL: TUTORIAL_API,
  DIET: DIET_API,
  RECOMMEND_REJECT: RECOMMEND_REJECT_API,
  HOME: HOME_API,
  WEATHER: WEATHER_API,
  LOCATION: LOCATION_API,
  CATEGORY: CATEGORY_API,
  UPLOAD: UPLOAD_API,
  CAPTCHA: CAPTCHA_API,
  PAYMENT: PAYMENT_API,
  ADMIN: ADMIN_API,
  BANNER: BANNER_API,
  HISTORY: HISTORY_API,
  WISHLIST: WISHLIST_API,
  GROUP_ORDER: GROUP_ORDER_API,
  FEEDBACK: FEEDBACK_API
}

export default API
