/**
 * 快速问题配置
 * 用于AI聊天界面的快捷问题提示
 */

/**
 * 默认展开的分类ID
 */
export const DEFAULT_EXPANDED_CATEGORY = 'recommendation'

/**
 * 快速问题分类配置
 */
export const QUICK_QUESTION_CATEGORIES = [
  {
    id: 'recommendation',
    title: '饮食推荐',
    description: '想不到吃什么时，先从这里开始',
    accent: '#D4845A',
    questions: [
      '给我推荐一份适合减脂的午餐',
      '今天晚餐吃什么更清爽',
      '推荐几道高蛋白又好做的菜'
    ]
  },
  {
    id: 'nutrition',
    title: '营养分析',
    description: '快速看热量、蛋白质和搭配是否合理',
    accent: '#7BAE7F',
    questions: [
      '帮我分析番茄炒蛋的热量和营养',
      '这份餐的蛋白质够不够',
      '我今天的饮食有没有太油腻'
    ]
  },
  {
    id: 'plan',
    title: '饮食规划',
    description: '适合给自己做一份更完整的安排',
    accent: '#8E7CC3',
    questions: [
      '给我做一份一周健康饮食计划',
      '帮我安排今天三餐的搭配',
      '运动后适合吃什么'
    ]
  },
  {
    id: 'personal',
    title: '个性化建议',
    description: '结合你的偏好和目标做推荐',
    accent: '#E2B455',
    questions: [
      '根据我的口味推荐几道菜',
      '适合控糖的外卖怎么点',
      '推荐几种低卡但有饱腹感的食物'
    ]
  }
]

/**
 * 根据配置初始化快速问题分类
 * @param {string} defaultExpandedId - 默认展开的分类ID
 * @returns {Array} 初始化后的分类数组
 */
export const initializeQuickQuestions = (defaultExpandedId = DEFAULT_EXPANDED_CATEGORY) => {
  return QUICK_QUESTION_CATEGORIES.map((category) => ({
    ...category,
    expanded: category.id === defaultExpandedId
  }))
}

/**
 * 获取指定分类的问题列表
 * @param {string} categoryId - 分类ID
 * @returns {Array|null} 问题列表，如果分类不存在则返回null
 */
export const getQuestionsByCategory = (categoryId) => {
  const category = QUICK_QUESTION_CATEGORIES.find((cat) => cat.id === categoryId)
  return category ? category.questions : null
}

/**
 * 获取所有问题（扁平化）
 * @returns {Array} 所有问题对象，包含categoryId
 */
export const getAllQuestions = () => {
  const allQuestions = []
  QUICK_QUESTION_CATEGORIES.forEach((category) => {
    category.questions.forEach((question) => {
      allQuestions.push({
        question,
        categoryId: category.id,
        categoryTitle: category.title
      })
    })
  })
  return allQuestions
}
