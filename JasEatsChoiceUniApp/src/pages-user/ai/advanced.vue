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
      <!-- 营养分析 -->
      <view v-if="activeTab === 'nutrition'" class="nutrition-analysis">
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
      <view v-if="activeTab === 'recommend'" class="smart-recommend">
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
      <view v-if="activeTab === 'report'" class="health-report">
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

// 选项卡
const activeTab = ref('nutrition')

// 今日日期
const todayDate = computed(() => {
  const now = new Date()
  return `${now.getMonth() + 1}月${now.getDate()}日`
})

// 今日热量
const todayCalories = ref('1856')

// 营养素数据
const nutrition = ref({
  protein: 85,
  proteinTarget: 100,
  proteinPercent: 85,
  carbs: 220,
  carbsTarget: 280,
  carbsPercent: 79,
  fat: 58,
  fatTarget: 70,
  fatPercent: 83,
  fiber: 18,
  fiberTarget: 25,
  fiberPercent: 72
})

// 微量元素
const micronutrients = ref([
  { name: '维生素A', value: 650, unit: 'μg', icon: 'A', status: 'normal', statusText: '正常' },
  { name: '维生素C', value: 78, unit: 'mg', icon: 'C', status: 'lack', statusText: '偏低' },
  { name: '钙', value: 850, unit: 'mg', icon: 'Ca', status: 'normal', statusText: '正常' },
  { name: '铁', value: 15, unit: 'mg', icon: 'Fe', status: 'good', statusText: '充足' },
  { name: '锌', value: 12, unit: 'mg', icon: 'Zn', status: 'normal', statusText: '正常' },
  { name: '维生素D', value: 5, unit: 'μg', icon: 'D', status: 'lack', statusText: '不足' }
])

// AI 建议
const suggestions = ref([
  { type: 'warning', icon: 'alert', title: '维生素C摄入不足', desc: '建议增加西兰花、青椒、猕猴桃等富含维C的食物' },
  { type: 'success', icon: 'checkmarkempty', title: '蛋白质摄入达标', desc: '您的蛋白质摄入量接近目标，继续保持' },
  { type: 'info', icon: 'info', title: '建议补充维生素D', desc: '适当晒太阳或食用富含维D的食物如鱼类、蛋黄' }
])

// 推荐菜品
const recommendDishes = ref([
  {
    id: 1,
    name: '西兰花炒虾仁',
    image: 'https://via.placeholder.com/200x150/4CAF50/FFFFFF?text=西兰花虾仁',
    tags: ['高蛋白', '低脂', '补充维C'],
    calories: 180,
    protein: 28,
    matchScore: 95
  },
  {
    id: 2,
    name: '番茄牛腩汤',
    image: 'https://via.placeholder.com/200x150/FF6B35/FFFFFF?text=番茄牛腩',
    tags: ['温热', '维C丰富', '高蛋白'],
    calories: 220,
    protein: 25,
    matchScore: 92
  },
  {
    id: 3,
    name: '清蒸鲈鱼',
    image: 'https://via.placeholder.com/200x150/2196F3/FFFFFF?text=清蒸鲈鱼',
    tags: ['低脂', '高蛋白', '富含维D'],
    calories: 150,
    protein: 30,
    matchScore: 90
  }
])

// 营养搭配
const mealCombos = ref([
  {
    type: '午餐推荐',
    totalCalories: 650,
    dishes: ['西兰花炒虾仁（180kcal）', '糙米饭（200kcal）', '紫菜蛋花汤（50kcal）', '清炒时蔬（80kcal）', '水果拼盘（140kcal）']
  },
  {
    type: '晚餐推荐',
    totalCalories: 450,
    dishes: ['清蒸鲈鱼（150kcal）', '杂粮粥（150kcal）', '凉拌蔬菜（80kcal）', '酸奶（70kcal）']
  }
])

// 健康评分
const healthScore = ref(85)
const scoreDetails = ref({
  balance: 88,
  calories: 90,
  regularity: 78
})

// 饮食习惯
const habits = ref({
  avgCalories: 1850,
  proteinRatio: 18,
  veggieFreq: 5,
  diningOut: 35
})

// 改进建议
const improvements = ref([
  {
    priority: 'high',
    priorityText: '高优先级',
    title: '增加维生素C摄入',
    desc: '每日维生素C摄入量为78mg，低于推荐值100mg，建议增加新鲜蔬果摄入'
  },
  {
    priority: 'medium',
    priorityText: '中优先级',
    title: '优化晚餐时间',
    desc: '检测到您晚餐时间较晚（平均21:30），建议提前到19:00-20:00'
  },
  {
    priority: 'low',
    priorityText: '低优先级',
    title: '增加全谷物摄入',
    desc: '建议将部分精制米面替换为全谷物，提高膳食纤维摄入'
  }
])

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

  // 模拟数据
  const data = [1800, 1950, 1750, 2000, 1850, 1900, 1856]
  const labels = ['周一', '周二', '周三', '周四', '周五', '周六', '今天']

  const maxValue = Math.max(...data)
  const minValue = Math.min(...data)
  const range = maxValue - minValue

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
  uni.navigateTo({ url: '/pages-user/index/index?filter=recommend' })
}

// 查看菜品
const viewDish = (id) => {
  uni.navigateTo({ url: `/pages-user/dish/detail?id=${id}` })
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
          uni.navigateTo({ url: '/pages-user/index/index?filter=' + item.type })
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

// 咨询AI
const consultAI = () => {
  uni.navigateTo({ url: '/pages-user/ai/index' })
}

onMounted(() => {
  nextTick(() => {
    drawNutritionChart()
  })
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
