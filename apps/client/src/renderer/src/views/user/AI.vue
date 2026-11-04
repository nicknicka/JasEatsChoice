<template>
  <el-main class="ai-page-content">
    <header class="page-header">
      <h2>AI饮食助手</h2>
      <div class="status-badge" :class="{ offline: aiStatus !== '在线' }">
        <span class="status-dot"></span>
        {{ aiStatus }}
      </div>
    </header>

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

    <div v-if="capabilityNotice" class="capability-notice">
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
          v-bind="activeTab === 'recipe' ? { recipeStreamTimeoutMs } : {}"
        />
      </keep-alive>
    </div>
  </el-main>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onActivated, defineAsyncComponent, h, markRaw } from 'vue'
import { ChatRound, Camera, Document, Link as LinkIcon } from '@element-plus/icons-vue'
import AiChatFull from './AI/components/AIChatFull.vue'
import { API_CONFIG } from '../../config'

const DishRecognition = defineAsyncComponent(() =>
  import('./AI/components/DishRecognition.vue')
)

const RecipeOptimization = defineAsyncComponent({
  loader: () => import('./AI/components/RecipeOptimization.vue'),
  errorComponent: () => h('div', { style: 'padding: 20px; text-align: center; color: #909399;' }, [
    h('p', '食谱优化组件加载失败'),
    h('p', { style: 'font-size: 14px; margin-top: 8px;' }, '请刷新页面重试')
  ]),
  delay: 200,
  timeout: 3000
})

const ContentExtractionTab = defineAsyncComponent({
  loader: () => import('./AI/components/ContentExtractionTab.vue'),
  errorComponent: () => h('div', { style: 'padding: 20px; text-align: center; color: #909399;' }, [
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
    const response = await fetch(API_CONFIG.baseURL + API_CONFIG.ai.capabilities)
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
@import '../../assets/css/nordic-theme.less';

.ai-page-content {
  padding: 20px 20px 0 20px;
  background-color: @nordic-bg;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  max-width: 1400px;
  width: 95%;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid @nordic-border;

  h2 {
    font-size: @nordic-text-xl;
    font-weight: 700;
    margin: 0;
    color: @nordic-text;
    letter-spacing: -0.5px;
  }

  .status-badge {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 14px;
    background: @nordic-green-light;
    color: @nordic-green-dark;
    border-radius: @nordic-radius-pill;
    font-size: @nordic-text-sm;
    font-weight: 600;

    &.offline {
      background: @nordic-border;
      color: @nordic-text-secondary;

      .status-dot {
        background: @nordic-text-secondary;
        animation: none;
      }
    }

    .status-dot {
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: @nordic-green;
      animation: statusPulse 2s ease-in-out infinite;
    }
  }
}

.tab-nav {
  margin-bottom: 16px;
  flex-shrink: 0;

  .tab-nav-track {
    display: inline-flex;
    position: relative;
    background: @nordic-border;
    border-radius: 12px;
    padding: 4px;
    gap: 4px;
  }

  .tab-btn {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    gap: 7px;
    padding: 10px 20px;
    border: none;
    background: transparent;
    color: @nordic-text-secondary;
    font-family: inherit;
    font-size: @nordic-text-base;
    font-weight: 500;
    cursor: pointer;
    border-radius: 9px;
    transition: color 0.3s ease;
    white-space: nowrap;

    &:hover:not(.active) {
      color: @nordic-text;
    }

    &.active {
      color: @nordic-text;
      font-weight: 600;
    }

    &.disabled,
    &:disabled {
      color: @nordic-text-secondary;
      cursor: not-allowed;
      opacity: 0.65;
    }

    .tab-unavailable {
      padding: 1px 5px;
      border-radius: 4px;
      background: @nordic-border;
      color: @nordic-text-secondary;
      font-size: 11px;
      font-weight: 500;
    }
  }
}

.capability-notice {
  margin-bottom: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  background: @nordic-border;
  color: @nordic-text-secondary;
  font-size: @nordic-text-sm;
}

.tab-slider {
    position: absolute;
    top: 4px;
    bottom: 4px;
    background: @nordic-surface;
    border-radius: 9px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08), 0 0 1px rgba(0, 0, 0, 0.04);
    transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
    z-index: 0;
}

.tab-content-area {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  background: @nordic-surface;
  border-radius: @nordic-radius-lg;
  box-shadow: 0 2px 12px @nordic-shadow;

  .tab-pane {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
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
