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
    recognizeDish: '/v1/ai/dish-recognize', // 菜品识别接口
    // AI聊天历史管理
    history: '/v1/ai/chat/history', // 获取聊天历史
    save: '/v1/ai/chat/save', // 保存聊天消息
    clear: '/v1/ai/chat/clear', // 清空聊天记录
    hasHistory: '/v1/ai/chat/has-history' // 检查是否有聊天历史
  },

  // 用户API端点
  user: {
    login: '/v1/users/login', // 登录接口
    register: '/v1/users/register', // 注册接口
    profile: '/v1/users/{userId}', // 用户信息接口
    update: '/v1/users/{userId}', // 更新用户信息接口
    updatePassword: '/v1/users/{userId}/password', // 修改密码接口
    uploadAvatar: '/v1/users/{userId}/avatar/base64', // 上传头像接口
    preferences: '/v1/users/{userId}/preferences', // 用户偏好接口
    feedback: '/v1/users/feedback', // 提交反馈接口
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
    list: '/v1/dishes', // 菜品列表接口
    detail: '/v1/dishes/', // 菜品详情接口
    status: '/v1/dishes', // 菜品状态更新接口（使用时拼接 /{dishId}/status）
    batchStatus: '/v1/dishes/batch/status' // 批量更新菜品状态接口
  },

  // 消息API端点
  message: {
    list: '/notifications/user', // 消息列表接口（系统通知）
    send: '/v1/message/send' // 发送消息接口（聊天消息）
  },

  // 教程API端点
  tutorial: {
    // 用户端（公开）
    featured: '/v1/tutorial/featured', // 精选教程接口（用于首页展示）
    list: '/v1/tutorial/list', // 全部教程接口
    detail: '/v1/tutorial/', // 教程详情接口（需要拼接id）
    page: '/v1/tutorial/page', // 分页查询教程

    // 普通用户（需要认证）
    userCreate: '/v1/tutorial/user/create', // 用户创建教程
    userMy: '/v1/tutorial/user/my', // 获取我的教程列表
    userUpdate: '/v1/tutorial/user', // 更新教程（需要拼接 '/' + id）
    userSubmit: '/v1/tutorial/user', // 提交审核（需要拼接 '/' + id + '/submit'）
    userDelete: '/v1/tutorial/user', // 删除教程（需要拼接 '/' + id）

    // 管理员端
    adminList: '/v1/tutorial/admin/list', // 获取所有教程（管理员专用）
    adminCreate: '/v1/tutorial/admin/create', // 管理员创建教程
    adminPending: '/v1/tutorial/admin/pending', // 获取待审核列表
    adminApprove: '/v1/tutorial/admin', // 审核通过（需要拼接 '/' + id + '/approve'）
    adminReject: '/v1/tutorial/admin', // 审核拒绝（需要拼接 '/' + id + '/reject'）
    adminToggleFeatured: '/v1/tutorial/admin', // 设置精选（需要拼接 '/' + id + '/featured'）
    adminDelete: '/v1/tutorial/admin', // 删除教程（需要拼接 '/' + id）

    // 商家端
    merchantCreate: '/v1/tutorial/merchant/create', // 商家创建教程
    merchantUpdate: '/v1/tutorial/merchant', // 商家更新教程（需要拼接 '/' + id）
    merchantSubmit: '/v1/tutorial/merchant', // 提交审核（需要拼接 '/' + id + '/submit'）
    merchantMy: '/v1/tutorial/merchant/my', // 获取商家教程列表
    merchantDelete: '/v1/tutorial/merchant' // 商家删除教程（需要拼接 '/' + id）
  },

  // 首页API端点
  home: {
    hotTopic: '/v1/home/hot-topic', // 今日热点接口
    hotTopicClick: '/v1/home/hot-topic/click', // 记录热点点击
    hotTopicShare: '/v1/home/hot-topic/share' // 记录热点分享
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
  },

  // 推荐拒绝API端点
  recommendReject: {
    add: '/v1/recommendations/rejects', // 记录拒绝推荐 (参数: userId, dishId, reason?)
    count: '/v1/recommendations/rejects/count', // 统计拒绝次数 (参数: userId, dishId)
    list: '/v1/recommendations/rejects/list', // 获取已拒绝菜品列表 (参数: userId)
    frequent: '/v1/recommendations/rejects/frequent', // 获取频繁拒绝的菜品 (参数: userId, threshold?)
    clear: '/v1/recommendations/rejects' // 清除拒绝记录 (参数: userId, dishId)
  },

  // 文件上传API端点
  upload: {
    image: '/v1/chat/upload-image', // 上传图片接口
    file: '/v1/chat/upload-file' // 上传文件接口
  },

  // 管理员API端点
  admin: {
    // 认证
    login: '/admin/login', // 管理员登录
    current: '/admin/current', // 获取当前管理员信息
    list: '/admin/list', // 获取管理员列表
    create: '/admin/create', // 创建管理员
    updateStatus: '/admin/{adminId}/status', // 修改管理员状态
    resetPassword: '/admin/{adminId}/password', // 重置管理员密码

    // 统计
    dashboard: '/admin/statistics/dashboard', // 控制台统计数据
    userStats: '/admin/statistics/users', // 用户统计数据
    orderStats: '/admin/statistics/orders', // 订单统计数据
    revenueStats: '/admin/statistics/revenue', // 收入统计数据

    // 用户管理
    userList: '/admin/users', // 用户列表
    userDetail: '/admin/users/{userId}', // 用户详情
    updateUserStatus: '/admin/users/{userId}/status', // 修改用户状态
    deleteUser: '/admin/users/{userId}', // 删除用户

    // 商家管理
    merchantList: '/admin/merchants', // 商家列表
    merchantDetail: '/admin/merchants/{merchantId}', // 商家详情
    auditMerchant: '/admin/merchants/{merchantId}/audit', // 审核商家
    updateMerchantStatus: '/admin/merchants/{merchantId}/status', // 修改商家状态

    // 订单管理
    orderList: '/admin/orders', // 订单列表
    orderDetail: '/admin/orders/{orderId}', // 订单详情

    // 菜品管理
    dishList: '/admin/dishes', // 菜品列表
    dishDetail: '/admin/dishes/{dishId}', // 菜品详情
    auditDish: '/admin/dishes/{dishId}/audit', // 审核菜品
    updateDishStatus: '/admin/dishes/{dishId}/status', // 修改菜品状态

    // 财务管理
    withdrawalList: '/admin/finance/withdrawals', // 提现申请列表
    auditWithdrawal: '/admin/finance/withdrawals/{id}/audit', // 审核提现
    rechargeList: '/admin/finance/recharges', // 充值记录
    refundList: '/admin/finance/refunds', // 退款记录

    // 系统日志
    operationLogs: '/admin/logs/operations', // 操作日志
    systemLogs: '/admin/logs/system', // 系统日志
    loginLogs: '/admin/logs/login' // 登录日志
  }
}

// WebSocket配置
export const WS_CONFIG = {
  url: 'ws://localhost:11277/ws', // WebSocket服务器地址 - 通用端点（用于订单等）
  chatUrl: 'ws://localhost:11277/ws/chat' // 聁天专用端点
}

// 角色名称映射配置（用于修复数据库中的乱码问题）
export const ROLE_NAME_MAP = {
  'SUPER_ADMIN': '超级管理员',
  'ADMIN': '管理员',
  'USER_MANAGER': '用户管理员',
  'MERCHANT_MANAGER': '商家管理员',
  'CONTENT_MANAGER': '内容管理员',
  'FINANCE_MANAGER': '财务管理员'
}
