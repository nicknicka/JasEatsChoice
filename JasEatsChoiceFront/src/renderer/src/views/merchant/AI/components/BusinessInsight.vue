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

const timeRange = ref('week')
const isLoading = ref(false)

// 核心指标
const metrics = ref({
  revenue: 25680,
  revenueChange: 12.5,
  orders: 892,
  ordersChange: 8.3,
  averagePrice: 28.75,
  averageChange: 3.8,
  rating: 4.6,
  ratingChange: 0.2
})

// 销售趋势
const salesTrend = ref([
  { label: '周一', value: 3200 },
  { label: '周二', value: 2800 },
  { label: '周三', value: 3500 },
  { label: '周四', value: 3100 },
  { label: '周五', value: 4200 },
  { label: '周六', value: 4800 },
  { label: '周日', value: 4080 }
])

const maxSales = computed(() => Math.max(...salesTrend.value.map(s => s.value)))

// 热销菜品
const topDishes = ref([
  { name: '红烧肉', sales: 156, trend: 12 },
  { name: '宫保鸡丁', sales: 142, trend: 8 },
  { name: '鱼香肉丝', sales: 128, trend: -3 },
  { name: '麻婆豆腐', sales: 98, trend: 5 },
  { name: '糖醋排骨', sales: 86, trend: 15 }
])

// AI建议
const aiSuggestions = ref([
  { type: 'warning', content: '「麻婆豆腐」销量下滑3%，建议检查口味或推出优惠活动' },
  { type: 'success', content: '周末订单量较高，建议增加人手和备货量' },
  { type: 'opportunity', content: '晚餐时段订单较少，可考虑推出晚餐专属优惠' },
  { type: 'success', content: '「糖醋排骨」增长最快，可设为主推菜品' }
])

// 评价分布
const ratingDistribution = ref([
  { stars: 5, count: 156, percent: 65 },
  { stars: 4, count: 52, percent: 22 },
  { stars: 3, count: 18, percent: 8 },
  { stars: 2, count: 8, percent: 3 },
  { stars: 1, count: 6, percent: 2 }
])

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

const loadInsights = () => {
  // TODO: 加载实际数据
}

const refreshData = () => {
  isLoading.value = true
  setTimeout(() => {
    isLoading.value = false
  }, 1000)
}

onMounted(() => {
  loadInsights()
})
</script>

<style scoped lang="less">
.business-insight {
  height: 100%;
  padding: 16px;
  overflow-y: auto;
}

.time-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #FFF;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .metric-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;

    &.revenue {
      background: linear-gradient(135deg, #FEF3C7, #FDE68A);
      color: #CA8A04;
    }

    &.orders {
      background: linear-gradient(135deg, #DBEAFE, #BFDBFE);
      color: #2563EB;
    }

    &.average {
      background: linear-gradient(135deg, #D1FAE5, #A7F3D0);
      color: #059669;
    }

    &.rating {
      background: linear-gradient(135deg, #FEE2E2, #FECACA);
      color: #DC2626;
    }
  }

  .metric-content {
    .metric-value {
      font-size: 24px;
      font-weight: 700;
      color: #374151;
    }

    .metric-label {
      font-size: 13px;
      color: #6B7280;
      margin-top: 4px;
    }

    .metric-change {
      font-size: 12px;
      margin-top: 4px;

      &.up {
        color: #059669;
      }

      &.down {
        color: #DC2626;
      }
    }
  }
}

.insights-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.insight-card {
  background: #FFF;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h3 {
      margin: 0;
      font-size: 15px;
      color: #374151;
    }

    .ai-icon {
      color: #DC2626;
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
    background: linear-gradient(to top, #DC2626, #F87171);
    border-radius: 4px 4px 0 0;
    min-height: 20px;
    position: relative;

    .bar-value {
      position: absolute;
      top: -20px;
      font-size: 11px;
      color: #6B7280;
      white-space: nowrap;
    }

    .bar-label {
      position: absolute;
      bottom: -24px;
      font-size: 12px;
      color: #9CA3AF;
    }
  }
}

.dish-list {
  .dish-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 0;
    border-bottom: 1px solid #F3F4F6;

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
      font-size: 12px;
      font-weight: 600;
      background: #F3F4F6;
      color: #6B7280;

      &.rank-1 {
        background: linear-gradient(135deg, #FEF3C7, #FDE68A);
        color: #CA8A04;
      }

      &.rank-2 {
        background: linear-gradient(135deg, #E5E7EB, #D1D5DB);
        color: #374151;
      }

      &.rank-3 {
        background: linear-gradient(135deg, #FED7AA, #FDBA74);
        color: #C2410C;
      }
    }

    .name {
      flex: 1;
      font-size: 14px;
      color: #374151;
    }

    .sales {
      font-size: 13px;
      color: #6B7280;
    }

    .trend {
      font-size: 12px;

      &.up {
        color: #059669;
      }

      &.down {
        color: #DC2626;
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
    font-size: 13px;
    color: #374151;
    line-height: 1.5;

    .el-icon {
      margin-top: 2px;
      flex-shrink: 0;

      &.warning {
        color: #F59E0B;
      }

      &.success {
        color: #059669;
      }

      &.opportunity {
        color: #2563EB;
      }
    }
  }
}

.rating-distribution {
  .rating-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;

    .stars {
      width: 36px;
      font-size: 13px;
      color: #6B7280;
    }

    .bar-container {
      flex: 1;
      height: 8px;
      background: #F3F4F6;
      border-radius: 4px;
      overflow: hidden;

      .bar {
        height: 100%;
        background: linear-gradient(90deg, #DC2626, #F87171);
        border-radius: 4px;
        transition: width 0.3s ease;
      }
    }

    .count {
      width: 40px;
      font-size: 12px;
      color: #9CA3AF;
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
