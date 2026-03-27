/**
 * 简单的Markdown解析器
 * 用于渲染AI回复中的格式化文本
 */

/**
 * 解析Markdown文本为HTML
 * @param {string} text - Markdown文本
 * @returns {string} HTML字符串
 */
export function parseMarkdown(text) {
  if (!text) return ''

  let html = text

  // 转义HTML特殊字符（防止XSS）
  html = escapeHtml(html)

  // 代码块 (```code```)
  html = html.replace(/```(\w+)?\n([\s\S]*?)```/g, (match, lang, code) => {
    return `<pre><code class="language-${lang || 'text'}">${code.trim()}</code></pre>`
  })

  // 行内代码 (`code`)
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>')

  // 粗体 (**text** 或 __text__)
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/__([^_]+)__/g, '<strong>$1</strong>')

  // 斜体 (*text* 或 _text_)
  html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>')
  html = html.replace(/_([^_]+)_/g, '<em>$1</em>')

  // 删除线 (~~text~~)
  html = html.replace(/~~([^~]+)~~/g, '<del>$1</del>')

  // 标题 (# H1 to ###### H6)
  html = html.replace(/^######\s+(.+)$/gm, '<h6>$1</h6>')
  html = html.replace(/^#####\s+(.+)$/gm, '<h5>$1</h5>')
  html = html.replace(/^####\s+(.+)$/gm, '<h4>$1</h4>')
  html = html.replace(/^###\s+(.+)$/gm, '<h3>$1</h3>')
  html = html.replace(/^##\s+(.+)$/gm, '<h2>$1</h2>')
  html = html.replace(/^#\s+(.+)$/gm, '<h1>$1</h1>')

  // 预处理：修复粘连的有序列表
  // 在非换行符后的数字序号前添加换行
  // 例如："推荐：1. xxx2. xxx" → "推荐：1. xxx\n2. xxx"
  html = html.replace(/([^\n])(\d+\.\s+)/g, '$1\n$2')

  // 无序列表 (- item 或 * item)
  html = html.replace(/^[\-\*]\s+(.+)$/gm, '<li>$1</li>')
  html = html.replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>')

  // 有序列表 (1. item)
  html = html.replace(/^\d+\.\s+(.+)$/gm, '<li>$1</li>')
  html = html.replace(/(<li>.*<\/li>\n?)+/g, '<ol>$&</ol>')

  // 引用 (> text)
  html = html.replace(/^>\s+(.+)$/gm, '<blockquote>$1</blockquote>')

  // 水平线 (--- 或 ***)
  html = html.replace(/^(\-{3,}|\*{3,})$/gm, '<hr>')

  // 表格 (| col1 | col2 | ...)
  // 匹配表格头部和分隔行
  html = html.replace(
    /^(\|.+?\|)\s*\n(\|[-:\s|]+\|)\s*\n((?:\|.+?\|\s*\n?)+)/gm,
    (match, header, separator, body) => {
      // 解析表头
      const headers = header
        .split('|')
        .filter((cell) => cell.trim() !== '')
        .map((cell) => cell.trim())
      // 解析表格内容
      const rows = body
        .trim()
        .split('\n')
        .map((row) => {
          return row
            .split('|')
            .filter((cell) => cell.trim() !== '')
            .map((cell) => cell.trim())
        })

      // 构建HTML表格
      let tableHtml = '<table><thead><tr>'
      headers.forEach((h) => {
        tableHtml += `<th>${h}</th>`
      })
      tableHtml += '</tr></thead><tbody>'

      rows.forEach((row) => {
        tableHtml += '<tr>'
        row.forEach((cell) => {
          tableHtml += `<td>${cell}</td>`
        })
        tableHtml += '</tr>'
      })

      tableHtml += '</tbody></table>'
      return tableHtml
    }
  )

  // 链接 ([text](url))
  html = html.replace(
    /\[([^\]]+)\]\(([^)]+)\)/g,
    '<a href="$2" target="_blank" rel="noopener">$1</a>'
  )

  // 图片 (![alt](url))
  html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" />')

  // 换行处理（连续的换行视为段落分隔）
  // 先将连续的两个或更多换行符替换为段落分隔
  html = html.replace(/\n{2,}/g, '</p><p>')
  // 然后将单个换行符替换为br
  html = html.replace(/\n/g, '<br>')

  // 包裹在段落标签中
  html = '<p>' + html + '</p>'

  return html
}

/**
 * 转义HTML特殊字符
 * @param {string} text - 要转义的文本
 * @returns {string} 转义后的文本
 */
function escapeHtml(text) {
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  }
  return text.replace(/[&<>"']/g, (m) => map[m])
}

/**
 * 检测文本是否包含Markdown语法
 * @param {string} text - 要检测的文本
 * @returns {boolean} 是否包含Markdown语法
 */
export function hasMarkdownSyntax(text) {
  const markdownPatterns = [
    /```/, // 代码块
    /`[^`]+`/, // 行内代码
    /\*\*[^*]+\*\*/, // 粗体
    /__[^_]+__/, // 粗体
    /\*[^*]+\*/, // 斜体
    /_[^_]+_/, // 斜体
    /~~[^~]+~~/, // 删除线
    /^#{1,6}\s/, // 标题
    /^\s*[-*]\s/, // 无序列表
    /^\s*\d+\.\s/, // 有序列表
    /^\s*>/, // 引用
    /\[.*\]\(.*\)/, // 链接
    /!\[.*\]\(.*\)/ // 图片
  ]

  return markdownPatterns.some((pattern) => pattern.test(text))
}

/**
 * 提取纯文本（移除Markdown语法）
 * @param {string} markdown - Markdown文本
 * @returns {string} 纯文本
 */
export function stripMarkdown(markdown) {
  if (!markdown) return ''

  let text = markdown

  // 移除代码块
  text = text.replace(/```[\s\S]*?```/g, '')

  // 移除行内代码
  text = text.replace(/`[^`]+`/g, '')

  // 移除链接
  text = text.replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')

  // 移除图片
  text = text.replace(/!\[([^\]]*)\]\([^)]+\)/g, '$1')

  // 移除粗体、斜体、删除线标记
  text = text.replace(/\*\*([^*]+)\*\*/g, '$1')
  text = text.replace(/__([^_]+)__/g, '$1')
  text = text.replace(/\*([^*]+)\*/g, '$1')
  text = text.replace(/_([^_]+)_/g, '$1')
  text = text.replace(/~~([^~]+)~~/g, '$1')

  // 移除标题标记
  text = text.replace(/^#{1,6}\s+/gm, '')

  // 移除引用标记
  text = text.replace(/^>\s+/gm, '')

  // 移除列表标记
  text = text.replace(/^[\-\*]\s+/gm, '')
  text = text.replace(/^\d+\.\s+/gm, '')

  return text
}
