/**
 * 统一日志服务
 *
 * 功能：
 * - 区分开发和生产环境
 * - 提供不同级别的日志
 * - 生产环境自动禁用调试日志
 *
 * @author Claude
 * @date 2026-03-31
 */

/**
 * 日志级别枚举
 * @enum {number}
 */
const LogLevel = {
	DEBUG: 0,
	INFO: 1,
	WARN: 2,
	ERROR: 3,
	SILENT: 4
}

/**
 * 当前日志级别
 * 开发环境: DEBUG
 * 生产环境: ERROR
 */
const currentLogLevel = process.env.NODE_ENV === 'production' ? LogLevel.ERROR : LogLevel.DEBUG

/**
 * 日志服务类
 */
class Logger {
	/**
	 * 格式化日志前缀
	 * @param {string} level - 日志级别
	 * @param {string} context - 上下文
	 * @returns {string} 格式化的前缀
	 */
	formatPrefix(level, context = '') {
		const timestamp = new Date().toISOString()
		const contextStr = context ? `[${context}]` : ''
		return `${timestamp} ${level} ${contextStr}`
	}

	/**
	 * 调试级别日志（仅开发环境）
	 * @param {string} message - 日志消息
	 * @param {*} data - 附加数据
	 * @param {string} context - 上下文
	 */
	debug(message, data, context = '') {
		if (currentLogLevel <= LogLevel.DEBUG) {
			const prefix = this.formatPrefix('DEBUG', context)
			if (data !== undefined) {
				console.log(prefix, message, data)
			} else {
				console.log(prefix, message)
			}
		}
	}

	/**
	 * 信息级别日志（仅开发环境）
	 * @param {string} message - 日志消息
	 * @param {*} data - 附加数据
	 * @param {string} context - 上下文
	 */
	info(message, data, context = '') {
		if (currentLogLevel <= LogLevel.INFO) {
			const prefix = this.formatPrefix('INFO', context)
			if (data !== undefined) {
				console.log(prefix, message, data)
			} else {
				console.log(prefix, message)
			}
		}
	}

	/**
	 * 警告级别日志（所有环境）
	 * @param {string} message - 日志消息
	 * @param {*} data - 附加数据
	 * @param {string} context - 上下文
	 */
	warn(message, data, context = '') {
		if (currentLogLevel <= LogLevel.WARN) {
			const prefix = this.formatPrefix('WARN', context)
			if (data !== undefined) {
				console.warn(prefix, message, data)
			} else {
				console.warn(prefix, message)
			}
		}
	}

	/**
	 * 错误级别日志（所有环境）
	 * @param {string} message - 日志消息
	 * @param {Error|*} error - 错误对象或数据
	 * @param {string} context - 上下文
	 */
	error(message, error, context = '') {
		if (currentLogLevel <= LogLevel.ERROR) {
			const prefix = this.formatPrefix('ERROR', context)
			if (error instanceof Error) {
				console.error(prefix, message, {
					message: error.message,
					stack: error.stack,
					...error
				})
			} else if (error !== undefined) {
				console.error(prefix, message, error)
			} else {
				console.error(prefix, message)
			}
		}
	}
}

// 创建单例实例
const logger = new Logger()

// 导出便捷方法
export const debug = (message, data, context) => logger.debug(message, data, context)
export const info = (message, data, context) => logger.info(message, data, context)
export const warn = (message, data, context) => logger.warn(message, data, context)
export const error = (message, error, context) => logger.error(message, error, context)

// 默认导出 logger 实例
export default logger
