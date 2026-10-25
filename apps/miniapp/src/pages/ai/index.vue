<template>
	<view class="ai-page">
		<!-- 统一顶部导航栏 -->
		<view class="unified-nav">
			<view class="nav-tabs">
				<view
					class="tab-item"
					v-for="tab in tabs"
					:key="tab.key"
					:class="{ active: activeTab === tab.key }"
					@click="switchTab(tab.key)"
				>
					<text class="tab-icon">{{ tab.icon }}</text>
					<text class="tab-label">{{ tab.label }}</text>
				</view>
			</view>
		</view>

		<!-- 标签页内容 -->
		<view class="tabs-content">
			<!-- AI聊天 -->
			<view v-if="activeTab === 'chat'" class="tab-pane chat-pane">
				<view class="chat-container">
					<view class="chat-content">
						<scroll-view v-if="showWelcomeGuide" class="welcome-scroll" scroll-y>
							<ChatWelcomeGuide
								@start="handleStartChat"
								@showQuestions="showQuestionsDrawer = true"
							/>
						</scroll-view>

						<ChatMessageList
							v-else
							:messages="displayMessages"
							:scrollIntoView="scrollIntoView"
						/>
					</view>

					<ChatInputArea
						class="chat-input-fixed"
						:inputText="inputText"
						:isStreaming="isStreaming"
						:uploadedImages="uploadedImages"
						:showEmojiPicker="showEmojiPicker"
						:loadingText="loadingText"
						@update:inputText="inputText = $event"
						@send="sendMessage"
						@stop="stopStreamingHandler"
						@toggleEmoji="toggleEmoji"
						@selectEmoji="selectEmoji"
						@chooseImage="handleChooseImage"
						@removeImage="removeUploadedImage"
						@toggleQuickQuestions="showQuestionsDrawer = true"
						@clearHistory="confirmClearHistory"
					/>
				</view>

				<QuickQuestionsDrawer
					:visible="showQuestionsDrawer"
					:questions="quickQuestions"
					@select="handleQuickQuestionSelect"
					@update:visible="showQuestionsDrawer = $event"
				/>
			</view>

			<!-- 菜品识别 -->
			<view v-if="activeTab === 'recognition'" class="tab-pane">
				<DishRecognition />
			</view>

			<!-- 食谱优化 -->
			<view v-if="activeTab === 'recipe'" class="tab-pane">
				<RecipeOptimization />
			</view>

			<!-- 内容提取 -->
			<view v-if="activeTab === 'extraction'" class="tab-pane">
				<ContentExtraction />
			</view>
		</view>
	</view>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { useChatMessages } from '@/composables/ai'
import { useChatInput } from '@/composables/ai'
import { useChatStreaming } from '@/composables/ai'
import { useErrorHandler } from '@/composables/useErrorHandler'
import { createPageDebug } from '@/utils/page-debug'

// 组件导入
import ChatMessageList from './components/ChatMessageList.vue'
import ChatWelcomeGuide from './components/ChatWelcomeGuide.vue'
import QuickQuestionsDrawer from './components/QuickQuestionsDrawer.vue'
import ChatInputArea from './components/ChatInputArea.vue'
import DishRecognition from './components/DishRecognition.vue'
import RecipeOptimization from './components/RecipeOptimization.vue'
import ContentExtraction from './components/ContentExtraction.vue'

// 用户信息store
const userStore = useUserStore()
const pageDebug = createPageDebug('AI助手')

// 错误处理
const { handleError, confirm } = useErrorHandler()

// ==================== 标签页 ====================
const activeTab = ref('chat')
const tabs = ref([
	{ key: 'chat', label: 'AI聊天', icon: '聊' },
	{ key: 'recognition', label: '识别', icon: '识' },
	{ key: 'recipe', label: '食谱', icon: '谱' },
	{ key: 'extraction', label: '提取', icon: '提' }
])

// ==================== 使用 Composables ====================
const {
	messages,
	scrollIntoView,
	hasLoadedHistory,
	isEmpty,
	loadHistory,
	addMessage,
	appendContent,
	updateMessage,
	saveMessageToBackend,
	clearHistory
} = useChatMessages()

const {
	inputText,
	uploadedImages,
	showEmojiPicker,
	quickQuestions,
	quickQuestionsExpanded,
	canSend,
	selectEmoji,
	toggleEmoji,
	toggleQuickQuestions,
	chooseImage,
	removeImage,
	getInputData,
	resetInput
} = useChatInput()

const {
	streamingState,
	isStreaming,
	isTyping,
	isLoading,
	loadingText,
	startStreaming,
	stopStreaming,
	parseCardData
} = useChatStreaming()

// ==================== 计算属性 ====================
const displayMessages = computed(() => messages.value)

const WELCOME_MESSAGE = '您好！我是AI饮食助手，有什么可以帮您的吗？'
const hasUserContext = computed(() => {
	return Boolean(uni.getStorageSync('userId') || userStore.userInfo?.userId)
})

const showWelcomeGuide = computed(() => {
	if (isLoading.value) {
		return false
	}

	if (hasUserContext.value && !hasLoadedHistory.value && messages.value.length === 0) {
		return false
	}

	if (messages.value.length === 0) {
		return true
	}

	return (
		messages.value.length === 1 &&
		messages.value[0].sender === 'ai' &&
		messages.value[0].content === WELCOME_MESSAGE
	)
})

const showQuestionsDrawer = ref(false)

// ==================== 方法 ====================
const switchTab = (tabKey) => {
	pageDebug.action('切换AI标签页', {
		from: activeTab.value,
		to: tabKey
	})
	activeTab.value = tabKey
	showQuestionsDrawer.value = false
	showEmojiPicker.value = false
	if (tabKey === 'chat') {
		setTimeout(() => {
			scrollToBottom()
		}, 300)
	}
}

const scrollToBottom = async () => {
	await new Promise((resolve) => setTimeout(resolve, 100))
}

const sendMessage = async () => {
	const { text } = getInputData()
	if (!text) return
	pageDebug.action('发送AI消息', {
		textLength: text.length,
		hasImage: uploadedImages.value.length > 0
	})

	addMessage({
		sender: 'user',
		content: text,
		avatar: '👤',
		isUser: true
	})

	saveMessageToBackend('user', text)

	if (quickQuestionsExpanded.value) {
		quickQuestionsExpanded.value = false
	}
	showQuestionsDrawer.value = false

	resetInput()
	await scrollToBottom()

	const aiMessageIndex = messages.value.length
	addMessage({
		sender: 'ai',
		content: '',
		avatar: '🤖',
		isUser: false
	})

	await scrollToBottom()

	await startStreaming(
		text,
		messages.value.slice(0, -1).map((msg) => ({
			role: msg.isUser ? 'user' : 'assistant',
			content: msg.content
		})),
		(content) => {
			appendContent(aiMessageIndex, content)
		},
		async () => {
			const aiContent = messages.value[aiMessageIndex].content
			const { content: cleanContent, cardData, messageType } = parseCardData(aiContent)
			updateMessage(aiMessageIndex, {
				content: cleanContent,
				messageType,
				cardData
			})
			await saveMessageToBackend('ai', cleanContent, messageType, cardData)
			pageDebug.requestSuccess('AI回复完成', {
				messageType,
				contentLength: cleanContent.length
			})
		},
		(error) => {
			pageDebug.requestFail('AI回复失败', error)
			handleError(error, 'sendMessage')
			if (!messages.value[aiMessageIndex].content) {
				updateMessage(aiMessageIndex, {
					content: '抱歉，我现在无法回答这个问题，请稍后再试。'
				})
			}
		}
	)
}

const stopStreamingHandler = () => {
	pageDebug.action('停止AI回复')
	stopStreaming()
	uni.showToast({
		title: '已停止AI回复',
		icon: 'none'
	})
}

const handleChooseImage = async () => {
	try {
		pageDebug.action('选择聊天图片')
		await chooseImage(3)
		pageDebug.requestSuccess('选择聊天图片', {
			count: uploadedImages.value.length
		})
	} catch (error) {
		pageDebug.requestFail('选择聊天图片', error)
		handleError(error, 'chooseImage')
	}
}

const removeUploadedImage = (index) => {
	pageDebug.action('移除聊天图片', {
		index
	})
	removeImage(index)
}

const handleQuickQuestionSelect = (question) => {
	pageDebug.action('选择快捷提问', {
		question
	})
	inputText.value = question
	sendMessage()
}

const handleStartChat = (question = '') => {
	pageDebug.action('开始AI聊天', {
		hasPresetQuestion: Boolean(question)
	})
	if (question) {
		inputText.value = question
		sendMessage()
		return
	}
	showQuestionsDrawer.value = true
}

const confirmClearHistory = async () => {
	pageDebug.action('尝试清空聊天记录')
	const confirmed = await confirm('确定要清空所有聊天记录吗？')
	if (confirmed) {
		try {
			const userId = uni.getStorageSync('userId') || userStore.userInfo?.userId
			const success = await clearHistory(userId)
			if (success) {
				pageDebug.requestSuccess('清空聊天记录')
				uni.showToast({
					title: '已清空聊天记录',
					icon: 'success'
				})
			}
		} catch (error) {
			pageDebug.requestFail('清空聊天记录', error)
			handleError(error, 'clearHistory')
		}
	}
}

// ==================== 生命周期 ====================
onMounted(async () => {
	pageDebug.lifecycle('页面挂载', {
		hasUserContext: hasUserContext.value,
		activeTab: activeTab.value
	})
	const userId = uni.getStorageSync('userId') || userStore.userInfo?.userId
	if (userId) {
		pageDebug.requestStart('加载聊天历史', {
			userId
		})
		await loadHistory(userId)
		pageDebug.requestSuccess('加载聊天历史', {
			messageCount: messages.value.length
		})
	} else {
		pageDebug.anomaly('缺少用户信息，跳过加载聊天历史')
	}
})
</script>
<style lang="scss">
page {
	height: 100%;
	width: 100%;
	overflow: hidden;
	-webkit-overflow-scrolling: touch;
}
</style>
<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.ai-page {
	height: 100%;
	min-height: 100vh;
	width: 100%;
	background: $bg-color-light;
	display: flex;
	flex-direction: column;
	overflow: hidden;
	box-sizing: border-box;
}

/* 统一顶部导航栏 */
.unified-nav {
	background: $bg-color-white;
	padding: $spacing-sm $spacing-md;
	flex-shrink: 0;
	position: relative;
	z-index: $z-index-sticky;
}

.nav-tabs {
	display: inline-flex;
	align-items: center;
	gap: $spacing-xs;
	padding: $spacing-xs;
	background: $bg-color-base;
	border-radius: $border-radius-round;
	margin: 0 auto;
}

.tab-item {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 10rpx;
	padding: 14rpx 22rpx;
	border-radius: $border-radius-round;
	transition: $transition-base;
	position: relative;

	&.active {
		background: $bg-color-white;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.06);

		.tab-label {
			color: $primary-500;
			font-weight: $font-weight-bold;
		}
	}
}

.tab-icon {
	font-size: 22rpx;
	color: $text-color-secondary;
	line-height: 1;
}

.tab-item.active .tab-icon {
	color: $primary-500;
}

.tab-label {
	font-size: 24rpx;
	color: $text-color-regular;
	transition: $transition-base;
	line-height: 1;
}

/* 标签页内容 */
.tabs-content {
	flex: 1;
	height: 0;
	display: flex;
	flex-direction: column;
	overflow: hidden;
}

.tab-pane {
	flex: 1;
	height: 100%;
	overflow: hidden;
	display: flex;
	flex-direction: column;
}

/* 聊天容器 */
.chat-pane {
	display: flex;
	flex-direction: column;
	height: 100%;
	overflow: hidden;
	position: relative;
}

.chat-container {
	display: flex;
	flex: 1;
	flex-direction: column;
	min-height: 0;
	overflow: hidden;
	background: $bg-color-white;
	position: relative;
}

.chat-content {
	flex: 1;
	height: 0;
	min-height: 0;
	overflow: hidden;
	box-sizing: border-box;
}

.welcome-scroll {
	flex: 1;
	min-height: 0;
	height: 100%;
}

.welcome-scroll :deep(.welcome-guide) {
	min-height: 100%;
}

.chat-input-fixed {
	flex-shrink: 0;
	position: relative;
	z-index: 1;
}
</style>
