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
						<!-- 消息列表 -->
						<view
							class="message-wrapper"
							v-for="(msg, index) in displayMessages"
							:key="msg.id"
							:id="'msg-' + index"
						>
							<view class="message" :class="{ user: msg.isUser }">
								<!-- AI头像 -->
								<view class="message-avatar" v-if="!msg.isUser">
									<text class="avatar-icon">🤖</text>
								</view>

								<!-- 消息内容 -->
								<view
									class="message-content"
									:class="{ user: msg.isUser }"
								>
									<!-- AI消息：if-else 切换 -->
									<template v-if="!msg.isUser">
										<!-- 情况1：内容为空，显示加载动画 -->
										<view
											v-if="!msg.content"
											class="typing-indicator"
										>
											<view class="typing-dot"></view>
											<view class="typing-dot"></view>
											<view class="typing-dot"></view>
										</view>
											<template v-else>
												<!-- 优先显示卡片 -->
												<view
													v-if="msg.messageType && msg.cardData"
													class="card-wrapper"
												>
													<!-- 菜品列表卡片 -->
													<DishListCard
														v-if="msg.messageType === 'dish_list_card'"
														:data="msg.cardData"
														@action="handleCardAction"
													/>

													<!-- 订单列表卡片 -->
													<OrderListCard
														v-if="msg.messageType === 'order_list_card'"
														:data="msg.cardData"
														@action="handleCardAction"
													/>

													<!-- 收藏列表卡片 -->
													<FavoriteListCard
														v-if="msg.messageType === 'favorite_list_card'"
														:data="msg.cardData"
														@action="handleCardAction"
													/>

													<!-- 用户信息卡片 -->
													<UserInfoCard
														v-if="msg.messageType === 'user_info_card'"
														:data="msg.cardData"
														@action="handleCardAction"
													/>

													<!-- 健康建议卡片 -->
													<HealthCard
														v-if="msg.messageType === 'health_card'"
														:data="msg.cardData"
													/>
												</view>

												<!-- 文本内容 -->
												<text class="content-text">{{
													msg.content
												}}</text>
												<text class="message-time">{{
													msg.time
												}}</text>
											</template>
									</template>

									<!-- 用户消息：正常显示 -->
									<template v-else>
										<text class="content-text">{{
											msg.content
										}}</text>
										<text class="message-time">{{ msg.time }}</text>
									</template>
								</view>

								<!-- 用户头像 -->
								<view class="message-avatar user" v-if="msg.isUser">
									<image
										v-if="userInfo.avatar"
										class="avatar-image"
										:src="userInfo.avatar"
										mode="aspectFill"
									/>
									<text v-else class="avatar-icon">👤</text>
								</view>
							</view>
						</view>
					</scroll-view>

					<!-- 快捷提问面板 -->
					<view class="quick-questions-panel" v-if="quickQuestionsExpanded">
						<view class="quick-questions-header">
							<text class="quick-questions-title">💬 快捷提问</text>
							<view
								class="quick-questions-close"
								@click="toggleQuickQuestions"
							>
								<text class="close-icon">✕</text>
							</view>
						</view>
						<view class="quick-questions-list">
							<view
								class="quick-question-item"
								v-for="(question, index) in quickQuestions"
								:key="index"
								@click="askQuickQuestion(question)"
							>
								<text class="quick-question-text">{{ question }}</text>
								<text class="quick-question-arrow">→</text>
							</view>
						</view>
					</view>

					<view class="chat-input-area">
						<!-- 已上传图片预览 -->
						<view
							class="uploaded-images-preview"
							v-if="uploadedImages.length > 0"
						>
							<view
								class="uploaded-image-item"
								v-for="(img, index) in uploadedImages"
								:key="index"
							>
								<image
									class="uploaded-image"
									:src="img.url"
									mode="aspectFill"
								/>
								<view
									class="remove-image-btn"
									@click="removeUploadedImage(index)"
								>
									<text class="remove-icon">×</text>
								</view>
							</view>
						</view>

						<!-- 表情面板 -->
						<view class="emoji-panel" v-if="showEmojiPicker">
							<view class="emoji-grid">
								<text
									class="emoji-item"
									v-for="emoji in commonEmojis"
									:key="emoji"
									@click="selectEmoji(emoji)"
								>
									{{ emoji }}
								</text>
							</view>
						</view>

						<!-- 工具栏 -->
						<view class="toolbar-row">
							<view class="toolbar-btn" @click="toggleEmoji">
								<text class="toolbar-icon">😊</text>
							</view>
							<view class="toolbar-btn" @click="chooseImage">
								<text class="toolbar-icon">🖼️</text>
							</view>
							<view class="toolbar-btn" @click="clearHistory">
								<text class="toolbar-icon">🗑️</text>
							</view>
							<view class="toolbar-btn" @click="toggleQuickQuestions">
								<text class="toolbar-icon">💬</text>
							</view>

							<!-- AI回复状态 + 停止按钮 -->
							<view class="action-row" v-if="isStreaming">
								<view class="streaming-status">
									<text class="status-dot">●</text>
									<text class="status-text">AI正在输入...</text>
								</view>
							</view>
						</view>

						<!-- 输入行 -->
						<view class="input-row">
							<input
								class="chat-input"
								type="text"
								v-model="inputText"
								placeholder="输入您的饮食问题"
								:maxlength="500"
								@confirm="sendMessage"
								confirm-type="send"
							/>

							<!-- 发送按钮 -->
							<view
								class="send-btn"
								@click="sendMessage"
								:class="{ disabled: !inputText.trim() }"
							>
								<text
									class="stop-icon"
									v-if="isStreaming"
									@click="stopStreaming"
									>⏹️</text
								>
								<text class="send-icon" v-else>➤</text>
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
	import { parseCardDataFromContent, parseCardData, hasCardData } from "@/utils/cardParser";
import DishRecognition from "./components/DishRecognition.vue";
import RecipeOptimization from "./components/RecipeOptimization.vue";
	// 导入卡片组件
	import DishListCard from "./components/cards/DishListCard.vue";
	import OrderListCard from "./components/cards/OrderListCard.vue";
	import FavoriteListCard from "./components/cards/FavoriteListCard.vue";
	import UserInfoCard from "./components/cards/UserInfoCard.vue";
		import HealthCard from "./components/cards/HealthCard.vue";
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
	avatar: userStore.userInfo?.avatar || "", // 简化：如果有头像就用，没有就留空
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
const quickQuestionsExpanded = ref(false);

// 已上传的图片列表
const uploadedImages = ref([]);

// 是否显示表情面板
const showEmojiPicker = ref(false);

// 常用表情列表
const commonEmojis = ref([
	"😊",
	"👍",
	"❤️",
	"🎉",
	"🤔",
	"😂",
	"🙏",
	"💪",
	"👌",
	"✨",
	"🔥",
	"💯",
]);

// AbortController用于取消请求（兼容性处理）
let abortController = null;
// 检查环境是否支持 AbortController
const isAbortControllerSupported = typeof AbortController !== "undefined";

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
 * 获取用户ID（优化版，增加环境判断和数据同步）
 */
const getUserId = () => {
	// 优先从 store 中获取 userId
	if (userStore.userId) {
		// 同步到本地存储，确保数据一致性
		uni.setStorageSync("userId", userStore.userId);
		return userStore.userId;
	}

	// 其次从 userInfo 中获取
	if (userStore.userInfo?.userId) {
		const userId = userStore.userInfo.userId;
		// 同步到本地存储，确保数据一致性
		uni.setStorageSync("userId", userId);
		return userId;
	}

	// 从本地存储获取
	const localUserId = uni.getStorageSync("userId");
	if (localUserId) {
		// 同步到 store，确保数据一致性
		if (userStore.userInfo) {
			userStore.userInfo.userId = localUserId;
		}
		return localUserId;
	}

	// 判断当前环境
	const isDevelopment = process.env.NODE_ENV === "development";

	if (isDevelopment) {
		// 开发环境：允许使用默认测试用户ID
		console.warn("⚠️ 开发环境：使用默认测试用户ID '1'");
		const testUserId = "1";
		// 同步到 store 和本地存储
		uni.setStorageSync("userId", testUserId);
		if (userStore.userInfo) {
			userStore.userInfo.userId = testUserId;
		}
		return testUserId;
	} else {
		// 生产环境：禁止使用默认值，抛出错误
		console.error("❌ 生产环境：无法获取用户ID，请先登录");

		// 跳转到登录页
		uni.showToast({
			title: "请先登录",
			icon: "none",
			duration: 2000,
		});

		setTimeout(() => {
			uni.reLaunch({
				url: "/pages/login/index",
			});
		}, 2000);

		// 返回空字符串，避免使用错误的用户ID
		return "";
	}
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
	// 避免重复加载
	if (hasLoadedHistory.value) {
		console.log("⏭️ 已加载过历史记录，跳过重复加载");
		return;
	}

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
			messages.value = historyResponse.data.map((item, index) => {
				// 🔧 清理AI消息中的markdown代码块
				const cleanedContent = item.sender === "ai" && item.content
					? aiApi.cleanMarkdownCodeBlocks(item.content)
					: item.content;

				return {
					id: index + 1,
					sender: item.sender,
					content: cleanedContent,
					time: formatTime(new Date(item.createTime)),
					avatar: item.sender === "ai" ? "🤖" : "👤",
					isUser: item.sender === "user",
				};
			});
			hasLoadedHistory.value = true;
			console.log("✅ 成功加载聊天历史:", messages.value.length, "条消息");
		} else {
			// 没有历史记录，仅当消息列表为空时才添加欢迎消息
			if (messages.value.length === 0) {
				console.log("📭 没有历史记录，显示欢迎消息");
				addWelcomeMessage();
			}
			hasLoadedHistory.value = true; // 标记已尝试加载
		}

		// 滚动到底部
		await scrollToBottom();
	} catch (error) {
		console.error("❌ 加载聊天记录失败:", error);

		// 加载失败时，仅当消息列表为空时才添加欢迎消息
		if (messages.value.length === 0) {
			addWelcomeMessage();
		}
		hasLoadedHistory.value = true; // 标记已尝试加载，避免重复尝试
	}
};

/**
 * 添加欢迎消息
 */
const addWelcomeMessage = () => {
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
 * 保存消息到后端（带重试机制）
 */
const saveMessageToBackend = async (sender, content, messageType = null, cardData = null, retryCount = 0) => {
	const maxRetries = 3; // 最大重试次数

	try {
		const userId = getUserId();
		console.log(`💾 开始保存${sender}消息到后端:`, {
			userId,
			sender,
			messageType,
			hasCardData: !!cardData,
			content: content.substring(0, 50) + (content.length > 50 ? "..." : ""),
			timestamp: new Date().toISOString(),
		});

		// 构建请求数据
		const requestData = {
			userId,
			sender,
			content,
		};

		// 如果有卡片数据，添加到请求中
		if (messageType) {
			requestData.messageType = messageType;
		}
		if (cardData) {
			requestData.cardData = cardData;
		}

		const response = await aiApi.saveMessage(requestData);

		// 检查响应状态
		if (
			response &&
			(response.success === true ||
				response.code === 200 ||
				response.code === "200")
		) {
			console.log(`✅ ${sender}消息保存成功:`, {
				code: response.code,
				message: response.message,
				timestamp: new Date().toISOString(),
			});
		} else {
			// API返回成功状态码，但业务失败
			throw new Error(response?.message || "保存失败");
		}
	} catch (error) {
		console.error(`❌ 保存${sender}消息失败:`, {
			error: error.message || error,
			retryCount: `${retryCount + 1}/${maxRetries}`,
			timestamp: new Date().toISOString(),
		});

		// 重试机制
		if (retryCount < maxRetries) {
			console.log(`🔄 重试保存消息 (${retryCount + 1}/${maxRetries})...`);
			await new Promise((resolve) => setTimeout(resolve, 1000 * (retryCount + 1))); // 延迟重试
			return saveMessageToBackend(sender, content, retryCount + 1);
		} else {
			console.error(`❌ ${sender}消息保存失败，已达到最大重试次数 (${maxRetries})`);
			// 保存到本地存储作为备份
			saveChatHistoryToLocal();
		}
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

	// 🔧 中断上一个未完成的流式请求（兼容性处理）
	if (abortController) {
		console.log("🛑 中断上一个未完成的流式请求");
		if (isAbortControllerSupported) {
			abortController.abort();
		}
		abortController = null;
	}

	// 清空上一次的流式状态
	if (isStreaming.value) {
		console.log("🔄 清空上一次的流式状态");
		isTyping.value = false;
		isStreaming.value = false;
	}

	// 创建新的 AbortController（兼容性处理）
	if (isAbortControllerSupported) {
		abortController = new AbortController();
	} else {
		// 不支持的环境使用简单的标志对象
		abortController = {
			aborted: false,
			abort: function () {
				this.aborted = true;
			},
		};
	}

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

	// 立即保存用户消息到后端（不阻塞后续流程）
	saveMessageToBackend("user", text);

	// 发送消息后自动收起快捷提问，提升体验
	if (quickQuestionsExpanded.value) {
		quickQuestionsExpanded.value = false;
	}

	// 滚动到底部
	await scrollToBottom();

	// 立即创建空的AI消息对���（显示加载动画）
	const aiMessageIndex = messages.value.length;
	messages.value.push({
		id: Date.now() + 1,
		sender: "ai",
		content: "", // 初始为空，显示加载动画
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
				// 追加内容（首次收到内容时，自动从加载动画切换到显示内容）
				messages.value[aiMessageIndex].content += content;
				nextTick(() => scrollToBottom());
			},
			// onComplete - 完成回调
			async () => {
				console.log("✅ AI消息接收完成");
				isTyping.value = false;
				isStreaming.value = false;

					// 获取AI回复内容
					const aiContent = messages.value[aiMessageIndex].content;

					// 🔧 解析卡片数据（参照桌面端）
					console.log("🔍 [UniApp] 开始解析卡片数据");
					const { content: cleanContent, cardData, messageType } = parseCardDataFromContent(aiContent);

					// 更新消息对象
					messages.value[aiMessageIndex].content = cleanContent;
					messages.value[aiMessageIndex].messageType = messageType;
					messages.value[aiMessageIndex].cardData = cardData;

					console.log("✅ [UniApp] 卡片数据解析完成:", {
						messageType,
						hasCardData: !!cardData,
						contentLength: cleanContent.length
					});

					// 保存AI消息到后端（包含卡片数据）
					await saveMessageToBackend("ai", cleanContent, messageType, cardData);

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
	// 收起表情面板
	if (quickQuestionsExpanded.value) {
		showEmojiPicker.value = false;
	}
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

						// 直接添加欢迎消息，不调用loadChatHistory避免逻辑矛盾
						addWelcomeMessage();

						// 清空本地存储
						uni.removeStorageSync("chatHistory");

						// 重置加载状态
						hasLoadedHistory.value = true;

						uni.showToast({
							title: "已清空聊天记录",
							icon: "success",
						});
					} else {
						// 后端清空失败，不清空前端数据，仅给出错误提示
						uni.showToast({
							title: clearResponse.message || "清空失败，请稍后重试",
							icon: "none",
						});
					}
				} catch (error) {
					console.error("❌ 清空聊天记录失败:", error);

					// 后端清空失败，不清空前端数据，仅给出错误提示
					uni.showToast({
						title: "清空失败，请检查网络连接",
						icon: "none",
					});
				}
			}
		},
	});
};

/**
 * 切换表情面板
 */
const toggleEmoji = () => {
	showEmojiPicker.value = !showEmojiPicker.value;
	// 收起快捷提问
	if (showEmojiPicker.value) {
		quickQuestionsExpanded.value = false;
	}
};

/**
 * 选择表情
 */
const selectEmoji = (emoji) => {
	inputText.value += emoji;
	showEmojiPicker.value = false;
};

/**
 * 选择图片
 */
const chooseImage = () => {
	uni.chooseImage({
		count: 3, // 最多选择3张
		sizeType: ["compressed"],
		sourceType: ["album", "camera"],
		success: (res) => {
			const tempFilePaths = res.tempFilePaths;
			tempFilePaths.forEach((filePath) => {
				uploadedImages.value.push({
					id: Date.now() + Math.random(),
					url: filePath,
				});
			});
		},
		fail: (err) => {
			// 判断是否是用户取消，如果是则不报错
			if (err.errMsg && err.errMsg.includes("cancel")) {
				console.log("用户取消选择图片");
				return;
			}
			// 其他错误才打印
			console.error("选择图片失败:", err);
		},
	});
};

/**
 * 移除已上传的图片
 */
const removeUploadedImage = (index) => {
	uploadedImages.value.splice(index, 1);
};

/**
 * 语音输入（占位）
 */
const startVoiceInput = () => {
	uni.showToast({
		title: "语音输入功能开发中",
		icon: "none",
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
		if (isAbortControllerSupported) {
			abortController.abort();
		} else {
			// 简单标志对象的处理
			abortController.aborted = true;
		}
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

/**
 * 处理卡片操作事件
 * @param {Object} event - 操作事件对象
 */
const handleCardAction = (event) => {
	console.log('🎯 卡片操作事件:', event);
	const { type, dish, order, item } = event;

	switch (type) {
		case 'add_to_cart':
			// 加入购物车
			uni.showToast({
				title: '已加入购物车',
				icon: 'success'
			});
			break;

		case 'add_to_favorite':
		case 'remove':
			// 收藏/取消收藏
			uni.showToast({
				title: type === 'add_to_favorite' ? '已收藏' : '已取消收藏',
				icon: 'success'
			});
			break;

		case 'view_detail':
			// 查看详情
			if (dish) {
				uni.navigateTo({
					url: `/pages-user/dish/detail?id=${dish.dishId}`
				});
			} else if (order) {
				uni.navigateTo({
					url: `/pages-user/order/progress?orderId=${order.orderId}`
				});
			} else if (item) {
				uni.navigateTo({
					url: `/pages-user/dish/detail?id=${item.dishId || item.id}`
				});
			}
			break;

		case 'view_profile':
		case 'edit_profile':
			// 查看或编辑个人资料
			uni.switchTab({
				url: '/pages/user-center/index'
			});
			break;

		default:
			console.log('未知操作类型:', type);
	}
};

// 组件卸载
onUnmounted(() => {
	// 清理请求（兼容性处理）
	if (abortController) {
		if (isAbortControllerSupported) {
			abortController.abort();
		} else {
			abortController.aborted = true;
		}
	}
});
</script>

<style lang="scss">
/* 禁用页面整体原生滚动，仅允许内部scroll-view滚动 */
page {
	height: 100%;
	width: 100%;
	overflow: hidden;
	-webkit-overflow-scrolling: touch;
}
</style>

<style lang="scss" scoped>
@import "@/styles/variables.scss";
@import "@/styles/mixins.scss";

.ai-page {
	height: 100%;
	width: 100%;
	background: $bg-color-white; // 纯白背景，删除渐变
	display: flex;
	flex-direction: column;
	/* 核心：禁止外层容器溢出，确保只有内部scroll-view滚动 */
	overflow: hidden;
}

/* ==================== 统一顶部导航栏（合并标签栏+AI助手头部） ==================== */
.unified-nav {
	height: $nav-height; // 112rpx，贴合移动端规范
	background: $bg-color-white;
	border-bottom: 1rpx solid $border-color-light;
	box-shadow: $box-shadow-sm;
	@include flex-between;
	padding: 0 $spacing-md;
	/* 核心：固定在顶部，不参与滚动 */
	flex-shrink: 0;
	position: relative; /* 确保在正常文档流中 */
	z-index: $z-index-sticky; /* 确保显示在上层 */
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
	flex: 1; // 占满导航栏之外的所有剩余高度
	height: 0; /* flex布局关键：强制容器只占剩余空间，不被内容撑开 */
	display: flex;
	flex-direction: column;
	overflow: hidden; // 禁止溢出
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

.chat-container {
	display: flex;
	flex: 1;
	flex-direction: column;
	min-height: 0; // 关键：允许flex子元素正确收缩
	overflow: hidden; // 防止内容溢出
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
	/* 核心：占满输入框之外的所有剩余空间，作为唯一滚动区域 */
	flex: 1;
	height: 0; /* flex布局关键：强制容器只占剩余空间，不被内容撑开 */
	width: 100%;
	// padding: $spacing-lg $spacing-lg 280rpx $spacing-lg; // 上、右、下、左（底部留出输入框空间，增加至280rpx防止遮挡）
	background: $bg-color-light;
	scrollbar-width: none;
	-ms-overflow-style: none;

	&::-webkit-scrollbar {
		display: none;
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
	display: flex;
	align-items: flex-start; // 多行消息时头像对齐第一行顶部
	gap: $spacing-sm;
	width: 100%; // 占满宽度，方便对齐

	// AI消息：靠左
	&:not(.user) {
		justify-content: flex-start;
		max-width: 85%;
	}

	// 用户消息：靠右
	&.user {
		flex-direction: row; // 正常顺序：内容 → 头像
		justify-content: flex-end; // 整体靠右对齐
		max-width: 85%; // 限制最大宽度，防止超出边界
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

/* 消息气泡（响应式宽度+不对称圆角） */
.message-content {
	max-width: $message-bubble-width; // 75vw
	max-width: $message-bubble-max-width; // 540rpx
	min-width: $message-bubble-min-width; // 120rpx
	min-height: $message-bubble-min-height; // 80rpx，确保气泡有最小高度
	padding: 16rpx 24rpx; // 水平24rpx、垂直16rpx
	background-color: $primary-100; // 主色100纯色，删除渐变
	border-radius: 24rpx; // 默认24rpx对称圆角
	box-shadow: $box-shadow-sm;
	position: relative;
	transition: $transition-base;

	&.user {
		background: $primary-500; // 主色500纯色，删除渐变
		color: $bg-color-white;
		box-shadow: $box-shadow-md;
		border-radius: 24rpx 24rpx 8rpx 24rpx; // 用户消息：左下直角贴近头像
	}

	&:not(.user) {
		background: $primary-100;
		color: $text-color-primary;
		border: 1rpx solid $primary-300;
		border-radius: 8rpx 24rpx 24rpx 24rpx; // AI消息：左上直角贴近头像
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

/* ==================== 快捷提问面板 ==================== */
.quick-questions-panel {
	position: fixed; /* 核心：固定定位，不占用布局空间 */
	bottom: 240rpx; /* 悬浮在输入框上方，避免重叠（输入框+工具栏约200rpx + 间距） */
	left: 0;
	right: 0;
	background: rgba(255, 255, 255, 0.98);
	backdrop-filter: blur(20rpx);
	border-top: 1rpx solid $border-color-light;
	box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.08);
	padding: $spacing-md $spacing-lg;
	z-index: $z-index-sticky; /* 确保显示在聊天消息上方 */
	animation: slideUp 0.3s ease-out;
	max-height: 400rpx; /* 限制最大高度 */
	overflow-y: auto; /* 内容过多时可滚动 */
}

@keyframes slideUp {
	from {
		opacity: 0;
		transform: translateY(20rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.quick-questions-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: $spacing-md;
}

.quick-questions-title {
	font-size: $font-size-base;
	font-weight: $font-weight-bold;
	color: $text-color-primary;
}

.quick-questions-close {
	width: 48rpx;
	height: 48rpx;
	@include flex-center;
	background: $bg-color-base;
	border-radius: 50%;
	transition: $transition-base;

	&:active {
		transform: scale(0.9);
		background: $bg-color-hover;
	}
}

.close-icon {
	font-size: 32rpx;
	color: $text-color-secondary;
	font-weight: bold;
}

.quick-questions-list {
	display: flex;
	flex-direction: column;
	gap: $spacing-sm;
	max-height: 300rpx;
	overflow-y: auto;
}

.quick-question-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: $spacing-md $spacing-lg;
	background: $primary-50;
	border: 1rpx solid $primary-200;
	border-radius: $border-radius-base;
	transition: $transition-base;

	&:active {
		transform: scale(0.98);
		background: $primary-100;
		border-color: $primary-400;
	}
}

.quick-question-text {
	flex: 1;
	font-size: $font-size-base;
	color: $text-color-primary;
	line-height: $line-height-lg;
}

.quick-question-arrow {
	font-size: $font-size-xl;
	color: $primary-500;
	margin-left: $spacing-md;
}

/* ==================== 底部输入区（固定高度，防止被挤压） ==================== */
.chat-input-area {
	display: flex;
	flex-direction: column;
	background-color: $bg-color-white;
	padding: $spacing-md $spacing-lg;
	border-top: 1rpx solid $border-color-light;
	box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.04);
	/* 核心：禁止收缩，永远固定在底部 */
	flex-shrink: 0;
	width: 100%;
	min-height: 144rpx; // ✨ 最小高度：输入框96rpx + 上下padding 48rpx
	height: auto; // ✨ 自动高度，适应内容
}

/* ==================== 输入行（语音 + 输入框 + 发送按钮） ==================== */
.input-row {
	display: flex;
	align-items: center;
	gap: $spacing-md;
}

/* 语音输入按钮 */
.voice-btn {
	@include flex-center;
	gap: $spacing-xs;
	padding: $spacing-sm $spacing-md;
	background: $primary-50;
	border-radius: $border-radius-base;
	transition: $transition-base;
	flex-shrink: 0;

	&:active {
		transform: scale(0.95);
		background: $primary-100;
	}
}

.voice-icon {
	font-size: $font-size-xl;
	color: $primary-500;
}

.voice-label {
	font-size: $font-size-sm;
	color: $primary-500;
	font-weight: $font-weight-medium;
}

/* 输入框 */
.chat-input {
	flex: 1;
	max-width: 77%; /* 限制最大宽度为80%，避免太宽 */
	height: $input-height-current; // 96rpx
	padding: 0 $spacing-md;
	margin: $spacing-sm 0;
	background-color: $bg-color-input;
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

/* ==================== 操作行（AI回复状态 + 停止按钮） ==================== */
.action-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
  
	margin-top: $spacing-md;
	padding: $spacing-sm 0;
}

/* AI回复状态 */
.streaming-status {
	@include flex-center;
	gap: $spacing-xs;
}

.status-dot {
	font-size: $font-size-xs;
	color: $primary-500;
	animation: pulse 1.5s infinite;
}

@keyframes pulse {
	0%,
	100% {
		opacity: 1;
	}
	50% {
		opacity: 0.3;
	}
}

.status-text {
	font-size: $font-size-sm;
	color: $text-color-secondary;
}

/* 停止按钮 */
.stop-btn {
	@include flex-center;
	gap: $spacing-xs;
	padding: $spacing-xs $spacing-md;
	background: rgba(255, 82, 82, 0.1);
	border-radius: $border-radius-round;
	transition: $transition-base;

	&:active {
		transform: scale(0.95);
		background: rgba(255, 82, 82, 0.2);
	}
}

.stop-icon {
	font-size: $font-size-base;
	color: #ff5252;
}

.stop-label {
	font-size: $font-size-sm;
	color: #ff5252;
	font-weight: $font-weight-medium;
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

/* 发送按钮禁用状态 */
.send-btn.disabled {
	opacity: 0.5;
	background: $bg-color-base;
}

/* ==================== 工具栏 ==================== */
.toolbar-row {
	display: flex;
	align-items: center;
	gap: $spacing-md;
	padding: $spacing-sm $spacing-lg;
	border-bottom: 1rpx solid $border-color-light;
}

.toolbar-btn {
	width: 48rpx;
	height: 48rpx;
	@include flex-center;
	background: $bg-color-base;
	border-radius: $border-radius-base;
	transition: $transition-base;

	&:active {
		transform: scale(0.95);
		background: $primary-100;
	}
}

.toolbar-icon {
	font-size: 32rpx;
}

.toolbar-spacer {
	flex: 1;
}

/* ==================== 表情面板 ==================== */
.emoji-panel {
	padding: $spacing-md $spacing-lg;
	background: $bg-color-white;
	border-top: 1rpx solid $border-color-light;
	max-height: 300rpx;
	overflow-y: auto;
}

.emoji-grid {
	display: flex;
	flex-wrap: wrap;
	gap: $spacing-sm;
}

.emoji-item {
	font-size: 48rpx;
	padding: $spacing-xs;
	transition: $transition-base;

	&:active {
		transform: scale(1.2);
	}
}

/* ==================== 图片预览 ==================== */
.uploaded-images-preview {
	display: flex;
	flex-wrap: wrap;
	gap: $spacing-md;
	padding: $spacing-md $spacing-lg;
	background: $bg-color-white;
}

.uploaded-image-item {
	position: relative;
	width: 160rpx;
	height: 160rpx;
	border-radius: $border-radius-base;
	overflow: hidden;
	box-shadow: $box-shadow-sm;
}

.uploaded-image {
	width: 100%;
	height: 100%;
}

.remove-image-btn {
	position: absolute;
	top: $spacing-xs;
	right: $spacing-xs;
	width: 40rpx;
	height: 40rpx;
	@include flex-center;
	background: rgba(0, 0, 0, 0.6);
	border-radius: 50%;
	transition: $transition-base;

	&:active {
		transform: scale(0.9);
		background: rgba(0, 0, 0, 0.8);
	}
}

.remove-icon {
	color: $bg-color-white;
	font-size: 32rpx;
	font-weight: bold;
	line-height: 1;
}
</style>
