/**
 * 加菜功能常量配置
 */
export const ADD_DISH_CONFIG = {
  // 超时时间（分钟）
  TIMEOUT_MINUTES: 15,

  // 首次提醒时间（分钟）
  FIRST_REMIND_MINUTES: 10,

  // 加菜前缀
  ADD_DISH_PREFIX: '【加购】',

  // 默认驳回原因
  DEFAULT_REJECT_REASONS: ['超出聚餐预算', '菜品已备齐', '其他原因'],

  // 刷新间隔（毫秒）
  REFRESH_INTERVAL: 30000
}

/**
 * 加菜审核状态
 */
export const ADD_DISH_APPROVAL_STATUS = {
  PENDING: 0, // 待审核
  APPROVED: 1, // 审核通过
  REJECTED: 2, // 审核驳回
  WITHDRAWN: 3, // 已撤回
  TIMEOUT_REJECTED: 4 // 超时驳回
}

/**
 * 加菜权限类型
 */
export const ADD_DISH_PERMISSION = {
  ALL_MEMBERS: 0, // 全员可加菜
  INITIATOR_ONLY: 1 // 仅发起者可加菜
}

/**
 * 状态标签映射
 */
export const STATUS_TAG_MAP = {
  [ADD_DISH_APPROVAL_STATUS.PENDING]: {
    type: 'warning',
    text: '待审核'
  },
  [ADD_DISH_APPROVAL_STATUS.APPROVED]: {
    type: 'success',
    text: '已通过'
  },
  [ADD_DISH_APPROVAL_STATUS.REJECTED]: {
    type: 'danger',
    text: '已驳回'
  },
  [ADD_DISH_APPROVAL_STATUS.WITHDRAWN]: {
    type: 'info',
    text: '已撤回'
  },
  [ADD_DISH_APPROVAL_STATUS.TIMEOUT_REJECTED]: {
    type: 'danger',
    text: '超时驳回'
  }
}

// 单独导出常用的常量，方便组件直接使用
export const DEFAULT_REJECT_REASONS = ADD_DISH_CONFIG.DEFAULT_REJECT_REASONS
