/**
 * 教程相关类型定义
 */

/**
 * 教程类型
 */
export type TutorialType = 'video' | 'article'

/**
 * 来源类型
 */
export type SourceType = 'ADMIN' | 'MERCHANT' | 'USER' | 'AI_GENERATED'

/**
 * 作者类型
 */
export type AuthorType = 'ADMIN' | 'MERCHANT' | 'USER' | 'AI'

/**
 * 状态类型
 */
export type Status = 'DRAFT' | 'PENDING' | 'PUBLISHED' | 'REJECTED'

/**
 * 审核状态
 */
export type ReviewStatus = 'NOT_SUBMITTED' | 'PENDING' | 'APPROVED' | 'REJECTED'

/**
 * 难度类型
 */
export type Difficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'

/**
 * 标签数组
 */
export type TutorialTags = string[]

/**
 * 教程接口
 */
export interface Tutorial {
  id?: string | number
  title: string
  type: TutorialType
  duration?: string
  views?: string // 旧字段，保留兼容

  // 来源信息
  sourceType: SourceType
  sourceId?: number
  authorType: AuthorType
  authorId?: number
  author?: string

  // 状态管理
  status: Status
  reviewStatus?: ReviewStatus
  reviewerId?: number
  reviewTime?: string
  reviewComment?: string
  featured: boolean
  isOfficial: boolean

  // 关联信息
  linkedMerchantId?: number
  linkedDishId?: number
  aiModelVersion?: string

  // 内容
  content?: string
  thumbnail?: string
  coverImage?: string
  videoUrl?: string
  tags?: TutorialTags

  // 扩展信息
  difficulty?: Difficulty
  calories?: number
  prepTime?: string
  servings?: number

  // 统计数据
  rating?: number
  ratingCount?: number
  favoriteCount?: number
  viewCount?: number
  shareCount?: number

  // 时间戳
  createTime?: string
  updateTime?: string

  // 关联数据（用于显示）
  merchantName?: string
  merchantLogo?: string
  dishName?: string
  dishImage?: string
  reviewerName?: string
}

/**
 * 教程列表响应
 */
export interface TutorialListResponse {
  data: Tutorial[]
  message?: string
}

/**
 * 教程详情响应
 */
export interface TutorialDetailResponse {
  data: Tutorial
  message?: string
}

/**
 * 教程分页参数
 */
export interface TutorialPageParams {
  page?: number
  size?: number
  sourceType?: SourceType
  status?: Status
}

/**
 * 教程分页响应
 */
export interface TutorialPageResponse {
  records: Tutorial[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 审核请求
 */
export interface ReviewRequest {
  comment: string
  setFeatured?: boolean
}

/**
 * API响应包装
 */
export interface ApiResponse<T = any> {
  success: boolean
  message: string
  data?: T
}

/**
 * 来源类型显示名称映射
 */
export const SOURCE_TYPE_MAP: Record<SourceType, string> = {
  ADMIN: '管理员',
  MERCHANT: '商家',
  USER: '用户',
  AI_GENERATED: 'AI生成'
}

/**
 * 作者类型显示名称映射
 */
export const AUTHOR_TYPE_MAP: Record<AuthorType, string> = {
  ADMIN: '管理员',
  MERCHANT: '商家',
  USER: '用户',
  AI: 'AI'
}

/**
 * 状态显示名称映射
 */
export const STATUS_MAP: Record<Status, string> = {
  DRAFT: '草稿',
  PENDING: '待审核',
  PUBLISHED: '已发布',
  REJECTED: '已拒绝'
}

/**
 * 审核状态显示名称映射
 */
export const REVIEW_STATUS_MAP: Record<ReviewStatus, string> = {
  NOT_SUBMITTED: '未提交',
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝'
}

/**
 * 难度显示名称映射
 */
export const DIFFICULTY_MAP: Record<Difficulty, string> = {
  BEGINNER: '初级',
  INTERMEDIATE: '中级',
  ADVANCED: '高级'
}

/**
 * 获取来源类型显示名称
 */
export function getSourceTypeName(type: SourceType): string {
  return SOURCE_TYPE_MAP[type] || type
}

/**
 * 获取状态显示名称
 */
export function getStatusName(status: Status): string {
  return STATUS_MAP[status] || status
}

/**
 * 获取审核状态显示名称
 */
export function getReviewStatusName(status: ReviewStatus): string {
  return REVIEW_STATUS_MAP[status] || status
}

/**
 * 获取难度显示名称
 */
export function getDifficultyName(difficulty: Difficulty): string {
  return DIFFICULTY_MAP[difficulty] || difficulty
}

/**
 * 判断教程是否可编辑
 */
export function isTutorialEditable(tutorial: Tutorial): boolean {
  return tutorial.status !== 'PUBLISHED' || tutorial.reviewStatus === 'PENDING'
}

/**
 * 判断教程是否需要审核
 */
export function doesTutorialNeedReview(tutorial: Tutorial): boolean {
  return tutorial.reviewStatus !== 'APPROVED'
}

/**
 * 判断教程是否可以设为精选
 */
export function canTutorialBeFeatured(tutorial: Tutorial): boolean {
  return tutorial.status === 'PUBLISHED' && tutorial.reviewStatus === 'APPROVED'
}
