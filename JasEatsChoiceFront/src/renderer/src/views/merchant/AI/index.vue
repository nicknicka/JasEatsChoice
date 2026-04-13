<template>
  <div class="merchant-ai-container">
    <!-- 装饰背景层 -->
    <div class="bg-decoration">
      <div class="bg-orb bg-orb-1"></div>
      <div class="bg-orb bg-orb-2"></div>
      <div class="bg-grain"></div>
    </div>

    <!-- 页面头部 -->
    <header class="ai-header" :class="{ 'header-visible': headerVisible }">
      <div class="header-left">
        <div class="header-badge">AI</div>
        <div class="header-text">
          <h2>经营助手</h2>
          <p class="header-subtitle">智能分析 · 高效运营</p>
        </div>
      </div>
      <div class="header-right">
        <div class="status-badge">
          <span class="status-dot"></span>
          <span class="status-text">在线服务中</span>
        </div>
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
          <span class="tab-icon-wrap">
            <el-icon :size="14"><component :is="tab.icon" /></el-icon>
          </span>
          <span class="tab-label">{{ tab.label }}</span>
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
import { useUserStore } from '@/store/userStore'

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
    setTimeout(() => { navVisible.value = true }, 150)
    setTimeout(() => {
      activeTab.value = 'chat'
      contentVisible.value = true
      updateSlider()
    }, 300)
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
  padding: @nordic-space-lg @nordic-space-xl;
  background: @merchant-bg;
  position: relative;
  overflow: hidden;
  max-width: 1440px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}

// --- 装饰背景 ---
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.35;
  animation: orbFloat 20s ease-in-out infinite;

  &.bg-orb-1 {
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(74, 122, 77, 0.25), transparent 70%);
    top: -100px;
    right: -80px;
    animation-delay: 0s;
  }

  &.bg-orb-2 {
    width: 350px;
    height: 350px;
    background: radial-gradient(circle, rgba(181, 106, 74, 0.18), transparent 70%);
    bottom: -60px;
    left: -60px;
    animation-delay: -7s;
  }
}

.bg-grain {
  position: absolute;
  inset: 0;
  opacity: 0.025;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
  background-repeat: repeat;
  background-size: 180px;
}

// --- 页面头部 ---
.ai-header {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: @nordic-space-md;
  padding: @nordic-space-md @nordic-space-lg;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.7), rgba(255, 255, 255, 0.45));
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: @nordic-radius-lg;
  opacity: 0;
  transform: translateY(-16px);
  transition: all 0.6s cubic-bezier(0.22, 1, 0.36, 1);

  &.header-visible {
    opacity: 1;
    transform: translateY(0);
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .header-badge {
    width: 48px;
    height: 48px;
    border-radius: 14px;
    background: linear-gradient(135deg, @merchant-primary 0%, darken(@merchant-primary, 8%) 100%);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: 'Georgia', 'Noto Serif SC', serif;
    font-size: 18px;
    font-weight: 700;
    letter-spacing: -0.5px;
    box-shadow:
      0 4px 14px rgba(74, 122, 77, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 0.15);
    position: relative;
    overflow: hidden;

    &::after {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 50%;
      background: linear-gradient(180deg, rgba(255,255,255,0.15), transparent);
      border-radius: 14px 14px 0 0;
    }
  }

  .header-text {
    h2 {
      font-family: 'Georgia', 'Noto Serif SC', 'Songti SC', serif;
      font-size: 22px;
      font-weight: 700;
      margin: 0;
      color: @merchant-text;
      letter-spacing: -0.5px;
      line-height: 1.2;
    }

    .header-subtitle {
      font-size: 12px;
      color: @merchant-text-muted;
      margin: 4px 0 0;
      letter-spacing: 1.5px;
      font-weight: 500;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .status-badge {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    background: linear-gradient(135deg, @merchant-success-light, rgba(232, 244, 233, 0.6));
    border: 1px solid rgba(90, 143, 94, 0.15);
    border-radius: @nordic-radius-pill;
    font-size: @nordic-text-sm;
    font-weight: 600;

    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: @merchant-success;
      box-shadow: 0 0 8px rgba(90, 143, 94, 0.4);
      animation: statusPulse 2s ease-in-out infinite;
    }

    .status-text {
      color: @merchant-success;
    }
  }
}

// --- 自定义标签导航 ---
.tab-nav {
  position: relative;
  z-index: 1;
  margin-bottom: @nordic-space-md;
  flex-shrink: 0;
  opacity: 0;
  transform: translateY(10px);
  transition: all 0.5s cubic-bezier(0.22, 1, 0.36, 1);

  &.nav-visible {
    opacity: 1;
    transform: translateY(0);
  }

  .tab-nav-track {
    display: inline-flex;
    position: relative;
    background: rgba(255, 255, 255, 0.55);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.5);
    border-radius: 14px;
    padding: 5px;
    gap: 4px;
  }

  .tab-btn {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 22px;
    border: none;
    background: transparent;
    color: @merchant-text-sec;
    font-family: inherit;
    font-size: @nordic-text-sm;
    font-weight: 500;
    cursor: pointer;
    border-radius: 10px;
    transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
    white-space: nowrap;

    .tab-icon-wrap {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 24px;
      height: 24px;
      border-radius: 7px;
      transition: all 0.3s ease;
    }

    &:hover:not(.active) {
      color: @merchant-text;
      background: rgba(74, 122, 77, 0.04);
    }

    &.active {
      color: @merchant-text;
      font-weight: 600;

      .tab-icon-wrap {
        color: @merchant-primary;
      }
    }
  }

  .tab-slider {
    position: absolute;
    top: 5px;
    bottom: 5px;
    background: @merchant-surface;
    border-radius: 10px;
    box-shadow:
      0 1px 3px rgba(0, 0, 0, 0.06),
      0 4px 12px rgba(0, 0, 0, 0.04),
      0 0 0 1px rgba(0, 0, 0, 0.02);
    transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
    z-index: 0;
  }
}

// --- 标签内容区域 ---
.tab-content-area {
  position: relative;
  z-index: 1;
  flex: 1;
  min-height: 0;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(255,255,255,0.85), rgba(255,255,255,0.7));
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  box-shadow:
    0 4px 24px rgba(45, 42, 38, 0.06),
    0 1px 2px rgba(45, 42, 38, 0.04);
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s cubic-bezier(0.22, 1, 0.36, 1);

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

// --- 过渡动画 ---
.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}

.slide-left-enter-from {
  opacity: 0;
  transform: translateX(24px);
}

.slide-left-leave-to {
  opacity: 0;
  transform: translateX(-24px) scale(0.98);
}

.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}

.slide-right-enter-from {
  opacity: 0;
  transform: translateX(-24px);
}

.slide-right-leave-to {
  opacity: 0;
  transform: translateX(24px) scale(0.98);
}

// --- 装饰动画 ---
@keyframes statusPulse {
  0%, 100% {
    opacity: 1;
    box-shadow: 0 0 8px rgba(90, 143, 94, 0.4);
  }
  50% {
    opacity: 0.6;
    box-shadow: 0 0 4px rgba(90, 143, 94, 0.2);
  }
}

@keyframes orbFloat {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(15px, -20px) scale(1.03);
  }
  50% {
    transform: translate(-10px, 10px) scale(0.97);
  }
  75% {
    transform: translate(8px, 15px) scale(1.01);
  }
}
</style>
