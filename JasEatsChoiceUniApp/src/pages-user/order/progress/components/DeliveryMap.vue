<!--
组件名称：DeliveryMap
用途：配送地图展示
页面专用：订单进度页面
创建时间：2026-03-20
-->
<template>
  <view class="delivery-map">
    <map
      class="map-view"
      :latitude="latitude"
      :longitude="longitude"
      :markers="markers"
      :polyline="polylines"
      :show-location="true"
    >
      <!-- 骑手位置标记 -->
      <cover-view class="map-marker rider">
        <cover-view class="marker-icon">🛵️</cover-view>
      </cover-view>

      <!-- 商家位置标记 -->
      <cover-view class="map-marker merchant" :style="merchantStyle">
        <cover-view class="marker-icon">🏪</cover-view>
      </cover-view>

      <!-- 用户位置标记 -->
      <cover-view class="map-marker user" :style="userStyle">
        <cover-view class="marker-icon">📍</cover-view>
      </cover-view>
    </map>

    <!-- 地图图例 -->
    <view class="map-legend">
      <view class="legend-item">
        <view class="legend-dot rider"></view>
        <text class="legend-text">骑手位置</text>
      </view>
      <view class="legend-item">
        <view class="legend-dot merchant"></view>
        <text class="legend-text">商家位置</text>
      </view>
      <view class="legend-item">
        <view class="legend-dot user"></view>
        <text class="legend-text">收货地址</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  rider: {
    type: Object,
    default: () => ({})
  },
  merchant: {
    type: Object,
    default: () => ({})
  },
  user: {
    type: Object,
    default: () => ({})
  }
})

const latitude = computed(() => {
  return props.rider.latitude || 22.5431
})

const longitude = computed(() => {
  return props.rider.longitude || 114.0579
})

const markers = computed(() => {
  const result = []

  if (props.merchant.latitude) {
    result.push({
      id: 1,
      latitude: props.merchant.latitude,
      longitude: props.merchant.longitude,
      iconPath: '/static/marker-merchant.png',
      width: 30,
      height: 30
    })
  }

  if (props.user.latitude) {
    result.push({
      id: 2,
      latitude: props.user.latitude,
      longitude: props.user.longitude,
      iconPath: '/static/marker-user.png',
      width: 30,
      height: 30
    })
  }

  return result
})

const polylines = computed(() => {
  if (!props.rider.path || props.rider.path.length === 0) {
    return []
  }

  return [{
    points: props.rider.path,
    color: '#FF6B35',
    width: 4,
    dottedLine: false
  }]
})

const merchantStyle = computed(() => {
  if (!props.merchant.latitude) return {}
  return {
    position: 'absolute',
    transform: 'translate(-50%, -100%)'
  }
})

const userStyle = computed(() => {
  if (!props.user.latitude) return {}
  return {
    position: 'absolute',
    transform: 'translate(-50%, -100%)'
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.delivery-map {
  position: relative;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
}

.map-view {
  width: 100%;
  height: 400rpx;
}

.map-marker {
  position: absolute;
  width: 60rpx;
  height: 60rpx;
  transform: translate(-50%, -100%);

  &.rider {
    z-index: 10;
  }

  &.merchant {
    z-index: 5;
  }

  &.user {
    z-index: 5;
  }
}

.marker-icon {
  width: 100%;
  height: 100%;
  font-size: 40rpx;
  @include flex-center;
}

.map-legend {
  position: absolute;
  bottom: 20rpx;
  left: 20rpx;
  right: 20rpx;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12rpx;
  padding: 20rpx;
  display: flex;
  justify-content: space-around;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.legend-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;

  &.rider {
    background: #FF6B35;
  }

  &.merchant {
    background: #52C41A;
  }

  &.user {
    background: #1677FF;
  }
}

.legend-text {
  font-size: 22rpx;
  color: #666;
}
</style>
