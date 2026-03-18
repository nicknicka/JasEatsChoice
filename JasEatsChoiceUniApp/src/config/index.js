/**
 * 配置文件
 * 根据环境自动加载对应配置
 */

// 环境判断
const ENV = process.env.NODE_ENV || 'development'

// 开发环境配置
const development = {
  baseURL: 'http://localhost:8080',
  wsURL: 'ws://localhost:8080/ws',
  uploadURL: 'http://localhost:8080/v1/upload',
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

// 导出配置
export default {
  // 当前环境
  ENV,

  // API配置
  ...config,

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
