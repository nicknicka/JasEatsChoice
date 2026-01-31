<template>
  <div class="wish-list-audit-page">
    <div class="page-header">
      <h2>想吃列表审核</h2>
      <el-button @click="loadItems" :loading="loading">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 审核组件 -->
    <wish-list-audit />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
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

<style scoped lang="scss">
.wish-list-audit-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
  }
}
</style>
