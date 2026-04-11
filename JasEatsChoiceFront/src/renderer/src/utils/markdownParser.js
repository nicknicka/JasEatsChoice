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
  breaks: false,      // 关闭换行符转 <br>，避免列表项之间出现多余空行
  linkify: true,      // 自动识别链接
  typographer: false, // 不做排版替换（保持中文标点）
})

/**
 * 预处理 Markdown 文本，压缩多余的空行
 * 解决 LLM 返回内容中列表项之间多余换行导致的页面过长问题
 *
 * @param {string} text - 原始 Markdown 文本
 * @returns {string} 处理后的文本
 */
function preprocessMarkdown(text) {
  if (!text) return ''

  // 1. 压缩连续3个及以上的换行为2个换行（保留一个空行）
  // 例如："\n\n\n" -> "\n\n"
  let result = text.replace(/\n{3,}/g, '\n\n')

  // 1.1 压缩段落和列表之间多余的空行，避免列表前出现过大的视觉留白
  result = result.replace(/\n{3,}(\s*(?:[-*]|\d+\.)\s)/g, '\n\n$1')

  // 2. 处理列表项前后的多余空行
  // 列表项前面最多保留一个空行
  result = result.replace(/\n{2,}(\s*[-*]\s)/g, '\n\n$1')
  // 有序列表同理
  result = result.replace(/\n{2,}(\s*\d+\.\s)/g, '\n\n$1')

  // 3. 列表项后面不要有多余空行（列表项之间只保留单个换行）
  // 匹配：列表行 + 多个换行 + 列表行 -> 列表行 + 单个换行 + 列表行
  result = result.replace(/(\s*[-*]\s[^\n]+)\n{2,}(\s*[-*]\s)/g, '$1\n$2')
  result = result.replace(/(\s*\d+\.\s[^\n]+)\n{2,}(\s*\d+\.\s)/g, '$1\n$2')

  return result
}

/**
 * 解析 Markdown 文本为 HTML
 * @param {string} text - Markdown 文本
 * @returns {string} HTML 字符串
 */
export function parseMarkdown(text) {
  if (!text) return ''
  // 预处理：压缩多余空行
  const processed = preprocessMarkdown(text)
  return md.render(processed)
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
