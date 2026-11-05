/**
 * 北欧暖光主题 - JavaScript 常量
 * 颜色统一从 tokens.css 的 CSS 变量读取，供 <script> 中需要动态引用主题值的场景使用
 */

const readToken = (name) =>
  getComputedStyle(document.documentElement).getPropertyValue(name).trim()

// --- 颜色 ---
export const NORDIC_COLORS = {
  bg: readToken('--canvas'),
  surface: readToken('--surface'),
  white: readToken('--surface'),
  text: readToken('--ink-900'),
  textSecondary: readToken('--ink-500'),
  textMuted: readToken('--ink-400'),
  accent: readToken('--brand-500'),
  accentLight: readToken('--brand-100'),
  accentDark: readToken('--brand-600'),
  green: readToken('--success'),
  greenLight: readToken('--success-bg'),
  greenDark: readToken('--success'),
  blue: readToken('--info'),
  blueLight: readToken('--info-bg'),
  yellow: readToken('--warning'),
  yellowLight: readToken('--warning-bg'),
  yellowDark: readToken('--warning'),
  red: readToken('--danger'),
  redLight: readToken('--danger-bg'),
  border: readToken('--line'),
  divider: readToken('--line'),
  shadow: readToken('--shadow-sm'),
  shadowHover: readToken('--shadow-md')
}

// 营养素颜色映射
export const NORDIC_NUTRITION_COLORS = {
  protein: NORDIC_COLORS.blue,
  carbs: NORDIC_COLORS.green,
  fat: NORDIC_COLORS.yellow
}

// 营养素背景色映射
export const NORDIC_NUTRITION_BG = {
  protein: NORDIC_COLORS.blueLight,
  carbs: NORDIC_COLORS.greenLight,
  fat: NORDIC_COLORS.yellowLight
}

// --- 间距 ---
export const NORDIC_SPACE = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  '2xl': 48
}

// --- 字号 ---
export const NORDIC_FONT_SIZE = {
  xs: 12,
  sm: 13,
  base: 14,
  md: 16,
  lg: 20,
  xl: 28,
  '2xl': 36
}

// --- 圆角 ---
export const NORDIC_RADIUS = {
  xs: 3,
  sm: 6,
  md: 10,
  lg: 16,
  pill: 50
}
