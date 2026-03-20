<!--
页面名称：order/progress/index（重构版）
原代码行数：1221行
重构后行数：约300行
减少比例：75%
重构时间：2026-03-20
-->
<template>
  <view class="order-progress-container">
    <!-- 加载状态 -->
    <view class="loading-state" v-if="loading">
      <uni-load-more status="loading" text="加载中..." />
    </view>

    <!-- 订单进度内容 -->
    <view class="progress-content" v-else-if="order">
      <!-- 订单状态卡片 -->
      <OrderStatus :status="order.status" />

      <scroll-view class="scroll-container" scroll-y>
        <!-- 进度时间轴 -->
        <OrderTimeline
          :steps="timelineSteps"
          :current-index="currentProgressIndex"
        />

        <!-- 商家信息 -->
        <view class="card merchant-section">
          <view class="section-title">商家信息</view>
          <view class="merchant-card" @tap="contactMerchant">
            <image class="merchant-image" :src="order.merchant.image" mode="aspectFill" />
            <view class="merchant-info">
              <text class="merchant-name">{{ order.merchant.name }}</text>
              <text class="merchant-address">{{ order.merchant.address }}</text>
            </view>
            <view class="contact-btn">
              <text class="btn-icon">📞</text>
            </view>
          </view>
        </view>

        <!-- 配送信息 -->
        <view class="card delivery-section" v-if="order.delivery">
          <view class="section-title">配送信息</view>

          <!-- 骑手信息 -->
          <RiderInfoCard
            v-if="order.delivery.rider"
            :rider="order.delivery.rider"
            @contact="contactRider"
          />

          <!-- 配送地址 -->
          <view class="delivery-address">
            <view class="address-item">
              <text class="address-icon">📍</text>
              <view class="address-detail">
                <text class="address-text">{{ order.delivery.address }}</text>
                <text class="address-contact">{{ order.delivery.contact }} {{ order.delivery.phone }}</text>
              </view>
            </view>
          </view>

          <!-- 配送地图 -->
          <DeliveryMap
            v-if="order.delivery.showMap"
            :rider="order.delivery.rider || {}"
            :merchant="order.delivery.merchant || {}"
            :user="order.delivery.user || {}"
          />
        </view>

        <!-- 订单详情 -->
        <view class="card order-detail-section">
          <view class="section-header">
            <text class="section-title">订单详情</text>
            <text class="order-no">订单号：{{ order.orderNo }}</text>
          </view>

          <OrderDishList :items="orderItems" />

          <OrderAmount :amount="orderAmount" />
        </view>

        <!-- 订单备注 -->
        <view class="card remark-section" v-if="order.remark">
          <view class="section-title">订单备注</view>
          <text class="remark-text">{{ order.remark }}</text>
        </view>
      </scroll-view>

      <!-- 底部操作栏 -->
      <OrderActions
        :status="order.status"
        @cancel="cancelOrder"
        @confirm="confirmReceipt"
        @review="reviewOrder"
        @reorder="orderAgain"
      />
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <view class="empty-content">
        <text class="empty-icon">📦</text>
        <text class="empty-text">订单不存在</text>
        <text class="empty-desc">该订单可能已被删除</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import OrderStatus from '@/components/business/order/OrderStatus.vue'
import OrderTimeline from '@/components/business/order/OrderTimeline.vue'
import OrderDishList from '@/components/business/order/OrderDishList.vue'
import OrderAmount from '@/components/business/order/OrderAmount.vue'
import OrderActions from '@/components/business/order/OrderActions.vue'
import RiderInfoCard from './components/RiderInfoCard.vue'
import DeliveryMap from './components/DeliveryMap.vue'

// 加载状态
const loading = ref(true)

// 订单信息
const order = ref(null)

// 订单进度
const timelineSteps = ref([])
const currentProgressIndex = ref(0)

// 订单商品
const orderItems = ref([])

// 订单金额
const orderAmount = ref({})

onLoad((options) => {
  if (options && options.id) {
    loadOrderProgress(options.id)
  }
})

/**
 * 加载订单进度
 */
const loadOrderProgress = async (orderId) => {
  try {
    loading.value = true

    // const res = await orderApi.getProgress(orderId)

    // 模拟数据
    setTimeout(() => {
      order.value = {
        id: orderId,
        orderNo: 'DD' + Date.now(),
        status: 'delivering',
        remark: '少放辣，多放葱',
        merchant: {
          id: '1',
          name: '老王家常菜',
          image: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=店',
          address: '深圳市南山区科技园'
        },
        delivery: {
          rider: {
            name: '骑手小李',
            phone: '138****8888',
            avatar: 'https://via.placeholder.com/80/722ED1/FFFFFF?text=骑',
            latitude: 22.5431,
            longitude: 114.0579
          },
          address: '广东省深圳市南山区科技园南区XX大厦A座1001',
          contact: '张三',
          phone: '138****8888',
          showMap: true
        }
      }

      timelineSteps.value = [
        { title: '订单已提交', time: '12:30', completed: true },
        { title: '支付成功', time: '12:31', completed: true },
        { title: '商家已接单', time: '12:32', completed: true },
        { title: '制作完成', time: '12:45', completed: true },
        { title: '骑手已取餐', time: '12:46', completed: false },
        { title: '配送中', time: '', completed: false },
        { title: '已送达', time: '', completed: false }
      ]

      currentProgressIndex.value = 3

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

      orderAmount.value = {
        dishPrice: '28.00',
        deliveryFee: '5.00',
        packingFee: '2.00',
        couponDiscount: '0.00',
        totalPrice: '35.00'
      }

      loading.value = false
    }, 500)
  } catch (error) {
    console.error('加载订单进度失败:', error)
    loading.value = false
  }
}

/**
 * 联系商家
 */
const contactMerchant = () => {
  uni.navigateTo({
    url: `/pages-common/chat/chat-room?userId=${order.value.merchant.id}`
  })
}

/**
 * 联系骑手
 */
const contactRider = () => {
  uni.makePhoneCall({
    phoneNumber: '138****8888'
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
        uni.showToast({
          title: '确认成功',
          icon: 'success'
        })
        loadOrderProgress(order.value.id)
      }
    }
  })
}

/**
 * 评价订单
 */
const reviewOrder = () => {
  uni.navigateTo({
    url: `/pages/review/create?orderId=${order.value.id}`
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

.order-progress-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

.loading-state {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
}

.progress-content {
  padding: 20rpx;
}

.scroll-container {
  padding-bottom: 20rpx;
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

/* 商家信息 */
.merchant-section {
  .merchant-card {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 25rpx;
    background: #F5F5F5;
    border-radius: 12rpx;
  }

  .merchant-image {
    width: 80rpx;
    height: 80rpx;
    border-radius: 12rpx;
  }

  .merchant-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
  }

  .merchant-name {
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
  }

  .merchant-address {
    font-size: 22rpx;
    color: #999;
  }

  .contact-btn {
    width: 70rpx;
    height: 70rpx;
    background: #fff;
    border-radius: 50%;
    @include flex-center;
  }

  .btn-icon {
    font-size: 32rpx;
  }
}

/* 配送信息 */
.delivery-section {
  .delivery-address {
    margin-top: 20rpx;
  }

  .address-item {
    display: flex;
    gap: 15rpx;
  }

  .address-icon {
    font-size: 32rpx;
  }

  .address-detail {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
  }

  .address-text {
    font-size: 26rpx;
    color: #333;
    line-height: 1.6;
  }

  .address-contact {
    font-size: 24rpx;
    color: #999;
  }
}

/* 订单详情 */
.order-detail-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }

  .order-no {
    font-size: 22rpx;
    color: #999;
  }
}

/* 备注 */
.remark-section {
  .remark-text {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
  }
}

/* 空状态 */
.empty-state {
  padding: 200rpx 0;
  display: flex;
  justify-content: center;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
}

.empty-icon {
  font-size: 100rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #333;
}

.empty-desc {
  font-size: 24rpx;
  color: #999;
}
</style>
