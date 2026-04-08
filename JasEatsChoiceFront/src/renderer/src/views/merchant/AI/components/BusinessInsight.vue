<template>
  <div class="business-insight">
    <!-- 时间选择 -->
    <div class="time-selector">
      <el-radio-group v-model="timeRange" @change="loadInsights">
        <el-radio-button label="today">今日</el-radio-button>
        <el-radio-button label="week">本周</el-radio-button>
        <el-radio-button label="month">本月</el-radio-button>
      </el-radio-group>
      <el-button @click="refreshData" :loading="isLoading">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 核心指标 -->
    <div class="metrics-grid">
      <div class="metric-card">
        <div class="metric-icon revenue">
          <el-icon><Money /></el-icon>
        </div>
        <div class="metric-content">
          <div class="metric-value">¥{{ metrics.revenue.toLocaleString() }}</div>
          <div class="metric-label">营业额</div>
          <div class="metric-change" :class="metrics.revenueChange >= 0 ? 'up' : 'down'">
            {{ metrics.revenueChange >= 0 ? '↑' : '↓' }} {{ Math.abs(metrics.revenueChange) }}%
          </div>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-icon orders">
          <el-icon><List /></el-icon>
        </div>
        <div class="metric-content">
          <div class="metric-value">{{ metrics.orders }}</div>
          <div class="metric-label">订单数</div>
          <div class="metric-change" :class="metrics.ordersChange >= 0 ? 'up' : 'down'">
            {{ metrics.ordersChange >= 0 ? '↑' : '↓' }} {{ Math.abs(metrics.ordersChange) }}%
          </div>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-icon average">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="metric-content">
          <div class="metric-value">¥{{ metrics.averagePrice }}</div>
          <div class="metric-label">客单价</div>
          <div class="metric-change" :class="metrics.averageChange >= 0 ? 'up' : 'down'">
            {{ metrics.averageChange >= 0 ? '↑' : '↓' }} {{ Math.abs(metrics.averageChange) }}%
          </div>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-icon rating">
          <el-icon><Star /></el-icon>
        </div>
        <div class="metric-content">
          <div class="metric-value">{{ metrics.rating }}</div>
          <div class="metric-label">平均评分</div>
          <div class="metric-change" :class="metrics.ratingChange >= 0 ? 'up' : 'down'">
            {{ metrics.ratingChange >= 0 ? '↑' : '↓' }} {{ Math.abs(metrics.ratingChange) }}
          </div>
        </div>
      </div>
    </div>

    <!-- 洞察卡片 -->
    <div class="insights-grid">
      <!-- 销售趋势 -->
      <div class="insight-card">
        <div class="card-header">
          <h3>销售趋势</h3>
          <el-tag type="info">{{ timeRangeLabel }}</el-tag>
        </div>
        <div class="trend-chart">
          <div
            v-for="(item, index) in salesTrend"
            :key="index"
            class="trend-bar"
            :style="{ height: (item.value / maxSales * 100) + '%' }"
          >
            <span class="bar-value">¥{{ item.value }}</span>
            <span class="bar-label">{{ item.label }}</span>
          </div>
        </div>
      </div>

      <!-- 热销菜品 -->
      <div class="insight-card">
        <div class="card-header">
          <h3>热销菜品 TOP 5</h3>
          <el-button text type="primary" size="small">查看全部</el-button>
        </div>
        <div class="dish-list">
          <div v-for="(dish, index) in topDishes" :key="index" class="dish-item">
            <span class="rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
            <span class="name">{{ dish.name }}</span>
            <span class="sales">{{ dish.sales }}份</span>
            <span class="trend" :class="dish.trend >= 0 ? 'up' : 'down'">
              {{ dish.trend >= 0 ? '↑' : '↓' }} {{ Math.abs(dish.trend) }}%
            </span>
          </div>
        </div>
      </div>

      <!-- AI建议 -->
      <div class="insight-card suggestions">
        <div class="card-header">
          <h3>AI经营建议</h3>
          <el-icon class="ai-icon"><MagicStick /></el-icon>
        </div>
        <div class="suggestion-list">
          <div v-for="(suggestion, index) in aiSuggestions" :key="index" class="suggestion-item">
            <el-icon :class="suggestion.type"><component :is="getSuggestionIcon(suggestion.type)" /></el-icon>
            <span>{{ suggestion.content }}</span>
          </div>
        </div>
      </div>

      <!-- 评价分析 -->
      <div class="insight-card">
        <div class="card-header">
          <h3>评价分布</h3>
        </div>
        <div class="rating-distribution">
          <div v-for="rating in ratingDistribution" :key="rating.stars" class="rating-item">
            <span class="stars">{{ rating.stars }}星</span>
            <div class="bar-container">
              <div class="bar" :style="{ width: rating.percent + '%' }"></div>
            </div>
            <span class="count">{{ rating.count }}条</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  Refresh,
  Money,
  List,
  TrendCharts,
  Star,
  MagicStick,
  Warning,
  CircleCheck,
  Opportunity
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api, { buildUrl, MERCHANT_AI_API } from '@/api'

const props = defineProps({
  merchantId: {
    type: String,
    required: true
  }
})

const timeRange = ref('week')
const isLoading = ref(false)

// 核心指标
const metrics = ref({
  revenue: 0,
  revenueChange: 0,
  orders: 0,
  ordersChange: 0,
  averagePrice: 0,
  averageChange: 0,
  rating: 0,
  ratingChange: 0
})

// 销售趋势
const salesTrend = ref([])

const maxSales = computed(() => {
  const values = salesTrend.value.map(s => s.value)
  return values.length > 0 ? Math.max(...values) : 1
})

// 热销菜品
const topDishes = ref([])

// AI建议
const aiSuggestions = ref([])

// 评价分布
const ratingDistribution = ref([])

const timeRangeLabel = computed(() => {
  const labels = { today: '今日', week: '本周', month: '本月' }
  return labels[timeRange.value]
})

const getSuggestionIcon = (type) => {
  const icons = {
    warning: Warning,
    success: CircleCheck,
    opportunity: Opportunity
  }
  return icons[type] || CircleCheck
}

const loadInsights = async () => {
  isLoading.value = true
  try {
    // 调用完整洞察接口
    const url = buildUrl(MERCHANT_AI_API.INSIGHT_FULL, { merchantId: props.merchantId })
    const response = await api.get(`${url}?timeRange=${timeRange.value}`)

    if (response.data) {
      // 核心指标
      if (response.data.metrics) {
        metrics.value = {
          revenue: response.data.metrics.revenue || 0,
          revenueChange: response.data.metrics.revenueChange || 0,
          orders: response.data.metrics.orders || 0,
          ordersChange: response.data.metrics.ordersChange || 0,
          averagePrice: response.data.metrics.averagePrice || 0,
          averageChange: response.data.metrics.averageChange || 0,
          rating: response.data.metrics.rating || 0,
          ratingChange: response.data.metrics.ratingChange || 0
        }
      }

      // 销售趋势
      salesTrend.value = response.data.salesTrend || []

      // 热销菜品
      topDishes.value = response.data.topDishes || []

      // AI建议
      aiSuggestions.value = response.data.aiSuggestions || []

      // 评价分布
      ratingDistribution.value = response.data.ratingDistribution || []
    }
  } catch (error) {
    console.error('加载经营洞察失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    isLoading.value = false
  }
}

const refreshData = () => {
  loadInsights()
}

onMounted(() => {
  loadInsights()
})
</script>

<style scoped lang="less">
@import '../../../../assets/css/nordic-theme.less';
@import '../../../../assets/css/merchant-theme.less';

.business-insight {
  height: 100%;
  padding: @nordic-space-md;
  overflow-y: auto;
}

.time-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: @nordic-space-lg;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: @nordic-space-md;
  margin-bottom: @nordic-space-lg;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: @nordic-space-md;
  padding: @nordic-space-lg;
  background: @merchant-surface;
  border-radius: @nordic-radius-md;
  box-shadow: 0 2px 8px @merchant-shadow;

  .metric-icon {
    width: 48px;
    height: 48px;
    border-radius: @nordic-radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;

    &.revenue {
      background: linear-gradient(135deg, @merchant-warning-light, lighten(@merchant-warning, 30%));
      color: @merchant-warning;
    }

    &.orders {
      background: linear-gradient(135deg, @merchant-info-light, lighten(@merchant-info, 30%));
      color: @merchant-info;
    }

    &.average {
      background: linear-gradient(135deg, @merchant-success-light, lighten(@merchant-success, 30%));
      color: @merchant-success;
    }

    &.rating {
      background: linear-gradient(135deg, @merchant-secondary-light, lighten(@merchant-secondary, 30%));
      color: @merchant-secondary;
    }
  }

  .metric-content {
    .metric-value {
      font-size: @nordic-text-xl;
      font-weight: 700;
      color: @merchant-text;
    }

    .metric-label {
      font-size: @nordic-text-sm;
      color: @merchant-text-sec;
      margin-top: 4px;
    }

    .metric-change {
      font-size: @nordic-text-xs;
      margin-top: 4px;

      &.up {
        color: @merchant-success;
      }

      &.down {
        color: @merchant-error;
      }
    }
  }
}

.insights-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: @nordic-space-md;
}

.insight-card {
  background: @merchant-surface;
  border-radius: @nordic-radius-md;
  padding: @nordic-space-lg;
  box-shadow: 0 2px 8px @merchant-shadow;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: @nordic-space-md;

    h3 {
      margin: 0;
      font-size: 15px;
      color: @merchant-text;
    }

    .ai-icon {
      color: @merchant-secondary;
      font-size: 20px;
    }
  }
}

.trend-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 160px;
  padding-top: 20px;

  .trend-bar {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 12%;
    background: linear-gradient(to top, @merchant-primary, lighten(@merchant-primary, 20%));
    border-radius: 4px 4px 0 0;
    min-height: 20px;
    position: relative;

    .bar-value {
      position: absolute;
      top: -20px;
      font-size: 11px;
      color: @merchant-text-sec;
      white-space: nowrap;
    }

    .bar-label {
      position: absolute;
      bottom: -24px;
      font-size: @nordic-text-xs;
      color: @merchant-text-muted;
    }
  }
}

.dish-list {
  .dish-item {
    display: flex;
    align-items: center;
    gap: @nordic-space-md;
    padding: 10px 0;
    border-bottom: 1px solid @merchant-divider;

    &:last-child {
      border-bottom: none;
    }

    .rank {
      width: 24px;
      height: 24px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: @nordic-text-xs;
      font-weight: 600;
      background: @merchant-surface-alt;
      color: @merchant-text-sec;

      &.rank-1 {
        background: linear-gradient(135deg, @merchant-warning-light, lighten(@merchant-warning, 30%));
        color: @merchant-warning;
      }

      &.rank-2 {
        background: linear-gradient(135deg, @merchant-border, lighten(@merchant-border, 10%));
        color: @merchant-text;
      }

      &.rank-3 {
        background: linear-gradient(135deg, @merchant-secondary-light, lighten(@merchant-secondary, 20%));
        color: @merchant-secondary;
      }
    }

    .name {
      flex: 1;
      font-size: @nordic-text-base;
      color: @merchant-text;
    }

    .sales {
      font-size: @nordic-text-sm;
      color: @merchant-text-sec;
    }

    .trend {
      font-size: @nordic-text-xs;

      &.up {
        color: @merchant-success;
      }

      &.down {
        color: @merchant-error;
      }
    }
  }
}

.suggestion-list {
  .suggestion-item {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 10px 0;
    font-size: @nordic-text-sm;
    color: @merchant-text;
    line-height: 1.5;

    .el-icon {
      margin-top: 2px;
      flex-shrink: 0;

      &.warning {
        color: @merchant-warning;
      }

      &.success {
        color: @merchant-success;
      }

      &.opportunity {
        color: @merchant-info;
      }
    }
  }
}

.rating-distribution {
  .rating-item {
    display: flex;
    align-items: center;
    gap: @nordic-space-md;
    margin-bottom: @nordic-space-sm;

    .stars {
      width: 36px;
      font-size: @nordic-text-sm;
      color: @merchant-text-sec;
    }

    .bar-container {
      flex: 1;
      height: 8px;
      background: @merchant-divider;
      border-radius: 4px;
      overflow: hidden;

      .bar {
        height: 100%;
        background: linear-gradient(90deg, @merchant-primary, lighten(@merchant-primary, 15%));
        border-radius: 4px;
        transition: width 0.3s ease;
      }
    }

    .count {
      width: 40px;
      font-size: @nordic-text-xs;
      color: @merchant-text-muted;
      text-align: right;
    }
  }
}

@media (max-width: 1200px) {
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .insights-grid {
    grid-template-columns: 1fr;
  }
}
</style>
