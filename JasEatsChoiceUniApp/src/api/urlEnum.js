/**
 * API 接口地址统一枚举
 * 统一管理所有后端接口地址，便于维护和版本控制
 *
 * 使用说明：
 * 1. 所有接口地址使用大写命名
 * 2. 使用 :param 表示路径参数，如 :id
 * 3. 按功能模块分组
 * 4. 注释中标注请求方法和参数说明
 */

// ==================== 用户相关 ====================
export const USER_API = {
  // 认证相关
  LOGIN: '/v1/users/login',                              // POST - 用户登录（验证码/密码）
  REGISTER: '/v1/users/register',                        // POST - 用户注册
  SEND_SMS_CODE: '/v1/users/send-sms-code',              // POST - 发送验证码
  WECHAT_LOGIN: '/v1/users/wechat-login',                // POST - 微信授权登录
  RESET_PASSWORD: '/v1/users/reset-password',            // POST - 重置密码

  // 用户信息
  GET_USER_INFO: '/v1/users/:userId',                    // GET - 获取用户信息
  UPDATE_USER_INFO: '/v1/users/:userId',                 // PUT - 更新用户信息
  CHANGE_PASSWORD: '/v1/users/:userId/password',         // POST - 修改密码
  UPLOAD_AVATAR: '/v1/users/:userId/avatar/base64',      // POST - 上传头像
  DELETE_USER: '/v1/users/:userId',                      // DELETE - 删除用户

  // 用户统计和目标
  GET_USER_STATS: '/v1/user-statistics/:userId/overview', // GET - 获取用户统计数据
  COMPLETE_PROFILE: '/v1/user/profile',                  // POST - 完善身体数据
  GET_USER_GOALS: '/v1/user/goals/:userId',              // GET - 获取用户饮食目标

  // 收藏相关
  GET_FAVORITES: '/v1/users/:userId/favorites',          // GET - 获取收藏列表
  ADD_FAVORITE: '/v1/users/:userId/favorites',           // POST - 添加收藏
  DELETE_FAVORITE: '/v1/users/:userId/favorites/:targetType/:targetId',  // DELETE - 取消收藏
  CHECK_FAVORITE: '/v1/users/:userId/favorites/check'    // GET - 检查是否收藏
}

// ==================== 商家相关 ====================
export const MERCHANT_API = {
  // 商家认证
  MERCHANT_LOGIN: '/v1/merchant/login',                  // POST - 商家登录
  MERCHANT_REGISTER: '/v1/merchant/register',            // POST - 商家注册
  MERCHANT_LOGOUT: '/v1/merchant/logout',                // POST - 商家退出登录

  // 商家列表
  GET_MERCHANTS: '/v1/merchants',                        // GET - 获取商家列表
  GET_MERCHANT_DETAIL: '/v1/merchants/:merchantId',      // GET - 获取商家详情
  GET_NEARBY_MERCHANTS: '/v1/merchants/nearby',          // GET - 获取附近商家

  // 商家信息
  GET_MERCHANT_INFO: '/v1/merchant/info',                // GET - 获取商家信息（当前登录）
  GET_MERCHANT_PROFILE: '/v1/merchant/profile',          // GET - M-001 获取商家资料
  UPDATE_MERCHANT_PROFILE: '/v1/merchant/profile',       // PUT - M-002 保存商家资料
  UPDATE_MERCHANT_INFO: '/v1/merchants/:merchantId',     // PUT - 更新商家信息

  // 店铺管理
  GET_SHOP_INFO: '/v1/merchant/shop',                    // GET - M-003 获取店铺信息
  UPDATE_SHOP_INFO: '/v1/merchant/shop',                 // PUT - M-004 保存店铺信息

  // 商家设置
  GET_SETTINGS: '/v1/merchant/settings',                 // GET - M-005 获取商家设置
  UPDATE_SETTINGS: '/v1/merchant/settings',              // PUT - 保存商家设置
  UPDATE_LANGUAGE: '/v1/merchant/settings/language',     // PUT - M-009 保存语言设置

  // 商家收藏
  FAVORITE_MERCHANT: '/v1/users/:userId/favorites/merchants',  // POST - 收藏商家
  UNFAVORITE_MERCHANT: '/v1/users/:userId/favorites/merchants/:merchantId',  // DELETE - 取消收藏
  CHECK_FAVORITE_MERCHANT: '/v1/users/:userId/favorites/merchants/:merchantId/check',  // GET - 检查是否收藏

  // 商家优惠券
  GET_MERCHANT_COUPONS: '/v1/merchants/:merchantId/coupons',  // GET - 获取商家优惠券列表

  // 商家评价
  GET_MERCHANT_REVIEWS: '/v1/merchants/:merchantId/reviews',  // GET - 获取商家评价列表

  // 商家统计
  GET_MERCHANT_STATISTICS: '/v1/merchants/:merchantId/statistics',  // GET - 获取商家统计数据
  GET_MERCHANT_FINANCE: '/v1/merchants/:merchantId/finance',  // GET - 获取商家财务数据
  MERCHANT_WITHDRAW: '/v1/merchants/:merchantId/withdraw',  // POST - 商家提现申请

  // 商家财务（新接口）
  GET_FINANCE_DATA: '/v1/merchant/finance',              // GET - M-012 获取商家财务数据
  GET_TRANSACTIONS: '/v1/merchant/finance/transactions', // GET - M-013 获取交易记录
  GET_WITHDRAW_DATA: '/v1/merchant/withdraw',            // GET - M-015 获取提现数据
  SUBMIT_WITHDRAW: '/v1/merchant/withdraw',              // POST - M-016 提交提现申请

  // 商家教程
  GET_TUTORIALS: '/v1/merchant/tutorials',               // GET - M-017 获取教程数据
  SUBMIT_TUTORIAL_FEEDBACK: '/v1/merchant/tutorials/:tutorialId/feedback',  // POST - M-019 记录反馈
}

// ==================== 轮播图相关 ====================
export const BANNER_API = {
  GET_LIST: '/v1/banners',                                // GET - 获取轮播图列表
  GET_DETAIL: '/v1/banners/:bannerId',                   // GET - 获取轮播图详情
  CREATE: '/v1/banners',                                  // POST - 创建轮播图
  UPDATE: '/v1/banners/:bannerId',                       // PUT - 更新轮播图
  DELETE: '/v1/banners/:bannerId',                       // DELETE - 删除轮播图
  UPDATE_STATUS: '/v1/banners/:bannerId/status'          // PUT - 更新轮播图状态
}

// ==================== 菜品相关 ====================
export const DISH_API = {
  // 菜品列表
  GET_LIST: '/v1/dishes',                                 // GET - 获取菜品列表
  GET_DETAIL: '/v1/dishes/:dishId',                       // GET - 获取菜品详情
  GET_RECOMMEND: '/v1/dishes/recommended',                // GET - 获取推荐菜品
  SEARCH: '/v1/dishes/search',                            // GET - 搜索菜品

  // 菜品分类
  GET_CATEGORIES: '/v1/dishes/categories',                // GET - 获取菜品分类

  // 菜品管理
  CREATE: '/v1/dishes',                                   // POST - 创建菜品
  UPDATE: '/v1/dishes/:dishId',                          // PUT - 更新菜品
  DELETE: '/v1/dishes/:dishId',                          // DELETE - 删除菜品
  UPDATE_STATUS: '/v1/dishes/:dishId/status',            // PUT - 更新菜品状态

  // 菜品步骤模板
  GET_STEP_TEMPLATES: '/v1/dish-step-templates',          // GET - 获取步骤模板列表
  CREATE_STEP_TEMPLATE: '/v1/dish-step-templates',        // POST - 创建步骤模板
  UPDATE_STEP_TEMPLATE: '/v1/dish-step-templates/:id',   // PUT - 更新步骤模板
  DELETE_STEP_TEMPLATE: '/v1/dish-step-templates/:id'    // DELETE - 删除步骤模板
}

// ==================== 订单相关 ====================
export const ORDER_API = {
  // 用户订单
  GET_USER_ORDERS: '/v1/orders/user/:userId',            // GET - 获取用户订单列表
  GET_ORDER_DETAIL: '/v1/orders/:orderId',               // GET - 获取订单详情
  CREATE_ORDER: '/v1/orders',                             // POST - 创建订单
  CANCEL_ORDER: '/v1/orders/:orderId/cancel',            // PUT - 取消订单
  UPDATE_ORDER_STATUS: '/v1/orders/:orderId/status',     // PUT - 更新订单状态
  GET_ORDER_DISHES: '/v1/orders/:orderId/dishes',        // GET - 获取订单菜品列表

  // 商家订单
  GET_MERCHANT_ORDERS: '/v1/orders/merchant/:merchantId', // GET - 获取商家订单列表
  ACCEPT_ORDER: '/v1/orders/:orderId/accept',            // PUT - 接单
  REJECT_ORDER: '/v1/orders/:orderId/reject',            // PUT - 拒单
  COMPLETE_ORDER: '/v1/orders/:orderId/complete'         // PUT - 完成订单
}

// ==================== 购物车相关 ====================
export const CART_API = {
  GET_CART: '/v1/cart/:userId',                           // GET - 获取购物车
  ADD_ITEM: '/v1/cart/items',                             // POST - 添加商品到购物车
  UPDATE_ITEM: '/v1/cart/items/:itemId',                 // PUT - 更新购物车商品
  DELETE_ITEM: '/v1/cart/items/:itemId',                 // DELETE - 删除购物车商品
  CLEAR_CART: '/v1/cart/:userId/clear'                   // DELETE - 清空购物车
}

// ==================== 评价相关 ====================
export const REVIEW_API = {
  GET_REVIEWS: '/v1/reviews',                             // GET - 获取评价列表
  GET_USER_REVIEWS: '/v1/reviews/user/:userId',          // GET - 获取用户评价
  GET_MERCHANT_REVIEWS: '/v1/reviews/merchant/:merchantId',  // GET - 获取商家评价
  GET_DISH_REVIEWS: '/v1/reviews/dish/:dishId',          // GET - 获取菜品评价
  CREATE_REVIEW: '/v1/reviews',                           // POST - 创建评价
  DELETE_REVIEW: '/v1/reviews/:reviewId'                // DELETE - 删除评价
}

// ==================== 优惠券相关 ====================
export const COUPON_API = {
  GET_USER_COUPONS: '/v1/coupons/user/:userId',          // GET - 获取用户优惠券
  GET_MERCHANT_COUPONS: '/v1/coupons/merchant/:merchantId',  // GET - 获取商家优惠券
  GET_AVAILABLE_COUPONS: '/v1/coupons/available',        // GET - 获取可用优惠券
  CLAIM_COUPON: '/v1/coupons/:couponId/claim',           // POST - 领取优惠券
  USE_COUPON: '/v1/coupons/:couponId/use',               // POST - 使用优惠券
}

// ==================== 钱包相关 ====================
export const WALLET_API = {
  GET_WALLET: '/v1/wallet/:userId',                       // GET - 获取钱包信息
  GET_BALANCE: '/v1/wallet/:userId/balance',             // GET - 获取余额
  RECHARGE: '/v1/wallet/:userId/recharge',               // POST - 充值
  WITHDRAW: '/v1/wallet/:userId/withdraw',               // POST - 提现
  GET_TRANSACTIONS: '/v1/wallet/:userId/transactions',   // GET - 获取交易记录
}

// ==================== 地址相关 ====================
export const ADDRESS_API = {
  GET_ADDRESSES: '/v1/addresses',                         // GET - 获取地址列表
  GET_ADDRESS: '/v1/addresses/:addressId',               // GET - 获取地址详情
  CREATE_ADDRESS: '/v1/addresses',                        // POST - 创建地址
  UPDATE_ADDRESS: '/v1/addresses/:addressId',            // PUT - 更新地址
  DELETE_ADDRESS: '/v1/addresses/:addressId',            // DELETE - 删除地址
  SET_DEFAULT: '/v1/addresses/:addressId/default'        // PUT - 设置默认地址
}

// ==================== 食谱相关 ====================
export const RECIPE_API = {
  GET_TODAY_RECIPE: '/v1/recipe/today',                 // GET - 获取今日食谱推荐
  GET_MY_RECIPES: '/v1/recipe/my',                      // GET - 获取我的食谱列表
  GET_RECIPE_DETAIL: '/v1/recipe/:recipeId',           // GET - 获取食谱详情
  CREATE_RECIPE: '/v1/recipe/create',                   // POST - 创建自定义食谱
  UPDATE_RECIPE: '/v1/recipe/:recipeId',               // PUT - 更新食谱
  DELETE_RECIPE: '/v1/recipe/:recipeId',               // DELETE - 删除食谱
  RECOMMEND_RECIPE: '/v1/recipe/recommend',             // GET - 推荐食谱
  GET_RECIPE_STEPS: '/v1/recipe/:recipeId/steps',      // GET - 获取食谱制作步骤
  FAVORITE_RECIPE: '/v1/recipe/:recipeId/favorite',    // POST - 收藏食谱
  UNFAVORITE_RECIPE: '/v1/recipe/:recipeId/favorite',  // DELETE - 取消收藏食谱
  SHARE_RECIPE: '/v1/recipe/:recipeId/share',          // POST - 分享食谱
  GET_NUTRITION: '/v1/recipe/nutrition'                // GET - 获取营养分析
}

// ==================== AI 相关 ====================
export const AI_API = {
  CHAT: '/v1/ai/chat',                                   // POST - AI 对话
  STREAM_CHAT: '/v1/ai/chat/stream',                     // POST - AI 流式对话
  RECOMMEND: '/v1/ai/recommend',                         // POST - AI 推荐
  ANALYZE: '/v1/ai/analyze'                             // POST - AI 分析
}

// ==================== 聊天相关 ====================
export const CHAT_API = {
  GET_CONVERSATIONS: '/v1/conversations',                // GET - 获取会话列表
  GET_CONVERSATION: '/v1/conversations/:conversationId', // GET - 获取会话详情
  CREATE_CONVERSATION: '/v1/conversations',              // POST - 创建会话
  DELETE_CONVERSATION: '/v1/conversations/:conversationId',  // DELETE - 删除会话

  GET_MESSAGES: '/v1/messages',                          // GET - 获取消息列表
  SEND_MESSAGE: '/v1/messages',                          // POST - 发送消息
  DELETE_MESSAGE: '/v1/messages/:messageId',            // DELETE - 删除消息
  MARK_READ: '/v1/messages/:messageId/read'             // PUT - 标记消息已读
}

// ==================== 通知相关 ====================
export const NOTIFICATION_API = {
  GET_NOTIFICATIONS: '/v1/notifications',                // GET - 获取通知列表
  GET_NOTIFICATION: '/v1/notifications/:notificationId', // GET - 获取通知详情
  MARK_READ: '/v1/notifications/:notificationId/read',  // PUT - 标记通知已读
  MARK_ALL_READ: '/v1/notifications/read-all',          // PUT - 标记所有通知已读
  DELETE_NOTIFICATION: '/v1/notifications/:notificationId',  // DELETE - 删除通知
}

// ==================== 历史记录相关 ====================
export const HISTORY_API = {
  GET_BROWSE_HISTORY: '/v1/history/browse',              // GET - 获取浏览历史
  GET_SEARCH_HISTORY: '/v1/history/search',              // GET - 获取搜索历史
  CLEAR_HISTORY: '/v1/history/clear'                     // DELETE - 清空历史记录
}

// ==================== 愿望清单相关 ====================
export const WISHLIST_API = {
  GET_WISHLIST: '/v1/wishlist',                          // GET - 获取愿望清单
  ADD_WISH: '/v1/wishlist',                              // POST - 添加愿望
  DELETE_WISH: '/v1/wishlist/:wishId',                  // DELETE - 删除愿望
  COMPLETE_WISH: '/v1/wishlist/:wishId/complete'        // PUT - 完成愿望
}

// ==================== 拼单相关 ====================
export const GROUP_ORDER_API = {
  GET_GROUP_ORDERS: '/v1/group-orders',                  // GET - 获取拼单列表
  GET_GROUP_ORDER: '/v1/group-orders/:groupOrderId',    // GET - 获取拼单详情
  CREATE_GROUP_ORDER: '/v1/group-orders',                // POST - 创建拼单
  JOIN_GROUP_ORDER: '/v1/group-orders/:groupOrderId/join',  // POST - 加入拼单
  LEAVE_GROUP_ORDER: '/v1/group-orders/:groupOrderId/leave',  // POST - 离开拼单
  UPDATE_GROUP_ORDER: '/v1/group-orders/:groupOrderId',  // PUT - 更新拼单
  DELETE_GROUP_ORDER: '/v1/group-orders/:groupOrderId'  // DELETE - 删除拼单
}

// ==================== 反馈相关 ====================
export const FEEDBACK_API = {
  GET_FEEDBACKS: '/v1/feedback',                          // GET - 获取反馈列表
  CREATE_FEEDBACK: '/v1/feedback',                        // POST - 提交反馈
  DELETE_FEEDBACK: '/v1/feedback/:feedbackId'           // DELETE - 删除反馈
}

// ==================== 验证码相关 ====================
export const CAPTCHA_API = {
  GET_CAPTCHA: '/v1/captcha/checkCode',                  // GET - 获取图形验证码
  VERIFY_CAPTCHA: '/v1/captcha/verify'                   // POST - 验证图形验证码
}

// ==================== 位置相关 ====================
export const LOCATION_API = {
  GET_LOCATION: '/v1/location',                           // GET - 获取位置信息
  UPDATE_LOCATION: '/v1/location',                        // PUT - 更新位置信息
  GEOCODE: '/v1/location/geocode',                       // POST - 地址转坐标
  REVERSE_GEOCODE: '/v1/location/reverse-geocode'        // POST - 坐标转地址
}

// ==================== 支付相关 ====================
export const PAYMENT_API = {
  CREATE_PAYMENT: '/v1/payment/create',                  // POST - 创建支付
  QUERY_PAYMENT: '/v1/payment/query',                    // POST - 查询支付状态
  REFUND: '/v1/payment/refund',                          // POST - 申请退款
  CALLBACK: '/v1/payment/callback'                       // POST - 支付回调
}

// ==================== 管理员相关 ====================
export const ADMIN_API = {
  LOGIN: '/v1/admin/login',                              // POST - 管理员登录
  GET_USERS: '/v1/admin/users',                          // GET - 获取用户列表
  GET_MERCHANTS: '/v1/admin/merchants',                  // GET - 获取商家列表
  GET_DISHES: '/v1/admin/dishes',                        // GET - 获取菜品列表
  GET_ORDERS: '/v1/admin/orders',                        // GET - 获取订单列表
  GET_STATISTICS: '/v1/admin/statistics'                // GET - 获取统计数据
}

// ==================== 辅助函数 ====================

/**
 * 构建带路径参数的 URL
 * @param {string} url - URL 模板，如 '/v1/users/:userId'
 * @param {Object} params - 路径参数对象，如 { userId: '123' }
 * @returns {string} 完整 URL，如 '/v1/users/123'
 * @example
 * buildUrl('/v1/users/:userId', { userId: '123' }) // '/v1/users/123'
 * buildUrl('/v1/orders/:orderId/status', { orderId: '456' }) // '/v1/orders/456/status'
 */
export const buildUrl = (url, params = {}) => {
  let result = url
  Object.keys(params).forEach(key => {
    result = result.replace(`:${key}`, params[key])
  })
  return result
}

/**
 * 构建查询参数字符串
 * @param {Object} params - 查询参数对象
 * @returns {string} 查询参数字符串，如 '?key1=value1&key2=value2'
 * @example
 * buildQueryParams({ page: 1, size: 10 }) // '?page=1&size=10'
 */
export const buildQueryParams = (params = {}) => {
  const query = Object.keys(params)
    .filter(key => params[key] !== undefined && params[key] !== null)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
  return query ? `?${query}` : ''
}

// 默认导出所有枚举
export default {
  USER_API,
  MERCHANT_API,
  BANNER_API,
  DISH_API,
  ORDER_API,
  CART_API,
  REVIEW_API,
  COUPON_API,
  WALLET_API,
  ADDRESS_API,
  RECIPE_API,
  AI_API,
  CHAT_API,
  NOTIFICATION_API,
  HISTORY_API,
  WISHLIST_API,
  GROUP_ORDER_API,
  FEEDBACK_API,
  CAPTCHA_API,
  LOCATION_API,
  PAYMENT_API,
  ADMIN_API,
  buildUrl,
  buildQueryParams
}
