<template>
  <div class="uni-card-header">
    <div class="header-content">
      <div class="header-title">
        <span v-if="header.title?.icon" class="icon">{{ header.title.icon }}</span>
        <span class="title">{{ header.title?.text }}</span>
      </div>
      <div v-if="header.subtitle" class="header-subtitle">{{ header.subtitle }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  header: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
/* §3.1：卡片类型靠图标/标题区分，头部走中性表面，不用满铺色 */
.uni-card-header {
  background: var(--canvas);
  border-bottom: 1px solid var(--line);
  color: var(--ink-900);
  padding: 14px 20px;
  border-radius: 16px 16px 0 0;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 4px;
}

.icon {
  font-size: 22px;
}

.title {
  line-height: 1.4;
}

.header-subtitle {
  font-size: 13px;
  color: var(--ink-500);
  margin-top: 2px;
}
</style>
