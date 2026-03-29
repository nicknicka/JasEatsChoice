/**
 * 分类图标映射配置
 * 将后端返回的分类名称映射为图标和代码
 */

export const CATEGORY_ICON_MAP = {
  // 中式快餐
  '中式快餐': { icon: '🍚', code: 'chinese_fast_food' },
  '中餐': { icon: '🍚', code: 'chinese' },
  '家常菜': { icon: '🥘', code: 'home_cooking' },

  // 火锅
  '火锅': { icon: '🍲', code: 'hotpot' },

  // 烧烤
  '烧烤': { icon: '🍢', code: 'bbq' },
  '烤肉': { icon: '🥩', code: 'roast' },

  // 川菜
  '川菜': { icon: '🌶️', code: 'sichuan' },
  '麻辣烫': { icon: '🍜', code: 'spicy' },
  '串串香': { icon: '🍢', code: 'skewer' },
  '麻辣香锅': { icon: '🥘', code: 'spicy_pot' },

  // 湘菜
  '湘菜': { icon: '🌶️', code: 'hunan' },

  // 粤菜
  '粤菜': { icon: '🥟', code: 'cantonese' },
  '早茶': { icon: '🍵', code: 'morning_tea' },

  // 西餐
  '西餐': { icon: '🍔', code: 'western' },
  '汉堡': { icon: '🍔', code: 'burger' },
  '披萨': { icon: '🍕', code: 'pizza' },
  '牛排': { icon: '🥩', code: 'steak' },
  '意大利菜': { icon: '🍝', code: 'italian' },
  '法国菜': { icon: '🥐', code: 'french' },
  '美式快餐': { icon: '🍟', code: 'american' },
  '三明治': { icon: '🥪', code: 'sandwich' },
  '沙拉': { icon: '🥗', code: 'salad' },

  // 日韩料理
  '日韩料理': { icon: '🍣', code: 'japanese_korean' },
  '日本料理': { icon: '🍣', code: 'japanese' },
  '寿司': { icon: '🍣', code: 'sushi' },
  '刺身': { icon: '🍱', code: 'sashimi' },
  '拉面': { icon: '🍜', code: 'ramen' },
  '韩国料理': { icon: '🍜', code: 'korean' },
  '韩料': { icon: '🍜', code: 'korean' },
  '炸鸡': { icon: '🍗', code: 'fried_chicken' },

  // 东北菜
  '东北菜': { icon: '🌽', code: 'northeast' },

  // 西北菜
  '西北菜': { icon: '🍜', code: 'northwest' },

  // 云南菜
  '云南菜': { icon: '🍄', code: 'yunnan' },

  // 贵州菜
  '贵州菜': { icon: '🌶️', code: 'guizhou' },

  // 地方菜系
  '鲁菜': { icon: '🥘', code: 'shandong' },
  '苏菜': { icon: '🦆', code: 'jiangsu' },
  '浙菜': { icon: '🐟', code: 'zhejiang' },
  '闽菜': { icon: '🦐', code: 'fujian' },
  '徽菜': { icon: '🐔', code: 'anhui' },
  '楚菜': { icon: '🐟', code: 'hubei' },
  '本帮菜': { icon: '🥘', code: 'shanghai' },
  '客家菜': { icon: '🥟', code: 'hakka' },
  '潮汕菜': { icon: '🍜', code: 'chaoshan' },
  '新疆菜': { icon: '🍖', code: 'xinjiang' },
  '藏菜': { icon: '🥩', code: 'tibetan' },
  '清真菜': { icon: '🍖', code: 'halal' },

  // 东南亚菜
  '东南亚菜': { icon: '🍛', code: 'southeast_asian' },
  '泰国菜': { icon: '🍛', code: 'thai' },

  // 小吃快餐
  '小吃快餐': { icon: '🍢', code: 'snack' },
  '快餐': { icon: '🍟', code: 'fast_food' },
  '早餐': { icon: '🥐', code: 'breakfast' },
  '夜宵': { icon: '🌙', code: 'night_snack' },
  '大排档': { icon: '🍢', code: 'food_stall' },

  // 饮品甜点
  '饮品甜点': { icon: '🧁', code: 'drink_dessert' },
  '甜点': { icon: '🍰', code: 'dessert' },
  '饮品': { icon: '🥤', code: 'drink' },
  '咖啡': { icon: '☕', code: 'coffee' },
  '奶茶': { icon: '🧋', code: 'milk_tea' },
  '果汁': { icon: '🧃', code: 'juice' },
  '冰淇淋': { icon: '🍦', code: 'ice_cream' },
  '蛋糕': { icon: '🎂', code: 'cake' },
  '面包甜点': { icon: '🍞', code: 'bakery' },
  '茶餐厅': { icon: '🍵', code: 'tea_restaurant' },

  // 特殊菜品
  '酸菜鱼': { icon: '🐟', code: 'sauerkraut_fish' },
  '烤鱼': { icon: '🐟', code: 'grilled_fish' },
  '小龙虾': { icon: '🦞', code: 'crayfish' },
  '铁板烧': { icon: '🥘', code: 'teppanyaki' },
  '涮涮锅': { icon: '🍲', code: 'shabu_shabu' },
  '冒菜': { icon: '🍲', code: 'maocai' }
}

/**
 * 根据分类名称获取图标和代码
 * @param {string} categoryName - 分类名称
 * @returns {object} { icon, code }
 */
export function getCategoryIcon(categoryName) {
  return CATEGORY_ICON_MAP[categoryName] || { icon: '🍽️', code: 'other' }
}

/**
 * 将分类名称列表转换为分类对象列表
 * @param {array} categoryNames - 分类名称数组
 * @returns {array} 分类对象数组
 */
export function normalizeCategories(categoryNames) {
  if (!Array.isArray(categoryNames)) return []

  return categoryNames.map((name, index) => ({
    id: index + 1,
    name,
    ...getCategoryIcon(name)
  }))
}
