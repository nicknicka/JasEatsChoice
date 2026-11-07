<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { loadAMapSDK, resolveAmapLocation } from '../composables/useAmapLocation'

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
const locationSource = ref('unknown')

// 本地存储键名
const LOCATION_STORAGE_KEY = 'user_last_location'
const LOCATION_CACHE_DURATION = 24 * 60 * 60 * 1000
const TRUSTED_LOCATION_SOURCES = ['gps', 'manual', 'search']

const getClientIpForLocation = () => {
  try {
    const ip = localStorage.getItem('client_ip') || localStorage.getItem('public_ip')
    return ip && ip.trim() ? ip.trim() : null
  } catch (error) {
    console.warn('读取本地IP失败:', error)
    return null
  }
}

/**
 * 多级定位策略（自动定位）
 * 优先级：本地缓存（24小时）> GPS定位 > IP定位 > 默认位置
 */
const getCurrentLocation = async () => {
  isLocating.value = true

  try {
    await loadAMapSDK()
  } catch (error) {
    console.warn('地图 SDK 加载失败，继续使用后端定位与降级链路:', error.message)
  }

  try {
    const result = await resolveAmapLocation({
      getLastLocation,
      saveLastLocation,
      clientIp: null,
      preferCacheFirst: true,
      cacheSources: TRUSTED_LOCATION_SOURCES,
      useHighAccuracy: false,
      AMap: typeof AMap !== 'undefined' ? AMap : null
    })

    if (result?.lng && result?.lat) {
      currentLocation.value = { latitude: result.lat, longitude: result.lng }
      locationCity.value = result.city || ''
      locationSource.value = result.source || 'unknown'
      locationError.value = result.source === 'default'

      emit('location-changed', {
        latitude: result.lat,
        longitude: result.lng,
        city: result.city || '',
        address: result.address || '',
        location: currentLocation.value,
        source: locationSource.value,
        accuracy: result.accuracy || 'unknown'
      })

      if (result.source === 'default') {
        ElMessage.warning({
          message: '无法自动定位，已使用默认位置。推荐准确性可能受影响。',
          duration: 5000,
          showClose: true
        })
      } else {
        ElMessage.success(`定位成功：${result.city || ''}`)
      }
      return
    }

    locationError.value = true
    emit('location-error', new Error('未获取到有效定位结果'))
    ElMessage.warning({
      message: '无法自动定位，已使用默认位置。推荐准确性可能受影响。',
      duration: 5000,
      showClose: true
    })
  } catch (error) {
    console.error('定位失败:', error)
    locationError.value = true
    emit('location-error', error)
    emit('location-changed', {
      latitude: null,
      longitude: null,
      city: '',
      address: '',
      location: null,
      source: 'error'
    })

    ElMessage.error('定位失败，请稍后重试')
  } finally {
    isLocating.value = false
  }
}

/**
 * 手动选择城市
 */
const selectCity = async (city) => {
  if (!city) return

  locationCity.value = city
  locationSource.value = 'manual'

  // 对外发射定位结果（手动选择城市时，经纬度为空）
  emit('location-changed', {
    latitude: null,
    longitude: null,
    city,
    address: city,
    location: null,
    source: locationSource.value
  })
}

/**
 * 保存位置到本地存储（24小时有效期）
 */
const saveLastLocation = (lng, lat, source = 'unknown') => {
  if (!TRUSTED_LOCATION_SOURCES.includes(source)) {
    return
  }

  try {
    const locationData = {
      lng,
      lat,
      timestamp: Date.now(),
      source,
      accuracy: source
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

      // 检查是否过期（24小时内有效）
      if (Date.now() - locationData.timestamp < LOCATION_CACHE_DURATION) {
        return {
          lng: locationData.lng,
          lat: locationData.lat,
          source: locationData.source || 'cache',
          timestamp: locationData.timestamp,
          accuracy: locationData.accuracy || locationData.source || 'cache',
          address: locationData.address || ''
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
  locationSource,
  getLastLocation,
  saveLastLocation
})
</script>
