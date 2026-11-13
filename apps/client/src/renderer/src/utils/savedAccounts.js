/**
 * 「保存的账号」只持久化手机号，密码永不落盘（见 #567）。
 *
 * 历史版本把登录密码明文写进 electron-store / Web 版 localStorage，
 * 任何一次 XSS 都能把账号密码整批取走。这里统一归一化，读回时顺手清掉旧字段。
 */

const extractPhone = (account) => {
  if (typeof account === 'string') return account.trim()
  if (!account || typeof account !== 'object') return ''
  const phone = account.phone || account.username || ''
  return typeof phone === 'string' ? phone.trim() : ''
}

/** 归一化账号列表：只保留 { phone }，丢弃 password 等一切旧字段并去重。 */
export const sanitizeSavedAccounts = (accounts) => {
  if (!Array.isArray(accounts)) return []

  const seen = new Set()
  const result = []
  accounts.forEach((account) => {
    const phone = extractPhone(account)
    if (!phone || seen.has(phone)) return
    seen.add(phone)
    result.push({ phone })
  })
  return result
}

/** 登录成功后记住账号：只追加手机号，不动已有条目。 */
export const rememberAccount = (accounts, phone) => {
  const list = sanitizeSavedAccounts(accounts)
  const value = typeof phone === 'string' ? phone.trim() : ''
  if (!value || list.some((account) => account.phone === value)) return list
  return [...list, { phone: value }]
}

/** 删除一个已保存的账号。 */
export const forgetAccount = (accounts, phone) =>
  sanitizeSavedAccounts(accounts).filter((account) => account.phone !== phone)

export default { sanitizeSavedAccounts, rememberAccount, forgetAccount }
