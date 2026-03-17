/**
 * API统一导出
 */
export { userApi } from './modules/user'
export { dishApi } from './modules/dish'
export { orderApi } from './modules/order'

// 导出所有API（方便使用）
export default {
  user: require('./modules/user').userApi,
  dish: require('./modules/dish').dishApi,
  order: require('./modules/order').orderApi
}
