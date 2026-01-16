/**
 * 天气相关逻辑组合式函数
 */
import { ref } from 'vue'
import api from '../utils/api'
import { API_CONFIG } from '../config'
import { Weather, WeatherResponse, LocationResponse } from '../types'
import { HOME_CONSTANTS } from '../constants/home'

export function useWeather() {
  const weather = ref<Weather>({
    temp: 32,
    condition: '晴天',
    city: '',
    address: ''
  })

  const loading = ref(false)

  /**
   * 获取天气和位置数据
   */
  const fetchWeather = async (selectedCity: string | null = null) => {
    try {
      loading.value = true

      if (selectedCity) {
        // 为选择的城市获取天气信息
        weather.value.city = selectedCity
        const weatherResponse = await api.get(
          `${API_CONFIG.weather.current}?city=${encodeURIComponent(selectedCity)}`
        )

        if (weatherResponse?.data) {
          const { temperature, condition } = weatherResponse.data
          if (temperature !== undefined) {
            weather.value.temp = temperature
          }
          if (condition !== undefined) {
            weather.value.condition = condition
          }
        }
      } else {
        // 从后端获取当前位置
        const locationResponse = await api.get(API_CONFIG.location.location)
        if (locationResponse.data) {
          let { city, address } = locationResponse.data

          // 处理异常数据格式
          if (Array.isArray(city)) {
            city = city.join('')
          }
          if (Array.isArray(address) || address === '[][]') {
            address = '未获取到详细地址'
          }

          weather.value.city = city
          weather.value.address = address

          // 根据城市获取天气信息
          const weatherResponse = await api.get(
            `${API_CONFIG.weather.current}?city=${encodeURIComponent(city)}`
          )

          if (weatherResponse?.data) {
            const { temperature, condition } = weatherResponse.data
            if (temperature !== undefined) {
              weather.value.temp = temperature
            }
            if (condition !== undefined) {
              weather.value.condition = condition
            }
          }
        }
      }
    } catch (error) {
      console.error(selectedCity ? '加载天气失败:' : '加载天气或位置失败:', error)
    } finally {
      loading.value = false
    }

    console.log('获取天气数据:', weather.value)
    return weather.value
  }

  return {
    weather,
    loading,
    fetchWeather
  }
}
