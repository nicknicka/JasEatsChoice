/**
 * 统一的错误处理工具
 */

// 图片服务器配置（注意：需要包含/api前缀，因为后端context-path是/api）
const IMAGE_SERVER = 'http://192.168.137.188:8080/api'

/**
 * 处理图片URL，确保返回完整的URL
 * @param {string} url - 原始图片URL
 * @returns {string} 处理后的图片URL
 */
export const processImageUrl = (url) => {
  if (!url || typeof url !== 'string') {
    return url || ''
  }

  // 如果已经是完整URL（http/https开头）
  if (url.startsWith('http://') || url.startsWith('https://')) {
    // 开发环境下替换localhost为局域网IP
    if (url.includes('localhost')) {
      return url.replace('localhost', '192.168.137.188')
    }
    return url
  }

  // 相对路径：添加服务器地址
  if (url.startsWith('/')) {
    return IMAGE_SERVER + url
  }

  // 其他情况：直接返回
  return url
}

/**
 * 显示错误提示
 * @param {string} message - 错误信息
 * @param {number} duration - 持续时间(ms)
 */
export const showError = (message, duration = 2000) => {
  uni.showToast({
    title: message,
    icon: 'none',
    duration
  })
}

/**
 * 显示成功提示
 * @param {string} message - 成功信息
 * @param {number} duration - 持续时间(ms)
 */
export const showSuccess = (message, duration = 2000) => {
  uni.showToast({
    title: message,
    icon: 'success',
    duration
  })
}

/**
 * 显示加载提示
 * @param {string} message - 加载信息
 */
export const showLoading = (message = '加载中...') => {
  uni.showLoading({
    title: message,
    mask: true
  })
}

/**
 * 隐藏加载提示
 */
export const hideLoading = () => {
  uni.hideLoading()
}

/**
 * 显示确认对话框
 * @param {string} content - 确认内容
 * @param {string} title - 标题
 * @returns {Promise<boolean>} - 用户是否确认
 */
export const showConfirm = (content, title = '提示') => {
  return new Promise((resolve) => {
    uni.showModal({
      title,
      content,
      success: (res) => {
        resolve(res.confirm)
      },
      fail: () => {
        resolve(false)
      }
    })
  })
}

/**
 * API错误处理
 * @param {Error} error - 错误对象
 * @param {string} defaultMessage - 默认错误信息
 */
export const handleError = (error, defaultMessage = '操作失败') => {
  console.error('Error:', error)

  let message = defaultMessage

  if (error.message) {
    message = error.message
  } else if (typeof error === 'string') {
    message = error
  }

  // 根据错误类型显示不同提示
  if (message.includes('网络')) {
    message = '网络错误，请检查网络连接'
  } else if (message.includes('超时')) {
    message = '请求超时，请重试'
  } else if (message.includes('401')) {
    message = '登录已过期，请重新登录'
  } else if (message.includes('403')) {
    message = '没有权限执行此操作'
  } else if (message.includes('404')) {
    message = '请求的资源不存在'
  } else if (message.includes('500')) {
    message = '服务器错误，请稍后重试'
  }

  showError(message)
}

/**
 * async错误包装器
 * 自动处理async函数中的错误
 * @param {Function} fn - async函数
 * @param {string} errorMessage - 错误信息
 */
export const asyncErrorHandler = async (fn, errorMessage = '操作失败') => {
  try {
    return await fn()
  } catch (error) {
    handleError(error, errorMessage)
    throw error
  }
}

/**
 * 检查登录状态
 * @returns {boolean} 是否已登录
 */
export const checkLogin = () => {
  const token = uni.getStorageSync('token')
  return !!token
}

/**
 * 跳转到登录页（如果未登录）
 * @returns {boolean} 是否已登录
 */
export const requireLogin = () => {
  if (!checkLogin()) {
    uni.showModal({
      title: '提示',
      content: '请先登录',
      showCancel: false,
      success: () => {
        uni.reLaunch({
          url: '/pages/login/index'
        })
      }
    })
    return false
  }
  return true
}

/**
 * 格式化价格
 * @param {number} price - 价格
 * @returns {string} 格式化后的价格字符串
 */
export const formatPrice = (price) => {
  if (typeof price !== 'number') {
    price = parseFloat(price)
  }
  return price.toFixed(2)
}

/**
 * 格式化日期时间
 * @param {string|Date} date - 日期
 * @param {string} format - 格式化模板
 * @returns {string} 格式化后的日期字符串
 */
export const formatDateTime = (date, format = 'YYYY-MM-DD HH:mm:ss') => {
  const d = new Date(date)

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute)
    .replace('ss', second)
}

/**
 * 格式化相对时间（刚刚/分钟前/小时前/天前）
 * @param {string|Date} time - 时间
 * @returns {string} 格式化后的相对时间字符串
 */
export const formatRelativeTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return `${date.getMonth() + 1}-${date.getDate()}`
}

/**
 * 格式化日期（YYYY-MM-DD）
 * @param {string|Date} date - 日期
 * @returns {string} 格式化后的日期字符串
 */
export const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 格式化时间（HH:mm）
 * @param {string|Date} time - 时间
 * @returns {string} 格式化后的时间字符串
 */
export const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  return `${hour}:${minute}`
}

/**
 * 格式化日期时间（MM-DD HH:mm）
 * @param {string|Date} date - 日期时间
 * @returns {string} 格式化后的日期时间字符串
 */
export const formatShortDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
}

/**
 * 节流函数
 * @param {Function} fn - 要节流的函数
 * @param {number} delay - 延迟时间(ms)
 * @returns {Function} 节流后的函数
 */
export const throttle = (fn, delay = 300) => {
  let lastTime = 0
  return function(...args) {
    const now = Date.now()
    if (now - lastTime >= delay) {
      lastTime = now
      return fn.apply(this, args)
    }
  }
}

/**
 * 防抖函数
 * @param {Function} fn - 要防抖的函数
 * @param {number} delay - 延迟时间(ms)
 * @returns {Function} 防抖后的函数
 */
export const debounce = (fn, delay = 300) => {
  let timer = null
  return function(...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 复制到剪贴板
 * @param {string} data - 要复制的数据
 * @param {string} successMessage - 成功提示信息
 */
export const copyToClipboard = (data, successMessage = '已复制') => {
  uni.setClipboardData({
    data,
    success: () => {
      showSuccess(successMessage)
    },
    fail: () => {
      showError('复制失败')
    }
  })
}

/**
 * 预览图片
 * @param {string} url - 图片URL
 * @param {number} current - 当前图片索引
 * @param {Array} urls - 图片URL列表
 */
export const previewImage = (url, current = 0, urls = []) => {
  const imageUrls = urls.length > 0 ? urls : [url]
  uni.previewImage({
    current,
    urls: imageUrls
  })
}

/**
 * 保存图片到相册
 * @param {string} filePath - 图片文件路径
 */
export const saveImageToPhotosAlbum = (filePath) => {
  uni.saveImageToPhotosAlbum({
    filePath,
    success: () => {
      showSuccess('保存成功')
    },
    fail: () => {
      showError('保存失败')
    }
  })
}

/**
 * 拨打电话
 * @param {string} phoneNumber - 电话号码
 */
export const makePhoneCall = (phoneNumber) => {
  uni.makePhoneCall({
    phoneNumber
  })
}

/**
 * 打开地图导航
 * @param {number} latitude - 纬度
 * @param {number} longitude - 经度
 * @param {string} name - 地点名称
 * @param {string} address - 地址
 */
export const openLocation = (latitude, longitude, name, address) => {
  uni.openLocation({
    latitude,
    longitude,
    name,
    address
  })
}

export default {
  showError,
  showSuccess,
  showLoading,
  hideLoading,
  showConfirm,
  handleError,
  asyncErrorHandler,
  checkLogin,
  requireLogin,
  formatPrice,
  formatDateTime,
  formatRelativeTime,
  formatDate,
  formatTime,
  formatShortDateTime,
  throttle,
  debounce,
  copyToClipboard,
  previewImage,
  saveImageToPhotosAlbum,
  makePhoneCall,
  openLocation,
  processImageUrl
}
