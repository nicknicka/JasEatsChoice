// 后端API配置
export const API_CONFIG = {
  // 基础URL
  baseURL: 'http://localhost:8080/api',

  // AI助手API端点
  ai: {
    chat: '/v1/ai/stream/chat', // AI聊天接口（流式传输）
    chatLegacy: '/v1/ai/chat', // AI聊天接口（普通接口，备用）
    recipe: '/v1/ai/recipe', // 食谱推荐接口
    nutrient: '/v1/ai/nutrient', // 营养分析接口
    recognizeDish: '/v1/ai/dish-recognize' // 菜品识别接口
  },

  // 用户API端点
  user: {
    login: '/v1/users/login', // 登录接口
    register: '/v1/users/register', // 注册接口
    profile: '/v1/users/{userId}', // 用户信息接口
    update: '/v1/users/{userId}', // 更新用户信息接口
    preferences: '/v1/users/{userId}/preferences', // 用户偏好接口
    sendSmsCode: '/v1/users/send-sms-code', // 发送手机验证码接口
    sendEmailCode: '/v1/users/send-email-code' // 发送邮箱验证码接口
  },

  // 食谱API端点
  recipe: {
    today: '/v1/recipe/today', // 今日食谱接口
    favorite: '/v1/recipe/favorite', // 我的食谱接口
    recommend: '/v1/recipe/recommend', // 推荐食谱接口
    all: '/v1/recipe/all', // 所有食谱接口
    add: '/v1/recipe', // 新增食谱接口
    update: '/v1/recipe/', // 更新食谱接口 (需要拼接id)
    delete: '/v1/recipe/', // 删除食谱接口 (需要拼接id)
    toggleFavorite: '/v1/recipe/toggle-favorite/', // 切换收藏状态 (需要拼接id)
    batchToggleFavorite: '/v1/recipe/batch-toggle-favorite', // 批量切换收藏状态
    setToday: '/v1/recipe/', // 设置为今日食谱 (需要拼接id)
    unsetToday: '/v1/recipe/' // 取消今日食谱 (需要拼接id)
  },

  // 商家API端点
  merchant: {
    list: '/v1/merchant', // 商家列表接口
    detail: '/v1/merchant/', // 商家详情接口
    register: '/v1/merchant/register', // 商家注册接口
    update: '/v1/merchant/{merchantId}', // 更新商家信息接口
    menu: '/v1/menus/merchants/{merchantId}/menu', // 商家菜单接口
    comments: '/v1/merchant/{merchantId}/comments', // 商家评价接口
    album: '/v1/merchant/{merchantId}/album', // 商家相册接口
    discounts: '/v1/merchant/{merchantId}/discounts', // 商家优惠接口
    announcements: '/v1/merchant/{merchantId}/announcements', // 商家公告接口
    businessOverview: '/v1/merchant/{merchantId}/business-overview', // 营业概览接口
    avatar: '/v1/merchant/{merchantId}/avatar' // 商家头像接口
  },

  // 菜品API端点
  dish: {
    list: '/dishes', // 菜品列表接口
    detail: '/dishes/' // 菜品详情接口
  },

  // 消息API端点
  message: {
    list: '/v1/message/records', // 消息列表接口
    send: '/v1/message/send' // 发送消息接口
  },

  // 教程API端点
  tutorial: {
    featured: '/v1/tutorial/featured', // 精选教程接口（用于首页展示）
    list: '/v1/tutorial/list' // 全部教程接口
  },

  // 首页API端点
  home: {
    hotTopic: '/v1/home/hot-topic' // 今日热点接口
  },

  // 天气API端点
  weather: {
    current: '/v1/weather' // 获取天气信息接口
  },

  // 位置选择API端点
  location: {
    location: '/v1/location', // 获取当前定位接口
    cascaderData: '/v1/location/cascader', // 获取级联选择器地址数据接口
    search: '/v1/location/search' // 地址搜索接口
  },

  // 经营品类API端点
  category: {
    list: '/v1/category/list', // 获取所有经营品类接口
    common: '/v1/category/common' // 获取常用品类接口
  },

  // 订单API端点
  order: {
    list: '/v1/orders/user/', // 获取用户订单列表接口
    detail: '/v1/orders/' // 获取订单详情接口
  },

  // 饮食记录API端点
  diet: {
    list: '/calorie-records', // 饮食记录接口
    user: '/calorie-records/user/', // 根据用户ID获取记录
    date: '/calorie-records/user/{userId}/date/', // 根据用户ID和日期获取记录
    week: '/calorie-records/user/{userId}/week', // 根据用户ID获取本周记录
    add: '/calorie-records', // 添加饮食记录接口
    update: '/calorie-records', // 编辑饮食记录接口
    delete: '/calorie-records/{id}' // 删除饮食记录接口
  },

  // 收藏API端点
  collection: {
    list: '/v1/collections', // 获取用户收藏列表 (参数: userId)
    listByType: '/v1/collections/type', // 根据类型获取收藏 (参数: userId, type)
    add: '/v1/collections', // 添加收藏
    remove: '/v1/collections', // 取消收藏 (参数: userId, type, id)
    check: '/v1/collections/check', // 检查是否已收藏 (参数: userId, type, id)
    clear: '/v1/collections/user/{userId}' // 清空用户所有收藏
  }
}

// WebSocket配置
export const WS_CONFIG = {
  url: 'ws://localhost:11277/ws', // WebSocket服务器地址 - 通用端点（用于订单等）
  chatUrl: 'ws://localhost:11277/ws/chat' // 聊天专用端点
}
