<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { Search, RefreshLeft, Location, ShoppingCart, Shop } from '@element-plus/icons-vue'

// 引入API配置
import { API_CONFIG } from '../../config/index.js'

const router = useRouter()

// 商家列表数据 - 初始化为空数组
const merchants = ref([])

// 加载状态
const isLoading = ref(false)

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
    query: { viewMode: 'order' }
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
    query: { viewMode: 'detail' }
  })
}
</script>

<template>
  <div class="merchant-list-container">
    <h2 class="fade-in-up">商家查找</h2>

    <!-- 搜索和筛选区 -->
    <div class="search-filter-section fade-in-up delay-100">
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

        <el-button @click="resetFilters" class="reset-btn" :icon="RefreshLeft">重置</el-button>
      </div>

      <div class="filter-tags slide-in-left delay-200">
        <div
          v-for="option in typeOptions"
          :key="option.value"
          :class="['filter-tag', { active: filters.type === option.value }]"
          @click="filters.type = option.value"
        >
          {{ option.label }}
        </div>
      </div>

      <div class="sort-options slide-in-left delay-200">
        <span class="sort-label">排序方式：</span>
        <div
          v-for="option in sortOptions"
          :key="option.value"
          :class="['sort-tag', { active: filters.sort === option.value }]"
          @click="filters.sort = option.value"
        >
          {{ option.label }}
        </div>
      </div>
    </div>

    <!-- 商家列表 -->
    <div class="merchant-grid">
      <!-- 加载中状态 -->
      <el-skeleton :rows="6" v-if="isLoading" class="loading-skeleton" />

      <el-card
        v-for="merchant in filteredMerchants"
        :key="merchant.id"
        :class="['merchant-card stagger-item card-hover-effect', merchant.isOpen ? 'merchant-card-open' : 'merchant-card-closed']"
        v-else-if="filteredMerchants.length > 0"
        @click="goToMerchantDetail(merchant)"
      >
        <div class="card-header">
          <div class="merchant-image">
            <img
              v-if="merchant.image && merchant.image !== '未知'"
              :src="merchant.image"
              :alt="merchant.name"
              class="merchant-img"
            />
            <div v-else class="default-icon">
              <el-icon :size="40"><Shop /></el-icon>
            </div>
          </div>
          <div class="merchant-info">
            <div class="merchant-name">{{ merchant.name }}</div>
            <div class="merchant-meta">
              <div class="merchant-rating">
                <div class="rating-wrapper">
                  <el-rate v-model="merchant.rating" :disabled="true" size="small" />
                  <span class="rating-number">{{ formatRating(merchant.rating) }}</span>
                </div>
                <span class="distance">
                  <el-icon class="distance-icon"><Location /></el-icon>
                  {{ merchant.distance || '未知距离' }}
                </span>
              </div>
              <div class="merchant-status">
                <el-tag :type="merchant.isOpen ? 'success' : 'danger'" size="small">
                  {{ merchant.normalizedStatus }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 商家信息行 -->
        <div class="merchant-details">
          <!-- 商家类型 - 只在有数据时显示 -->
          <div class="merchant-type" v-if="merchant.type">
            <el-tag type="primary" size="small">{{ merchant.type }}</el-tag>
          </div>

          <!-- 商家特色/优惠信息 -->
          <div class="merchant-features">
            <el-tag v-if="merchant.isNew" type="warning" size="small">新店</el-tag>
            <el-tag v-if="merchant.discount" type="success" size="small">{{
              merchant.discount
            }}</el-tag>
          </div>

          <!-- 商家标签 - 只在有数据时显示 -->
          <div class="merchant-tags" v-if="merchant.tags && merchant.tags.length > 0">
            <el-tag v-for="tag in merchant.tags.slice(0, 2)" :key="tag" size="small" type="info">
              {{ tag }}
            </el-tag>
            <el-tag v-if="merchant.tags.length > 2" size="small" type="info">
              +{{ merchant.tags.length - 2 }}
            </el-tag>
          </div>
        </div>

        <div class="card-actions">
          <el-button type="primary" size="small" @click.stop="orderNow(merchant)">
            <el-icon class="btn-icon"><ShoppingCart /></el-icon>
            立即下单
          </el-button>
        </div>
      </el-card>

      <!-- 空数据提示 -->
      <div class="empty-data" v-else>
        <div class="empty-icon">🍴</div>
        <div class="empty-text">
          <h3>暂无商家数据</h3>
          <p>当前条件下没有找到任何商家</p>
        </div>
        <div class="empty-actions">
          <el-button type="primary" @click="resetFilters">重置筛选条件</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.merchant-list-container {
  width: 100%;
  box-sizing: border-box;
  padding: 0 @nordic-space-lg @nordic-space-lg @nordic-space-lg;
  background: @nordic-bg;
  min-height: 100%;

  h2 {
    font-size: @nordic-text-xl;
    margin: 0 0 @nordic-space-lg @nordic-space-lg;
    color: @nordic-text;
    font-weight: 800;
    letter-spacing: @nordic-letter-tight;

    &::after {
      content: '';
      display: block;
      width: 60px;
      height: 4px;
      background: linear-gradient(135deg, @nordic-accent 0%, @nordic-blue 100%);
      border-radius: @nordic-radius-xs;
      margin-top: 12px;
    }
  }

  .search-filter-section {
    display: flex;
    flex-direction: column;
    gap: @nordic-space-md - @nordic-space-xs;
    margin-bottom: @nordic-space-lg;
    padding: @nordic-space-lg;
    background: linear-gradient(135deg, @nordic-accent-light 0%, @nordic-blue-light 100%);
    border-radius: @nordic-radius-lg;
    box-shadow: 0 6px 24px fade(@nordic-accent, 12%);
    border: 1px solid fade(@nordic-accent, 10%);
    width: 100%;
    box-sizing: border-box;

    .search-row {
      display: flex;
      gap: @nordic-space-md - @nordic-space-xs;
      align-items: center;
    }

    .search-input {
      flex: 1;

      :deep(.el-input__wrapper) {
        border-radius: @nordic-radius-md;
        border: 2px solid fade(@nordic-accent, 15%);
        padding: @nordic-space-sm @nordic-space-md;
        transition: all @nordic-transition-slow ease;
        background-color: @nordic-surface;
        box-shadow: none;

        &:hover {
          border-color: fade(@nordic-accent, 30%);
        }

        &.is-focus {
          border-color: @nordic-accent;
          box-shadow: 0 0 0 3px fade(@nordic-accent, 10%);
        }
      }

      :deep(.el-input__inner) {
        font-size: @nordic-text-md - 1px;
        color: @nordic-text;
      }

      :deep(.el-input__prefix) {
        color: @nordic-accent;
        font-size: @nordic-text-lg - 2px;
      }

      :deep(.el-input__suffix) {
        .el-icon {
          font-size: @nordic-text-base + 2px;
          color: @nordic-text-muted;

          &:hover {
            color: @nordic-accent;
          }
        }
      }
    }

    .reset-btn {
      flex-shrink: 0;
      border-radius: @nordic-radius-md;
      height: 48px;
      padding: 0 @nordic-space-lg;
      font-size: @nordic-text-md - 1px;
      transition: all @nordic-transition-slow ease;
      background: linear-gradient(135deg, @nordic-bg 0%, @nordic-border 100%);
      border: 1px solid @nordic-border;
      color: @nordic-text-secondary;

      &:hover {
        background: linear-gradient(135deg, @nordic-border 0%, darken(@nordic-border, 5%) 100%);
        border-color: @nordic-text-muted;
        color: @nordic-text;
      }
    }

    .filter-tags {
      display: flex;
      flex-wrap: wrap;
      gap: @nordic-space-2sm;
      align-items: center;
      padding-bottom: @nordic-space-xs;

      .filter-tag {
        padding: @nordic-space-sm 18px;
        border-radius: @nordic-radius-pill;
        font-size: @nordic-text-base;
        color: @nordic-text-secondary;
        background: @nordic-surface;
        border: 1px solid fade(@nordic-accent, 10%);
        cursor: pointer;
        transition: all @nordic-transition-slow cubic-bezier(0.4, 0, 0.2, 1);
        user-select: none;
        position: relative;

        &:hover {
          color: @nordic-accent;
          border-color: fade(@nordic-accent, 30%);
          background: fade(@nordic-accent, 5%);
          transform: translateY(-1px);
        }

        &:active {
          transform: translateY(0) scale(0.98);
        }

        &.active {
          color: @nordic-surface;
          background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
          border-color: transparent;
          font-weight: 500;
          box-shadow: 0 4px 12px fade(@nordic-accent, 30%);

          &:hover {
            box-shadow: 0 6px 16px fade(@nordic-accent, 40%);
            transform: translateY(-2px);
          }

          &:active {
            transform: translateY(0) scale(0.98);
          }
        }
      }
    }

    .sort-options {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: @nordic-space-2sm;
      padding-top: @nordic-space-sm;
      border-top: 1px dashed fade(@nordic-accent, 15%);

      .sort-label {
        font-size: @nordic-text-sm;
        color: @nordic-text-secondary;
        font-weight: 500;
        margin-right: @nordic-space-xs;
      }

      .sort-tag {
        padding: 6px @nordic-space-md;
        border-radius: @nordic-radius-pill - 10px;
        font-size: @nordic-text-sm;
        color: @nordic-text-secondary;
        background: @nordic-surface;
        border: 1px solid fade(@nordic-accent, 10%);
        cursor: pointer;
        transition: all @nordic-transition-slow cubic-bezier(0.4, 0, 0.2, 1);
        user-select: none;

        &:hover {
          color: @nordic-accent;
          border-color: fade(@nordic-accent, 30%);
          background: fade(@nordic-accent, 5%);
          transform: translateY(-1px);
        }

        &:active {
          transform: translateY(0) scale(0.98);
        }

        &.active {
          color: @nordic-surface;
          background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
          border-color: transparent;
          font-weight: 500;
          box-shadow: 0 2px 8px fade(@nordic-accent, 25%);

          &:hover {
            box-shadow: 0 4px 12px fade(@nordic-accent, 35%);
            transform: translateY(-1px);
          }

          &:active {
            transform: translateY(0) scale(0.98);
          }
        }
      }
    }
  }

  .merchant-grid {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    gap: @nordic-space-lg;
    padding: 0 @nordic-space-lg;
    justify-content: center;
  }

  .merchant-card {
    flex: 1 1 320px;
    max-width: 480px;
    box-sizing: border-box;
    .nordic-card();
    cursor: pointer;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: linear-gradient(90deg, @nordic-accent 0%, @nordic-blue 100%);
      opacity: 0;
      transition: opacity @nordic-transition-slow ease;
    }

    &:hover {
      box-shadow: 0 12px 40px fade(@nordic-accent, 18%);
      transform: translateY(-6px);
      border-color: fade(@nordic-accent, 25%);

      &::before {
        opacity: 1;
      }
    }

    &:active {
      transform: translateY(-2px);
    }
  }

  // 营业中商家卡片
  .merchant-card-open {
    box-shadow: 0 2px 15px fade(@nordic-accent, 8%);

    &:hover {
      box-shadow: 0 12px 40px fade(@nordic-accent, 22%);

      .merchant-status .el-tag--success {
        box-shadow: 0 0 20px fade(@nordic-green, 60%);
        transform: scale(1.05);
      }
    }
  }

  // 已停业商家卡片
  .merchant-card-closed {
    opacity: 0.85;

    &:hover {
      opacity: 1;
    }
  }

  // 所有商家卡片通用样式
  .merchant-card {
    .card-header {
      display: flex;
      gap: @nordic-space-lg;
      margin-bottom: @nordic-space-lg;
      align-items: center;
      padding: @nordic-space-xs;

      .merchant-image {
        font-size: 50px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;

        .merchant-img {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          object-fit: cover;
          border: 3px solid fade(@nordic-accent, 10%);
          transition: all @nordic-transition-slow ease;
        }

        .default-icon {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          color: @nordic-surface;
          filter: drop-shadow(0 2px 8px fade(@nordic-accent, 15%));
        }
      }

      .merchant-info {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        gap: @nordic-space-2sm;
        flex: 1;
        min-width: 0;

        .merchant-name {
          font-size: @nordic-text-lg;
          font-weight: 700;
          margin-bottom: 2px;
          color: @nordic-accent-dark;
          letter-spacing: -0.3px;
          line-height: 1.3;
        }

        .merchant-meta {
          display: flex;
          align-items: center;
          justify-content: space-between;
          width: 100%;
          gap: @nordic-space-md - @nordic-space-xs;
        }

        .merchant-rating {
          display: flex;
          flex-direction: column;
          gap: 6px;
          flex: 1;
          min-width: 0;

          .rating-wrapper {
            display: flex;
            align-items: center;
            gap: @nordic-space-sm;

            :deep(.el-rate) {
              .el-rate__icon {
                font-size: @nordic-text-base + 2px;
              }
            }

            .rating-number {
              font-size: @nordic-text-md - 1px;
              font-weight: 600;
              color: @nordic-yellow;
            }
          }

          .distance {
            font-size: @nordic-text-sm;
            color: @nordic-text-secondary;
            display: flex;
            align-items: center;
            gap: @nordic-space-xs;
            font-weight: 500;

            .distance-icon {
              font-size: @nordic-text-base;
              color: @nordic-text-muted;
            }
          }
        }

        .merchant-status {
          flex-shrink: 0;

          .el-tag {
            transition: all @nordic-transition-slow cubic-bezier(0.4, 0, 0.2, 1);
          }

          // 营业中标签样式
          .el-tag--success {
            background: linear-gradient(135deg, @nordic-green 0%, @nordic-green-dark 100%);
            border: none;
            color: @nordic-surface;
            font-weight: 500;
            padding: @nordic-space-xs @nordic-space-md - @nordic-space-xs;

            &:hover {
              background: linear-gradient(135deg, @nordic-green-dark 0%, darken(@nordic-green-dark, 8%) 100%);
              transform: scale(1.05);
            }
          }

          // 非营业中标签样式
          .el-tag--danger {
            background: linear-gradient(135deg, @nordic-text-muted 0%, @nordic-text-secondary 100%);
            border: none;
            color: @nordic-surface;
            font-weight: 500;
            padding: @nordic-space-xs @nordic-space-md - @nordic-space-xs;
          }
        }
      }
    }

    .merchant-details {
      margin-bottom: @nordic-space-md;
      padding: @nordic-space-md - @nordic-space-xs;
      display: flex;
      flex-wrap: wrap;
      gap: @nordic-space-sm;
      align-items: center;
      background: fade(@nordic-accent, 3%);
      border-radius: @nordic-radius-md;
    }

    .merchant-type {
      margin: 0;
    }

    .merchant-features {
      display: flex;
      gap: @nordic-space-sm;
    }

    .merchant-tags {
      margin: 0;
      display: flex;
      gap: @nordic-space-sm;
      flex-wrap: wrap;
    }

    .card-actions {
      display: flex;
      justify-content: center;
      padding-top: @nordic-space-md;
      border-top: 1px solid fade(@nordic-accent, 10%);

      .el-button {
        width: 100%;
        border-radius: @nordic-radius-md;
        font-weight: 600;
        height: 46px;
        font-size: @nordic-text-md - 1px;
        background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
        border: none;
        transition: all @nordic-transition-slow cubic-bezier(0.4, 0, 0.2, 1);
        display: flex;
        align-items: center;
        justify-content: center;
        gap: @nordic-space-sm;

        .btn-icon {
          font-size: @nordic-text-lg - 2px;
        }

        &:hover {
          background: linear-gradient(135deg, @nordic-accent-dark 0%, darken(@nordic-accent-dark, 8%) 100%);
          transform: translateY(-2px);
          box-shadow: 0 6px 20px fade(@nordic-accent, 45%);
        }

        &:active {
          transform: translateY(0);
        }
      }
    }
  }

  // 加载中样式
  .loading-skeleton {
    width: 100%;
    flex-shrink: 0;
  }

  // 空数据样式
  .empty-data {
    width: 100%;
    flex-shrink: 0;
    text-align: center;
    padding: 80px @nordic-space-lg;
    background: linear-gradient(135deg, @nordic-accent-light 0%, @nordic-blue-light 100%);
    border-radius: @nordic-radius-lg;
    border: 2px dashed fade(@nordic-accent, 20%);

    .empty-icon {
      font-size: 80px;
      margin-bottom: @nordic-space-lg;
      opacity: 0.7;
    }

    .empty-text {
      color: @nordic-text-secondary;

      h3 {
        font-size: @nordic-text-lg;
        margin: 0 0 @nordic-space-2sm 0;
        color: @nordic-accent-dark;
        font-weight: 600;
      }

      p {
        font-size: @nordic-text-base;
        margin: 0;
        color: @nordic-text-secondary;
      }
    }

    .empty-actions {
      margin-top: 30px;

      .el-button {
        padding: @nordic-space-sm 28px;
        font-size: @nordic-text-base;
        border-radius: @nordic-radius-md;
        background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
        border: none;

        &:hover {
          background: linear-gradient(135deg, @nordic-accent-dark 0%, darken(@nordic-accent-dark, 8%) 100%);
          box-shadow: 0 4px 12px fade(@nordic-accent, 40%);
        }
      }
    }
  }
}
</style>
