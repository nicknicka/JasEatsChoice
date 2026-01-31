/**
 * 表单验证工具
 */

/**
 * 验证密码复杂度
 * 规则：
 * 1. 长度至少8位
 * 2. 包含大写字母
 * 3. 包含小写字母
 * 4. 包含数字
 * 5. 包含特殊字符
 */
export function validatePassword(password) {
  const errors = []

  if (password.length < 8) {
    errors.push('密码长度至少为8位')
  }

  if (!/[A-Z]/.test(password)) {
    errors.push('密码必须包含至少一个大写字母')
  }

  if (!/[a-z]/.test(password)) {
    errors.push('密码必须包含至少一个小写字母')
  }

  if (!/[0-9]/.test(password)) {
    errors.push('密码必须包含至少一个数字')
  }

  if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
    errors.push('密码必须包含至少一个特殊字符')
  }

  return {
    valid: errors.length === 0,
    errors
  }
}

/**
 * 验证手机号
 */
export function validatePhone(phone) {
  const reg = /^1[3-9]\d{9}$/
  return {
    valid: reg.test(phone),
    message: reg.test(phone) ? '' : '请输入正确的手机号'
  }
}

/**
 * 验证邮箱
 */
export function validateEmail(email) {
  const reg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return {
    valid: reg.test(email),
    message: reg.test(email) ? '' : '请输入正确的邮箱地址'
  }
}

/**
 * 验证用户名
 * 规则：4-20位，只能包含字母、数字、下划线
 */
export function validateUsername(username) {
  const reg = /^[a-zA-Z0-9_]{4,20}$/
  return {
    valid: reg.test(username),
    message: reg.test(username) ? '' : '用户名长度4-20位，只能包含字母、数字、下划线'
  }
}

/**
 * 强度等级计算
 * 返回：0-弱, 1-中, 2-强
 */
export function getPasswordStrength(password) {
  let strength = 0

  if (password.length >= 8) strength++
  if (password.length >= 12) strength++
  if (/[A-Z]/.test(password) && /[a-z]/.test(password)) strength++
  if (/[0-9]/.test(password)) strength++
  if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) strength++

  if (strength <= 2) return 0
  if (strength <= 3) return 1
  return 2
}

/**
 * 获取密码强度文本
 */
export function getPasswordStrengthText(strength) {
  const texts = ['弱', '中', '强']
  return texts[strength] || '未知'
}

/**
 * 获取密码强度颜色
 */
export function getPasswordStrengthColor(strength) {
  const colors = ['#f56c6c', '#e6a23c', '#67c23a']
  return colors[strength] || '#909399'
}
