<template>
  <div class="merchant-ai-container">
    <!-- AppPageHeader compact -->
    <AppPageHeader title="AI 经营助手" :compact="true" class="ai-page-header">
      <template #actions>
        <CommonBackButton type="default" size="default" />
        <el-button type="primary" size="default" @click="refreshData">刷新数据</el-button>
      </template>
    </AppPageHeader>

    <!-- AppTabs 标签导航 -->
    <el-tabs v-model="activeTab" class="ai-tabs" type="card">
      <el-tab-pane name="chat" label="经营助手">
        <MerchantAIChatPanel />
      </el-tab-pane>
      <el-tab-pane name="insight" label="经营洞察">
        <BusinessInsight :merchant-id="merchantId" />
      </el-tab-pane>
      <el-tab-pane name="reply" label="评价回复">
        <QuickReplyGenerator :merchant-id="merchantId" />
      </el-tab-pane>
      <el-tab-pane name="dish" label="菜品描述">
        <DishDescGenerator />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MerchantAIChatPanel from './components/MerchantAIChatPanel.vue'
import BusinessInsight from './components/BusinessInsight.vue'
import QuickReplyGenerator from './components/QuickReplyGenerator.vue'
import DishDescGenerator from './components/DishDescGenerator.vue'
import { useUserStore } from '@/store/userStore'
import { useAuthStore } from '@/store/authStore'

const route = useRoute()
const userStore = useUserStore()
const authStore = useAuthStore()

const normalizeMerchantId = (value) => {
  const normalized = String(value || '').trim()
  return normalized && normalized !== 'null' ? normalized : ''
}

const merchantId = computed(() => {
  const authMerchantId = normalizeMerchantId(authStore.merchantId)
  if (authMerchantId) return authMerchantId

  const userMerchantId = normalizeMerchantId(userStore.userInfo?.merchantId)
  if (userMerchantId) {
    authStore.setMerchantId(userMerchantId)
    return userMerchantId
  }

  const localMerchantId = normalizeMerchantId(localStorage.getItem('auth_merchantId'))
  if (localMerchantId) {
    authStore.setMerchantId(localMerchantId)
    return localMerchantId
  }

  return ''
})

const activeTab = ref('chat')

const refreshData = () => {
  console.log('刷新 AI 建议数据')
}

onMounted(() => {
  if (route.query.tab) {
    activeTab.value = route.query.tab
  }
})
</script>

<style scoped lang="less">
@import '../../../assets/css/nordic-theme.less';

.merchant-ai-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: var(--card-padding-compact) var(--page-gutter);
  background: var(--canvas);
  max-width: 1440px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}

.ai-tabs {
  margin-top: var(--space-4);
  flex: 1;
  overflow: hidden;

  .el-tabs__header {
    margin-bottom: var(--space-4);
  }

  .el-tabs__content {
    overflow-y: auto;
    height: calc(100% - 56px);
  }
}
</style>
