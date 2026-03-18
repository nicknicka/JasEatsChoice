<template>
  <view class="delivery-section" v-if="order.delivery">
    <view class="section-header">
      <text class="section-title">配送信息</text>
    </view>

    <view class="delivery-card">
      <!-- 骑手信息 -->
      <view class="rider-info" v-if="order.delivery.rider" @click="handleContactRider">
        <image class="rider-avatar" :src="order.delivery.rider.avatar" mode="aspectFill" />
        <view class="rider-detail">
          <text class="rider-name">{{ order.delivery.rider.name }}</text>
          <text class="rider-phone">{{ order.delivery.rider.phone }}</text>
        </view>
        <view class="contact-btn">
          <text class="btn-icon">📞</text>
        </view>
      </view>

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
      <view class="delivery-map" v-if="order.delivery.showMap && showMap">
        <map
          class="map-view"
          :latitude="order.delivery.latitude"
          :longitude="order.delivery.longitude"
          :markers="mapMarkers"
          :polyline="mapPolyline"
        />
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 订单数据
  order: {
    type: Object,
    required: true
  },
  // 是否显示地图
  showMap: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['contactRider'])

/**
 * 地图标记
 */
const mapMarkers = computed(() => {
  if (!props.order || !props.order.delivery) return []

  const markers = []

  // 商家位置
  if (props.order.merchant?.latitude && props.order.merchant?.longitude) {
    markers.push({
      id: 1,
      latitude: props.order.merchant.latitude,
      longitude: props.order.merchant.longitude,
      iconPath: '/static/marker-merchant.png',
      width: 30,
      height: 30
    })
  }

  // 骑手位置
  if (props.order.delivery.rider?.latitude && props.order.delivery.rider?.longitude) {
    markers.push({
      id: 2,
      latitude: props.order.delivery.rider.latitude,
      longitude: props.order.delivery.rider.longitude,
      iconPath: '/static/marker-rider.png',
      width: 30,
      height: 30
    })
  }

  // 收货地址
  if (props.order.delivery.latitude && props.order.delivery.longitude) {
    markers.push({
      id: 3,
      latitude: props.order.delivery.latitude,
      longitude: props.order.delivery.longitude,
      iconPath: '/static/marker-address.png',
      width: 30,
      height: 30
    })
  }

  return markers
})

/**
 * 地图路线
 */
const mapPolyline = computed(() => {
  if (!props.order || !props.order.delivery) return []

  const points = []

  if (props.order.merchant?.latitude && props.order.merchant?.longitude) {
    points.push({
      latitude: props.order.merchant.latitude,
      longitude: props.order.merchant.longitude
    })
  }

  if (props.order.delivery.latitude && props.order.delivery.longitude) {
    points.push({
      latitude: props.order.delivery.latitude,
      longitude: props.order.delivery.longitude
    })
  }

  if (points.length < 2) return []

  return [{
    points,
    color: '#FF6B35',
    width: 4,
    dottedLine: true
  }]
})

/**
 * 联系骑手
 */
const handleContactRider = () => {
  emit('contactRider', props.order.delivery.rider)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.delivery-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.section-header {
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.delivery-card {
  @include flex-center-column;
  gap: $spacing-md;
}

.rider-info {
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;

  &:active {
    background-color: rgba(0, 0, 0, 0.02);
  }
}

.rider-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.rider-detail {
  flex: 1;
  margin-left: $spacing-md;
  @include flex-center-column;
  align-items: flex-start;
  gap: 4rpx;
}

.rider-name {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.rider-phone {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.contact-btn {
  width: 72rpx;
  height: 72rpx;
  @include flex-center;
  background-color: $primary-color;
  border-radius: 50%;
  flex-shrink: 0;

  &:active {
    opacity: 0.8;
  }
}

.btn-icon {
  font-size: $font-size-xl;
  color: #fff;
}

.delivery-address {
  @include flex-center-column;
  gap: $spacing-md;
}

.address-item {
  @include flex-start;
  gap: $spacing-md;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  width: 100%;
}

.address-icon {
  font-size: $font-size-xl;
  flex-shrink: 0;
}

.address-detail {
  flex: 1;
  @include flex-center-column;
  align-items: flex-start;
  gap: 4rpx;
}

.address-text {
  font-size: $font-size-base;
  color: $text-color-primary;
}

.address-contact {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.delivery-map {
  margin-top: $spacing-md;
  border-radius: $border-radius-base;
  overflow: hidden;
}

.map-view {
  width: 100%;
  height: 400rpx;
}
</style>
