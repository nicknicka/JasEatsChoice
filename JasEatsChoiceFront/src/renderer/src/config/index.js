/**
 * 应用配置文件
 * 统一管理所有配置项
 */

// 环境判断
const ENV = process.env.NODE_ENV || 'development'

// 开发环境配置
const development = {
  baseURL: 'http://localhost:7777/api',
  wsURL: 'ws://localhost:11277/ws',
  wsChatURL: 'ws://localhost:11277/ws/chat',
  uploadURL: 'http://localhost:7777/api/v1/upload',
  imageCDN: '',
  debug: true,
  timeout: 30000,
  enableLog: true
}

// 生产环境配置
const production = {
  baseURL: 'https://api.yourdomain.com',
  wsURL: 'wss://api.yourdomain.com/ws',
  wsChatURL: 'wss://api.yourdomain.com/ws/chat',
  uploadURL: 'https://api.yourdomain.com/v1/upload',
  imageCDN: 'https://cdn.yourdomain.com',
  debug: false,
  timeout: 30000,
  enableLog: false
}

// 根据环境选择配置
const envConfig = {
  development,
  production
}[ENV] || development

// WebSocket配置
export const WS_CONFIG = {
  url: envConfig.wsURL,
  chatUrl: envConfig.wsChatURL
}

const getEnvValue = (name, fallback) => {
  if (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env[name]) {
    return import.meta.env[name]
  }

  return fallback
}

// 高德地图API配置
export const AMAP_CONFIG = {
  // Web端(JS API) Key
  key: getEnvValue('VITE_AMAP_KEY', 'ef5cbde8753cd2fa578899bee7f9bf71'),
  // 对应的安全密钥（用于后续接入安全校验时使用）
  securityJsCode: getEnvValue('VITE_AMAP_SECURITY_JS_CODE', '34a9a321bca66a4623d9ddc3c6e5228f'),
  baseURL: 'https://restapi.amap.com/v3',
  district: '/config/district'
}

// 角色名称映射配置
export const ROLE_NAME_MAP = {
  SUPER_ADMIN: '超级管理员',
  ADMIN: '管理员',
  USER_MANAGER: '用户管理员',
  MERCHANT_MANAGER: '商家管理员',
  CONTENT_MANAGER: '内容管理员',
  FINANCE_MANAGER: '财务管理员'
}

// 业务配置
export const BUSINESS_CONFIG = {
  // 订单超时时间（分钟）
  orderTimeout: 30,
  // 自动收货时间（天）
  autoConfirmDays: 7,
  // 退款审核时间（天）
  refundAuditDays: 3,
  // 配送范围（米）
  deliveryRadius: 5000,
  // 最小起送金额
  minOrderAmount: 10,
  // 免配送费金额
  freeDeliveryAmount: 50
}

// API配置（命名导出，供其他模块使用）
export const API_CONFIG = {
  ...envConfig,

  // AI助手API端点
  ai: {
    chat: '/agent/supervisor-sse/chat',
    chatLegacy: '/v1/ai/stream/chat',
    chatSupervisor: '/agent/supervisor/chat',
    recipe: '/v1/ai/recipe',
    nutrient: '/v1/ai/nutrient',
    recognizeDish: '/v1/ai/dish-recognize',
    history: '/v1/ai/chat/history',
    save: '/v1/ai/chat/save',
    clear: '/v1/ai/chat/clear',
    hasHistory: '/v1/ai/chat/has-history'
  },

  // 用户API端点
  user: {
    login: '/v1/users/login',
    register: '/v1/users/register',
    profile: '/v1/users/{userId}',
    update: '/v1/users/{userId}',
    updatePassword: '/v1/users/{userId}/password',
    uploadAvatar: '/v1/users/{userId}/avatar/base64',
    preferences: '/v1/users/{userId}/preferences',
    feedback: '/v1/users/feedback',
    sendSmsCode: '/v1/users/send-sms-code',
    sendEmailCode: '/v1/users/send-email-code'
  },

  // 食谱API端点
  recipe: {
    today: '/v1/recipe/today',
    favorite: '/v1/recipe/favorite',
    recommend: '/v1/recipe/recommend',
    all: '/v1/recipe/all',
    add: '/v1/recipe',
    update: '/v1/recipe/',
    delete: '/v1/recipe/',
    toggleFavorite: '/v1/recipe/toggle-favorite/',
    batchToggleFavorite: '/v1/recipe/batch-toggle-favorite',
    setToday: '/v1/recipe/',
    unsetToday: '/v1/recipe/'
  },

  // 商家API端点
  merchant: {
    list: '/v1/merchant',
    detail: '/v1/merchant/',
    register: '/v1/merchant/register',
    update: '/v1/merchant/{merchantId}',
    menu: '/v1/menus/merchants/{merchantId}/menu',
    comments: '/v1/merchant/{merchantId}/comments',
    album: '/v1/merchant/{merchantId}/album',
    discounts: '/v1/merchant/{merchantId}/discounts',
    announcements: '/v1/merchant/{merchantId}/announcements',
    businessOverview: '/v1/merchant/{merchantId}/business-overview',
    avatar: '/v1/merchant/{merchantId}/avatar',
    updateOrderStatus: '/v1/orders/{orderId}/status',
    notifyUser: '/v1/orders/{orderId}/notify'
  },

  // 菜品API端点
  dish: {
    list: '/v1/dishes',
    detail: '/v1/dishes/',
    status: '/v1/dishes',
    batchStatus: '/v1/dishes/batch/status',
    batchDelete: '/v1/dishes/batch'
  },

  // 消息API端点
  message: {
    list: '/notifications/user',
    send: '/v1/message/send'
  },

  // 教程API端点
  tutorial: {
    featured: '/v1/tutorial/featured',
    list: '/v1/tutorial/list',
    detail: '/v1/tutorial/',
    page: '/v1/tutorial/page',
    userCreate: '/v1/tutorial/user/create',
    userMy: '/v1/tutorial/user/my',
    userUpdate: '/v1/tutorial/user',
    userSubmit: '/v1/tutorial/user',
    userDelete: '/v1/tutorial/user',
    merchantMy: '/v1/tutorial/merchant/my',
    merchantCreate: '/v1/tutorial/merchant/create',
    merchantUpdate: '/v1/tutorial/merchant',
    merchantSubmit: '/v1/tutorial/merchant',
    merchantDelete: '/v1/tutorial/merchant',
    adminList: '/v1/tutorial/admin/list',
    adminCreate: '/v1/tutorial/admin/create',
    adminPending: '/v1/tutorial/admin/pending',
    adminApprove: '/v1/tutorial/admin/',
    adminReject: '/v1/tutorial/admin/',
    adminDelete: '/v1/tutorial/admin/',
    adminToggleFeatured: '/v1/tutorial/admin/'
  },

  // 首页API端点
  home: {
    hotTopic: '/v1/home/hot-topic',
    hotTopicClick: '/v1/home/hot-topic/click',
    hotTopicShare: '/v1/home/hot-topic/share'
  },

  // 天气API端点
  weather: {
    current: '/v1/weather'
  },

  // 位置API端点
  location: {
    location: '/v1/location',
    cascaderData: '/v1/location/cascader',
    search: '/v1/location/search'
  },

  // 品类API端点
  category: {
    list: '/v1/category/list',
    common: '/v1/category/common'
  },

  // 订单API端点
  order: {
    list: '/v1/orders/user/',
    detail: '/v1/orders/'
  },

  // 饮食记录API端点
  diet: {
    list: '/calorie-records',
    user: '/calorie-records/user/',
    date: '/calorie-records/user/{userId}/date/',
    week: '/calorie-records/user/{userId}/week',
    add: '/calorie-records',
    update: '/calorie-records',
    delete: '/calorie-records/{id}'
  },

  // 收藏API端点
  collection: {
    list: '/v1/collections',
    listByType: '/v1/collections/type',
    add: '/v1/collections',
    remove: '/v1/collections',
    check: '/v1/collections/check',
    clear: '/v1/collections/user/{userId}'
  },

  // 推荐拒绝API端点
  recommendReject: {
    add: '/v1/recommendations/rejects',
    count: '/v1/recommendations/rejects/count',
    list: '/v1/recommendations/rejects/list',
    frequent: '/v1/recommendations/rejects/frequent',
    clear: '/v1/recommendations/rejects'
  },

  // 文件上传API端点
  upload: {
    image: '/v1/chat/upload-image',
    file: '/v1/chat/upload-file'
  },

  // 群组API端点
  group: {
    list: '/v1/groups/my',
    detail: '/v1/groups/{groupId}',
    create: '/v1/groups',
    update: '/v1/groups/{groupId}',
    delete: '/v1/groups/{groupId}',
    leave: '/v1/groups/{groupId}/leave',
    members: '/v1/groups/{groupId}/members',
    addMember: '/v1/groups/{groupId}/members',
    removeMember: '/v1/groups/{groupId}/members/{userId}',
    checkMember: '/v1/groups/{groupId}/members/{userId}/check',
    userRole: '/v1/groups/{groupId}/members/{userId}/role'
  },

  // 管理员API端点
  admin: {
    login: '/admin/login',
    current: '/admin/current',
    list: '/admin/list',
    create: '/admin/create',
    updateStatus: '/admin/{adminId}/status',
    resetPassword: '/admin/{adminId}/password',
    dashboard: '/admin/statistics/dashboard',
    userStats: '/admin/statistics/users',
    orderStats: '/admin/statistics/orders',
    userList: '/admin/users',
    userDetail: '/admin/users/{userId}',
    updateUserStatus: '/admin/users/{userId}/status',
    deleteUser: '/admin/users/{userId}',
    hotTopics: '/v1/admin/hot-topic',
    hotTopicDetail: '/v1/admin/hot-topic/detail',
    hotTopicCreate: '/v1/admin/hot-topic/create',
    hotTopicUpdate: '/v1/admin/hot-topic/update',
    hotTopicDelete: '/v1/admin/hot-topic/delete',
    hotTopicReview: '/v1/admin/hot-topic/review',
    hotTopicBatchDelete: '/v1/admin/hot-topic/batch-delete',
    hotTopicStatistics: '/v1/admin/hot-topic/statistics',
    announcements: '/admin/announcements',
    announcementDetail: '/admin/announcements',
    announcementCreate: '/admin/announcements',
    announcementUpdate: '/admin/announcements',
    announcementDelete: '/admin/announcements',
    announcementBatchDelete: '/admin/announcements/batch',
    announcementUpdateStatus: '/admin/announcements',
    announcementStatistics: '/admin/announcements/statistics',
    operationLogs: '/admin/logs/operations',
    systemLogs: '/admin/logs/system',
    loginLogs: '/admin/logs/login',
    merchantList: '/admin/merchants',
    merchantDetail: '/admin/merchants/{merchantId}',
    auditMerchant: '/admin/merchants/{merchantId}/audit',
    updateMerchantStatus: '/admin/merchants/{merchantId}/status',
    pendingMerchants: '/admin/merchants/pending',
    orderList: '/admin/orders',
    orderDetail: '/admin/orders/{orderId}',
    updateOrderStatus: '/admin/orders/{orderId}/status',
    dishList: '/admin/dishes',
    dishDetail: '/admin/dishes/{dishId}',
    dishAuditList: '/admin/dishes/audit',
    auditDish: '/admin/dishes/{dishId}/audit',
    updateDishStatus: '/admin/dishes/{dishId}/status',
    withdrawalList: '/admin/finance/withdrawals',
    withdrawalDetail: '/admin/finance/withdrawals/{id}',
    auditWithdrawal: '/admin/finance/withdrawals/{id}/process',
    batchProcessWithdrawal: '/admin/finance/withdrawals/batch/process',
    completeWithdrawal: '/admin/finance/withdrawals/{id}/complete',
    failWithdrawal: '/admin/finance/withdrawals/{id}/fail',
    withdrawalStatistics: '/admin/finance/withdrawals/statistics',
    rechargeList: '/admin/finance/recharges',
    rechargeStats: '/admin/finance/recharges/stats',
    refundList: '/admin/finance/refunds',
    refundStats: '/admin/finance/refunds/stats',
    processRefund: '/admin/finance/refunds/{refundId}/process',
    roleList: '/admin/roles',
    allRoles: '/admin/roles/all',
    permissionList: '/admin/permissions',
    permissionTree: '/admin/permissions/tree',
    configList: '/admin/settings/config',
    configGroups: '/admin/settings/config/groups',
    configByGroup: '/admin/settings/config/group/{configGroup}',
    configDetail: '/admin/settings/config/{configId}',
    createConfig: '/admin/settings/config',
    updateConfig: '/admin/settings/config/{configId}',
    deleteConfig: '/admin/settings/config/{configId}',
    batchUpdateConfig: '/admin/settings/config/batch',
    refreshConfigCache: '/admin/settings/config/refresh'
  }
}

// 导出环境判断方法
export const isDev = ENV === 'development'
export const isProd = ENV === 'production'

// 导出环境配置
export const ENV_CONFIG = envConfig

// 默认导出
export default {
  ENV,
  ...envConfig,
  WS_CONFIG,
  AMAP_CONFIG,
  ROLE_NAME_MAP,
  BUSINESS_CONFIG
}
