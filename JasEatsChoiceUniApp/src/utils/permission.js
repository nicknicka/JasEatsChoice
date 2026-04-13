/**
 * 权限工具类
 */
import config from '@/config'

export const permission = {
  /**
   * 请求定位权限
   * @returns {Promise<boolean>}
   */
  async requestLocation() {
    return new Promise((resolve) => {
      uni.authorize({
        scope: 'scope.userLocation',
        success: () => {
          resolve(true)
        },
        fail: () => {
          // 未授权，引导用户打开设置
          uni.showModal({
            title: '提示',
            content: '需要获取您的位置信息，请确认授权',
            confirmText: '去设置',
            success: (res) => {
              if (res.confirm) {
                uni.openSetting({
                  success: (settingRes) => {
                    if (settingRes.authSetting['scope.userLocation']) {
                      resolve(true)
                    } else {
                      resolve(false)
                    }
                  }
                })
              } else {
                resolve(false)
              }
            }
          })
        }
      })
    })
  },

  /**
   * 请求相机权限
   * @returns {Promise<boolean>}
   */
  async requestCamera() {
    return new Promise((resolve) => {
      uni.authorize({
        scope: 'scope.camera',
        success: () => {
          resolve(true)
        },
        fail: () => {
          uni.showModal({
            title: '提示',
            content: '需要使用您的相机，请确认授权',
            confirmText: '去设置',
            success: (res) => {
              if (res.confirm) {
                uni.openSetting({
                  success: (settingRes) => {
                    resolve(!!settingRes.authSetting['scope.camera'])
                  }
                })
              } else {
                resolve(false)
              }
            }
          })
        }
      })
    })
  },

  /**
   * 请求相册权限
   * @returns {Promise<boolean>}
   */
  async requestAlbum() {
    return new Promise((resolve) => {
      uni.authorize({
        scope: 'scope.writePhotosAlbum',
        success: () => {
          resolve(true)
        },
        fail: () => {
          uni.showModal({
            title: '提示',
            content: '需要访问您的相册，请确认授权',
            confirmText: '去设置',
            success: (res) => {
              if (res.confirm) {
                uni.openSetting({
                  success: (settingRes) => {
                    resolve(!!settingRes.authSetting['scope.writePhotosAlbum'])
                  }
                })
              } else {
                resolve(false)
              }
            }
          })
        }
      })
    })
  },

  /**
   * 请求麦克风权限
   * @returns {Promise<boolean>}
   */
  async requestRecord() {
    return new Promise((resolve) => {
      uni.authorize({
        scope: 'scope.record',
        success: () => {
          resolve(true)
        },
        fail: () => {
          uni.showModal({
            title: '提示',
            content: '需要使用您的麦克风，请确认授权',
            confirmText: '去设置',
            success: (res) => {
              if (res.confirm) {
                uni.openSetting({
                  success: (settingRes) => {
                    resolve(!!settingRes.authSetting['scope.record'])
                  }
                })
              } else {
                resolve(false)
              }
            }
          })
        }
      })
    })
  },

  /**
   * 获取用户信息授权
   * @returns {Promise<Object>}
   */
  async getUserProfile() {
    return new Promise((resolve, reject) => {
      uni.getUserProfile({
        desc: '用于完善用户资料',
        success: (res) => {
          resolve(res)
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  },

  /**
   * 获取手机号
   * @param {string} code - 微信code
   * @returns {Promise<Object>}
   */
  async getPhoneNumber(code) {
    return new Promise((resolve, reject) => {
      // 这里应该调用后端接口，使用code换取手机号
      // 暂时返回模拟数据
      uni.request({
        url: `${config.baseURL}/wechat/phone`,
        method: 'POST',
        data: { code },
        success: (res) => {
          resolve(res.data)
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  }
}
