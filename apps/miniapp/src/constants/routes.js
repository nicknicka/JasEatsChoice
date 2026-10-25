/**
 * 页面路由常量
 * 统一管理所有页面路径，便于维护和避免路径错误
 */

// ==================== 主包页面 ====================
export const SPLASH = '/pages/splash/index'
export const ROLE_SELECT = '/pages/role-select/index'
export const LOGIN = '/pages/login/index'
export const REGISTER = '/pages/register/index'
export const HOME = '/pages/home/index/index'
export const RECIPE = '/pages/recipe/index'
export const AI = '/pages/ai/index'
export const MESSAGE = '/pages/message/index'
export const USER_CENTER = '/pages/profile/user-center/index'

// ==================== 用户端分包 (src/pages-user) ====================
export const USER_SEARCH = '/pages-user/search/index'
export const USER_MERCHANT_DETAIL = '/pages-user/merchant/detail/index'
export const USER_DISH_DETAIL = '/pages-user/dish/detail/index'
export const USER_DISH_LIST = '/pages-user/dish/list/index'
export const USER_CART = '/pages-user/cart/index'
export const USER_ORDER_CONFIRM = '/pages-user/order/confirm/index'
export const USER_ORDER_DETAIL = '/pages-user/order/detail/index'
export const USER_ORDER_PROGRESS = '/pages-user/order/progress/index'
export const USER_ORDERS = '/pages-user/orders/index'
export const USER_REVIEW_LIST = '/pages-user/review/list/index'
export const USER_REVIEW_SUBMIT = '/pages-user/review/submit/index'
export const USER_RECIPE_MY = '/pages-user/recipe/my'
export const USER_RECIPE_DETAIL = '/pages-user/recipe/detail/index'
export const USER_AI_ADVANCED = '/pages-user/ai/advanced'
export const USER_AI_CONTENT_EXTRACT = '/pages-user/ai/content-extract'
export const USER_SETTINGS = '/pages-user/settings/index'
export const USER_PROFILE_EDIT = '/pages-user/profile/user-center/edit/index'
export const USER_ADDRESS = '/pages-user/address/index'
export const USER_ADDRESS_EDIT = '/pages-user/address/edit/index'
export const USER_COLLECTION = '/pages-user/collection/index'
export const USER_HISTORY = '/pages-user/history/index'
export const USER_COUPON = '/pages-user/coupon/index'
export const USER_WALLET = '/pages-user/wallet/index'
export const USER_WALLET_TRANSACTIONS = '/pages-user/wallet/transactions'
export const USER_MESSAGE = '/pages-user/message/index'
export const USER_HELP = '/pages-user/help/index'
export const USER_FEEDBACK = '/pages-user/feedback/index'
export const USER_CALORIE = '/pages-user/calorie/index'
export const USER_CALORIE_RECORD = '/pages-user/calorie/record'
export const USER_CALORIE_STATISTICS = '/pages-user/calorie/statistics'
export const USER_HOME_MERCHANT_LIST = '/pages-user/home/merchant-list'
export const USER_DISH_CUSTOMIZE = '/pages-user/dish/customize'
export const USER_PROFILE_INTEGRAL = '/pages-user/profile/integral'
export const USER_PROFILE_ABOUT = '/pages-user/profile/about'
export const USER_DEMO_PERFORMANCE = '/pages-user/demo/performance'
export const CUSTOMER_SERVICE = '/pages-user/help/index'

// ==================== 商家端分包 (src/pages-merchant) ====================
export const MERCHANT_HOME = '/pages-merchant/home/index'
export const MERCHANT_STATISTICS = '/pages-merchant/home/statistics'
export const MERCHANT_ANALYTICS = '/pages-merchant/home/analytics'
export const MERCHANT_ORDER = '/pages-merchant/order/index'
export const MERCHANT_ORDER_DETAIL = '/pages-merchant/order/detail'
export const MERCHANT_ORDER_PROCESS = '/pages-merchant/order/process'
export const MERCHANT_ORDER_TODAY = '/pages-merchant/order/today'
export const MERCHANT_DISH = '/pages-merchant/dish/index'
export const MERCHANT_DISH_ADD = '/pages-merchant/dish/add'
export const MERCHANT_DISH_EDIT = '/pages-merchant/dish/edit'
export const MERCHANT_DISH_STEP_CONFIG = '/pages-merchant/dish/step-config'
export const MERCHANT_MENU = '/pages-merchant/menu/index'
export const MERCHANT_MENU_EDIT = '/pages-merchant/menu/edit'
export const MERCHANT_COMMENT = '/pages-merchant/comment/index'
export const MERCHANT_COMMENT_DETAIL = '/pages-merchant/comment/detail'
export const MERCHANT_COMMENT_REPLY = '/pages-merchant/comment/reply'
export const MERCHANT_WISHLIST = '/pages-merchant/wishlist/index'
export const MERCHANT_WISHLIST_AUDIT = '/pages-merchant/wishlist/audit'
export const MERCHANT_CHAT = '/pages-merchant/chat/index'
export const MERCHANT_CHAT_DETAIL = '/pages-merchant/chat/detail'
export const MERCHANT_PROFILE = '/pages-merchant/profile/index'
export const MERCHANT_PROFILE_EDIT = '/pages-merchant/profile/edit'
export const MERCHANT_PROFILE_SHOP = '/pages-merchant/profile/shop'
export const MERCHANT_PROFILE_FINANCE = '/pages-merchant/profile/finance'
export const MERCHANT_PROFILE_WITHDRAW = '/pages-merchant/profile/withdraw'
export const MERCHANT_PROFILE_SETTINGS = '/pages-merchant/profile/settings'
export const MERCHANT_PROFILE_TUTORIALS = '/pages-merchant/profile/tutorials'

// ==================== 通用分包 (src/pages-common) ====================
export const CHAT_CONVERSATION_LIST = '/pages-common/chat/conversation-list'
export const CHAT_ROOM = '/pages-common/chat/chat-room'
export const PAYMENT = '/pages-common/payment/index'
export const PAYMENT_RESULT = '/pages-common/payment/result'

// ==================== 路由导航辅助函数 ====================

/**
 * 跳转到指定页面
 * @param {string} url - 页面路径
 * @param {object} params - 查询参数
 * @param {object} options - navigatorTo 配置项
 */
export const navigateTo = (url, params = {}, options = {}) => {
  let fullUrl = url

  // 拼接参数
  if (params && Object.keys(params).length > 0) {
    const query = Object.keys(params)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      .join('&')
    fullUrl = `${url}?${query}`
  }

  uni.navigateTo({
    url: fullUrl,
    ...options
  })
}

/**
 * 重定向到指定页面
 * @param {string} url - 页面路径
 * @param {object} params - 查询参数
 */
export const redirectTo = (url, params = {}) => {
  let fullUrl = url

  if (params && Object.keys(params).length > 0) {
    const query = Object.keys(params)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      .join('&')
    fullUrl = `${url}?${query}`
  }

  uni.redirectTo({
    url: fullUrl
  })
}

/**
 * 切换到指定页面（清空页面栈）
 * @param {string} url - 页面路径
 * @param {object} params - 查询参数
 */
export const reLaunch = (url, params = {}) => {
  let fullUrl = url

  if (params && Object.keys(params).length > 0) {
    const query = Object.keys(params)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      .join('&')
    fullUrl = `${url}?${query}`
  }

  uni.reLaunch({
    url: fullUrl
  })
}

/**
 * 返回上一页
 * @param {number} delta - 返回的页面数
 */
export const navigateBack = (delta = 1) => {
  uni.navigateBack({
    delta
  })
}

// ==================== 默认导出所有路径 ====================
export default {
  // 主包
  SPLASH,
  ROLE_SELECT,
  LOGIN,
  REGISTER,
  HOME,
  RECIPE,
  AI,
  MESSAGE,
  USER_CENTER,

  // 用户端
  USER_SEARCH,
  USER_MERCHANT_DETAIL,
  USER_DISH_DETAIL,
  USER_DISH_LIST,
  USER_CART,
  USER_ORDER_CONFIRM,
  USER_ORDER_DETAIL,
  USER_ORDER_PROGRESS,
  USER_ORDERS,
  USER_REVIEW_LIST,
  USER_REVIEW_SUBMIT,
  USER_RECIPE_MY,
  USER_RECIPE_DETAIL,
  USER_AI_ADVANCED,
  USER_AI_CONTENT_EXTRACT,
  USER_SETTINGS,
  USER_PROFILE_EDIT,
  USER_ADDRESS,
  USER_ADDRESS_EDIT,
  USER_COLLECTION,
  USER_HISTORY,
  USER_COUPON,
  USER_WALLET,
  USER_WALLET_TRANSACTIONS,
  USER_MESSAGE,
  USER_HELP,
  USER_FEEDBACK,
  USER_CALORIE,
  USER_CALORIE_RECORD,
  USER_CALORIE_STATISTICS,
  USER_HOME_MERCHANT_LIST,
  USER_DISH_CUSTOMIZE,
  USER_PROFILE_INTEGRAL,
  USER_PROFILE_ABOUT,
  USER_DEMO_PERFORMANCE,
  CUSTOMER_SERVICE,

  // 商家端
  MERCHANT_HOME,
  MERCHANT_STATISTICS,
  MERCHANT_ANALYTICS,
  MERCHANT_ORDER,
  MERCHANT_ORDER_DETAIL,
  MERCHANT_ORDER_PROCESS,
  MERCHANT_ORDER_TODAY,
  MERCHANT_DISH,
  MERCHANT_DISH_ADD,
  MERCHANT_DISH_EDIT,
  MERCHANT_DISH_STEP_CONFIG,
  MERCHANT_MENU,
  MERCHANT_MENU_EDIT,
  MERCHANT_COMMENT,
  MERCHANT_COMMENT_DETAIL,
  MERCHANT_COMMENT_REPLY,
  MERCHANT_WISHLIST,
  MERCHANT_WISHLIST_AUDIT,
  MERCHANT_CHAT,
  MERCHANT_CHAT_DETAIL,
  MERCHANT_PROFILE,
  MERCHANT_PROFILE_EDIT,
  MERCHANT_PROFILE_SHOP,
  MERCHANT_PROFILE_FINANCE,
  MERCHANT_PROFILE_WITHDRAW,
  MERCHANT_PROFILE_SETTINGS,
  MERCHANT_PROFILE_TUTORIALS,

  // 通用
  CHAT_CONVERSATION_LIST,
  CHAT_ROOM,
  PAYMENT,
  PAYMENT_RESULT,

  // 辅助函数
  navigateTo,
  redirectTo,
  reLaunch,
  navigateBack
}
