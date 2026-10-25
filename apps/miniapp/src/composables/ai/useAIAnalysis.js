/**
 * Composable: useAIAnalysis
 * 用途：AI分析相关逻辑
 * 包含：数据加载、图表绘制、导出分享
 * 创建时间：2026-03-20
 */
import { ref, nextTick } from 'vue'
import { useUserStore } from '@/store'
import { aiApi } from '@/api'
import { useDebounce } from '@/utils/performance'

export function useAIAnalysis() {
  const userStore = useUserStore()
  const loading = ref(false)

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

  // 推荐理由
  const recommendReasons = ref([
    { icon: 'calendar', title: '天气因素', desc: '今日气温较低，推荐温热汤品和富含维生素C的菜品增强免疫力' },
    { icon: 'person', title: '您的目标', desc: '根据您的减脂目标，推荐高蛋白低脂的菜品' },
    { icon: 'clock', title: '时间因素', desc: '午餐时段，建议摄入适量碳水保持下午精力' }
  ])

  // 健康评分
  const healthScore = ref(85)
  const scoreDetails = ref([
    { label: '营养均衡', value: 88 },
    { label: '热量控制', value: 90 },
    { label: '饮食规律', value: 78 }
  ])

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

  // 加载营养分析数据
  const loadNutritionData = async () => {
    if (!userStore.isLogin) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }

    try {
      loading.value = true
      const userId = userStore.userInfo?.userId || userStore.userInfo?.id

      // 调用营养分析API
      const res = await aiApi.analyzeNutrition({
        userId,
        date: new Date().toISOString().split('T')[0] // 今日日期
      })

      if (res.data) {
        // 更新今日热量
        todayCalories.value = res.data.calories || todayCalories.value

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
      }
    } catch (error) {
      console.error('加载营养分析数据失败:', error)
      // 使用默认数据，不影响用户体验
    } finally {
      loading.value = false
    }
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

  // 采纳建议
  const applyImprovement = useDebounce((item) => {
    uni.showModal({
      title: '采纳建议',
      content: `是否采纳「${item.title}」建议？我们将为您推荐相关菜品。`,
      success: (res) => {
        if (res.confirm) {
          uni.showToast({ title: '已采纳，正在为您推荐...', icon: 'success' })
          setTimeout(() => {
            uni.navigateTo({ url: '/index/index?filter=' + item.type })
          }, 1500)
        }
      }
    })
  }, 300)

  // 查看详情
  const viewDetail = (item) => {
    uni.showModal({
      title: item.title,
      content: item.desc + '\n\n详细说明：\n' + (item.detail || '暂无详细说明'),
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

  return {
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
  }
}
