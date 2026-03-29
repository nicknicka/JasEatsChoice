<template>
	<view class="ai-page">
		<!-- 统一顶部导航栏-->
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
					<!-- 聊天消息 -->
					<scroll-view
						class="chat-messages"
						scroll-y
						:scroll-into-view="scrollIntoView"
						:scroll-with-animation="true"
					>
						<!-- 欢迎消息 -->
						<view class="message-welcome" v-if="isShowWelcome">
							<text class="welcome-icon">👋</text>
							<text class="welcome-text">您好！我是您的专属AI饮食助手</text>
							<text class="welcome-desc"
								>我可以帮您推荐健康食谱、分析营养成分、制定专属饮食计划、解答各类饮食疑问</text
							>
						</view>

						<!-- 空状态提示（无历史记录且无欢迎消息时） -->
						<view class="empty-state" v-if="messages.length === 0">
							<text class="empty-text"
								>暂无对话记录，试试上方的快捷提问吧</text
							>
						</view>

						<!-- 消息列表 -->
						<view
							class="message-wrapper"
							v-for="(msg, index) in displayMessages"
							:key="msg.id"
							:id="'msg-' + index"
						>
							<view class="message" :class="{ user: msg.isUser }">
								<!-- AI消息 -->
								<view class="message-avatar" v-if="!msg.isUser">
									<text class="avatar-icon">🤖</text>
								</view>

								<!-- 消息内容 -->
								<view
									class="message-content"
									:class="{ user: msg.isUser }"
								>
									<!-- 文本消息 -->
									<text class="content-text">{{ msg.content }}</text>

									<!-- 时间戳 -->
									<text class="message-time">{{ msg.time }}</text>
								</view>

								<!-- 用户头像 -->
								<view class="message-avatar user" v-if="msg.isUser">
									<image
										class="avatar-image"
										:src="userInfo.avatar"
										mode="aspectFill"
									/>
								</view>
							</view>
						</view>

						<!-- 加载动画 -->
						<view class="message-wrapper" v-if="isTyping">
							<view class="message">
								<view class="message-avatar">
									<text class="avatar-icon">🤖</text>
								</view>
								<view class="message-content typing">
									<view class="typing-indicator">
										<view class="typing-dot"></view>
										<view class="typing-dot"></view>
										<view class="typing-dot"></view>
									</view>
								</view>
							</view>
						</view>
					</scroll-view>

					<view class="chat-input-area">
						<view class="input-extensions">
							<text class="extension-icon">🎤</text>
							<view class="nav-actions">
								<text
									class="action-btn stop-btn"
									v-if="activeTab === 'chat' && isStreaming"
									@click="stopStreaming"
								>
									⏹️
								</text>
								<text
									class="action-btn clear-btn"
									v-if="activeTab === 'chat' && !isStreaming"
									@click="clearHistory"
								>
									🗑️
								</text>
							</view>
						</view>

						<view class="input-area">
							<input
								class="chat-input"
								type="text"
								v-model="inputText"
								placeholder="输入您的饮食问题，例如：减脂一日三餐食谱、家常菜营养分析"
								:maxlength="500"
								@confirm="sendMessage"
								confirm-type="send"
							/>

							<!-- 圆形发送按钮（有输入时显示，空输入时隐藏） -->
							<view class="send-btn" @click="sendMessage">
								<text class="send-icon">➤</text>
							</view>
						</view>
					</view>
				</view>
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
import { ref, computed, nextTick, onMounted, onUnmounted } from "vue";
import { useUserStore } from "@/store";
import { formatTime } from "@/utils/helper";
import { aiApi } from "@/api";
import DishRecognition from "./components/DishRecognition.vue";
import RecipeOptimization from "./components/RecipeOptimization.vue";
import ContentExtraction from "./components/ContentExtraction.vue";

// 用户信息store
const userStore = useUserStore();

// 当前激活的标签
const activeTab = ref("chat");

// 标签页配置
const tabs = ref([
	{ key: "chat", label: "AI聊天", icon: "💬" },
	{ key: "recognition", label: "菜品识别", icon: "📷" },
	{ key: "recipe", label: "食谱优化", icon: "🍳" },
	{ key: "extraction", label: "内容提取", icon: "📝" },
]);

// 用户信息
const userInfo = ref({
	avatar:
		userStore.userInfo?.avatar ||
		"https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=用户",
});

// 消息列表
const messages = ref([]);

// 输入文本
const inputText = ref("");

// 是否正在输入
const isTyping = ref(false);

// 是否正在流式传输
const isStreaming = ref(false);

// 滚动位置
const scrollIntoView = ref("");

// 快捷提问展开状态（默认展开）
const quickQuestionsExpanded = ref(true);

// AbortController用于取消请求
let abortController = null;

// 是否已加载历史记录
const hasLoadedHistory = ref(false);

// 是否显示欢迎消息
const isShowWelcome = computed(() => {
	return (
		messages.value.length === 1 &&
		messages.value[0].sender === "ai" &&
		!hasLoadedHistory.value
	);
});

// 快捷提问 - 参考前端设计优化
const quickQuestions = ref([
	"推荐适合减肥的食谱",
	"今日卡路里摄入建议",
	"如何搭配营养均衡的饮食",
	"推荐低卡路里零食",
	"适合运动后的食物",
]);

// 显示的消息（排除欢迎消息）
const displayMessages = computed(() => {
	return messages.value;
});

/**
 * 获取用户ID
 */
const getUserId = () => {
	return uni.getStorageSync("userId") || userStore.userInfo?.userId || "1";
};

/**
 * 切换标签页
 */
const switchTab = (tabKey) => {
	activeTab.value = tabKey;

	// 如果切换到聊天tab，触发滚动
	if (tabKey === "chat") {
		setTimeout(() => {
			scrollToBottom();
		}, 300);
	}
};

/**
 * 滚动到底部
 */
const scrollToBottom = async () => {
	await nextTick();
	if (messages.value.length > 0) {
		scrollIntoView.value = "msg-" + (messages.value.length - 1);
	}
};

/**
 * 加载聊天历史记录
 */
const loadChatHistory = async () => {
	try {
		const userId = getUserId();
		console.log("📥 开始加载聊天记录，userId:", userId);

		const historyResponse = await aiApi.getHistory(userId);
		console.log("📡 后端响应:", historyResponse);

		if (
			historyResponse.code === 200 &&
			historyResponse.data &&
			historyResponse.data.length > 0
		) {
			// 转换为前端格式
			messages.value = historyResponse.data.map((item, index) => ({
				id: index + 1,
				sender: item.sender,
				content: item.content,
				time: formatTime(new Date(item.createTime)),
				avatar: item.sender === "ai" ? "🤖" : "👤",
				isUser: item.sender === "user",
			}));
			hasLoadedHistory.value = true;
			console.log("✅ 成功加载聊天历史:", messages.value.length, "条消息");
		} else {
			// 没有历史记录，显示欢迎消息
			console.log("📭 没有历史记录，显示欢迎消息");
			addWelcomeMessage();
			hasLoadedHistory.value = false;
		}

		// 滚动到底部
		await scrollToBottom();
	} catch (error) {
		console.error("❌ 加载聊天记录失败:", error);

		// 加载失败时显示欢迎消息
		addWelcomeMessage();
		hasLoadedHistory.value = false;
	}
};

/**
 * 添加欢迎消息
 */
const addWelcomeMessage = () => {
	hasLoadedHistory.value = false;
	messages.value = [
		{
			id: Date.now(),
			sender: "ai",
			content: "您好！我是AI饮食助手，有什么可以帮您的吗？",
			time: formatTime(new Date()),
			avatar: "🤖",
			isUser: false,
		},
	];
};

/**
 * 保存消息到后端
 */
const saveMessageToBackend = async (sender, content) => {
	try {
		const userId = getUserId();
		await aiApi.saveMessage({
			userId,
			sender,
			content,
		});
		console.log("✅ 消息已保存到后端:", sender);
	} catch (error) {
		console.error("❌ 保存消息到后端失败:", error);
	}
};

/**
 * 发送消息（支持流式响应）
 */
const sendMessage = async () => {
	const text = inputText.value.trim();
	if (!text) return;

	console.log("==================== AI聊天请求开始 ====================");
	console.log("⏰ 请求时间:", new Date().toLocaleString());
	console.log("📝 用户消息:", text);

	// 清空输入框
	inputText.value = "";

	// 添加用户消息
	const userMsg = {
		id: Date.now(),
		sender: "user",
		content: text,
		time: formatTime(new Date()),
		avatar: "👤",
		isUser: true,
	};
	messages.value.push(userMsg);

	// 发送消息后自动收起快捷提问，提升体验
	if (quickQuestionsExpanded.value) {
		quickQuestionsExpanded.value = false;
	}

	// 滚动到底部
	await scrollToBottom();

	// 创建AI消息对象（初始为空）
	const aiMessageIndex = messages.value.length;
	messages.value.push({
		id: Date.now() + 1,
		sender: "ai",
		content: "",
		time: formatTime(new Date()),
		avatar: "🤖",
		isUser: false,
	});

	await scrollToBottom();

	// 显示输入状态
	isTyping.value = true;
	isStreaming.value = true;

	try {
		// 调用AI流式对话API
		await aiApi.streamChat(
			{
				message: text,
				conversationId: "",
				history: messages.value.slice(0, -1).map((msg) => ({
					role: msg.isUser ? "user" : "assistant",
					content: msg.content,
				})),
			},
			// onMessage - 接收消息内容
			(content) => {
				messages.value[aiMessageIndex].content += content;
				nextTick(() => scrollToBottom());
			},
			// onComplete - 完成回调
			async () => {
				console.log("✅ AI消息接收完成");
				isTyping.value = false;
				isStreaming.value = false;

				// 保存AI消息到后端
				const aiContent = messages.value[aiMessageIndex].content;
				await saveMessageToBackend("ai", aiContent);

				// 保存到本地存储作为备份
				saveChatHistoryToLocal();

				await scrollToBottom();
			},
			// onError - 错误处理
			(error) => {
				console.error("❌ AI请求失败:", error);
				isTyping.value = false;
				isStreaming.value = false;

				// 如果没有收到任何内容，显示错误消息
				if (!messages.value[aiMessageIndex].content) {
					messages.value[aiMessageIndex].content =
						"抱歉，我现在无法回答这个问题，请稍后再试。";
				}
			}
		);
	} catch (error) {
		console.error("❌ 发送消息失败:", error);
		isTyping.value = false;
		isStreaming.value = false;

		// 如果API调用失败，使用本地模拟回复
		const aiMsg = {
			id: Date.now() + 1,
			sender: "ai",
			content: generateAIResponse(text),
			time: formatTime(new Date()),
			avatar: "🤖",
			isUser: false,
		};

		messages.value[aiMessageIndex] = aiMsg;
		await scrollToBottom();
		saveChatHistoryToLocal();

		uni.showToast({
			title: "网络连接失败，已切换到离线模式",
			icon: "none",
		});
	}
};

/**
 * 生成AI回复（模拟）
 */
const generateAIResponse = (text) => {
	const responses = {
		推荐健康食谱:
			"根据您的需求，我为您推荐以下健康食谱：\n\n早餐：燕麦牛奶粥配鸡蛋（约420卡）\n午餐：清蒸鲈鱼配时蔬（约580卡）\n晚餐：鸡胸肉蔬菜沙拉（约380卡）\n\n这些食谱营养均衡，适合日常食用。",
		分析营养成分:
			"请告诉我您想分析哪种食物的营养成分？我可以为您提供详细的分析报告。",
		制定饮食计划:
			"为了制定个性化的饮食计划，我需要了解以下信息：\n\n1. 您的身高体重\n2. 运动习惯\n3. 饮食偏好\n4. 健康目标\n\n请提供这些信息，我会为您制定专属计划。",
		default: `收到您的问题："${text}"\n\n我正在为您分析，稍后会给出专业建议。\n\n您可以问我关于：\n• 营养成分分析\n• 食谱推荐\n• 饮食计划\n• 健康建议`,
	};

	for (const [key, value] of Object.entries(responses)) {
		if (text.includes(key)) {
			return value;
		}
	}

	return responses["default"];
};

/**
 * 快捷提问
 */
const askQuickQuestion = (question) => {
	inputText.value = question;
	sendMessage();
};

/**
 * 切换快捷提问展开/折叠状态
 */
const toggleQuickQuestions = () => {
	quickQuestionsExpanded.value = !quickQuestionsExpanded.value;
};

/**
 * 清空历史
 */
const clearHistory = async () => {
	uni.showModal({
		title: "清空聊天记录",
		content: "确定要清空所有聊天记录吗？",
		confirmColor: "#FF6B35",
		success: async (res) => {
			if (res.confirm) {
				try {
					const userId = getUserId();
					console.log("🗑️ 开始清空聊天记录，userId:", userId);

					// 调用后端API清空聊天记录
					const clearResponse = await aiApi.clearHistory(userId);

					if (clearResponse.code === 200) {
						console.log("✅ 后端清空成功");

						// 清空前端显示
						messages.value = [];

						// 重新加载消息（会显示欢迎消息）
						await loadChatHistory();

						// 清空本地存储
						uni.removeStorageSync("chatHistory");

						uni.showToast({
							title: "已清空聊天记录",
							icon: "success",
						});
					} else {
						uni.showToast({
							title: clearResponse.message || "清空失败，请稍后重试",
							icon: "none",
						});
					}
				} catch (error) {
					console.error("❌ 清空聊天记录失败:", error);

					// 即使后端失败，也清空前端显示
					messages.value = [];
					addWelcomeMessage();
					uni.removeStorageSync("chatHistory");

					uni.showToast({
						title: "已清空本地记录",
						icon: "success",
					});
				}
			}
		},
	});
};

/**
 * 保存聊天历史到本地
 */
const saveChatHistoryToLocal = () => {
	try {
		uni.setStorageSync("chatHistory", JSON.stringify(messages.value));
	} catch (error) {
		console.error("保存聊天历史失败:", error);
	}
};

/**
 * 停止流式传输
 */
const stopStreaming = () => {
	if (abortController) {
		console.log("🛑 用户主动停止流式传输");
		abortController.abort();
		isStreaming.value = false;
		isTyping.value = false;

		uni.showToast({
			title: "已停止AI回复",
			icon: "none",
		});
	}
};

// 组件挂载
onMounted(async () => {
	await loadChatHistory();
});

// 组件卸载
onUnmounted(() => {
	// 清理请求
	if (abortController) {
		abortController.abort();
	}
});
</script>

<style lang="scss" scoped>
@import "@/styles/variables.scss";
@import "@/styles/mixins.scss";

.ai-page {
	min-height: 100vh;
	background: $bg-color-white; // 纯白背景，删除渐变
	display: flex;
	flex-direction: column;
}

/* ==================== 统一顶部导航栏（合并标签栏+AI助手头部） ==================== */
.unified-nav {
	height: $nav-height; // 112rpx，贴合移动端规范
	background: $bg-color-white;
	border-bottom: 1rpx solid $border-color-light;
	box-shadow: $box-shadow-sm;
	@include flex-between;
	padding: 0 $spacing-md;
	position: sticky;
	top: 0;
	z-index: $z-index-sticky;
	flex-shrink: 0;
}

.nav-tabs {
	flex: 1;
	@include flex-center;
	gap: $spacing-md;
}

.tab-item {
	@include flex-center-column;
	gap: $spacing-xs;
	padding: $spacing-sm;
	transition: $transition-base;
	position: relative;

	&.active {
		.tab-label {
			color: $primary-500;
			font-weight: $font-weight-bold;
		}

		&::after {
			content: "";
			position: absolute;
			bottom: 0;
			left: 50%;
			transform: translateX(-50%);
			width: 32rpx; // 从60rpx缩小为32rpx
			height: 4rpx; // 从6rpx缩小为4rpx
			background: linear-gradient(135deg, $primary-500, $primary-800);
			border-radius: 2rpx;
		}
	}
}

.tab-icon {
	font-size: 40rpx; // 从44rpx调整为40rpx
	display: block;
}

.tab-label {
	font-size: 26rpx; // 从24rpx调整为26rpx
	color: $text-color-regular; // 未激活态用#666666
	transition: $transition-base;
}

.nav-title {
	font-size: $font-size-lg;
	font-weight: $font-weight-bold;
	color: $text-color-primary;
	flex: 1;
	text-align: center;
}

.nav-actions {
	@include flex-center;
	gap: $spacing-sm;
}

.action-btn {
	width: $touch-min-size; // 96rpx，符合触屏规范
	height: $touch-min-size;
	@include flex-center;
	background-color: $bg-color-base;
	border-radius: $border-radius-round;
	font-size: $font-size-lg;
	color: $text-color-primary;
	transition: $transition-fast;

	&.stop-btn {
		background-color: $danger-color;
		color: $bg-color-white;
	}

	&:active {
		opacity: 0.7;
		transform: scale(0.95);
	}
}

/* ==================== 标签页内容 ==================== */
.tabs-content {
	flex: 1;
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

/* ==================== 聊天容器 ==================== */
.chat-pane {
	display: flex;
	flex-direction: column;
	height: 100%;
	overflow: hidden;
	position: relative; // 为 fixed 定位的快捷提问提供参考
}

/* ==================== 快捷提问悬浮区（提升到 chat-pane 级别） ==================== */
.quick-questions-floating {
	position: fixed;
	bottom: 160rpx; // ✨ 在输入框上方（输入区域144rpx + 间距16rpx）
	left: $spacing-lg;
	right: $spacing-lg;
	background: rgba(255, 255, 255, 0.98);
	backdrop-filter: blur(20rpx);
	border-radius: $border-radius-lg;
	padding: $spacing-lg;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
	border: 1rpx solid $border-color-light;
	z-index: $z-index-float;
	animation: slideUpFadeIn 0.3s ease-out;
}

@keyframes slideUpFadeIn {
	from {
		opacity: 0;
		transform: translateY(20rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.quick-header {
	@include flex-between;
	margin-bottom: $spacing-md;
}

.quick-title {
	font-size: $font-size-base;
	color: $text-color-primary;
	font-weight: $font-weight-bold;
}

.quick-close {
	font-size: $font-size-lg;
	color: $text-color-secondary;
	padding: $spacing-xs;
	margin: -$spacing-xs;
	transition: $transition-fast;

	&:active {
		color: $text-color-primary;
		transform: scale(0.9);
	}
}

.quick-list {
	display: flex;
	flex-direction: column;
	gap: $spacing-sm;
	max-height: 400rpx; // 限制最大高度
	overflow-y: auto;
}

.quick-item {
	@include flex-between;
	padding: $spacing-md $spacing-lg;
	background: $bg-color-white;
	border-radius: $border-radius-base;
	border: 1rpx solid $primary-300;
	transition: $transition-base;
	box-shadow: $box-shadow-sm;

	&:active {
		transform: scale(0.98);
		border-color: $primary-500;
		background: $primary-100;
		box-shadow: $box-shadow-md;
	}
}

.quick-text {
	flex: 1;
	font-size: 30rpx;
	color: $text-color-primary;
	line-height: $line-height-lg;
	font-weight: $font-weight-medium;
}

.quick-arrow {
	font-size: $font-size-xl;
	color: $primary-500;
	margin-left: $spacing-md;
}

/* 快捷提问折叠按钮（提升到 chat-pane 级别） */
.quick-toggle-btn {
	position: fixed;
	bottom: 180rpx; // ✨ 在快捷提问下方（快捷提问160rpx + 间距20rpx）
	left: $spacing-lg;
	width: 80rpx;
	height: 80rpx;
	@include flex-center;
	background: rgba(255, 107, 53, 0.95);
	backdrop-filter: blur(20rpx);
	border-radius: 50%;
	box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.3);
	z-index: $z-index-float + 1; // 比快捷提问高一层
	transition: $transition-base;
	animation: scaleIn 0.3s ease-out;

	&:active {
		transform: scale(0.95);
	}
}

@keyframes scaleIn {
	from {
		opacity: 0;
		transform: scale(0.8);
	}
	to {
		opacity: 1;
		transform: scale(1);
	}
}

.toggle-icon {
	font-size: $font-size-xl;
}

/* ==================== 聊天消息区（可滚动区域，100%动态空间） ==================== */
.chat-messages {
	flex: 1;
	height: 0; // 关键：配合 flex: 1 使用，确保不超出容器
	padding: $spacing-lg;
	padding-bottom: 160rpx; // ✨ 底部留出空间（输入区域144rpx + 间距16rpx）
	background: $bg-color-light;
	scrollbar-width: none;
	-ms-overflow-style: none;
	transition: padding-bottom 0.3s ease; // 平滑过渡

	&::-webkit-scrollbar {
		display: none;
	}

	// 快捷提问展开时，增加底部间距
	&.with-quick-questions {
		padding-bottom: 480rpx; // ✨ 输入区域160rpx + 快捷提问约300rpx + 间距
	}
}

/* 欢迎消息 */
.message-welcome {
	@include flex-center-column;
	align-items: center;
	padding: 80rpx $spacing-lg;
	text-align: center;
	animation: welcomeFadeIn $duration-slow ease-out;
}

@keyframes welcomeFadeIn {
	from {
		opacity: 0;
		transform: translateY(30rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.welcome-icon {
	font-size: 120rpx;
	margin-bottom: $spacing-lg;
	animation: iconBounce 1s ease-out;
	display: block;
}

@keyframes iconBounce {
	0%,
	100% {
		transform: scale(1);
	}
	50% {
		transform: scale(1.2);
	}
}

.welcome-text {
	font-size: $font-size-xl;
	font-weight: $font-weight-bold;
	color: $primary-500;
	margin-bottom: $spacing-md;
	display: block;
}

.welcome-desc {
	font-size: $font-size-base;
	color: $text-color-regular;
	line-height: $line-height-lg;
	padding: 0 $spacing-lg;
	display: block;
	max-width: 600rpx;
}

/* 空状态提示 */
.empty-state {
	@include flex-center;
	padding: $spacing-xl;
	text-align: center;
}

.empty-text {
	font-size: $font-size-sm;
	color: $text-color-secondary;
	line-height: $line-height-base;
}

/* 消息列表 */
.message-wrapper {
	margin-bottom: $spacing-lg;
	animation: messageFadeIn $duration-base ease-out; // 0.3s，从0.4s优化
}

@keyframes messageFadeIn {
	from {
		opacity: 0;
		transform: translateY(10rpx) scale(0.98); // 从20rpx优化为10rpx
	}
	to {
		opacity: 1;
		transform: translateY(0) scale(1);
	}
}

.message {
	@include flex-center;
	gap: $spacing-sm;

	&.user {
		flex-direction: row-reverse;
	}
}

/* 头像（响应式尺寸） */
.message-avatar {
	width: $avatar-size;
	max-width: $avatar-max-size; // 76rpx
	min-width: $avatar-min-size; // 60rpx
	height: $avatar-size;
	max-height: $avatar-max-size;
	min-height: $avatar-min-size;
	border-radius: 50%;
	@include flex-center;
	background: $primary-100; // 主色100，删除蓝色渐变
	flex-shrink: 0;
	box-shadow: $box-shadow-sm;

	&.user {
		background: linear-gradient(135deg, $primary-500, $primary-800); // 保留主色渐变
		box-shadow: $box-shadow-sm;
	}
}

.avatar-icon {
	font-size: 38rpx;
}

.avatar-image {
	width: 100%;
	height: 100%;
	border-radius: 50%;
}

/* 消息气泡（响应式宽度+对称圆角） */
.message-content {
	max-width: $message-bubble-width; // 75vw
	max-width: $message-bubble-max-width; // 540rpx
	min-width: $message-bubble-min-width; // 120rpx
	padding: 16rpx 24rpx; // 水平24rpx、垂直16rpx
	background-color: $primary-100; // 主色100纯色，删除渐变
	border-radius: 24rpx; // 统一24rpx对称圆角
	box-shadow: $box-shadow-sm;
	position: relative;
	transition: $transition-base;

	&.user {
		background: $primary-500; // 主色500纯色，删除渐变
		color: $bg-color-white;
		box-shadow: $box-shadow-md;
	}

	&:not(.user) {
		background: $primary-100;
		color: $text-color-primary;
		border: 1rpx solid $primary-300;
	}

	&.typing {
		padding: $spacing-md $spacing-lg;
	}
}

.content-text {
	font-size: $font-size-base;
	line-height: 1.8;
	white-space: pre-wrap;
	word-break: break-word;
	font-weight: $font-weight-medium;
}

.message-time {
	display: none; // 默认隐藏
	font-size: $font-size-xs;
	color: rgba(0, 0, 0, 0.3);
	margin-top: 6rpx;

	.user & {
		color: rgba(255, 255, 255, 0.6);
	}
}

/* 点击气泡显示时间戳 */
.message-content:active .message-time {
	display: block;
}

/* 打字指示器（优化为2s柔和循环） */
.typing-indicator {
	@include flex-center;
	gap: 8rpx;
}

.typing-dot {
	width: 12rpx;
	height: 12rpx;
	background: $primary-500; // 主色，删除渐变
	border-radius: 50%;
	animation: typingBounce $duration-typing infinite ease-in-out; // 2s循环

	&:nth-child(1) {
		animation-delay: -0.32s;
	}

	&:nth-child(2) {
		animation-delay: -0.16s;
	}
}

@keyframes typingBounce {
	0%,
	80%,
	100% {
		transform: scale(0.6);
		opacity: 0.5;
	}
	40% {
		transform: scale(1);
		opacity: 1;
	}
}

/* ==================== 底部输入区（固定高度，防止被挤压） ==================== */
.chat-input-area {
	display: flex;
	flex-direction: column;
	background-color: $bg-color-white;
	padding: $spacing-md $spacing-lg;
	border-top: 1rpx solid $border-color-light;
	box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.04);
	flex-shrink: 0;
	min-height: 144rpx; // ✨ 最小高度：输入框96rpx + 上下padding 48rpx
	height: auto; // ✨ 自动高度，适应内容
}

/* 功能扩展位（语音输入） */
.input-extensions {
  display: flex;
	gap: $spacing-xs;
	padding: $spacing-sm;
	background: $primary-50;
	border-radius: $border-radius-base;
	transition: $transition-base;

	&:active {
		transform: scale(0.95);
		background: $primary-100;
	}
}

.extension-icon {
	font-size: $font-size-xl;
	color: $primary-500;
}

.extension-label {
	font-size: $font-size-sm;
	color: $primary-500;
	font-weight: $font-weight-medium;
}


.input-area {
  display: flex;
  gap: $spacing-md;
  margin-top: $spacing-md;
  align-items: center; // ✨ 垂直居中
  justify-content: center; // 水平居中
}
/* 输入框（占据80%宽度） */
.chat-input {
	flex: 1;
	height: $input-height-current; // 96rpx，从80rpx优化
	padding: 0 $spacing-md; // 水平24rpx
	background-color: $bg-color-input; // #f5f5f5
	border-radius: 40rpx;
	font-size: $font-size-base;
	color: $text-color-primary;
	border: 2rpx solid transparent;
	transition: $transition-base;

	&:focus {
		border-color: $primary-500;
		background-color: $bg-color-white;
	}
}

/* 圆形发送按钮（64rpx直径，主色渐变） */
.send-btn {
	width: 64rpx; // 圆形按钮
	height: 64rpx;
	@include flex-center;
	background: linear-gradient(135deg, $primary-500, $primary-800);
	color: $bg-color-white;
	border-radius: 50%;
	transition: $transition-base;
	box-shadow: $box-shadow-md;

	&:active {
		transform: scale(0.95);
		box-shadow: $box-shadow-sm;
	}
}

.send-icon {
	font-size: 28rpx;
	font-weight: $font-weight-bold;
}
</style>
