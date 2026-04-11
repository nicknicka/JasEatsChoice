<template>
  <div class="recipe-content-wrapper">
    <!-- 顶部装饰条 -->
    <div class="page-header-strip">
      <div class="strip-pattern"></div>
    </div>

    <div class="recipe-section">
      <!-- 输入区域 -->
      <div class="input-section">
        <div class="input-header">
          <div class="input-title">
            <span class="title-icon">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
                <polyline points="10 9 9 9 8 9"/>
              </svg>
            </span>
            输入您的食谱
          </div>
          <span class="input-hint">详细描述食材和步骤，AI将为您优化</span>
        </div>
        <div class="input-area">
          <textarea
            v-model="originalRecipe"
            placeholder="例如：西红柿鸡蛋的做法：1. 准备西红柿2个，鸡蛋2个；2. 煎鸡蛋；3. 炒西红柿；4. 混合翻炒..."
            maxlength="10000"
            class="recipe-textarea"
          ></textarea>
          <div class="input-footer">
            <span class="char-count">{{ originalRecipe.length }} / 10000</span>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <button
          class="action-btn primary-btn"
          @click="optimizeRecipe"
          :disabled="!originalRecipe || optimizationLoading"
        >
          <span class="btn-bg"></span>
          <span class="btn-content">
            <svg v-if="optimizationLoading" class="spin-icon" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M12 2v4m0 12v4m-7.07-3.93l2.83-2.83m8.48-8.48l2.83-2.83M2 12h4m12 0h4M4.93 4.93l2.83 2.83m8.48 8.48l2.83 2.83"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M12 3l1.5 4.5L18 9l-4.5 1.5L12 15l-1.5-4.5L6 9l4.5-1.5L12 3z"/>
              <path d="M5 19l1 3 3-1 3 2 3-2 3 1 1-3" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            {{ optimizationLoading ? '优化中...' : '开始优化' }}
          </span>
        </button>
      </div>

      <!-- 优化中动画 -->
      <Transition name="process-reveal">
        <div v-if="optimizationLoading" class="optimization-process">
          <div class="process-track">
            <div class="process-line">
              <div class="process-fill" :style="{ width: processProgress + '%' }"></div>
            </div>
          </div>
          <div class="process-steps">
            <div
              v-for="(step, index) in processSteps"
              :key="index"
              class="process-step"
              :class="{ active: loadingStep >= index + 1, current: loadingStep === index + 1 }"
            >
              <div class="step-indicator">
                <span class="step-ring"></span>
                <span class="step-dot"></span>
                <span class="step-icon">{{ step.icon }}</span>
              </div>
              <span class="step-label">{{ step.label }}</span>
            </div>
          </div>
          <!-- 流式预览：实时展示AI生成内容 -->
          <Transition name="fade">
            <div v-if="streamingText" class="streaming-preview">
              <pre class="streaming-content">{{ streamingText }}<span class="cursor-blink">|</span></pre>
            </div>
          </Transition>
        </div>
      </Transition>

      <!-- 优化结果 -->
      <Transition name="result-reveal">
        <div v-if="optimizedRecipe" class="recipe-result">
          <!-- 结果标题 -->
          <div class="result-hero">
            <div class="result-badge">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                <path d="M12 3l1.5 4.5L18 9l-4.5 1.5L12 15l-1.5-4.5L6 9l4.5-1.5L12 3z"/>
              </svg>
              AI 优化完成
            </div>
            <h3 class="result-title">食谱对比</h3>
            <p class="result-subtitle">左侧为原食谱，右侧为优化后的食谱</p>
          </div>

          <!-- 对比卡片 -->
          <div class="comparison-container">
            <!-- 原食谱 -->
            <div class="recipe-card original-card">
              <div class="card-header">
                <span class="card-badge original-badge">原食谱</span>
                <span class="card-label">输入内容</span>
              </div>
              <div class="card-body">
                <pre class="recipe-text">{{ optimizedRecipe.original }}</pre>
              </div>
              <div class="card-footer">
                <button class="copy-btn" @click="copyToClipboard(optimizedRecipe.original, '原食谱')">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                  </svg>
                  复制
                </button>
              </div>
            </div>

            <!-- 箭头 -->
            <div class="comparison-arrow">
              <div class="arrow-line"></div>
              <div class="arrow-icon">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M5 12h14M12 5l7 7-7 7"/>
                </svg>
              </div>
              <div class="arrow-line"></div>
            </div>

            <!-- 优化后食谱 -->
            <div class="recipe-card optimized-card">
              <div class="card-header optimized">
                <span class="card-badge optimized-badge">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                    <path d="M12 3l1.5 4.5L18 9l-4.5 1.5L12 15l-1.5-4.5L6 9l4.5-1.5L12 3z"/>
                  </svg>
                  优化后
                </span>
                <span class="card-label">AI 推荐</span>
              </div>
              <div class="card-body">
                <pre class="recipe-text">{{ optimizedRecipe.optimized }}</pre>
              </div>
              <div class="card-footer">
                <button class="copy-btn" @click="copyToClipboard(optimizedRecipe.optimized, '优化后食谱')">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                  </svg>
                  复制
                </button>
                <button class="save-btn" @click="saveToMyRecipes" :disabled="savingRecipe">
                  <svg v-if="savingRecipe" class="spin-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 2v4m0 12v4m-7.07-3.93l2.83-2.83m8.48-8.48l2.83-2.83M2 12h4m12 0h4"/>
                  </svg>
                  <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
                    <polyline points="17 21 17 13 7 13 7 21"/>
                    <polyline points="7 3 7 8 15 8"/>
                  </svg>
                  {{ savingRecipe ? '保存中...' : '保存食谱' }}
                </button>
              </div>
            </div>
          </div>

          <!-- 优化亮点 -->
          <div v-if="optimizedRecipe.improvements?.length" class="improvements-section">
            <div class="section-title">
              <span class="title-bar"></span>
              优化亮点
            </div>
            <div class="improvements-list">
              <div
                v-for="(improvement, index) in optimizedRecipe.improvements"
                :key="index"
                class="improvement-item"
                :style="{ animationDelay: (index * 100) + 'ms' }"
              >
                <span class="improvement-icon">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                    <path d="M20 6L9 17l-5-5"/>
                  </svg>
                </span>
                <span class="improvement-text">{{ improvement }}</span>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { Loading, DocumentCopy, FolderAdd } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import axios from "axios";
import { API_CONFIG } from "../../../../config/index";
import { validateRecipe } from "../../../../utils/imageValidator";
import { handleApiError } from "../../../../utils/errorHandler";
import { logger } from "../../../../config/chatConfig";
import { useAuthStore } from "../../../../store/authStore";
import { useUserStore } from "../../../../store/userStore";
import { useStreamResponse } from "../../../../composables/useStreamResponse";

// 初始化 Pinia store
const authStore = useAuthStore();
const userStore = useUserStore();

// 状态
const originalRecipe = ref("");
const optimizedRecipe = ref(null);
const optimizationLoading = ref(false);
const loadingStep = ref(0);
const savingRecipe = ref(false);
const streamingText = ref("");

// 处理步骤
const processSteps = [
  { icon: '🔍', label: '分析食谱' },
  { icon: '🧠', label: 'AI优化' },
  { icon: '✨', label: '生成结果' }
];

// 进度百分比
const processProgress = computed(() => {
  return (loadingStep.value / processSteps.length) * 100;
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
 * 保存到我的食谱
 */
const saveToMyRecipes = async () => {
  if (!optimizedRecipe.value) {
    ElMessage.warning("请先优化食谱");
    return;
  }

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
    const rawData = optimizedRecipe.value.rawData;
    const recipeName = rawData?.name || "AI优化食谱";
    const steps = rawData?.steps || "";
    const calorie = Number(rawData?.calorie) || 0;
    const protein = Number(rawData?.protein) || 0;
    const carb = Number(rawData?.carb) || 0;
    const fat = Number(rawData?.fat) || 0;

    // 将食材字符串解析为食材名称数组
    const ingredientNames = rawData?.ingredients
      ? rawData.ingredients
          .split(/[,，、;；\n]/)
          .map((s) => s.trim())
          .filter(Boolean)
      : [];

    logger.log("📦 解析后的食材列表:", ingredientNames);

    // 构建符合 RecipeDetail 期望的 items 结构：
    // 每个元素是一个"菜品"对象，包含 name、ingredients 数组和营养信息
    const items = [
      {
        name: recipeName,
        ingredients: ingredientNames,
        calories: calorie,
        protein: protein,
        carbs: carb,
        fat: fat,
      },
    ];

    const newRecipe = {
      name: recipeName,
      type: "dinner",
      items: JSON.stringify(items),
      calories: calorie,
      protein: protein,
      carbs: carb,
      fat: fat,
      cookTime: "30分钟",
      favorite: false,
      detail: steps,
      userId: userId,
    };

    const response = await axios.post(
      `${API_CONFIG.baseURL}${API_CONFIG.recipe.add}`,
      newRecipe
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

const { parseSSELine } = useStreamResponse();

/**
 * 优化食谱（通过 SSE 获取真实进度）
 */
const optimizeRecipe = async () => {
  const validation = validateRecipe(originalRecipe.value);
  if (!validation.valid) {
    ElMessage.warning(validation.error);
    return;
  }

  optimizationLoading.value = true;
  loadingStep.value = 0;
  streamingText.value = "";

  // 进度消息到步骤编号的映射
  const progressToStepMap = {
    '分析食谱': 1,
    '调用AI': 2,
    '生成优化结果': 3,
  };

  try {
    const response = await fetch(API_CONFIG.baseURL + API_CONFIG.ai.recipeStream, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'text/event-stream',
      },
      body: JSON.stringify({ foodName: originalRecipe.value }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split('\n');
      buffer = lines.pop() || '';

      for (const line of lines) {
        const parsedData = parseSSELine(line);
        if (!parsedData) continue;

        // 流式token事件：累积展示AI生成内容
        if (parsedData.streaming === true && parsedData.content) {
          streamingText.value += parsedData.content;
          continue;
        }

        // 进度事件：推进步骤指示器
        if (parsedData.progress === true && parsedData.content) {
          for (const [keyword, step] of Object.entries(progressToStepMap)) {
            if (parsedData.content.includes(keyword)) {
              loadingStep.value = step;
              break;
            }
          }
          continue;
        }

        // 完成事件：包含食谱数据
        if (parsedData.done === true && parsedData.recipes) {
          loadingStep.value = 3;
          const backendRecipes = parsedData.recipes;
          if (backendRecipes.length > 0) {
            const firstRecipe = backendRecipes[0];
            optimizedRecipe.value = {
              original: originalRecipe.value,
              optimized: `推荐食谱：${firstRecipe.name}
难度：${firstRecipe.difficulty}
卡路里：${firstRecipe.calorie}大卡
食材：${firstRecipe.ingredients}
步骤：${firstRecipe.steps}`,
              improvements: ["营养均衡", "口味优化", "步骤简化"],
              rawData: firstRecipe,
            };
            logger.log("✅ 食谱优化成功:", firstRecipe.name);
          } else {
            optimizedRecipe.value = {
              original: originalRecipe.value,
              optimized: "优化失败：没有找到合适的优化食谱。",
              improvements: [],
            };
          }
          continue;
        }

        // 错误事件
        if (parsedData.error) {
          throw new Error(parsedData.message || '优化失败');
        }
      }
    }
  } catch (error) {
    logger.error("❌ 食谱优化失败:", error);
    optimizedRecipe.value = {
      original: originalRecipe.value,
      optimized: handleApiError(error),
      improvements: [],
    };
    ElMessage.error(handleApiError(error));
  } finally {
    loadingStep.value = 3;
    streamingText.value = "";
    setTimeout(() => {
      optimizationLoading.value = false;
    }, 500);
  }
};
</script>

<style scoped lang="less">
@import '../../../../assets/css/nordic-theme.less';

.recipe-content-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  flex: 1;
  overflow: hidden;
  min-height: 0;
  box-sizing: border-box;
  background: @nordic-bg;
}

// 顶部装饰条
.page-header-strip {
  height: 4px;
  flex-shrink: 0;
  background: linear-gradient(90deg, @nordic-green, @nordic-accent, @nordic-yellow, @nordic-green);
  background-size: 200% 100%;
  animation: gradientShift 4s ease infinite;

  .strip-pattern {
    height: 100%;
    background: repeating-linear-gradient(
      90deg,
      transparent,
      transparent 8px,
      rgba(255,255,255,0.3) 8px,
      rgba(255,255,255,0.3) 10px
    );
  }
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.recipe-section {
  width: 100%;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 28px 24px;
  box-sizing: border-box;

  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: @nordic-border;
    border-radius: 3px;
  }
}

// ===== 输入区域 =====
.input-section {
  background: @nordic-surface;
  border-radius: 18px;
  padding: 24px;
  margin-bottom: 16px;
  border: 1px solid @nordic-border;
}

.input-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;
}

.input-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 700;
  color: @nordic-text;
  letter-spacing: -0.3px;

  .title-icon {
    display: flex;
    color: @nordic-green;
  }
}

.input-hint {
  font-size: 12px;
  color: @nordic-text-muted;
  letter-spacing: 0.2px;
}

.input-area {
  position: relative;
}

.recipe-textarea {
  width: 100%;
  min-height: 160px;
  padding: 16px;
  border: 2px solid @nordic-border;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.7;
  color: @nordic-text;
  background: @nordic-bg;
  resize: vertical;
  transition: all 0.3s ease;
  font-family: inherit;
  box-sizing: border-box;

  &::placeholder {
    color: @nordic-text-muted;
  }

  &:focus {
    outline: none;
    border-color: @nordic-green;
    box-shadow: 0 0 0 4px rgba(123, 174, 127, 0.1);
  }
}

.input-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.char-count {
  font-size: 12px;
  color: @nordic-text-muted;
}

// ===== 操作按钮 =====
.action-bar {
  margin-bottom: 20px;
}

.action-btn {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  letter-spacing: -0.2px;

  &.primary-btn {
    background: linear-gradient(135deg, @nordic-green, @nordic-green-dark);
    color: #fff;
    box-shadow: 0 4px 16px rgba(123, 174, 127, 0.25);

    .btn-bg {
      position: absolute;
      inset: 0;
      background: linear-gradient(135deg, rgba(255,255,255,0.15), transparent);
      opacity: 0;
      transition: opacity 0.3s;
    }

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(123, 174, 127, 0.35);

      .btn-bg {
        opacity: 1;
      }
    }

    &:disabled {
      background: @nordic-border;
      box-shadow: none;
      cursor: not-allowed;
      color: @nordic-text-muted;
    }
  }

  .spin-icon {
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// ===== 优化过程 =====
.optimization-process {
  background: @nordic-surface;
  border-radius: 18px;
  padding: 28px 24px;
  margin-bottom: 20px;
  border: 1px solid @nordic-border;
}

.process-track {
  margin-bottom: 24px;
}

.process-line {
  height: 4px;
  background: @nordic-divider;
  border-radius: 2px;
  overflow: hidden;
}

.process-fill {
  height: 100%;
  border-radius: 2px;
  background: linear-gradient(90deg, @nordic-green, @nordic-accent);
  transition: width 0.5s ease;
}

.process-steps {
  display: flex;
  justify-content: space-between;
}

.process-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  opacity: 0.35;
  transition: all 0.4s ease;

  &.active {
    opacity: 1;
  }

  &.current {
    .step-indicator {
      transform: scale(1.1);
    }

    .step-ring {
      animation: ringPulse 1.5s ease-in-out infinite;
    }

    .step-icon {
      animation: iconBounce 0.8s ease-in-out infinite;
    }
  }
}

.step-indicator {
  position: relative;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.step-ring {
  position: absolute;
  inset: 0;
  border: 2px solid @nordic-green;
  border-radius: 50%;
  opacity: 0.3;
}

.step-dot {
  position: absolute;
  width: 12px;
  height: 12px;
  background: @nordic-green;
  border-radius: 50%;
}

.step-icon {
  position: relative;
  font-size: 20px;
  z-index: 1;
}

.step-label {
  font-size: 12px;
  font-weight: 600;
  color: @nordic-text-secondary;
}

// ===== 流式预览 =====
.streaming-preview {
  margin-top: 16px;
  padding: 12px 16px;
  background: @nordic-surface;
  border-radius: 8px;
  border: 1px solid @nordic-border;
  max-height: 120px;
  overflow-y: auto;
}

.streaming-content {
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  color: @nordic-text-secondary;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.cursor-blink {
  animation: cursorBlink 1s step-end infinite;
  color: @nordic-accent;
  font-weight: bold;
}

@keyframes cursorBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

// ===== 过渡动画 =====
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

@keyframes ringPulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.2); opacity: 0.5; }
}

@keyframes iconBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

// ===== 优化结果 =====
.recipe-result {
  animation: resultReveal 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes resultReveal {
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.result-hero {
  text-align: center;
  margin-bottom: 24px;
  padding: 24px;
  background: @nordic-surface;
  border-radius: 18px;
  border: 1px solid @nordic-border;
}

.result-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 30px;
  background: @nordic-green-light;
  color: @nordic-green-dark;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 12px;
}

.result-title {
  font-family: 'Georgia', 'Palatino', serif;
  font-size: 26px;
  font-weight: 700;
  color: @nordic-text;
  margin: 0 0 8px;
  letter-spacing: -0.5px;
}

.result-subtitle {
  font-size: 13px;
  color: @nordic-text-muted;
  margin: 0;
}

// 对比容器
.comparison-container {
  display: flex;
  align-items: stretch;
  gap: 16px;
  margin-bottom: 20px;

  @media (max-width: 768px) {
    flex-direction: column;

    .comparison-arrow {
      transform: rotate(90deg);
      align-self: center;
    }
  }
}

.recipe-card {
  flex: 1;
  background: @nordic-surface;
  border-radius: 18px;
  border: 1px solid @nordic-border;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  }
}

.card-header {
  padding: 18px 20px;
  background: @nordic-bg;
  border-bottom: 1px solid @nordic-border;
  display: flex;
  align-items: center;
  justify-content: space-between;

  &.optimized {
    background: linear-gradient(135deg, @nordic-green-light, @nordic-surface);
    border-bottom-color: rgba(123, 174, 127, 0.2);
  }
}

.card-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;

  &.original-badge {
    background: @nordic-divider;
    color: @nordic-text-secondary;
  }

  &.optimized-badge {
    background: @nordic-green;
    color: #fff;
  }
}

.card-label {
  font-size: 12px;
  color: @nordic-text-muted;
  font-weight: 500;
}

.card-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  max-height: 320px;

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: @nordic-border;
    border-radius: 2px;
  }
}

.recipe-text {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.8;
  color: @nordic-text;
}

.card-footer {
  padding: 14px 20px;
  background: @nordic-bg;
  border-top: 1px solid @nordic-border;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.copy-btn, .save-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  border: none;
}

.copy-btn {
  background: @nordic-surface;
  border: 1px solid @nordic-border;
  color: @nordic-text-secondary;

  &:hover {
    border-color: @nordic-accent;
    color: @nordic-accent;
    background: @nordic-accent-light;
  }
}

.save-btn {
  background: linear-gradient(135deg, @nordic-green, @nordic-green-dark);
  color: #fff;

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(123, 174, 127, 0.3);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  .spin-icon {
    animation: spin 1s linear infinite;
  }
}

// 箭头
.comparison-arrow {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-shrink: 0;
  padding: 0 8px;
}

.arrow-line {
  width: 2px;
  height: 24px;
  background: linear-gradient(180deg, transparent, @nordic-green);
  border-radius: 1px;

  &:last-child {
    background: linear-gradient(180deg, @nordic-green, transparent);
  }
}

.arrow-icon {
  color: @nordic-green;
  display: flex;
}

// 优化亮点
.improvements-section {
  background: @nordic-surface;
  border-radius: 18px;
  padding: 24px;
  border: 1px solid @nordic-border;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 700;
  color: @nordic-text;
  margin-bottom: 18px;
  letter-spacing: -0.3px;

  .title-bar {
    width: 3px;
    height: 16px;
    border-radius: 2px;
    background: @nordic-green;
    flex-shrink: 0;
  }
}

.improvements-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.improvement-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 12px;
  background: @nordic-green-light;
  color: @nordic-green-dark;
  font-size: 13px;
  font-weight: 500;
  animation: chipIn 0.4s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

.improvement-icon {
  display: flex;
  color: @nordic-green;
}

.improvement-text {
  letter-spacing: -0.2px;
}

// ===== 动画 =====
.process-reveal-enter-active {
  animation: slideDown 0.35s ease-out;
}
.process-reveal-leave-active {
  animation: slideUp 0.25s ease-in forwards;
}

.result-reveal-enter-active {
  animation: resultReveal 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}
.result-reveal-leave-active {
  animation: resultHide 0.3s ease-in forwards;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideUp {
  to { opacity: 0; transform: translateY(-8px); }
}

@keyframes resultHide {
  to { opacity: 0; transform: translateY(-12px); }
}

@keyframes chipIn {
  from {
    opacity: 0;
    transform: scale(0.8) translateY(8px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
</style>
