/**
 * 路由工具类
 * 统一管理所有页面路由跳转
 */

/**
 * 主包页面路径
 */
const MAIN_PAGES = {
  LOGIN: '/pages/login/index',
  REGISTER: '/pages/register/index',
  HOME: '/pages/home/index/index',
  RECIPE: '/pages/recipe/index',
  AI: '/pages/ai/index',
  PROFILE: '/pages/profile/user-center/index',
  CUSTOMER_SERVICE: '/pages-user/help/index'
}

/**
 * 用户端页面路径（分包）
 */
const USER_PAGES = {
  HOME: '/pages/home/index/index',
  RECIPE_TODAY: '/pages/recipe/index',
  AI: '/pages/ai/index',
  AI_ADVANCED: '/pages-user/ai/advanced',
  AI_CONTENT_EXTRACT: '/pages-user/ai/content-extract',
  PROFILE: '/pages/profile/user-center/index',
  SEARCH: '/pages-user/search/index',
  MERCHANT_DETAIL: '/pages-user/merchant/detail/index',
  DISH_DETAIL: '/pages-user/dish/detail/index',
  DISH_LIST: '/pages-user/dish/list/index',
  DISH_CUSTOMIZE: '/pages-user/dish/customize',
  CART: '/pages-user/cart/index',
  ORDER_CONFIRM: '/pages-user/order/confirm/index',
  ORDER_DETAIL: '/pages-user/order/detail/index',
  ORDER_PROGRESS: '/pages-user/order/progress/index',
  ORDER_LIST: '/pages-user/orders/index',
  REVIEW_LIST: '/pages-user/review/list/index',
  REVIEW_SUBMIT: '/pages-user/review/submit/index',
  RECIPE_MY: '/pages-user/recipe/my',
  RECIPE_DETAIL: '/pages-user/recipe/detail/index',
  PROFILE_EDIT: '/pages-user/profile/user-center/edit/index',
  ADDRESS_LIST: '/pages-user/address/index',
  ADDRESS_EDIT: '/pages-user/address/edit/index',
  COLLECTION: '/pages-user/collection/index',
  HISTORY: '/pages-user/history/index',
  COUPON: '/pages-user/coupon/index',
  WALLET: '/pages-user/wallet/index',
  WALLET_TRANSACTIONS: '/pages-user/wallet/transactions',
  MESSAGE: '/pages-user/message/index',
  HELP: '/pages-user/help/index',
  FEEDBACK: '/pages-user/feedback/index',
  CALORIE: '/pages-user/calorie/index',
  CALORIE_RECORD: '/pages-user/calorie/record',
  CALORIE_STATISTICS: '/pages-user/calorie/statistics',
  MERCHANT_LIST: '/pages-user/home/merchant-list',
  INTEGRAL: '/pages-user/profile/integral',
  ABOUT: '/pages-user/profile/about',
  DEMO_PERFORMANCE: '/pages-user/demo/performance',
  SETTINGS: '/pages-user/settings/index'
}

/**
 * 商家端页面路径
 */
const MERCHANT_PAGES = {
  HOME: '/pages-merchant/home/index',
  STATISTICS: '/pages-merchant/home/statistics',
  ANALYTICS: '/pages-merchant/home/analytics',
  ORDER: '/pages-merchant/order/index',
  ORDER_DETAIL: '/pages-merchant/order/detail',
  ORDER_PROCESS: '/pages-merchant/order/process',
  ORDER_TODAY: '/pages-merchant/order/today',
  DISH: '/pages-merchant/dish/index',
  DISH_ADD: '/pages-merchant/dish/add',
  DISH_EDIT: '/pages-merchant/dish/edit',
  DISH_STEP_CONFIG: '/pages-merchant/dish/step-config',
  MENU: '/pages-merchant/menu/index',
  MENU_EDIT: '/pages-merchant/menu/edit',
  COMMENT: '/pages-merchant/comment/index',
  COMMENT_DETAIL: '/pages-merchant/comment/detail',
  COMMENT_REPLY: '/pages-merchant/comment/reply',
  WISHLIST: '/pages-merchant/wishlist/index',
  WISHLIST_AUDIT: '/pages-merchant/wishlist/audit',
  CHAT: '/pages-merchant/chat/index',
  CHAT_DETAIL: '/pages-merchant/chat/detail',
  PROFILE: '/pages-merchant/profile/index',
  PROFILE_EDIT: '/pages-merchant/profile/edit',
  SHOP: '/pages-merchant/profile/shop',
  FINANCE: '/pages-merchant/profile/finance',
  WITHDRAW: '/pages-merchant/profile/withdraw',
  SETTINGS: '/pages-merchant/profile/settings',
  TUTORIALS: '/pages-merchant/profile/tutorials'
}

/**
 * 公共页面路径
 */
const COMMON_PAGES = {
  CONVERSATION_LIST: '/pages-common/chat/conversation-list',
  CHAT_ROOM: '/pages-common/chat/chat-room',
  PAYMENT: '/pages-common/payment/index',
  PAYMENT_RESULT: '/pages-common/payment/result'
}

// 导出路径常量
export const paths = {
  MAIN: MAIN_PAGES,
  USER: USER_PAGES,
  MERCHANT: MERCHANT_PAGES,
  COMMON: COMMON_PAGES
}

// 导出跳转方法
export const toLogin = () => {
  console.log('🔄 [路由] 跳转登录页')
  uni.reLaunch({ url: MAIN_PAGES.LOGIN })
}

export const toUserHome = () => {
  console.log('🔄 [路由] 切换到用户首页')
  uni.switchTab({ url: '/pages/home/index/index' })
}

export const toMerchantHome = () => {
  console.log('🔄 [路由] 切换到商家首页')
  uni.switchTab({ url: '/pages-merchant/home/index' })
}

export const toProfile = () => {
  console.log('🔄 [路由] 切换到用户中心')
  uni.switchTab({ url: '/pages/profile/user-center/index' })
}

export const toDishDetail = (dishId) => {
  console.log('🔄 [路由] 跳转菜品详情', { dishId, url: `${USER_PAGES.DISH_DETAIL}?id=${dishId}` })
  uni.navigateTo({
    url: `${USER_PAGES.DISH_DETAIL}?id=${dishId}`,
    success: () => console.log('✅ [路由] 菜品详情跳转成功'),
    fail: (err) => console.error('❌ [路由] 菜品详情跳转失败:', err)
  })
}

export const toMerchantDetail = (merchantId) => {
  console.log('🔄 [路由] 跳转商家详情', { merchantId, url: `${USER_PAGES.MERCHANT_DETAIL}?id=${merchantId}` })
  uni.navigateTo({
    url: `${USER_PAGES.MERCHANT_DETAIL}?id=${merchantId}`,
    success: () => console.log('✅ [路由] 商家详情跳转成功'),
    fail: (err) => console.error('❌ [路由] 商家详情跳转失败:', err)
  })
}

export const toOrderDetail = (orderId) => {
  console.log('🔄 [路由] 跳转订单详情', { orderId, url: `${USER_PAGES.ORDER_DETAIL}?id=${orderId}` })
  uni.navigateTo({
    url: `${USER_PAGES.ORDER_DETAIL}?id=${orderId}`,
    success: () => console.log('✅ [路由] 订单详情跳转成功'),
    fail: (err) => console.error('❌ [路由] 订单详情跳转失败:', err)
  })
}

export const toOrderConfirm = (params) => {
  const query = Object.keys(params).map(key => `${key}=${encodeURIComponent(params[key])}`).join('&')
  const url = `${USER_PAGES.ORDER_CONFIRM}?${query}`
  console.log('🔄 [路由] 跳转订单确认', { params, url })
  uni.navigateTo({
    url,
    success: () => console.log('✅ [路由] 订单确认跳转成功'),
    fail: (err) => console.error('❌ [路由] 订单确认跳转失败:', err)
  })
}

export const toCart = () => {
  console.log('🔄 [路由] 跳转购物车', { url: USER_PAGES.CART })
  uni.navigateTo({
    url: USER_PAGES.CART,
    success: () => console.log('✅ [路由] 购物车跳转成功'),
    fail: (err) => console.error('❌ [路由] 购物车跳转失败:', err)
  })
}

export const toSearch = () => {
  console.log('🔄 [路由] 跳转搜索页', { url: USER_PAGES.SEARCH })
  uni.navigateTo({
    url: USER_PAGES.SEARCH,
    success: () => console.log('✅ [路由] 搜索页跳转成功'),
    fail: (err) => console.error('❌ [路由] 搜索页跳转失败:', err)
  })
}

export const toRecipeDetail = (recipeId) => {
  console.log('🔄 [路由] 跳转食谱详情', { recipeId, url: `${USER_PAGES.RECIPE_DETAIL}?id=${recipeId}` })
  uni.navigateTo({
    url: `${USER_PAGES.RECIPE_DETAIL}?id=${recipeId}`,
    success: () => console.log('✅ [路由] 食谱详情跳转成功'),
    fail: (err) => console.error('❌ [路由] 食谱详情跳转失败:', err)
  })
}

export const toAddressList = () => {
  console.log('🔄 [路由] 跳转地址列表', { url: USER_PAGES.ADDRESS_LIST })
  uni.navigateTo({
    url: USER_PAGES.ADDRESS_LIST,
    success: () => console.log('✅ [路由] 地址列表跳转成功'),
    fail: (err) => console.error('❌ [路由] 地址列表跳转失败:', err)
  })
}

export const toAddressEdit = (addressId = '') => {
  const url = addressId ? `${USER_PAGES.ADDRESS_EDIT}?id=${addressId}` : USER_PAGES.ADDRESS_EDIT
  console.log('🔄 [路由] 跳转地址编辑', { addressId, url })
  uni.navigateTo({
    url,
    success: () => console.log('✅ [路由] 地址编辑跳转成功'),
    fail: (err) => console.error('❌ [路由] 地址编辑跳转失败:', err)
  })
}

export const backOrHome = () => {
  const pages = getCurrentPages()
  console.log('🔄 [路由] 返回或首页', { pageCount: pages.length })
  if (pages.length > 1) {
    console.log('🔄 [路由] 执行返回')
    uni.navigateBack()
  } else {
    console.log('🔄 [路由] 返回首页')
    uni.switchTab({ url: '/pages/home/index/index' })
  }
}

/**
 * 通用路由跳转函数（支持参数传递）
 * @param {string} url - 页面路径
 * @param {object} params - 查询参数
 * @param {string} navigationType - 导航类型：navigateTo/redirectTo/reLaunch/switchTab
 */
export const navigate = (url, params = {}, navigationType = 'navigateTo') => {
  let fullUrl = url

  // 拼接参数
  if (params && Object.keys(params).length > 0) {
    const query = Object.keys(params)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      .join('&')
    fullUrl = `${url}?${query}`
  }

  console.log(`🔄 [路由] ${navigationType}`, { url: fullUrl })

  const navigationMethods = {
    navigateTo: uni.navigateTo,
    redirectTo: uni.redirectTo,
    reLaunch: uni.reLaunch,
    switchTab: uni.switchTab
  }

  const method = navigationMethods[navigationType] || uni.navigateTo

  method({
    url: fullUrl,
    success: () => console.log(`✅ [路由] ${navigationType} 成功`),
    fail: (err) => console.error(`❌ [路由] ${navigationType} 失败:`, err)
  })
}

/**
 * 返回上一页
 * @param {number} delta - 返回页面数
 */
export const goBack = (delta = 1) => {
  console.log('🔄 [路由] 返回上一页', { delta })
  uni.navigateBack({ delta })
}

/**
 * 跳转到客服中心
 */
export const toCustomerService = () => {
  console.log('🔄 [路由] 跳转客服中心')
  navigate(MAIN_PAGES.CUSTOMER_SERVICE)
}

/**
 * 跳转到钱包交易明细
 */
export const toWalletTransactions = () => {
  console.log('🔄 [路由] 跳转交易明细')
  navigate(USER_PAGES.WALLET_TRANSACTIONS)
}

export default {
  toLogin,
  toUserHome,
  toMerchantHome,
  toDishDetail,
  toMerchantDetail,
  toOrderDetail,
  toOrderConfirm,
  toCart,
  toSearch,
  toRecipeDetail,
  toAddressList,
  toAddressEdit,
  toProfile,
  backOrHome,
  navigate,
  goBack,
  toCustomerService,
  toWalletTransactions,
  paths
}
