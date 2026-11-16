import { defineStore } from 'pinia'
import { ref } from 'vue'
import { merchantApi } from '@/api'

/**
 * 商家 Store
 */
export const useMerchantStore = defineStore('merchant', () => {
  // 状态
  const merchantInfo = ref(null)
  const merchantId = ref('')
  const shopName = ref('')
  const shopLogo = ref('')
  const shopStatus = ref('closed') // open, closed, busy

  // Actions
  const setMerchantInfo = (info) => {
    merchantInfo.value = info
    merchantId.value = info.merchantId || info.id || ''
    shopName.value = info.shopName || info.name || ''
    shopLogo.value = info.shopLogo || info.logo || ''
    shopStatus.value = info.shopStatus || 'closed'

    // 持久化到本地存储
    uni.setStorageSync('merchantInfo', JSON.stringify(info))
    // merchantId 单独落一份：pages-merchant 下的页面按这个键取商家身份
    if (merchantId.value) {
      uni.setStorageSync('merchantId', merchantId.value)
    }
  }

  const updateShopStatus = (status) => {
    shopStatus.value = status
    if (merchantInfo.value) {
      merchantInfo.value.shopStatus = status
    }
  }

  const clearMerchantInfo = () => {
    merchantInfo.value = null
    merchantId.value = ''
    shopName.value = ''
    shopLogo.value = ''
    shopStatus.value = 'closed'

    // 清除本地存储
    uni.removeStorageSync('merchantInfo')
    uni.removeStorageSync('merchantId')
  }

  // 从本地存储加载商家信息
  const loadMerchantInfo = () => {
    try {
      const localInfo = uni.getStorageSync('merchantInfo')
      if (localInfo) {
        const info = JSON.parse(localInfo)
        setMerchantInfo(info)
      }
    } catch (error) {
      console.error('加载商家信息失败:', error)
    }
  }

  // 统一的商家ID解析：store 状态 → storage 的 merchantId → 缓存的 merchantInfo → 登录用户信息。
  // 以前各页面直接 uni.getStorageSync('merchantId') || <写死的商家id>，取不到时用假 id 继续发请求（#593）。
  const resolveMerchantId = () => {
    if (merchantId.value) return merchantId.value

    const fromStorage = uni.getStorageSync('merchantId')
    if (fromStorage) {
      merchantId.value = fromStorage
      return fromStorage
    }

    const cached = uni.getStorageSync('merchantInfo')
    if (cached) {
      try {
        const info = typeof cached === 'string' ? JSON.parse(cached) : cached
        const id = info?.merchantId || info?.id || ''
        if (id) {
          setMerchantInfo(info)
          return merchantId.value
        }
      } catch (error) {
        console.error('解析已缓存的商家信息失败:', error)
      }
    }

    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.merchantId || ''
    if (userId) {
      merchantId.value = userId
      uni.setStorageSync('merchantId', userId)
      return userId
    }

    return ''
  }

  // 商家端页面统一入口：拿到商家ID并（首次）加载商家详情。
  // 拿不到身份时返回 null，由页面显示错误态，不再回退到写死的商家 id。
  const ensureMerchant = async () => {
    const id = resolveMerchantId()
    if (!id) return null

    const cachedInfo = merchantInfo.value
    if (cachedInfo && (cachedInfo.merchantId === id || cachedInfo.id === id)) {
      return cachedInfo
    }

    const res = await merchantApi.getDetail(id)
    const detail = res?.data || res
    if (!detail) return null

    setMerchantInfo({
      ...detail,
      merchantId: detail.merchantId || detail.id || id
    })
    return merchantInfo.value
  }

  return {
    // 状态
    merchantInfo,
    merchantId,
    shopName,
    shopLogo,
    shopStatus,

    // 方法
    setMerchantInfo,
    updateShopStatus,
    clearMerchantInfo,
    loadMerchantInfo,
    resolveMerchantId,
    ensureMerchant
  }
})
