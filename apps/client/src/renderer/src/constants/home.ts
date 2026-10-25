/**
 * 首页相关常量配置
 */

export const HOME_CONSTANTS = {
  // WebSocket 配置
  WS: {
    MAX_ATTEMPTS: 10,
    RETRY_DELAY_BASE: 3000,
    RETRY_DELAY_MAX: 30000
  },

  // 请求重试配置
  RETRY: {
    MAX_RETRIES: 3,
    DELAY: 1000
  },

  // 轮播配置
  CAROUSEL: {
    INTERVAL: 3000,
    HEIGHT: '320px'
  },

  // 教程显示配置
  TUTORIAL: {
    MAX_DISPLAY: 4,
    DEFAULT_DURATION: '5分钟'
  },

  // 通知显示时长
  TOAST_DURATION: {
    SUCCESS: 2000,
    ERROR: 3000
  },

  // 动画时长
  ANIMATION: {
    FAST: 200,
    NORMAL: 300,
    SLOW: 600
  },

  // 图片占位符
  DEFAULT_DISH_IMAGE:
    'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="300"%3E%3Cdefs%3E%3ClinearGradient id="grad1" x1="0%25" y1="0%25" x2="100%25" y2="100%25"%3E%3Cstop offset="0%25" style="stop-color:%23ff6b6b;stop-opacity:0.1" /%3E%3Cstop offset="100%25" style="stop-color:%23ffa8a8;stop-opacity:0.2" /%3E%3C/linearGradient%3E%3C/defs%3E%3Crect fill="url(%23grad1)" width="400" height="300"/%3E%3Ccircle cx="200" cy="130" r="50" fill="%23ff6b6b" opacity="0.15"/%3E%3Ctext x="200" y="130" font-size="48" text-anchor="middle" fill="%23ff6b6b" opacity="0.3"%3E🍽️%3C/text%3E%3Ctext x="200" y="200" font-family="Arial, sans-serif" font-size="20" font-weight="600" text-anchor="middle" fill="%23999"%3E暂无图片%3C/text%3E%3Ctext x="200" y="230" font-family="Arial, sans-serif" font-size="14" text-anchor="middle" fill="%23bbb"%3E精彩美食即将呈现%3C/text%3E%3C/svg%3E',

  DEFAULT_TUTORIAL_THUMBNAIL:
    'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="200"%3E%3Cdefs%3E%3ClinearGradient id="grad2" x1="0%25" y1="0%25" x2="100%25" y2="100%25"%3E%3Cstop offset="0%25" style="stop-color:%236ba4ff;stop-opacity:0.1" /%3E%3Cstop offset="100%25" style="stop-color:%23a8c8ff;stop-opacity:0.2" /%3E%3C/linearGradient%3E%3C/defs%3E%3Crect fill="url(%23grad2)" width="300" height="200"/%3E%3Ccircle cx="150" cy="85" r="40" fill="%236ba4ff" opacity="0.15"/%3E%3Ctext x="150" y="90" font-size="40" text-anchor="middle" fill="%236ba4ff" opacity="0.3"%3E📖%3C/text%3E%3Ctext x="150" y="150" font-family="Arial, sans-serif" font-size="16" font-weight="600" text-anchor="middle" fill="%23999"%3E暂无缩略图%3C/text%3E%3Ctext x="150" y="175" font-family="Arial, sans-serif" font-size="12" text-anchor="middle" fill="%23bbb"%3E教程内容加载中%3C/text%3E%3C/svg%3E'
}
