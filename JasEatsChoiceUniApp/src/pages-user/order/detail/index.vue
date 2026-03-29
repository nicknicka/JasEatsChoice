<template>
  <view class="order-detail-container">
    <!-- 订单状态卡片 -->
    <view class="status-section">
      <view class="status-icon">{{ orderStatus.icon }}</view>
      <view class="status-text">{{ orderStatus.text }}</view>
      <view class="status-tips">{{ orderStatus.tips }}</view>
    </view>

    <scroll-view class="scroll-container" scroll-y>
      <!-- 订单进度 -->
      <view class="progress-section card" v-if="orderProgress.length > 0">
        <view class="section-title">订单进度</view>
        <view class="progress-list">
          <view
            class="progress-item"
            :class="{ active: index === currentProgressIndex }"
            v-for="(step, index) in orderProgress"
            :key="index"
          >
            <view class="progress-dot">
              <text>{{ step.completed ? '✓' : index + 1 }}</text>
            </view>
            <view class="progress-line" v-if="index < orderProgress.length - 1"></view>
            <view class="progress-info">
              <view class="progress-title">{{ step.title }}</view>
              <view class="progress-time">{{ step.time }}</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 订单商品 -->
      <view class="dishes-section card">
        <view class="section-title">订单商品</view>

        <view class="merchant-group" v-for="group in orderItems" :key="group.merchantId">
          <view class="merchant-info">
            <image class="merchant-logo" :src="group.merchant.logo" mode="aspectFill" />
            <text class="merchant-name">{{ group.merchant.name }}</text>
          </view>

          <view class="dish-list">
            <view class="dish-item" v-for="item in group.items" :key="item.dish.id">
              <image class="dish-image" :src="item.dish.image" mode="aspectFill" />

              <view class="dish-info">
                <view class="dish-name">{{ item.dish.name }}</view>
                <view class="dish-spec" v-if="item.spec">{{ item.spec }}</view>
                <view class="dish-bottom">
                  <view class="dish-price">
                    <text class="price-symbol">¥</text>
                    <text class="price-value">{{ item.dish.price }}</text>
                  </view>
                  <view class="dish-quantity">×{{ item.quantity }}</view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 订单信息 -->
      <view class="order-info-section card">
        <view class="section-title">订单信息</view>

        <view class="info-item">
          <text class="info-label">订单编号</text>
          <view class="info-value-wrapper">
            <text class="info-value">{{ orderInfo.orderNo }}</text>
            <text class="copy-btn" @click="copyOrderNo">复制</text>
          </view>
        </view>

        <view class="info-item">
          <text class="info-label">下单时间</text>
          <text class="info-value">{{ orderInfo.createTime }}</text>
        </view>

        <view class="info-item">
          <text class="info-label">支付方式</text>
          <text class="info-value">{{ orderInfo.paymentMethod }}</text>
        </view>

        <view class="info-item">
          <text class="info-label">配送时间</text>
          <text class="info-value">{{ orderInfo.deliveryTime }}</text>
        </view>
      </view>

      <!-- 收货地址 -->
      <view class="address-section card">
        <view class="section-title">收货地址</view>

        <view class="address-content">
          <view class="address-header">
            <text class="address-name">{{ orderAddress.name }}</text>
            <text class="address-phone">{{ orderAddress.phone }}</text>
          </view>
          <view class="address-detail">
            <text class="address-text">{{ orderAddress.address }}</text>
          </view>
        </view>
      </view>

      <!-- 金额明细 -->
      <view class="amount-section card">
        <view class="section-title">金额明细</view>

        <view class="amount-item">
          <text class="amount-label">商品金额</text>
          <text class="amount-value">¥{{ orderAmount.dishPrice }}</text>
        </view>

        <view class="amount-item">
          <text class="amount-label">配送费</text>
          <text class="amount-value">¥{{ orderAmount.deliveryFee }}</text>
        </view>

        <view class="amount-item">
          <text class="amount-label">包装费</text>
          <text class="amount-value">¥{{ orderAmount.packingFee }}</text>
        </view>

        <view class="amount-item" v-if="orderAmount.couponDiscount > 0">
          <text class="amount-label">优惠券</text>
          <text class="amount-value discount">-¥{{ orderAmount.couponDiscount }}</text>
        </view>

        <view class="amount-item total">
          <text class="amount-label">实付金额</text>
          <view class="amount-value-wrapper">
            <text class="amount-value final">¥{{ orderAmount.totalPrice }}</text>
          </view>
        </view>
      </view>

      <!-- 备注 -->
      <view class="remark-section card" v-if="orderRemark">
        <view class="section-title">备注</view>
        <view class="remark-content">{{ orderRemark }}</view>
      </view>

      <!-- 联系商家 -->
      <view class="contact-section card">
        <view class="contact-btn" @click="contactMerchant">
          <text class="contact-icon">💬</text>
          <text class="contact-text">联系商家</text>
        </view>
        <view class="contact-btn" @click="callMerchant">
          <text class="contact-icon">📞</text>
          <text class="contact-text">拨打电话</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="action-buttons">
        <button
          class="action-btn secondary"
          v-if="orderStatus.value === 'pending'"
          @click="cancelOrder"
        >
          取消订单
        </button>

        <button
          class="action-btn secondary"
          v-if="orderStatus.value === 'delivering'"
          @click="confirmReceipt"
        >
          确认收货
        </button>

        <button
          class="action-btn primary"
          v-if="orderStatus.value === 'completed'"
          @click="reviewOrder"
        >
          评价订单
        </button>

        <button
          class="action-btn primary"
          v-if="orderStatus.value === 'pending'"
          @click="payOrder"
        >
          立即支付
        </button>

        <button
          class="action-btn secondary"
          @click="orderAgain"
        >
          再来一单
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { orderApi } from '@/api'

// 订单ID
const orderId = ref('')

// 订单状态
const orderStatus = ref({
  value: 'processing',
  icon: '🍳',
  text: '商家正在准备',
  tips: '预计还需20分钟送达'
})

// 订单进度
const orderProgress = ref([
  {
    title: '订单已提交',
    time: '2026-03-17 12:00',
    completed: true
  },
  {
    title: '商家已接单',
    time: '2026-03-17 12:02',
    completed: true
  },
  {
    title: '商家正在准备',
    time: '2026-03-17 12:05',
    completed: true
  },
  {
    title: '骑手已取餐',
    time: '',
    completed: false
  },
  {
    title: '已送达',
    time: '',
    completed: false
  }
])

// 当前进度索引
const currentProgressIndex = ref(2)

// 订单商品
const orderItems = ref([
  {
    merchantId: '1',
    merchant: {
      id: '1',
      name: '老王家常菜',
      logo: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=老王'
    },
    items: [
      {
        dish: {
          id: '1',
          name: '宫保鸡丁',
          price: 28,
          image: 'https://via.placeholder.com/300x300/FF6B35/FFFFFF?text=宫保鸡丁'
        },
        quantity: 2,
        spec: ''
      },
      {
        dish: {
          id: '4',
          name: '麻婆豆腐',
          price: 18,
          image: 'https://via.placeholder.com/300x300/faad14/FFFFFF?text=麻婆豆腐'
        },
        quantity: 1,
        spec: '微辣'
      }
    ]
  }
])

// 订单信息
const orderInfo = ref({
  orderNo: 'OD20260317120000001',
  createTime: '2026-03-17 12:00:00',
  paymentMethod: '微信支付',
  deliveryTime: '尽快送达（预计30分钟）'
})

// 收货地址
const orderAddress = ref({
  name: '张三',
  phone: '138****8888',
  address: '北京市朝阳区XX街道XX小区XX号楼XX单元XX室'
})

// 订单金额
const orderAmount = ref({
  dishPrice: '74.00',
  deliveryFee: '5.00',
  packingFee: '3.00',
  couponDiscount: '10.00',
  totalPrice: '72.00'
})

// 备注
const orderRemark = ref('少放油，多放葱')

/**
 * 计算属性：根据订单状态返回不同的状态信息
 */
const statusConfig = computed(() => {
  const statusMap = {
    pending: {
      icon: '⏰',
      text: '等待支付',
      tips: '请在15分钟内完成支付'
    },
    paid: {
      icon: '✅',
      text: '支付成功',
      tips: '商家正在接单'
    },
    processing: {
      icon: '🍳',
      text: '商家正在准备',
      tips: '预计还需20分钟送达'
    },
    delivering: {
      icon: '🚴',
      text: '骑手配送中',
      tips: '骑手正在快速赶往目的地'
    },
    completed: {
      icon: '🎉',
      text: '订单已完成',
      tips: '感谢您的订购，期待再次光临'
    },
    cancelled: {
      icon: '❌',
      text: '订单已取消',
      tips: '订单已取消，如有疑问请联系客服'
    },
    refunding: {
      icon: '⏳',
      text: '退款中',
      tips: '正在处理退款，请耐心等待'
    },
    refunded: {
      icon: '💰',
      text: '已退款',
      tips: '退款已原路返回'
    }
  }
  return statusMap[orderStatus.value] || statusMap.pending
})

/**
 * 复制订单号
 */
const copyOrderNo = () => {
  uni.setClipboardData({
    data: orderInfo.value.orderNo,
    success: () => {
      uni.showToast({
        title: '已复制',
        icon: 'success'
      })
    }
  })
}

/**
 * 联系商家 - U-007: 跳转到聊天页
 */
const contactMerchant = async () => {
  try {
    // 获取商家ID（从第一个订单商品组中获取）
    const merchantId = orderItems.value[0]?.merchantId
    if (!merchantId) {
      uni.showToast({
        title: '商家信息不存在',
        icon: 'none'
      })
      return
    }

    // 调用API创建或获取与商家的会话
    const { chatApi } = await import('@/api')
    const res = await chatApi.createConversation({
      targetUserId: merchantId,
      orderId: orderId.value
    })

    if (res.code === 200 || res.conversationId) {
      const conversationId = res.conversationId || res.data?.conversationId || res.id
      // 跳转到聊天页面
      uni.navigateTo({
        url: `/pages-common/chat/chat-room?conversationId=${conversationId}&merchantId=${merchantId}`,
        fail: () => {
          // 如果页面路径不存在，尝试其他可能的路径
          uni.navigateTo({
            url: `/pages-user/chat/chat-room?conversationId=${conversationId}&merchantId=${merchantId}`
          })
        }
      })
    } else {
      throw new Error(res.message || '创建会话失败')
    }
  } catch (error) {
    console.error('跳转聊天页面失败:', error)
    uni.showToast({
      title: error.message || '打开聊天失败',
      icon: 'none'
    })
  }
}

/**
 * 拨打电话
 */
const callMerchant = () => {
  uni.makePhoneCall({
    phoneNumber: '13800138000'
  })
}

/**
 * 取消订单
 */
const cancelOrder = async () => {
  uni.showModal({
    title: '取消订单',
    content: '确定要取消这个订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '处理中...' })

          await orderApi.cancel(orderId.value)

          uni.hideLoading()
          uni.showToast({
            title: '订单已取消',
            icon: 'success'
          })

          // 重新加载订单详情
          await loadOrderDetail()
        } catch (error) {
          console.error('取消订单失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: error.message || '取消失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 确认收货
 */
const confirmReceipt = async () => {
  uni.showModal({
    title: '确认收货',
    content: '确认已收到商品吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '处理中...' })

          await orderApi.confirm(orderId.value)

          uni.hideLoading()
          uni.showToast({
            title: '确认收货成功',
            icon: 'success'
          })

          // 重新加载订单详情
          await loadOrderDetail()
        } catch (error) {
          console.error('确认收货失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: error.message || '确认失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 评价订单 - U-008: 跳转到评价页
 */
const reviewOrder = () => {
  if (!orderId.value) {
    uni.showToast({
      title: '订单信息不存在',
      icon: 'none'
    })
    return
  }

  // 跳转到评价提交页面，携带订单ID
  uni.navigateTo({
    url: `/pages-user/review/submit/index?orderId=${orderId.value}`,
    success: () => {
      console.log('跳转到评价页面成功')
    },
    fail: (err) => {
      console.error('跳转评价页面失败:', err)
      uni.showToast({
        title: '打开评价页面失败',
        icon: 'none'
      })
    }
  })
}

/**
 * 立即支付 - U-009: 调用支付API
 */
const payOrder = async () => {
  if (!orderId.value) {
    uni.showToast({
      title: '订单信息不存在',
      icon: 'none'
    })
    return
  }

  // 确认支付对话框
  uni.showModal({
    title: '确认支付',
    content: `支付金额：¥${orderAmount.value.totalPrice}`,
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '正在支付...' })

          // 导入支付API
          const { paymentApi } = await import('@/api')

          // 步骤1：创建支付订单
          const createPaymentRes = await paymentApi.createPayment({
            orderId: orderId.value,
            userId: uni.getStorageSync('userId') || uni.getStorageSync('userInfo')?.id,
            paymentMethod: 'wechat' // 默认微信支付，可以根据实际需求调整
          })

          uni.hideLoading()

          if (createPaymentRes.code !== 200) {
            throw new Error(createPaymentRes.message || '创建支付订单失败')
          }

          const paymentNo = createPaymentRes.data?.paymentNo
          if (!paymentNo) {
            throw new Error('支付流水号获取失败')
          }

          // 步骤2：调用微信支付
          uni.showLoading({ title: '调起支付...' })
          const wechatPayRes = await paymentApi.wechatPay({ paymentNo })
          uni.hideLoading()

          if (wechatPayRes.code !== 200) {
            throw new Error(wechatPayRes.message || '获取支付参数失败')
          }

          // 步骤3：调起微信支付
          const payParams = wechatPayRes.data
          uni.requestPayment({
            provider: 'wxpay',
            timeStamp: payParams.timeStamp,
            nonceStr: payParams.nonceStr,
            package: payParams.package,
            signType: payParams.signType || 'MD5',
            paySign: payParams.paySign,
            success: async () => {
              // 支付成功
              uni.showToast({
                title: '支付成功',
                icon: 'success',
                duration: 2000
              })

              // 更新订单状态
              orderStatus.value = {
                value: 'paid',
                icon: '✅',
                text: '支付成功',
                tips: '商家将尽快接单'
              }
              currentProgressIndex.value = 1

              // 重新加载订单详情
              setTimeout(async () => {
                await loadOrderDetail()
              }, 1000)
            },
            fail: (err) => {
              // 支付失败或取消
              console.error('支付失败:', err)
              if (err.errMsg.includes('cancel')) {
                uni.showToast({
                  title: '已取消支付',
                  icon: 'none'
                })
              } else {
                uni.showToast({
                  title: '支付失败，请重试',
                  icon: 'none'
                })
              }
            }
          })
        } catch (error) {
          console.error('支付失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: error.message || '支付失败，请重试',
            icon: 'none',
            duration: 2000
          })
        }
      }
    }
  })
}

/**
 * 再来一单 - U-010: 将商品添加到购物车
 */
const orderAgain = async () => {
  try {
    uni.showLoading({ title: '加入购物车...' })

    // 导入购物车API
    const { cartApi } = await import('@/api')

    // 遍历所有订单商品，添加到购物车
    const addPromises = []
    const merchantMap = new Map()

    // 按商家分组商品
    orderItems.value.forEach(group => {
      const merchantId = group.merchantId
      if (!merchantMap.has(merchantId)) {
        merchantMap.set(merchantId, {
          merchantId,
          merchant: group.merchant,
          items: []
        })
      }

      group.items.forEach(item => {
        merchantMap.get(merchantId).items.push(item)
      })
    })

    // 为每个商品创建添加到购物车的Promise
    for (const [merchantId, group] of merchantMap) {
      group.items.forEach(item => {
        const promise = cartApi.add({
          dishId: item.dish.id,
          merchantId: merchantId,
          quantity: item.quantity,
          spec: item.spec || '',
          remark: ''
        })
        addPromises.push(promise)
      })
    }

    // 等待所有添加操作完成
    await Promise.all(addPromises)

    uni.hideLoading()
    uni.showToast({
      title: '已加入购物车',
      icon: 'success',
      duration: 2000
    })

    // 跳转到购物车页面
    setTimeout(() => {
      uni.navigateTo({
        url: '/src/pages-user/cart/index'
      })
    }, 1500)
  } catch (error) {
    console.error('加入购物车失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '加入购物车失败',
      icon: 'none'
    })
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.id) {
    orderId.value = options.id
    await loadOrderDetail()
  }
})

/**
 * 加载订单详情
 */
const loadOrderDetail = async () => {
  try {
    uni.showLoading({ title: '加载中...' })

    const res = await orderApi.getDetail(orderId.value)

    // 数据映射
    orderStatus.value = mapOrderStatus(res.status || res.orderStatus)

    // 映射订单商品
    if (res.items && Array.isArray(res.items)) {
      orderItems.value = [{
        merchantId: res.merchantId || res.merchant?.id,
        merchant: {
          id: res.merchantId || res.merchant?.id,
          name: res.merchantName || res.merchant?.name,
          logo: res.merchant?.logo || res.merchant?.avatar || ''
        },
        items: res.items.map(item => ({
          dish: {
            id: item.dishId || item.dish?.id,
            name: item.dishName || item.dish?.name,
            price: item.price,
            image: item.dish?.image || item.dish?.coverImage || ''
          },
          quantity: item.quantity,
          spec: item.spec || ''
        }))
      }]
    }

    // 订单基本信息
    orderInfo.value = {
      orderNo: res.orderNo || res.orderNumber,
      createTime: res.createTime || res.createdAt,
      paymentMethod: mapPaymentMethod(res.paymentMethod),
      deliveryTime: res.deliveryTime || res.expectedDeliveryTime || '尽快送达'
    }

    // 收货地址
    if (res.address) {
      orderAddress.value = {
        name: res.address.receiverName || res.address.name,
        phone: res.address.receiverPhone || res.address.phone,
        address: `${res.address.province || ''}${res.address.city || ''}${res.address.district || ''}${res.address.detailAddress || res.address.address || ''}`
      }
    }

    // 订单金额
    if (res.amount) {
      orderAmount.value = {
        subtotal: parseFloat(res.amount.subtotal || res.amount.goodsAmount || 0),
        deliveryFee: parseFloat(res.amount.deliveryFee || 0),
        packingFee: parseFloat(res.amount.packingFee || 0),
        discount: parseFloat(res.amount.discount || res.amount.couponDiscount || 0),
        total: parseFloat(res.amount.total || res.amount.finalAmount || 0)
      }
    }

    // 订单进度
    if (res.progress && Array.isArray(res.progress)) {
      orderProgress.value = res.progress.map((step, index) => ({
        title: step.title || step.status,
        time: step.time || step.createTime || '',
        completed: step.completed || step.status === 'completed'
      }))

      // 找到当前进度
      const currentIndex = orderProgress.value.findIndex(step => !step.completed)
      currentProgressIndex.value = currentIndex === -1 ? orderProgress.value.length - 1 : currentIndex
    }

    uni.hideLoading()
  } catch (error) {
    console.error('加载订单详情失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 映射订单状态
 */
const mapOrderStatus = (status) => {
  const statusMap = {
    'pending': { value: 'pending', icon: '⏳', text: '等待支付', tips: '请尽快完成支付' },
    'paid': { value: 'paid', icon: '✅', text: '支付成功', tips: '商家将尽快接单' },
    'confirmed': { value: 'confirmed', icon: '👨‍🍳', text: '商家已接单', tips: '正在准备您的餐品' },
    'preparing': { value: 'preparing', icon: '🍳', text: '正在准备', tips: '预计还需20分钟' },
    'ready': { value: 'ready', icon: '📦', text: '已出餐', tips: '等待骑手取餐' },
    'delivering': { value: 'delivering', icon: '🚴', text: '配送中', tips: '骑手正在配送中' },
    'completed': { value: 'completed', icon: '✅', text: '已送达', tips: '感谢您的订购' },
    'cancelled': { value: 'cancelled', icon: '❌', text: '已取消', tips: '订单已取消' },
    'refunded': { value: 'refunded', icon: '💰', text: '已退款', tips: '退款将在3-5个工作日到账' }
  }
  return statusMap[status] || { value: status, icon: '📋', text: '未知状态', tips: '' }
}

/**
 * 映射支付方式
 */
const mapPaymentMethod = (method) => {
  const methodMap = {
    'wechat': '微信支付',
    'alipay': '支付宝',
    'balance': '余额支付'
  }
  return methodMap[method] || method || '在线支付'
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.order-detail-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 140rpx;
}

/* 订单状态卡片 */
.status-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60rpx $spacing-md;
  text-align: center;
  color: #fff;
}

.status-icon {
  font-size: 120rpx;
  margin-bottom: $spacing-md;
}

.status-text {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-sm;
}

.status-tips {
  font-size: $font-size-sm;
  opacity: 0.9;
}

.scroll-container {
  height: calc(100vh - 140rpx);
}

.card {
  background-color: $bg-color-white;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-md;
}

/* 订单进度 */
.progress-section {
  .progress-list {
    display: flex;
    align-items: flex-start;
  }

  .progress-item {
    flex: 1;
    position: relative;

    &.active {
      .progress-dot {
        background-color: $primary-color;
      }

      .progress-title {
        color: $primary-color;
      }
    }
  }

  .progress-dot {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    background-color: $border-color-base;
    @include flex-center;
    color: #fff;
    font-size: $font-size-sm;
    margin-bottom: $spacing-sm;
    z-index: 1;
  }

  .progress-line {
    position: absolute;
    top: 24rpx;
    left: 50%;
    width: 100%;
    height: 2rpx;
    background-color: $border-color-base;
    z-index: 0;
  }

  .progress-info {
    padding: 0 $spacing-xs;
  }

  .progress-title {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $text-color-primary;
    margin-bottom: $spacing-xs;
  }

  .progress-time {
    font-size: $font-size-xs;
    color: $text-color-secondary;
  }
}

/* 订单商品 */
.dishes-section {
  .merchant-group {
    margin-bottom: $spacing-md;
    padding-bottom: $spacing-md;
    border-bottom: 1rpx solid $border-color-light;

    &:last-child {
      margin-bottom: 0;
      padding-bottom: 0;
      border-bottom: none;
    }
  }

  .merchant-info {
    @include flex-center;
    gap: $spacing-sm;
    margin-bottom: $spacing-md;
  }

  .merchant-logo {
    width: 60rpx;
    height: 60rpx;
    border-radius: $border-radius-sm;
  }

  .merchant-name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $text-color-primary;
  }

  .dish-list {
    .dish-item {
      display: flex;
      margin-bottom: $spacing-md;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  .dish-image {
    width: 120rpx;
    height: 120rpx;
    border-radius: $border-radius-base;
    flex-shrink: 0;
  }

  .dish-info {
    flex: 1;
    margin-left: $spacing-sm;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .dish-name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $text-color-primary;
  }

  .dish-spec {
    font-size: $font-size-sm;
    color: $text-color-secondary;
    margin-top: $spacing-xs;
  }

  .dish-bottom {
    @include flex-between;
    align-items: center;
  }

  .dish-price {
    @include flex-center;
    gap: 2rpx;
    color: $danger-color;
    font-weight: $font-weight-bold;

    .price-symbol {
      font-size: $font-size-sm;
    }

    .price-value {
      font-size: $font-size-lg;
    }
  }

  .dish-quantity {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }
}

/* 订单信息 */
.order-info-section {
  .info-item {
    @include flex-between;
    padding: $spacing-sm 0;
    border-bottom: 1rpx solid $border-color-light;

    &:last-child {
      border-bottom: none;
    }
  }

  .info-label {
    font-size: $font-size-base;
    color: $text-color-secondary;
  }

  .info-value-wrapper {
    @include flex-center;
    gap: $spacing-sm;
  }

  .info-value {
    font-size: $font-size-base;
    color: $text-color-primary;
  }

  .copy-btn {
    font-size: $font-size-sm;
    color: $primary-color;
  }
}

/* 收货地址 */
.address-section {
  .address-content {
    padding: $spacing-sm;
    background-color: $bg-color-base;
    border-radius: $border-radius-base;
  }

  .address-header {
    @include flex-center;
    gap: $spacing-md;
    margin-bottom: $spacing-xs;
  }

  .address-name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $text-color-primary;
  }

  .address-phone {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }

  .address-detail {
    .address-text {
      font-size: $font-size-base;
      color: $text-color-regular;
      line-height: $line-height-lg;
    }
  }
}

/* 金额明细 */
.amount-section {
  .amount-item {
    @include flex-between;
    padding: $spacing-sm 0;

    &.total {
      margin-top: $spacing-md;
      padding-top: $spacing-md;
      border-top: 1rpx solid $border-color-light;
    }
  }

  .amount-label {
    font-size: $font-size-base;
    color: $text-color-secondary;
  }

  .amount-value {
    font-size: $font-size-base;
    color: $text-color-primary;

    &.discount {
      color: $danger-color;
    }

    &.final {
      font-size: $font-size-xl;
      font-weight: $font-weight-bold;
      color: $danger-color;
    }
  }

  .amount-value-wrapper {
    @include flex-center;
    gap: 2rpx;
  }
}

/* 备注 */
.remark-section {
  .remark-content {
    padding: $spacing-sm;
    background-color: $bg-color-base;
    border-radius: $border-radius-base;
    font-size: $font-size-base;
    color: $text-color-regular;
    line-height: $line-height-lg;
  }
}

/* 联系商家 */
.contact-section {
  @include flex-center;
  gap: $spacing-md;
}

.contact-btn {
  flex: 1;
  @include flex-center-column;
  gap: $spacing-sm;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
}

.contact-icon {
  font-size: 48rpx;
}

.contact-text {
  font-size: $font-size-sm;
  color: $text-color-regular;
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: $z-index-fixed;
}

.action-buttons {
  @include flex-center;
  gap: $spacing-md;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  @include flex-center;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-round;
  border: none;

  &.primary {
    background-color: $primary-color;
    color: #fff;
  }

  &.secondary {
    background-color: $bg-color-base;
    color: $text-color-primary;
  }
}
</style>
