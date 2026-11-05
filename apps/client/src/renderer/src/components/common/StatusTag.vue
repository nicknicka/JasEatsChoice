<template>
  <span class="status-tag" :class="[`status-tag--${meta.type}`, `status-tag--${size}`]">
    {{ text || meta.text }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    required: true
  },
  text: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md'].includes(value)
  }
})

const NEUTRAL_META = Object.freeze({ type: 'neutral', text: '无状态' })

// 业务状态词典：type 决定语义色，text 是缺省中文文案。
// key 统一去掉下划线和连字符，兼容 pending_review / pendingReview / PENDING_REVIEW。
const STATUS_DICT = Object.freeze({
  // success：已完成、已通过、已上架、成功
  approved: { type: 'success', text: '已通过' },
  autoapproved: { type: 'success', text: '超时自动通过' },
  appealapproved: { type: 'success', text: '申诉成功' },
  completed: { type: 'success', text: '已完成' },
  complete: { type: 'success', text: '已完成' },
  delivered: { type: 'success', text: '已送达' },
  success: { type: 'success', text: '成功' },
  published: { type: 'success', text: '已发布' },
  online: { type: 'success', text: '已上架' },
  active: { type: 'success', text: '已启用' },
  available: { type: 'success', text: '可用' },
  received: { type: 'success', text: '已领取' },
  open: { type: 'success', text: '营业中' },
  enabled: { type: 'success', text: '已启用' },
  replied: { type: 'success', text: '已回复' },
  sent: { type: 'success', text: '已发送' },
  read: { type: 'success', text: '已读' },
  saved: { type: 'success', text: '已保存' },
  synced: { type: 'success', text: '已同步' },
  healthy: { type: 'success', text: '正常' },
  normal: { type: 'success', text: '正常' },
  good: { type: 'success', text: '良好' },
  paid: { type: 'success', text: '已支付' },
  reviewed: { type: 'success', text: '已评价' },
  resolved: { type: 'success', text: '已解决' },
  served: { type: 'success', text: '已上菜' },
  fastfoodserved: { type: 'success', text: '已出餐' },
  fulfilled: { type: 'success', text: '已履约' },

  // warning：待处理、待审核、待付款
  pending: { type: 'warning', text: '待处理' },
  pendingpayment: { type: 'warning', text: '待付款' },
  unpaid: { type: 'warning', text: '待付款' },
  unreplied: { type: 'warning', text: '待回复' },
  unread: { type: 'warning', text: '未读' },
  pendingaccept: { type: 'warning', text: '待接单' },
  pendingacceptance: { type: 'warning', text: '待接单' },
  pendingreview: { type: 'warning', text: '待审核' },
  pendingaudit: { type: 'warning', text: '待审核' },
  pendingpreparation: { type: 'warning', text: '待备菜' },
  pendingcomment: { type: 'warning', text: '待评价' },
  approvedpendingpayment: { type: 'warning', text: '待付款' },
  waitingforserving: { type: 'warning', text: '待上菜' },
  fastfoodready: { type: 'warning', text: '待出餐' },
  almostsold: { type: 'warning', text: '即将售罄' },
  soldout: { type: 'warning', text: '已售罄' },
  outofstock: { type: 'warning', text: '缺货' },
  busy: { type: 'warning', text: '忙碌中' },
  lack: { type: 'warning', text: '资源不足' },
  retryable: { type: 'warning', text: '可重试' },
  warning: { type: 'warning', text: '警告' },

  // danger：已驳回、已取消、失败
  rejected: { type: 'danger', text: '已驳回' },
  timeoutrejected: { type: 'danger', text: '超时驳回' },
  appealrejected: { type: 'danger', text: '申诉失败' },
  cancelled: { type: 'danger', text: '已取消' },
  canceled: { type: 'danger', text: '已取消' },
  failed: { type: 'danger', text: '失败' },
  fail: { type: 'danger', text: '失败' },
  error: { type: 'danger', text: '异常' },
  critical: { type: 'danger', text: '严重' },
  unhealthy: { type: 'danger', text: '异常' },
  parsefailed: { type: 'danger', text: '解析失败' },
  blocked: { type: 'danger', text: '已拉黑' },
  timeout: { type: 'danger', text: '超时' },

  // info：进行中、已受理
  processing: { type: 'info', text: '进行中' },
  confirmed: { type: 'info', text: '已接单' },
  ready: { type: 'info', text: '备餐完成' },
  delivering: { type: 'info', text: '配送中' },
  executing: { type: 'info', text: '执行中' },
  thinking: { type: 'info', text: '思考中' },
  sending: { type: 'info', text: '发送中' },
  loading: { type: 'info', text: '加载中' },
  preparing: { type: 'info', text: '进行中' },
  cooking: { type: 'info', text: '进行中' },
  plating: { type: 'info', text: '进行中' },
  preprocessing: { type: 'info', text: '进行中' },
  fastfoodmaking: { type: 'info', text: '进行中' },
  fastfoodpacking: { type: 'info', text: '进行中' },
  accepted: { type: 'info', text: '已受理' },
  reviewing: { type: 'info', text: '审核中' },
  appealing: { type: 'info', text: '申诉中' },
  inprogress: { type: 'info', text: '进行中' },
  ongoing: { type: 'info', text: '进行中' },

  // neutral：已关闭、草稿、无状态、已下架/停用
  closed: { type: 'neutral', text: '已关闭' },
  draft: { type: 'neutral', text: '草稿' },
  inactive: { type: 'neutral', text: '已停用' },
  offline: { type: 'neutral', text: '已下架' },
  disabled: { type: 'neutral', text: '已停用' },
  locked: { type: 'neutral', text: '已锁定' },
  notsubmitted: { type: 'neutral', text: '未提交' },
  authrequired: { type: 'neutral', text: '需要登录' },
  withdrawn: { type: 'neutral', text: '已撤回' },
  expired: { type: 'neutral', text: '已过期' },
  used: { type: 'neutral', text: '已使用' },
  unused: { type: 'neutral', text: '未使用' },
  refund: { type: 'neutral', text: '已退款' },
  refunded: { type: 'neutral', text: '已退款' },
  unavailable: { type: 'neutral', text: '不可用' },
  deleted: { type: 'neutral', text: '已删除' },
  paused: { type: 'neutral', text: '已暂停' },
  none: { type: 'neutral', text: '无状态' },
  unknown: { type: 'neutral', text: '无状态' }
})

const toLookupKey = (status) =>
  String(status)
    .trim()
    .toLowerCase()
    .replace(/[\s_-]/g, '')

const meta = computed(() => STATUS_DICT[toLookupKey(props.status)] || NEUTRAL_META)
</script>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: fit-content;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-family: var(--font-sans);
  /* §3.2 小字号档：md 13px、sm 12px；字号变量尚未落地 tokens.css，先同值兜底 */
  font-size: var(--text-body-sm, 13px);
  line-height: 20px;
  white-space: nowrap;
}

.status-tag--sm {
  font-size: var(--text-caption, 12px);
  line-height: 18px;
}

.status-tag--success {
  color: var(--success);
  background: var(--success-bg);
}

.status-tag--warning {
  color: var(--warning);
  background: var(--warning-bg);
}

.status-tag--danger {
  color: var(--danger);
  background: var(--danger-bg);
}

.status-tag--info {
  color: var(--info);
  background: var(--info-bg);
}

.status-tag--neutral {
  color: var(--neutral);
  background: var(--neutral-bg);
}
</style>
