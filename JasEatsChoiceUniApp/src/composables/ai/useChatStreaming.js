import { debug, info, warn, error } from '@/utils/logger.js'
/**
 * useChatStreaming - AI 流式响应管理 Composable
 *
 * 功能：
 * - 流式响应控制
 * - 加载状态管理
 * - 请求取消
 * - 卡片数据解析
 *
 * @author Claude
 * @date 2026-03-31
 */

import { ref, nextTick } from 'vue'
import { aiApi } from '@/api'
import { parseCardDataFromContent } from '@/utils/cardParser'

/**
 * 流式响应状态枚举
 * @enum {string}
 */
const StreamingState = {
	IDLE: 'idle', // 空闲
	CONNECTING: 'connecting', // 连接中
	TYPING: 'typing', // AI思考中
	STREAMING: 'streaming' // 流式输出中
}

/**
 * AI 流式响应管理 Composable
 * @returns {Object} 流式响应管理相关的状态和方法
 */
export function useChatStreaming() {
	// ==================== 状态 ====================
	/** @type {import('vue').Ref<StreamingState>} 当前流式状态 */
	const streamingState = ref(StreamingState.IDLE)

	/** @type {import('vue').Ref<boolean>} 是否正在流式传输 */
	const isStreaming = ref(false)

	/** @type {import('vue').Ref<boolean>} 是否正在输入 */
	const isTyping = ref(false)

	// AbortController 用于取消请求
	let abortController = null
	const isAbortControllerSupported = typeof AbortController !== 'undefined'

	/**
	 * 创建 AbortController（含完整的 polyfill）
	 * @returns {Object} AbortController 实例或 polyfill
	 */
	const createAbortController = () => {
		if (isAbortControllerSupported) {
			return new AbortController()
		}

		// 完整的 polyfill 对象
		return {
			signal: {
				aborted: false,
				addEventListener: () => {},
				removeEventListener: () => {}
			},
			abort: function () {
				this.signal.aborted = true
			}
		}
	}

	// ==================== 计算属性 ====================

	/**
	 * 是否处于加载中状态
	 * @type {import('vue').ComputedRef<boolean>}
	 */
	const isLoading = computed(() => {
		return streamingState.value !== StreamingState.IDLE
	})

	/**
	 * 加载状态文本
	 * @type {import('vue').ComputedRef<string>}
	 */
	const loadingText = computed(() => {
		const textMap = {
			[StreamingState.IDLE]: '',
			[StreamingState.CONNECTING]: '连接中...',
			[StreamingState.TYPING]: 'AI思考中...',
			[StreamingState.STREAMING]: 'AI正在输入...'
		}
		return textMap[streamingState.value] || ''
	})

	// ==================== 方法 ====================

	/**
	 * 开始流式对话
	 * @param {string} message - 用户消息
	 * @param {Array} history - 历史消息
	 * @param {Function} onMessage - 消息回调
	 * @param {Function} onComplete - 完成回调
	 * @param {Function} onError - 错误回调
	 * @param {number} timeout - 超时时间（毫秒），默认60000ms
	 * @returns {Promise<void>}
	 */
	const startStreaming = async (
		message,
		history,
		onMessage,
		onComplete,
		onError,
		timeout = 60000
	) => {
		// 输入验证
		if (!message || typeof message !== 'string' || message.trim().length === 0) {
			const error = new Error('消息内容不能为空')
			error('无效的消息内容', error, 'useChatStreaming')
			if (onError) {
				onError(error)
			}
			return
		}

		if (!Array.isArray(history)) {
			warn('历史记录格式不正确，期望为数组', { history }, 'useChatStreaming')
		}

		debug('AI聊天请求开始', null, 'useChatStreaming')
		debug('请求时间', { time: new Date().toLocaleString() }, 'useChatStreaming')
		debug('用户消息', { message }, 'useChatStreaming')

		// 设置超时计时器
		let timeoutId = null
		if (timeout > 0) {
			timeoutId = setTimeout(() => {
				if (isStreaming.value) {
					const timeoutError = new Error(`请求超时 (${timeout}ms)`)
					error('请求超时', timeoutError, 'useChatStreaming')
					stopStreaming()
					if (onError) {
						onError(timeoutError)
					}
				}
			}, timeout)
		}

		// 中断上一个未完成的流式请求
		if (abortController) {
			debug('中断上一个未完成的流式请求', null, 'useChatStreaming')
			if (isAbortControllerSupported) {
				abortController.abort()
			}
			abortController = null
		}

		// 清空上一次的流式状态
		if (isStreaming.value) {
			debug('清空上一次的流式状态', null, 'useChatStreaming')
			isTyping.value = false
			isStreaming.value = false
		}

		// 创建新的 AbortController（使用完整的 polyfill）
		abortController = createAbortController()

		// 更新状态
		streamingState.value = StreamingState.CONNECTING
		isStreaming.value = true

		try {
			// 调用AI流式对话API
			await aiApi.streamChat(
				{
					message,
					conversationId: '',
					history: history.map((msg) => ({
						role: msg.isUser ? 'user' : 'assistant',
						content: msg.content
					}))
				},
				// onMessage - 接收消息内容
				(content) => {
					streamingState.value = StreamingState.STREAMING
					isTyping.value = true

					// 回调处理（追加内容）
					if (onMessage) {
						onMessage(content)
					}

					// 滚动到底部
					nextTick(() => {
						// 滚动逻辑由外部处理
					})
				},
				// onComplete - 完成回调
				async () => {
					// 清除超时计时器
					if (timeoutId) {
						clearTimeout(timeoutId)
						timeoutId = null
					}

					info('AI消息接收完成', null, 'useChatStreaming')
					streamingState.value = StreamingState.IDLE
					isTyping.value = false
					isStreaming.value = false

					if (onComplete) {
						await onComplete()
					}
				},
				// onError - 错误处理
				(error) => {
					// 清除超时计时器
					if (timeoutId) {
						clearTimeout(timeoutId)
						timeoutId = null
					}

					error('AI请求失败', error, 'useChatStreaming')
					streamingState.value = StreamingState.IDLE
					isTyping.value = false
					isStreaming.value = false

					if (onError) {
						onError(error)
					}
				}
			)
		} catch (error) {
			// 清除超时计时器
			if (timeoutId) {
				clearTimeout(timeoutId)
				timeoutId = null
			}

			error('发送消息失败', error, 'useChatStreaming')
			streamingState.value = StreamingState.IDLE
			isTyping.value = false
			isStreaming.value = false

			if (onError) {
				onError(error)
			}
		}
	}

	/**
	 * 停止流式传输
	 */
	const stopStreaming = () => {
		if (abortController) {
			debug('用户主动停止流式传输', null, 'useChatStreaming')
			abortController.abort()

			streamingState.value = StreamingState.IDLE
			isStreaming.value = false
			isTyping.value = false

			uni.showToast({
				title: '已停止AI回复',
				icon: 'none'
			})
		}
	}

	/**
	 * 解析AI响应中的卡片数据
	 * @param {string} content - AI响应内容
	 * @returns {{content: string, cardData: Object|null, messageType: string|null}} 解析后的数据
	 */
	const parseCardData = (content) => {
		debug('开始解析卡片数据', null, 'useChatStreaming')
		const result = parseCardDataFromContent(content)

		debug('卡片数据解析完成', {
			messageType: result.messageType,
			hasCardData: !!result.cardData,
			contentLength: result.content.length
		})

		return result
	}

	/**
	 * 生成模拟AI回复（降级方案）
	 * @param {string} text - 用户消息
	 * @returns {string} AI回复
	 */
	const generateMockResponse = (text) => {
		const responses = {
			推荐健康食谱:
				'根据您的需求，我为您推荐以下健康食谱：\n\n早餐：燕麦牛奶粥配鸡蛋（约420卡）\n午餐：清蒸鲈鱼配时蔬（约580卡）\n晚餐：鸡胸肉蔬菜沙拉（约380卡）\n\n这些食谱营养均衡，适合日常食用。',
			分析营养成分:
				'请告诉我您想分析哪种食物的营养成分？我可以为您提供详细的分析报告。',
			制定饮食计划:
				'为了制定个性化的饮食计划，我需要了解以下信息：\n\n1. 您的身高体重\n2. 运动习惯\n3. 饮食偏好\n4. 健康目标\n\n请提供这些信息，我会为您制定专属计划。',
			default: `收到您的问题："${text}"\n\n我正在为您分析，稍后会给出专业建议。\n\n您可以问我关于：\n• 营养成分分析\n• 食谱推荐\n• 饮食计划\n• 健康建议`
		}

		for (const [key, value] of Object.entries(responses)) {
			if (text.includes(key)) {
				return value
			}
		}

		return responses['default']
	}

	/**
	 * 重置所有状态
	 */
	const reset = () => {
		streamingState.value = StreamingState.IDLE
		isStreaming.value = false
		isTyping.value = false
		abortController = null
	}

	// ==================== 返回公共接口 ====================
	return {
		// 状态
		streamingState,
		isStreaming,
		isTyping,
		isLoading,
		loadingText,

		// 常量
		StreamingState,

		// 方法
		startStreaming,
		stopStreaming,
		parseCardData,
		generateMockResponse,
		reset
	}
}
