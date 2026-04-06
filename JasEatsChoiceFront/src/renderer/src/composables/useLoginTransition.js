import { ref } from 'vue'

// 登录过渡动画状态 —— 全局单例
const isVisible = ref(false)
const isFadingOut = ref(false)

export function useLoginTransition() {
  const showTransition = () => {
    isFadingOut.value = false
    isVisible.value = true
  }

  const hideTransition = () => {
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
