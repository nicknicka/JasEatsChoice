<template>
  <div class="markdown-element">
    <div class="markdown-content" v-html="sanitizedContent"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  element: {
    type: Object,
    required: true
  }
})

// 渲染内容，优先使用 content，降级使用 text
const content = computed(() => props.element.content || props.element.text || '')

// 基础的安全清理：移除 script 标签
const sanitizedContent = computed(() => {
  const raw = content.value
  if (!raw) return ''
  // 移除 script 标签及其内容
  return raw.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
})
</script>

<style scoped>
.markdown-element {
  width: 100%;
}

.markdown-content {
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  word-break: break-word;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3) {
  margin: 12px 0 8px;
  color: #1a1a1a;
}

.markdown-content :deep(p) {
  margin: 8px 0;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  padding-left: 20px;
  margin: 8px 0;
}

.markdown-content :deep(code) {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  color: #c7254e;
}

.markdown-content :deep(pre) {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #667eea;
  padding-left: 12px;
  margin: 8px 0;
  color: #666;
}
</style>
