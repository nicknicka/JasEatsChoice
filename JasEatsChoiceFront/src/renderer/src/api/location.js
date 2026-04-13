/**
 * 定位后端 API
 * 统一走 /v1/location。
 */
import api from '../utils/api'
import { AMAP_CONFIG } from '../config'

export default {
  searchAddress(keywords, city = null) {
    const params = { keywords }
    if (city) {
      params.city = city
    }
    return api.get('/v1/location/search', { params })
  },

  geocode(address, city = null) {
    const params = { address }
    if (city) {
      params.city = city
    }
    return api.get('/v1/location/geocode', { params })
  },

  regeocode(lng, lat) {
    return api.get('/v1/location/reverse-geocode', {
      params: { lng, lat }
    })
  },

  ipLocation() {
    return api.get('/v1/location')
  },

  getDistrictData(keywords = '中国', subdistrict = 3) {
    return api.get('/v1/location/cascader', {
      params: { keywords, subdistrict }
    })
  },

  async getDistrictDataDirect(keywords = '中国', subdistrict = 3, key = null) {
    const apiKey = key || AMAP_CONFIG.key

    if (!apiKey || apiKey === 'YOUR_AMAP_KEY') {
      throw new Error('请先在config/index.js中配置有效的高德地图API Key')
    }

    const url = `${AMAP_CONFIG.baseURL}${AMAP_CONFIG.district}`
    const params = new URLSearchParams({
      key: apiKey,
      keywords: keywords,
      subdistrict: subdistrict,
      extensions: 'base',
      output: 'JSON'
    })

    try {
      const response = await fetch(`${url}?${params}`)
      const data = await response.json()
      return data
    } catch (error) {
      console.error('调用高德地图API失败:', error)
      throw error
    }
  },

  convertToCascaderFormat(districts, level = 1) {
    if (!districts || !Array.isArray(districts)) {
      return []
    }

    return districts.map((district) => {
      const item = {
        value: district.adcode,
        label: district.name,
        level: level
      }

      if (district.districts && district.districts.length > 0 && level < 3) {
        item.children = this.convertToCascaderFormat(district.districts, level + 1)
      }

      return item
    })
  }
}