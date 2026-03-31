import { debug, info, warn, error } from '@/utils/logger.js'
/**
 * useChatMessages - 聊天消息管理 Composable
 *
 * 功能：
 * - 消息列表管理
 * - 消息添加和更新
 * - 滚动控制
 * - 消息持久化
 *
 * @author Claude
 * @date 2026-03-31
 */

import { ref, computed, nextTick } from 'vue'
import { formatTime } from '@/utils/helper'
import { aiApi } from '@/api'

/**
 * @typedef {Object} ChatMessage
 * @property {string} id - 消息ID (唯一标识符)
 * @property {'user'|'ai'} sender - 发送者
 * @property {string} content - 消息内容
 * @property {string} time - 时间戳
 * @property {boolean} isUser - 是否为用户消息
 * @property {string} [avatar] - 头像（emoji）
 * @property {string|null} [messageType] - 消息类型 (dish_list_card, order_list_card 等)
 * @property {Object|null} [cardData] - 卡片数据
 */

/**
 * 聊天消息管理 Composable
 * @returns {Object} 消息管理相关的状态和方法
 */
export function useChatMessages() {
	// ==================== 状态 ====================
	/** @type {import('vue').Ref<ChatMessage[]>} 消息列表 */
	const messages = ref([])

	/** @type {import('vue').Ref<string>} 滚动位置 */
	const scrollIntoView = ref('')

	/** @type {import('vue').Ref<boolean>} 是否已加载历史记录 */
	const hasLoadedHistory = ref(false)

	// ==================== 计算属性 ====================

	/**
	 * 是否显示欢迎消息
	 * @type {import('vue').ComputedRef<boolean>}
	 */
	const isShowWelcome = computed(() => {
		return (
			messages.value.length === 1 &&
			messages.value[0].sender === 'ai' &&
			!hasLoadedHistory.value
		)
	})

	/**
	 * 是否为空状态
	 * @type {import('vue').ComputedRef<boolean>}
	 */
	const isEmpty = computed(() => {
		return messages.value.length === 0
	})

	// ==================== 方法 ====================

	/**
	 * 生成唯一的消息ID
	 * @returns {string} 唯一ID
	 */
	const generateMessageId = () => {
		return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
	}

	/**
	 * 添加消息到列表
	 * @param {Omit<ChatMessage, 'id'|'time'>} msgData - 消息数据（不含id和时间）
	 * @returns {ChatMessage} 添加后的完整消息对象
	 */
	const addMessage = (msgData) => {
		const message = {
			id: generateMessageId(),
			time: formatTime(new Date()),
			...msgData
		}
		messages.value.push(message)
		nextTick(() => scrollToBottom())
		return message
	}

	/**
	 * 更新消息内容
	 * @param {number} index - 消息索引
	 * @param {Partial<ChatMessage>} updates - 要更新的字段
	 */
	const updateMessage = (index, updates) => {
		if (index >= 0 && index < messages.value.length) {
			messages.value[index] = {
				...messages.value[index],
				...updates
			}
		}
	}

	/**
	 * 追加消息内容（用于流式响应）
	 * @param {number} index - 消息索引
	 * @param {string} content - 要追加的内容
	 */
	const appendContent = (index, content) => {
		if (index >= 0 && index < messages.value.length) {
			messages.value[index].content += content
		}
	}

	/**
	 * 滚动到底部
	 * @returns {Promise<void>}
	 */
	const scrollToBottom = async () => {
		await nextTick()
		if (messages.value.length > 0) {
			scrollIntoView.value = 'msg-' + (messages.value.length - 1)
		}
	}

	/**
	 * 添加欢迎消息
	 */
	const addWelcomeMessage = () => {
		messages.value = [
			{
				id: Date.now(),
				sender: 'ai',
				content: '您好！我是AI饮食助手，有什么可以帮您的吗？',
				time: formatTime(new Date()),
				avatar: '🤖',
				isUser: false
			}
		]
	}

	/**
	 * 清空消息列表
	 */
	const clearMessages = () => {
		messages.value = []
	}

	/**
	 * 加载聊天历史记录
	 * @param {string} userId - 用户ID
	 * @returns {Promise<boolean>} 是否成功加载
	 */
	const loadHistory = async (userId) => {
		// 输入验证
		if (!userId || typeof userId !== 'string') {
			error('无效的用户ID', new Error('userId must be a non-empty string'), 'useChatMessages')
			return false
		}

		// 避免重复加载
		if (hasLoadedHistory.value) {
			debug('已加载过历史记录，跳过重复加载', null, 'useChatMessages')
			return true
		}

		try {
			debug('开始加载聊天记录', { userId }, 'useChatMessages')

			const historyResponse = await aiApi.getHistory(userId)
			debug('后端响应', historyResponse, 'useChatMessages')

			if (
				historyResponse.code === 200 &&
				historyResponse.data &&
				historyResponse.data.length > 0
			) {
				// 转换为前端格式
				messages.value = historyResponse.data.map((item, index) => {
					// 清理AI消息中的markdown代码块
					const cleanedContent =
						item.sender === 'ai' && item.content
							? aiApi.cleanMarkdownCodeBlocks(item.content)
							: item.content

					return {
						id: index + 1,
						sender: item.sender,
						content: cleanedContent,
						time: formatTime(new Date(item.createTime)),
						avatar: item.sender === 'ai' ? '🤖' : '👤',
						isUser: item.sender === 'user'
					}
				})
				hasLoadedHistory.value = true
				info('成功加载聊天历史', { count: messages.value.length }, 'useChatMessages')
				return true
			} else {
				// 没有历史记录，添加欢迎消息
				if (messages.value.length === 0) {
					debug('没有历史记录，显示欢迎消息', null, 'useChatMessages')
					addWelcomeMessage()
				}
				hasLoadedHistory.value = true
				return false
			}
		} catch (error) {
			error('加载聊天记录失败', error, 'useChatMessages')

			// 加载失败时，添加欢迎消息
			if (messages.value.length === 0) {
				addWelcomeMessage()
			}
			hasLoadedHistory.value = true
			return false
		}
	}

	/**
	 * 保存消息到后端（带重试机制）
	 * @param {string} sender - 发送者 (user/ai)
	 * @param {string} content - 消息内容
	 * @param {string|null} messageType - 消息类型
	 * @param {Object|null} cardData - 卡片数据
	 * @param {number} retryCount - 当前重试次数
	 * @returns {Promise<boolean>} 是否保存成功
	 */
	const saveMessageToBackend = async (
		sender,
		content,
		messageType = null,
		cardData = null,
		retryCount = 0
	) => {
		const maxRetries = 3

		try {
			const userId = getUserId()
			debug(`开始保存${sender}消息到后端`, {
				userId,
				sender,
				messageType,
				hasCardData: !!cardData,
				content: content.substring(0, 50) + (content.length > 50 ? '...' : ''),
				timestamp: new Date().toISOString()
			})

			const requestData = {
				userId,
				sender,
				content
			}

			if (messageType) {
				requestData.messageType = messageType
			}
			if (cardData) {
				requestData.cardData = cardData
			}

			const response = await aiApi.saveMessage(requestData)

			if (
				response &&
				(response.success === true || response.code === 200 || response.code === '200')
			) {
				debug(`${sender}消息保存成功`, {
					code: response.code,
					message: response.message,
					timestamp: new Date().toISOString()
				})
				return true
			} else {
				throw new Error(response?.message || '保存失败')
			}
		} catch (error) {
			error(`${sender}消息保存失败`, {
				error: error.message || error,
				retryCount: `${retryCount + 1}/${maxRetries}`,
				timestamp: new Date().toISOString()
			})

			if (retryCount < maxRetries) {
				warn(`重试保存消息 (${retryCount + 1}/${maxRetries})`, null, 'useChatMessages')
				await new Promise((resolve) => setTimeout(resolve, 1000 * (retryCount + 1)))
				return saveMessageToBackend(sender, content, messageType, cardData, retryCount + 1)
			} else {
				error(`${sender}消息保存失败，已达到最大重试次数 (${maxRetries})`, null, 'useChatMessages')
				return false
			}
		}
	}

	/**
	 * 清空聊天历史
	 * @param {string} userId - 用户ID
	 * @returns {Promise<boolean>} 是否成功清空
	 */
	const clearHistory = async (userId) => {
		try {
			debug('开始清空聊天记录', { userId }, 'useChatMessages'

			const clearResponse = await aiApi.clearHistory(userId)

			if (clearResponse.code === 200) {
				info('后端清空成功', null, 'useChatMessages')

				// 清空前端显示
				clearMessages()

				// 添加欢迎消息
				addWelcomeMessage()

				// 清空本地存储
				uni.removeStorageSync('chatHistory')

				// 重置加载状态
				hasLoadedHistory.value = true

				return true
			} else {
				error('后端清空失败', new Error(clearResponse.message), 'useChatMessages')
				return false
			}
		} catch (error) {
			error('清空聊天记录失败', error, 'useChatMessages')
			return false
		}
	}

	/**
	 * 保存聊天历史到本地存储
	 */
	const saveToLocal = () => {
		try {
			uni.setStorageSync('chatHistory', JSON.stringify(messages.value))
		} catch (error) {
			error('保存聊天历史失败', error, 'useChatMessages')
		}
	}

	// ==================== 返回公共接口 ====================
	return {
		// 状态
		messages,
		scrollIntoView,
		hasLoadedHistory,
		isShowWelcome,
		isEmpty,

		// 方法
		addMessage,
		updateMessage,
		appendContent,
		scrollToBottom,
		addWelcomeMessage,
		clearMessages,
		loadHistory,
		saveMessageToBackend,
		clearHistory,
		saveToLocal
	}
}

/**
 * 获取用户ID
 * @returns {string} 用户ID
 */
function getUserId() {
	// 优先从 store 获取（简化版）
	const userId = uni.getStorageSync('userId')
	if (userId) {
		return userId
	}

	// 开发环境使用默认值
	const isDevelopment = process.env.NODE_ENV === 'development'
	if (isDevelopment) {
		warn('开发环境：使用默认测试用户ID', null, 'useChatMessages')
		return '1'
	} else {
		error('生产环境：无法获取用户ID', null, 'useChatMessages')
		return ''
	}
}
