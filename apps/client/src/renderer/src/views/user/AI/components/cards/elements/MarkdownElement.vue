<template>
  <div class="markdown-element">
    <div class="markdown-content" v-html="sanitizedContent"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { sanitizeAICardMarkdown } from '@/utils/aiCardMarkdownSanitizer'

const props = defineProps({
  element: {
    type: Object,
    required: true
  }
})

// 渲染内容，优先使用 content，降级使用 text
const content = computed(() => props.element.content || props.element.text || '')

const sanitizedContent = computed(() => sanitizeAICardMarkdown(content.value))
</script>

<style scoped>
.markdown-element {
  width: 100%;
}

.markdown-content {
  font-size: 14px;
  line-height: 1.4;
  color: #333;
  word-break: break-word;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3) {
  margin: 6px 0 4px;
  color: #1a1a1a;
}

.markdown-content :deep(p) {
  margin: 4px 0;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  padding-left: 20px;
  margin: 4px 0;
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
  padding: 6px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 4px 0;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #667eea;
  padding-left: 12px;
  margin: 4px 0;
  color: #666;
}
</style>
