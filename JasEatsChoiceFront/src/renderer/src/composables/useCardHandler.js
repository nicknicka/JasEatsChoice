import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../config/index'
import { useAuthStore } from '../store/authStore'
import { orderActions, favoriteActions, cartActions, reviewActions } from '../views/user/AI/utils/cardActionService'

/**
 * 卡片操作处理器
 * 处理卡片类型转换、数据解析、操作分发
 * @param {import('vue').Ref<Array>} messages - 消息列表引用
 * @returns {Object} 卡片处理方法和状态
 */
export function useCardHandler(messages) {
	const authStore = useAuthStore()

	const getUserId = () => String(authStore.userId)

	// 记录最后发送的结构化查询类型（用于刷新卡片）
	const lastQueryType = ref(null)
	const lastQueryMessageIndex = ref(-1)

	/**
	 * 将后端返回的卡片类型转换为前端支持的卡片类型
	 * @param {string} cardType - 后端卡片类型
	 * @returns {string|null} 前端消息类型
	 */
	const convertToSupportedCardType = (cardType) => {
		const typeMapping = {
			'foodrecommendationcard': 'dish_list_card',
			'food_recommendation_card': 'dish_list_card',
			'dish': 'dish_list_card',
			'order': 'order_list_card',
			'order_card': 'order_list_card',
			'favorite': 'favorite_list_card',
			'favorite_card': 'favorite_list_card',
			'merchant': 'notification_list_card',
			'review': 'review_list_card',
			'review_card': 'review_list_card',
			'coupon': 'coupon_list_card',
			'coupon_card': 'coupon_list_card',
			'user': 'user_info_card',
			'user_info': 'user_info_card',
			'user_info_card': 'user_info_card',
			'health': 'notification_list_card',
			'health_card': 'notification_list_card',
			'nutrition': 'notification_list_card',
		}

		if (typeMapping[cardType]) return typeMapping[cardType]

		const supportedTypes = [
			'order_list_card', 'favorite_list_card', 'review_list_card',
			'coupon_list_card', 'user_info_card', 'dish_list_card',
			'notification_list_card', 'error_card'
		]
		if (supportedTypes.includes(cardType)) return cardType

		console.warn('未知的卡片类型:', cardType)
		return null
	}

	/**
	 * 从消息内容中提取卡片数据和类型
	 * @param {string} content - 原始消息内容
	 * @returns {{ content: string, cardData: object|null, messageType: string|null }}
	 */
	const parseCardDataFromContent = (content) => {
		if (!content || typeof content !== 'string') {
			return { content, cardData: null, messageType: null }
		}

		if (!content.includes('[CARD_DATA_START]') || !content.includes('[CARD_DATA_END]')) {
			return { content, cardData: null, messageType: null }
		}

		const cardDataStart = content.indexOf('[CARD_DATA_START]')
		const cardDataEnd = content.indexOf('[CARD_DATA_END]')

		if (cardDataStart === -1 || cardDataEnd === -1) {
			return { content, cardData: null, messageType: null }
		}

		const cardDataString = content.substring(
			cardDataStart + '[CARD_DATA_START]'.length,
			cardDataEnd
		).trim()

		const cleanContent = content.substring(0, cardDataStart).trim()

		let cardData = null
		let messageType = null

		try {
			let parsedData = JSON.parse(cardDataString)
			let cardDataArray = Array.isArray(parsedData) ? parsedData : [parsedData]

			if (cardDataArray.length > 0) {
				const firstCard = cardDataArray[0]
				const cardType = firstCard.type || firstCard.cardType

				if (cardType) {
					messageType = convertToSupportedCardType(cardType)
				}

				if (firstCard.recommendations) {
					cardData = { recommendations: firstCard.recommendations }
				} else if (firstCard.orders) {
					cardData = { orders: firstCard.orders }
				} else if (firstCard.dishes) {
					cardData = { dishes: firstCard.dishes }
				} else if (cardType === 'dish') {
					cardData = {
						dishes: cardDataArray.map(dish => ({
							dishId: dish.dishId || dish.id,
							dishName: dish.dishName || dish.title,
							imageUrl: dish.imageUrl || dish.image,
							description: dish.description || dish.highlight,
							price: dish.price,
							rating: dish.rating,
							category: dish.category,
							tags: dish.tags || [],
							actions: dish.actions || []
						}))
					}
				} else {
					cardData = firstCard
				}
			}
		} catch (error) {
			console.warn('解析卡片数据失败:', error.message)
		}

		return { content: cleanContent, cardData, messageType }
	}

	/**
	 * 刷新卡片数据
	 */
	const refreshCard = async () => {
		if (!lastQueryType.value || lastQueryMessageIndex.value < 0) return

		const userId = getUserId()
		try {
			const response = await axios.post(
				`${API_CONFIG.baseURL}/v1/ai/assistant/chat`,
				{
					messageType: 'structured_query',
					queryType: lastQueryType.value,
					userId,
					params: {},
				},
				{ headers: { Authorization: `Bearer ${authStore.token}` } }
			)

			if (response.data.code === 200 && response.data.data) {
				const cardData = response.data.data
				const idx = lastQueryMessageIndex.value
				if (messages.value[idx]) {
					messages.value[idx].messageType = cardData.messageType
					messages.value[idx].cardData = cardData.data
					messages.value[idx].content = cardData.summary || '查询成功'
				}
				ElMessage.success('数据已刷新')
			}
		} catch (error) {
			console.error('刷新卡片失败:', error)
			ElMessage.error('刷新失败，请稍后重试')
		}
	}

	/**
	 * 恢复历史消息的卡片数据（向后兼容）
	 */
	const restoreCardDataForMessages = async () => {
		const userId = getUserId()

		for (let i = 0; i < messages.value.length; i++) {
			const message = messages.value[i]

			if (message.sender === 'ai' && !message.messageType && message.content) {
				let queryType = null
				const content = message.content

				if (content.includes('找到') && content.includes('条订单')) queryType = 'list_orders'
				else if (content.includes('收藏列表') || content.includes('我的收藏')) queryType = 'get_favorites'
				else if (content.includes('评价列表') || content.includes('我的评价')) queryType = 'get_user_reviews'
				else if (content.includes('优惠券') || content.includes('我的优惠券')) queryType = 'get_user_coupons'
				else if (content.includes('用户档案') || content.includes('用户信息')) queryType = 'get_user_info'

				if (queryType) {
					try {
						const response = await axios.post(
							API_CONFIG.baseURL + '/v1/ai/assistant/chat',
							{
								messageType: 'structured_query',
								queryType,
								userId,
								params: {},
							},
							{ headers: { Authorization: `Bearer ${authStore.token}` } }
						)

						if (response.data.code === 200 && response.data.data) {
							const cardData = response.data.data
							messages.value[i].messageType = cardData.messageType
							messages.value[i].cardData = cardData.data
						}
					} catch (error) {
						console.warn('恢复卡片数据失败:', queryType, error)
					}
				}
			}
		}
	}

	/**
	 * 处理卡片操作
	 * @param {Object} action - 操作对象 { type, data }
	 * @param {Object} router - Vue Router 实例
	 */
	const handleCardAction = async (action, router) => {
		const userId = getUserId()

		try {
			switch (action.type) {
				case 'detail': {
					if (action.data.orderId) {
						router.push({ name: 'OrderDetail', params: { orderId: action.data.orderId } })
							.catch(() => ElMessage.info(`订单ID: ${action.data.orderId}`))
					}
					break
				}
				case 'cancel': {
					try {
						await ElMessageBox.confirm('确认取消此订单？', '提示', {
							confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
						})
						const result = await orderActions.cancelOrder(action.data.orderId)
						if (result.code === 200) {
							ElMessage.success('订单已取消')
							await refreshCard()
						} else {
							ElMessage.error(result.message || '取消订单失败')
						}
					} catch (error) {
						if (error !== 'cancel') {
							console.error('取消订单失败:', error)
							ElMessage.error('取消订单失败，请稍后重试')
						}
					}
					break
				}
				case 'urge': {
					try {
						const result = await orderActions.urgeOrder(action.data.orderId)
						if (result.code === 200) {
							ElMessage.success('已通知商家尽快处理您的订单')
						} else {
							ElMessage.warning(result.message || '催单请求已发送')
						}
					} catch (error) {
						ElMessage.success('已通知商家尽快处理您的订单')
					}
					break
				}
				case 'add_to_cart': {
					try {
						const result = await cartActions.addToCart(userId, action.data.dishId, 1)
						if (result.code === 200) {
							ElMessage.success(`已将 ${action.data.dishName} 加入购物车`)
						} else {
							ElMessage.warning(result.message || '已添加到购物车')
						}
					} catch (error) {
						ElMessage.error('加入购物车失败，请稍后重试')
					}
					break
				}
				case 'remove_favorite': {
					try {
						await ElMessageBox.confirm('确认取消收藏此菜品？', '提示', {
							confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
						})
						const result = await favoriteActions.removeFavorite(userId, action.data.dishId)
						if (result.code === 200) {
							ElMessage.success('已取消收藏')
							await refreshCard()
						} else {
							ElMessage.error(result.message || '取消收藏失败')
						}
					} catch (error) {
						if (error !== 'cancel') {
							ElMessage.error('取消收藏失败，请稍后重试')
						}
					}
					break
				}
				case 'delete': {
					try {
						await ElMessageBox.confirm('确认删除此评价？删除后无法恢复。', '提示', {
							confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
						})
						const result = await reviewActions.deleteReview(action.data.reviewId)
						if (result.code === 200) {
							ElMessage.success('评价已删除')
							await refreshCard()
						} else {
							ElMessage.error(result.message || '删除评价失败')
						}
					} catch (error) {
						if (error !== 'cancel') {
							ElMessage.error('删除评价失败，请稍后重试')
						}
					}
					break
				}
				case 'edit_profile': {
					router.push({ name: 'user-profile' })
						.catch(() => ElMessage.info('跳转到个人资料编辑页面'))
					break
				}
				case 'view_health': {
					router.push({ name: 'user-calorie' })
						.catch(() => ElMessage.info('跳转到健康分析页面'))
					break
				}
				case 'add_favorite': {
					try {
						const result = await favoriteActions.addFavorite(userId, action.data.dishId)
						if (result.code === 200) {
							ElMessage.success(`已收藏 ${action.data.dishName}`)
						} else {
							ElMessage.warning(result.message || '收藏成功')
						}
					} catch (error) {
						ElMessage.error('收藏失败，请稍后重试')
					}
					break
				}
				case 'view_detail': {
					if (action.data.dishId) {
						router.push({ name: 'dish-detail', params: { dishId: action.data.dishId } })
							.catch(() => ElMessage.info(`查看 ${action.data.dishName} 详情`))
					}
					break
				}
				default:
					ElMessage.info(`操作: ${action.type}`)
			}
		} catch (error) {
			console.error('处理卡片操作失败:', error)
			ElMessage.error('操作失败，请稍后重试')
		}
	}

	return {
		lastQueryType,
		lastQueryMessageIndex,
		convertToSupportedCardType,
		parseCardDataFromContent,
		handleCardAction,
		refreshCard,
		restoreCardDataForMessages,
	}
}
