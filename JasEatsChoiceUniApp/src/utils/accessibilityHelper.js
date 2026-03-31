/**
 * 可访问性辅助工具
 *
 * 功能：
 * - ARIA 标签生成
 * - 触摸区域规范
 * - 焦点管理
 * - 屏幕阅读器支持
 *
 * @author Claude
 * @date 2026-03-31
 */

/**
 * 生成 ARIA 标签属性
 */
export function generateAriaProps(role, label, description) {
	const props = {
		role: role,
		'aria-label': label
	}

	if (description) {
		props['aria-description'] = description
	}

	return props
}

/**
 * 验证触摸区域是否符合 WCAG 标准
 */
export function validateTouchArea(width, height) {
	const MIN_SIZE = 48 // 最小 48rpx
	return width >= MIN_SIZE && height >= MIN_SIZE
}

/**
 * 生成符合规范的触摸区域样式
 */
export function getTouchAreaStyle(size = 96) {
	const finalSize = Math.max(size, 96)
	return {
		width: \`\${finalSize}rpx\`,
		height: \`\${finalSize}rpx\`,
		minWidth: \`\${finalSize}rpx\`,
		minHeight: \`\${finalSize}rpx\`
	}
}

/**
 * 焦点管理辅助类
 */
export class FocusManager {
	constructor() {
		this.focusableElements = []
		this.currentFocusIndex = -1
	}

	collectFocusableElements(container) {
		const selectors = [
			'button:not([disabled])',
			'[role="button"]:not([disabled])',
			'input:not([disabled])',
			'a[href]',
			'[tabindex]:not([tabindex="-1"])'
		]
		this.focusableElements = Array.from(
			container.querySelectorAll(selectors.join(','))
		)
	}

	focusFirst() {
		if (this.focusableElements.length > 0) {
			this.focusableElements[0].focus()
			this.currentFocusIndex = 0
		}
	}

	clear() {
		this.focusableElements = []
		this.currentFocusIndex = -1
	}
}

/**
 * 生成无障碍提示文本
 */
export function getAccessibleText(text) {
	const emojiMap = {
		'🤖': 'AI助手',
		'👤': '用户',
		'💬': '聊天',
		'✕': '关闭'
	}
	
	let accessibleText = text
	for (const [emoji, desc] of Object.entries(emojiMap)) {
		accessibleText = accessibleText.replace(new RegExp(emoji, 'g'), desc)
	}
	return accessibleText
}
