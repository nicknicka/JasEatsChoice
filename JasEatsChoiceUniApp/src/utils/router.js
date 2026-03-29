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
  HOME: '/home/index',
  RECIPE_TODAY: '/recipe/today',
  AI: '/ai/index',
  PROFILE: '/profile/user-center/index',
  SEARCH: '/search/index',
  MERCHANT_DETAIL: '/merchant/detail/index',
  DISH_DETAIL: '/dish/detail/index',
  CART: '/cart/index',
  ORDER_CONFIRM: '/order/confirm/index',
  ORDER_DETAIL: '/order/detail/index',
  ORDER_PROGRESS: '/order/progress/index',
  ORDER_LIST: '/orders/index',
  REVIEW_LIST: '/review/list/index',
  REVIEW_SUBMIT: '/review/submit/index',
  RECIPE_MY: '/recipe/my',
  RECIPE_DETAIL: '/recipe/detail/index',
  PROFILE_EDIT: '/profile/edit/index',
  ADDRESS_LIST: '/address/index',
  ADDRESS_EDIT: '/address/edit/index',
  COLLECTION: '/collection/index',
  HISTORY: '/history/index',
  COUPON: '/coupon/index',
  WALLET: '/wallet/index',
  MESSAGE: '/message/index',
  HELP: '/help/index',
  FEEDBACK: '/feedback/index',
  CALORIE: '/calorie/index',
  CALORIE_RECORD: '/calorie/record',
  CALORIE_STATISTICS: '/calorie/statistics',
  MERCHANT_LIST: '/home/merchant-list',
  DISH_CUSTOMIZE: '/dish/customize',
  INTEGRAL: '/profile/integral',
  ABOUT: '/profile/about'
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
export const toLogin = () => uni.reLaunch({ url: MAIN_PAGES.LOGIN })
export const toUserHome = () => uni.switchTab({ url: '/src/pages/home/index/index' })
export const toMerchantHome = () => uni.switchTab({ url: '/src/pages-merchant/home/index' })
export const toProfile = () => uni.switchTab({ url: '/src/pages/profile/user-center/index' })

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
