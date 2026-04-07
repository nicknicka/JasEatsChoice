<template>
  <el-main class="ai-page-content">
    <!-- 功能标签页 -->
    <el-tabs v-model="activeTab" type="border-card" class="ai-tabs fade-in-up">
      <!-- AI聊天 - 使用简化版组件 -->
      <el-tab-pane label="AI聊天" name="chat">
        <AiChatFull ref="aiChatRef" />
      </el-tab-pane>

      <!-- 菜品识别 -->
      <el-tab-pane label="菜品识别" name="recognition">
        <DishRecognition />
      </el-tab-pane>

      <!-- 食谱优化 -->
      <el-tab-pane label="食谱优化" name="recipe">
        <RecipeOptimization />
      </el-tab-pane>

      <!-- 内容提取 - 新增 -->
      <el-tab-pane label="内容提取" name="extraction">
        <ContentExtractionTab />
      </el-tab-pane>
    </el-tabs>
  </el-main>
</template>

<script setup>
import { ref, defineAsyncComponent, h, watch, nextTick, onMounted, onActivated } from 'vue'
import AiChatFull from './AI/components/AIChatFull.vue'

// AI聊天组件引用
const aiChatRef = ref(null)

// 使用异步加载组件，避免编译错误
const DishRecognition = defineAsyncComponent(() =>
  import('./AI/components/DishRecognition.vue')
)

const RecipeOptimization = defineAsyncComponent({
  loader: () => import('./AI/components/RecipeOptimization.vue'),
  errorComponent: () => h('div', { style: 'padding: 20px; text-align: center; color: #909399;' }, [
    h('p', '食谱优化组件加载失败'),
    h('p', { style: 'font-size: 1rem /* 原值: 14px */; margin-top: 8px;' }, '请刷新页面重试')
  ]),
  delay: 200,
  timeout: 3000
})

// 内容提取组件
const ContentExtractionTab = defineAsyncComponent({
  loader: () => import('./AI/components/ContentExtractionTab.vue'),
  errorComponent: () => h('div', { style: 'padding: 20px; text-align: center; color: #909399;' }, [
    h('p', '内容提取组件加载失败'),
    h('p', { style: 'font-size: 1rem /* 原值: 14px */; margin-top: 8px;' }, '请刷新页面重试')
  ]),
  delay: 200,
  timeout: 3000
})

const activeTab = ref('')

// 触发AI聊天滚动的统一方法
const triggerAiChatScroll = async () => {
  console.log('📑 触发AI聊天滚动')
  await nextTick()
  setTimeout(() => {
    if (aiChatRef.value && aiChatRef.value.scrollToBottomOnActivate) {
      aiChatRef.value.scrollToBottomOnActivate()
    }
  }, 100)
}

// 监听tab切换，当切换到AI聊天时触发滚动
watch(activeTab, async (newTab) => {
  if (newTab === 'chat') {
    console.log('📑 切换到AI聊天tab')
    await triggerAiChatScroll()
  }
})

// 页面加载后延迟自动激活AI聊天tab
onMounted(() => {
  console.log('🚀 AI页面加载完成，准备延迟激活tab')
  // 延迟800ms后自动激活"AI聊天"tab，确保组件完全初始化
  setTimeout(() => {
    console.log('✅ 延迟激活AI聊天tab')
    activeTab.value = 'chat'
  }, 800)
})

// 页面被激活时（从其他菜单切换回来），触发滚动
onActivated(() => {
  console.log('🔄 AI页面被激活（keep-alive）')
  // 如果当前激活的是聊天tab，则触发滚动
  if (activeTab.value === 'chat') {
    console.log('📑 页面激活时当前在聊天tab，准备滚动')
    setTimeout(() => {
      triggerAiChatScroll()
    }, 300) // 延时300ms确保页面完全显示
  }
})
</script>

<style scoped lang="less">
/* AI页面内容区域 — 暖陶花园 */
.ai-page-content {
  padding: 20px 20px 0 20px;
  background-color: #F5F3EF;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  max-width: 1400px;
  width: 95%;
  margin: 0 auto;
}

.ai-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(180, 140, 100, 0.1);
  height: 100%;
  border: 1px solid #E8E4DE;

  :deep(.el-tabs__header) {
    margin: 0;
    background: linear-gradient(135deg, #FDF8F3 0%, #FEFCF9 100%);
    border-bottom: 2px solid #F0D5C4;
    flex-shrink: 0;
  }

  :deep(.el-tabs__nav) {
    border: none;
  }

  :deep(.el-tabs__item) {
    font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
    font-size: 1.071rem;
    font-weight: 600;
    color: #7A7168;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    letter-spacing: 0.3px;

    &:hover {
      color: #D4845A;
    }

    &.is-active {
      color: #D4845A;
      background: linear-gradient(135deg, #F0D5C4 0%, #FDF8F3 100%);
    }
  }

  :deep(.el-tabs__active-bar) {
    background-color: #D4845A;
    height: 3px;
    border-radius: 3px 3px 0 0;
  }

  :deep(.el-tabs__content) {
    flex: 1;
    overflow: hidden;
    padding: 0 !important;
    display: flex;
    flex-direction: column;
    background: #FEFCF9;
  }

  :deep(.el-tab-pane) {
    height: 100%;
    display: flex;
    flex-direction: column;
    padding: 8px 0 0 0 !important;
  }
}

.chat-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #9E9E9E;

  p {
    margin: 12px 0 0 0;
    font-size: 1.143rem;
  }

  .hint {
    font-size: 1rem;
    color: #BFB8B0;
  }
}
</style>
