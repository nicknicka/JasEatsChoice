<template>
  <div class="app-container">
    <div class="main-content">
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
            <!-- AI聊天 -->
            <el-tab-pane label="AI聊天" name="chat" :icon="ChatRound">
              <AIChatPanel />
            </el-tab-pane>

            <!-- 菜品识别 -->
            <el-tab-pane label="菜品识别" name="recognition" :icon="Camera">
              <DishRecognition />
            </el-tab-pane>

            <!-- 食谱优化 -->
            <el-tab-pane label="食谱优化" name="recipe" :icon="Document">
              <RecipeOptimization />
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  ChatRound,
  Camera,
  Document
} from '@element-plus/icons-vue'
import AIChatPanel from './components/AIChatPanel.vue'
import DishRecognition from './components/DishRecognition.vue'
import RecipeOptimization from './components/RecipeOptimization.vue'

// 当前激活的tab
const activeTab = ref('chat')

onMounted(() => {
  activeTab.value = 'chat'
})
</script>

<style scoped lang="less">
.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.content-area {
  padding: 20px 20px 0 20px;
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
</style>
