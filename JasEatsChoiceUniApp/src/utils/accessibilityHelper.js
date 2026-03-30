/**
 * 无障碍辅助工具
 * 用于提升应用的可访问性
 */
class AccessibilityHelper {
  /**
   * 为元素设置无障碍标签
   * @param {string} label - 无障碍标签
   * @param {string} hint - 提示信息
   */
  static setAccessibilityProps(label, hint) {
    return {
      'aria-label': label,
      'aria-hint': hint
    }
  }

  /**
   * 设置按钮的无障碍属性
   * @param {string} label - 按钮标签
   * @param {string} role - 角色，默认为button
   */
  static setButtonProps(label, role = 'button') {
    return {
      role,
      'aria-label': label
    }
  }

  /**
   * 设置图片的无障碍属性
   * @param {string} alt - 替代文本
   */
  static setImageProps(alt) {
    return {
      alt,
      role: 'img'
    }
  }

  /**
   * 设置输入框的无障碍属性
   * @param {string} label - 标签
   * @param {boolean} required - 是否必填
   */
  static setInputProps(label, required = false) {
    return {
      role: 'textbox',
      'aria-label': label,
      'aria-required': required
    }
  }

  /**
   * 为列表项设置无障碍属性
   * @param {number} index - 索引
   * @param {number} total - 总数
   */
  static setListItemProps(index, total) {
    return {
      role: 'listitem',
      'aria-setsize': total,
      'aria-posinset': index + 1
    }
  }

  /**
   * 设置实时区域的属性
   * @param {string} status - 状态
   */
  static setLiveRegion(status = 'polite') {
    return {
      'aria-live': status,
      'aria-atomic': 'true'
    }
  }

  /**
   * 为菜品卡片生成无障碍标签
   * @param {object} dish - 菜品对象
   */
  static getDishAccessibilityProps(dish) {
    const label = `${dish.name}, 价格${dish.price}元${dish.calories ? `, ${dish.calories}卡路里` : ''}`
    const hint = dish.recommendReason ? `推荐理由: ${dish.recommendReason}` : '点击查看详情'

    return {
      ...this.setButtonProps(label, 'article'),
      'aria-description': hint
    }
  }

  /**
   * 为商家卡片生成无障碍标签
   * @param {object} merchant - 商家对象
   */
  static getMerchantAccessibilityProps(merchant) {
    const label = `${merchant.name}, 评分${merchant.rating}分, 月售${merchant.monthlySales}单`
    const hint = '点击查看商家详情'

    return {
      ...this.setButtonProps(label),
      'aria-description': hint
    }
  }

  /**
   * 为分类项生成无障碍标签
   * @param {object} category - 分类对象
   */
  static getCategoryAccessibilityProps(category) {
    const label = `${category.name}分类`
    const hint = `点击浏览${category.name}类菜品`

    return {
      ...this.setButtonProps(label),
      'aria-description': hint
    }
  }

  /**
   * 检查颜色对比度（简化版）
   * @param {string} foreground - 前景色
   * @param {string} background - 背景色
   */
  static checkColorContrast(foreground, background) {
    // 这是一个简化的实现，实际项目中应使用更精确的算法
    const fg = this.hexToRgb(foreground)
    const bg = this.hexToRgb(background)

    if (!fg || !bg) return true // 如果无法解析，假设通过

    const luminance1 = (0.299 * fg.r + 0.587 * fg.g + 0.114 * fg.b) / 255
    const luminance2 = (0.299 * bg.r + 0.587 * bg.g + 0.114 * bg.b) / 255

    const ratio = (Math.max(luminance1, luminance2) + 0.05) / (Math.min(luminance1, luminance2) + 0.05)

    // WCAG AA标准要求至少4.5:1的对比度
    return ratio >= 4.5
  }

  /**
   * 十六进制颜色转RGB
   * @private
   */
  static hexToRgb(hex) {
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
    return result ? {
      r: parseInt(result[1], 16),
      g: parseInt(result[2], 16),
      b: parseInt(result[3], 16)
    } : null
  }

  /**
   * 设置焦点管理
   * @param {string} elementId - 元素ID
   */
  static setFocus(elementId) {
    // 在H5端使用
    if (typeof document !== 'undefined') {
      const element = document.getElementById(elementId)
      if (element) {
        element.focus()
      }
    }
  }

  /**
   * 朗读文本（屏幕阅读器支持）
   * @param {string} text - 要朗读的文本
   */
  static announce(text) {
    // 在支持的环境中使用
    if (typeof uni !== 'undefined' && uni.getSystemInfoSync().platform === 'ios') {
      // iOS可以使用系统的语音播报
      // 这里只是一个占位实现
      console.log('Announce:', text)
    }
  }

  /**
   * 触觉反馈
   * @param {string} type - 反馈类型
   */
  static vibrate(type = 'light') {
    if (typeof uni !== 'undefined') {
      switch (type) {
        case 'light':
          uni.vibrateShort()
          break
        case 'heavy':
          uni.vibrateLong()
          break
        case 'success':
          uni.vibrateShort({ success: () => {
            setTimeout(() => uni.vibrateShort(), 100)
          })
          break
        case 'error':
          uni.vibrateLong({ success: () => {
            setTimeout(() => uni.vibrateShort(), 100)
          })
          break
      }
    }
  }

  /**
   * 检查触摸目标大小是否符合无障碍标准
   * @param {number} width - 宽度
   * @param {number} height - 高度
   */
  static checkTouchTargetSize(width, height) {
    // WCAG标准建议触摸目标至少为44x44 CSS像素
    // 在rpx中，我们建议至少为88x88 rpx（约44px）
    const MIN_SIZE = 88
    return width >= MIN_SIZE && height >= MIN_SIZE
  }

  /**
   * 获取推荐的无障碍配置
   */
  static getRecommendations() {
    return {
      minimumTouchTargetSize: 88, // rpx
      colorContrastRatio: 4.5, // WCAG AA
      enableVibrationFeedback: true,
      enableScreenReaderSupport: true,
      fontSize: {
        minimum: 24, // rpx, 约12px
        recommended: 28 // rpx, 约14px
      }
    }
  }
}

export default AccessibilityHelper
