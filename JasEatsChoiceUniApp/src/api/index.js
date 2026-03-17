/**
 * API统一导出
 */
import user from './user'
import dish from './dish'
import order from './order'
import merchant from './merchant'
import recipe from './recipe'
import address from './address'
import ai from './ai'
import message from './message'
import coupon from './coupon'

// 导出所有API（方便使用）
export default {
  user,
  dish,
  order,
  merchant,
  recipe,
  address,
  ai,
  message,
  coupon
}

// 也可以单独导入
export { user, dish, order, merchant, recipe, address, ai, message, coupon }
