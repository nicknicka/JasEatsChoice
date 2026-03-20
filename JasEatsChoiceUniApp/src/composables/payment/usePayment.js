/**
 * Composable: usePayment
 * 用途：支付核心逻辑管理
 * 创建时间：2026-03-20
 */
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { paymentApi } from '@/api/modules/payment.js'
import { walletApi } from '@/api/modules/wallet.js'

export function usePayment() {
  // 订单信息
  const orderInfo = ref({
    orderId: '',
    orderNo: '',
    merchantId: '',
    merchantName: '',
    merchantAvatar: '',
    merchantDesc: '',
    orderTime: '',
    totalAmount: '0.00',
    paidAmount: '0.00',
    discount: '0.00',
    finalAmount: '0.00'
  })

  // 用户余额
  const userBalance = ref('0.00')

  // 选中的支付方式
  const selectedMethod = ref('wechat')

  // 选中的优惠券
  const selectedCoupon = ref(null)

  // 可用优惠券列表
  const availableCoupons = ref([])

  // 支付中状态
  const paying = ref(false)

  // 最终支付金额
  const finalAmount = computed(() => {
    return orderInfo.value.finalAmount
  })

  /**
   * 初始化
   */
  const init = async (options) => {
    if (options && options.orderId) {
      await loadOrderInfo(options.orderId)
    }
    await loadUserBalance()
    await loadAvailableCoupons()
  }

  /**
   * 加载订单信息
   */
  const loadOrderInfo = async (orderId) => {
    try {
      // const res = await paymentApi.getOrderInfo(orderId)

      // 模拟数据
      orderInfo.value = {
        orderId,
        orderNo: 'DD' + Date.now(),
        merchantId: '1',
        merchantName: '老王家常菜',
        merchantAvatar: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=店',
        merchantDesc: '快餐 · 人均¥25',
        orderTime: formatTime(new Date()),
        totalAmount: '58.00',
        paidAmount: '0.00',
        discount: '0.00',
        finalAmount: '58.00'
      }
    } catch (error) {
      console.error('加载订单信息失败:', error)
    }
  }

  /**
   * 加载用户余额
   */
  const loadUserBalance = async () => {
    try {
      // const res = await walletApi.getBalance()
      userBalance.value = '100.00'
    } catch (error) {
      console.error('加载用户余额失败:', error)
    }
  }

  /**
   * 加载可用优惠券
   */
  const loadAvailableCoupons = async () => {
    try {
      // const res = await couponApi.getAvailableCoupons(orderInfo.value.orderId)

      // 模拟数据
      availableCoupons.value = [
        {
          id: 1,
          name: '满50减5',
          discount: '5.00',
          condition: '满50元可用',
          validTime: '2026.03.20-2026.04.20'
        },
        {
          id: 2,
          name: '满30减3',
          discount: '3.00',
          condition: '满30元可用',
          validTime: '2026.03.20-2026.04.20'
        }
      ]
    } catch (error) {
      console.error('加载优惠券失败:', error)
    }
  }

  /**
   * 选择优惠券
   */
  const selectCoupon = (coupon) => {
    selectedCoupon.value = coupon

    // 重新计算金额
    const discount = parseFloat(coupon.discount)
    const total = parseFloat(orderInfo.value.totalAmount)
    const final = Math.max(0, total - discount).toFixed(2)

    orderInfo.value.discount = discount.toFixed(2)
    orderInfo.value.finalAmount = final
  }

  /**
   * 取消优惠券
   */
  const cancelCoupon = () => {
    selectedCoupon.value = null
    orderInfo.value.discount = '0.00'
    orderInfo.value.finalAmount = orderInfo.value.totalAmount
  }

  /**
   * 确认支付
   */
  const confirmPayment = async () => {
    if (!selectedMethod.value) {
      uni.showToast({
        title: '请选择支付方式',
        icon: 'none'
      })
      return
    }

    paying.value = true

    try {
      // 调用支付API
      const res = await paymentApi.pay({
        orderId: orderInfo.value.orderId,
        method: selectedMethod.value,
        couponId: selectedCoupon.value?.id,
        amount: finalAmount.value
      })

      if (res.code === 200) {
        // 支付成功
        uni.showToast({
          title: '支付成功',
          icon: 'success'
        })

        setTimeout(() => {
          uni.redirectTo({
            url: `/pages/order/detail?id=${orderInfo.value.orderId}`
          })
        }, 1500)
      } else {
        throw new Error(res.message || '支付失败')
      }
    } catch (error) {
      console.error('支付失败:', error)
      uni.showToast({
        title: error.message || '支付失败',
        icon: 'none'
      })
    } finally {
      paying.value = false
    }
  }

  /**
   * 查看商家
   */
  const viewMerchant = () => {
    uni.navigateTo({
      url: `/pages/merchant/detail?id=${orderInfo.value.merchantId}`
    })
  }

  /**
   * 格式化时间
   */
  const formatTime = (date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hour}:${minute}`
  }

  // 生命周期
  onLoad((options) => {
    init(options)
  })

  return {
    // 数据
    orderInfo,
    userBalance,
    selectedMethod,
    selectedCoupon,
    availableCoupons,
    paying,
    finalAmount,

    // 方法
    loadOrderInfo,
    loadUserBalance,
    loadAvailableCoupons,
    selectCoupon,
    cancelCoupon,
    confirmPayment,
    viewMerchant
  }
}
