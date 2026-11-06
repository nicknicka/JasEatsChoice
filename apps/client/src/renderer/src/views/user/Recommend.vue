<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import CommonLocationPicker from '../../components/CommonLocationPicker.vue'
import CommonWeatherWidget from '../../components/CommonWeatherWidget.vue'
import { useRouter } from 'vue-router'
import { useDebounceFn } from '@vueuse/core'
import { useRecommendations } from '../../composables/useRecommendations.js'
import { useFavorites } from '../../composables/useFavorites.js'
import { useRecommendationFilters } from '../../composables/useRecommendationFilters.js'
import { RECOMMENDATION_TYPE_TAGS } from '../../constants/recommendationConstants.js'

const router = useRouter()

// 使用composables
const {
  recommendations,
  isLoading,
  refreshing,
  loadAllRecommendations,
  rejectRecommendation,
  onRefresh,
  recordClickFeedback,
  recordOrderFeedback,
  recordFavoriteBehavior
} = useRecommendations()

const { favorites, favoritesCount, initFavorites, toggleFavorite, isFavoritedItem } = useFavorites()

const {
  selectedCalorieRange,
  selectedSources,
  searchKeyword,
  sortBy,
  CALORIE_RANGES,
  RECOMMENDATION_TYPES,
  filteredAndSortedRecommendations,
  hasActiveFilters,
  resetFilters
} = useRecommendationFilters(recommendations)

// P2：关键字也属于筛选条件，变化时给出结果数量反馈
const hasQueryFeedback = computed(() => hasActiveFilters.value || searchKeyword.value.trim().length > 0)

// 定位相关
const locationPicker = ref(null)
const currentLocation = ref(null)
const locationError = ref(false)
const currentCity = ref('')

// 处理来自 CommonLocationPicker 的定位错误
const handleLocationErrorFromPicker = (error) => {
  locationError.value = true
  console.error('定位组件错误:', error)
}

// 天气相关
const weatherWidget = ref(null)
const failedImageDishIds = ref(new Set())

// UI状态
const showFilters = ref(false)
const showNutritionDetail = ref(null)

// 定位变化处理（防抖优化）
const handleLocationChanged = useDebounceFn((locationData) => {
  currentLocation.value = locationData.location
  currentCity.value = locationData.city
  // 根据位置更新推荐
  updateRecommendationsByLocation(locationData)
}, 1000)

// 天气变化处理（防抖优化）
const handleWeatherUpdated = useDebounceFn((weatherData) => {
  // 根据天气数据更新推荐
  console.log('天气数据更新:', weatherData)
  // 重新加载推荐
  loadAllRecommendations()
}, 2000)

// 根据位置更新推荐
const updateRecommendationsByLocation = (location) => {
  console.log('根据位置更新推荐:', location)
  // 这里可以添加根据经纬度获取附近商家和推荐菜品的逻辑
}

// 处理收藏点击（增强版 - 记录行为）
const handleFavoriteClick = async (item) => {
  const isFavorited = isFavoritedItem(item)
  const success = await toggleFavorite(item)

  if (success) {
    await recordFavoriteBehavior(item, !isFavorited)
    await initFavorites()
  }
}

// 处理点击菜品（记录点击反馈）
const handleDishClick = async (item) => {
  // 记录点击行为
  await recordClickFeedback(item)
}

// 处理下单（记录下单反馈）
const handleOrder = async (item) => {
  // 记录下单行为
  await recordOrderFeedback(item)

  // 跳转到商家页面
  router.push({
    path: '/user/home/merchants',
    query: {
      search: item.name.replace(/(.*推荐:|.*特色:)/, '').trim()
    }
  })
}

// 获取推荐来源标签类型
const getSourceTagType = (source) => {
  return RECOMMENDATION_TYPE_TAGS[source]?.type || 'info'
}

// 获取推荐来源标签文本
const getSourceLabel = (source) => {
  const labelMap = {
    '个性化推荐': '个性化',
    '时间推荐': '时间',
    '节日推荐': '节日',
    '系统推荐': '系统'
  }
  return labelMap[source] || source
}

// 显示营养详情
const openNutritionDetail = (item) => {
  showNutritionDetail.value = item
}

const getDishKey = (item) => String(item?.dishId || item?.id || item?.name || '')

const canShowDishImage = (item) => {
  if (!item?.image) {
    return false
  }
  return !failedImageDishIds.value.has(getDishKey(item))
}

const handleImageError = (item) => {
  const nextFailedIds = new Set(failedImageDishIds.value)
  nextFailedIds.add(getDishKey(item))
  failedImageDishIds.value = nextFailedIds
}

// 格式化分数显示（直接使用后端返回的百分比值）
const formatScore = (score) => {
  // 如果 score 已经是百分比格式（大于 1），直接使用
  // 如果 score 是小数格式（0-1），转换为百分比
  if (score > 1) {
    return Math.round(score)
  }
  return Math.round(score * 100)
}

// 页面加载时获取推荐数据（定位由 CommonLocationPicker 组件自动处理）
onMounted(async () => {
  await loadAllRecommendations()
  initFavorites()
})

watch(recommendations, () => {
  failedImageDishIds.value = new Set()
})
</script>

<template>
  <div class="nordic-recommend">
    <!-- 引入定位和天气组件 -->
    <CommonLocationPicker
      ref="locationPicker"
      auto-locate
      @location-changed="handleLocationChanged"
      @location-error="handleLocationErrorFromPicker"
    />
    <CommonWeatherWidget
      ref="weatherWidget"
      :city="currentCity"
      @weather-updated="handleWeatherUpdated"
    />

    <!-- 页面标题 -->
    <div class="nordic-page-header">
      <h2>我的推荐</h2>
      <button class="refresh-btn" @click="onRefresh" :class="{ spinning: refreshing }">
        <span class="refresh-icon">&#8635;</span>
      </button>
    </div>

    <!-- 搜索与筛选：定位提示降级为工具栏内侧的紧凑警示块 -->
    <div class="toolbar-row">
      <div class="search-box">
        <span class="search-icon">&#128269;</span>
        <input
          v-model="searchKeyword"
          placeholder="搜索推荐菜品..."
          class="search-input"
        />
      </div>
      <button
        class="filter-toggle"
        :class="{ active: showFilters }"
        @click="showFilters = !showFilters"
      >
        筛选 <span class="filter-arrow" :class="{ open: showFilters }">&#x276F;</span>
      </button>
      <select v-model="sortBy" class="sort-select">
        <option value="default">默认排序</option>
        <option value="calories_asc">卡路里 ↑</option>
        <option value="calories_desc">卡路里 ↓</option>
        <option value="rating_desc">评分最高</option>
        <option value="rating_asc">评分最低</option>
      </select>
      <div v-if="locationError" class="location-warn" title="定位服务不可用，推荐准确性可能受影响">
        <el-icon class="location-warn-icon"><Warning /></el-icon>
        <span>定位服务不可用，推荐准确性可能受影响</span>
      </div>
    </div>

    <!-- 筛选面板 -->
    <transition name="nordic-slide">
      <div class="filter-panel" v-show="showFilters">
        <div class="filter-group">
          <div class="filter-label">卡路里范围</div>
          <div class="filter-options">
            <label class="filter-chip" :class="{ selected: selectedCalorieRange === 0 }">
              <input type="radio" :value="0" v-model="selectedCalorieRange" /> 全部
            </label>
            <label
              v-for="range in CALORIE_RANGES"
              :key="range.id"
              class="filter-chip"
              :class="{ selected: selectedCalorieRange === range.id }"
            >
              <input type="radio" :value="range.id" v-model="selectedCalorieRange" /> {{ range.label }}
            </label>
          </div>
        </div>
        <div class="filter-group">
          <div class="filter-label">推荐来源</div>
          <div class="filter-options">
            <label class="filter-chip" :class="{ selected: selectedSources.length === Object.values(RECOMMENDATION_TYPES).length }">
              <input
                type="checkbox"
                :checked="selectedSources.length === Object.values(RECOMMENDATION_TYPES).length"
                @change="(e) => e.target.checked ? (selectedSources = Object.values(RECOMMENDATION_TYPES)) : (selectedSources = [])"
              /> 全部
            </label>
            <label
              v-for="(label, key) in RECOMMENDATION_TYPES"
              :key="key"
              class="filter-chip"
              :class="{ selected: selectedSources.includes(label) }"
            >
              <input type="checkbox" :value="label" v-model="selectedSources" /> {{ label }}
            </label>
          </div>
        </div>
        <div class="filter-footer">
          <button class="filter-reset" @click="resetFilters">重置筛选</button>
        </div>
      </div>
    </transition>

    <!-- 筛选结果提示（P2：筛选/关键字变化时反馈结果数量） -->
    <div class="filter-info" v-if="hasQueryFeedback">
      <span>找到 {{ filteredAndSortedRecommendations.length }} 个推荐</span>
      <button class="clear-link" @click="resetFilters">清除</button>
    </div>

    <!-- 加载中 -->
    <div class="loading-state" v-if="isLoading && recommendations.length === 0">
      <div class="loading-skeleton" v-for="n in 3" :key="n">
        <div class="skel-img"></div>
        <div class="skel-lines">
          <div class="skel-line long"></div>
          <div class="skel-line mid"></div>
          <div class="skel-line short"></div>
        </div>
      </div>
    </div>

    <!-- 推荐列表 -->
    <transition-group name="card-list" tag="div" class="recommend-list" v-else-if="filteredAndSortedRecommendations.length > 0">
      <div
        v-for="item in filteredAndSortedRecommendations"
        :key="item.id"
        class="recommend-item"
        @click="handleDishClick(item)"
      >
        <!-- 卡片顶部：图片 + 信息 -->
        <div class="item-top">
          <div class="item-image">
            <img
              v-if="canShowDishImage(item)"
              :src="item.image"
              :alt="item.name"
              loading="lazy"
              @error="handleImageError(item)"
            />
            <span v-else class="img-placeholder">{{ item.name?.charAt(0) || '?' }}</span>
          </div>
          <div class="item-info">
            <div class="item-name">{{ item.name }}</div>
            <div class="item-meta">
              <span class="meta-tag" v-if="item.type">{{ item.type }}</span>
              <span class="meta-source" v-if="item.recommendSource">{{ getSourceLabel(item.recommendSource) }}</span>
            </div>
            <!-- 匹配度 -->
            <div class="match-bar" v-if="item.score !== undefined && item.score !== null">
              <div class="match-track">
                <div
                  class="match-fill"
                  :style="{ width: formatScore(item.score) + '%' }"
                ></div>
              </div>
              <span class="match-val">匹配度 {{ formatScore(item.score) }}%</span>
            </div>
          </div>
        </div>

        <!-- 卡路里 -->
        <div class="item-calories" v-if="item.calories">
          <span class="cal-val">{{ item.calories }}</span>
          <span class="cal-unit">kcal</span>
          <button class="cal-detail" v-if="item.nutrition" @click.stop="openNutritionDetail(item)">营养详情</button>
        </div>

        <!-- 标签 -->
        <div class="item-tags" v-if="item.tagsWithType && item.tagsWithType.length">
          <span v-for="tag in item.tagsWithType" :key="tag.name" class="tag-chip">{{ tag.name }}</span>
        </div>

        <!-- 推荐理由 -->
        <div class="item-reason" v-if="item.reason">
          {{ item.reason }}
        </div>

        <!-- 操作按钮 -->
        <div class="item-actions">
          <button class="act-btn primary" @click.stop="handleOrder(item)">下单</button>
          <button
            class="act-btn"
            :class="{ favorited: isFavoritedItem(item) }"
            @click.stop="handleFavoriteClick(item)"
          >
            {{ isFavoritedItem(item) ? '★ 已收藏' : '☆ 收藏' }}
          </button>
          <button class="act-btn ghost" @click.stop="rejectRecommendation(item)">不感兴趣</button>
        </div>
      </div>
    </transition-group>

    <!-- 空状态 -->
    <div class="empty-state" v-else>
      <div class="empty-icon">🍽</div>
      <div class="empty-title">{{ hasActiveFilters ? '没有找到符合条件的推荐' : '暂无推荐数据' }}</div>
      <div class="empty-desc">{{ hasActiveFilters ? '试试调整筛选条件' : '系统正在努力为您生成个性化推荐' }}</div>
      <button class="act-btn primary" @click="hasActiveFilters ? resetFilters() : onRefresh()">
        {{ hasActiveFilters ? '清除筛选' : '重新获取' }}
      </button>
    </div>

    <!-- 营养详情弹窗 -->
    <el-dialog v-model="showNutritionDetail" title="营养成分" width="380px" :append-to-body="true">
      <div class="nutrition-modal" v-if="showNutritionDetail?.nutrition">
        <div class="nutri-row">
          <span>碳水化合物</span>
          <span class="nutri-val">{{ showNutritionDetail.nutrition.carbs }}g</span>
        </div>
        <div class="nutri-row">
          <span>蛋白质</span>
          <span class="nutri-val">{{ showNutritionDetail.nutrition.protein }}g</span>
        </div>
        <div class="nutri-row">
          <span>脂肪</span>
          <span class="nutri-val">{{ showNutritionDetail.nutrition.fat }}g</span>
        </div>
      </div>
      <div v-else class="nutri-empty">暂无详细营养信息</div>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.nordic-recommend {
  .nordic-page-container();
  max-width: 900px;
  margin: 0 auto;
}

// --- 页面标题（§4.7 标准版：--text-h1 + --space-6） ---
.nordic-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);

  h2 {
    font-size: var(--text-h1);
    line-height: 32px;
    font-weight: var(--weight-semibold);
    color: var(--ink-900);
    margin: 0;
    letter-spacing: var(--tracking-tight);
  }

  .refresh-btn {
    width: 36px;
    height: 36px;
    border: 1px solid @nordic-border;
    background: @nordic-surface;
    border-radius: @nordic-radius-sm;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: border-color var(--duration-fast) var(--ease-out),
                color var(--duration-fast) var(--ease-out);

    &:hover {
      border-color: var(--brand-500);
      color: var(--brand-500);
    }

    &.spinning .refresh-icon {
      animation: spin var(--spin-duration) linear infinite;
    }

    .refresh-icon {
      font-size: 18px;
    }
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// --- 定位警告：吸附到工具栏，降级为紧凑警示块（§3.1 语义色小面积使用、§4.5 不与内容抢位） ---
.location-warn {
  flex: 0 1 auto;
  min-width: 0;
  max-width: 260px;
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-2);
  background: var(--warning-bg);
  color: var(--warning);
  border-radius: var(--radius-sm);
  font-size: var(--text-caption);
  line-height: 18px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  span {
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .location-warn-icon {
    font-size: 14px;
    flex-shrink: 0;
  }
}

// --- 工具栏 ---
.toolbar-row {
  display: flex;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
  align-items: center;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: @nordic-surface;
  border: 1px solid @nordic-border;
  border-radius: @nordic-radius-md;
  transition: border-color var(--duration-fast) var(--ease-out);

  &:focus-within {
    border-color: var(--brand-500);
  }

  .search-icon {
    color: @nordic-text-muted;
    font-size: 14px;
  }

  .search-input {
    flex: 1;
    border: none;
    outline: none;
    background: transparent;
    font-size: @nordic-text-base;
    color: @nordic-text;

    &::placeholder {
      color: @nordic-text-muted;
    }
  }
}

.filter-toggle {
  padding: 8px 16px;
  background: @nordic-surface;
  border: 1px solid @nordic-border;
  border-radius: @nordic-radius-md;
  cursor: pointer;
  font-size: @nordic-text-sm;
  color: @nordic-text-secondary;
  transition: border-color var(--duration-fast) var(--ease-out),
              color var(--duration-fast) var(--ease-out),
              background-color var(--duration-fast) var(--ease-out);
  white-space: nowrap;

  &.active {
    border-color: var(--brand-500);
    color: var(--brand-500);
    background: var(--brand-100);
  }

  .filter-arrow {
    font-size: 10px;
    transition: transform var(--duration-fast) var(--ease-in-out);
    display: inline-block;

    &.open { transform: rotate(90deg); }
  }
}

.sort-select {
  padding: 8px 12px;
  background: @nordic-surface;
  border: 1px solid @nordic-border;
  border-radius: @nordic-radius-md;
  font-size: @nordic-text-sm;
  color: @nordic-text-secondary;
  cursor: pointer;
  outline: none;
  min-width: 120px;
  transition: border-color var(--duration-fast) var(--ease-out);

  &:focus {
    border-color: var(--brand-500);
  }
}

// --- 筛选面板 ---
.filter-panel {
  background: @nordic-surface;
  border: 1px solid @nordic-border;
  border-radius: @nordic-radius-lg;
  padding: @nordic-space-lg;
  margin-bottom: @nordic-space-md;

  .filter-group {
    margin-bottom: @nordic-space-md;

    &:last-child { margin-bottom: 0; }
  }

  .filter-label {
    font-size: @nordic-text-sm;
    font-weight: 600;
    color: @nordic-text;
    margin-bottom: 10px;
  }

  .filter-options {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .filter-chip {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 6px 14px;
    background: @nordic-bg;
    border: 1px solid @nordic-border;
    border-radius: @nordic-radius-pill;
    font-size: @nordic-text-sm;
    color: @nordic-text-secondary;
    cursor: pointer;
    transition: border-color var(--duration-fast) var(--ease-out),
                color var(--duration-fast) var(--ease-out),
                background-color var(--duration-fast) var(--ease-out);

    input { display: none; }

    &.selected {
      background: var(--brand-100);
      border-color: var(--brand-500);
      color: var(--brand-500);
    }

    &:hover {
      border-color: var(--brand-500);
    }
  }

  .filter-footer {
    display: flex;
    justify-content: flex-end;
    padding-top: @nordic-space-md;
    border-top: 1px solid @nordic-divider;

    .filter-reset {
      padding: 6px 16px;
      border: 1px solid @nordic-border;
      background: transparent;
      border-radius: @nordic-radius-md;
      font-size: @nordic-text-sm;
      color: @nordic-text-secondary;
      cursor: pointer;
      transition: border-color var(--duration-fast) var(--ease-out),
                  color var(--duration-fast) var(--ease-out);

      &:hover {
        border-color: var(--brand-500);
        color: var(--brand-500);
      }
    }
  }
}

// --- 筛选信息 ---
.filter-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-2) var(--space-4);
  background: var(--canvas-sunken);
  border-radius: var(--radius-md);
  font-size: var(--text-body-sm);
  color: var(--ink-500);
  margin-bottom: var(--space-4);

  .clear-link {
    background: none;
    border: none;
    color: var(--brand-600);
    cursor: pointer;
    font-size: var(--text-body-sm);
    font-weight: var(--weight-medium);
    transition: color var(--duration-fast) var(--ease-out);

    &:hover { color: var(--brand-500); text-decoration: underline; }
  }
}

// --- 加载骨架 ---
.loading-state {
  display: flex;
  flex-direction: column;
  gap: @nordic-space-md;
}

.loading-skeleton {
  display: flex;
  gap: @nordic-space-md;
  padding: @nordic-space-lg;
  background: @nordic-surface;
  border-radius: @nordic-radius-lg;
  border: 1px solid @nordic-border;

  .skel-img {
    width: 64px;
    height: 64px;
    border-radius: @nordic-radius-md;
    background: @nordic-divider;
    animation: pulse 1.5s ease-in-out infinite;
  }

  .skel-lines { flex: 1; }

  .skel-line {
    height: 12px;
    background: @nordic-divider;
    border-radius: 6px;
    margin-bottom: 8px;
    animation: pulse 1.5s ease-in-out infinite;

    &.long { width: 70%; }
    &.mid { width: 50%; }
    &.short { width: 30%; }
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

// --- 推荐卡片列表 ---
.recommend-list {
  display: flex;
  flex-direction: column;
  gap: @nordic-space-md;
}

.recommend-item {
  background: @nordic-surface;
  border: 1px solid @nordic-border;
  border-radius: @nordic-radius-lg;
  padding: @nordic-space-lg;
  cursor: pointer;
  transition: border-color var(--duration-fast) var(--ease-out),
              box-shadow var(--duration-fast) var(--ease-out),
              transform var(--duration-fast) var(--ease-out);

  &:hover {
    border-color: var(--line-strong);
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
  }
}

.item-top {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-2);
}

.item-image {
  width: 96px;
  height: 72px;
  border-radius: @nordic-radius-md;
  overflow: hidden;
  flex-shrink: 0;
  background: @nordic-bg;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .img-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: @nordic-text-muted;
    background: @nordic-divider;
  }
}

.item-info {
  flex: 1;
  min-width: 0;

  .item-name {
    font-size: @nordic-text-md;
    font-weight: 600;
    color: @nordic-text;
    margin-bottom: 6px;
  }

  .item-meta {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;

    .meta-tag {
      .nordic-pill-tag(@nordic-bg, @nordic-text-secondary);
    }

    .meta-source {
      .nordic-pill-tag(@nordic-accent-light, @nordic-accent);
    }
  }
}

// 匹配度：全列表统一品牌强调色，带"匹配度"文字标签
.match-bar {
  display: flex;
  align-items: center;
  gap: var(--space-2);

  .match-track {
    flex: 1;
    height: 4px;
    background: var(--line);
    border-radius: var(--radius-full);
    overflow: hidden;
  }

  .match-fill {
    height: 100%;
    border-radius: var(--radius-full);
    background: var(--brand-500);
    transition: width var(--duration-slow) var(--ease-out);
  }

  .match-val {
    font-size: var(--text-caption);
    font-weight: var(--weight-semibold);
    color: var(--ink-500);
    white-space: nowrap;
  }
}

// 卡路里
.item-calories {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-bottom: var(--space-2);

  .cal-val {
    font-size: var(--text-h2);
    font-weight: var(--weight-bold);
    color: var(--brand-500);
  }

  .cal-unit {
    font-size: @nordic-text-xs;
    color: @nordic-text-muted;
  }

  .cal-detail {
    margin-left: auto;
    background: none;
    border: none;
    color: var(--brand-600);
    font-size: var(--text-body-sm);
    cursor: pointer;
    padding: 2px 8px;
    transition: color var(--duration-fast) var(--ease-out);

    &:hover { color: var(--brand-500); text-decoration: underline; }
  }
}

// 标签
.item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-2);

  .tag-chip {
    padding: 2px 10px;
    background: @nordic-bg;
    border-radius: @nordic-radius-pill;
    font-size: @nordic-text-xs;
    color: @nordic-text-secondary;
  }
}

// 推荐理由
.item-reason {
  font-size: var(--text-body-sm);
  color: var(--ink-500);
  line-height: 20px;
  margin-bottom: var(--space-3);
}

// 操作按钮
.item-actions {
  display: flex;
  gap: var(--space-2);
  padding-top: var(--space-3);
  border-top: 1px solid var(--line);
}

.act-btn {
  padding: 6px 16px;
  border: 1px solid @nordic-border;
  background: transparent;
  border-radius: @nordic-radius-md;
  font-size: @nordic-text-sm;
  color: @nordic-text-secondary;
  cursor: pointer;
  transition: border-color var(--duration-fast) var(--ease-out),
              color var(--duration-fast) var(--ease-out),
              background-color var(--duration-fast) var(--ease-out);

  &:hover {
    border-color: var(--line-strong);
  }

  &.primary {
    background: var(--brand-500);
    border-color: var(--brand-500);
    color: var(--surface);

    &:hover {
      background: var(--brand-600);
      border-color: var(--brand-600);
    }
  }

  &.favorited {
    color: var(--brand-600);
    border-color: var(--brand-500);
  }

  &.ghost {
    color: @nordic-text-muted;
    border-color: transparent;

    &:hover {
      color: @nordic-text-secondary;
      border-color: @nordic-border;
    }
  }
}

// --- 空状态 ---
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: @nordic-surface;
  border: 1px dashed @nordic-border;
  border-radius: @nordic-radius-lg;

  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
    opacity: 0.6;
  }

  .empty-title {
    font-size: @nordic-text-lg;
    font-weight: 600;
    color: @nordic-text;
    margin-bottom: 8px;
  }

  .empty-desc {
    font-size: @nordic-text-sm;
    color: @nordic-text-muted;
    margin-bottom: 24px;
  }
}

// --- 营养弹窗 ---
.nutrition-modal {
  .nutri-row {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid @nordic-divider;
    font-size: @nordic-text-base;
    color: @nordic-text-secondary;

    &:last-child { border-bottom: none; }

    .nutri-val {
      font-weight: 600;
      color: @nordic-text;
    }
  }
}

.nutri-empty {
  text-align: center;
  padding: 32px;
  color: @nordic-text-muted;
  font-size: @nordic-text-sm;
}

// --- 卡片过渡（§3.5：进场 200ms + 列表重排 240ms，只动 transform/opacity） ---
.card-list-enter-active {
  transition: opacity var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out);
}

.card-list-leave-active {
  transition: opacity var(--duration-fast) var(--ease-standard),
              transform var(--duration-fast) var(--ease-standard);
}

.card-list-enter-from {
  opacity: 0;
  transform: translateY(4px);
}

.card-list-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.card-list-move {
  transition: transform var(--duration-slow) var(--ease-in-out);
}

// --- 滑入过渡 ---
.nordic-slide-enter-active,
.nordic-slide-leave-active {
  transition: max-height var(--duration-base) var(--ease-standard),
              opacity var(--duration-base) var(--ease-standard),
              margin-bottom var(--duration-base) var(--ease-standard);
  max-height: 400px;
  overflow: hidden;
}

.nordic-slide-enter-from,
.nordic-slide-leave-to {
  max-height: 0;
  opacity: 0;
  margin-bottom: 0;
}

// --- 响应式 ---
@media (max-width: 640px) {
  .toolbar-row {
    flex-wrap: wrap;

    .search-box { width: 100%; order: 1; }
    .filter-toggle { order: 2; }
    .sort-select { order: 3; flex: 1; min-width: 0; }
    .location-warn { order: 4; flex: 1 1 100%; max-width: none; }
  }
}
</style>
