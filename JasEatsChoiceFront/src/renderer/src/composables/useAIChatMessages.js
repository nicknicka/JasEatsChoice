import { ref, nextTick } from 'vue'
import axios from 'axios'
import DOMPurify from 'dompurify'
import { API_CONFIG } from '../config/index'
import { useAuthStore } from '../store/authStore'
import { useUserStore } from '../store/userStore'
import { parseMarkdown } from '../utils/markdownParser'
import { isCardMessage } from '../views/user/AI/utils/cardMapper'

// 欢迎消息常量
const WELCOME_MESSAGE = '您好！我是您的AI饮食助手。\n\n我可以帮助您：\n\n- 推荐健康食谱\n- 分析营养成分\n- 提供饮食建议\n\n有什么可以帮您的吗？'

/**
 * AI聊天消息管理
 * 消息状态、渲染、打字机效果、显示内容管理
 * @param {Object} options - 配置选项
 * @param {Function} [options.parseCardDataFromContent] - 内容解析函数
 * @param {Function} [options.restoreCardDataForMessages] - 卡片数据恢复函数
 * @returns {Object} 消息管理方法和状态
 */
export function useAIChatMessages(options = {}) {
	const authStore = useAuthStore()
	const userStore = useUserStore()

	const getUserId = () => String(authStore.userId)

	// 状态
	const messages = ref([])
	const isMounted = ref(false)
	const isInitialLoading = ref(true)

	/**
	 * 渲染内容（支持Markdown或纯文本）并添加XSS防护
	 */
	const renderContent = (content, useMarkdown) => {
		const renderedContent = useMarkdown
			? parseMarkdown(content)
			: content.replace(/\n/g, '<br>')

		return DOMPurify.sanitize(renderedContent, {
			ALLOWED_TAGS: [
				'p', 'br', 'strong', 'em', 'u', 'a', 'code', 'pre',
				'h1', 'h2', 'h3', 'ul', 'ol', 'li', 'blockquote',
				'table', 'thead', 'tbody', 'tr', 'th', 'td'
			],
			ALLOWED_ATTR: ['href', 'class', 'target'],
			ALLOW_DATA_ATTR: false,
		})
	}

	/**
	 * 获取消息的CSS类
	 */
	const getMessageClasses = (message) => ({
		'chat-message': true,
		'user-message': message.sender === 'user',
		'ai-message': message.sender === 'ai',
	})

	/**
	 * 是否显示卡片消息
	 */
	const shouldShowCard = (message) =>
		message.messageType && isCardMessage(message.messageType)

	/**
	 * 是否显示纯文本内容
	 */
	const shouldShowTextContent = (message) =>
		getDisplayContent(message) && !shouldShowCard(message)

	/**
	 * 是否显示卡片总结文本
	 */
	const shouldShowCardSummary = (message) =>
		getDisplayContent(message) && shouldShowCard(message)

	/**
	 * 是否显示更多操作按钮
	 */
	const shouldShowMoreButton = (message) =>
		!message.messageType || !isCardMessage(message.messageType)

	/**
	 * 判断消息是否应该显示
	 */
	const shouldShowMessage = (message) => {
		if (message.isTyping) return true
		if (message.sender === 'ai' && getProgressStatus(message)) return true
		if (message.displayContent !== undefined) {
			return (
				message.displayContent.length > 0 ||
				(message.messageType && isCardMessage(message.messageType))
			)
		}
		return message.content && message.content.length > 0
	}

	/**
	 * 获取消息的显示内容
	 */
	const getDisplayContent = (message) => {
		if (message.isTyping && message.displayContent !== undefined) return message.displayContent
		if (message.displayContent !== undefined) return message.displayContent
		return message.content || ''
	}

	/**
	 * 获取进度状态
	 */
	const getProgressStatus = (message) => {
		if (message.completed) return null
		if (message.isThinking) return 'thinking'
		if (message.progress === true) return 'executing'
		return null
	}

	/**
	 * 打字机效果
	 */
	const startTypewriterEffect = async (messageIndex, fullText, speed = 30) => {
		const message = messages.value[messageIndex]
		if (!message) return

		message.isTyping = true
		message.typingIndex = 0
		message.showCursor = true
		message.displayContent = ''

		const totalLength = fullText.length
		while (message.typingIndex < totalLength && message.isTyping) {
			const charsToAdd = Math.min(3, totalLength - message.typingIndex)
			message.typingIndex += charsToAdd
			message.displayContent = fullText.substring(0, message.typingIndex)
			await new Promise(resolve => setTimeout(resolve, speed))
			if (isMounted.value) await nextTick()
		}

		message.isTyping = false
		message.showCursor = false
		message.displayContent = fullText
	}

	/**
	 * 保存消息到后端
	 */
	const saveMessageToBackend = async (sender, content, messageType = null, cardData = null) => {
		try {
			const userId = getUserId()
			const payload = { userId, sender, content }
			if (messageType && cardData) {
				payload.messageType = messageType
				payload.cardData = cardData
			}
			await axios.post(API_CONFIG.baseURL + API_CONFIG.ai.save, payload, {
				headers: { Authorization: `Bearer ${authStore.token}` }
			})
		} catch (error) {
			console.error('保存消息到后端失败:', error)
		}
	}

	/**
	 * 验证并保存消息
	 */
	const validateAndSaveMessage = async (messageIndex) => {
		if (!isMounted.value) return false
		const message = messages.value[messageIndex]
		if (!message || !message.content) return false
		if (message._isProgressMessage) return true
		try {
			await saveMessageToBackend('ai', message.content, message.messageType, message.cardData)
			return true
		} catch (error) {
			console.warn('保存消息到后端失败:', error.message)
			return false
		}
	}

	/**
	 * 获取消息对象（带验证）
	 */
	const getMessage = (messageIndex) => {
		if (!isMounted.value || !messages.value[messageIndex]) return null
		return messages.value[messageIndex]
	}

	/**
	 * 加载聊天历史记录
	 */
	const loadMessages = async () => {
		try {
			const userId = getUserId()
			const response = await axios.get(API_CONFIG.baseURL + API_CONFIG.ai.history, {
				params: { userId }
			})

			if (response.data.code === 200 && response.data.data && response.data.data.length > 0) {
				const historyData = response.data.data
				messages.value = historyData.map((item, index) => {
					let parsedResult = { content: item.content, cardData: null, messageType: null }
					if (options.parseCardDataFromContent) {
						parsedResult = options.parseCardDataFromContent(item.content)
					}
					return {
						id: index + 1,
						sender: item.sender,
						content: parsedResult.content,
						displayContent: parsedResult.content || '',
						time: new Date(item.createTime).toLocaleTimeString([], {
							hour: '2-digit', minute: '2-digit'
						}),
						avatar: item.sender === 'ai' ? '🤖' : userStore.userInfo?.avatar || '',
						enableMarkdown: true,
						messageType: parsedResult.messageType || item.messageType || null,
						cardData: parsedResult.cardData || item.cardData || null,
						isToolExecuting: false,
						toolCompleted: false,
						hasToolPrompt: false,
						progress: false,
					}
				})

				if (options.restoreCardDataForMessages) {
					await options.restoreCardDataForMessages()
				}
			} else {
				await _loadWelcomeMessage(userId)
			}
			isInitialLoading.value = false
		} catch (error) {
			console.error('加载聊天记录失败:', error)
			_setFallbackWelcome()
			isInitialLoading.value = false
		}
	}

	// ========== 私有辅助方法 ==========

	const _loadWelcomeMessage = async (userId) => {
		// 无历史记录时直接显示欢迎消息，无需调用后端
		messages.value = [_createWelcomeMessage(WELCOME_MESSAGE)]
	}

	const _setFallbackWelcome = () => {
		messages.value = [{
			id: 1,
			sender: 'ai',
			content: WELCOME_MESSAGE,
			time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
			avatar: '🤖',
			enableMarkdown: true,
		}]
	}

	const _createWelcomeMessage = (content) => ({
		id: 1,
		sender: 'ai',
		content,
		time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
		avatar: '🤖',
		enableMarkdown: true,
	})

	return {
		messages,
		isMounted,
		isInitialLoading,
		loadMessages,
		saveMessageToBackend,
		validateAndSaveMessage,
		getMessage,
		renderContent,
		getMessageClasses,
		shouldShowCard,
		shouldShowTextContent,
		shouldShowCardSummary,
		shouldShowMoreButton,
		shouldShowMessage,
		getDisplayContent,
		getProgressStatus,
		startTypewriterEffect,
	}
}
