/**
 * 北欧暖光主题 - JavaScript 常量
 * 与 nordic-theme.less 中的变量保持同步
 * 供 <script> 中需要动态引用主题值的场景使用
 */

// --- 颜色 ---
export const NORDIC_COLORS = {
  bg: '#F5F3EF',
  surface: '#FFFFFF',
  white: '#FFFFFF',
  text: '#1A1A1A',
  textSecondary: '#6B6B6B',
  textMuted: '#9E9E9E',
  accent: '#D4845A',
  accentLight: '#F0D5C4',
  accentDark: '#B8704A',
  green: '#7BAE7F',
  greenLight: '#E3F0E4',
  greenDark: '#4a7a4d',
  blue: '#6B9BD2',
  blueLight: '#E0EDF6',
  yellow: '#E2B455',
  yellowLight: '#F7EDDA',
  yellowDark: '#8B6914',
  red: '#D47B7B',
  redLight: '#F6E0E0',
  border: '#E8E4DE',
  divider: '#F0ECE6',
  shadow: 'rgba(0, 0, 0, 0.06)',
  shadowHover: 'rgba(0, 0, 0, 0.1)'
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
