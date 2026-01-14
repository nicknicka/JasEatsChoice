/**
 * 收藏功能相关逻辑
 */
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../config/index.js'

export function useFavorites() {
  // 状态管理
  const favorites = ref([])
  const isLoading = ref(false)

  /**
   * 从本地存储加载收藏列表
   */
  const loadFavoritesFromStorage = () => {
    const saved = localStorage.getItem('favorites')
    if (saved) {
      try {
        favorites.value = JSON.parse(saved)
      } catch (error) {
        console.error('解析收藏数据失败:', error)
        favorites.value = []
      }
    }
  }

  /**
   * 保存收藏列表到本地存储
   */
  const saveFavoritesToStorage = () => {
    localStorage.setItem('favorites', JSON.stringify(favorites.value))
  }

  /**
   * 从后端获取收藏列表
   */
  const fetchFavoritesFromBackend = async () => {
    try {
      isLoading.value = true
      const userId = localStorage.getItem('userId') || '1'

      const response = await axios.get(`${API_CONFIG.baseURL}/v1/favorites/user/${userId}`)

      if (response.data.code === 200 && response.data.data) {
        favorites.value = response.data.data
        saveFavoritesToStorage()
      }
    } catch (error) {
      console.error('获取收藏列表失败:', error)
      // 失败时从本地存储加载
      loadFavoritesFromStorage()
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 添加收藏
   */
  const addFavorite = async (item) => {
    try {
      const userId = localStorage.getItem('userId') || '1'

      // 检查是否已收藏
      const isFavorited = favorites.value.some(
        (fav) => fav.name === item.name && fav.type === item.type
      )

      if (isFavorited) {
        ElMessage.warning('您已收藏过该菜品')
        return false
      }

      // 调用后端API
      await axios.post(`${API_CONFIG.baseURL}/v1/favorites`, {
        userId: parseInt(userId),
        dishId: item.id,
        dishName: item.name,
        dishType: item.type,
        calories: item.calories,
        tags: item.tags,
        image: item.image,
        rating: item.rating
      })

      // 添加到本地收藏列表
      favorites.value.push({
        id: Date.now(),
        dishId: item.id,
        name: item.name,
        type: item.type,
        calories: item.calories,
        tags: item.tags,
        image: item.image,
        rating: item.rating,
        createdAt: new Date().toISOString()
      })

      saveFavoritesToStorage()
      ElMessage.success('收藏成功')
      return true
    } catch (error) {
      console.error('收藏失败:', error)

      // 后端失败时，仅添加到本地
      const isFavorited = favorites.value.some(
        (fav) => fav.name === item.name && fav.type === item.type
      )

      if (!isFavorited) {
        favorites.value.push({
          id: Date.now(),
          dishId: item.id,
          name: item.name,
          type: item.type,
          calories: item.calories,
          tags: item.tags,
          image: item.image,
          rating: item.rating,
          createdAt: new Date().toISOString()
        })
        saveFavoritesToStorage()
        ElMessage.success('收藏成功（仅本地）')
        return true
      }

      return false
    }
  }

  /**
   * 取消收藏
   */
  const removeFavorite = async (item) => {
    try {
      const userId = localStorage.getItem('userId') || '1'

      // 找到收藏项的ID
      const favoriteItem = favorites.value.find(
        (fav) => fav.dishId === item.id || fav.name === item.name
      )

      if (favoriteItem) {
        // 调用后端API删除
        await axios.delete(`${API_CONFIG.baseURL}/v1/favorites/${favoriteItem.id}`)
      }

      // 从本地列表中移除
      const index = favorites.value.findIndex(
        (fav) => fav.dishId === item.id || fav.name === item.name
      )

      if (index > -1) {
        favorites.value.splice(index, 1)
        saveFavoritesToStorage()
        ElMessage.success('已取消收藏')
        return true
      }

      return false
    } catch (error) {
      console.error('取消收藏失败:', error)

      // 后端失败时，仅从本地移除
      const index = favorites.value.findIndex(
        (fav) => fav.dishId === item.id || fav.name === item.name
      )

      if (index > -1) {
        favorites.value.splice(index, 1)
        saveFavoritesToStorage()
        ElMessage.success('已取消收藏（仅本地）')
        return true
      }

      return false
    }
  }

  /**
   * 切换收藏状态
   */
  const toggleFavorite = async (item) => {
    const isFavorited = isFavoritedItem(item)

    if (isFavorited) {
      await removeFavorite(item)
    } else {
      await addFavorite(item)
    }

    return !isFavorited
  }

  /**
   * 检查是否已收藏
   */
  const isFavoritedItem = (item) => {
    return favorites.value.some(
      (fav) => fav.dishId === item.id || fav.name === item.name
    )
  }

  /**
   * 计算属性：收藏数量
   */
  const favoritesCount = computed(() => favorites.value.length)

  /**
   * 初始化收藏列表
   */
  const initFavorites = () => {
    loadFavoritesFromStorage()
    fetchFavoritesFromBackend()
  }

  return {
    // 状态
    favorites,
    isLoading,
    favoritesCount,

    // 方法
    initFavorites,
    addFavorite,
    removeFavorite,
    toggleFavorite,
    isFavoritedItem,
    fetchFavoritesFromBackend,
    loadFavoritesFromStorage
  }
}
