/**
 * 订单相关常量配置
 */

/**
 * 订单状态
 */
export const ORDER_STATUS = {
  ACTIVE: 'active',       // 活动中
  CLOSED: 'closed',       // 已关闭
  PAID: 'paid'           // 已支付
}

/**
 * 订单配置
 */
export const ORDER_CONFIG = {
  ORDER_ID_PREFIX: 'GO',      // 群订单ID前缀
  DEFAULT_MEMBER: '我'         // 默认成员名称
}

/**
 * 订单错误提示
 */
export const ORDER_ERRORS = {
  NO_CONVERSATION: '请先选择一个群聊',
  NO_ORDER: '当前群没有订单，请先创建群订单',
  ORDER_INACTIVE: '该群订单已关闭或已支付，无法加入',
  EMPTY_CART: '购物车为空，无法进行订单确认',
  NO_MERCHANT: '请先创建群订单',
  SELECT_AT_LEAST_ONE: '请至少选择一个商品'
}

/**
 * 成功提示
 */
export const ORDER_SUCCESS = {
  CREATED: '群订单已创建',
  JOINED: '已加入群订单',
  ALREADY_JOINED: '你已经在群订单中了',
  ADDED_TO_CART: '商品已加入购物车',
  ADDED_TO_ORDER: '商品已添加到群订单'
}
