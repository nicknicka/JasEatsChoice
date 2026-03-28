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
      // U-033: 调用天气API
      try {
        const { weatherApi } = await import('@/api')
        const { latitude, longitude } = locationStore.currentLocation

        // 调用天气API获取当前天气
        const res = await weatherApi.getByLocation({
          latitude,
          longitude
        })

        if (res && res.data) {
          // 更新天气信息
          weatherInfo.value = {
            temperature: res.data.temperature || res.data.temp || '--',
            condition: res.data.condition || res.data.weather || '晴',
            icon: res.data.icon || '',
            humidity: res.data.humidity || 0,
            windSpeed: res.data.windSpeed || 0
          }
        }
      } catch (error) {
        console.error('获取天气信息失败，使用默认值:', error)
        // 如果API调用失败，使用默认天气信息
        weatherInfo.value = {
          temperature: '--',
          condition: '未知',
          icon: '',
          humidity: 0,
          windSpeed: 0
        }
      }
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
 * 选择城市 - U-034: 实现城市选择页面
 */
const chooseCity = () => {
  // U-034: 跳转到城市选择页面
  uni.navigateTo({
    url: '/pages-common/city-selector/index',
    success: () => {
      console.log('跳转到城市选择页面成功')
    },
    fail: () => {
      // 如果城市选择页面不存在，显示城市选择器
      showCityPicker()
    }
  })
}

/**
 * 显示城市选择器（备用方案）
 */
const showCityPicker = () => {
  const cities = [
    { name: '北京市', code: 'beijing' },
    { name: '上海市', code: 'shanghai' },
    { name: '广州市', code: 'guangzhou' },
    { name: '深圳市', code: 'shenzhen' },
    { name: '杭州市', code: 'hangzhou' },
    { name: '成都市', code: 'chengdu' }
  ]

  uni.showActionSheet({
    itemList: cities.map(c => c.name),
    success: (res) => {
      if (res.tapIndex >= 0) {
        const selectedCity = cities[res.tapIndex]
        // 更新位置信息
        locationStore.setCurrentCity(selectedCity)

        // 重新获取天气信息
        getLocationAndWeather()
      }
    }
  })
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
@import '../../styles/mixins.scss';

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
