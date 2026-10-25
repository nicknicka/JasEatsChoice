/**
 * 微信小程序工具函数
 * 包含登录、支付、分享等微信特有功能
 */
import config from '@/config/index.js'

/**
 * 微信登录
 * @returns {Promise<Object>} 登录结果
 */
export const wechatLogin = () => {
  return new Promise((resolve, reject) => {
    // 1. 获取微信code
    uni.login({
      provider: 'weixin',
      success: (loginRes) => {
        console.log('微信登录code:', loginRes.code)

        // 2. 调用后端接口进行登录
        uni.request({
          url: config.baseURL + '/v1/user/wechat-login',
          method: 'POST',
          data: {
            code: loginRes.code
          },
          success: (res) => {
            if (res.statusCode === 200 && res.data.success) {
              // 登录成功，保存token和用户信息
              const { token, userInfo } = res.data.data

              uni.setStorageSync('token', token)
              uni.setStorageSync('userInfo', userInfo)

              resolve({ token, userInfo })
            } else {
              reject(new Error(res.data.message || '登录失败'))
            }
          },
          fail: (err) => {
            console.error('登录请求失败:', err)
            reject(err)
          }
        })
      },
      fail: (err) => {
        console.error('微信登录失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 获取微信用户信息（已废弃，使用按钮获取）
 * @deprecated 微信小程序已废弃此接口，请使用button组件的open-type="getUserInfo"
 */
export const getUserInfo = () => {
  return new Promise((resolve, reject) => {
    uni.getUserInfo({
      provider: 'weixin',
      success: (res) => {
        resolve(res.userInfo)
      },
      fail: (err) => {
        console.error('获取用户信息失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 获取微信用户手机号
 * @param {Object} e - button组件的事件对象
 * @returns {Promise<string>} 手机号
 */
export const getPhoneNumber = (e) => {
  return new Promise((resolve, reject) => {
    if (e.detail.errMsg === 'getPhoneNumber:ok') {
      const { encryptedData, iv } = e.detail

      // 调用后端接口解密手机号
      const token = uni.getStorageSync('token')

      uni.request({
        url: config.baseURL + '/v1/user/phone',
        method: 'POST',
        header: {
          'Authorization': `Bearer ${token}`
        },
        data: {
          encryptedData,
          iv
        },
        success: (res) => {
          if (res.statusCode === 200 && res.data.success) {
            resolve(res.data.data.phoneNumber)
          } else {
            reject(new Error(res.data.message || '获取手机号失败'))
          }
        },
        fail: (err) => {
          console.error('获取手机号失败:', err)
          reject(err)
        }
      })
    } else {
      reject(new Error('用户取消授权'))
    }
  })
}

/**
 * 微信支付
 * @param {Object} orderInfo - 订单信息
 * @param {string} orderInfo.orderId - 订单ID
 * @param {string} orderInfo.orderNo - 订单号
 * @param {number} orderInfo.amount - 支付金额
 * @returns {Promise<Object>} 支付结果
 */
export const wechatPay = (orderInfo) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')

    // 1. 调用后端接口获取支付参数
    uni.request({
      url: config.baseURL + '/v1/payment/wechat/prepay',
      method: 'POST',
      header: {
        'Authorization': `Bearer ${token}`
      },
      data: {
        orderId: orderInfo.orderId,
        orderNo: orderInfo.orderNo,
        amount: orderInfo.amount
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.success) {
          const paymentParams = res.data.data

          // 2. 调用微信支付
          uni.requestPayment({
            provider: 'wxpay',
            timeStamp: paymentParams.timeStamp,
            nonceStr: paymentParams.nonceStr,
            package: paymentParams.package,
            signType: paymentParams.signType,
            paySign: paymentParams.paySign,
            success: (payRes) => {
              console.log('支付成功:', payRes)
              resolve({ success: true, ...payRes })
            },
            fail: (err) => {
              console.error('支付失败:', err)
              if (err.errMsg === 'requestPayment:fail cancel') {
                // 用户取消支付
                resolve({ success: false, cancelled: true })
              } else {
                reject(new Error('支付失败'))
              }
            }
          })
        } else {
          reject(new Error(res.data.message || '获取支付参数失败'))
        }
      },
      fail: (err) => {
        console.error('请求支付参数失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 分享到微信好友
 * @param {Object} options - 分享配置
 * @param {string} options.title - 分享标题
 * @param {string} options.path - 分享路径
 * @param {string} options.imageUrl - 分享图片
 */
export const shareToFriend = (options) => {
  return {
    title: options.title || '佳食宜选',
    path: options.path || '/pages/splash/index',
    imageUrl: options.imageUrl || '/static/share-default.jpg',
    success: () => {
      console.log('分享成功')
    },
    fail: (err) => {
      console.error('分享失败:', err)
    }
  }
}

/**
 * 分享到朋友圈
 * 注意：小程序不能直接分享到朋友圈，需要生成海报图片
 */
export const shareToMoments = (options) => {
  // 需要先生成海报，然后用户手动保存到相册，再从相册分享到朋友圈
  console.log('分享到朋友圈需要生成海报')
  return null
}

/**
 * 保存图片到相册
 * @param {string} filePath - 图片路径（网络图片需要先下载）
 */
export const saveImageToPhotosAlbum = (filePath) => {
  return new Promise((resolve, reject) => {
    // 如果是网络图片，先下载
    if (filePath.startsWith('http')) {
      uni.downloadFile({
        url: filePath,
        success: (downloadRes) => {
          saveToAlbum(downloadRes.tempFilePath)
            .then(resolve)
            .catch(reject)
        },
        fail: (err) => {
          console.error('下载图片失败:', err)
          reject(err)
        }
      })
    } else {
      // 本地图片直接保存
      saveToAlbum(filePath)
        .then(resolve)
        .catch(reject)
    }
  })
}

/**
 * 保存到相册内部方法
 */
const saveToAlbum = (filePath) => {
  return new Promise((resolve, reject) => {
    uni.saveImageToPhotosAlbum({
      filePath,
      success: () => {
        uni.showToast({
          title: '已保存到相册',
          icon: 'success'
        })
        resolve()
      },
      fail: (err) => {
        console.error('保存到相册失败:', err)

        // 如果是权限问题，引导用户授权
        if (err.errMsg.includes('auth')) {
          uni.showModal({
            title: '需要相册权限',
            content: '需要您授权保存图片到相册',
            success: (modalRes) => {
              if (modalRes.confirm) {
                uni.openSetting()
              }
            }
          })
        }

        reject(err)
      }
    })
  })
}

/**
 * 获取收货地址
 * @returns {Promise<Object>} 地址信息
 */
export const chooseAddress = () => {
  return new Promise((resolve, reject) => {
    uni.chooseAddress({
      success: (res) => {
        resolve(res)
      },
      fail: (err) => {
        console.error('获取收货地址失败:', err)

        // 如果是权限问题，引导用户授权
        if (err.errMsg.includes('auth')) {
          uni.showModal({
            title: '需要地址权限',
            content: '需要您授权获取收货地址',
            success: (modalRes) => {
              if (modalRes.confirm) {
                uni.openSetting()
              }
            }
          })
        }

        reject(err)
      }
    })
  })
}

/**
 * 联系客服
 * @param {string} path - 客服路径（可选）
 */
export const contactService = (path = '') => {
  uni.openCustomerServiceChat({
    extInfo: {
      path: path
    },
    corpId: '', // 需要在微信公众平台配置
    success: () => {
      console.log('打开客服成功')
    },
    fail: (err) => {
      console.error('打开客服失败:', err)
      uni.showToast({
        title: '打开客服失败',
        icon: 'none'
      })
    }
  })
}

/**
 * 扫码
 * @param {string} scanType - 扫码类型
 * @returns {Promise<string>} 扫码结果
 */
export const scanCode = (scanType = 'qrCode') => {
  return new Promise((resolve, reject) => {
    uni.scanCode({
      scanType: [scanType],
      success: (res) => {
        console.log('扫码结果:', res.result)
        resolve(res.result)
      },
      fail: (err) => {
        console.error('扫码失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 设置剪贴板
 * @param {string} data - 要复制的数据
 */
export const setClipboardData = (data) => {
  return new Promise((resolve, reject) => {
    uni.setClipboardData({
      data,
      success: () => {
        uni.showToast({
          title: '已复制',
          icon: 'success'
        })
        resolve()
      },
      fail: (err) => {
        console.error('复制失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 获取系统信息
 * @returns {Object} 系统信息
 */
export const getSystemInfo = () => {
  try {
    return uni.getSystemInfoSync()
  } catch (e) {
    console.error('获取系统信息失败:', e)
    return null
  }
}

/**
 * 检查更新
 */
export const checkUpdate = () => {
  if (uni.canIUse('getUpdateManager')) {
    const updateManager = uni.getUpdateManager()

    updateManager.onCheckForUpdate((res) => {
      console.log('是否有新版本:', res.hasUpdate)
    })

    updateManager.onUpdateReady(() => {
      uni.showModal({
        title: '更新提示',
        content: '新版本已经准备好，是否重启应用？',
        success: (modalRes) => {
          if (modalRes.confirm) {
            updateManager.applyUpdate()
          }
        }
      })
    })

    updateManager.onUpdateFailed(() => {
      console.error('新版本下载失败')
    })
  }
}

/**
 * 震动反馈
 * @param {string} type - 震动类型（short/long）
 */
export const vibrate = (type = 'short') => {
  if (type === 'short') {
    uni.vibrateShort()
  } else {
    uni.vibrateLong()
  }
}

/**
 * 添加手机号快捷登录
 * @returns {Promise<Object>} 登录结果
 */
export const quickLogin = () => {
  return new Promise((resolve, reject) => {
    // 需要使用button组件的open-type="getPhoneNumber"
    // 这里只是示例，实际使用需要在页面中添加button
    uni.showModal({
      title: '提示',
      content: '请使用页面中的手机号快捷登录按钮',
      showCancel: false
    })
    reject(new Error('请使用button组件'))
  })
}

export default {
  wechatLogin,
  getUserInfo,
  getPhoneNumber,
  wechatPay,
  shareToFriend,
  shareToMoments,
  saveImageToPhotosAlbum,
  chooseAddress,
  contactService,
  scanCode,
  setClipboardData,
  getSystemInfo,
  checkUpdate,
  vibrate,
  quickLogin
}
