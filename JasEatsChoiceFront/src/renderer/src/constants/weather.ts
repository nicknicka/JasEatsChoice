/**
 * 天气推荐规则常量
 */

export const WEATHER_RULES = {
  // 高温天气
  HOT: {
    TEMP_THRESHOLD: 28,
    SERIES: '冰饮/凉菜系列',
    CONDITIONS: ['晴']
  },

  // 低温天气
  COLD: {
    TEMP_THRESHOLD: 15,
    SERIES: '热食/火锅系列',
    CONDITIONS: ['雪']
  },

  // 雨天
  RAINY: {
    SERIES: '汤品/暖食系列',
    CONDITIONS: ['雨', '雷']
  },

  // 多云阴天
  CLOUDY: {
    SERIES: '均衡饮食系列',
    CONDITIONS: ['云', '阴']
  },

  // 默认推荐
  DEFAULT: '特色菜品系列'
} as const

/**
 * 根据天气条件获取推荐菜品系列
 */
export function getRecommendedDishesSeries(condition: string, temp: number): string {
  if (!condition) return WEATHER_RULES.DEFAULT

  // 高温天气推荐
  if (
    temp > WEATHER_RULES.HOT.TEMP_THRESHOLD ||
    WEATHER_RULES.HOT.CONDITIONS.some((c) => condition.includes(c))
  ) {
    return WEATHER_RULES.HOT.SERIES
  }

  // 低温天气推荐
  if (
    temp < WEATHER_RULES.COLD.TEMP_THRESHOLD ||
    WEATHER_RULES.COLD.CONDITIONS.some((c) => condition.includes(c))
  ) {
    return WEATHER_RULES.COLD.SERIES
  }

  // 雨天推荐
  if (WEATHER_RULES.RAINY.CONDITIONS.some((c) => condition.includes(c))) {
    return WEATHER_RULES.RAINY.SERIES
  }

  // 多云阴天推荐
  if (WEATHER_RULES.CLOUDY.CONDITIONS.some((c) => condition.includes(c))) {
    return WEATHER_RULES.CLOUDY.SERIES
  }

  // 默认推荐
  return WEATHER_RULES.DEFAULT
}
