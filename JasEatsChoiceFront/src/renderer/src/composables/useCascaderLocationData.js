import { ref } from 'vue'
import api from '../utils/api.js'
import { API_CONFIG } from '../config/index.js'

const DEFAULT_CACHE_KEY = 'jaseatschoice_location_cascader_data'

const getFallbackAddressData = () => [
  {
    value: '北京市',
    label: '北京市',
    children: [
      { value: '朝阳区', label: '朝阳区' },
      { value: '海淀区', label: '海淀区' },
      { value: '东城区', label: '东城区' },
      { value: '西城区', label: '西城区' }
    ]
  },
  {
    value: '上海市',
    label: '上海市',
    children: [
      { value: '黄浦区', label: '黄浦区' },
      { value: '徐汇区', label: '徐汇区' },
      { value: '长宁区', label: '长宁区' },
      { value: '浦东新区', label: '浦东新区' }
    ]
  },
  {
    value: '广东省',
    label: '广东省',
    children: [
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '天河区', label: '天河区' },
          { value: '越秀区', label: '越秀区' }
        ]
      },
      {
        value: '深圳市',
        label: '深圳市',
        children: [
          { value: '福田区', label: '福田区' },
          { value: '南山区', label: '南山区' }
        ]
      }
    ]
  }
]

const readCachedData = (cacheKey) => {
  try {
    const cachedData = localStorage.getItem(cacheKey)
    return cachedData ? JSON.parse(cachedData) : []
  } catch (error) {
    console.warn('读取缓存地址数据失败:', error)
    return []
  }
}

const saveCachedData = (cacheKey, data) => {
  try {
    localStorage.setItem(cacheKey, JSON.stringify(data))
  } catch (error) {
    console.warn('保存缓存地址数据失败:', error)
  }
}

const normalizeLocationData = (response) => {
  const payload = response?.data ?? response

  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.data)) return payload.data
  if (Array.isArray(payload?.rows)) return payload.rows
  if (Array.isArray(payload?.list)) return payload.list

  return []
}

export function useCascaderLocationData(options = {}) {
  const cascaderData = ref([])
  const loading = ref(false)
  const cacheKey = options.cacheKey || DEFAULT_CACHE_KEY
  const fallbackData =
    typeof options.fallbackData === 'function' ? options.fallbackData : getFallbackAddressData

  const loadLocationData = async () => {
    loading.value = true

    try {
      const response = await api.get(API_CONFIG.location.cascaderData)
      const locationData = normalizeLocationData(response)

      if (locationData.length > 0) {
        cascaderData.value = locationData
        saveCachedData(cacheKey, locationData)
        return { source: 'backend', data: locationData }
      }

      throw new Error('后端未返回有效的地区数据')
    } catch (error) {
      console.error('加载地区级联数据失败:', error)

      const cachedData = readCachedData(cacheKey)
      if (cachedData.length > 0) {
        cascaderData.value = cachedData
        return { source: 'cache', data: cachedData }
      }

      const fallback = fallbackData()
      cascaderData.value = fallback
      return { source: 'fallback', data: fallback }
    } finally {
      loading.value = false
    }
  }

  return {
    cascaderData,
    loading,
    loadLocationData
  }
}

export { getFallbackAddressData }