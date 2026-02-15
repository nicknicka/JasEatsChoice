<script setup>
import { ref, computed, onMounted } from 'vue'

import { Search } from '@element-plus/icons-vue'
import axios from 'axios'
import { API_CONFIG } from '../../config'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../../store/authStore'

// 引入组件
import RecipeDetail from '../../components/RecipeDetail.vue'
import AddDish from '../../components/AddDish.vue'
import ImportMerchantDish from '../../components/ImportMerchantDish.vue'
import AddRecipe from '../../components/recipe/AddRecipe.vue'
import ReplaceDish from '../../components/ReplaceDish.vue'
import RecipeCard from '../../components/RecipeCard.vue'
import { useUserStore } from '../../store/userStore'

// 初始化 Pinia store
const authStore = useAuthStore()
const userStore = useUserStore()

// 我的食谱数据
const myRecipes = ref([])
const loadingFailed = ref(false)

// 批量操作相关变量
const selectedRecipes = ref([]) // 存储选中的食谱

// 全选/取消全选功能
const toggleAllRecipes = () => {
  if (selectedRecipes.value.length === filteredRecipes.value.length) {
    selectedRecipes.value = [] // 取消全选
  } else {
    selectedRecipes.value = filteredRecipes.value.map((recipe) => recipe.id) // 全选
  }
}

// 切换单个食谱的选择状态
const toggleRecipeSelection = (recipeId) => {
  console.log('=== toggleRecipeSelection 调用 ===')
  console.log('接收到的参数:', recipeId)
  console.log('参数类型:', typeof recipeId)
  console.log('当前选中列表:', selectedRecipes.value)

  // 兼容处理：如果传入的是对象则取id，如果是数字则直接使用
  const id = typeof recipeId === 'object' ? recipeId?.id : recipeId
  console.log('处理后的ID:', id)

  const index = selectedRecipes.value.indexOf(id)
  console.log('在数组中的索引:', index)

  if (index === -1) {
    selectedRecipes.value.push(id)
    console.log('添加ID到选中列表')
  } else {
    selectedRecipes.value.splice(index, 1)
    console.log('从选中列表移除ID')
  }
  console.log('更新后的选中列表:', selectedRecipes.value)
  console.log('=== toggleRecipeSelection 结束 ===')
}

// 模态框状态
const replaceDishVisible = ref(false)
const addDishVisible = ref(false)
const importMerchantDishVisible = ref(false)

// 当前选中的菜品
const selectedDish = ref(null)

// 加载我的食谱数据
const loadMyRecipes = () => {
  // 获取用户信息 - 从Pinia store获取
  const authStore = useAuthStore()
  const userStore = useUserStore()

  let userId = null

  // 从authStore获取userId，如果authStore中没有则从userStore的userInfo中获取
  if (authStore.userId) {
    userId = authStore.userId
  } else if (userStore.userInfo?.userId) {
    userId = userStore.userInfo.userId
  } else {
    console.error('加载我的食谱失败: 无法获取用户ID')
    ElMessage.error('加载我的食谱失败: 无法获取用户ID')
    return
  }

  // 确保userId有效
  if (!userId) {
    console.error('加载我的食谱失败: 用户ID无效')
    ElMessage.error('加载我的食谱失败: 用户ID无效')
    return
  }

  // 通过API从后端获取我的食谱数据
  axios
    .get(`${API_CONFIG.baseURL}${API_CONFIG.recipe.all}`, {
      params: {
        userId: userId
      }
    })
    .then((response) => {
      console.log('加载我的食谱成功:', response)
      if (response.data?.code === '200' && response.data?.data) {
        myRecipes.value = response.data.data.map((recipe) => ({
          ...recipe,
          // 确保食谱数据格式一致
          items: recipe.items
            ? typeof recipe.items === 'string'
              ? JSON.parse(recipe.items)
              : recipe.items
            : [],
          ingredients: recipe.ingredients
            ? typeof recipe.ingredients === 'string'
              ? JSON.parse(recipe.ingredients)
              : recipe.ingredients
            : [],
          // 处理 detail 字段：后端返回的是 detail，但为了兼容性同时处理 detail 和 details
          detail: recipe.detail,
          time: recipe.time 
        }))
      console.log('处理后的食谱列表数据:', myRecipes.value)
      } else {
        myRecipes.value = []
      }
      loadingFailed.value = false
      console.log('加载我的食谱成功:', myRecipes.value)
    })
    .catch((error) => {
      console.error('加载我的食谱失败:', error)
      myRecipes.value = []
      loadingFailed.value = true
      ElMessage.error('加载我的食谱失败，请稍后重试')
    })
}

// 组件挂载时加载数据
onMounted(() => {
  loadMyRecipes()
})

// 食谱筛选
const recipeFilter = ref('all')
// 搜索关键词
const searchKeyword = ref('')

// 防抖搜索函数
const debouncedSearch = ref(null)

// 滚动容器引用
const scrollContainer = ref(null)

// 滚动到顶部功能
const scrollToTop = () => {
  // 尝试多个滚动目标，确保在不同环境下都能工作
  const scrollTargets = [
    scrollContainer.value,
    document.querySelector('.main-content'),
    document.querySelector('.el-main'),
    document.documentElement,
    document.body
  ]

  for (const target of scrollTargets) {
    if (target && target.scrollHeight > target.clientHeight) {
      target.scrollTo({
        top: 0,
        behavior: 'smooth'
      })
      break // 找到第一个可滚动的元素后停止
    }
  }
}

// 计算属性：过滤后的食谱列表，收藏的食谱排在前面
const filteredRecipes = computed(() => {
  let filtered = []

  // 按类型筛选
  if (recipeFilter.value === 'all') {
    filtered = [...myRecipes.value]
  } else {
    filtered = myRecipes.value.filter((recipe) => recipe.type === recipeFilter.value)
  }

  // 按搜索关键词筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(
      (recipe) =>
        recipe.name.toLowerCase().includes(keyword) ||
        (recipe.items &&
          recipe.items.some((item) =>
            (typeof item === 'string' ? item : item.name).toLowerCase().includes(keyword)
          )) ||
        (recipe.ingredients &&
          recipe.ingredients.some((ingredient) =>
            (typeof ingredient === 'string' ? ingredient : ingredient.name)
              .toLowerCase()
              .includes(keyword)
          ))
    )
  }

  // 排序：收藏的食谱在前
  return filtered.sort((a, b) => {
    // 如果a收藏而b未收藏，a排在前面
    if (a.favorite && !b.favorite) return -1
    // 如果b收藏而a未收藏，b排在前面
    if (!a.favorite && b.favorite) return 1
    // 否则保持原顺序
    return 0
  })
})

// 切换收藏状态
const toggleFavorite = (recipe) => {
  // 更新本地状态
  recipe.favorite = !recipe.favorite

  // 立即同步到后端
  axios
    .put(`${API_CONFIG.baseURL}${API_CONFIG.recipe.toggleFavorite}${recipe.id}`, {
      favorite: recipe.favorite
    })
    .then((response) => {
      console.log('更新收藏状态成功:', response)
      if (response.data?.code !== '200') {
        recipe.favorite = !recipe.favorite // 恢复状态
        ElMessage.error('更新收藏状态失败')
      } else {
        // 显示成功消息
        if (recipe.favorite) {
          ElMessage.success('已收藏到我的食谱')
        } else {
          ElMessage.success('已取消收藏')
        }
      }
    })
    .catch((error) => {
      console.error('更新收藏状态失败:', error)
      recipe.favorite = !recipe.favorite // 恢复状态
      ElMessage.error('更新收藏状态失败，请检查网络')
    })
}

// 食谱详情组件相关
const detailDialogVisible = ref(false)
const selectedRecipe = ref(null)

// 查看详情
const viewRecipeDetails = (recipe) => {
  selectedRecipe.value = recipe
  detailDialogVisible.value = true
}

// 更新收藏状态
const updateRecipe = async (updatedRecipe) => {
  console.log('=== MyRecipe updateRecipe 开始 ===')
  console.log('接收到的更新食谱数据:', updatedRecipe)
  console.log('食谱ID:', updatedRecipe.id)
  console.log('食谱名称:', updatedRecipe.name)

  // 在myRecipes数组中找到对应的食谱并更新
  const index = myRecipes.value.findIndex((recipe) => recipe.id === updatedRecipe.id)
  console.log('找到的食谱索引:', index)

  if (index === -1) {
    console.warn('未找到对应的食谱')
    return
  }

  // 保存原始状态
  const originalRecipe = { ...myRecipes.value[index] }
  console.log('原始食谱数据:', originalRecipe)

  try {
    // 准备请求数据：将 items、ingredients 序列化为 JSON 字符串
    // customNutrition 也要序列化，但只在非 null 时才需要
    let customNutritionToSave = null
    if (updatedRecipe.customNutrition) {
      customNutritionToSave = typeof updatedRecipe.customNutrition === 'string'
        ? updatedRecipe.customNutrition
        : JSON.stringify(updatedRecipe.customNutrition)
    }

    const requestData = {
      ...updatedRecipe,
      items: updatedRecipe.items
        ? (typeof updatedRecipe.items === 'string'
            ? updatedRecipe.items
            : JSON.stringify(updatedRecipe.items))
        : null,
      ingredients: updatedRecipe.ingredients
        ? (typeof updatedRecipe.ingredients === 'string'
            ? updatedRecipe.ingredients
            : JSON.stringify(updatedRecipe.ingredients))
        : null,
      // 包含自定义营养信息（已序列化的）
      customNutrition: customNutritionToSave
    }

    console.log('准备调用后端API更新食谱')
    console.log('API URL:', API_CONFIG.baseURL + API_CONFIG.recipe.update + updatedRecipe.id)
    console.log('原始请求数据:', updatedRecipe)
    console.log('转换后的请求数据:', requestData)

    // 调用后端API更新食谱
    const response = await axios.put(
      API_CONFIG.baseURL + API_CONFIG.recipe.update + updatedRecipe.id,
      requestData
    )

    console.log('后端完整响应:', response)
    console.log('响应状态码:', response.status)
    console.log('响应数据 code:', response.data?.code)
    console.log('响应数据 message:', response.data?.message)
    console.log('响应数据 data:', response.data?.data)

    if (response.data?.code === '200') {
      console.log('✅ 更新成功，开始更新本地数据')
      console.log('后端返回的食谱数据:', response.data.data)

      // 更新本地数据
      myRecipes.value[index] = {
        ...response.data.data,
        items:
          typeof response.data.data.items === 'string'
            ? JSON.parse(response.data.data.items)
            : response.data.data.items || [],
        ingredients:
          typeof response.data.data.ingredients === 'string'
            ? JSON.parse(response.data.data.ingredients)
            : response.data.data.ingredients || []
      }
      selectedRecipe.value = myRecipes.value[index]
      console.log('本地数据已更新:', myRecipes.value[index])
      ElMessage.success('食谱更新成功')
    } else {
      console.warn('后端返回错误:', response.data)
      // 如果后端返回失败，恢复本地状态
      myRecipes.value[index] = originalRecipe
      selectedRecipe.value = originalRecipe
      ElMessage.error('食谱更新失败')
    }
  } catch (error) {
    console.error('更新食谱失败:', error)
    // 请求失败时恢复本地状态
    myRecipes.value[index] = originalRecipe
    selectedRecipe.value = originalRecipe
    ElMessage.error('更新食谱失败，请稍后重试')
  }

  console.log('=== MyRecipe updateRecipe 结束 ===')
}

// 更新烹饪时间
const handleUpdateCookTime = (newCookTime) => {
  if (selectedRecipe.value) {
    // 更新本地数据
    selectedRecipe.value.cookTime = newCookTime

    // 在myRecipes数组中找到对应的食谱并更新
    const index = myRecipes.value.findIndex((recipe) => recipe.id === selectedRecipe.value.id)
    if (index !== -1) {
      myRecipes.value[index].cookTime = newCookTime
    }

    // 调用后端API更新食谱
    axios
      .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + selectedRecipe.value.id, {
        ...selectedRecipe.value,
        cookTime: newCookTime
      })
      .then((response) => {
        console.log('更新烹饪时间成功:', response)
      })
      .catch((error) => {
        console.error('更新烹饪时间失败:', error)
      })
  }
}

// 替换菜品
const replaceDish = (recipe, dish) => {
  selectedRecipe.value = recipe
  selectedDish.value = dish
  replaceDishVisible.value = true
}

// 处理替换菜品点击（从 RecipeCard 组件）
const handleReplaceDishClick = ({ recipe, dish }) => {
  replaceDish(recipe, dish)
}

// 处理删除菜品点击（从 RecipeCard 组件）
const handleDeleteDishClick = ({ recipe, dish }) => {
  deleteDish(recipe, dish)
}

// 处理菜品替换
const handleReplaceDish = async ({ recipe, oldDish, newDish }) => {
  if (!recipe || !oldDish || !recipe.items) {
    console.error('替换菜品失败：无效的参数')
    return
  }

  // 先保存原菜品，以便失败时恢复
  const originalItems = [...recipe.items]

  try {
    // 找到并替换菜品
    const index = recipe.items.indexOf(oldDish)
    if (index !== -1) {
      // 替换菜品，保留营养数据
      recipe.items[index] = {
        name: newDish.name,
        ingredients: newDish.ingredients || [],
        calories: newDish.calories || newDish.calorie || 0,
        protein: newDish.protein || 0,
        carbs: newDish.carbs || 0,
        fat: newDish.fat || 0
      }

      // 调用后端API更新食谱 - 将items转换为JSON字符串
      const updateData = {
        ...recipe,
        items: JSON.stringify(recipe.items)
      }

      const response = await axios.put(
        API_CONFIG.baseURL + API_CONFIG.recipe.update + recipe.id,
        updateData
      )

      // 更新本地数据 - 确保items字段已解析
      const recipeIndex = myRecipes.value.findIndex((r) => r.id === recipe.id)
      if (recipeIndex !== -1) {
        const updatedRecipe = response.data.data
        // 确保items和ingredients字段被正确解析为数组
        myRecipes.value[recipeIndex] = {
          ...updatedRecipe,
          items: updatedRecipe.items
            ? typeof updatedRecipe.items === 'string'
              ? JSON.parse(updatedRecipe.items)
              : updatedRecipe.items
            : [],
          ingredients: updatedRecipe.ingredients
            ? typeof updatedRecipe.ingredients === 'string'
              ? JSON.parse(updatedRecipe.ingredients)
              : updatedRecipe.ingredients
            : []
        }
      }

      // 关闭对话框并重置状态
      replaceDishVisible.value = false
      selectedRecipe.value = null
      selectedDish.value = null
    }
  } catch (error) {
    console.error('替换菜品失败:', error)
    // 失败时恢复本地数据
    recipe.items = originalItems
    throw error // 让组件处理错误提示
  }
}

// 添加菜品
const addDish = (recipe) => {
  // 确保recipe.items是数组
  recipe.items = recipe.items || []
  selectedRecipe.value = recipe
  addDishVisible.value = true
}

// 打开导入商家菜品对话框
const openImportMerchantDish = (recipe) => {
  selectedRecipe.value = recipe
  importMerchantDishVisible.value = true
}

// 处理导入商家菜品
const handleImportMerchantDishes = (recipe, dishesToImport) => {
  if (dishesToImport.length > 0) {
    // 这里需要知道要导入到哪个食谱，需要先设置 selectedRecipe
    if (recipe) {
      // 先保存当前的items，以便失败时恢复
      const originalItems = [...recipe.items]

      // 将商家菜品转换为食谱需要的格式并添加到本地
      dishesToImport.forEach((dish) => {
        const recipeDish = {
          name: dish.name,
          ingredients: [], // 商家菜品默认没有食材，用户可以后续添加
          calories: 0,
          protein: 0,
          carbs: 0,
          fat: 0
        }
        recipe.items.push(recipeDish)
      })

      // 调用后端API更新食谱 - 将items转换为JSON字符串
      const updateData = {
        ...recipe,
        items: JSON.stringify(recipe.items)
      }

      axios
        .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + recipe.id, updateData)
        .then((response) => {
          // 更新本地数据 - 确保items字段已解析
          const recipeIndex = myRecipes.value.findIndex((r) => r.id === recipe.id)
          if (recipeIndex !== -1) {
            const updatedRecipe = response.data.data
            // 确保items和ingredients字段被正确解析为数组
            myRecipes.value[recipeIndex] = {
              ...updatedRecipe,
              items: updatedRecipe.items
                ? typeof updatedRecipe.items === 'string'
                  ? JSON.parse(updatedRecipe.items)
                  : updatedRecipe.items
                : [],
              ingredients: updatedRecipe.ingredients
                ? typeof updatedRecipe.ingredients === 'string'
                  ? JSON.parse(updatedRecipe.ingredients)
                  : updatedRecipe.ingredients
                : []
            }
          }

          ElMessage.success(`成功导入 ${dishesToImport.length} 道菜品`)
        })
        .catch((error) => {
          console.error('导入商家菜品失败:', error)
          // 失败时恢复本地数据
          recipe.items = originalItems
          ElMessage.error('导入商家菜品失败')
        })
    } else {
      ElMessage.error('请先选择要导入到的食谱')
    }
  } else {
    ElMessage.warning('请先选择要导入的菜品')
  }
}

// 处理添加菜品
const handleAddDish = (recipe, newDish) => {
  if (recipe && newDish.name.trim()) {
    // 先添加到本地
    recipe.items.push(newDish)

    // 调用后端API更新食谱 - 将items转换为JSON字符串
    const updateData = {
      ...recipe,
      items: JSON.stringify(recipe.items)
    }

    axios
      .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + recipe.id, updateData)
      .then((response) => {
        // 更新本地数据 - 确保items字段已解析
        const recipeIndex = myRecipes.value.findIndex((r) => r.id === recipe.id)
        if (recipeIndex !== -1) {
          const updatedRecipe = response.data.data
          // 确保items和ingredients字段被正确解析为数组
          myRecipes.value[recipeIndex] = {
            ...updatedRecipe,
            items: updatedRecipe.items
              ? typeof updatedRecipe.items === 'string'
                ? JSON.parse(updatedRecipe.items)
                : updatedRecipe.items
              : [],
            ingredients: updatedRecipe.ingredients
              ? typeof updatedRecipe.ingredients === 'string'
                ? JSON.parse(updatedRecipe.ingredients)
                : updatedRecipe.ingredients
              : []
          }
        }

        ElMessage.success('菜品已添加')
      })
      .catch((error) => {
        console.error('添加菜品失败:', error)
        // 失败时恢复本地数据
        recipe.items.pop()
        ElMessage.error('添加菜品失败')
      })
  }
}

// 删除菜品
const deleteDish = (recipe, dish) => {
  if (recipe && dish && recipe.items) {
    // 先更新本地数据
    const index = recipe.items.indexOf(dish)
    if (index !== -1) {
      recipe.items.splice(index, 1)

      // 调用后端API更新食谱
      const updateData = {
        ...recipe,
        items: JSON.stringify(recipe.items)
      }

      axios
        .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + recipe.id, updateData)
        .then((response) => {
          // 更新本地数据
          const recipeIndex = myRecipes.value.findIndex((r) => r.id === recipe.id)
          if (recipeIndex !== -1) {
            const updatedRecipe = response.data.data
            // 确保items字段被正确解析为数组
            myRecipes.value[recipeIndex] = {
              ...updatedRecipe,
              items: updatedRecipe.items
                ? typeof updatedRecipe.items === 'string'
                  ? JSON.parse(updatedRecipe.items)
                  : updatedRecipe.items
                : [],
              ingredients: updatedRecipe.ingredients
                ? typeof updatedRecipe.ingredients === 'string'
                  ? JSON.parse(updatedRecipe.ingredients)
                  : updatedRecipe.ingredients
                : []
            }
          }

          ElMessage.success('菜品已删除')
        })
        .catch((error) => {
          console.error('删除菜品失败:', error)
          // 失败时恢复本地数据
          recipe.items.splice(index, 0, dish)
          ElMessage.error('删除菜品失败')
        })
    }
  }
}

// 批量删除食谱
const batchDeleteRecipes = () => {
  if (selectedRecipes.value.length === 0) return

  // 确认删除
  ElMessageBox.confirm('确定要批量删除选中的食谱吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 遍历删除选中的食谱
      const deletePromises = selectedRecipes.value.map((id) =>
        axios.delete(API_CONFIG.baseURL + API_CONFIG.recipe.delete + id)
      )

      Promise.all(deletePromises)
        .then((responses) => {
          // 删除成功，更新本地数据
          selectedRecipes.value.forEach((id) => {
            const index = myRecipes.value.findIndex((r) => r.id === id)
            if (index !== -1) {
              myRecipes.value.splice(index, 1)
            }
          })
          // 清空选中列表
          selectedRecipes.value = []
          ElMessage.success(`成功删除${responses.length}个食谱`)
        })
        .catch((error) => {
          console.error('批量删除失败:', error)
          ElMessage.error('批量删除失败')
        })
    })
    .catch(() => {
      // 取消删除
    })
}

// 批量收藏食谱
const batchFavoriteRecipes = () => {
  if (selectedRecipes.value.length === 0) return

  // 批量设置所有选中的食谱为收藏状态
  const recipeIds = selectedRecipes.value
  const favorite = true // 批量收藏

  axios
    .put(API_CONFIG.baseURL + API_CONFIG.recipe.batchToggleFavorite, {
      recipeIds,
      favorite
    })
    .then((response) => {
      // 更新本地数据
      console.log('批量收藏成功:', response)
      const updatedRecipes = response.data.data || []
      updatedRecipes.forEach((updatedRecipe) => {
        const recipeIndex = myRecipes.value.findIndex((r) => r.id === updatedRecipe.id)
        if (recipeIndex !== -1) {
          // 确保返回的食谱有items数组并已解析
          const originalRecipe = myRecipes.value[recipeIndex]
          // 使用 Object.assign 创建新对象，避免覆盖原对象
          const updatedRecipeWithParsedItems = Object.assign({}, originalRecipe, updatedRecipe, {
            items:
              typeof updatedRecipe.items === 'string'
                ? JSON.parse(updatedRecipe.items)
                : updatedRecipe.items || originalRecipe.items || [], // 确保items字段不会丢失
            // 统一字段名称，将后端返回的favorite映射为前端使用的isFavorite
            isFavorite:
              updatedRecipe.favorite !== undefined
                ? updatedRecipe.favorite
                : updatedRecipe.isFavorite
          })
          myRecipes.value[recipeIndex] = updatedRecipeWithParsedItems
        }
      })

      // 清空选中列表
      selectedRecipes.value = []
      ElMessage.success(`成功收藏${updatedRecipes.length}个食谱`)
    })
    .catch((error) => {
      console.error('批量收藏失败:', error)
      ElMessage.error('批量收藏失败')
    })
}

// 添加食谱组件相关
const addDialogVisible = ref(false)
const editingRecipe = ref(null) // 当前正在编辑的食谱

// 添加新食谱
const handleAddRecipe = (newRecipe) => {
  // 调用后端API保存新食谱
  axios
    .post(`${API_CONFIG.baseURL}${API_CONFIG.recipe.add}`, {
      ...newRecipe,
      userId: authStore.userId || userStore.userInfo?.userId, // 添加用户ID
      favorite: false, // 默认未收藏
      items: JSON.stringify([]), // 将空数组转换为JSON字符串
      calories: 0 // 默认0热量
    })
    .then((response) => {
      console.log('保存食谱:', response)
      if (response.data?.code === '200' && response.data?.data) {
        // 将后端返回的完整食谱数据添加到本地列表
        const savedRecipe = {
          ...response.data.data
        }
        myRecipes.value.push(savedRecipe)
        ElMessage.success('食谱添加成功')
      } else {
        ElMessage.error('保存食谱失败')
      }
    })
    .catch((error) => {
      console.error('保存食谱失败:', error)
      ElMessage.error('保存食谱失败，请稍后重试')
    })
}

// 打开添加食谱对话框
const openAddDialog = () => {
  editingRecipe.value = null
  addDialogVisible.value = true
}

// 编辑食谱
const editRecipe = (recipe) => {
  console.log('收到编辑食谱请求:', recipe)
  if (!recipe) {
    console.warn('编辑食谱失败：食谱数据为空')
    return
  }
  editingRecipe.value = recipe
  addDialogVisible.value = true
  console.log('打开编辑对话框，食谱数据:', editingRecipe.value)
}

// 快捷编辑：从详情对话框直接进入编辑模式
const startEdit = (recipe) => {
  console.log('收到快捷编辑请求:', recipe)
  if (!recipe) {
    console.warn('编辑食谱失败：食谱数据为空')
    return
  }
  editingRecipe.value = recipe
  addDialogVisible.value = true
  console.log('打开编辑对话框，食谱数据:', editingRecipe.value)
}

// 保存食谱编辑
const saveRecipeEdit = async (updatedRecipe) => {
  console.log('=== MyRecipe saveRecipeEdit 开始 ===')
  console.log('接收到的更新食谱数据:', updatedRecipe)

  try {
    // 准备请求数据：将 items、ingredients 序列化为 JSON 字符串
    // customNutrition 也要序列化，但只在非 null 时才需要
    let customNutritionToSave = null
    if (updatedRecipe.customNutrition) {
      customNutritionToSave = typeof updatedRecipe.customNutrition === 'string'
        ? updatedRecipe.customNutrition
        : JSON.stringify(updatedRecipe.customNutrition)
    }

    const requestData = {
      ...updatedRecipe,
      items: updatedRecipe.items
        ? (typeof updatedRecipe.items === 'string'
            ? updatedRecipe.items
            : JSON.stringify(updatedRecipe.items))
        : null,
      ingredients: updatedRecipe.ingredients
        ? (typeof updatedRecipe.ingredients === 'string'
            ? updatedRecipe.ingredients
            : JSON.stringify(updatedRecipe.ingredients))
        : null,
      // 包含自定义营养信息（已序列化的）
      customNutrition: customNutritionToSave
    }

    console.log('转换后的请求数据:', requestData)

    const response = await axios.put(
      API_CONFIG.baseURL + API_CONFIG.recipe.update + updatedRecipe.id,
      requestData
    )

    console.log('后端完整响应:', response)
    console.log('响应状态码:', response.status)
    console.log('响应数据 code:', response.data?.code)
    console.log('响应数据 message:', response.data?.message)
    console.log('响应数据 data:', response.data?.data)

    if (response.data?.code === '200') {
      console.log('✅ 更新成功，开始更新本地数据')
      console.log('后端返回的食谱数据:', response.data.data)

      // 更新本地数据
      const index = myRecipes.value.findIndex((r) => r.id === updatedRecipe.id)
      if (index !== -1) {
        myRecipes.value[index] = {
          ...response.data.data,
          items:
            typeof response.data.data.items === 'string'
              ? JSON.parse(response.data.data.items)
              : response.data.data.items || [],
          ingredients:
            typeof response.data.data.ingredients === 'string'
              ? JSON.parse(response.data.data.ingredients)
              : response.data.data.ingredients || []
        }
        console.log('本地数据已更新:', myRecipes.value[index])
      }
      ElMessage.success('食谱更新成功')
    } else {
      console.warn('后端返回错误:', response.data)
      ElMessage.error('食谱更新失败')
    }
  } catch (error) {
    console.error('更新食谱失败:', error)
    ElMessage.error('更新食谱失败，请稍后重试')
  }

  console.log('=== MyRecipe saveRecipeEdit 结束 ===')
}

// 订单导入相关
const orders = ref([])
const importDialogVisible = ref(false)
const selectedOrder = ref(null)

// 从订单导入食谱
const importFromOrders = () => {
  const authStore = useAuthStore()
  const userStore = useUserStore()

  let userId = null

  // 获取用户ID
  if (authStore.userId) {
    userId = authStore.userId
  } else if (userStore.userInfo?.userId) {
    userId = userStore.userInfo.userId
  } else {
    ElMessage.error('无法获取用户ID')
    return
  }

  // 获取用户订单
  axios
    .get(`${API_CONFIG.baseURL}${API_CONFIG.order.list}${userId}`)
    .then((response) => {
      if (response.data?.code === '200' && response.data?.data) {
        orders.value = response.data.data
        importDialogVisible.value = true
      } else {
        ElMessage.warning('暂无订单数据')
      }
    })
    .catch((error) => {
      console.error('获取订单失败:', error)
      ElMessage.error('获取订单失败，请稍后重试')
    })
}

// 确认从订单导入食谱
const confirmImportFromOrder = () => {
  if (!selectedOrder.value) return

  // 构造新食谱数据
  const newRecipe = {
    name: `从订单导入 - ${selectedOrder.value.orderNo}`,
    type: '晚餐', // 默认餐型
    items: selectedOrder.value.items.map((item) => ({
      name: item.name,
      quantity: item.quantity,
      ingredients: [],
      calories: 0,
      protein: 0,
      carbs: 0,
      fat: 0
    })),
    calories: 0,
    time: '30分钟', // 默认时间
    favorite: false
  }

  // 计算总热量
  newRecipe.calories = selectedOrder.value.items.reduce(
    (sum, item) => sum + (item.calories || 0),
    0
  )

  // 调用添加食谱API
  axios
    .post(`${API_CONFIG.baseURL}${API_CONFIG.recipe.add}`, newRecipe)
    .then((response) => {
      if (response.data?.code === '200' && response.data?.data) {
        ElMessage.success('从订单导入食谱成功')
        importDialogVisible.value = false
        selectedOrder.value = null
        loadMyRecipes() // 重新加载食谱列表
      } else {
        ElMessage.error('导入食谱失败')
      }
    })
    .catch((error) => {
      console.error('导入食谱失败:', error)
      ElMessage.error('导入食谱失败，请稍后重试')
    })
}

// 导出食谱到饮食记录
const exportToDietRecord = () => {
  const authStore = useAuthStore()
  const userStore = useUserStore()

  let userId = null

  // 获取用户ID
  if (authStore.userId) {
    userId = authStore.userId
  } else if (userStore.userInfo?.userId) {
    userId = userStore.userInfo.userId
  } else {
    ElMessage.error('无法获取用户ID')
    return
  }

  // 确认导出
  ElMessageBox.confirm(
    `确定要将选中的 ${selectedRecipes.value.length} 个食谱导出到饮食记录吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  )
    .then(() => {
      // 批量导出
      const exportPromises = selectedRecipes.value.map((recipeId) => {
        // 找到对应的食谱
        const recipe = myRecipes.value.find((r) => r.id === recipeId)
        if (!recipe) return Promise.resolve()

        // 计算食谱的总营养成分
        let totalCalories = 0
        let totalProtein = 0
        let totalFat = 0
        let totalCarbs = 0

        // 优先使用自定义营养信息（如果存在）
        let customNutrition = null
        if (recipe.customNutrition) {
          if (typeof recipe.customNutrition === 'string') {
            try {
              customNutrition = JSON.parse(recipe.customNutrition)
            } catch (e) {
              console.error('解析 customNutrition 失败:', e)
            }
          } else {
            customNutrition = recipe.customNutrition
          }
        }

        if (customNutrition) {
          // 使用自定义营养信息
          console.log('使用自定义营养信息:', customNutrition)
          totalCalories = customNutrition.calories || 0
          totalProtein = customNutrition.protein || 0
          totalFat = customNutrition.fat || 0
          totalCarbs = customNutrition.carbs || 0
        } else {
          // 否则遍历食谱的菜品 items，累加营养信息
          if (recipe.items && Array.isArray(recipe.items)) {
            recipe.items.forEach((item) => {
              console.log('处理菜品item:', item)
              totalCalories += item.calories || 0
              totalProtein += item.protein || 0
              totalFat += item.fat || 0
              totalCarbs += item.carbs || 0
            })
          }
        }

        console.log('食谱总营养:', { totalCalories, totalProtein, totalFat, totalCarbs })

        // 检查营养值是否全部为 0
        if (totalCalories === 0 && totalProtein === 0 && totalFat === 0 && totalCarbs === 0) {
          console.warn('食谱营养信息为空，跳过导出')
          ElMessage.warning(`食谱"${recipe.name}"没有营养信息，请先添加菜品营养或设置自定义营养信息`)
          return Promise.reject(new Error('营养信息为空'))
        }

        // 构造饮食记录数据 - 字段名必须与后端 CalorieRecord 实体类匹配
        const dietRecord = {
          userId: userId,
          dishId: recipeId, // 使用 recipeId 作为 dishId
          calorie: Math.round(totalCalories), // 注意字段名是 calorie 不是 calories
          protein: Number(totalProtein.toFixed(2)),
          fat: Number(totalFat.toFixed(2)),
          carbohydrate: Number(totalCarbs.toFixed(2)),
          mealTime: recipe.type || '未分类', // 使用食谱的餐型（早餐、午餐等）
          recordTime: new Date().toISOString(), // 记录时间使用 ISO 格式
          foodName: recipe.name, // 注意字段名是 foodName 不是 name
          description: `从食谱"${recipe.name}"导出` // 添加描述信息
        }

        console.log('导出饮食记录数据:', JSON.stringify(dietRecord, null, 2))

        // 调用添加饮食记录API
        return axios.post(`${API_CONFIG.baseURL}${API_CONFIG.diet.add}`, dietRecord)
      })

      // 处理所有请求
      Promise.allSettled(exportPromises)
        .then((results) => {
          const successCount = results.filter(
            (result) => result.status === 'fulfilled' && result.value?.data?.code === '200'
          ).length
          const skippedCount = results.filter(
            (result) => result.status === 'rejected' && result.reason?.message === '营养信息为空'
          ).length

          if (successCount > 0) {
            ElMessage.success(`成功导出 ${successCount} 个食谱到饮食记录${skippedCount > 0 ? `，跳过 ${skippedCount} 个无营养信息的食谱` : ''}`)
            selectedRecipes.value = [] // 清空选择
          } else if (skippedCount > 0) {
            ElMessage.warning(`所有食谱都没有营养信息，请先添加菜品营养或设置自定义营养信息`)
          }
        })
        .catch((error) => {
          console.error('导出失败:', error)
          ElMessage.error('导出失败，请稍后重试')
        })
    })
    .catch(() => {
      // 取消导出
      ElMessage.info('已取消导出')
    })
}
</script>

<template>
  <!-- 单一根节点包裹器，用于 Transition 动画 -->
  <div class="my-recipe-wrapper">
    <div ref="scrollContainer" class="my-recipe-container">
      <div class="recipe-header fade-in-up">
        <div>
          <h2>我的食谱</h2>
          <div v-if="filteredRecipes.length > 0" class="search-result-count">
            <span>共找到 {{ filteredRecipes.length }} 个食谱</span>
          </div>
        </div>

        <div class="filter-section fade-in-up">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索食谱、菜品或食材"
          size="default"
          style="width: 100%; max-width: 500px; margin: 0 0 12px 0"
          clearable
        >
          <template #prefix>
            <el-icon class="el-input__icon"><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="recipeFilter"
          placeholder="筛选食谱"
          size="small"
          style="width: 160px; margin: 0"
        >
          <el-option label="全部" value="all" />
          <el-option label="早餐" value="早餐" />
          <el-option label="午餐" value="午餐" />
          <el-option label="晚餐" value="晚餐" />
          <el-option label="加餐" value="加餐" />
        </el-select>
      </div>
    </div>

    <!-- 添加食谱和批量管理按钮 -->
    <div class="add-recipe-section">
      <div class="button-group">
        <el-button type="primary" size="small" @click="openAddDialog"> ➕ 添加食谱 </el-button>
        <el-button type="success" size="small" @click="importFromOrders"> 📥 从订单导入 </el-button>

        <!-- 全选/取消全选按钮 -->
        <el-button
          type="default"
          size="small"
          @click="toggleAllRecipes"
          :disabled="filteredRecipes.length === 0"
        >
          {{ selectedRecipes.length === filteredRecipes.length ? '❌ 取消全选' : '✅ 全选' }}
        </el-button>

        <el-button
          type="danger"
          size="small"
          :disabled="selectedRecipes.length === 0"
          @click="batchDeleteRecipes"
        >
          🗑️ 批量删除
        </el-button>
        <el-button
          type="warning"
          size="small"
          :disabled="selectedRecipes.length === 0"
          @click="batchFavoriteRecipes"
        >
          ⭐ 批量收藏
        </el-button>
        <el-button
          type="info"
          size="small"
          :disabled="selectedRecipes.length === 0"
          @click="exportToDietRecord"
        >
          📤 导出到饮食记录
        </el-button>
      </div>
    </div>

    <!-- 食谱列表 -->
    <div class="recipe-list">
      <RecipeCard
        v-for="recipe in filteredRecipes"
        :key="recipe.id"
        :recipe="recipe"
        :selectable="true"
        :selected-ids="selectedRecipes"
        :show-nutrition="true"
        :show-time="true"
        @toggle-select="toggleRecipeSelection"
        @toggle-favorite="toggleFavorite"
        @view-details="viewRecipeDetails"
        @add-dish="addDish"
        @import-merchant-dish="openImportMerchantDish"
        @replace-dish="handleReplaceDishClick"
        @delete-dish="handleDeleteDishClick"
      />
    </div>

    <!-- 空数据提示 -->
    <el-empty
      v-if="filteredRecipes.length === 0"
      :description="loadingFailed ? '暂未找到我的食谱' : '暂无食谱'"
    ></el-empty>

    <!-- 滚动到顶部按钮 -->
    <el-button type="primary" circle size="small" class="scroll-to-top-btn" @click="scrollToTop">
      ↑
    </el-button>
  </div>

  <!-- 食谱详情组件 -->
  <RecipeDetail
    v-model:visible="detailDialogVisible"
    v-model:recipe="selectedRecipe"
    @update:recipe="updateRecipe"
    @update:cook-time="handleUpdateCookTime"
    @start-edit="editRecipe"
  />

  <!-- 替换菜品组件 -->
  <ReplaceDish
    v-model:visible="replaceDishVisible"
    :recipe="selectedRecipe"
    :dish="selectedDish"
    @replace="handleReplaceDish"
    @close="selectedRecipe = null; selectedDish = null"
  ></ReplaceDish>

  <!-- 添加菜品组件 -->
  <AddDish
    v-model:visible="addDishVisible"
    :recipe="selectedRecipe"
    @add="handleAddDish"
    @close="selectedRecipe = null"
  ></AddDish>

  <!-- 导入商家菜品组件 -->
  <ImportMerchantDish
    v-model:visible="importMerchantDishVisible"
    :recipe="selectedRecipe"
    @import="handleImportMerchantDishes"
    @close="selectedRecipe = null"
  ></ImportMerchantDish>

  <!-- 添加食谱组件 -->
  <AddRecipe
    v-model:visible="addDialogVisible"
    :recipe="editingRecipe"
    @add-recipe="handleAddRecipe"
    @update-recipe="saveRecipeEdit"
    @start-edit="startEdit"
  />

  <!-- 从订单导入对话框 -->
  <el-dialog
    v-model="importDialogVisible"
    title="从订单导入食谱"
    width="600px"
    top="10%"
    @close="selectedOrder = null"
  >
    <div v-if="orders.length > 0">
      <el-select
        v-model="selectedOrder"
        placeholder="请选择要导入的订单"
        style="width: 100%"
        size="large"
      >
        <el-option
          v-for="order in orders"
          :key="order.id"
          :label="`订单号: ${order.orderNo} (${new Date(order.createTime).toLocaleString()})`"
          :value="order"
        />
      </el-select>

      <div v-if="selectedOrder" style="margin-top: 20px">
        <h4>订单详情:</h4>
        <p>订单号: {{ selectedOrder.orderNo }}</p>
        <p>创建时间: {{ new Date(selectedOrder.createTime).toLocaleString() }}</p>
        <h5>菜品:</h5>
        <el-tag
          v-for="(item, index) in selectedOrder.items"
          :key="index"
          type="info"
          style="margin: 2px"
        >
          {{ item.name }} ({{ item.quantity }})
        </el-tag>
      </div>
    </div>
    <div v-else>暂无订单数据</div>

    <template #footer>
      <el-button @click="importDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="confirmImportFromOrder">导入</el-button>
    </template>
  </el-dialog>
  </div>
</template>

<style lang="less">
.my-recipe-wrapper {
  display: contents;
}

.my-recipe-container {
  padding: 24px;
  max-height: 100%;
  overflow-y: auto;

  .recipe-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 24px;

    h2 {
      font-size: 32px;
      margin: 0;
      color: #333;
    }

    .filter-section {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 12px;
      flex-wrap: wrap;
    }
  }

  .recipe-list {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
    gap: 0 !important; /* 设置为0以避免双重间距 */

    // 在中等屏幕以下改为一行一列
    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }

    // 在极小屏幕上确保卡片有最小边距
    @media (max-width: 420px) {
      padding: 0 10px;
    }
  }

  .recipe-card {
    background: #ffffff !important;
    border-radius: 16px !important;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
    border: 1px solid #e8e8e8 !important;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    overflow: hidden;
    position: relative;
    margin-bottom: 27px !important;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 4px;
      background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    }

    &.recipe-card-favorited {
      border: 2px solid #ffd700 !important;
      box-shadow:
        0 8px 24px rgba(255, 215, 0, 0.2),
        0 0 0 4px rgba(255, 215, 0, 0.08);

      &::before {
        background: linear-gradient(90deg, #ffd700 0%, #ffed4e 100%);
      }
    }

    &:hover {
      transform: translateY(-6px) scale(1.02);
      box-shadow: 0 16px 40px rgba(0, 0, 0, 0.12);
      border-color: #d8d8d8 !important;
    }

    .card-header {
      position: relative;
      display: flex;
      align-items: center;
      gap: 16px;
      font-size: 20px;
      font-weight: 700;
      color: #2c3e50;
      padding: 20px 24px !important;

      .meal-icon {
        font-size: 32px;
        padding: 10px;
        border-radius: 50%;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        box-shadow: none;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 56px;
        height: 56px;
      }
    }

    .recipe-items {
      margin: 24px;
      display: flex;
      flex-wrap: wrap;
      gap: 12px;

      .el-tag {
        padding: 8px 16px;
        border-radius: 20px;
        font-size: 14px;
        font-weight: 500;
      }
    }

    .recipe-stats {
      margin: 0 24px;
      display: flex;
      gap: 20px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 5px;
        color: #666;
      }
    }

    .recipe-actions {
      display: flex;
      justify-content: flex-end;
      margin: 20px 24px 24px;
      gap: 8px; /* 统一间距 */

      .el-button {
        font-size: 14px;
        padding: 6px 16px;
        border-radius: 8px;
        margin: 0;
      }
    }
  }

  // 右上角收藏按钮样式
  .card-favorite {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
  }

  // 批量选择复选框样式
  .checkbox-wrapper {
    :deep(.el-checkbox__label) {
      display: none !important;
    }
    margin-right: 10px;
  }

  // 添加食谱按钮样式
  .add-recipe-section {
    margin-bottom: 24px;
  }

  .button-group {
    display: flex;
    flex-wrap: wrap;
    gap: 12px; /* 按钮之间的间距 */
    padding: 16px;
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

    .el-button {
      border-radius: 8px !important;
      padding: 10px 20px !important;
      font-weight: 500 !important;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.18);
      }

      &:active {
        transform: translateY(-1px);
      }
    }
  }

  // 选中卡片样式 - 增强版
  .recipe-card-selected {
    border: 3px solid #667eea !important; /* 更粗的边框 */
    box-shadow:
      0 12px 32px rgba(102, 126, 234, 0.3),
      /* 更强的阴影 */ 0 0 0 6px rgba(102, 126, 234, 0.12); /* 更大的光晕 */
    transform: scale(1.05); /* 轻微放大效果 */
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &::before {
      background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
      height: 6px; /* 更宽的顶部渐变条 */
    }

    // 同时处理选中和悬停状态，确保样式优先级
    &:hover {
      border: 3px solid #667eea !important;
      box-shadow:
        0 12px 32px rgba(102, 126, 234, 0.3),
        0 0 0 6px rgba(102, 126, 234, 0.12) !important;
      transform: scale(1.05) !important;
      border-color: #667eea !important;
    }
  }

  // 收藏按钮样式
  .favorite-btn {
    color: #ffd700 !important; // 收藏状态用金色，确保覆盖默认样式
    font-weight: bold;
  }

  /* 添加食谱表单样式 */
  .add-recipe-form {
    padding: 20px 0;

    .el-form {
      max-width: 400px;
      margin: 0 auto;
    }

    .el-form-item {
      margin-bottom: 20px;
    }
  }

  /* 不同类型食谱卡片的样式 */
  .recipe-card {
    &.早餐 {
      border-left: 4px solid #ffc107;

      &::before {
        background: linear-gradient(90deg, #ffc107 0%, #ffeb3b 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #ffc107 0%, #ffeb3b 100%) !important;
        color: #333 !important;
      }
    }

    &.午餐 {
      border-left: 4px solid #4caf50;

      &::before {
        background: linear-gradient(90deg, #4caf50 0%, #8bc34a 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #4caf50 0%, #8bc34a 100%) !important;
        color: white !important;
      }
    }

    &.晚餐 {
      border-left: 4px solid #2196f3;

      &::before {
        background: linear-gradient(90deg, #2196f3 0%, #64b5f6 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #2196f3 0%, #64b5f6 100%) !important;
        color: white !important;
      }
    }

    &.加餐 {
      border-left: 4px solid #1e88e5;

      &::before {
        background: linear-gradient(90deg, #1e88e5 0%, #42a5f5 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%) !important;
        color: white !important;
      }
    }
  }

  /* 自定义搜索框和筛选框样式 - 优化版 */
  :deep(.el-input) {
    border-radius: 8px;
    border: 1px solid #e0e0e0;
    transition: all 0.3s ease;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);

    &:hover {
      border-color: #667eea;
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.12);
    }

    &__inner {
      background-color: #ffffff;
      border: none;
      color: #333;
      font-size: 14px;
      padding: 10px 14px;
      border-radius: 8px;
    }

    &__prefix {
      color: #999;
      font-size: 16px;
    }

    &__suffix {
      color: #999;
    }
  }

  :deep(.el-select) {
    border-radius: 8px;
    border: 1px solid #e0e0e0;
    transition: all 0.3s ease;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);

    &:hover {
      border-color: #667eea;
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.12);
    }

    &__inner {
      background-color: #ffffff;
      border: none;
      color: #333;
      font-size: 14px;
      padding: 10px 14px;
      border-radius: 8px;
    }

    &__arrow {
      color: #999;
    }
  }

  /* 自定义标签颜色和交互 */
  :deep(.el-tag) {
    transition: all 0.3s ease;
    cursor: pointer;
    border-radius: 20px;
    font-weight: 500;

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
      opacity: 0.9;
    }
  }

  :deep(.el-tag--warning) {
    background-color: #fff3e0;
    color: #f57c00;
  }

  :deep(.el-tag--success) {
    background-color: #e8f5e9;
    color: #388e3c;
  }

  :deep(.el-tag--primary) {
    background-color: #e3f2fd;
    color: #1976d2;
  }

  :deep(.el-tag--info) {
    background-color: #e1f5fe;
    color: #0288d1;
  }

  :deep(.el-tag--purple) {
    background-color: #f3e5f5;
    color: #7b1fa2;
  }
  :deep(.el-tag--blue) {
    background-color: #e3f2fd;
    color: #1565c0;
  }

  // 滚动到顶部按钮
  .scroll-to-top-btn {
    position: fixed;
    bottom: 30px;
    right: 30px;
    z-index: 1000;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transition: opacity 0.3s;
    opacity: 0.8;

    &:hover {
      opacity: 1;
      transform: translateY(-2px);
    }
  }

  .recipe-detail-dialog {
    padding: 24px;
    background-color: #f5f7fa;
    font-family:
      -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  }

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 700px;
    border: 1px solid red;
  }
}
</style>
