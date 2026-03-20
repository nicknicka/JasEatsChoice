<!--
页面名称：ai/advanced（重构版）
原代码行数：1385行
重构后行数：约320行
减少比例：77%
重构时间：2026-03-20
-->
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
    <view class="tabs-wrapper">
      <TabSwitcher
        v-model="activeTab"
        :tabs="tabList"
      />
    </view>

    <scroll-view class="content" scroll-y>
      <!-- 营养分析 -->
      <view v-if="activeTab === 'nutrition'" class="nutrition-analysis">
        <!-- 今日摄入 -->
        <SectionCard :title="`今日营养摄入  ${todayDate}`">
          <NutritionChart
            :calories="todayCalories"
            :nutrition-data="nutritionChartData"
          />
          <NutritionDetails :nutrition-list="nutritionList" />
        </SectionCard>

        <!-- 微量元素 -->
        <SectionCard title="维生素与矿物质">
          <MicronutrientList :list="micronutrients" />
        </SectionCard>

        <!-- AI 建议 -->
        <SectionCard title="AI 饮食建议">
          <AISuggestions :suggestions="suggestions" />
        </SectionCard>
      </view>

      <!-- 智能推荐 -->
      <view v-if="activeTab === 'recommend'" class="smart-recommend">
        <!-- 推荐理由 -->
        <SectionCard title="今日推荐理由">
          <RecommendReason :reasons="recommendReasons" />
        </SectionCard>

        <!-- 推荐菜品 -->
        <SectionCard title="智能推荐菜品" extra="查看更多 ›" @extra-click="viewMoreRecommend">
          <RecommendDishCard
            v-for="dish in recommendDishes"
            :key="dish.id"
            :dish="dish"
            @tap="viewDish"
          />
        </SectionCard>

        <!-- 营养搭配 -->
        <SectionCard title="营养搭配建议">
          <MealCombo :combos="mealCombos" />
        </SectionCard>
      </view>

      <!-- 健康报告 -->
      <view v-if="activeTab === 'report'" class="health-report">
        <!-- 健康评分 -->
        <SectionCard title="本周健康评分">
          <HealthScoreCard
            :health-score="healthScore"
            :score-details="scoreDetails"
          />
        </SectionCard>

        <!-- 饮食趋势 -->
        <SectionCard title="近7天饮食趋势">
          <view class="trend-chart">
            <canvas canvas-id="trendCanvas" id="trendCanvas" class="chart-canvas"></canvas>
          </view>
        </SectionCard>

        <!-- 饮食习惯分析 -->
        <SectionCard title="饮食习惯分析">
          <view class="habits-analysis">
            <view class="habit-item" v-for="(value, key) in habits" :key="key">
              <text class="habit-label">{{ habitsLabels[key] }}</text>
              <text class="habit-value">{{ habitsValues(key, value) }}</text>
            </view>
          </view>
        </SectionCard>

        <!-- 改进建议 -->
        <SectionCard title="改进建议">
          <ImprovementCard
            :improvements="improvements"
            @apply="applyImprovement"
            @detail="viewDetail"
          />
        </SectionCard>

        <!-- 导出报告 -->
        <SectionCard title="报告管理">
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
        </SectionCard>
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
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import TabSwitcher from '@/components/common/TabSwitcher.vue'
import SectionCard from '@/components/common/SectionCard.vue'
import NutritionChart from './components/NutritionChart.vue'
import NutritionDetails from './components/NutritionDetails.vue'
import MicronutrientList from './components/MicronutrientList.vue'
import AISuggestions from './components/AISuggestions.vue'
import RecommendReason from './components/RecommendReason.vue'
import RecommendDishCard from './components/RecommendDishCard.vue'
import MealCombo from './components/MealCombo.vue'
import HealthScoreCard from './components/HealthScoreCard.vue'
import ImprovementCard from './components/ImprovementCard.vue'
import { useAIAnalysis } from '@/composables/ai/useAIAnalysis'

// 使用AI分析 composable
const {
  loading,
  todayCalories,
  nutrition,
  micronutrients,
  suggestions,
  recommendDishes,
  mealCombos,
  recommendReasons,
  healthScore,
  scoreDetails,
  habits,
  improvements,
  loadNutritionData,
  drawTrendChart,
  applyImprovement,
  viewDetail,
  exportReport,
  shareReport
} = useAIAnalysis()

// 选项卡
const activeTab = ref('nutrition')
const tabList = ref([
  { label: '营养分析', value: 'nutrition' },
  { label: '智能推荐', value: 'recommend' },
  { label: '健康报告', value: 'report' }
])

// 今日日期
const todayDate = computed(() => {
  const now = new Date()
  return `${now.getMonth() + 1}月${now.getDate()}日`
})

// 营养图表数据
const nutritionChartData = computed(() => [
  { percent: nutrition.value.proteinPercent, color: '#4CAF50' },
  { percent: nutrition.value.carbsPercent, color: '#2196F3' },
  { percent: nutrition.value.fatPercent, color: '#FFC107' },
  { percent: nutrition.value.fiberPercent, color: '#9C27B0' }
])

// 营养详情列表
const nutritionList = computed(() => [
  {
    name: '蛋白质',
    value: nutrition.value.protein,
    target: nutrition.value.proteinTarget,
    percent: nutrition.value.proteinPercent,
    color: '#4CAF50'
  },
  {
    name: '碳水化合物',
    value: nutrition.value.carbs,
    target: nutrition.value.carbsTarget,
    percent: nutrition.value.carbsPercent,
    color: '#2196F3'
  },
  {
    name: '脂肪',
    value: nutrition.value.fat,
    target: nutrition.value.fatTarget,
    percent: nutrition.value.fatPercent,
    color: '#FFC107'
  },
  {
    name: '膳食纤维',
    value: nutrition.value.fiber,
    target: nutrition.value.fiberTarget,
    percent: nutrition.value.fiberPercent,
    color: '#9C27B0'
  }
])

// 饮食习惯标签
const habitsLabels = {
  avgCalories: '平均每日热量',
  proteinRatio: '蛋白质摄入占比',
  veggieFreq: '蔬菜摄入频率',
  diningOut: '外食比例'
}

// 饮食习惯值格式化
const habitsValues = (key, value) => {
  const formats = {
    avgCalories: `${value}kcal`,
    proteinRatio: `${value}%`,
    veggieFreq: `${value}次/周`,
    diningOut: `${value}%`
  }
  return formats[key]
}

// 切换选项卡
watch(activeTab, (newTab) => {
  nextTick(() => {
    if (newTab === 'report') {
      drawTrendChart()
    }
  })
})

// 查看更多推荐
const viewMoreRecommend = () => {
  uni.navigateTo({ url: '/pages-user/index/index?filter=recommend' })
}

// 查看菜品
const viewDish = (id) => {
  uni.navigateTo({ url: `/pages-user/dish/detail?id=${id}` })
}

// 咨询AI
const consultAI = () => {
  uni.navigateTo({ url: '/pages-user/ai/index' })
}

onMounted(() => {
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

.tabs-wrapper {
  padding: 0 30rpx;
  margin: -30rpx 0 20rpx;
}

.content {
  height: calc(100vh - 200rpx);
  padding: 0 20rpx 20rpx;
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
