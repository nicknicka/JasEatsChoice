// API服务器配置
export const API_SERVER = {
  BASE_URL: 'http://localhost:7777/api'
}

// AI助手API端点
export const AI_API = {
  CHAT: '/v1/ai/chat', // AI聊天接口
  RECIPE: '/v1/ai/recipe', // 食谱推荐接口
  NUTRIENT: '/v1/ai/nutrient', // 营养分析接口
  RECOGNIZE_DISH: '/v1/ai/dish-recognize' // 菜品识别接口
}

// 用户API端点
export const USER_API = {
  LOGIN: '/v1/users/login', // 登录接口
  REGISTER: '/v1/users/register', // 注册接口
  PROFILE: '/v1/users/{userId}', // 用户信息接口
  UPDATE: '/v1/users/{userId}', // 更新用户信息接口
  PREFERENCES: '/v1/users/{userId}/preferences', // 用户偏好接口
  SEND_SMS_CODE: '/v1/users/send-sms-code', // 发送手机验证码接口
  SEND_EMAIL_CODE: '/v1/users/send-email-code' // 发送邮箱验证码接口
}

// 食谱API端点
export const RECIPE_API = {
  TODAY: '/v1/recipe/today', // 今日食谱接口
  FAVORITE: '/v1/recipe/favorite', // 我的食谱接口
  RECOMMEND: '/v1/recipe/recommend' // 推荐食谱接口
}

// 商家API端点
export const MERCHANT_API = {
  LIST: '/v1/merchant', // 商家列表接口
  DETAIL: '/v1/merchant/', // 商家详情接口
  REGISTER: '/v1/merchant/register', // 商家注册接口
  MENU: '/v1/menus/merchants/{merchantId}/menu', // 商家菜单接口
  COMMENTS: '/v1/merchant/{merchantId}/comments', // 商家评价接口
  ALBUM: '/v1/merchant/{merchantId}/album', // 商家相册接口
  DISCOUNTS: '/v1/merchant/{merchantId}/discounts', // 商家优惠接口
  BUSINESS_OVERVIEW: '/v1/merchant/{merchantId}/business-overview', // 营业概览接口
  AVATAR: '/v1/merchant/{merchantId}/avatar' // 商家头像接口
}

// 菜品API端点
export const DISH_API = {
  LIST: '/dishes', // 菜品列表接口
  DETAIL: '/dishes/' // 菜品详情接口
}

// 消息API端点
export const MESSAGE_API = {
  LIST: '/v1/message/records', // 消息列表接口
  SEND: '/v1/message/send' // 发送消息接口
}

// 教程API端点
export const TUTORIAL_API = {
  FEATURED: '/v1/tutorial/featured', // 精选教程接口（用于首页展示）
  LIST: '/v1/tutorial/list' // 全部教程接口
}

// 首页API端点
export const HOME_API = {
  HOT_TOPIC: '/v1/home/hot-topic' // 今日热点接口
}

// 天气API端点
export const WEATHER_API = {
  CURRENT: '/v1/weather', // 获取天气信息接口
  LOCATION: '/v1/location' // 获取当前定位接口
}

// 位置选择API端点
export const LOCATION_API = {
  CASCADER_DATA: '/v1/location/cascader' // 获取级联选择器地址数据接口
}

// 订单API端点
export const ORDER_API = {
  LIST: '/v1/orders/user/', // 获取用户订单列表接口
  DETAIL: '/v1/orders/' // 获取订单详情接口
}

// 饮食记录API端点
export const DIET_API = {
  LIST: '/calorie-records', // 饮食记录接口
  USER: '/calorie-records/user/', // 根据用户ID获取记录
  DATE: '/calorie-records/user/{userId}/date/', // 根据用户ID和日期获取记录
  WEEK: '/calorie-records/user/{userId}/week', // 根据用户ID获取本周记录
  ADD: '/calorie-records' // 添加饮食记录接口
}

// 收藏API端点
export const COLLECTION_API = {
  LIST: '/v1/collections/user/{userId}', // 获取用户收藏列表
  REMOVE: '/v1/collections/{id}', // 移除单个收藏
  CLEAR: '/v1/collections/user/{userId}' // 清空用户所有收藏
}
