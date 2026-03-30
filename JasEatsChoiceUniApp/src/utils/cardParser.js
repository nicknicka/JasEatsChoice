/**
 * 卡片数据解析工具
 * 参照桌面端实现，解析SupervisorAgent返回的卡片数据
 *
 * 卡片数据格式：
 * [CARD_DATA_START]
 * {
 *   "type": "dish_list_card",
 *   "data": { ... }
 * }
 * [CARD_DATA_END]
 */

/**
 * 从消息内容中提取卡片数据和类型
 * @param {string} content - 原始消息内容
 * @returns {object} { content: 纯文本内容, cardData: 卡片数据, messageType: 消息类型 }
 */
export function parseCardDataFromContent(content) {
	if (!content || typeof content !== 'string') {
		return { content, cardData: null, messageType: null };
	}

	// 检查是否包含卡片数据标记
	if (!content.includes('[CARD_DATA_START]') || !content.includes('[CARD_DATA_END]')) {
		return { content, cardData: null, messageType: null };
	}

	console.log('🔍 [UniApp] 检测到卡片数据标记');

	// 提取卡片数据
	const cardDataStart = content.indexOf('[CARD_DATA_START]');
	const cardDataEnd = content.indexOf('[CARD_DATA_END]');

	if (cardDataStart === -1 || cardDataEnd === -1) {
		console.warn('⚠️ [UniApp] 卡片数据标记不完整');
		return { content, cardData: null, messageType: null };
	}

	const cardDataString = content.substring(
		cardDataStart + '[CARD_DATA_START]'.length,
		cardDataEnd
	).trim();

	// 移除卡片数据标记，只保留文本内容
	const cleanContent = content.substring(0, cardDataStart).trim();

	let cardData = null;
	let messageType = null;

	try {
		// 解析卡片数据
		const cardDataArray = JSON.parse(cardDataString);

		if (!Array.isArray(cardDataArray) || cardDataArray.length === 0) {
			console.warn('⚠️ [UniApp] 卡片数据格式错误：不是数组或为空');
			return { content: cleanContent, cardData: null, messageType: null };
		}

		const firstCard = cardDataArray[0];
		const cardType = firstCard.type;

		console.log('✅ [UniApp] 原始卡片类型:', cardType);

		// 转换为支持的卡片类型
		messageType = convertToSupportedCardType(cardType);

		if (messageType) {
			console.log('✅ [UniApp] 转换后的消息类型:', messageType);

			// 提取卡片数据（根据不同类型提取不同字段）
			if (firstCard.recommendations) {
				cardData = { recommendations: firstCard.recommendations };
			} else if (firstCard.orders) {
				cardData = { orders: firstCard.orders };
			} else if (firstCard.dishes) {
				cardData = { dishes: firstCard.dishes };
			} else if (cardType === 'dish') {
				// 菜品卡片：整个数组就是菜品列表，需要字段映射
				const mappedDishes = cardDataArray.map(dish => ({
					dishId: dish.dishId || dish.id,
					dishName: dish.dishName || dish.title,
					imageUrl: dish.imageUrl || dish.image,
					description: dish.description || dish.highlight,
					price: dish.price,
					rating: dish.rating,
					category: dish.category,
					tags: dish.tags || [],
					actions: dish.actions || []
				}));
				cardData = { dishes: mappedDishes };
				console.log('✅ [UniApp] 菜品数组已映射并包装');
			} else {
				// 其他情况，直接使用整个对象
				cardData = firstCard;
			}

			console.log('✅ [UniApp] 卡片数据:', cardData);
		}
	} catch (error) {
		console.warn('⚠️ [UniApp] 解析卡片数据失败:', error.message);
	}

	return { content: cleanContent, cardData, messageType };
}

/**
 * 转换为支持的卡片类型
 * @param {string} cardType - 原始卡片类型
 * @returns {string|null} 转换后的消息类型
 */
function convertToSupportedCardType(cardType) {
	if (!cardType) return null;

	// 类型映射表（参照桌面端）
	const typeMapping = {
		'recommendation_dish': 'dish_list_card',
		'recommendation_recipe': 'dish_list_card',
		'dish': 'dish_list_card',
		'dishlist': 'dish_list_card',
		'orderlist': 'order_list_card',
		'order': 'order_list_card',
		'favoritelist': 'favorite_list_card',
		'favorite': 'favorite_list_card',
		'reviewlist': 'review_list_card',
		'review': 'review_list_card',
		'couponlist': 'coupon_list_card',
		'coupon': 'coupon_list_card',
		'userinfo': 'user_info_card',
		'user_info': 'user_info_card',
	};

	// 如果在映射表中，使用映射值
	if (typeMapping[cardType]) {
		return typeMapping[cardType];
	}

	// 如果前端已经支持这个类型，直接返回
	const supportedTypes = [
		'order_list_card',
		'favorite_list_card',
		'review_list_card',
		'coupon_list_card',
		'user_info_card',
		'dish_list_card'
	];

	if (supportedTypes.includes(cardType)) {
		return cardType;
	}

	// 默认返回 null（不显示卡片）
	console.warn('⚠️ [UniApp] 未知的卡片类型:', cardType);
	return null;
}

/**
 * 解析卡片数据（处理JSON字符串或对象）
 * @param {string|object} cardData - 卡片数据
 * @returns {object|null} 解析后的卡片数据
 */
export function parseCardData(cardData) {
	if (!cardData) return null;

	// 如果已经是对象，直接返回
	if (typeof cardData === 'object') {
		return cardData;
	}

	// 如果是字符串，解析JSON
	if (typeof cardData === 'string') {
		try {
			return JSON.parse(cardData);
		} catch (error) {
			console.error('❌ [UniApp] 解析卡片数据失败:', error);
			return null;
		}
	}

	return null;
}

/**
 * 检查消息是否包含卡片数据
 * @param {object} message - 消息对象
 * @returns {boolean} 是否包含卡片
 */
export function hasCardData(message) {
	return !!(message.messageType && message.cardData);
}
