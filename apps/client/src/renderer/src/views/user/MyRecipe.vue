<script setup>
import { ref, computed, onMounted } from 'vue'

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
  <div class="my-recipe-wrapper">
  <div ref="scrollContainer" class="nordic-my-recipe">
    <!-- 页面标题 -->
    <div class="nordic-page-header">
      <div>
        <h2>我的食谱</h2>
        <span class="recipe-count" v-if="filteredRecipes.length > 0">
          共 {{ filteredRecipes.length }} 个食谱
        </span>
      </div>
    </div>

    <!-- 搜索与筛选 -->
    <div class="toolbar-row">
      <div class="search-box">
        <span class="search-icon">&#128269;</span>
        <input
          v-model="searchKeyword"
          placeholder="搜索食谱、菜品或食材..."
          class="search-input"
        />
      </div>
      <div class="filter-chips">
        <button
          class="meal-chip"
          :class="{ active: recipeFilter === 'all' }"
          @click="recipeFilter = 'all'"
        >全部</button>
        <button
          class="meal-chip"
          :class="{ active: recipeFilter === '早餐' }"
          @click="recipeFilter = '早餐'"
        >早餐</button>
        <button
          class="meal-chip"
          :class="{ active: recipeFilter === '午餐' }"
          @click="recipeFilter = '午餐'"
        >午餐</button>
        <button
          class="meal-chip"
          :class="{ active: recipeFilter === '晚餐' }"
          @click="recipeFilter = '晚餐'"
        >晚餐</button>
        <button
          class="meal-chip"
          :class="{ active: recipeFilter === '加餐' }"
          @click="recipeFilter = '加餐'"
        >加餐</button>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <button class="nordic-btn accent" @click="openAddDialog">+ 添加食谱</button>
      <button class="nordic-btn" @click="importFromOrders">从订单导入</button>
      <div class="action-spacer"></div>
      <button
        class="nordic-btn ghost"
        @click="toggleAllRecipes"
        :disabled="filteredRecipes.length === 0"
      >
        {{ selectedRecipes.length === filteredRecipes.length ? '取消全选' : '全选' }}
      </button>
      <button
        class="nordic-btn ghost"
        :disabled="selectedRecipes.length === 0"
        @click="batchDeleteRecipes"
      >批量删除</button>
      <button
        class="nordic-btn ghost"
        :disabled="selectedRecipes.length === 0"
        @click="batchFavoriteRecipes"
      >批量收藏</button>
      <button
        class="nordic-btn ghost"
        :disabled="selectedRecipes.length === 0"
        @click="exportToDietRecord"
      >导出到饮食记录</button>
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
    <div class="empty-state" v-if="filteredRecipes.length === 0">
      <div class="empty-icon">&#127858;</div>
      <div class="empty-title">{{ loadingFailed ? '暂未找到我的食谱' : '暂无食谱' }}</div>
      <div class="empty-desc">{{ loadingFailed ? '请稍后重试' : '点击上方按钮添加你的第一个食谱' }}</div>
    </div>

    <!-- 回到顶部 -->
    <button class="scroll-top-btn" @click="scrollToTop" title="回到顶部">&#8593;</button>
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
    <div class="import-order-panel">
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

        <div v-if="selectedOrder" class="order-preview">
          <div class="order-meta">
            <span>订单号: {{ selectedOrder.orderNo }}</span>
            <span>{{ new Date(selectedOrder.createTime).toLocaleString() }}</span>
          </div>
          <div class="order-dishes">
            <span
              v-for="(item, index) in selectedOrder.items"
              :key="index"
              class="dish-chip"
            >{{ item.name }} ({{ item.quantity }})</span>
          </div>
        </div>
      </div>
      <div v-else class="no-orders">暂无订单数据</div>
    </div>

    <template #footer>
      <el-button @click="importDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="confirmImportFromOrder">导入</el-button>
    </template>
  </el-dialog>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.my-recipe-wrapper {
  display: contents;
}

.nordic-my-recipe {
  .nordic-page-container();
  max-width: 900px;
  margin: 0 auto;

  // --- 页面标题 ---
  .nordic-page-header {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    margin-bottom: @nordic-space-lg;

    h2 {
      font-size: @nordic-text-xl;
      font-weight: 700;
      color: @nordic-text;
      margin: 0;
      letter-spacing: -0.5px;
    }

    .recipe-count {
      font-size: @nordic-text-sm;
      color: @nordic-text-muted;
      margin-left: 8px;
    }
  }

  // --- 搜索与筛选 ---
  .toolbar-row {
    display: flex;
    gap: @nordic-space-md;
    align-items: center;
    margin-bottom: @nordic-space-md;
    flex-wrap: wrap;
  }

  .search-box {
    flex: 1;
    min-width: 200px;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 14px;
    background: @nordic-surface;
    border: 1px solid @nordic-border;
    border-radius: @nordic-radius-md;
    transition: border-color 0.2s;

    &:focus-within {
      border-color: @nordic-accent;
    }

    .search-icon {
      color: @nordic-text-muted;
      font-size: 14px;
    }

    .search-input {
      flex: 1;
      border: none;
      outline: none;
      background: transparent;
      font-size: @nordic-text-base;
      color: @nordic-text;

      &::placeholder {
        color: @nordic-text-muted;
      }
    }
  }

  .filter-chips {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }

  .meal-chip {
    padding: 6px 18px;
    border: 1px solid @nordic-border;
    background: @nordic-surface;
    border-radius: @nordic-radius-pill;
    font-size: @nordic-text-sm;
    color: @nordic-text-secondary;
    cursor: pointer;
    transition: all 0.2s;
    font-weight: 500;

    &:hover {
      border-color: @nordic-accent;
      color: @nordic-accent;
    }

    &.active {
      background: @nordic-accent;
      border-color: @nordic-accent;
      color: #fff;
    }
  }

  // --- 操作按钮栏 ---
  .action-bar {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: @nordic-space-lg;
    flex-wrap: wrap;

    .action-spacer {
      flex: 1;
    }
  }

  .nordic-btn {
    padding: 6px 16px;
    border: 1px solid @nordic-border;
    background: @nordic-surface;
    border-radius: @nordic-radius-md;
    font-size: @nordic-text-sm;
    color: @nordic-text-secondary;
    cursor: pointer;
    transition: all 0.2s;
    font-weight: 500;
    white-space: nowrap;

    &:hover:not(:disabled) {
      border-color: @nordic-text-secondary;
    }

    &.accent {
      background: @nordic-accent;
      border-color: @nordic-accent;
      color: #fff;

      &:hover {
        background: darken(@nordic-accent, 8%);
      }
    }

    &.ghost {
      border-color: transparent;
      color: @nordic-text-muted;

      &:hover:not(:disabled) {
        color: @nordic-text-secondary;
        border-color: @nordic-border;
      }
    }

    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
  }

  // --- 食谱列表 ---
  .recipe-list {
    display: flex;
    flex-direction: column;
    gap: @nordic-space-md;
  }

  // --- 空状态 ---
  .empty-state {
    text-align: center;
    padding: 60px 20px;
    background: @nordic-surface;
    border: 1px dashed @nordic-border;
    border-radius: @nordic-radius-lg;

    .empty-icon {
      font-size: 48px;
      margin-bottom: 16px;
      opacity: 0.6;
    }

    .empty-title {
      font-size: @nordic-text-lg;
      font-weight: 600;
      color: @nordic-text;
      margin-bottom: 8px;
    }

    .empty-desc {
      font-size: @nordic-text-sm;
      color: @nordic-text-muted;
    }
  }

  // --- 回到顶部 ---
  .scroll-top-btn {
    position: fixed;
    bottom: 30px;
    right: 30px;
    z-index: 1000;
    width: 36px;
    height: 36px;
    border: 1px solid @nordic-border;
    background: @nordic-surface;
    border-radius: @nordic-radius-sm;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: @nordic-text-secondary;
    box-shadow: 0 2px 8px @nordic-shadow;
    transition: all 0.2s;
    opacity: 0.8;

    &:hover {
      opacity: 1;
      border-color: @nordic-accent;
      color: @nordic-accent;
      transform: translateY(-2px);
    }
  }

  // --- 导入订单面板 ---
  .import-order-panel {
    padding: @nordic-space-lg;
    background: @nordic-bg;
    border-radius: @nordic-radius-md;

    .order-preview {
      margin-top: @nordic-space-md;
      padding: @nordic-space-md;
      background: @nordic-surface;
      border-radius: @nordic-radius-md;
      border: 1px solid @nordic-border;

      .order-meta {
        display: flex;
        justify-content: space-between;
        font-size: @nordic-text-sm;
        color: @nordic-text-secondary;
        margin-bottom: @nordic-space-sm;
      }

      .order-dishes {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
      }

      .dish-chip {
        padding: 4px 12px;
        background: @nordic-blue-light;
        color: @nordic-blue;
        border-radius: @nordic-radius-pill;
        font-size: @nordic-text-xs;
        font-weight: 500;
      }
    }

    .no-orders {
      text-align: center;
      padding: 32px;
      color: @nordic-text-muted;
      font-size: @nordic-text-sm;
    }
  }

  // --- 响应式 ---
  @media (max-width: 640px) {
    .toolbar-row {
      flex-direction: column;
      align-items: stretch;

      .search-box { width: 100%; }
    }

    .action-bar {
      flex-wrap: wrap;
      .action-spacer { display: none; }
    }
  }
}
</style>
