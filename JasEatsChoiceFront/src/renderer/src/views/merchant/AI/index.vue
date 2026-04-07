<template>
  <div class="merchant-ai-container">
    <div class="ai-header">
      <h2>AI经营助手</h2>
      <div class="header-actions">
        <el-tag type="success" effect="dark">
          <el-icon><Connection /></el-icon>
          在线
        </el-tag>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="border-card" class="ai-tabs">
      <!-- 经营助手 -->
      <el-tab-pane label="经营助手" name="chat">
        <MerchantAIChatPanel />
      </el-tab-pane>

      <!-- 经营洞察 -->
      <el-tab-pane label="经营洞察" name="insight">
        <BusinessInsight />
      </el-tab-pane>

      <!-- 评价回复 -->
      <el-tab-pane label="评价回复" name="reply">
        <QuickReplyGenerator />
      </el-tab-pane>

      <!-- 菜品描述 -->
      <el-tab-pane label="菜品描述" name="dish">
        <DishDescGenerator />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Connection } from '@element-plus/icons-vue'
import MerchantAIChatPanel from './components/MerchantAIChatPanel.vue'
import BusinessInsight from './components/BusinessInsight.vue'
import QuickReplyGenerator from './components/QuickReplyGenerator.vue'
import DishDescGenerator from './components/DishDescGenerator.vue'

// 当前激活的tab
const activeTab = ref('chat')

onMounted(() => {
  activeTab.value = 'chat'
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
  background: linear-gradient(135deg, @merchant-secondary-light 0%, @merchant-surface 100%);

  .ai-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: @nordic-space-lg;
    padding-bottom: @nordic-space-md;
    border-bottom: 2px solid @merchant-border;

    h2 {
      font-family: 'Georgia', 'Noto Serif SC', 'Songti SC', serif;
      font-size: @nordic-text-xl;
      font-weight: 700;
      margin: 0;
      color: @merchant-secondary;
    }

    .header-actions {
      display: flex;
      gap: @nordic-space-md;
      align-items: center;

      .el-tag {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .ai-tabs {
    flex: 1;
    display: flex;
    flex-direction: column;
    border-radius: @nordic-radius-lg;
    overflow: hidden;
    box-shadow: 0 4px 20px @merchant-shadow;

    :deep(.el-tabs__header) {
      margin: 0;
      background: linear-gradient(135deg, @merchant-secondary-light 0%, @merchant-surface 100%);
      border-bottom: 2px solid @merchant-border;
      flex-shrink: 0;
    }

    :deep(.el-tabs__nav) {
      border: none;
    }

    :deep(.el-tabs__item) {
      font-size: @nordic-text-sm;
      font-weight: 600;
      color: @merchant-text-sec;
      transition: all @nordic-transition-base ease;

      &:hover {
        color: @merchant-secondary;
      }

      &.is-active {
        color: @merchant-secondary;
        background: linear-gradient(135deg, @merchant-secondary-light 0%, @merchant-surface 100%);
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
    }
  }
}
</style>
