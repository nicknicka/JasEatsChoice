/**
 * 权限指令
 * 用法：v-permission="'admin:user:delete'"
 *      v-permission="['admin:user:edit', 'admin:user:delete']"
 */
import { hasPermission, hasAnyPermission } from '@/utils/auth'

export default {
  mounted(el, binding) {
    const { value } = binding

    if (value) {
      let hasAuth = false

      if (Array.isArray(value)) {
        // 数组形式，有任一权限即可
        hasAuth = hasAnyPermission(value)
      } else if (typeof value === 'string') {
        // 字符串形式，需要该权限
        hasAuth = hasPermission(value)
      }

      if (!hasAuth) {
        // 没有权限，移除元素
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error('需要指定权限！如：v-permission="\'admin:user:delete\'"')
    }
  }
}
