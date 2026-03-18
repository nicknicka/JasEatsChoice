/**
 * 路由工具类
 * 统一管理所有页面路由跳转
 */

/**
 * 主包页面路径
 */
const MAIN_PAGES = {
  LOGIN: '/pages/login/index',
  REGISTER: '/pages/register/index'
}

/**
 * 用户端页面路径
 */
const USER_PAGES = {
  HOME: '/pages-user/home/index',
  RECIPE_TODAY: '/pages-user/recipe/today',
  AI: '/pages-user/ai/index',
  PROFILE: '/pages-user/profile/index',
  SEARCH: '/pages-user/search/index',
  MERCHANT_DETAIL: '/pages-user/merchant/detail/index',
  DISH_DETAIL: '/pages-user/dish/detail/index',
  CART: '/pages-user/cart/index',
  ORDER_CONFIRM: '/pages-user/order/confirm/index',
  ORDER_DETAIL: '/pages-user/order/detail/index',
  ORDER_PROGRESS: '/pages-user/order/progress/index',
  ORDER_LIST: '/pages-user/orders/index',
  REVIEW_LIST: '/pages-user/review/list/index',
  REVIEW_SUBMIT: '/pages-user/review/submit/index',
  RECIPE_MY: '/pages-user/recipe/my',
  RECIPE_DETAIL: '/pages-user/recipe/detail/index',
  PROFILE_EDIT: '/pages-user/profile/edit/index',
  ADDRESS_LIST: '/pages-user/address/index',
  ADDRESS_EDIT: '/pages-user/address/edit/index',
  COLLECTION: '/pages-user/collection/index',
  HISTORY: '/pages-user/history/index',
  COUPON: '/pages-user/coupon/index',
  WALLET: '/pages-user/wallet/index',
  MESSAGE: '/pages-user/message/index',
  HELP: '/pages-user/help/index',
  FEEDBACK: '/pages-user/feedback/index',
  CALORIE: '/pages-user/calorie/index',
  CALORIE_RECORD: '/pages-user/calorie/record',
  CALORIE_STATISTICS: '/pages-user/calorie/statistics',
  MERCHANT_LIST: '/pages-user/home/merchant-list',
  DISH_CUSTOMIZE: '/pages-user/dish/customize',
  INTEGRAL: '/pages-user/profile/integral',
  ABOUT: '/pages-user/profile/about'
}

/**
 * 商家端页面路径
 */
const MERCHANT_PAGES = {
  HOME: '/pages-merchant/home/index',
  ORDER: '/pages-merchant/order/index',
  DISH: '/pages-merchant/dish/index',
  PROFILE: '/pages-merchant/profile/index'
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
export const toLogin = () => uni.reLaunch({ url: MAIN_PAGES.LOGIN })
export const toUserHome = () => uni.switchTab({ url: USER_PAGES.HOME })
export const toMerchantHome = () => uni.switchTab({ url: MERCHANT_PAGES.HOME })
export const toProfile = () => uni.switchTab({ url: USER_PAGES.PROFILE })

export const toDishDetail = (dishId) => uni.navigateTo({ url: `${USER_PAGES.DISH_DETAIL}?id=${dishId}` })
export const toMerchantDetail = (merchantId) => uni.navigateTo({ url: `${USER_PAGES.MERCHANT_DETAIL}?id=${merchantId}` })
export const toOrderDetail = (orderId) => uni.navigateTo({ url: `${USER_PAGES.ORDER_DETAIL}?id=${orderId}` })
export const toOrderConfirm = (params) => {
  const query = Object.keys(params).map(key => `${key}=${encodeURIComponent(params[key])}`).join('&')
  uni.navigateTo({ url: `${USER_PAGES.ORDER_CONFIRM}?${query}` })
}
export const toCart = () => uni.navigateTo({ url: USER_PAGES.CART })
export const toSearch = () => uni.navigateTo({ url: USER_PAGES.SEARCH })
export const toRecipeDetail = (recipeId) => uni.navigateTo({ url: `${USER_PAGES.RECIPE_DETAIL}?id=${recipeId}` })
export const toAddressList = () => uni.navigateTo({ url: USER_PAGES.ADDRESS_LIST })
export const toAddressEdit = (addressId = '') => {
  const url = addressId ? `${USER_PAGES.ADDRESS_EDIT}?id=${addressId}` : USER_PAGES.ADDRESS_EDIT
  uni.navigateTo({ url })
}

export const backOrHome = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    toUserHome()
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
