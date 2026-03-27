/**
 * 快速问题配置
 * 用于AI聊天界面的快捷问题提示
 */

/**
 * 默认展开的分类ID
 */
export const DEFAULT_EXPANDED_CATEGORY = 'dish-exploration'

/**
 * 快速问题分类配置
 */
export const QUICK_QUESTION_CATEGORIES = [
  {
    id: 'dish-exploration',
    title: '🍽️ 菜品探索',
    icon: 'dish',
    description: '搜索和发现各种美食',
    expanded: false, // 将在使用时动态设置
    questions: [
      '帮我搜索一些主食菜品',
      '有什么推荐的甜点吗',
      '搜索包含鸡肉的菜肴',
      '查看汤品分类的菜品'
    ]
  },
  {
    id: 'nutrition-analysis',
    title: '📊 营养分析',
    icon: 'chart',
    description: '了解菜品的营养成分和热量',
    expanded: false,
    questions: [
      '分析西红柿炒鸡蛋的营养成分',
      '宫保鸡丁的热量是多少',
      '这份菜的蛋白质含量高吗',
      '分析这碗米饭的营养价值'
    ]
  },
  {
    id: 'order-management',
    title: '🛒 订单管理',
    icon: 'cart',
    description: '创建和管理您的订单',
    expanded: false,
    questions: [
      '我要下单宫保鸡丁和红烧肉',
      '查询我的订单状态',
      '创建一个新订单',
      '我的订单准备好了吗'
    ]
  },
  {
    id: 'personalized-recommendation',
    title: '👤 个性化推荐',
    icon: 'user',
    description: '根据您的喜好获取推荐',
    expanded: false,
    questions: [
      '根据我的喜好推荐菜品',
      '查看我的饮食偏好',
      '我最近都点了什么菜',
      '有什么适合我的健康菜品推荐'
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
