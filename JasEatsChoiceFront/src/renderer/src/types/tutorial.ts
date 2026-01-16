/**
 * 教程相关类型定义
 */

export type TutorialType = 'video' | 'article'

export interface Tutorial {
  id?: string | number
  name: string
  thumbnail?: string
  type: TutorialType
  duration?: string
  rating?: number
}
