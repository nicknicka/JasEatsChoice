/**
 * 天气相关类型定义
 */

export interface Weather {
  temp: number
  condition: string
  city: string
  address: string
}

export interface WeatherResponse {
  temperature?: number
  condition?: string
}

export interface LocationResponse {
  city: string | string[]
  address: string | string[]
}
