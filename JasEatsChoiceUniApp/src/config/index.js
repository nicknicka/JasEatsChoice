/**
 * 配置文件
 * 根据环境自动加载对应配置
 */

// 环境判断
const ENV = process.env.NODE_ENV || 'development'

// 开发环境配置
const development = {
  baseURL: 'http://localhost:7777/api', // 改为 localhost
  wsURL: 'ws://localhost:7777/api/ws',
  uploadURL: 'http://localhost:7777/api/v1/upload',
  imageCDN: '',
  debug: true,
  timeout: 30000,
  enableLog: true,
  wechat: {
    appId: 'wx1234567890abcdef'
  }
}

// 生产环境配置
const production = {
  baseURL: 'https://api.yourdomain.com',
  wsURL: 'wss://api.yourdomain.com/ws',
  uploadURL: 'https://api.yourdomain.com/v1/upload',
  imageCDN: 'https://cdn.yourdomain.com',
  debug: false,
  timeout: 30000,
  enableLog: false,
  wechat: {
    appId: 'your_production_appid'
  }
}

// 根据环境选择配置
const config = {
  development,
  production
}[ENV] || development

// API端点配置（参照前端桌面端）
export const API_CONFIG = {
  // 用户API端点
  user: {
    login: '/v1/users/login', // 登录接口
    register: '/v1/users/register', // 注册接口
    profile: '/v1/users/{userId}', // 用户信息接口
    update: '/v1/users/{userId}', // 更新用户信息接口
    updatePassword: '/v1/users/{userId}/password', // 修改密码接口
    uploadAvatar: '/v1/users/{userId}/avatar/base64', // 上传头像接口
    sendSmsCode: '/v1/users/send-sms-code', // 发送手机验证码接口
    wechatLogin: '/v1/users/wechat-login' // 微信登录接口
  },

  // 商家API端点
  merchant: {
    list: '/v1/merchant', // 商家列表接口
    detail: '/v1/merchant/', // 商家详情接口
    menu: '/v1/menus/merchants/{merchantId}/menu', // 商家菜单接口
    comments: '/v1/merchant/{merchantId}/comments', // 商家评价接口
    register: '/v1/merchant/register', // 商家注册接口
    login: '/v1/merchant/login' // 商家登录接口
  },

  // 菜品API端点
  dish: {
    list: '/v1/dishes', // 菜品列表接口
    detail: '/v1/dishes/', // 菜品详情接口
    status: '/v1/dishes', // 菜品状态更新接口
    batchStatus: '/v1/dishes/batch/status' // 批量更新菜品状态接口
  },

  // 订单API端点
  order: {
    list: '/v1/orders/user/', // 获取用户订单列表接口
    detail: '/v1/orders/', // 获取订单详情接口
    create: '/v1/orders', // 创建订单接口
    cancel: '/v1/orders/', // 取消订单接口 (需要拼接orderId)
    confirm: '/v1/orders/', // 确认收货接口 (需要拼接orderId/confirm)
    pay: '/v1/orders/' // 支付接口 (需要拼接orderId/pay)
  },

  // 食谱API端点
  recipe: {
    today: '/v1/recipe/today', // 今日食谱接口
    favorite: '/v1/recipe/favorite', // 我的食谱接口
    recommend: '/v1/recipe/recommend', // 推荐食谱接口
    all: '/v1/recipe/all', // 所有食谱接口
    toggleFavorite: '/v1/recipe/toggle-favorite/' // 切换收藏状态
  },

  // AI助手API端点
  ai: {
    chat: '/agent/supervisor-sse/chat', // SupervisorAgent SSE接口
    chatLegacy: '/v1/ai/stream/chat', // 旧版AI聊天接口
    recipe: '/v1/ai/recipe', // 食谱推荐接口
    nutrient: '/v1/ai/nutrient', // 营养分析接口
    recognizeDish: '/v1/ai/dish-recognize' // 菜品识别接口
  },

  // 聊天API端点
  chat: {
    conversations: '/v1/conversations', // 会话列表
    messages: '/v1/messages', // 消息列表
    send: '/v1/messages/send' // 发送消息
  },

  // 验证码API端点
  captcha: {
    checkCode: '/v1/captcha/checkCode' // 获取图形验证码
  },

  // 地址API端点
  address: {
    list: '/v1/addresses', // 地址列表
    default: '/v1/addresses/default', // 默认地址
    add: '/v1/addresses', // 添加地址
    update: '/v1/addresses/', // 更新地址 (需要拼接id)
    delete: '/v1/addresses/', // 删除地址 (需要拼接id)
    setDefault: '/v1/addresses/' // 设置默认地址 (需要拼接id/default)
  },

  // 收藏API端点
  collection: {
    list: '/v1/collections', // 获取用户收藏列表
    add: '/v1/collections', // 添加收藏
    remove: '/v1/collections', // 取消收藏
    check: '/v1/collections/check' // 检查是否已收藏
  },

  // 评价API端点
  review: {
    list: '/v1/merchant/{merchantId}/comments', // 商家评价列表
    add: '/v1/reviews', // 添加评价
    my: '/v1/reviews/my' // 我的评价
  },

  // 优惠券API端点
  coupon: {
    list: '/v1/coupons/user', // 用户优惠券列表
    available: '/v1/coupons/available', // 可用优惠券
    receive: '/v1/coupons/' // 领取优惠券 (需要拼接id/receive)
  },

  // 钱包API端点
  wallet: {
    info: '/v1/wallet', // 钱包信息
    recharge: '/v1/wallet/recharge', // 充值
    withdraw: '/v1/wallet/withdraw', // 提现
    transactions: '/v1/wallet/transactions' // 交易记录
  }
}

// 导出配置
export default {
  // 当前环境
  ENV,

  // API配置
  ...config,

  // API端点配置
  API_CONFIG,

  // 业务配置
  business: {
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
  },

  // 微信小程序权限配置
  permissions: {
    // 位置权限
    location: {
      scope: 'scope.userLocation',
      desc: '您的位置信息将用于推荐附近商家和计算配送距离'
    },
    // 地址权限
    address: {
      scope: 'scope.address',
      desc: '需要使用您的收货地址'
    },
    // 相机权限
    camera: {
      scope: 'scope.camera',
      desc: '需要使用您的相机拍摄菜品照片'
    },
    // 相册权限
    writePhotosAlbum: {
      scope: 'scope.writePhotosAlbum',
      desc: '需要保存图片到您的相册'
    }
  },

  // 分享配置
  share: {
    title: '佳食宜选 - 智能校园订餐',
    path: '/pages/splash/index',
    imageUrl: '/static/share-default.jpg'
  }
}

// 导出环境判断方法
export const isDev = ENV === 'development'
export const isProd = ENV === 'production'
