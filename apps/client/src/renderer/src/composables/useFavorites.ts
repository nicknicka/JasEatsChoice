/**
 * 菜品收藏功能组合式函数
 */
import { ref } from 'vue'
import { ElNotification } from 'element-plus'
import { Dish } from '../types'
import { HOME_CONSTANTS } from '../constants/home'

const FAVORITES_STORAGE_KEY = 'favoriteDishes'

export function useFavorites() {
  const favoriteDishIds = ref<Set<string | number>>(new Set())

  /**
   * 加载收藏列表
   */
  const loadFavorites = () => {
    const saved = localStorage.getItem(FAVORITES_STORAGE_KEY)
    if (saved) {
      try {
        favoriteDishIds.value = new Set(JSON.parse(saved))
      } catch (error) {
        console.error('加载收藏失败:', error)
        favoriteDishIds.value = new Set()
      }
    }
  }

  /**
   * 保存收藏列表
   */
  const saveFavorites = () => {
    localStorage.setItem(FAVORITES_STORAGE_KEY, JSON.stringify([...favoriteDishIds.value]))
  }

  /**
   * 检查菜品是否已收藏
   */
  const isFavorite = (dish: Dish): boolean => {
    return favoriteDishIds.value.has(dish.id || dish.name)
  }

  /**
   * 切换收藏状态
   */
  const toggleFavorite = (dish: Dish, event?: Event) => {
    if (event) {
      event.stopPropagation()
    }

    const dishId = dish.id || dish.name

    if (favoriteDishIds.value.has(dishId)) {
      favoriteDishIds.value.delete(dishId)
      showSuccess(`已取消收藏: ${dish.name}`)
    } else {
      favoriteDishIds.value.add(dishId)
      showSuccess(`已收藏: ${dish.name}`)
    }

    saveFavorites()
  }

  /**
   * 显示成功提示
   */
  const showSuccess = (message: string) => {
    ElNotification.success({
      title: '成功',
      message,
      duration: HOME_CONSTANTS.TOAST_DURATION.SUCCESS
    })
  }

  /**
   * 显示错误提示
   */
  const showError = (message: string) => {
    ElNotification.error({
      title: '错误',
      message,
      duration: HOME_CONSTANTS.TOAST_DURATION.ERROR
    })
  }

  return {
    favoriteDishIds,
    loadFavorites,
    saveFavorites,
    isFavorite,
    toggleFavorite,
    showError
  }
}
