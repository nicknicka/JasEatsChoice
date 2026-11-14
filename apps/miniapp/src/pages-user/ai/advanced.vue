<template>
  <view class="ai-advanced-container">
    <!-- 顶部导航 -->
    <view class="header">
      <view class="header-content">
        <text class="title">AI 智能分析</text>
        <text class="subtitle">基于您的饮食习惯深度分析</text>
      </view>
    </view>

    <!-- 分析选项卡 -->
    <view class="tabs">
      <view
        class="tab-item"
        :class="{ active: activeTab === 'nutrition' }"
        @tap="switchTab('nutrition')"
      >
        <text>营养分析</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'recommend' }"
        @tap="switchTab('recommend')"
      >
        <text>智能推荐</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'report' }"
        @tap="switchTab('report')"
      >
        <text>健康报告</text>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <!-- 加载 / 错误 / 空态：无服务端数据时不展示任何写死数值 -->
      <view class="ai-state" v-if="loading">
        <text class="ai-state-text">正在分析中...</text>
      </view>
      <view class="ai-state" v-else-if="loadError">
        <text class="ai-state-text">{{ loadError }}</text>
        <button class="ai-state-btn" @tap="loadNutritionData">重试</button>
      </view>
      <view class="ai-state" v-else-if="!hasData">
        <text class="ai-state-text">暂无营养分析数据</text>
        <text class="ai-state-desc">记录几餐饮食后，这里会生成你的专属分析</text>
        <button class="ai-state-btn" @tap="loadNutritionData">刷新</button>
      </view>

      <!-- 营养分析 -->
      <view v-else-if="activeTab === 'nutrition'" class="nutrition-analysis">
        <!-- 今日摄入 -->
        <view class="section-card">
          <view class="section-header">
            <text class="section-title">今日营养摄入</text>
            <text class="date">{{ todayDate }}</text>
          </view>

          <!-- 营养素环形图 -->
          <view class="nutrition-chart">
            <canvas canvas-id="nutritionCanvas" id="nutritionCanvas" class="chart-canvas"></canvas>
            <view class="chart-center">
              <text class="total-calories">{{ todayCalories }}</text>
              <text class="unit">kcal</text>
            </view>
          </view>

          <!-- 营养素详情 -->
          <view class="nutrition-details">
            <view class="nutrition-item">
              <view class="nutrition-header">
                <view class="color-dot protein"></view>
                <text class="nutrition-name">蛋白质</text>
                <text class="nutrition-value">{{ nutrition.protein }}g</text>
              </view>
              <view class="progress-bar">
                <view class="progress-fill protein" :style="{ width: nutrition.proteinPercent + '%' }"></view>
              </view>
              <text class="nutrition-target">目标: {{ nutrition.proteinTarget }}g</text>
            </view>

            <view class="nutrition-item">
              <view class="nutrition-header">
                <view class="color-dot carbs"></view>
                <text class="nutrition-name">碳水化合物</text>
                <text class="nutrition-value">{{ nutrition.carbs }}g</text>
              </view>
              <view class="progress-bar">
                <view class="progress-fill carbs" :style="{ width: nutrition.carbsPercent + '%' }"></view>
              </view>
              <text class="nutrition-target">目标: {{ nutrition.carbsTarget }}g</text>
            </view>

            <view class="nutrition-item">
              <view class="nutrition-header">
                <view class="color-dot fat"></view>
                <text class="nutrition-name">脂肪</text>
                <text class="nutrition-value">{{ nutrition.fat }}g</text>
              </view>
              <view class="progress-bar">
                <view class="progress-fill fat" :style="{ width: nutrition.fatPercent + '%' }"></view>
              </view>
              <text class="nutrition-target">目标: {{ nutrition.fatTarget }}g</text>
            </view>

            <view class="nutrition-item">
              <view class="nutrition-header">
                <view class="color-dot fiber"></view>
                <text class="nutrition-name">膳食纤维</text>
                <text class="nutrition-value">{{ nutrition.fiber }}g</text>
              </view>
              <view class="progress-bar">
                <view class="progress-fill fiber" :style="{ width: nutrition.fiberPercent + '%' }"></view>
              </view>
              <text class="nutrition-target">目标: {{ nutrition.fiberTarget }}g</text>
            </view>
          </view>
        </view>

        <!-- 微量元素 -->
        <view class="section-card">
          <view class="section-header">
            <text class="section-title">维生素与矿物质</text>
          </view>
          <view class="micronutrients">
            <view class="micro-item" v-for="item in micronutrients" :key="item.name">
              <view class="micro-icon">
                <text class="icon">{{ item.icon }}</text>
              </view>
              <view class="micro-info">
                <text class="micro-name">{{ item.name }}</text>
                <text class="micro-value">{{ item.value }}{{ item.unit }}</text>
              </view>
              <view class="micro-status" :class="item.status">
                <text>{{ item.statusText }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- AI 建议 -->
        <view class="section-card">
          <view class="section-header">
            <text class="section-title">AI 饮食建议</text>
          </view>
          <view class="ai-suggestions">
            <view class="suggestion-item" v-for="(suggestion, index) in suggestions" :key="index">
              <view class="suggestion-icon" :class="suggestion.type">
                <uni-icons :type="suggestion.icon" size="20" color="#fff"></uni-icons>
              </view>
              <view class="suggestion-content">
                <text class="suggestion-title">{{ suggestion.title }}</text>
                <text class="suggestion-desc">{{ suggestion.desc }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 智能推荐 -->
      <view v-else-if="activeTab === 'recommend'" class="smart-recommend">
        <!-- 推荐理由 -->
        <view class="section-card">
          <view class="section-header">
            <text class="section-title">今日推荐理由</text>
          </view>
          <view class="recommend-reason">
            <view class="reason-item">
              <uni-icons type="calendar" size="22" color="#FF6B35"></uni-icons>
              <view class="reason-content">
                <text class="reason-title">天气因素</text>
                <text class="reason-desc">今日气温较低，推荐温热汤品和富含维生素C的菜品增强免疫力</text>
              </view>
            </view>
            <view class="reason-item">
              <uni-icons type="person" size="22" color="#FF6B35"></uni-icons>
              <view class="reason-content">
                <text class="reason-title">您的目标</text>
                <text class="reason-desc">根据您的减脂目标，推荐高蛋白低脂的菜品</text>
              </view>
            </view>
            <view class="reason-item">
              <uni-icons type="clock" size="22" color="#FF6B35"></uni-icons>
              <view class="reason-content">
                <text class="reason-title">时间因素</text>
                <text class="reason-desc">午餐时段，建议摄入适量碳水保持下午精力</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 推荐菜品 -->
        <view class="section-card">
          <view class="section-header">
            <text class="section-title">智能推荐菜品</text>
            <text class="more" @tap="viewMoreRecommend">查看更多 ›</text>
          </view>
          <view class="recommend-dishes">
            <view class="dish-card" v-for="dish in recommendDishes" :key="dish.id" @tap="viewDish(dish.id)">
              <image class="dish-image" :src="dish.image" mode="aspectFill"></image>
              <view class="dish-info">
                <text class="dish-name">{{ dish.name }}</text>
                <view class="dish-tags">
                  <text class="tag" v-for="tag in dish.tags" :key="tag">{{ tag }}</text>
                </view>
                <view class="dish-nutrition">
                  <text class="nutrition-item">{{ dish.calories }}kcal</text>
                  <text class="nutrition-item">蛋白质{{ dish.protein }}g</text>
                </view>
              </view>
              <view class="match-score">
                <text class="score-label">匹配度</text>
                <text class="score-value">{{ dish.matchScore }}%</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 营养搭配 -->
        <view class="section-card">
          <view class="section-header">
            <text class="section-title">营养搭配建议</text>
          </view>
          <view class="meal-combo">
            <view class="combo-item" v-for="combo in mealCombos" :key="combo.type">
              <view class="combo-header">
                <text class="combo-type">{{ combo.type }}</text>
                <text class="combo-calories">{{ combo.totalCalories }}kcal</text>
              </view>
              <view class="combo-dishes">
                <view class="combo-dish" v-for="dish in combo.dishes" :key="dish">
                  <text class="dish-text">{{ dish }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 健康报告 -->
      <view v-else-if="activeTab === 'report'" class="health-report">
        <!-- 健康评分 -->
        <view class="section-card">
          <view class="section-header">
            <text class="section-title">本周健康评分</text>
          </view>
          <view class="health-score">
            <view class="score-circle">
              <text class="score-number">{{ healthScore }}</text>
              <text class="score-label">分</text>
            </view>
            <view class="score-details">
              <view class="score-item">
                <text class="item-label">营养均衡</text>
                <text class="item-value">{{ scoreDetails.balance }}</text>
              </view>
              <view class="score-item">
                <text class="item-label">热量控制</text>
                <text class="item-value">{{ scoreDetails.calories }}</text>
              </view>
              <view class="score-item">
                <text class="item-label">饮食规律</text>
                <text class="item-value">{{ scoreDetails.regularity }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 饮食趋势 -->
        <view class="section-card">
          <view class="section-header">
            <text class="section-title">近7天饮食趋势</text>
          </view>
          <view class="trend-chart">
            <canvas canvas-id="trendCanvas" id="trendCanvas" class="chart-canvas"></canvas>
          </view>
        </view>

        <!-- 饮食习惯分析 -->
        <view class="section-card">
          <view class="section-header">
            <text class="section-title">饮食习惯分析</text>
          </view>
          <view class="habits-analysis">
            <view class="habit-item">
              <text class="habit-label">平均每日热量</text>
              <text class="habit-value">{{ habits.avgCalories }}kcal</text>
            </view>
            <view class="habit-item">
              <text class="habit-label">蛋白质摄入占比</text>
              <text class="habit-value">{{ habits.proteinRatio }}%</text>
            </view>
            <view class="habit-item">
              <text class="habit-label">蔬菜摄入频率</text>
              <text class="habit-value">{{ habits.veggieFreq }}次/周</text>
            </view>
            <view class="habit-item">
              <text class="habit-label">外食比例</text>
              <text class="habit-value">{{ habits.diningOut }}%</text>
            </view>
          </view>
        </view>

        <!-- 改进建议 -->
        <view class="section-card">
          <view class="section-header">
            <text class="section-title">改进建议</text>
          </view>
          <view class="improvements">
            <view class="improvement-item" v-for="(item, index) in improvements" :key="index">
              <view class="improvement-header">
                <view class="priority-badge" :class="'priority-' + item.priority">
                  <text>{{ item.priorityText }}</text>
                </view>
                <text class="improvement-title">{{ item.title }}</text>
              </view>
              <text class="improvement-desc">{{ item.desc }}</text>
              <view class="improvement-actions">
                <button class="action-btn" @tap="applyImprovement(item)">采纳建议</button>
                <button class="action-btn secondary" @tap="viewDetail(item)">查看详情</button>
              </view>
            </view>
          </view>
        </view>

        <!-- 导出报告 -->
        <view class="section-card">
          <view class="section-header">
            <text class="section-title">报告管理</text>
          </view>
          <view class="report-actions">
            <button class="report-btn" @tap="exportReport('pdf')">
              <uni-icons type="download" size="20" color="#FF6B35"></uni-icons>
              <text>导出 PDF</text>
            </button>
            <button class="report-btn" @tap="exportReport('image')">
              <uni-icons type="image" size="20" color="#FF6B35"></uni-icons>
              <text>保存为图片</text>
            </button>
            <button class="report-btn" @tap="shareReport">
              <uni-icons type="redo" size="20" color="#FF6B35"></uni-icons>
              <text>分享报告</text>
            </button>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <button class="consult-btn" @tap="consultAI">
        <uni-icons type="chatbubble" size="20" color="#fff"></uni-icons>
        <text>咨询 AI 助手</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useDebounce } from '@/utils/performance'
import { useUserStore } from '@/store'
import { aiApi } from '@/api'
import { navigate, paths, toDishDetail } from '@/utils/router'

// 用户信息store
const userStore = useUserStore()

// 加载状态
const loading = ref(false)

// 选项卡
const activeTab = ref('nutrition')

// 今日日期
const todayDate = computed(() => {
  const now = new Date()
  return `${now.getMonth() + 1}月${now.getDate()}日`
})

// 加载与错误状态
const loadError = ref('')

// 今日热量
const todayCalories = ref('0')

// 是否已拿到任何服务端分析结果（为空则走空态，不展示写死数值）
const hasData = computed(() => {
  return !!nutrition.value && (Number(nutrition.value.protein) > 0 ||
    Number(nutrition.value.carbs) > 0 ||
    Number(nutrition.value.fat) > 0)
})

// 营养素数据（初始为 0，仅由接口填充）
const nutrition = ref({
  protein: 0,
  proteinTarget: 100,
  proteinPercent: 0,
  carbs: 0,
  carbsTarget: 280,
  carbsPercent: 0,
  fat: 0,
  fatTarget: 70,
  fatPercent: 0,
  fiber: 0,
  fiberTarget: 25,
  fiberPercent: 0
})

// 微量元素（仅由接口填充）
const micronutrients = ref([])

// AI 建议（仅由接口填充）
const suggestions = ref([])

// 推荐菜品（仅由接口填充）
const recommendDishes = ref([])

// 营养搭配（仅由接口填充）
const mealCombos = ref([])

// 健康评分（仅由接口填充）
const healthScore = ref(0)
const scoreDetails = ref({
  balance: 0,
  calories: 0,
  regularity: 0
})

// 饮食习惯（仅由接口填充）
const habits = ref({
  avgCalories: 0,
  proteinRatio: 0,
  veggieFreq: 0,
  diningOut: 0
})

// 近7天饮食趋势（仅由接口填充；为空时不画曲线）
const trendData = ref({
  labels: [],
  values: []
})

// 改进建议（仅由接口填充）
const improvements = ref([])

// 切换选项卡
const switchTab = (tab) => {
  activeTab.value = tab
  nextTick(() => {
    if (tab === 'nutrition') {
      drawNutritionChart()
    } else if (tab === 'report') {
      drawTrendChart()
    }
  })
}

// 绘制营养环形图
const drawNutritionChart = () => {
  const ctx = uni.createCanvasContext('nutritionCanvas')
  const centerX = 100
  const centerY = 100
  const radius = 80
  const lineWidth = 15

  const data = [
    { value: nutrition.value.proteinPercent, color: '#4CAF50' },
    { value: nutrition.value.carbsPercent, color: '#2196F3' },
    { value: nutrition.value.fatPercent, color: '#FFC107' },
    { value: nutrition.value.fiberPercent, color: '#9C27B0' }
  ]

  let startAngle = -90

  data.forEach((item, index) => {
    const endAngle = startAngle + (item.value / 100) * 360

    ctx.beginPath()
    ctx.arc(centerX, centerY, radius, startAngle * Math.PI / 180, endAngle * Math.PI / 180)
    ctx.setLineWidth(lineWidth)
    ctx.setStrokeStyle(item.color)
    ctx.stroke()

    startAngle = endAngle
  })

  ctx.draw()
}

// 绘制趋势图
const drawTrendChart = () => {
  const ctx = uni.createCanvasContext('trendCanvas')
  const width = 650
  const height = 200
  const padding = 40

  // 趋势点位来自服务端；无数据时不画任何曲线
  const data = trendData.value.values

  if (data.length < 2) {
    ctx.setFillStyle('#999999')
    ctx.setFontSize(14)
    ctx.fillText('暂无趋势数据', width / 2 - 50, height / 2)
    ctx.draw()
    return
  }

  const maxValue = Math.max(...data)
  const minValue = Math.min(...data)
  const range = maxValue - minValue || 1

  // 绘制网格线
  ctx.setStrokeStyle('#E0E0E0')
  ctx.setLineWidth(1)
  for (let i = 0; i <= 4; i++) {
    const y = padding + (height - 2 * padding) * i / 4
    ctx.beginPath()
    ctx.moveTo(padding, y)
    ctx.lineTo(width - padding, y)
    ctx.stroke()
  }

  // 绘制折线
  ctx.setStrokeStyle('#FF6B35')
  ctx.setLineWidth(3)
  ctx.beginPath()

  data.forEach((value, index) => {
    const x = padding + (width - 2 * padding) * index / (data.length - 1)
    const y = height - padding - ((value - minValue) / range) * (height - 2 * padding)

    if (index === 0) {
      ctx.moveTo(x, y)
    } else {
      ctx.lineTo(x, y)
    }
  })

  ctx.stroke()

  // 绘制数据点
  data.forEach((value, index) => {
    const x = padding + (width - 2 * padding) * index / (data.length - 1)
    const y = height - padding - ((value - minValue) / range) * (height - 2 * padding)

    ctx.setFillStyle('#FF6B35')
    ctx.beginPath()
    ctx.arc(x, y, 5, 0, 2 * Math.PI)
    ctx.fill()
  })

  ctx.draw()
}

// 查看更多推荐
const viewMoreRecommend = () => {
  navigate(paths.USER.DISH_LIST, { filter: 'recommend' })
}

// 查看菜品
const viewDish = (id) => {
  toDishDetail(id)
}

// 采纳建议
const applyImprovement = useDebounce((item) => {
  uni.showModal({
    title: '采纳建议',
    content: `是否采纳「${item.title}」建议？我们将为您推荐相关菜品。`,
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '已采纳，正在为您推荐...', icon: 'success' })
        setTimeout(() => {
          navigate(paths.USER.DISH_LIST, { filter: item.type })
        }, 1500)
      }
    }
  })
}, 300)

// 查看详情
const viewDetail = (item) => {
  uni.showModal({
    title: item.title,
    content: item.desc + '\n\n详细说明：\n' + item.detail || '暂无详细说明',
    showCancel: false
  })
}

// 导出报告
const exportReport = (type) => {
  uni.showLoading({ title: '生成中...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: type === 'pdf' ? 'PDF已生成' : '图片已保存',
      icon: 'success'
    })
  }, 2000)
}

// 分享报告
const shareReport = () => {
  uni.showActionSheet({
    itemList: ['分享到微信', '分享到朋友圈', '保存图片'],
    success: (res) => {
      const actions = ['已分享到微信', '已分享到朋友圈', '图片已保存']
      uni.showToast({ title: actions[res.tapIndex], icon: 'success' })
    }
  })
}

// 加载营养分析数据
const loadNutritionData = async () => {
  if (!userStore.isLogin) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  loading.value = true
  loadError.value = ''

  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    // 调用营养分析API
    const res = await aiApi.analyzeNutrition({
      userId,
      date: new Date().toISOString().split('T')[0] // 今日日期
    })

    if (!res || !res.data) {
      throw new Error('未获取到营养分析数据')
    }

    // 更新今日热量
    todayCalories.value = res.data.calories || '0'

    // 更新营养素数据
    if (res.data.nutrition) {
      nutrition.value = {
        ...nutrition.value,
        ...res.data.nutrition
      }
    }

    // 更新微量元素
    if (res.data.micronutrients) {
      micronutrients.value = res.data.micronutrients
    }

    // 更新AI建议
    if (res.data.suggestions) {
      suggestions.value = res.data.suggestions
    }

    // 更新推荐菜品
    if (res.data.recommendDishes) {
      recommendDishes.value = res.data.recommendDishes
    }

    // 更新健康评分 / 饮食习惯 / 改进建议 / 趋势
    if (res.data.healthScore != null) {
      healthScore.value = res.data.healthScore
    }
    if (res.data.scoreDetails) {
      scoreDetails.value = { ...scoreDetails.value, ...res.data.scoreDetails }
    }
    if (res.data.habits) {
      habits.value = { ...habits.value, ...res.data.habits }
    }
    if (res.data.improvements) {
      improvements.value = res.data.improvements
    }
    if (res.data.mealCombos) {
      mealCombos.value = res.data.mealCombos
    }
    if (res.data.trend) {
      trendData.value = {
        labels: (res.data.trend.labels || []).map(String),
        values: (res.data.trend.values || []).map(v => Number(v) || 0)
      }
    }

    // 数据到位后重绘图表
    nextTick(() => {
      drawNutritionChart()
      if (activeTab.value === 'report') {
        drawTrendChart()
      }
    })
  } catch (error) {
    console.error('加载营养分析数据失败:', error)
    // 失败时不保留任何写死数值，清空并置错误态
    loadError.value = error?.message || '营养分析数据加载失败'
    todayCalories.value = '0'
    nutrition.value = {
      protein: 0, proteinTarget: 100, proteinPercent: 0,
      carbs: 0, carbsTarget: 280, carbsPercent: 0,
      fat: 0, fatTarget: 70, fatPercent: 0,
      fiber: 0, fiberTarget: 25, fiberPercent: 0
    }
    micronutrients.value = []
    suggestions.value = []
    recommendDishes.value = []
    mealCombos.value = []
    improvements.value = []
    healthScore.value = 0
    scoreDetails.value = { balance: 0, calories: 0, regularity: 0 }
    habits.value = { avgCalories: 0, proteinRatio: 0, veggieFreq: 0, diningOut: 0 }
    trendData.value = { labels: [], values: [] }
  } finally {
    loading.value = false
  }
}

// 咨询AI
const consultAI = () => {
  navigate(paths.MAIN.AI)
}

onMounted(() => {
  nextTick(() => {
    drawNutritionChart()
  })

  // 加载营养分析数据
  loadNutritionData()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.ai-advanced-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 140rpx;
}

/* 加载 / 错误 / 空态 */
.ai-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
  padding: 160rpx 60rpx;
}

.ai-state-text {
  font-size: 30rpx;
  color: #999;
}

.ai-state-desc {
  font-size: 26rpx;
  color: #bbb;
  text-align: center;
  line-height: 1.6;
}

.ai-state-btn {
  min-width: 240rpx;
  height: 72rpx;
  line-height: 72rpx;
  background: linear-gradient(135deg, #FF6B35, #FF8F61);
  color: #fff;
  font-size: 28rpx;
  border-radius: 36rpx;
  border: none;
}

.header {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%);
  padding: 40rpx 30rpx 60rpx;

  .header-content {
    .title {
      font-size: 40rpx;
      font-weight: bold;
      color: #fff;
      display: block;
      margin-bottom: 10rpx;
    }

    .subtitle {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.9);
    }
  }
}

.tabs {
  display: flex;
  background: #fff;
  margin: -30rpx 30rpx 20rpx;
  border-radius: 16rpx;
  padding: 10rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);

  .tab-item {
    flex: 1;
    text-align: center;
    padding: 20rpx 0;
    border-radius: 12rpx;
    transition: all 0.3s;

    text {
      font-size: 28rpx;
      color: #666;
    }

    &.active {
      background: #FF6B35;

      text {
        color: #fff;
        font-weight: bold;
      }
    }
  }
}

.content {
  height: calc(100vh - 200rpx);
  padding: 0 20rpx 20rpx;
}

.section-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    .section-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }

    .date {
      font-size: 24rpx;
      color: #999;
    }

    .more {
      font-size: 26rpx;
      color: #FF6B35;
    }
  }
}

// 营养分析样式
.nutrition-chart {
  position: relative;
  width: 200px;
  height: 200px;
  margin: 40rpx auto;

  .chart-canvas {
    width: 100%;
    height: 100%;
  }

  .chart-center {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;

    .total-calories {
      font-size: 36px;
      font-weight: bold;
      color: #333;
      display: block;
    }

    .unit {
      font-size: 14px;
      color: #999;
    }
  }
}

.nutrition-details {
  .nutrition-item {
    margin-bottom: 30rpx;

    .nutrition-header {
      display: flex;
      align-items: center;
      margin-bottom: 10rpx;

      .color-dot {
        width: 12rpx;
        height: 12rpx;
        border-radius: 50%;
        margin-right: 10rpx;

        &.protein { background: #4CAF50; }
        &.carbs { background: #2196F3; }
        &.fat { background: #FFC107; }
        &.fiber { background: #9C27B0; }
      }

      .nutrition-name {
        flex: 1;
        font-size: 28rpx;
        color: #333;
      }

      .nutrition-value {
        font-size: 28rpx;
        font-weight: bold;
        color: #FF6B35;
      }
    }

    .progress-bar {
      height: 12rpx;
      background: #F0F0F0;
      border-radius: 6rpx;
      overflow: hidden;
      margin-bottom: 8rpx;

      .progress-fill {
        height: 100%;
        border-radius: 6rpx;
        transition: width 0.3s;
      }
    }

    .nutrition-target {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.micronutrients {
  .micro-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .micro-icon {
      width: 60rpx;
      height: 60rpx;
      border-radius: 12rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      @include flex-center;
      margin-right: 20rpx;

      .icon {
        font-size: 24rpx;
        font-weight: bold;
        color: #fff;
      }
    }

    .micro-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 5rpx;

      .micro-name {
        font-size: 28rpx;
        color: #333;
      }

      .micro-value {
        font-size: 24rpx;
        color: #666;
      }
    }

    .micro-status {
      padding: 8rpx 20rpx;
      border-radius: 20rpx;
      font-size: 24rpx;

      &.normal {
        background: #E8F5E9;
        color: #4CAF50;
      }

      &.lack {
        background: #FFF3E0;
        color: #FF9800;
      }

      &.good {
        background: #E3F2FD;
        color: #2196F3;
      }
    }
  }
}

.ai-suggestions {
  .suggestion-item {
    display: flex;
    gap: 20rpx;
    padding: 24rpx;
    background: #F8F9FA;
    border-radius: 12rpx;
    margin-bottom: 16rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .suggestion-icon {
      width: 60rpx;
      height: 60rpx;
      border-radius: 50%;
      @include flex-center;
      flex-shrink: 0;

      &.warning {
        background: #FFF3E0;
      }

      &.success {
        background: #E8F5E9;
      }

      &.info {
        background: #E3F2FD;
      }
    }

    .suggestion-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8rpx;

      .suggestion-title {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
      }

      .suggestion-desc {
        font-size: 26rpx;
        color: #666;
        line-height: 1.6;
      }
    }
  }
}

// 智能推荐样式
.recommend-reason {
  .reason-item {
    display: flex;
    gap: 20rpx;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .reason-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8rpx;

      .reason-title {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
      }

      .reason-desc {
        font-size: 26rpx;
        color: #666;
        line-height: 1.6;
      }
    }
  }
}

.recommend-dishes {
  .dish-card {
    display: flex;
    gap: 20rpx;
    padding: 20rpx;
    background: #F8F9FA;
    border-radius: 12rpx;
    margin-bottom: 20rpx;
    position: relative;

    &:last-child {
      margin-bottom: 0;
    }

    .dish-image {
      width: 160rpx;
      height: 120rpx;
      border-radius: 12rpx;
      flex-shrink: 0;
    }

    .dish-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8rpx;

      .dish-name {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
      }

      .dish-tags {
        display: flex;
        gap: 10rpx;
        flex-wrap: wrap;

        .tag {
          padding: 4rpx 12rpx;
          background: #E8F5E9;
          color: #4CAF50;
          font-size: 22rpx;
          border-radius: 4rpx;
        }
      }

      .dish-nutrition {
        display: flex;
        gap: 20rpx;

        .nutrition-item {
          font-size: 24rpx;
          color: #666;
        }
      }
    }

    .match-score {
      position: absolute;
      top: 20rpx;
      right: 20rpx;
      text-align: center;

      .score-label {
        display: block;
        font-size: 20rpx;
        color: #999;
        margin-bottom: 5rpx;
      }

      .score-value {
        font-size: 28rpx;
        font-weight: bold;
        color: #FF6B35;
      }
    }
  }
}

.meal-combo {
  .combo-item {
    padding: 24rpx;
    background: #F8F9FA;
    border-radius: 12rpx;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .combo-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;

      .combo-type {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
      }

      .combo-calories {
        font-size: 24rpx;
        color: #FF6B35;
        font-weight: bold;
      }
    }

    .combo-dishes {
      .combo-dish {
        padding: 12rpx 0;

        .dish-text {
          font-size: 26rpx;
          color: #666;
          line-height: 1.6;
        }
      }
    }
  }
}

// 健康报告样式
.health-score {
  display: flex;
  align-items: center;
  gap: 40rpx;

  .score-circle {
    width: 180rpx;
    height: 180rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%);
    @include flex-center;
    flex-direction: column;
    flex-shrink: 0;

    .score-number {
      font-size: 56rpx;
      font-weight: bold;
      color: #fff;
      line-height: 1;
    }

    .score-label {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.9);
      margin-top: 5rpx;
    }
  }

  .score-details {
    flex: 1;

    .score-item {
      display: flex;
      justify-content: space-between;
      padding: 16rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .item-label {
        font-size: 26rpx;
        color: #666;
      }

      .item-value {
        font-size: 26rpx;
        font-weight: bold;
        color: #FF6B35;
      }
    }
  }
}

.trend-chart {
  .chart-canvas {
    width: 100%;
    height: 200px;
  }
}

.habits-analysis {
  .habit-item {
    display: flex;
    justify-content: space-between;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .habit-label {
      font-size: 28rpx;
      color: #333;
    }

    .habit-value {
      font-size: 28rpx;
      font-weight: bold;
      color: #FF6B35;
    }
  }
}

.improvements {
  .improvement-item {
    padding: 24rpx;
    background: #F8F9FA;
    border-radius: 12rpx;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .improvement-header {
      display: flex;
      align-items: center;
      gap: 15rpx;
      margin-bottom: 12rpx;

      .priority-badge {
        padding: 6rpx 16rpx;
        border-radius: 8rpx;
        font-size: 22rpx;

        &.priority-high {
          background: #FFEBEE;
          color: #F44336;
        }

        &.priority-medium {
          background: #FFF3E0;
          color: #FF9800;
        }

        &.priority-low {
          background: #E3F2FD;
          color: #2196F3;
        }
      }

      .improvement-title {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
      }
    }

    .improvement-desc {
      font-size: 26rpx;
      color: #666;
      line-height: 1.6;
      margin-bottom: 20rpx;
    }

    .improvement-actions {
      display: flex;
      gap: 20rpx;

      .action-btn {
        flex: 1;
        height: 60rpx;
        border-radius: 8rpx;
        font-size: 26rpx;
        border: none;

        background: #FF6B35;
        color: #fff;

        &.secondary {
          background: #fff;
          color: #666;
          border: 1rpx solid #ddd;
        }
      }
    }
  }
}

.report-actions {
  display: flex;
  gap: 20rpx;

  .report-btn {
    flex: 1;
    height: 80rpx;
    background: #fff;
    border: 1rpx solid #FF6B35;
    border-radius: 12rpx;
    @include flex-center;
    gap: 10rpx;

    text {
      font-size: 26rpx;
      color: #FF6B35;
    }
  }
}

// 底部操作栏
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background: #fff;
  border-top: 1rpx solid #eee;
  padding-bottom: calc(20rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));

  .consult-btn {
    width: 100%;
    height: 80rpx;
    background: linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%);
    color: #fff;
    border: none;
    border-radius: 40rpx;
    font-size: 28rpx;
    @include flex-center;
    gap: 10rpx;
  }
}
</style>
