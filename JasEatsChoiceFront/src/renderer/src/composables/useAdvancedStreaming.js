import { ref, nextTick } from 'vue'
import { API_CONFIG } from '../config/index'
import { useAuthStore } from '../store/authStore'
import {
	Search, User, Apple, Document, Shop, Clock, Location, Tools,
	ChatDotRound, CircleCheck, Loading
} from '@element-plus/icons-vue'

/**
 * 增强版流式传输
 * 支持进度指示器、卡片数据收集、SSE解析
 * @param {Object} options - 配置选项
 * @param {import('vue').Ref<Array>} options.messages - 消息列表引用
 * @param {import('vue').Ref<boolean>} options.isMounted - 组件挂载状态
 * @param {Function} options.getMessage - 获取消息对象的方法
 * @param {Function} options.parseCardDataFromContent - 解析卡片数据的方法
 * @param {Function} options.convertToSupportedCardType - 卡片类型转换方法
 * @param {Function} options.validateAndSaveMessage - 验证保存消息方法
 * @param {Function} options.handleFinalResult - 处理最终结果方法
 * @param {Function} options.updateUI - UI更新回调
 * @returns {Object} 流式传输方法和状态
 */
export function useAdvancedStreaming(options) {
	const {
		messages, isMounted, getMessage, parseCardDataFromContent,
		convertToSupportedCardType, validateAndSaveMessage, handleFinalResult,
		updateUI
	} = options

	const authStore = useAuthStore()
	const getUserId = () => String(authStore.userId)

	const isStreaming = ref(false)
	const abortController = ref(null)

	// ========== 进度指示器相关 ==========

	const getProgressIcon = (message) => {
		const text = message.content || ''
		if (text.includes('搜索菜品') || text.includes('菜品搜索')) return Search
		if (text.includes('分析偏好') || text.includes('偏好分析')) return User
		if (text.includes('营养') || text.includes('分析营养成分')) return Apple
		if (text.includes('订单') || text.includes('处理订单')) return Document
		if (text.includes('商家') || text.includes('查询商家')) return Shop
		if (text.includes('时段') || text.includes('分析时段')) return Clock
		if (text.includes('位置') || text.includes('查询位置')) return Location
		if (text.includes('查询数据') || text.includes('数据查询')) return Tools
		if (text.includes('分析需求') || text.includes('需求分析')) return ChatDotRound
		if (text.includes('完成')) return CircleCheck
		return Loading
	}

	const getProgressText = (message) => {
		if (message.content && message.content.trim()) return message.content.trim()
		return 'AI正在处理中...'
	}

	const getProgressDots = (message) => {
		const status = message.completed ? null : (message.isThinking ? 'thinking' : (message.progress === true ? 'executing' : null))
		return status === 'thinking' || status === 'executing'
	}

	const getProgressClass = (message) => {
		const status = message.completed ? null : (message.isThinking ? 'thinking' : (message.progress === true ? 'executing' : null))
		return {
			'status-thinking': status === 'thinking',
			'status-executing': status === 'executing',
			'status-completed': status === 'completed',
		}
	}

	// ========== 流式传输核心 ==========

	/**
	 * 解析SSE数据
	 */
	const _parseSSEData = (data) => {
		let parsedData
		let isPlainText = false

		try {
			if (data.startsWith('[')) {
				const dataArray = JSON.parse(data)
				const actualDataItem = dataArray.find(item => {
					const d = item.data
					return d && typeof d === 'object' && !d.mediaType &&
						(d.hasOwnProperty('done') || d.hasOwnProperty('content') || d.hasOwnProperty('card_data'))
				})
				if (actualDataItem?.data) parsedData = actualDataItem.data
			} else if (data.startsWith('{')) {
				parsedData = JSON.parse(data)
				if (parsedData.char !== undefined) {
					isPlainText = true
					parsedData = { content: parsedData.char, done: false }
				}
			} else {
				isPlainText = true
				parsedData = { content: data, done: false }
			}
		} catch (error) {
			if (data.length > 0 && !data.startsWith('[')) {
				return { content: data, done: false, _isPlainText: true }
			}
			return null
		}

		if (parsedData) parsedData._isPlainText = isPlainText
		return parsedData
	}

	/**
	 * 处理卡片数据标记
	 * 支持：精确匹配（标记独占一行）和内嵌匹配（标记和文本混在同一 chunk 中）
	 */
	const _processCardDataMarkers = (data, isCollectingCardData, cardDataBuffer, messageIndex) => {
		const trimmed = data.trim()

		// 精确匹配：标记独占一个 data 字段
		if (trimmed === '[CARD_DATA_START]') {
			return { action: 'start_collect', cardDataBuffer: '', isCollectingCardData: true }
		}

		if (trimmed === '[CARD_DATA_END]') {
			if (cardDataBuffer.trim()) {
				_finalizeCardData(cardDataBuffer, messageIndex)
			}
			return { action: 'end_collect', cardDataBuffer: '', isCollectingCardData: false }
		}

		// 增强：内嵌标记检测（标记和文本混在同一 chunk 中）
		if (!isCollectingCardData && trimmed.includes('[CARD_DATA_START]')) {
			const idx = trimmed.indexOf('[CARD_DATA_START]')
			const after = trimmed.substring(idx + '[CARD_DATA_START]'.length)

			if (after.includes('[CARD_DATA_END]')) {
				const endIdx = after.indexOf('[CARD_DATA_END]')
				_finalizeCardData(after.substring(0, endIdx).trim(), messageIndex)
				return { action: 'complete', cardDataBuffer: '', isCollectingCardData: false }
			}

			return { action: 'start_collect', cardDataBuffer: after, isCollectingCardData: true }
		}

		// 正在收集时检测结束标记
		if (isCollectingCardData && trimmed.includes('[CARD_DATA_END]')) {
			const endIdx = trimmed.indexOf('[CARD_DATA_END]')
			cardDataBuffer += trimmed.substring(0, endIdx)
			_finalizeCardData(cardDataBuffer.trim(), messageIndex)
			return { action: 'end_collect', cardDataBuffer: '', isCollectingCardData: false }
		}

		if (isCollectingCardData) {
			return { action: 'collect', cardDataBuffer: cardDataBuffer + data, isCollectingCardData: true }
		}

		return null
	}

	/**
	 * 解析并应用卡片数据到消息
	 */
	const _finalizeCardData = (cardDataBuffer, messageIndex) => {
		try {
			let dataToParse = cardDataBuffer.trim()
			if (!dataToParse.startsWith('[')) dataToParse = `[${dataToParse}]`

			const parsedArray = JSON.parse(dataToParse)
			if (!Array.isArray(parsedArray) || parsedArray.length === 0) return

			const firstCard = parsedArray[0]
			const cardType = firstCard.type || firstCard.cardType

			if (cardType) {
				const messageType = convertToSupportedCardType(cardType)
				const message = getMessage(messageIndex)
				if (message && messageType) message.messageType = messageType
			}

			const message = getMessage(messageIndex)
			if (!message) return

			if (firstCard.recommendations) {
				message.cardData = { recommendations: firstCard.recommendations }
			} else if (firstCard.orders) {
				message.cardData = { orders: firstCard.orders }
			} else if (firstCard.dishes) {
				message.cardData = {
					dishes: firstCard.dishes.map(dish => ({
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
			} else if (cardType === 'dish') {
				message.cardData = {
					dishes: parsedArray.map(dish => ({
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
				message.cardData = firstCard
			}
		} catch (error) {
			console.warn('卡片数据解析失败:', error.message)
		}
	}

	/**
	 * 处理解析后的SSE数据
	 */
	const _processParsedData = async (parsedData, messageIndex) => {
		if (!parsedData) return

		// done 标记
		if (parsedData.done === true) {
			const message = getMessage(messageIndex)
			if (message) await validateAndSaveMessage(messageIndex)
			return { shouldReturn: true }
		}

		// 完成标记
		if (parsedData.completed === true) {
			const message = getMessage(messageIndex)
			if (message) {
				message.progress = false
				message.completed = true
			}
			return { shouldContinue: true }
		}

		// 进度消息
		if (parsedData.progress === true && parsedData.message) {
			const message = getMessage(messageIndex)
			if (message) {
				message.content = parsedData.message
				message.progress = true
			}
			return { shouldContinue: true }
		}

		// 识别消息类型并标记
		if (parsedData.type && ['dish', 'merchant', 'order', 'health'].includes(parsedData.type)) {
			let cardContent = `**${parsedData.title}**\n`
			if (parsedData.subtitle) cardContent += `${parsedData.subtitle}\n`
			if (parsedData.description) cardContent += `\n${parsedData.description}`
			parsedData.content = cardContent
			parsedData._isFinalResult = true
			parsedData._cardData = parsedData
			parsedData._cardType = parsedData.type
		} else if (parsedData.type === 'info' && parsedData.content) {
			parsedData._isFinalResult = true
		} else if (parsedData.agentName === 'SupervisorAgent' && parsedData.output) {
			parsedData.content = parsedData.output
			parsedData._isFinalResult = true
		}

		// 处理 card_data 字段（后端发来的 card_data 是 JSON 字符串，需先解析）
		if (parsedData.card_data) {
			const message = getMessage(messageIndex)
			if (message) {
				try {
					let cardDataObj = parsedData.card_data
					if (typeof cardDataObj === 'string') {
						cardDataObj = JSON.parse(cardDataObj)
					}

					let cardArray = Array.isArray(cardDataObj) ? cardDataObj : [cardDataObj]
					if (cardArray.length > 0) {
						const firstCard = cardArray[0]
						const cardType = firstCard.type || firstCard.cardType

						if (cardType) {
							const messageType = convertToSupportedCardType(cardType)
							if (messageType) message.messageType = messageType
						}

						if (firstCard.orders) {
							message.cardData = { orders: firstCard.orders }
						} else if (firstCard.dishes) {
							message.cardData = {
								dishes: firstCard.dishes.map(dish => ({
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
						} else if (firstCard.recommendations) {
							message.cardData = { recommendations: firstCard.recommendations }
						} else {
							message.cardData = firstCard
						}

						if (updateUI) await updateUI()
					}
				} catch (error) {
					console.warn('更新卡片数据失败:', error.message)
				}
			}
		}

		// 处理 content 字段
		if (parsedData.content) {
			const message = getMessage(messageIndex)
			if (!message) return { shouldBreak: true }

			// 清除思考/进度状态
			if (message.isThinking) message.isThinking = false
			if (message.progress === true) message.progress = false

			let newContent = parsedData.content

			// 过滤卡片数据标记（防止流式过程中标记泄漏到显示内容）
			if (newContent) {
				const cardStartIdx = newContent.indexOf('[CARD_DATA_START]')
				if (cardStartIdx !== -1) newContent = newContent.substring(0, cardStartIdx)
				const cardEndIdx = newContent.indexOf('[CARD_DATA_END]')
				if (cardEndIdx !== -1) newContent = newContent.substring(0, cardEndIdx)
			}

			// 解析内容中的卡片数据
			if (parseCardDataFromContent) {
				const { content: cleanContent, cardData, messageType } = parseCardDataFromContent(newContent)
				if (cardData) message.cardData = cardData
				if (messageType) message.messageType = messageType
				newContent = cleanContent
			}

			// 工具提示过滤
			const toolPromptRegex = /🔧\s*正在执行工具函数[.。]{0,3}/
			const hasToolPrompt = toolPromptRegex.test(newContent)

			message.content = (message.content || '') + newContent
			message.displayContent = (message.displayContent || '') + newContent.replace(toolPromptRegex, '')

			if (updateUI) await updateUI()

			// 处理最终结果
			if (parsedData._isFinalResult && handleFinalResult) {
				await handleFinalResult(messageIndex, parsedData)
			}
		}

		return {}
	}

	/**
	 * 流式传输主函数
	 */
	const streamResponse = async (messageIndex, reader) => {
		isStreaming.value = true

		if (!messages.value[messageIndex]) {
			isStreaming.value = false
			return
		}

		const msg = messages.value[messageIndex]
		msg.content ??= ''
		msg.displayContent ??= ''

		const decoder = new TextDecoder()
		let buffer = ''
		let cardDataBuffer = ''
		let isCollectingCardData = false

		try {
			while (true) {
				if (!isMounted.value) break

				const { done, value } = await reader.read()
				if (done) break

				const chunk = decoder.decode(value, { stream: true })
				buffer += chunk
				const lines = buffer.split('\n')
				buffer = lines.pop() || ''

				let currentEvent = 'message'

				for (const line of lines) {
					const trimmedLine = line.trim()

					if (trimmedLine.startsWith('event:')) {
						currentEvent = trimmedLine.substring(6).trim()
						continue
					}

					if (currentEvent === 'end' || currentEvent === 'error') {
						const message = getMessage(messageIndex)
						if (message) await validateAndSaveMessage(messageIndex)
						return
					}

					if (currentEvent !== 'message') continue
					if (!trimmedLine.startsWith('data:')) continue

					const data = trimmedLine.substring(5).trim()
					if (!data) continue

					// 处理卡片数据标记
					const markerResult = _processCardDataMarkers(data, isCollectingCardData, cardDataBuffer, messageIndex)
					if (markerResult) {
						isCollectingCardData = markerResult.isCollectingCardData
						cardDataBuffer = markerResult.cardDataBuffer
						continue
					}

					// 解析SSE数据
					const parsedData = _parseSSEData(data)
					if (!parsedData) continue

					const result = await _processParsedData(parsedData, messageIndex)
					if (result?.shouldReturn || result?.shouldBreak) return
					if (result?.shouldContinue) continue
				}
			}
		} catch (error) {
			if (error.name === 'AbortError') return
			console.error('流式传输错误:', error)
			throw error
		} finally {
			isStreaming.value = false
		}
	}

	/**
	 * 停止流式传输
	 */
	const stopStreaming = () => {
		if (abortController.value) {
			abortController.value.abort()
		}
	}

	/**
	 * 发送流式请求
	 */
	const sendStreamRequest = async (userInput, userId, beforeSend, onError) => {
		abortController.value = new AbortController()

		try {
			const apiUrl = API_CONFIG.baseURL + API_CONFIG.ai.chat
			const response = await fetch(apiUrl, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Accept: 'text/event-stream',
					Authorization: `Bearer ${authStore.token}`,
				},
				body: JSON.stringify({ message: userInput, userId }),
				signal: abortController.value.signal,
			})

			if (!response.ok) {
				throw new Error(`HTTP error! status: ${response.status}`)
			}

			return response.body.getReader()
		} catch (error) {
			if (error.name === 'AbortError') return null
			throw error
		}
	}

	return {
		isStreaming,
		abortController,
		streamResponse,
		stopStreaming,
		sendStreamRequest,
		getProgressIcon,
		getProgressText,
		getProgressDots,
		getProgressClass,
	}
}
