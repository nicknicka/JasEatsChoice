/**
 * 想吃列表 API（用户 + 商家）
 * 对接后端 WishListController，基础路径 /v1/wish-list（连字符）
 * 桌面端同源参照实现：apps/client/src/renderer/src/api/wishList.js
 */
import { get, post, del } from '@/utils/request'
import { WISH_LIST_API, buildUrl } from '../urlEnum'

/**
 * 拒绝原因枚举
 *
 * 逐项对齐后端 RejectionReason 枚举
 * （apps/server/src/main/java/com/xx/jaseatschoicejava/enums/RejectionReason.java）。
 * 后端 auditWishListItem 在 approved=false 时校验 rejectionReasonCode 必填，
 * 缺失会抛「拒绝时必须选择拒绝原因」，所以拒绝必须从这份列表里带一个 code 过去。
 * 后端新增/调整枚举项时这里要同步。
 */
export const WISH_REJECTION_REASONS = [
  { code: 1, title: '食材季节性短缺' },
  { code: 2, title: '食材供应链问题' },
  { code: 3, title: '制作工艺过于复杂' },
  { code: 4, title: '成本过高' },
  { code: 5, title: '与餐厅定位不符' },
  { code: 6, title: '食品安全考虑' },
  { code: 7, title: '原料品质不稳定' },
  { code: 8, title: '制作时间过长' },
  { code: 9, title: '特殊设备限制' },
  { code: 99, title: '其他原因' }
]

/** 「其他原因」的 code，选中它时页面会额外让商家填说明 */
export const OTHER_REJECTION_CODE = 99

export const wishApi = {
  /**
   * 获取当前用户的想吃列表
   * GET /v1/wish-list/items
   */
  getUserItems: () => get(WISH_LIST_API.GET_USER_ITEMS),

  /**
   * 获取想吃列表项详情
   * GET /v1/wish-list/item/{itemId}
   * @param {string} itemId - 列表项ID
   */
  getDetail: (itemId) => get(buildUrl(WISH_LIST_API.GET_DETAIL, { itemId })),

  /**
   * 获取商家待审核列表
   * GET /v1/wish-list/merchant/pending
   * 无查询参数：商家身份由后端从请求身份（token）取
   */
  getMerchantPending: () => get(WISH_LIST_API.GET_MERCHANT_PENDING),

  /**
   * 审核想吃列表项（受理/拒绝同一个端点）
   * POST /v1/wish-list/audit
   * @param {string} itemId - 列表项ID
   * @param {Object} data - { approved, rejectionReasonCode, rejectionReason, auditRemark, actualAvailableTime }
   */
  audit: (itemId, data) => post(WISH_LIST_API.AUDIT, { wishListItemId: itemId, ...data }),

  /**
   * 受理想吃列表项（approved: true）
   * @param {string} itemId - 列表项ID
   * @param {Object} data - 可选 { actualAvailableTime }
   */
  approve: (itemId, data = {}) =>
    post(WISH_LIST_API.AUDIT, { wishListItemId: itemId, approved: true, ...data }),

  /**
   * 拒绝想吃列表项（approved: false，rejectionReasonCode 必填）
   * @param {string} itemId - 列表项ID
   * @param {Object} data - { rejectionReasonCode, rejectionReason, auditRemark }
   */
  reject: (itemId, data = {}) =>
    post(WISH_LIST_API.AUDIT, { wishListItemId: itemId, approved: false, ...data }),

  /**
   * 创建想吃列表项
   * POST /v1/wish-list/item
   */
  create: (data) => post(WISH_LIST_API.CREATE, data),

  /**
   * 用户申诉被拒绝的想吃列表项
   * POST /v1/wish-list/appeal
   * @param {string} itemId - 列表项ID
   * @param {Object} data - { appealContent }
   */
  appeal: (itemId, data = {}) =>
    post(WISH_LIST_API.APPEAL, { wishListItemId: itemId, ...data }),

  /**
   * 用户撤回想吃列表项
   * DELETE /v1/wish-list/item/{itemId}
   * @param {string} itemId - 列表项ID
   */
  withdraw: (itemId) => del(buildUrl(WISH_LIST_API.WITHDRAW, { itemId }))
}

export default wishApi
