/**
 * API统一导出
 * 统一使用modules目录下的API模块
 */

// 从modules目录导入所有API模块
export { userApi } from './modules/user'
export { dishApi } from './modules/dish'
export { orderApi } from './modules/order'
export { merchantApi } from './modules/merchant'
export { recipeApi } from './modules/recipe'
export { addressApi } from './modules/address'
export { aiApi } from './modules/ai'
export { chatApi } from './modules/chat'
export { couponApi } from './modules/coupon'
export { favoriteApi } from './modules/favorite'
export { reviewApi } from './modules/review'
export { walletApi } from './modules/wallet'
export { historyApi } from './modules/history'
export { notificationApi } from './modules/notification'
export { feedbackApi } from './modules/feedback'
export { wishlistApi } from './modules/wishlist'
export { groupOrderApi } from './modules/groupOrder'
export { cartApi } from './modules/cart'

// 默认导出所有API的集合（向后兼容）
import { userApi } from './modules/user'
import { dishApi } from './modules/dish'
import { orderApi } from './modules/order'
import { merchantApi } from './modules/merchant'
import { recipeApi } from './modules/recipe'
import { addressApi } from './modules/address'
import { aiApi } from './modules/ai'
import { chatApi } from './modules/chat'
import { couponApi } from './modules/coupon'
import { favoriteApi } from './modules/favorite'
import { reviewApi } from './modules/review'
import { walletApi } from './modules/wallet'
import { historyApi } from './modules/history'
import { notificationApi } from './modules/notification'
import { feedbackApi } from './modules/feedback'
import { wishlistApi } from './modules/wishlist'
import { groupOrderApi } from './modules/groupOrder'
import { cartApi } from './modules/cart'

export default {
  user: userApi,
  dish: dishApi,
  order: orderApi,
  merchant: merchantApi,
  recipe: recipeApi,
  address: addressApi,
  ai: aiApi,
  chat: chatApi,
  coupon: couponApi,
  favorite: favoriteApi,
  review: reviewApi,
  wallet: walletApi,
  history: historyApi,
  notification: notificationApi,
  feedback: feedbackApi,
  wishlist: wishlistApi,
  groupOrder: groupOrderApi,
  cart: cartApi
}
