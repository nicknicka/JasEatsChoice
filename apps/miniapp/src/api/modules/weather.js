/**
 * 天气相关 API
 * 对接后端 WeatherController
 * 基础路径: /v1/weather
 */
import { get } from '@/utils/request'

export const weatherApi = {
  /**
   * 获取天气信息
   * GET /v1/weather?city={city}
   * @param {Object} params - 查询参数
   * @param {string} params.city - 城市名（后端为 @RequestParam 必填）
   * @returns {Promise} 返回天气信息
   */
  getWeatherInfo: (params) => get('/v1/weather', params)
}

export default weatherApi
