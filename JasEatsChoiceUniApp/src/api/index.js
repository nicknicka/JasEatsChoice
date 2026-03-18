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

export default {
  user: userApi,
  dish: dishApi,
  order: orderApi,
  merchant: merchantApi,
  recipe: recipeApi,
  address: addressApi,
  ai: aiApi,
  chat: chatApi,
  coupon: couponApi
}
