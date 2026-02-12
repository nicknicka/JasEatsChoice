<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { API_CONFIG } from '../../config/index.js'
// 导入authStore
import { useAuthStore } from '../../store/authStore'
// 导入图标
import { Search, Plus, CircleCheck, CircleClose, Delete } from '@element-plus/icons-vue'
import {
  Goods as GoodsIcon,
  Money as MoneyIcon,
  Folder as FolderIcon,
  GoodsFilled as StockIcon,
  SwitchButton as StatusIcon,
  List as IngredientsIcon,
  Document as DetailsIcon,
  Warning as FlameIcon,
  Star as StarIcon,
  CirclePlus as CirclePlusIcon
} from '@element-plus/icons-vue'

// 创建路由实例
const router = useRouter()

// 菜品数据
const dishesList = ref([])

const loading = ref(false)
const searchKeyword = ref('')
const selectedDishes = ref([])

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const filteredDishes = ref([])
const paginatedDishes = ref([]) // 分页后的菜品数据

// 更新分页数据
const updatePagination = () => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  paginatedDishes.value = filteredDishes.value.slice(startIndex, endIndex)
}
// 三态全选复选框的状态：0=未选择，1=部分选择，2=全选
// const selectAllState = ref(0); // 不再需要这个状态变量，直接通过计算获得
// 页面加载时初始化
onMounted(() => {
  loading.value = true
  // 从authStore获取商家ID
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId
  if (!merchantId) {
    ElMessage.error('未检测到商家ID，请重新登录')
    loading.value = false
    return
  }

  // 从API获取菜品数据
  axios
    .get(`${API_CONFIG.baseURL}${API_CONFIG.dish.list}`, {
      params: {
        merchantId: merchantId
      }
    })
    .then((response) => {
      if (response.data && response.data.code === '200') {
        // 预处理菜品数据，确保所有菜品都有有效的状态和时间格式
        const processedDishes = response.data.data.map((dish) => {
          // 转换时间格式为 yyyy-MM-dd HH:mm:ss
          if (dish.createTime) {
            dish.createTime = new Date(dish.createTime).toLocaleString('zh-CN', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit'
            })
          }
          if (dish.updateTime) {
            dish.updateTime = new Date(dish.updateTime).toLocaleString('zh-CN', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit'
            })
          }

          // 确保price显示为正确的数字格式
          if (dish.price && typeof dish.price === 'object') {
            dish.price = dish.price.toString()
          }

          // 将布尔值的 status 转换为显示用的字符串
          if (typeof dish.status === 'boolean') {
            // 保持布尔值不变，用于判断
            dish.statusBoolean = dish.status
            dish.statusString = dish.status ? 'online' : 'offline'
          } else {
            // 如果已经是字符串，保持不变
            dish.statusBoolean = dish.status === 'online'
            dish.statusString = dish.status || 'offline'
          }

          // 处理库存空值情况
          if (dish.stock == null || dish.stock === '') {
            dish.stock = 0
          }

          // 优化分类字段显示
          if (dish.category && dish.category.startsWith('category_')) {
            // 将 category_1 转换为 分类1
            dish.category = `分类${dish.category.replace('category_', '')}`
          }

          // 处理食材数据 - 支持多种格式
          // 优先使用 ingredients 字段，如果没有则从 optionalIngredients 和 requiredIngredients 构建
          if (dish.ingredients && typeof dish.ingredients === 'string') {
            try {
              dish.ingredients = JSON.parse(dish.ingredients)
            } catch (error) {
              console.error('解析食材信息失败:', error)
              dish.ingredients = { mandatory: [], optional: [] }
            }
          } else if (!dish.ingredients || (typeof dish.ingredients === 'object' && !dish.ingredients.mandatory && !dish.ingredients.optional)) {
            // 如果没有 ingredients 对象，或者 ingredients 对象中没有 mandatory 和 optional 字段
            // 尝试从 optionalIngredients 和 requiredIngredients 构建兼容格式
            const mandatory = dish.requiredIngredients || []
            const optional = dish.optionalIngredients || []

            dish.ingredients = {
              mandatory: Array.isArray(mandatory) ? mandatory : [],
              optional: Array.isArray(optional) ? optional.map(item => {
                // 处理可选食材可能是字符串或对象的情况
                return typeof item === 'string' ? item : (item.name || String(item))
              }) : []
            }
          }

          // 确保 ingredients 有正确的结构
          if (!dish.ingredients) {
            dish.ingredients = { mandatory: [], optional: [] }
          }
          if (!dish.ingredients.mandatory) {
            dish.ingredients.mandatory = []
          }
          if (!dish.ingredients.optional) {
            dish.ingredients.optional = []
          }

          // 将后端的 calorie 字段映射到前端的 totalCalories 字段
          if (dish.calorie !== undefined) {
            dish.totalCalories = dish.calorie
          }

          return dish
        })
        dishesList.value = processedDishes
        filteredDishes.value = [...dishesList.value] // 更新筛选后的菜品
        updatePagination() // 初始化分页数据
      }
    })
    .catch((error) => {
      console.error('加载菜品失败:', error)
      ElMessage.error('加载菜品失败')
    })
    .finally(() => {
      loading.value = false
      // 滚动到页面顶部
      window.scrollTo({ top: 0, behavior: 'smooth' })
    })
})

// 筛选菜品 - 修复重复声明

// 更新筛选
const updateFilter = () => {
  filteredDishes.value = dishesList.value.filter((dish) => {
    // 搜索筛选
    if (
      searchKeyword.value &&
      !dish.name.includes(searchKeyword.value) &&
      !dish.category.includes(searchKeyword.value)
    ) {
      return false
    }

    return true
  })

  // 重置到第一页
  currentPage.value = 1

  // 更新分页数据
  updatePagination()
}

// 编辑菜品
const editDish = (dish) => {
  openEditDishDialog(dish)
}

// 保存编辑后的菜品
const saveEditedDish = () => {
  // 简单的表单验证
  if (!editDishForm.value.name.trim()) {
    ElMessage.warning('请填写菜品名称')
    return
  }

  // 准备请求数据，将 ingredients 对象序列化为 JSON 字符串，并将 totalCalories 映射为 calorie
  const requestData = {
    id: editDishForm.value.id,
    merchantId: editDishForm.value.merchantId,
    name: editDishForm.value.name,
    price: editDishForm.value.price,
    category: editDishForm.value.category,
    description: editDishForm.value.description,
    stock: editDishForm.value.stock || 0,
    calorie: editDishForm.value.totalCalories,
    image: editDishForm.value.image,
    ingredients: JSON.stringify(editDishForm.value.ingredients),
    status: editDishForm.value.statusString === 'online' // 将 statusString 转换为布尔值
  }

  // 发送后端请求
  axios
    .put(`${API_CONFIG.baseURL}${API_CONFIG.dish.detail}${requestData.id}`, requestData)
    .then((response) => {
      console.log('编辑菜品响应:', response)
      if (response.data && response.data.code === '200') {
        // 从后端返回中获取更新后的菜品数据
        let updatedDish = response.data.data

        // 处理后端返回的食材数据格式
        if (updatedDish) {
          // 处理 status 字段
          if (typeof updatedDish.status === 'boolean') {
            updatedDish.statusBoolean = updatedDish.status
            updatedDish.statusString = updatedDish.status ? 'online' : 'offline'
          }

          // 如果 ingredients 是字符串，解析它
          if (typeof updatedDish.ingredients === 'string') {
            try {
              updatedDish.ingredients = JSON.parse(updatedDish.ingredients)
            } catch (error) {
              console.error('解析食材信息失败:', error)
              updatedDish.ingredients = { mandatory: [], optional: [] }
            }
          }

          // 如果后端返回的是 optionalIngredients 和 requiredIngredients，转换为 ingredients 格式
          if (!updatedDish.ingredients || (typeof updatedDish.ingredients === 'object' && !updatedDish.ingredients.mandatory && !updatedDish.ingredients.optional)) {
            const mandatory = updatedDish.requiredIngredients || []
            const optional = updatedDish.optionalIngredients || []

            updatedDish.ingredients = {
              mandatory: Array.isArray(mandatory) ? mandatory : [],
              optional: Array.isArray(optional) ? optional.map(item => {
                return typeof item === 'string' ? item : (item.name || String(item))
              }) : []
            }
          }

          // 确保 ingredients 有正确的结构
          if (!updatedDish.ingredients) {
            updatedDish.ingredients = { mandatory: [], optional: [] }
          }
          if (!updatedDish.ingredients.mandatory) {
            updatedDish.ingredients.mandatory = []
          }
          if (!updatedDish.ingredients.optional) {
            updatedDish.ingredients.optional = []
          }

          // 映射 calorie 字段
          if (updatedDish.calorie !== undefined) {
            updatedDish.totalCalories = updatedDish.calorie
          }
        }

        // 更新本地菜品列表
        console.log('updatedDish.id:', updatedDish.id, '类型:', typeof updatedDish.id)
        console.log('dishesList中的菜品ID:', dishesList.value.map(item => ({ id: item.id, type: typeof item.id })))
        const index = dishesList.value.findIndex((item) => item.id === updatedDish.id)
        console.log('找到的索引:', index)
        if (index !== -1) {
          console.log('更新前菜品数据:', dishesList.value[index])
          console.log('更新后菜品数据:', updatedDish)
          dishesList.value[index] = updatedDish
          updateFilter()
        }
        // 关闭对话框并显示成功消息
        editDishDialogVisible.value = false
        ElMessage.success('菜品已更新')
      } else {
        ElMessage.error(response.data?.message || '菜品更新失败')
      }
    })
    .catch((error) => {
      console.error('更新菜品失败:', error)
      ElMessage.error('网络错误，菜品更新失败')
    })
}

// 切换菜品状态（上架/下架）
const toggleDishStatus = (dish) => {
  const currentStatus = dish.statusBoolean
  const newStatus = !currentStatus
  const statusText = newStatus ? '上架' : '下架'

  ElMessageBox.confirm(`确定要将该菜品${statusText}吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 调用后端API更新菜品状态
      axios
        .put(`${API_CONFIG.baseURL}${API_CONFIG.dish.status}/${dish.id}/status`, {
          status: newStatus
        })
        .then((response) => {
          console.log('更改菜品状态响应:', response)
          if (response.data && response.data.code === '200') {
            dish.statusBoolean = newStatus
            dish.statusString = newStatus ? 'online' : 'offline'
            ElMessage.success(`菜品已${statusText}`)

            // 当菜品下架时，同步更新该菜品在所有菜单中的状态为下架
            if (!newStatus) {
              axios
                .get(`${API_CONFIG.baseURL}/v1/menus/dishes/${dish.id}/menus`)
                .then((menuResponse) => {
                  if (menuResponse.data && menuResponse.data.code === '200') {
                    const menuIds = menuResponse.data.data.map((menu) => menu.id)
                    axios
                      .put(`${API_CONFIG.baseURL}/v1/menus/dishes/${dish.id}/status`, {
                        menuIds: menuIds,
                        status: 0 // 0 表示下架
                      })
                      .then((batchResponse) => {
                        if (batchResponse.data && batchResponse.data.code === '200') {
                          console.log('菜品在所有菜单中的状态已同步下架')
                        }
                      })
                      .catch((error) => {
                        console.error('同步菜品在菜单中的状态失败:', error)
                      })
                  }
                })
                .catch((error) => {
                  console.error('获取菜品关联菜单失败:', error)
                })
            }
          } else {
            ElMessage.error(response.data?.message || `菜品${statusText}失败`)
          }
        })
        .catch((error) => {
          console.error(`更新菜品状态失败:`, error)
          ElMessage.error(`网络错误，菜品${statusText}失败`)
        })
    })
    .catch(() => {
      ElMessage.info('已取消操作')
    })
}

// 菜单关联对话框状态
const menuAssociationDialogVisible = ref(false)
const currentDish = ref(null)
const associatedMenus = ref([])
const selectedStatus = ref(1)
const loadingMenus = ref(false)

// 根据菜单类型获取标签类型
const getMenuTypeTagType = (type) => {
  const typeMap = {
    早餐: 'warning',
    午餐: 'success',
    晚餐: 'info',
    夜宵: 'danger',
    套餐: 'primary'
  }
  return typeMap[type] || 'info'
}

// 查看菜单详情
const viewMenuDetails = (menu) => {
  // 跳转到菜单编辑页面，因为菜单编辑页面包含了菜单的详细信息
  router.push({
    name: 'merchant-menu-edit',
    query: { menuId: menu.id }
  })
  // 关闭当前对话框
  menuAssociationDialogVisible.value = false
}

// 显示菜单关联对话框
const showMenuAssociationDialog = (dish) => {
  currentDish.value = dish
  menuAssociationDialogVisible.value = true
  loadingMenus.value = true

  console.log('开始获取菜品关联菜单，菜品ID:', dish.id) // 新增调试日志

  // 调用API获取该菜品关联的所有菜单
  axios
    .get(`${API_CONFIG.baseURL}/v1/menus/dishes/${dish.id}/menus`)
    .then((response) => {
      console.log('获取菜单关联响应:', response) // 新增调试日志
      if (response.data && response.data.code === '200') {
        console.log('菜单关联数据:', response.data.data) // 新增调试日志
        associatedMenus.value = response.data.data.map((menu) => ({
          ...menu,
          dishStatus: menu.dish_status
        }))
        // 如果没有关联菜单，也显示提示信息
        if (response.data.data.length === 0) {
          console.log('该菜品尚未关联任何菜单')
        }
      } else {
        console.error('响应状态码错误:', response.data?.code)
        ElMessage.error('获取菜单关联失败')
      }
    })
    .catch((error) => {
      console.error('获取菜单关联失败:', error)
      ElMessage.error('获取菜单关联失败')
    })
    .finally(() => {
      loadingMenus.value = false
    })
}

// 更新菜品在特定菜单中的状态
const updateDishStatusInMenu = (menu) => {
  axios
    .put(`${API_CONFIG.baseURL}/v1/menus/menu/${menu.id}/dishes/${currentDish.value.id}/status`, {
      status: menu.dishStatus
    })
    .then((response) => {
      if (response.data && response.data.code === '200') {
        ElMessage.success('菜品状态更新成功')
      } else {
        ElMessage.error(response.data?.message || '菜品状态更新失败')
      }
    })
    .catch((error) => {
      console.error('更新菜品状态失败:', error)
      ElMessage.error('网络错误，菜品状态更新失败')
    })
}

// 批量更新菜品在所有菜单中的状态
const batchUpdateDishStatus = () => {
  const menuIds = associatedMenus.value.map((menu) => menu.id)
  axios
    .put(`${API_CONFIG.baseURL}/v1/menus/dishes/${currentDish.value.id}/status`, {
      menuIds: menuIds,
      status: selectedStatus.value
    })
    .then((response) => {
      if (response.data && response.data.code === '200') {
        ElMessage.success('批量更新成功')
        // 更新本地数据
        associatedMenus.value.forEach((menu) => {
          menu.dishStatus = selectedStatus.value
        })
      } else {
        ElMessage.error(response.data?.message || '批量更新失败')
      }
    })
    .catch((error) => {
      console.error('批量更新失败:', error)
      ElMessage.error('网络错误，批量更新失败')
    })
}

// 删除菜品
const deleteDish = (dish) => {
  ElMessageBox.confirm('确定要删除该菜品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 调用后端API删除菜品
      axios
        .delete(`${API_CONFIG.baseURL}${API_CONFIG.dish.detail}${dish.id}`)
        .then((response) => {
          if (response.data && response.data.code === '200') {
            const index = dishesList.value.findIndex((item) => item.id === dish.id)
            if (index !== -1) {
              dishesList.value.splice(index, 1)
              updateFilter()
              ElMessage.success('菜品已删除')
            }
          } else {
            ElMessage.error(response.data?.message || '删除菜品失败')
          }
        })
        .catch((error) => {
          console.error('删除菜品失败:', error)
          ElMessage.error('网络错误，删除菜品失败')
        })
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
}

// 批量操作
const batchOperation = (operation) => {
  if (selectedDishes.value.length === 0) {
    ElMessage.warning('请先选择菜品')
    return
  }

  // 批量操作确认对话框
  const getConfirmMessage = () => {
    switch (operation) {
      case 'online':
        return '确定要将所选菜品批量上架吗？'
      case 'offline':
        return '确定要将所选菜品批量下架吗？'
      case 'delete':
        return '确定要删除所选菜品吗？'
      default:
        return '确定要执行批量操作吗？'
    }
  }

  const getSuccessMessage = () => {
    switch (operation) {
      case 'online':
        return '批量上架成功'
      case 'offline':
        return '批量下架成功'
      case 'delete':
        return '批量删除成功'
      default:
        return '批量操作成功'
    }
  }

  // 显示确认对话框
  ElMessageBox.confirm(getConfirmMessage(), '操作确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 执行批量操作
      if (operation === 'online' || operation === 'offline') {
        const statusBoolean = operation === 'online' ? true : false
        const dishIds = selectedDishes.value.map((dish) => dish.id)

        // 调用后端API批量更新菜品状态
        axios
          .put(`${API_CONFIG.baseURL}${API_CONFIG.dish.batchStatus}`, {
            dishIds: dishIds,
            status: statusBoolean
          })
          .then((response) => {
            if (response.data && response.data.code === '200') {
              // 更新前端状态
              selectedDishes.value.forEach((dish) => {
                dish.statusBoolean = statusBoolean
                dish.statusString = statusBoolean ? 'online' : 'offline'
              })
              updateFilter()
              selectedDishes.value = []
              ElMessage.success(getSuccessMessage())
            } else {
              ElMessage.error(response.data?.message || '批量操作失败')
            }
          })
          .catch((error) => {
            console.error('批量更新菜品状态失败:', error)
            ElMessage.error('网络错误，批量操作失败')
          })
      } else if (operation === 'delete') {
        // 批量删除操作
        const dishIds = selectedDishes.value.map((dish) => dish.id)

        // 调用后端API批量删除菜品（后端不支持DELETE，使用PUT方法）
        axios
          .put(`${API_CONFIG.baseURL}${API_CONFIG.dish.batchDelete}`, {
            dishIds: dishIds
          })
          .then((response) => {
            if (response.data && response.data.code === '200') {
              // 从前端列表中删除已删除的菜品
              dishesList.value = dishesList.value.filter((dish) => !selectedDishes.value.includes(dish))
              updateFilter()
              selectedDishes.value = []
              ElMessage.success(getSuccessMessage())
            } else {
              ElMessage.error(response.data?.message || '批量删除失败')
            }
          })
          .catch((error) => {
            console.error('批量删除菜品失败:', error)
            ElMessage.error('网络错误，批量删除失败')
          })
      }
    })
    .catch(() => {
      // 用户取消操作
      ElMessage.info('已取消批量操作')
    })
}

// 新增菜品对话框
const addDishDialogVisible = ref(false)

// 食材数据已移除，改为直接输入

// 新必选食材输入
const newMandatoryIngredient = ref('')

// 新可选食材输入
const newOptionalIngredient = ref('')

// 新菜品表单数据
const newDish = ref({
  name: '',
  price: 0,
  category: '主食',
  stock: 100,
  status: 'online', // 默认上架
  ingredients: {
    mandatory: [], // 必选食材改为字符串数组
    optional: [] // 可选食材改为字符串数组
  },
  totalCalories: 0, // 总卡路里
  protein: 0, // 蛋白质
  fat: 0, // 脂肪
  carbs: 0 // 碳水化合物
})

// 添加必选食材
const addMandatoryIngredient = () => {
  if (newMandatoryIngredient.value.trim()) {
    const ingredient = newMandatoryIngredient.value.trim()
    // 检查重复
    if (!newDish.value.ingredients.mandatory.includes(ingredient)) {
      newDish.value.ingredients.mandatory.push(ingredient)
      newMandatoryIngredient.value = ''
      calculateTotalCalories()
    } else {
      ElMessage.warning('该必选食材已存在')
    }
  }
}

// 添加可选食材
const addOptionalIngredient = () => {
  if (newOptionalIngredient.value.trim()) {
    const ingredient = newOptionalIngredient.value.trim()
    // 检查重复
    if (!newDish.value.ingredients.optional.includes(ingredient)) {
      newDish.value.ingredients.optional.push(ingredient)
      newOptionalIngredient.value = ''
      calculateTotalCalories()
    } else {
      ElMessage.warning('该可选食材已存在')
    }
  }
}

// 删除必选食材
const removeMandatoryIngredient = (index) => {
  newDish.value.ingredients.mandatory.splice(index, 1)
  calculateTotalCalories()
}

// 删除可选食材
const removeOptionalIngredient = (index) => {
  newDish.value.ingredients.optional.splice(index, 1)
  calculateTotalCalories()
}

// 计算总卡路里
const calculateTotalCalories = () => {
  // 当食材是商家自定义时，保持当前输入的卡路里值不变
  // 如果需要自动计算，需要实现食材卡路里数据库匹配功能
  // 目前支持商家手动输入总卡路里值
}

// 编辑菜品对话框
const editDishDialogVisible = ref(false)

// 编辑菜品表单数据
const editDishForm = ref({
  ingredients: {
    mandatory: [], // 必选食材改为字符串数组
    optional: [] // 可选食材改为字符串数组
  },
  totalCalories: 0,
  protein: 0, // 蛋白质
  fat: 0, // 脂肪
  carbs: 0 // 碳水化合物
})

// 新必选食材输入（编辑时使用）
const editNewMandatoryIngredient = ref('')

// 新可选食材输入（编辑时使用）
const editNewOptionalIngredient = ref('')

// 添加必选食材（编辑时使用）
const editAddMandatoryIngredient = () => {
  if (editNewMandatoryIngredient.value.trim()) {
    const ingredient = editNewMandatoryIngredient.value.trim()
    // 检查重复
    if (!editDishForm.value.ingredients.mandatory.includes(ingredient)) {
      editDishForm.value.ingredients.mandatory.push(ingredient)
      editNewMandatoryIngredient.value = ''
      calculateEditTotalCalories()
    } else {
      ElMessage.warning('该必选食材已存在')
    }
  }
}

// 添加可选食材（编辑时使用）
const editAddOptionalIngredient = () => {
  if (editNewOptionalIngredient.value.trim()) {
    const ingredient = editNewOptionalIngredient.value.trim()
    // 检查重复
    if (!editDishForm.value.ingredients.optional.includes(ingredient)) {
      editDishForm.value.ingredients.optional.push(ingredient)
      editNewOptionalIngredient.value = ''
      calculateEditTotalCalories()
    } else {
      ElMessage.warning('该可选食材已存在')
    }
  }
}

// 删除必选食材（编辑时使用）
const editRemoveMandatoryIngredient = (index) => {
  editDishForm.value.ingredients.mandatory.splice(index, 1)
  calculateEditTotalCalories()
}

// 删除可选食材（编辑时使用）
const editRemoveOptionalIngredient = (index) => {
  editDishForm.value.ingredients.optional.splice(index, 1)
  calculateEditTotalCalories()
}

// 打开编辑菜品对话框
const openEditDishDialog = (dish) => {
  // 复制菜品数据到编辑表单，确保包含食材信息且为数组
  editDishForm.value = JSON.parse(
    JSON.stringify({
      ...dish,
      ingredients: {
        mandatory: Array.isArray(dish.ingredients?.mandatory) ? dish.ingredients.mandatory : [],
        optional: Array.isArray(dish.ingredients?.optional) ? dish.ingredients.optional : []
      },
      totalCalories: dish.totalCalories || 0,
      statusString: dish.statusString || (dish.statusBoolean ? 'online' : 'offline')
    })
  )

  editDishDialogVisible.value = true
}

// 计算编辑菜品的总卡路里
const calculateEditTotalCalories = () => {
  // 当食材是商家自定义时，保持当前输入的卡路里值不变
  // 如果需要自动计算，需要实现食材卡路里数据库匹配功能
  // 目前支持商家手动输入总卡路里值
}

// 打开添加菜品对话框
const openAddDishDialog = () => {
  addDishDialogVisible.value = true
}

// 保存新菜品
const saveNewDish = () => {
  // 简单的表单验证
  if (!newDish.value.name.trim()) {
    ElMessage.warning('请填写菜品名称')
    return
  }

  // 从authStore获取商家ID
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId
  if (!merchantId) {
    ElMessage.error('未检测到商家ID，请重新登录')
    return
  }

  // 准备请求数据，将 ingredients 对象序列化为 JSON 字符串，并将 totalCalories 映射为 calorie
  const requestData = {
    name: newDish.value.name,
    price: newDish.value.price,
    category: newDish.value.category,
    stock: newDish.value.stock || 100,
    ingredients: JSON.stringify(newDish.value.ingredients),
    calorie: newDish.value.totalCalories,
    merchantId,
    description: newDish.value.description,
    image: newDish.value.image,
    status: newDish.value.status === 'online' || newDish.value.status === true // 转换为布尔值，默认上架
  }

  // 发送后端请求
  axios
    .post(`${API_CONFIG.baseURL}${API_CONFIG.dish.list}`, requestData)
    .then((response) => {
      console.log('新增菜品响应:', response)
      if (response.data && response.data.code === '200') {
        let dishData = response.data.data // 获取后端返回的完整菜品数据

        // 处理后端返回的食材数据格式
        if (dishData) {
          // 处理 status 字段
          if (typeof dishData.status === 'boolean') {
            dishData.statusBoolean = dishData.status
            dishData.statusString = dishData.status ? 'online' : 'offline'
          }

          // 如果 ingredients 是字符串，解析它
          if (typeof dishData.ingredients === 'string') {
            try {
              dishData.ingredients = JSON.parse(dishData.ingredients)
            } catch (error) {
              console.error('解析食材信息失败:', error)
              dishData.ingredients = { mandatory: [], optional: [] }
            }
          }

          // 如果后端返回的是 optionalIngredients 和 requiredIngredients，转换为 ingredients 格式
          if (!dishData.ingredients || (typeof dishData.ingredients === 'object' && !dishData.ingredients.mandatory && !dishData.ingredients.optional)) {
            const mandatory = dishData.requiredIngredients || []
            const optional = dishData.optionalIngredients || []

            dishData.ingredients = {
              mandatory: Array.isArray(mandatory) ? mandatory : [],
              optional: Array.isArray(optional) ? optional.map(item => {
                return typeof item === 'string' ? item : (item.name || String(item))
              }) : []
            }
          }

          // 确保 ingredients 有正确的结构
          if (!dishData.ingredients) {
            dishData.ingredients = { mandatory: [], optional: [] }
          }
          if (!dishData.ingredients.mandatory) {
            dishData.ingredients.mandatory = []
          }
          if (!dishData.ingredients.optional) {
            dishData.ingredients.optional = []
          }

          // 映射 calorie 字段
          if (dishData.calorie !== undefined) {
            dishData.totalCalories = dishData.calorie
          }
        }

        dishesList.value.push(dishData)
        updateFilter()
        addDishDialogVisible.value = false
        ElMessage.success('菜品已添加')
      } else {
        ElMessage.error(response.data?.message || '菜品添加失败')
      }
    })
    .catch((error) => {
      console.error('添加菜品失败:', error)
      ElMessage.error('网络错误，菜品添加失败')
    })
}

// 选择/取消选择单个菜品
const toggleDishSelection = (dish) => {
  const index = selectedDishes.value.findIndex((item) => item.id === dish.id)

  if (index === -1) {
    selectedDishes.value.push(dish)
  } else {
    selectedDishes.value.splice(index, 1)
  }
  // console.log('dish',dish) ;
  // console.log('选择状态：', getSelectAllState());
  // console.log('已选择菜品：', selectedDishes.value);
}

// 全选/取消全选
const toggleSelectAll = () => {
  const currentState = getSelectAllState()

  if (currentState === 2) {
    // 当前是全选状态，点击后取消全选
    selectedDishes.value = []
  } else {
    // 当前是未选或部分选择状态，点击后全选
    selectedDishes.value = [...filteredDishes.value]
  }

  // 触发Vue的响应式更新
  selectedDishes.value = [...selectedDishes.value]

  // console.log('全选状态：', getSelectAllState());
  // console.log('已选择菜品：', selectedDishes.value);
}

// 检查全选状态
const getSelectAllState = () => {
  if (selectedDishes.value.length === 0) {
    return 0
  } else if (
    selectedDishes.value.length === filteredDishes.value.length &&
    filteredDishes.value.length > 0
  ) {
    // 已选择所有项目
    return 2
  } else {
    // 部分选择
    return 1
  }
}

// 监听filteredDishes变化，确保全选状态正确更新
watch(
  () => filteredDishes.value,
  () => {
    // 如果过滤后的菜品数量减少，且当前选中的菜品数量等于过滤前的数量，那么需要调整选中的菜品
    if (selectedDishes.value.length > filteredDishes.value.length) {
      // 只保留过滤后仍存在的菜品
      selectedDishes.value = selectedDishes.value.filter((selectedDish) =>
        filteredDishes.value.some((filteredDish) => filteredDish.id === selectedDish.id)
      )
    }
  }
)

// 获取单个菜品的选中状态
const getDishCheckedState = (dish) => {
  // 直接根据selectedDishes数组判断菜品是否被选中
  // console.log('getDishCheckedState selected',selectedDishes.value);
  // console.log('getDishCheckedState',dish);
  // console.log('getDishCheckedState checked', selectedDishes.value.some(item => item.id === dish.id));

  // 确保返回值是布尔类型
  const isChecked = selectedDishes.value.some((item) => item.id === dish.id)
  // console.log('getDishCheckedState final result:', isChecked);
  return isChecked
}
</script>

<template>
  <div class="dish-management-container">
    <div class="dish-header">
      <div class="header-left">
        <h3 class="page-title">【菜品管理】</h3>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="输入菜品名称或分类..."
          style="min-width: 200px; max-width: 300px; width: auto; flex: 1; margin-right: 12px"
          clearable
          @input="updateFilter"
        >
          <template #prefix>
            <el-icon style="color: #909399"><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" class="add-button" @click="openAddDishDialog">
          <el-icon><CirclePlus /></el-icon>
          新增菜品
        </el-button>
      </div>
    </div>

    <!-- 批量操作区域 -->
    <div v-if="filteredDishes.length > 0" class="batch-actions-section">
      <div class="batch-actions-bar">
        <div class="selection-info">
          <el-checkbox
            :indeterminate="getSelectAllState() === 1"
            :model-value="getSelectAllState() === 2"
            @change="toggleSelectAll"
            class="select-all-checkbox"
          >
            <span class="select-all-text">全选</span>
          </el-checkbox>
          <span v-if="selectedDishes.length > 0" class="selected-count">
            已选择 <strong>{{ selectedDishes.length }}</strong> / {{ filteredDishes.length }} 项
          </span>
        </div>

        <div class="batch-buttons">
          <el-button
            type="success"
            size="default"
            :disabled="selectedDishes.length === 0"
            class="batch-btn-success"
            @click="batchOperation('online')"
          >
            <el-icon><CircleCheck /></el-icon>
            批量上架
          </el-button>

          <el-button
            type="warning"
            size="default"
            :disabled="selectedDishes.length === 0"
            class="batch-btn-warning"
            @click="batchOperation('offline')"
          >
            <el-icon><CircleClose /></el-icon>
            批量下架
          </el-button>

          <el-button
            type="danger"
            size="default"
            :disabled="selectedDishes.length === 0"
            class="batch-btn-danger"
            @click="batchOperation('delete')"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
      </div>
    </div>

    <div class="dish-list">
      <div class="dish-list-container">
        <div v-for="dish in paginatedDishes" :key="dish.id" class="dish-item">
          <div class="dish-selection">
            <el-checkbox
              :model-value="getDishCheckedState(dish)"
              @change="toggleDishSelection(dish)"
            />
          </div>

          <div class="dish-content">
            <div class="dish-info">
              <div class="dish-name">
                <span class="name">{{ dish.name }}</span>
                <el-tag
                  :type="dish.statusBoolean ? 'success' : 'danger'"
                  size="small"
                  style="margin-left: 8px; font-size: 12px"
                >
                  {{ dish.statusBoolean ? '上架' : '下架' }}
                </el-tag>
              </div>

              <div class="dish-stats">
                <div class="stat-item">
                  <span class="stat-label">🍽️ 分类：</span>
                  <span class="stat-value">{{ dish.category }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">💰 价格：</span>
                  <span class="stat-value">¥{{ dish.price }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">📦 库存：</span>
                  <span class="stat-value">{{ dish.stock || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">⏰ 更新时间：</span>
                  <span class="stat-value">{{ dish.updateTime }}</span>
                </div>
              </div>

              <!-- 食材信息 -->
              <div v-if="dish.ingredients" class="dish-ingredients">
                <!-- 必选食材 -->
                <div class="ingredients-section">
                  <div class="ingredients-title">
                    <el-icon :size="14" color="#f56c6c"><StarIcon /></el-icon>
                    <span>必选食材</span>
                  </div>
                  <div v-if="dish.ingredients.mandatory && dish.ingredients.mandatory.length > 0" class="ingredients-tags">
                    <el-tag
                      v-for="(ingredient, index) in dish.ingredients.mandatory.slice(0, 5)"
                      :key="index"
                      type="danger"
                      effect="plain"
                      size="small"
                      class="ingredient-tag"
                    >
                      {{ ingredient }}
                    </el-tag>
                    <span v-if="dish.ingredients.mandatory.length > 5" class="more-ingredients">
                      +{{ dish.ingredients.mandatory.length - 5 }}
                    </span>
                  </div>
                  <div v-else class="no-ingredients">
                    <span class="no-ingredients-text">暂无必选食材</span>
                  </div>
                </div>

                <!-- 可选食材 -->
                <div class="ingredients-section">
                  <div class="ingredients-title">
                    <el-icon :size="14" color="#409eff"><CirclePlusIcon /></el-icon>
                    <span>可选食材</span>
                  </div>
                  <div v-if="dish.ingredients.optional && dish.ingredients.optional.length > 0" class="ingredients-tags">
                    <el-tag
                      v-for="(ingredient, index) in dish.ingredients.optional.slice(0, 5)"
                      :key="index"
                      type="primary"
                      effect="plain"
                      size="small"
                      class="ingredient-tag"
                    >
                      {{ ingredient }}
                    </el-tag>
                    <span v-if="dish.ingredients.optional.length > 5" class="more-ingredients">
                      +{{ dish.ingredients.optional.length - 5 }}
                    </span>
                  </div>
                  <div v-else class="no-ingredients">
                    <span class="no-ingredients-text">暂无可选食材</span>
                  </div>
                </div>
              </div>

              <!-- 营养成分信息 -->
              <div v-if="dish.totalCalories || dish.protein || dish.fat || dish.carbs" class="dish-nutrition">
                <div class="nutrition-title">
                  <el-icon :size="14" color="#52c41a"><FlameIcon /></el-icon>
                  <span>营养成分</span>
                </div>
                <div class="nutrition-tags">
                  <el-tag v-if="dish.totalCalories" type="warning" effect="plain" size="small" class="nutrition-tag">
                    🔥 {{ dish.totalCalories || 0 }} kcal
                  </el-tag>
                  <!-- <el-tag v-if="dish.protein" type="success" effect="plain" size="small" class="nutrition-tag">
                    🥩 蛋白质: {{ dish.protein }}g
                  </el-tag> -->
                  <!-- <el-tag v-if="dish.fat" type="danger" effect="plain" size="small" class="nutrition-tag">
                    💧 脂肪: {{ dish.fat }}g
                  </el-tag> -->
                  <!-- <el-tag v-if="dish.carbs" type="primary" effect="plain" size="small" class="nutrition-tag">
                    🌾 碳水: {{ dish.carbs }}g
                  </el-tag> -->
                </div>
              </div>
            </div>

            <div class="dish-actions">
              <el-button
                size="small"
                :class="{ 'btn-active': true }"
                @click="toggleDishStatus(dish)"
              >
                {{ dish.statusBoolean ? '下架' : '上架' }}
              </el-button>

              <el-button
                type="primary"
                size="small"
                :class="{ 'btn-active': true }"
                @click="editDish(dish)"
              >
                编辑
              </el-button>

              <el-button
                type="info"
                size="small"
                :class="{ 'btn-active': true }"
                @click="showMenuAssociationDialog(dish)"
              >
                菜单关联
              </el-button>

              <el-button
                type="warning"
                size="small"
                :class="{ 'btn-active': true }"
                @click="deleteDish(dish)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页组件 -->
    <div v-if="filteredDishes.length > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="filteredDishes.length"
        :page-sizes="[5, 10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="updatePagination"
        @current-change="updatePagination"
      />
    </div>

    <!-- 空数据提示 -->
    <el-empty v-if="filteredDishes.length === 0" description="暂无菜品">
      <template #bottom>
        <el-button type="primary" size="small" @click="addDishDialogVisible = true"
          >新增菜品</el-button
        >
      </template>
    </el-empty>

    <!-- 添加菜品对话框 -->
    <el-dialog
      v-model="addDishDialogVisible"
      title="添加新菜品"
      width="700px"
      center
      transition="dialog-fade"
    >
      <div class="add-dish-form">
        <el-form :model="newDish" label-width="120px" status-icon class="custom-form">
          <el-form-item label="名称" prop="name" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><GoodsIcon /></el-icon>
                <span>名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称</span>
              </div>
            </template>
            <el-input v-model="newDish.name" placeholder="例：宫保鸡丁" />
          </el-form-item>

          <el-form-item label="价格" prop="price" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><MoneyIcon /></el-icon>
                <span>价&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;格</span>
              </div>
            </template>
            <el-input v-model.number="newDish.price" placeholder="请输入价格" type="number" />
          </el-form-item>

          <el-form-item label="分类" prop="category" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><FolderIcon /></el-icon>
                <span>分&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;类</span>
              </div>
            </template>
            <el-select
              v-model="newDish.category"
              style="width: 100%"
              filterable
              allow-create
              default-first-option
              placeholder="请选择或输入分类"
            >
              <el-option label="主食" value="主食" />
              <el-option label="汤品" value="汤品" />
              <el-option label="饮料" value="饮料" />
              <el-option label="小吃" value="小吃" />
            </el-select>
          </el-form-item>

          <el-form-item label="库存" prop="stock" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StockIcon /></el-icon>
                <span>库&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;存</span>
              </div>
            </template>
            <el-input v-model.number="newDish.stock" placeholder="请输入库存" type="number" />
          </el-form-item>

          <el-form-item label="状态">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StatusIcon /></el-icon>
                <span>状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态</span>
              </div>
            </template>
            <el-select v-model="newDish.status" style="width: 100%">
              <el-option label="上架" value="online" />
              <el-option label="即将售罄" value="almost_sold" />
              <el-option label="下架" value="offline" />
            </el-select>
          </el-form-item>

          <!-- 必选食材 -->
          <el-form-item label="必选食材" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><IngredientsIcon /></el-icon>
                <span>必选食材</span>
              </div>
            </template>
            <div class="optional-ingredients-container">
              <div class="input-button-row">
                <el-input
                  v-model="newMandatoryIngredient"
                  placeholder="请输入必选食材"
                  clearable
                  style="width: calc(350px - 80px)"
                  @keyup.enter="addMandatoryIngredient"
                />
                <el-button
                  type="primary"
                  style="margin-left: 10px"
                  class="add-ingredient-btn"
                  @click="addMandatoryIngredient"
                >
                  添加
                </el-button>
              </div>
              <div class="ingredients-tags">
                <el-tag
                  v-for="(ingredient, index) in newDish.ingredients.mandatory"
                  :key="index"
                  type="warning"
                  closable
                  class="ingredient-tag"
                  @close="removeMandatoryIngredient(index)"
                >
                  {{ ingredient }}
                </el-tag>
              </div>
            </div>
          </el-form-item>

          <!-- 可选食材 -->
          <el-form-item label="可选食材">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><IngredientsIcon /></el-icon>
                <span>可选食材</span>
              </div>
            </template>
            <div class="optional-ingredients-container">
              <div class="input-button-row">
                <el-input
                  v-model="newOptionalIngredient"
                  placeholder="请输入可选食材"
                  clearable
                  style="width: calc(350px - 80px)"
                  @keyup.enter="addOptionalIngredient"
                />
                <el-button
                  type="primary"
                  style="margin-left: 10px"
                  class="add-ingredient-btn"
                  @click="addOptionalIngredient"
                >
                  添加
                </el-button>
              </div>
              <div class="ingredients-tags">
                <el-tag
                  v-for="(ingredient, index) in newDish.ingredients.optional"
                  :key="index"
                  type="success"
                  closable
                  class="ingredient-tag"
                  @close="removeOptionalIngredient(index)"
                >
                  {{ ingredient }}
                </el-tag>
              </div>
            </div>
          </el-form-item>

          <!-- 卡路里计算 -->
          <el-form-item label="总卡路里">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><FlameIcon /></el-icon>
                <span>总卡路里</span>
              </div>
            </template>
            <el-input
              v-model.number="newDish.totalCalories"
              placeholder="请输入总卡路里值"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>kcal</template>
            </el-input>
          </el-form-item>

          <!-- 营养成分 -->
          <el-form-item label="蛋白质">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StarIcon /></el-icon>
                <span>蛋白质</span>
              </div>
            </template>
            <el-input
              v-model.number="newDish.protein"
              placeholder="请输入蛋白质含量"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>g</template>
            </el-input>
          </el-form-item>

          <el-form-item label="脂肪">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StarIcon /></el-icon>
                <span>脂肪</span>
              </div>
            </template>
            <el-input
              v-model.number="newDish.fat"
              placeholder="请输入脂肪含量"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>g</template>
            </el-input>
          </el-form-item>

          <el-form-item label="碳水">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StarIcon /></el-icon>
                <span>碳水</span>
              </div>
            </template>
            <el-input
              v-model.number="newDish.carbs"
              placeholder="请输入碳水含量"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>g</template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveNewDish">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑菜品对话框 -->
    <el-dialog
      v-model="editDishDialogVisible"
      title="编辑菜品"
      width="700px"
      center
      transition="dialog-fade"
    >
      <div class="add-dish-form">
        <el-form :model="editDishForm" label-width="120px" status-icon class="custom-form">
          <el-form-item label="名称" prop="name" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><GoodsIcon /></el-icon>
                <span>名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称</span>
              </div>
            </template>
            <el-input v-model="editDishForm.name" placeholder="例：宫保鸡丁" />
          </el-form-item>

          <el-form-item label="价格" prop="price" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><MoneyIcon /></el-icon>
                <span>价&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;格</span>
              </div>
            </template>
            <el-input v-model.number="editDishForm.price" placeholder="请输入价格" type="number" />
          </el-form-item>

          <el-form-item label="分类" prop="category" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><FolderIcon /></el-icon>
                <span>分&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;类</span>
              </div>
            </template>
            <el-select
              v-model="editDishForm.category"
              style="width: 100%"
              filterable
              allow-create
              default-first-option
              placeholder="请选择或输入分类"
            >
              <el-option label="主食" value="主食" />
              <el-option label="汤品" value="汤品" />
              <el-option label="饮料" value="饮料" />
              <el-option label="小吃" value="小吃" />
            </el-select>
          </el-form-item>

          <el-form-item label="库存" prop="stock" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StockIcon /></el-icon>
                <span>库&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;存</span>
              </div>
            </template>
            <el-input v-model.number="editDishForm.stock" placeholder="请输入库存" type="number" />
          </el-form-item>

          <el-form-item label="状态">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StatusIcon /></el-icon>
                <span>状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态</span>
              </div>
            </template>
            <el-select v-model="editDishForm.statusString" style="width: 100%">
              <el-option label="上架" value="online" />
              <el-option label="即将售罄" value="almost_sold" />
              <el-option label="下架" value="offline" />
            </el-select>
          </el-form-item>

          <!-- 必选食材 -->
          <el-form-item label="必选食材">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><IngredientsIcon /></el-icon>
                <span>必选食材</span>
              </div>
            </template>
            <div class="optional-ingredients-container">
              <div class="input-button-row">
                <el-input
                  v-model="editNewMandatoryIngredient"
                  placeholder="请输入必选食材"
                  clearable
                  style="width: calc(350px - 80px)"
                  @keyup.enter="editAddMandatoryIngredient"
                />
                <el-button
                  type="primary"
                  style="margin-left: 10px"
                  class="add-ingredient-btn"
                  @click="editAddMandatoryIngredient"
                >
                  添加
                </el-button>
              </div>
              <div class="ingredients-tags">
                <el-tag
                  v-for="(ingredient, index) in editDishForm.ingredients.mandatory"
                  :key="index"
                  type="warning"
                  closable
                  class="ingredient-tag"
                  @close="editRemoveMandatoryIngredient(index)"
                >
                  {{ ingredient }}
                </el-tag>
              </div>
            </div>
          </el-form-item>

          <!-- 可选食材 -->
          <el-form-item label="可选食材">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><IngredientsIcon /></el-icon>
                <span>可选食材</span>
              </div>
            </template>
            <div class="optional-ingredients-container">
              <div class="input-button-row">
                <el-input
                  v-model="editNewOptionalIngredient"
                  placeholder="请输入可选食材"
                  clearable
                  style="width: calc(350px - 80px)"
                  @keyup.enter="editAddOptionalIngredient"
                />
                <el-button
                  type="primary"
                  style="margin-left: 10px"
                  class="add-ingredient-btn"
                  @click="editAddOptionalIngredient"
                >
                  添加
                </el-button>
              </div>
              <div class="ingredients-tags">
                <el-tag
                  v-for="(ingredient, index) in editDishForm.ingredients.optional"
                  :key="index"
                  type="success"
                  closable
                  class="ingredient-tag"
                  @close="editRemoveOptionalIngredient(index)"
                >
                  {{ ingredient }}
                </el-tag>
              </div>
            </div>
          </el-form-item>

          <!-- 卡路里计算 -->
          <el-form-item label="总卡路里">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><FlameIcon /></el-icon>
                <span>总卡路里</span>
              </div>
            </template>
            <el-input
              v-model.number="editDishForm.totalCalories"
              placeholder="请输入总卡路里值"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>kcal</template>
            </el-input>
            <div class="calorie-hint">
              <el-icon size="12"><Warning /></el-icon>
              <span>自定义食材请手动输入卡路里值</span>
            </div>
          </el-form-item>

          <!-- 营养成分 -->
          <el-form-item label="蛋白质">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StarIcon /></el-icon>
                <span>蛋白质</span>
              </div>
            </template>
            <el-input
              v-model.number="editDishForm.protein"
              placeholder="请输入蛋白质含量"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>g</template>
            </el-input>
          </el-form-item>

          <el-form-item label="脂肪">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StarIcon /></el-icon>
                <span>脂肪</span>
              </div>
            </template>
            <el-input
              v-model.number="editDishForm.fat"
              placeholder="请输入脂肪含量"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>g</template>
            </el-input>
          </el-form-item>

          <el-form-item label="碳水">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StarIcon /></el-icon>
                <span>碳水</span>
              </div>
            </template>
            <el-input
              v-model.number="editDishForm.carbs"
              placeholder="请输入碳水含量"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>g</template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEditedDish">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 菜单关联对话框 -->
    <el-dialog v-model="menuAssociationDialogVisible" title="菜品菜单关联管理" width="600px" center>
      <div class="menu-association-container">
        <div class="dialog-header">
          <h4>{{ currentDish?.name }} - 菜单关联状态</h4>
          <p class="dialog-description">管理该菜品在各个菜单中的上架/下架状态</p>
        </div>

        <div class="batch-operation">
          <el-select
            v-model="selectedStatus"
            placeholder="选择批量操作状态"
            style="width: 200px; margin-right: 10px"
          >
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
          <el-button type="primary" @click="batchUpdateDishStatus"> 批量应用到所有菜单 </el-button>
        </div>

        <el-table
          :data="associatedMenus"
          style="width: 100%; margin-top: 20px"
          border
          stripe
          v-loading="loadingMenus"
          empty-text="暂无关联菜单"
        >
          <el-table-column
            prop="name"
            label="菜单名称"
            min-width="180"
            show-overflow-tooltip
            align="center"
          >
            <template #default="{ row }">
              <div class="menu-info">
                <el-tag
                  v-if="row.type"
                  size="small"
                  style="margin-right: 8px; margin-bottom: 4px"
                  :type="getMenuTypeTagType(row.type)"
                >
                  {{ row.type }}
                </el-tag>
                <div class="menu-name">{{ row.name }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="menu_status" label="菜单状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="row.menu_status === 'active' ? 'success' : 'danger'"
                size="small"
                effect="light"
              >
                {{ row.menu_status === 'active' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="dishStatus" label="菜品状态" width="120" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.dishStatus"
                :active-value="1"
                :inactive-value="0"
                @change="updateDishStatusInMenu(row)"
                :active-text="`上架`"
                :inactive-text="`下架`"
              />
            </template>
          </el-table-column>

          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click="viewMenuDetails(row)">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="menuAssociationDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
/* 菜单关联对话框样式 */
.menu-association-container {
  .dialog-header {
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #f0f0f0;

    h4 {
      margin: 0 0 5px 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }

    .dialog-description {
      margin: 0;
      font-size: 14px;
      color: #909399;
    }
  }

  .batch-operation {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
  }

  :deep(.el-table) {
    .cell {
      padding: 12px 0;
    }

    // 表格头部样式
    .el-table__header-wrapper {
      .el-table__header {
        th {
          background-color: #f5f7fa;
          color: #606266;
          font-weight: 600;
          border-bottom: 1px solid #e8eaed;
        }
      }
    }

    // 表格行样式
    .el-table__body-wrapper {
      .el-table__body {
        tr {
          &:hover {
            td {
              background-color: #f5f7fa;
            }
          }

          &.el-table__row--striped {
            td {
              background-color: #fafbfc;
            }
          }
        }
      }
    }

    // 空数据样式
    .el-table__empty-block {
      .el-table__empty-text {
        color: #909399;
        font-size: 14px;
      }
    }
  }

  // 菜单信息样式
  .menu-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;

    .menu-name {
      font-weight: 500;
      color: #303133;
      margin-top: 4px;
    }
  }
}

.calorie-display {
  font-size: 16px;
  font-weight: 600;
  color: #e6a23c;
}

.calorie-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  background-color: #f5f7fa;
  padding: 6px 12px;
  border-radius: 6px;
  width: fit-content;
}

.optional-ingredients-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 350px;

  .input-button-row {
    display: flex;
    align-items: center;
  }

  .ingredients-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 8px;
  }
}

/* 表单容器 */
.add-dish-form {
  padding: 30px 0;
  max-width: 540px;
  margin: 0 auto;
}

/* 自定义Dialog样式 */
:deep(.el-dialog__header) {
  border-bottom: 2px solid rgba(102, 126, 234, 0.3);
  background: linear-gradient(135deg, rgba(230, 247, 255, 0.8) 0%, rgba(186, 231, 255, 0.8) 100%);
  padding: 24px 28px;
}

:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #1890ff;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:deep(.el-dialog__body) {
  padding: 32px 28px;
}

/* 表单字段样式 */
:deep(.el-form-item) {
  margin-bottom: 20px; /* 调整字段间距 */
}

/* 带图标的标签样式 */
.form-item-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label-icon {
  font-size: 18px;
  color: #667eea;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #555;
  font-size: 14px;
}

:deep(.el-form-item__label::before) {
  content: '';
  display: none; /* 隐藏原来的指示线 */
}

/* 输入框样式 */
:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  border-color: #91d5ff;
  box-shadow: 0 0 0 3px rgba(145, 213, 255, 0.1);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus),
:deep(.el-textarea__inner.is-focus) {
  border-color: #40a9ff;
  box-shadow: 0 0 0 3px rgba(64, 169, 255, 0.15);
}

/* 弹窗动画 */
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.dialog-fade-enter-from {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

.dialog-fade-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

/* 按钮样式 */
:deep(.dialog-footer) {
  text-align: center;
  padding: 0 28px 24px;
}

:deep(.dialog-footer .el-button) {
  padding: 10px 28px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.dialog-footer .el-button--primary) {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border: 1px solid #91d5ff;
  color: #0050b3;
}

:deep(.dialog-footer .el-button--primary:hover) {
  background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(64, 169, 255, 0.3);
}

:deep(.dialog-footer .el-button--default) {
  border-color: #e5e7eb;
  background-color: #fafafa;
  color: #666;
}

:deep(.dialog-footer .el-button--default:hover) {
  border-color: #d9d9d9;
  background-color: #f0f0f0;
  color: #333;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 添加食材按钮样式 */
.add-ingredient-btn {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border: 1px solid #91d5ff;
  color: #0050b3;
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(64, 169, 255, 0.2);
  transition: all 0.3s ease;

  &:hover {
    background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
    box-shadow: 0 4px 12px rgba(64, 169, 255, 0.3);
    transform: translateY(-1px);
  }
}

/* 食材标签样式 */
.ingredient-tag {
  border-radius: 8px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 500;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

.dish-management-container {
  padding: 24px;
  background-color: #fafbfc;
  min-height: 100vh;

  .dish-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding: 20px;
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);

    .page-title {
      font-size: 20px;
      font-weight: 700;
      margin: 0;
      color: #4a5568;
    }

    // 固定搜索和新增按钮区域的宽度范围，确保布局稳定
    .header-right {
      width: 55%; /* 占父容器55%宽度 */
      max-width: 550px; /* 最大宽度限制 */
      min-width: 350px; /* 最小宽度限制 */
      display: flex;
      align-items: center;
      gap: 10px; /* 统一内部元素间距 */
    }

    /* 小屏幕响应式调整 */
    @media (max-width: 767px) {
      flex-direction: column;
      gap: 16px;
      align-items: stretch;

      .header-right {
        width: 100% !important; /* 小屏幕下占满宽度 */
        min-width: auto !important; /* 取消最小宽度限制 */
        max-width: none !important; /* 取消最大宽度限制 */
        flex-direction: column;
        gap: 12px;
      }

      .el-input {
        min-width: auto;
        max-width: none;
      }
    }
  }

  .dish-filters {
    margin-bottom: 24px;

    .filter-section {
      display: flex;
      align-items: center;
      gap: 12px;

      .filter-label {
        font-weight: 500;
      }

      .status-filter {
        cursor: pointer;

        &:hover {
          opacity: 0.8;
        }
      }
    }
  }

  .dish-list {
    margin-bottom: 30px;

    /* List transition animations */
    .list-enter-active,
    .list-leave-active {
      transition: all 0.3s ease;
    }

    .list-enter-from,
    .list-leave-to {
      opacity: 0;
      transform: translateY(10px);
    }

    .dish-item {
      display: flex;
      align-items: flex-start;
      padding: 20px;
      border: none;
      border-radius: 16px;
      margin-bottom: 16px;
      background-color: #ffffff;
      transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(
          135deg,
          rgba(64, 169, 255, 0.08) 0%,
          rgba(145, 213, 255, 0.08) 100%
        );
        opacity: 0;
        transition: opacity 0.4s ease;
        z-index: -1;
        pointer-events: none;
      }

      &:hover {
        box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
        transform: translateY(-8px) scale(1.03);
        cursor: pointer;

        &::before {
          opacity: 1;
        }

        .dish-actions .el-button {
          transform: translateY(0);
          opacity: 1;
        }
      }

      .dish-content {
        flex: 1;
        position: relative;
        z-index: 1;
      }

      .dish-actions {
        display: flex;
        gap: 8px;

        .el-button {
          transform: translateY(5px);
          opacity: 0.9;
          transition: all 0.3s ease;
        }
      }

      .dish-selection {
        margin-top: 4px;
        margin-right: 16px;
      }

      .dish-content {
        flex: 1;
        display: flex;
        flex-direction: column;

        .dish-info {
          .dish-name {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 16px;

            .name {
              font-size: 18px;
              font-weight: 700;
              color: #2d3748;
            }
          }

          .dish-stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
            gap: 16px;
            margin-bottom: 20px;
            font-size: 14px;

            .stat-item {
              display: flex;
              flex-direction: column;
              gap: 4px;

              .stat-label {
                color: #718096;
                font-size: 12px;
                font-weight: 500;
              }

              .stat-value {
                color: #4a5568;
                font-size: 14px;
                font-weight: 600;
              }
            }
          }

          .dish-ingredients {
            margin-top: 16px;
            margin-bottom: 20px;
            padding-top: 16px;
            padding-bottom: 8px;
            border-top: 1px solid #f0f0f0;

            .ingredients-section {
              margin-bottom: 12px;

              &:last-child {
                margin-bottom: 0;
              }

              .ingredients-title {
                display: flex;
                align-items: center;
                gap: 6px;
                margin-bottom: 8px;
                font-size: 13px;
                font-weight: 600;
                color: #4a5568;
              }

              .ingredients-tags {
                display: flex;
                flex-wrap: wrap;
                gap: 6px;
                align-items: center;

                .ingredient-tag {
                  border-radius: 6px;
                  font-size: 12px;
                  padding: 2px 8px;
                  height: 24px;
                  line-height: 20px;
                }

                .more-ingredients {
                  font-size: 12px;
                  color: #909399;
                  font-weight: 500;
                  padding: 2px 6px;
                  background-color: #f5f7fa;
                  border-radius: 6px;
                }
              }

              .no-ingredients {
                padding: 8px 12px;
                background-color: #fafafa;
                border-radius: 6px;
                border: 1px dashed #e4e7ed;

                .no-ingredients-text {
                  font-size: 12px;
                  color: #909399;
                  font-weight: 400;
                }
              }
            }
          }
        }

        // 营养成分信息样式
        .dish-nutrition {
          margin-top: 16px;
          padding-top: 16px;
          padding-bottom: 8px;
          border-top: 1px solid #f0f0f0;

          .nutrition-title {
            display: flex;
            align-items: center;
            gap: 6px;
            margin-bottom: 12px;
            font-size: 13px;
            font-weight: 600;
            color: #4a5568;
          }

          .nutrition-tags {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            align-items: center;

            .nutrition-tag {
              border-radius: 6px;
              font-size: 12px;
              padding: 4px 10px;
              height: 26px;
              line-height: 18px;
              font-weight: 500;
            }
          }
        }

        .dish-actions {
          display: flex;
          flex-direction: row;
          gap: 10px;
          justify-content: flex-start;
          flex-wrap: wrap;

          button {
            width: 90px;
            border-radius: 10px;
            font-weight: 500;
            transition: all 0.3s ease;
            border: none;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

            &:hover {
              transform: translateY(-1px);
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
            }

            &:active {
              transform: translateY(0);
            }
          }

          .btn-active {
            background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
            color: #0050b3;

            &:hover {
              background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
            }
          }
        }
      }
    }
  }

  // 批量操作区域样式
  .batch-actions-section {
    margin-bottom: 20px;
    background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
    border-radius: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    border: 1px solid #e9ecef;
    overflow: hidden;

    .batch-actions-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 24px;
      gap: 20px;

      .selection-info {
        display: flex;
        align-items: center;
        gap: 16px;
        flex: 1;

        .select-all-checkbox {
          :deep(.el-checkbox__label) {
            .select-all-text {
              font-weight: 600;
              color: #4a5568;
              font-size: 14px;
            }
          }
        }

        .selected-count {
          display: inline-flex;
          align-items: center;
          padding: 6px 14px;
          background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
          border-radius: 20px;
          font-size: 13px;
          color: #0050b3;
          font-weight: 500;
          box-shadow: 0 2px 6px rgba(64, 169, 255, 0.15);

          strong {
            margin: 0 4px;
            font-size: 15px;
            color: #1890ff;
          }
        }
      }

      .batch-buttons {
        display: flex;
        gap: 10px;
        align-items: center;
      }
    }
  }

  // 批量操作按钮样式
  .batch-btn-success,
  .batch-btn-warning,
  .batch-btn-danger {
    border-radius: 10px;
    font-weight: 600;
    padding: 10px 20px;
    transition: all 0.3s ease;
    border: none;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    display: inline-flex;
    align-items: center;
    gap: 6px;

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    &:active:not(:disabled) {
      transform: translateY(0);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
      transform: none !important;
    }

    .el-icon {
      font-size: 16px;
    }
  }

  .batch-btn-success {
    background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
    color: #ffffff;

    &:hover:not(:disabled) {
      background: linear-gradient(135deg, #73d13d 0%, #95de64 100%);
      box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
    }
  }

  .batch-btn-warning {
    background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
    color: #ffffff;

    &:hover:not(:disabled) {
      background: linear-gradient(135deg, #ffc53d 0%, #ffd666 100%);
      box-shadow: 0 4px 12px rgba(250, 173, 20, 0.3);
    }
  }

  .batch-btn-danger {
    background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
    color: #ffffff;

    &:hover:not(:disabled) {
      background: linear-gradient(135deg, #ff7875 0%, #ffa39e 100%);
      box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
    }
  }

  // 旧的批量操作样式（保留兼容性）
  .batch-actions {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px 20px;
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);

    .select-all {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      color: #4a5568;
    }
  }

  // 分页容器样式
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
    padding: 16px;
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  }

  .dialog-footer {
    text-align: right;
  }

  .add-button {
    background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
    border: none;
    border-radius: 10px;
    padding: 10px 20px;
    font-weight: 600;
    color: #389e0d;
    box-shadow: 0 2px 8px rgba(56, 158, 13, 0.2);
    transition: all 0.3s ease;

    &:hover {
      background: linear-gradient(135deg, #d9f7be 0%, #b7eb8f 100%);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(56, 158, 13, 0.3);
    }

    &:active {
      transform: translateY(0);
    }
  }

  .batch-btn {
    border-radius: 10px;
    font-weight: 500;
    transition: all 0.3s ease;
    border: none;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

    &:hover:not(:disabled) {
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
    }

    &:active:not(:disabled) {
      transform: translateY(0);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }

  // 响应式布局
  @media (max-width: 768px) {
    .batch-actions-section {
      .batch-actions-bar {
        flex-direction: column;
        align-items: stretch;
        padding: 16px;

        .selection-info {
          justify-content: space-between;
          margin-bottom: 12px;
        }

        .batch-buttons {
          flex-direction: column;
          width: 100%;

          .el-button {
            width: 100%;
            justify-content: center;
          }
        }
      }
    }
  }
}
</style>
