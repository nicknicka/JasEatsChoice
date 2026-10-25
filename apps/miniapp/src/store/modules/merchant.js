import { defineStore } from 'pinia'
import { ref } from 'vue'

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
    loadMerchantInfo
  }
})
