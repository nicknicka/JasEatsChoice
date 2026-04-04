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
.merchant-ai-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
  background: linear-gradient(135deg, #FEF2F2 0%, #FFF 100%);

  .ai-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 2px solid #FECACA;

    h2 {
      font-family: 'Playfair Display SC', serif;
      font-size: 28px;
      font-weight: 700;
      margin: 0;
      background: linear-gradient(135deg, #DC2626 0%, #B91C1C 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .header-actions {
      display: flex;
      gap: 12px;
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
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(220, 38, 38, 0.1);

    :deep(.el-tabs__header) {
      margin: 0;
      background: linear-gradient(135deg, #FEF2F2 0%, #FFF 100%);
      border-bottom: 2px solid #FECACA;
      flex-shrink: 0;
    }

    :deep(.el-tabs__nav) {
      border: none;
    }

    :deep(.el-tabs__item) {
      font-size: 15px;
      font-weight: 600;
      color: #6B7280;
      transition: all 0.3s ease;

      &:hover {
        color: #DC2626;
      }

      &.is-active {
        color: #DC2626;
        background: linear-gradient(135deg, #FEE2E2 0%, #FFF 100%);
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
