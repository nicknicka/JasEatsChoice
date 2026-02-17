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
    formatter: (params) => {
      if (params && params.length > 0) {
        const item = params[0]
        return `${item.axisValue}: ${Math.round(item.value)} 单`
      }
      return ''
    }
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    axisLabel: {
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
      lineStyle: {
        color: '#67c23a'
      },
      itemStyle: {
        color: '#67c23a'
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
    <!-- 页面头部 -->
    <div class="stats-header">
      <div class="header-left">
        <h3 class="page-title">
          <el-icon class="title-icon"><TrendCharts /></el-icon>
          经营统计
        </h3>
      </div>
      <div class="header-right">
        <div class="time-range-selector">
          <el-tag
            v-for="range in timeRangeOptions"
            :key="range"
            :type="activeTimeRange === range ? 'primary' : 'info'"
            effect="plain"
            class="time-range-tag"
            @click="changeTimeRange(range)"
          >
            {{ getTimeRangeLabel(range) }}
          </el-tag>
        </div>
      </div>
    </div>

    <div v-loading="isLoading" class="stats-content">
      <!-- 基本统计卡片 -->
      <el-row :gutter="20" class="basic-stats-section">
        <el-col :span="12">
          <div class="stat-card stat-card-primary">
            <div class="stat-row-first">
              <div class="stat-icon">
                <el-icon><ShoppingCart /></el-icon>
              </div>
              <div class="stat-value">{{ currentBasicStats.orders }}</div>
            </div>
            <div class="stat-label">总订单数</div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="stat-card stat-card-success">
            <div class="stat-row-first">
              <div class="stat-icon">
                <el-icon><Coin /></el-icon>
              </div>
              <div class="stat-value">{{ formatCurrency(currentBasicStats.totalAmount) }}</div>
            </div>
            <div class="stat-label">总销售额</div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="stat-card stat-card-info">
            <div class="stat-row-first">
              <div class="stat-icon">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stat-value">{{ formatCurrency(currentBasicStats.avgAmount) }}</div>
            </div>
            <div class="stat-label">客单价</div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="stat-card stat-card-warning">
            <div class="stat-row-first">
              <div class="stat-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-value">{{ currentBasicStats.newCustomers }}</div>
            </div>
            <div class="stat-label">新客户数</div>
          </div>
        </el-col>
      </el-row>

      <!-- 订单趋势图表 -->
      <el-row :gutter="20" class="order-trend-section">
        <el-col :span="24">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <h4 class="section-title">
                  <el-icon class="title-icon"><TrendCharts /></el-icon>
                  订单趋势
                </h4>
              </div>
            </template>
            <div class="chart-container">
              <div v-show="currentOrderTrend.length > 0" ref="orderChartRef" class="chart"></div>
              <div v-show="currentOrderTrend.length === 0" class="chart-placeholder">
                <el-empty description="暂时没有数据提供" :image-size="100" />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 菜品销量排行 -->
      <el-row :gutter="20" class="dish-sales-section">
        <el-col :span="24">
          <el-card class="rank-card">
            <template #header>
              <div class="card-header">
                <h4 class="section-title">
                  <el-icon class="title-icon"><Trophy /></el-icon>
                  菜品销量排行
                </h4>
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
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped lang="less">
.statistics-container {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  min-height: calc(100vh - 40px);

  // 页面头部
  .stats-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    .header-left {
      .page-title {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 1.714rem;
        font-weight: 600;
        margin: 0;
        color: #303133;

        .title-icon {
          font-size: 2rem;
          color: #409eff;
        }
      }
    }

    .header-right {
      .time-range-selector {
        display: flex;
        gap: 8px;

        .time-range-tag {
          cursor: pointer;
          transition: all 0.3s ease;
          padding: 8px 16px;
          font-size: 0.929rem;
          border-radius: 8px;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
          }
        }
      }
    }
  }

  .stats-content {
    // 统计卡片样式
    .basic-stats-section {
      margin-bottom: 20px;

      .stat-card {
        display: flex;
        flex-direction: column;
        gap: 16px;
        padding: 20px;
        background: white;
        border-radius: 12px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
        transition: all 0.3s ease;
        cursor: default;
        min-height: 120px;

        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
        }

        // 第一行：图标 + 数值
        .stat-row-first {
          display: flex;
          align-items: center;
          gap: 12px;
          flex: 1;

          .stat-icon {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 48px;
            height: 48px;
            border-radius: 10px;
            font-size: 1.5rem;
            color: white;
            flex-shrink: 0;
          }

          .stat-value {
            font-size: clamp(1.286rem, 3vw, 2.286rem);
            font-weight: 700;
            color: #303133;
            line-height: 1.2;
            flex: 1;
            min-width: 0;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }

        // 第二行：描述
        .stat-label {
          font-size: 0.929rem;
          color: #909399;
          text-align: center;
        }

        // 不同颜色主题
        &.stat-card-primary {
          .stat-icon {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }
          .stat-value {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            background-clip: text;
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
          }
        }

        &.stat-card-success {
          .stat-icon {
            background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
          }
          .stat-value {
            color: #67c23a;
          }
        }

        &.stat-card-info {
          .stat-icon {
            background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
          }
          .stat-value {
            color: #409eff;
          }
        }

        &.stat-card-warning {
          .stat-icon {
            background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
          }
          .stat-value {
            color: #e6a23c;
          }
        }
      }
    }

    // 图表卡片样式
    .order-trend-section {
      margin-bottom: 20px;

      .chart-card {
        border-radius: 12px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
        transition: all 0.3s ease;

        &:hover {
          box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
        }

        :deep(.el-card__header) {
          background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
          border-bottom: 1px solid #f0f0f0;
          padding: 16px 24px;

          .card-header {
            .section-title {
              display: flex;
              align-items: center;
              gap: 8px;
              margin: 0;
              font-size: 1.143rem;
              font-weight: 600;
              color: #303133;

              .title-icon {
                font-size: 1.286rem;
                color: #409eff;
              }
            }
          }
        }

        :deep(.el-card__body) {
          padding: 24px;
        }

        .chart-container {
          width: 100%;
          height: 350px;
          position: relative;

          .chart {
            width: 100%;
            height: 100%;
          }

          .chart-placeholder {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
          }
        }
      }
    }

    // 销量排行卡片样式
    .dish-sales-section {
      margin-bottom: 20px;

      .rank-card {
        border-radius: 12px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
        transition: all 0.3s ease;

        &:hover {
          box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
        }

        :deep(.el-card__header) {
          background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
          border-bottom: 1px solid #f0f0f0;
          padding: 16px 24px;

          .card-header {
            .section-title {
              display: flex;
              align-items: center;
              gap: 8px;
              margin: 0;
              font-size: 1.143rem;
              font-weight: 600;
              color: #303133;

              .title-icon {
                font-size: 1.286rem;
                color: #e6a23c;
              }
            }
          }
        }

        :deep(.el-card__body) {
          padding: 24px;
        }

        .no-data-placeholder {
          text-align: center;
          padding: 40px 0;
        }

        .sales-rank-list {
          .sales-rank-item {
            display: flex;
            align-items: center;
            gap: 16px;
            padding: 16px;
            border: 1px solid #ebeef5;
            border-radius: 12px;
            margin-bottom: 16px;
            background: white;
            transition: all 0.3s ease;
            animation: slideInUp 0.5s ease-out forwards;

            &:last-child {
              margin-bottom: 0;
            }

            &:hover {
              box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
              transform: translateY(-3px);
              border-color: #e6a23c;
            }

            .rank-badge {
              width: 48px;
              height: 48px;
              display: flex;
              align-items: center;
              justify-content: center;
              border-radius: 12px;
              flex-shrink: 0;
              background: #f0f2f5;

              .rank-number {
                font-size: 1.286rem;
                font-weight: 700;
                color: #606266;
              }

              // 前三名特殊样式
              &.rank-1 {
                background: linear-gradient(135deg, #ffd700, #ffed4e);
                box-shadow: 0 4px 12px rgba(255, 215, 0, 0.3);

                .rank-number {
                  color: #fff;
                  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
                }
              }

              &.rank-2 {
                background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
                box-shadow: 0 4px 12px rgba(192, 192, 192, 0.3);

                .rank-number {
                  color: #fff;
                  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
                }
              }

              &.rank-3 {
                background: linear-gradient(135deg, #cd7f32, #e3a869);
                box-shadow: 0 4px 12px rgba(205, 127, 50, 0.3);

                .rank-number {
                  color: #fff;
                  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
                }
              }
            }

            .dish-info {
              flex: 1;

              .dish-name {
                display: flex;
                align-items: center;
                gap: 8px;
                font-size: 1rem;
                font-weight: 600;
                margin-bottom: 6px;
                color: #303133;

                .dish-icon {
                  color: #e6a23c;
                  font-size: 1.143rem;
                }
              }

              .dish-sales {
                font-size: 0.857rem;
                color: #909399;
              }
            }

            .dish-revenue {
              display: flex;
              flex-direction: column;
              align-items: flex-end;
              gap: 4px;

              .revenue-label {
                font-size: 0.857rem;
                color: #909399;
              }

              .revenue-value {
                font-size: 1.286rem;
                font-weight: 700;
                color: #67c23a;
              }
            }
          }
        }
      }
    }
  }
}

// 动画定义
@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式布局
@media (max-width: 1200px) {
  .statistics-container {
    .stats-content {
      .basic-stats-section {
        .el-col {
          margin-bottom: 16px;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .statistics-container {
    padding: 12px;

    .stats-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }

    .stats-content {
      .basic-stats-section {
        .el-col {
          margin-bottom: 12px;
        }
      }
    }
  }
}
</style>
