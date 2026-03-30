<template>
  <view class="health-report-container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-title">健康报告</view>
      <view class="nav-action" @click="generateNewReport">
        <text class="action-text">生成</text>
      </view>
    </view>

    <!-- 报告类型选择 -->
    <view class="type-selector">
      <scroll-view class="type-scroll" scroll-x>
        <view
          class="type-item"
          v-for="type in reportTypes"
          :key="type.value"
          :class="{ active: currentType === type.value }"
          @click="switchType(type.value)"
        >
          <text class="type-icon">{{ type.icon }}</text>
          <text class="type-text">{{ type.label }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 报告列表 -->
    <scroll-view
      class="report-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 空状态 -->
      <view class="empty-container" v-if="reportList.length === 0 && !loading">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无健康报告</text>
        <text class="empty-desc">生成您的第一份健康报告吧~</text>
        <button class="generate-btn" @click="generateNewReport">
          <text class="btn-icon">📊</text>
          <text class="btn-text">生成报告</text>
        </button>
      </view>

      <!-- 报告列表 -->
      <view class="report-list" v-else>
        <view
          class="report-item"
          v-for="report in reportList"
          :key="report.id"
          @click="viewReport(report)"
        >
          <!-- 报告封面 -->
          <view class="report-cover" :class="'type-' + report.type">
            <view class="cover-icon">{{ getReportIcon(report.type) }}</view>
            <view class="cover-info">
              <text class="cover-title">{{ getReportTitle(report.type) }}</text>
              <text class="cover-date">{{ formatDate(report.createTime) }}</text>
            </view>
            <view class="cover-score">
              <text class="score-value">{{ report.score }}</text>
              <text class="score-label">分</text>
            </view>
          </view>

          <!-- 报告摘要 -->
          <view class="report-summary">
            <view class="summary-row">
              <text class="summary-label">饮食评分</text>
              <view class="summary-stars">
                <text class="star" v-for="n in 5" :key="n">{{ n <= report.dietScore ? '⭐' : '☆' }}</text>
              </view>
            </view>

            <view class="summary-row">
              <text class="summary-label">营养均衡</text>
              <text class="summary-value" :class="getNutritionClass(report.nutritionScore)">
                {{ getNutritionText(report.nutritionScore) }}
              </text>
            </view>

            <view class="summary-row">
              <text class="summary-label">健康趋势</text>
              <text class="summary-value" :class="getTrendClass(report.trend)">
                {{ getTrendText(report.trend) }}
              </text>
            </view>
          </view>

          <!-- 报告操作 -->
          <view class="report-actions" @click.stop>
            <button class="action-btn detail-btn" @click="viewReport(report)">
              查看详情
            </button>
            <button class="action-btn share-btn" @click="shareReport(report)">
              分享
            </button>
            <button class="action-btn export-btn" @click="exportReport(report)">
              导出
            </button>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="reportList.length > 0 && hasMore">
        <uni-load-more
          status="loading"
          content-text="{
            contentdown: '上拉加载更多',
            contentrefresh: '加载中...',
            contentnomore: '没有更多了'
          }"
        ></uni-load-more>
      </view>
    </scroll-view>

    <!-- 报告详情弹窗 -->
    <view class="report-modal" v-if="showModal" @click="closeModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">健康报告详情</text>
          <view class="modal-actions">
            <text class="modal-action" @click="exportCurrentReport">导出</text>
            <text class="modal-close" @click="closeModal">×</text>
          </view>
        </view>

        <scroll-view class="modal-body" scroll-y v-if="currentReport">
          <!-- 报告头部 -->
          <view class="detail-header">
            <view class="score-circle">
              <text class="score-number">{{ currentReport.score }}</text>
              <text class="score-text">健康分</text>
            </view>

            <view class="header-info">
              <text class="report-name">{{ getReportTitle(currentReport.type) }}</text>
              <text class="report-time">生成时间：{{ formatFullTime(currentReport.createTime) }}</text>
            </view>
          </view>

          <!-- 饮食分析 -->
          <view class="detail-section">
            <view class="section-title">
              <text class="title-icon">🍽️</text>
              <text class="title-text">饮食分析</text>
            </view>

            <view class="analysis-list">
              <view class="analysis-item" v-for="(item, index) in currentReport.dietAnalysis" :key="index">
                <text class="analysis-label">{{ item.label }}</text>
                <view class="analysis-bar-wrapper">
                  <view class="analysis-bar">
                    <view class="analysis-fill" :style="{ width: item.percent + '%' }"></view>
                  </view>
                  <text class="analysis-percent">{{ item.percent }}%</text>
                </view>
                <text class="analysis-desc">{{ item.desc }}</text>
              </view>
            </view>
          </view>

          <!-- 营养评分 -->
          <view class="detail-section">
            <view class="section-title">
              <text class="title-icon">📊</text>
              <text class="title-text">营养评分</text>
            </view>

            <view class="nutrition-grid">
              <view class="nutrition-card" v-for="(item, index) in currentReport.nutritionScores" :key="index">
                <text class="nutrition-icon">{{ item.icon }}</text>
                <text class="nutrition-name">{{ item.name }}</text>
                <text class="nutrition-score">{{ item.score }}</text>
                <text class="nutrition-status" :class="'status-' + item.level">{{ getScoreLevel(item.level) }}</text>
              </view>
            </view>
          </view>

          <!-- 健康建议 -->
          <view class="detail-section">
            <view class="section-title">
              <text class="title-icon">💡</text>
              <text class="title-text">健康建议</text>
            </view>

            <view class="advice-list">
              <view class="advice-item" v-for="(advice, index) in currentReport.advices" :key="index">
                <view class="advice-icon" :class="'type-' + advice.type">
                  <text class="icon-text">{{ advice.icon }}</text>
                </view>
                <view class="advice-content">
                  <text class="advice-title">{{ advice.title }}</text>
                  <text class="advice-desc">{{ advice.desc }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 趋势图表 -->
          <view class="detail-section" v-if="currentReport.trendData && currentReport.trendData.length > 0">
            <view class="section-title">
              <text class="title-icon">📈</text>
              <text class="title-text">健康趋势</text>
            </view>

            <view class="trend-chart">
              <view class="chart-container">
                <view class="chart-y-axis">
                  <text class="axis-label">100</text>
                  <text class="axis-label">80</text>
                  <text class="axis-label">60</text>
                  <text class="axis-label">40</text>
                  <text class="axis-label">20</text>
                  <text class="axis-label">0</text>
                </view>

                <view class="chart-content">
                  <view class="chart-bars">
                    <view
                      class="chart-bar-item"
                      v-for="(item, index) in currentReport.trendData"
                      :key="index"
                    >
                      <view class="bar-wrapper">
                        <view
                          class="bar-fill"
                          :style="{ height: item.value + '%' }"
                          :class="'trend-' + item.trend"
                        ></view>
                      </view>
                      <text class="bar-label">{{ item.label }}</text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { healthReportApi } from '@/api'

// 报告类型
const reportTypes = [
  { label: '全部', value: 'all', icon: '📊' },
  { label: '周报', value: 'weekly', icon: '📅' },
  { label: '月报', value: 'monthly', icon: '📆' },
  { label: '季度', value: 'quarterly', icon: '📇' }
]

// 当前类型
const currentType = ref('all')

// 报告列表
const reportList = ref([])

// 加载状态
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)

// 弹窗状态
const showModal = ref(false)
const currentReport = ref(null)

// 组件挂载
onMounted(() => {
  loadReports()
})

// 过滤后的报告列表
const filteredReports = computed(() => {
  if (currentType.value === 'all') {
    return reportList.value
  }
  return reportList.value.filter(report => report.type === currentType.value)
})

/**
 * 加载报告列表
 */
const loadReports = async () => {
  loading.value = true

  try {
    // TODO: 调用真实API
    // const res = await healthReportApi.getList({
    //   type: currentType.value,
    //   page: 1,
    //   size: 20
    // })

    // 模拟数据
    const now = new Date()

    const mockData = [
      {
        id: 1,
        type: 'weekly',
        createTime: new Date(now - 1000 * 60 * 60 * 24 * 7).toISOString(),
        score: 85,
        dietScore: 4,
        nutritionScore: 78,
        trend: 'up',
        dietAnalysis: [
          { label: '蛋白质摄入', percent: 85, desc: '蛋白质摄入充足' },
          { label: '碳水化合物', percent: 70, desc: '碳水化合物适量' },
          { label: '脂肪摄入', percent: 65, desc: '脂肪摄入略高' },
          { label: '蔬菜水果', percent: 80, desc: '蔬果摄入良好' }
        ],
        nutritionScores: [
          { icon: '🥩', name: '蛋白质', score: 85, level: 'good' },
          { icon: '🍚', name: '碳水', score: 78, level: 'good' },
          { icon: '🥑', name: '脂肪', score: 72, level: 'normal' },
          { icon: '🥦', name: '纤维', score: 80, level: 'good' },
          { icon: '💧', name: '水分', score: 90, level: 'good' },
          { icon: '🧂', name: '维生素', score: 75, level: 'normal' }
        ],
        advices: [
          { type: 'good', icon: '✅', title: '整体良好', desc: '您的饮食结构较为均衡，继续保持' },
          { type: 'warning', icon: '⚠️', title: '脂肪偏高', desc: '建议适当减少油脂摄入，选择清淡烹饪方式' },
          { type: 'info', icon: '💡', title: '增加多样性', desc: '建议增加不同种类食物，获取更全面的营养' }
        ],
        trendData: [
          { label: '周一', value: 75, trend: 'normal' },
          { label: '周二', value: 78, trend: 'up' },
          { label: '周三', value: 82, trend: 'up' },
          { label: '周四', value: 79, trend: 'down' },
          { label: '周五', value: 85, trend: 'up' },
          { label: '周六', value: 88, trend: 'up' },
          { label: '周日', value: 85, trend: 'normal' }
        ]
      },
      {
        id: 2,
        type: 'monthly',
        createTime: new Date(now - 1000 * 60 * 60 * 24 * 30).toISOString(),
        score: 82,
        dietScore: 4,
        nutritionScore: 75,
        trend: 'up',
        dietAnalysis: [
          { label: '蛋白质摄入', percent: 82, desc: '蛋白质摄入充足' },
          { label: '碳水化合物', percent: 75, desc: '碳水化合物适中' },
          { label: '脂肪摄入', percent: 70, desc: '脂肪摄入适中' },
          { label: '蔬菜水果', percent: 78, desc: '蔬果摄入良好' }
        ],
        nutritionScores: [
          { icon: '🥩', name: '蛋白质', score: 82, level: 'good' },
          { icon: '🍚', name: '碳水', score: 75, level: 'normal' },
          { icon: '🥑', name: '脂肪', score: 70, level: 'normal' },
          { icon: '🥦', name: '纤维', score: 78, level: 'good' },
          { icon: '💧', name: '水分', score: 88, level: 'good' },
          { icon: '🧂', name: '维生素', score: 73, level: 'normal' }
        ],
        advices: [
          { type: 'good', icon: '✅', title: '整体良好', desc: '本月饮食状况良好，营养摄入均衡' },
          { type: 'info', icon: '💡', title: '保持习惯', desc: '建议继续保持当前的饮食习惯' },
          { type: 'warning', icon: '⚠️', title: '注意盐分', desc: '部分菜品盐分稍高，建议减少食盐摄入' }
        ],
        trendData: []
      },
      {
        id: 3,
        type: 'quarterly',
        createTime: new Date(now - 1000 * 60 * 60 * 24 * 90).toISOString(),
        score: 80,
        dietScore: 3,
        nutritionScore: 72,
        trend: 'stable',
        dietAnalysis: [
          { label: '蛋白质摄入', percent: 78, desc: '蛋白质摄入充足' },
          { label: '碳水化合物', percent: 72, desc: '碳水化合物适中' },
          { label: '脂肪摄入', percent: 68, desc: '脂肪摄入适中' },
          { label: '蔬菜水果', percent: 75, desc: '蔬果摄入良好' }
        ],
        nutritionScores: [
          { icon: '🥩', name: '蛋白质', score: 78, level: 'good' },
          { icon: '🍚', name: '碳水', score: 72, level: 'normal' },
          { icon: '🥑', name: '脂肪', score: 68, level: 'normal' },
          { icon: '🥦', name: '纤维', score: 75, level: 'normal' },
          { icon: '💧', name: '水分', score: 85, level: 'good' },
          { icon: '🧂', name: '维生素', score: 70, level: 'normal' }
        ],
        advices: [
          { type: 'info', icon: '💡', title: '稳定进步', desc: '饮食状况稳定，保持健康饮食习惯' },
          { type: 'warning', icon: '⚠️', title: '改进空间', desc: '仍有提升空间，建议增加蛋白质和蔬果摄入' }
        ],
        trendData: []
      }
    ]

    reportList.value = mockData
    hasMore.value = false
  } catch (error) {
    console.error('加载报告失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 下拉刷新
 */
const onRefresh = async () => {
  refreshing.value = true
  await loadReports()
  refreshing.value = false
}

/**
 * 切换类型
 */
const switchType = (type) => {
  currentType.value = type
}

/**
 * 生成新报告
 */
const generateNewReport = () => {
  uni.showActionSheet({
    itemList: ['生成周报', '生成月报', '生成季度报告'],
    success: (res) => {
      const typeMap = ['weekly', 'monthly', 'quarterly']
      const type = typeMap[res.tapIndex]

      uni.showLoading({ title: '生成中...' })

      setTimeout(() => {
        uni.hideLoading()

        uni.showToast({
          title: '报告生成成功',
          icon: 'success'
        })

        // 刷新列表
        loadReports()
      }, 2000)
    }
  })
}

/**
 * 查看报告
 */
const viewReport = (report) => {
  currentReport.value = report
  showModal.value = true
}

/**
 * 关闭弹窗
 */
const closeModal = () => {
  showModal.value = false
  currentReport.value = null
}

/**
 * 分享报告
 */
const shareReport = (report) => {
  uni.showActionSheet({
    itemList: ['分享给好友', '生成海报', '保存图片'],
    success: (res) => {
      uni.showToast({
        title: '分享功能开发中...',
        icon: 'none'
      })
    }
  })
}

/**
 * 导出报告
 */
const exportReport = (report) => {
  uni.showLoading({ title: '导出中...' })

  setTimeout(() => {
    uni.hideLoading()

    uni.showToast({
      title: '已导出为图片',
      icon: 'success'
    })
  }, 1500)
}

/**
 * 导出当前报告
 */
const exportCurrentReport = () => {
  if (currentReport.value) {
    exportReport(currentReport.value)
  }
}

/**
 * 获取报告图标
 */
const getReportIcon = (type) => {
  const iconMap = {
    'weekly': '📅',
    'monthly': '📆',
    'quarterly': '📇'
  }
  return iconMap[type] || '📊'
}

/**
 * 获取报告标题
 */
const getReportTitle = (type) => {
  const titleMap = {
    'weekly': '周健康报告',
    'monthly': '月健康报告',
    'quarterly': '季度健康报告'
  }
  return titleMap[type] || '健康报告'
}

/**
 * 获取营养文本
 */
const getNutritionText = (score) => {
  if (score >= 80) return '优秀'
  if (score >= 60) return '良好'
  return '需改善'
}

/**
 * 获取营养样式
 */
const getNutritionClass = (score) => {
  if (score >= 80) return 'value-good'
  if (score >= 60) return 'value-normal'
  return 'value-warning'
}

/**
 * 获取趋势文本
 */
const getTrendText = (trend) => {
  const trendMap = {
    'up': '↗️ 上升',
    'down': '↘️ 下降',
    'stable': '→ 稳定'
  }
  return trendMap[trend] || '-'
}

/**
 * 获取趋势样式
 */
const getTrendClass = (trend) => {
  const classMap = {
    'up': 'trend-up',
    'down': 'trend-down',
    'stable': 'trend-stable'
  }
  return classMap[trend] || ''
}

/**
 * 获取评分等级
 */
const getScoreLevel = (level) => {
  const levelMap = {
    'good': '优秀',
    'normal': '良好',
    'warning': '需改善'
  }
  return levelMap[level] || '未知'
}

/**
 * 格式化日期
 */
const formatDate = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

/**
 * 格式化完整时间
 */
const formatFullTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.health-report-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  display: flex;
  flex-direction: column;
}

/* 导航栏 */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  background-color: $bg-color-white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-md;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.nav-back {
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
}

.back-icon {
  font-size: 48rpx;
  color: $text-color-primary;
  font-weight: bold;
}

.nav-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.nav-action {
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
}

.action-text {
  font-size: $font-size-base;
  color: $primary-color;
}

/* 类型选择器 */
.type-selector {
  position: fixed;
  top: 88rpx;
  left: 0;
  right: 0;
  background-color: $bg-color-white;
  z-index: 99;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.type-scroll {
  white-space: nowrap;
  padding: $spacing-md;
}

.type-item {
  display: inline-block;
  padding: $spacing-sm $spacing-lg;
  margin: 0 $spacing-xs;
  border-radius: $border-radius-round;
  background-color: $bg-color-base;
  transition: all 0.3s;

  &.active {
    background-color: $primary-color;

    .type-text {
      color: #fff;
      font-weight: $font-weight-bold;
    }
  }
}

.type-icon {
  font-size: 28rpx;
  margin-right: 4rpx;
}

.type-text {
  font-size: $font-size-base;
  color: $text-color-primary;
}

/* 报告列表 */
.report-scroll {
  flex: 1;
  margin-top: 168rpx;
  padding: $spacing-md;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 空状态 */
.empty-container {
  @include flex-center-column;
  padding: 200rpx $spacing-xl;
  text-align: center;
}

.empty-icon {
  font-size: 160rpx;
  margin-bottom: $spacing-lg;
}

.empty-text {
  font-size: $font-size-xl;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-sm;
}

.empty-desc {
  font-size: $font-size-base;
  color: $text-color-secondary;
  margin-bottom: $spacing-xl;
}

.generate-btn {
  padding: $spacing-md $spacing-xl;
  background-color: $primary-color;
  color: #fff;
  border-radius: $border-radius-round;
  @include flex-center;
  gap: $spacing-sm;
  border: none;

  &::after {
    border: none;
  }
}

.btn-icon {
  font-size: 32rpx;
}

.btn-text {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
}

/* 报告列表 */
.report-list {
  .report-item {
    background-color: $bg-color-white;
    border-radius: $border-radius-lg;
    padding: $spacing-lg;
    margin-bottom: $spacing-md;
    box-shadow: $box-shadow-sm;
  }
}

/* 报告封面 */
.report-cover {
  display: flex;
  align-items: center;
  margin-bottom: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  position: relative;
  overflow: hidden;

  &.type-weekly {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  &.type-monthly {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }

  &.type-quarterly {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }
}

.cover-icon {
  font-size: 64rpx;
  margin-right: $spacing-md;
}

.cover-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.cover-title {
  font-size: $font-size-lg;
  color: #fff;
  font-weight: $font-weight-bold;
}

.cover-date {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.8);
}

.cover-score {
  @include flex-center-column;
  gap: 4rpx;
}

.score-value {
  font-size: 48rpx;
  color: #fff;
  font-weight: $font-weight-bold;
  line-height: 1;
}

.score-label {
  font-size: $font-size-xs;
  color: rgba(255, 255, 255, 0.8);
}

/* 报告摘要 */
.report-summary {
  margin-bottom: $spacing-md;
}

.summary-row {
  @include flex-between;
  align-items: center;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $border-color-lighter;

  &:last-child {
    border-bottom: none;
  }
}

.summary-label {
  font-size: $font-size-base;
  color: $text-color-primary;
}

.summary-stars {
  @include flex-center;
}

.star {
  font-size: 28rpx;
  letter-spacing: 4rpx;
}

.summary-value {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;

  &.value-good {
    color: $success-color;
  }

  &.value-normal {
    color: $primary-color;
  }

  &.value-warning {
    color: $warning-color;
  }

  &.trend-up {
    color: $success-color;
  }

  &.trend-down {
    color: $danger-color;
  }

  &.trend-stable {
    color: $text-color-secondary;
  }
}

/* 报告操作 */
.report-actions {
  @include flex-center;
  gap: $spacing-sm;
}

.action-btn {
  flex: 1;
  padding: $spacing-sm;
  border-radius: $border-radius-base;
  font-size: $font-size-sm;
  border: 1rpx solid $border-color-base;

  &::after {
    border: none;
  }
}

.detail-btn {
  background-color: $primary-color;
  color: #fff;
  border-color: $primary-color;
}

.share-btn {
  background-color: #fff;
  color: $primary-color;
}

.export-btn {
  background-color: #fff;
  color: $text-color-primary;
}

/* 详情弹窗 */
.report-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  z-index: 1000;
  @include flex-center;
}

.modal-content {
  width: 640rpx;
  height: 80vh;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  @include flex-between;
  padding: $spacing-lg;
  border-bottom: 1rpx solid $border-color-lighter;
}

.modal-title {
  font-size: $font-size-lg;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.modal-actions {
  @include flex-center;
  gap: $spacing-md;
}

.modal-action {
  font-size: $font-size-base;
  color: $primary-color;
}

.modal-close {
  font-size: 48rpx;
  color: $text-color-secondary;
  line-height: 1;
  padding: 0 $spacing-xs;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-lg;
}

/* 详情头部 */
.detail-header {
  @include flex-center-column;
  gap: $spacing-md;
  padding: $spacing-xl;
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  border-radius: $border-radius-lg;
  margin-bottom: $spacing-lg;
}

.score-circle {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background-color: #fff;
  @include flex-center-column;
  justify-content: center;
  margin-bottom: $spacing-sm;
}

.score-number {
  font-size: 64rpx;
  color: $primary-color;
  font-weight: $font-weight-bold;
  line-height: 1;
}

.score-text {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.header-info {
  text-align: center;
}

.report-name {
  display: block;
  font-size: $font-size-xl;
  color: #fff;
  font-weight: $font-weight-bold;
  margin-bottom: 4rpx;
}

.report-time {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.8);
}

/* 详情区块 */
.detail-section {
  margin-bottom: $spacing-lg;
}

.section-title {
  @include flex-center;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}

.title-icon {
  font-size: 32rpx;
}

.title-text {
  font-size: $font-size-lg;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

/* 分析列表 */
.analysis-list {
  .analysis-item {
    margin-bottom: $spacing-md;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.analysis-label {
  display: block;
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-xs;
}

.analysis-bar-wrapper {
  @include flex-center;
  gap: $spacing-sm;
  margin-bottom: 4rpx;
}

.analysis-bar {
  flex: 1;
  height: 16rpx;
  background-color: $bg-color-base;
  border-radius: 8rpx;
  overflow: hidden;
}

.analysis-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF6B35, #FF8C61);
  border-radius: 8rpx;
}

.analysis-percent {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  min-width: 50rpx;
  text-align: right;
}

.analysis-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 营养评分 */
.nutrition-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-sm;
}

.nutrition-card {
  @include flex-center-column;
  gap: 4rpx;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
}

.nutrition-icon {
  font-size: 32rpx;
}

.nutrition-name {
  font-size: $font-size-xs;
  color: $text-color-primary;
}

.nutrition-score {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.nutrition-status {
  font-size: $font-size-xs;

  &.status-good {
    color: $success-color;
  }

  &.status-normal {
    color: $primary-color;
  }

  &.status-warning {
    color: $warning-color;
  }
}

/* 建议列表 */
.advice-list {
  .advice-item {
    display: flex;
    align-items: flex-start;
    gap: $spacing-md;
    padding: $spacing-md;
    border-radius: $border-radius-base;
    margin-bottom: $spacing-sm;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.advice-icon {
  width: 64rpx;
  height: 64rpx;
  @include flex-center;
  border-radius: 50%;
  flex-shrink: 0;

  &.type-good {
    background-color: rgba($success-color, 0.1);
  }

  &.type-warning {
    background-color: rgba($warning-color, 0.1);
  }

  &.type-info {
    background-color: rgba($primary-color, 0.1);
  }
}

.icon-text {
  font-size: 32rpx;
}

.advice-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.advice-title {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.advice-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  line-height: 1.4;
}

/* 趋势图表 */
.trend-chart {
  margin-top: $spacing-md;
}

.chart-container {
  display: flex;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
}

.chart-y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding-right: $spacing-sm;
  margin-right: $spacing-sm;
  border-right: 1rpx solid $border-color-lighter;
}

.axis-label {
  font-size: $font-size-xs;
  color: $text-color-secondary;
  height: 40rpx;
  line-height: 40rpx;
}

.chart-content {
  flex: 1;
}

.chart-bars {
  display: flex;
  justify-content: space-between;
  height: 240rpx;
}

.chart-bar-item {
  flex: 1;
  @include flex-center-column;
  gap: 4rpx;
}

.bar-wrapper {
  flex: 1;
  @include flex-center;
  align-items: flex-end;
  height: 200rpx;
  padding: 0 4rpx;
}

.bar-fill {
  width: 32rpx;
  border-radius: 16rpx 16rpx 0 0;
  background-color: $primary-color;

  &.trend-up {
    background-color: $success-color;
  }

  &.trend-down {
    background-color: $danger-color;
  }

  &.trend-normal {
    background-color: $text-color-secondary;
  }
}

.bar-label {
  font-size: $font-size-xs;
  color: $text-color-secondary;
  text-align: center;
}
</style>
