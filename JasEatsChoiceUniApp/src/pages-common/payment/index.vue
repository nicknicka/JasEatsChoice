<template>
  <view class="payment-container">
    <!-- 订单信息 -->
    <view class="order-info-card">
      <view class="merchant-info" @tap="viewMerchant">
        <image class="merchant-avatar" :src="orderInfo.merchantAvatar" mode="aspectFill"></image>
        <view class="merchant-details">
          <text class="merchant-name">{{ orderInfo.merchantName }}</text>
          <text class="merchant-desc">{{ orderInfo.merchantDesc }}</text>
        </view>
      </view>

      <view class="order-summary">
        <view class="summary-item">
          <text class="item-label">订单号</text>
          <text class="item-value">{{ orderInfo.orderNo }}</text>
        </view>
        <view class="summary-item">
          <text class="item-label">下单时间</text>
          <text class="item-value">{{ orderInfo.orderTime }}</text>
        </view>
        <view class="summary-item">
          <text class="item-label">订单金额</text>
          <text class="item-value amount">¥{{ orderInfo.totalAmount }}</text>
        </view>
      </view>
    </view>

    <!-- 支付方式 -->
    <view class="payment-methods">
      <view class="section-title">选择支付方式</view>
      <view class="methods-list">
        <view
          class="method-item"
          :class="{ active: selectedMethod === 'wechat' }"
          @tap="selectMethod('wechat')"
        >
          <view class="method-icon wechat">
            <uni-icons type="weixin" size="28" color="#fff"></uni-icons>
          </view>
          <view class="method-info">
            <text class="method-name">微信支付</text>
            <text class="method-desc">推荐使用</text>
          </view>
          <view class="method-check" v-if="selectedMethod === 'wechat'">
            <uni-icons type="circle-filled" size="20" color="#09BB07"></uni-icons>
          </view>
        </view>

        <view
          class="method-item"
          :class="{ active: selectedMethod === 'alipay' }"
          @tap="selectMethod('alipay')"
        >
          <view class="method-icon alipay">
            <uni-icons type="wallet-filled" size="28" color="#fff"></uni-icons>
          </view>
          <view class="method-info">
            <text class="method-name">支付宝</text>
            <text class="method-desc">数亿用户的选择</text>
          </view>
          <view class="method-check" v-if="selectedMethod === 'alipay'">
            <uni-icons type="circle-filled" size="20" color="#1677FF"></uni-icons>
          </view>
        </view>

        <view
          class="method-item wallet-method"
          :class="{ active: selectedMethod === 'wallet' }"
          @tap="selectMethod('wallet')"
        >
          <view class="method-icon wallet">
            <uni-icons type="wallet" size="28" color="#fff"></uni-icons>
          </view>
          <view class="method-info">
            <text class="method-name">余额支付</text>
            <text class="method-desc">余额：¥{{ userBalance }}</text>
          </view>
          <view class="method-check" v-if="selectedMethod === 'wallet'">
            <uni-icons type="circle-filled" size="20" color="#FF6B35"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 支付详情 -->
    <view class="payment-details">
      <view class="section-title">支付详情</view>
      <view class="detail-row">
        <text class="detail-label">订单金额</text>
        <text class="detail-value">¥{{ orderInfo.totalAmount }}</text>
      </view>
      <view class="detail-row" v-if="orderInfo.discount > 0">
        <text class="detail-label">优惠减免</text>
        <text class="detail-value discount">-¥{{ orderInfo.discount }}</text>
      </view>
      <view class="detail-row total">
        <text class="detail-label">实付金额</text>
        <text class="detail-value final">¥{{ orderInfo.finalAmount }}</text>
      </view>
    </view>

    <!-- 优惠券 -->
    <view class="coupon-section" v-if="availableCoupons.length > 0" @tap="selectCoupon">
      <view class="coupon-left">
        <uni-icons type="gift-filled" size="20" color="#FF6B35"></uni-icons>
        <text class="coupon-text">{{ selectedCoupon ? selectedCoupon.name : `选择优惠券（${availableCoupons.length}张可用）` }}</text>
      </view>
      <view class="coupon-right">
        <text class="coupon-saving" v-if="selectedCoupon">-¥{{ selectedCoupon.discount }}</text>
        <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
      </view>
    </view>

    <!-- 支付说明 -->
    <view class="payment-notice">
      <view class="notice-title">
        <uni-icons type="info" size="16" color="#FF6B35"></uni-icons>
        <text class="notice-text">支付说明</text>
      </view>
      <view class="notice-list">
        <text class="notice-item">• 支付成功后将自动跳转到订单详情页</text>
        <text class="notice-item">• 如遇支付问题，请联系客服处理</text>
        <text class="notice-item">• 订单超时未支付将自动取消</text>
      </view>
    </view>

    <!-- 底部支付按钮 -->
    <view class="payment-footer">
      <view class="footer-info">
        <text class="pay-label">实付金额</text>
        <text class="pay-amount">¥{{ finalAmount }}</text>
      </view>
      <button
        class="pay-btn"
        :disabled="!selectedMethod || paying"
        @tap="confirmPayment"
      >
        {{ paying ? '支付中...' : '确认支付' }}
      </button>
    </view>

    <!-- 优惠券选择弹窗 -->
    <uni-popup ref="couponPopup" type="bottom">
      <view class="coupon-selector">
        <view class="selector-header">
          <text class="selector-title">选择优惠券</text>
          <view class="selector-close" @tap="closeCouponPopup">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>
        <scroll-view class="coupon-list" scroll-y>
          <view
            class="coupon-option"
            v-for="coupon in availableCoupons"
            :key="coupon.id"
            @tap="useCoupon(coupon)"
          >
            <view class="coupon-left">
              <view class="coupon-amount">
                <text class="amount-value">¥{{ coupon.discount }}</text>
                <text class="amount-condition">{{ coupon.condition }}</text>
              </view>
              <text class="coupon-name">{{ coupon.name }}</text>
              <text class="coupon-time">{{ coupon.validTime }}</text>
            </view>
            <view class="coupon-check" v-if="selectedCoupon && selectedCoupon.id === coupon.id">
              <uni-icons type="circle-filled" size="20" color="#FF6B35"></uni-icons>
            </view>
          </view>
        </scroll-view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { paymentApi } from '@/api/modules/payment.js'
import { walletApi } from '@/api/modules/wallet.js'
import { PaymentMethod } from '@/config/payment.js'

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

// 用户ID
const userId = ref('')

// 支付方式
const selectedMethod = ref('wechat')
const paying = ref(false)

// 优惠券
const availableCoupons = ref([])
const selectedCoupon = ref(null)

// 弹窗
const couponPopup = ref(null)

onLoad((options) => {
  // 获取用户ID
  userId.value = uni.getStorageSync('userId') || ''

  if (options.orderId) {
    loadOrderDetail(options.orderId)
    loadUserBalance()
  }
  loadCoupons()
})

/**
 * 加载订单详情 - PAY-001: 调用API获取订单详情
 */
const loadOrderDetail = async (orderId) => {
  try {
    uni.showLoading({ title: '加载中...' })

    // 调用后端API获取订单支付信息
    const res = await paymentApi.getOrderInfo(orderId)

    if (res.code === 200 && res.data) {
      const data = res.data

      // 格式化订单数据
      orderInfo.value = {
        orderId: data.orderId,
        orderNo: data.orderNo,
        merchantId: data.merchantId,
        merchantName: data.merchantName || '商家',
        merchantAvatar: data.merchantAvatar || '/static/default-merchant.png',
        merchantDesc: data.merchantDesc || '',
        orderTime: formatTime(new Date()), // 如果后端有时间字段，使用后端数据
        totalAmount: formatAmount(data.totalAmount),
        paidAmount: formatAmount(data.paidAmount || 0),
        discount: formatAmount(data.discount || 0),
        finalAmount: formatAmount(data.finalAmount || data.totalAmount)
      }

      console.log('订单详情加载成功:', orderInfo.value)
    } else {
      throw new Error(res.message || '获取订单信息失败')
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    uni.showToast({
      title: error.message || '加载订单失败',
      icon: 'none'
    })

    // 加载失败，延迟返回上一页
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } finally {
    uni.hideLoading()
  }
}

/**
 * 加载用户余额
 */
const loadUserBalance = async () => {
  try {
    if (!userId.value) return

    const res = await walletApi.getBalance(userId.value)

    if (res.code === 200 && res.data) {
      userBalance.value = formatAmount(res.data.balance || 0)
    }
  } catch (error) {
    console.error('加载余额失败:', error)
    // 余额加载失败不影响支付流程
  }
}

/**
 * 加载优惠券 - PAY-002: 调用API获取可用优惠券
 */
const loadCoupons = async () => {
  try {
    // 需要用户ID和订单金额
    if (!userId.value) {
      console.log('用户未登录，跳过加载优惠券')
      return
    }

    // 计算当前订单金额（不含优惠券）
    const orderAmount = parseFloat(orderInfo.value.totalAmount) || 0

    if (orderAmount <= 0) {
      console.log('订单金额无效，跳过加载优惠券')
      return
    }

    // 调用后端API获取可用优惠券
    const res = await paymentApi.getAvailableCoupons({
      userId: userId.value,
      orderAmount: orderAmount
    })

    if (res.code === 200 && res.data) {
      // 转换后端数据格式为前端所需格式
      availableCoupons.value = res.data.map(coupon => ({
        id: coupon.id,
        name: coupon.name || '优惠券',
        // 后端字段是 amount，转换为 discount
        discount: formatAmount(coupon.amount || 0),
        // 根据最低消费金额生成条件描述
        condition: coupon.minAmount && coupon.minAmount > 0
          ? `满${coupon.minAmount}元可用`
          : '无门槛',
        // 格式化过期时间
        validTime: coupon.expireTime
          ? `到期时间：${formatExpireTime(coupon.expireTime)}`
          : '永久有效',
        // 额外的数据，供后续使用
        originalAmount: coupon.amount,
        minAmount: coupon.minAmount,
        status: coupon.status
      }))

      console.log('加载优惠券成功:', availableCoupons.value.length, '张可用')
    } else {
      console.log('暂无可用优惠券')
      availableCoupons.value = []
    }
  } catch (error) {
    console.error('加载优惠券失败:', error)
    // 加载优惠券失败不影响支付流程，只是不显示优惠券选项
    availableCoupons.value = []
  }
}

/**
 * 格式化过期时间
 */
const formatExpireTime = (timeStr) => {
  if (!timeStr) return ''

  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')

  return `${year}-${month}-${day}`
}

/**
 * 最终金额
 */
const finalAmount = computed(() => {
  let amount = parseFloat(orderInfo.value.finalAmount)
  if (selectedCoupon.value) {
    amount -= parseFloat(selectedCoupon.value.discount)
  }
  return amount.toFixed(2)
})

/**
 * 选择支付方式
 */
const selectMethod = (method) => {
  selectedMethod.value = method
}

/**
 * 选择优惠券
 */
const selectCoupon = () => {
  couponPopup.value?.open()
}

/**
 * 关闭优惠券弹窗
 */
const closeCouponPopup = () => {
  couponPopup.value?.close()
}

/**
 * 使用优惠券
 */
const useCoupon = (coupon) => {
  selectedCoupon.value = coupon
  closeCouponPopup()

  uni.showToast({
    title: `已选择${coupon.name}`,
    icon: 'success',
    duration: 1500
  })
}

/**
 * 查看商家
 */
const viewMerchant = () => {
  uni.navigateTo({
    url: `/pages/home/merchant-detail?id=${orderInfo.value.merchantId}`
  })
}

/**
 * 确认支付
 */
const confirmPayment = () => {
  if (!selectedMethod.value) {
    uni.showToast({
      title: '请选择支付方式',
      icon: 'none'
    })
    return
  }

  // 余额支付检查
  if (selectedMethod.value === 'wallet') {
    if (parseFloat(userBalance.value) < parseFloat(finalAmount.value)) {
      uni.showModal({
        title: '余额不足',
        content: '当前余额不足，请选择其他支付方式或先充值',
        showCancel: false,
        success: () => {
          uni.navigateTo({
            url: '/pages/profile/recharge'
          })
        }
      })
      return
    }
  }

  uni.showModal({
    title: '确认支付',
    content: `确认支付 ¥${finalAmount.value}？`,
    success: (res) => {
      if (res.confirm) {
        processPayment()
      }
    }
  })
}

/**
 * 处理支付
 */
const processPayment = async () => {
  paying.value = true

  try {
    // PAY-003: 调用统一支付API创建支付订单
    const paymentData = {
      orderId: orderInfo.value.orderId,
      userId: userId.value,
      paymentMethod: selectedMethod.value,
      couponId: selectedCoupon.value?.id || null
    }

    console.log('创建支付订单:', paymentData)

    const res = await paymentApi.createPayment(paymentData)

    if (res.code === 200 && res.data) {
      const { paymentNo, amount, status } = res.data

      console.log('支付订单创建成功:', { paymentNo, amount, status })

      // 根据支付方式调用不同的支付接口
      if (selectedMethod.value === PaymentMethod.WECHAT) {
        // PAY-004: 调用微信支付
        const payResult = await wechatPay(paymentNo)
        if (payResult) {
          await checkPaymentStatus(paymentNo)
        } else {
          paying.value = false
        }
      } else if (selectedMethod.value === PaymentMethod.ALIPAY) {
        // PAY-005: 调用支付宝支付
        const payResult = await alipay(paymentNo)
        if (payResult) {
          await checkPaymentStatus(paymentNo)
        } else {
          paying.value = false
        }
      } else if (selectedMethod.value === PaymentMethod.WALLET) {
        // PAY-006: 调用余额支付API
        await walletPay(paymentNo)
      }
    } else {
      throw new Error(res.message || '创建支付订单失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    paying.value = false

    uni.showToast({
      title: error.message || '支付失败',
      icon: 'none',
      duration: 2000
    })
  }
}

/**
 * 微信支付 - PAY-004: 调用微信支付
 * @param {string} paymentNo - 支付流水号
 */
const wechatPay = async (paymentNo) => {
  try {
    // 调用后端API获取微信支付参数
    const res = await paymentApi.wechatPay({ paymentNo })

    if (res.code === 200 && res.data) {
      const payParams = res.data

      // 检查是否需要调起支付
      if (payParams.status === 'success') {
        // 已经支付成功（可能是余额直接扣款）
        return true
      }

      // 调起微信支付
      return new Promise((resolve) => {
        uni.requestPayment({
          provider: 'wxpay',
          timeStamp: payParams.timeStamp || Date.now().toString(),
          nonceStr: payParams.nonceStr || Math.random().toString(36).substr(2, 15),
          package: payParams.package || 'prepay_id=wx',
          signType: payParams.signType || 'MD5',
          paySign: payParams.paySign || '',
          success: () => {
            uni.showToast({
              title: '支付成功',
              icon: 'success'
            })
            resolve(true)
          },
          fail: (err) => {
            console.error('微信支付失败:', err)

            if (err.errMsg === 'requestPayment:fail cancel') {
              uni.showToast({
                title: '取消支付',
                icon: 'none'
              })
            } else {
              uni.showToast({
                title: '支付失败',
                icon: 'none'
              })
            }

            resolve(false)
          }
        })
      })
    } else {
      throw new Error(res.message || '获取微信支付参数失败')
    }
  } catch (error) {
    console.error('微信支付异常:', error)

    // 如果是开发环境或后端返回"功能开发中"，模拟支付成功
    if (error.message?.includes('开发中') || process.env.NODE_ENV === 'development') {
      console.log('微信支付功能开发中，模拟支付成功')
      uni.showToast({
        title: '模拟支付成功',
        icon: 'success'
      })
      return true
    }

    throw error
  }
}

/**
 * 支付宝支付 - PAY-005: 调用支付宝支付
 * @param {string} paymentNo - 支付流水号
 */
const alipay = async (paymentNo) => {
  try {
    // 调用后端API获取支付宝支付参数
    const res = await paymentApi.alipay({ paymentNo })

    if (res.code === 200 && res.data) {
      const payParams = res.data

      // 检查是否需要调起支付
      if (payParams.status === 'success') {
        // 已经支付成功
        return true
      }

      // 调起支付宝支付
      return new Promise((resolve) => {
        uni.requestPayment({
          provider: 'alipay',
          orderInfo: payParams.orderInfo || JSON.stringify({
            orderNo: paymentNo
          }),
          success: () => {
            uni.showToast({
              title: '支付成功',
              icon: 'success'
            })
            resolve(true)
          },
          fail: (err) => {
            console.error('支付宝支付失败:', err)

            if (err.errMsg === 'requestPayment:fail cancel') {
              uni.showToast({
                title: '取消支付',
                icon: 'none'
              })
            } else {
              uni.showToast({
                title: '支付失败',
                icon: 'none'
              })
            }

            resolve(false)
          }
        })
      })
    } else {
      throw new Error(res.message || '获取支付宝支付参数失败')
    }
  } catch (error) {
    console.error('支付宝支付异常:', error)

    // 如果是开发环境或后端返回"功能开发中"，模拟支付成功
    if (error.message?.includes('开发中') || process.env.NODE_ENV === 'development') {
      console.log('支付宝支付功能开发中，模拟支付成功')
      uni.showToast({
        title: '模拟支付成功',
        icon: 'success'
      })
      return true
    }

    throw error
  }
}

/**
 * 余额支付 - PAY-006: 调用余额支付API
 * @param {string} paymentNo - 支付流水号
 */
const walletPay = async (paymentNo) => {
  try {
    // 检查是否需要输入支付密码
    const needPassword = await checkPaymentPasswordRequired()

    let paymentPassword = null

    if (needPassword) {
      // 弹出支付密码输入框
      paymentPassword = await showPaymentPasswordDialog()

      if (!paymentPassword) {
        // 用户取消输入
        paying.value = false
        return
      }
    }

    // 调用后端API进行余额支付
    const res = await paymentApi.balancePay({
      paymentNo,
      paymentPassword
    })

    if (res.code === 200 && res.data) {
      const { status, paymentNo: paidPaymentNo } = res.data

      if (status === 'success') {
        // 支付成功
        uni.showToast({
          title: '支付成功',
          icon: 'success'
        })

        // 更新本地余额显示
        const currentBalance = parseFloat(userBalance.value)
        const payAmount = parseFloat(finalAmount.value)
        userBalance.value = (currentBalance - payAmount).toFixed(2)

        // 跳转到结果页
        setTimeout(() => {
          goToResult(true, paidPaymentNo)
        }, 1500)
      } else if (status === 'failed') {
        throw new Error('余额支付失败')
      }
    } else {
      throw new Error(res.message || '余额支付失败')
    }
  } catch (error) {
    console.error('余额支付异常:', error)

    // 根据错误码显示不同提示
    let errorMessage = '支付失败'

    if (error.message?.includes('余额不足')) {
      errorMessage = '余额不足，请先充值'
    } else if (error.message?.includes('支付密码错误')) {
      errorMessage = '支付密码错误，请重试'
    } else if (error.message) {
      errorMessage = error.message
    }

    uni.showModal({
      title: '支付失败',
      content: errorMessage,
      showCancel: false,
      success: () => {
        paying.value = false
      }
    })
  }
}

/**
 * 检查是否需要支付密码
 */
const checkPaymentPasswordRequired = () => {
  return new Promise((resolve) => {
    // TODO: 可以从后端查询用户是否设置了支付密码
    // 这里简化处理：如果金额超过100元，需要密码
    const amount = parseFloat(finalAmount.value)
    resolve(amount >= 100)
  })
}

/**
 * 显示支付密码输入框
 */
const showPaymentPasswordDialog = () => {
  return new Promise((resolve) => {
    uni.showModal({
      title: '输入支付密码',
      content: '请输入6位支付密码',
      editable: true,
      placeholderText: '请输入密码',
      success: (res) => {
        if (res.confirm && res.content) {
          const password = res.content.trim()

          // 验证密码长度
          if (password.length !== 6) {
            uni.showToast({
              title: '请输入6位支付密码',
              icon: 'none'
            })
            resolve(null)
            return
          }

          resolve(password)
        } else {
          resolve(null)
        }
      },
      fail: () => {
        resolve(null)
      }
    })
  })
}

/**
 * 查询支付状态 - PAY-007: 轮询查询支付状态
 * @param {string} paymentNo - 支付流水号
 */
const checkPaymentStatus = async (paymentNo) => {
  try {
    // 显示轮询提示
    uni.showLoading({
      title: '确认支付中...',
      mask: true
    })

    // 调用API模块的轮询方法
    await paymentApi.pollPaymentStatus(paymentNo, {
      interval: 2000,      // 每2秒查询一次
      maxAttempts: 15,     // 最多查询15次（30秒）
      onSuccess: (data) => {
        console.log('支付轮询成功:', data)
        uni.hideLoading()

        uni.showToast({
          title: '支付成功',
          icon: 'success'
        })

        // 延迟跳转到结果页
        setTimeout(() => {
          goToResult(true, paymentNo)
        }, 1500)
      },
      onFailed: (data) => {
        console.log('支付轮询失败:', data)
        uni.hideLoading()

        uni.showModal({
          title: '支付失败',
          content: '支付未成功，请重试或联系客服',
          showCancel: false,
          success: () => {
            paying.value = false
          }
        })
      },
      onTimeout: () => {
        console.log('支付轮询超时')
        uni.hideLoading()

        uni.showModal({
          title: '支付超时',
          content: '支付确认超时，请稍后在订单中查看支付状态',
          showCancel: false,
          success: () => {
            goToResult(false, paymentNo)
          }
        })
      }
    })
  } catch (error) {
    console.error('支付状态查询异常:', error)
    uni.hideLoading()
    paying.value = false

    uni.showToast({
      title: '查询支付状态失败',
      icon: 'none'
    })
  }
}

/**
 * 跳转到结果页
 * @param {boolean} success - 是否支付成功
 * @param {string} paymentNo - 支付流水号
 */
const goToResult = (success, paymentNo = '') => {
  paying.value = false

  uni.redirectTo({
    url: `/pages-common/payment/result?success=${success}&orderId=${orderInfo.value.orderId}&paymentNo=${paymentNo}`
  })
}

/**
 * 生成订单号
 */
const generateOrderNo = () => {
  const now = new Date()
  const year = now.getFullYear().toString().substr(2)
  const month = (now.getMonth() + 1).toString().padStart(2, '0')
  const day = now.getDate().toString().padStart(2, '0')
  const hour = now.getHours().toString().padStart(2, '0')
  const minute = now.getMinutes().toString().padStart(2, '0')
  const second = now.getSeconds().toString().padStart(2, '0')
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
  return `${year}${month}${day}${hour}${minute}${second}${random}`
}

/**
 * 格式化金额
 */
const formatAmount = (amount) => {
  if (typeof amount !== 'number') {
    amount = parseFloat(amount) || 0
  }
  return amount.toFixed(2)
}

/**
 * 格式化时间
 */
const formatTime = (date) => {
  const d = new Date(date)
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const day = d.getDate().toString().padStart(2, '0')
  const hour = d.getHours().toString().padStart(2, '0')
  const minute = d.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.payment-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 150rpx;
}

/* 订单信息 */
.order-info-card {
  background: #fff;
  margin-bottom: 20rpx;
}

.merchant-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.merchant-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
}

.merchant-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.merchant-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.merchant-desc {
  font-size: 24rpx;
  color: #999;
}

.order-summary {
  padding: 30rpx;
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-label {
  font-size: 26rpx;
  color: #666;
}

.item-value {
  font-size: 28rpx;
  color: #333;

  &.amount {
    font-size: 32rpx;
    color: #FF6B35;
    font-weight: bold;
  }
}

/* 支付方式 */
.payment-methods {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.methods-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.method-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 25rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;

  &.active {
    background: #FFF7E6;
    border-color: #FF6B35;
  }

  &.wallet-method {
    background: linear-gradient(135deg, #FF6B35, #FF8C5A);
    color: #fff;

    .method-name,
    .method-desc {
      color: #fff;
    }
  }
}

.method-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 12rpx;
  @include flex-center;

  &.wechat {
    background: #09BB07;
  }

  &.alipay {
    background: #1677FF;
  }

  &.wallet {
    background: #FF6B35;
  }
}

.method-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.method-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.method-desc {
  font-size: 22rpx;
  color: #999;
}

.method-check {
  width: 40rpx;
  height: 40rpx;
  @include flex-center;
}

/* 支付详情 */
.payment-details {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15rpx 0;
  border-bottom: 1rpx solid #eee;

  &:last-child {
    border-bottom: none;
    padding-top: 20rpx;
    margin-top: 10rpx;
    border-top: 1rpx solid #eee;
  }
}

.detail-label {
  font-size: 26rpx;
  color: #666;
}

.detail-value {
  font-size: 28rpx;
  color: #333;

  &.discount {
    color: #52C41A;
  }

  &.final {
    font-size: 36rpx;
    color: #FF6B35;
    font-weight: bold;
  }
}

/* 优惠券 */
.coupon-section {
  background: #fff;
  padding: 25rpx 30rpx;
  margin-bottom: 20rpx;
  border-radius: 12rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.coupon-left {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.coupon-text {
  font-size: 28rpx;
  color: #333;
}

.coupon-right {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.coupon-saving {
  font-size: 28rpx;
  color: #FF6B35;
  font-weight: bold;
}

/* 支付说明 */
.payment-notice {
  background: #FFF7E6;
  padding: 25rpx 30rpx;
  margin-bottom: 20rpx;
  border-radius: 12rpx;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 15rpx;
}

.notice-text {
  font-size: 26rpx;
  font-weight: bold;
  color: #FF6B35;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.notice-item {
  font-size: 24rpx;
  color: #666;
  line-height: 1.6;
}

/* 底部支付栏 */
.payment-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.footer-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.pay-label {
  font-size: 24rpx;
  color: #999;
}

.pay-amount {
  font-size: 40rpx;
  color: #FF6B35;
  font-weight: bold;
}

.pay-btn {
  width: 240rpx;
  height: 80rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 40rpx;
  border: none;
  @include flex-center;

  &[disabled] {
    background: #E8E8E8;
    color: #999;
  }
}

/* 优惠券选择弹窗 */
.coupon-selector {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 30rpx;
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.selector-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.selector-close {
  width: 60rpx;
  height: 60rpx;
  @include flex-center;
}

.coupon-list {
  max-height: 500rpx;
}

.coupon-option {
  background: #F5F5F5;
  padding: 25rpx;
  margin-bottom: 20rpx;
  border-radius: 12rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;

  &:last-child {
    margin-bottom: 0;
  }
}

.coupon-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.coupon-amount {
  display: flex;
  align-items: baseline;
  gap: 5rpx;
}

.amount-value {
  font-size: 32rpx;
  color: #FF6B35;
  font-weight: bold;
}

.amount-condition {
  font-size: 20rpx;
  color: #999;
}

.coupon-name {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.coupon-time {
  font-size: 22rpx;
  color: #999;
}

.coupon-check {
  width: 40rpx;
  height: 40rpx;
  @include flex-center;
}
</style>
