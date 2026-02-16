/**
 * 高德地图后端代理 API
 * 通过后端调用高德地图接口，避免安全密钥暴露给前端
 */
import api from '../utils/api'
import { AMAP_CONFIG } from '../config'

export default {
  /**
   * 地址搜索
   * @param {string} keywords - 搜索关键词
   * @param {string} city - 城市（可选，默认全国）
   * @returns {Promise} 搜索结果列表
   */
  searchAddress(keywords, city = null) {
    const params = { keywords }
    if (city) {
      params.city = city
    }
    return api.get('/v1/amap/search', { params })
  },

  /**
   * 地理编码（地址 → 坐标）
   * @param {string} address - 地址
   * @param {string} city - 城市（可选）
   * @returns {Promise} 坐标信息
   */
  geocode(address, city = null) {
    const params = { address }
    if (city) {
      params.city = city
    }
    return api.get('/v1/amap/geocode', { params })
  },

  /**
   * 逆地理编码（坐标 → 地址）
   * @param {string} lng - 经度
   * @param {string} lat - 纬度
   * @returns {Promise} 地址信息
   */
  regeocode(lng, lat) {
    return api.get('/v1/amap/regeocode', {
      params: { lng, lat }
    })
  },

  /**
   * IP 定位（通过 IP 地址获取大概位置）
   * @returns {Promise} 位置信息（城市级别精度）
   */
  ipLocation() {
    return api.get('/v1/amap/ip/location')
  },

  /**
   * 获取行政区域数据（用于级联选择器）
   * 方案1: 通过后端代理调用（推荐）
   * @param {string} keywords - 查询关键词（如：中国）
   * @param {number} subdistrict - 子级行政区域等级（1-3）
   * @returns {Promise} 级联选择器格式的省市区数据
   */
  getDistrictData(keywords = '中国', subdistrict = 3) {
    return api.get('/v1/amap/district', {
      params: { keywords, subdistrict }
    })
  },

  /**
   * 获取行政区域数据 - 前端直接调用高德API
   * 注意：需要在config/index.js中配置有效的AMAP_CONFIG.key
   * @param {string} keywords - 查询关键词（如：中国）
   * @param {number} subdistrict - 子级行政区域等级（1-3）
   * @param {string} key - 高德地图API Key（可选，如果不传则使用配置文件中的key）
   * @returns {Promise} 高德地图API返回的原始数据
   */
  async getDistrictDataDirect(keywords = '中国', subdistrict = 3, key = null) {
    const apiKey = key || AMAP_CONFIG.key

    if (apiKey === 'YOUR_AMAP_KEY') {
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

  /**
   * 将高德地图API返回的行政区数据转换为级联选择器格式
   * @param {Array} districts - 高德地图API返回的districts数组
   * @param {number} level - 当前层级（用于递归）
   * @returns {Array} 级联选择器格式的数据
   */
  convertToCascaderFormat(districts, level = 1) {
    if (!districts || !Array.isArray(districts)) {
      return []
    }

    return districts.map(district => {
      const item = {
        value: district.adcode,
        label: district.name,
        level: level
      }

      // 如果有下级行政区且需要继续展开（最多3级：省-市-区）
      if (district.districts && district.districts.length > 0 && level < 3) {
        item.children = this.convertToCascaderFormat(district.districts, level + 1)
      }

      return item
    })
  }
}
