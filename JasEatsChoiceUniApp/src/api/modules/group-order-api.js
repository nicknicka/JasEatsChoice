/**
 * 群订单管理 API
 * 对接后端 GroupOrderController
 * 基础路径: /v1/group-orders
 */
import { get, post, put, del } from '@/utils/request'

export const groupOrderApi = {
  /**
   * GROUP-002: 创建群订单
   * POST /v1/group-orders
   * @param {Object} data - 群订单数据
   * @param {string} data.merchantId - 商家ID
   * @param {string} data.creatorId - 创建人ID
   * @param {string} data.name - 群订单名称
   * @param {number} data.maxParticipants - 最大参与人数
   * @param {string} data.orderCode - 订单码（6位数字）
   * @param {string} data.deadline - 截止时间
   * @param {string} data.deliveryAddress - 配送地址
   * @param {string} data.remark - 备注
   */
  create: (data) => post('/v1/group-orders', data),

  /**
   * GROUP-001: 生成邀请二维码
   * GET /v1/group-orders/{orderId}/qrcode
   * @param {string} orderId - 群订单ID
   * @param {Object} params - 参数
   * @param {number} params.width - 二维码宽度，默认300
   */
  getQRCode: (orderId, params) => get(`/v1/group-orders/${orderId}/qrcode`, params),

  /**
   * GROUP-008: 获取用户的群订单列表
   * GET /v1/group-orders
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {string} params.status - 状态（pending/in_progress/completed/cancelled）
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getList: (params) => get('/v1/group-orders', params),

  /**
   * GROUP-007: 获取群订单详情
   * GET /v1/group-orders/{orderId}
   * @param {string} orderId - 群订单ID
   */
  getDetail: (orderId) => get(`/v1/group-orders/${orderId}`),

  /**
   * GROUP-009: 通过订单码加入群订单
   * POST /v1/group-orders/join
   * @param {Object} data - 加入数据
   * @param {string} data.orderCode - 订单码（6位数字）
   * @param {string} data.userId - 用户ID
   */
  joinByCode: (data) => post('/v1/group-orders/join', data),

  /**
   * 加入群订单
   * POST /v1/group-orders/{orderId}/join
   * @param {string} orderId - 群订单ID
   * @param {Object} data - 加入数据
   * @param {string} data.userId - 用户ID
   */
  join: (orderId, data) => post(`/v1/group-orders/${orderId}/join`, data),

  /**
   * 退出群订单
   * POST /v1/group-orders/{orderId}/leave
   * @param {string} orderId - 群订单ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   */
  leave: (orderId, data) => post(`/v1/group-orders/${orderId}/leave`, data),

  /**
   * GROUP-005: 获取可选菜品列表
   * GET /v1/group-orders/{orderId}/dishes
   * @param {string} orderId - 群订单ID
   * @param {Object} params - 查询参数
   * @param {string} params.category - 分类筛选
   * @param {string} params.keyword - 搜索关键词
   */
  getAvailableDishes: (orderId, params) => get(`/v1/group-orders/${orderId}/dishes`, params),

  /**
   * GROUP-006: 保存用户选择的菜品
   * POST /v1/group-orders/{orderId}/selections
   * @param {string} orderId - 群订单ID
   * @param {Object} data - 选择数据
   * @param {string} data.userId - 用户ID
   * @param {Array} data.dishes - 菜品列表
   * @param {string} data.dishes[].dishId - 菜品ID
   * @param {number} data.dishes[].quantity - 数量
   * @param {Object} data.dishes[].specification - 规格信息
   */
  saveSelections: (orderId, data) => post(`/v1/group-orders/${orderId}/selections`, data),

  /**
   * 获取用户选择的菜品
   * GET /v1/group-orders/{orderId}/selections/{userId}
   * @param {string} orderId - 群订单ID
   * @param {string} userId - 用户ID
   */
  getUserSelections: (orderId, userId) => get(`/v1/group-orders/${orderId}/selections/${userId}`),

  /**
   * GROUP-003: 获取订单结算信息
   * GET /v1/group-orders/{orderId}/settlement
   * @param {string} orderId - 群订单ID
   */
  getSettlement: (orderId) => get(`/v1/group-orders/${orderId}/settlement`),

  /**
   * GROUP-004: 处理群订单支付
   * POST /v1/group-orders/{orderId}/pay
   * @param {string} orderId - 群订单ID
   * @param {Object} data - 支付数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.paymentType - 支付类型（single/all）
   * @param {string} data.paymentMethod - 支付方式（wechat/alipay/balance）
   * @param {string} data.couponId - 优惠券ID（可选）
   */
  pay: (orderId, data) => post(`/v1/group-orders/${orderId}/pay`, data),

  /**
   * 支付单个成员订单
   * POST /v1/group-orders/{orderId}/members/{memberId}/pay
   * @param {string} orderId - 群订单ID
   * @param {string} memberId - 成员ID
   * @param {Object} data - 支付数据
   * @param {string} data.paymentMethod - 支付方式
   * @param {string} data.couponId - 优惠券ID（可选）
   */
  payMember: (orderId, memberId, data) => post(`/v1/group-orders/${orderId}/members/${memberId}/pay`, data),

  /**
   * GROUP-010: 取消群订单
   * POST /v1/group-orders/{orderId}/cancel
   * @param {string} orderId - 群订单ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.reason - 取消原因
   */
  cancel: (orderId, data) => post(`/v1/group-orders/${orderId}/cancel`, data),

  /**
   * 添加菜品到群订单（群订单聊天中使用）
   * POST /v1/group-orders/{orderId}/dishes
   * @param {string} orderId - 群订单ID
   * @param {Object} data - 菜品数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.dishId - 菜品ID
   * @param {number} data.quantity - 数量
   */
  addDish: (orderId, data) => post(`/v1/group-orders/${orderId}/dishes`, data),

  /**
   * 获取群订单成员列表
   * GET /v1/group-orders/{orderId}/members
   * @param {string} orderId - 群订单ID
   */
  getMembers: (orderId) => get(`/v1/group-orders/${orderId}/members`),

  /**
   * 更新群订单信息
   * PUT /v1/group-orders/{orderId}
   * @param {string} orderId - 群订单ID
   * @param {Object} data - 更新数据
   */
  update: (orderId, data) => put(`/v1/group-orders/${orderId}`, data),

  /**
   * 删除群订单
   * DELETE /v1/group-orders/{orderId}
   * @param {string} orderId - 群订单ID
   */
  delete: (orderId) => del(`/v1/group-orders/${orderId}`)
}

export default groupOrderApi
