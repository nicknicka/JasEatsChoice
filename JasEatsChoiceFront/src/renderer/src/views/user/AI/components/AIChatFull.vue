<template>
	<div class="chat-content-wrapper">
		<!-- 聊天消息区域 -->
		<div class="chat-messages" ref="chatContainerRef">
			<!-- 初始加载时的打字机等待效果 -->
			<transition name="fade-in">
				<div v-if="isInitialLoading" class="initial-loading-container">
					<div class="ai-avatar-loading">
						<div class="avatar-emoji">🤖</div>
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
					<!-- 用户头像：使用真实头像 -->
					<CommonAvatar
						v-if="message.sender === 'user'"
						:avatar-url="message.avatar"
						:size="42"
						:fallback-text="userStore.userInfo?.nickname || '用'"
						class="message-avatar-custom"
					/>
					<!-- AI头像：使用emoji -->
					<div v-else class="message-avatar">{{ message.avatar }}</div>
					<div class="message-content">
						<!-- 图片附件 -->
						<div
							v-if="message.images && message.images.length > 0"
							class="message-images"
						>
							<div
								v-for="(img, idx) in message.images"
								:key="idx"
								class="message-image"
							>
								<img :src="img.url" :alt="`图片${idx + 1}`" />
							</div>
						</div>

						<!-- 消息文本（支持Markdown或纯文本） -->
						<div class="message-text" v-show="shouldShowMessage(message)">
							<!-- 进度指示器（AI消息专属） -->
							<div
								v-if="message.sender === 'ai' && getMessageProgressStatus(message)"
								class="message-progress-indicator"
								:class="getMessageProgressClass(message)"
							>
								<div class="progress-content">
									<!-- 图标 -->
									<div class="progress-icon">
										<component :is="getProgressIcon(message)" />
									</div>
									<!-- 文本 -->
									<div class="progress-text">
										{{ getProgressText(message) }}
									</div>
									<!-- 动画点 -->
									<div v-if="getProgressDots(message)" class="progress-dots">
										<span class="progress-dot"></span>
										<span class="progress-dot"></span>
										<span class="progress-dot"></span>
									</div>
								</div>
							</div>

							<!-- 卡片消息渲染 -->
							<div
								v-if="shouldShowCard(message)"
								class="card-message-wrapper"
							>
								<component
									:is="getCardComponent(message.messageType)"
									:data="parseCardData(message.cardData)"
									@action="handleCardAction"
								/>
							</div>

							<!-- 文本消息渲染（纯文本，无卡片） -->
							<div
								v-else-if="shouldShowTextContent(message)"
								class="message-text-content"
								:class="{
									'markdown-content': message.enableMarkdown,
									typing: message.isTyping,
								}"
								v-html="
									renderContent(
										getDisplayContent(message),
										message.enableMarkdown
									)
								"
							></div>

							<!-- 卡片总结文本（有卡片时的AI总结） -->
							<div
								v-else-if="shouldShowCardSummary(message)"
								class="card-summary-text"
								:class="{
									'markdown-content': message.enableMarkdown,
									typing: message.isTyping,
								}"
								v-html="
									renderContent(
										getDisplayContent(message),
										message.enableMarkdown
									)
								"
							></div>

							<!-- 更多操作按钮（仅文本消息显示） -->
							<el-dropdown
								trigger="click"
								@command="
									(cmd) =>
										handleMessageAction(
											cmd,
											getDisplayContent(message)
										)
								"
								v-if="shouldShowMoreButton(message)"
							>
								<span class="more-btn">
									<el-icon :size="12">
										<More />
									</el-icon>
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
					<div
						v-for="img in uploadedImages"
						:key="img.id"
						class="uploaded-image-item"
					>
						<img :src="img.url" alt="上传的图片" />
						<el-button
							:icon="Delete"
							circle
							size="small"
							class="remove-image-btn"
							@click="removeUploadedImage(img.id)"
						/>
					</div>
				</div>
			</transition>

			<!-- 表情面板 -->
			<transition name="slide-up">
				<div v-if="showEmojiPicker" class="emoji-panel">
					<div class="emoji-grid">
						<span
							v-for="emoji in commonEmojis"
							:key="emoji"
							class="emoji-item"
							@click="selectEmoji(emoji)"
							:title="emoji"
						>
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
							<!-- 表情按钮 -->
							<el-tooltip content="表情" placement="top">
								<el-button
									:icon="Operation"
									circle
									size="small"
									@click="toggleEmoji"
									:class="{ 'is-active': showEmojiPicker }"
								/>
							</el-tooltip>

							<!-- 图片上传按钮 -->
							<input
								type="file"
								accept="image/*"
								@change="handleImageUpload"
								class="hidden-file-input"
								ref="imageInputRef"
							/>
							<el-tooltip content="上传图片" placement="top">
								<el-button
									:icon="Picture"
									circle
									size="small"
									@click="triggerImageUpload"
								/>
							</el-tooltip>

							<!-- 清空对话按钮 -->
							<el-tooltip content="清空对话" placement="top">
								<el-button
									:icon="Delete"
									circle
									size="small"
									@click="clearChat"
								/>
							</el-tooltip>

							<!-- 快捷提问按钮 -->
							<div class="quick-question-button-wrapper">
								<!-- 快捷提问面板 -->
								<transition name="fade-slide">
									<div
										v-if="showQuickQuestions"
										class="quick-questions-panel-fixed"
									>
										<div class="quick-questions-title">
											<span>快速提问</span>
											<el-icon
												:size="14"
												class="close-panel-icon"
												@click="showQuickQuestions = false"
											>
												<Close />
											</el-icon>
										</div>
										<div class="quick-questions-categories">
											<div
												v-for="(
													category, categoryIndex
												) in quickQuestionCategories"
												:key="categoryIndex"
												class="question-category"
											>
												<div
													class="category-header"
													@click="toggleCategory(categoryIndex)"
													:class="{
														'is-active':
															expandedCategory ===
															categoryIndex,
													}"
												>
													<span>{{ category.title }}</span>
													<el-icon
														:size="12"
														class="category-arrow"
														:class="{
															'is-expanded':
																expandedCategory ===
																categoryIndex,
														}"
													>
														<ArrowRight />
													</el-icon>
												</div>
												<transition name="slide-right-sub">
													<div
														v-show="
															expandedCategory ===
															categoryIndex
														"
														class="category-questions"
													>
														<div
															v-for="(
																question, qIndex
															) in category.questions"
															:key="qIndex"
															class="question-item"
															@click="
																handleQuickQuestion(
																	question
																)
															"
														>
															{{ question }}
														</div>
													</div>
												</transition>
											</div>
										</div>
									</div>
								</transition>

								<el-tooltip content="快捷提问" placement="top">
									<el-button
										:icon="QuestionFilled"
										circle
										size="small"
										@click="toggleQuickQuestions"
										:class="{ 'is-active': showQuickQuestions }"
									/>
								</el-tooltip>
							</div>

							<!-- AI个性化数据开关 -->
							<el-tooltip
								content="开启后AI将使用您的个人数据提供个性化建议"
								placement="bottom"
							>
								<el-switch
									v-model="aiPersonalDataEnabled"
									@change="handlePersonalDataToggle"
									size="small"
								/>
							</el-tooltip>
						</div>

						<div class="toolbar-right"></div>
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
						<!-- 字数统计 -->
						<div class="char-count-wrapper">
							<span
								class="char-count"
								:class="{
									'near-limit':
										inputMessage.length >= 450 &&
										inputMessage.length < 500,
									'at-limit': inputMessage.length >= 500,
								}"
							>
								{{ inputMessage.length }}/500
							</span>
						</div>
					</div>
				</div>

				<el-button
					:type="isStreaming ? 'danger' : 'primary'"
					class="send-btn"
					@click="handleSendClick"
					:disabled="isLoading && !isStreaming"
					:loading="isLoading && !isStreaming"
				>
					{{ isStreaming ? "停止" : "发送" }}
				</el-button>
			</div>
		</div>
	</div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted, watch, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
	ChatDotRound,
	Close,
	Delete,
	Picture,
	DocumentCopy,
	More,
	Operation,
	QuestionFilled,
	ArrowRight,
	Search,
	User,
	Apple,
	Document,
	Shop,
	Clock,
	Location,
	Tools,
	CircleCheck,
	Loading,
} from "@element-plus/icons-vue";
import { parseMarkdown } from "../../../../utils/markdownParser";
import axios from "axios";
import { API_CONFIG } from "../../../../config/index";
import {
	initializeQuickQuestions,
	DEFAULT_EXPANDED_CATEGORY,
} from "../../../../config/quickQuestions";
import { useAuthStore } from "../../../../store/authStore";
import { useUserStore } from "../../../../store/userStore";
import CommonAvatar from "@/components/CommonAvatar.vue";
import { isCardMessage, renderCard } from "../utils/cardMapper";
import cardActionService from "../utils/cardActionService";
import { useRouter } from "vue-router";
import DOMPurify from "dompurify";

// 获取认证store
const authStore = useAuthStore();
// 获取用户store
const userStore = useUserStore();
// 获取路由
const router = useRouter();

// 获取用户ID
const getUserId = () => {
	return String(authStore.userId);
};

// ========== 计算属性优化 ==========

// 是否为开发环境
const isDevelopment = computed(() => import.meta.env.MODE === "development");

// 消息样式类计算
const getMessageClasses = (message) => ({
	"chat-message": true,
	"user-message": message.sender === "user",
	"ai-message": message.sender === "ai",
});

// 是否显示卡片消息
const shouldShowCard = (message) => {
	return message.messageType && isCardMessage(message.messageType);
};

// 是否显示纯文本内容
const shouldShowTextContent = (message) => {
	const displayContent = getDisplayContent(message);
	const hasCard = shouldShowCard(message);
	return displayContent && !hasCard;
};

// 是否显示卡片总结文本
const shouldShowCardSummary = (message) => {
	const displayContent = getDisplayContent(message);
	const hasCard = shouldShowCard(message);
	return displayContent && hasCard;
};

// 是否显示更多操作按钮
const shouldShowMoreButton = (message) => {
	return !message.messageType || !isCardMessage(message.messageType);
};

// ========== 统一进度指示器相关 ==========

// 获取消息进度状态
const getMessageProgressStatus = (message) => {
	// 完成状态时不显示进度指示器（使用completed字段）
	if (message.completed) return null;

	// 初始思考状态
	if (message.isThinking) return "thinking";

	// 直接使用 progress 字段判断（后端发送的进度消息）
	if (message.progress === true) return "executing";

	return null;
};

// 获取进度指示器的样式类
const getMessageProgressClass = (message) => {
	const status = getMessageProgressStatus(message);
	return {
		"status-thinking": status === "thinking",
		"status-executing": status === "executing",
		"status-completed": status === "completed",
	};
};

// 获取进度指示器的文本
const getProgressText = (message) => {
	// 直接返回后端传来的进度文本
	if (message.content && message.content.trim()) {
		return message.content.trim();
	}

	// 默认文本（降级处理）
	return "AI正在处理中...";
};

// 是否显示进度动画点
const getProgressDots = (message) => {
	const status = getMessageProgressStatus(message);
	// 只要显示进度指示器，就显示动画点
	return status === "thinking" || status === "executing";
};

// 获取进度指示器的图标
const getProgressIcon = (message) => {
	const text = message.content || "";

	// 根据文本内容返回对应的图标
	if (text.includes("搜索菜品") || text.includes("菜品搜索")) return Search;
	if (text.includes("分析偏好") || text.includes("偏好分析")) return User;
	if (text.includes("营养") || text.includes("分析营养成分")) return Apple;
	if (text.includes("订单") || text.includes("处理订单")) return Document;
	if (text.includes("商家") || text.includes("查询商家")) return Shop;
	if (text.includes("时段") || text.includes("分析时段")) return Clock;
	if (text.includes("位置") || text.includes("查询位置")) return Location;
	if (text.includes("查询数据") || text.includes("数据查询")) return Tools;
	if (text.includes("分析需求") || text.includes("需求分析")) return ChatDotRound;
	if (text.includes("完成")) return CircleCheck;

	// 默认加载图标
	return Loading;
};

// 状态
const messages = ref([]);
const isMounted = ref(false); // 添加组件挂载状态标记，初始为 false
const inputMessage = ref("");
const isLoading = ref(false);
const isStreaming = ref(false);
const isInitialLoading = ref(true); // 初始加载状态，显示等待效果
const abortController = ref(null);
const chatContainerRef = ref(null);
const bottomContainerRef = ref(null);
const showQuickQuestions = ref(false);
const showEmojiPicker = ref(false);
const uploadedImages = ref([]);

// 展开的分类索引（只能有一个分类展开）
const expandedCategory = ref(null); // 默认不展开任何分类

// 用户手动滚动标记
const userHasScrolled = ref(false);
let isAutoScrolling = false; // 防止滚动时触发滚动事件
let isUserScrollingUp = false; // 标记用户是否主动向上滚动
let lastScrollTop = 0; // 记录上一次的滚动位置，用于判断滚动方向

// 导入卡片组件
import OrderListCard from "./cards/OrderListCard.vue";
import FavoriteListCard from "./cards/FavoriteListCard.vue";
import ReviewListCard from "./cards/ReviewListCard.vue";
import CouponListCard from "./cards/CouponListCard.vue";
import UserInfoCard from "./cards/UserInfoCard.vue";
import DishListCard from "./cards/DishListCard.vue";
// NotificationListCard 已移除 - 通知类型以纯文本形式显示
import ErrorCard from "./cards/ErrorCard.vue";
import OrderGuideCard from "./cards/OrderGuideCard.vue";

const cardComponents = {
	order_list_card: OrderListCard,
	favorite_list_card: FavoriteListCard,
	review_list_card: ReviewListCard,
	coupon_list_card: CouponListCard,
	user_info_card: UserInfoCard,
	dish_list_card: DishListCard,
	// notification_list_card 已移除 - 通知类型以纯文本形式显示
	error_card: ErrorCard,
	order_guide_card: OrderGuideCard,
};

// 获取卡片组件
const getCardComponent = (messageType) => {
	return cardComponents[messageType];
};

// 解析卡片数据（处理JSON字符串）
const parseCardData = (cardData) => {
	if (!cardData) return null;

	// 如果已经是对象，直接返回
	if (typeof cardData === "object") {
		return cardData;
	}

	// 如果是字符串，解析JSON
	if (typeof cardData === "string") {
		try {
			return JSON.parse(cardData);
		} catch (error) {
			console.error("解析卡片数据失败:", error);
			return null;
		}
	}

	return null;
};

// 记录最后发送的结构化查询类型（用于刷新卡片）
const lastQueryType = ref(null);
const lastQueryMessageIndex = ref(-1);

// AI个性化数据开关状态（隐私保护原则：默认未授权）
const aiPersonalDataEnabled = ref(false);

// 快捷问题分类列表（与后端Function Calling功能对应）
// 从配置文件初始化，默认展开第一个分类
const quickQuestionCategories = ref(initializeQuickQuestions(DEFAULT_EXPANDED_CATEGORY));

// 切换分类展开/折叠状态（同时只能展开一个分类）
const toggleCategory = (categoryIndex) => {
	if (expandedCategory.value === categoryIndex) {
		// 已展开，则折叠
		expandedCategory.value = null;
	} else {
		// 未展开或展开了其他分类，则展开当前分类
		expandedCategory.value = categoryIndex;
	}
};

// 常用表情列表
const commonEmojis = ref([
	"😊",
	"😂",
	"🤔",
	"👍",
	"👎",
	"❤️",
	"🔥",
	"✨",
	"🍎",
	"🥗",
	"🍲",
	"🍜",
	"🍕",
	"🍰",
	"☕",
	"🥤",
	"💪",
	"🏃",
	"🧘",
	"😋",
	"🤤",
	"😌",
	"🤗",
	"😎",
]);

// 加载用户偏好设置
const loadUserPreference = async () => {
	try {
		const userId = getUserId();
		console.log("📥 加载用户偏好设置，userId:", userId);

		const response = await axios.get(
			`${API_CONFIG.baseURL}/v1/users/${userId}/preferences`
		);

		if (response.data && response.data.data) {
			// 只有明确设置为 true 时才启用（隐私保护原则）
			aiPersonalDataEnabled.value =
				response.data.data.enableAiPersonalData === true;
			console.log("✅ 用户偏好加载成功:", aiPersonalDataEnabled.value);
		}
	} catch (error) {
		console.error("❌ 加载用户偏好失败:", error);
		// 失败时使用默认值（隐私保护原则：默认未授权）
		aiPersonalDataEnabled.value = false;
	}
};

// 从后端加载聊天记录
const loadMessages = async () => {
	try {
		const userId = getUserId();
		console.log("📥 开始加载聊天记录，userId:", userId);

		const response = await axios.get(API_CONFIG.baseURL + API_CONFIG.ai.history, {
			params: { userId },
		});

		console.log("📡 后端响应:", response.data);
		console.log("📊 响应数据:", response.data.data);
		console.log("📏 数据长度:", response.data.data ? response.data.data.length : 0);

		if (
			response.data.code === 200 &&
			response.data.data &&
			response.data.data.length > 0
		) {
			// 将后端数据转换为前端格式
			const historyData = response.data.data;
			messages.value = historyData.map((item, index) => {
				// 解析content中的卡片数据标记
				const { content: cleanContent, cardData, messageType } = parseCardDataFromContent(item.content);

				return {
					id: index + 1,
					sender: item.sender, // 'user' 或 'ai'
					content: cleanContent,
					displayContent: cleanContent || "", // 显示内容
					time: new Date(item.createTime).toLocaleTimeString([], {
						hour: "2-digit",
						minute: "2-digit",
					}),
					avatar: item.sender === "ai" ? "🤖" : userStore.userInfo?.avatar || "",
					enableMarkdown: true,
					messageType: messageType || item.messageType || null, // 优先使用解析出的类型
					cardData: cardData || item.cardData || null, // 优先使用解析出的数据
					isToolExecuting: false,
					toolCompleted: false,
					hasToolPrompt: false,
					progress: false, // 历史消息默认不是进度消息
				};
			});

			// 检查是否需要重新加载卡片数据（向后兼容旧消息）
			await restoreCardDataForMessages();

			console.log("✅ 成功加载聊天历史:", messages.value.length, "条消息");

			// 卡片数据恢复后，延时滚动确保DOM完全渲染
			setTimeout(() => {
				if (isMounted.value && chatContainerRef.value) {
					scrollToBottom(true);
				}
			}, 200); // 延时 200ms
		} else {
			// 没有历史记录，调用后端获取欢迎消息
			console.log("📭 没有历史记录，从后端获取欢迎消息");

			try {
				const userId = getUserId();
				const clearResponse = await axios.delete(
					`${API_CONFIG.baseURL}/v1/agent/context/${userId}`
				);

				console.log("📡 后端欢迎消息响应:", clearResponse.data);

				if (clearResponse.data.success === true) {
					const welcomeMessage =
						clearResponse.data.data?.welcomeMessage || WELCOME_MESSAGE;

					messages.value = [
						{
							id: 1,
							sender: "ai",
							content: welcomeMessage,
							time: new Date().toLocaleTimeString([], {
								hour: "2-digit",
								minute: "2-digit",
							}),
							avatar: "🤖",
							enableMarkdown: true,
						},
					];

					console.log("✅ 已显示Agent欢迎消息");
				} else {
					// 降级：使用硬编码欢迎消息
					console.warn("⚠️ 后端返回失败，使用硬编码欢迎消息");
					messages.value = [
						{
							id: 1,
							sender: "ai",
							content: WELCOME_MESSAGE,
							time: new Date().toLocaleTimeString([], {
								hour: "2-digit",
								minute: "2-digit",
							}),
							avatar: "🤖",
							enableMarkdown: true,
						},
					];
				}
			} catch (error) {
				console.error("❌ 获取欢迎消息失败，使用硬编码欢迎消息:", error);
				messages.value = [
					{
						id: 1,
						sender: "ai",
						content: WELCOME_MESSAGE,
						time: new Date().toLocaleTimeString([], {
							hour: "2-digit",
							minute: "2-digit",
						}),
						avatar: "🤖",
						enableMarkdown: true,
					},
				];
			}
		}
		isLoading.value = false;
		isInitialLoading.value = false; // 隐藏初始加载效果
	} catch (error) {
		console.error("❌ 加载聊天记录失败:", error);
		console.error("❌ 错误详情:", error.response?.data || error.message);
		// 加载失败时，显示欢迎消息
		messages.value = [
			{
				id: 1,
				sender: "ai",
				content:
					"您好！我是您的AI饮食助手。😊\n\n我可以帮助您：\n\n- 推荐健康食谱\n- 分析营养成分\n- 提供饮食建议\n\n有什么可以帮您的吗？",
				time: new Date().toLocaleTimeString([], {
					hour: "2-digit",
					minute: "2-digit",
				}),
				avatar: "🤖",
				enableMarkdown: true,
			},
		];
		isLoading.value = false;
		isInitialLoading.value = false; // 隐藏初始加载效果
		// 延时滚动，确保 DOM 完全渲染
		setTimeout(() => {
			scrollToBottom(true);
		}, 100);
	}
};

// 保存消息到后端
const saveMessageToBackend = async (
	sender,
	content,
	messageType = null,
	cardData = null
) => {
	try {
		const userId = getUserId();

		const payload = {
			userId,
			sender, // 'user' 或 'ai'
			content,
		};

		// 如果有卡片数据，也保存
		if (messageType && cardData) {
			payload.messageType = messageType;
			payload.cardData = cardData;
		}

		await axios.post(API_CONFIG.baseURL + API_CONFIG.ai.save, payload, {
			headers: {
				Authorization: `Bearer ${authStore.token}`,
			},
		});
		console.log(
			"✅ 消息已保存到后端:",
			sender,
			messageType ? `(卡片: ${messageType})` : ""
		);
	} catch (error) {
		console.error("❌ 保存消息到后端失败:", error);
	}
};

// 渲染内容（支持Markdown或纯文本）并添加XSS防护
const renderContent = (content, useMarkdown) => {
	let renderedContent;
	if (useMarkdown) {
		renderedContent = parseMarkdown(content);
	} else {
		renderedContent = content.replace(/\n/g, "<br>");
	}

	// 使用DOMPurify清理HTML，防止XSS攻击
	return DOMPurify.sanitize(renderedContent, {
		ALLOWED_TAGS: [
			"p",
			"br",
			"strong",
			"em",
			"u",
			"a",
			"code",
			"pre",
			"h1",
			"h2",
			"h3",
			"ul",
			"ol",
			"li",
			"blockquote",
			"table",
			"thead",
			"tbody",
			"tr",
			"th",
			"td",
		],
		ALLOWED_ATTR: ["href", "class", "target"],
		ALLOW_DATA_ATTR: false,
	});
};

// ========== 辅助函数：消息统计日志 ==========
const logMessageStats = (message) => {
	if (!message) return;
	console.log("✅ AI消息接收完成");
	console.log("📊 消息统计:");
	console.log(`   - 总字符数: ${message.content?.length || 0}`);
	console.log(`   - 消息类型: ${message.messageType || "纯文本"}`);
	console.log(`   - 包含卡片: ${message.cardData ? "是" : "否"}`);
	console.log(`   - 换行符数量: ${(message.content?.match(/\n/g) || []).length}`);
	console.log(`   - 完整内容（原始）:`);
	console.log("─".repeat(60));
	console.log(message.content.replace(/\n/g, "↵\n"));
	console.log("─".repeat(60));
};

// ========== 辅助函数：验证并保存消息 ==========
const validateAndSaveMessage = async (messageIndex) => {
	if (!isMounted.value) return false;

	const message = messages.value[messageIndex];
	if (!message || !message.content) {
		console.warn("⚠️ 消息对象不存在或无内容");
		return false;
	}

	// 跳过进度消息
	if (message._isProgressMessage) {
		console.log("⏭️ 跳过进度消息保存");
		return true;
	}

	try {
		await saveMessageToBackend(
			"ai",
			message.content,
			message.messageType,
			message.cardData
		);
		return true;
	} catch (error) {
		console.warn("⚠️ 保存消息到后端失败:", error.message);
		return false;
	}
};

// ========== 辅助函数：获取消息对象（带验证）==========
const getMessage = (messageIndex) => {
	if (!isMounted.value || !messages.value[messageIndex]) {
		return null;
	}
	return messages.value[messageIndex];
};

// ========== 辅助函数：解析content中的卡片数据 ==========

/**
 * 将后端返回的卡片类型转换为前端支持的卡片类型
 * @param {string} cardType - 后端卡片类型
 * @returns {string} 前端消息类型
 */
const convertToSupportedCardType = (cardType) => {
	// 类型映射表
	const typeMapping = {
		// 食品推荐相关
		'foodrecommendationcard': 'dish_list_card',
		'food_recommendation_card': 'dish_list_card',
		'dish': 'dish_list_card',

		// 订单相关
		'order': 'order_list_card',
		'order_card': 'order_list_card',

		// 收藏相关
		'favorite': 'favorite_list_card',
		'favorite_card': 'favorite_list_card',

		// 商家相关
		'merchant': 'notification_list_card', // 暂时映射到通知卡片

		// 评价相关
		'review': 'review_list_card',
		'review_card': 'review_list_card',

		// 优惠券相关
		'coupon': 'coupon_list_card',
		'coupon_card': 'coupon_list_card',

		// 用户信息相关
		'user': 'user_info_card',
		'user_info': 'user_info_card',
		'user_info_card': 'user_info_card',

		// 营养/健康相关
		'health': 'notification_list_card', // 暂时映射到通知卡片
		'health_card': 'notification_list_card',
		'nutrition': 'notification_list_card',
	};

	// 如果已经在映射表中，直接返回
	if (typeMapping[cardType]) {
		return typeMapping[cardType];
	}

	// 如果前端已经支持这个类型，直接返回
	if (cardType && typeof cardType === 'string') {
		// 检查是否是前端支持的类型（通过检查 cardMapper）
		const supportedTypes = [
			'order_list_card',
			'favorite_list_card',
			'review_list_card',
			'coupon_list_card',
			'user_info_card',
			'dish_list_card',
			'notification_list_card',
			'error_card'
		];
		if (supportedTypes.includes(cardType)) {
			return cardType;
		}
	}

	// 默认返回 null（不显示卡片）
	console.warn("⚠️ [类型转换] 未知的卡片类型:", cardType);
	return null;
};
/**
 * 从消息内容中提取卡片数据和类型
 * @param {string} content - 原始消息内容
 * @returns {object} { content: 纯文本内容, cardData: 卡片数据, messageType: 消息类型 }
 */
const parseCardDataFromContent = (content) => {
	if (!content || typeof content !== 'string') {
		return { content, cardData: null, messageType: null };
	}

	// 检查是否包含卡片数据标记
	if (!content.includes("[CARD_DATA_START]") || !content.includes("[CARD_DATA_END]")) {
		return { content, cardData: null, messageType: null };
	}

	console.log("🔍 [解析] 检测到卡片数据标记");

	// 提取卡片数据
	const cardDataStart = content.indexOf("[CARD_DATA_START]");
	const cardDataEnd = content.indexOf("[CARD_DATA_END]");

	if (cardDataStart === -1 || cardDataEnd === -1) {
		console.warn("⚠️ [解析] 卡片数据标记不完整");
		return { content, cardData: null, messageType: null };
	}

	const cardDataString = content.substring(
		cardDataStart + "[CARD_DATA_START]".length,
		cardDataEnd
	).trim();

	// 移除卡片数据标记，只保留文本内容
	const cleanContent = content.substring(0, cardDataStart).trim();

	let cardData = null;
	let messageType = null;

	try {
		// 解析卡片数据
		let parsedData = JSON.parse(cardDataString);

		// 兼容两种格式：
		// 1. 数组格式：[{ type: "...", recommendations: [...] }]
		// 2. 对象格式：{ type: "...", recommendations: [...] }
		let cardDataArray = Array.isArray(parsedData) ? parsedData : [parsedData];

		if (cardDataArray.length > 0) {
			const firstCard = cardDataArray[0];

			// 提取卡片类型（支持两种字段名：type 和 cardType）
			const cardType = firstCard.type || firstCard.cardType;
			if (cardType) {
				// 转换为前端支持的格式
				messageType = convertToSupportedCardType(cardType);
				console.log("✅ [解析] 原始卡片类型:", cardType);
				console.log("✅ [解析] 转换后的消息类型:", messageType);
			}

			// 提取卡片数据（根据不同类型提取不同字段）
			if (firstCard.recommendations) {
				cardData = { recommendations: firstCard.recommendations };
			} else if (firstCard.orders) {
				cardData = { orders: firstCard.orders };
			} else if (firstCard.dishes) {
				cardData = { dishes: firstCard.dishes };
			} else if (cardType === 'dish') {
				// 菜品卡片：整个数组就是菜品列表，需要字段映射
				const mappedDishes = cardDataArray.map(dish => ({
					dishId: dish.dishId || dish.id,
					dishName: dish.dishName || dish.title,
					imageUrl: dish.imageUrl || dish.image,
					description: dish.description || dish.highlight,
					price: dish.price,
					rating: dish.rating,
					category: dish.category,
					tags: dish.tags || [],
					actions: dish.actions || []
				}));
				cardData = { dishes: mappedDishes };
				console.log("✅ [解析] 菜品数组已映射并包装");
			} else {
				// 其他情况，直接使用整个对象
				cardData = firstCard;
			}

			console.log("✅ [解析] 卡片数据:", cardData);
		}
	} catch (error) {
		console.warn("⚠️ [解析] 解析卡片数据失败:", error.message);
	}

	return { content: cleanContent, cardData, messageType };
};

// ========== 辅助函数：UI更新 ==========
const updateUI = async () => {
	if (isMounted.value) {
		await nextTick();
		scrollToBottom();
	}
};

// ========== 辅助函数：处理最终结果 ==========
const handleFinalResult = async (messageIndex, parsedData) => {
	console.log("🏁 [最终结果] 处理完成，准备启动打字机效果");

	const message = getMessage(messageIndex);
	if (!message) return;

	// 跳过进度消息
	if (message._isProgressMessage) {
		console.log("⏭️ 跳过进度消息保存");
		return;
	}

	// 统计信息
	console.log("✅ AI消息接收完成（最终结果）");
	console.log("📊 消息统计:");
	console.log(`   - 总字符数: ${message.content?.length || 0}`);
	console.log(`   - 消息类型: ${message.messageType || "纯文本"}`);
	console.log(`   - 包含卡片: ${message.cardData ? "是" : "否"}`);

	// 打字机效果（仅纯文本消息）
	if (!message.messageType && message.displayContent) {
		const fullText = message.displayContent;
		console.log("⌨️ 启动打字机效果，字符数:", fullText.length);
		message.displayContent = "";
		await startTypewriterEffect(messageIndex, fullText, 20);
		console.log("✅ 打字机效果完成");
	}

	// 保存到后端
	try {
		await saveMessageToBackend(
			"ai",
			message.content,
			message.messageType,
			message.cardData
		);
		console.log("✅ 最终结果已保存到后端");
	} catch (error) {
		console.warn("⚠️ 保存消息到后端失败:", error.message);
	}
};

// ========== 流式传输：逐块读取AI回复 ==========
const streamResponse = async (messageIndex, reader) => {
	isStreaming.value = true;

	// 验证 messageIndex 和消息对象
	if (!messages.value[messageIndex]) {
		console.error("❌ 消息索引无效:", messageIndex);
		isStreaming.value = false;
		return;
	}

	// 确保消息对象有必要的属性
	const msg = messages.value[messageIndex];
	msg.content ??= "";
	msg.displayContent ??= "";

	const decoder = new TextDecoder();
	let buffer = "";
	let cardDataBuffer = "";  // 卡片数据缓冲区
	let isCollectingCardData = false;  // 是否正在收集卡片数据

	try {
		while (true) {
			// 组件挂载状态检查
			if (!isMounted.value) {
				console.log("ℹ️ 组件已卸载，停止流式处理");
				break;
			}

			const { done, value } = await reader.read();
			if (done) break;

			const chunk = decoder.decode(value, { stream: true });
			buffer += chunk;
			const lines = buffer.split("\n");
			buffer = lines.pop() || "";

			let currentEvent = "message";

			for (const line of lines) {
				const trimmedLine = line.trim();

				// 处理事件名称
				if (trimmedLine.startsWith("event:")) {
					currentEvent = trimmedLine.substring(6).trim();
					continue;
				}

				// 处理 end/error 事件：完成流式传输
				if (currentEvent === "end" || currentEvent === "error") {
					const message = getMessage(messageIndex);
					if (message) {
						logMessageStats(message);
						await validateAndSaveMessage(messageIndex);
					}
					return;
				}

				// 只处理 message 事件
				if (currentEvent !== "message") continue;
				if (!trimmedLine.startsWith("data:")) continue;

				const data = trimmedLine.substring(5).trim();
				if (!data || data === "" || data === "data:") continue;

				// ========== 【优先处理】检查是否为卡片数据标记 ==========
				if (data.trim() === "[CARD_DATA_START]") {
					console.log("🔄 [卡片数据] 开始收集");
					isCollectingCardData = true;
					cardDataBuffer = "";
					continue;
				}

				if (data.trim() === "[CARD_DATA_END]") {
					console.log("🔄 [卡片数据] 结束收集");
					console.log("📦 [卡片数据] 收集到的数据:", cardDataBuffer);

					// 解析卡片数据
					if (cardDataBuffer.trim()) {
						try {
							// 检查数据是否已经包含包装
							let dataToParse = cardDataBuffer.trim();
							if (!dataToParse.startsWith('[')) {
								// 不是数组，需要包装
								dataToParse = `[${dataToParse}]`;
							}

							console.log("📦 [卡片数据] 准备解析的数据:", dataToParse);

							// 直接解析JSON
							const parsedArray = JSON.parse(dataToParse);
							console.log("✅ [卡片数据] JSON解析成功，数组长度:", parsedArray.length);

							if (Array.isArray(parsedArray) && parsedArray.length > 0) {
								const firstCard = parsedArray[0];
								console.log("✅ [卡片数据] 第一个元素:", firstCard);

								// 提取卡片类型（支持两种字段名：type 和 cardType）
								const cardType = firstCard.type || firstCard.cardType;
								if (cardType) {
									const messageType = convertToSupportedCardType(cardType);
									console.log("✅ [卡片数据] 原始卡片类型:", cardType);
									console.log("✅ [卡片数据] 转换后的消息类型:", messageType);

									if (messageType) {
										const message = getMessage(messageIndex);
										if (message) {
											message.messageType = messageType;
											console.log("✅ [卡片数据] 消息类型已设置:", messageType);
										}
									}
								}

								// 提取卡片数据（根据类型提取对应字段）
								const message = getMessage(messageIndex);
								if (message) {
									if (firstCard.recommendations) {
										message.cardData = { recommendations: firstCard.recommendations };
										console.log("✅ [卡片数据] 卡片数据已设置 (recommendations)");
									} else if (firstCard.orders) {
										message.cardData = { orders: firstCard.orders };
										console.log("✅ [卡片数据] 卡片数据已设置 (orders)");
									} else if (firstCard.dishes) {
										// 字段名映射：将后端字段转换为前端期望的字段
										const mappedDishes = firstCard.dishes.map(dish => ({
											dishId: dish.dishId || dish.id,
											dishName: dish.dishName || dish.title,
											imageUrl: dish.imageUrl || dish.image,
											description: dish.description || dish.highlight,
											price: dish.price,
											rating: dish.rating,
											category: dish.category,
											tags: dish.tags || [],
											actions: dish.actions || []
										}));
										message.cardData = { dishes: mappedDishes };
										console.log("✅ [卡片数据] 卡片数据已设置 (dishes, 已映射字段)");
									} else if (cardType === 'dish') {
										// 菜品类型：整个数组就是菜品列表，需要字段映射
										const mappedDishes = parsedArray.map(dish => ({
											dishId: dish.dishId || dish.id,
											dishName: dish.dishName || dish.title,
											imageUrl: dish.imageUrl || dish.image,
											description: dish.description || dish.highlight,
											price: dish.price,
											rating: dish.rating,
											category: dish.category,
											tags: dish.tags || [],
											actions: dish.actions || []
										}));
										message.cardData = { dishes: mappedDishes };
										console.log("✅ [卡片数据] 菜品数组已映射并包装，数量:", mappedDishes.length);
									} else {
										// 其他类型，直接使用整个对象
										message.cardData = firstCard;
										console.log("✅ [卡片数据] 卡片数据已设置 (原始对象)");
									}
								}
							}
						} catch (error) {
							console.warn("⚠️ [卡片数据] 解析失败:", error.message);
							console.warn("⚠️ [卡片数据] 错误详情:", error);
						}
					}

					// 重置收集状态
					isCollectingCardData = false;
					cardDataBuffer = "";
					continue;
				}

				// 如果正在收集卡片数据，添加到缓冲区
				if (isCollectingCardData) {
					cardDataBuffer += data;
					console.log("📦 [卡片数据] 收集中:", data);
					continue;
				}

				try {
					// 解析SSE数据
					let parsedData;
					let isPlainText = false;

					if (data.startsWith("[")) {
						// Spring Boot SseEmitter数组格式
						const dataArray = JSON.parse(data);
						const actualDataItem = dataArray.find((item) => {
							const itemData = item.data;
							return (
								itemData &&
								typeof itemData === "object" &&
								!itemData.mediaType &&
								(itemData.hasOwnProperty("done") ||
									itemData.hasOwnProperty("content") ||
									itemData.hasOwnProperty("card_data"))
							);
						});
						if (actualDataItem?.data) parsedData = actualDataItem.data;
					} else if (data.startsWith("{")) {
						// JSON对象格式
						parsedData = JSON.parse(data);
						if (parsedData.hasOwnProperty("char")) {
							if (parsedData.char === "\n") console.log("📥 收到换行符");
							isPlainText = true;
							parsedData = { content: parsedData.char, done: false };
						}
					} else {
						// 纯文本格式
						isPlainText = true;
						parsedData = { content: data, done: false };
					}

					// 验证解析结果
					if (!isPlainText && parsedData && typeof parsedData !== "object") {
						console.warn("⚠️ 解析结果类型错误:", typeof parsedData);
						continue;
					}
					if (!parsedData) continue;

					// 调试日志
					console.log("📥 [SSE接收] 接收到message事件");
					console.log(
						"📦 原始数据:",
						data.substring(0, 100) + (data.length > 100 ? "..." : "")
					);
					console.log("🔍 解析类型:", isPlainText ? "纯文本" : "JSON对象");
					console.log("📋 解析结果:", parsedData);
					if (parsedData.message)
						console.log(
							"💬 消息内容:",
							parsedData.message.substring(0, 100) + "..."
						);
					if (parsedData.agentName)
						console.log("🤖 Agent名称:", parsedData.agentName);
					if (parsedData.output)
						console.log(
							"📤 Agent输出:",
							parsedData.output.substring(0, 100) + "..."
						);

					// 处理 done 字段
					if (parsedData.done === true) {
						const message = getMessage(messageIndex);
						if (message) {
							logMessageStats(message);
							await validateAndSaveMessage(messageIndex);
						}
						return;
					}

					// 识别消息类型
					// 优先级1：完成标记（最高优先级）
					if (parsedData.completed === true) {
						console.log("✅ [完成标记] 任务已完成，隐藏进度指示器");
						const message = getMessage(messageIndex);
						if (message) {
							message.progress = false;  // 清除进度标记
							message.completed = true;  // 设置完成标记
						}
						continue;
					}

					// 优先级2：进度消息
					if (parsedData.progress === true && parsedData.message) {
						console.log("📢 [进度消息]", parsedData.message);
						const message = getMessage(messageIndex);
						if (message) {
							message.content = parsedData.message;
							message.progress = true;  // 直接设置 progress 字段
						}
						continue;
					}

					// 优先级2：卡片数据
					if (
						parsedData.type &&
						["dish", "merchant", "order", "health"].includes(parsedData.type)
					) {
						console.log(
							"🎴 [卡片数据] 类型:",
							parsedData.type,
							"标题:",
							parsedData.title
						);
						let cardContent = `**${parsedData.title}**\n`;
						if (parsedData.subtitle)
							cardContent += `${parsedData.subtitle}\n`;
						if (parsedData.description)
							cardContent += `\n${parsedData.description}`;
						parsedData.content = cardContent;
						parsedData._isFinalResult = true;
						parsedData._cardData = parsedData;
						parsedData._cardType = parsedData.type;
					}
					// 优先级3：info消息
					else if (parsedData.type === "info" && parsedData.content) {
						console.log(
							"✅ [最终结果] 收到AI回复，长度:",
							parsedData.content.length
						);
						console.log("📋 标题:", parsedData.title);
						parsedData._isFinalResult = true;
					}
					// 优先级4：SupervisorAgent的output
					else if (
						parsedData.agentName === "SupervisorAgent" &&
						parsedData.output
					) {
						console.log(
							"✅ [最终结果] 收到SupervisorAgent输出，长度:",
							parsedData.output.length
						);
						parsedData.content = parsedData.output;
						parsedData._isFinalResult = true;
					}
					// 优先级5：普通内容
					else if (parsedData.content) {
						console.log("📝 [内容更新] 长度:", parsedData.content.length);
					}

					// 处理 card_data 字段
					if (parsedData.card_data) {
						const message = getMessage(messageIndex);
						if (!message) {
							console.warn("⚠️ 组件已卸载或消息索引无效");
							break;
						}

						console.log("📊 收到卡片数据:", parsedData.card_data);
						try {
							const {
								messageType: cardMessageType,
								data: cardDataPayload,
							} = parsedData.card_data;
							if (cardMessageType && cardDataPayload) {
								message.messageType = cardMessageType;
								message.cardData = cardDataPayload;
								console.log("✅ 卡片数据已设置:", {
									messageType: message.messageType,
									hasCardData: !!message.cardData,
									cardDataKeys: message.cardData
										? Object.keys(message.cardData)
										: [],
								});
								await updateUI();
							} else {
								console.warn("⚠️ 卡片数据无效:", {
									cardMessageType,
									cardDataPayload,
								});
							}
						} catch (error) {
							console.warn("⚠️ 更新卡片数据失败:", error.message);
						}
					}

					// 处理 content 字段
					if (parsedData.content) {
						const message = getMessage(messageIndex);
						if (!message) {
							console.warn("⚠️ 组件已卸载或消息索引无效");
							break;
						}

						// 清除思考状态和进度标记
						if (message.isThinking) {
							message.isThinking = false;
							console.log("💭 思考状态已清除，开始接收内容");
						}

						// 清除进度标记（开始接收实际内容）
						if (message.progress === true) {
							message.progress = false;
							console.log("📢 进度标记已清除，开始接收实际内容");
						}

						let newContent = parsedData.content;

						// 检查并解析content中的卡片数据标记
						const { content: cleanContent, cardData, messageType } = parseCardDataFromContent(newContent);

						// 如果提取到了卡片数据，更新消息
						if (cardData) {
							message.cardData = cardData;
							console.log("✅ [实时消息] 卡片数据已提取");
						}
						if (messageType) {
							message.messageType = messageType;
							console.log("✅ [实时消息] 卡片类型已提取:", messageType);
						}

						// 使用清理后的内容（移除了卡片数据标记）
						newContent = cleanContent;

						const toolPromptRegex = /🔧\s*正在执行工具函数[.。]{0,3}/;
						const hasToolPrompt = toolPromptRegex.test(newContent);

						try {
							message.content ??= "";
							message.displayContent ??= "";

							if (hasToolPrompt && !message.isToolExecuting) {
								message.isToolExecuting = true;
								message.toolCompleted = false;
								message.hasToolPrompt = true;
								console.log("🔧 工具开始执行");
							}

							const filteredContent = newContent.replace(
								toolPromptRegex,
								""
							);
							message.content += newContent;
							message.displayContent += filteredContent;

							if (
								message.isToolExecuting &&
								filteredContent.trim() &&
								!hasToolPrompt
							) {
								message.isToolExecuting = false;
								message.toolCompleted = true;
								console.log("✅ 工具执行完成");

								// 3秒后隐藏完成通知
								const currentIndex = messageIndex;
								setTimeout(() => {
									if (
										isMounted.value &&
										messages.value?.[currentIndex] &&
										Object.prototype.hasOwnProperty.call(
											messages.value[currentIndex],
											"toolCompleted"
										)
									) {
										messages.value[
											currentIndex
										].toolCompleted = false;
									}
								}, 3000);
							}
						} catch (error) {
							console.warn("⚠️ 更新消息内容失败:", error.message);
						}

						await updateUI();

						// 处理最终结果
						if (parsedData._isFinalResult) {
							await handleFinalResult(messageIndex, parsedData);
						}
					}
				} catch (error) {
					if (error.name === "SyntaxError") {
						console.warn(
							"⚠️ JSON解析失败，数据格式不正确:",
							data.substring(0, 100)
						);
					} else {
						console.warn("⚠️ 数据处理错误:", error.message);
					}
				}
			}
		}
	} catch (error) {
		if (error.name === "AbortError") {
			console.log("ℹ️ 用户主动停止流式传输");
			return;
		}
		console.error("❌ 流式传输错误:", error);
		throw error;
	} finally {
		isStreaming.value = false;
	}
};

// 发送消息
const sendMessage = async () => {
	const message = inputMessage.value.trim();
	const hasImages = uploadedImages.value.length > 0;

	if (!message && !hasImages) {
		ElMessage.warning("请输入问题或上传图片");
		return;
	}

	if (message.length > 500) {
		ElMessage.warning("消息长度不能超过500个字符");
		return;
	}

	// ⌨️ 停止所有正在进行的打字机效果
	messages.value.forEach((msg) => {
		if (msg.isTyping) {
			msg.isTyping = false;
			msg.showCursor = false;
			// 恢复完整内容
			if (msg.content) {
				msg.displayContent = msg.content;
			}
		}
	});

	// ========== 日志记录：请求开始 ==========
	const requestStartTime = Date.now();
	console.log("==================== AI聊天请求开始 ====================");
	console.log("⏰ 请求时间:", new Date().toLocaleString());
	console.log("📝 用户消息:", message);
	console.log("📏 消息长度:", message.length, "字符");
	console.log("📊 当前消息数量:", messages.value.length);

	// 创建用户消息
	const userMessage = {
		id: messages.value.length + 1,
		sender: "user",
		content: message,
		time: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
		avatar: userStore.userInfo?.avatar || "", // 使用用户真实头像
		images: hasImages ? [...uploadedImages.value] : undefined,
	};

	messages.value.push(userMessage);
	const userInput = message;

	// 清空输入
	inputMessage.value = "";
	uploadedImages.value = [];

	// 保存用户消息到后端
	await saveMessageToBackend("user", message);

	// 用户发送新消息时,重置滚动标志并强制滚动到底部
	userHasScrolled.value = false;
	isUserScrollingUp = false; // 重置向上滚动标记
	scrollToBottom(true);

	// Call backend AI API
	isLoading.value = true;

	// 创建AI消息占位
	const aiMessageIndex = messages.value.length;
	messages.value.push({
		id: aiMessageIndex,
		sender: "ai",
		content: "",
		displayContent: "", // 过滤后的显示内容(不含工具提示)
		time: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
		avatar: "🤖",
		enableMarkdown: true,
		isToolExecuting: false, // 是否正在执行工具
		toolCompleted: false, // 工具是否执行完成
		hasToolPrompt: false, // 是否包含工具提示
		messageType: null, // 消息类型(用于卡片渲染)
		cardData: null, // 卡片数据
		isTyping: false, // 是否正在打字机效果
		typingIndex: 0, // 当前打字位置索引
		showCursor: false, // 是否显示光标
		isThinking: true, // 初始状态为思考中
		progress: false, // 进度消息标记（后端发送的progress字段）
	});

	// 不自动滚动,让用户控制查看位置

	// ========== 日志记录：API调用 ==========
	const userId = getUserId();
	const apiUrl = API_CONFIG.baseURL + API_CONFIG.ai.chat;
	console.log("🌐 发送流式API请求到:", apiUrl);
	console.log("👤 authStore.userId:", authStore.userId);
	console.log("👤 实际使用的userId:", userId);
	console.log("📦 请求体:", { message: userInput, userId });
	console.log("🔑 Token存在:", !!authStore.token);

	// 创建新的AbortController用于取消请求
	abortController.value = new AbortController();

	try {
		// 使用fetch API发起流式请求
		const response = await fetch(apiUrl, {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				Accept: "text/event-stream",
				Authorization: `Bearer ${authStore.token}`, // 添加JWT token
			},
			body: JSON.stringify({ message: userInput, userId }), // 添加userId
			signal: abortController.value.signal,
		});

		if (!response.ok) {
			throw new Error(`HTTP error! status: ${response.status}`);
		}

		// ========== 日志记录：响应接收 ==========
		const responseTime = Date.now() - requestStartTime;
		console.log("✅ 连接成功，耗时:", responseTime, "ms");

		// 获取流式读取器
		const reader = response.body.getReader();

		// 使用流式传输处理响应（传入消息索引而不是消息对象）
		await streamResponse(aiMessageIndex, reader);

		const totalTime = Date.now() - requestStartTime;
		console.log("✨ 整体请求完成，总耗时:", totalTime, "ms");
		console.log(
			"📝 AI回复最终内容长度:",
			messages.value[aiMessageIndex].content.length,
			"字符"
		);
		console.log("==================== AI聊天请求完成 ====================\n");
	} catch (error) {
		// ========== 日志记录：错误处理 ==========

		// 用户主动取消，静默处理，不显示错误日志
		if (error.name === "AbortError") {
			console.log("ℹ️ 用户主动取消AI回复");
			console.log("==================== AI聊天请求已取消 ====================\n");
			return; // 直接返回，不执行后续错误处理
		}

		// 其他错误的处理
		const errorTime = Date.now() - requestStartTime;
		console.error("❌ API请求失败，耗时:", errorTime, "ms");
		console.error("📋 错误对象:", error);
		console.error("❌ 错误消息:", error.message);

		let errorMsg = "对不起，暂时无法获取AI回复，请稍后重试。";

		// Add more specific error messages
		if (error.message.includes("HTTP error")) {
			const statusCode = parseInt(error.message.match(/\d+/)?.[0] || "500");
			console.error("🔴 服务器错误状态码:", statusCode);

			if (statusCode === 404) {
				errorMsg = "AI聊天服务暂时不可用，请稍后重试。";
			} else if (statusCode === 500) {
				errorMsg = "服务器内部错误，请稍后重试。";
			} else {
				errorMsg = `服务器错误(${statusCode})，请稍后重试。`;
			}
		} else if (error.message.includes("fetch")) {
			// Network error
			console.error("🔴 网络错误，无响应");
			errorMsg = "网络连接超时，请检查网络设置。";
		}

		// 只有当内容为空时才显示错误消息
		if (!messages.value[aiMessageIndex].content) {
			messages.value[aiMessageIndex].content = errorMsg;
		}

		const totalTime = Date.now() - requestStartTime;
		console.log("==================== AI聊天请求失败 ====================\n");
	} finally {
		isLoading.value = false;
		abortController.value = null;
	}
};

// 滚动到底部
// 处理滚动事件,检测用户是否手动滚动
const handleScroll = () => {
	// 如果是自动滚动,不处理
	if (isAutoScrolling) {
		return;
	}

	const container = chatContainerRef.value;
	if (!container) {
		return;
	}

	const currentScrollTop = container.scrollTop;

	// 检测滚动方向
	if (currentScrollTop < lastScrollTop) {
		// 用户向上滚动
		isUserScrollingUp = true;
	} else if (currentScrollTop > lastScrollTop) {
		// 用户向下滚动，检查是否接近底部
		const threshold = container.scrollHeight * 0.11;
		const isNearBottom =
			container.scrollHeight - currentScrollTop - container.clientHeight <
			threshold;

		if (isNearBottom) {
			// 如果用户在底部11%范围内，重置向上滚动标记
			isUserScrollingUp = false;
		}
	}

	// 更新上一次的滚动位置
	lastScrollTop = currentScrollTop;

	// 检查是否接近底部(阈值为底部11%)
	const threshold = container.scrollHeight * 0.11;
	const isNearBottom =
		container.scrollHeight - container.scrollTop - container.clientHeight < threshold;

	// 如果不在底部11%范围内,标记用户已手动滚动
	if (!isNearBottom) {
		userHasScrolled.value = true;
	} else {
		// 如果用户在底部11%范围内,重置标志(允许自动滚动)
		userHasScrolled.value = false;
	}
};

const scrollToBottom = (force = false) => {
	// 组件挂载状态检查
	if (!isMounted.value) {
		console.log("🚫 组件未挂载，跳过滚动");
		return;
	}

	// 只有在强制滚动、用户未手动滚动或用户未主动向上滚动时才自动滚动
	if (force || !userHasScrolled.value || !isUserScrollingUp) {
		isAutoScrolling = true;

		// 使用双重 nextTick 确保 DOM 完全渲染
		nextTick(() => {
			nextTick(() => {
				// 双重检查：组件可能在此期间卸载
				if (!isMounted.value || !chatContainerRef.value) {
					return;
				}

				try {
					chatContainerRef.value.scrollTop =
						chatContainerRef.value.scrollHeight;
				} catch (error) {
					console.warn("⚠️ 滚动到底部失败:", error.message);
				}

				// 延迟重置标志,确保滚动事件不会误触发
				setTimeout(() => {
					isAutoScrolling = false;
				}, 100);
			});
		});
	} else {
		console.log(
			"🚫 跳过自动滚动: force=",
			force,
			", userHasScrolled=",
			userHasScrolled.value
		);
	}
};

// 键盘事件
const handleKeydown = (event) => {
	if (event.key === "Enter" && event.shiftKey) {
		event.preventDefault();
		sendMessage();
	}
};

// 发送按钮点击
const handleSendClick = () => {
	if (isStreaming.value) {
		stopStreaming();
	} else {
		sendMessage();
	}
};

// 停止流式传输
const stopStreaming = () => {
	if (abortController.value) {
		console.log("🛑 用户主动停止流式传输");
		abortController.value.abort();
		ElMessage.info("已停止AI回复");
	}
};

// 快捷提问点击
const handleQuickQuestion = (question) => {
	inputMessage.value = question;
	sendMessage();
	// 发送后自动关闭面板
	showQuickQuestions.value = false;
};

// 刷新卡片数据
const refreshCard = async () => {
	if (lastQueryType.value && lastQueryMessageIndex.value >= 0) {
		const userId = getUserId();

		try {
			const apiUrl = `${API_CONFIG.baseURL}/v1/ai/assistant/chat`;
			const response = await axios.post(
				apiUrl,
				{
					messageType: "structured_query",
					queryType: lastQueryType.value,
					userId: userId,
					params: {},
				},
				{
					headers: {
						Authorization: `Bearer ${authStore.token}`,
					},
				}
			);

			if (response.data.code === 200 && response.data.data) {
				const cardData = response.data.data;
				messages.value[lastQueryMessageIndex.value].messageType =
					cardData.messageType;
				messages.value[lastQueryMessageIndex.value].cardData = cardData.data;
				messages.value[lastQueryMessageIndex.value].content =
					cardData.summary || "查询成功";
				ElMessage.success("数据已刷新");
			}
		} catch (error) {
			console.error("刷新卡片失败:", error);
			ElMessage.error("刷新失败，请稍后重试");
		}
	}
};

// 处理卡片操作
const handleCardAction = async (action) => {
	console.log("🎯 卡片操作:", action);
	const userId = getUserId();

	try {
		switch (action.type) {
			case "detail":
				// 查看订单详情 - 导航到订单详情页
				if (action.data.orderId) {
					router
						.push({
							name: "OrderDetail",
							params: { orderId: action.data.orderId },
						})
						.catch(() => {
							ElMessage.info(`订单ID: ${action.data.orderId}`);
						});
				}
				break;

			case "cancel":
				// 取消订单
				try {
					await ElMessageBox.confirm("确认取消此订单？", "提示", {
						confirmButtonText: "确定",
						cancelButtonText: "取消",
						type: "warning",
					});

					const cancelResult = await cardActionService.order.cancelOrder(
						action.data.orderId
					);

					if (cancelResult.code === 200) {
						ElMessage.success("订单已取消");
						// 刷新卡片数据
						await refreshCard();
					} else {
						ElMessage.error(cancelResult.message || "取消订单失败");
					}
				} catch (error) {
					if (error !== "cancel") {
						console.error("取消订单失败:", error);
						ElMessage.error("取消订单失败，请稍后重试");
					}
				}
				break;

			case "urge":
				// 催单
				try {
					const urgeResult = await cardActionService.order.urgeOrder(
						action.data.orderId
					);
					if (urgeResult.code === 200) {
						ElMessage.success("已通知商家尽快处理您的订单");
					} else {
						ElMessage.warning(urgeResult.message || "催单请求已发送");
					}
				} catch (error) {
					console.warn("催单失败，但显示友好提示:", error);
					ElMessage.success("已通知商家尽快处理您的订单");
				}
				break;

			case "add_to_cart":
				// 加入购物车
				try {
					const dishId = action.data.dishId;
					const addToCartResult = await cardActionService.cart.addToCart(
						userId,
						dishId,
						1
					);

					if (addToCartResult.code === 200) {
						ElMessage.success(`已将 ${action.data.dishName} 加入购物车`);
					} else {
						ElMessage.warning(addToCartResult.message || "已添加到购物车");
					}
				} catch (error) {
					console.error("加入购物车失败:", error);
					ElMessage.error("加入购物车失败，请稍后重试");
				}
				break;

			case "remove_favorite":
				// 取消收藏
				try {
					await ElMessageBox.confirm("确认取消收藏此菜品？", "提示", {
						confirmButtonText: "确定",
						cancelButtonText: "取消",
						type: "warning",
					});

					const removeResult = await cardActionService.favorite.removeFavorite(
						userId,
						action.data.dishId
					);

					if (removeResult.code === 200) {
						ElMessage.success("已取消收藏");
						// 刷新卡片数据
						await refreshCard();
					} else {
						ElMessage.error(removeResult.message || "取消收藏失败");
					}
				} catch (error) {
					if (error !== "cancel") {
						console.error("取消收藏失败:", error);
						ElMessage.error("取消收藏失败，请稍后重试");
					}
				}
				break;

			case "delete":
				// 删除评价
				try {
					await ElMessageBox.confirm(
						"确认删除此评价？删除后无法恢复。",
						"提示",
						{
							confirmButtonText: "确定",
							cancelButtonText: "取消",
							type: "warning",
						}
					);

					const deleteResult = await cardActionService.review.deleteReview(
						action.data.reviewId
					);

					if (deleteResult.code === 200) {
						ElMessage.success("评价已删除");
						// 刷新卡片数据
						await refreshCard();
					} else {
						ElMessage.error(deleteResult.message || "删除评价失败");
					}
				} catch (error) {
					if (error !== "cancel") {
						console.error("删除评价失败:", error);
						ElMessage.error("删除评价失败，请稍后重试");
					}
				}
				break;

			case "edit_profile":
				// 编辑资料 - 导航到个人资料页
				router
					.push({
						name: "user-profile",
					})
					.catch(() => {
						ElMessage.info("跳转到个人资料编辑页面");
					});
				break;

			case "view_health":
				// 健康分析 - 导航到卡路里统计页面
				router
					.push({
						name: "user-calorie",
					})
					.catch(() => {
						ElMessage.info("跳转到健康分析页面");
					});
				break;

			case "add_favorite":
				// 收藏菜品
				try {
					const addResult = await cardActionService.favorite.addFavorite(
						userId,
						action.data.dishId
					);

					if (addResult.code === 200) {
						ElMessage.success(`已收藏 ${action.data.dishName}`);
					} else {
						ElMessage.warning(addResult.message || "收藏成功");
					}
				} catch (error) {
					console.error("收藏失败:", error);
					ElMessage.error("收藏失败，请稍后重试");
				}
				break;

			case "view_detail":
				// 查看菜品详情 - 导航到菜品详情页
				if (action.data.dishId) {
					router
						.push({
							name: "dish-detail",
							params: { dishId: action.data.dishId },
						})
						.catch(() => {
							ElMessage.info(`查看 ${action.data.dishName} 详情`);
						});
				}
				break;

			default:
				ElMessage.info(`操作: ${action.type}`);
		}
	} catch (error) {
		console.error("处理卡片操作失败:", error);
		ElMessage.error("操作失败，请稍后重试");
	}
};

// 切换表情面板
const toggleEmoji = () => {
	showEmojiPicker.value = !showEmojiPicker.value;
	// 关闭快捷提问面板
	if (showEmojiPicker.value) {
		showQuickQuestions.value = false;
	}
};

// 切换快捷提问面板
const toggleQuickQuestions = () => {
	const isOpening = !showQuickQuestions.value;
	showQuickQuestions.value = !showQuickQuestions.value;

	// 打开面板时重置展开状态，关闭时保持当前状态（用户体验更好）
	if (isOpening) {
		expandedCategory.value = null;
	}

	// 关闭表情面板
	if (showQuickQuestions.value) {
		showEmojiPicker.value = false;
	}
};

// 选择表情
const selectEmoji = (emoji) => {
	inputMessage.value += emoji;
	showEmojiPicker.value = false;
	nextTick(() => {
		const textarea = bottomContainerRef.value?.querySelector("textarea");
		if (textarea) {
			textarea.focus();
			textarea.selectionStart = textarea.selectionEnd = textarea.value.length;
		}
	});
};

// 触发图片上传
const triggerImageUpload = () => {
	const input = bottomContainerRef.value?.querySelector('input[type="file"]');
	if (input) {
		input.click();
	}
};

// 处理图片上传
const handleImageUpload = (event) => {
	const file = event.target.files[0];
	if (!file) return;

	// 验证文件类型
	if (!file.type.startsWith("image/")) {
		ElMessage.error("请选择图片文件");
		return;
	}

	// 验证文件大小（10MB）
	if (file.size > 10 * 1024 * 1024) {
		ElMessage.error("图片大小不能超过10MB");
		return;
	}

	// 创建预览
	const reader = new FileReader();
	reader.onload = (e) => {
		uploadedImages.value.push({
			id: Date.now(),
			url: e.target.result,
			file: file,
		});
		ElMessage.success("图片上传成功");
	};
	reader.readAsDataURL(file);

	// 清空input，允许重复上传
	event.target.value = "";
};

// 移除已上传的图片
const removeUploadedImage = (imageId) => {
	const index = uploadedImages.value.findIndex((img) => img.id === imageId);
	if (index > -1) {
		uploadedImages.value.splice(index, 1);
	}
};

// 恢复历史消息的卡片数据（向后兼容）
const restoreCardDataForMessages = async () => {
	const userId = getUserId();

	for (let i = 0; i < messages.value.length; i++) {
		const message = messages.value[i];

		// 只处理AI消息，且没有卡片数据的情况
		if (message.sender === "ai" && !message.messageType && message.content) {
			let queryType = null;
			const content = message.content;

			// 检测消息类型并重新查询卡片数据
			if (content.includes("找到") && content.includes("条订单")) {
				queryType = "list_orders";
			} else if (content.includes("共收到") && content.includes("条通知")) {
				queryType = "list_notifications";
			} else if (content.includes("收藏列表") || content.includes("我的收藏")) {
				queryType = "get_favorites";
			} else if (content.includes("评价列表") || content.includes("我的评价")) {
				queryType = "get_user_reviews";
			} else if (content.includes("优惠券") || content.includes("我的优惠券")) {
				queryType = "get_user_coupons";
			} else if (content.includes("用户档案") || content.includes("用户信息")) {
				queryType = "get_user_info";
			}

			// 如果检测到需要卡片数据，重新查询
			if (queryType) {
				try {
					console.log("🔄 恢复卡片数据: queryType=", queryType);

					const response = await axios.post(
						API_CONFIG.baseURL + "/v1/ai/assistant/chat",
						{
							messageType: "structured_query",
							queryType: queryType,
							userId: userId,
							params: {},
						},
						{
							headers: {
								Authorization: `Bearer ${authStore.token}`,
							},
						}
					);

					if (response.data.code === 200 && response.data.data) {
						const cardData = response.data.data;
						messages.value[i].messageType = cardData.messageType;
						messages.value[i].cardData = cardData.data;
						console.log("✅ 卡片数据已恢复:", cardData.messageType);
					}
				} catch (error) {
					console.warn("⚠️ 恢复卡片数据失败:", queryType, error);
				}
			}
		}
	}
};

// 清空输入
// 清空对话
const clearChat = () => {
	ElMessageBox.confirm("确定要清空所有聊天记录吗？", "提示", {
		confirmButtonText: "确定",
		cancelButtonText: "取消",
		type: "warning",
	})
		.then(async () => {
			try {
				const userId = getUserId();
				console.log("🗑️ 开始清空聊天记录，userId:", userId);

				// 调用Agent清除接口（返回欢迎消息）
				const clearResponse = await axios.delete(
					`${API_CONFIG.baseURL}/v1/agent/context/${userId}`
				);
				console.log("📡 后端清空响应:", clearResponse.data);

				// 检查后端是否成功清空
				if (clearResponse.data.success === true) {
					console.log("✅ 后端清空成功");

					// 清空前端显示
					messages.value = [];

					// 获取后端返回的欢迎消息
					const welcomeMessage =
						clearResponse.data.data?.welcomeMessage || WELCOME_MESSAGE;

					// 创建并添加欢迎消息（启用markdown渲染）
					const aiMessage = {
						id: Date.now(),
						sender: "ai",
						content: welcomeMessage,
						time: new Date().toLocaleTimeString([], {
							hour: "2-digit",
							minute: "2-digit",
						}),
						avatar: "🤖",
						enableMarkdown: true, // 启用markdown渲染
					};
					messages.value.push(aiMessage);

					console.log("✅ 已显示Agent欢迎消息");
					ElMessage.success("聊天记录已清空");

					// 清空后滚动到顶部
					nextTick(() => {
						if (chatContainerRef.value) {
							chatContainerRef.value.scrollTop = 0;
						}
					});
				} else {
					// 后端返回错误
					console.error("❌ 后端清空失败");
					console.error("❌ 错误信息:", clearResponse.data.message);
					ElMessage.error(clearResponse.data.message || "清空失败，请稍后重试");
				}
			} catch (error) {
				console.error("❌ 清空聊天记录失败:", error);
				console.error("❌ 错误详情:", error.response?.data || error.message);

				// 根据错误类型显示不同提示
				let errorMsg = "清空失败，请稍后重试";
				if (error.response) {
					// 服务器返回了错误响应
					if (error.response.status === 404) {
						errorMsg = "清空服务暂时不可用";
					} else if (error.response.status === 500) {
						errorMsg = "服务器内部错误";
					} else if (error.response.data?.message) {
						errorMsg = error.response.data.message;
					}
				} else if (error.message) {
					errorMsg = `网络错误：${error.message}`;
				}

				ElMessage.error(errorMsg);
			}
		})
		.catch(() => {
			console.log("ℹ️ 用户取消清空操作");
		});
};

// 点击外部关闭表情面板和快捷提问面板
const handleClickOutside = (event) => {
	// 获取快捷提问面板元素
	const quickQuestionsPanel = document.querySelector(".quick-questions-panel-fixed");

	if (bottomContainerRef.value && !bottomContainerRef.value.contains(event.target)) {
		// 如果快捷提问面板显示中，并且点击的不是面板内部，才关闭
		if (showQuickQuestions.value) {
			if (quickQuestionsPanel && !quickQuestionsPanel.contains(event.target)) {
				showQuickQuestions.value = false;
			}
		} else {
			showEmojiPicker.value = false;
		}
	}
};

// 切换个性化数据开关
const handlePersonalDataToggle = async (value) => {
	try {
		const userId = getUserId();
		console.log("🔄 切换AI个性化数据:", value);

		await axios.put(`${API_CONFIG.baseURL}/v1/users/${userId}/preferences`, {
			enableAiPersonalData: value,
		});

		ElMessage.success(value ? "已开启个性化建议" : "已关闭个性化建议");
		console.log("✅ 用户偏好更新成功");
	} catch (error) {
		console.error("❌ 更新偏好设置失败:", error);
		ElMessage.error("设置保存失败");

		// 恢复原状态
		aiPersonalDataEnabled.value = !value;
	}
};

// 判断消息是否应该显示
const shouldShowMessage = (message) => {
	// 如果正在打字机效果中，始终显示
	if (message.isTyping) {
		return true;
	}
	// 如果有进度状态，显示消息
	if (message.sender === 'ai' && getMessageProgressStatus(message)) {
		return true;
	}
	// 如果有 displayContent 字段，使用它
	if (message.displayContent !== undefined) {
		return (
			message.displayContent.length > 0 ||
			(message.messageType && isCardMessage(message.messageType))
		);
	}
	// 否则使用 content 字段
	return message.content && message.content.length > 0;
};

// 打字机效果函数
const startTypewriterEffect = async (messageIndex, fullText, speed = 30) => {
	const message = messages.value[messageIndex];
	if (!message) return;

	// 初始化打字状态
	message.isTyping = true;
	message.typingIndex = 0;
	message.showCursor = true;

	// 清空显示内容，准备开始打字
	message.displayContent = "";

	// 逐字显示
	const totalLength = fullText.length;
	while (message.typingIndex < totalLength && message.isTyping) {
		// 每次增加几个字符（加快速度）
		const charsToAdd = Math.min(3, totalLength - message.typingIndex);
		message.typingIndex += charsToAdd;
		message.displayContent = fullText.substring(0, message.typingIndex);

		// 等待一段时间
		await new Promise((resolve) => setTimeout(resolve, speed));

		// 自动滚动到底部
		if (isMounted.value) {
			await nextTick();
			scrollToBottom();
		}
	}

	// 打字完成
	message.isTyping = false;
	message.showCursor = false;
	message.displayContent = fullText;

	console.log("✅ 打字机效果完成");
};

// 获取消息的显示内容
const getDisplayContent = (message) => {
	// 如果正在打字机效果中，返回打字内容
	if (message.isTyping && message.displayContent !== undefined) {
		return message.displayContent;
	}
	// 优先使用 displayContent（过滤后的内容）
	if (message.displayContent !== undefined) {
		return message.displayContent;
	}
	// 否则使用原始 content
	return message.content || "";
};

// 复制消息
const copyMessage = async (content) => {
	try {
		// 在Electron环境中，优先使用clipboard模块
		if (window.api && window.api.clipboard) {
			window.api.clipboard.writeText(content);
			ElMessage.success("复制成功");
			return;
		}

		// 优先尝试使用现代剪贴板API
		if (navigator.clipboard && navigator.clipboard.writeText) {
			await navigator.clipboard.writeText(content);
			ElMessage.success("复制成功");
			return;
		}

		// Fallback: 使用传统方法创建临时文本区域
		const textArea = document.createElement("textarea");
		textArea.value = content;
		textArea.style.position = "fixed";
		textArea.style.left = "-999999px";
		textArea.style.top = "-999999px";
		textArea.setAttribute("readonly", ""); // 防止移动端弹出键盘
		document.body.appendChild(textArea);

		// 使用select()而非setSelectionRange()以获得更好的兼容性
		textArea.focus();
		textArea.select();

		try {
			const successful = document.execCommand("copy");
			if (successful) {
				ElMessage.success("复制成功");
			} else {
				throw new Error("execCommand failed");
			}
		} finally {
			document.body.removeChild(textArea);
		}
	} catch (error) {
		console.error("复制失败:", error);
		ElMessage.error("复制失败,请手动复制");
	}
};

// 处理消息操作菜单命令
const handleMessageAction = async (command, content) => {
	switch (command) {
		case "copy":
			await copyMessage(content);
			break;
		// 可以在这里添加更多操作
	}
};

// 监听消息变化，延时自动滚动到底部
watch(
	messages,
	async (newMessages) => {
		// 只有在组件已挂载且有消息时才滚动
		if (isMounted.value && newMessages.length > 0) {
			// 延时滚动，避免频繁触发
			setTimeout(async () => {
				if (!isMounted.value) return;
				await nextTick();
				await nextTick();
				scrollToBottom(true);
			}, 300); // 延时 300ms
		}
	},
	{ flush: "post", immediate: false } // 确保在DOM更新后执行，不立即执行
);

// 生命周期
onMounted(async () => {
	console.log("🚀 组件已挂载");

	// 标记组件已挂载
	isMounted.value = true;

	document.addEventListener("click", handleClickOutside);

	// 添加滚动事件监听器
	if (chatContainerRef.value) {
		chatContainerRef.value.addEventListener("scroll", handleScroll);
		// 初始化滚动位置
		lastScrollTop = chatContainerRef.value.scrollTop || 0;
		console.log("📜 聊天容器滚动事件已绑定");
	} else {
		console.warn("⚠️ 聊天容器引用不存在");
	}

	// 加载聊天历史记录
	await loadMessages();
	// 加载用户偏好设置
	await loadUserPreference();

	console.log("✅ AI聊天组件初始化完成");
});

// 暴露给父组件调用的方法 - 当tab激活时滚动到底部
const scrollToBottomOnActivate = async () => {
	if (!chatContainerRef.value) return;

	console.log("🔄 Tab激活，开始滚动到底部");

	// 重置滚动标志
	userHasScrolled.value = false;
	isUserScrollingUp = false;

	// 延时确保内容渲染完成
	await nextTick();
	await nextTick();

	requestAnimationFrame(() => {
		requestAnimationFrame(() => {
			if (chatContainerRef.value) {
				const scrollHeight = chatContainerRef.value.scrollHeight;
				chatContainerRef.value.scrollTop = scrollHeight;
				console.log("✅ Tab激活滚动完成:", scrollHeight);

				// 二次验证
				setTimeout(() => {
					if (chatContainerRef.value) {
						const finalScrollHeight = chatContainerRef.value.scrollHeight;
						chatContainerRef.value.scrollTop = finalScrollHeight;
					}
				}, 100);
			}
		});
	});
};

// 暴露方法给父组件
defineExpose({
	scrollToBottomOnActivate,
});

onUnmounted(() => {
	// 标记组件已卸载
	isMounted.value = false;

	document.removeEventListener("click", handleClickOutside);

	// 移除滚动事件监听器
	if (chatContainerRef.value) {
		chatContainerRef.value.removeEventListener("scroll", handleScroll);
	}
});
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
	scroll-behavior: smooth; // 添加平滑滚动效果
	// 性能优化：启用硬件加速
	transform: translateZ(0);
	-webkit-overflow-scrolling: touch; // iOS平滑滚动

	&::-webkit-scrollbar {
		width: 6px;
	}

	&::-webkit-scrollbar-thumb {
		background: #dee2e6;
		border-radius: 3px;

		&:hover {
			background: #adb5bd;
		}
	}

	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		height: 100%;
		color: #909399;

		p {
			margin: 12px 0 0 0;
			font-size: 1.143rem /* 原值: 16px */;
		}

		.hint {
			font-size: 1rem /* 原值: 14px */;
			color: #c0c4cc;
		}
	}

	// 初始加载容器
	.initial-loading-container {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		height: 100%;
		gap: 24px;

		.ai-avatar-loading {
			position: relative;
			display: flex;
			align-items: center;
			gap: 16px;

			.avatar-emoji {
				font-size: 48px;
				filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.15));
				animation: avatarFloat 2s ease-in-out infinite;
			}

			.typing-indicator {
				display: flex;
				align-items: center;
				gap: 6px;
				padding: 12px 18px;
				background: linear-gradient(135deg, #fff9fa 0%, #fff3f4 100%);
				border-radius: 20px;
				border: 1px solid #ffe0e3;
				box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

				.typing-dot {
					width: 8px;
					height: 8px;
					background: #ff6b6b;
					border-radius: 50%;
					animation: typingBounce 1.4s ease-in-out infinite;

					&:nth-child(1) {
						animation-delay: 0s;
					}

					&:nth-child(2) {
						animation-delay: 0.2s;
					}

					&:nth-child(3) {
						animation-delay: 0.4s;
					}
				}
			}
		}

		.loading-text {
			font-size: 1rem;
			color: #909399;
			animation: textPulse 2s ease-in-out infinite;
		}
	}

	// AI进度指示器（独立显示）
	.ai-progress-indicator {
		display: flex !important;
		align-items: center;
		gap: 12px;
		padding: 16px 20px;
		margin-bottom: 16px;
		background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
		border: 1px solid #bae6fd;
		border-radius: 12px;
		box-shadow: 0 2px 8px rgba(14, 165, 233, 0.15);
		opacity: 1 !important;
		position: relative;
		z-index: 10;
		width: 100%;
		box-sizing: border-box;
		// 性能优化：提示浏览器这个元素会发生变化
		will-change: opacity, transform;
		// GPU加速
		transform: translateZ(0);

		.progress-avatar {
			flex-shrink: 0;

			.avatar-emoji {
				font-size: 32px;
				filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
				animation: avatarPulse 2s ease-in-out infinite;
				// GPU加速
				transform: translateZ(0);
			}
		}

		.progress-content {
			flex: 1;
			display: flex;
			flex-direction: column;
			gap: 8px;

			.progress-text {
				font-size: 0.95rem;
				font-weight: 500;
				color: #0ea5e9;
				line-height: 1.5;
			}

			.progress-dots {
				display: flex;
				align-items: center;
				gap: 6px;

				.progress-dot {
					width: 6px;
					height: 6px;
					background: #0ea5e9;
					border-radius: 50%;
					animation: dotBounce 1.4s ease-in-out infinite;
					// GPU加速
					will-change: transform, opacity;
					transform: translateZ(0);

					&:nth-child(1) {
						animation-delay: 0s;
					}

					&:nth-child(2) {
						animation-delay: 0.2s;
					}

					&:nth-child(3) {
						animation-delay: 0.4s;
					}
				}
			}
		}
	}

	// 动画定义
	@keyframes progressSlideIn {
		from {
			opacity: 0;
			transform: translateY(-10px);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	@keyframes avatarPulse {
		0%,
		100% {
			transform: scale(1);
		}
		50% {
			transform: scale(1.05);
		}
	}

	@keyframes dotBounce {
		0%,
		80%,
		100% {
			transform: scale(0.8);
			opacity: 0.5;
		}
		40% {
			transform: scale(1);
			opacity: 1;
		}
	}

	.chat-message {
		display: flex;
		gap: 12px;
		margin-bottom: 24px;
		animation: messageFadeIn 0.4s ease-out;
		// 性能优化：提示浏览器这个元素会发生变化
		will-change: opacity, transform;
		// GPU加速
		transform: translateZ(0);

		&.user-message {
			flex-direction: row-reverse;
			justify-content: flex-start;

			.message-content {
				align-items: flex-end;

				.message-text {
					background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
					color: #fff;
					border-radius: 20px 4px 20px 20px;
					box-shadow: 0 4px 12px rgba(255, 107, 107, 0.25);
					font-weight: 500;
				}
			}
		}

		&.ai-message {
			flex-direction: row;
			justify-content: flex-start;

			.message-content {
				align-items: flex-start;

				.message-text {
					background: linear-gradient(135deg, #fff9fa 0%, #fff3f4 100%);
					color: #c8232c;
					border-radius: 4px 20px 20px 20px;
					box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
					border: 1px solid #ffe0e3;
				}

				// AI思考指示器样式
				.ai-thinking-indicator {
					padding: 16px 20px;
					background: linear-gradient(135deg, #fff9fa 0%, #fff3f4 100%);
					border-radius: 4px 20px 20px 20px;
					box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
					border: 1px solid #ffe0e3;
					display: flex;
					align-items: center;
					gap: 12px;

					.typing-animation {
						display: flex;
						align-items: center;
						gap: 6px;

						.dot {
							width: 10px;
							height: 10px;
							background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
							border-radius: 50%;
							animation: typingBounce 1.4s infinite ease-in-out both;

							&:nth-child(1) {
								animation-delay: -0.32s;
							}

							&:nth-child(2) {
								animation-delay: -0.16s;
							}
						}
					}

					.thinking-text {
						font-size: 0.929rem;
						color: #909399;
						font-weight: 500;
					}
				}
			}
		}

		.message-avatar {
			font-size: 42px;
			flex-shrink: 0;
			filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.15));
			line-height: 1;
		}

		// 自定义头像组件样式
		.message-avatar-custom {
			flex-shrink: 0;
			filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.15));

			:deep(.avatar-container) {
				padding: 8px; /* 适中的padding，让光晕完整显示 */
				overflow: visible; /* 确保光晕不被裁剪 */
			}

			:deep(.avatar-container::after) {
				/* 减小模糊范围，让光晕更集中 */
				filter: blur(12px); /* 从21px减小到12px */
				opacity: 0.8; /* 稍微降低不透明度，让光晕更柔和 */
			}

			:deep(.user-avatar) {
				border-width: 2px; /* 减小边框宽度 */
			}
		}

		.message-content {
			display: flex;
			flex-direction: column;
			gap: 8px;
			max-width: 75%;

			.message-images {
				display: flex;
				gap: 8px;
				margin-bottom: 4px;

				.message-image {
					border-radius: 8px;
					overflow: hidden;
					box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

					img {
						max-width: 150px;
						max-height: 150px;
						object-fit: cover;
						display: block;
					}
				}
			}

			.message-text {
				padding: 14px 18px;
				border-radius: 20px;
				line-height: 1.37; /* 进一步减小行高，让换行更紧凑 */
				transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
				font-size: 0.929rem /* 原值: 15px，调整为13px */;
				white-space: pre-wrap;
				word-break: break-word;
				display: inline;
				position: relative;

				.message-text-content {
					display: inline;
				}

				// 更多操作按钮（内联在文本末尾）
				:deep(.el-dropdown) {
					display: inline;
					vertical-align: middle;
				}

				.more-btn {
					display: inline;
					cursor: pointer;
					opacity: 0;
					transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1);
					color: inherit;
					vertical-align: middle;
					margin-left: 2px;
					user-select: none;

					&:active {
						transform: scale(0.95);
					}
				}

				&:hover .more-btn {
					opacity: 0.5;
				}

				.more-btn:hover {
					opacity: 1 !important;
				}

				// 下拉菜单样式
				:deep(.el-dropdown-menu__item) {
					display: flex;
					align-items: center;
					gap: 8px;
					padding: 8px 16px;

					.el-icon {
						font-size: 1.143rem /* 原值: 16px */;
					}
				}

				&:hover {
					transform: translateY(-2px) scale(1.01);
				}

				// Markdown样式
				&.markdown-content {
					// 极简换行符间距
					:deep(br) {
						display: block;
						content: "";
						margin: 0; /* 完全去除<br>的额外间距 */
					}

					// 标题
					:deep(h1),
					:deep(h2),
					:deep(h3) {
						margin: 8px 0 6px 0; /* 减小标题间距 */
						font-weight: 600;
						line-height: 1.5;
					}

					:deep(h1) {
						font-size: 1.286rem;
					}
					:deep(h2) {
						font-size: 1.143rem;
					}
					:deep(h3) {
						font-size: 1rem;
					}

					&:first-child {
						margin-top: 0;
					}

					// 段落
					:deep(p) {
						margin: 2px 0; /* 进一步减小段落间距 */
						line-height: 1.3; /* 与基础行高保持一致 */
					}
				}

				// 代码块
				:deep(pre) {
					background: rgba(0, 0, 0, 0.05);
					border-radius: 8px;
					padding: 8px; /* 减小内边距 */
					margin: 6px 0; /* 减小外边距 */
					overflow-x: auto;

					code {
						font-family: "Courier New", monospace;
						font-size: 0.857rem /* 原值: 13px，改为12px */;
						line-height: 1.4; /* 减小行高 */
						color: #333;
					}
				}

				// 行内代码
				:deep(code:not(pre code)) {
					background: rgba(0, 0, 0, 0.05);
					padding: 2px 5px; /* 稍微减小内边距 */
					border-radius: 4px;
					font-family: "Courier New", monospace;
					font-size: 0.85em; /* 稍微减小行内代码 */
				}

				// 粗体和斜体
				:deep(strong) {
					font-weight: 600;
				}

				:deep(em) {
					font-style: italic;
				}

				// 链接
				:deep(a) {
					color: inherit;
					text-decoration: underline;
					opacity: 0.8;

					&:hover {
						opacity: 1;
					}
				}

				// 列表
				:deep(ul),
				:deep(ol) {
					margin: 2px 0; /* 进一步减小列表间距 */
					padding-left: 18px; /* 稍微减小缩进 */
					line-height: 1.25; /* 与基础行高保持一致 */
				}

				:deep(li) {
					margin: 1px 0; /* 进一步减小列表项间距 */
				}

				// 表格样式
				:deep(table) {
					width: 100%;
					border-collapse: collapse;
					margin: 12px 0;
					font-size: 0.929rem;
					background: #ffffff;
					border-radius: 8px;
					overflow: hidden;
					box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

					th {
						background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
						color: #ffffff;
						font-weight: 600;
						padding: 10px 12px;
						text-align: left;
						font-size: 0.929rem;
					}

					td {
						padding: 10px 12px;
						border-bottom: 1px solid #f0f0f0;
						color: #333;
						line-height: 1.5;
					}

					tr:last-child td {
						border-bottom: none;
					}

					tr:hover {
						background-color: #f8f9fa;
					}
				}

				// 换行
				:deep(br) {
					line-height: 1.2; /* 减小空行高度 */
				}
			}

			.message-time {
				font-size: 0.857rem /* 原值: 12px */;
				color: #a8abb2;
				margin-top: 2px;
			}
		}
	}
}

// 统一的消息进度指示器样式
.message-progress-indicator {
	display: flex !important;
	align-items: center;
	gap: 8px;  // 减小间距
	padding: 0;  // 去掉内边距
	margin: 0;  // 去掉外边距
	border-radius: 0;  // 去掉圆角
	box-shadow: none;  // 去掉阴影
	border: none;  // 去掉边框
	background: transparent;  // 透明背景
	position: relative;
	z-index: 10;
	width: 100%;
	box-sizing: border-box;
	// 性能优化
	will-change: opacity, transform;
	transform: translateZ(0);

	.progress-avatar {
		display: none;  // 隐藏头像，因为外层已经有头像了
	}

	.progress-content {
		flex: 1;
		display: flex;
		flex-direction: row;  // 改为横向布局
		align-items: center;  // 垂直居中
		gap: 8px;  // 文字和点之间的间距

		.progress-icon {
			display: flex;
			align-items: center;
			justify-content: center;
			width: 20px;
			height: 20px;
			color: #0ea5e9;
			font-size: 18px;
			flex-shrink: 0;

			svg {
				width: 18px;
				height: 18px;
			}
		}

		.progress-text {
			font-size: 0.9rem;
			font-weight: 500;
			line-height: 1.5;
			flex: 1;
		}

		.progress-dots {
			display: flex;
			align-items: center;
			gap: 6px;

			.progress-dot {
				width: 6px;
				height: 6px;
				border-radius: 50%;
				animation: dotBounce 1.4s ease-in-out infinite;
				// GPU加速
				will-change: transform, opacity;
				transform: translateZ(0);

				&:nth-child(1) {
					animation-delay: 0s;
				}

				&:nth-child(2) {
					animation-delay: 0.2s;
				}

				&:nth-child(3) {
					animation-delay: 0.4s;
				}
			}
		}
	}

	// 思考状态样式
	&.status-thinking {
		.progress-text {
			color: #ff6b6b;
		}

		.progress-dot {
			background: #ff6b6b;
		}
	}

	// 执行状态样式
	&.status-executing {
		.progress-text {
			color: #667eea;
		}

		.progress-dot {
			background: #667eea;
		}
	}

	// 完成状态样式
	&.status-completed {
		background: linear-gradient(135deg, #43a047 0%, #66bb6a 100%);
		border: 1px solid #43a047;

		.progress-text {
			color: #ffffff;
		}

		.avatar-emoji {
			animation: successPulse 0.6s ease-out;
		}
	}
}

// 成功完成脉冲动画
@keyframes successPulse {
	0% {
		transform: scale(0.8);
		opacity: 0;
	}
	50% {
		transform: scale(1.1);
	}
	100% {
		transform: scale(1);
		opacity: 1;
	}
}

// 工具执行状态样式（保留用于向后兼容）
.tool-executing-status {
	display: inline-flex;
	align-items: center;
	gap: 8px;
	padding: 8px 16px;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #ffffff;
	border-radius: 20px;
	font-size: 0.929rem;
	margin-bottom: 12px;
	box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
	animation: toolStatusFadeIn 0.3s ease-out;
	// 性能优化
	will-change: opacity, transform;
	transform: translateZ(0);

	.el-icon {
		font-size: 1.143rem;
		animation: rotate 1.5s linear infinite;
		// GPU加速
		transform: translateZ(0);
	}

	span {
		font-weight: 500;
	}
}

// 工具执行完成通知
.tool-completed-notice {
	display: inline-flex;
	align-items: center;
	gap: 8px;
	padding: 8px 16px;
	background: linear-gradient(135deg, #43a047 0%, #66bb6a 100%);
	color: #ffffff;
	border-radius: 20px;
	font-size: 0.929rem;
	margin-bottom: 12px;
	box-shadow: 0 4px 12px rgba(67, 160, 71, 0.3);
	animation: toolStatusFadeIn 0.3s ease-out;
	// 性能优化
	will-change: opacity, transform;
	transform: translateZ(0);

	.el-icon {
		font-size: 1.143rem;
	}

	span {
		font-weight: 500;
	}
}

@keyframes toolStatusFadeIn {
	from {
		opacity: 0;
		transform: translateY(-8px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

@keyframes rotate {
	from {
		transform: rotate(0deg);
	}
	to {
		transform: rotate(360deg);
	}
}

@keyframes messageFadeIn {
	from {
		opacity: 0;
		transform: translateY(15px) scale(0.98);
	}
	to {
		opacity: 1;
		transform: translateY(0) scale(1);
	}
}

// 初始加载动画
@keyframes avatarFloat {
	0%,
	100% {
		transform: translateY(0);
	}
	50% {
		transform: translateY(-8px);
	}
}

@keyframes typingBounce {
	0%,
	60%,
	100% {
		transform: translateY(0);
	}
	30% {
		transform: translateY(-12px);
	}
}

@keyframes textPulse {
	0%,
	100% {
		opacity: 0.6;
	}
	50% {
		opacity: 1;
	}
}

// 淡入动画
.fade-in-enter-active,
.fade-in-leave-active {
	transition: opacity 0.3s ease-out;
	// 性能优化
	will-change: opacity;
}

.fade-in-enter-from,
.fade-in-leave-to {
	opacity: 0;
}

.fade-in-enter-to,
.fade-in-leave-from {
	opacity: 1;
}

@keyframes dropdownFadeIn {
	from {
		opacity: 0;
		transform: scale(0.95) translateY(-10px);
	}
	to {
		opacity: 1;
		transform: scale(1) translateY(0);
	}
}

@keyframes slideUpFadeIn {
	from {
		opacity: 0;
		transform: scaleY(0) translateY(10px);
	}
	to {
		opacity: 1;
		transform: scaleY(1) translateY(0);
	}
}

@keyframes slideInRight {
	from {
		opacity: 0;
		transform: scaleX(0) translateX(-10px);
	}
	to {
		opacity: 1;
		transform: scaleX(1) translateX(0);
	}
}

.bottom-container {
	flex-shrink: 0;
	display: flex;
	flex-direction: column;
	gap: 8px;
	position: relative; /* 为绝对定位的 emoji panel 提供定位上下文 */
}

// 卡片消息包装器
.card-message-wrapper {
	width: 100%;
	max-width: 600px;
	margin-bottom: 12px;
}

// 卡片总结文本样式
.card-summary-text {
	margin-top: 12px;
	padding: 12px 16px;
	background: #f8f9fa;
	border-left: 3px solid #667eea;
	border-radius: 4px;
	font-size: 14px;
	line-height: 1.6;
	color: #666;
}

// 打字机光标效果
.typing-cursor {
	display: inline-block;
	width: 2px;
	height: 1.2em;
	background: #667eea;
	margin-left: 2px;
	vertical-align: text-bottom;
	animation: cursorBlink 1s step-end infinite;
}

@keyframes cursorBlink {
	0%,
	50% {
		opacity: 1;
	}
	51%,
	100% {
		opacity: 0;
	}
}

// 打字中的消息文本样式
.message-text-content.typing,
.card-summary-text.typing {
	position: relative;

	&::after {
		content: "";
		display: inline-block;
		width: 2px;
		height: 1.2em;
		background: #667eea;
		margin-left: 2px;
		vertical-align: text-bottom;
		animation: cursorBlink 1s step-end infinite;
	}
}

// 快捷提问按钮包装器（提供相对定位参考）
.quick-question-button-wrapper {
	position: relative;
	display: inline-block;
}

// 快捷提问面板（定位到快捷提问按钮的右上角）
.quick-questions-panel-fixed {
	position: absolute;
	bottom: calc(100% + 4px); // 在按钮上方，保持4px间距
	left: calc(100% + 4px); // 在按钮右侧，保持4px间距
	width: 200px; // 固定宽度，只显示分类标题
	max-height: 400px; // 限制最大高度
	background: #ffffff;
	border: 1px solid #e8ecef;
	border-radius: 12px;
	padding: 16px;
	box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
	z-index: 9999;

	.quick-questions-title {
		display: flex;
		align-items: center;
		justify-content: space-between;
		font-size: 1rem;
		font-weight: 600;
		color: #303133;
		margin-bottom: 12px;
		padding-bottom: 10px;
		border-bottom: 2px solid #ff6b6b;

		.close-panel-icon {
			cursor: pointer;
			color: #909399;
			transition: all 0.2s ease;

			&:hover {
				color: #ff6b6b;
				transform: rotate(90deg);
			}
		}
	}

	.quick-questions-categories {
		display: flex;
		flex-direction: column;
		gap: 8px;

		.question-category {
			position: relative;
			display: flex;
			align-items: flex-start;

			.category-header {
				flex-shrink: 0;
				display: flex;
				align-items: center;
				justify-content: space-between;
				padding: 10px 14px;
				background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
				border: 1px solid #e8ecef;
				border-radius: 8px;
				cursor: pointer;
				user-select: none;
				transition: all 0.2s ease;
				width: 160px; // 固定宽度
				flex-shrink: 0;

				&:hover {
					background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
					border-color: #ff6b6b;
				}

				&.is-active {
					background: linear-gradient(135deg, #ff6b6b 0%, #ff8787 100%);
					border-color: #ff6b6b;
					color: #ffffff;

					.category-arrow {
						color: #ffffff;
					}
				}

				span {
					font-size: 0.857rem;
					font-weight: 600;
				}

				.category-arrow {
					color: #909399;
					transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

					&.is-expanded {
						transform: rotate(90deg);
					}
				}
			}

			.category-questions {
				position: absolute;
				left: 100%;
				top: 0;
				margin-left: 8px;
				display: flex;
				flex-direction: column;
				gap: 6px;
				width: 280px; // 固定宽度
				background: #ffffff;
				border: 1px solid #e8ecef;
				border-radius: 8px;
				padding: 8px;
				box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

				.question-item {
					padding: 10px 12px;
					font-size: 0.786rem;
					color: #606266;
					background: #f5f7fa;
					border-radius: 6px;
					cursor: pointer;
					transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
					text-align: left;
					line-height: 1.4;

					&:hover {
						background: linear-gradient(135deg, #409eff 0%, #5dade2 100%);
						color: #ffffff;
						transform: translateX(4px);
					}

					&:active {
						transform: translateX(2px);
					}
				}
			}
		}
	}
}

// 快捷提问面板淡入淡出动画
.fade-slide-enter-active,
.fade-slide-leave-active {
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
	opacity: 0;
	transform: translateY(-10px);
}

.fade-slide-leave-to {
	opacity: 0;
	transform: translateY(-10px);
}

// 二级问题向右展开动画
.slide-right-sub-enter-active {
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-right-sub-leave-active {
	transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-right-sub-enter-from {
	opacity: 0;
	transform: translateX(-10px);
}

.slide-right-sub-leave-to {
	opacity: 0;
	transform: translateX(-10px);
}

// 已上传图片预览
.uploaded-images-preview {
	display: flex;
	gap: 12px;
	padding: 12px;
	background-color: #f8f9fa;
	border-radius: 12px;

	.uploaded-image-item {
		position: relative;
		width: 100px;
		height: 100px;
		border-radius: 12px;
		overflow: hidden;
		box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

		img {
			width: 100%;
			height: 100%;
			object-fit: cover;
		}

		.remove-image-btn {
			position: absolute;
			top: 4px;
			right: 4px;
			width: 24px;
			height: 24px;
			min-height: 24px;
			padding: 0;
			background-color: rgba(0, 0, 0, 0.6);
			border: none;
			color: #fff;
			opacity: 0;
			transition: all 0.2s ease;

			&:hover {
				background-color: rgba(255, 107, 107, 0.9);
				transform: scale(1.1);
			}
		}

		&:hover .remove-image-btn {
			opacity: 1;
		}
	}
}

// 表情面板
.emoji-panel {
	position: absolute;
	bottom: 100%;
	left: 0;
	margin-bottom: 8px;
	background: #ffffff;
	border: 1px solid #e8ecef;
	border-radius: 8px;
	padding: 10px;
	box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
	max-height: 180px;
	overflow-y: auto;
	z-index: 1000; /* 提高 z-index 确保浮在最上层 */

	&::-webkit-scrollbar {
		width: 6px;
	}

	&::-webkit-scrollbar-thumb {
		background: #dee2e6;
		border-radius: 3px;

		&:hover {
			background: #adb5bd;
		}
	}

	.emoji-grid {
		display: grid;
		grid-template-columns: repeat(8, 1fr);
		gap: 4px;

		.emoji-item {
			font-size: 1.429rem /* 原值: 20px */;
			text-align: center;
			padding: 6px 4px;
			border-radius: 6px;
			cursor: pointer;
			transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
			user-select: none;

			&:hover {
				background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
				transform: scale(1.2);
			}

			&:active {
				transform: scale(1.05);
			}
		}
	}
}

// 表情面板动画
.slide-up-enter-active,
.slide-up-leave-active {
	transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-up-enter-from {
	opacity: 0;
	transform: translateY(12px);
}

.slide-up-leave-to {
	opacity: 0;
	transform: translateY(6px);
}

.fade-enter-active,
.fade-leave-active {
	transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
	opacity: 0;
}

// 隐藏文件输入框
.hidden-file-input {
	display: none;
}

.input-area {
	flex-shrink: 0;
	display: flex;
	gap: 12px;
	align-items: flex-end;
	background: linear-gradient(to bottom, #ffffff 0%, #fafbfc 100%);
	border: 1px solid #e8ecef;
	border-radius: 16px;
	padding: 16px 18px;
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
	transition: all 0.3s ease;
	// 性能优化：提示浏览器这个元素会发生变化
	will-change: box-shadow, border-color;
	// GPU加速
	transform: translateZ(0);

	&:hover {
		box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
		border-color: #e0e4e8;
	}

	.input-wrapper {
		flex: 1;
		display: flex;
		flex-direction: column;
		gap: 10px;
	}

	.toolbar {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 0 2px;
		margin-bottom: 6px;

		.toolbar-left {
			display: flex;
			gap: 8px;
			align-items: center;
		}

		.input-with-counter {
			flex: 1;
			display: flex;
			flex-direction: column;
			gap: 8px;
			margin-top: 8px;
		}

		.toolbar-divider {
			width: 1px;
			height: 20px;
			background: linear-gradient(to bottom, transparent, #e8ecef, transparent);
			margin: 0 2px;
		}

		// 开关样式优化
		:deep(.el-switch) {
			--el-switch-on-color: #ff6b6b;
			--el-switch-off-color: #dcdfe6;

			&.el-switch--small {
				height: 20px;

				.el-switch__core {
					height: 20px;
					min-width: 40px;
					border-radius: 10px;
					transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

					&::after {
						width: 16px;
						height: 16px;
						top: 1px;
						left: 1px;
						box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
						transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
					}

					&.is-checked::after {
						left: calc(100% - 17px);
					}
				}

				&:hover .el-switch__core {
					transform: scale(1.02);
				}
			}

			.el-switch__action {
				background-color: #fff;
			}

			.el-switch__label {
				font-size: 0.857rem /* 原值: 12px */;
				font-weight: 600;
				color: #606266;

				&.is-active {
					color: #ff6b6b;
				}
			}
		}

		:deep(.el-button) {
			border: 1px solid #e8ecef;
			background: #ffffff;
			color: #5a6c7d;
			transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
			font-weight: 500;
			width: 32px;
			height: 32px;
			padding: 0;
			// 性能优化：提示浏览器这个元素会发生变化
			will-change: transform, box-shadow, border-color;
			// GPU加速
			transform: translateZ(0);

			&:hover {
				border-color: #ff6b6b;
				color: #ff6b6b;
				transform: translateY(-2px) scale(1.05);
				box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
				background: #fff;
			}

			&:active {
				transform: translateY(0) scale(1);
			}

			&.is-active {
				border-color: #ff6b6b;
				color: #ff6b6b;
				background: linear-gradient(
					135deg,
					rgba(255, 107, 107, 0.1) 0%,
					rgba(255, 82, 82, 0.1) 100%
				);
				box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.15),
					0 2px 8px rgba(255, 107, 107, 0.2);

				&:hover {
					background: linear-gradient(
						135deg,
						rgba(255, 107, 107, 0.15) 0%,
						rgba(255, 82, 82, 0.15) 100%
					);
				}
			}
		}
	}

	.message-input {
		flex: 1;

		:deep(.el-textarea__inner) {
			border-radius: 10px;
			border: 2px solid #e8ecef;
			background: #ffffff;
			padding: 8px 12px;
			font-size: 1rem /* 原值: 14px */;
			line-height: 1.6;
			transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
			resize: none;
			// 性能优化：提示浏览器这个元素会发生变化
			will-change: border-color, box-shadow;
			// GPU加速
			transform: translateZ(0);

			&:focus {
				border-color: #ff6b6b;
				box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.12);
				background: #ffffff;
			}

			&:hover:not(:focus) {
				border-color: #d0d7de;
			}
		}
	}

	// 字数统计包装器
	.char-count-wrapper {
		display: flex;
		justify-content: flex-end;
		align-items: center;
		padding: 0 2px;
		margin-top: 4px;

		.char-count {
			font-size: 0.857rem /* 原值: 12px */;
			color: #909399;
			padding: 4px 12px;
			background: linear-gradient(135deg, #f5f7fa 0%, #eef1f6 100%);
			border-radius: 12px;
			font-weight: 600;
			border: 1px solid #e8ecef;
			transition: all 0.3s ease;
			display: inline-block;
			user-select: none;

			&:hover {
				background: linear-gradient(135deg, #eef1f6 0%, #e8ebf1 100%);
				border-color: #d0d7de;
				transform: translateY(-1px);
			}

			// 接近上限时的样式
			&.near-limit {
				color: #e6a23c;
				background: linear-gradient(135deg, #fef0e6 0%, #fde6d3 100%);
				border-color: #f5dab1;
			}

			// 达到上限时的样式
			&.at-limit {
				color: #f56c6c;
				background: linear-gradient(135deg, #fee 0%, #fecaca 100%);
				border-color: #fbc4c4;
				animation: pulse 1.5s ease-in-out infinite;
			}
		}
	}

	// 字数接近上限时的脉冲动画
	@keyframes pulse {
		0%,
		100% {
			opacity: 1;
		}
		50% {
			opacity: 0.7;
		}
	}

	// AI思考时的跳动动画
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

	.send-btn {
		flex-shrink: 0;
		background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
		border: none;
		padding: 10px 28px;
		font-size: 1.071rem /* 原值: 15px */;
		font-weight: 600;
		border-radius: 10px;
		box-shadow: 0 2px 8px rgba(255, 107, 107, 0.25);
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
		height: 56px;
		// 性能优化：提示浏览器这个元素会发生变化
		will-change: transform, box-shadow;
		// GPU加速
		transform: translateZ(0);

		&:hover:not(:disabled) {
			transform: translateY(-2px);
			box-shadow: 0 6px 16px rgba(255, 107, 107, 0.35);
		}

		&:active:not(:disabled) {
			transform: translateY(0);
		}

		&:disabled {
			background: #e9ecef;
			box-shadow: none;
			color: #adb5bd;
		}
	}
}

// fade-slide 过渡动画
.fade-slide-enter-active,
.fade-slide-leave-active {
	transition: all 0.25s ease;
}

.fade-slide-enter-from {
	opacity: 0;
	transform: translateY(-10px);
}

.fade-slide-leave-to {
	opacity: 0;
	transform: translateY(-10px);
}

// 卡片进入动画
.card-enter-active,
.card-leave-active {
	transition: all 0.3s ease;
}

.card-enter-from {
	opacity: 0;
	transform: translateY(-20px) scale(0.95);
}

.card-leave-to {
	opacity: 0;
	transform: translateY(20px) scale(0.95);
}
</style>
