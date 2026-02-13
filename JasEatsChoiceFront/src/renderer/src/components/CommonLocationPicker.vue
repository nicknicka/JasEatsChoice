<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../config/index.js'

// 对外暴露的属性和事件
const props = defineProps({
  // 是否自动获取定位
  autoLocate: {
    type: Boolean,
    default: false
  },
  // 是否显示定位误差提示
  showAccuracyAlert: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['location-changed', 'location-error'])

// 定位相关状态
const isLocating = ref(false)
const currentLocation = ref(null)
const locationError = ref(false)
const locationCity = ref('')

// 本地存储键名
const LOCATION_STORAGE_KEY = 'user_last_location'

/**
 * 多级定位策略（自动定位）
 * 优先级：本地缓存 > IP定位 > GPS定位 > 默认位置
 */
const getCurrentLocation = async () => {
  if (!('geolocation' in navigator)) {
    locationError.value = true
    ElMessage.error('您的浏览器不支持定位功能')
    return
  }

  isLocating.value = true

  // ========== 第一级：使用本地缓存的上次位置（最快） ==========
  const lastLocation = getLastLocation()
  if (lastLocation) {
    console.log('使用本地缓存位置:', lastLocation)
    currentLocation.value = { latitude: lastLocation.lat, longitude: lastLocation.lng }
    locationError.value = false

    // 获取城市信息
    await getCityByLocation(lastLocation.lng, lastLocation.lat)
    isLocating.value = false
    return
  }

  // ========== 第二级：IP 定位（快速稳定，无需授权） ==========
  try {
    console.log('尝试 IP 定位...')
    const response = await axios.get(`${API_CONFIG.baseURL}/v1/amap/ip/location`)

    if (response.data && response.data.code === '200' && response.data.data) {
      const { lng, lat, province, city } = response.data.data

      if (lng && lat) {
        console.log('IP 定位成功:', province, city, lng, lat)
        currentLocation.value = { latitude: lat, longitude: lng }
        locationError.value = false

        // 保存到本地缓存
        saveLastLocation(lng, lat)

        // 直接使用IP定位返回的城市信息，无需再调用逆地理编码
        locationCity.value = city || ''
        emit('location-changed', {
          latitude: lat,
          longitude: lng,
          city: city || '',
          location: currentLocation.value
        })

        isLocating.value = false
        ElMessage.success(`定位成功：${province || ''}${city || ''}`)
        return
      }
    }
  } catch (error) {
    console.log('IP 定位失败，尝试其他方式:', error.message)
  }

  // ========== 第三级：浏览器 GPS 定位（需要用户授权） ==========
  try {
    console.log('尝试 GPS 定位...')
    await new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          console.log('GPS 定位成功')
          const { latitude, longitude } = position.coords
          const accuracy = position.coords.accuracy

          currentLocation.value = { latitude, longitude }

          // 检查定位误差
          if (showAccuracyAlert.value && accuracy > 500) {
            locationError.value = true
            ElMessageBox.warning({
              title: '定位误差提示',
              message: `当前定位误差为 ${Math.round(accuracy)} 米，可能影响推荐准确性。是否重新定位？`,
              confirmButtonText: '重新定位',
              cancelButtonText: '使用此位置',
              callback: (action) => {
                if (action === 'confirm') {
                  getCurrentLocation()
                } else {
                  // 用户接受误差，继续处理
                  processLocationAfterGPS(latitude, longitude)
                }
              }
            })
          } else {
            locationError.value = false
            processLocationAfterGPS(latitude, longitude)
          }

          resolve()
        },
        (error) => {
          console.log('GPS 定位失败:', error.message)
          reject(error)
        },
        {
          enableHighAccuracy: false, // 桌面端关闭高精度定位
          timeout: 15000, // 15秒超时
          maximumAge: 300000 // 5分钟缓存
        }
      )
    })
    isLocating.value = false
    return
  } catch (error) {
    console.log('GPS 定位异常或被拒绝')
  }

  // ========== 第四级：使用默认位置（兜底） ==========
  console.log('使用默认位置（北京）')
  const defaultLocation = { lng: 116.397428, lat: 39.90923 }
  currentLocation.value = { latitude: defaultLocation.lat, longitude: defaultLocation.lng }
  locationError.value = true // 标记为定位失败状态

  await getCityByLocation(defaultLocation.lng, defaultLocation.lat)
  isLocating.value = false

  ElMessage.warning({
    message: '无法自动定位，已使用默认位置。推荐准确性可能受影响。',
    duration: 5000,
    showClose: true
  })
}

/**
 * GPS定位成功后的处理
 */
const processLocationAfterGPS = async (latitude, longitude) => {
  // 保存到本地缓存
  saveLastLocation(longitude, latitude)

  // 获取城市信息
  await getCityByLocation(longitude, latitude)
}

/**
 * 根据经纬度获取城市信息
 */
const getCityByLocation = async (longitude, latitude) => {
  try {
    const response = await axios.get(API_CONFIG.baseURL + API_CONFIG.location.location, {
      params: {
        latitude,
        longitude
      }
    })

    if (response.data) {
      const { city } = response.data
      locationCity.value = city

      // 对外发射定位结果
      emit('location-changed', {
        latitude,
        longitude,
        city,
        location: currentLocation.value
      })
    }
  } catch (error) {
    console.error('逆地理编码失败:', error)

    // 即使获取城市失败，也发射定位结果
    emit('location-changed', {
      latitude,
      longitude,
      city: '',
      location: currentLocation.value
    })
  }
}

/**
 * 手动选择城市
 */
const selectCity = async (city) => {
  if (!city) return

  locationCity.value = city

  // 对外发射定位结果（手动选择城市时，经纬度为空）
  emit('location-changed', {
    latitude: null,
    longitude: null,
    city,
    location: null
  })
}

/**
 * 保存位置到本地存储（7天有效期）
 */
const saveLastLocation = (lng, lat) => {
  try {
    const locationData = {
      lng,
      lat,
      timestamp: Date.now()
    }
    localStorage.setItem(LOCATION_STORAGE_KEY, JSON.stringify(locationData))
    console.log('位置已保存到本地存储')
  } catch (error) {
    console.warn('保存位置失败:', error)
  }
}

/**
 * 从本地存储获取上次位置
 */
const getLastLocation = () => {
  try {
    const stored = localStorage.getItem(LOCATION_STORAGE_KEY)
    if (stored) {
      const locationData = JSON.parse(stored)

      // 检查是否过期（7天内有效）
      const sevenDays = 7 * 24 * 60 * 60 * 1000
      if (Date.now() - locationData.timestamp < sevenDays) {
        return {
          lng: locationData.lng,
          lat: locationData.lat
        }
      } else {
        // 过期则删除
        localStorage.removeItem(LOCATION_STORAGE_KEY)
      }
    }
  } catch (error) {
    console.warn('读取本地位置失败:', error)
  }
  return null
}

// 组件挂载时自动定位
onMounted(() => {
  if (props.autoLocate) {
    getCurrentLocation()
  }
})

// 暴露方法给父组件
defineExpose({
  getCurrentLocation,
  selectCity,
  currentLocation,
  locationCity,
  locationError,
  getLastLocation,
  saveLastLocation
})
</script>

<template>
  <!-- 该组件主要提供定位功能，不包含UI展示 -->
</template>
