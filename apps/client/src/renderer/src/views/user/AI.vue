<template>
  <el-main class="ai-page-content">
    <!-- 页头：标准版，标题 --text-h1 + 上下 --space-6（§4.7）；状态徽章进右侧动作区 -->
    <AppPageHeader title="AI饮食助手">
      <template #actions>
        <span class="status-badge" :class="{ offline: aiStatus !== '在线' }">
          <span class="status-dot"></span>
          {{ aiStatus }}
        </span>
      </template>
    </AppPageHeader>

    <nav class="tab-nav">
      <div class="tab-nav-track">
        <button
          v-for="tab in tabs"
          :id="`tab-btn-${tab.name}`"
          :key="tab.name"
          class="tab-btn"
          :class="{ active: activeTab === tab.name, disabled: !isTabAvailable(tab.name) }"
          :disabled="!isTabAvailable(tab.name)"
          :title="tabUnavailableText(tab.name)"
          @click="switchTab(tab.name)"
        >
          <el-icon :size="16"><component :is="tab.icon" /></el-icon>
          <span>{{ tab.label }}</span>
          <span v-if="isCapabilityControlled(tab.name) && !isTabAvailable(tab.name)" class="tab-unavailable">
            暂未开放
          </span>
        </button>
        <div class="tab-slider" :style="sliderStyle"></div>
      </div>
    </nav>

    <div v-if="capabilityNotice && !aiUnavailable" class="capability-notice">
      {{ capabilityNotice }}
    </div>

    <div class="tab-content-area">
      <!-- 缓存流式任务组件，避免切换 tab 时中断 -->
      <keep-alive :include="['AiChatFull', 'RecipeOptimization', 'DishRecognition']">
        <component
          :is="currentTabComponent"
          :key="activeTab"
          ref="aiChatRef"
          class="tab-pane"
          :inert="chatUnavailable || undefined"
          v-bind="activeTab === 'recipe'
            ? { recipeStreamTimeoutMs }
            : activeTab === 'recognition'
              ? { dishRecognitionTimeoutMs }
              : {}"
        />
      </keep-alive>

      <!-- 未配置 / 状态未知：对话工作区转为不可用态，只保留说明与重试入口（§4.5、§4.6） -->
      <AppEmptyState
        v-if="chatUnavailable"
        class="ai-workspace-unavailable"
        :icon="Warning"
        :description="unavailableText"
        action-text="重试"
        @action="retryCapabilities"
      />
    </div>
  </el-main>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onActivated, defineAsyncComponent, h, markRaw } from 'vue'
import { ChatRound, Camera, Document, Link as LinkIcon, Warning } from '@element-plus/icons-vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import AiChatFull from './AI/components/AIChatFull.vue'
import { API_CONFIG } from '../../config'
import { useAuthStore } from '../../store/authStore'

const DishRecognition = defineAsyncComponent(() =>
  import('./AI/components/DishRecognition.vue')
)

const RecipeOptimization = defineAsyncComponent({
  loader: () => import('./AI/components/RecipeOptimization.vue'),
  errorComponent: () => h('div', { style: 'padding: 20px; text-align: center; color: var(--ink-500);' }, [
    h('p', '食谱优化组件加载失败'),
    h('p', { style: 'font-size: 14px; margin-top: 8px;' }, '请刷新页面重试')
  ]),
  delay: 200,
  timeout: 3000
})

const ContentExtractionTab = defineAsyncComponent({
  loader: () => import('./AI/components/ContentExtractionTab.vue'),
  errorComponent: () => h('div', { style: 'padding: 20px; text-align: center; color: var(--ink-500);' }, [
    h('p', '内容提取组件加载失败'),
    h('p', { style: 'font-size: 14px; margin-top: 8px;' }, '请刷新页面重试')
  ]),
  delay: 200,
  timeout: 3000
})

const tabs = [
  { name: 'chat', label: 'AI聊天', icon: ChatRound },
  { name: 'recognition', label: '菜品识别', icon: Camera },
  { name: 'recipe', label: '食谱优化', icon: Document },
  { name: 'extraction', label: '内容提取', icon: LinkIcon }
]

const activeTab = ref('')
const capabilities = ref(null)
const capabilitiesLoaded = ref(false)
const capabilitiesError = ref('')
const DEFAULT_RECIPE_STREAM_TIMEOUT_MS = 120000
const DEFAULT_DISH_RECOGNITION_TIMEOUT_MS = 30000
const sliderLeft = ref(0)
const sliderWidth = ref(0)
const aiChatRef = ref(null)

// 动态组件映射
const tabComponents = {
  chat: markRaw(AiChatFull),
  recognition: markRaw(DishRecognition),
  recipe: markRaw(RecipeOptimization),
  extraction: markRaw(ContentExtractionTab)
}

// 当前 tab 对应的组件
const currentTabComponent = computed(() => tabComponents[activeTab.value] || null)

const recipeStreamTimeoutMs = computed(() => {
  const timeout = Number(capabilities.value?.recipeStreamTimeoutMs)
  return Number.isFinite(timeout) && timeout > 0 ? timeout : DEFAULT_RECIPE_STREAM_TIMEOUT_MS
})

const dishRecognitionTimeoutMs = computed(() => {
  const timeout = Number(capabilities.value?.dishRecognitionTimeoutMs)
  return Number.isFinite(timeout) && timeout > 0 ? timeout : DEFAULT_DISH_RECOGNITION_TIMEOUT_MS
})

const isCapabilityControlled = (name) => name === 'recognition' || name === 'recipe'

const isTabAvailable = (name) => {
  if (!isCapabilityControlled(name)) return true
  if (!capabilitiesLoaded.value || !capabilities.value) return false
  return name === 'recognition' ? capabilities.value.vision === true : capabilities.value.streaming === true
}

const tabUnavailableText = (name) => {
  if (!isCapabilityControlled(name) || isTabAvailable(name)) return ''
  return name === 'recognition' ? '菜品识别暂未开放' : '食谱优化暂未开放'
}

const aiStatus = computed(() => {
  if (!capabilitiesLoaded.value) return '检测中'
  if (capabilitiesError.value) return '状态未知'
  return capabilities.value?.available ? '在线' : '未配置'
})

const capabilityNotice = computed(() => {
  if (!capabilitiesLoaded.value) return '正在检测 AI 能力...'
  if (capabilitiesError.value) return 'AI 能力状态读取失败，菜品识别与食谱优化暂不可用'

  const unavailable = []
  if (capabilities.value?.vision !== true) unavailable.push('菜品识别')
  if (capabilities.value?.streaming !== true) unavailable.push('食谱优化')
  return unavailable.length > 0 ? `${unavailable.join('、')}暂未开放` : ''
})

// 未配置或状态未知：对话能力整体不可用
const aiUnavailable = computed(
  () => capabilitiesLoaded.value && (!!capabilitiesError.value || capabilities.value?.available !== true)
)

// 仅当前停在对话 tab 时才锁工作区，切到「内容提取」等仍可正常使用
const chatUnavailable = computed(() => aiUnavailable.value && activeTab.value === 'chat')

const unavailableText = computed(() => {
  if (capabilitiesError.value) return capabilitiesError.value
  return capabilities.value?.unavailableMessage || 'AI 助手暂不可用'
})

const retryCapabilities = () => {
  capabilitiesError.value = ''
  capabilitiesLoaded.value = false
  loadCapabilities()
}

const sliderStyle = computed(() => ({
  left: `${sliderLeft.value}px`,
  width: `${sliderWidth.value}px`,
  opacity: sliderWidth.value > 0 ? 1 : 0
}))

const updateSlider = () => {
  nextTick(() => {
    const el = document.getElementById(`tab-btn-${activeTab.value}`)
    if (el) {
      sliderLeft.value = el.offsetLeft
      sliderWidth.value = el.offsetWidth
    }
  })
}

const switchTab = (name) => {
  if (name === activeTab.value || !isTabAvailable(name)) return
  activeTab.value = name
  updateSlider()
}

const loadCapabilities = async () => {
  try {
    // SecurityConfig 默认拒绝后 /v1/ai/capabilities 要求登录态（见 docs/technical/public-routes.md）。
    // 这里是裸 fetch，不走 axios/全局 fetch 拦截器，必须自己带令牌；
    // 否则 401 会被全局拦截器判定为「用户态失效」而清登录态跳登录页。
    const authStore = useAuthStore()
    const token = authStore.token || localStorage.getItem('auth_token')
    const headers = {}
    if (token) headers.Authorization = `Bearer ${token}`

    const response = await fetch(API_CONFIG.baseURL + API_CONFIG.ai.capabilities, { headers })
    const result = await response.json()
    if (!response.ok || !result?.success || !result.data) {
      throw new Error(result?.message || '能力状态读取失败')
    }
    capabilities.value = result.data
  } catch (error) {
    capabilitiesError.value = error?.message || '能力状态读取失败'
  } finally {
    capabilitiesLoaded.value = true
    if (!isTabAvailable(activeTab.value)) {
      activeTab.value = 'chat'
      updateSlider()
    }
  }
}

const triggerAiChatScroll = async () => {
  await nextTick()
  setTimeout(() => {
    if (aiChatRef.value && aiChatRef.value.scrollToBottomOnActivate) {
      aiChatRef.value.scrollToBottomOnActivate()
    }
  }, 100)
}

watch(activeTab, async (newTab) => {
  if (newTab === 'chat') {
    await triggerAiChatScroll()
  }
  updateSlider()
})

onMounted(() => {
  loadCapabilities()
  // 兜底：能力接口未及时返回时先展示聊天页；若用户已点开可用 tab，不能覆盖其选择。
  setTimeout(() => {
    if (!activeTab.value || !isTabAvailable(activeTab.value)) {
      activeTab.value = 'chat'
      updateSlider()
    }
  }, 100)
})

onActivated(() => {
  if (activeTab.value === 'chat') {
    setTimeout(() => triggerAiChatScroll(), 300)
  }
  updateSlider()
})
</script>

<style scoped lang="less">
.ai-page-content {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  max-width: var(--page-max);
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-6);
  background-color: var(--canvas);
}

/* 状态徽章：进页头右侧动作区，未配置/状态未知统一走中性色（§4.7、§5.3） */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--success-bg);
  color: var(--success);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
  white-space: nowrap;

  .status-dot {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: currentColor;
    animation: statusPulse 2s ease-in-out infinite;
  }

  &.offline {
    background: var(--neutral-bg);
    color: var(--ink-500);

    .status-dot {
      animation: none;
    }
  }
}

.tab-nav {
  margin-bottom: var(--space-4);
  flex-shrink: 0;

  .tab-nav-track {
    display: inline-flex;
    position: relative;
    background: var(--line);
    border-radius: var(--radius-lg);
    padding: var(--space-1);
    gap: var(--space-1);
  }

  .tab-btn {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    gap: var(--space-1);
    padding: var(--space-2) var(--space-5);
    border: none;
    background: transparent;
    color: var(--ink-500);
    font-family: inherit;
    font-size: var(--text-body);
    font-weight: var(--weight-medium);
    cursor: pointer;
    border-radius: var(--radius-md);
    transition: color var(--duration-fast) var(--ease-out);
    white-space: nowrap;

    &:hover:not(.active) {
      color: var(--ink-900);
    }

    &.active {
      color: var(--ink-900);
      font-weight: var(--weight-semibold);
    }

    &.disabled,
    &:disabled {
      color: var(--ink-400);
      cursor: not-allowed;
      opacity: 0.65;
    }

    .tab-unavailable {
      padding: 1px var(--space-1);
      border-radius: var(--radius-sm);
      background: var(--neutral-bg);
      color: var(--ink-500);
      font-size: var(--text-caption);
      font-weight: var(--weight-medium);
    }
  }
}

/* 提示条：降到副信息层级，不与工作区抢主次（§3.4、§4.6） */
.capability-notice {
  margin-bottom: var(--space-3);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.tab-slider {
  position: absolute;
  top: var(--space-1);
  bottom: var(--space-1);
  background: var(--surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: all var(--duration-base) var(--ease-in-out);
  z-index: 0;
}

/* 对话工作区：固定高度、内部滚动、输入框固定在底部（§4.6） */
.tab-content-area {
  position: relative;
  flex: 1;
  min-height: 0;
  overflow: hidden;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);

  .tab-pane {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
}

/* 不可用态：覆盖对话工作区，只留图标 + 说明 + 重试（§4.5、§4.6） */
.ai-workspace-unavailable {
  position: absolute;
  inset: 0;
  background: var(--surface);
  z-index: 2;
}

@media (prefers-reduced-motion: reduce) {
  .status-badge .status-dot {
    animation: none;
  }
}

.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-left-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.slide-left-leave-to {
  opacity: 0;
  transform: translateX(-30px) scale(0.98);
}

.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-right-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.slide-right-leave-to {
  opacity: 0;
  transform: translateX(30px) scale(0.98);
}

@keyframes statusPulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(0.8);
  }
}
</style>
