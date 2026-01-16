/**
 * API 响应通用类型定义
 */

export interface ApiResponse<T = any> {
  data: T
  message?: string
  code?: number
}
