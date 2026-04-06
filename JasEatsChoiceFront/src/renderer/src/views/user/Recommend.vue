<script setup>
import { ref, onMounted } from 'vue'
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

  // 记录收藏/取消收藏行为
  await recordFavoriteBehavior(item, !isFavorited)

  // 切换收藏状态
  await toggleFavorite(item)

  // 更新收藏状态
  initFavorites()
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

// 格式化分数显示（直接使用后端返回的百分比值）
const formatScore = (score) => {
  // 如果 score 已经是百分比格式（大于 1），直接使用
  // 如果 score 是小数格式（0-1），转换为百分比
  if (score > 1) {
    return Math.round(score)
  }
  return Math.round(score * 100)
}

// 获取分数等级（用于样式）
const getScoreLevel = (score) => {
  const percentage = score > 1 ? score : score * 100
  if (percentage >= 90) return 'excellent'
  if (percentage >= 80) return 'good'
  if (percentage >= 70) return 'medium'
  return 'low'
}

// 页面加载时获取推荐数据（定位由 CommonLocationPicker 组件自动处理）
onMounted(async () => {
  await loadAllRecommendations()
  initFavorites()
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

    <!-- 定位警告提示 -->
    <div v-if="locationError" class="location-warn">
      定位服务不可用，推荐准确性可能受影响
    </div>

    <!-- 搜索与筛选 -->
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

    <!-- 筛选结果提示 -->
    <div class="filter-info" v-if="hasActiveFilters">
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
            <img v-if="item.image" :src="item.image" :alt="item.name" loading="lazy" />
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
                  :class="getScoreLevel(item.score)"
                  :style="{ width: formatScore(item.score) + '%' }"
                ></div>
              </div>
              <span class="match-val" :class="getScoreLevel(item.score)">{{ formatScore(item.score) }}%</span>
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

// --- 页面标题 ---
.nordic-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: @nordic-space-lg;

  h2 {
    font-size: @nordic-text-xl;
    font-weight: 700;
    color: @nordic-text;
    margin: 0;
    letter-spacing: -0.5px;
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
    transition: all 0.2s;

    &:hover {
      border-color: @nordic-accent;
      color: @nordic-accent;
    }

    &.spinning .refresh-icon {
      animation: spin 0.8s linear infinite;
    }

    .refresh-icon {
      font-size: 18px;
    }
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// --- 定位警告 ---
.location-warn {
  padding: 10px 16px;
  background: @nordic-yellow-light;
  color: #8B6914;
  border-radius: @nordic-radius-md;
  font-size: @nordic-text-sm;
  margin-bottom: @nordic-space-md;
}

// --- 工具栏 ---
.toolbar-row {
  display: flex;
  gap: 10px;
  margin-bottom: @nordic-space-md;
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
  transition: border-color 0.2s;

  &:focus-within {
    border-color: @nordic-accent;
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
  transition: all 0.2s;
  white-space: nowrap;

  &.active {
    border-color: @nordic-accent;
    color: @nordic-accent;
    background: @nordic-accent-light;
  }

  .filter-arrow {
    font-size: 10px;
    transition: transform 0.2s;
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

  &:focus {
    border-color: @nordic-accent;
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
    transition: all 0.2s;

    input { display: none; }

    &.selected {
      background: @nordic-accent-light;
      border-color: @nordic-accent;
      color: @nordic-accent;
    }

    &:hover {
      border-color: @nordic-accent;
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
      transition: all 0.2s;

      &:hover {
        border-color: @nordic-accent;
        color: @nordic-accent;
      }
    }
  }
}

// --- 筛选信息 ---
.filter-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: @nordic-blue-light;
  border-radius: @nordic-radius-md;
  font-size: @nordic-text-sm;
  color: @nordic-text-secondary;
  margin-bottom: @nordic-space-md;

  .clear-link {
    background: none;
    border: none;
    color: @nordic-blue;
    cursor: pointer;
    font-size: @nordic-text-sm;
    font-weight: 500;

    &:hover { text-decoration: underline; }
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
  transition: all 0.25s ease;

  &:hover {
    border-color: darken(@nordic-border, 10%);
    box-shadow: 0 4px 16px @nordic-shadow-hover;
    transform: translateY(-2px);
  }
}

.item-top {
  display: flex;
  gap: @nordic-space-md;
  margin-bottom: @nordic-space-md;
}

.item-image {
  width: 64px;
  height: 64px;
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
    font-size: 24px;
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
      .nordic-pill-tag(@nordic-blue-light, @nordic-blue);
    }

    .meta-source {
      .nordic-pill-tag(@nordic-accent-light, @nordic-accent);
    }
  }
}

// 匹配度
.match-bar {
  display: flex;
  align-items: center;
  gap: 8px;

  .match-track {
    flex: 1;
    height: 4px;
    background: @nordic-divider;
    border-radius: 2px;
    overflow: hidden;
  }

  .match-fill {
    height: 100%;
    border-radius: 2px;
    transition: width 0.6s ease;

    &.excellent { background: @nordic-green; }
    &.good { background: @nordic-blue; }
    &.medium { background: @nordic-yellow; }
    &.low { background: @nordic-red; }
  }

  .match-val {
    font-size: @nordic-text-xs;
    font-weight: 600;
    min-width: 32px;
    text-align: right;

    &.excellent { color: @nordic-green; }
    &.good { color: @nordic-blue; }
    &.medium { color: @nordic-yellow; }
    &.low { color: @nordic-red; }
  }
}

// 卡路里
.item-calories {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: @nordic-space-sm;

  .cal-val {
    font-size: @nordic-text-lg;
    font-weight: 700;
    color: @nordic-accent;
  }

  .cal-unit {
    font-size: @nordic-text-xs;
    color: @nordic-text-muted;
  }

  .cal-detail {
    margin-left: auto;
    background: none;
    border: none;
    color: @nordic-blue;
    font-size: @nordic-text-xs;
    cursor: pointer;
    padding: 2px 8px;

    &:hover { text-decoration: underline; }
  }
}

// 标签
.item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: @nordic-space-sm;

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
  font-size: @nordic-text-sm;
  color: @nordic-text-secondary;
  line-height: 1.5;
  margin-bottom: @nordic-space-md;
}

// 操作按钮
.item-actions {
  display: flex;
  gap: 8px;
  padding-top: @nordic-space-md;
  border-top: 1px solid @nordic-divider;
}

.act-btn {
  padding: 6px 16px;
  border: 1px solid @nordic-border;
  background: transparent;
  border-radius: @nordic-radius-md;
  font-size: @nordic-text-sm;
  color: @nordic-text-secondary;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: @nordic-text-secondary;
  }

  &.primary {
    background: @nordic-accent;
    border-color: @nordic-accent;
    color: #fff;

    &:hover {
      background: darken(@nordic-accent, 8%);
    }
  }

  &.favorited {
    color: @nordic-yellow;
    border-color: @nordic-yellow;
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

// --- 卡片过渡 ---
.card-list-enter-active,
.card-list-leave-active {
  transition: all 0.3s ease;
}

.card-list-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.card-list-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.card-list-move {
  transition: transform 0.3s ease;
}

// --- 滑入过渡 ---
.nordic-slide-enter-active,
.nordic-slide-leave-active {
  transition: all 0.3s ease;
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
  }
}
</style>
