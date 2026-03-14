<template>
  <el-main class="ai-page-content">
    <div class="ai-chat-container">
      <!-- 功能标签页 -->
      <el-tabs v-model="activeTab" type="border-card" class="ai-tabs fade-in-up">
        <!-- AI聊天 - 使用简化版组件 -->
        <el-tab-pane label="AI聊天" name="chat">
          <AIChatSimple />
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
    </div>
  </el-main>
</template>

<script setup>
import { ref, defineAsyncComponent, h } from 'vue'
import AIChatSimple from './AI/components/AIChatFull.vue'

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

const activeTab = ref('chat')
</script>

<style scoped lang="less">
/* AI页面内容区域 */
.ai-page-content {
  padding: 20px 20px 0 20px;
  background-color: #fafafa;
  height: 100%;
  box-sizing: border-box;
}

.ai-chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  max-width: 1400px;
  width: 95%;
  margin: 0 auto;
  padding-bottom: 0;

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
    height: 100%;

    :deep(.el-tabs__header) {
      margin: 0;
      background: linear-gradient(135deg, #fff9fa 0%, #fff 100%);
      border-bottom: 2px solid #ffe0e3;
      flex-shrink: 0;
    }

    :deep(.el-tabs__nav) {
      border: none;
    }

    :deep(.el-tabs__item) {
      font-size: 1.071rem /* 原值: 15px */;
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
      padding: 0 !important;
      display: flex;
      flex-direction: column;
    }

    :deep(.el-tab-pane) {
      height: 100%;
      display: flex;
      flex-direction: column;
      padding: 8px 0 0 0 !important;
    }
  }
}

.chat-placeholder {
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
</style>
