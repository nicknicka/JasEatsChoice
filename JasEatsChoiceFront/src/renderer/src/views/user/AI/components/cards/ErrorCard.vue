<template>
  <div class="error-card">
    <div class="card-header">
      <div class="header-title">
        <span class="icon">❌</span>
        <span class="title">操作失败</span>
      </div>
      <div class="header-summary">{{ data.summary }}</div>
    </div>

    <div class="card-content">
      <el-alert
        :title="data.data.error || '操作失败，请稍后重试'"
        type="error"
        :closable="false"
        show-icon
      />

      <div v-if="data.data.queryType" class="error-info">
        <span class="error-label">查询类型：</span>
        <span class="error-value">{{ data.data.queryType }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.error-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  padding: 16px 20px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.icon {
  font-size: 24px;
}

.header-summary {
  font-size: 14px;
  opacity: 0.9;
}

.card-content {
  background: white;
  padding: 16px;
}

.error-info {
  margin-top: 12px;
  font-size: 14px;
  color: #666;
}

.error-label {
  color: #999;
  margin-right: 8px;
}

.error-value {
  color: #333;
  font-weight: 500;
}
</style>
