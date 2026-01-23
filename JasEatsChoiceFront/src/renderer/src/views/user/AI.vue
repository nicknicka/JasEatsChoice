<script setup>
import { ref, onMounted, onUnmounted, nextTick } from "vue";
import {
	ChatRound,
	Camera,
	Document,
	Loading,
	Delete,
	Picture,
	ChatDotRound,
	Close,
} from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import axios from "axios";

// 从配置中导入API地址
import { API_CONFIG } from "../../config/index.js";

// 导入authStore获取用户ID
import { useAuthStore } from "../../store/authStore";

// 获取认证store
const authStore = useAuthStore();

// 常用问题快捷入口
const quickQuestions = ref([
	"推荐适合减肥的食谱",
	"今日卡路里摄入建议",
	"如何搭配营养均衡的饮食",
	"推荐低卡路里零食",
	"适合运动后的食物",
]);

// 快捷提问显示状态
const showQuickQuestions = ref(true);

// 表情选择器状态
const showEmojiPicker = ref(false);
const inputContainerRef = ref(null);

// 常用表情列表
const commonEmojis = [
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
];

// 上传的图片
const uploadedImages = ref([]);

// Chat messages
const messages = ref([]);

// User input for chat
const inputMessage = ref("");
const inputMaxLength = 500; // Maximum message length for chat

// Loading state for chat
const isLoading = ref(false);
const isStreaming = ref(false); // 流式传输状态
const abortController = ref(null); // 用于取消请求

// 获取用户ID
const getUserId = () => {
	return String(authStore.userId || "1");
};

// AI个性化数据开关状态（隐私保护原则：默认未授权）
const aiPersonalDataEnabled = ref(false);

// 加载用户偏好设置
const loadUserPreference = async () => {
	try {
		const userId = getUserId();
		console.log("📥 加载用户偏好设置，userId:", userId);

		const response = await axios.get(`${API_CONFIG.baseURL}/v1/users/${userId}/preferences`);

		if (response.data && response.data.data) {
			// 只有明确设置为 true 时才启用（隐私保护原则）
			aiPersonalDataEnabled.value = response.data.data.enableAiPersonalData === true;
			console.log("✅ 用户偏好加载成功:", aiPersonalDataEnabled.value);
		}
	} catch (error) {
		console.error("❌ 加载用户偏好失败:", error);
		// 失败时使用默认值（隐私保护原则：默认未授权）
		aiPersonalDataEnabled.value = false;
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
			messages.value = historyData.map((item, index) => ({
				id: index + 1,
				sender: item.sender, // 'user' 或 'ai'
				content: item.content,
				time: new Date(item.createTime).toLocaleTimeString([], {
					hour: "2-digit",
					minute: "2-digit",
				}),
				avatar: item.sender === "ai" ? "🤖" : "👤",
			}));
			console.log("✅ 成功加载聊天历史:", messages.value.length, "条消息");
		} else {
			// 没有历史记录，显示欢迎消息并保存到后端
			console.log("📭 没有历史记录，显示欢迎消息");
			const welcomeMessage = "您好！我是您的AI饮食助手。有什么可以帮您的吗？";
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
				},
			];
			console.log("💾 保存欢迎消息到后端");
			// 保存欢迎消息到后端
			await saveMessageToBackend("ai", welcomeMessage);
		}
	} catch (error) {
		console.error("❌ 加载聊天记录失败:", error);
		console.error("❌ 错误详情:", error.response?.data || error.message);
		// 加载失败时，显示欢迎消息
		messages.value = [
			{
				id: 1,
				sender: "ai",
				content: "您好！我是您的AI饮食助手。有什么可以帮您的吗？",
				time: new Date().toLocaleTimeString([], {
					hour: "2-digit",
					minute: "2-digit",
				}),
				avatar: "🤖",
			},
		];
	}
};

// 保存消息到后端
const saveMessageToBackend = async (sender, content) => {
	try {
		const userId = getUserId();
		await axios.post(API_CONFIG.baseURL + API_CONFIG.ai.save, {
			userId,
			sender, // 'user' 或 'ai'
			content,
		});
		console.log("✅ 消息已保存到后端:", sender);
	} catch (error) {
		console.error("❌ 保存消息到后端失败:", error);
	}
};

// 清空聊天记录
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

				// 调用后端API清空聊天记录
				const clearResponse = await axios.delete(
					API_CONFIG.baseURL + API_CONFIG.ai.clear,
					{
						params: { userId },
					}
				);
				console.log("📡 后端清空响应:", clearResponse.data);

				// 检查后端是否成功清空
				if (clearResponse.data.code === 200) {
					console.log("✅ 后端清空成功");

					// 清空前端显示
					messages.value = [];

					// 重新加载消息（会显示欢迎消息并保存到后端）
					await loadMessages();

					console.log("✅ 前端已重新加载消息");
					ElMessage.success("聊天记录已清空");

					// 清空后滚动到顶部
					nextTick(() => {
						const chatContainer = document.querySelector(".chat-messages");
						if (chatContainer) {
							chatContainer.scrollTop = 0;
						}
					});
				} else {
					// 后端返回错误码
					console.error("❌ 后端清空失败，响应码:", clearResponse.data.code);
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

// 滚动到底部函数
const scrollToBottom = (smooth = true) => {
	nextTick(() => {
		const chatContainer = document.querySelector(".chat-messages");
		if (chatContainer) {
			if (smooth) {
				chatContainer.scrollTo({
					top: chatContainer.scrollHeight,
					behavior: "smooth",
				});
			} else {
				chatContainer.scrollTop = chatContainer.scrollHeight;
			}
		}
	});
};

// 流式传输：逐块读取AI回复
// 前端职责：接收后端发来的 content 和 done 两个字段，追加文本内容
const streamResponse = async (messageIndex, reader) => {
	isStreaming.value = true;
	messages.value[messageIndex].content = "";

	const decoder = new TextDecoder();
	let buffer = "";

	try {
		while (true) {
			const { done, value } = await reader.read();
			if (done) break;
			const chunk = decoder.decode(value, { stream: true });
			// console.log("chunk:", chunk);
			buffer += chunk;
			const lines = buffer.split("\n");
			buffer = lines.pop() || "";

			for (const line of lines) {
				const trimmedLine = line.trim();
				if (!trimmedLine.startsWith("data:")) continue;

				const data = trimmedLine.substring(5).trim();
				if (!data) continue;

				try {
					// 解析SSE数据（可能是数组格式或直接的对象）
					let parsedData;

					if (data.startsWith("[")) {
						// Spring Boot的SseEmitter数组格式：[{...}, {...}, {...}]
						const dataArray = JSON.parse(data);

						// 查找mediaType为null的元素（包含实际数据）
						const actualDataItem = dataArray.find(
							(item) => item.mediaType === null
						);

						if (actualDataItem && actualDataItem.data) {
							parsedData = actualDataItem.data;
						}
					} else {
						// 直接的对象格式：{ content: string, done: boolean }
						parsedData = JSON.parse(data);
					}

					if (!parsedData) continue;

					// 接收 done 字段：检查是否结束
					if (parsedData.done === true) {
						console.log("✅ 接收完成");

						// 保存AI的完整回复到后端
						const aiContent = messages.value[messageIndex].content;
						if (aiContent) {
							await saveMessageToBackend("ai", aiContent);
						}

						return;
					}

					// 接收 content 字段：追加文本
					if (parsedData.content) {
						messages.value[messageIndex].content += parsedData.content;
						await nextTick();
						scrollToBottom(false);
					}
				} catch (error) {
					console.log("⚠️ 跳过无效数据:", data);
				}
			}
		}
	} catch (error) {
		// 用户主动取消，不显示错误日志
		if (error.name === 'AbortError') {
			console.log("ℹ️ 用户主动停止流式传输");
			return;
		}
		// 其他错误正常处理
		console.error("❌ 流式传输错误:", error);
		throw error;
	} finally {
		isStreaming.value = false;
	}
};

// Tab selection - AI聊天已设置为默认
const activeTab = ref("chat");

// AI Dish Recognition
const recognitionResult = ref(null);
const recognitionLoading = ref(false);
const recognitionProgress = ref(0); // 识别进度
const selectedImage = ref(null);
const imageMaxSize = 10 * 1024 * 1024; // 10MB maximum image size
const isDragging = ref(false); // 拖拽状态

// 拖拽上传处理
const handleDragOver = (event) => {
	event.preventDefault();
	isDragging.value = true;
};

const handleDragLeave = (event) => {
	event.preventDefault();
	isDragging.value = false;
};

const handleDrop = (event) => {
	event.preventDefault();
	isDragging.value = false;

	const file = event.dataTransfer.files[0];
	if (file) {
		// Validate file type
		if (!file.type.startsWith("image/")) {
			ElMessage.error("请选择图片文件");
			return;
		}

		// Validate file size
		if (file.size > imageMaxSize) {
			ElMessage.error("图片大小不能超过10MB");
			return;
		}

		selectedImage.value = URL.createObjectURL(file);
		recognitionResult.value = null; // Clear previous result
		ElMessage.success("图片上传成功");
	}
};

// 重新识别
const reRecognize = () => {
	recognitionResult.value = null;
	recognizeDish();
};

// AI Recipe Optimization
const originalRecipe = ref("");
const optimizedRecipe = ref(null);
const optimizationLoading = ref(false);
const recipeMinLength = 20; // Minimum recipe length
const recipeMaxLength = 10000; // Maximum recipe length

// Image upload handling
const handleImageUpload = (event) => {
	const file = event.target.files[0];
	if (file) {
		// Validate file type
		if (!file.type.startsWith("image/")) {
			ElMessage.error("请选择图片文件");
			event.target.value = ""; // Clear the input to allow reselect
			return;
		}

		// Validate file size
		if (file.size > imageMaxSize) {
			ElMessage.error("图片大小不能超过10MB");
			event.target.value = ""; // Clear the input to allow reselect
			return;
		}

		selectedImage.value = URL.createObjectURL(file);
		recognitionResult.value = null; // Clear previous result
		ElMessage.success("图片上传成功");
	}
};

// New method to handle image upload click
const handleUploadClick = () => {
	const input = document.getElementById("image-upload");
	if (input) {
		input.click();
	}
};

// Simulate AI dish recognition
const recognizeDish = () => {
	if (!selectedImage.value) {
		return;
	}

	recognitionLoading.value = true;
	recognitionProgress.value = 0;

	// 模拟进度条
	const progressInterval = setInterval(() => {
		if (recognitionProgress.value < 90) {
			recognitionProgress.value += 10;
		}
	}, 150);

	// Mock AI recognition
	setTimeout(() => {
		clearInterval(progressInterval);
		recognitionProgress.value = 100;
		recognitionResult.value = {
			name: "宫保鸡丁",
			ingredients: ["鸡肉", "花生米", "辣椒", "黄瓜", "胡萝卜"],
			calories: 450,
			protein: 28,
			fat: 18,
			carbs: 15,
			difficulty: "中等",
			preparationTime: "25分钟",
			tags: ["川菜", "经典", "蛋白质丰富"],
			nutritionScore: 85,
		};
		recognitionLoading.value = false;
		ElMessage.success("识别成功！");
	}, 2000);
};

// Simulate AI recipe optimization
const optimizeRecipe = () => {
	// Validate recipe content
	const trimmedRecipe = originalRecipe.value.trim();
	if (!trimmedRecipe) {
		ElMessage.warning("请输入食谱");
		return;
	}
	if (trimmedRecipe.length < recipeMinLength) {
		ElMessage.warning(`食谱长度不能少于${recipeMinLength}个字符`);
		return;
	}
	if (trimmedRecipe.length > recipeMaxLength) {
		ElMessage.warning(`食谱长度不能超过${recipeMaxLength}个字符`);
		return;
	}

	optimizationLoading.value = true;

	// Call backend API for recipe optimization
	axios
		.post(API_CONFIG.baseURL + API_CONFIG.ai.recipe, {
			foodName: originalRecipe.value,
		})
		.then((response) => {
			// Format the backend response into the expected structure
			const backendRecipes = response.data.data;
			// For simplicity, take the first recipe as the optimized result
			if (backendRecipes && backendRecipes.length > 0) {
				const firstRecipe = backendRecipes[0];
				optimizedRecipe.value = {
					original: originalRecipe.value,
					optimized: `推荐食谱：${firstRecipe.name}
难度：${firstRecipe.difficulty}
卡路里：${firstRecipe.calorie}大卡
食材：${firstRecipe.ingredients}
步骤：${firstRecipe.steps}`,
					improvements: ["营养均衡", "口味优化", "步骤简化"],
				};
			} else {
				// No recipes returned from backend
				optimizedRecipe.value = {
					original: originalRecipe.value,
					optimized: `优化失败：没有找到合适的优化食谱。`,
					improvements: [],
				};
			}
		})
		.catch((error) => {
			console.error("食谱优化接口调用失败:", error);
			let errorMsg = `优化失败：无法获取AI优化建议。`;

			// Add more specific error messages
			if (error.response) {
				// Server responded with error status code
				if (error.response.status === 404) {
					errorMsg = "食谱优化服务暂时不可用，请稍后重试。";
				} else if (error.response.status === 500) {
					errorMsg = "服务器内部错误，请稍后重试。";
				}
			} else if (error.request) {
				// No response received from server
				errorMsg = "网络连接超时，请检查网络设置。";
			}

			optimizedRecipe.value = {
				original: originalRecipe.value,
				optimized: errorMsg,
				improvements: [],
			};
			ElMessage.error(errorMsg);
		})
		.finally(() => {
			optimizationLoading.value = false;
		});
};

// 清空所有输入内容（文本 + 图片）
const clearAllInput = () => {
	inputMessage.value = "";
	uploadedImages.value = [];
	ElMessage.success("已清空");
};

// 切换表情面板
const toggleEmoji = () => {
	showEmojiPicker.value = !showEmojiPicker.value;
};

// 选择表情
const selectEmoji = (emoji) => {
	inputMessage.value += emoji;
	showEmojiPicker.value = false;
	nextTick(() => {
		// 聚焦回输入框
		const textarea = document.querySelector(".message-textarea textarea");
		if (textarea) textarea.focus();
	});
};

// 点击外部区域关闭表情面板
const handleClickOutside = (event) => {
	if (inputContainerRef.value && !inputContainerRef.value.contains(event.target)) {
		showEmojiPicker.value = false;
	}
};

onMounted(() => {
	activeTab.value = "chat";
	loadMessages(); // 加载聊天历史记录
	loadUserPreference(); // 加载用户偏好设置
	document.addEventListener("click", handleClickOutside);
	// 组件加载后滚动到底部
	scrollToBottom(false);
});

onUnmounted(() => {
	document.removeEventListener("click", handleClickOutside);
});

// 处理聊天图片上传
const handleChatImageUpload = async (event) => {
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

	try {
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
	} catch (error) {
		ElMessage.error("图片上传失败");
	}

	// 清空input，允许重复上传同一张图片
	event.target.value = "";
};

// 移除上传的图片
const removeUploadedImage = (imageId) => {
	const index = uploadedImages.value.findIndex((img) => img.id === imageId);
	if (index > -1) {
		uploadedImages.value.splice(index, 1);
	}
};

// Handle keydown event for textarea
const handleKeyDown = (event) => {
	// Shift+Enter for newline, Enter to send
	if (event.key === "Enter" && !event.shiftKey) {
		event.preventDefault();
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

// Send message to AI
const sendMessage = async () => {
	// Validate message content
	const trimmedMsg = inputMessage.value.trim();
	if (!trimmedMsg) {
		ElMessage.warning("请输入问题");
		return;
	}
	if (trimmedMsg.length > inputMaxLength) {
		ElMessage.warning(`消息长度不能超过${inputMaxLength}个字符`);
		return;
	}

	// ========== 日志记录：请求开始 ==========
	const requestStartTime = Date.now();
	console.log("==================== AI聊天请求开始 ====================");
	console.log("⏰ 请求时间:", new Date().toLocaleString());
	console.log("📝 用户消息:", trimmedMsg);
	console.log("📏 消息长度:", trimmedMsg.length, "字符");
	console.log("📊 当前消息数量:", messages.value.length);

	// Add user message
	const userMsg = {
		id: messages.value.length + 1,
		sender: "user",
		content: trimmedMsg,
		time: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
		avatar: "👤",
	};
	messages.value.push(userMsg);
	const userInput = trimmedMsg;
	inputMessage.value = "";

	// 保存用户消息到后端
	await saveMessageToBackend("user", trimmedMsg);

	// 滚动到底部（用户消息发送后）
	scrollToBottom(true);

	// Call backend AI API
	isLoading.value = true;

	// 创建AI消息对象（初始为空）
	const aiResponse = {
		id: messages.value.length + 1,
		sender: "ai",
		content: "",
		time: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
		avatar: "🤖",
	};
	messages.value.push(aiResponse);

	// 保存AI消息的索引，用于后续更新
	const aiMessageIndex = messages.value.length - 1;

	// 再次滚动到底部，确保AI消息气泡可见
	scrollToBottom(false);

	// ========== 日志记录：API调用 ==========
	const apiUrl = API_CONFIG.baseURL + API_CONFIG.ai.chat;
	console.log("🌐 发送流式API请求到:", apiUrl);
	console.log("📦 请求体:", { message: userInput });

	// 创建新的AbortController用于取消请求
	abortController.value = new AbortController();

	try {
		// 使用fetch API发起流式请求
		const response = await fetch(apiUrl, {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				Accept: "text/event-stream",
			},
			body: JSON.stringify({ message: userInput }),
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
			return;  // 直接返回，不执行后续错误处理
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
</script>

<template>
	<div class="app-container">
		<div class="main-content">
			<!-- Right Content Area -->
			<el-main class="content-area">
				<div class="ai-chat-container">
					<div class="chat-header">
						<h2>AI饮食助手</h2>
						<div class="chat-info">
							<el-tag type="success">在线</el-tag>
						</div>
					</div>

					<!-- Tab Menu -->
					<el-tabs v-model="activeTab" type="border-card" class="ai-tabs">
						<el-tab-pane label="AI聊天" name="chat" :icon="ChatRound">
							<div class="chat-content-wrapper">
								<!-- 聊天消息区域 - flex: 1 -->
								<div class="chat-messages">
									<div
										v-for="message in messages"
										:key="message.id"
										class="chat-message"
										:class="{
											'user-message': message.sender === 'user',
											'ai-message': message.sender === 'ai',
										}"
									>
										<div class="message-avatar">
											{{ message.avatar }}
										</div>
										<div class="message-content">
											<div class="message-text">
												{{ message.content }}
											</div>
											<div class="message-time">
												{{ message.time }}
											</div>
										</div>
									</div>
								</div>

								<!-- 底部输入区域容器 -->
								<div class="bottom-input-container">
									<!-- 快捷提问区域 - 可关闭 -->
									<transition name="slide-down">
										<div
											v-if="showQuickQuestions"
											class="quick-questions-panel"
										>
											<div class="quick-questions-header">
												<span class="quick-questions-title"
													>💡 快捷提问</span
												>
												<el-button
													:icon="Close"
													circle
													size="small"
													text
													@click="showQuickQuestions = false"
												/>
											</div>
											<div class="quick-questions-list">
												<el-tag
													v-for="q in quickQuestions"
													:key="q"
													@click="inputMessage = q"
													class="question-tag"
													type="info"
													effect="plain"
												>
													{{ q }}
												</el-tag>
											</div>
										</div>
									</transition>

									<!-- 输入框区域 -->
									<div
										class="message-input-container"
										ref="inputContainerRef"
									>
										<div class="input-wrapper">
											<!-- 工具栏 -->
											<div class="toolbar">
												<div class="toolbar-left">
													<!-- 表情按钮 -->
													<el-tooltip
														content="表情"
														placement="top"
													>
														<el-button
															:icon="ChatDotRound"
															circle
															size="small"
															@click="toggleEmoji"
															:class="{
																'is-active': showEmojiPicker,
															}"
														/>
													</el-tooltip>

													<!-- 图片上传按钮 -->
													<input
														type="file"
														accept="image/*"
														@change="handleChatImageUpload"
														style="display: none"
														ref="chatImageInput"
													/>
													<el-tooltip
														content="上传图片"
														placement="top"
													>
														<el-button
															:icon="Picture"
															circle
															size="small"
															@click="
																$refs.chatImageInput.click()
															"
														/>
													</el-tooltip>

													<div class="toolbar-divider"></div>

													<!-- 清空输入按钮 -->
													<el-tooltip
														content="清空输入"
														placement="top"
													>
														<el-button
															:icon="Delete"
															circle
															size="small"
															@click="clearAllInput"
														/>
													</el-tooltip>
												</div>
												<div class="toolbar-right">
													<!-- AI个性化数据开关 -->
													<el-tooltip
														content="开启后AI将使用您的个人数据提供个性化建议"
														placement="bottom"
													>
														<el-switch
															v-model="aiPersonalDataEnabled"
															active-text="个性化"
															inactive-text="通用"
															@change="handlePersonalDataToggle"
															size="small"
															style="margin-right: 8px"
														/>
													</el-tooltip>

													<!-- 清空对话记录按钮 -->
													<el-button
														link
														type="danger"
														@click="clearChat"
													>
														🗑️ 清空对话
													</el-button>

													<el-button
														v-if="!showQuickQuestions"
														link
														type="primary"
														@click="showQuickQuestions = true"
													>
														💡 快捷提问
													</el-button>
												</div>
											</div>

											<!-- 表情面板 -->
											<transition name="slide-up">
												<div
													v-if="showEmojiPicker"
													class="emoji-panel"
												>
													<div class="emoji-grid">
														<span
															v-for="emoji in commonEmojis"
															:key="emoji"
															class="emoji-item"
															@click="selectEmoji(emoji)"
														>
															{{ emoji }}
														</span>
													</div>
												</div>
											</transition>

											<!-- 已上传图片预览 -->
											<div
												v-if="uploadedImages.length > 0"
												class="uploaded-images-preview"
											>
												<div
													v-for="img in uploadedImages"
													:key="img.id"
													class="uploaded-image-item"
												>
													<img
														:src="img.url"
														alt="上传的图片"
													/>
													<el-button
														:icon="Delete"
														circle
														size="small"
														class="remove-image-btn"
														@click="
															removeUploadedImage(img.id)
														"
													/>
												</div>
											</div>

											<!-- 输入框和发送按钮 -->
											<div class="input-area">
												<el-input
													v-model="inputMessage"
													placeholder="请输入您的问题...（例如：推荐适合减肥的食谱）"
													clearable
													resize="none"
													:rows="2"
													type="textarea"
													@keydown="handleKeyDown"
													maxlength="500"
													show-word-limit
													class="message-textarea"
												/>
												<el-button
													:type="
														isStreaming ? 'danger' : 'primary'
													"
													class="send-btn"
													@click="
														isStreaming
															? stopStreaming()
															: sendMessage()
													"
													:disabled="isLoading && !isStreaming"
													:icon="
														isStreaming ? Close : ChatRound
													"
												>
													{{ isStreaming ? "停止" : "发送" }}
												</el-button>
											</div>
										</div>
									</div>
								</div>
							</div>
						</el-tab-pane>

						<el-tab-pane label="菜品识别" name="recognition" :icon="Camera">
							<div class="recognition-section">
								<div class="upload-area">
									<input
										type="file"
										accept="image/*"
										style="display: none"
										id="image-upload"
										@change="handleImageUpload"
									/>
									<div
										class="upload-zone"
										:class="{
											'has-image': selectedImage,
											'is-dragging': isDragging,
										}"
										@click="handleUploadClick"
										@dragover="handleDragOver"
										@dragleave="handleDragLeave"
										@drop="handleDrop"
									>
										<div
											v-if="!selectedImage"
											class="upload-placeholder"
										>
											<el-icon :size="48"><Camera /></el-icon>
											<p class="upload-text">
												点击或拖拽上传菜品图片
											</p>
											<p class="upload-hint">
												支持 JPG、PNG 格式，最大 10MB
											</p>
										</div>
										<div v-else class="image-preview">
											<img :src="selectedImage" alt="菜品图片" />
											<div class="image-overlay">
												<el-button
													type="danger"
													size="small"
													@click.stop="selectedImage = null"
												>
													<el-icon><Delete /></el-icon>
													删除图片
												</el-button>
											</div>
										</div>
									</div>
								</div>

								<!-- 识别进度条 -->
								<div
									v-if="recognitionLoading"
									class="recognition-progress"
								>
									<el-progress
										:percentage="recognitionProgress"
										:stroke-width="12"
									/>
									<p class="progress-text">正在识别菜品，请稍候...</p>
								</div>

								<div class="recognition-buttons">
									<el-button
										type="primary"
										size="large"
										class="recognize-btn"
										@click="recognizeDish"
										:disabled="!selectedImage || recognitionLoading"
									>
										<el-icon v-if="recognitionLoading"
											><Loading
										/></el-icon>
										{{
											recognitionLoading
												? "识别中..."
												: "🔍 开始识别菜品"
										}}
									</el-button>

									<el-button
										v-if="recognitionResult"
										type="success"
										size="large"
										class="re-recognize-btn"
										@click="reRecognize"
										:disabled="recognitionLoading"
									>
										🔄 重新识别
									</el-button>
								</div>

								<div v-if="recognitionResult" class="recognition-result">
									<div class="result-header">
										<h4>✨ 识别结果</h4>
									</div>
									<div class="result-cards">
										<div class="result-card main-card">
											<div class="card-label">菜品名称</div>
											<div class="card-value">
												{{ recognitionResult.name }}
											</div>
										</div>
										<div class="result-card calories-card">
											<div class="card-label">🔥 卡路里</div>
											<div class="card-value highlight">
												{{ recognitionResult.calories }} kcal
											</div>
										</div>
										<div class="result-card">
											<div class="card-label">👨‍🍳 难度</div>
											<div class="card-value">
												{{ recognitionResult.difficulty }}
											</div>
										</div>
										<div class="result-card">
											<div class="card-label">⏱️ 烹饪时间</div>
											<div class="card-value">
												{{ recognitionResult.preparationTime }}
											</div>
										</div>

										<!-- 营养成分图表 -->
										<div
											class="result-card full-width nutrition-card"
										>
											<div class="card-label">📊 营养成分</div>
											<div class="nutrition-chart">
												<div class="nutrition-item">
													<div class="nutrition-label">
														<span class="nutrition-icon"
															>💪</span
														>
														<span>蛋白质</span>
													</div>
													<div class="nutrition-bar">
														<div
															class="nutrition-fill protein"
															:style="{
																width:
																	recognitionResult.protein +
																	'%',
															}"
														></div>
													</div>
													<div class="nutrition-value">
														{{ recognitionResult.protein }}g
													</div>
												</div>
												<div class="nutrition-item">
													<div class="nutrition-label">
														<span class="nutrition-icon"
															>🧈</span
														>
														<span>脂肪</span>
													</div>
													<div class="nutrition-bar">
														<div
															class="nutrition-fill fat"
															:style="{
																width:
																	recognitionResult.fat +
																	'%',
															}"
														></div>
													</div>
													<div class="nutrition-value">
														{{ recognitionResult.fat }}g
													</div>
												</div>
												<div class="nutrition-item">
													<div class="nutrition-label">
														<span class="nutrition-icon"
															>🍞</span
														>
														<span>碳水</span>
													</div>
													<div class="nutrition-bar">
														<div
															class="nutrition-fill carbs"
															:style="{
																width:
																	recognitionResult.carbs +
																	'%',
															}"
														></div>
													</div>
													<div class="nutrition-value">
														{{ recognitionResult.carbs }}g
													</div>
												</div>
											</div>
										</div>

										<div class="result-card full-width">
											<div class="card-label">🥗 主要食材</div>
											<div class="card-value">
												<el-tag
													v-for="ingredient in recognitionResult.ingredients"
													:key="ingredient"
													class="ingredient-tag"
												>
													{{ ingredient }}
												</el-tag>
											</div>
										</div>
										<div class="result-card full-width">
											<div class="card-label">🏷️ 标签</div>
											<div class="card-value">
												<el-tag
													v-for="tag in recognitionResult.tags"
													:key="tag"
													type="success"
													class="tag-item"
												>
													{{ tag }}
												</el-tag>
											</div>
										</div>
									</div>
								</div>
							</div>
						</el-tab-pane>

						<el-tab-pane label="食谱优化" name="recipe" :icon="Document">
							<div class="recipe-section">
								<div class="recipe-input">
									<el-input
										v-model="originalRecipe"
										placeholder="请输入您的食谱...&#10;例如：西红柿鸡蛋的做法：1. 准备西红柿2个，鸡蛋2个；2. 煎鸡蛋；3. 炒西红柿；4. 混合翻炒"
										clearable
										resize="vertical"
										:rows="6"
										type="textarea"
										maxlength="10000"
										show-word-limit
									/>
								</div>

								<el-button
									type="primary"
									size="large"
									class="optimize-btn"
									@click="optimizeRecipe"
									:disabled="!originalRecipe || optimizationLoading"
								>
									<el-icon v-if="optimizationLoading"
										><Loading
									/></el-icon>
									{{
										optimizationLoading
											? "优化中..."
											: "✨ 开始优化食谱"
									}}
								</el-button>

								<div v-if="optimizedRecipe" class="recipe-result">
									<div class="result-header">
										<h4>✨ 优化结果</h4>
									</div>

									<div class="recipe-comparison">
										<div class="recipe-card original-recipe-card">
											<div class="card-header">
												<span class="card-title">📝 原食谱</span>
											</div>
											<div class="card-content">
												<pre>{{ optimizedRecipe.original }}</pre>
											</div>
										</div>

										<div class="recipe-arrow">→</div>

										<div class="recipe-card optimized-recipe-card">
											<div class="card-header">
												<span class="card-title">⭐ 优化后</span>
											</div>
											<div class="card-content">
												<pre>{{ optimizedRecipe.optimized }}</pre>
											</div>
										</div>
									</div>

									<div class="improvements-section">
										<div class="improvements-title">🎯 优化亮点</div>
										<div class="improvements-tags">
											<el-tag
												v-for="improvement in optimizedRecipe.improvements"
												:key="improvement"
												size="large"
												type="success"
												effect="plain"
											>
												{{ improvement }}
											</el-tag>
										</div>
									</div>
								</div>
							</div>
						</el-tab-pane>
					</el-tabs>
				</div>
			</el-main>
		</div>
	</div>
</template>

<style scoped lang="less">
.app-container {
	height: 100vh;
	display: flex;
	flex-direction: column;
}

.top-nav-bar {
	background-color: #fff;
	box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 20px;
}

.logo {
	font-size: 24px;
	font-weight: bold;
	color: #ff6b6b;
}

.search-input {
	width: 400px;
}

.user-info {
	display: flex;
	align-items: center;
	gap: 10px;
	font-size: 16px;
}

.main-content {
	display: flex;
	flex: 1;
	overflow: hidden;
}

.sidebar-menu {
	background-color: #f0f2f5;
	border-right: 1px solid #e6e8eb;
	padding: 20px 0;
	display: flex;
	flex-direction: column;

	.avatar-section {
		text-align: center;
		padding-bottom: 20px;
		border-bottom: 1px solid #e6e8eb;
		margin-bottom: 20px;
	}

	.menu-list {
		border: none;
		flex: 1;
	}

	.setting-menu {
		border-top: 1px solid #e6e8eb;
		margin-top: auto;
		width: 100%;
	}
}

.content-area {
	padding: 20px 20px 0 20px; /* 移除底部padding */
	background-color: #fafafa;
	overflow-y: auto;
	height: 100%;
	box-sizing: border-box;
	display: flex;
	flex-direction: column;
}

.ai-chat-container {
	height: 100%;
	display: flex;
	flex-direction: column;
	max-width: 900px;
	margin: 0 auto;
	padding-bottom: 0; /* 确保容器底部没有padding */

	.chat-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 24px;
		padding-bottom: 16px;
		border-bottom: 2px solid #f0f0f0;

		h2 {
			font-size: 26px;
			font-weight: 700;
			margin: 0;
			background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
			-webkit-background-clip: text;
			-webkit-text-fill-color: transparent;
			background-clip: text;
		}

		.chat-info {
			display: flex;
			gap: 12px;
			align-items: center;
		}
	}

	.ai-tabs {
		flex: 1;
		display: flex;
		flex-direction: column;
		border-radius: 16px;
		overflow: hidden;
		box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
		height: 100%; /* 确保tabs容器有明确高度 */

		:deep(.el-tabs__header) {
			margin: 0;
			background: linear-gradient(135deg, #fff9fa 0%, #fff 100%);
			border-bottom: 2px solid #ffe0e3;
			flex-shrink: 0; /* 防止头部被压缩 */
		}

		:deep(.el-tabs__nav) {
			border: none;
		}

		:deep(.el-tabs__item) {
			font-size: 15px;
			font-weight: 600;
			color: #606266;
			transition: all 0.3s ease;

			&:hover {
				color: #ff6b6b;
			}

			&.is-active {
				color: #ff6b6b;
				background: linear-gradient(135deg, #ffe8e8 0%, #fff 100%);
			}
		}

		:deep(.el-tabs__content) {
			flex: 1;
			overflow: hidden;
			padding: 0 !important; /* 完全移除padding */
			display: flex;
			flex-direction: column;
		}

		:deep(.el-tab-pane) {
			height: 100%;
			display: flex;
			flex-direction: column;
			padding: 8px 0 0 0 !important; /* 只给顶部少量padding */
		}
	}

	/* 聊天内容包装器 - flex布局 */
	.chat-content-wrapper {
		display: flex;
		flex-direction: column;
		height: 100%;
		gap: 8px; /* 减小gap */
		overflow: hidden; /* 防止内容溢出 */
	}

	.chat-messages {
		/* flex: 1 占据剩余空间 */
		flex: 1;
		min-height: 0; /* 重要：允许flex子元素滚动 */
		overflow-y: auto;
		background-color: #fff;
		border-radius: 16px;
		padding: 24px;
		box-shadow: 0 2px 16px 0 rgba(0, 0, 0, 0.06);

		.chat-message {
			display: flex;
			gap: 12px;
			margin-bottom: 24px;
			animation: messageFadeIn 0.4s ease-out;

			&.user-message {
				flex-direction: row-reverse;
				justify-content: flex-start;

				.message-content {
					align-items: flex-end;

					.message-text {
						background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
						color: #fff;
						border-radius: 20px 20px 4px 20px;
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
						border-radius: 20px 20px 20px 4px;
						box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
						border: 1px solid #ffe0e3;
					}
				}
			}

			.message-avatar {
				font-size: 42px;
				flex-shrink: 0;
				filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.15));
				line-height: 1;
			}

			.message-content {
				display: flex;
				flex-direction: column;
				gap: 6px;

				.message-text {
					max-width: 75%;
					padding: 14px 18px;
					border-radius: 20px;
					line-height: 1.7;
					transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
					font-size: 15px;

					&:hover {
						transform: translateY(-2px) scale(1.01);
					}
				}

				.message-time {
					font-size: 12px;
					color: #a8abb2;
					margin-top: 2px;
				}
			}
		}
	}

	/* 底部输入容器 */
	.bottom-input-container {
		flex-shrink: 0;
		display: flex;
		flex-direction: column;
		gap: 6px; /* 减小gap */
	}

	/* 快捷提问面板 */
	.quick-questions-panel {
		background: linear-gradient(135deg, #f0f9ff 0%, #e8f4fd 100%);
		border: 1px solid #d1e9ff;
		border-radius: 12px;
		padding: 12px 16px;
		box-shadow: 0 2px 8px rgba(64, 158, 255, 0.08);

		.quick-questions-header {
			display: flex;
			justify-content: space-between;
			align-items: center;
			margin-bottom: 10px;

			.quick-questions-title {
				font-size: 14px;
				font-weight: 600;
				color: #2c7be5;
			}
		}

		.quick-questions-list {
			display: flex;
			flex-wrap: wrap;
			gap: 8px;

			.question-tag {
				margin: 0;
				padding: 6px 14px;
				cursor: pointer;
				transition: all 0.3s ease;
				font-size: 13px;
				font-weight: 500;
				border-radius: 20px;
				background-color: #fff;
				border-color: #b3e0ff;
				color: #409eff;

				&:hover {
					transform: translateY(-2px);
					box-shadow: 0 4px 12px rgba(64, 158, 255, 0.25);
					background: linear-gradient(135deg, #409eff 0%, #5dade2 100%);
					color: #fff;
					border-color: transparent;
				}
			}
		}
	}

	/* 消息输入容器 - 参考MessageInput设计 */
	.message-input-container {
		background: linear-gradient(to bottom, #ffffff 0%, #fafbfc 100%);
		border: 1px solid #e8ecef;
		border-radius: 12px;
		padding: 10px 14px; /* 保持原有padding */
		box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
		margin-bottom: 0; /* 确保没有底部margin */

		.input-wrapper {
			display: flex;
			flex-direction: column;
			gap: 8px;
			position: relative;
		}

		.toolbar {
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 0 2px;

			.toolbar-left,
			.toolbar-right {
				display: flex;
				gap: 6px;
				align-items: center;
			}

			.toolbar-divider {
				width: 1px;
				height: 16px;
				background: #e8ecef;
				margin: 0 4px;
			}

			:deep(.el-button) {
				border: 1px solid #e8ecef;
				background: #ffffff;
				color: #5a6c7d;
				transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
				font-weight: 500;

				&:hover {
					border-color: #667eea;
					color: #667eea;
					transform: translateY(-2px);
					box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
					background: #ffffff;
				}

				&:active {
					transform: translateY(0);
				}

				&.is-active {
					border-color: #667eea;
					color: #667eea;
					background: linear-gradient(
						135deg,
						rgba(102, 126, 234, 0.1) 0%,
						rgba(118, 75, 162, 0.1) 100%
					);
					box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15),
						0 2px 8px rgba(102, 126, 234, 0.2);

					&:hover {
						background: linear-gradient(
							135deg,
							rgba(102, 126, 234, 0.15) 0%,
							rgba(118, 75, 162, 0.15) 100%
						);
					}
				}
			}
		}

		.input-area {
			display: flex;
			gap: 10px;
			align-items: flex-end;

			.message-textarea {
				flex: 1;

				:deep(.el-textarea__inner) {
					border-radius: 10px;
					border: 2px solid #e8ecef;
					background: #ffffff;
					padding: 8px 12px;
					font-size: 14px;
					line-height: 1.6;
					transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
					resize: none;

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

			.send-btn {
				flex-shrink: 0;
				background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
				border: none;
				padding: 8px 24px;
				font-size: 14px;
				font-weight: 600;
				border-radius: 10px;
				box-shadow: 0 2px 8px rgba(255, 107, 107, 0.25);
				transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
				height: 60px;

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
	}

	/* 表情面板样式 - 参考对话界面设计 */
	.emoji-panel {
		position: absolute;
		bottom: 100%;
		left: 0;
		right: 0;
		background: #ffffff;
		border: 1px solid #e8ecef;
		border-radius: 8px;
		padding: 10px;
		box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
		margin-bottom: 6px;
		max-height: 180px;
		overflow-y: auto;
		z-index: 100;

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
				font-size: 20px;
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

	/* 上传图片预览样式 */
	.uploaded-images-preview {
		display: flex;
		gap: 12px;
		padding: 12px;
		background-color: #f8f9fa;
		border-radius: 12px;
		margin-bottom: 12px;
		flex-wrap: wrap;

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

	/* Dish Recognition Section */
	.recognition-section {
		padding: 24px;
		background-color: #fff;
		border-radius: 16px;
		box-shadow: 0 2px 16px 0 rgba(0, 0, 0, 0.06);

		.upload-area {
			margin-bottom: 24px;

			.upload-zone {
				border: 3px dashed #ff6b6b;
				border-radius: 16px;
				padding: 48px;
				text-align: center;
				cursor: pointer;
				transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
				background: linear-gradient(135deg, #fff9fa 0%, #fff 100%);

				&:hover {
					border-color: #ff5252;
					background: linear-gradient(135deg, #ffe8e8 0%, #fff 100%);
					transform: scale(1.01);
					box-shadow: 0 4px 16px rgba(255, 107, 107, 0.15);
				}

				&.has-image {
					padding: 0;
					border-style: solid;
					border-width: 2px;
				}

				.upload-placeholder {
					.el-icon {
						color: #ff6b6b;
						margin-bottom: 16px;
						font-size: 56px;
					}

					.upload-text {
						font-size: 17px;
						font-weight: 600;
						color: #303133;
						margin: 12px 0;
					}

					.upload-hint {
						font-size: 14px;
						color: #909399;
					}
				}

				.image-preview {
					position: relative;
					width: 100%;
					height: 320px;
					overflow: hidden;
					border-radius: 12px;

					img {
						width: 100%;
						height: 100%;
						object-fit: cover;
					}

					.image-overlay {
						position: absolute;
						top: 0;
						left: 0;
						right: 0;
						bottom: 0;
						background: rgba(0, 0, 0, 0.6);
						display: flex;
						align-items: center;
						justify-content: center;
						opacity: 0;
						transition: opacity 0.3s ease;

						&:hover {
							opacity: 1;
						}
					}
				}
			}
		}

		.recognize-btn {
			width: 100%;
			height: 54px;
			font-size: 17px;
			font-weight: 600;
			margin-bottom: 20px;
			background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
			border: none;
			transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
			border-radius: 14px;

			&:hover:not(:disabled) {
				transform: translateY(-3px);
				box-shadow: 0 8px 20px rgba(255, 107, 107, 0.4);
			}

			&:disabled {
				background: linear-gradient(135deg, #d3d4d6 0%, #c8c9cc 100%);
				cursor: not-allowed;
			}
		}

		.recognition-result {
			animation: resultFadeIn 0.5s ease-out;

			.result-header {
				text-align: center;
				margin-bottom: 28px;

				h4 {
					font-size: 22px;
					font-weight: 700;
					color: #303133;
					margin: 0;
				}
			}

			.result-cards {
				display: grid;
				grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
				gap: 18px;

				.result-card {
					background: linear-gradient(135deg, #fff 0%, #fff9fa 100%);
					border: 2px solid #ffe0e3;
					border-radius: 16px;
					padding: 24px;
					transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

					&:hover {
						transform: translateY(-6px);
						box-shadow: 0 8px 24px rgba(255, 107, 107, 0.2);
						border-color: #ff6b6b;
					}

					&.main-card {
						grid-column: 1 / -1;
						background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
						border: none;

						.card-label {
							color: rgba(255, 255, 255, 0.95);
							font-size: 15px;
						}

						.card-value {
							color: #fff;
							font-size: 28px;
							font-weight: 700;
						}
					}

					&.calories-card {
						.card-value.highlight {
							color: #ff6b6b;
							font-size: 32px;
							font-weight: 700;
						}
					}

					&.full-width {
						grid-column: 1 / -1;
					}

					.card-label {
						font-size: 14px;
						font-weight: 600;
						color: #909399;
						margin-bottom: 10px;
					}

					.card-value {
						font-size: 17px;
						font-weight: 600;
						color: #303133;

						.ingredient-tag,
						.tag-item {
							margin: 5px;
							padding: 8px 14px;
							font-weight: 500;
							border-radius: 20px;
						}
					}
				}
			}
		}
	}

	/* Recipe Optimization Section */
	.recipe-section {
		padding: 24px;
		background-color: #fff;
		border-radius: 16px;
		box-shadow: 0 2px 16px 0 rgba(0, 0, 0, 0.06);

		.recipe-input {
			margin-bottom: 24px;

			.el-input {
				textarea {
					border-radius: 14px;
					transition: all 0.3s ease;
					font-size: 15px;
					padding: 14px 16px;

					&:focus {
						box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.12);
						border-color: #ff6b6b;
					}
				}
			}
		}

		.optimize-btn {
			width: 100%;
			height: 54px;
			font-size: 17px;
			font-weight: 600;
			margin-bottom: 24px;
			background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
			border: none;
			transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
			border-radius: 14px;

			&:hover:not(:disabled) {
				transform: translateY(-3px);
				box-shadow: 0 8px 20px rgba(255, 107, 107, 0.4);
			}

			&:disabled {
				background: linear-gradient(135deg, #d3d4d6 0%, #c8c9cc 100%);
				cursor: not-allowed;
			}
		}

		.recipe-result {
			animation: resultFadeIn 0.5s ease-out;

			.result-header {
				text-align: center;
				margin-bottom: 28px;

				h4 {
					font-size: 22px;
					font-weight: 700;
					color: #303133;
					margin: 0;
				}
			}

			.recipe-comparison {
				display: flex;
				align-items: stretch;
				gap: 24px;
				margin-bottom: 32px;

				@media (max-width: 768px) {
					flex-direction: column;
				}

				.recipe-card {
					flex: 1;
					background: #fff;
					border: 2px solid #ffe0e3;
					border-radius: 16px;
					overflow: hidden;
					transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

					&:hover {
						transform: translateY(-6px);
						box-shadow: 0 8px 24px rgba(255, 107, 107, 0.2);
					}

					.card-header {
						padding: 18px 24px;
						background: linear-gradient(135deg, #fff9fa 0%, #ffe8e8 100%);
						border-bottom: 2px solid #ffe0e3;

						.card-title {
							font-size: 17px;
							font-weight: 700;
							color: #303133;
						}
					}

					.card-content {
						padding: 24px;
						max-height: 420px;
						overflow-y: auto;

						pre {
							margin: 0;
							white-space: pre-wrap;
							word-wrap: break-word;
							font-family: inherit;
							line-height: 1.9;
							color: #606266;
							font-size: 15px;
						}
					}

					&.optimized-recipe-card {
						.card-header {
							background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
							border-bottom: none;

							.card-title {
								color: #fff;
							}
						}
					}
				}

				.recipe-arrow {
					display: flex;
					align-items: center;
					font-size: 40px;
					color: #ff6b6b;
					font-weight: 700;
					flex-shrink: 0;

					@media (max-width: 768px) {
						transform: rotate(90deg);
					}
				}
			}

			.improvements-section {
				background: linear-gradient(135deg, #fff 0%, #f0f9ff 100%);
				border: 2px solid #b3e0ff;
				border-radius: 16px;
				padding: 24px;

				.improvements-title {
					font-size: 17px;
					font-weight: 700;
					color: #303133;
					margin-bottom: 18px;
				}

				.improvements-tags {
					display: flex;
					flex-wrap: wrap;
					gap: 12px;

					.el-tag {
						padding: 12px 24px;
						font-size: 15px;
						font-weight: 600;
						border-radius: 24px;
					}
				}
			}
		}
	}

	/* 动画定义 */
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

	@keyframes resultFadeIn {
		from {
			opacity: 0;
			transform: scale(0.95) translateY(10px);
		}
		to {
			opacity: 1;
			transform: scale(1) translateY(0);
		}
	}

	/* 快捷提问面板滑入滑出动画 */
	.slide-down-enter-active,
	.slide-down-leave-active {
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	}

	.slide-down-enter-from {
		opacity: 0;
		transform: translateY(-12px);
	}

	.slide-down-leave-to {
		opacity: 0;
		transform: translateY(-12px);
	}

	/* 表情面板滑入滑出动画 */
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

	/* 菜品识别增强样式 */
	.recognition-section {
		.upload-zone {
			&.is-dragging {
				border-color: #409eff;
				background: linear-gradient(135deg, #e3f2fd 0%, #fff 100%);
				transform: scale(1.02);
			}
		}

		.recognition-progress {
			margin: 20px 0;
			padding: 20px;
			background-color: #f0f9ff;
			border-radius: 12px;

			.progress-text {
				text-align: center;
				margin-top: 10px;
				font-size: 14px;
				color: #409eff;
				font-weight: 500;
			}
		}

		.recognition-buttons {
			display: flex;
			gap: 10px;
			margin-bottom: 20px;

			.recognize-btn,
			.re-recognize-btn {
				flex: 1;
			}
		}

		.nutrition-card {
			.nutrition-chart {
				margin-top: 15px;

				.nutrition-item {
					display: flex;
					align-items: center;
					gap: 15px;
					margin-bottom: 15px;

					&:last-child {
						margin-bottom: 0;
					}

					.nutrition-label {
						flex: 0 0 80px;
						display: flex;
						align-items: center;
						gap: 5px;
						font-size: 13px;
						font-weight: 500;
						color: #606266;

						.nutrition-icon {
							font-size: 18px;
						}
					}

					.nutrition-bar {
						flex: 1;
						height: 24px;
						background-color: #f0f2f5;
						border-radius: 12px;
						overflow: hidden;
						position: relative;

						.nutrition-fill {
							height: 100%;
							border-radius: 12px;
							transition: width 0.6s ease-out;
							position: relative;

							&.protein {
								background: linear-gradient(
									90deg,
									#667eea 0%,
									#764ba2 100%
								);
							}

							&.fat {
								background: linear-gradient(
									90deg,
									#f093fb 0%,
									#f5576c 100%
								);
							}

							&.carbs {
								background: linear-gradient(
									90deg,
									#4facfe 0%,
									#00f2fe 100%
								);
							}
						}
					}

					.nutrition-value {
						flex: 0 0 50px;
						text-align: right;
						font-size: 14px;
						font-weight: bold;
						color: #303133;
					}
				}
			}
		}
	}
}
</style>
