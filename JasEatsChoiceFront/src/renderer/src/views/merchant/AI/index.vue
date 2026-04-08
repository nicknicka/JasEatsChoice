<template>
  <div class="merchant-ai-container">
    <!-- 页面头部 -->
    <header class="ai-header" :class="{ 'header-visible': headerVisible }">
      <h2>AI经营助手</h2>
      <div class="status-badge">
        <span class="status-dot"></span>
        在线
      </div>
    </header>

    <!-- 自定义标签导航 -->
    <nav class="tab-nav" :class="{ 'nav-visible': navVisible }">
      <div class="tab-nav-track">
        <button
          v-for="tab in tabs"
          :id="`merchant-tab-${tab.name}`"
          :key="tab.name"
          class="tab-btn"
          :class="{ active: activeTab === tab.name }"
          @click="switchTab(tab.name)"
        >
          <el-icon :size="15"><component :is="tab.icon" /></el-icon>
          <span>{{ tab.label }}</span>
        </button>
        <div class="tab-slider" :style="sliderStyle"></div>
      </div>
    </nav>

    <!-- 标签内容区域 -->
    <div class="tab-content-area" :class="{ 'content-visible': contentVisible }">
      <Transition :name="transitionName" mode="out-in">
        <div v-if="activeTab === 'chat'" key="chat" class="tab-pane">
          <MerchantAIChatPanel />
        </div>
        <div v-else-if="activeTab === 'insight'" key="insight" class="tab-pane">
          <BusinessInsight :merchant-id="merchantId" />
        </div>
        <div v-else-if="activeTab === 'reply'" key="reply" class="tab-pane">
          <QuickReplyGenerator :merchant-id="merchantId" />
        </div>
        <div v-else-if="activeTab === 'dish'" key="dish" class="tab-pane">
          <DishDescGenerator />
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onActivated, defineAsyncComponent, h } from 'vue'
import { ChatRound, TrendCharts, Comment, Dish } from '@element-plus/icons-vue'
import MerchantAIChatPanel from './components/MerchantAIChatPanel.vue'
import { useUserStore } from '@/stores/user'

// 获取当前商家ID
const userStore = useUserStore()
const merchantId = computed(() => userStore.userInfo?.merchantId || '')

// 异步加载非核心组件
const BusinessInsight = defineAsyncComponent({
  loader: () => import('./components/BusinessInsight.vue'),
  errorComponent: () => h('div', { style: 'padding: 20px; text-align: center; color: #9E9893;' }, [
    h('p', '经营洞察组件加载失败'),
    h('p', { style: 'font-size: 14px; margin-top: 8px;' }, '请刷新页面重试')
  ]),
  delay: 200,
  timeout: 3000
})

const QuickReplyGenerator = defineAsyncComponent({
  loader: () => import('./components/QuickReplyGenerator.vue'),
  errorComponent: () => h('div', { style: 'padding: 20px; text-align: center; color: #9E9893;' }, [
    h('p', '评价回复组件加载失败'),
    h('p', { style: 'font-size: 14px; margin-top: 8px;' }, '请刷新页面重试')
  ]),
  delay: 200,
  timeout: 3000
})

const DishDescGenerator = defineAsyncComponent({
  loader: () => import('./components/DishDescGenerator.vue'),
  errorComponent: () => h('div', { style: 'padding: 20px; text-align: center; color: #9E9893;' }, [
    h('p', '菜品描述组件加载失败'),
    h('p', { style: 'font-size: 14px; margin-top: 8px;' }, '请刷新页面重试')
  ]),
  delay: 200,
  timeout: 3000
})

// 标签页配置
const tabs = [
  { name: 'chat', label: '经营助手', icon: ChatRound },
  { name: 'insight', label: '经营洞察', icon: TrendCharts },
  { name: 'reply', label: '评价回复', icon: Comment },
  { name: 'dish', label: '菜品描述', icon: Dish }
]

const activeTab = ref('')
const transitionName = ref('slide-left')
const sliderLeft = ref(0)
const sliderWidth = ref(0)

// 入场动画状态
const headerVisible = ref(false)
const navVisible = ref(false)
const contentVisible = ref(false)

// 滑块位置样式
const sliderStyle = computed(() => ({
  left: `${sliderLeft.value}px`,
  width: `${sliderWidth.value}px`,
  opacity: sliderWidth.value > 0 ? 1 : 0
}))

// 更新滑块位置
const updateSlider = () => {
  nextTick(() => {
    const el = document.getElementById(`merchant-tab-${activeTab.value}`)
    if (el) {
      sliderLeft.value = el.offsetLeft
      sliderWidth.value = el.offsetWidth
    }
  })
}

// 切换标签页
const switchTab = (name) => {
  if (name === activeTab.value) return
  const oldIndex = tabs.findIndex(t => t.name === activeTab.value)
  const newIndex = tabs.findIndex(t => t.name === name)
  transitionName.value = newIndex > oldIndex ? 'slide-left' : 'slide-right'
  activeTab.value = name
  updateSlider()
}

// 监听tab变化
watch(activeTab, () => {
  updateSlider()
})

// 交错入场动画
const playEntrance = () => {
  headerVisible.value = false
  navVisible.value = false
  contentVisible.value = false

  requestAnimationFrame(() => {
    headerVisible.value = true
    setTimeout(() => { navVisible.value = true }, 120)
    setTimeout(() => {
      activeTab.value = 'chat'
      contentVisible.value = true
      updateSlider()
    }, 240)
  })
}

onMounted(() => {
  playEntrance()
})

onActivated(() => {
  updateSlider()
})
</script>

<style scoped lang="less">
@import '../../../assets/css/nordic-theme.less';
@import '../../../assets/css/merchant-theme.less';

.merchant-ai-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: @nordic-space-lg;
  background: linear-gradient(135deg, @merchant-secondary-light 0%, @merchant-bg 100%);
  max-width: 1400px;
  width: 95%;
  margin: 0 auto;
  box-sizing: border-box;
}

// 页面头部
.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: @nordic-space-md;
  padding-bottom: @nordic-space-md;
  border-bottom: 2px solid @merchant-border;
  opacity: 0;
  transform: translateY(-12px);
  transition: all 0.45s cubic-bezier(0.23, 1, 0.32, 1);

  &.header-visible {
    opacity: 1;
    transform: translateY(0);
  }

  h2 {
    font-family: 'Georgia', 'Noto Serif SC', 'Songti SC', serif;
    font-size: @nordic-text-xl;
    font-weight: 700;
    margin: 0;
    color: @merchant-secondary;
    letter-spacing: @nordic-letter-tight;
  }

  .status-badge {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 14px;
    background: @merchant-success-light;
    color: @merchant-success;
    border-radius: @nordic-radius-pill;
    font-size: @nordic-text-sm;
    font-weight: 600;

    .status-dot {
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: @merchant-success;
      animation: statusPulse 2s ease-in-out infinite;
    }
  }
}

// 自定义标签导航
.tab-nav {
  margin-bottom: @nordic-space-md;
  flex-shrink: 0;
  opacity: 0;
  transform: translateY(8px);
  transition: all 0.45s cubic-bezier(0.23, 1, 0.32, 1);

  &.nav-visible {
    opacity: 1;
    transform: translateY(0);
  }

  .tab-nav-track {
    display: inline-flex;
    position: relative;
    background: @merchant-border;
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
    color: @merchant-text-sec;
    font-family: inherit;
    font-size: @nordic-text-sm;
    font-weight: 500;
    cursor: pointer;
    border-radius: 9px;
    transition: color 0.3s ease;
    white-space: nowrap;

    &:hover:not(.active) {
      color: @merchant-text;
    }

    &.active {
      color: @merchant-text;
      font-weight: 600;
    }
  }

  .tab-slider {
    position: absolute;
    top: 4px;
    bottom: 4px;
    background: @merchant-surface;
    border-radius: 9px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08), 0 0 1px rgba(0, 0, 0, 0.04);
    transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
    z-index: 0;
  }
}

// 标签内容区域
.tab-content-area {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  background: @merchant-surface;
  border-radius: @nordic-radius-lg;
  box-shadow: 0 4px 20px @merchant-shadow;
  opacity: 0;
  transform: translateY(16px);
  transition: all 0.5s cubic-bezier(0.23, 1, 0.32, 1);

  &.content-visible {
    opacity: 1;
    transform: translateY(0);
  }

  .tab-pane {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
}

// 过渡动画 - 左滑
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

// 过渡动画 - 右滑
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

// 状态点脉冲动画
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
