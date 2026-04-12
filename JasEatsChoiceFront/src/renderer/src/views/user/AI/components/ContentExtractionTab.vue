<template>
  <div class="extraction-content-wrapper">
    <!-- 顶部装饰条 -->
    <div class="page-header-strip">
      <div class="strip-pattern"></div>
    </div>

    <div class="extraction-section">
      <!-- 页面标题 -->
      <div class="page-hero">
        <div class="hero-content">
          <h2 class="page-title">内容提取</h2>
          <p class="page-subtitle">从视频、文章中智能提取菜品信息</p>
        </div>
        <div class="hero-actions">
          <button class="action-btn primary-btn" @click="showAddDialog">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            添加内容源
          </button>
          <button class="action-btn outline-btn" @click="showHistory">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            提取历史
          </button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon processing-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M12 6v6l4 2"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ processingCount }}</span>
            <span class="stat-label">提取中</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon success-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ successCount }}</span>
            <span class="stat-label">已完成</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon total-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ sources.length }}</span>
            <span class="stat-label">总记录</span>
          </div>
        </div>
      </div>

      <!-- 内容源列表 -->
      <div class="sources-container">
        <!-- 进行中的提取 -->
        <div v-if="processingSources.length > 0" class="sources-section">
          <div class="section-header">
            <span class="section-title">
              <span class="title-dot processing-dot"></span>
              提取中
            </span>
            <span class="section-count">{{ processingSources.length }}</span>
          </div>
          <div class="source-cards">
            <div
              v-for="(source, index) in processingSources"
              :key="source.id"
              class="source-card processing-card"
              :style="{ animationDelay: (index * 80) + 'ms' }"
            >
              <div class="card-cover">
                <img v-if="source.coverImage" :src="source.coverImage" :alt="source.title" referrerpolicy="no-referrer" />
                <div v-else class="cover-placeholder">
                  <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.5">
                    <rect x="2" y="2" width="20" height="20" rx="2.18" ry="2.18"/>
                    <line x1="7" y1="2" x2="7" y2="22"/>
                    <line x1="17" y1="2" x2="17" y2="22"/>
                    <line x1="2" y1="12" x2="22" y2="12"/>
                    <line x1="2" y1="7" x2="7" y2="7"/>
                    <line x1="2" y1="17" x2="7" y2="17"/>
                    <line x1="17" y1="7" x2="22" y2="7"/>
                    <line x1="17" y1="17" x2="22" y2="17"/>
                  </svg>
                </div>
                <div class="processing-overlay">
                  <div class="processing-spinner">
                    <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2.5">
                      <path d="M12 2v4m0 12v4M4.93 4.93l2.83 2.83m8.48 8.48l2.83 2.83M2 12h4m12 0h4M4.93 19.07l2.83-2.83m8.48-8.48l2.83-2.83"/>
                    </svg>
                  </div>
                </div>
              </div>
              <div class="card-body">
                <h4 class="card-title">{{ source.title || '未命名内容' }}</h4>
                <div class="card-meta">
                  <span class="platform-tag" :class="getPlatformClass(source.platform)">
                    {{ source.platformName }}
                  </span>
                  <span class="meta-url">{{ truncateUrl(source.contentUrl) }}</span>
                </div>
                <div class="card-status">
                  <span class="status-indicator processing-indicator"></span>
                  <span class="status-text">正在提取中...</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 已完成的提取 -->
        <div class="sources-section">
          <div class="section-header">
            <span class="section-title">
              <span class="title-dot success-dot"></span>
              提取完成
            </span>
            <span class="section-count">{{ completedSources.length }}</span>
          </div>

          <div v-if="!loading && completedSources.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="12" y1="18" x2="12" y2="12"/>
                <line x1="9" y1="15" x2="15" y2="15"/>
              </svg>
            </div>
            <p class="empty-text">暂无提取记录</p>
            <p class="empty-hint">点击上方"添加内容源"开始提取</p>
          </div>

          <div v-else class="source-cards">
            <div
              v-for="(source, index) in completedSources"
              :key="source.id"
              class="source-card"
              :class="{ published: source.isPublished, failed: source.extractionStatus === 'FAILED' }"
              :style="{ animationDelay: (index * 60) + 'ms' }"
              @click="handleViewDetail(source)"
            >
              <div class="card-cover">
                <img v-if="source.extractedDishImage" :src="source.extractedDishImage" :alt="source.extractedDishName" referrerpolicy="no-referrer" />
                <img v-else-if="source.coverImage" :src="source.coverImage" :alt="source.title" referrerpolicy="no-referrer" />
                <div v-else class="cover-placeholder">
                  <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                  </svg>
                </div>
                <div v-if="source.isPublished" class="published-badge">
                  <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor">
                    <path d="M20 6L9 17l-5-5"/>
                  </svg>
                  已发布
                </div>
              </div>
              <div class="card-body">
                <h4 class="card-title">{{ source.extractedDishName || source.title || '未命名' }}</h4>
                <div class="card-meta">
                  <span class="platform-tag" :class="getPlatformClass(source.platform)">
                    {{ source.platformName }}
                  </span>
                  <span class="content-type">{{ source.contentTypeName }}</span>
                </div>
                <div class="card-footer">
                  <span class="card-time">{{ formatTime(source.extractionTime || source.createTime) }}</span>
                  <div class="card-actions">
                    <button
                      v-if="!source.isPublished && source.extractionStatus === 'SUCCESS'"
                      class="action-icon publish-icon"
                      @click.stop="handlePublish(source)"
                      title="发布为食谱"
                    >
                      <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/>
                        <line x1="4" y1="22" x2="4" y2="15"/>
                      </svg>
                    </button>
                    <button
                      class="action-icon view-icon"
                      @click.stop="handleViewDetail(source)"
                      title="查看详情"
                    >
                      <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                        <circle cx="12" cy="12" r="3"/>
                      </svg>
                    </button>
                    <button
                      class="action-icon delete-icon"
                      @click.stop="handleDelete(source)"
                      title="删除"
                    >
                      <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="3 6 5 6 21 6"/>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加内容源弹窗 -->
    <content-extraction-dialog
      v-model:visible="addDialogVisible"
      @success="handleAddSuccess"
    />

    <!-- 提取详情弹窗 -->
    <extraction-detail-dialog
      v-model:visible="detailDialogVisible"
      :extraction-id="selectedExtractionId"
      @published="handlePublished"
    />

    <!-- 提取历史弹窗 -->
    <el-dialog
      v-model="historyDialogVisible"
      title="提取历史"
      width="800px"
      class="history-dialog"
    >
      <div class="history-table-wrapper">
        <table class="history-table">
          <thead>
            <tr>
              <th>内容链接</th>
              <th>平台</th>
              <th>类型</th>
              <th>状态</th>
              <th>提取菜品</th>
              <th>创建时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="source in sources" :key="source.id">
              <td class="url-cell">{{ truncateUrl(source.contentUrl, 35) }}</td>
              <td>
                <span class="platform-tag" :class="getPlatformClass(source.platform)">
                  {{ source.platformName }}
                </span>
              </td>
              <td>{{ source.contentTypeName }}</td>
              <td>
                <span class="status-badge" :class="getStatusClass(source.extractionStatus)">
                  {{ source.extractionStatusName }}
                </span>
              </td>
              <td>{{ source.extractedDishName || '-' }}</td>
              <td class="time-cell">{{ source.createTime }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, List } from '@element-plus/icons-vue'
import ContentSourceList from '@/components/ContentSourceList.vue'
import ContentExtractionDialog from '@/components/ContentExtractionDialog.vue'
import ExtractionDetailDialog from '@/components/ExtractionDetailDialog.vue'
import contentExtractionApi from '@/api/contentExtraction'

const loading = ref(false)
const sources = ref([])
const addDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const selectedExtractionId = ref(null)

// 自动刷新定时器
let refreshTimer = null
const POLL_INTERVAL = 3000

// 统计数据
const processingCount = computed(() => {
  return sources.value.filter(s =>
    s.extractionStatus === 'PENDING' || s.extractionStatus === 'PROCESSING'
  ).length
})

const successCount = computed(() => {
  return sources.value.filter(s => s.extractionStatus === 'SUCCESS').length
})

// 进行中的提取
const processingSources = computed(() => {
  return sources.value.filter(s =>
    s.extractionStatus === 'PENDING' || s.extractionStatus === 'PROCESSING'
  )
})

// 已完成的提取
const completedSources = computed(() => {
  return sources.value.filter(s =>
    s.extractionStatus === 'SUCCESS' || s.extractionStatus === 'FAILED'
  )
})

// 检查是否有处理中的任务
const hasProcessingTasks = computed(() => {
  return sources.value.some(s =>
    s.extractionStatus === 'PENDING' || s.extractionStatus === 'PROCESSING'
  )
})

// 加载内容源列表
const loadSources = async () => {
  loading.value = true
  try {
    const response = await contentExtractionApi.getSources()
    if (response.code === '200' || response.code === 200) {
      sources.value = response.data || []
    } else {
      ElMessage.error(response.message || '加载失败')
    }
  } catch (error) {
    console.error('加载内容源失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 显示添加弹窗
const showAddDialog = () => {
  addDialogVisible.value = true
}

// 显示历史
const showHistory = () => {
  historyDialogVisible.value = true
}

// 添加成功
const handleAddSuccess = () => {
  ElMessage.success('内容源添加成功，正在提取中...')
  loadSources()
  startPolling()
}

// 查看详情
const handleViewDetail = (source) => {
  if (source.extractionId) {
    selectedExtractionId.value = source.extractionId
    detailDialogVisible.value = true
  } else {
    ElMessage.info('内容正在提取中，请稍后查看')
  }
}

// 发布为食谱
const handlePublish = async (source) => {
  if (!source.extractionId) {
    ElMessage.warning('无法发布，请等待提取完成')
    return
  }

  try {
    await ElMessageBox.confirm('确认发布为食谱？', '提示', {
      type: 'warning'
    })

    const response = await contentExtractionApi.publishAsRecipe(source.extractionId, {})
    if (response.code === '200' || response.code === 200) {
      ElMessage.success('发布成功')
      loadSources()
    } else {
      ElMessage.error(response.message || '发布失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
      ElMessage.error('发布失败')
    }
  }
}

// 删除内容源
const handleDelete = async (source) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个内容源吗？删除后无法恢复。',
      '确认删除',
      { type: 'warning' }
    )

    const response = await contentExtractionApi.deleteSource(source.id)
    if (response.code === '200' || response.code === 200) {
      ElMessage.success('删除成功')
      loadSources()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 发布成功回调
const handlePublished = () => {
  loadSources()
}

// 获取平台样式类
const getPlatformClass = (platform) => {
  const classMap = {
    '抖音': 'douyin',
    '小红书': 'xiaohongshu',
    '哔哩哔哩': 'bilibili',
    '微信': 'wechat',
    '今日头条': 'toutiao',
    '快手': 'kuaishou'
  }
  return classMap[platform] || 'default'
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    'PENDING': 'pending',
    'PROCESSING': 'processing',
    'SUCCESS': 'success',
    'FAILED': 'failed'
  }
  return classMap[status] || 'pending'
}

// 截断URL
const truncateUrl = (url, maxLen = 40) => {
  if (!url) return ''
  if (url.length > maxLen) {
    return url.substring(0, maxLen) + '...'
  }
  return url
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return date.toLocaleDateString()
}

// 启动自动轮询
const startPolling = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }

  refreshTimer = setInterval(() => {
    if (hasProcessingTasks.value) {
      console.log('自动刷新：有处理中的任务')
      loadSources()
    } else {
      console.log('自动刷新：没有处理中的任务，停止轮询')
      stopPolling()
    }
  }, POLL_INTERVAL)
}

// 停止轮询
const stopPolling = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(() => {
  loadSources()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped lang="less">
@import '../../../../assets/css/nordic-theme.less';

.extraction-content-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  flex: 1;
  overflow: hidden;
  min-height: 0;
  box-sizing: border-box;
  background: @nordic-bg;
}

// 顶部装饰条
.page-header-strip {
  height: 4px;
  flex-shrink: 0;
  background: linear-gradient(90deg, @nordic-blue, @nordic-accent, @nordic-yellow, @nordic-blue);
  background-size: 200% 100%;
  animation: gradientShift 4s ease infinite;

  .strip-pattern {
    height: 100%;
    background: repeating-linear-gradient(
      90deg,
      transparent,
      transparent 8px,
      rgba(255,255,255,0.3) 8px,
      rgba(255,255,255,0.3) 10px
    );
  }
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.extraction-section {
  width: 100%;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 28px 24px;
  box-sizing: border-box;

  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: @nordic-border;
    border-radius: 3px;
  }
}

// ===== 页面标题 =====
.page-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.hero-content {
  .page-title {
    font-family: 'Georgia', 'Palatino', serif;
    font-size: 28px;
    font-weight: 700;
    color: @nordic-text;
    margin: 0 0 6px;
    letter-spacing: -0.5px;
  }

  .page-subtitle {
    font-size: 14px;
    color: @nordic-text-muted;
    margin: 0;
  }
}

.hero-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  border: none;

  &.primary-btn {
    background: linear-gradient(135deg, @nordic-blue, #5A8BC2);
    color: #fff;
    box-shadow: 0 4px 12px rgba(107, 155, 210, 0.25);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 18px rgba(107, 155, 210, 0.35);
    }
  }

  &.outline-btn {
    background: @nordic-surface;
    border: 2px solid @nordic-border;
    color: @nordic-text-secondary;

    &:hover {
      border-color: @nordic-blue;
      color: @nordic-blue;
      background: @nordic-blue-light;
    }
  }
}

// ===== 统计卡片 =====
.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 140px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: @nordic-surface;
  border-radius: 14px;
  border: 1px solid @nordic-border;
  transition: all 0.25s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  }
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;

  &.processing-icon {
    background: @nordic-blue-light;
    color: @nordic-blue;
  }

  &.success-icon {
    background: @nordic-green-light;
    color: @nordic-green;
  }

  &.total-icon {
    background: @nordic-accent-light;
    color: @nordic-accent;
  }
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 2px;

  .stat-value {
    font-size: 24px;
    font-weight: 700;
    color: @nordic-text;
    letter-spacing: -0.5px;
  }

  .stat-label {
    font-size: 12px;
    color: @nordic-text-muted;
    font-weight: 500;
  }
}

// ===== 内容源列表 =====
.sources-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.sources-section {
  background: @nordic-surface;
  border-radius: 18px;
  padding: 24px;
  border: 1px solid @nordic-border;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 700;
  color: @nordic-text;
  letter-spacing: -0.2px;
}

.title-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;

  &.processing-dot {
    background: @nordic-blue;
    animation: dotPulse 1.5s ease-in-out infinite;
  }

  &.success-dot {
    background: @nordic-green;
  }
}

@keyframes dotPulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.3); opacity: 0.7; }
}

.section-count {
  font-size: 13px;
  font-weight: 600;
  color: @nordic-text-muted;
  background: @nordic-bg;
  padding: 4px 12px;
  border-radius: 20px;
}

.source-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.source-card {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: @nordic-bg;
  border-radius: 14px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  animation: cardIn 0.4s cubic-bezier(0.4, 0, 0.2, 1) both;

  &:hover {
    background: @nordic-surface;
    border-color: @nordic-border;
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  }

  &.processing-card {
    border-color: rgba(107, 155, 210, 0.3);
    background: linear-gradient(135deg, @nordic-blue-light 0%, @nordic-surface 100%);
  }

  &.published {
    border-color: rgba(123, 174, 127, 0.3);

    .card-cover::after {
      border-color: @nordic-green;
    }
  }

  &.failed {
    opacity: 0.7;
  }
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-cover {
  position: relative;
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    border: 2px solid transparent;
    border-radius: 10px;
    transition: border-color 0.3s;
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: @nordic-divider;
  color: @nordic-text-muted;
}

.processing-overlay {
  position: absolute;
  inset: 0;
  background: rgba(107, 155, 210, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
}

.processing-spinner {
  color: #fff;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.published-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 3px 8px;
  background: @nordic-green;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  border-radius: 20px;
}

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: @nordic-text;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.platform-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;

  &.douyin {
    background: #FFE8EC;
    color: #FF2D55;
  }

  &.xiaohongshu {
    background: #FFE8EE;
    color: #FF2442;
  }

  &.bilibili {
    background: #E8F4FF;
    color: #00A1D6;
  }

  &.wechat {
    background: #E8F8E8;
    color: #07C160;
  }

  &.toutiao {
    background: #FFF3E8;
    color: #F85911;
  }

  &.kuaishou {
    background: #FFF0E8;
    color: #FF6A00;
  }

  &.default {
    background: @nordic-divider;
    color: @nordic-text-secondary;
  }
}

.meta-url {
  font-size: 11px;
  color: @nordic-text-muted;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 120px;
}

.content-type {
  font-size: 11px;
  color: @nordic-text-muted;
}

.card-status {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-indicator {
  width: 6px;
  height: 6px;
  border-radius: 50%;

  &.processing-indicator {
    background: @nordic-blue;
    animation: dotPulse 1.5s ease-in-out infinite;
  }
}

.status-text {
  font-size: 12px;
  color: @nordic-blue;
  font-weight: 500;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.card-time {
  font-size: 11px;
  color: @nordic-text-muted;
}

.card-actions {
  display: flex;
  gap: 6px;
}

.action-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: @nordic-text-muted;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;

  &:hover {
    background: @nordic-divider;
  }

  &.publish-icon:hover {
    background: @nordic-green-light;
    color: @nordic-green;
  }

  &.view-icon:hover {
    background: @nordic-blue-light;
    color: @nordic-blue;
  }

  &.delete-icon:hover {
    background: @nordic-red-light;
    color: @nordic-red;
  }
}

// ===== 空状态 =====
.empty-state {
  text-align: center;
  padding: 48px 24px;
}

.empty-icon {
  color: @nordic-border;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 15px;
  font-weight: 600;
  color: @nordic-text-secondary;
  margin: 0 0 6px;
}

.empty-hint {
  font-size: 13px;
  color: @nordic-text-muted;
  margin: 0;
}

// ===== 历史弹窗 =====
.history-dialog {
  :deep(.el-dialog) {
    border-radius: 18px;

    .el-dialog__header {
      padding: 20px 24px;
      border-bottom: 1px solid @nordic-border;
    }

    .el-dialog__title {
      font-weight: 700;
      color: @nordic-text;
    }

    .el-dialog__body {
      padding: 0;
    }
  }
}

.history-table-wrapper {
  overflow-x: auto;
}

.history-table {
  width: 100%;
  border-collapse: collapse;

  th, td {
    padding: 14px 16px;
    text-align: left;
    border-bottom: 1px solid @nordic-divider;
  }

  th {
    font-size: 12px;
    font-weight: 600;
    color: @nordic-text-muted;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    background: @nordic-bg;
  }

  td {
    font-size: 13px;
    color: @nordic-text;
  }

  .url-cell {
    max-width: 200px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .time-cell {
    color: @nordic-text-muted;
    font-size: 12px;
  }
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;

  &.pending {
    background: @nordic-divider;
    color: @nordic-text-secondary;
  }

  &.processing {
    background: @nordic-blue-light;
    color: @nordic-blue;
  }

  &.success {
    background: @nordic-green-light;
    color: @nordic-green-dark;
  }

  &.failed {
    background: @nordic-red-light;
    color: #C45C5C;
  }
}
</style>
