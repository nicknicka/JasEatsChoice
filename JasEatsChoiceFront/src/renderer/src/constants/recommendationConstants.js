/**
 * 推荐系统相关常量
 */

// 标签类型
export const TAG_TYPES = ['primary', 'success', 'warning', 'info', 'danger']

// 推荐来源类型
export const RECOMMENDATION_TYPES = {
  PERSONALIZED: '个性化推荐', // 基于用户偏好
  WEATHER: '天气推荐', // 基于天气
  TIME: '时间推荐', // 基于餐食时间
  FESTIVAL: '节日特供' // 节日/节气特色
}

// 推荐来源对应的标签类型
export const RECOMMENDATION_TYPE_TAGS = {
  个性化推荐: { type: 'primary', color: '#409EFF' },
  天气推荐: { type: 'success', color: '#67C23A' },
  时间推荐: { type: 'warning', color: '#E6A23C' },
  节日特供: { type: 'danger', color: '#F56C6C' }
}

// 时间段定义
export const TIME_PERIODS = {
  BREAKFAST: { name: '早餐', hours: [6, 7, 8, 9] },
  LUNCH: { name: '午餐', hours: [10, 11, 12, 13] },
  AFTERNOON_TEA: { name: '下午茶', hours: [14, 15, 16, 17] },
  DINNER: { name: '晚餐', hours: [18, 19, 20, 21] },
  LATE_NIGHT: { name: '夜宵', hours: [22, 23, 0, 1, 2, 3, 4, 5] }
}

// 天气标签映射
export const WEATHER_TAGS = {
  HOT: { condition: (temp) => temp > 30, tags: ['冰饮', '凉菜', '轻食'] },
  COLD: { condition: (temp) => temp < 10, tags: ['热饮', '热菜', '火锅'] },
  HUMID: { condition: (_, humidity) => humidity > 80, tags: ['祛湿粥品', '清淡饮食'] }
}

// 卡路里范围定义
export const CALORIE_RANGES = [
  { label: '低卡路里 (0-200)', min: 0, max: 200 },
  { label: '中卡路里 (200-400)', min: 200, max: 400 },
  { label: '高卡路里 (400+)', min: 400, max: Infinity }
]

// 模拟菜品数据库
export const MOCK_DISHES = [
  {
    name: '冰爽柠檬水',
    type: '冰饮',
    calories: 50,
    tags: ['冰饮', '夏季', '解渴'],
    nutrition: { carbs: 12, protein: 0, fat: 0 }
  },
  {
    name: '凉拌黄瓜',
    type: '凉菜',
    calories: 80,
    tags: ['凉菜', '夏季', '清爽'],
    nutrition: { carbs: 8, protein: 2, fat: 5 }
  },
  {
    name: '鸡肉沙拉',
    type: '轻食',
    calories: 350,
    tags: ['轻食', '健康', '低卡'],
    nutrition: { carbs: 15, protein: 40, fat: 12 }
  },
  {
    name: '南瓜粥',
    type: '热饮',
    calories: 120,
    tags: ['热饮', '早餐', '营养'],
    nutrition: { carbs: 25, protein: 3, fat: 1 }
  },
  {
    name: '红烧肉',
    type: '热菜',
    calories: 450,
    tags: ['热菜', '晚餐', '贴秋膘'],
    nutrition: { carbs: 10, protein: 25, fat: 35 }
  },
  {
    name: '羊肉火锅',
    type: '火锅',
    calories: 600,
    tags: ['火锅', '冬季', '暖胃'],
    nutrition: { carbs: 15, protein: 35, fat: 45 }
  },
  {
    name: '红豆薏米粥',
    type: '祛湿粥品',
    calories: 150,
    tags: ['祛湿粥品', '养生', '清淡'],
    nutrition: { carbs: 30, protein: 5, fat: 1 }
  },
  {
    name: '菊花茶',
    type: '热饮',
    calories: 30,
    tags: ['热饮', '下午茶', '清火'],
    nutrition: { carbs: 6, protein: 0, fat: 0 }
  }
]

// 拒绝次数阈值
export const REJECTION_THRESHOLD = 2

// 推荐多样性配置
export const DIVERSITY_CONFIG = {
  minPerType: 1, // 每种类型至少推荐一个
  maxPerType: 5, // 每种类型最多推荐五个
  totalMax: 20 // 总推荐数上限
}

// 获取随机标签类型
export const getRandomTagType = () => {
  return TAG_TYPES[Math.floor(Math.random() * TAG_TYPES.length)]
}

// 获取当前时间段
export const getCurrentTimePeriod = () => {
  const hour = new Date().getHours()

  if (hour >= 6 && hour < 10) return TIME_PERIODS.BREAKFAST.name
  if (hour >= 10 && hour < 14) return TIME_PERIODS.LUNCH.name
  if (hour >= 14 && hour < 18) return TIME_PERIODS.AFTERNOON_TEA.name
  if (hour >= 18 && hour < 22) return TIME_PERIODS.DINNER.name
  return TIME_PERIODS.LATE_NIGHT.name
}
