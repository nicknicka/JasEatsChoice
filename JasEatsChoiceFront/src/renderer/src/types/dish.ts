/**
 * 菜品相关类型定义
 */

export interface Dish {
  id: string | number
  name: string
  image?: string
  category?: string
  tags?: string
  kcal: number
  rating?: number
}

export interface DishCardProps {
  dish: Dish
  isFavorite: boolean
  onToggleFavorite: (dish: Dish, event: Event) => void
  onShare: (dish: Dish, event: Event) => void
  onClick: (dish: Dish) => void
}
