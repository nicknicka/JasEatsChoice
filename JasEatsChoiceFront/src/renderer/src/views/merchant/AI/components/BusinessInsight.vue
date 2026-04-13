<template>
  <div class="business-insight">
    <!-- 时间选择 -->
    <div class="time-selector">
      <div class="time-tabs">
        <button
          v-for="opt in timeOptions"
          :key="opt.value"
          class="time-btn"
          :class="{ active: timeRange === opt.value }"
          @click="timeRange = opt.value; loadInsights()"
        >
          {{ opt.label }}
        </button>
      </div>
      <button class="refresh-btn" @click="refreshData" :class="{ spinning: isLoading }">
        <el-icon :size="16"><Refresh /></el-icon>
        <span>刷新数据</span>
      </button>
    </div>

    <!-- 核心指标 -->
    <div class="metrics-grid">
      <div
        v-for="(metric, index) in metricCards"
        :key="metric.key"
        class="metric-card"
        :class="`metric-${metric.key}`"
        :style="{ animationDelay: `${index * 0.08}s` }"
      >
        <div class="metric-glow"></div>
        <div class="metric-icon-wrap">
          <el-icon :size="22"><component :is="metric.icon" /></el-icon>
        </div>
        <div class="metric-body">
          <div class="metric-label">{{ metric.label }}</div>
          <div class="metric-value">{{ metric.prefix }}{{ metric.value }}{{ metric.suffix }}</div>
          <div class="metric-trend" :class="metric.change >= 0 ? 'up' : 'down'">
            <span class="trend-arrow">{{ metric.change >= 0 ? '↑' : '↓' }}</span>
            <span>{{ Math.abs(metric.change) }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 洞察卡片 -->
    <div class="insights-grid">
      <!-- 销售趋势 -->
      <div class="insight-card trend-card">
        <div class="card-header">
          <div class="card-title-group">
            <span class="card-icon trend-icon">
              <el-icon :size="16"><TrendCharts /></el-icon>
            </span>
            <h3>销售趋势</h3>
          </div>
          <span class="card-badge">{{ timeRangeLabel }}</span>
        </div>
        <div class="trend-chart">
          <div
            v-for="(item, index) in salesTrend"
            :key="index"
            class="trend-column"
          >
            <div class="bar-value">¥{{ item.value }}</div>
            <div
              class="trend-bar"
              :style="{ height: (item.value / maxSales * 100) + '%' }"
            ></div>
            <div class="bar-label">{{ item.label }}</div>
          </div>
        </div>
      </div>

      <!-- 热销菜品 -->
      <div class="insight-card dishes-card">
        <div class="card-header">
          <div class="card-title-group">
            <span class="card-icon dishes-icon">
              <el-icon :size="16"><Star /></el-icon>
            </span>
            <h3>热销菜品 TOP 5</h3>
          </div>
          <button class="link-btn">查看全部</button>
        </div>
        <div class="dish-list">
          <div v-for="(dish, index) in topDishes" :key="index" class="dish-item">
            <span class="rank" :class="[index < 3 ? `rank-gold rank-${index + 1}` : '']">
              {{ index + 1 }}
            </span>
            <span class="name">{{ dish.name }}</span>
            <span class="sales">{{ dish.sales }}份</span>
            <span class="trend" :class="dish.trend >= 0 ? 'up' : 'down'">
              {{ dish.trend >= 0 ? '↑' : '↓' }} {{ Math.abs(dish.trend) }}%
            </span>
          </div>
        </div>
      </div>

      <!-- AI建议 -->
      <div class="insight-card suggestions-card">
        <div class="card-header">
          <div class="card-title-group">
            <span class="card-icon ai-icon-wrap">
              <el-icon :size="16"><MagicStick /></el-icon>
            </span>
            <h3>AI经营建议</h3>
          </div>
          <span class="ai-badge">AI</span>
        </div>
        <div class="suggestion-list">
          <div v-for="(suggestion, index) in aiSuggestions" :key="index" class="suggestion-item">
            <span class="suggestion-icon" :class="suggestion.type">
              <el-icon :size="16"><component :is="getSuggestionIcon(suggestion.type)" /></el-icon>
            </span>
            <span class="suggestion-text">{{ suggestion.content }}</span>
          </div>
        </div>
      </div>

      <!-- 评价分析 -->
      <div class="insight-card rating-card">
        <div class="card-header">
          <div class="card-title-group">
            <span class="card-icon rating-icon">
              <el-icon :size="16"><Star /></el-icon>
            </span>
            <h3>评价分布</h3>
          </div>
        </div>
        <div class="rating-distribution">
          <div v-for="rating in ratingDistribution" :key="rating.stars" class="rating-item">
            <span class="stars">{{ rating.stars }}星</span>
            <div class="bar-container">
              <div class="bar" :style="{ width: rating.percent + '%' }">
                <div class="bar-shine"></div>
              </div>
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
import api from '@/utils/api'
import { buildUrl, MERCHANT_AI_API } from '@/api'

const props = defineProps({
  merchantId: {
    type: String,
    required: true
  }
})

const timeRange = ref('week')
const isLoading = ref(false)

const timeOptions = [
  { value: 'today', label: '今日' },
  { value: 'week', label: '本周' },
  { value: 'month', label: '本月' }
]

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

// 指标卡片配置
const metricCards = computed(() => [
  {
    key: 'revenue',
    label: '营业额',
    value: metrics.value.revenue.toLocaleString(),
    change: metrics.value.revenueChange,
    prefix: '¥',
    suffix: '',
    icon: Money
  },
  {
    key: 'orders',
    label: '订单数',
    value: metrics.value.orders,
    change: metrics.value.ordersChange,
    prefix: '',
    suffix: ' 单',
    icon: List
  },
  {
    key: 'average',
    label: '客单价',
    value: metrics.value.averagePrice,
    change: metrics.value.averageChange,
    prefix: '¥',
    suffix: '',
    icon: TrendCharts
  },
  {
    key: 'rating',
    label: '平均评分',
    value: metrics.value.rating,
    change: metrics.value.ratingChange,
    prefix: '',
    suffix: ' 分',
    icon: Star
  }
])

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
    const url = buildUrl(MERCHANT_AI_API.INSIGHT_FULL, { merchantId: props.merchantId })
    const response = await api.get(`${url}?timeRange=${timeRange.value}`)

    if (response.data) {
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
      salesTrend.value = response.data.salesTrend || []
      topDishes.value = response.data.topDishes || []
      aiSuggestions.value = response.data.aiSuggestions || []
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
  padding: 20px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 5px;
  }

  &::-webkit-scrollbar-thumb {
    background: @merchant-border;
    border-radius: 3px;
  }
}

// --- 时间选择 ---
.time-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.time-tabs {
  display: flex;
  gap: 4px;
  background: rgba(226, 222, 216, 0.4);
  border-radius: 10px;
  padding: 3px;
}

.time-btn {
  padding: 7px 18px;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  background: transparent;
  color: @merchant-text-sec;
  transition: all 0.25s ease;
  font-family: inherit;

  &:hover:not(.active) {
    color: @merchant-text;
  }

  &.active {
    background: @merchant-surface;
    color: @merchant-text;
    font-weight: 600;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  }
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  border: 1px solid @merchant-border;
  border-radius: 10px;
  background: @merchant-surface;
  color: @merchant-text-sec;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: inherit;

  &:hover {
    border-color: @merchant-primary;
    color: @merchant-primary;
  }

  &.spinning .el-icon {
    animation: spin 1s linear infinite;
  }
}

// --- 指标卡片 ---
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.metric-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(255,255,255,0.9), rgba(255,255,255,0.7));
  backdrop-filter: blur(8px);
  border: 1px solid rgba(226, 222, 216, 0.5);
  border-radius: 16px;
  overflow: hidden;
  animation: metricEnter 0.5s cubic-bezier(0.22, 1, 0.36, 1) both;
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px rgba(45, 42, 38, 0.08);
  }

  .metric-glow {
    position: absolute;
    top: -30px;
    right: -30px;
    width: 80px;
    height: 80px;
    border-radius: 50%;
    opacity: 0.08;
    filter: blur(20px);
  }

  &.metric-revenue .metric-glow { background: @merchant-warning; }
  &.metric-orders .metric-glow { background: @merchant-info; }
  &.metric-average .metric-glow { background: @merchant-success; }
  &.metric-rating .metric-glow { background: @merchant-secondary; }

  .metric-icon-wrap {
    width: 46px;
    height: 46px;
    border-radius: 13px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &.metric-revenue .metric-icon-wrap {
    background: linear-gradient(135deg, @merchant-warning-light, rgba(247, 237, 218, 0.6));
    color: @merchant-warning;
  }

  &.metric-orders .metric-icon-wrap {
    background: linear-gradient(135deg, @merchant-info-light, rgba(224, 237, 246, 0.6));
    color: @merchant-info;
  }

  &.metric-average .metric-icon-wrap {
    background: linear-gradient(135deg, @merchant-success-light, rgba(232, 244, 233, 0.6));
    color: @merchant-success;
  }

  &.metric-rating .metric-icon-wrap {
    background: linear-gradient(135deg, @merchant-secondary-light, rgba(244, 230, 222, 0.6));
    color: @merchant-secondary;
  }

  .metric-body {
    .metric-label {
      font-size: 12px;
      color: @merchant-text-muted;
      font-weight: 500;
      letter-spacing: 0.3px;
    }

    .metric-value {
      font-size: 22px;
      font-weight: 700;
      color: @merchant-text;
      letter-spacing: -0.5px;
      margin: 2px 0;
      line-height: 1.2;
    }

    .metric-trend {
      display: inline-flex;
      align-items: center;
      gap: 3px;
      font-size: 12px;
      font-weight: 600;
      padding: 2px 8px;
      border-radius: 20px;

      &.up {
        color: @merchant-success;
        background: rgba(90, 143, 94, 0.08);
      }

      &.down {
        color: @merchant-error;
        background: rgba(196, 91, 91, 0.08);
      }
    }
  }
}

// --- 洞察卡片网格 ---
.insights-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.insight-card {
  background: linear-gradient(135deg, rgba(255,255,255,0.9), rgba(255,255,255,0.7));
  backdrop-filter: blur(8px);
  border: 1px solid rgba(226, 222, 216, 0.5);
  border-radius: 16px;
  padding: 20px;
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.3s ease;

  &:hover {
    box-shadow: 0 6px 20px rgba(45, 42, 38, 0.06);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 18px;

    .card-title-group {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    h3 {
      margin: 0;
      font-size: 15px;
      font-weight: 600;
      color: @merchant-text;
      letter-spacing: -0.2px;
    }
  }

  .card-icon {
    width: 30px;
    height: 30px;
    border-radius: 9px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .trend-icon {
    background: linear-gradient(135deg, @merchant-info-light, rgba(224, 237, 246, 0.5));
    color: @merchant-info;
  }

  .dishes-icon {
    background: linear-gradient(135deg, @merchant-warning-light, rgba(247, 237, 218, 0.5));
    color: @merchant-warning;
  }

  .ai-icon-wrap {
    background: linear-gradient(135deg, @merchant-primary-light, rgba(227, 240, 228, 0.5));
    color: @merchant-primary;
  }

  .rating-icon {
    background: linear-gradient(135deg, @merchant-secondary-light, rgba(244, 230, 222, 0.5));
    color: @merchant-secondary;
  }

  .card-badge {
    font-size: 12px;
    color: @merchant-text-muted;
    padding: 4px 10px;
    background: rgba(226, 222, 216, 0.3);
    border-radius: 20px;
    font-weight: 500;
  }

  .ai-badge {
    font-size: 11px;
    font-weight: 700;
    color: @merchant-primary;
    background: @merchant-primary-light;
    padding: 3px 10px;
    border-radius: 20px;
    letter-spacing: 0.5px;
  }

  .link-btn {
    font-size: 13px;
    color: @merchant-primary;
    background: none;
    border: none;
    cursor: pointer;
    font-weight: 500;
    padding: 4px 8px;
    border-radius: 6px;
    transition: all 0.2s ease;
    font-family: inherit;

    &:hover {
      background: @merchant-primary-light;
    }
  }
}

// --- 趋势图表 ---
.trend-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 160px;
  padding: 24px 0 0;

  .trend-column {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 10%;
    gap: 6px;
  }

  .bar-value {
    font-size: 10px;
    color: @merchant-text-muted;
    white-space: nowrap;
    order: -1;
  }

  .trend-bar {
    width: 100%;
    background: linear-gradient(180deg, @merchant-primary, lighten(@merchant-primary, 15%));
    border-radius: 6px 6px 0 0;
    min-height: 8px;
    transition: height 0.6s cubic-bezier(0.22, 1, 0.36, 1);
    position: relative;

    &::after {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 40%;
      background: linear-gradient(180deg, rgba(255,255,255,0.2), transparent);
      border-radius: 6px 6px 0 0;
    }
  }

  .bar-label {
    font-size: 11px;
    color: @merchant-text-muted;
    text-align: center;
  }
}

// --- 菜品排行 ---
.dish-list {
  .dish-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 0;
    border-bottom: 1px solid @merchant-divider;

    &:last-child {
      border-bottom: none;
    }

    .rank {
      width: 26px;
      height: 26px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: 700;
      background: rgba(226, 222, 216, 0.3);
      color: @merchant-text-sec;

      &.rank-gold {
        color: #fff;
      }

      &.rank-1 {
        background: linear-gradient(135deg, #D4A855, #C49340);
        box-shadow: 0 2px 6px rgba(212, 168, 85, 0.3);
      }

      &.rank-2 {
        background: linear-gradient(135deg, #A8A8A8, #8E8E8E);
        box-shadow: 0 2px 6px rgba(168, 168, 168, 0.3);
      }

      &.rank-3 {
        background: linear-gradient(135deg, #C0855A, #A8704A);
        box-shadow: 0 2px 6px rgba(192, 133, 90, 0.3);
      }
    }

    .name {
      flex: 1;
      font-size: 14px;
      color: @merchant-text;
      font-weight: 500;
    }

    .sales {
      font-size: 13px;
      color: @merchant-text-sec;
    }

    .trend {
      font-size: 12px;
      font-weight: 600;

      &.up {
        color: @merchant-success;
      }

      &.down {
        color: @merchant-error;
      }
    }
  }
}

// --- AI建议 ---
.suggestion-list {
  .suggestion-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid @merchant-divider;

    &:last-child {
      border-bottom: none;
    }

    .suggestion-icon {
      width: 28px;
      height: 28px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      &.warning {
        background: rgba(212, 168, 85, 0.1);
        color: @merchant-warning;
      }

      &.success {
        background: rgba(90, 143, 94, 0.1);
        color: @merchant-success;
      }

      &.opportunity {
        background: rgba(91, 139, 210, 0.1);
        color: @merchant-info;
      }
    }

    .suggestion-text {
      font-size: 13px;
      color: @merchant-text;
      line-height: 1.6;
      padding-top: 3px;
    }
  }
}

// --- 评价分布 ---
.rating-distribution {
  .rating-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }

    .stars {
      width: 36px;
      font-size: 13px;
      color: @merchant-text-sec;
      font-weight: 500;
    }

    .bar-container {
      flex: 1;
      height: 8px;
      background: rgba(226, 222, 216, 0.3);
      border-radius: 4px;
      overflow: hidden;

      .bar {
        height: 100%;
        background: linear-gradient(90deg, @merchant-primary, lighten(@merchant-primary, 12%));
        border-radius: 4px;
        transition: width 0.6s cubic-bezier(0.22, 1, 0.36, 1);
        position: relative;
        overflow: hidden;

        .bar-shine {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          height: 50%;
          background: linear-gradient(180deg, rgba(255,255,255,0.25), transparent);
        }
      }
    }

    .count {
      width: 40px;
      font-size: 12px;
      color: @merchant-text-muted;
      text-align: right;
    }
  }
}

// --- 动画 ---
@keyframes metricEnter {
  from {
    opacity: 0;
    transform: translateY(12px) scale(0.97);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// --- 响应式 ---
@media (max-width: 1200px) {
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .insights-grid {
    grid-template-columns: 1fr;
  }
}
</style>
