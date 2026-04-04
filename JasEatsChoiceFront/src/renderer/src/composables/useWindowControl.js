/**
 * 窗口控制 composable
 * 封装 Electron 窗口尺寸切换 API
 */
export function useWindowControl() {
  const expandToMain = async () => {
    if (window.api?.window?.resizeToMain) {
      await window.api.window.resizeToMain()
    }
  }

  const shrinkToLogin = async () => {
    if (window.api?.window?.resizeToLogin) {
      await window.api.window.resizeToLogin()
    }
  }

  const resizeToRegister = async () => {
    if (window.api?.window?.resizeToRegister) {
      await window.api.window.resizeToRegister()
    }
  }

  const resizeToAdminLogin = async () => {
    if (window.api?.window?.resizeToAdminLogin) {
      await window.api.window.resizeToAdminLogin()
    }
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
