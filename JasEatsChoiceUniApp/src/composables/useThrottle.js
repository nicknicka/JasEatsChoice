/**
 * useThrottle - 节流 Composable
 *
 * 功能：
 * - 限制函数执行频率
 * - 在指定时间内只执行一次
 * - 适用于滚动事件、鼠标移动等高频事件
 *
 * @author Claude
 * @date 2026-03-31
 */

import { ref, onUnmounted } from 'vue'

/**
 * 节流 Composable（时间戳版本）
 * @param {Function} fn - 要节流的函数
 * @param {number} interval - 时间间隔（毫秒）
 * @returns {Function} 节流后的函数
 */
export function useThrottle(fn, interval = 300) {
	let lastTime = 0

	/**
	 * 节流函数
	 * @param {...any} args - 函数参数
	 */
	const throttledFn = (...args) => {
		const now = Date.now()

		if (now - lastTime >= interval) {
			lastTime = now
			fn(...args)
		}
	}

	return throttledFn
}

/**
 * 节流 Composable（定时器版本）
 * @param {Function} fn - 要节流的函数
 * @param {number} interval - 时间间隔（毫秒）
 * @returns {Function} 节流后的函数
 */
export function useThrottleTimer(fn, interval = 300) {
	let timeoutId = null
	let lastTime = 0

	/**
	 * 节流函数
	 * @param {...any} args - 函数参数
	 */
	const throttledFn = (...args) => {
		const now = Date.now()
		const remaining = interval - (now - lastTime)

		if (remaining <= 0) {
			// 立即执行
			if (timeoutId) {
				clearTimeout(timeoutId)
				timeoutId = null
			}
			lastTime = now
			fn(...args)
		} else if (!timeoutId) {
			// 延迟执行
			timeoutId = setTimeout(() => {
				lastTime = Date.now()
				timeoutId = null
				fn(...args)
			}, remaining)
		}
	}

	/**
	 * 取消待执行的函数
	 */
	const cancel = () => {
		if (timeoutId) {
			clearTimeout(timeoutId)
			timeoutId = null
		}
	}

	// 组件卸载时清理
	onUnmounted(() => {
		cancel()
	})

	return throttledFn
}

/**
 * 节流 Composable（立即执行版本）
 * 第一次调用立即执行，后续调用按间隔执行
 *
 * @param {Function} fn - 要节流的函数
 * @param {number} interval - 时间间隔（毫秒）
 * @returns {Function} 节流后的函数
 */
export function useThrottleImmediate(fn, interval = 300) {
	let timeoutId = null
	let lastTime = 0

	/**
	 * 节流函数
	 * @param {...any} args - 函数参数
	 */
	const throttledFn = (...args) => {
		const now = Date.now()
		const shouldExecute = now - lastTime >= interval

		if (shouldExecute) {
			// 立即执行
			lastTime = now
			fn(...args)
		} else if (!timeoutId) {
			// 设置延迟执行
			timeoutId = setTimeout(() => {
				lastTime = Date.now()
				timeoutId = null
				fn(...args)
			}, interval - (now - lastTime))
		}
	}

	/**
	 * 取消待执行的函数
	 */
	const cancel = () => {
		if (timeoutId) {
			clearTimeout(timeoutId)
			timeoutId = null
		}
	}

	// 组件卸载时清理
	onUnmounted(() => {
		cancel()
	})

	return throttledFn
}
