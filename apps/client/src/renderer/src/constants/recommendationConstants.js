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

// 卡路里范围定义
export const CALORIE_RANGES = [
  { id: 1, label: '低卡路里 (0-200)', min: 0, max: 200 },
  { id: 2, label: '中卡路里 (200-400)', min: 200, max: 400 },
  { id: 3, label: '高卡路里 (400+)', min: 400, max: Infinity }
]

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
