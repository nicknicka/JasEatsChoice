<template>
  <div class="data-statistics-container">
    <!-- 页头：统一标题容器（§4.7） -->
    <AppPageHeader title="数据统计" subtitle="系统运营数据分析和可视化" :compact="true" />

    <!-- 日期选择器 -->
    <el-card class="date-selector-card" shadow="never">
      <el-form :inline="true">
        <el-form-item label="统计周期">
          <el-radio-group v-model="dateRange" @change="fetchStatistics">
            <el-radio-button label="7">最近7天</el-radio-button>
            <el-radio-button label="30">最近30天</el-radio-button>
            <el-radio-button label="90">最近90天</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="自定义">
          <el-date-picker
            v-model="customDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="fetchStatistics"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Refresh" :loading="loading" @click="fetchStatistics">
            刷新
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 核心指标卡片（§4.8 线性图标 + 数值 + 标签，四卡一行） -->
    <!-- 保留 .stats-cards .stat-card 这两个类名：Playwright 13.8 依赖该 DOM 契约 -->
    <div class="stats-cards">
      <AppStatCard
        class="stat-card"
        label="总用户数"
        :value="stats.totalUsers"
        icon="User"
        :trend="delta(stats.newUsers)"
      />
      <AppStatCard
        class="stat-card"
        label="总商家数"
        :value="stats.totalMerchants"
        icon="Shop"
        :trend="delta(stats.newMerchants)"
      />
      <AppStatCard
        class="stat-card"
        label="总订单数"
        :value="stats.totalOrders"
        icon="Tickets"
        :trend="delta(stats.newOrders)"
      />
      <AppStatCard
        class="stat-card"
        label="总收入"
        :value="formatMoney(stats.totalRevenue)"
        icon="Money"
        :trend="delta(stats.newRevenue)"
      />
    </div>

    <!-- 图表区域：整片加载遮罩，切周期不整页白屏（§4.6） -->
    <div v-loading="loading" class="charts-container">
      <!-- 用户增长趋势 -->
      <el-card class="chart-card" shadow="never">
        <template #header>
          <div class="chart-header">
            <span class="chart-title">用户增长趋势</span>
            <span class="chart-summary"
              >近 {{ periodDays }} 天新增用户 {{ sum(trend.newUsers) }}</span
            >
          </div>
        </template>
        <div class="chart-body">
          <div ref="userTrendChartRef" class="chart"></div>
          <!-- 无正向数据时用中文空态盖住贴底折线（§4.5、§5.5） -->
          <AppEmptyState
            v-if="!loading && !userTrendHasData"
            class="chart-empty"
            compact
            icon="TrendCharts"
            :description="`所选周期（近 ${periodDays} 天）内没有新增用户数据`"
            action-text="刷新"
            @action="fetchStatistics"
          />
        </div>
      </el-card>

      <!-- 订单趋势 -->
      <el-card class="chart-card" shadow="never">
        <template #header>
          <div class="chart-header">
            <span class="chart-title">订单趋势</span>
            <span class="chart-summary">近 {{ periodDays }} 天订单 {{ sum(trend.orders) }}</span>
          </div>
        </template>
        <div class="chart-body">
          <div ref="orderTrendChartRef" class="chart"></div>
          <AppEmptyState
            v-if="!loading && !orderTrendHasData"
            class="chart-empty"
            compact
            icon="Tickets"
            :description="`所选周期（近 ${periodDays} 天）内没有订单数据`"
            action-text="刷新"
            @action="fetchStatistics"
          />
        </div>
      </el-card>

      <!-- 收入趋势 -->
      <el-card class="chart-card" shadow="never">
        <template #header>
          <div class="chart-header">
            <span class="chart-title">收入趋势</span>
            <span class="chart-summary"
              >近 {{ periodDays }} 天收入 {{ formatMoney(sum(trend.revenue)) }}</span
            >
          </div>
        </template>
        <div class="chart-body">
          <div ref="revenueTrendChartRef" class="chart"></div>
          <AppEmptyState
            v-if="!loading && !revenueTrendHasData"
            class="chart-empty"
            compact
            icon="Money"
            :description="`所选周期（近 ${periodDays} 天）内没有收入数据`"
            action-text="刷新"
            @action="fetchStatistics"
          />
        </div>
      </el-card>

      <!-- 用户分布 -->
      <el-card class="chart-card" shadow="never">
        <template #header>
          <div class="chart-header">
            <span class="chart-title">用户类型分布</span>
            <span class="chart-summary"
              >共 {{ stats.totalUsers }} 名用户 / {{ stats.totalMerchants }} 家商家</span
            >
          </div>
        </template>
        <div class="chart-body">
          <div ref="userDistributionChartRef" class="chart"></div>
          <AppEmptyState
            v-if="!loading && !distributionHasData"
            class="chart-empty"
            compact
            icon="PieChart"
            description="还没有用户与商家数据"
            action-text="刷新"
            @action="fetchStatistics"
          />
        </div>
      </el-card>
    </div>

    <!-- 详细数据表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="chart-header">
          <span class="chart-title">每日详细数据</span>
          <el-button type="primary" size="small" :icon="Download" @click="handleExport"
            >导出数据</el-button
          >
        </div>
      </template>
      <!-- 空数据：紧凑中文空状态替换表体（§4.3、§5.5） -->
      <AppEmptyState
        v-if="!loading && dailyData.length === 0"
        compact
        icon="Document"
        description="所选周期内没有每日明细数据，换个周期试试"
        action-text="刷新"
        @action="fetchStatistics"
      />
      <el-table v-else v-loading="loading" :data="dailyData" stripe>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="newUsers" label="新增用户" width="100" />
        <el-table-column prop="newMerchants" label="新增商家" width="100" />
        <el-table-column prop="totalOrders" label="订单数" width="100" />
        <el-table-column prop="completedOrders" label="完成订单" width="100" />
        <el-table-column prop="revenue" label="收入" width="120">
          <template #default="{ row }">¥{{ Number(row.revenue || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="activeUsers" label="活跃用户" width="100" />
        <el-table-column prop="averageOrderAmount" label="客单价" width="120">
          <template #default="{ row }"
            >¥{{ Number(row.averageOrderAmount || 0).toFixed(2) }}</template
          >
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppStatCard from '@/components/common/AppStatCard.vue'
import AppEmptyState from '@/components/common/AppEmptyState.vue'
import statisticsApi from '../../api/statistics'

const dateRange = ref('7')
const customDateRange = ref(null)
const loading = ref(false)

const stats = reactive({
  totalUsers: 0,
  newUsers: 0,
  totalMerchants: 0,
  newMerchants: 0,
  totalOrders: 0,
  newOrders: 0,
  totalRevenue: 0,
  newRevenue: 0
})

const dailyData = ref([])

// 趋势原始数据：模板摘要与画布共用同一份，空态判定不看画布
const trend = reactive({
  dates: [],
  newUsers: [],
  orders: [],
  revenue: []
})

// 图表引用
const userTrendChartRef = ref(null)
const orderTrendChartRef = ref(null)
const revenueTrendChartRef = ref(null)
const userDistributionChartRef = ref(null)

// 图表实例
let userTrendChart = null
let orderTrendChart = null
let revenueTrendChart = null
let userDistributionChart = null

// 图表用 Canvas 渲染，拿不到 CSS 变量，运行时从 tokens.css 读唯一颜色源（§7.1）
const tokenColor = (name) =>
  getComputedStyle(document.documentElement).getPropertyValue(name).trim()

// 面积色由同一条 token 派生，避免再引入第二处颜色定义（§7.1）
const withAlpha = (color, alpha) => {
  const hex = String(color).replace('#', '')
  if (hex.length !== 6) return color
  const r = parseInt(hex.slice(0, 2), 16)
  const g = parseInt(hex.slice(2, 4), 16)
  const b = parseInt(hex.slice(4, 6), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

// 每个指标绑固定主题色，去掉默认蓝绿随机感（§3.1）
const METRIC_TOKENS = {
  newUsers: '--info',
  orders: '--success',
  revenue: '--brand-500'
}

const sum = (arr) => (Array.isArray(arr) ? arr.reduce((acc, v) => acc + Number(v || 0), 0) : 0)
const hasPositive = (arr) => Array.isArray(arr) && arr.some((v) => Number(v) > 0)

const formatMoney = (value) =>
  `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`

// 增量只有为正才用成功色，0 落中性（§3.1）
const delta = (value) => ({
  direction: Number(value) > 0 ? 'up' : 'flat',
  text: `+${Number(value || 0)}`
})

// 接口只给用户/商家总量，没有管理员口径；分布直接用真实总量，不再写死数字（§5.2）
const distribution = computed(() => [
  { value: Number(stats.totalUsers) || 0, name: '普通用户' },
  { value: Number(stats.totalMerchants) || 0, name: '商家用户' }
])

const periodDays = computed(() => trend.dates.length)
const userTrendHasData = computed(() => hasPositive(trend.newUsers))
const orderTrendHasData = computed(() => hasPositive(trend.orders))
const revenueTrendHasData = computed(() => hasPositive(trend.revenue))
const distributionHasData = computed(() => distribution.value.some((item) => item.value > 0))

// 折线通用骨架：网格线、坐标字色、线色全部取 token（§3.3、§7.1）
const buildLineOption = (dates, data, name, tokenName) => {
  const color = tokenColor(tokenName)
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 8, right: 16, top: 16, bottom: 8, containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLine: { lineStyle: { color: tokenColor('--line') } },
      axisLabel: { color: tokenColor('--ink-500'), fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: tokenColor('--line') } },
      axisLabel: { color: tokenColor('--ink-500'), fontSize: 12 }
    },
    series: [
      {
        name,
        type: 'line',
        smooth: true,
        symbolSize: 6,
        data,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: withAlpha(color, 0.28) },
            { offset: 1, color: withAlpha(color, 0.04) }
          ])
        },
        lineStyle: { color, width: 2 },
        itemStyle: { color }
      }
    ]
  }
}

// 渲染用户增长趋势图
const renderUserTrendChart = (dates, data) => {
  if (!userTrendChartRef.value) return
  if (!userTrendChart) userTrendChart = echarts.init(userTrendChartRef.value)
  userTrendChart.setOption(buildLineOption(dates, data, '新增用户', METRIC_TOKENS.newUsers))
}

// 渲染订单趋势图
const renderOrderTrendChart = (dates, data) => {
  if (!orderTrendChartRef.value) return
  if (!orderTrendChart) orderTrendChart = echarts.init(orderTrendChartRef.value)
  orderTrendChart.setOption(buildLineOption(dates, data, '订单数', METRIC_TOKENS.orders))
}

// 渲染收入趋势图
const renderRevenueTrendChart = (dates, data) => {
  if (!revenueTrendChartRef.value) return
  if (!revenueTrendChart) revenueTrendChart = echarts.init(revenueTrendChartRef.value)
  const option = buildLineOption(dates, data, '收入', METRIC_TOKENS.revenue)
  option.tooltip.formatter = '{b}<br/>收入: ¥{c}'
  revenueTrendChart.setOption(option)
}

// 渲染用户分布饼图（数据来自真实总量）
const renderUserDistributionChart = (data) => {
  if (!userDistributionChartRef.value) return
  if (!userDistributionChart) userDistributionChart = echarts.init(userDistributionChartRef.value)
  const option = {
    color: [tokenColor('--brand-500'), tokenColor('--info')],
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}（{d}%）'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      textStyle: { color: tokenColor('--ink-500'), fontSize: 12 }
    },
    series: [
      {
        name: '用户类型',
        type: 'pie',
        radius: '60%',
        data,
        label: { color: tokenColor('--ink-700') },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: tokenColor('--overlay')
          }
        }
      }
    ]
  }
  userDistributionChart.setOption(option)
}

// 获取统计数据
const fetchStatistics = async () => {
  loading.value = true
  try {
    const response = await statisticsApi.getDashboardStatistics(dateRange.value)

    if (response.code === '200' && response.data) {
      const data = response.data
      const summary = data.summary || {}

      stats.totalUsers = summary.totalUsers || 0
      stats.newUsers = summary.newUsers || 0
      stats.totalMerchants = summary.totalMerchants || 0
      stats.newMerchants = summary.newMerchants || 0
      stats.totalOrders = summary.totalOrders || 0
      stats.newOrders = summary.newOrders || 0
      stats.totalRevenue = summary.totalRevenue || 0
      stats.newRevenue = summary.newRevenue || 0

      dailyData.value = data.daily || []

      const trends = data.trends || {}
      trend.dates = trends.dates || []
      trend.newUsers = trends.newUsers || []
      trend.orders = trends.orders || []
      trend.revenue = trends.revenue || []

      await nextTick()
      renderUserTrendChart(trend.dates, trend.newUsers)
      renderOrderTrendChart(trend.dates, trend.orders)
      renderRevenueTrendChart(trend.dates, trend.revenue)
      renderUserDistributionChart(distribution.value)
    } else {
      ElMessage.error(response.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 导出数据
const handleExport = async () => {
  try {
    const response = await statisticsApi.exportStatistics({
      days: dateRange.value,
      format: 'excel'
    })

    if (response.code === '200') {
      if (response.data.downloadUrl) {
        window.open(response.data.downloadUrl, '_blank')
        ElMessage.success('数据导出成功')
      } else {
        ElMessage.info('数据导出功能开发中，敬请期待')
      }
    } else {
      ElMessage.error(response.message || '导出失败')
    }
  } catch (error) {
    console.error('导出数据失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  }
}

// 窗口大小改变时重新渲染图表
const handleResize = () => {
  userTrendChart?.resize()
  orderTrendChart?.resize()
  revenueTrendChart?.resize()
  userDistributionChart?.resize()
}

onMounted(() => {
  fetchStatistics()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  userTrendChart?.dispose()
  orderTrendChart?.dispose()
  revenueTrendChart?.dispose()
  userDistributionChart?.dispose()
})
</script>

<style scoped lang="less">
.data-statistics-container {
  padding: var(--card-padding-compact) var(--page-gutter);
  background: var(--canvas);
  max-width: 1440px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;

  .date-selector-card {
    margin-bottom: var(--space-8);
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    gap: var(--card-gap-compact);
    margin-bottom: var(--space-8);
  }

  .charts-container {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: var(--card-gap-compact);
    margin-bottom: var(--space-8);
  }

  /* 趋势图卡头统一：标题（--text-h3/600/--ink-900）+ 结论摘要（--ink-500）
     与 12-提现审核 共用同一套 .chart-header / .chart-title 命名（§4.7） */
  .chart-header {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: var(--space-3);
    min-width: 0;

    .chart-title {
      color: var(--ink-900);
      font-size: var(--text-h3);
      font-weight: var(--weight-semibold);
    }

    .chart-summary {
      color: var(--ink-500);
      font-size: var(--text-body-sm);
      white-space: nowrap;
    }
  }

  .chart-card {
    .chart-body {
      position: relative;
    }

    .chart {
      height: 300px;
    }

    /* 空态盖住贴底折线，避免“直线 + 大面积空白”（§4.5） */
    .chart-empty {
      position: absolute;
      inset: 0;
      background: var(--surface);
    }
  }

  .table-card {
    .chart-header {
      align-items: center;
    }
  }
}

@media (max-width: 1200px) {
  .charts-container {
    grid-template-columns: 1fr;
  }
}

/* §4.3：admin 列表只保留单行 40px / 双行 ≤56px 两档，列宽不足由单元格省略而不是折行。 */
.data-statistics-container :deep(.el-table__body .el-table__cell) {
  padding-top: 0;
  padding-bottom: 0;
}

.data-statistics-container :deep(.el-table__body .el-table__row) {
  height: 40px;
}
</style>
