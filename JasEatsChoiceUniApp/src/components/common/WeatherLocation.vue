<template>
  <view class="weather-location" @click="handleLocationClick">
    <!-- 定位图标 -->
    <view class="location-icon">📍</view>

    <!-- 位置信息 -->
    <view class="location-text">{{ locationText }}</view>

    <!-- 天气信息 -->
    <view class="weather-info" v-if="weather">
      <text class="weather-icon">{{ weather.icon }}</text>
      <text class="weather-temp">{{ weather.temperature }}°</text>
    </view>

    <!-- 加载中 -->
    <view class="loading" v-if="loading">
      <text>定位中...</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useLocationStore } from '@/store'

// Store
const locationStore = useLocationStore()

// 状态
const loading = ref(false)

// 计算属性
const locationText = computed(() => {
  if (locationStore.selectedCity) {
    return locationStore.selectedCity
  }
  if (locationStore.currentLocation?.district) {
    return locationStore.currentLocation.district
  }
  return '定位中...'
})

const weather = computed(() => {
  return locationStore.weather
})

/**
 * 获取当前位置和天气
 */
const getLocationAndWeather = async () => {
  loading.value = true

  try {
    // 获取当前位置
    await locationStore.getCurrentPosition()

    // 如果有位置信息，获取天气
    if (locationStore.currentLocation) {
      // TODO: 调用天气API
      // await locationStore.getWeather()
    }
  } catch (error) {
    console.error('获取位置信息失败:', error)
    uni.showToast({
      title: '获取位置失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 点击位置组件
 */
const handleLocationClick = () => {
  uni.showActionSheet({
    itemList: ['重新定位', '切换城市'],
    success: (res) => {
      if (res.tapIndex === 0) {
        // 重新定位
        getLocationAndWeather()
      } else if (res.tapIndex === 1) {
        // 切换城市
        chooseCity()
      }
    }
  })
}

/**
 * 选择城市
 */
const chooseCity = () => {
  uni.showToast({
    title: '城市选择功能开发中',
    icon: 'none'
  })

  // TODO: 实现城市选择页面
  // uni.navigateTo({
  //   url: '/pages-common/city-selector/index'
  // })
}

// 组件挂载时获取位置
onMounted(() => {
  if (!locationStore.currentLocation) {
    getLocationAndWeather()
  }
})

// 暴露方法给父组件
defineExpose({
  getLocationAndWeather
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.weather-location {
  @include flex-center;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-md;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: $border-radius-lg;
  margin-bottom: $spacing-md;
}

.location-icon {
  font-size: $font-size-lg;
}

.location-text {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  max-width: 200rpx;
  @include text-ellipsis;
}

.weather-info {
  @include flex-center;
  gap: $spacing-xs;
  margin-left: $spacing-xs;

  .weather-icon {
    font-size: $font-size-lg;
  }

  .weather-temp {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
  }
}

.loading {
  font-size: $font-size-sm;
  opacity: 0.8;
}
</style>
