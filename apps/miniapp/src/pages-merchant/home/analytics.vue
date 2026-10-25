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

    <!-- 关键指标卡片 -->
    <view class="metrics-cards">
      <view class="metric-card" v-for="(metric, index) in keyMetrics" :key="index">
        <view class="metric-header">
          <text class="metric-title">{{ metric.title }}</text>
          <view class="metric-trend" :class="{ up: metric.trend > 0, down: metric.trend < 0 }">
            <text class="trend-icon">{{ metric.trend > 0 ? '↑' : '↓' }}</text>
            <text class="trend-value">{{ Math.abs(metric.trend) }}%</text>
          </view>
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
          <text class="chart-title">营业额趋势</text>
          <view class="chart-actions">
            <view class="action-btn" @click="switchChartType('turnover')">
              <text class="btn-text">日</text>
            </view>
            <view class="action-btn" @click="switchChartType('week')">
              <text class="btn-text">周</text>
            </view>
            <view class="action-btn" @click="switchChartType('month')">
              <text class="btn-text">月</text>
            </view>
          </view>
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
            <image class="dish-image" :src="item.image" mode="aspectFill"></image>
            <view class="dish-info">
              <text class="dish-name">{{ item.name }}</text>
              <text class="dish-sales">{{ item.sales }}{{ rankingType === 'quantity' ? '份' : '元' }}</text>
            </view>
            <view class="dish-amount">
              <text class="amount-value">¥{{ item.amount }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 用户行为分析 -->
      <view class="behavior-card">
        <view class="behavior-header">
          <text class="behavior-title">用户行为分析</text>
        </view>
        <view class="behavior-metrics">
          <view class="behavior-item">
            <view class="behavior-icon">🕐</view>
            <view class="behavior-info">
              <text class="behavior-label">下单高峰期</text>
              <text class="behavior-value">{{ peakHour }}:00</text>
            </view>
          </view>
          <view class="behavior-item">
            <view class="behavior-icon">📍</view>
            <view class="behavior-info">
              <text class="behavior-label">主要配送区域</text>
              <text class="behavior-value">{{ mainArea }}</text>
            </view>
          </view>
          <view class="behavior-item">
            <view class="behavior-icon">⭐</view>
            <view class="behavior-info">
              <text class="behavior-label">平均评分</text>
              <text class="behavior-value">{{ avgRating }}分</text>
            </view>
          </view>
          <view class="behavior-item">
            <view class="behavior-icon">🔄</view>
            <view class="behavior-info">
              <text class="behavior-label">复购率</text>
              <text class="behavior-value">{{ repurchaseRate }}%</text>
            </view>
          </view>
        </view>
      </view>
    </view>

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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import uCharts from '@/utils/u-charts.js'

// 日期选择器
const datePopup = ref(null)
const dateMode = ref('range')
const datePickerValue = ref([2026, 3, 18])
const years = ref([])
const months = ref([])
const days = ref([])

// 时间选择
const currentTimeTab = ref(0)
const timeTabs = ref(['今天', '昨天', '本周', '本月', '自定义'])
const dateRange = ref('2026-03-18')

// 图表类型
const chartType = ref('turnover')

// 排行类型
const rankingType = ref('quantity')

// 关键指标
const keyMetrics = ref([
  { title: '总营业额', value: '15,680', unit: '元', trend: 12.5 },
  { title: '订单数', value: '328', unit: '单', trend: 8.3 },
  { title: '客单价', value: '47.8', unit: '元', trend: -2.4 },
  { title: '完成率', value: '96.5', unit: '%', trend: 5.2 }
])

// 销售排行
const rankingList = ref([
  { name: '招牌红烧肉', image: 'https://via.placeholder.com/100x100', sales: 156, amount: 4680 },
  { name: '宫保鸡丁', image: 'https://via.placeholder.com/100x100', sales: 128, amount: 3840 },
  { name: '鱼香肉丝', image: 'https://via.placeholder.com/100x100', sales: 112, amount: 3360 },
  { name: '麻婆豆腐', image: 'https://via.placeholder.com/100x100', sales: 98, amount: 2940 },
  { name: '糖醋排骨', image: 'https://via.placeholder.com/100x100', sales: 87, amount: 2610 }
])

// 用户行为数据
const peakHour = ref(11)
const mainArea = ref('天河区')
const avgRating = ref(4.8)
const repurchaseRate = ref(68)

// 初始化日期数据
onMounted(() => {
  const now = new Date()
  const currentYear = now.getFullYear()
  const currentMonth = now.getMonth() + 1
  const daysInMonth = new Date(currentYear, currentMonth, 0).getDate()

  years.value = Array.from({ length: 5 }, (_, i) => currentYear - i)
  months.value = Array.from({ length: 12 }, (_, i) => i + 1)
  days.value = Array.from({ length: daysInMonth }, (_, i) => i + 1)

  // 初始化图表
  initCharts()
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
const loadAnalyticsData = () => {
  // 模拟加载数据
  // 实际应该调用API获取数据
}

// 切换图表类型
const switchChartType = (type) => {
  chartType.value = type
  updateCharts()
}

// 切换排行类型
const switchRankingType = (type) => {
  rankingType.value = type
  // 重新排序
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

// 绘制营业额趋势图
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
    categories: ['10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00'],
    series: [{
      name: '营业额',
      data: [1200, 1800, 2500, 3200, 2800, 3500, 4200, 3800],
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

// 绘制订单分布图
const drawOrderChart = () => {
  const ctx = uni.createCanvasContext('orderChart')
  const systemInfo = uni.getSystemInfoSync()
  const width = systemInfo.windowWidth - 64
  const height = 300

  const chart = new uCharts({
    type: 'pie',
    context: ctx,
    width: width,
    height: height,
    series: [{
      data: [
        { name: '待付款', value: 12, color: '#faad14' },
        { name: '待接单', value: 8, color: '#1677ff' },
        { name: '制作中', value: 25, color: '#eb2f96' },
        { name: '配送中', value: 18, color: '#52c41a' },
        { name: '已完成', value: 37, color: '#13c2c2' }
      ]
    }],
    legend: {
      position: 'bottom'
    }
  })

  chart.init()
}

// 导出报表
const exportReport = () => {
  uni.showActionSheet({
    itemList: ['导出Excel', '导出PDF', '发送到邮箱'],
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.showToast({
          title: '正在导出Excel...',
          icon: 'loading'
        })
      } else if (res.tapIndex === 1) {
        uni.showToast({
          title: '正在导出PDF...',
          icon: 'loading'
        })
      } else if (res.tapIndex === 2) {
        uni.showToast({
          title: '正在发送到邮箱...',
          icon: 'loading'
        })
      }
    }
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.analytics-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
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
