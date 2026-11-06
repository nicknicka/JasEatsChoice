<script setup>
import { ref, onMounted, onActivated, computed } from 'vue'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'
import { ElMessage } from 'element-plus'
import { useAuthStore } from './../../store/authStore'
import { useUserStore } from './../../store/userStore'
import { useRouter } from 'vue-router'
import { NORDIC_COLORS, NORDIC_NUTRITION_COLORS, NORDIC_NUTRITION_BG } from '../../constants/nordicTheme.js'
import { ArrowDown, WarningFilled, CircleCheckFilled, Bowl } from '@element-plus/icons-vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'

// 卡路里统计数据
const calorieData = ref({
  today: {
    consumed: 0,
    remaining: 0,
    target: 2000 // 默认值，将从API获取
  },
  weekly: [
    { day: '周一', consumed: 0 },
    { day: '周二', consumed: 0 },
    { day: '周三', consumed: 0 },
    { day: '周四', consumed: 0 },
    { day: '周五', consumed: 0 },
    { day: '周六', consumed: 0 },
    { day: '周日', consumed: 0 }
  ],
  nutrition: [
    { name: '蛋白质', value: 0, unit: 'g' },
    { name: '碳水化合物', value: 0, unit: 'g' },
    { name: '脂肪', value: 0, unit: 'g' }
  ]
})

// 健康建议显示状态
const showAdvice = ref(false)

// 切换健康建议显示/隐藏
const toggleAdvice = () => {
  showAdvice.value = !showAdvice.value
}

// 推荐营养目标（根据膳食指南）
const recommendedGoals = ref({
  蛋白质: 90, // g
  碳水化合物: 250, // g
  脂肪: 70 // g
})

// 自定义营养目标（用户设置）
const customGoals = ref({})

const getCurrentUserId = () => {
  const authStore = useAuthStore()
  const userStore = useUserStore()

  if (authStore.userId) {
    return authStore.userId
  }

  if (userStore.userInfo?.userId) {
    return userStore.userInfo.userId
  }

  return null
}

const router = useRouter()

// 今日为零数据时展示空状态与「记录第一餐」入口
const hasTodayData = computed(() => calorieData.value.today.consumed > 0)

// 本周全为 0 时收起历史趋势，避免整页空而长
const hasWeeklyData = computed(() => calorieData.value.weekly.some((day) => day.consumed > 0))

const goRecordMeal = () => {
  router.push('/user/home/diet-record')
}

const updateRemainingCalories = () => {
  calorieData.value.today.remaining = calorieData.value.today.target - calorieData.value.today.consumed
}

const resetTodayNutrition = () => {
  calorieData.value.today.consumed = 0
  calorieData.value.nutrition[0].value = 0
  calorieData.value.nutrition[1].value = 0
  calorieData.value.nutrition[2].value = 0
  updateRemainingCalories()
}

const loadUserPreferences = async (userId) => {
  const response = await axios.get(
    `${API_CONFIG.baseURL}${API_CONFIG.user.preferences.replace('{userId}', userId)}`
  )

  if (response.data && response.data.code === '200') {
    if (response.data.data.calorieTarget) {
      calorieData.value.today.target = response.data.data.calorieTarget
    }

    if (response.data.data.nutritionGoals) {
      customGoals.value = response.data.data.nutritionGoals
    }
  }

  updateRemainingCalories()
}

const loadTodayDietRecords = async (userId) => {
  const currentDate = new Date().toISOString().split('T')[0]
  const response = await axios.get(
    `${API_CONFIG.baseURL}${API_CONFIG.diet.date.replace('{userId}', userId)}${currentDate}`
  )

  if (
    response.data &&
    response.data.code === '200' &&
    response.data.data &&
    Array.isArray(response.data.data)
  ) {
    const records = response.data.data

    let totalCalories = 0
    let totalProtein = 0
    let totalCarbs = 0
    let totalFat = 0

    records.forEach((record) => {
      totalCalories += Number(record?.calorie) || 0
      totalProtein += Number(record?.protein) || 0
      totalCarbs += Number(record?.carbohydrate) || 0
      totalFat += Number(record?.fat) || 0
    })

    calorieData.value.today.consumed = Math.round(totalCalories)
    calorieData.value.nutrition[0].value = Math.round(totalProtein * 100) / 100
    calorieData.value.nutrition[1].value = Math.round(totalCarbs * 100) / 100
    calorieData.value.nutrition[2].value = Math.round(totalFat * 100) / 100
    updateRemainingCalories()
    return
  }

  resetTodayNutrition()
}

const loadWeeklyCalories = async (userId) => {
  const response = await axios.get(`${API_CONFIG.baseURL}${API_CONFIG.diet.week.replace('{userId}', userId)}`)

  if (
    response.data &&
    response.data.code === '200' &&
    response.data.data &&
    Array.isArray(response.data.data)
  ) {
    calorieData.value.weekly = response.data.data.map((item) => ({
      day: item.day || '',
      consumed: Number(item.consumed) || 0
    }))
    return
  }

  console.warn('本周卡路里统计数据格式不正确，将使用默认值')
}

const loadCalorieData = async () => {
  const userId = getCurrentUserId()
  if (!userId) {
    ElMessage.error('未找到用户信息，请先登录')
    return
  }

  try {
    await loadUserPreferences(userId)
  } catch (error) {
    console.error('加载用户偏好失败:', error)
  }

  try {
    await loadTodayDietRecords(userId)
  } catch (error) {
    console.error('加载今日饮食记录失败:', error)
    resetTodayNutrition()
  }

  try {
    await loadWeeklyCalories(userId)
  } catch (error) {
    console.error('加载本周卡路里记录失败:', error)
    ElMessage.error('加载本周卡路里记录失败，请稍后重试')
  }
}

// 从API获取数据
onMounted(() => {
  loadCalorieData()
})

onActivated(() => {
  loadCalorieData()
})

const nutritionKeyMap = {
  蛋白质: 'protein',
  碳水化合物: 'carbs',
  脂肪: 'fat'
}

const getNutritionThemeKey = (name) => nutritionKeyMap[name] || 'protein'

// 获取营养百分比
const getNutritionPercentage = (value, name) => {
  // 优先使用用户自定义目标，若无则使用推荐目标
  const goal = customGoals.value[name] || recommendedGoals.value[name] || 1
  // 避免除以0
  let percentage = goal > 0 ? (value / goal) * 100 : 0
  // 不再限制百分比在0-100之间，允许超过
  // 四舍五入保留两位小数
  return Math.round(percentage * 100) / 100
}

// 获取卡路里百分比
const getCaloriePercentage = (consumed) => {
  // 避免除以0
  let percentage =
    calorieData.value.today.target > 0 ? (consumed / calorieData.value.today.target) * 100 : 0
  // 不再限制百分比在0-100之间，允许超过
  // 四舍五入保留两位小数
  return Math.round(percentage * 100) / 100
}

// 判断是否为极端值 - 与健康提示阈值一致
const isExtremeValue = (value, name) => {
  const percent = getNutritionPercentage(value, name)
  // 超过150%则标记为极端值
  return percent > 150
}

// 获取营养颜色 - 基于百分比动态变化（使用主题常量）
const getNutritionColor = (name, percentage) => {
  const normalColors = {
    蛋白质: NORDIC_COLORS.blue,
    碳水化合物: NORDIC_COLORS.green,
    脂肪: NORDIC_COLORS.yellow,
    default: NORDIC_COLORS.red
  }

  const warningColors = {
    蛋白质: NORDIC_COLORS.yellow,
    碳水化合物: NORDIC_COLORS.yellow,
    脂肪: NORDIC_COLORS.yellow,
    default: NORDIC_COLORS.yellow
  }

  const dangerColors = {
    蛋白质: NORDIC_COLORS.red,
    碳水化合物: NORDIC_COLORS.red,
    脂肪: NORDIC_COLORS.red,
    default: NORDIC_COLORS.red
  }

  if (percentage > 200) {
    return dangerColors[name] || dangerColors.default
  } else if (percentage > 100) {
    return warningColors[name] || warningColors.default
  } else {
    return normalColors[name] || normalColors.default
  }
}

// 获取卡路里进度条颜色 - 基于百分比动态变化（使用主题常量）
const getCalorieColor = (percentage) => {
  if (percentage > 200) {
    return NORDIC_COLORS.red
  } else if (percentage > 100) {
    return NORDIC_COLORS.yellow
  } else {
    return NORDIC_COLORS.accent
  }
}

// 动态计算健康建议
const healthAdvice = computed(() => {
  const advice = []

  // 遍历所有营养元素
  calorieData.value.nutrition.forEach((item) => {
    const { name, value } = item
    const percent = getNutritionPercentage(value, name)

    // 根据不同情况生成建议
    if (name === '蛋白质') {
      if (percent > 200) {
        advice.push('蛋白质摄入已超过推荐值2倍，长期过量摄入可能加重肾脏负担')
      } else if (percent > 150) {
        advice.push('蛋白质摄入已超过推荐值1.5倍，建议合理搭配饮食')
      } else if (percent < 70) {
        advice.push('蛋白质摄入不足，建议增加鸡蛋、牛奶、瘦肉等高蛋白食物')
      }
    } else if (name === '碳水化合物') {
      if (percent > 200) {
        advice.push('碳水化合物摄入已超过推荐值2倍，长期过量可能导致血糖波动')
      } else if (percent > 150) {
        advice.push('碳水化合物摄入已超过推荐值1.5倍，建议增加膳食纤维摄入')
      } else if (percent < 70) {
        advice.push('碳水化合物摄入不足，建议增加主食摄入，保证能量供应')
      }
    } else if (name === '脂肪') {
      if (percent > 200) {
        advice.push('脂肪摄入已超过推荐值2倍，长期过量摄入会增加健康风险')
      } else if (percent > 150) {
        advice.push('脂肪摄入已超过推荐值1.5倍，建议适当减少油炸食品摄入')
      } else if (percent < 70) {
        advice.push('脂肪摄入不足，建议适量摄入健康脂肪如坚果、鱼类等')
      }
    }
  })

  return advice
})

// --- 北欧暖光主题计算属性 ---

// 圆环进度相关
const calorieRingCircumference = 2 * Math.PI * 54 // r=54

const calorieRingOffset = computed(() => {
  const pct = Math.min(getCaloriePercentage(calorieData.value.today.consumed), 100) / 100
  return calorieRingCircumference * (1 - pct)
})

const calorieRingColor = computed(() => {
  const pct = getCaloriePercentage(calorieData.value.today.consumed)
  return getCalorieColor(pct)
})

// 营养素颜色映射（引用主题常量）
const nordicNutritionColors = NORDIC_NUTRITION_COLORS

const nordicNutritionBg = NORDIC_NUTRITION_BG

// 每周柱状图最大值
const weeklyMax = computed(() => {
  const maxVal = Math.max(...calorieData.value.weekly.map((d) => d.consumed))
  return maxVal > 0 ? maxVal : calorieData.value.today.target
})

// 今日是周几 (0=周一 ... 6=周日)
const todayDayIndex = computed(() => {
  const day = new Date().getDay()
  return day === 0 ? 6 : day - 1 // 转为周一=0
})

const getNutritionBarStyle = (item) => {
  const percentage = getNutritionPercentage(item.value, item.name)
  return {
    width: Math.max(0, Math.min(percentage, 100)) + '%',
    background: getNutritionColor(item.name, percentage)
  }
}
</script>

<template>
  <div class="nordic-calorie">
    <!-- 页面标题 -->
    <header class="calorie-header">
      <h1>卡路里统计</h1>
      <span class="header-date">{{ new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }) }}</span>
    </header>

    <!-- 今日状态 -->
    <h2 class="section-title">今日状态</h2>

    <div v-if="hasTodayData" class="today-hero nordic-card">
      <div class="hero-ring-wrapper">
        <svg class="ring-svg" viewBox="0 0 120 120">
          <circle class="ring-bg" cx="60" cy="60" r="54" />
          <circle
            class="ring-fill"
            cx="60" cy="60" r="54"
            :stroke="calorieRingColor"
            :stroke-dasharray="calorieRingCircumference"
            :stroke-dashoffset="calorieRingOffset"
          />
        </svg>
        <div class="ring-center">
          <span class="ring-value">{{ calorieData.today.consumed }}</span>
          <span class="ring-label">已摄入 kcal</span>
        </div>
      </div>

      <div class="hero-stats">
        <div class="stat-block">
          <span class="stat-label">剩余额度</span>
          <span class="stat-num">{{ Math.max(0, calorieData.today.remaining) }}<em>kcal</em></span>
        </div>
        <div class="stat-block">
          <span class="stat-label">每日目标</span>
          <span class="stat-num">{{ calorieData.today.target }}<em>kcal</em></span>
        </div>
      </div>
    </div>

    <div v-else class="today-empty nordic-card">
      <AppEmptyState description="今天还没有饮食记录" :icon="Bowl" />
      <el-button type="primary" class="today-empty__action" @click="goRecordMeal">记录第一餐</el-button>
    </div>

    <!-- 营养摄入 -->
    <h2 class="section-title">营养摄入</h2>

    <div class="nutrition-grid">
      <div
        v-for="item in calorieData.nutrition"
        :key="item.name"
        class="nutrition-item nordic-card"
        :class="{ 'extreme': isExtremeValue(item.value, item.name) }"
      >
        <div class="nutri-header">
          <span class="nutri-name">{{ item.name }}</span>
          <span class="nutri-badge" :style="{ background: nordicNutritionBg[getNutritionThemeKey(item.name)], color: nordicNutritionColors[getNutritionThemeKey(item.name)] }">
            {{ Math.round(getNutritionPercentage(item.value, item.name)) }}%
          </span>
        </div>
        <div class="nutri-value-row">
          <span class="nutri-val">{{ item.value }}</span>
          <span class="nutri-unit">{{ item.unit }}</span>
          <span class="nutri-goal">/ {{ customGoals[item.name] || recommendedGoals[item.name] || 0 }}{{ item.unit }}</span>
        </div>
        <div class="nutri-bar-track">
          <div
            class="nutri-bar-fill"
            :style="getNutritionBarStyle(item)"
          ></div>
        </div>
      </div>
    </div>

    <!-- 健康建议 -->
    <div class="advice-section nordic-card" v-if="healthAdvice.length > 0 || true">
      <div class="advice-toggle" @click="toggleAdvice">
        <span class="advice-title-text">饮食建议</span>
        <span class="advice-count" v-if="healthAdvice.length > 0">{{ healthAdvice.length }}条</span>
        <span class="advice-arrow" :class="{ open: showAdvice }">&#x276F;</span>
      </div>
      <transition name="nordic-slide">
        <div class="advice-body" v-show="showAdvice">
          <div v-for="(tip, idx) in healthAdvice" :key="idx" class="advice-tip">
            <span class="tip-icon">&#9679;</span>
            <span>{{ tip }}</span>
          </div>
          <div class="advice-ok" v-if="healthAdvice.length === 0">
            <span class="ok-icon">&#10003;</span>
            <span>营养摄入在推荐范围内，继续保持</span>
          </div>
        </div>
      </transition>
    </div>

    <!-- 历史趋势 -->
    <template v-if="hasWeeklyData">
      <h2 class="section-title">历史趋势</h2>

      <div class="weekly-summary">
        <div class="weekly-stat">
          <span class="ws-value">{{ calorieData.weekly.reduce((s, i) => s + i.consumed, 0) }}</span>
          <span class="ws-label">总摄入 kcal</span>
        </div>
        <div class="weekly-divider"></div>
        <div class="weekly-stat">
          <span class="ws-value">{{ Math.round(calorieData.weekly.reduce((s, i) => s + i.consumed, 0) / Math.max(calorieData.weekly.length, 1)) }}</span>
          <span class="ws-label">日均 kcal</span>
        </div>
        <div class="weekly-divider"></div>
        <div class="weekly-stat">
          <span class="ws-value">{{ calorieData.today.target }}</span>
          <span class="ws-label">每日目标</span>
        </div>
      </div>

      <div class="weekly-chart nordic-card">
        <div
          v-for="(item, idx) in calorieData.weekly"
          :key="item.day"
          class="weekly-bar-row"
          :class="{ today: idx === todayDayIndex }"
        >
          <span class="bar-day">{{ item.day }}</span>
          <div class="bar-track">
            <div
              class="bar-fill"
              :style="{
                width: (weeklyMax > 0 ? (item.consumed / weeklyMax) * 100 : 0) + '%',
                background: idx === todayDayIndex ? 'var(--brand-500)' : 'var(--info)'
              }"
            ></div>
          </div>
          <span class="bar-val">{{ item.consumed }}</span>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.nordic-calorie {
  .nordic-page-container();
  max-width: var(--page-max);
  margin: 0 auto;
}

// --- 页面标题（§4.7 用户端标准版） ---
.calorie-header {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
  margin-bottom: var(--space-6);

  h1 {
    font-size: var(--text-h1);
    font-weight: var(--weight-semibold);
    color: var(--ink-900);
    margin: 0;
    letter-spacing: var(--tracking-tight);
  }

  .header-date {
    font-size: var(--text-body-sm);
    color: var(--ink-400);
  }
}

// --- 通用卡片 ---
.nordic-card {
  .nordic-card();
  padding: @nordic-space-lg;
}

// --- 今日摄入 ---
.today-hero {
  display: flex;
  align-items: center;
  gap: var(--space-8);
  padding: var(--space-8);
  margin-bottom: var(--space-6);
}

.hero-ring-wrapper {
  position: relative;
  width: 168px;
  height: 168px;
  flex-shrink: 0;

  .ring-svg {
    width: 100%;
    height: 100%;
    transform: rotate(-90deg);
  }

  .ring-bg {
    fill: none;
    stroke: @nordic-divider;
    stroke-width: 8;
  }

  .ring-fill {
    fill: none;
    stroke-width: 8;
    stroke-linecap: round;
    transition: stroke-dashoffset var(--duration-num) var(--ease-out);
  }

  .ring-center {
    position: absolute;
    inset: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: var(--space-1);

    .ring-value {
      font-family: var(--font-num);
      font-size: var(--text-display);
      font-weight: var(--weight-bold);
      color: var(--ink-900);
      line-height: 1;
      font-variant-numeric: tabular-nums;
      letter-spacing: var(--tracking-tight);
    }

    .ring-label {
      font-size: var(--text-body-sm);
      color: var(--ink-500);
    }
  }
}

.hero-stats {
  display: flex;
  gap: var(--space-4);
  flex: 1;
  min-width: 0;

  .stat-block {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: var(--space-2);
    padding: var(--space-4) var(--space-5);
    background: var(--canvas);
    border-radius: var(--radius-md);

    .stat-label {
      font-size: var(--text-body-sm);
      color: var(--ink-500);
    }

    .stat-num {
      display: flex;
      align-items: baseline;
      gap: var(--space-1);
      font-family: var(--font-num);
      font-size: var(--text-h1);
      font-weight: var(--weight-bold);
      color: var(--ink-900);
      font-variant-numeric: tabular-nums;
      letter-spacing: var(--tracking-tight);

      em {
        font-style: normal;
        font-size: var(--text-body-sm);
        font-weight: var(--weight-medium);
        color: var(--ink-400);
      }
    }
  }
}

// --- 今日空状态 ---
.today-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 0 var(--space-6);
  margin-bottom: var(--space-6);
}

// --- 分区标题 ---
h2.section-title {
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
  margin: 0 0 var(--space-4);
  letter-spacing: var(--tracking-normal);
}

// --- 营养摄入 ---
.nutrition-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: @nordic-space-md;
  margin-bottom: @nordic-space-lg;

  @media (max-width: 640px) {
    grid-template-columns: 1fr;
  }
}

.nutrition-item {
  padding: @nordic-space-md;
  animation: nordic-fade-in 0.4s ease both;

  &.extreme {
    border-color: @nordic-red;
  }

  .nutri-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  .nutri-name {
    font-size: @nordic-text-sm;
    color: @nordic-text-secondary;
    font-weight: 500;
  }

  .nutri-badge {
    font-size: 11px;
    font-weight: 600;
    padding: 2px 8px;
    border-radius: @nordic-radius-pill;
  }

  .nutri-value-row {
    margin-bottom: 12px;

    .nutri-val {
      font-size: @nordic-text-xl;
      font-weight: 700;
      color: @nordic-text;
      letter-spacing: -0.5px;
    }

    .nutri-unit {
      font-size: @nordic-text-sm;
      color: @nordic-text-muted;
      margin-left: 2px;
    }

    .nutri-goal {
      font-size: @nordic-text-xs;
      color: @nordic-text-muted;
      margin-left: 4px;
    }
  }

  .nutri-bar-track {
    height: 6px;
    background: @nordic-divider;
    border-radius: 3px;
    overflow: hidden;
  }

  .nutri-bar-fill {
    height: 100%;
    border-radius: 3px;
    transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  }
}

// --- 健康建议 ---
.advice-section {
  margin-bottom: @nordic-space-lg;
  padding: 0;
  overflow: hidden;

  .advice-toggle {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: @nordic-space-md @nordic-space-lg;
    cursor: pointer;
    user-select: none;
    transition: background 0.2s;

    &:hover {
      background: @nordic-bg;
    }

    .advice-title-text {
      font-size: @nordic-text-base;
      font-weight: 600;
      color: @nordic-text;
    }

    .advice-count {
      .nordic-pill-tag(@nordic-yellow-light, @nordic-yellow);
    }

    .advice-arrow {
      margin-left: auto;
      font-size: 11px;
      color: @nordic-text-muted;
      transition: transform 0.3s ease;

      &.open {
        transform: rotate(90deg);
      }
    }
  }

  .advice-body {
    padding: 0 @nordic-space-lg @nordic-space-md;
  }

  .advice-tip {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    font-size: @nordic-text-sm;
    color: @nordic-text-secondary;
    line-height: 1.6;
    padding: 6px 0;

    .tip-icon {
      color: @nordic-accent;
      font-size: 8px;
      margin-top: 6px;
      flex-shrink: 0;
    }
  }

  .advice-ok {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: @nordic-green-light;
    border-radius: @nordic-radius-md;
    color: var(--success);
    font-size: @nordic-text-sm;

    .ok-icon {
      font-weight: 700;
      flex-shrink: 0;
    }
  }
}

// --- 本周汇总 ---
.weekly-summary {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: @nordic-space-lg;
  background: @nordic-surface;
  border-radius: @nordic-radius-lg;
  border: 1px solid @nordic-border;
  margin-bottom: @nordic-space-md;

  .weekly-stat {
    text-align: center;

    .ws-value {
      display: block;
      font-size: @nordic-text-lg;
      font-weight: 700;
      color: @nordic-text;
      letter-spacing: -0.5px;
    }

    .ws-label {
      display: block;
      font-size: @nordic-text-xs;
      color: @nordic-text-muted;
      margin-top: 4px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
  }

  .weekly-divider {
    width: 1px;
    height: 40px;
    background: @nordic-border;
  }
}

// --- 周趋势图 ---
.weekly-chart {
  padding: @nordic-space-lg;

  .weekly-bar-row {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 0;

    &:not(:last-child) {
      border-bottom: 1px solid @nordic-divider;
    }

    &.today {
      .bar-day {
        color: @nordic-accent;
        font-weight: 600;
      }
    }

    .bar-day {
      width: 32px;
      font-size: @nordic-text-sm;
      color: @nordic-text-muted;
      flex-shrink: 0;
    }

    .bar-track {
      flex: 1;
      height: 8px;
      background: @nordic-divider;
      border-radius: 4px;
      overflow: hidden;
    }

    .bar-fill {
      height: 100%;
      border-radius: 4px;
      transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
      min-width: 2px;
    }

    .bar-val {
      width: 50px;
      text-align: right;
      font-size: @nordic-text-sm;
      color: @nordic-text-secondary;
      font-weight: 500;
      flex-shrink: 0;
    }
  }
}

// --- 过渡动画 ---
.nordic-slide-enter-active,
.nordic-slide-leave-active {
  transition: all 0.3s ease;
  max-height: 400px;
  overflow: hidden;
}

.nordic-slide-enter-from,
.nordic-slide-leave-to {
  max-height: 0;
  opacity: 0;
  padding-top: 0;
  padding-bottom: 0;
}
</style>
