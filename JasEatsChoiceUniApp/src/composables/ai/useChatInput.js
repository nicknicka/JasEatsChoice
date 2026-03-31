/**
 * useChatInput - 聊天输入管理 Composable
 *
 * 功能：
 * - 输入框状态管理
 * - 图片上传
 * - 表情选择
 * - 快捷提问
 *
 * @author Claude
 * @date 2026-03-31
 */

import { ref, computed } from 'vue'

/**
 * 聊天输入管理 Composable
 * @returns {Object} 输入管理相关的状态和方法
 */
export function useChatInput() {
	// ==================== 状态 ====================
	/** @type {import('vue').Ref<string>} 输入文本 */
	const inputText = ref('')

	/** @type {import('vue').Ref<boolean>} 是否正在输入 */
	const isTyping = ref(false)

	/** @type {import('vue').Ref<Array<{id: number, url: string}>>} 已上传的图片列表 */
	const uploadedImages = ref([])

	/** @type {import('vue').Ref<boolean>} 是否显示表情面板 */
	const showEmojiPicker = ref(false)

	/** @type {import('vue').Ref<boolean>} 是否展开快捷提问 */
	const quickQuestionsExpanded = ref(false)

	/** @type {import('vue').Ref<string[]>} 快捷提问列表 */
	const quickQuestions = ref([
		'推荐适合减肥的食谱',
		'今日卡路里摄入建议',
		'如何搭配营养均衡的饮食',
		'推荐低卡路里零食',
		'适合运动后的食物'
	])

	/** @type {import('vue').Ref<string[]>} 常用表情列表 */
	const commonEmojis = ref(['😊', '👍', '❤️', '🎉', '🤔', '😂', '🙏', '💪', '👌', '✨', '🔥', '💯'])

	// ==================== 计算属性 ====================

	/**
	 * 是否可以发送消息
	 * @type {import('vue').ComputedRef<boolean>}
	 */
	const canSend = computed(() => {
		return inputText.value.trim().length > 0 || uploadedImages.value.length > 0
	})

	/**
	 * 输入框占位符文本
	 * @type {import('vue').ComputedRef<string>}
	 */
	const placeholder = computed(() => {
		if (uploadedImages.value.length > 0) {
			return `已选择${uploadedImages.value.length}张图片，可以说点什么...`
		}
		return '输入您的饮食问题'
	})

	// ==================== 方法 ====================

	/**
	 * 清空输入
	 */
	const clearInput = () => {
		inputText.value = ''
	}

	/**
	 * 设置输入文本
	 * @param {string} text - 文本内容
	 */
	const setInput = (text) => {
		inputText.value = text
	}

	/**
	 * 追加文本到输入框
	 * @param {string} text - 要追加的文本
	 */
	const appendInput = (text) => {
		inputText.value += text
	}

	/**
	 * 选择表情
	 * @param {string} emoji - 表情符号
	 */
	const selectEmoji = (emoji) => {
		appendInput(emoji)
		showEmojiPicker.value = false
	}

	/**
	 * 切换表情面板
	 */
	const toggleEmoji = () => {
		showEmojiPicker.value = !showEmojiPicker.value
		// 收起快捷提问
		if (showEmojiPicker.value) {
			quickQuestionsExpanded.value = false
		}
	}

	/**
	 * 切换快捷提问面板
	 */
	const toggleQuickQuestions = () => {
		quickQuestionsExpanded.value = !quickQuestionsExpanded.value
		// 收起表情面板
		if (quickQuestionsExpanded.value) {
			showEmojiPicker.value = false
		}
	}

	/**
	 * 选择快捷提问
	 * @param {string} question - 问题文本
	 */
	const selectQuickQuestion = (question) => {
		setInput(question)
		quickQuestionsExpanded.value = false
		return question // 返回问题文本，方便外部调用发送
	}

	/**
	 * 选择图片
	 * @param {number} count - 最大选择数量
	 * @param {number} maxSize - 最大文件大小（字节），默认5MB
	 * @returns {Promise<void>}
	 */
	const chooseImage = (count = 3, maxSize = 5 * 1024 * 1024) => {
		return new Promise((resolve, reject) => {
			uni.chooseImage({
				count,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: async (res) => {
					try {
						const tempFilePaths = res.tempFilePaths
						const validImages = []

						for (const filePath of tempFilePaths) {
							try {
								// 获取文件信息
								const fileInfo = await uni.getFileInfo({
									filePath
								})

								// 检查文件大小
								if (fileInfo.size > maxSize) {
									uni.showToast({
										title: `图片过大，限制${Math.floor(maxSize / 1024 / 1024)}MB`,
										icon: 'none',
										duration: 2000
									})
									continue
								}

								validImages.push({
									id: Date.now() + Math.random(),
									url: filePath,
									size: fileInfo.size
								})
							} catch (err) {
								console.error('获取文件信息失败:', err)
								// 如果获取文件信息失败，仍然允许添加
								validImages.push({
									id: Date.now() + Math.random(),
									url: filePath
								})
							}
						}

						uploadedImages.value.push(...validImages)
						resolve(validImages.map((img) => img.url))
					} catch (err) {
						reject(err)
					}
				},
				fail: (err) => {
					// 判断是否是用户取消
					if (err.errMsg && err.errMsg.includes('cancel')) {
						console.log('用户取消选择图片')
						resolve([])
					} else {
						console.error('选择图片失败:', err)
						reject(err)
					}
				}
			})
		})
	}

	/**
	 * 移除已上传的图片
	 * @param {number} index - 图片索引
	 */
	const removeImage = (index) => {
		if (index >= 0 && index < uploadedImages.value.length) {
			uploadedImages.value.splice(index, 1)
		}
	}

	/**
	 * 清空所有已上传的图片
	 */
	const clearImages = () => {
		uploadedImages.value = []
	}

	/**
	 * 获取输入数据（用于发送消息）
	 * @returns {{text: string, images: Array<string>}} 输入数据
	 */
	const getInputData = () => {
		return {
			text: inputText.value.trim(),
			images: uploadedImages.value.map((img) => img.url)
		}
	}

	/**
	 * 重置所有输入状态
	 */
	const resetInput = () => {
		clearInput()
		clearImages()
		showEmojiPicker.value = false
		quickQuestionsExpanded.value = false
	}

	// ==================== 返回公共接口 ====================
	return {
		// 状态
		inputText,
		isTyping,
		uploadedImages,
		showEmojiPicker,
		quickQuestionsExpanded,
		quickQuestions,
		commonEmojis,
		canSend,
		placeholder,

		// 方法
		clearInput,
		setInput,
		appendInput,
		selectEmoji,
		toggleEmoji,
		toggleQuickQuestions,
		selectQuickQuestion,
		chooseImage,
		removeImage,
		clearImages,
		getInputData,
		resetInput
	}
}
