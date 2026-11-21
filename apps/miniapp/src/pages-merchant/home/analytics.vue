<template>
  <view class="analytics-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <view class="nav-title">数据分析</view>
      <view class="nav-action" @click="exportReport">
        <text class="icon">📊</text>
        <text class="text">导出</text>
      </view>
    </view>

    <!-- 时间选择器 -->
    <view class="time-selector">
      <view class="time-tabs">
        <view
          class="time-tab"
          v-for="(tab, index) in timeTabs"
          :key="index"
          :class="{ active: currentTimeTab === index }"
          @click="switchTimeTab(index)"
        >
          <text class="tab-text">{{ tab }}</text>
        </view>
      </view>
      <view class="custom-time" @click="showDatePicker">
        <text class="date-text">{{ dateRange }}</text>
        <text class="icon">📅</text>
      </view>
    </view>

    <!-- 加载 / 错误：失败时不渲染任何指标，避免旧数据伪装成当前数据 -->
    <view class="state-block" v-if="loading">
      <text class="state-text">加载中...</text>
    </view>
    <view class="state-block" v-else-if="loadError">
      <text class="state-text">{{ loadError }}</text>
      <button class="state-btn" @tap="loadAnalyticsData">重试</button>
    </view>

    <template v-else>
    <!-- 关键指标卡片 -->
    <view class="metrics-cards">
      <view class="metric-card" v-for="(metric, index) in keyMetrics" :key="index">
        <view class="metric-header">
          <text class="metric-title">{{ metric.title }}</text>
        </view>
        <view class="metric-value">
          <text class="value">{{ metric.value }}</text>
          <text class="unit">{{ metric.unit }}</text>
        </view>
      </view>
    </view>

    <!-- 图表区域 -->
    <view class="charts-section">
      <!-- 营业额趋势 -->
      <view class="chart-card">
        <view class="chart-header">
          <text class="chart-title">订单趋势</text>
        </view>
        <view class="chart-container">
          <canvas class="turnover-chart" canvas-id="turnoverChart"></canvas>
        </view>
      </view>

      <!-- 订单分布 -->
      <view class="chart-card">
        <view class="chart-header">
          <text class="chart-title">订单分布</text>
        </view>
        <view class="chart-container">
          <canvas class="order-chart" canvas-id="orderChart"></canvas>
        </view>
      </view>

      <!-- 菜品销售排行 -->
      <view class="ranking-card">
        <view class="ranking-header">
          <text class="ranking-title">菜品销售排行</text>
          <view class="ranking-tabs">
            <view class="ranking-tab" :class="{ active: rankingType === 'quantity' }" @click="switchRankingType('quantity')">
              <text class="tab-text">按销量</text>
            </view>
            <view class="ranking-tab" :class="{ active: rankingType === 'amount' }" @click="switchRankingType('amount')">
              <text class="tab-text">按金额</text>
            </view>
          </view>
        </view>
        <view class="ranking-list">
          <view class="ranking-item" v-for="(item, index) in rankingList" :key="index">
            <view class="rank-number" :class="{ top3: index < 3 }">{{ index + 1 }}</view>
            <view class="dish-image dish-image-placeholder">{{ item.name.charAt(0) }}</view>
            <view class="dish-info">
              <text class="dish-name">{{ item.name }}</text>
              <text class="dish-sales">{{ rankingType === 'quantity' ? `${item.sales}份` : `¥${item.amount}` }}</text>
            </view>
            <view class="dish-amount">
              <text class="amount-value">¥{{ item.amount }}</text>
            </view>
          </view>
        </view>
      </view>

    </view>
    </template>

    <!-- 日期选择器弹窗 -->
    <uni-popup ref="datePopup" type="bottom">
      <view class="date-picker-popup">
        <view class="popup-header">
          <text class="popup-title">选择日期范围</text>
          <view class="close-btn" @click="closeDatePicker">✕</view>
        </view>
        <view class="popup-content">
          <view class="date-mode-selector">
            <view class="mode-item" :class="{ active: dateMode === 'range' }" @click="dateMode = 'range'">
              <text class="mode-text">日期范围</text>
            </view>
            <view class="mode-item" :class="{ active: dateMode === 'month' }" @click="dateMode = 'month'">
              <text class="mode-text">按月</text>
            </view>
          </view>
          <view class="date-picker-content">
            <picker-view class="date-picker" mode="date" :value="datePickerValue" @change="onDateChange">
              <picker-view-column>
                <view class="picker-item" v-for="(item, index) in years" :key="index">{{ item }}年</view>
              </picker-view-column>
              <picker-view-column>
                <view class="picker-item" v-for="(item, index) in months" :key="index">{{ item }}月</view>
              </picker-view-column>
              <picker-view-column>
                <view class="picker-item" v-for="(item, index) in days" :key="index">{{ item }}日</view>
              </picker-view-column>
            </picker-view>
          </view>
        </view>
        <view class="popup-actions">
          <button class="btn btn-outline" @click="closeDatePicker">取消</button>
          <button class="btn btn-primary" @click="confirmDate">确认</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import uCharts from '@/utils/u-charts.js'
import { merchantApi } from '@/api/modules/merchant.js'
import { useMerchantStore } from '@/store/modules/merchant'

// 日期选择器
const datePopup = ref(null)
const dateMode = ref('range')
const datePickerValue = ref([new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate()])
const years = ref([])
const months = ref([])
const days = ref([])

// 时间选择
const currentTimeTab = ref(0)
const timeTabs = ref(['今天', '昨天', '本周', '本月', '自定义'])
const dateRange = ref('今天')

// 排行类型
const rankingType = ref('quantity')

// 加载与错误状态
const loading = ref(false)
const loadError = ref('')
const merchantStore = useMerchantStore()

// 关键指标：全部来自 /v1/merchant/{id}/statistics 的 basicStats
const keyMetrics = ref([])

// 销售排行：来自同一个接口的 dishSalesRank
const rankingList = ref([])

// 趋势图与订单分布的数据源
const orderTrend = ref([])

// 初始化日期数据
onMounted(() => {
  const now = new Date()
  const currentYear = now.getFullYear()
  const currentMonth = now.getMonth() + 1
  const daysInMonth = new Date(currentYear, currentMonth, 0).getDate()

  years.value = Array.from({ length: 5 }, (_, i) => currentYear - i)
  months.value = Array.from({ length: 12 }, (_, i) => i + 1)
  days.value = Array.from({ length: daysInMonth }, (_, i) => i + 1)

  // 初始化图表后拉取真实经营数据（图表数据随接口刷新）
  initCharts()
  loadAnalyticsData()
})

onUnmounted(() => {
  // 清理图表
})

// 切换时间标签
const switchTimeTab = (index) => {
  currentTimeTab.value = index
  updateDateRange()
  loadAnalyticsData()
}

// 更新日期范围
const updateDateRange = () => {
  const now = new Date()
  const tabs = ['今天', '昨天', '本周', '本月', '自定义']

  switch (tabs[currentTimeTab.value]) {
    case 0: // 今天
      dateRange.value = `${now.getMonth() + 1}/${now.getDate()}`
      break
    case 1: // 昨天
      const yesterday = new Date(now.getTime() - 86400000)
      dateRange.value = `${yesterday.getMonth() + 1}/${yesterday.getDate()}`
      break
    case 2: // 本周
      const weekStart = new Date(now.getTime() - (now.getDay() - 1) * 86400000)
      dateRange.value = `${weekStart.getMonth() + 1}/${weekStart.getDate()} - ${now.getMonth() + 1}/${now.getDate()}`
      break
    case 3: // 本月
      dateRange.value = `${now.getMonth() + 1}月`
      break
    case 4: // 自定义
      showDatePicker()
      break
  }
}

// 显示日期选择器
const showDatePicker = () => {
  datePopup.value?.open()
}

// 关闭日期选择器
const closeDatePicker = () => {
  datePopup.value?.close()
}

// 确认日期
const confirmDate = () => {
  const [year, month, day] = datePickerValue.value
  dateRange.value = `${year}/${month}/${day}`
  closeDatePicker()
  loadAnalyticsData()
}

// 日期变化
const onDateChange = (e) => {
  datePickerValue.value = e.detail.value
}

// 加载分析数据
// 时间标签 → 后端 timeRange 取值（与 MerchantController.getStatistics 的 switch 对齐）
const TIME_RANGES = ['today', 'yesterday', 'week', 'month', 'all']

const formatMoney = (value) => Number(value || 0).toFixed(2)

/**
 * 加载分析数据
 * 只展示接口真实返回的内容；接口没有的数据（趋势百分比、高峰时段、区域、复购率）不编造。
 */
const loadAnalyticsData = async () => {
  const merchantId = merchantStore.resolveMerchantId()
  if (!merchantId) {
    loadError.value = '未检测到商家信息，请重新登录'
    keyMetrics.value = []
    rankingList.value = []
    orderTrend.value = []
    return
  }

  loading.value = true
  loadError.value = ''

  try {
    const res = await merchantApi.getStatistics(merchantId, {
      timeRange: TIME_RANGES[currentTimeTab.value] || 'today'
    })

    if (!res || !res.success || !res.data) {
      loadError.value = (res && res.message) || '加载经营数据失败'
      keyMetrics.value = []
      rankingList.value = []
      orderTrend.value = []
      return
    }

    const data = res.data
    const basic = data.basicStats || {}

    keyMetrics.value = [
      { title: '总营业额', value: formatMoney(basic.totalAmount), unit: '元' },
      { title: '订单数', value: String(basic.orders ?? 0), unit: '单' },
      { title: '客单价', value: formatMoney(basic.avgAmount), unit: '元' },
      { title: '下单人数', value: String(basic.newCustomers ?? 0), unit: '人' }
    ]

    rankingList.value = (data.dishSalesRank || []).map(item => ({
      name: item.name,
      sales: item.sales,
      revenue: Number(item.revenue) || 0,
      amount: formatMoney(item.revenue)
    }))

    orderTrend.value = data.orderTrend || []
    updateCharts()
  } catch (error) {
    console.error('加载经营分析失败:', error)
    loadError.value = '加载经营数据失败，请稍后重试'
    keyMetrics.value = []
    rankingList.value = []
    orderTrend.value = []
  } finally {
    loading.value = false
  }
}

// 切换排行类型：按销量或按金额重新排序（数据都来自接口）
const switchRankingType = (type) => {
  rankingType.value = type
  const sortKey = type === 'amount' ? 'revenue' : 'sales'
  rankingList.value = [...rankingList.value].sort((a, b) => b[sortKey] - a[sortKey])
}

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    drawTurnoverChart()
    drawOrderChart()
  })
}

// 更新图表
const updateCharts = () => {
  nextTick(() => {
    drawTurnoverChart()
    drawOrderChart()
  })
}

// 趋势图：接口只提供订单量趋势（orderTrend: [{time, orders}]），不画营业额曲线
const drawTurnoverChart = () => {
  const ctx = uni.createCanvasContext('turnoverChart')
  const systemInfo = uni.getSystemInfoSync()
  const width = systemInfo.windowWidth - 64
  const height = 300

  const chart = new uCharts({
    type: 'line',
    context: ctx,
    width: width,
    height: height,
    categories: orderTrend.value.map(item => item.time),
    series: [{
      name: '订单量',
      data: orderTrend.value.map(item => Number(item.orders) || 0),
      color: '#ff6b6b',
      smooth: true
    }],
    xAxis: {
      disableGrid: true
    },
    yAxis: {
      gridType: 'dash',
      dashLength: 2
    },
    legend: {
      show: false
    },
    extra: {
      area: true,
      gradient: true
    }
  })

  chart.init()
}

// 订单分布图：接口未提供状态分布，这里按当前时段订单总量呈现，不编造分段
const drawOrderChart = () => {
  const ctx = uni.createCanvasContext('orderChart')
  const systemInfo = uni.getSystemInfoSync()
  const width = systemInfo.windowWidth - 64
  const height = 300

  const totalOrders = orderTrend.value.reduce((sum, item) => sum + (Number(item.orders) || 0), 0)
  const series = totalOrders > 0
    ? [{ data: [{ name: '订单量', value: totalOrders, color: '#FF6B35' }] }]
    : [{ data: [] }]

  const chart = new uCharts({
    type: 'pie',
    context: ctx,
    width: width,
    height: height,
    series: series,
    legend: {
      position: 'bottom'
    }
  })

  chart.init()
}

// 导出报表
// 导出报表：后端暂无导出接口，如实告知未开放，不弹假的导出进度
const exportReport = () => {
  uni.showModal({
    title: '暂未开放',
    content: '报表导出功能尚未开放，当前可在本页查看实时经营数据。',
    showCancel: false
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
@import '@/styles/mixins.scss';

.analytics-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* 加载 / 错误态：与 statistics 页保持同一套写法 */
.state-block {
  @include flex-center;
  flex-direction: column;
  gap: 20rpx;
  padding: 80rpx 40rpx;
  margin: 20rpx;
  background: #fff;
  border-radius: 16rpx;
}

.state-text {
  font-size: 28rpx;
  color: #999;
}

.state-btn {
  min-width: 200rpx;
  height: 72rpx;
  @include flex-center;
  background: linear-gradient(135deg, #FF6B35, #FF8F61);
  color: #fff;
  font-size: 28rpx;
  border-radius: 36rpx;
  border: none;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  background: #ffffff;
  padding: 0 32rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .nav-back {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .icon {
      font-size: 48rpx;
      color: #333333;
    }
  }

  .nav-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333333;
  }

  .nav-action {
    display: flex;
    align-items: center;
    gap: 8rpx;

    .icon {
      font-size: 32rpx;
    }

    .text {
      font-size: 26rpx;
      color: #333333;
    }
  }
}

.time-selector {
  background: #ffffff;
  padding: 24rpx 32rpx;
  margin-bottom: 24rpx;

  .time-tabs {
    display: flex;
    gap: 16rpx;
    margin-bottom: 24rpx;

    .time-tab {
      padding: 12rpx 24rpx;
      background: #f5f5f5;
      border-radius: 24rpx;
      border: 2rpx solid transparent;

      .tab-text {
        font-size: 26rpx;
        color: #666666;
      }

      &.active {
        background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
        border-color: #ff6b6b;

        .tab-text {
          color: #ffffff;
        }
      }
    }
  }

  .custom-time {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16rpx 24rpx;
    background: #f5f5f5;
    border-radius: 12rpx;

    .date-text {
      font-size: 26rpx;
      color: #333333;
    }

    .icon {
      font-size: 28rpx;
    }
  }
}

.metrics-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  padding: 0 32rpx 24rpx;

  .metric-card {
    flex: 1;
    min-width: calc((100% - 48rpx) / 2);
    background: #ffffff;
    border-radius: 16rpx;
    padding: 24rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);

    .metric-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;

      .metric-title {
        font-size: 26rpx;
        color: #999999;
      }

      .metric-trend {
        display: flex;
        align-items: center;
        gap: 4rpx;
        font-size: 22rpx;

        &.up {
          color: #52c41a;
        }

        &.down {
          color: #ff4d4f;
        }

        .trend-icon {
          font-size: 20rpx;
        }

        .trend-value {
          font-weight: 500;
        }
      }
    }

    .metric-value {
      display: flex;
      align-items: baseline;
      gap: 4rpx;

      .value {
        font-size: 48rpx;
        font-weight: bold;
        color: #333333;
      }

      .unit {
        font-size: 24rpx;
        color: #999999;
      }
    }
  }
}

.charts-section {
  padding: 0 32rpx 24rpx;

  .chart-card,
  .ranking-card,
  .behavior-card {
    background: #ffffff;
    border-radius: 16rpx;
    padding: 32rpx;
    margin-bottom: 24rpx;

    .chart-header,
    .ranking-header,
    .behavior-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24rpx;

      .chart-title,
      .ranking-title,
      .behavior-title {
        font-size: 28rpx;
        font-weight: bold;
        color: #333333;
      }

      .chart-actions {
        display: flex;
        gap: 8rpx;

        .action-btn {
          padding: 8rpx 16rpx;
          background: #f5f5f5;
          border-radius: 16rpx;

          .btn-text {
            font-size: 24rpx;
            color: #666666;
          }
        }
      }

      .ranking-tabs {
        display: flex;
        gap: 16rpx;
        background: #f5f5f5;
        border-radius: 24rpx;
        padding: 4rpx;

        .ranking-tab {
          padding: 8rpx 16rpx;
          border-radius: 20rpx;

          &.active {
            background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);

            .tab-text {
              color: #ffffff;
            }
          }

          .tab-text {
            font-size: 24rpx;
            color: #666666;
          }
        }
      }
    }
  }

  .chart-container {
    .turnover-chart,
    .order-chart {
      width: 100%;
      height: 300rpx;
    }
  }

  .ranking-list {
    .ranking-item {
      display: flex;
      align-items: center;
      gap: 16rpx;
      padding: 16rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .rank-number {
        width: 48rpx;
        height: 48rpx;
        border-radius: 50%;
        background: #f0f0f0;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24rpx;
        font-weight: bold;
        color: #999999;

        &.top3 {
          background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
          color: #ffffff;
        }
      }

      .dish-image {
        width: 80rpx;
        height: 80rpx;
        border-radius: 8rpx;
      }

      .dish-image-placeholder {
        @include flex-center;
        background: #fff3ec;
        color: #FF6B35;
        font-size: 32rpx;
      }

      .dish-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 8rpx;

        .dish-name {
          font-size: 28rpx;
          color: #333333;
        }

        .dish-sales {
          font-size: 24rpx;
          color: #999999;
        }
      }

      .dish-amount {
        .amount-value {
          font-size: 28rpx;
          font-weight: bold;
          color: #ff6b6b;
        }
      }
    }
  }

  .behavior-metrics {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 24rpx;

    .behavior-item {
      display: flex;
      align-items: center;
      gap: 16rpx;
      padding: 20rpx;
      background: #f9f9f9;
      border-radius: 12rpx;

      .behavior-icon {
        font-size: 36rpx;
      }

      .behavior-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 8rpx;

        .behavior-label {
          font-size: 24rpx;
          color: #999999;
        }

        .behavior-value {
          font-size: 28rpx;
          font-weight: 500;
          color: #333333;
        }
      }
    }
  }
}

.date-picker-popup {
  background: #ffffff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 32rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32rpx;

    .popup-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333333;
    }

    .close-btn {
      width: 48rpx;
      height: 48rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36rpx;
      color: #999999;
    }
  }

  .popup-content {
    margin-bottom: 32rpx;

    .date-mode-selector {
      display: flex;
      gap: 16rpx;
      margin-bottom: 24rpx;

      .mode-item {
        flex: 1;
        padding: 12rpx;
        background: #f5f5f5;
        border-radius: 12rpx;
        text-align: center;

        &.active {
          background: #ff6b6b;

          .mode-text {
            color: #ffffff;
          }
        }

        .mode-text {
          font-size: 26rpx;
          color: #333333;
        }
      }
    }

    .date-picker-content {
      height: 400rpx;
    }
  }

  .popup-actions {
    display: flex;
    gap: 24rpx;

    .btn {
      flex: 1;
      height: 80rpx;
      border-radius: 40rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28rpx;
      font-weight: 500;
      border: none;

      &.btn-primary {
        background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
        color: #ffffff;
      }

      &.btn-outline {
        background: #ffffff;
        color: #ff6b6b;
        border: 2rpx solid #ff6b6b;
      }

      &:active {
        opacity: 0.8;
      }
    }
  }
}
</style>
