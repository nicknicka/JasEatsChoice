/**
 * useErrorHandler - 统一错误处理 Composable
 *
 * 功能：
 * - 统一错误提示
 * - 错误日志记录
 * - 友好的错误消息转换
 *
 * @author Claude
 * @date 2026-03-31
 */

/**
 * 错误类型枚举
 * @enum {string}
 */
const ErrorType = {
	NETWORK: 'network', // 网络错误
	AUTH: 'auth', // 认证错误
	VALIDATION: 'validation', // 参数校验错误
	BUSINESS: 'business', // 业务错误
	UNKNOWN: 'unknown' // 未知错误
}

/**
 * 错误处理 Composable
 * @returns {Object} 错误处理相关的方法
 */
export function useErrorHandler() {
	/**
	 * 获取友好的错误消息
	 * @param {Error|string|Object} error - 错误对象
	 * @param {ErrorType} type - 错误类型
	 * @returns {string} 友好的错误消息
	 */
	const getFriendlyMessage = (error, type = ErrorType.UNKNOWN) => {
		// 默认错误消息
		const defaultMessages = {
			[ErrorType.NETWORK]: '网络连接失败，请检查网络设置',
			[ErrorType.AUTH]: '登录已过期，请重新登录',
			[ErrorType.VALIDATION]: '参数错误，请检查输入',
			[ErrorType.BUSINESS]: '操作失败，请稍后重试',
			[ErrorType.UNKNOWN]: '发生未知错误，请稍后重试'
		}

		// 提取错误消息
		let errorMessage = ''
		if (typeof error === 'string') {
			errorMessage = error
		} else if (error instanceof Error) {
			errorMessage = error.message || String(error)
		} else if (error && typeof error === 'object') {
			errorMessage = error.message || error.msg || String(error)
		} else {
			errorMessage = String(error)
		}

		// 特殊错误消息处理
		const specialMessages = {
			'Failed to parse': 'AI服务暂时异常，请重新发送消息',
			'```json': 'AI服务暂时异常，请重新发送消息',
			'网络': '网络连接失败，请检查网络设置',
			'超时': '请求超时，请稍后重试',
			'登录': '请先登录',
			'认证': '登录已过期，请重新登录'
		}

		// 检查是否包含特殊错误关键词
		for (const [keyword, friendlyMsg] of Object.entries(specialMessages)) {
			if (errorMessage.includes(keyword)) {
				return friendlyMsg
			}
		}

		// 返回原始错误消息或默认消息
		return errorMessage && errorMessage !== 'undefined'
			? errorMessage
			: defaultMessages[type]
	}

	/**
	 * 处理错误
	 * @param {Error|string|Object} error - 错误对象
	 * @param {string} context - 错误上下文（用于日志）
	 * @param {ErrorType} type - 错误类型
	 * @param {Object} options - 可选配置
	 * @param {boolean} options.showToast - 是否显示 Toast 提示（默认 true）
	 * @param {boolean} options.log - 是否记录日志（默认 true）
	 * @param {number} options.duration - Toast 显示时长（默认 2000ms）
	 */
	const handleError = (
		error,
		context = '',
		type = ErrorType.UNKNOWN,
		options = {}
	) => {
		const { showToast = true, log = true, duration = 2000 } = options

		// 记录错误日志
		if (log) {
			const errorMsg =
				typeof error === 'string' ? error : error instanceof Error ? error.message : String(error)
			console.error(`[ErrorHandler${context ? ` - ${context}` : ''}]`, {
				type,
				error: errorMsg,
				timestamp: new Date().toISOString(),
				stack: error instanceof Error ? error.stack : undefined
			})
		}

		// 显示用户友好的错误提示
		if (showToast) {
			const friendlyMessage = getFriendlyMessage(error, type)
			uni.showToast({
				title: friendlyMessage,
				icon: 'none',
				duration
			})
		}
	}

	/**
	 * 处理网络错误
	 * @param {Error|string|Object} error - 错误对象
	 * @param {string} context - 错误上下文
	 */
	const handleNetworkError = (error, context = '') => {
		handleError(error, context, ErrorType.NETWORK)
	}

	/**
	 * 处理认证错误
	 * @param {Error|string|Object} error - 错误对象
	 * @param {string} context - 错误上下文
	 */
	const handleAuthError = (error, context = '') => {
		handleError(error, context, ErrorType.AUTH)

		// 跳转到登录页
		setTimeout(() => {
			uni.navigateTo({
				url: '/pages/login/index'
			})
		}, 2000)
	}

	/**
	 * 处理参数校验错误
	 * @param {Error|string|Object} error - 错误对象
	 * @param {string} context - 错误上下文
	 */
	const handleValidationError = (error, context = '') => {
		handleError(error, context, ErrorType.VALIDATION)
	}

	/**
	 * 处理业务错误
	 * @param {Error|string|Object} error - 错误对象
	 * @param {string} context - 错误上下文
	 */
	const handleBusinessError = (error, context = '') => {
		handleError(error, context, ErrorType.BUSINESS)
	}

	/**
	 * 显示成功提示
	 * @param {string} message - 成功消息
	 * @param {number} duration - 显示时长（默认 2000ms）
	 */
	const showSuccess = (message, duration = 2000) => {
		uni.showToast({
			title: message,
			icon: 'success',
			duration
		})
	}

	/**
	 * 显示确认对话框
	 * @param {string} content - 对话框内容
	 * @param {Object} options - 可选配置
	 * @param {string} options.title - 对话框标题（默认 '提示'）
	 * @param {string} options.confirmText - 确认按钮文本（默认 '确定'）
	 * @param {string} options.cancelText - 取消按钮文本（默认 '取消'）
	 * @param {boolean} options.showCancel - 是否显示取消按钮（默认 true）
	 * @param {string} options.confirmColor - 确认按钮颜色（默认 #FF6B35）
	 * @returns {Promise<boolean>} 用户是否点击确认
	 */
	const confirm = (content, options = {}) => {
		const {
			title = '提示',
			confirmText = '确定',
			cancelText = '取消',
			showCancel = true,
			confirmColor = '#FF6B35'
		} = options

		return new Promise((resolve) => {
			uni.showModal({
				title,
				content,
				confirmText,
				cancelText,
				showCancel,
				confirmColor,
				success: (res) => {
					resolve(res.confirm)
				},
				fail: () => {
					resolve(false)
				}
			})
		})
	}

	// ==================== 返回公共接口 ====================
	return {
		// 方法
		handleError,
		handleNetworkError,
		handleAuthError,
		handleValidationError,
		handleBusinessError,
		showSuccess,
		confirm,
		getFriendlyMessage
	}
}

// 导出错误类型枚举
export { ErrorType }
