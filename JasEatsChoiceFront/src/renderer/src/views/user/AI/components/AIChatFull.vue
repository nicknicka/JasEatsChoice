<template>
	<div class="chat-content-wrapper">
		<!-- 聊天消息区域 -->
		<div class="chat-messages" ref="chatContainerRef">
			<!-- 初始加载时的打字机等待效果 -->
			<transition name="fade-in">
				<div v-if="isInitialLoading" class="initial-loading-container">
					<div class="ai-avatar-loading">
						<div class="avatar-emoji"><el-icon :size="32"><Service /></el-icon></div>
						<div class="typing-indicator">
							<span class="typing-dot"></span>
							<span class="typing-dot"></span>
							<span class="typing-dot"></span>
						</div>
					</div>
					<div class="loading-text">AI正在准备中...</div>
				</div>
			</transition>

			<!-- 消息列表 -->
			<template v-if="messages.length > 0">
				<div
					v-for="message in messages"
					:key="message.id"
					:class="getMessageClasses(message)"
				>
					<!-- 用户头像 -->
					<CommonAvatar
						v-if="message.sender === 'user'"
						:avatar-url="message.avatar"
						:size="42"
						:fallback-text="userStore.userInfo?.nickname || '用'"
						class="message-avatar-custom"
					/>
					<!-- AI头像 -->
					<div v-else class="message-avatar">
							<div class="ai-avatar-icon"><el-icon :size="24"><Service /></el-icon></div>
						</div>
					<div class="message-content">
						<!-- 图片附件 -->
						<div v-if="message.images && message.images.length > 0" class="message-images">
							<div v-for="(img, idx) in message.images" :key="idx" class="message-image">
								<img :src="img.url" :alt="`图片${idx + 1}`" />
							</div>
						</div>

						<!-- 消息文本 -->
						<div class="message-text" v-show="shouldShowMessage(message)">
							<!-- 进度指示器 -->
							<div
								v-if="message.sender === 'ai' && getProgressStatus(message)"
								class="message-progress-indicator"
								:class="getProgressClass(message)"
							>
								<div class="progress-content">
									<div class="progress-icon">
										<component :is="getProgressIcon(message)" />
									</div>
									<div class="progress-text">{{ getProgressText(message) }}</div>
									<div v-if="getProgressDots(message)" class="progress-dots">
										<span class="progress-dot"></span>
										<span class="progress-dot"></span>
										<span class="progress-dot"></span>
									</div>
								</div>
							</div>

							<!-- 卡片消息 -->
							<div v-if="shouldShowCardEnhanced(message)" class="card-message-wrapper">
								<component
									:is="getCardComponent(message.messageType)"
									:data="parseCardData(message.cardData)"
									@action="handleCardAction"
								/>
							</div>

							<!-- 纯文本消息 -->
							<div
								v-else-if="shouldShowTextContent(message)"
								class="message-text-content"
								:class="{ 'markdown-content': message.enableMarkdown, typing: message.isTyping }"
								v-html="renderContent(getDisplayContent(message), message.enableMarkdown)"
							></div>

							<!-- 卡片总结文本 -->
							<div
								v-else-if="shouldShowCardSummary(message)"
								class="card-summary-text"
								:class="{ 'markdown-content': message.enableMarkdown, typing: message.isTyping }"
								v-html="renderContent(getDisplayContent(message), message.enableMarkdown)"
							></div>

							<!-- 更多操作按钮 -->
							<el-dropdown
								trigger="click"
								@command="(cmd) => handleMessageAction(cmd, getDisplayContent(message))"
								v-if="shouldShowMoreButton(message)"
							>
								<span class="more-btn">
									<el-icon :size="12"><More /></el-icon>
								</span>
								<template #dropdown>
									<el-dropdown-menu>
										<el-dropdown-item command="copy">
											<el-icon><DocumentCopy /></el-icon>
											<span>复制</span>
										</el-dropdown-item>
									</el-dropdown-menu>
								</template>
							</el-dropdown>
						</div>
						<div class="message-time">{{ message.time }}</div>
					</div>
				</div>
			</template>
		</div>

		<!-- 底部容器 -->
		<div class="bottom-container" ref="bottomContainerRef">
			<!-- 已上传图片预览 -->
			<transition name="slide-up">
				<div v-if="uploadedImages.length > 0" class="uploaded-images-preview">
					<div v-for="img in uploadedImages" :key="img.id" class="uploaded-image-item">
						<img :src="img.url" alt="上传的图片" />
						<el-button :icon="Delete" circle size="small" class="remove-image-btn" @click="removeUploadedImage(img.id)" />
					</div>
				</div>
			</transition>

			<!-- 表情面板 -->
			<transition name="slide-up">
				<div v-if="showEmojiPicker" class="emoji-panel">
					<div class="emoji-grid">
						<span v-for="emoji in commonEmojis" :key="emoji" class="emoji-item" @click="selectEmoji(emoji)" :title="emoji">
							{{ emoji }}
						</span>
					</div>
				</div>
			</transition>

			<!-- 输入区域 -->
			<div class="input-area">
				<div class="input-wrapper">
					<!-- 工具栏 -->
					<div class="toolbar">
						<div class="toolbar-left">
							<el-tooltip content="表情" placement="top">
								<el-button :icon="Operation" circle size="small" @click="toggleEmoji" :class="{ 'is-active': showEmojiPicker }" />
							</el-tooltip>

							<input type="file" accept="image/*" @change="handleImageUpload" class="hidden-file-input" ref="imageInputRef" />
							<el-tooltip content="上传图片" placement="top">
								<el-button :icon="Picture" circle size="small" @click="triggerImageUpload" />
							</el-tooltip>

							<!-- 快捷提问 -->
							<div class="quick-question-button-wrapper">
								<transition name="fade-slide">
									<div v-if="showQuickQuestions" class="quick-questions-panel-fixed">
										<div class="quick-questions-title">
											<span>快速提问</span>
											<el-icon :size="14" class="close-panel-icon" @click="showQuickQuestions = false">
												<Close />
											</el-icon>
										</div>
										<div class="quick-questions-categories">
											<div v-for="(category, categoryIndex) in quickQuestionCategories" :key="categoryIndex" class="question-category">
												<div class="category-header" @click="toggleCategory(categoryIndex)" :class="{ 'is-active': expandedCategory === categoryIndex }">
													<span>{{ category.title }}</span>
													<el-icon :size="12" class="category-arrow" :class="{ 'is-expanded': expandedCategory === categoryIndex }">
														<ArrowRight />
													</el-icon>
												</div>
												<transition name="slide-right-sub">
													<div v-show="expandedCategory === categoryIndex" class="category-questions">
														<div v-for="(question, qIndex) in category.questions" :key="qIndex" class="question-item" @click="handleQuickQuestion(question)">
															{{ question }}
														</div>
													</div>
												</transition>
											</div>
										</div>
									</div>
								</transition>
								<el-tooltip content="快捷提问" placement="top">
									<el-button :icon="QuestionFilled" circle size="small" @click="toggleQuickQuestions" :class="{ 'is-active': showQuickQuestions }" />
								</el-tooltip>
							</div>

							<!-- AI个性化数据开关 -->
							<el-tooltip content="开启后AI将使用您的个人数据提供个性化建议" placement="bottom">
								<el-switch v-model="aiPersonalDataEnabled" @change="handlePersonalDataToggle" size="small" />
							</el-tooltip>
						</div>
						<div class="toolbar-right">
							<el-tooltip content="清空对话" placement="top">
								<el-button :icon="Delete" circle size="small" type="danger" plain @click="clearChat" class="clear-chat-btn" />
							</el-tooltip>
						</div>
					</div>

					<!-- 文本输入框 -->
					<div class="input-with-counter">
						<el-input
							v-model="inputMessage"
							placeholder="请输入您的问题...（支持Markdown格式）"
							:rows="2"
							type="textarea"
							:maxlength="500"
							:disabled="isLoading"
							@keydown="handleKeydown"
							class="message-input"
						/>
						<div class="char-count-wrapper">
							<span class="char-count" :class="{ 'near-limit': inputMessage.length >= 450 && inputMessage.length < 500, 'at-limit': inputMessage.length >= 500 }">
								{{ inputMessage.length }}/500
							</span>
						</div>
					</div>
				</div>

				<el-button
					v-if="isStreaming"
					class="stop-btn"
					@click="stopStreamRequest(); ElMessage.info('已停止AI回复')"
				>
					<el-icon><VideoPause /></el-icon>
					<span>停止</span>
				</el-button>
				<el-button
					v-else
					type="primary"
					class="send-btn"
					@click="sendMessage"
					:disabled="isLoading"
					:loading="isLoading"
				>
					发送
				</el-button>
			</div>
		</div>
	</div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
	Close, Delete, Picture, DocumentCopy, More, Operation,
	QuestionFilled, ArrowRight, Service, VideoPause,
} from '@element-plus/icons-vue'
import { useAuthStore } from '../../../../store/authStore'
import { useUserStore } from '../../../../store/userStore'
import { API_CONFIG } from '../../../../config/index'
import { initializeQuickQuestions, DEFAULT_EXPANDED_CATEGORY } from '../../../../config/quickQuestions'
import CommonAvatar from '@/components/CommonAvatar.vue'
import { useRouter } from 'vue-router'

// ========== Composables ==========
import { useScrollManager } from '../../../../composables/useScrollManager'
import { useCardHandler } from '../../../../composables/useCardHandler'
import { useAIChatMessages } from '../../../../composables/useAIChatMessages'
import { useAdvancedStreaming } from '../../../../composables/useAdvancedStreaming'

// ========== Stores ==========
const authStore = useAuthStore()
const userStore = useUserStore()
const router = useRouter()

// ========== 卡片组件导入 ==========
// 所有卡片统一走 UniCard 渲染，旧卡片组件不再使用
import UniCard from './cards/UniCard.vue'

/**
 * 所有卡片消息统一使用 UniCard 组件渲染
 */
const getCardComponent = () => UniCard

const parseCardData = (cardData) => {
	if (!cardData) return null
	if (typeof cardData === 'object') return cardData
	if (typeof cardData === 'string') {
		try { return JSON.parse(cardData) } catch { return null }
	}
	return null
}

// ========== 引用 ==========
const chatContainerRef = ref(null)
const bottomContainerRef = ref(null)
// ========== 卡片处理器 ==========
const {
	parseCardDataFromContentUniCard: parseCardDataFromContent,
	restoreCardDataForMessages: _restoreCardData,
} = useCardHandler(ref([]))

// ========== 消息管理 ==========
const {
	messages, isMounted, isInitialLoading,
	loadMessages, saveMessageToBackend, validateAndSaveMessage,
	getMessage, renderContent, getMessageClasses,
	shouldShowCard, shouldShowCardEnhanced, shouldShowTextContent, shouldShowCardSummary,
	shouldShowMoreButton, shouldShowMessage, getDisplayContent,
	getProgressStatus, startTypewriterEffect,
} = useAIChatMessages({
	parseCardDataFromContent,
	restoreCardDataForMessages: _restoreCardData,
})

// 将消息ref传给cardHandler（用于restoreCardDataForMessages）
const cardHandler = useCardHandler(messages)
const handleCardAction = (action) => cardHandler.handleCardAction(action, router)

// ========== 滚动管理 ==========
const { scrollToBottom, handleScroll, resetScrollState, initScrollPosition } = useScrollManager(chatContainerRef, isMounted)

// ========== UI更新 ==========
const updateUI = async () => {
	if (isMounted.value) {
		await nextTick()
		scrollToBottom()
	}
}

// ========== 流式传输 ==========
const {
	isStreaming,
	streamResponse, stopStreaming: stopStreamRequest,
	sendStreamRequest, getProgressIcon, getProgressText,
	getProgressDots, getProgressClass,
} = useAdvancedStreaming({
	messages, isMounted, getMessage, parseCardDataFromContent,
	convertToSupportedCardType: cardHandler.convertToSupportedCardTypeWithUniCard,
	validateAndSaveMessage,
	handleFinalResult: async (messageIndex, _parsedData) => {
		const message = getMessage(messageIndex)
		if (!message) return
		if (message._isProgressMessage) return
		if (!message.messageType && message.displayContent) {
			const fullText = message.displayContent
			message.displayContent = ''
			await startTypewriterEffect(messageIndex, fullText, 20)
		}
		await saveMessageToBackend('ai', message.content, message.messageType, message.cardData)
	},
	updateUI,
})

// ========== 本地UI状态 ==========
const inputMessage = ref('')
const isLoading = ref(false)
const showQuickQuestions = ref(false)
const showEmojiPicker = ref(false)
const uploadedImages = ref([])
const expandedCategory = ref(null)
const aiPersonalDataEnabled = ref(false)

const quickQuestionCategories = ref(initializeQuickQuestions(DEFAULT_EXPANDED_CATEGORY))
const commonEmojis = ref([
	'😊', '😂', '🤔', '👍', '👎', '❤️', '🔥', '✨',
	'🍎', '🥗', '🍲', '🍜', '🍕', '🍰', '☕', '🥤',
	'💪', '🏃', '🧘', '😋', '🤤', '😌', '🤗', '😎',
])

// ========== 核心操作 ==========

const sendMessage = async () => {
	const message = inputMessage.value.trim()
	if (!message && uploadedImages.value.length === 0) {
		ElMessage.warning('请输入问题或上传图片')
		return
	}
	if (message.length > 500) {
		ElMessage.warning('消息长度不能超过500个字符')
		return
	}

	// 停止打字机效果
	messages.value.forEach(msg => {
		if (msg.isTyping) {
			msg.isTyping = false
			msg.showCursor = false
			if (msg.content) msg.displayContent = msg.content
		}
	})

	const userMessage = {
		id: messages.value.length + 1,
		sender: 'user',
		content: message,
		time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
		avatar: userStore.userInfo?.avatar || '',
		images: uploadedImages.value.length > 0 ? [...uploadedImages.value] : undefined,
	}
	messages.value.push(userMessage)

	const userInput = message
	inputMessage.value = ''
	uploadedImages.value = []

	await saveMessageToBackend('user', message)
	resetScrollState()
	scrollToBottom(true)

	isLoading.value = true
	const aiMessageIndex = messages.value.length
	messages.value.push({
		id: aiMessageIndex,
		sender: 'ai',
		content: '',
		displayContent: '',
		time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
		avatar: 'ai',
		enableMarkdown: true,
		isToolExecuting: false,
		toolCompleted: false,
		hasToolPrompt: false,
		messageType: null,
		cardData: null,
		isTyping: false,
		typingIndex: 0,
		showCursor: false,
		isThinking: true,
		progress: false,
	})

	try {
		const userId = String(authStore.userId)
		const reader = await sendStreamRequest(userInput, userId)
		if (reader) {
			await streamResponse(aiMessageIndex, reader)
		}
	} catch (error) {
		if (error.name === 'AbortError') return
		let errorMsg = '对不起，暂时无法获取AI回复，请稍后重试。'
		if (error.message.includes('HTTP error')) {
			const statusCode = parseInt(error.message.match(/\d+/)?.[0] || '500')
			if (statusCode === 404) errorMsg = 'AI聊天服务暂时不可用，请稍后重试。'
			else if (statusCode === 500) errorMsg = '服务器内部错误，请稍后重试。'
			else errorMsg = `服务器错误(${statusCode})，请稍后重试。`
		} else if (error.message.includes('fetch')) {
			errorMsg = '网络连接超时，请检查网络设置。'
		}
		if (!messages.value[aiMessageIndex].content) {
			messages.value[aiMessageIndex].content = errorMsg
		}
	} finally {
		isLoading.value = false
	}
}

const clearChat = () => {
	ElMessageBox.confirm('确定要清空所有聊天记录吗？', '提示', {
		confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',
	}).then(async () => {
		try {
			const userId = String(authStore.userId)
			const clearResponse = await fetch(`${API_CONFIG.baseURL}${API_CONFIG.ai.clear}?userId=${userId}`, {
				method: 'DELETE',
				headers: { Authorization: `Bearer ${authStore.token}` }
			})
			const data = await clearResponse.json()
			if (data.code === 200) {
				messages.value = []
				const welcomeMessage = data.data?.welcomeMessage || '您好！我是您的AI饮食助手。'
				messages.value.push({
					id: Date.now(),
					sender: 'ai',
					content: welcomeMessage,
					time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
					avatar: 'ai',
					enableMarkdown: true,
				})
				ElMessage.success('聊天记录已清空')
				nextTick(() => { if (chatContainerRef.value) chatContainerRef.value.scrollTop = 0 })
			} else {
				ElMessage.error(data.message || '清空失败，请稍后重试')
			}
		} catch (error) {
			ElMessage.error('清空失败，请稍后重试')
		}
	}).catch(() => {})
}

// ========== UI交互 ==========

const handleKeydown = (event) => {
	if (event.key === 'Enter' && event.shiftKey) {
		event.preventDefault()
		sendMessage()
	}
}

const handleSendClick = () => {
	if (isStreaming.value) {
		stopStreamRequest()
		ElMessage.info('已停止AI回复')
	} else {
		sendMessage()
	}
}

const toggleCategory = (index) => {
	expandedCategory.value = expandedCategory.value === index ? null : index
}

const handleQuickQuestion = (question) => {
	inputMessage.value = question
	sendMessage()
	showQuickQuestions.value = false
}

const toggleEmoji = () => {
	showEmojiPicker.value = !showEmojiPicker.value
	if (showEmojiPicker.value) showQuickQuestions.value = false
}

const toggleQuickQuestions = () => {
	const isOpening = !showQuickQuestions.value
	showQuickQuestions.value = !showQuickQuestions.value
	if (isOpening) expandedCategory.value = null
	if (showQuickQuestions.value) showEmojiPicker.value = false
}

const selectEmoji = (emoji) => {
	inputMessage.value += emoji
	showEmojiPicker.value = false
	nextTick(() => {
		const textarea = bottomContainerRef.value?.querySelector('textarea')
		if (textarea) {
			textarea.focus()
			textarea.selectionStart = textarea.selectionEnd = textarea.value.length
		}
	})
}

const triggerImageUpload = () => {
	const input = bottomContainerRef.value?.querySelector('input[type="file"]')
	if (input) input.click()
}

const handleImageUpload = (event) => {
	const file = event.target.files[0]
	if (!file) return
	if (!file.type.startsWith('image/')) { ElMessage.error('请选择图片文件'); return }
	if (file.size > 10 * 1024 * 1024) { ElMessage.error('图片大小不能超过10MB'); return }

	const reader = new FileReader()
	reader.onload = (e) => {
		uploadedImages.value.push({ id: Date.now(), url: e.target.result, file })
		ElMessage.success('图片上传成功')
	}
	reader.readAsDataURL(file)
	event.target.value = ''
}

const removeUploadedImage = (imageId) => {
	const index = uploadedImages.value.findIndex(img => img.id === imageId)
	if (index > -1) uploadedImages.value.splice(index, 1)
}

const loadUserPreference = async () => {
	try {
		const userId = String(authStore.userId)
		const response = await fetch(`${API_CONFIG.baseURL}/v1/users/${userId}/preferences`, {
			headers: { Authorization: `Bearer ${authStore.token}` }
		})
		const data = await response.json()
		if (data?.data) aiPersonalDataEnabled.value = data.data.enableAiPersonalData === true
	} catch { aiPersonalDataEnabled.value = false }
}

const handlePersonalDataToggle = async (value) => {
	try {
		const userId = String(authStore.userId)
		await fetch(`${API_CONFIG.baseURL}/v1/users/${userId}/preferences`, {
			method: 'PUT',
			headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${authStore.token}` },
			body: JSON.stringify({ enableAiPersonalData: value }),
		})
		ElMessage.success(value ? '已开启个性化建议' : '已关闭个性化建议')
	} catch {
		ElMessage.error('设置保存失败')
		aiPersonalDataEnabled.value = !value
	}
}

const copyMessage = async (content) => {
	try {
		if (window.api?.clipboard) { window.api.clipboard.writeText(content); ElMessage.success('复制成功'); return }
		if (navigator.clipboard?.writeText) { await navigator.clipboard.writeText(content); ElMessage.success('复制成功'); return }
		const textArea = document.createElement('textarea')
		textArea.value = content
		textArea.style.cssText = 'position:fixed;left:-999999px;top:-999999px'
		textArea.setAttribute('readonly', '')
		document.body.appendChild(textArea)
		textArea.focus()
		textArea.select()
		document.execCommand('copy') ? ElMessage.success('复制成功') : ElMessage.error('复制失败')
		document.body.removeChild(textArea)
	} catch { ElMessage.error('复制失败,请手动复制') }
}

const handleMessageAction = async (command, content) => {
	if (command === 'copy') await copyMessage(content)
}

const handleClickOutside = (event) => {
	const quickQuestionsPanel = document.querySelector('.quick-questions-panel-fixed')
	if (bottomContainerRef.value && !bottomContainerRef.value.contains(event.target)) {
		if (showQuickQuestions.value) {
			if (quickQuestionsPanel && !quickQuestionsPanel.contains(event.target)) showQuickQuestions.value = false
		} else {
			showEmojiPicker.value = false
		}
	}
}

// ========== 监听 ==========

watch(messages, async (newMessages) => {
	if (isMounted.value && newMessages.length > 0) {
		setTimeout(async () => {
			if (!isMounted.value) return
			await nextTick()
			await nextTick()
			scrollToBottom(true)
		}, 300)
	}
}, { flush: 'post', immediate: false })

// ========== 生命周期 ==========

onMounted(async () => {
	isMounted.value = true
	document.addEventListener('click', handleClickOutside)
	if (chatContainerRef.value) {
		chatContainerRef.value.addEventListener('scroll', handleScroll)
		initScrollPosition()
	}
	await loadMessages()
	await loadUserPreference()
})

const scrollToBottomOnActivate = async () => {
	if (!chatContainerRef.value) return
	resetScrollState()
	await nextTick()
	await nextTick()
	requestAnimationFrame(() => {
		requestAnimationFrame(() => {
			if (chatContainerRef.value) {
				chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
			}
		})
	})
}

defineExpose({ scrollToBottomOnActivate })

onUnmounted(() => {
	isMounted.value = false
	document.removeEventListener('click', handleClickOutside)
	if (chatContainerRef.value) chatContainerRef.value.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped lang="less">
.chat-content-wrapper {
	position: relative;
	display: flex;
	flex-direction: column;
	height: 100%;
	width: 100%;
	flex: 1;
	gap: 12px;
	overflow: hidden;
	box-sizing: border-box;
}

.chat-messages {
	flex: 1;
	overflow-y: auto;
	background-color: #fff;
	border-radius: 16px;
	padding: 24px;
	box-shadow: 0 2px 16px 0 rgba(0, 0, 0, 0.06);
	scroll-behavior: smooth;
	transform: translateZ(0);
	-webkit-overflow-scrolling: touch;

	&::-webkit-scrollbar { width: 6px; }
	&::-webkit-scrollbar-thumb { background: #dee2e6; border-radius: 3px; &:hover { background: #adb5bd; } }

	.initial-loading-container {
		display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; gap: 24px;
		.ai-avatar-loading {
			display: flex; align-items: center; gap: 16px;
			.avatar-emoji { display: flex; align-items: center; justify-content: center; width: 56px; height: 56px; background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%); border-radius: 16px; color: #fff; box-shadow: 0 4px 12px rgba(255,107,107,0.3); animation: avatarFloat 2s ease-in-out infinite; }
			.typing-indicator {
				display: flex; align-items: center; gap: 6px; padding: 12px 18px;
				background: linear-gradient(135deg, #fff9fa 0%, #fff3f4 100%); border-radius: 20px; border: 1px solid #ffe0e3;
				.typing-dot { width: 8px; height: 8px; background: #ff6b6b; border-radius: 50%; animation: typingBounce 1.4s ease-in-out infinite;
					&:nth-child(1) { animation-delay: 0s; } &:nth-child(2) { animation-delay: 0.2s; } &:nth-child(3) { animation-delay: 0.4s; }
				}
			}
		}
		.loading-text { font-size: 1rem; color: #909399; animation: textPulse 2s ease-in-out infinite; }
	}

	.chat-message {
		display: flex; gap: 12px; margin-bottom: 24px; animation: messageFadeIn 0.4s ease-out;
		&.user-message {
			flex-direction: row-reverse; justify-content: flex-start;
			.message-content { align-items: flex-end;
				.message-text { background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%); color: #fff; border-radius: 20px 4px 20px 20px; box-shadow: 0 4px 12px rgba(255,107,107,0.25); font-weight: 500; }
			}
		}
		&.ai-message {
			flex-direction: row; justify-content: flex-start;
			.message-content { align-items: flex-start;
				.message-text { background: linear-gradient(135deg, #fff9fa 0%, #fff3f4 100%); color: #333; border-radius: 4px 20px 20px 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); border: 1px solid #ffe0e3; }
			}
		}
		.message-avatar { font-size: 42px; flex-shrink: 0; filter: drop-shadow(0 2px 6px rgba(0,0,0,0.15)); line-height: 1; }
		.message-avatar-custom { flex-shrink: 0; filter: drop-shadow(0 2px 6px rgba(0,0,0,0.15));
			:deep(.avatar-container) { padding: 8px; overflow: visible; }
			:deep(.user-avatar) { border-width: 2px; }
		}
		.message-content {
			display: flex; flex-direction: column; gap: 8px; max-width: 75%;
			.message-images { display: flex; gap: 8px; margin-bottom: 4px;
				.message-image { border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
					img { max-width: 150px; max-height: 150px; object-fit: cover; display: block; }
				}
			}
			.message-text {
				padding: 14px 18px; border-radius: 20px; line-height: 1.37; transition: all 0.3s cubic-bezier(0.4,0,0.2,1);
				font-size: 0.929rem; white-space: pre-wrap; word-break: break-word; display: inline; position: relative;
				.more-btn { display: inline; cursor: pointer; opacity: 0; transition: opacity 0.2s; color: inherit; vertical-align: middle; margin-left: 2px; user-select: none; &:active { transform: scale(0.95); } }
				&:hover .more-btn { opacity: 0.5; }
				.more-btn:hover { opacity: 1 !important; }
				&:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
				&.markdown-content {
					:deep(br) { display: block; content: ""; margin: 0; }
					:deep(h1), :deep(h2), :deep(h3) { margin: 8px 0 6px 0; font-weight: 600; line-height: 1.5; }
					:deep(h1) { font-size: 1.286rem; } :deep(h2) { font-size: 1.143rem; } :deep(h3) { font-size: 1rem; }
					:deep(p) { margin: 6px 0; line-height: 1.6; }
					:deep(pre) { background: rgba(0,0,0,0.05); border-radius: 8px; padding: 8px; margin: 6px 0; overflow-x: auto; code { font-family: 'Courier New', monospace; font-size: 0.857rem; line-height: 1.4; color: #333; } }
					:deep(code:not(pre code)) { background: rgba(0,0,0,0.05); padding: 2px 5px; border-radius: 4px; font-family: 'Courier New', monospace; font-size: 0.85em; }
					:deep(ul), :deep(ol) { margin: 6px 0; padding-left: 20px; line-height: 1.5; }
					:deep(li) { margin: 4px 0; }
					:deep(table) { width: 100%; border-collapse: collapse; margin: 12px 0; font-size: 0.929rem; background: #fff; border-radius: 8px; overflow: hidden;
						th { background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%); color: #fff; font-weight: 600; padding: 10px 12px; text-align: left; }
						td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; color: #333; line-height: 1.5; }
						tr:last-child td { border-bottom: none; } tr:hover { background-color: #f8f9fa; }
					}
				}
			}
			.message-time { font-size: 0.857rem; color: #a8abb2; margin-top: 2px; }
		}
	}
}

// 进度指示器
.message-progress-indicator {
	display: flex !important; align-items: center; gap: 8px; padding: 0; margin: 0;
	border-radius: 0; box-shadow: none; border: none; background: transparent;
	.progress-content { flex: 1; display: flex; flex-direction: row; align-items: center; gap: 8px;
		.progress-icon { display: flex; align-items: center; justify-content: center; width: 20px; height: 20px; color: #0ea5e9; font-size: 18px; flex-shrink: 0; svg { width: 18px; height: 18px; } }
		.progress-text { font-size: 0.9rem; font-weight: 500; line-height: 1.5; flex: 1; }
		.progress-dots { display: flex; align-items: center; gap: 6px;
			.progress-dot { width: 6px; height: 6px; border-radius: 50%; animation: dotBounce 1.4s ease-in-out infinite;
				&:nth-child(1) { animation-delay: 0s; } &:nth-child(2) { animation-delay: 0.2s; } &:nth-child(3) { animation-delay: 0.4s; }
			}
		}
	}
	&.status-thinking .progress-text { color: #ff6b6b; } &.status-thinking .progress-dot { background: #ff6b6b; }
	&.status-executing .progress-text { color: #ff9f43; } &.status-executing .progress-dot { background: #ff9f43; }
	&.status-completed { background: linear-gradient(135deg, #43a047 0%, #66bb6a 100%); border: 1px solid #43a047; .progress-text { color: #fff; } }
}

.card-message-wrapper { width: 100%; max-width: 600px; margin-bottom: 12px; }
.card-summary-text { margin-top: 12px; padding: 12px 16px; background: #f8f9fa; border-left: 3px solid #ff6b6b; border-radius: 4px; font-size: 14px; line-height: 1.6; color: #666; }
.message-text-content.typing, .card-summary-text.typing { position: relative; &::after { content: ""; display: inline-block; width: 2px; height: 1.2em; background: #ff6b6b; margin-left: 2px; vertical-align: text-bottom; animation: cursorBlink 1s step-end infinite; } }

// 底部容器
.bottom-container { flex-shrink: 0; display: flex; flex-direction: column; gap: 8px; position: relative; }

// 快捷提问面板
.quick-question-button-wrapper { position: relative; display: inline-block; }
.quick-questions-panel-fixed {
	position: absolute; bottom: calc(100% + 8px); left: 0; width: 360px; max-height: 420px;
	overflow-y: auto; overflow-x: hidden; box-sizing: border-box;
	background: #fff; border: 1px solid #e8ecef; border-radius: 12px; padding: 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.15); z-index: 9999;
	.quick-questions-title { display: flex; align-items: center; justify-content: space-between; font-size: 1rem; font-weight: 600; color: #303133; margin-bottom: 12px; padding-bottom: 10px; border-bottom: 2px solid #ff6b6b;
		.close-panel-icon { cursor: pointer; color: #909399; transition: all 0.2s ease; &:hover { color: #ff6b6b; transform: rotate(90deg); } }
	}
	.quick-questions-categories { display: flex; flex-direction: column; gap: 8px;
		.question-category { position: relative; display: flex; flex-direction: column; align-items: stretch;
			.category-header { flex-shrink: 0; display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; box-sizing: border-box; background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%); border: 1px solid #e8ecef; border-radius: 8px; cursor: pointer; user-select: none; transition: all 0.2s ease; width: 100%;
				&:hover { background: linear-gradient(135deg, #fff5f5 0%, #fff 100%); border-color: #ff6b6b; }
				&.is-active { background: linear-gradient(135deg, #ff6b6b 0%, #ff8787 100%); border-color: #ff6b6b; color: #fff; .category-arrow { color: #fff; } }
				span { font-size: 0.857rem; font-weight: 600; }
				.category-arrow { color: #909399; transition: transform 0.3s cubic-bezier(0.4,0,0.2,1); &.is-expanded { transform: rotate(90deg); } }
			}
			.category-questions { position: static; margin-top: 8px; margin-left: 0; display: flex; flex-direction: column; gap: 6px; width: 100%; box-sizing: border-box; background: #fff; border: 1px solid #e8ecef; border-radius: 8px; padding: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);
				.question-item { padding: 10px 12px; font-size: 0.786rem; color: #606266; background: #f5f7fa; border-radius: 6px; cursor: pointer; transition: all 0.2s cubic-bezier(0.4,0,0.2,1); text-align: left; line-height: 1.4; word-break: break-word; overflow-wrap: break-word;
					&:hover { background: linear-gradient(135deg, #409eff 0%, #5dade2 100%); color: #fff; transform: translateX(4px); }
				}
			}
		}
	}
}

// 已上传图片预览
.uploaded-images-preview { display: flex; gap: 12px; padding: 12px; background-color: #f8f9fa; border-radius: 12px;
	.uploaded-image-item { position: relative; width: 100px; height: 100px; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
		img { width: 100%; height: 100%; object-fit: cover; }
		.remove-image-btn { position: absolute; top: 4px; right: 4px; width: 24px; height: 24px; min-height: 24px; padding: 0; background-color: rgba(0,0,0,0.6); border: none; color: #fff; opacity: 0; transition: all 0.2s ease;
			&:hover { background-color: rgba(255,107,107,0.9); transform: scale(1.1); }
		}
		&:hover .remove-image-btn { opacity: 1; }
	}
}

// 表情面板
.emoji-panel { position: absolute; bottom: 100%; left: 0; margin-bottom: 8px; background: #fff; border: 1px solid #e8ecef; border-radius: 8px; padding: 10px; box-shadow: 0 6px 16px rgba(0,0,0,0.12); max-height: 180px; overflow-y: auto; z-index: 1000;
	&::-webkit-scrollbar { width: 6px; } &::-webkit-scrollbar-thumb { background: #dee2e6; border-radius: 3px; &:hover { background: #adb5bd; } }
	.emoji-grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 4px;
		.emoji-item { font-size: 1.429rem; text-align: center; padding: 6px 4px; border-radius: 6px; cursor: pointer; transition: all 0.2s cubic-bezier(0.4,0,0.2,1); user-select: none;
			&:hover { background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%); transform: scale(1.2); }
		}
	}
}

// 输入区域
.input-area {
	flex-shrink: 0; display: flex; gap: 12px; align-items: flex-end;
	background: linear-gradient(to bottom, #fff 0%, #fafbfc 100%); border: 1px solid #e8ecef; border-radius: 16px; padding: 16px 18px;
	box-shadow: 0 4px 16px rgba(0,0,0,0.06); transition: all 0.3s ease;
	&:hover { box-shadow: 0 6px 20px rgba(0,0,0,0.08); border-color: #e0e4e8; }
	.input-wrapper { flex: 1; display: flex; flex-direction: column; gap: 10px; }
	.toolbar { display: flex; justify-content: space-between; align-items: center; padding: 0 2px; margin-bottom: 6px;
		.toolbar-left { display: flex; gap: 8px; align-items: center; }
		.input-with-counter { flex: 1; display: flex; flex-direction: column; gap: 8px; margin-top: 8px; }
		:deep(.el-switch) { --el-switch-on-color: #ff6b6b; --el-switch-off-color: #dcdfe6; }
		:deep(.el-button) { border: 1px solid #e8ecef; background: #fff; color: #5a6c7d; transition: all 0.3s cubic-bezier(0.4,0,0.2,1); font-weight: 500; width: 32px; height: 32px; padding: 0;
			&:hover { border-color: #ff6b6b; color: #ff6b6b; transform: translateY(-2px) scale(1.05); box-shadow: 0 4px 12px rgba(255,107,107,0.3); background: #fff; }
			&.is-active { border-color: #ff6b6b; color: #ff6b6b; background: linear-gradient(135deg, rgba(255,107,107,0.1) 0%, rgba(255,82,82,0.1) 100%); box-shadow: 0 0 0 3px rgba(255,107,107,0.15), 0 2px 8px rgba(255,107,107,0.2); }
		}
	}
	.message-input { flex: 1;
		:deep(.el-textarea__inner) { border-radius: 10px; border: 2px solid #e8ecef; background: #fff; padding: 8px 12px; font-size: 1rem; line-height: 1.6; transition: all 0.3s cubic-bezier(0.4,0,0.2,1); resize: none;
			&:focus { border-color: #ff6b6b; box-shadow: 0 0 0 3px rgba(255,107,107,0.12); background: #fff; }
			&:hover:not(:focus) { border-color: #d0d7de; }
		}
	}
	.char-count-wrapper { display: flex; justify-content: flex-end; align-items: center; padding: 0 2px; margin-top: 4px;
		.char-count { font-size: 0.857rem; color: #909399; padding: 4px 12px; background: linear-gradient(135deg, #f5f7fa 0%, #eef1f6 100%); border-radius: 12px; font-weight: 600; border: 1px solid #e8ecef; transition: all 0.3s ease; display: inline-block; user-select: none;
			&.near-limit { color: #e6a23c; background: linear-gradient(135deg, #fef0e6 0%, #fde6d3 100%); border-color: #f5dab1; }
			&.at-limit { color: #f56c6c; background: linear-gradient(135deg, #fee 0%, #fecaca 100%); border-color: #fbc4c4; animation: pulse 1.5s ease-in-out infinite; }
		}
	}
	.stop-btn { flex-shrink: 0; background: #fff; border: 2px solid #f56c6c; color: #f56c6c; padding: 10px 20px; font-size: 1rem; font-weight: 600; border-radius: 10px; height: 56px; transition: all 0.3s cubic-bezier(0.4,0,0.2,1); display: flex; align-items: center; gap: 6px;
		&:hover { background: #f56c6c; color: #fff; box-shadow: 0 4px 12px rgba(245,108,108,0.35); transform: translateY(-2px); }
	}
	.send-btn { flex-shrink: 0; background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%); border: none; padding: 10px 28px; font-size: 1.071rem; font-weight: 600; border-radius: 10px; box-shadow: 0 2px 8px rgba(255,107,107,0.25); transition: all 0.3s cubic-bezier(0.4,0,0.2,1); height: 56px;
		&:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(255,107,107,0.35); }
		&:active:not(:disabled) { transform: translateY(0); }
		&:disabled { background: #e9ecef; box-shadow: none; color: #adb5bd; }
	}
	.toolbar-right { display: flex; align-items: center; }
	.clear-chat-btn { border-color: #fbc4c4; color: #f56c6c;
		&:hover { background-color: #f56c6c; border-color: #f56c6c; color: #fff; }
	}
	}

// 动画
@keyframes messageFadeIn { from { opacity: 0; transform: translateY(15px) scale(0.98); } to { opacity: 1; transform: translateY(0) scale(1); } }
@keyframes avatarFloat { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-8px); } }
@keyframes typingBounce { 0%, 60%, 100% { transform: translateY(0); } 30% { transform: translateY(-12px); } }
@keyframes textPulse { 0%, 100% { opacity: 0.6; } 50% { opacity: 1; } }
@keyframes dotBounce { 0%, 80%, 100% { transform: scale(0.8); opacity: 0.5; } 40% { transform: scale(1); opacity: 1; } }
@keyframes cursorBlink { 0%, 50% { opacity: 1; } 51%, 100% { opacity: 0; } }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.7; } }

.fade-in-enter-active, .fade-in-leave-active { transition: opacity 0.3s ease-out; }
.fade-in-enter-from, .fade-in-leave-to { opacity: 0; }
.slide-up-enter-active, .slide-up-leave-active { transition: all 0.25s cubic-bezier(0.4,0,0.2,1); }
.slide-up-enter-from { opacity: 0; transform: translateY(12px); }
.slide-up-leave-to { opacity: 0; transform: translateY(6px); }
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.3s cubic-bezier(0.4,0,0.2,1); }
.fade-slide-enter-from, .fade-slide-leave-to { opacity: 0; transform: translateY(-10px); }
.slide-right-sub-enter-active { transition: all 0.3s cubic-bezier(0.4,0,0.2,1); }
.slide-right-sub-leave-active { transition: all 0.2s cubic-bezier(0.4,0,0.2,1); }
.slide-right-sub-enter-from, .slide-right-sub-leave-to { opacity: 0; transform: translateX(-10px); }
.hidden-file-input { display: none; }

	// ========== 减少动画偏好支持 ==========
	@media (prefers-reduced-motion: reduce) {
		.chat-message { animation: none !important; }
		.avatar-emoji { animation: none !important; }
		.typing-dot { animation: none !important; opacity: 1; }
		.loading-text { animation: none !important; opacity: 1; }
		.progress-dot { animation: none !important; opacity: 1; }
		.char-count.at-limit { animation: none !important; }
		.message-text-content.typing::after,
		.card-summary-text.typing::after { animation: none !important; opacity: 1; }
	}
</style>
