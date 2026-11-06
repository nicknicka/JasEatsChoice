<script setup>
import { ref, onMounted, computed } from 'vue'
import { VideoCamera, Document, Search, Plus, User, Grid, Refresh, Shop, MagicStick, CircleCheck, Star } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import { ElMessage } from 'element-plus'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

const router = useRouter()
const route = useRoute()

// 检测跳转来源：从侧边栏跳转时带有 fromSidebar=true 参数
const fromSidebar = computed(() => route.query.fromSidebar === 'true')

// 是否显示返回按钮
const showBackButton = computed(() => !fromSidebar.value)

// 教程数据
const tutorials = ref([])
const loading = ref(false)

const normalizeTutorial = (tutorial = {}) => {
  return {
    ...tutorial,
    sourceType: tutorial.sourceType || tutorial.source_type || ''
  }
}

// 筛选条件
const searchKeyword = ref('')
const selectedType = ref('all') // all, video, article
const selectedSource = ref('all') // all, ADMIN, MERCHANT, USER, AI_GENERATED
const selectedDifficulty = ref('all') // all, BEGINNER, INTERMEDIATE, ADVANCED

// 统计数据
const stats = computed(() => {
  const all = tutorials.value
  return {
    total: all.length,
    video: all.filter(t => t.type === 'video').length,
    article: all.filter(t => t.type === 'article').length
  }
})

// 过滤后的教程列表
const filteredTutorials = computed(() => {
  let result = tutorials.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(t =>
      (t.title && t.title.toLowerCase().includes(keyword)) ||
      (t.content && t.content.toLowerCase().includes(keyword))
    )
  }

  // 类型过滤
  if (selectedType.value !== 'all') {
    result = result.filter(t => t.type === selectedType.value)
  }

  // 来源过滤
  if (selectedSource.value !== 'all') {
    result = result.filter(t => t.sourceType === selectedSource.value)
  }

  // 难度过滤
  if (selectedDifficulty.value !== 'all') {
    result = result.filter(t => t.difficulty === selectedDifficulty.value)
  }

  return result
})

// 获取教程数据
const fetchTutorials = async () => {
  loading.value = true
  try {
  const response = await api.get(API_CONFIG.tutorial.list)
    if (Array.isArray(response)) {
      tutorials.value = response.map(normalizeTutorial)
    } else if (Array.isArray(response?.data)) {
      tutorials.value = response.data.map(normalizeTutorial)
    }
  } catch (error) {
    console.error('加载教程列表失败:', error)
    tutorials.value = []
    ElMessage.error('加载教程列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 获取难度名称
const getDifficultyName = (difficulty) => {
  const map = {
    BEGINNER: '初级',
    INTERMEDIATE: '中级',
    ADVANCED: '高级'
  }
  return map[difficulty] || ''
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  selectedType.value = 'all'
  selectedSource.value = 'all'
  selectedDifficulty.value = 'all'
}

// 页面加载时获取数据
onMounted(() => {
  fetchTutorials()
})
</script>

<template>
  <div class="tutorials-plaza-container">
    <AppPageHeader title="教程广场" subtitle="探索美食制作技巧，分享你的独家秘方">
      <template #actions>
        <CommonBackButton v-if="showBackButton" type="default" />
        <el-button @click="router.push('/user/home/tutorials/my')">
          <el-icon><User /></el-icon>
          我的教程
        </el-button>
        <el-button type="primary" @click="router.push('/user/home/tutorials/publish')">
          <el-icon><Plus /></el-icon>
          发布教程
        </el-button>
      </template>
    </AppPageHeader>

    <!-- 统计摘要：保留总览，不占用首屏的卡片空间 -->
    <div class="tutorials-summary">
      <span class="summary-item">
        <strong class="summary-value">{{ stats.total }}</strong>
        全部教程
      </span>
      <span class="summary-divider" aria-hidden="true"></span>
      <span class="summary-item">
        <strong class="summary-value">{{ stats.video }}</strong>
        视频教程
      </span>
      <span class="summary-divider" aria-hidden="true"></span>
      <span class="summary-item">
        <strong class="summary-value">{{ stats.article }}</strong>
        图文指南
      </span>
    </div>

    <!-- 搜索和筛选栏 -->
    <div class="filter-bar">
      <div class="filter-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索教程标题或内容..."
          :prefix-icon="Search"
          clearable
          class="search-input"
        />
      </div>

      <div class="filter-right">
        <el-select v-model="selectedType" placeholder="教程类型" class="filter-select">
          <template #prefix>
            <el-icon><Grid /></el-icon>
          </template>
          <el-option label="全部类型" value="all">
            <span class="option-label">
              <el-icon><Grid /></el-icon>
              全部类型
            </span>
          </el-option>
          <el-option label="视频教程" value="video">
            <span class="option-label">
              <el-icon><VideoCamera /></el-icon>
              视频教程
            </span>
          </el-option>
          <el-option label="图文指南" value="article">
            <span class="option-label">
              <el-icon><Document /></el-icon>
              图文指南
            </span>
          </el-option>
        </el-select>

        <el-select v-model="selectedSource" placeholder="来源" class="filter-select">
          <template #prefix>
            <el-icon><Shop /></el-icon>
          </template>
          <el-option label="全部来源" value="all">
            <span class="option-label">
              <el-icon><Grid /></el-icon>
              全部来源
            </span>
          </el-option>
          <el-option label="官方认证" value="ADMIN">
            <span class="option-label">
              <el-icon><CircleCheck /></el-icon>
              官方认证
            </span>
          </el-option>
          <el-option label="商家贡献" value="MERCHANT">
            <span class="option-label">
              <el-icon><Shop /></el-icon>
              商家贡献
            </span>
          </el-option>
          <el-option label="用户贡献" value="USER">
            <span class="option-label">
              <el-icon><User /></el-icon>
              用户贡献
            </span>
          </el-option>
          <el-option label="AI生成" value="AI_GENERATED">
            <span class="option-label">
              <el-icon><MagicStick /></el-icon>
              AI生成
            </span>
          </el-option>
        </el-select>

        <el-select v-model="selectedDifficulty" placeholder="难度" class="filter-select">
          <template #prefix>
            <el-icon><Star /></el-icon>
          </template>
          <el-option label="全部难度" value="all">
            <span class="option-label">
              <el-icon><Grid /></el-icon>
              全部难度
            </span>
          </el-option>
          <el-option label="初级" value="BEGINNER">
            <span class="option-label">
              <span class="difficulty-icon">🌱</span>
              初级
            </span>
          </el-option>
          <el-option label="中级" value="INTERMEDIATE">
            <span class="option-label">
              <span class="difficulty-icon">🌿</span>
              中级
            </span>
          </el-option>
          <el-option label="高级" value="ADVANCED">
            <span class="option-label">
              <span class="difficulty-icon">🌳</span>
              高级
            </span>
          </el-option>
        </el-select>

        <el-button @click="resetFilters" :icon="Refresh" class="reset-btn">重置</el-button>
      </div>
    </div>

    <!-- 活跃筛选信息 -->
    <div v-if="selectedType !== 'all' || selectedSource !== 'all' || selectedDifficulty !== 'all' || searchKeyword" class="filter-info">
      <span class="filter-info-text">
        当前筛选:
        <el-tag v-if="selectedType !== 'all'" size="small" closable @close="selectedType = 'all'" class="filter-tag">
          {{ selectedType === 'video' ? '视频教程' : '图文指南' }}
        </el-tag>
        <el-tag v-if="selectedSource !== 'all'" size="small" closable @close="selectedSource = 'all'" class="filter-tag">
          {{ selectedSource === 'ADMIN' ? '官方认证' : selectedSource === 'MERCHANT' ? '商家贡献' : selectedSource === 'USER' ? '用户贡献' : 'AI生成' }}
        </el-tag>
        <el-tag v-if="selectedDifficulty !== 'all'" size="small" closable @close="selectedDifficulty = 'all'" class="filter-tag">
          {{ getDifficultyName(selectedDifficulty) }}
        </el-tag>
        <el-tag v-if="searchKeyword" size="small" closable @close="searchKeyword = ''" class="filter-tag">
          搜索: {{ searchKeyword }}
        </el-tag>
      </span>
      <el-button text type="primary" @click="resetFilters" :icon="Refresh">清除全部</el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- 空状态 -->
    <div v-else-if="filteredTutorials.length === 0" class="empty-container">
      <el-empty description="没有找到符合条件的教程" :image-size="180">
        <template #image>
          <div class="empty-icon">
            <el-icon :size="100"><Document /></el-icon>
          </div>
        </template>
        <el-button type="primary" @click="resetFilters" :icon="Refresh">清除筛选条件</el-button>
      </el-empty>
    </div>

    <!-- 教程列表 -->
    <transition-group name="tutorial-fade" v-else tag="div" class="tutorial-grid">
      <el-card
        v-for="tutorial in filteredTutorials"
        :key="tutorial.id"
        class="tutorial-card"
        shadow="never"
        @click="router.push(`/user/home/tutorials/${tutorial.id}`)"
      >
        <div class="tutorial-header">
          <div class="tutorial-attributes">
            <span class="type-badge" :class="tutorial.type">
              <el-icon :size="16">
                <component :is="tutorial.type === 'video' ? VideoCamera : Document" />
              </el-icon>
              <span>{{ tutorial.type === 'video' ? '视频' : '图文' }}</span>
            </span>
            <el-tag
              v-if="tutorial.difficulty"
              type="info"
              size="small"
              effect="plain"
              class="difficulty-tag"
            >
              {{ getDifficultyName(tutorial.difficulty) }}
            </el-tag>
          </div>
          <div v-if="tutorial.rating" class="rating-badge">
            <el-icon><Star /></el-icon>
            <span>{{ tutorial.rating }}</span>
          </div>
        </div>

        <div class="tutorial-content">
          <h3 class="tutorial-title">{{ tutorial.title }}</h3>
        </div>
      </el-card>
    </transition-group>
  </div>
</template>

<style scoped lang="less">
/* 页面颜色、间距、圆角与字阶全部来自 tokens.css（§3、§7.1）。 */
.tutorials-plaza-container {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

/* 页头动作区控件高度取 --control-height-md（§3.4、§4.7）。 */
.tutorials-plaza-container :deep(.app-page-header__actions .el-button) {
  height: var(--control-height-md);
  min-height: var(--control-height-md);
}

.tutorials-summary {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
  transition: border-color var(--duration-fast) var(--ease-standard);
}

.summary-item {
  display: inline-flex;
  align-items: baseline;
  gap: var(--space-2);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.summary-value {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-h3);
  font-variant-numeric: tabular-nums;
  line-height: 24px;
}

.summary-divider {
  width: 1px;
  height: var(--space-5);
  background: var(--line);
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
  padding: var(--space-4);
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
  transition:
    border-color var(--duration-fast) var(--ease-standard),
    box-shadow var(--duration-fast) var(--ease-standard);
}

.filter-bar:focus-within {
  border-color: var(--brand-300);
  box-shadow:
    inset 0 1px 0 var(--surface-highlight),
    var(--shadow-sm);
}

.filter-left {
  flex: 1 1 320px;
  min-width: 240px;
}

.search-input {
  width: 100%;
}

.filter-right {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.filter-select {
  width: 140px;
}

.filter-bar :deep(.el-input__wrapper),
.filter-bar :deep(.el-select__wrapper) {
  box-sizing: border-box;
  height: var(--control-height-sm);
  min-height: var(--control-height-sm);
  border-radius: var(--radius-md);
  transition: box-shadow var(--duration-fast) var(--ease-standard);
}

.filter-select .option-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.reset-btn {
  height: var(--control-height-sm);
  min-height: var(--control-height-sm);
  border-radius: var(--radius-md);
  transition:
    transform var(--duration-fast) var(--ease-out),
    border-color var(--duration-fast) var(--ease-standard),
    color var(--duration-fast) var(--ease-standard);
}

.reset-btn:hover {
  transform: translateY(-1px);
}

.reset-btn:active {
  transform: translateY(0);
}

.filter-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: var(--canvas-sunken);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
}

.filter-info-text {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-2);
  color: var(--ink-700);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.filter-info :deep(.el-button) {
  height: var(--control-height-sm);
  min-height: var(--control-height-sm);
}

.loading-container {
  padding: var(--space-10);
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
}

.empty-container {
  padding: var(--space-8) var(--space-5);
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  text-align: center;
}

.empty-icon {
  margin-bottom: var(--space-5);
}

.empty-icon .el-icon {
  color: var(--ink-400);
}

.tutorial-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--card-gap);
}

.tutorial-card {
  cursor: pointer;
  overflow: hidden;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
  transition:
    transform var(--duration-fast) var(--ease-out),
    border-color var(--duration-fast) var(--ease-standard),
    box-shadow var(--duration-fast) var(--ease-standard);
}

.tutorial-card:hover {
  transform: translateY(-2px);
  border-color: var(--line-strong);
  box-shadow:
    inset 0 1px 0 var(--surface-highlight),
    var(--shadow-md);
}

.tutorial-card:active {
  transform: translateY(0);
}

.tutorial-card :deep(.el-card__body) {
  padding: var(--card-padding);
}

.tutorial-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--line);
}

.tutorial-attributes {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-2);
  min-width: 0;
}

.type-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--neutral-bg);
  color: var(--ink-700);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  line-height: 20px;
}

.type-badge .el-icon {
  color: var(--brand-500);
  font-size: 16px;
}

.difficulty-tag {
  border-radius: var(--radius-full);
  font-weight: var(--weight-medium);
}

.rating-badge {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--warning-bg);
  color: var(--warning);
  font-size: var(--text-body-sm);
  font-variant-numeric: tabular-nums;
  font-weight: var(--weight-semibold);
  line-height: 20px;
}

.rating-badge .el-icon {
  font-size: 14px;
}

.tutorial-title {
  display: -webkit-box;
  min-height: 48px;
  margin: 0;
  overflow: hidden;
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  line-height: 24px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  text-overflow: ellipsis;
}

.tutorial-fade-enter-active,
.tutorial-fade-leave-active {
  transition:
    opacity var(--duration-base) var(--ease-standard),
    transform var(--duration-base) var(--ease-standard);
}

.tutorial-fade-enter-from {
  opacity: 0;
  transform: translateY(var(--space-3));
}

.tutorial-fade-leave-to {
  opacity: 0;
  transform: translateY(calc(-1 * var(--space-3)));
}

@media (max-width: 1200px) {
  .tutorial-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 768px) {
  .tutorials-plaza-container {
    padding: 0 var(--space-4) var(--space-8);
  }

  .tutorials-summary {
    gap: var(--space-3);
    padding: var(--space-3) var(--space-4);
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-left {
    min-width: 0;
    flex: 1 1 auto;
  }

  .filter-right {
    justify-content: space-between;
  }

  .filter-select {
    flex: 1 1 120px;
    width: auto;
  }

  .filter-info {
    flex-direction: column;
    align-items: stretch;
  }

  .tutorial-grid {
    grid-template-columns: 1fr;
  }
}
</style>
