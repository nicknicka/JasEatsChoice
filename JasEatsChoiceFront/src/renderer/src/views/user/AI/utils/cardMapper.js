/**
 * AI 聊天卡片组件映射器
 * 根据消息类型返回对应的卡片组件
 */

import { h } from 'vue'
import OrderListCard from '../components/cards/OrderListCard.vue'
import UserInfoCard from '../components/cards/UserInfoCard.vue'
import FavoriteListCard from '../components/cards/FavoriteListCard.vue'
import ReviewListCard from '../components/cards/ReviewListCard.vue'
import CouponListCard from '../components/cards/CouponListCard.vue'
import DishListCard from '../components/cards/DishListCard.vue'
import NotificationListCard from '../components/cards/NotificationListCard.vue'
import ErrorCard from '../components/cards/ErrorCard.vue'

// 组件映射表
// 注意：notification_list_card 不在此表中，会以纯文本形式显示
const CARD_COMPONENT_MAP = {
  order_list_card: OrderListCard,
  favorite_list_card: FavoriteListCard,
  review_list_card: ReviewListCard,
  coupon_list_card: CouponListCard,
  user_info_card: UserInfoCard,
  dish_list_card: DishListCard,
  error_card: ErrorCard
}

/**
 * 根据 messageType 获取对应的卡片组件
 * @param {string} messageType - 消息类型
 * @returns {Component|null} Vue 组件
 */
export const getCardComponent = (messageType) => {
  return CARD_COMPONENT_MAP[messageType] || null
}

/**
 * 判断消息是否为卡片类型
 * @param {string} messageType - 消息类型
 * @returns {boolean}
 */
export const isCardMessage = (messageType) => {
  return CARD_COMPONENT_MAP.hasOwnProperty(messageType)
}

/**
 * 渲染卡片组件
 * @param {string} messageType - 消息类型
 * @param {Object} data - 卡片数据
 * @param {Function} onAction - 操作回调函数
 * @returns {VNode|null}
 */
export const renderCard = (messageType, data, onAction) => {
  const CardComponent = getCardComponent(messageType)

  if (!CardComponent) {
    console.warn(`未找到消息类型 ${messageType} 对应的卡片组件`)
    return null
  }

  return h(CardComponent, {
    data: data,
    onAction: onAction
  })
}

/**
 * 获取所有支持的卡片类型
 * @returns {string[]}
 */
export const getSupportedCardTypes = () => {
  return Object.keys(CARD_COMPONENT_MAP)
}
