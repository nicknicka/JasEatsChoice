/**
 * 窗口控制 composable
 * 封装 Electron 窗口尺寸切换 API
 */
export function useWindowControl() {
  const runWindowAction = async (action, timeout = 1500) => {
    if (!action) return

    try {
      await Promise.race([
        action(),
        new Promise((resolve) => setTimeout(resolve, timeout))
      ])
    } catch (error) {
      console.warn('窗口操作失败，已继续执行:', error)
    }
  }

  const expandToMain = async () => {
    await runWindowAction(window.api?.window?.resizeToMain)
  }

  const shrinkToLogin = async () => {
    await runWindowAction(window.api?.window?.resizeToLogin)
  }

  const resizeToRegister = async () => {
    await runWindowAction(window.api?.window?.resizeToRegister)
  }

  const resizeToAdminLogin = async () => {
    await runWindowAction(window.api?.window?.resizeToAdminLogin)
  }

  const close = async () => {
    if (window.api?.window?.close) {
      await window.api.window.close()
    }
  }

  const minimize = async () => {
    if (window.api?.window?.minimize) {
      await window.api.window.minimize()
    }
  }

  return { expandToMain, shrinkToLogin, resizeToRegister, resizeToAdminLogin, close, minimize }
}
