<template>
  <view class="weather-location" @click="handleLocationClick">
    <!-- 左侧：定位图标和位置 -->
    <view class="location-section">
      <view class="location-icon">📍</view>
      <view class="location-info">
        <view class="location-text">{{ locationText }}</view>
        <view class="location-hint" v-if="!loading">点击切换城市</view>
      </view>
    </view>

    <!-- 右侧：天气信息 -->
    <view class="weather-section" v-if="weather && !loading">
      <view class="weather-icon">{{ weather.icon || '☀️' }}</view>
      <view class="weather-detail">
        <text class="weather-temp">{{ weather.temperature }}°</text>
        <text class="weather-condition">{{ weather.condition }}</text>
      </view>
    </view>

    <!-- 加载状态 -->
    <view class="loading-state" v-if="loading">
      <text class="loading-text">定位中...</text>
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

// 计算属性 - 直接使用 store 的 getter
const locationText = computed(() => locationStore.locationText)

const weather = computed(() => locationStore.weather)

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
      // U-033: 调用天气API（暂时使用模拟数据）
      try {
        // TODO: 集成真实天气API（如和风天气、高德天气等）
        // const { weatherApi } = await import('@/api')
        // const { latitude, longitude } = locationStore.currentLocation
        // const res = await weatherApi.getByLocation({ latitude, longitude })

        // 暂时使用模拟数据
        const mockWeatherData = {
          temperature: 26,
          condition: '晴',
          icon: '☀️',
          humidity: 65,
          windSpeed: 3
        }

        // 更新天气信息到store
        locationStore.setWeather(mockWeatherData)

        console.log('天气信息已更新:', mockWeatherData)
      } catch (error) {
        console.error('获取天气信息失败，使用默认值:', error)

        // 如果API调用失败，使用默认天气信息
        locationStore.setWeather({
          temperature: '--',
          condition: '未知',
          icon: '❓',
          humidity: 0,
          windSpeed: 0
        })
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
        // 更新位置信息 - 使用 setSelectedCity 方法
        locationStore.setSelectedCity({
          name: selectedCity.name,
          adcode: selectedCity.code
        })

        uni.showToast({
          title: `已切换到${selectedCity.name}`,
          icon: 'success'
        })

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
@import '@/styles/mixins.scss';

.weather-location {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-sm $spacing-md;
  background-color: $bg-color-white;
  border-radius: $border-radius-base;
  box-shadow: $box-shadow-light;
  margin-bottom: $spacing-md;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.98);
    background-color: $bg-color-base;
  }
}

// 左侧定位区域
.location-section {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  flex: 1;
  min-width: 0; // 允许文本溢出
}

.location-icon {
  font-size: 32rpx;
  flex-shrink: 0;
}

.location-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  min-width: 0;
}

.location-text {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
  @include text-ellipsis;
}

.location-hint {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

// 右侧天气区域
.weather-section {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  flex-shrink: 0;
}

.weather-icon {
  font-size: 40rpx;
  line-height: 1;
}

.weather-detail {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2rpx;
}

.weather-temp {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $danger-color;
  line-height: 1;
}

.weather-condition {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

// 加载状态
.loading-state {
  flex-shrink: 0;
}

.loading-text {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}
</style>
