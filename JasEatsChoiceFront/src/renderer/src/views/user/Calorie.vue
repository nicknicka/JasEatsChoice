<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'
import { ElMessage } from 'element-plus'
import { useAuthStore } from './../../store/authStore'
import { useUserStore } from './../../store/userStore'
import { NORDIC_COLORS, NORDIC_NUTRITION_COLORS, NORDIC_NUTRITION_BG } from '../../constants/nordicTheme.js'
// 新增图标导入
import { ArrowDown, WarningFilled, CircleCheckFilled } from '@element-plus/icons-vue'

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

// 从API获取数据
onMounted(() => {
  // 获取用户信息 - 从Pinia store获取
  const authStore = useAuthStore()
  const userStore = useUserStore()

  let userId = null

  console.log('Auth Store:', authStore)
  console.log('User Store:', userStore)
  // 从authStore获取userId，如果authStore中没有则从userStore的userInfo中获取
  if (authStore.userId) {
    userId = authStore.userId
  } else if (userStore.userInfo?.userId) {
    userId = userStore.userInfo.userId
  } else {
    // 用户未登录或没有保存用户信息
    ElMessage.error('未找到用户信息，请先登录')
    return
  }

  // 获取用户偏好设置（包含卡路里目标和营养目标）
  axios
    .get(`${API_CONFIG.baseURL}${API_CONFIG.user.preferences.replace('{userId}', userId)}`)
    .then((response) => {
      console.log('用户偏好:', response.data)
      if (response.data && response.data.code === '200') {
        // 设置卡路里目标
        if (response.data.data.calorieTarget) {
          calorieData.value.today.target = response.data.data.calorieTarget
        }

        // 设置自定义营养目标
        if (response.data.data.nutritionGoals) {
          customGoals.value = response.data.data.nutritionGoals
        }
      }
    })
    .catch((error) => {
      console.error('加载用户偏好失败:', error)
    })

  // 获取今日饮食记录并计算营养摄入
  // 获取当前日期字符串，格式为YYYY-MM-DD
  const currentDate = new Date().toISOString().split('T')[0]

  // 根据用户ID和日期获取饮食记录
  axios
    .get(`${API_CONFIG.baseURL}${API_CONFIG.diet.date.replace('{userId}', userId)}${currentDate}`)
    .then((response) => {
      console.log('今日饮食记录:', response.data)
      if (
        response.data &&
        response.data.code === '200' &&
        response.data.data &&
        Array.isArray(response.data.data)
      ) {
        const records = response.data.data

        // 计算今日总营养摄入
        let totalCalories = 0
        let totalProtein = 0
        let totalCarbs = 0
        let totalFat = 0

        // 遍历饮食记录计算营养总和
        records.forEach((record) => {
          totalCalories += record?.calorie || 0
          totalProtein += record?.protein || 0
          totalCarbs += record?.carbohydrate || 0
          totalFat += record?.fat || 0
        })

        // 更新今日卡路里数据
        calorieData.value.today.consumed = totalCalories
        calorieData.value.today.remaining = calorieData.value.today.target - totalCalories

        // 更新营养数据
        calorieData.value.nutrition[0].value = totalProtein // 蛋白质
        calorieData.value.nutrition[1].value = totalCarbs // 碳水化合物
        calorieData.value.nutrition[2].value = totalFat // 脂肪
      }
    })
    .catch((error) => {
      console.error('加载今日饮食记录失败:', error)
    })

  // 直接从后端获取本周卡路里统计
  axios
    .get(`${API_CONFIG.baseURL}${API_CONFIG.diet.week.replace('{userId}', userId)}`)
    .then((response) => {
      console.log('本周卡路里统计:', response.data)
      if (
        response.data &&
        response.data.code === '200' &&
        response.data.data &&
        Array.isArray(response.data.data)
      ) {
        // 确保每个项目都有day和consumed属性，与模板结构一致
        const processedWeekly = response.data.data.map((item) => ({
          day: item.day || '',
          consumed: item.consumed || 0
        }))

        // 使用处理后的每周数据
        calorieData.value.weekly = processedWeekly
      } else {
        // 如果数据格式不正确，保留默认模拟数据
        console.warn('本周卡路里统计数据格式不正确，将使用默认模拟数据')
      }
    })
    .catch((error) => {
      console.error('加载本周卡路里记录失败:', error)
      ElMessage.error('加载本周卡路里记录失败，请稍后重试')
    })
})

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
    return NORDIC_COLORS.blue
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
</script>

<template>
  <div class="nordic-calorie">
    <!-- 页面标题 -->
    <div class="nordic-page-header">
      <h2>卡路里统计</h2>
      <span class="header-date">{{ new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }) }}</span>
    </div>

    <!-- 今日摄入 - 圆环卡片 -->
    <div class="today-hero nordic-card">
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
        <div class="stat-pill">
          <span class="stat-dot remaining"></span>
          <span class="stat-num">{{ Math.max(0, calorieData.today.remaining) }}</span>
          <span class="stat-txt">剩余</span>
        </div>
        <div class="stat-pill">
          <span class="stat-dot target"></span>
          <span class="stat-num">{{ calorieData.today.target }}</span>
          <span class="stat-txt">目标</span>
        </div>
      </div>
    </div>

    <!-- 营养摄入 -->
    <div class="section-title">
      <span>营养摄入</span>
    </div>

    <div class="nutrition-grid">
      <div
        v-for="item in calorieData.nutrition"
        :key="item.name"
        class="nutrition-item nordic-card"
        :class="{ 'extreme': isExtremeValue(item.value, item.name) }"
      >
        <div class="nutri-header">
          <span class="nutri-name">{{ item.name }}</span>
          <span class="nutri-badge" :style="{ background: nordicNutritionBg[item.name], color: nordicNutritionColors[item.name] }">
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
            :style="{
              width: Math.min(getNutritionPercentage(item.value, item.name), 100) + '%',
              background: nordicNutritionColors[item.name]
            }"
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

    <!-- 本周统计 -->
    <div class="section-title" style="margin-top: var(--nordic-space-xl, 32px);">
      <span>本周趋势</span>
    </div>

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
              background: idx === todayDayIndex ? 'var(--nordic-accent, #D4845A)' : 'var(--nordic-blue, #6B9BD2)'
            }"
          ></div>
        </div>
        <span class="bar-val">{{ item.consumed }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.nordic-calorie {
  .nordic-page-container();
  max-width: 800px;
  margin: 0 auto;
}

// --- 页面标题 ---
.nordic-page-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: @nordic-space-lg;

  h2 {
    font-size: @nordic-text-xl;
    font-weight: 700;
    color: @nordic-text;
    margin: 0;
    letter-spacing: -0.5px;
  }

  .header-date {
    font-size: @nordic-text-sm;
    color: @nordic-text-muted;
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
  gap: 40px;
  padding: 32px;
  margin-bottom: @nordic-space-lg;
  .nordic-animate-fade();
}

.hero-ring-wrapper {
  position: relative;
  width: 160px;
  height: 160px;
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
    transition: stroke-dashoffset 1s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .ring-center {
    position: absolute;
    inset: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    .ring-value {
      font-size: 36px;
      font-weight: 700;
      color: @nordic-text;
      letter-spacing: -1px;
      line-height: 1;
    }

    .ring-label {
      font-size: @nordic-text-xs;
      color: @nordic-text-muted;
      margin-top: 4px;
    }
  }
}

.hero-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;

  .stat-pill {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    background: @nordic-bg;
    border-radius: @nordic-radius-md;

    .stat-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      flex-shrink: 0;

      &.remaining { background: @nordic-green; }
      &.target { background: @nordic-blue; }
    }

    .stat-num {
      font-size: @nordic-text-lg;
      font-weight: 600;
      color: @nordic-text;
    }

    .stat-txt {
      font-size: @nordic-text-sm;
      color: @nordic-text-muted;
    }
  }
}

// --- 分区标题 ---
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: @nordic-space-md;

  span {
    font-size: @nordic-text-md;
    font-weight: 600;
    color: @nordic-text;
  }
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
    color: #4a7a4d;
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
