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

// 高德地图API配置
export const AMAP_CONFIG = {
  key: 'YOUR_AMAP_KEY',
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
export const API_CONFIG = envConfig

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
