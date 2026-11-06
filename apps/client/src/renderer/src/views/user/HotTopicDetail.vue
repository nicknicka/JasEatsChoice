<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Share } from '@element-plus/icons-vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

const route = useRoute()
const router = useRouter()

// 热点数据：字段与后端 HotTopicResponse 对齐（content/sourceType/createdAt/redirectUrl/clickable）
const hotTopic = ref({
  content: '',
  sourceType: '',
  redirectUrl: '',
  clickable: false,
  createdAt: ''
})

const loading = ref(true)

// 获取热点详情
const fetchHotTopicDetail = async () => {
  loading.value = true
  try {
    // 从路由参数或 localStorage 获取热点数据
    const topicData = route.params.data
      ? JSON.parse(decodeURIComponent(route.params.data))
      : JSON.parse(localStorage.getItem('currentHotTopic') || '{}')

    if (topicData && topicData.content) {
      hotTopic.value = topicData
    } else {
      // 如果没有传递数据，从后端重新获取
      const response = await api.get(API_CONFIG.home.hotTopic)
      if (response.data) {
        hotTopic.value = typeof response.data === 'object' ? response.data : {
          content: response.data,
          clickable: false
        }
      }
    }
  } catch (error) {
    console.error('加载热点详情失败:', error)
    ElMessage.error('加载热点详情失败')
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 分享热点
const shareHotTopic = async () => {
  const shareData = {
    title: '今日热点',
    text: hotTopic.value.content,
    url: window.location.href
  }

  try {
    if (navigator.share) {
      await navigator.share(shareData)
      ElMessage.success('分享成功')
    } else {
      // 降级处理:复制到剪贴板
      const shareText = `${shareData.title}\n${shareData.text}\n${shareData.url}`
      await navigator.clipboard.writeText(shareText)
      ElMessage.success('已复制到剪贴板')
    }
  } catch (error) {
    if (error.name !== 'AbortError') {
      console.error('分享失败:', error)
      ElMessage.error('分享失败,请重试')
    }
  }
}

// 打开热点关联链接：http 走外部浏览器，站内路径走路由（契约同旧实现）
const openRedirect = () => {
  if (!hotTopic.value.redirectUrl) return
  if (hotTopic.value.redirectUrl.startsWith('http')) {
    window.api?.openExternal(hotTopic.value.redirectUrl)
  } else {
    router.push(hotTopic.value.redirectUrl)
  }
}

// 来源类型中文名：与后端 HotTopicResponse 的取值对齐
const getSourceTypeName = (sourceType) => {
  const typeMap = {
    MANUAL: '手动配置',
    TUTORIAL: '教程',
    AI: 'AI生成',
    API: '第三方来源'
  }
  return typeMap[sourceType] || ''
}

// 格式化时间：只展示接口返回的 createdAt
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchHotTopicDetail()
})
</script>

<template>
  <div class="hot-topic-detail-page">
    <AppPageHeader title="今日热点">
      <template #actions>
        <el-button v-if="!loading && hotTopic.content" class="share-btn" @click="shareHotTopic">
          <el-icon><Share /></el-icon>
          <span>分享</span>
        </el-button>
        <CommonBackButton type="default" @click="goBack" />
      </template>
    </AppPageHeader>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton animated>
        <template #template>
          <el-skeleton-item variant="text" class="skeleton-meta" />
          <el-skeleton-item variant="h1" class="skeleton-title" />
          <el-skeleton-item variant="text" class="skeleton-line" />
          <el-skeleton-item variant="text" class="skeleton-line skeleton-line--short" />
        </template>
      </el-skeleton>
    </div>

    <!-- 热点详情：来源、时间、正文（P0） -->
    <article v-else-if="hotTopic.content" class="hot-topic-article">
      <div class="meta-info">
        <span v-if="getSourceTypeName(hotTopic.sourceType)" class="source-tag">
          {{ getSourceTypeName(hotTopic.sourceType) }}
        </span>
        <span v-if="formatTime(hotTopic.createdAt)" class="publish-time">
          {{ formatTime(hotTopic.createdAt) }}
        </span>
        <el-button
          v-if="hotTopic.clickable && hotTopic.redirectUrl"
          class="redirect-btn"
          type="primary"
          link
          @click="openRedirect"
        >
          查看详情
        </el-button>
      </div>

      <h2 class="topic-title">{{ hotTopic.content }}</h2>
    </article>

    <!-- 无内容：明确空状态，不再渲染空白卡（P0） -->
    <AppEmptyState
      v-else
      class="hot-topic-empty"
      description="暂无热点内容"
      action-text="返回首页"
      @action="goBack"
    />
  </div>
</template>

<style scoped lang="less">
/* 本页颜色、间距、圆角、字阶与动效全部取自 tokens.css（§3、§7.1），无裸色值。 */
.hot-topic-detail-page {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-12);
  background: var(--canvas);
}

/* 页头动作区控件高度取 --control-height-md（§3.4、§4.7）。 */
.hot-topic-detail-page :deep(.app-page-header__actions .el-button) {
  height: var(--control-height-md);
  min-height: var(--control-height-md);
}

.loading-container {
  padding: var(--space-10) 0;
}

.skeleton-meta {
  width: 160px;
  margin-bottom: var(--space-6);
}

.skeleton-title {
  width: 60%;
  margin-bottom: var(--space-5);
}

.skeleton-line {
  width: 80%;
}

.skeleton-line--short {
  width: 70%;
}

/* 正文卡：L1 微质感（§3.4），静态阅读卡不带 hover 位移。 */
.hot-topic-article {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  padding: var(--space-8);
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
  animation: topic-enter var(--duration-base) var(--ease-out);
}

.meta-info {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--line);
}

/* 来源标签：品牌暖色胶囊（§3.1、§3.4）。 */
.source-tag {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--brand-50);
  color: var(--brand-700);
  font-size: var(--text-caption);
  font-weight: var(--weight-semibold);
  line-height: 18px;
}

.publish-time {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

/* 跳转动作取次级文字按钮层级，不与页头动作争主（§4.9）。 */
.redirect-btn {
  height: var(--control-height-sm);
  min-height: var(--control-height-sm);
  padding: 0 var(--space-2);
  font-size: var(--text-body-sm);
}

/* 正文：页面主视觉，字阶取 --text-display、字重取 bold（§3.2）。 */
.topic-title {
  margin: 0;
  color: var(--ink-900);
  font-size: var(--text-display);
  font-weight: var(--weight-bold);
  letter-spacing: var(--tracking-tight);
  line-height: 44px;
  white-space: pre-wrap;
  word-break: break-word;
}

.hot-topic-empty {
  padding: var(--space-12) var(--space-6);
}

@keyframes topic-enter {
  from {
    opacity: 0;
    transform: translateY(var(--space-2));
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .hot-topic-article {
    animation: none;
  }
}

@media (max-width: 768px) {
  .hot-topic-article {
    padding: var(--space-5);
  }

  .topic-title {
    font-size: var(--text-h1);
    line-height: 34px;
  }
}
</style>
