/**
 * AI 聊天消息类型常量
 * 定义不同类型的卡片消息
 */

// 消息类型枚举
export const MessageTypes = {
  // 普通文本消息
  TEXT: 'text',

  // 结构化查询消息
  STRUCTURED_QUERY: 'structured_query',

  // 卡片消息类型
  ORDER_LIST_CARD: 'order_list_card', // 订单列表卡片
  FAVORITE_LIST_CARD: 'favorite_list_card', // 收藏列表卡片
  REVIEW_LIST_CARD: 'review_list_card', // 评价列表卡片
  COUPON_LIST_CARD: 'coupon_list_card', // 优惠券列表卡片
  USER_INFO_CARD: 'user_info_card', // 用户信息卡片
  DISH_LIST_CARD: 'dish_list_card', // 菜品列表卡片
  ERROR_CARD: 'error_card' // 错误卡片
}

// 查询类型枚举（用于快速操作按钮）
export const QueryTypes = {
  ORDER_LIST: 'order_list',
  FAVORITE_LIST: 'favorite_list',
  REVIEW_LIST: 'review_list',
  COUPON_LIST: 'coupon_list',
  USER_INFO: 'user_info',
  DISH_LIST: 'dish_list'
}

// 快速操作按钮配置
export const QuickActions = [
  {
    id: 'orders',
    label: '我的订单',
    icon: '📋',
    queryType: QueryTypes.ORDER_LIST,
    description: '查看我的所有订单'
  },
  {
    id: 'favorites',
    label: '我的收藏',
    icon: '⭐',
    queryType: QueryTypes.FAVORITE_LIST,
    description: '查看我收藏的菜品'
  },
  {
    id: 'reviews',
    label: '我的评价',
    icon: '💬',
    queryType: QueryTypes.REVIEW_LIST,
    description: '查看我的评价记录'
  },
  {
    id: 'coupons',
    label: '我的优惠券',
    icon: '🎟️',
    queryType: QueryTypes.COUPON_LIST,
    description: '查看我的优惠券'
  },
  {
    id: 'userInfo',
    label: '我的信息',
    icon: '👤',
    queryType: QueryTypes.USER_INFO,
    description: '查看个人信息'
  }
]

// 订单状态映射
export const OrderStatusMap = {
  0: { text: '待支付', color: 'warning' },
  1: { text: '待接单', color: 'info' },
  2: { text: '备菜中', color: 'primary' },
  3: { text: '烹饪中', color: 'primary' },
  4: { text: '待上菜', color: 'primary' },
  5: { text: '已送达', color: 'success' },
  6: { text: '已取消', color: 'danger' },
  7: { text: '待评价', color: 'warning' },
  8: { text: '已评价', color: 'success' }
}

// BMI状态映射
export const BMIStatusMap = {
  underweight: { text: '偏瘦', color: 'info' },
  normal: { text: '正常', color: 'success' },
  overweight: { text: '偏胖', color: 'warning' },
  obese: { text: '肥胖', color: 'danger' }
}
