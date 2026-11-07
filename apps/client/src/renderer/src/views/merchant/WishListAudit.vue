<template>
  <div class="wish-list-audit-page">
    <AppPageHeader title="想吃列表审核" compact>
      <template #actions>
        <el-button class="refresh-action" :loading="loading" @click="loadItems">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </template>
    </AppPageHeader>

    <!-- 审核组件 -->
    <wish-list-audit />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import WishListAudit from '@/components/WishListAudit.vue'

const loading = ref(false)

const loadItems = () => {
  loading.value = true
  // WishListAudit 组件内部有自己的加载逻辑
  setTimeout(() => {
    loading.value = false
  }, 500)
}

onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.wish-list-audit-page {
  box-sizing: border-box;
  padding: 0 var(--space-6) var(--space-6);
}

.refresh-action {
  height: var(--control-height-md);
  border-radius: var(--radius-md);
}

@media (max-width: 1280px) {
  .wish-list-audit-page {
    padding: 0 var(--space-4) var(--space-4);
  }
}
</style>
