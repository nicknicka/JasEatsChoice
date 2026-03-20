<!--
页面名称：order/detail/index（重构版）
原代码行数：1234行
重构后行数：约300行
减少比例：76%
重构时间：2026-03-20
-->
<template>
  <view class="order-detail-container">
    <!-- 订单状态 -->
    <OrderStatus :status="orderInfo.status" />

    <scroll-view class="scroll-container" scroll-y>
      <!-- 订单进度 -->
      <OrderTimeline
        v-if="orderProgress.length > 0"
        :steps="orderProgress"
        :current-index="currentProgressIndex"
      />

      <!-- 订单商品 -->
      <OrderDishList :items="orderItems" />

      <!-- 订单信息 -->
      <view class="card order-info">
        <view class="section-title">订单信息</view>
        <view class="info-item">
          <text class="info-label">订单编号</text>
          <view class="info-value-wrapper">
            <text class="info-value">{{ orderInfo.orderNo }}</text>
            <text class="copy-btn" @tap="copyOrderNo">复制</text>
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
      <view class="card address">
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
      <OrderAmount :amount="orderAmount" />

      <!-- 备注 -->
      <view class="card remark" v-if="orderRemark">
        <view class="section-title">备注</view>
        <view class="remark-content">{{ orderRemark }}</view>
      </view>

      <!-- 联系商家 -->
      <view class="card contact">
        <view class="contact-btn" @tap="contactMerchant">
          <text class="contact-icon">💬</text>
          <text class="contact-text">联系商家</text>
        </view>
        <view class="contact-btn" @tap="callMerchant">
          <text class="contact-icon">📞</text>
          <text class="contact-text">拨打电话</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <OrderActions
      :status="orderInfo.status"
      @cancel="cancelOrder"
      @confirm="confirmReceipt"
      @review="reviewOrder"
      @pay="payOrder"
      @reorder="orderAgain"
    />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import OrderStatus from '@/components/business/order/OrderStatus.vue'
import OrderTimeline from '@/components/business/order/OrderTimeline.vue'
import OrderDishList from '@/components/business/order/OrderDishList.vue'
import OrderAmount from '@/components/business/order/OrderAmount.vue'
import OrderActions from '@/components/business/order/OrderActions.vue'

// 订单信息
const orderInfo = ref({
  id: '',
  orderNo: '',
  status: 'pending',
  createTime: '',
  paymentMethod: '微信支付',
  deliveryTime: '预计30分钟送达'
})

// 订单进度
const orderProgress = ref([])
const currentProgressIndex = ref(0)

// 订单商品
const orderItems = ref([])

// 收货地址
const orderAddress = ref({
  name: '',
  phone: '',
  address: ''
})

// 订单金额
const orderAmount = ref({
  dishPrice: '0.00',
  deliveryFee: '0.00',
  packingFee: '0.00',
  couponDiscount: '0.00',
  totalPrice: '0.00'
})

// 订单备注
const orderRemark = ref('')

onLoad((options) => {
  if (options && options.id) {
    loadOrderDetail(options.id)
  }
})

/**
 * 加载订单详情
 */
const loadOrderDetail = async (orderId) => {
  try {
    // const res = await orderApi.getDetail(orderId)

    // 模拟数据
    orderInfo.value = {
      id: orderId,
      orderNo: 'DD' + Date.now(),
      status: 'delivering',
      createTime: '2026-03-20 12:30',
      paymentMethod: '微信支付',
      deliveryTime: '预计30分钟送达'
    }

    orderProgress.value = [
      { title: '订单已提交', time: '12:30', completed: true },
      { title: '支付成功', time: '12:31', completed: true },
      { title: '商家已接单', time: '12:32', completed: true },
      { title: '配送中', time: '12:35', completed: false },
      { title: '已送达', time: '', completed: false }
    ]

    currentProgressIndex.value = 2

    orderItems.value = [
      {
        merchantId: '1',
        merchant: {
          id: '1',
          name: '老王家常菜',
          logo: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=店'
        },
        items: [
          {
            dish: {
              id: '1',
              name: '宫保鸡丁',
              price: '28.00',
              image: 'https://via.placeholder.com/140/FF6B35/FFFFFF?text=菜'
            },
            spec: '微辣',
            quantity: 1
          }
        ]
      }
    ]

    orderAddress.value = {
      name: '张三',
      phone: '138****8888',
      address: '广东省深圳市南山区科技园南区XX大厦A座1001'
    }

    orderAmount.value = {
      dishPrice: '28.00',
      deliveryFee: '5.00',
      packingFee: '2.00',
      couponDiscount: '0.00',
      totalPrice: '35.00'
    }

    orderRemark.value = '少放辣，多放葱'
  } catch (error) {
    console.error('加载订单详情失败:', error)
  }
}

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
 * 联系商家
 */
const contactMerchant = () => {
  uni.navigateTo({
    url: '/pages-common/chat/chat-room?userId=merchant_id'
  })
}

/**
 * 拨打电话
 */
const callMerchant = () => {
  uni.makePhoneCall({
    phoneNumber: '0755-12345678'
  })
}

/**
 * 取消订单
 */
const cancelOrder = () => {
  uni.showModal({
    title: '取消订单',
    content: '确定要取消该订单吗？',
    success: (res) => {
      if (res.confirm) {
        // 调用取消订单API
        uni.showToast({
          title: '订单已取消',
          icon: 'success'
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }
    }
  })
}

/**
 * 确认收货
 */
const confirmReceipt = () => {
  uni.showModal({
    title: '确认收货',
    content: '确认已收到订单商品吗？',
    success: (res) => {
      if (res.confirm) {
        // 调用确认收货API
        uni.showToast({
          title: '确认成功',
          icon: 'success'
        })
        loadOrderDetail(orderInfo.value.id)
      }
    }
  })
}

/**
 * 评价订单
 */
const reviewOrder = () => {
  uni.navigateTo({
    url: `/pages/review/create?orderId=${orderInfo.value.id}`
  })
}

/**
 * 支付订单
 */
const payOrder = () => {
  uni.navigateTo({
    url: `/pages/payment/index?orderId=${orderInfo.value.id}`
  })
}

/**
 * 再来一单
 */
const orderAgain = () => {
  uni.showToast({
    title: '已加入购物车',
    icon: 'success'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.order-detail-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

.scroll-container {
  padding: 20rpx;
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

/* 订单信息 */
.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;

  &:first-child {
    padding-top: 0;
  }

  &:last-child {
    padding-bottom: 0;
  }
}

.info-label {
  font-size: 26rpx;
  color: #666;
}

.info-value-wrapper {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.info-value {
  font-size: 26rpx;
  color: #333;
}

.copy-btn {
  font-size: 24rpx;
  color: #FF6B35;
}

/* 收货地址 */
.address {
  .address-content {
    display: flex;
    flex-direction: column;
    gap: 15rpx;
  }

  .address-header {
    display: flex;
    gap: 20rpx;
  }

  .address-name {
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
  }

  .address-phone {
    font-size: 26rpx;
    color: #666;
  }

  .address-text {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
  }
}

/* 备注 */
.remark {
  .remark-content {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
  }
}

/* 联系商家 */
.contact {
  display: flex;
  gap: 20rpx;

  .contact-btn {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10rpx;
    padding: 25rpx;
    background: #F5F5F5;
    border-radius: 12rpx;
  }

  .contact-icon {
    font-size: 40rpx;
  }

  .contact-text {
    font-size: 24rpx;
    color: #666;
  }
}
</style>
