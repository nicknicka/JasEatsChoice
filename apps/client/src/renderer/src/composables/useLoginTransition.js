import { ref } from 'vue'

// 登录过渡动画状态 —— 全局单例
const isVisible = ref(false)
const isFadingOut = ref(false)
let autoHideTimer = null

export function useLoginTransition() {
  const showTransition = (maxVisibleMs = 8000) => {
    if (autoHideTimer) {
      clearTimeout(autoHideTimer)
      autoHideTimer = null
    }

    isFadingOut.value = false
    isVisible.value = true

    autoHideTimer = setTimeout(() => {
      hideTransition()
    }, maxVisibleMs)
  }

  const hideTransition = () => {
    if (autoHideTimer) {
      clearTimeout(autoHideTimer)
      autoHideTimer = null
    }

    if (!isVisible.value) {
      isFadingOut.value = false
      return
    }

    // 先触发淡出动画
    isFadingOut.value = true
    // 等待动画完成后彻底隐藏
    setTimeout(() => {
      isVisible.value = false
      isFadingOut.value = false
    }, 600)
  }

  return {
    isVisible,
    isFadingOut,
    showTransition,
    hideTransition
  }
}
