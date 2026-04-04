/**
 * Markdown 解析器（基于 markdown-it）
 *
 * 使用 markdown-it 替代自定义正则解析器，
 * 解决两位数有序列表渲染、嵌套列表、复杂格式等边界问题。
 */
import MarkdownIt from 'markdown-it'

// 创建 markdown-it 实例
const md = new MarkdownIt({
  html: false,        // 禁止原始 HTML（安全考虑）
  breaks: true,       // 支持换行符转 <br>
  linkify: true,      // 自动识别链接
  typographer: false, // 不做排版替换（保持中文标点）
})

/**
 * 解析 Markdown 文本为 HTML
 * @param {string} text - Markdown 文本
 * @returns {string} HTML 字符串
 */
export function parseMarkdown(text) {
  if (!text) return ''
  return md.render(text)
}

/**
 * 检测文本是否包含 Markdown 语法
 * @param {string} text - 要检测的文本
 * @returns {boolean} 是否包含 Markdown 语法
 */
export function hasMarkdownSyntax(text) {
  if (!text) return false

  const markdownPatterns = [
    /```/,          // 代码块
    /`[^`]+`/,      // 行内代码
    /\*\*[^*]+\*\*/, // 粗体
    /__[^_]+__/,    // 粗体
    /\*[^*]+\*/,    // 斜体
    /_[^_]+_/,      // 斜体
    /~~[^~]+~~/,    // 删除线
    /^#{1,6}\s/,    // 标题
    /^\s*[-*]\s/,   // 无序列表
    /^\s*\d+\.\s/,  // 有序列表
    /^\s*>/,        // 引用
    /\[.*\]\(.*\)/, // 链接
    /!\[.*\]\(.*\)/ // 图片
  ]

  return markdownPatterns.some(pattern => pattern.test(text))
}

/**
 * 提取纯文本（移除 Markdown 语法）
 * @param {string} markdown - Markdown 文本
 * @returns {string} 纯文本
 */
export function stripMarkdown(markdown) {
  if (!markdown) return ''

  let text = markdown

  // 使用 markdown-it 渲染后提取纯文本
  const html = md.render(text)
  const tmp = document.createElement('div')
  tmp.innerHTML = html
  return tmp.textContent || tmp.innerText || ''
}
