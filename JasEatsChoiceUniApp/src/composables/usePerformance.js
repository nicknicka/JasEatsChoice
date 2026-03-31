/**
 * usePerformance - 性能监控 Composable
 *
 * 功能：
 * - 监控组件渲染性能
 * - 监控 API 请求性能
 * - 监控滚动性能
 * - 提供性能优化建议
 *
 * @author Claude
 * @date 2026-03-31
 */

import { ref, onMounted, onUnmounted } from 'vue'
import { debug } from '@/utils/logger'

/**
 * 性能指标
 * @enum {string}
 */
const MetricType = {
	RENDER: 'render', // 渲染时间
	API_REQUEST: 'api_request', // API请求时间
	SCROLL: 'scroll', // 滚动帧率
	MEMORY: 'memory' // 内存使用
}

/**
 * 性能监控 Composable
 * @param {string} componentName - 组件名称
 * @returns {Object} 性能监控相关的状态和方法
 */
export function usePerformance(componentName = 'Unknown') {
	/** @type {import('vue').Ref<Object>} 性能指标数据 */
	const metrics = ref({
		renderTime: [],
		apiRequestTime: [],
		scrollFPS: [],
		memoryUsage: []
	})

	/** @type {import('vue').Ref<boolean>} 是否启用性能监控 */
	const isEnabled = ref(process.env.NODE_ENV === 'development')

	/**
	 * 开始性能测量
	 * @param {MetricType} type - 指标类型
	 * @returns {Function} 结束测量的函数
	 */
	const startMeasure = (type) => {
		if (!isEnabled.value) return () => {}

		const startTime = Date.now()
		const startMemory = performance?.memory?.usedJSHeapSize

		return () => {
			const endTime = Date.now()
			const duration = endTime - startTime
			const endMemory = performance?.memory?.usedJSHeapSize
			const memoryDelta = endMemory && startMemory ? endMemory - startMemory : 0

			const metric = {
				timestamp: endTime,
				duration,
				memoryDelta,
				component: componentName
			}

			// 记录到对应的指标数组
			switch (type) {
				case MetricType.RENDER:
					metrics.value.renderTime.push(metric)
					break
				case MetricType.API_REQUEST:
					metrics.value.apiRequestTime.push(metric)
					break
				case MetricType.MEMORY:
					metrics.value.memoryUsage.push(metric)
					break
			}

			// 记录日志
			debug(`性能监控 [${type}]`, {
				component: componentName,
				duration: `${duration}ms`,
				memoryDelta: memoryDelta ? `${(memoryDelta / 1024 / 1024).toFixed(2)}MB` : 'N/A'
			}, 'usePerformance')

			return metric
		}
	}

	/**
	 * 监控 API 请求性能
	 * @param {string} apiName - API 名称
	 * @param {Function} apiFn - API 函数
	 * @returns {Promise} API 函数的执行结果
	 */
	const measureAPI = async (apiName, apiFn) => {
		if (!isEnabled.value) {
			return apiFn()
		}

		const endMeasure = startMeasure(MetricType.API_REQUEST)

		try {
			const result = await apiFn()
			const metric = endMeasure()

			debug(`API 请求 [${apiName}]`, {
				duration: `${metric.duration}ms`,
				success: true
			}, 'usePerformance')

			return result
		} catch (error) {
			const metric = endMeasure()

			debug(`API 请求 [${apiName}]`, {
				duration: `${metric.duration}ms`,
				success: false,
				error: error.message
			}, 'usePerformance')

			throw error
		}
	}

	/**
	 * 监控滚动性能
	 * @param {number} sampleInterval - 采样间隔（毫秒）
	 * @returns {Function} 停止监控的函数
	 */
	const watchScrollFPS = (sampleInterval = 1000) => {
		if (!isEnabled.value) return () => {}

		let frameCount = 0
		let lastTime = Date.now()
		let animationFrameId = null

		const countFrame = () => {
			frameCount++
			animationFrameId = requestAnimationFrame(countFrame)
		}

		countFrame()

		// 定期采样 FPS
		const intervalId = setInterval(() => {
			const now = Date.now()
			const elapsed = now - lastTime
			const fps = Math.round((frameCount / elapsed) * 1000)

			metrics.value.scrollFPS.push({
				timestamp: now,
				fps,
				component: componentName
			})

			debug(`滚动 FPS`, {
				component: componentName,
				fps: `${fps}fps`
			}, 'usePerformance')

			// 重置计数
			frameCount = 0
			lastTime = now
		}, sampleInterval)

		// 返回停止函数
		return () => {
			cancelAnimationFrame(animationFrameId)
			clearInterval(intervalId)
		}
	}

	/**
	 * 获取性能报告
	 * @returns {Object} 性能报告
	 */
	const getReport = () => {
		const renderTimes = metrics.value.renderTime.map((m) => m.duration)
		const apiTimes = metrics.value.apiRequestTime.map((m) => m.duration)
		const scrollFPS = metrics.value.scrollFPS.map((m) => m.fps)

		return {
			component: componentName,
			render: {
				avg: renderTimes.length ? average(renderTimes) : 0,
				max: renderTimes.length ? Math.max(...renderTimes) : 0,
				min: renderTimes.length ? Math.min(...renderTimes) : 0,
				count: renderTimes.length
			},
			api: {
				avg: apiTimes.length ? average(apiTimes) : 0,
				max: apiTimes.length ? Math.max(...apiTimes) : 0,
				min: apiTimes.length ? Math.min(...apiTimes) : 0,
				count: apiTimes.length
			},
			scroll: {
				avg: scrollFPS.length ? average(scrollFPS) : 0,
				max: scrollFPS.length ? Math.max(...scrollFPS) : 0,
				min: scrollFPS.length ? Math.min(...scrollFPS) : 0,
				count: scrollFPS.length
			}
		}
	}

	/**
	 * 获取性能优化建议
	 * @returns {Array<string>} 优化建议列表
	 */
	const getSuggestions = () => {
		const suggestions = []
		const report = getReport()

		// 渲染性能建议
		if (report.render.avg > 16) {
			suggestions.push('渲染时间过长（>16ms），建议使用虚拟滚动或减少 DOM 节点')
		}

		// API 性能建议
		if (report.api.avg > 1000) {
			suggestions.push('API 响应时间过长（>1s），建议添加缓存或优化后端查询')
		}

		// 滚动性能建议
		if (report.scroll.avg < 50) {
			suggestions.push('滚动帧率过低（<50fps），建议优化列表渲染或减少重绘')
		}

		return suggestions
	}

	/**
	 * 重置所有指标
	 */
	const reset = () => {
		metrics.value = {
			renderTime: [],
			apiRequestTime: [],
			scrollFPS: [],
			memoryUsage: []
		}
	}

	return {
		// 状态
		metrics,
		isEnabled,

		// 方法
		startMeasure,
		measureAPI,
		watchScrollFPS,
		getReport,
		getSuggestions,
		reset,

		// 常量
		MetricType
	}
}

/**
 * 计算数组平均值
 * @param {Array<number>} arr - 数值数组
 * @returns {number} 平均值
 */
function average(arr) {
	if (!arr || arr.length === 0) return 0
	return arr.reduce((sum, val) => sum + val, 0) / arr.length
}

/**
 * 创建性能监控 Hook
 * 用于监控组件的生命周期性能
 *
 * @param {string} componentName - 组件名称
 * @returns {Object} 性能监控对象
 */
export function useComponentPerformance(componentName) {
	const performance = usePerformance(componentName)

	onMounted(() => {
		const endMeasure = performance.startMeasure(MetricType.RENDER)

		// 在下一个 tick 结束测量
		setTimeout(() => {
			endMeasure()
		}, 0)
	})

	return performance
}
