<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { API_CONFIG } from '../../config'
import axios from 'axios'
import { useAuthStore } from '../../store/authStore'

// 导入自定义组件
import RecipeDetail from '../../components/RecipeDetail.vue'
import AddDish from '../../components/AddDish.vue'
import ImportMerchantDish from '../../components/ImportMerchantDish.vue'
import AddRecipe from '../../components/recipe/AddRecipe.vue'
import ReplaceDish from '../../components/ReplaceDish.vue'
import RecipeCard from '../../components/RecipeCard.vue'

// 获取认证信息
const authStore = useAuthStore()

// 今日食谱数据
const todayRecipes = ref([])

// 批量操作相关变量
const selectedRecipes = ref([]) // 存储选中的食谱

// 添加菜品相关变量
const newDish = ref({
  name: '',
  ingredients: [],
  calories: 0,
  protein: 0,
  carbs: 0,
  fat: 0
})
const newIngredient = ref('') // 单个食材输入

// 营养摄入数据 - 从菜品数据计算得出
// TODO: 未来可以通过AI预测菜品营养数据，当前若菜品无数据则默认为0
const nutritionData = computed(() => {
  let totalCalories = 0
  let totalProtein = 0
  let totalCarbs = 0
  let totalFat = 0

  // 遍历筛选后的食谱和菜品，确保recipe和recipe.items存在
  filteredRecipes.value.forEach((recipe) => {
    if (recipe && recipe.items) {
      // 确保recipe.items是数组
      const items = Array.isArray(recipe.items)
        ? recipe.items
        : typeof recipe.items === 'string'
          ? JSON.parse(recipe.items)
          : []
      items.forEach((dish) => {
        // 如果菜品有营养数据则累加，否则默认为0
        totalCalories += dish?.calories || 0
        totalProtein += dish?.protein || 0
        totalCarbs += dish?.carbs || 0
        totalFat += dish?.fat || 0
      })
    }
  })

  return {
    calories: totalCalories,
    protein: totalProtein,
    carbs: totalCarbs,
    fat: totalFat
  }
})

// 筛选条件
const filters = ref({
  mealType: 'all'
})

// 加载今日食谱数据
const loadTodayRecipes = () => {
  // 确保有userId
  if (!authStore.userId) {
    console.error('加载今日食谱失败: 缺少userId')
    ElMessage.error('加载今日食谱失败: 用户未登录')
    todayRecipes.value = []
    return
  }

  axios
    .get(API_CONFIG.baseURL + API_CONFIG.recipe.today, {
      params: {
        userId: authStore.userId
      }
    })
    .then((response) => {
      console.log(response) ;
      if (
        response.data.data &&
        response.data.data.recipes &&
        response.data.data.recipes.length > 0
      ) {
        // console.log('加载今日食谱成功:', response.data.data.recipes);
        // 确保所有食谱都有items数组，并且移除任何null或无效的食谱
        todayRecipes.value = response.data.data.recipes
          .filter((recipe) => recipe && recipe.id) // 确保食谱存在且有id
          .map((recipe) => ({
            ...recipe,
            items: typeof recipe.items === 'string' ? JSON.parse(recipe.items) : recipe.items || []
          }))
        // nutritionData now computed from recipe items, no need for direct assignment
      } else {
        // 后端没有返回数据
        todayRecipes.value = []
      }
    })
    .catch((error) => {
      console.error('加载今日食谱失败:', error)
      // 请求失败时，也显示没有数据
      todayRecipes.value = []
      ElMessage.error('加载今日食谱失败')
    })
}

// 组件挂载时加载数据
onMounted(() => {
  loadTodayRecipes()
})

// 默认使用一列布局
const layoutType = ref('one-column')

// 模态框状态
const detailDialogVisible = ref(false)
const replaceDishVisible = ref(false)
const addDishVisible = ref(false)

// 当前选中的食谱和菜品
const selectedRecipe = ref(null)
const selectedDish = ref(null)

// 导入商家菜品对话框
const importMerchantDishVisible = ref(false)

// 订单列表 使用mock数据
const orders = ref([
  {
    id: 1,
    orderNo: 'ORDER_001',
    totalPrice: 89.5,
    dishes: [
      { name: '宫保鸡丁', nutrition: '250kcal/份' },
      { name: '清炒西兰花', nutrition: '120kcal/份' },
      { name: '米饭', nutrition: '110kcal/碗' }
    ]
  },
  {
    id: 2,
    orderNo: 'ORDER_002',
    totalPrice: 68.0,
    dishes: [
      { name: '番茄鸡蛋面', nutrition: '320kcal/份' },
      { name: '凉拌黄瓜', nutrition: '80kcal/份' }
    ]
  }
])

// 导入订单对话框
const importOrderVisible = ref(false)
const selectedOrder = ref(null)

// 添加食谱对话框
const addRecipeVisible = ref(false)

// 查看详情
const viewRecipeDetails = (recipe) => {
  selectedRecipe.value = recipe
  detailDialogVisible.value = true
}

// 统一处理食谱更新
const handleUpdateRecipe = (updatedRecipe) => {
  if (!updatedRecipe || !updatedRecipe.id) {
    console.error('更新食谱失败：无效的食谱数据')
    return
  }

  console.log('收到更新的食谱数据:', updatedRecipe)

  // 在todayRecipes数组中找到对应的食谱并更新
  const index = todayRecipes.value.findIndex((recipe) => recipe.id === updatedRecipe.id)
  if (index !== -1) {
    // 确保items字段正确解析
    const parsedRecipe = {
      ...updatedRecipe,
      items:
        typeof updatedRecipe.items === 'string'
          ? JSON.parse(updatedRecipe.items)
          : updatedRecipe.items || []
    }
    todayRecipes.value[index] = parsedRecipe

    // 更新selectedRecipe
    if (selectedRecipe.value && selectedRecipe.value.id === updatedRecipe.id) {
      selectedRecipe.value = parsedRecipe
    }

    console.log('食谱已在本地列表中更新')
  } else {
    console.warn('未找到对应的食谱:', updatedRecipe.id)
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
      const recipeIndex = todayRecipes.value.findIndex((r) => r.id === recipe.id)
      if (recipeIndex !== -1) {
        const updatedRecipe = {
          ...response.data.data,
          items:
            typeof response.data.data.items === 'string'
              ? JSON.parse(response.data.data.items)
              : response.data.data.items || []
        }
        todayRecipes.value[recipeIndex] = updatedRecipe
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

// 确认添加菜品（已迁移到组件）
// const confirmAddDish = () => {

// 确认从订单导入食谱
const confirmImportOrder = () => {
  if (selectedOrder.value) {
    console.log('Selected order:', selectedOrder.value)
    // 创建新食谱数据
    const newRecipeData = {
      name: `订单-${selectedOrder.value.orderNo}`,
      type: 'dinner', // 默认类型，可根据实际情况调整
      items: selectedOrder.value.dishes.map((dish) => ({
        name: dish.name,
        ingredients: [],
        calories: 0,
        protein: 0,
        carbs: 0,
        fat: 0
      })),
      userId: authStore.userId
    }

    // 调用后端API添加食谱 - 将items转换为JSON字符串
    const newRecipeDataWithStringItems = {
      ...newRecipeData,
      items: JSON.stringify(newRecipeData.items)
    }

    axios
      .post(API_CONFIG.baseURL + API_CONFIG.recipe.add, newRecipeDataWithStringItems)
      .then((response) => {
        // 检查返回的数据是否有效
        if (response.data.data) {
          // 确保返回的食谱有items数组并已解析
          const newRecipe = {
            ...response.data.data,
            items:
              typeof response.data.data.items === 'string'
                ? JSON.parse(response.data.data.items)
                : response.data.data.items || []
          }
          // 将返回的食谱添加到本地列表
          todayRecipes.value.push(newRecipe)
          ElMessage.success('订单已成功导入为新食谱')
          importOrderVisible.value = false
          selectedOrder.value = null
        } else {
          ElMessage.error('导入订单失败: 服务器返回无效数据')
        }
      })
      .catch((error) => {
        console.error('导入订单失败:', error)
        ElMessage.error('导入订单失败')
      })
  }
}

// 确认添加菜品
const confirmAddDish = () => {
  if (selectedRecipe.value && newDish.value.name.trim()) {
    // 验证菜品名称格式
    if (!isValidDishName(newDish.value.name)) {
      ElMessage.error('菜品名称只能包含中文、英文、数字和常见符号')
      return
    }

    // 如果有食材，将菜品和食材一起保存
    const dishWithIngredients = {
      name: newDish.value.name,
      ingredients: [...newDish.value.ingredients],
      calories: newDish.value.calories,
      protein: newDish.value.protein,
      carbs: newDish.value.carbs,
      fat: newDish.value.fat
    }

    // 先添加到本地
    selectedRecipe.value.items.push(dishWithIngredients)

    // 调用后端API更新食谱 - 将items转换为JSON字符串
    const updateData = {
      ...selectedRecipe.value,
      items: JSON.stringify(selectedRecipe.value.items)
    }

    axios
      .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + selectedRecipe.value.id, updateData)
      .then((response) => {
        // 更新本地数据 - 确保items字段已解析
        const recipeIndex = todayRecipes.value.findIndex((r) => r.id === selectedRecipe.value.id)
        if (recipeIndex !== -1) {
          // 确保返回的食谱有items数组并已解析
          const updatedRecipe = {
            ...response.data.data,
            items:
              typeof response.data.data.items === 'string'
                ? JSON.parse(response.data.data.items)
                : response.data.data.items || []
          }
          todayRecipes.value[recipeIndex] = updatedRecipe
        }

        ElMessage.success('菜品已添加')
        addDishVisible.value = false

        // 重置表单
        newDish.value = {
          name: '',
          ingredients: [],
          calories: 0,
          protein: 0,
          carbs: 0,
          fat: 0
        }
        newIngredient.value = ''

        selectedRecipe.value = null
      })
      .catch((error) => {
        console.error('添加菜品失败:', error)
        // 失败时恢复本地数据
        selectedRecipe.value.items.pop()
        ElMessage.error('添加菜品失败')
      })
  } else {
    ElMessage.error('请输入菜品名称')
  }
}

// 验证菜品名称格式的函数
const isValidDishName = (name) => {
  // 允许中文、英文、数字、空格以及常见的标点符号
  const nameRegex = /^[\u4e00-\u9fa5a-zA-Z0-9\s\-_\(\)\[\]\{\}\/\.\,，。！？；：]*$/
  return nameRegex.test(name.trim())
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
        const recipeIndex = todayRecipes.value.findIndex((r) => r.id === recipe.id)
        if (recipeIndex !== -1) {
          // 确保返回的食谱有items数组并已解析
          const updatedRecipe = {
            ...response.data.data,
            items:
              typeof response.data.data.items === 'string'
                ? JSON.parse(response.data.data.items)
                : response.data.data.items || []
          }
          todayRecipes.value[recipeIndex] = updatedRecipe
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
        ...recipe
      }

      axios
        .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + recipe.id, updateData)
        .then((response) => {
          // 更新本地数据
          const recipeIndex = todayRecipes.value.findIndex((r) => r.id === recipe.id)
          if (recipeIndex !== -1) {
            todayRecipes.value[recipeIndex] = response.data.data
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
            const index = todayRecipes.value.findIndex((r) => r.id === id)
            if (index !== -1) {
              todayRecipes.value.splice(index, 1)
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
          const recipeIndex = todayRecipes.value.findIndex((r) => r.id === recipe.id)
          if (recipeIndex !== -1) {
            // 确保返回的食谱有items数组并已解析
            const updatedRecipe = {
              ...response.data.data,
              items:
                typeof response.data.data.items === 'string'
                  ? JSON.parse(response.data.data.items)
                  : response.data.data.items || []
            }
            todayRecipes.value[recipeIndex] = updatedRecipe
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
        const recipeIndex = todayRecipes.value.findIndex((r) => r.id === updatedRecipe.id)
        if (recipeIndex !== -1) {
          // 确保返回的食谱有items数组并已解析
          const originalRecipe = todayRecipes.value[recipeIndex]
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
          todayRecipes.value[recipeIndex] = updatedRecipeWithParsedItems
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

// 添加新菜单
const addNewMenu = () => {
  // 这个函数已经被AddRecipe组件替换
}

// 处理从AddRecipe组件添加的新食谱
const handleAddRecipe = (newRecipeData) => {
  const authStore = useAuthStore()

  // 准备食谱数据 - 将type转换为小写
  const recipeData = {
    ...newRecipeData,
    type: newRecipeData.type.toLowerCase(), // 保持与系统一致的小写格式
    items: newRecipeData.ingredients || [], // 映射字段
    userId: authStore.userId || 0 // 使用当前用户ID
  }

  // 调用后端API添加食谱 - 将items转换为JSON字符串
  const recipeDataWithStringItems = {
    ...recipeData,
    items: JSON.stringify(recipeData.items)
  }

  axios
    .post(API_CONFIG.baseURL + API_CONFIG.recipe.add, recipeDataWithStringItems)
    .then((response) => {
      // 检查返回的数据是否有效
      if (response.data.data) {
        // 确保返回的食谱有items数组并已解析
        const newRecipe = {
          ...response.data.data,
          items:
            typeof response.data.data.items === 'string'
              ? JSON.parse(response.data.data.items)
              : response.data.data.items || [],
          isFavorite: response.data.data.favorite || false // 映射收藏字段
        }

        // 将返回的食谱添加到本地列表
        todayRecipes.value.push(newRecipe)
        ElMessage.success('食谱已添加')
      } else {
        ElMessage.error('添加食谱失败: 服务器返回无效数据')
      }
    })
    .catch((error) => {
      console.error('添加食谱失败:', error)
      ElMessage.error('添加食谱失败')
    })
}

// 单个食谱收藏/取消收藏
const toggleRecipeFavorite = (recipe) => {
  // 发送API请求切换收藏状态
  axios
    .put(API_CONFIG.baseURL + API_CONFIG.recipe.toggleFavorite + recipe.id, {})
    .then((response) => {
      console.log('切换收藏状态成功:', response)
      const updatedRecipe = response.data.data
      if (updatedRecipe && updatedRecipe.id) {
        // 更新本地数据 - 确保items字段已解析
        const recipeIndex = todayRecipes.value.findIndex((r) => r.id === updatedRecipe.id)
        if (recipeIndex !== -1) {
          // 确保返回的食谱有items数组并已解析
          const originalRecipe = todayRecipes.value[recipeIndex]
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
          todayRecipes.value[recipeIndex] = updatedRecipeWithParsedItems
        }
        // 显示提示
        if (updatedRecipe.isFavorite) {
          ElMessage.success('食谱已收藏')
        } else {
          ElMessage.success('食谱已取消收藏')
        }
      } else {
        // API请求失败，本地切换收藏状态
        recipe.isFavorite = !recipe.isFavorite
        // 显示提示
        if (recipe.isFavorite) {
          ElMessage.success('食谱已收藏')
        } else {
          ElMessage.success('食谱已取消收藏')
        }
      }
    })
    .catch((error) => {
      console.error('切换收藏状态失败:', error)
      // 网络错误时，本地切换收藏状态作为降级方案
      recipe.isFavorite = !recipe.isFavorite
      // 显示提示
      if (recipe.isFavorite) {
        ElMessage.warning('网络异常，已在本地标记为收藏')
      } else {
        ElMessage.warning('网络异常，已在本地取消收藏')
      }
    })
}

// 打开导入商家菜品对话框
const openImportMerchantDish = (recipe) => {
  selectedRecipe.value = recipe
  importMerchantDishVisible.value = true
}

// 切换卡片选中状态
const toggleCardSelection = (recipeId) => {
  const index = selectedRecipes.value.indexOf(recipeId)
  if (index > -1) {
    selectedRecipes.value.splice(index, 1)
  } else {
    selectedRecipes.value.push(recipeId)
  }
}

// 筛选后的食谱列表
const filteredRecipes = computed(() => {
  let filtered = [...todayRecipes.value]

  // 首先筛选掉null和没有id的食谱
  filtered = filtered.filter((recipe) => recipe && recipe.id)

  // 按收藏状态和修改时间排序：收藏的食谱置顶，然后按照修改时间从晚到早排序（更改越晚越靠前）
  filtered.sort((a, b) => {
    // 首先比较收藏状态，收藏的排前面
    if (a.isFavorite && !b.isFavorite) return -1
    if (!a.isFavorite && b.isFavorite) return 1

    // 如果收藏状态相同，比较修改时间（假设字段名为updateTime）
    // 这里需要根据实际字段名调整，如果没有则可以注释这部分
    const timeA = new Date(a.updateTime || 0)
    const timeB = new Date(b.updateTime || 0)
    return timeB - timeA // 从晚到早排序（最新修改的在最上面）
  })

  // 餐型筛选
  if (filters.value.mealType !== 'all') {
    if (filters.value.mealType === 'snack') {
      // 加餐包含所有零食类餐型
      filtered = filtered.filter(
        (recipe) =>
          recipe &&
          [
            // 再次确保recipe不为null
            'snack',
            'night_snack',
            'morning_snack',
            'afternoon_tea',
            'tea',
            'brunch',
            'midnight_snack'
          ].includes(recipe.type)
      )
    } else {
      filtered = filtered.filter((recipe) => recipe && recipe.type === filters.value.mealType)
    }
  }

  return filtered
})
</script>

<template>
  <div class="today-recipe-wrapper">
    <div class="nordic-today-recipe">
    <!-- 页面标题 -->
    <div class="nordic-page-header">
      <h2>今日食谱</h2>
    </div>

    <!-- 营养摄入概览 -->
    <div class="nutrition-overview">
      <div class="nutri-stat">
        <span class="nutri-val">{{ nutritionData.calories }}</span>
        <span class="nutri-unit">kcal</span>
        <span class="nutri-label">卡路里</span>
      </div>
      <div class="nutri-divider"></div>
      <div class="nutri-stat">
        <span class="nutri-val green">{{ nutritionData.protein }}</span>
        <span class="nutri-unit">g</span>
        <span class="nutri-label">蛋白质</span>
      </div>
      <div class="nutri-divider"></div>
      <div class="nutri-stat">
        <span class="nutri-val blue">{{ nutritionData.carbs }}</span>
        <span class="nutri-unit">g</span>
        <span class="nutri-label">碳水</span>
      </div>
      <div class="nutri-divider"></div>
      <div class="nutri-stat">
        <span class="nutri-val amber">{{ nutritionData.fat }}</span>
        <span class="nutri-unit">g</span>
        <span class="nutri-label">脂肪</span>
      </div>
    </div>

    <!-- 餐型筛选 -->
    <div class="meal-filter-row">
      <div class="meal-chips">
        <button
          class="meal-chip"
          :class="{ active: filters.mealType === 'all' }"
          @click="filters.mealType = 'all'"
        >全部</button>
        <button
          class="meal-chip"
          :class="{ active: filters.mealType === 'breakfast' }"
          @click="filters.mealType = 'breakfast'"
        >早餐</button>
        <button
          class="meal-chip"
          :class="{ active: filters.mealType === 'lunch' }"
          @click="filters.mealType = 'lunch'"
        >午餐</button>
        <button
          class="meal-chip"
          :class="{ active: filters.mealType === 'dinner' }"
          @click="filters.mealType = 'dinner'"
        >晚餐</button>
        <button
          class="meal-chip"
          :class="{ active: filters.mealType === 'snack' }"
          @click="filters.mealType = 'snack'"
        >加餐</button>
      </div>
      <div class="action-btns">
        <button class="nordic-btn accent" @click="addRecipeVisible = true">+ 添加食谱</button>
        <button class="nordic-btn" @click="importOrderVisible = true">从订单导入</button>
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
      </div>
    </div>

    <!-- 食谱列表 -->
    <div class="recipe-list" :class="layoutType">
      <div v-if="filteredRecipes.length === 0" class="empty-recipes">
        <el-empty description="今日没有食谱数据"></el-empty>
      </div>
      <div v-else class="recipe-cards">
        <RecipeCard
          v-for="recipe in filteredRecipes"
          :key="recipe.id"
          :recipe="recipe"
          :selectable="true"
          :selected-ids="selectedRecipes"
          @toggle-select="toggleCardSelection"
          @toggle-favorite="toggleRecipeFavorite"
          @view-details="viewRecipeDetails"
          @add-dish="addDish"
          @import-merchant-dish="openImportMerchantDish"
          @replace-dish="handleReplaceDishClick"
          @delete-dish="handleDeleteDishClick"
        />
      </div>
    </div>
  </div>

  <!-- 查看详情组件 -->
  <RecipeDetail
    v-model:visible="detailDialogVisible"
    :recipe="selectedRecipe"
    @close="selectedRecipe = null"
    @update:recipe="handleUpdateRecipe"
  ></RecipeDetail>

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

  <!-- 导入订单对话框 -->
  <el-dialog v-model="importOrderVisible" title="从订单导入食谱" width="600px" top="10%">
    <div class="import-order-panel">
      <el-select v-model="selectedOrder" placeholder="请选择要导入的订单" style="width: 100%">
        <el-option
          v-for="order in orders"
          :key="order.id"
          :label="`订单号: ${order.orderNo} - 总价: ${order.totalPrice}元`"
          :value="order"
        >
          <template #default>
            <div>
              <div style="margin-bottom: 6px;">订单号: {{ order.orderNo }} - 总价: {{ order.totalPrice }}元</div>
              <div style="display: flex; gap: 6px; flex-wrap: wrap;">
                <el-tag v-for="dish in order.dishes" :key="dish.name" size="small" type="info">
                  {{ dish.name }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-option>
      </el-select>
    </div>

    <template #footer>
      <el-button @click="importOrderVisible = false">取消</el-button>
      <el-button type="primary" @click="confirmImportOrder">导入为新食谱</el-button>
    </template>
  </el-dialog>

  <!-- 添加食谱对话框 -->
  <AddRecipe v-model:visible="addRecipeVisible" @add-recipe="handleAddRecipe" />
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.today-recipe-wrapper {
  display: contents;
}

.nordic-today-recipe {
  .nordic-page-container();
  max-width: 900px;
  margin: 0 auto;

  // --- 页面标题 ---
  .nordic-page-header {
    margin-bottom: @nordic-space-lg;

    h2 {
      font-size: @nordic-text-xl;
      font-weight: 700;
      color: @nordic-text;
      margin: 0;
      letter-spacing: -0.5px;
    }
  }

  // --- 营养概览 ---
  .nutrition-overview {
    display: flex;
    align-items: center;
    justify-content: space-around;
    padding: @nordic-space-lg;
    background: @nordic-surface;
    border-radius: @nordic-radius-lg;
    border: 1px solid @nordic-border;
    margin-bottom: @nordic-space-lg;
    .nordic-animate-fade();

    .nutri-stat {
      text-align: center;
      flex: 1;

      .nutri-val {
        display: block;
        font-size: @nordic-text-2xl;
        font-weight: 700;
        color: @nordic-accent;
        letter-spacing: -1px;
        line-height: 1;

        &.green { color: @nordic-green; }
        &.blue { color: @nordic-blue; }
        &.amber { color: @nordic-yellow; }
      }

      .nutri-unit {
        font-size: @nordic-text-xs;
        color: @nordic-text-muted;
      }

      .nutri-label {
        display: block;
        font-size: @nordic-text-xs;
        color: @nordic-text-muted;
        margin-top: 4px;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }
    }

    .nutri-divider {
      width: 1px;
      height: 40px;
      background: @nordic-border;
    }
  }

  // --- 筛选与操作 ---
  .meal-filter-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: @nordic-space-md;
    margin-bottom: @nordic-space-lg;
    flex-wrap: wrap;

    .meal-chips {
      display: flex;
      gap: 6px;
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

    .action-btns {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }

  // --- 北欧按钮 ---
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
    .recipe-cards {
      display: flex;
      flex-direction: column;
      gap: @nordic-space-md;
    }
  }

  // --- 空状态 ---
  .empty-recipes {
    text-align: center;
    padding: 60px 20px;
  }

  // --- 导入订单面板 ---
  .import-order-panel {
    padding: @nordic-space-lg;
    background: @nordic-bg;
    border-radius: @nordic-radius-md;
  }
}
</style>
