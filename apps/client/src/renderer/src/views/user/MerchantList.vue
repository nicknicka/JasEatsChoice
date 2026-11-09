<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { Search, RefreshLeft, Location, ShoppingCart, Shop } from '@element-plus/icons-vue'

// 引入API配置
import { API_CONFIG } from '../../config/index.js'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppCard from '../../components/common/AppCard.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import StatusTag from '../../components/common/StatusTag.vue'

const router = useRouter()

// 商家列表数据 - 初始化为空数组
const merchants = ref([])

// 加载状态
const isLoading = ref(false)

// 图片加载失败状态，按商家维度记录
const imageErrors = ref({})

// 筛选条件
const filters = ref({
  type: 'all',
  sort: 'distance'
})

// 搜索关键词
const searchKeyword = ref('')

// 商家类型筛选选项
const typeOptions = ref([
  { label: '全部', value: 'all' },
  { label: '轻食', value: '轻食' },
  { label: '早餐', value: '早餐' },
  { label: '中餐', value: '中餐' },
  { label: '健身餐', value: '健身餐' }
])

// 商家排序选项
const sortOptions = ref([
  { label: '距离最近', value: 'distance' },
  { label: '评分最高', value: 'rating' }
])

// 获取当前路由
const route = useRoute()

// 页面加载时从URL获取搜索参数并加载商家数据
onMounted(() => {
  const searchQuery = route.query.search
  if (searchQuery) {
    searchKeyword.value = searchQuery
  }

  // 加载商家列表数据
  loadMerchants()
})

// 监听筛选条件变化，重新加载数据
watch([searchKeyword, () => filters.value.type, () => filters.value.sort], () => {
  loadMerchants()
})

// 从后端加载商家列表
const loadMerchants = () => {
  isLoading.value = true
  imageErrors.value = {}

  // 调用后端API获取商家列表，包含所有筛选参数
  axios
    .get(API_CONFIG.baseURL + API_CONFIG.merchant.list, {
      params: {
        search: searchKeyword.value,
        type: filters.value.type,
        sort: filters.value.sort
      }
    })
    .then((response) => {
      console.log('获取商家列表成功:', response.data)
      // 假设后端返回的数据结构与前端期望的一致
      // 如果结构不同，需要在这里进行转换
      if (response.data.data) {
        merchants.value = response.data.data
      } else {
        // 处理空数据情况
        merchants.value = []
      }
    })
    .catch((error) => {
      console.error('加载商家列表失败:', error)
    })
    .finally(() => {
      isLoading.value = false
    })
}

// 立即下单功能
const orderNow = (merchant) => {
  // 将商家信息存储到会话存储
  sessionStorage.setItem('selectedMerchant', JSON.stringify(merchant))
  // 跳转到商家详情页面的立即下单流程
  router.push({
    path: '/user/home/merchant-detail',
    query: {
      viewMode: 'order',
      merchantId: merchant.merchantId || merchant.id
    }
  })
}

// 重置筛选条件
const resetFilters = () => {
  searchKeyword.value = ''
  filters.value = {
    type: 'all',
    sort: 'distance'
  }
}

// 计算属性：过滤和排序后的商家列表
const filteredMerchants = computed(() => {
  let result = [...merchants.value].map((merchant) => {
    // 统一状态处理
    let normalizedStatus = '未知状态'
    let isOpen = false
    if (merchant.status === true || merchant.status === 'true' || merchant.status === '营业中') {
      normalizedStatus = '营业中'
      isOpen = true
    } else if (
      merchant.status === false ||
      merchant.status === 'false' ||
      merchant.status === '已停业'
    ) {
      normalizedStatus = '已停业'
      isOpen = false
    }

    // 返回包含归一化状态的商家对象副本
    return {
      ...merchant,
      normalizedStatus,
      isOpen
    }
  })

  // 类型筛选
  if (filters.value.type !== 'all') {
    result = result.filter((merchant) => merchant.type === filters.value.type)
  }

  // 搜索筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(
      (merchant) =>
        merchant.name.toLowerCase().includes(keyword) ||
        (merchant.tags && merchant.tags.some((tag) => tag.toLowerCase().includes(keyword)))
    )
  }

  // 排序
  if (filters.value.sort === 'distance') {
    // 按距离排序
    result.sort((a, b) => {
      const distanceA =
        a.distance && a.distance !== '未知距离'
          ? parseFloat(a.distance.replace('km', ''))
          : Infinity
      const distanceB =
        b.distance && b.distance !== '未知距离'
          ? parseFloat(b.distance.replace('km', ''))
          : Infinity
      return distanceA - distanceB
    })
  } else if (filters.value.sort === 'rating') {
    // 按评分排序
    result.sort((a, b) => b.rating - a.rating)
  }

  return result
})

// 格式化评分显示
const formatRating = (rating) => {
  if (!rating || rating === 0) return '暂无评价'
  return rating.toFixed(1)
}

// 跳转到商家详情页
const goToMerchantDetail = (merchant) => {
  sessionStorage.setItem('selectedMerchant', JSON.stringify(merchant))
  router.push({
    path: '/user/home/merchant-detail',
    query: {
      viewMode: 'detail',
      merchantId: merchant.merchantId || merchant.id
    }
  })
}

// 头像加载失败后使用中性占位，避免碎图或白块
const getMerchantKey = (merchant) =>
  String(merchant?.merchantId || merchant?.id || merchant?.name || '')

const shouldShowMerchantImage = (merchant) => {
  const image = String(merchant?.image || '').trim()
  return Boolean(image && image !== '未知' && !imageErrors.value[getMerchantKey(merchant)])
}

const handleMerchantImageError = (merchant) => {
  imageErrors.value = {
    ...imageErrors.value,
    [getMerchantKey(merchant)]: true
  }
}
</script>

<template>
  <div class="merchant-list-container">
    <AppPageHeader title="商家查找" />

    <!-- 搜索和筛选区 -->
    <AppCard class="search-filter-section" :level="1" padding="md">
      <div class="search-row">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商家名称、类型或特色..."
          clearable
          class="search-input"
          aria-label="搜索商家名称、类型或特色"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-button class="reset-btn" :icon="RefreshLeft" @click="resetFilters">重置</el-button>
      </div>

      <div class="filter-row">
        <div class="filter-tags" role="group" aria-label="商家类型筛选">
          <button
            v-for="option in typeOptions"
            :key="option.value"
            type="button"
            :class="['filter-tag', { active: filters.type === option.value }]"
            @click="filters.type = option.value"
          >
            {{ option.label }}
          </button>
        </div>

        <div class="sort-options" role="group" aria-label="排序方式">
          <span class="sort-label">排序方式：</span>
          <button
            v-for="option in sortOptions"
            :key="option.value"
            type="button"
            :class="['sort-tag', { active: filters.sort === option.value }]"
            @click="filters.sort = option.value"
          >
            {{ option.label }}
          </button>
        </div>
      </div>
    </AppCard>

    <div class="result-summary" aria-live="polite">
      <span v-if="isLoading">正在加载商家…</span>
      <span v-else>找到 {{ filteredMerchants.length }} 家商家</span>
    </div>

    <!-- 商家列表 -->
    <div class="merchant-grid" :aria-busy="isLoading">
      <template v-if="isLoading">
        <AppCard
          v-for="index in 3"
          :key="`skeleton-${index}`"
          class="merchant-card skeleton-card"
          :level="2"
          padding="md"
        >
          <el-skeleton :rows="4" animated />
        </AppCard>
      </template>

      <template v-else-if="filteredMerchants.length > 0">
        <AppCard
          v-for="merchant in filteredMerchants"
          :key="merchant.id"
          class="merchant-card"
          :level="2"
          interactive
          padding="md"
          role="button"
          tabindex="0"
          @click="goToMerchantDetail(merchant)"
          @keydown.enter="goToMerchantDetail(merchant)"
          @keydown.space.prevent="goToMerchantDetail(merchant)"
        >
          <div class="card-header">
            <div class="merchant-image">
              <img
                v-if="shouldShowMerchantImage(merchant)"
                :src="merchant.image"
                :alt="merchant.name"
                loading="lazy"
                class="merchant-img"
                @error="handleMerchantImageError(merchant)"
              />
              <div v-else class="default-icon" aria-hidden="true">
                <el-icon :size="40"><Shop /></el-icon>
              </div>
            </div>

            <div class="merchant-info">
              <div class="merchant-name-row">
                <h2 class="merchant-name">{{ merchant.name }}</h2>
                <StatusTag
                  :status="merchant.isOpen ? 'open' : 'closed'"
                  :text="merchant.normalizedStatus"
                  size="sm"
                />
              </div>

              <div class="merchant-meta">
                <div class="merchant-rating">
                  <el-rate v-model="merchant.rating" :disabled="true" size="small" />
                  <span class="rating-number">{{ formatRating(merchant.rating) }}</span>
                </div>
                <span class="distance">
                  <el-icon class="distance-icon"><Location /></el-icon>
                  {{ merchant.distance || '未知距离' }}
                </span>
              </div>
            </div>
          </div>

          <!-- 商家信息行 -->
          <div
            v-if="
              merchant.type ||
              merchant.isNew ||
              merchant.discount ||
              (merchant.tags && merchant.tags.length > 0)
            "
            class="merchant-details"
          >
            <div v-if="merchant.type" class="merchant-type">
              <el-tag type="primary" size="small">{{ merchant.type }}</el-tag>
            </div>

            <div class="merchant-features">
              <el-tag v-if="merchant.isNew" type="warning" size="small">新店</el-tag>
              <el-tag v-if="merchant.discount" type="success" size="small">{{
                merchant.discount
              }}</el-tag>
            </div>

            <div v-if="merchant.tags && merchant.tags.length > 0" class="merchant-tags">
              <el-tag v-for="tag in merchant.tags.slice(0, 2)" :key="tag" size="small" type="info">
                {{ tag }}
              </el-tag>
              <el-tag v-if="merchant.tags.length > 2" size="small" type="info">
                +{{ merchant.tags.length - 2 }}
              </el-tag>
            </div>
          </div>

          <div class="card-actions">
            <el-button
              class="order-btn"
              type="primary"
              size="small"
              @click.stop="orderNow(merchant)"
            >
              <el-icon class="btn-icon"><ShoppingCart /></el-icon>
              立即下单
            </el-button>
          </div>
        </AppCard>
      </template>

      <AppEmptyState
        v-else
        class="empty-data"
        :icon="Shop"
        description="暂无商家数据，当前条件下没有找到任何商家"
        action-text="重置筛选条件"
        @action="resetFilters"
      />
    </div>
  </div>
</template>

<style scoped>
.merchant-list-container {
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  box-sizing: border-box;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

.search-filter-section {
  margin-bottom: var(--space-3);
}

.search-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.search-input {
  flex: 1;
  min-width: 0;
}

.search-input :deep(.el-input__wrapper) {
  min-height: var(--control-height-sm);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  padding: 0 var(--space-3);
  background: var(--surface);
  box-shadow: none;
  transition:
    border-color var(--duration-fast) var(--ease-out),
    outline-color var(--duration-fast) var(--ease-out);
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: var(--line-strong);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--brand-500);
  box-shadow: none;
  outline: 2px solid var(--brand-200);
  outline-offset: 1px;
}

.search-input :deep(.el-input__inner) {
  color: var(--ink-900);
  font-size: var(--text-body);
}

.search-input :deep(.el-input__prefix) {
  color: var(--ink-400);
  font-size: var(--text-body);
}

.search-input :deep(.el-input__suffix .el-icon) {
  color: var(--ink-400);
  font-size: var(--text-body);
}

.reset-btn {
  flex-shrink: 0;
  min-height: var(--control-height-sm);
  border-color: var(--line-strong);
  border-radius: var(--radius-md);
  color: var(--ink-700);
  font-size: var(--text-body-sm);
  transition:
    border-color var(--duration-fast) var(--ease-out),
    color var(--duration-fast) var(--ease-out),
    background-color var(--duration-fast) var(--ease-out);
}

.reset-btn:hover,
.reset-btn:focus-visible {
  border-color: var(--brand-300);
  background: var(--brand-50);
  color: var(--brand-600);
}

.filter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  margin-top: var(--space-3);
}

.filter-tags,
.sort-options {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-2);
  min-width: 0;
}

.filter-tag,
.sort-tag {
  appearance: none;
  min-height: 28px;
  border: 1px solid var(--line);
  border-radius: var(--radius-full);
  padding: 0 var(--space-3);
  background: var(--surface);
  color: var(--ink-700);
  font: inherit;
  font-size: var(--text-body-sm);
  line-height: 26px;
  cursor: pointer;
  transition:
    border-color var(--duration-fast) var(--ease-out),
    color var(--duration-fast) var(--ease-out),
    background-color var(--duration-fast) var(--ease-out);
}

.filter-tag:hover,
.sort-tag:hover {
  border-color: var(--brand-300);
  background: var(--brand-50);
  color: var(--brand-600);
}

.filter-tag:focus-visible,
.sort-tag:focus-visible {
  outline: 2px solid var(--brand-500);
  outline-offset: 2px;
}

.filter-tag.active,
.sort-tag.active {
  border-color: var(--brand-500);
  background: var(--brand-500);
  color: var(--surface);
}

.sort-label {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  white-space: nowrap;
}

.result-summary {
  min-height: 20px;
  margin-bottom: var(--space-3);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  text-align: right;
}

.merchant-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--card-gap);
  align-items: stretch;
}

.merchant-card {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}

.merchant-card :deep(.app-card__body) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

.merchant-card:focus-visible {
  outline: 2px solid var(--brand-500);
  outline-offset: 2px;
}

.card-header {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  min-width: 0;
}

.merchant-image {
  flex: 0 0 80px;
  width: 80px;
  height: 80px;
}

.merchant-img,
.default-icon {
  width: 80px;
  height: 80px;
  box-sizing: border-box;
  border: 1px solid var(--line);
  border-radius: var(--radius-full);
}

.merchant-img {
  display: block;
  object-fit: cover;
  background: var(--canvas-sunken);
}

.default-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--canvas-sunken);
  color: var(--ink-400);
}

.merchant-info {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: var(--space-2);
  min-width: 0;
}

.merchant-name-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-2);
  min-width: 0;
}

.merchant-name {
  min-width: 0;
  margin: 0;
  overflow: hidden;
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  line-height: 24px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.merchant-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-1) var(--space-3);
  min-width: 0;
}

.merchant-rating {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  min-width: 0;
}

.merchant-rating :deep(.el-rate) {
  height: 20px;
}

.merchant-rating :deep(.el-rate__icon) {
  margin-right: 0;
  font-size: var(--text-body);
}

.rating-number,
.distance {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
  white-space: nowrap;
}

.rating-number {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
  font-weight: var(--weight-semibold);
}

.distance {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
}

.distance-icon {
  color: var(--ink-400);
}

.merchant-details {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  margin-top: var(--space-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--line);
}

.merchant-type,
.merchant-features,
.merchant-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.card-actions {
  display: flex;
  margin-top: auto;
  padding-top: var(--space-4);
  border-top: 1px solid var(--line);
}

.order-btn {
  width: 100%;
  min-height: var(--control-height-md);
  border-radius: var(--radius-md);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
}

.btn-icon {
  font-size: var(--text-body);
}

.skeleton-card {
  cursor: default;
}

.empty-data {
  grid-column: 1 / -1;
  max-height: 240px;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: var(--shadow-sm);
}

@media (max-width: 1024px) {
  .merchant-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .merchant-list-container {
    padding: 0 var(--space-4) var(--space-8);
  }

  .merchant-grid {
    grid-template-columns: 1fr;
  }

  .filter-row {
    align-items: flex-start;
    flex-direction: column;
    gap: var(--space-2);
  }

  .search-row {
    flex-wrap: wrap;
  }

  .reset-btn {
    width: 100%;
  }
}
</style>
