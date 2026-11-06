<script setup>
import { ref, computed, onMounted } from 'vue'
import { VideoCamera, Document, Check, Shop, MagicStick, Star, View } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 返回教程列表页面（根据来源决定返回哪里）
const goBackToList = () => {
  // 检查URL参数中的from字段
  const from = route.query.from

  if (from === 'my') {
    // 从"我的教程"来的，返回到我的教程
    router.push('/user/home/tutorials/my')
  } else if (from === 'square') {
    // 从"教程广场"来的，返回到教程广场
    router.push('/user/home/tutorials')
  } else {
    // 默认返回到我的教程（因为通常用户是从自己的教程进入的）
    router.push('/user/home/tutorials/my')
  }
}

// 当前教程
const currentTutorial = ref(null)
const loading = ref(true)

// 媒体区状态：加载完成后淡入，失败则落到明确空状态
const mediaReady = ref(false)
const mediaError = ref(false)

const normalizeTutorial = (tutorial = {}) => {
  return {
    ...tutorial,
    sourceType: tutorial.sourceType || tutorial.source_type || '',
    reviewStatus: tutorial.reviewStatus || tutorial.review_status || '',
    coverImage: tutorial.coverImage || tutorial.cover_image || '',
    videoUrl: tutorial.videoUrl || tutorial.video_url || '',
    viewCount: tutorial.viewCount ?? tutorial.view_count ?? tutorial.views ?? 0,
    ratingCount: tutorial.ratingCount ?? tutorial.rating_count ?? 0,
    isOfficial: tutorial.isOfficial ?? tutorial.is_official ?? tutorial.official ?? false
  }
}

// 媒体区是否有可用媒体源（封面或视频地址）
const hasMedia = computed(() => {
  const tutorial = currentTutorial.value
  if (!tutorial) return false
  return Boolean(tutorial.coverImage) || (tutorial.type === 'video' && Boolean(tutorial.videoUrl))
})

const resetMediaState = () => {
  mediaReady.value = false
  mediaError.value = false
}

const onMediaError = () => {
  mediaError.value = true
}

// 从后端获取教程详情
const fetchTutorialDetail = async () => {
  loading.value = true
  resetMediaState()

  try {
    const tutorialId = route.params.id
    const response = await api.get(`${API_CONFIG.tutorial.detail}${tutorialId}`)

    // 支持多种响应格式：
    // 1. response.data 是教程对象
    // 2. response 本身是教程对象
    let tutorial = null

    if (response.data && typeof response.data === 'object' && response.data.id) {
      tutorial = response.data
    } else if (response && typeof response === 'object' && response.id) {
      tutorial = response
    }

    if (tutorial) {
      currentTutorial.value = normalizeTutorial(tutorial)
    } else {
      ElMessage.error('教程不存在')
      goBackToList()
    }
  } catch (error) {
    if (error.response?.status === 404) {
      ElMessage.error('教程不存在')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '加载教程失败')
    }
  } finally {
    loading.value = false
  }
}

// 获取难度显示名称
const getDifficultyName = (difficulty) => {
  const map = {
    BEGINNER: '初级',
    INTERMEDIATE: '中级',
    ADVANCED: '高级'
  }
  return map[difficulty] || difficulty
}

// Markdown 转 HTML（简单的实现）
// 标题整体下移一级，保证页内正文不再产出 h1/h2（§4.7：每页一个 h1）
const renderMarkdown = (content) => {
  if (!content) return ''

  const html = content
    // 标题（需要按顺序替换，避免 # 被处理多次）
    .replace(/^### (.*$)/gim, '<h5>$1</h5>')
    .replace(/^## (.*$)/gim, '<h4>$1</h4>')
    .replace(/^# (.*$)/gim, '<h3>$1</h3>')
    // 粗体
    .replace(/\*\*(.*?)\*\*/gim, '<strong>$1</strong>')
    // 斜体
    .replace(/\*(.*?)\*/gim, '<em>$1</em>')
    // 链接
    .replace(/\[([^\]]+)\]\(([^)]+)\)/gim, '<a href="$2" target="_blank">$1</a>')
    // 无序列表
    .replace(/^- (.*$)/gim, '<li>$1</li>')
    // 有序列表
    .replace(/^\d+\. (.*$)/gim, '<li>$1</li>')
    // 将连续的列表项包装在 ul 中
    .replace(/(<li>.*<\/li>)/gim, '<ul>$1</ul>')
    // 合并相邻的 ul
    .replace(/<\/ul>\s*<ul>/gim, '')
    // 换行（除了在 HTML 标签后的换行）
    .replace(/\n/gim, '<br>')

  return html
}

// 格式化内容（支持Markdown）
const formatContent = (content) => {
  if (!content) return ''
  return renderMarkdown(content)
}

// 页面加载时获取教程数据
onMounted(() => {
  fetchTutorialDetail()
})
</script>

<template>
  <div class="tutorial-detail-container">
    <AppPageHeader title="教程详情">
      <template #actions>
        <CommonBackButton
          type="default"
          text="返回列表"
          :use-router-back="false"
          @click="goBackToList"
        />
      </template>
    </AppPageHeader>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton animated>
        <template #template>
          <el-skeleton-item variant="image" class="skeleton-media" />
          <el-skeleton-item variant="h1" class="skeleton-title" />
          <el-skeleton-item variant="text" class="skeleton-line" />
          <el-skeleton-item variant="text" class="skeleton-line skeleton-line--short" />
        </template>
      </el-skeleton>
    </div>

    <!-- 教程详情 -->
    <article v-else-if="currentTutorial" class="tutorial-article">
      <!-- 媒体区：视频播放器 / 真实封面 / 明确空状态（P0） -->
      <div class="tutorial-media">
        <video
          v-if="currentTutorial.type === 'video' && currentTutorial.videoUrl && !mediaError"
          class="tutorial-media__video"
          :class="{ 'is-ready': mediaReady }"
          :src="currentTutorial.videoUrl"
          :poster="currentTutorial.coverImage || undefined"
          controls
          preload="metadata"
          @loadeddata="mediaReady = true"
          @error="onMediaError"
        ></video>

        <img
          v-else-if="currentTutorial.coverImage && !mediaError"
          class="tutorial-media__image"
          :class="{ 'is-ready': mediaReady }"
          :src="currentTutorial.coverImage"
          :alt="currentTutorial.title"
          @load="mediaReady = true"
          @error="onMediaError"
        />

        <!-- 无封面 / 视频地址，或媒体加载失败：明确空状态，不再用空渐变冒充内容 -->
        <AppEmptyState
          v-else
          class="tutorial-media__empty"
          :icon="currentTutorial.type === 'video' ? VideoCamera : Document"
          :description="hasMedia ? '封面暂时无法加载' : '该教程暂无封面'"
        />

        <span v-if="hasMedia && !mediaError" class="tutorial-media__type">
          <el-icon>
            <component :is="currentTutorial.type === 'video' ? VideoCamera : Document" />
          </el-icon>
          {{ currentTutorial.type === 'video' ? '视频教程' : '图文指南' }}
        </span>
      </div>

      <div class="tutorial-body">
        <!-- 来源 / 难度标签：统一次级层级，不与标题争主（P1） -->
        <div class="tutorial-tags">
          <el-tag
            v-if="currentTutorial.sourceType === 'ADMIN' && currentTutorial.isOfficial"
            type="primary"
            effect="plain"
            size="small"
          >
            <el-icon><Check /></el-icon> 官方认证
          </el-tag>

          <el-tag
            v-if="currentTutorial.sourceType === 'MERCHANT'"
            type="warning"
            effect="plain"
            size="small"
          >
            <el-icon><Shop /></el-icon> {{ currentTutorial.merchantName || '商家贡献' }}
          </el-tag>

          <el-tag
            v-if="currentTutorial.sourceType === 'AI_GENERATED'"
            :type="currentTutorial.reviewStatus === 'APPROVED' ? 'success' : 'info'"
            effect="plain"
            size="small"
          >
            <el-icon><MagicStick /></el-icon> AI生成
            <span v-if="currentTutorial.reviewStatus === 'APPROVED'" class="reviewed-badge">
              ✓ 人工审核通过
            </span>
          </el-tag>

          <el-tag v-if="currentTutorial.difficulty" type="info" effect="plain" size="small">
            {{ getDifficultyName(currentTutorial.difficulty) }}
          </el-tag>
        </div>

        <!-- 正文标题：首屏第二个视觉锚点（P0） -->
        <h2 class="tutorial-title">{{ currentTutorial.title }}</h2>

        <div class="tutorial-meta">
          <span class="duration">
            <el-icon><VideoCamera /></el-icon>
            {{ currentTutorial.duration }}
          </span>
          <span class="views">
            <el-icon><View /></el-icon>
            {{ currentTutorial.viewCount || 0 }} 浏览
          </span>
          <span v-if="currentTutorial.rating" class="rating">
            <el-icon><Star /></el-icon>
            {{ currentTutorial.rating }}
            <span class="rating-count">({{ currentTutorial.ratingCount || 0 }}人评分)</span>
          </span>
          <span v-if="currentTutorial.author" class="author">
            作者: {{ currentTutorial.author }}
          </span>
        </div>

        <div class="tutorial-content">
          <h3 class="tutorial-content__title">内容</h3>
          <div class="content-text" v-html="formatContent(currentTutorial.content)"></div>
        </div>
      </div>
    </article>

    <!-- 未找到教程 -->
    <div v-else class="not-found">
      <h2>教程不存在</h2>
      <el-button type="primary" @click="goBackToList">返回列表</el-button>
    </div>
  </div>
</template>

<style scoped lang="less">
/* 页面颜色、间距、圆角、字阶与动效全部取自 tokens.css（§3、§7.1），无裸色值。 */
.tutorial-detail-container {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-12);
  background: var(--canvas);
}

/* 页头动作区控件高度取 --control-height-md（§3.4、§4.7）。 */
.tutorial-detail-container :deep(.app-page-header__actions .el-button) {
  height: var(--control-height-md);
  min-height: var(--control-height-md);
}

.loading-container {
  padding: var(--space-10) 0;
}

.skeleton-media {
  width: 100%;
  height: 320px;
  border-radius: var(--radius-lg);
}

.skeleton-title {
  width: 60%;
  margin: var(--space-5) 0;
}

.skeleton-line {
  width: 80%;
}

.skeleton-line--short {
  width: 70%;
}

/* 文章容器：L1 微质感卡（§3.4），静态阅读卡不带 hover 位移 */
.tutorial-article {
  overflow: hidden;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
}

/* 媒体区：视频播放器 / 真实封面 / 明确空状态（P0） */
.tutorial-media {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 320px;
  overflow: hidden;
  background: var(--canvas-sunken);
}

.tutorial-media__video,
.tutorial-media__image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity var(--duration-base) var(--ease-standard);
}

.tutorial-media__video.is-ready,
.tutorial-media__image.is-ready {
  opacity: 1;
}

.tutorial-media__video {
  object-fit: contain;
  background: var(--ink-900);
}

.tutorial-media__empty {
  width: 100%;
}

.tutorial-media__type {
  position: absolute;
  top: var(--space-4);
  right: var(--space-4);
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--surface-scrim);
  backdrop-filter: blur(16px);
  color: var(--ink-700);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
}

/* 正文区：媒体到标题的距离取 --space-6（P1） */
.tutorial-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: var(--space-6);
}

.tutorial-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
}

.tutorial-tags :deep(.el-tag) {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  border-radius: var(--radius-sm);
  font-size: var(--text-body-sm);
}

.reviewed-badge {
  margin-left: var(--space-1);
  padding-left: var(--space-2);
  border-left: 1px solid currentColor;
  font-size: var(--text-caption);
}

/* 正文标题：首屏第二个视觉锚点（P0、§3.2） */
.tutorial-title {
  margin: 0;
  color: var(--ink-900);
  font-size: var(--text-display);
  font-weight: var(--weight-bold);
  letter-spacing: var(--tracking-tight);
  line-height: 40px;
}

.tutorial-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-5);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.tutorial-meta span {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
}

.tutorial-meta .el-icon {
  font-size: var(--text-body);
}

.rating-count {
  color: var(--ink-400);
}

.tutorial-content {
  margin-top: var(--space-2);
}

/* 区块标题：去掉抢眼的红色分隔线，改用中性分隔（P1） */
.tutorial-content__title {
  margin: 0 0 var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--line);
  color: var(--ink-900);
  font-size: var(--text-h2);
  font-weight: var(--weight-semibold);
  line-height: 28px;
}

.content-text {
  color: var(--ink-700);
  font-size: var(--text-body);
  line-height: 2;
}

.content-text :deep(h3),
.content-text :deep(h4),
.content-text :deep(h5) {
  margin: var(--space-6) 0 var(--space-3);
  color: var(--ink-900);
  font-weight: var(--weight-semibold);
}

.content-text :deep(h3) {
  font-size: var(--text-h2);
}

.content-text :deep(h4) {
  font-size: var(--text-h3);
}

.content-text :deep(h5) {
  font-size: var(--text-body);
}

.content-text :deep(p) {
  margin: 0 0 var(--space-4);
}

.content-text :deep(ul),
.content-text :deep(ol) {
  margin: 0 0 var(--space-4);
  padding-left: var(--space-6);
}

.content-text :deep(li) {
  margin-bottom: var(--space-2);
}

.content-text :deep(strong) {
  color: var(--ink-900);
  font-weight: var(--weight-semibold);
}

.content-text :deep(code) {
  padding: 2px var(--space-1);
  border-radius: var(--radius-sm);
  background: var(--canvas-sunken);
  font-family: var(--font-num);
  font-size: var(--text-body-sm);
}

.not-found {
  padding: var(--space-12) 0;
  text-align: center;
}

.not-found h2 {
  margin: 0 0 var(--space-5);
  color: var(--ink-500);
  font-size: var(--text-h2);
  font-weight: var(--weight-semibold);
}

@media (max-width: 768px) {
  .tutorial-detail-container {
    padding: 0 var(--space-4) var(--space-8);
  }

  .tutorial-media {
    height: 220px;
  }

  .tutorial-title {
    font-size: var(--text-h1);
    line-height: 32px;
  }

  .tutorial-body {
    padding: var(--space-5);
  }
}
</style>
