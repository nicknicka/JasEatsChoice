/**
 * AI 卡片 Markdown 消毒边界
 * 禁止 Markdown 中的原始 HTML，统一输出安全的展示用 HTML。
 */
import MarkdownIt from 'markdown-it'

const markdownRenderer = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true
})

export function sanitizeAICardMarkdown(rawContent) {
  if (rawContent === null || rawContent === undefined) return ''

  return markdownRenderer.render(String(rawContent)).trim()
}
