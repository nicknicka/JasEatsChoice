/**
 * 搜索高亮：把消息原文按关键词切成文本片段，交给组件用 <mark> 渲染。
 *
 * 这里只做切分，绝不生成 HTML 字符串 —— 消息原文是不可信输入，
 * 一旦拼成 HTML 再交给 v-html，就会变成存储型 XSS（见 #567）。
 */

const REGEXP_META = /[.*+?^${}()|[\]\\]/g

/** 转义正则元字符。用户输入 `(`、`*`、`\` 等字符时，RegExp 构造不能抛异常。 */
export const escapeRegExp = (value) => String(value ?? '').replace(REGEXP_META, '\\$&')

/**
 * 把 content 按 keyword（大小写不敏感）切成片段数组：
 *   [{ text, highlight }]
 * highlight 为 true 的片段由组件渲染成 <mark>，其余保持纯文本。
 */
export const buildHighlightSegments = (content, keyword) => {
  const text = typeof content === 'string' ? content : String(content ?? '')
  if (!text) return []

  const needle = typeof keyword === 'string' ? keyword : ''
  if (!needle) return [{ text, highlight: false }]

  const regex = new RegExp(escapeRegExp(needle), 'gi')
  const segments = []
  let lastIndex = 0
  let match

  while ((match = regex.exec(text)) !== null) {
    if (match.index > lastIndex) {
      segments.push({ text: text.slice(lastIndex, match.index), highlight: false })
    }
    segments.push({ text: match[0], highlight: true })
    lastIndex = match.index + match[0].length
    if (match[0].length === 0) {
      // 空匹配会原地打转，防御性推进
      regex.lastIndex += 1
    }
  }

  if (lastIndex < text.length) {
    segments.push({ text: text.slice(lastIndex), highlight: false })
  }

  return segments
}

export default { escapeRegExp, buildHighlightSegments }
