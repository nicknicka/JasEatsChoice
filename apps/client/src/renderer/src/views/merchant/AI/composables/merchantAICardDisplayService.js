/**
 * 商家AI卡片展示安全边界
 */
import { useUniCardParser } from '../../../user/AI/composables/useUniCardParser.js'

const FALLBACK_TEXT = '卡片内容暂不可用'

const looksLikeJSON = (text) => {
  const trimmedText = text.trim()
  if (!trimmedText) return false

  return (
    (trimmedText.startsWith('{') && trimmedText.endsWith('}')) ||
    (trimmedText.startsWith('[') && trimmedText.endsWith(']'))
  )
}

export function sanitizeMerchantAICardData(value) {
  if (Array.isArray(value)) {
    return value.map(sanitizeMerchantAICardData)
  }

  if (!value || typeof value !== 'object') {
    return value
  }

  return Object.fromEntries(
    Object.entries(value)
      .filter(([key]) => key !== 'actions')
      .map(([key, item]) => [key, sanitizeMerchantAICardData(item)])
  )
}

export function createMerchantAICardViewModel(rawCard, fallbackText = '') {
  const parseResult = useUniCardParser().parseCardData(rawCard)

  if (!parseResult.parsed || !parseResult.card) {
    const safeText = typeof fallbackText === 'string' ? fallbackText.trim() : ''
    return {
      kind: 'text',
      text: safeText && !looksLikeJSON(safeText) ? safeText : FALLBACK_TEXT
    }
  }

  return {
    kind: 'card',
    card: sanitizeMerchantAICardData(parseResult.card)
  }
}
