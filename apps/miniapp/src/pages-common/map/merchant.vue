<template>
  <view class="merchant-map">
    <!-- 地图容器 -->
    <view class="map-container">
      <map
        class="map"
        :longitude="longitude"
        :latitude="latitude"
        :scale="scale"
        :show-location="true"
        :markers="merchantMarkers"
        @tap="onMapTap"
        @markertap="onMarkerTap"
        @regionchange="onRegionChange"
      ></map>

      <!-- 搜索框 -->
      <view class="search-box">
        <view class="search-input">
          <text class="search-icon">搜</text>
          <input class="input" v-model="searchKeyword" placeholder="搜索附近商家" placeholder-style="color: #999999" @confirm="onSearch" />
          <button class="clear-icon" v-if="searchKeyword" @click="clearSearch" aria-label="清空搜索">清空</button>
        </view>
      </view>

      <!-- 地图控制按钮 -->
      <view class="map-controls">
        <button class="control-btn" @click="zoomIn" aria-label="放大地图"><text class="icon">+</text></button>
        <button class="control-btn" @click="zoomOut" aria-label="缩小地图"><text class="icon">-</text></button>
        <button class="control-btn" @click="moveToCurrentLocation" aria-label="回到当前位置"><text class="icon">定位</text></button>
      </view>

      <!-- 商家类型筛选 -->
      <view class="category-filter">
        <scroll-view class="filter-scroll" scroll-x>
          <button class="filter-item" v-for="(category, index) in categories" :key="index" :class="{ active: selectedCategory === index }" @click="selectCategory(index)" :aria-label="`筛选${category.name}`">
            <text class="filter-icon">{{ category.icon }}</text>
            <text class="filter-name">{{ category.name }}</text>
          </button>
        </scroll-view>
      </view>
    </view>

    <!-- 商家列表 -->
    <view class="merchant-list">
      <view class="list-header">
        <text class="list-title">附近商家</text>
        <text class="list-count">共{{ merchantList.length }}家</text>
      </view>
      <scroll-view class="list-scroll" scroll-y>
        <button class="merchant-item" v-for="(merchant, index) in merchantList" :key="index" @click="showMerchantDetail(merchant)" :aria-label="`查看${merchant.name}详情`">
          <view class="merchant-left">
            <AppImage class="merchant-image" :src="merchant.image" mode="aspectFill" :aria-label="merchant.name"></AppImage>
            <view class="merchant-badge" v-if="merchant.badge">
              <text class="badge-text">{{ merchant.badge }}</text>
            </view>
          </view>
          <view class="merchant-right">
            <view class="merchant-header">
              <text class="merchant-name">{{ merchant.name }}</text>
              <view class="merchant-rating">
                <text class="star">⭐</text>
                <text class="rating">{{ merchant.rating }}</text>
              </view>
            </view>
            <view class="merchant-info">
              <text class="info-text">{{ merchant.category }}</text>
              <text class="info-divider">•</text>
              <text class="info-text">{{ merchant.distance }}</text>
            </view>
            <view class="merchant-address">
              <text class="address-text">{{ merchant.address }}</text>
            </view>
            <view class="merchant-footer">
              <view class="delivery-info">
                <text class="delivery-icon">🚀</text>
                <text class="delivery-text">约{{ merchant.deliveryTime }}分钟</text>
              </view>
              <view class="min-order">
                <text class="min-text">起送¥{{ merchant.minOrder }}</text>
              </view>
            </view>
          </view>
        </button>
      </scroll-view>
    </view>

    <!-- 商家详情弹窗 -->
    <uni-popup ref="merchantPopup" type="bottom">
      <view class="merchant-popup">
        <view class="popup-header">
          <view class="header-left">
            <AppImage class="merchant-avatar" :src="selectedMerchant.image" mode="aspectFill" :aria-label="selectedMerchant.name"></AppImage>
            <view class="merchant-basic">
              <text class="merchant-name">{{ selectedMerchant.name }}</text>
              <text class="merchant-category">{{ selectedMerchant.category }}</text>
            </view>
          </view>
          <button class="close-btn" @click="closeMerchantPopup" aria-label="关闭商家详情">关闭</button>
        </view>
        <view class="popup-info">
          <view class="info-row">
            <text class="info-label">评分</text>
            <text class="info-value">⭐ {{ selectedMerchant.rating }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">距离</text>
            <text class="info-value">{{ selectedMerchant.distance }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">配送</text>
            <text class="info-value">约{{ selectedMerchant.deliveryTime }}分钟</text>
          </view>
        </view>
        <view class="popup-address">
          <text class="address-icon">位置</text>
          <text class="address-text">{{ selectedMerchant.address }}</text>
        </view>
        <view class="popup-actions">
          <button class="btn btn-outline" @click="navigateToMerchant">
            <text class="icon">导航</text>
            <text>导航</text>
          </button>
          <button class="btn btn-primary" @click="orderFromMerchant">
            <text class="icon">下单</text>
            <text>去下单</text>
          </button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const isDev = process.env.NODE_ENV !== 'production'

// 地图中心点坐标
const longitude = ref(113.264385)
const latitude = ref(23.129112)
const scale = ref(15)

// 搜索关键词
const searchKeyword = ref('')

// 商家类型
const categories = ref([
  { name: '全部', icon: '🍽️' },
  { name: '快餐', icon: '🍔' },
  { name: '中餐', icon: '🥢' },
  { name: '西餐', icon: '🍝' },
  { name: '日料', icon: '🍣' },
  { name: '饮品', icon: '🧋' },
  { name: '甜点', icon: '🍰' }
])
const selectedCategory = ref(0)

// 商家列表
const merchantList = ref([
  {
    id: 1,
    name: '美味餐厅',
    category: '中餐',
    image: 'https://via.placeholder.com/200x200',
    rating: 4.8,
    distance: '500m',
    address: '天河区珠江新城花城大道123号',
    deliveryTime: 30,
    minOrder: 20,
    longitude: 113.264385,
    latitude: 23.129112,
    badge: '品牌'
  },
  {
    id: 2,
    name: '快乐汉堡',
    category: '快餐',
    image: 'https://via.placeholder.com/200x200',
    rating: 4.5,
    distance: '800m',
    address: '天河区天河路208号',
    deliveryTime: 25,
    minOrder: 15,
    longitude: 113.270000,
    latitude: 23.125000,
    badge: ''
  },
  {
    id: 3,
    name: '日式料理',
    category: '日料',
    image: 'https://via.placeholder.com/200x200',
    rating: 4.9,
    distance: '1.2km',
    address: '天河区体育西路88号',
    deliveryTime: 35,
    minOrder: 30,
    longitude: 113.260000,
    latitude: 23.135000,
    badge: '新店'
  }
])

// 选中的商家
const selectedMerchant = ref({})

// 商家标记点
const merchantMarkers = computed(() => {
  return merchantList.value.map((merchant, index) => ({
    id: merchant.id,
    longitude: merchant.longitude,
    latitude: merchant.latitude,
    title: merchant.name,
    iconPath: '/static/images/merchant-marker.png',
    width: 32,
    height: 32,
    anchor: {
      x: 0.5,
      y: 1
    },
    callout: {
      content: merchant.name,
      color: '#333333',
      fontSize: 12,
      borderRadius: 8,
      bgColor: '#ffffff',
      padding: 8,
      display: 'ALWAYS'
    }
  }))
})

const merchantPopup = ref(null)

onLoad(() => {
  getCurrentLocation()
})

// 获取当前位置
const getCurrentLocation = () => {
  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      longitude.value = res.longitude
      latitude.value = res.latitude
    }
  })
}

// 地图点击
const onMapTap = (e) => {
  if (isDev) {
    console.log('Map tapped:', e.detail)
  }
}

// 标记点点击
const onMarkerTap = (e) => {
  const merchantId = e.detail.markerId
  const merchant = merchantList.value.find(m => m.id === merchantId)
  if (merchant) {
    showMerchantDetail(merchant)
  }
}

// 地图区域变化
const onRegionChange = (e) => {
  if (e.type === 'end') {
    // 可以根据新的地图区域加载附近商家
    loadNearbyMerchants()
  }
}

// 放大
const zoomIn = () => {
  if (scale.value < 18) {
    scale.value++
  }
}

// 缩小
const zoomOut = () => {
  if (scale.value > 5) {
    scale.value--
  }
}

// 移动到当前位置
const moveToCurrentLocation = () => {
  getCurrentLocation()
}

// 搜索
const onSearch = () => {
  if (!searchKeyword.value.trim()) {
    return
  }
  // 调用搜索API
  uni.showToast({
    title: '搜索中...',
    icon: 'loading'
  })
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
}

// 选择分类
const selectCategory = (index) => {
  selectedCategory.value = index
  // 根据分类筛选商家
}

// 加载附近商家
const loadNearbyMerchants = () => {
  // 调用API获取附近商家
}

// 显示商家详情
const showMerchantDetail = (merchant) => {
  selectedMerchant.value = merchant
  merchantPopup.value?.open()
}

// 关闭商家详情弹窗
const closeMerchantPopup = () => {
  merchantPopup.value?.close()
}

// 导航到商家
const navigateToMerchant = () => {
  uni.openLocation({
    latitude: selectedMerchant.value.latitude,
    longitude: selectedMerchant.value.longitude,
    name: selectedMerchant.value.name,
    address: selectedMerchant.value.address
  })
}

// 从商家下单
const orderFromMerchant = () => {
  merchantPopup.value?.close()
  uni.navigateTo({
    url: `/pages/merchant/index?id=${selectedMerchant.value.id}`
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.merchant-map {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.map-container {
  position: relative;
  width: 100%;
  height: 55vh;

  .map {
    width: 100%;
    height: 100%;
  }

  .search-box {
    position: absolute;
    top: 24rpx;
    left: 32rpx;
    right: 32rpx;
    z-index: 10;

    .search-input {
      display: flex;
      align-items: center;
      gap: 16rpx;
      min-height: $touch-min-size;
      background: $bg-color-white;
      border-radius: $border-radius-round;
      padding: 0 24rpx;
      box-shadow: $box-shadow-md;

      .search-icon {
        font-size: 32rpx;
      }

      .input {
        flex: 1;
        height: 100%;
        font-size: 28rpx;
        color: $text-color-primary;
      }

      .clear-icon {
        min-width: $touch-min-size;
        min-height: $touch-min-size;
        background: $primary-100;
        border-radius: $border-radius-round;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24rpx;
        color: $primary-700;
        border: none;
      }
    }
  }

  .map-controls {
    position: absolute;
    right: 32rpx;
    bottom: 32rpx;
    display: flex;
    flex-direction: column;
    gap: 16rpx;

    .control-btn {
      width: $touch-min-size;
      height: $touch-min-size;
      background: $bg-color-white;
      border-radius: 50%;
      box-shadow: $box-shadow-md;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28rpx;
      color: $text-color-primary;
      border: none;

      &:active {
        opacity: 0.8;
      }
    }
  }

  .category-filter {
    position: absolute;
    bottom: 32rpx;
    left: 32rpx;
    right: 140rpx;
    z-index: 10;

    .filter-scroll {
      white-space: nowrap;

      .filter-item {
        display: inline-flex;
        flex-direction: column;
        align-items: center;
        gap: 8rpx;
        min-width: 128rpx;
        min-height: $touch-min-size;
        padding: 12rpx 16rpx;
        margin-right: 16rpx;
        background: $bg-color-white;
        border-radius: 12rpx;
        border: 1rpx solid $border-color-light;

        &.active {
          background: $primary-500;

          .filter-icon,
          .filter-name {
            color: #ffffff;
          }
        }

        .filter-icon {
          font-size: 32rpx;
        }

        .filter-name {
          font-size: 22rpx;
          color: $text-color-primary;
        }
      }
    }
  }
}

.merchant-list {
  flex: 1;
  background: $bg-color-base;
  display: flex;
  flex-direction: column;

  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 32rpx 16rpx;
    background: $bg-color-white;

    .list-title {
      font-size: 28rpx;
      font-weight: bold;
      color: $text-color-primary;
    }

    .list-count {
      font-size: 24rpx;
      color: $text-color-secondary;
    }
  }

  .list-scroll {
    flex: 1;
    padding: 16rpx 32rpx;

    .merchant-item {
      display: flex;
      gap: 24rpx;
      background: $bg-color-white;
      border-radius: 16rpx;
      padding: 24rpx;
      margin-bottom: 16rpx;
      border: none;
      text-align: left;

      .merchant-left {
        position: relative;
        width: 160rpx;
        height: 160rpx;

        .merchant-image {
          width: 100%;
          height: 100%;
          border-radius: 12rpx;
        }

        .merchant-badge {
          position: absolute;
          top: 0;
          left: 0;
          background: linear-gradient(135deg, $primary-500 0%, $primary-700 100%);
          border-radius: 12rpx 0 12rpx 0;
          padding: 4rpx 12rpx;

          .badge-text {
            font-size: 20rpx;
            color: #ffffff;
          }
        }
      }

      .merchant-right {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;

        .merchant-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;

          .merchant-name {
            font-size: 30rpx;
            font-weight: bold;
            color: #333333;
          }

          .merchant-rating {
            display: flex;
            align-items: center;
            gap: 4rpx;

            .star {
              font-size: 24rpx;
            }

            .rating {
              font-size: 26rpx;
              color: $warning-color;
              font-weight: 500;
            }
          }
        }

        .merchant-info {
          display: flex;
          align-items: center;
          gap: 8rpx;
          margin-top: 8rpx;

          .info-text {
            font-size: 24rpx;
            color: #666666;
          }

          .info-divider {
            font-size: 20rpx;
            color: #dddddd;
          }
        }

        .merchant-address {
          margin-top: 8rpx;

          .address-text {
            font-size: 24rpx;
            color: #999999;
            line-height: 1.4;
          }
        }

        .merchant-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-top: 16rpx;

          .delivery-info {
            display: flex;
            align-items: center;
            gap: 8rpx;

            .delivery-icon {
              font-size: 24rpx;
            }

            .delivery-text {
              font-size: 24rpx;
              color: $primary-600;
            }
          }

          .min-order {
            .min-text {
              font-size: 24rpx;
              color: #999999;
            }
          }
        }
      }
    }
  }
}

.merchant-popup {
  background: $bg-color-white;
  border-radius: 24rpx 24rpx 0 0;
  padding: 32rpx;
  padding-bottom: calc(32rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32rpx;

    .header-left {
      display: flex;
      gap: 16rpx;

      .merchant-avatar {
        width: 96rpx;
        height: 96rpx;
        border-radius: 12rpx;
      }

      .merchant-basic {
        display: flex;
        flex-direction: column;
        gap: 8rpx;

        .merchant-name {
          font-size: 32rpx;
          font-weight: bold;
          color: #333333;
        }

        .merchant-category {
          font-size: 24rpx;
          color: #999999;
        }
      }
    }

    .close-btn {
      min-width: $touch-min-size;
      min-height: $touch-min-size;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24rpx;
      color: $text-color-secondary;
      border: none;
      background: transparent;
    }
  }

  .popup-info {
    display: flex;
    justify-content: space-around;
    margin-bottom: 24rpx;
    padding: 24rpx 0;
    background: #f9f9f9;
    border-radius: 12rpx;

    .info-row {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8rpx;

      .info-label {
        font-size: 24rpx;
        color: #999999;
      }

      .info-value {
        font-size: 28rpx;
        font-weight: 500;
        color: #333333;
      }
    }
  }

  .popup-address {
    display: flex;
    gap: 12rpx;
    margin-bottom: 32rpx;
    padding: 20rpx;
    background: #f5f5f5;
    border-radius: 12rpx;

    .address-icon {
      font-size: 32rpx;
      margin-top: 4rpx;
    }

    .address-text {
      flex: 1;
      font-size: 26rpx;
      color: #666666;
      line-height: 1.5;
    }
  }

  .popup-actions {
    display: flex;
    gap: 24rpx;

    .btn {
      flex: 1;
      height: 88rpx;
      border-radius: 44rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12rpx;
      font-size: 28rpx;
      font-weight: 500;
      border: none;

      .icon {
        font-size: 32rpx;
      }

      &.btn-primary {
        background: linear-gradient(135deg, $primary-500 0%, $primary-700 100%);
        color: #ffffff;
      }

      &.btn-outline {
        background: $bg-color-white;
        color: $primary-600;
        border: 2rpx solid $primary-500;
      }

      &:active {
        opacity: 0.8;
      }
    }
  }
}

button::after {
  border: none;
}
</style>
