/**
 * 节日和节气特色菜品映射
 */

export const FESTIVAL_DISHES = {
  // 节气
  立春: ['春卷', '春饼', '菠菜汤'],
  雨水: ['南瓜粥', '豆苗炒鸡蛋', '清蒸鲈鱼'],
  惊蛰: ['春笋', '韭菜炒鸡蛋', '山药排骨汤'],
  春分: ['春茶', '青团', '马兰头拌香干'],
  清明: ['清明粿', '青团', '乌米饭'],
  谷雨: ['谷雨茶', '香椿炒蛋', '鲫鱼炖豆腐'],
  立夏: ['立夏饭', '乌米饭', '咸鸭蛋'],
  小满: ['苦菜', '苦瓜炒鸡蛋', '绿豆汤'],
  芒种: ['青梅酒', '芒果布丁', '凉拌黄瓜'],
  夏至: ['夏至面', '绿豆汤', '西瓜'],
  小暑: ['凉面', '冰淇淋', '薄荷茶'],
  大暑: ['大暑羊', '凉茶', '西瓜'],
  立秋: ['贴秋膘', '红烧肉', '炖鸡'],
  处暑: ['老鸭汤', '莲藕排骨汤', '炒菱角'],
  白露: ['白露茶', '桂圆莲子粥', '烤鸭'],
  秋分: ['秋分蟹', '大闸蟹', '葡萄'],
  寒露: ['菊花酒', '芝麻糕', '银耳羹'],
  霜降: ['柿子', '牛肉火锅', '萝卜汤'],
  立冬: ['立冬饺', '羊肉汤', '板栗'],
  小雪: ['腊味', '火锅', '热奶茶'],
  大雪: ['腌肉', '羊肉火锅', '红薯粥'],
  冬至: ['冬至饺', '汤圆', '羊肉汤'],
  小寒: ['腊八粥', '炖羊肉', '热可可'],
  大寒: ['大寒粥', '涮羊肉', '暖锅'],

  // 节日
  春节: ['饺子', '年糕', '年夜饭'],
  元宵: ['元宵', '汤圆', '花灯'],
  端午: ['粽子', '雄黄酒', '咸鸭蛋'],
  中秋: ['月饼', '柚子', '螃蟹'],
  重阳: ['重阳糕', '菊花酒', '登高'],
  腊八: ['腊八粥', '腊八蒜', '腊八豆腐']
}

/**
 * 节气日期映射（简化版）
 */
export const SOLAR_TERMS = [
  { name: '小寒', month: 1, day: 5 },
  { name: '大寒', month: 1, day: 20 },
  { name: '立春', month: 2, day: 4 },
  { name: '雨水', month: 2, day: 19 },
  { name: '惊蛰', month: 3, day: 5 },
  { name: '春分', month: 3, day: 20 },
  { name: '清明', month: 4, day: 4 },
  { name: '谷雨', month: 4, day: 19 },
  { name: '立夏', month: 5, day: 5 },
  { name: '小满', month: 5, day: 20 },
  { name: '芒种', month: 6, day: 5 },
  { name: '夏至', month: 6, day: 21 },
  { name: '小暑', month: 7, day: 7 },
  { name: '大暑', month: 7, day: 22 },
  { name: '立秋', month: 8, day: 7 },
  { name: '处暑', month: 8, day: 23 },
  { name: '白露', month: 9, day: 7 },
  { name: '秋分', month: 9, day: 23 },
  { name: '寒露', month: 10, day: 8 },
  { name: '霜降', month: 10, day: 23 },
  { name: '立冬', month: 11, day: 7 },
  { name: '小雪', month: 11, day: 22 },
  { name: '大雪', month: 12, day: 7 },
  { name: '冬至', month: 12, day: 21 }
]

/**
 * 节日菜品卡路里估算（每100克/份）
 */
export const FESTIVAL_DISH_CALORIES = {
  // 主食类
  '饺子': 250,
  '春卷': 320,
  '春饼': 280,
  '面条': 200,
  '米饭': 130,
  '年糕': 180,
  '汤圆': 150,
  '元宵': 150,
  '粽子': 200,
  '腊八粥': 120,
  '乌米饭': 160,
  '立夏饭': 180,

  // 汤羹类
  '菠菜汤': 40,
  '山药排骨汤': 280,
  '鲫鱼炖豆腐': 180,
  '南瓜粥': 80,
  '红豆薏米粥': 90,
  '老鸭汤': 320,
  '莲藕排骨汤': 260,
  '银耳羹': 100,
  '羊肉汤': 350,
  '萝卜汤': 60,
  '牛肉火锅': 450,
  '涮羊肉': 420,

  // 菜肴类
  '豆苗炒鸡蛋': 180,
  '清蒸鲈鱼': 160,
  '韭菜炒鸡蛋': 200,
  '马兰头拌香干': 120,
  '香椿炒蛋': 190,
  '苦瓜炒鸡蛋': 140,
  '凉拌黄瓜': 50,
  '红烧肉': 480,
  '炖鸡': 280,
  '炒菱角': 150,
  '烤鸭': 380,
  '大闸蟹': 140,
  '腊味': 320,
  '腌肉': 350,
  '炖羊肉': 420,
  '贴秋膘': 450,
  '大寒粥': 110,
  '大暑羊': 420,
  '暖锅': 380,

  // 茶饮类
  '春茶': 5,
  '谷雨茶': 5,
  '白露茶': 5,
  '菊花茶': 3,
  '凉茶': 8,
  '热奶茶': 120,
  '薄荷茶': 5,
  '青梅酒': 150,
  '雄黄酒': 180,
  '菊花酒': 160,

  // 甜点水果类
  '青团': 180,
  '乌米饭': 160,
  '咸鸭蛋': 180,
  '绿豆汤': 60,
  '西瓜': 30,
  '凉面': 160,
  '冰淇淋': 220,
  '芒果布丁': 160,
  '月饼': 420,
  '柚子': 42,
  '重阳糕': 280,
  '葡萄': 69,
  '柿子': 82,
  '芝麻糕': 350,
  '腊八蒜': 90,
  '腊八豆腐': 140,
  '年夜饭': 500,
  '花灯': 0, // 不是食物
  '板栗': 200
}

/**
 * 根据菜品名称获取估算卡路里
 */
export const getDishCalories = (dishName) => {
  // 遍历映射表，查找包含该菜品名称的条目
  for (const [name, calories] of Object.entries(FESTIVAL_DISH_CALORIES)) {
    if (dishName.includes(name)) {
      return calories
    }
  }
  // 如果找不到，返回一个默认值
  return 200 // 默认中等卡路里
}

/**
 * 根据菜品名称获取图标
 */
export const getDishIcon = (dishName) => {
  if (dishName.includes('饺子') || dishName.includes('饺')) return '🥟'
  if (dishName.includes('粽子') || dishName.includes('粽')) return '🍙'
  if (dishName.includes('月饼') || dishName.includes('饼')) return '🥮'
  if (dishName.includes('汤') || dishName.includes('羹')) return '🍵'
  if (dishName.includes('茶')) return '🍵'
  if (dishName.includes('面') || dishName.includes('面条')) return '🍜'
  if (dishName.includes('饭') || dishName.includes('粥')) return '🍚'
  if (dishName.includes('火锅')) return '🍲'
  if (dishName.includes('蟹') || dishName.includes('螃蟹')) return '🦀'
  if (dishName.includes('西瓜')) return '🍉'
  if (dishName.includes('鸭蛋') || dishName.includes('蛋')) return '🥚'
  return '🍱'
}

/**
 * 判断当前日期是否为节日
 */
export const getCurrentFestival = () => {
  const now = new Date()
  const month = now.getMonth() + 1
  const day = now.getDate()

  // 检查节日
  if (month === 1 && day === 1) return '春节'
  if (month === 1 && day >= 15) return '元宵'
  if (month === 5 && day === 5) return '端午'
  if (month === 8 && day === 15) return '中秋'
  if (month === 9 && day === 9) return '重阳'
  if (month === 12 && day === 8) return '腊八'

  // 检查节气
  for (const term of SOLAR_TERMS) {
    if (term.month === month && term.day === day) {
      return term.name
    }
  }

  return null
}
