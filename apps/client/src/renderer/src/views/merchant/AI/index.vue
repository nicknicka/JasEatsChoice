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

  // Element Plus 的 .el-tabs__content / .el-tab-pane 由 <el-tabs> 内部渲染，
  // 普通 scoped 选择器命不中它们；#427 换成 <el-tabs> 后高度链断掉，
  // 四个 tab 都拿不到真实可用高（对话页消息区不滚动、输入框掉出视口）。
  :deep(.el-tabs__content) {
    overflow: hidden;
  }

  :deep(.el-tab-pane) {
    height: 100%;
  }

  // 四个 tab 子页都是 height:100% + padding:20px 并依赖 border-box 才撑满，
  // 统一按边框盒，避免各自的 padding 把高度撑出 pane（原来会溢出 40px、底部被裁）。
  :deep(.el-tab-pane > *) {
    box-sizing: border-box;
  }
}
</style>
