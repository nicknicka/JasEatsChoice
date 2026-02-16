<template>
	<div class="recipe-content-wrapper">
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
				<el-icon v-if="optimizationLoading" class="rotating"><Loading /></el-icon>
				{{ optimizationLoading ? "优化中..." : "✨ 开始优化食谱" }}
			</el-button>

			<!-- 优化中动画 -->
			<div v-if="optimizationLoading" class="optimization-loading">
				<div class="loading-animation">
					<div class="loading-dot"></div>
					<div class="loading-dot"></div>
					<div class="loading-dot"></div>
				</div>
				<div class="loading-steps">
					<div class="step-item" :class="{ active: loadingStep >= 1, blinking: loadingStep === 1 && blinkingStep === 1 }">
						<span class="step-icon">🔍</span>
						<span class="step-text">分析食谱内容</span>
					</div>
					<div class="step-item" :class="{ active: loadingStep >= 2, blinking: loadingStep === 2 && blinkingStep === 2 }">
						<span class="step-icon">🧠</span>
						<span class="step-text">AI智能优化</span>
					</div>
					<div class="step-item" :class="{ active: loadingStep >= 3, blinking: loadingStep === 3 && blinkingStep === 3 }">
						<span class="step-icon">✨</span>
						<span class="step-text">生成完美食谱中</span>
					</div>
				</div>
			</div>

			<div v-if="optimizedRecipe" class="recipe-result">
				<div class="result-header">
					<h4>✨ 优化结果</h4>
				</div>

				<div class="recipe-comparison">
					<div class="recipe-card original-recipe-card">
						<div class="card-header">
							<span class="card-title">📝 原食谱</span>
						</div>
						<div class="card-content-wrapper" @mouseenter="showCopyBtn.original = true" @mouseleave="showCopyBtn.original = false">
							<div class="card-content">
								<pre>{{ optimizedRecipe.original }}</pre>
							</div>
							<transition name="copy-btn-fade">
								<el-button
									v-show="showCopyBtn.original"
									class="copy-icon-btn"
									@click="copyToClipboard(optimizedRecipe.original, '原食谱')"
								>
									<el-icon><DocumentCopy /></el-icon>
								</el-button>
							</transition>
						</div>
					</div>

					<div class="recipe-arrow">→</div>

					<div class="recipe-card optimized-recipe-card">
						<div class="card-header">
							<span class="card-title">⭐ 优化后</span>
						</div>
						<div class="card-content-wrapper" @mouseenter="showCopyBtn.optimized = true" @mouseleave="showCopyBtn.optimized = false">
							<div class="card-content">
								<pre>{{ optimizedRecipe.optimized }}</pre>
							</div>
							<transition name="copy-btn-fade">
								<el-button
									v-show="showCopyBtn.optimized"
									class="copy-icon-btn"
									@click="copyToClipboard(optimizedRecipe.optimized, '优化后食谱')"
								>
									<el-icon><DocumentCopy /></el-icon>
								</el-button>
							</transition>
						</div>
						<div class="card-footer">
							<el-button
								type="primary"
								size="small"
								class="save-btn"
								@click="saveToMyRecipes"
								:loading="savingRecipe"
							>
								<el-icon><FolderAdd /></el-icon>
								{{ savingRecipe ? "保存中..." : "保存食谱" }}
							</el-button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup>
import { ref } from "vue";
import { Loading, DocumentCopy, FolderAdd } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import axios from "axios";
import { API_CONFIG } from "../../../../config/index";
import { validateRecipe } from "../../../../utils/imageValidator";
import { handleApiError } from "../../../../utils/errorHandler";
import { logger } from "../../../../config/chatConfig";
import { useAuthStore } from "../../../../store/authStore";
import { useUserStore } from "../../../../store/userStore";

// 初始化 Pinia store
const authStore = useAuthStore();
const userStore = useUserStore();

// 状态
const originalRecipe = ref("");
const optimizedRecipe = ref(null);
const optimizationLoading = ref(false);
const loadingStep = ref(1); // 初始显示第一步
const blinkingStep = ref(1); // 当前闪烁的步骤
const savingRecipe = ref(false);
const showCopyBtn = ref({
	original: false,
	optimized: false
});

/**
 * 复制到剪贴板
 */
const copyToClipboard = async (text, name) => {
	try {
		await navigator.clipboard.writeText(text);
		ElMessage.success(`${name}已复制到剪贴板`);
		logger.log(`✅ 已复制${name}:`, text.substring(0, 50) + "...");
	} catch (error) {
		// 降级方案：使用传统方法
		try {
			const textArea = document.createElement("textarea");
			textArea.value = text;
			textArea.style.position = "fixed";
			textArea.style.opacity = "0";
			document.body.appendChild(textArea);
			textArea.select();
			document.execCommand("copy");
			document.body.removeChild(textArea);
			ElMessage.success(`${name}已复制到剪贴板`);
			logger.log(`✅ 已复制${name}:`, text.substring(0, 50) + "...");
		} catch (fallbackError) {
			logger.error("❌ 复制失败:", fallbackError);
			ElMessage.error("复制失败，请手动复制");
		}
	}
};

/**
 * 解析食材字符串
 * 支持多种分隔符和格式
 */
const parseIngredients = (ingredientsStr) => {
	if (!ingredientsStr) return [];

	logger.log("🔍 开始解析食材:", ingredientsStr);

	// 1. 首先替换各种分隔符为统一的分隔符
	let normalized = ingredientsStr
		// 替换中文顿号为逗号
		.replace(/、/g, ',')
		// 替换中文分号为逗号
		.replace(/；/g, ',')
		// 替换英文分号为逗号
		.replace(/;/g, ',')
		// 替换中文句号为逗号
		.replace(/。/g, ',')
		// 移除换行符
		.replace(/[\n\r]/g, ',');

	// 2. 按逗号分割
	let parts = normalized.split(',').map(item => item.trim()).filter(item => item);

	logger.log("📋 分割后的部分:", parts);

	// 3. 进一步处理每个食材项
	const ingredients = [];

	parts.forEach(part => {
		// 处理括号中的说明，如 "鸡蛋(2个)" 或 "猪肉（200克）"
		// 优先匹配括号格式
		const bracketMatch = part.match(/^(.+?)（(.+?)）$/);

		if (bracketMatch) {
			// 中文括号
			const name = bracketMatch[1].trim();
			const bracketContent = bracketMatch[2].trim();
			ingredients.push({ name, amount: bracketContent });
			logger.log("✅ 匹配到中文括号:", name, bracketContent);
		} else {
			// 尝试英文括号
			const bracketMatchEn = part.match(/^(.+?)\((.+?)\)$/);
			if (bracketMatchEn) {
				const name = bracketMatchEn[1].trim();
				const bracketContent = bracketMatchEn[2].trim();
				ingredients.push({ name, amount: bracketContent });
				logger.log("✅ 匹配到英文括号:", name, bracketContent);
			} else {
				// 无括号的情况：尝试匹配数字+单位
				// 匹配模式：食材名称 + 数字 + 单位
				// 支持的常见单位：克、g、千克、kg、斤、两、个、只、条、根、勺等
				const amountMatch = part.match(/^(.+?)(\d+\.?\d*)\s*(克|g|千克|kg|斤|两|个|只|条|根|勺|毫升|ml|升|l|杯|颗|片|块|张|把|朵|包|袋|瓶|粒|枚|支|节|片|块|颗|粒|枚|支|朵|把|张|袋|包|瓶|勺|匙|茶匙|汤匙|适量|少许|若干)?$/);

				if (amountMatch) {
					const name = amountMatch[1].trim();
					const amount = amountMatch[2];
					const unit = amountMatch[3] || "";
					ingredients.push({ name, amount: unit ? `${amount}${unit}` : amount });
					logger.log("✅ 匹配到数字+单位:", name, amount, unit);
				} else {
					// 无法匹配格式，直接使用原始文本
					ingredients.push({ name: part.trim(), amount: "" });
					logger.log("⚠️ 无法匹配，使用原始文本:", part);
				}
			}
		}
	});

	logger.log("📦 解析完成的食材列表:", ingredients);

	return ingredients.map(item => ({
		name: item.amount ? `${item.name} ${item.amount}` : item.name,
		calories: 0,
		protein: 0,
		carbs: 0,
		fat: 0
	}));
};

/**
 * 保存到我的食谱
 */
const saveToMyRecipes = async () => {
	if (!optimizedRecipe.value) {
		ElMessage.warning("请先优化食谱");
		return;
	}

	// 获取用户ID
	let userId = null;
	if (authStore.userId) {
		userId = authStore.userId;
	} else if (userStore.userInfo?.userId) {
		userId = userStore.userInfo.userId;
	} else {
		ElMessage.error("无法获取用户ID,请先登录");
		return;
	}

	savingRecipe.value = true;

	try {
		// 解析优化后的食谱信息
		const recipeText = optimizedRecipe.value.optimized;
		const lines = recipeText.split("\n");

		// 提取食谱信息
		let recipeName = "AI优化食谱";
		let difficulty = "简单";
		let calorie = 0;
		let ingredients = [];
		let steps = "";

		lines.forEach((line) => {
			if (line.includes("推荐食谱：")) {
				recipeName = line.replace("推荐食谱：", "").trim();
			} else if (line.includes("难度：")) {
				difficulty = line.replace("难度：", "").trim();
			} else if (line.includes("卡路里：")) {
				const calorieStr = line
					.replace("卡路里：", "")
					.replace("大卡", "")
					.trim();
				calorie = parseInt(calorieStr) || 0;
			} else if (line.includes("食材：")) {
				const ingredientsStr = line.replace("食材：", "").trim();
				ingredients = parseIngredients(ingredientsStr);
			} else if (line.includes("步骤：")) {
				steps = line.replace("步骤：", "").trim();
			}
		});

		logger.log("📦 解析后的食材列表:", ingredients);

		// 构造食谱数据
		const newRecipe = {
			name: recipeName,
			type: "晚餐", // 默认为晚餐,用户可以在我的食谱中修改
			items: ingredients,
			ingredients: ingredients,
			calories: calorie,
			time: "30分钟", // 默认30分钟
			cookTime: "30分钟",
			favorite: false,
			// 添加AI优化相关信息
			description: steps,
			difficulty: difficulty,
		};

		// 调用后端API保存食谱
		const response = await axios.post(
			`${API_CONFIG.baseURL}${API_CONFIG.recipe.add}`,
			{
				...newRecipe,
				userId: userId,
				favorite: false,
				items: ingredients, // 直接传递数组，不要序列化
				calories: calorie,
			}
		);

		if (response.data?.code === "200" && response.data?.data) {
			logger.log("✅ 食谱保存成功:", response.data.data);
			ElMessage.success("已成功保存到我的食谱");
		} else {
			ElMessage.error("保存食谱失败,请稍后重试");
		}
	} catch (error) {
		logger.error("❌ 保存食谱失败:", error);
		ElMessage.error("保存食谱失败,请稍后重试");
	} finally {
		savingRecipe.value = false;
	}
};

/**
 * 优化食谱
 */
const optimizeRecipe = async () => {
	// 验证食谱
	const validation = validateRecipe(originalRecipe.value);
	if (!validation.valid) {
		ElMessage.warning(validation.error);
		return;
	}

	optimizationLoading.value = true;
	loadingStep.value = 1; // 初始显示第一步
	blinkingStep.value = 1; // 第一步开始闪烁

	// 记录开始时间
	const startTime = Date.now();
	let apiResponded = false; // 标记API是否已响应

	// 步骤1: 分析食谱内容 - 闪烁一段时间后进入步骤2 (3000ms)
	const step1Timer = setTimeout(() => {
		if (optimizationLoading.value && !apiResponded) {
			blinkingStep.value = 0; // 停止步骤1闪烁
			loadingStep.value = 2; // 进入步骤2
			blinkingStep.value = 2; // 开始步骤2闪烁
		}
	}, 3000);

	// 步骤2: AI智能优化 - 闪烁一段时间后进入步骤3 (3000ms后，共6000ms)
	const step2Timer = setTimeout(() => {
		if (optimizationLoading.value && !apiResponded) {
			blinkingStep.value = 0; // 停止步骤2闪烁
			loadingStep.value = 3; // 进入步骤3
			blinkingStep.value = 3; // 开始步骤3闪烁
		}
	}, 6000);

	// 步骤3: 生成完美食谱 - 等待API响应后停止闪烁，等待500ms显示结果
	const step3Timer = setTimeout(async () => {
		if (optimizationLoading.value) {
			// 如果API还未响应，等待API响应
			if (!apiResponded) {
				// 等待API响应（最多再等30秒，总时长约36秒）
				const checkResponse = setInterval(() => {
					if (apiResponded || Date.now() - startTime > 30000) {
						clearInterval(checkResponse);
						// API已响应或超时，停止闪烁
						blinkingStep.value = 0; // 停止闪烁
						// 等待500ms后显示结果
						setTimeout(() => {
							optimizationLoading.value = false;
							loadingStep.value = 0;
						}, 500);
					}
				}, 100);
			} else {
				// API已响应，停止闪烁
				blinkingStep.value = 0;
				// 等待500ms后显示结果
				setTimeout(() => {
					optimizationLoading.value = false;
					loadingStep.value = 0;
				}, 500);
			}
		}
	}, 10000);

	// 调用后端API
	axios
		.post(API_CONFIG.baseURL + API_CONFIG.ai.recipe, {
			foodName: originalRecipe.value,
		})
		.then((response) => {
			const backendRecipes = response.data.data;

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
				logger.log("✅ 食谱优化成功:", firstRecipe.name);
			} else {
				optimizedRecipe.value = {
					original: originalRecipe.value,
					optimized: "优化失败：没有找到合适的优化食谱。",
					improvements: [],
				};
			}
		})
		.catch((error) => {
			logger.error("❌ 食谱优化失败:", error);

			optimizedRecipe.value = {
				original: originalRecipe.value,
				optimized: handleApiError(error),
				improvements: [],
			};

			ElMessage.error(handleApiError(error));
		})
		.finally(() => {
			// 标记API已响应
			apiResponded = true;
		});
};
</script>

<style scoped lang="less">
.recipe-content-wrapper {
	display: flex;
	flex-direction: column;
	height: 100%;
	width: 100%;
	flex: 1;
	gap: 8px;
	overflow: hidden;
	min-height: 0;
	box-sizing: border-box;
}

.recipe-section {
	width: 100%;
	flex: 1;
	min-height: 0;
	overflow-y: auto;
	background-color: #fff;
	border-radius: 16px;
	padding: 24px;
	box-sizing: border-box;
	box-shadow: 0 2px 16px 0 rgba(0, 0, 0, 0.06);

	.recipe-input {
		margin-bottom: 24px;

		:deep(.el-textarea__inner) {
			border-radius: 14px;
			transition: all 0.3s ease;
			font-size: 1.071rem /* 原值: 15px */;
			padding: 14px 16px;

			&:focus {
				box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.12);
				border-color: #ff6b6b;
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

		.rotating {
			animation: rotate 1s linear infinite;
		}

		&:hover:not(:disabled) {
			transform: translateY(-3px);
			box-shadow: 0 8px 20px rgba(255, 107, 107, 0.4);
		}

		&:disabled {
			background: linear-gradient(135deg, #d3d4d6 0%, #c8c9cc 100%);
			cursor: not-allowed;
		}
	}

	// 优化中动画
	.optimization-loading {
		background: linear-gradient(135deg, #fff9fa 0%, #ffe8e8 100%);
		border: 2px solid #ffe0e3;
		border-radius: 16px;
		padding: 32px 24px;
		margin-bottom: 24px;
		animation: loadingFadeIn 0.5s ease-out;

		.loading-animation {
			display: flex;
			justify-content: center;
			align-items: center;
			gap: 12px;
			margin-bottom: 24px;

			.loading-dot {
				width: 12px;
				height: 12px;
				background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
				border-radius: 50%;
				animation: bounce 1.4s infinite ease-in-out both;

				&:nth-child(1) {
					animation-delay: -0.32s;
				}

				&:nth-child(2) {
					animation-delay: -0.16s;
				}
			}
		}

		.loading-steps {
			display: flex;
			justify-content: space-around;
			align-items: center;

			.step-item {
				display: flex;
				flex-direction: column;
				align-items: center;
				gap: 8px;
				opacity: 0.3;
				transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
				transform: scale(0.9);

				&.active {
					opacity: 1;
					transform: scale(1);

					&.blinking {
						.step-icon {
							animation: blinkingPulse 1.2s ease-in-out infinite;
						}
					}

					.step-icon {
						animation: pulse 1.5s ease-in-out infinite;
					}
				}

				.step-icon {
					font-size: 2.286rem /* 原值: 32px */;
					transition: all 0.3s ease;
				}

				.step-text {
					font-size: 1rem /* 原值: 14px */;
					font-weight: 600;
					color: #606266;
				}
			}
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
				transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);

				&:hover {
					transform: translateY(-6px);
					box-shadow: 0 8px 24px rgba(255, 107, 107, 0.2);
				}

				.card-header {
					padding: 18px 24px;
					background: linear-gradient(135deg, #fff9fa 0%, #ffe8e8 100%);
					border-bottom: 2px solid #ffe0e3;
					display: flex;
					justify-content: center;
					align-items: center;

					.card-title {
						font-size: 17px;
						font-weight: 700;
						color: #303133;
					}
				}

				.card-content-wrapper {
					position: relative;
					padding-bottom: 72px; /* 为复制按钮留出空间 */

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
							font-size: 1.071rem /* 原值: 15px */;
						}
					}

					.copy-icon-btn {
						position: absolute;
						bottom: 16px;
						right: 16px;
						width: 40px;
						height: 40px;
						border-radius: 50%;
						padding: 0;
						background-color: #f5f5f5;
						border: 1px solid #e0e0e0;
						color: #909399;
						transition: all 0.3s ease;
						z-index: 10;
						display: flex;
						align-items: center;
						justify-content: center;

						.el-icon {
							font-size: 1.286rem /* 原值: 18px */;
							margin: 0;
						}

						&:hover {
							background-color: #e0e0e0;
							border-color: #c0c0c0;
							color: #606266;
							transform: translateY(-2px);
							box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
						}
					}
				}

				.card-footer {
					padding: 16px 24px;
					background: #fafafa;
					border-top: 1px solid #f0f0f0;
					display: flex;
					justify-content: center;
					align-items: center;

					.save-btn {
						border-radius: 8px;
						padding: 8px 20px;
						font-size: 1rem /* 原值: 14px */;
						font-weight: 600;
						transition: all 0.4s ease;
						background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
						border: none;

						.el-icon {
							margin-right: 4px;
							font-size: 1rem /* 原值: 14px */;
						}

						&:hover {
							transform: translateY(-2px);
							box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
							background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
						}
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
				font-size: 2.857rem /* 原值: 40px */;
				color: #ff6b6b;
				font-weight: 700;
				flex-shrink: 0;

				@media (max-width: 768px) {
					transform: rotate(90deg);
				}
			}
		}
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


@keyframes rotate {
	from {
		transform: rotate(0deg);
	}
	to {
		transform: rotate(360deg);
	}
}

@keyframes bounce {
	0%,
	80%,
	100% {
		transform: scale(0);
	}
	40% {
		transform: scale(1);
	}
}

@keyframes pulse {
	0%,
	100% {
		transform: scale(1);
		opacity: 1;
	}
	50% {
		transform: scale(1.1);
		opacity: 0.8;
	}
}

@keyframes blinkingPulse {
	0%,
	100% {
		transform: scale(1);
		opacity: 1;
		filter: brightness(1);
	}
	50% {
		transform: scale(1.15);
		opacity: 0.6;
		filter: brightness(1.3);
	}
}

@keyframes loadingFadeIn {
	from {
		opacity: 0;
		transform: translateY(-10px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

/* 复制按钮淡入淡出动画 */
.copy-btn-fade-enter-active,
.copy-btn-fade-leave-active {
	transition: opacity 0.3s ease, transform 0.3s ease;
}

.copy-btn-fade-enter-from,
.copy-btn-fade-leave-to {
	opacity: 0;
	transform: translateY(5px);
}
</style>
