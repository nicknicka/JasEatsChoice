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
.merchant-list-container {
  width: 100%; // 确保容器宽度为100%
  box-sizing: border-box; // 确保padding不会增加额外宽度
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 2.286rem /* 原值: 32px */;
    margin: 0 0 20px 20px;
    color: #1a202c;
    font-weight: 800;
    letter-spacing: -0.5px;

    // 添加装饰性下划线 - 蓝色渐变
    &::after {
      content: '';
      display: block;
      width: 60px;
      height: 4px;
      background: linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%);
      border-radius: 2px;
      margin-top: 12px;
    }
  }

  .search-filter-section {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 24px;
    padding: 20px;
    background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%);
    border-radius: 20px;
    box-shadow: 0 6px 24px rgba(59, 130, 246, 0.12);
    border: 1px solid rgba(59, 130, 246, 0.1);
    width: 100%;
    box-sizing: border-box;

    .search-row {
      display: flex;
      gap: 12px;
      align-items: center;
    }

    .search-input {
      flex: 1;

      :deep(.el-input__wrapper) {
        border-radius: 14px;
        border: 2px solid rgba(59, 130, 246, 0.15);
        padding: 8px 16px;
        transition: all 0.3s ease;
        background-color: #ffffff;
        box-shadow: none;

        &:hover {
          border-color: rgba(59, 130, 246, 0.3);
        }

        &.is-focus {
          border-color: #3b82f6;
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }
      }

      :deep(.el-input__inner) {
        font-size: 1.071rem /* 原值: 15px */;
        color: #1e293b;
      }

      :deep(.el-input__prefix) {
        color: #3b82f6;
        font-size: 1.286rem /* 原值: 18px */;
      }

      :deep(.el-input__suffix) {
        .el-icon {
          font-size: 1.143rem /* 原值: 16px */;
          color: #cbd5e1;

          &:hover {
            color: #3b82f6;
          }
        }
      }
    }

    /* 重置按钮样式 */
    .reset-btn {
      flex-shrink: 0;
      border-radius: 14px;
      height: 48px;
      padding: 0 24px;
      font-size: 1.071rem /* 原值: 15px */;
      transition: all 0.3s ease;
      background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
      border: 1px solid #cbd5e1;
      color: #475569;

      &:hover {
        background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
        border-color: #94a3b8;
        color: #1e293b;
      }
    }

    .filter-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      align-items: center;
      padding-bottom: 4px;

      .filter-tag {
        padding: 8px 18px;
        border-radius: 20px;
        font-size: 1rem /* 原值: 14px */;
        color: #64748b;
        background: #ffffff;
        border: 1px solid rgba(59, 130, 246, 0.1);
        cursor: pointer;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        user-select: none;
        position: relative;

        &:hover {
          color: #3b82f6;
          border-color: rgba(59, 130, 246, 0.3);
          background: rgba(59, 130, 246, 0.05);
          transform: translateY(-1px);
        }

        &:active {
          transform: translateY(0) scale(0.98);
        }

        &.active {
          color: #ffffff;
          background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
          border-color: transparent;
          font-weight: 500;
          box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);

          &:hover {
            box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
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
      gap: 10px;
      padding-top: 8px;
      border-top: 1px dashed rgba(59, 130, 246, 0.15);

      .sort-label {
        font-size: 0.929rem /* 原值: 13px */;
        color: #64748b;
        font-weight: 500;
        margin-right: 4px;
      }

      .sort-tag {
        padding: 6px 16px;
        border-radius: 18px;
        font-size: 0.929rem /* 原值: 13px */;
        color: #64748b;
        background: #ffffff;
        border: 1px solid rgba(59, 130, 246, 0.1);
        cursor: pointer;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        user-select: none;

        &:hover {
          color: #3b82f6;
          border-color: rgba(59, 130, 246, 0.3);
          background: rgba(59, 130, 246, 0.05);
          transform: translateY(-1px);
        }

        &:active {
          transform: translateY(0) scale(0.98);
        }

        &.active {
          color: #ffffff;
          background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
          border-color: transparent;
          font-weight: 500;
          box-shadow: 0 2px 8px rgba(59, 130, 246, 0.25);

          &:hover {
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.35);
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
    gap: 24px;
    padding: 0 20px;
    justify-content: center;
  }

  .merchant-card {
    flex: 1 1 320px;
    max-width: 480px;
    box-sizing: border-box;
    transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
    border-radius: 16px;
    box-shadow: 0 2px 15px rgba(59, 130, 246, 0.08);
    border: 1px solid rgba(59, 130, 246, 0.08);
    background: linear-gradient(to bottom, #ffffff 0%, #f8fafc 100%);
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
      background: linear-gradient(90deg, #3b82f6 0%, #06b6d4 100%);
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    &:hover {
      box-shadow: 0 12px 40px rgba(59, 130, 246, 0.18);
      transform: translateY(-6px);
      border-color: rgba(59, 130, 246, 0.25);

      &::before {
        opacity: 1;
      }
    }

    &:active {
      transform: translateY(-2px);
    }
  }

  // 营业中商家卡片 - 蓝色光晕效果
  .merchant-card-open {
    box-shadow: 0 2px 15px rgba(59, 130, 246, 0.08);

    &:hover {
      box-shadow: 0 12px 40px rgba(59, 130, 246, 0.22);

      // 卡片悬浮时，为营业中标签添加蓝色光晕
      .merchant-status .el-tag--success {
        box-shadow: 0 0 20px rgba(59, 130, 246, 0.6);
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
      gap: 20px;
      margin-bottom: 20px;
      align-items: center;
      padding: 4px;

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
          border: 3px solid rgba(59, 130, 246, 0.1);
          transition: all 0.3s ease;
        }

        .default-icon {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          color: #ffffff;
          filter: drop-shadow(0 2px 8px rgba(59, 130, 246, 0.15));
        }
      }

      .merchant-info {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
        flex: 1;
        min-width: 0; /* 防止文本溢出 */

        .merchant-name {
          font-size: 1.429rem /* 原值: 20px */;
          font-weight: 700;
          margin-bottom: 2px;
          background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
          letter-spacing: -0.3px;
          line-height: 1.3;
        }

        .merchant-meta {
          display: flex;
          align-items: center;
          justify-content: space-between;
          width: 100%;
          gap: 12px;
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
            gap: 8px;

            :deep(.el-rate) {
              .el-rate__icon {
                font-size: 1.143rem /* 原值: 16px */;
              }
            }

            .rating-number {
              font-size: 1.071rem /* 原值: 15px */;
              font-weight: 600;
              color: #f59e0b;
              background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
              -webkit-background-clip: text;
              -webkit-text-fill-color: transparent;
              background-clip: text;
            }
          }

          .distance {
            font-size: 0.929rem /* 原值: 13px */;
            color: #64748b;
            display: flex;
            align-items: center;
            gap: 4px;
            font-weight: 500;

            .distance-icon {
              font-size: 1rem /* 原值: 14px */;
              color: #94a3b8;
            }
          }
        }

        .merchant-status {
          flex-shrink: 0;

          .el-tag {
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          }

          // 营业中标签样式 - 蓝色系
          .el-tag--success {
            background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
            border: none;
            color: white;
            font-weight: 500;
            padding: 4px 12px;

            &:hover {
              background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
              transform: scale(1.05);
            }
          }

          // 非营业中标签样式
          .el-tag--danger {
            background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
            border: none;
            color: white;
            font-weight: 500;
            padding: 4px 12px;
          }
        }
      }
    }

    .merchant-details {
      margin-bottom: 16px;
      padding: 12px;
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      align-items: center;
      background: rgba(59, 130, 246, 0.03);
      border-radius: 10px;
    }

    .merchant-type {
      margin: 0;
    }

    .merchant-features {
      display: flex;
      gap: 8px;
    }

    .merchant-tags {
      margin: 0;
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    .card-actions {
      display: flex;
      justify-content: center;
      padding-top: 16px;
      border-top: 1px solid rgba(59, 130, 246, 0.1);

      .el-button {
        width: 100%;
        border-radius: 12px;
        font-weight: 600;
        height: 46px;
        font-size: 1.071rem /* 原值: 15px */;
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        border: none;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;

        .btn-icon {
          font-size: 1.286rem /* 原值: 18px */;
        }

        &:hover {
          background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
          transform: translateY(-2px);
          box-shadow: 0 6px 20px rgba(59, 130, 246, 0.45);
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
    padding: 80px 20px;
    background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%);
    border-radius: 16px;
    border: 2px dashed rgba(59, 130, 246, 0.2);

    .empty-icon {
      font-size: 80px;
      margin-bottom: 20px;
      opacity: 0.7;
    }

    .empty-text {
      color: #475569;

      h3 {
        font-size: 1.429rem /* 原值: 20px */;
        margin: 0 0 10px 0;
        color: #1e40af;
        font-weight: 600;
      }

      p {
        font-size: 1rem /* 原值: 14px */;
        margin: 0;
        color: #64748b;
      }
    }

    .empty-actions {
      margin-top: 30px;

      .el-button {
        padding: 10px 28px;
        font-size: 1rem /* 原值: 14px */;
        border-radius: 10px;
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        border: none;

        &:hover {
          background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
          box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
        }
      }
    }
  }
}
</style>
