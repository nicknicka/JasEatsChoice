/**
 * 分享功能组合式函数
 */
import { ElNotification } from 'element-plus'
import { Dish } from '../types'
import { HOME_CONSTANTS } from '../constants/home'

export function useShare() {
  /**
   * 分享菜品
   */
  const shareDish = async (dish: Dish, event?: Event) => {
    if (event) {
      event.stopPropagation()
    }

    const shareData = {
      title: dish.name,
      text: `${dish.name} - ${dish.kcal} 卡路里`,
      url: window.location.href
    }

    try {
      if (navigator.share) {
        await navigator.share(shareData)
        showSuccess('分享成功')
      } else {
        // 降级处理:复制到剪贴板
        const shareText = `${shareData.title}\n${shareData.text}\n${shareData.url}`

        // 在Electron环境中，优先使用clipboard模块
        if (window.api && window.api.clipboard) {
          window.api.clipboard.writeText(shareText)
          showSuccess('已复制到剪贴板')
        } else {
          await navigator.clipboard.writeText(shareText)
          showSuccess('已复制到剪贴板')
        }
      }
    } catch (error) {
      if ((error as Error).name !== 'AbortError') {
        console.error('分享失败:', error)
        showError('分享失败,请重试')
      }
    }
  }

  /**
   * 显示成功提示
   */
  const showSuccess = (message: string) => {
    ElNotification.success({
      title: '成功',
      message,
      duration: HOME_CONSTANTS.TOAST_DURATION.SUCCESS
    })
  }

  /**
   * 显示错误提示
   */
  const showError = (message: string) => {
    ElNotification.error({
      title: '错误',
      message,
      duration: HOME_CONSTANTS.TOAST_DURATION.ERROR
    })
  }

  return {
    shareDish
  }
}
