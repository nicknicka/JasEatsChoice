/**
 * 路由工具类
 * 统一管理所有页面路由跳转
 */

/**
 * 主包页面路径
 */
const MAIN_PAGES = {
  LOGIN: '/src/pages/login/index',
  REGISTER: '/src/pages/register/index',
  HOME: '/src/pages/home/index/index',
  RECIPE: '/src/pages/recipe/index',
  AI: '/src/pages/ai/index',
  PROFILE: '/src/pages/profile/user-center/index'
}

/**
 * 用户端页面路径（分包）
 */
const USER_PAGES = {
  HOME: '/src/pages-user/home/index',
  RECIPE_TODAY: '/src/pages-user/recipe/today',
  AI: '/src/pages-user/ai/index',
  PROFILE: '/src/pages-user/profile/user-center/index',
  SEARCH: '/src/pages-user/search/index',
  MERCHANT_DETAIL: '/src/pages-user/merchant/detail/index',
  DISH_DETAIL: '/src/pages-user/dish/detail/index',
  CART: '/src/pages-user/cart/index',
  ORDER_CONFIRM: '/src/pages-user/order/confirm/index',
  ORDER_DETAIL: '/src/pages-user/order/detail/index',
  ORDER_PROGRESS: '/src/pages-user/order/progress/index',
  ORDER_LIST: '/src/pages-user/orders/index',
  REVIEW_LIST: '/src/pages-user/review/list/index',
  REVIEW_SUBMIT: '/src/pages-user/review/submit/index',
  RECIPE_MY: '/src/pages-user/recipe/my',
  RECIPE_DETAIL: '/src/pages-user/recipe/detail/index',
  PROFILE_EDIT: '/src/pages-user/profile/user-center/edit/index',
  ADDRESS_LIST: '/src/pages-user/address/index',
  ADDRESS_EDIT: '/src/pages-user/address/edit/index',
  COLLECTION: '/src/pages-user/collection/index',
  HISTORY: '/src/pages-user/history/index',
  COUPON: '/src/pages-user/coupon/index',
  WALLET: '/src/pages-user/wallet/index',
  MESSAGE: '/src/pages-user/message/index',
  HELP: '/src/pages-user/help/index',
  FEEDBACK: '/src/pages-user/feedback/index',
  CALORIE: '/src/pages-user/calorie/index',
  CALORIE_RECORD: '/src/pages-user/calorie/record',
  CALORIE_STATISTICS: '/src/pages-user/calorie/statistics',
  MERCHANT_LIST: '/src/pages-user/home/merchant-list',
  DISH_CUSTOMIZE: '/src/pages-user/dish/customize',
  INTEGRAL: '/src/pages-user/profile/integral',
  ABOUT: '/src/pages-user/profile/about'
}

/**
 * 商家端页面路径
 */
const MERCHANT_PAGES = {
  HOME: '/src/pages-merchant/home/index',
  ORDER: '/src/pages-merchant/order/index',
  DISH: '/src/pages-merchant/dish/index',
  PROFILE: '/src/pages-merchant/profile/index'
}

/**
 * 公共页面路径
 */
const COMMON_PAGES = {
  CONVERSATION_LIST: '/src/pages-common/chat/conversation-list',
  CHAT_ROOM: '/src/pages-common/chat/chat-room',
  PAYMENT: '/src/pages-common/payment/index',
  PAYMENT_RESULT: '/src/pages-common/payment/result'
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
  uni.switchTab({ url: '/src/pages/home/index/index' })
}

export const toMerchantHome = () => {
  console.log('🔄 [路由] 切换到商家首页')
  uni.switchTab({ url: '/src/pages-merchant/home/index' })
}

export const toProfile = () => {
  console.log('🔄 [路由] 切换到用户中心')
  uni.switchTab({ url: '/src/pages/profile/user-center/index' })
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
    uni.switchTab({ url: '/src/pages/home/index/index' })
  }
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
  paths
}
