/**
 * 搜索功能组合式函数
 */
import { ref, computed } from 'vue'
import { Dish } from '../types'

export function useSearch(dishes: Ref<Dish[]>) {
  const searchKeyword = ref('')

  /**
   * 过滤后的菜品列表
   */
  const filteredDishes = computed(() => {
    if (!searchKeyword.value) {
      return dishes.value
    }

    const keyword = searchKeyword.value.toLowerCase()
    return dishes.value.filter((dish) => {
      return (
        dish.name?.toLowerCase().includes(keyword) ||
        dish.category?.toLowerCase().includes(keyword) ||
        dish.tags?.toLowerCase().includes(keyword)
      )
    })
  })

  /**
   * 清空搜索
   */
  const clearSearch = () => {
    searchKeyword.value = ''
  }

  /**
   * 执行搜索
   */
  const handleSearch = () => {
    console.log('搜索:', searchKeyword.value)
    // 可以添加搜索分析或跳转到搜索结果页
  }

  return {
    searchKeyword,
    filteredDishes,
    clearSearch,
    handleSearch
  }
}
