/**
 * 错误处理工具类
 * 统一处理各种错误情况并提供友好的用户提示
 */
class ErrorHandler {
  /**
   * 处理网络错误
   * @param {Error} error - 错误对象
   * @param {object} options - 配置选项
   */
  handleNetworkError(error, options = {}) {
    const {
      showMessage = true,
      defaultMsg = '网络连接失败',
      onError = null
    } = options

    console.error('网络错误:', error)

    let message = defaultMsg

    // 根据错误类型提供更具体的消息
    if (error.errMsg) {
      if (error.errMsg.includes('timeout')) {
        message = '请求超时，请稍后重试'
      } else if (error.errMsg.includes('fail')) {
        message = '网络连接失败，请检查网络'
      }
    }

    if (showMessage) {
      uni.showToast({
        title: message,
        icon: 'none',
        duration: 2000
      })
    }

    if (onError) {
      onError(error)
    }
  }

  /**
   * 处理API错误
   * @param {object} response - 响应对象
   * @param {object} options - 配置选项
   */
  handleApiError(response, options = {}) {
    const {
      showMessage = true,
      defaultMsg = '操作失败',
      onError = null
    } = options

    console.error('API错误:', response)

    let message = defaultMsg

    // 根据状态码提供更具体的消息
    if (response.statusCode) {
      switch (response.statusCode) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '请先登录'
          break
        case 403:
          message = '没有权限访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器错误，请稍后重试'
          break
        case 502:
        case 503:
        case 504:
          message = '服务暂时不可用'
          break
        default:
          message = response.data?.message || defaultMsg
      }
    }

    if (showMessage) {
      uni.showToast({
        title: message,
        icon: 'none',
        duration: 2000
      })
    }

    if (onError) {
      onError(response)
    }
  }

  /**
   * 处理业务错误
   * @param {object} error - 业务错误对象
   * @param {object} options - 配置选项
   */
  handleBusinessError(error, options = {}) {
    const {
      showMessage = true,
      onError = null
    } = options

    console.error('业务错误:', error)

    const message = error.message || error.msg || '操作失败'

    if (showMessage) {
      uni.showToast({
        title: message,
        icon: 'none',
        duration: 2000
      })
    }

    if (onError) {
      onError(error)
    }
  }

  /**
   * 处理未知错误
   * @param {Error} error - 错误对象
   * @param {object} options - 配置选项
   */
  handleUnknownError(error, options = {}) {
    const {
      showMessage = true,
      defaultMsg = '操作失败，请重试',
      onError = null
    } = options

    console.error('未知错误:', error)

    if (showMessage) {
      uni.showToast({
        title: defaultMsg,
        icon: 'none',
        duration: 2000
      })
    }

    if (onError) {
      onError(error)
    }
  }

  /**
   * 统一错误处理入口
   * @param {Error} error - 错误对象
   * @param {object} options - 配置选项
   */
  handle(error, options = {}) {
    if (!error) return

    // 根据错误类型分发到不同的处理方法
    if (error.statusCode || (error.data && error.data.code)) {
      this.handleApiError(error, options)
    } else if (error.code && error.code.startsWith('BUSINESS_')) {
      this.handleBusinessError(error, options)
    } else if (error.errMsg && (error.errMsg.includes('network') || error.errMsg.includes('timeout'))) {
      this.handleNetworkError(error, options)
    } else {
      this.handleUnknownError(error, options)
    }
  }

  /**
   * 显示加载状态
   * @param {string} title - 加载提示文字
   */
  showLoading(title = '加载中...') {
    uni.showLoading({
      title,
      mask: true
    })
  }

  /**
   * 隐藏加载状态
   */
  hideLoading() {
    uni.hideLoading()
  }

  /**
   * 显示成功提示
   * @param {string} message - 提示文字
   */
  showSuccess(message) {
    uni.showToast({
      title: message,
      icon: 'success',
      duration: 2000
    })
  }

  /**
   * 包装异步函数，自动处理错误
   * @param {Function} asyncFn - 异步函数
   * @param {object} options - 配置选项
   */
  async wrap(asyncFn, options = {}) {
    const {
      showLoading: showLoad = false,
      loadingTitle = '加载中...',
      onError = null
    } = options

    try {
      if (showLoad) {
        this.showLoading(loadingTitle)
      }

      const result = await asyncFn()

      if (showLoad) {
        this.hideLoading()
      }

      return result
    } catch (error) {
      if (showLoad) {
        this.hideLoading()
      }

      this.handle(error, options)

      if (onError) {
        return onError(error)
      }

      throw error
    }
  }
}

// 创建单例
const errorHandler = new ErrorHandler()

export default errorHandler
