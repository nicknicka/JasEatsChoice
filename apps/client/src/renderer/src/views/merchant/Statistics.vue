<script setup>
import api from '../../utils/api'
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useAuthStore } from '../../store/authStore'
import { ElMessage } from 'element-plus'
import {
  TrendCharts,
  ShoppingCart,
  Coin,
  User,
  Trophy,
  Food
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

// 统计时间范围选项
const timeRangeOptions = ['all', 'today', 'yesterday', 'week', 'month']
const activeTimeRange = ref('all')

// 数据加载状态
const isLoading = ref(false)

// 从后端获取统计数据
const fetchStatisticsData = () => {
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId

  if (!merchantId) {
    console.error('获取统计数据失败: 商家ID不存在')
    ElMessage.error('商家ID不存在，请重新登录')
    return
  }

  isLoading.value = true

  api
    .get(`/v1/merchant/${merchantId}/statistics`, { params: { timeRange: activeTimeRange.value } })
    .then((response) => {
      if (response.code === '200' && response.data) {
        // 更新基本统计数据
        currentBasicStats.value = response.data.basicStats

        // 更新订单趋势数据
        if (response.data.orderTrend) {
          currentOrderTrend.value = response.data.orderTrend
          // 更新图表数据
          updateChartData()
        }

        // 更新菜品销量排行数据
        if (response.data.dishSalesRank) {
          dishSalesRank.value = response.data.dishSalesRank
        }
      }
    })
    .catch((error) => {
      console.error('获取统计数据失败:', error)
      ElMessage.error('获取统计数据失败，请稍后重试')
      // 如果获取失败，清空数据
      currentBasicStats.value = { orders: 0, totalAmount: 0.0, avgAmount: 0.0, newCustomers: 0 }
      currentOrderTrend.value = []
      dishSalesRank.value = []
      updateChartData()
    })
    .finally(() => {
      isLoading.value = false
    })
}

// 时间范围变化时调用的方法
const changeTimeRange = (range) => {
  activeTimeRange.value = range
  fetchStatisticsData()
}

// 格式化金额显示
const formatCurrency = (amount) => {
  const num = Number(amount)
  // 如果金额大于 10000，使用万单位
  if (num >= 10000) {
    const wan = (num / 10000).toFixed(1)
    return `¥${wan}万`
  }
  // 否则显示原始金额，保留两位小数
  return `¥${num.toFixed(2)}`
}

// 格式化完整金额（用于详情显示，不使用万单位）
const formatFullCurrency = (amount) => {
  return `¥${Number(amount).toFixed(2)}`
}

// 时间范围显示映射
const timeRangeLabels = computed(() => ({
  all: '全部',
  today: '今日',
  yesterday: '昨日',
  week: '本周',
  month: '本月'
}))

// 获取时间范围显示文本
const getTimeRangeLabel = (range) => {
  return timeRangeLabels.value[range] || range
}

// 页面加载时初始化数据
onMounted(() => {
  fetchStatisticsData()
  // 初始化图表
  nextTick(() => {
    initOrderChart()
    // 设置 ResizeObserver 监听容器尺寸变化
    if (orderChartRef.value) {
      resizeObserver = new ResizeObserver((entries) => {
        for (let entry of entries) {
          const { width, height } = entry.contentRect
          // 当容器有有效尺寸且图表实例不存在时，初始化图表
          if (width > 0 && height > 0 && !orderChartInstance && currentOrderTrend.value.length > 0) {
            nextTick(() => {
              initOrderChart()
              updateChartData()
            })
          }
          // 当图表实例存在时，调整尺寸
          if (orderChartInstance && width > 0 && height > 0) {
            orderChartInstance.resize()
          }
        }
      })
      resizeObserver.observe(orderChartRef.value)
    }
  })
  // 监听窗口大小变化
  window.addEventListener('resize', handleChartResize)
})

// 处理图表窗口大小变化
const handleChartResize = () => {
  if (orderChartInstance) {
    orderChartInstance.resize()
  }
}

// 组件卸载时清理
onBeforeUnmount(() => {
  if (orderChartInstance) {
    orderChartInstance.dispose()
    orderChartInstance = null
  }
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  window.removeEventListener('resize', handleChartResize)
})

// 当前显示的基础统计数据
const currentBasicStats = ref({ orders: 0, totalAmount: 0.0, avgAmount: 0.0, newCustomers: 0 })

// 当前显示的订单趋势数据
const currentOrderTrend = ref([])

// 订单趋势图表 ref
const orderChartRef = ref(null)

// 图表实例
let orderChartInstance = null

// ResizeObserver 实例
let resizeObserver = null

// 菜品销量排行数据
const dishSalesRank = ref([])

// 图表用 Canvas 渲染，拿不到 CSS 变量，运行时从 tokens.css 读唯一颜色源（§7.1）
const tokenColor = (name) => getComputedStyle(document.documentElement).getPropertyValue(name).trim()

// 配置订单趋势图表（§4.7：区块标题只在卡头出现一次，画布内不再重复；§3.4：网格线取 --line）
const orderChartOptions = ref({
  tooltip: {
    trigger: 'axis',
    formatter: (params) => {
      if (params && params.length > 0) {
        const item = params[0]
        return `${item.axisValue}: ${Math.round(item.value)} 单`
      }
      return ''
    }
  },
  grid: {
    left: 8,
    right: 16,
    top: 16,
    bottom: 8,
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: [],
    axisLine: {
      lineStyle: {
        color: tokenColor('--line')
      }
    },
    axisLabel: {
      color: tokenColor('--ink-500'),
      fontSize: 12
    }
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    axisLine: {
      show: false
    },
    splitLine: {
      lineStyle: {
        color: tokenColor('--line')
      }
    },
    axisLabel: {
      color: tokenColor('--ink-500'),
      fontSize: 12,
      formatter: (value) => {
        // 确保显示为整数
        return Math.round(value) + ' 单'
      }
    }
  },
  series: [
    {
      name: '订单数',
      data: [],
      type: 'line',
      smooth: true,
      symbolSize: 6,
      lineStyle: {
        width: 2,
        color: tokenColor('--success')
      },
      itemStyle: {
        color: tokenColor('--success')
      }
    }
  ]
})

// 检查容器是否有有效尺寸
const hasValidSize = (dom) => {
  return dom && dom.clientWidth > 0 && dom.clientHeight > 0
}

// 初始化图表
const initOrderChart = () => {
  if (orderChartRef.value && hasValidSize(orderChartRef.value)) {
    orderChartInstance = echarts.init(orderChartRef.value)
    orderChartInstance.setOption(orderChartOptions.value)
    // 延迟调用 resize 确保容器尺寸已确定
    setTimeout(() => {
      if (orderChartInstance) {
        orderChartInstance.resize()
      }
    }, 100)
  }
}

// 更新图表数据
const updateChartData = () => {
  if (currentOrderTrend.value.length === 0) {
    return
  }

  orderChartOptions.value.xAxis.data = currentOrderTrend.value.map((item) => item.time)
  orderChartOptions.value.series[0].data = currentOrderTrend.value.map((item) => item.orders)

  // 更新图表
  nextTick(() => {
    if (!orderChartInstance && orderChartRef.value && hasValidSize(orderChartRef.value)) {
      // 如果图表实例不存在但 DOM 已渲染且有尺寸,先初始化
      orderChartInstance = echarts.init(orderChartRef.value)
    }
    if (orderChartInstance) {
      orderChartInstance.setOption(orderChartOptions.value)
      // 确保图表正确填充容器
      setTimeout(() => {
        if (orderChartInstance) {
          orderChartInstance.resize()
        }
      }, 100)
    }
  })
}

// 监听数据变化,自动更新图表
watch(currentOrderTrend, () => {
  updateChartData()
}, { deep: true })
</script>

<template>
  <div class="statistics-container">
    <!-- 页头：merchant 紧凑版，周期筛选在右侧、返回在动作区（§4.7） -->
    <AppPageHeader title="经营统计" compact>
      <template #actions>
        <div class="time-range-selector" role="group" aria-label="统计周期">
          <button
            v-for="range in timeRangeOptions"
            :key="range"
            type="button"
            class="time-range-chip"
            :class="{ 'is-active': activeTimeRange === range }"
            :aria-pressed="activeTimeRange === range"
            @click="changeTimeRange(range)"
          >
            {{ getTimeRangeLabel(range) }}
          </button>
        </div>
        <CommonBackButton type="default" />
      </template>
    </AppPageHeader>

    <div v-loading="isLoading" class="stats-content">
      <!-- 概览：4 列等宽 KPI，图标+标签在上、数值在下，数值与标签贴近（§4.8） -->
      <div class="basic-stats-section">
        <div class="stat-card">
          <div class="stat-card__head">
            <span class="stat-icon"><el-icon><ShoppingCart /></el-icon></span>
            <span class="stat-label">总订单数</span>
          </div>
          <div class="stat-value">{{ currentBasicStats.orders }}</div>
        </div>

        <div class="stat-card">
          <div class="stat-card__head">
            <span class="stat-icon"><el-icon><Coin /></el-icon></span>
            <span class="stat-label">总销售额</span>
          </div>
          <div class="stat-value">{{ formatCurrency(currentBasicStats.totalAmount) }}</div>
        </div>

        <div class="stat-card">
          <div class="stat-card__head">
            <span class="stat-icon"><el-icon><TrendCharts /></el-icon></span>
            <span class="stat-label">客单价</span>
          </div>
          <div class="stat-value">{{ formatCurrency(currentBasicStats.avgAmount) }}</div>
        </div>

        <div class="stat-card">
          <div class="stat-card__head">
            <span class="stat-icon"><el-icon><User /></el-icon></span>
            <span class="stat-label">新客户数</span>
          </div>
          <div class="stat-value">{{ currentBasicStats.newCustomers }}</div>
        </div>
      </div>

      <!-- 订单趋势：区块标题只在卡头出现一次，不在画布内重复（§4.7、§3.4） -->
      <section class="order-trend-section">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header">
              <h2 class="section-title">
                <el-icon class="title-icon"><TrendCharts /></el-icon>
                订单趋势
              </h2>
            </div>
          </template>
          <div class="chart-container">
            <div v-show="currentOrderTrend.length > 0" ref="orderChartRef" class="chart"></div>
            <div v-show="currentOrderTrend.length === 0" class="chart-placeholder">
              <el-empty description="暂时没有数据提供" :image-size="100" />
            </div>
          </div>
        </el-card>
      </section>

      <!-- 菜品销量排行 -->
      <section class="dish-sales-section">
        <el-card class="rank-card" shadow="never">
          <template #header>
            <div class="card-header">
              <h2 class="section-title">
                <el-icon class="title-icon"><Trophy /></el-icon>
                菜品销量排行
              </h2>
            </div>
          </template>
          <div v-if="dishSalesRank.length > 0" class="sales-rank-list">
            <div v-for="(dish, index) in dishSalesRank" :key="dish.name" class="sales-rank-item">
              <div class="rank-badge" :class="`rank-${index + 1}`">
                <span class="rank-number">{{ index + 1 }}</span>
              </div>
              <div class="dish-info">
                <div class="dish-name">
                  <el-icon class="dish-icon"><Food /></el-icon>
                  {{ dish.name }}
                </div>
                <div class="dish-sales">销量: {{ dish.sales }} 份</div>
              </div>
              <div class="dish-revenue">
                <span class="revenue-label">销售额</span>
                <span class="revenue-value">{{ formatFullCurrency(dish.revenue) }}</span>
              </div>
            </div>
          </div>
          <div v-else class="no-data-placeholder">
            <el-empty description="暂时没有数据提供" :image-size="100" />
          </div>
        </el-card>
      </section>
    </div>
  </div>
</template>

<style scoped>
/* 本页颜色、间距、圆角、字阶一律取自 tokens.css（§3、§7.1），无裸色值。 */
.statistics-container {
  box-sizing: border-box;
  padding: 0 var(--space-6) var(--space-6);
}

/* 页头动作区：周期 chip 取 --control-height-sm，返回按钮取 --control-height-md（§3.4、§4.7） */
.statistics-container :deep(.app-page-header__actions) {
  flex-wrap: wrap;
}

.time-range-selector {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
}

/* 周期筛选：与 02/03 的状态 chip 同一套形态（§3.4、§4.6） */
.time-range-chip {
  display: inline-flex;
  align-items: center;
  height: var(--control-height-sm);
  padding: 0 var(--space-3);
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  background: var(--surface);
  color: var(--ink-500);
  font-family: inherit;
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  white-space: nowrap;
  cursor: pointer;
  transition:
    background-color var(--duration-fast) var(--ease-out),
    border-color var(--duration-fast) var(--ease-out),
    color var(--duration-fast) var(--ease-out);
}

.time-range-chip:hover {
  border-color: var(--brand-400);
  background: var(--brand-50);
  color: var(--brand-600);
}

.time-range-chip:focus-visible {
  outline: 2px solid var(--brand-400);
  outline-offset: 2px;
}

.time-range-chip.is-active {
  border-color: var(--brand-500);
  background: var(--brand-500);
  color: var(--surface);
}

/* 概览：4 列等宽 KPI 网格，图标+标签一行、数值紧随其下（§4.8） */
.basic-stats-section {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--card-gap-compact);
  margin-bottom: var(--card-gap-compact);
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--card-padding-compact);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
}

.stat-card__head {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  min-width: 0;
}

/* 四项指标共用同一浅底品牌图标，不再各配一色（§4.8、§7.1） */
.stat-icon {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background: var(--brand-50);
  color: var(--brand-500);
  font-size: var(--text-h2);
}

.stat-label {
  min-width: 0;
  overflow: hidden;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  line-height: 32px; /* 与 32px 图标同高，标签盒底即行底，数值紧随 --space-2（§4.8） */
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 数值：--font-num + tabular-nums，与标签贴近（§4.8） */
.stat-value {
  overflow: hidden;
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-h1);
  font-weight: var(--weight-bold);
  font-variant-numeric: tabular-nums;
  letter-spacing: var(--tracking-tight);
  line-height: 32px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 图表卡 / 排行卡：L1 微质感，不做大阴影（§3.4） */
.order-trend-section,
.dish-sales-section {
  margin-bottom: var(--card-gap-compact);
}

.chart-card,
.rank-card {
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
}

.chart-card :deep(.el-card__header),
.rank-card :deep(.el-card__header) {
  padding: var(--space-3) var(--space-5);
  border-bottom: 1px solid var(--line);
}

.chart-card :deep(.el-card__body),
.rank-card :deep(.el-card__body) {
  padding: var(--space-5);
}

.card-header .section-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0;
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
}

.section-title .title-icon {
  color: var(--brand-500);
  font-size: var(--text-h3);
}

.chart-container {
  position: relative;
  height: 320px;
}

.chart {
  width: 100%;
  height: 100%;
}

.chart-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-data-placeholder {
  padding: var(--space-8) 0;
  text-align: center;
}

/* 排行行：L1 微质感列表，静态内容不做 hover 位移（§3.4） */
.sales-rank-list .sales-rank-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3);
  margin-bottom: var(--space-2);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--surface);
}

.sales-rank-item:last-child {
  margin-bottom: 0;
}

.rank-badge {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background: var(--canvas-sunken);
}

.rank-badge .rank-number {
  color: var(--ink-700);
  font-family: var(--font-num);
  font-size: var(--text-body);
  font-weight: var(--weight-semibold);
  font-variant-numeric: tabular-nums;
}

/* 前三名用品牌色阶强调，不引第二套色系（§0、§3.4） */
.rank-badge.rank-1 {
  background: var(--brand-500);
}

.rank-badge.rank-1 .rank-number {
  color: var(--surface);
}

.rank-badge.rank-2 {
  background: var(--brand-300);
}

.rank-badge.rank-3 {
  background: var(--brand-200);
}

.rank-badge.rank-2 .rank-number,
.rank-badge.rank-3 .rank-number {
  color: var(--ink-900);
}

.dish-info {
  flex: 1;
  min-width: 0;
}

.dish-name {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
  overflow: hidden;
  color: var(--ink-900);
  font-size: var(--text-body);
  font-weight: var(--weight-semibold);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-name .dish-icon {
  color: var(--ink-400);
  font-size: var(--text-body);
}

.dish-sales {
  color: var(--ink-500);
  font-size: var(--text-caption);
}

.dish-revenue {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--space-1);
}

.revenue-label {
  color: var(--ink-500);
  font-size: var(--text-caption);
}

/* 金额统一 --ink-900 + tabular-nums，不带红绿蓝（§4.3、§7.1） */
.revenue-value {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-body);
  font-weight: var(--weight-bold);
  font-variant-numeric: tabular-nums;
}

@media (max-width: 1200px) {
  .basic-stats-section {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .statistics-container {
    padding: 0 var(--space-4) var(--space-4);
  }

  .basic-stats-section {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
