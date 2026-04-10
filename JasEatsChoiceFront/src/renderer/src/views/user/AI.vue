<template>
  <el-main class="ai-page-content">
    <header class="page-header">
      <h2>AI饮食助手</h2>
      <div class="status-badge">
        <span class="status-dot"></span>
        在线
      </div>
    </header>

    <nav class="tab-nav">
      <div class="tab-nav-track">
        <button
          v-for="tab in tabs"
          :id="`tab-btn-${tab.name}`"
          :key="tab.name"
          class="tab-btn"
          :class="{ active: activeTab === tab.name }"
          @click="switchTab(tab.name)"
        >
          <el-icon :size="16"><component :is="tab.icon" /></el-icon>
          <span>{{ tab.label }}</span>
        </button>
        <div class="tab-slider" :style="sliderStyle"></div>
      </div>
    </nav>

    <div class="tab-content-area">
      <Transition :name="transitionName" mode="out-in">
        <div v-if="activeTab === 'chat'" key="chat" class="tab-pane">
          <AiChatFull ref="aiChatRef" />
        </div>
        <div v-else-if="activeTab === 'recognition'" key="recognition" class="tab-pane">
          <DishRecognition />
        </div>
        <div v-else-if="activeTab === 'recipe'" key="recipe" class="tab-pane">
          <RecipeOptimization />
        </div>
        <div v-else-if="activeTab === 'extraction'" key="extraction" class="tab-pane">
          <ContentExtractionTab />
        </div>
      </Transition>
    </div>
  </el-main>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onActivated, defineAsyncComponent, h } from 'vue'
import { ChatRound, Camera, Document, Link as LinkIcon } from '@element-plus/icons-vue'
import AiChatFull from './AI/components/AIChatFull.vue'

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
const transitionName = ref('slide-left')
const sliderLeft = ref(0)
const sliderWidth = ref(0)
const aiChatRef = ref(null)

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
  if (name === activeTab.value) return
  const oldIndex = tabs.findIndex(t => t.name === activeTab.value)
  const newIndex = tabs.findIndex(t => t.name === name)
  transitionName.value = newIndex > oldIndex ? 'slide-left' : 'slide-right'
  activeTab.value = name
  updateSlider()
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
  setTimeout(() => {
    activeTab.value = 'chat'
    updateSlider()
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
