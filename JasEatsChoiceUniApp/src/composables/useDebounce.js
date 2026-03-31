/**
 * useDebounce - 防抖 Composable
 *
 * 功能：
 * - 延迟执行函数
 * - 在延迟时间内重复调用会重置计时器
 * - 适用于搜索输入、窗口resize等场景
 *
 * @author Claude
 * @date 2026-03-31
 */

import { ref, watch, onUnmounted } from 'vue'

/**
 * 防抖 Composable
 * @param {Function} fn - 要防抖的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function useDebounce(fn, delay = 300) {
	let timeoutId = null

	/**
	 * 防抖函数
	 * @param {...any} args - 函数参数
	 */
	const debouncedFn = (...args) => {
		// 清除之前的计时器
		if (timeoutId) {
			clearTimeout(timeoutId)
		}

		// 设置新的计时器
		timeoutId = setTimeout(() => {
			fn(...args)
			timeoutId = null
		}, delay)
	}

	/**
	 * 立即执行（取消延迟）
	 * @param {...any} args - 函数参数
	 */
	const immediate = (...args) => {
		if (timeoutId) {
			clearTimeout(timeoutId)
			timeoutId = null
		}
		fn(...args)
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

	return debouncedFn
}

/**
 * 防抖 Watch Composable
 * 监听响应式数据的变化，延迟执行回调
 *
 * @param {import('vue').Ref|import('vue').ComputedRef|Object} source - 监听的数据源
 * @param {Function} callback - 回调函数
 * @param {number} delay - 延迟时间（毫秒）
 * @param {Object} options - watch 选项
 */
export function useDebounceWatch(source, callback, delay = 300, options = {}) {
	let timeoutId = null

	/**
	 * 防抖回调包装
	 */
	const debouncedCallback = (...args) => {
		if (timeoutId) {
			clearTimeout(timeoutId)
		}

		timeoutId = setTimeout(() => {
			callback(...args)
			timeoutId = null
		}, delay)
	}

	// 使用 watch 监听数据源
	const stopWatch = watch(source, debouncedCallback, options)

	/**
	 * 停止监听
	 */
	const stop = () => {
		stopWatch()
		if (timeoutId) {
			clearTimeout(timeoutId)
			timeoutId = null
		}
	}

	// 组件卸载时清理
	onUnmounted(() => {
		stop()
	})

	return { stop }
}

/**
 * 防抖值 Composable
 * 创建一个防抖的 computed 值
 *
 * @param {import('vue').Ref} value - 原始值
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {import('vue').Ref} 防抖后的值
 */
export function useDebouncedValue(value, delay = 300) {
	const debouncedValue = ref(value.value)
	let timeoutId = null

	// 监听原始值的变化
	watch(
		value,
		(newValue) => {
			if (timeoutId) {
				clearTimeout(timeoutId)
			}

			timeoutId = setTimeout(() => {
				debouncedValue.value = newValue
				timeoutId = null
			}, delay)
		},
		{ immediate: true }
	)

	// 组件卸载时清理
	onUnmounted(() => {
		if (timeoutId) {
			clearTimeout(timeoutId)
		}
	})

	return debouncedValue
}
