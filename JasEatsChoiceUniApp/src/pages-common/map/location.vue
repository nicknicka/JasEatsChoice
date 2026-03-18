<template>
  <view class="location-select">
    <!-- 地图容器 -->
    <view class="map-container">
      <map
        class="map"
        :longitude="longitude"
        :latitude="latitude"
        :scale="scale"
        :show-location="true"
        :markers="markers"
        @tap="onMapTap"
        @regionchange="onRegionChange"
      ></map>

      <!-- 当前位置按钮 -->
      <view class="current-location-btn" @click="moveToCurrentLocation">
        <text class="icon">📍</text>
      </view>
    </view>

    <!-- 地址信息卡片 -->
    <view class="address-card">
      <view class="card-header">
        <view class="location-info">
          <text class="location-icon">📍</text>
          <view class="location-text">
            <text class="address-title">{{ selectedAddress.name || '选择位置' }}</text>
            <text class="address-desc">{{ selectedAddress.address || '点击地图选择位置' }}</text>
          </view>
        </view>
        <view class="refresh-btn" @click="refreshAddress">
          <text class="icon">🔄</text>
        </view>
      </view>

      <!-- 详细地址输入 -->
      <view class="detail-address" v-if="showDetailInput">
        <input class="address-input" v-model="detailAddress" placeholder="输入详细地址（门牌号、楼层等）" placeholder-style="color: #999999" />
      </view>

      <!-- 常用地址 -->
      <view class="common-address" v-if="commonAddressList.length > 0">
        <view class="section-title">常用地址</view>
        <scroll-view class="address-list" scroll-y>
          <view class="address-item" v-for="(item, index) in commonAddressList" :key="index" @click="selectCommonAddress(item)">
            <view class="address-icon">
              <text class="icon">{{ item.type === 'home' ? '🏠' : item.type === 'company' ? '🏢' : '📍' }}</text>
            </view>
            <view class="address-info">
              <text class="address-name">{{ item.name }}</text>
              <text class="address-detail">{{ item.address }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 附近地点 -->
      <view class="nearby-places" v-if="nearbyPlaces.length > 0">
        <view class="section-title">附近地点</view>
        <scroll-view class="place-list" scroll-y>
          <view class="place-item" v-for="(place, index) in nearbyPlaces" :key="index" @click="selectNearbyPlace(place)">
            <view class="place-icon">
              <text class="icon">{{ place.icon }}</text>
            </view>
            <view class="place-info">
              <text class="place-name">{{ place.name }}</text>
              <text class="place-distance">{{ place.distance }}</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="btn btn-outline" @click="cancelSelect">取消</button>
      <button class="btn btn-primary" @click="confirmLocation">确认位置</button>
    </view>

    <!-- 详细地址输入开关 -->
    <view class="detail-toggle" @click="toggleDetailInput">
      <text class="toggle-text">{{ showDetailInput ? '收起' : '补充详细地址' }}</text>
      <text class="toggle-icon">{{ showDetailInput ? '▲' : '▼' }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onLoad } from 'vue'

// 地图中心点坐标
const longitude = ref(113.264385)
const latitude = ref(23.129112)
const scale = ref(16)

// 选中的地址
const selectedAddress = ref({
  name: '',
  address: '',
  longitude: 0,
  latitude: 0
})

// 详细地址
const detailAddress = ref('')
const showDetailInput = ref(false)

// 地图标记点
const markers = ref([])

// 常用地址列表
const commonAddressList = ref([
  {
    type: 'home',
    name: '家',
    address: '天河区珠江新城花城大道123号',
    longitude: 113.264385,
    latitude: 23.129112
  },
  {
    type: 'company',
    name: '公司',
    address: '天河区天河路208号粤海天河城',
    longitude: 113.320000,
    latitude: 23.130000
  }
])

// 附近地点
const nearbyPlaces = ref([
  { name: '广州东站', distance: '500m', icon: '🚉' },
  { name: '天河城广场', distance: '300m', icon: '🏬' },
  { name: '体育西路站', distance: '800m', icon: '🚇' }
])

onLoad(() => {
  // 获取当前位置
  getCurrentLocation()
})

// 获取当前位置
const getCurrentLocation = () => {
  uni.showLoading({ title: '定位中...' })

  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      longitude.value = res.longitude
      latitude.value = res.latitude
      addMarker(res.longitude, res.latitude)
      getAddressFromLocation(res.longitude, res.latitude)
      uni.hideLoading()
    },
    fail: () => {
      uni.hideLoading()
      uni.showToast({
        title: '定位失败',
        icon: 'error'
      })
    }
  })
}

// 移动到当前位置
const moveToCurrentLocation = () => {
  getCurrentLocation()
}

// 地图点击事件
const onMapTap = (e) => {
  const { longitude, latitude } = e.detail
  selectedAddress.value.longitude = longitude
  selectedAddress.value.latitude = latitude
  addMarker(longitude, latitude)
  getAddressFromLocation(longitude, latitude)
}

// 地图区域变化
const onRegionChange = (e) => {
  if (e.type === 'end') {
    // 地图移动结束，可以更新附近地点
    loadNearbyPlaces()
  }
}

// 添加标记点
const addMarker = (lng, lat) => {
  markers.value = [{
    id: 1,
    longitude: lng,
    latitude: lat,
    iconPath: '/static/images/marker.png',
    width: 30,
    height: 30,
    anchor: {
      x: 0.5,
      y: 1
    }
  }]
}

// 根据坐标获取地址
const getAddressFromLocation = (lng, lat) => {
  // 调用逆地理编码API
  // 这里使用模拟数据
  uni.showLoading({ title: '获取地址...' })

  setTimeout(() => {
    selectedAddress.value = {
      name: '天河区珠江新城',
      address: '广东省广州市天河区珠江新城花城大道',
      longitude: lng,
      latitude: lat
    }
    uni.hideLoading()
  }, 500)
}

// 刷新地址
const refreshAddress = () => {
  if (selectedAddress.value.longitude && selectedAddress.value.latitude) {
    getAddressFromLocation(selectedAddress.value.longitude, selectedAddress.value.latitude)
  }
}

// 选择常用地址
const selectCommonAddress = (item) => {
  selectedAddress.value = { ...item }
  longitude.value = item.longitude
  latitude.value = item.latitude
  addMarker(item.longitude, item.latitude)
}

// 选择附近地点
const selectNearbyPlace = (place) => {
  // 这里应该调用地理编码API获取地点坐标
  uni.showToast({
    title: `已选择${place.name}`,
    icon: 'success'
  })
}

// 加载附近地点
const loadNearbyPlaces = () => {
  // 调用周边搜索API
  // 这里使用模拟数据
}

// 切换详细地址输入
const toggleDetailInput = () => {
  showDetailInput.value = !showDetailInput.value
}

// 取消选择
const cancelSelect = () => {
  uni.navigateBack()
}

// 确认位置
const confirmLocation = () => {
  if (!selectedAddress.value.address) {
    uni.showToast({
      title: '请选择位置',
      icon: 'none'
    })
    return
  }

  const addressInfo = {
    ...selectedAddress.value,
    detailAddress: detailAddress.value,
    fullAddress: selectedAddress.value.address + (detailAddress.value ? detailAddress.value : '')
  }

  // 返回上一页并传递地址信息
  const pages = getCurrentPages()
  const prevPage = pages[pages.length - 2]
  if (prevPage) {
    prevPage.$vm.selectedLocation = addressInfo
  }

  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.location-select {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.map-container {
  position: relative;
  width: 100%;
  height: 60vh;

  .map {
    width: 100%;
    height: 100%;
  }

  .current-location-btn {
    position: absolute;
    right: 32rpx;
    bottom: 32rpx;
    width: 88rpx;
    height: 88rpx;
    background: #ffffff;
    border-radius: 50%;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
    display: flex;
    align-items: center;
    justify-content: center;

    .icon {
      font-size: 40rpx;
    }

    &:active {
      opacity: 0.8;
    }
  }
}

.address-card {
  flex: 1;
  background: #ffffff;
  border-radius: 24rpx 24rpx 0 0;
  margin-top: -32rpx;
  padding: 32rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24rpx;

    .location-info {
      flex: 1;
      display: flex;
      gap: 16rpx;

      .location-icon {
        font-size: 36rpx;
        margin-top: 4rpx;
      }

      .location-text {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 8rpx;

        .address-title {
          font-size: 32rpx;
          font-weight: bold;
          color: #333333;
        }

        .address-desc {
          font-size: 26rpx;
          color: #999999;
          line-height: 1.4;
        }
      }
    }

    .refresh-btn {
      width: 64rpx;
      height: 64rpx;
      background: #f5f5f5;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;

      .icon {
        font-size: 28rpx;
      }

      &:active {
        opacity: 0.8;
      }
    }
  }

  .detail-address {
    margin-bottom: 24rpx;

    .address-input {
      width: 100%;
      height: 72rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      padding: 0 24rpx;
      font-size: 28rpx;
      color: #333333;
    }
  }

  .section-title {
    font-size: 26rpx;
    color: #999999;
    margin-bottom: 16rpx;
  }

  .common-address,
  .nearby-places {
    margin-bottom: 24rpx;

    .address-list,
    .place-list {
      max-height: 200rpx;
    }

    .address-item,
    .place-item {
      display: flex;
      gap: 16rpx;
      padding: 16rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .address-icon,
      .place-icon {
        width: 48rpx;
        height: 48rpx;
        background: #f5f5f5;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;

        .icon {
          font-size: 28rpx;
        }
      }

      .address-info,
      .place-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 4rpx;

        .address-name,
        .place-name {
          font-size: 28rpx;
          color: #333333;
        }

        .address-detail,
        .place-distance {
          font-size: 24rpx;
          color: #999999;
        }
      }
    }
  }
}

.detail-toggle {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx;
  background: #ffffff;
  border-top: 1rpx solid #f0f0f0;

  .toggle-text {
    font-size: 26rpx;
    color: #666666;
  }

  .toggle-icon {
    font-size: 24rpx;
    color: #666666;
  }
}

.action-buttons {
  display: flex;
  gap: 24rpx;
  padding: 24rpx 32rpx;
  background: #ffffff;
  border-top: 1rpx solid #f0f0f0;

  .btn {
    flex: 1;
    height: 88rpx;
    border-radius: 44rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
    font-weight: 500;
    border: none;

    &.btn-primary {
      background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
      color: #ffffff;
    }

    &.btn-outline {
      background: #ffffff;
      color: #ff6b6b;
      border: 2rpx solid #ff6b6b;
    }

    &:active {
      opacity: 0.8;
    }
  }
}
</style>
