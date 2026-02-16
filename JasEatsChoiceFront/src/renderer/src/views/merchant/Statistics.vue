<script setup>
import api from '../../utils/api'
import { ref, onMounted, watch, nextTick, onUnmounted } from 'vue'
import { useAuthStore } from '../../store/authStore'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DataZoomComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'

// 注册所需组件
use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DataZoomComponent,
  LegendComponent,
  LineChart,
  CanvasRenderer
])

// 统计时间范围选项
const timeRangeOptions = ['today', 'yesterday', 'week', 'month']
const activeTimeRange = ref('today')

// 图表容器宽度
const chartContainerWidth = ref(0)

// 图表引用
const chartRef = ref(null)

// 销售额数据

// 菜品销售数据

// 更新当前显示的销售额数据

// 从后端获取统计数据
const fetchStatisticsData = () => {
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId

  if (!merchantId) {
    console.error('获取统计数据失败: 商家ID不存在')
    return
  }
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
      // 如果获取失败，清空数据
      currentBasicStats.value = { orders: 0, totalAmount: 0.0, avgAmount: 0.0, newCustomers: 0 }
      currentOrderTrend.value = []
      dishSalesRank.value = []
      updateChartData()
    })
}

// 时间范围变化时调用的方法
const changeTimeRange = (range) => {
  activeTimeRange.value = range
  fetchStatisticsData()
}

// 监听时间范围变化更新数据
watch(() => activeTimeRange.value, fetchStatisticsData)

// 页面加载时初始化数据
onMounted(() => {
  fetchStatisticsData()
  // 初始化图表容器宽度
  nextTick(() => {
    updateChartContainerWidth()
  })

  // 监听窗口大小变化
  window.addEventListener('resize', updateChartContainerWidth)
})

// 在组件卸载时移除事件监听器
onUnmounted(() => {
  window.removeEventListener('resize', updateChartContainerWidth)
})

// 更新图表容器宽度
const updateChartContainerWidth = () => {
  nextTick(() => {
    if (chartRef.value && chartRef.value.$el) {
      chartContainerWidth.value = chartRef.value.$el.clientWidth
    } else if (chartRef.value && chartRef.value.$el === undefined) {
      // 如果 $el 不存在，尝试使用元素本身
      chartContainerWidth.value = chartRef.value.clientWidth || 0
    }
  })
}

// 基础统计数据 - 按时间范围

// 当前显示的基础统计数据
const currentBasicStats = ref({ orders: 0, totalAmount: 0.0, avgAmount: 0.0, newCustomers: 0 })

// 订单趋势数据 - 按时间范围

// 当前显示的订单趋势数据
const currentOrderTrend = ref([])

// 菜品销量排行数据
const dishSalesRank = ref([])

// 配置订单趋势图表
const orderChartOptions = ref({
  title: {
    text: '订单趋势',
    textStyle: {
      fontSize: 14
    }
  },
  tooltip: {
    trigger: 'axis',
    formatter: '{b}: {c} 单'
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: '{value} 单'
    }
  },
  series: [
    {
      name: '订单数',
      data: [],
      type: 'line',
      smooth: true,
      lineStyle: {
        color: '#67c23a'
      },
      itemStyle: {
        color: '#67c23a'
      }
    }
  ]
})

// 更新图表数据
const updateChartData = () => {
  orderChartOptions.value.xAxis.data = currentOrderTrend.value.map((item) => item.time)
  orderChartOptions.value.series[0].data = currentOrderTrend.value.map((item) => item.orders)
}

// 只监听currentOrderTrend变化，因为updateChartData会修改orderChartOptions
watch(
  currentOrderTrend,
  () => {
    updateChartData()
  },
  { deep: true }
)
</script>

<template>
  <div class="statistics-container">
    <div class="stats-header">
      <h3 class="page-title">【经营统计】</h3>
      <div class="time-range-selector">
        <el-tag
          v-for="range in timeRangeOptions"
          :key="range"
          :type="activeTimeRange === range ? 'primary' : 'info'"
          effect="plain"
          class="time-range-tag"
          @click="changeTimeRange(range)"
        >
          {{
            range === 'today'
              ? '今日'
              : range === 'yesterday'
                ? '昨日'
                : range === 'week'
                  ? '本周'
                  : '本月'
          }}
        </el-tag>
      </div>
    </div>

    <div class="stats-content">
      <!-- 基本统计卡片 -->
      <div class="basic-stats-section">
        <div class="stat-card">
          <div class="stat-icon orders-icon">🍽️</div>
          <div class="stat-info">
            <div class="stat-label">总订单数</div>
            <div class="stat-value">{{ currentBasicStats.orders }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon revenue-icon">💰</div>
          <div class="stat-info">
            <div class="stat-label">总销售额</div>
            <div class="stat-value">¥{{ currentBasicStats.totalAmount.toFixed(2) }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon avg-icon">📊</div>
          <div class="stat-info">
            <div class="stat-label">客单价</div>
            <div class="stat-value">¥{{ currentBasicStats.avgAmount.toFixed(2) }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon new-customers-icon">👤</div>
          <div class="stat-info">
            <div class="stat-label">新客户数</div>
            <div class="stat-value">{{ currentBasicStats.newCustomers }}</div>
          </div>
        </div>
      </div>

      <!-- 订单趋势图表 -->
      <div class="order-trend-section">
        <h4 class="section-title">📈 订单趋势</h4>
        <div v-show="true" class="chart-container">
          <v-chart
            v-if="chartContainerWidth > 0 && currentOrderTrend.length > 0"
            ref="chartRef"
            :options="orderChartOptions"
            style="height: 250px; width: 100%"
            :autoresize="true"
          />
          <div v-else-if="chartContainerWidth > 0" class="chart-placeholder">暂时没有数据提供</div>
          <div v-else class="chart-placeholder chart-loading">
            <span class="loading-text">图表加载中...</span>
          </div>
        </div>
      </div>

      <!-- 菜品销量排行 -->
      <div class="dish-sales-section">
        <h4 class="section-title">🏆 菜品销量排行</h4>
        <div v-if="dishSalesRank.length > 0" class="sales-rank-list">
          <div v-for="(dish, index) in dishSalesRank" :key="dish.name" class="sales-rank-item">
            <div class="rank-number">{{ index + 1 }}</div>
            <div class="dish-info">
              <div class="dish-name">{{ dish.name }}</div>
              <div class="dish-sales">销量: {{ dish.sales }} 份</div>
            </div>
            <div class="dish-revenue">¥{{ dish.revenue }}</div>
          </div>
        </div>
        <div v-else class="no-data-placeholder">暂时没有数据提供</div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.statistics-container {
  padding: 0 20px 20px 20px;

  .stats-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .page-title {
      font-size: 1.286rem /* 原值: 18px */;
      font-weight: 600;
      margin: 0;
    }

    .time-range-selector {
      display: flex;
      gap: 8px;

      .time-range-tag {
        cursor: pointer;
        &:hover {
          opacity: 0.8;
        }
      }
    }
  }

  .stats-content {
    .basic-stats-section {
      display: flex;
      gap: 20px;
      margin-bottom: 24px;
      flex-wrap: wrap;

      .stat-card {
        display: flex;
        align-items: center;
        gap: 16px;
        background-color: #fff;
        padding: 20px;
        border-radius: 8px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
        min-width: 200px;
        flex: 1;

        .stat-icon {
          font-size: 2.286rem /* 原值: 32px */;
        }

        .stat-info {
          .stat-label {
            font-size: 1rem /* 原值: 14px */;
            color: #606266;
            margin-bottom: 4px;
          }

          .stat-value {
            font-size: 1.429rem /* 原值: 20px */;
            font-weight: 600;
            color: #303133;
          }
        }
      }
    }

    .order-trend-section {
      background-color: #fff;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 20px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

      .section-title {
        font-size: 1.143rem /* 原值: 16px */;
        font-weight: 600;
        margin-bottom: 20px;
      }

      .chart-container {
        min-height: 250px;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;

        .chart-placeholder {
          color: #909399;
          font-size: 1rem /* 原值: 14px */;
        }
      }
    }

    .dish-sales-section {
      background-color: #fff;
      border-radius: 8px;
      padding: 16px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

      .section-title {
        font-size: 1.143rem /* 原值: 16px */;
        font-weight: 600;
        margin-bottom: 20px;
      }

      .no-data-placeholder {
        color: #909399;
        font-size: 1rem /* 原值: 14px */;
        padding: 40px 0;
        text-align: center;
      }

      .sales-rank-list {
        .sales-rank-item {
          display: flex;
          align-items: center;
          gap: 16px;
          padding: 12px 0;
          border-bottom: 1px solid #eee;

          &:last-child {
            border-bottom: none;
          }

          .rank-number {
            font-size: 1.286rem /* 原值: 18px */;
            font-weight: 600;
            width: 30px;
            text-align: center;
          }

          .dish-info {
            flex: 1;

            .dish-name {
              font-size: 1rem /* 原值: 14px */;
              font-weight: 500;
              margin-bottom: 4px;
            }

            .dish-sales {
              font-size: 0.857rem /* 原值: 12px */;
              color: #606266;
            }
          }

          .dish-revenue {
            font-size: 1.143rem /* 原值: 16px */;
            font-weight: 600;
            color: #67c23a;
          }
        }
      }
    }
  }
}
</style>
