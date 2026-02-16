<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'
import { ElMessage } from 'element-plus'
import { useAuthStore } from './../../store/authStore'
import { useUserStore } from './../../store/userStore'
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

// 获取营养颜色 - 基于百分比动态变化
const getNutritionColor = (name, percentage) => {
  // 正常范围（0-100%）
  const normalColors = {
    蛋白质: '#2196F3',
    碳水化合物: '#4CAF50',
    脂肪: '#FF9800',
    default: '#FF6B6B'
  }

  // 警告范围（101-200%）
  const warningColors = {
    蛋白质: '#FFC107',
    碳水化合物: '#FFC107',
    脂肪: '#FFC107',
    default: '#FFC107'
  }

  // 危险范围（>200%）
  const dangerColors = {
    蛋白质: '#FF6B6B',
    碳水化合物: '#FF6B6B',
    脂肪: '#FF6B6B',
    default: '#FF6B6B'
  }

  if (percentage > 200) {
    return dangerColors[name] || dangerColors.default
  } else if (percentage > 100) {
    return warningColors[name] || warningColors.default
  } else {
    return normalColors[name] || normalColors.default
  }
}

// 获取卡路里进度条颜色 - 基于百分比动态变化
const getCalorieColor = (percentage) => {
  if (percentage > 200) {
    return '#FF6B6B' // 危险红色
  } else if (percentage > 100) {
    return '#FFC107' // 警告黄色
  } else {
    return '#2196F3' // 正常蓝色
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
</script>

<template>
  <div class="calorie-container">
    <h2 class="fade-in-up">卡路里统计</h2>

    <!-- 今日概览 -->
    <div class="today-overview-section">
      <h3>今日概览</h3>
      <div class="today-overview">
        <el-card class="overview-card scale-in">
          <div class="overview-item">
            <div class="overview-label">今日已摄入</div>
            <div class="overview-value number-scroll">{{ calorieData.today.consumed }} kcal</div>
          </div>
        </el-card>

        <el-card class="overview-card scale-in">
          <div class="overview-item">
            <div class="overview-label">今日剩余</div>
            <div class="overview-value remaining number-scroll">{{ calorieData.today.remaining }} kcal</div>
          </div>
        </el-card>

        <el-card class="overview-card scale-in">
          <div class="overview-item">
            <div class="overview-label">今日目标</div>
            <div class="overview-value target number-scroll">{{ calorieData.today.target }} kcal</div>
          </div>
        </el-card>
      </div>

      <!-- 营养比例 -->
      <el-card class="nutrition-card fade-in-up delay-100">
        <template #header>
          <div class="card-header">营养摄入比例</div>
        </template>
        <div class="nutrition-chart">
          <div
            v-for="item in calorieData.nutrition"
            :key="item.name"
            class="nutrition-item stagger-item"
            :class="{ 'extreme-value': isExtremeValue(item.value, item.name) }"
          >
            <div class="nutrition-info">
              <div class="nutrition-name">{{ item.name }}</div>
              <div class="nutrition-value">
                {{ item.value }}{{ item.unit }}
                <span class="recommended-goal">
                  (推荐: {{ customGoals[item.name] || recommendedGoals[item.name] || 0
                  }}{{ item.unit }})
                </span>
              </div>
            </div>
            <el-progress
              :percentage="Math.min(getNutritionPercentage(item.value, item.name), 100)"
              :color="getNutritionColor(item.name, getNutritionPercentage(item.value, item.name))"
              :stroke-width="21"
              :text-inside="true"
              :class="[
                { 'extreme-progress': getNutritionPercentage(item.value, item.name) > 200 },
                `${item.name}-progress`,
                {
                  'zero-progress':
                    Math.min(getNutritionPercentage(item.value, item.name), 100) === 0
                }
              ]"
            >
              <span>
                {{
                  getNutritionPercentage(item.value, item.name) === 0
                    ? '0%'
                    : getNutritionPercentage(item.value, item.name) > 200
                      ? '严重超出'
                      : getNutritionPercentage(item.value, item.name) + '%'
                }}
              </span>
            </el-progress>
          </div>

          <!-- 饮食健康建议部分 -->
          <div class="health-advice-section">
            <div class="advice-header" @click="toggleAdvice">
              <el-icon class="arrow-icon" :class="{ rotate: showAdvice }"><ArrowDown /></el-icon>
              <span class="advice-title">饮食健康建议</span>
            </div>

            <div :class="['advice-content', { show: showAdvice }]">
              <div v-for="advice in healthAdvice" :key="advice" class="advice-item">
                <el-icon class="advice-icon"><WarningFilled /></el-icon>
                <span>{{ advice }}</span>
              </div>

              <div v-if="healthAdvice.length === 0" class="advice-empty">
                <el-icon class="empty-icon"><CircleCheckFilled /></el-icon>
                <span>您的营养摄入在推荐范围内，保持均衡饮食哦！</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 周统计 -->
    <div class="weekly-statistics-section">
      <h3>本周统计</h3>

      <!-- 周统计概览 -->
      <div class="weekly-overview">
        <el-card class="overview-card scale-in">
          <div class="overview-item">
            <div class="overview-label">本周总摄入</div>
            <div class="overview-value total number-scroll">
              {{ calorieData.weekly.reduce((sum, item) => sum + item.consumed, 0) }} kcal
            </div>
          </div>
        </el-card>

        <el-card class="overview-card scale-in">
          <div class="overview-item">
            <div class="overview-label">本周日均</div>
            <div class="overview-value average number-scroll">
              {{
                Math.round(
                  calorieData.weekly.reduce((sum, item) => sum + item.consumed, 0) /
                    Math.max(calorieData.weekly.length, 1)
                )
              }}
              kcal
            </div>
          </div>
        </el-card>

        <el-card class="overview-card scale-in">
          <div class="overview-item">
            <div class="overview-label">每日目标</div>
            <div class="overview-value target number-scroll">{{ calorieData.today.target }} kcal</div>
          </div>
        </el-card>
      </div>

      <el-card class="weekly-card">
        <template #header>
          <div class="card-header">每日卡路里摄入</div>
        </template>
        <div class="weekly-chart">
          <div v-for="item in calorieData.weekly" :key="item.day" class="weekly-bar stagger-item">
            <!-- 与营养摄入比例相同的信息布局：周几和卡路里值同行 -->
            <div class="weekly-info">
              <div class="bar-label">{{ item.day }}</div>
              <div class="bar-text">{{ item.consumed }} kcal</div>
            </div>

            <!-- 进度条容器 -->
            <div class="bar-container">
              <!-- 完全匹配营养摄入比例的进度条实现 -->
              <el-progress
                :percentage="Math.min(Math.round(getCaloriePercentage(item.consumed)), 100)"
                :color="getCalorieColor(getCaloriePercentage(item.consumed))"
                :stroke-width="21"
                :text-inside="true"
                :class="[
                  { 'extreme-progress': getCaloriePercentage(item.consumed) > 200 },
                  'calorie-progress',
                  {
                    'zero-progress':
                      Math.min(Math.round(getCaloriePercentage(item.consumed)), 100) === 0
                  }
                ]"
              >
                <span>
                  {{
                    getCaloriePercentage(item.consumed) === 0
                      ? '0%'
                      : getCaloriePercentage(item.consumed) > 200
                        ? '严重超出'
                        : Math.round(getCaloriePercentage(item.consumed)) + '%'
                  }}
                </span>
              </el-progress>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped lang="less">
.calorie-container {
  padding: 24px;
  min-height: 100vh;
  background: #f5f7fa;

  h2 {
    font-size: 2rem /* 原值: 28px */;
    margin: 0 0 32px 0;
    text-align: center;
    color: #333;
    font-weight: 700;
  }

  .today-overview {
    display: flex;
    gap: 24px;
    margin-bottom: 32px;
    flex-wrap: wrap;

    .overview-card {
      flex: 1;
      min-width: 200px;
      background: rgba(255, 255, 255, 0.95) !important;
      border-radius: 16px !important;
      padding: 24px !important;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
      }

      .overview-item {
        text-align: center;

        .overview-label {
          font-size: 1rem /* 原值: 14px */;
          color: #666;
          margin-bottom: 12px;
          text-transform: uppercase;
          letter-spacing: 1px;
        }

        .overview-value {
          font-size: 2.286rem /* 原值: 32px */;
          font-weight: 700;
          background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;

          &.remaining {
            background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
          }

          &.target {
            background: linear-gradient(135deg, #2196f3 0%, #0b7dda 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
          }
        }
      }
    }
  }

  .nutrition-card {
    margin-bottom: 32px;
    background: rgba(255, 255, 255, 0.95) !important;
    border-radius: 16px !important;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    transform: translateY(0);

    &:hover {
      transform: translateY(-8px);
      box-shadow: 0 20px 48px rgba(0, 0, 0, 0.15);
    }

    .card-header {
      font-size: 1.286rem /* 原值: 18px */;
      font-weight: 700;
      color: #333;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .nutrition-chart {
      padding: 28px;
    }

    .nutrition-item {
      margin-bottom: 28px;

      &:last-child {
        margin-bottom: 0;
      }

      .nutrition-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 12px;
        font-size: 1rem /* 原值: 14px */;
        font-weight: 600;
      }

      // 极端值样式 - 简化，移除卡片效果
      &.extreme-value {
        // 只有文字颜色变化
        .nutrition-value {
          color: #ff6b6b !important; // 红色数值
        }
      }

      // 推荐目标值样式
      .recommended-goal {
        font-size: 0.857rem /* 原值: 12px */; // 小号字体
        color: #999; // 灰色文字
        margin-left: 6px; // 与实际值保持间距
      }

      // 严重超出的进度条样式
      :deep(.extreme-progress) {
        .el-progress__text {
          color: #ff6b6b !important; // 红色文本
          font-weight: 700 !important; // 加粗
          font-size: 1.143rem /* 原值: 16px */ !important; // 增大字号
        }

        // 进度条光晕效果
        .el-progress-bar {
          filter: drop-shadow(0 0 12px rgba(255, 107, 107, 0.6));
        }
      }
    }
  }

  /* Section styles */
  .today-overview-section,
  .weekly-statistics-section {
    margin-bottom: 32px;
  }

  .today-overview-section h3,
  .weekly-statistics-section h3 {
    font-size: 22px;
    font-weight: 700;
    margin: 0 0 24px 0;
    color: #333;
  }

  // 周统计概览样式
  .weekly-overview {
    display: flex;
    gap: 24px;
    margin-bottom: 32px;
    flex-wrap: wrap;

    .overview-card {
      flex: 1;
      min-width: 200px;
      background: rgba(255, 255, 255, 0.95) !important;
      border-radius: 16px !important;
      padding: 24px !important;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
      }

      .overview-item {
        text-align: center;

        .overview-label {
          font-size: 1rem /* 原值: 14px */;
          color: #666;
          margin-bottom: 12px;
          text-transform: uppercase;
          letter-spacing: 1px;
        }

        .overview-value {
          font-size: 2.286rem /* 原值: 32px */;
          font-weight: 700;
          background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;

          &.total {
            background: linear-gradient(135deg, #2196f3 0%, #0b7dda 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
          }

          &.average {
            background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
          }
        }
      }
    }
  }

  .weekly-card {
    background: rgba(255, 255, 255, 0.95) !important;
    border-radius: 16px !important;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    transform: translateY(0);

    &:hover {
      transform: translateY(-8px);
      box-shadow: 0 20px 48px rgba(0, 0, 0, 0.15);
    }

    .card-header {
      font-size: 1.286rem /* 原值: 18px */;
      font-weight: 700;
      color: #333;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .weekly-chart {
      padding: 32px;
    }

    .weekly-bar {
      margin-bottom: 32px;
      position: relative;

      &:last-child {
        margin-bottom: 0;
      }

      // 与营养摄入比例相同的信息布局
      .weekly-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 12px;
        font-size: 1rem /* 原值: 14px */;
        font-weight: 600;

        .bar-label {
          color: #555;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }

        .bar-text {
          font-weight: 700;
          font-size: 1rem /* 原值: 14px */;
          color: #333;
        }
      }

      .bar-container {
        width: 100%;
        height: 21px; /* 与 stroke-width 保持一致 */
        background-color: #f0f0f0;
        border-radius: 11px; /* 保持与高度的比例 */
        box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.08);
        position: relative;
      }

      // 目标线指示器已移除
      .bar-target-line {
        display: none;
      }
    }
  }
}

// 进度条样式 - 所有进度条通用
:deep(.el-progress) {
  --el-progress-transition-duration: 3s; // 使用CSS变量控制动画持续时间
  transition: transform 0.3s ease; // 添加容器过渡动画
  transform-origin: center center; // 设置变换原点为中心

  .el-progress-bar__inner {
    transition: width var(--el-progress-transition-duration) cubic-bezier(0.25, 0.8, 0.25, 1);
    min-width: 30px; /* 为0%进度条设置最小宽度，确保文本可见 */
    border-radius: 10.5px 0 0 10.5px !important; /* 保持进度条与容器的圆角一致 */
  }

  .el-progress__text {
    transform-origin: center center; // 设置文本变换原点为中心，确保缩放时居中
  }

  // 进度条容器悬浮时等比例放大
  &:hover {
    transform: scale(1.05); // 等比例缩放，1.05倍大小
  }

  // 进度条容器默认无光晕，仅悬浮时显示
  .el-progress-bar {
    filter: none;
    transition: filter 0.4s cubic-bezier(0.25, 0.8, 0.25, 1); // 添加光晕过渡动画
  }

  // 蛋白质进度条 - 蓝色光晕
  &.蛋白质-progress:hover .el-progress-bar {
    filter: drop-shadow(0 0 18px rgba(33, 150, 243, 0.6));
  }

  // 碳水化合物进度条 - 绿色光晕
  &.碳水化合物-progress:hover .el-progress-bar {
    filter: drop-shadow(0 0 18px rgba(76, 175, 80, 0.6));
  }

  // 脂肪进度条 - 橙色光晕
  &.脂肪-progress:hover .el-progress-bar {
    filter: drop-shadow(0 0 18px rgba(255, 152, 0, 0.6));
  }

  // 卡路里进度条 - 蓝色光晕
  &.calorie-progress:hover .el-progress-bar {
    filter: drop-shadow(0 0 18px rgba(33, 150, 243, 0.6));
  }

  // 0%进度条样式优化
  &.zero-progress {
    .el-progress__text {
      color: #666; /* 更醒目的文本颜色 */
      font-weight: 600; /* 加粗文本 */
    }

    .el-progress-bar__inner {
      background-color: #e5e7eb; /* 浅灰色背景，增强对比度 */
    }
  }

  // 严重超出进度条的基础样式 - 默认小光晕
  &.extreme-progress {
    .el-progress-bar {
      filter: drop-shadow(0 0 8px rgba(255, 107, 107, 0.4)); // 默认小光晕
    }
  }

  // 针对各种类型的严重超出进度条，设置更具特异性的hover效果
  &.蛋白质-progress.extreme-progress:hover .el-progress-bar {
    filter: drop-shadow(0 0 20px rgba(255, 107, 107, 0.8));
  }

  &.碳水化合物-progress.extreme-progress:hover .el-progress-bar {
    filter: drop-shadow(0 0 20px rgba(255, 107, 107, 0.8));
  }

  &.脂肪-progress.extreme-progress:hover .el-progress-bar {
    filter: drop-shadow(0 0 20px rgba(255, 107, 107, 0.8));
  }

  // 严重超出的卡路里进度条，设置hover效果
  &.calorie-progress.extreme-progress:hover .el-progress-bar {
    filter: drop-shadow(0 0 20px rgba(255, 107, 107, 0.8));
  }
}

// 饮食健康建议样式
.nutrition-chart .health-advice-section {
  margin-top: 20px; /* 减小上边距 */
  border-top: 1px solid #f0f0f0;
  padding-top: 16px; /* 减小内边距 */
}

.health-advice-section .advice-header {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 1.143rem /* 原值: 16px */;
  font-weight: 600;
  color: #333;
  transition: all 0.3s ease;
  padding: 12px 0;

  &:hover {
    color: #409eff; /* 更柔和的蓝色 */
  }
}

.advice-header .arrow-icon {
  transition: transform 0.3s ease;

  &.rotate {
    transform: rotate(180deg);
  }
}

.nutrition-chart .health-advice-section .advice-content {
  margin-top: 16px;
  padding-left: 28px;
  max-height: 0;
  overflow: hidden;
  opacity: 0;
  transition:
    max-height 0.4s cubic-bezier(0.25, 0.8, 0.25, 1),
    opacity 0.3s ease;

  &.show {
    max-height: 300px; /* 足够大的高度容纳内容 */
    opacity: 1;
    animation: fadeIn 0.3s ease;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.advice-content .advice-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 14px;
  font-size: 1rem /* 原值: 14px */;
  color: #666;
  line-height: 22px;

  &:last-child {
    margin-bottom: 0;
  }
}

.advice-item .advice-icon {
  color: #ff9800;
  margin-top: 2px;
  flex-shrink: 0;
}

.advice-content .advice-empty {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1rem /* 原值: 14px */;
  color: #4caf50;
  padding: 16px;
  background-color: #f6fff6;
  border-radius: 8px;
  margin-top: 8px;
}

.advice-empty .empty-icon {
  font-size: 1.286rem /* 原值: 18px */;
  flex-shrink: 0;
}
</style>
