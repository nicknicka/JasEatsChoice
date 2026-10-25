import { ref, nextTick } from 'vue'

/**
 * 智能滚动管理
 * 自动滚动到底部，检测用户手动滚动行为
 * @param {import('vue').Ref<HTMLElement|null>} chatContainerRef - 聊天容器引用
 * @param {import('vue').Ref<boolean>} isMounted - 组件挂载状态
 * @returns {Object} 滚动管理方法和状态
 */
export function useScrollManager(chatContainerRef, isMounted) {
	// 用户手动滚动标记
	const userHasScrolled = ref(false)
	let isAutoScrolling = false
	let isUserScrollingUp = false
	let lastScrollTop = 0

	/**
	 * 处理滚动事件，检测用户是否手动滚动
	 */
	const handleScroll = () => {
		if (isAutoScrolling) return

		const container = chatContainerRef.value
		if (!container) return

		const currentScrollTop = container.scrollTop

		// 检测滚动方向
		if (currentScrollTop < lastScrollTop) {
			isUserScrollingUp = true
		} else if (currentScrollTop > lastScrollTop) {
			const threshold = container.scrollHeight * 0.11
			const isNearBottom =
				container.scrollHeight - currentScrollTop - container.clientHeight < threshold
			if (isNearBottom) {
				isUserScrollingUp = false
			}
		}

		lastScrollTop = currentScrollTop

		// 检查是否接近底部（阈值为底部11%）
		const threshold = container.scrollHeight * 0.11
		const isNearBottom =
			container.scrollHeight - container.scrollTop - container.clientHeight < threshold

		userHasScrolled.value = !isNearBottom
	}

	/**
	 * 滚动到底部
	 * @param {boolean} force - 是否强制滚动（忽略用户滚动标记）
	 */
	const scrollToBottom = (force = false) => {
		if (!isMounted.value) return

		if (force || !userHasScrolled.value || !isUserScrollingUp) {
			isAutoScrolling = true

			nextTick(() => {
				nextTick(() => {
					if (!isMounted.value || !chatContainerRef.value) return

					try {
						chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
					} catch (error) {
						console.warn('滚动到底部失败:', error.message)
					}

					setTimeout(() => {
						isAutoScrolling = false
					}, 100)
				})
			})
		}
	}

	/**
	 * 重置滚动状态（发送新消息时调用）
	 */
	const resetScrollState = () => {
		userHasScrolled.value = false
		isUserScrollingUp = false
	}

	/**
	 * 初始化滚动位置
	 */
	const initScrollPosition = () => {
		if (chatContainerRef.value) {
			lastScrollTop = chatContainerRef.value.scrollTop || 0
		}
	}

	return {
		userHasScrolled,
		handleScroll,
		scrollToBottom,
		resetScrollState,
		initScrollPosition,
	}
}
