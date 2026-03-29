/**
 * 网络请求封装
 * 参考桌面端配置，适配后端ResponseResult格式
 */
import config from '@/config/index.js'

// 不需要token的接口白名单
const AUTH_WHITELIST = [
  '/v1/users/login',              // 用户登录
  '/v1/users/register',           // 用户注册
  '/v1/users/send-sms-code',      // 发送验证码
  '/v1/users/wechat-login',       // 微信登录
  '/v1/merchant/login',           // 商家登录
  '/v1/merchant/register',        // 商家注册
  '/v1/admin/login',              // 管理员登录
  '/v1/captcha',                  // 验证码
  '/v1/captcha/checkCode'         // 验证码检查
]

// 检查请求是否在白名单中
const isWhitelisted = (url) => {
  return AUTH_WHITELIST.some((path) => url.includes(path))
}

/**
 * 统一请求方法
 * @param {Object} options - 请求配置
 * @param {string} options.url - 请求路径（会自动拼接baseURL）
 * @param {string} options.method - 请求方法 GET/POST/PUT/DELETE
 * @param {Object} options.data - 请求数据
 * @param {Object} options.params - URL参数（GET请求使用）
 * @param {boolean} options.needAuth - 是否需要token（默认true，白名单自动判断）
 * @param {number} options.timeout - 超时时间（毫秒），默认使用配置文件中的值
 */
export const request = (options) => {
  return new Promise((resolve, reject) => {
    // 构建完整URL
    let url = options.url
    if (!url.startsWith('http')) {
      url = config.baseURL + url
    }

    // GET请求使用params，其他请求使用data
    const requestData = options.method === 'GET' ? (options.params || {}) : (options.data || {})

    // 获取token
    let token = ''
    const needAuth = options.needAuth !== false && !isWhitelisted(options.url)
    if (needAuth) {
      token = uni.getStorageSync('token') || ''
    }

    // 设置超时时间
    // 对于登录、注册、验证码等关键接口，使用较短的超时时间
    // 对于其他接口，使用配置文件中的超时时间
    let timeout = options.timeout
    if (!timeout) {
      const isAuthApi = url.includes('/login') || url.includes('/register') || url.includes('/send-sms-code') || url.includes('/captcha')
      timeout = isAuthApi ? 10000 : config.timeout // 登录等接口10秒，其他30秒
    }

    // 请求日志
    if (options.url.includes('/orders') || options.url.includes('/v1/orders')) {
      console.log('🔍 API请求 - 订单相关', {
        method: options.method || 'GET',
        url: options.url,
        needAuth,
        hasToken: !!token,
        data: requestData,
        timeout,
        timestamp: new Date().toISOString()
      })
    }

    uni.request({
      url,
      method: options.method || 'GET',
      data: requestData,
      timeout,
      header: {
        'Content-Type': 'application/json',
        ...(needAuth && token ? { 'Authorization': `Bearer ${token}` } : {}),
        // 添加用户ID请求头用于行为追踪
        ...(needAuth && token ? { 'X-User-Id': uni.getStorageSync('userInfo')?.id || '' } : {})
      },
      success: (res) => {
        // 响应日志
        if (options.url.includes('/orders') || options.url.includes('/v1/orders')) {
          console.log('✅ API响应 - 订单相关', {
            method: options.method || 'GET',
            url: options.url,
            statusCode: res.statusCode,
            hasData: !!res.data,
            timestamp: new Date().toISOString()
          })
        }

        // HTTP状态码检查
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const response = res.data

          // 后端统一返回格式：{ success: boolean, code: string, message: string, data: any }
          if (response && typeof response === 'object') {
            // 检查业务状态
            if (response.success === true || response.code === '200' || response.code === 200) {
              // ✅ 修复：返回完整response对象，保留code、message、data等字段
              // 这样页面代码可以正确检查 res.code 和访问 res.data
              resolve(response)
            } else if (response.success === false) {
              // 业务失败
              const errorMsg = response.message || '请求失败'
              uni.showToast({
                title: errorMsg,
                icon: 'none',
                duration: 2000
              })
              reject({ message: errorMsg, code: response.code, response })
            } else {
              // 兼容旧格式或直接返回的数据
              resolve(response)
            }
          } else {
            // 直接返回数据（非标准格式）
            resolve(response)
          }
        } else if (res.statusCode === 401) {
          // 未授权，清除token并跳转登录
          handleTokenExpired(options.url)
          reject({ message: '未授权，请重新登录', statusCode: 401 })
        } else if (res.statusCode === 403) {
          // 权限不足
          uni.showToast({
            title: '权限不足',
            icon: 'none'
          })
          reject({ message: '权限不足', statusCode: 403 })
        } else if (res.statusCode === 404) {
          // 资源不存在
          uni.showToast({
            title: '请求的资源不存在',
            icon: 'none'
          })
          reject({ message: '请求的资源不存在', statusCode: 404 })
        } else if (res.statusCode >= 500) {
          // 服务器错误
          uni.showToast({
            title: '服务器错误，请稍后重试',
            icon: 'none'
          })
          reject({ message: '服务器错误', statusCode: res.statusCode })
        } else {
          // 其他错误
          const errorMsg = res.data?.message || `请求失败 (${res.statusCode})`
          uni.showToast({
            title: errorMsg,
            icon: 'none'
          })
          reject({ message: errorMsg, statusCode: res.statusCode, response: res.data })
        }
      },
      fail: (err) => {
        console.error('❌ API请求失败', {
          url: options.url,
          method: options.method,
          error: err,
          timestamp: new Date().toISOString()
        })

        let errorMsg = '网络连接失败'
        if (err.errMsg) {
          if (err.errMsg.includes('timeout')) {
            errorMsg = '请求超时，请检查网络'
          } else if (err.errMsg.includes('fail')) {
            errorMsg = '网络连接失败'
          }
        }

        uni.showToast({
          title: errorMsg,
          icon: 'none'
        })
        reject({ message: errorMsg, error: err })
      }
    })
  })
}

/**
 * 处理token过期
 */
const handleTokenExpired = (url) => {
  // 清除本地存储
  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')
  uni.removeStorageSync('merchantInfo')

  // 跳转到登录页
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const currentRoute = currentPage ? `/${currentPage.route}` : ''

  // 如果当前不在登录页，则跳转
  if (currentRoute && !currentRoute.includes('/pages/login')) {
    uni.showToast({
      title: '登录已过期',
      icon: 'none',
      duration: 1500
    })

    setTimeout(() => {
      // 根据URL判断跳转到哪个登录页
      if (url.includes('/merchant') || url.includes('/admin')) {
        uni.reLaunch({
          url: '/pages/login/index?role=merchant'
        })
      } else {
        uni.reLaunch({
          url: '/pages/login/index?redirect=' + encodeURIComponent(currentRoute)
        })
      }
    }, 1500)
  }
}

/**
 * GET请求
 * @param {string} url - 请求路径
 * @param {Object} params - URL参数
 */
export const get = (url, params) => {
  return request({
    url,
    method: 'GET',
    params
  })
}

/**
 * POST请求
 * @param {string} url - 请求路径
 * @param {Object} data - 请求数据
 */
export const post = (url, data) => {
  return request({
    url,
    method: 'POST',
    data
  })
}

/**
 * PUT请求
 * @param {string} url - 请求路径
 * @param {Object} data - 请求数据
 */
export const put = (url, data) => {
  return request({
    url,
    method: 'PUT',
    data
  })
}

/**
 * DELETE请求
 * @param {string} url - 请求路径
 * @param {Object} data - 请求数据
 */
export const del = (url, data) => {
  return request({
    url,
    method: 'DELETE',
    data
  })
}

/**
 * 文件上传
 * @param {string} url - 上传路径
 * @param {string} filePath - 文件路径
 * @param {Object} formData - 额外的表单数据
 */
export const upload = (url, filePath, formData = {}) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || ''

    uni.uploadFile({
      url: API_CONFIG.baseURL + url,
      filePath,
      name: 'file',
      formData,
      header: {
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      success: (res) => {
        if (res.statusCode === 200) {
          try {
            const data = JSON.parse(res.data)
            if (data.success === true || data.code === '200') {
              resolve(data.data)
            } else {
              uni.showToast({
                title: data.message || '上传失败',
                icon: 'none'
              })
              reject(data)
            }
          } catch (e) {
            reject({ message: '解析响应失败', error: e })
          }
        } else {
          reject({ message: '上传失败', statusCode: res.statusCode })
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '上传失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

export default {
  request,
  get,
  post,
  put,
  del,
  upload,
  baseURL: config.baseURL
}
