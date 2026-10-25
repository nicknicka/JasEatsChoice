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
	let cleanContent = content.substring(0, cardDataStart).trim();

	// 🔧 如果卡片前没有文本，尝试从卡片数据中提取可读文本
	if (!cleanContent) {
		try {
			const cardObj = JSON.parse(cardDataString);
			// 如果是数组，取第一个对象
			const data = Array.isArray(cardObj) ? cardObj[0] : cardObj;

			// 构建可读文本
			if (data.title) {
				cleanContent = `**${data.title}**\n`;
			}
			if (data.subtitle) {
				cleanContent += `${data.subtitle}\n\n`;
			}

			// 处理 stats 数组
			if (data.stats && Array.isArray(data.stats)) {
				data.stats.forEach(stat => {
					cleanContent += `**${stat.label}**: ${stat.value}\n`;
				});
				cleanContent += '\n';
			}

			// 添加建议
			if (data.suggestion) {
				cleanContent += `${data.suggestion}`;
			}

			// 处理 recommendations/dishes/orders
			if (data.recommendations && Array.isArray(data.recommendations)) {
				data.recommendations.forEach(item => {
					cleanContent += `- ${item.name || item.title}\n`;
				});
			}

			cleanContent = cleanContent.trim();
			console.log('📝 [UniApp] 从卡片数据中提取文本:', cleanContent.substring(0, 100));
		} catch (e) {
			console.warn('⚠️ [UniApp] 提取卡片文本失败:', e.message);
			cleanContent = '';
		}
	}

	let cardData = null;
	let messageType = null;

	try {
		// 解析卡片数据
		let parsedData = JSON.parse(cardDataString);

		// 🔧 兼容两种格式：
		// 1. 数组格式：[{ type: "...", recommendations: [...] }]
		// 2. 对象格式：{ type: "...", recommendations: [...] }
		let cardDataArray = Array.isArray(parsedData) ? parsedData : [parsedData];

		if (cardDataArray.length === 0) {
			console.warn('⚠️ [UniApp] 卡片数据为空');
			return { content: cleanContent, cardData: null, messageType: null };
		}

		const firstCard = cardDataArray[0];
		const cardType = firstCard.type || firstCard.cardType;

		console.log('✅ [UniApp] 原始卡片类型:', cardType);
		console.log('📋 [UniApp] 原始卡片数据:', firstCard);

		// 转换为支持的卡片类型
		messageType = convertToSupportedCardType(cardType);

		if (messageType) {
			console.log('✅ [UniApp] 转换后的消息类型:', messageType);

			// 🔧 提取卡片数据（支持多种字段名）
			if (firstCard.recommendations) {
				cardData = { recommendations: firstCard.recommendations };
			} else if (firstCard.orders) {
				cardData = { orders: firstCard.orders };
			} else if (firstCard.dishes) {
				cardData = { dishes: firstCard.dishes };
			} else if (firstCard.stats || cardType === 'health' || cardType === 'food_recommendation') {
				// 健康统计卡片（health类型）或食物推荐
				cardData = {
					title: firstCard.title || firstCard.name || '健康建议',
					subtitle: firstCard.subtitle || firstCard.description || '',
					stats: firstCard.stats || firstCard.data || [],
					suggestion: firstCard.suggestion || firstCard.recommendation || ''
				};
				console.log('✅ [UniApp] 健康卡片数据已提取:', cardData);
			} else if (cardType === 'dish' || cardType === 'food_recommendation') {
				// 菜品卡片：整个数组就是菜品列表，需要字段映射
				const mappedDishes = cardDataArray.map(dish => ({
					dishId: dish.dishId || dish.id,
					dishName: dish.dishName || dish.title || dish.name,
					imageUrl: dish.imageUrl || dish.image,
					description: dish.description || dish.highlight || dish.recommendation,
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
		// 菜品推荐相关
		'recommendation_dish': 'dish_list_card',
		'recommendation_recipe': 'dish_list_card',
		'dish': 'dish_list_card',
		'dishlist': 'dish_list_card',
		'food_recommendation': 'dish_list_card',

		// 订单相关
		'orderlist': 'order_list_card',
		'order': 'order_list_card',

		// 收藏相关
		'favoritelist': 'favorite_list_card',
		'favorite': 'favorite_list_card',

		// 评价相关
		'reviewlist': 'review_list_card',
		'review': 'review_list_card',

		// 优惠券相关
		'couponlist': 'coupon_list_card',
		'coupon': 'coupon_list_card',

		// 用户信息相关
		'userinfo': 'user_info_card',
		'user_info': 'user_info_card',

		// 健康建议
		'health': 'health_card',
		'food_recommendation': 'health_card',
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
		'dish_list_card',
		'health_card'
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
