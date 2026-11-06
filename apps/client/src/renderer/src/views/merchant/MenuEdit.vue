<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElTimePicker, ElSelect, ElOption, ElInput, ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  CircleCheck,
  CirclePlus,
  CircleClose,
  Document,
  Grid,
  Clock,
  Switch,
  Edit
} from '@element-plus/icons-vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppCard from '../../components/common/AppCard.vue'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'
import { useAuthStore } from '../../store/authStore'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

// 菜单基本信息
const menuInfo = ref({})

// 菜单状态映射
const menuStatusMap = {
  online: { text: '上架中', type: 'success' },
  draft: { text: '草稿', type: 'warning' },
  offline: { text: '下架中', type: 'danger' }
}

// 加载状态
const loading = ref(false)

// 保存中状态（主按钮 loading，防止重复提交，§4.6）
const saving = ref(false)

// 菜品列表
const dishesList = ref([])

// 菜品状态映射（与DishManagement.vue保持一致）
const dishStatusMap = {
  online: { text: '🟢 在售', type: 'success' },
  almost_sold: { text: '🟡 即将售罄', type: 'warning' },
  offline: { text: '🔴 下架', type: 'danger' }
}

// 原始数据副本（用于比较是否有未保存的更改）
const originalMenuInfo = ref({})
const originalDishesList = ref([])

// 加载菜单数据的函数
const loadMenuData = async (menuId) => {
  loading.value = true
  if (!menuId) {
    ElMessage.error('无效的菜单ID')
    router.push('/merchant/home/menu')
    return
  }

  try {
    // 从authStore获取商家ID
    const authStore = useAuthStore()
    const merchantId = authStore.merchantId
    if (!merchantId) {
      ElMessage.error('未检测到商家ID，请重新登录')
      router.push('/merchant/login')
      return
    }

    // 获取菜单详情
    const menuResponse = await axios.get(
      `${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}/${menuId}`
    )
    if (menuResponse.data && menuResponse.data.success) {
      menuInfo.value = menuResponse.data.data
      // 格式化时间 - 处理后端返回的 LocalDateTime 格式
      if (menuInfo.value.autoOnline) {
        // 检查是否是完整的日期时间格式 (如: 2025-01-07T09:20:21)
        if (menuInfo.value.autoOnline.includes('T') || menuInfo.value.autoOnline.length > 8) {
          menuInfo.value.autoOnline = dayjs(menuInfo.value.autoOnline).format('HH:mm:ss')
        }
      }
      if (menuInfo.value.autoOffline) {
        if (menuInfo.value.autoOffline.includes('T') || menuInfo.value.autoOffline.length > 8) {
          menuInfo.value.autoOffline = dayjs(menuInfo.value.autoOffline).format('HH:mm:ss')
        }
      }
      // 保存菜单原始数据
      originalMenuInfo.value = JSON.parse(JSON.stringify(menuInfo.value))
    }

    // 获取所有菜品数据
    const dishesResponse = await axios.get(`${API_CONFIG.baseURL}${API_CONFIG.dish.list}`)
    if (dishesResponse.data && dishesResponse.data.success) {
      availableDishes.value = dishesResponse.data.data.map((dish) => ({
        ...dish,
        statusText: dishStatusMap[dish.status] ? dishStatusMap[dish.status].text : '🔴 下架',
        globalStatus: dish.status === 'online' // 保存菜品全局状态（true=上架，false=下架）
      }))
    }

    // 获取菜单关联的菜品
    const menuDishesResponse = await axios.get(
      `${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}/${menuId}/dishes`
    )
    console.log('菜单关联的菜品数据:', menuDishesResponse.data)
    if (menuDishesResponse.data && menuDishesResponse.data.success) {
      dishesList.value = menuDishesResponse.data.data.map((dish) => ({
        ...dish,
        statusText: dishStatusMap[dish.status] ? dishStatusMap[dish.status].text : '🔴 下架',
        globalStatus: dish.globalStatus // 保存菜品全局状态（true=上架，false=下架）
      }))
      console.log('解析后的菜品列表:', dishesList.value)
      // 保存菜品原始数据
      originalDishesList.value = JSON.parse(JSON.stringify(dishesList.value))
    }
  } catch (error) {
    console.error('加载菜单数据失败:', error)
    ElMessage.error('加载菜单数据失败')
    router.push('/merchant/home/menu')
  } finally {
    loading.value = false
  }
}

// 页面加载
onMounted(async () => {
  const menuId = route.query.menuId
  await loadMenuData(menuId)
})

// 监听路由变化，当menuId变化时重新加载数据
watch(
  () => route.query.menuId,
  (newMenuId) => {
    if (newMenuId) {
      loadMenuData(newMenuId)
    }
  }
)

// 检查是否有未保存的更改
const hasUnsavedChanges = () => {
  // 比较菜单基本信息
  if (JSON.stringify(menuInfo.value) !== JSON.stringify(originalMenuInfo.value)) {
    return true
  }

  // 比较菜品列表（数量和内容）
  if (dishesList.value.length !== originalDishesList.value.length) {
    return true
  }

  // 比较菜品详细信息
  for (let i = 0; i < dishesList.value.length; i++) {
    const currentDish = dishesList.value[i]
    const originalDish = originalDishesList.value.find((d) => d.id === currentDish.id)

    if (!originalDish) {
      return true
    }

    // 比较重要字段（id, status, 其他可能变更的字段）
    if (currentDish.status !== originalDish.status) {
      return true
    }
  }

  return false
}

// 保存菜单
const saveMenu = async (saveType) => {
  saving.value = true
  loading.value = true
  try {
    // 根据保存类型更新菜单状态
    switch (saveType) {
      case 'online':
        menuInfo.value.status = 'online'
        break
      case 'offline':
        menuInfo.value.status = 'offline'
        break
      case 'draft':
        menuInfo.value.status = 'draft'
        break
    }

    // 表单验证
    if (!menuInfo.value.name.trim()) {
      ElMessage.warning('请填写菜单名称')
      return
    }

    // 验证自动时间
    if (menuInfo.value.autoOnline && menuInfo.value.autoOffline) {
      const onlineTime = dayjs(menuInfo.value.autoOnline)
      const offlineTime = dayjs(menuInfo.value.autoOffline)
      if (offlineTime.isBefore(onlineTime)) {
        ElMessage.warning('自动下架时间必须晚于自动上架时间')
        return
      }
    }

    // 从authStore获取商家ID
    const authStore = useAuthStore()
    const merchantId = authStore.merchantId
    if (!merchantId) {
      ElMessage.error('未检测到商家ID，请重新登录')
      router.push('/merchant/login')
      return
    }

    // 从路由参数获取菜单ID
    const menuId = route.query.menuId
    if (!menuId) {
      ElMessage.error('无效的菜单ID')
      router.push('/merchant/home/menu')
      return
    }

    // 准备保存的数据
    const saveData = {
      ...menuInfo.value,
      dishes: dishesList.value.map((dish) => ({
        id: dish.id,
        status: dish.status === 'online' ? 1 : 0 // 将前端状态转换为后端格式：1-上架，0-下架
      }))
    }

    // 更新菜单
    const response = await axios.put(
      `${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}/${menuId}`,
      saveData
    )
    console.log('resonse data', response)
    ElMessage.success('菜单保存成功')

    // 跳回菜单管理页面
    router.push('/merchant/home/menu')
  } catch (error) {
    console.error('保存菜单失败:', error)
    ElMessage.error('保存菜单失败')
  } finally {
    loading.value = false
    saving.value = false
  }
}

// 移除菜品
const removeDish = (dish) => {
  const index = dishesList.value.findIndex((item) => item.id === dish.id)
  if (index !== -1) {
    dishesList.value.splice(index, 1)
    ElMessage.success('菜品已移除')
  }
}

// 切换菜品状态（上架/下架）
const toggleDishStatus = (dish) => {
  const newStatus = dish.status === 'online' ? 'offline' : 'online'
  const statusText = newStatus === 'online' ? '上架' : '下架'
  const statusInt = newStatus === 'online' ? 1 : 0

  // 检查是否要上架菜品，如果是，先检查全局状态
  if (newStatus === 'online') {
    // 检查菜品全局状态（true=上架，false=下架）
    if (!dish.globalStatus) {
      ElMessage.warning('该菜品未在菜品管理中上架，无法在菜单中上架')
      return
    }
  }

  ElMessageBox.confirm(`确定要将该菜品${statusText}吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 调用后端API更新菜品在特定菜单中的状态
      axios
        .put(`${API_CONFIG.baseURL}/v1/menus/menu/${route.query.menuId}/dishes/${dish.id}/status`, {
          status: statusInt
        })
        .then((response) => {
          if (response.data && response.data.code === '200') {
            dish.status = newStatus
            dish.statusText = dishStatusMap[newStatus].text
            ElMessage.success(`菜品已${statusText}`)
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

// 可用菜品数据
const availableDishes = ref([])

// 过滤后的可用菜品（过滤掉已在菜单中的菜品）
const filteredAvailableDishes = computed(() => {
  // 获取已在菜单中的菜品ID列表
  const existingDishIds = dishesList.value.map(dish => dish.id)
  return availableDishes.value.filter(dish => !existingDishIds.includes(dish.id))
})

// 添加菜品对话框
const showAddDishDialog = ref(false)
const selectedDishIds = ref([])

// 批量关联菜品对话框
const showBatchAssociateDialog = ref(false)
const selectedDishIdsBatch = ref([])

// 添加菜品
const addDish = async () => {
  if (selectedDishIds.value.length > 0) {
    let addedCount = 0
    selectedDishIds.value.forEach((dishId) => {
      const dish = availableDishes.value.find((d) => d.id === dishId)
      if (dish) {
        dishesList.value.push({ ...dish })
        addedCount++
      }
    })

    // 保存到后端
    const authStore = useAuthStore()
    const merchantId = authStore.merchantId
    try {
      const menuId = route.query.menuId

      const saveData = {
        ...menuInfo.value,
        dishes: dishesList.value.map((dish) => ({
          id: dish.id,
          status: dish.status === 'online' ? 1 : 0
        }))
      }

      await axios.put(
        `${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}/${menuId}`,
        saveData
      )

      ElMessage.success(`成功添加 ${addedCount} 个菜品`)
    } catch (error) {
      console.error('保存菜品失败:', error)
      ElMessage.error('保存菜品失败，请重试')
      // 恢复原始列表
      const menuId = route.query.menuId
      const menuDishesResponse = await axios.get(
        `${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}/${menuId}/dishes`
      )
      if (menuDishesResponse.data && menuDishesResponse.data.success) {
        dishesList.value = menuDishesResponse.data.data.map((dish) => ({
          ...dish,
          statusText: dishStatusMap[dish.status] ? dishStatusMap[dish.status].text : '🔴 下架',
          globalStatus: dish.globalStatus
        }))
      }
      return
    }

    // 重置状态
    showAddDishDialog.value = false
    selectedDishIds.value = []
  }
}

// 设置自动上架时间为当前时间 + 1小时
const setAutoOnlineTime = () => {
  menuInfo.value.autoOnline = dayjs().add(1, 'hour').format('HH:mm:ss')
}

// 设置自动下架时间为当前时间 + 3小时
const setAutoOfflineTime = () => {
  menuInfo.value.autoOffline = dayjs().add(3, 'hour').format('HH:mm:ss')
}

// 处理取消编辑
const handleCancelEdit = () => {
  // 检查是否有未保存的更改
  if (!hasUnsavedChanges()) {
    // 没有未保存的更改，直接返回
    router.back()
    return
  }

  // 有未保存的更改，显示提示框
  ElMessageBox.confirm('确定要取消编辑吗？未保存的更改将丢失。', '提示', {
    confirmButtonText: '确定取消',
    cancelButtonText: '继续编辑',
    type: 'warning'
  })
    .then(() => {
      router.back()
    })
    .catch(() => {
      // 用户取消了取消操作，继续编辑
    })
}

// 批量关联菜品
const batchAssociateDishes = async () => {
  if (selectedDishIdsBatch.value.length > 0) {
    let addedCount = 0
    let existingCount = 0

    selectedDishIdsBatch.value.forEach((dishId) => {
      const dish = availableDishes.value.find((d) => d.id === dishId)
      if (dish) {
        const isExist = dishesList.value.some((existingDish) => existingDish.id === dish.id)
        if (!isExist) {
          dishesList.value.push({ ...dish })
          addedCount++
        } else {
          existingCount++
        }
      }
    })

    // 保存到后端
    const authStore = useAuthStore()
    const merchantId = authStore.merchantId
    try {
      const menuId = route.query.menuId

      const saveData = {
        ...menuInfo.value,
        dishes: dishesList.value.map((dish) => ({
          id: dish.id,
          status: dish.status === 'online' ? 1 : 0
        }))
      }

      await axios.put(
        `${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}/${menuId}`,
        saveData
      )

      // 显示结果信息
      const messages = []
      if (addedCount > 0) messages.push(`${addedCount} 个菜品已成功关联`)
      if (existingCount > 0) messages.push(`${existingCount} 个菜品已在菜单中`)

      if (messages.length > 0) {
        ElMessage.success(messages.join('；'))
      }
    } catch (error) {
      console.error('保存菜品失败:', error)
      ElMessage.error('保存菜品失败，请重试')
      // 恢复原始列表
      const menuId = route.query.menuId
      const menuDishesResponse = await axios.get(
        `${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}/${menuId}/dishes`
      )
      if (menuDishesResponse.data && menuDishesResponse.data.success) {
        dishesList.value = menuDishesResponse.data.data.map((dish) => ({
          ...dish,
          statusText: dishStatusMap[dish.status] ? dishStatusMap[dish.status].text : '🔴 下架',
          globalStatus: dish.globalStatus
        }))
      }
      return
    }

    // 重置状态
    showBatchAssociateDialog.value = false
    selectedDishIdsBatch.value = []
  }
}
</script>

<template>
  <div class="menu-edit-container">
    <!-- 页头：merchant 紧凑版，标题 --text-h2 + 上下 --space-4，返回在右侧动作区（§4.7） -->
    <AppPageHeader title="编辑菜单" :compact="true">
      <template #actions>
        <CommonBackButton type="default" :use-router-back="false" @click="handleCancelEdit" />
      </template>
    </AppPageHeader>

    <div class="menu-edit-content">
      <!-- 菜单基本信息：两列栅格铺满卡片，收回右侧留白（§4.4） -->
      <AppCard class="menu-info-section" padding="md">
        <h2 class="section-title">菜单基本信息</h2>
        <div class="form-grid">
          <div class="form-field">
            <span class="form-label">
              <el-icon><Document /></el-icon>
              菜单名称
              <span class="form-required" aria-hidden="true">*</span>
            </span>
            <el-input
              v-model="menuInfo.name"
              size="default"
              placeholder="请输入菜单名称"
              clearable
            ></el-input>
          </div>

          <div class="form-field">
            <span class="form-label">
              <el-icon><Switch /></el-icon>
              菜单状态
            </span>
            <el-select
              v-model="menuInfo.status"
              size="default"
              placeholder="选择菜单状态"
              clearable
            >
              <el-option
                v-for="(status, key) in menuStatusMap"
                :key="key"
                :value="key"
                :label="status.text"
              >
                <template #label>
                  <el-icon>
                    <CircleCheck v-if="key === 'online'" />
                    <CirclePlus v-else-if="key === 'draft'" />
                    <CircleClose v-else />
                  </el-icon>
                  {{ status.text }}
                </template>
              </el-option>
            </el-select>
          </div>

          <div class="form-field">
            <span class="form-label">
              <el-icon><Clock /></el-icon>
              自动上架时间
            </span>
            <el-time-picker
              v-model="menuInfo.autoOnline"
              size="default"
              type="fixed-time"
              format="HH:mm:ss"
              value-format="HH:mm:ss"
              placeholder="选择自动上架时间"
            ></el-time-picker>
          </div>

          <div class="form-field">
            <span class="form-label">
              <el-icon><Clock /></el-icon>
              自动下架时间
            </span>
            <el-time-picker
              v-model="menuInfo.autoOffline"
              size="default"
              type="fixed-time"
              format="HH:mm:ss"
              value-format="HH:mm:ss"
              placeholder="选择自动下架时间"
            ></el-time-picker>
          </div>

          <div class="form-field form-field--full">
            <span class="form-label">
              <el-icon><Edit /></el-icon>
              菜单描述
            </span>
            <el-input
              v-model="menuInfo.description"
              size="default"
              placeholder="请输入菜单描述"
              type="textarea"
              :rows="4"
              clearable
            ></el-input>
          </div>
        </div>
      </AppCard>

      <!-- 菜品管理 -->
      <AppCard class="dishes-section" padding="md">
        <h2 class="section-title">菜品管理</h2>
        <div class="dishes-header">
          <el-button type="default" size="default" @click="showAddDishDialog = true">
            添加菜品
          </el-button>
          <el-button type="default" size="default" @click="showBatchAssociateDialog = true">
            批量关联菜品
          </el-button>
        </div>
        <div class="dishes-list">
          <div v-for="dish in dishesList" :key="dish.id" class="dish-item">
            <span class="dish-info">
              <span class="dish-name">{{ dish.name }}</span>
              <span class="dish-price">¥{{ dish.price }}</span>
              <el-tag
                :type="dishStatusMap[dish.status]?.type || 'danger'"
                size="small"
                class="status-tag"
              >
                {{ dish.statusText }}
              </el-tag>
            </span>
            <div class="dish-actions">
              <el-button
                type="default"
                size="default"
                class="status-btn"
                @click="toggleDishStatus(dish)"
                :disabled="dish.status === 'offline' && !dish.globalStatus"
              >
                {{ dish.status === 'online' ? '下架' : '上架' }}
              </el-button>
              <el-button type="danger" plain size="default" @click="removeDish(dish)">
                移除
              </el-button>
            </div>
          </div>
        </div>
      </AppCard>

      <!-- 保存动作：一个实心主按钮 + 次级选择（§4.1） -->
      <div class="action-buttons">
        <el-button type="primary" size="large" :loading="saving" @click="saveMenu('online')">
          保存菜单并上架
        </el-button>
        <el-dropdown trigger="click" @command="saveMenu">
          <el-button size="large">
            更多保存方式
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="offline">保存菜单并下架</el-dropdown-item>
              <el-dropdown-item command="draft">保存为草稿</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- 添加菜品对话框 -->
      <el-dialog
        v-model="showAddDishDialog"
        title="添加菜品"
        width="450px"
        center
        transition="dialog-fade"
      >
        <div class="dialog-content">
          <el-select
            v-model="selectedDishIds"
            multiple
            placeholder="请选择要添加的菜品"
            style="width: 100%"
            filterable
            clearable
            collapse-tags
            collapse-tags-tooltip
          >
            <el-option
              v-for="dish in filteredAvailableDishes"
              :key="dish.id"
              :label="`${dish.name} - ¥${dish.price} ${dish.statusText}`"
              :value="dish.id"
            />
          </el-select>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showAddDishDialog = false">取消</el-button>
            <el-button type="primary" @click="addDish">确定添加</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 批量关联菜品对话框 -->
      <el-dialog
        v-model="showBatchAssociateDialog"
        title="批量关联菜品"
        width="450px"
        center
        transition="dialog-fade"
      >
        <div class="dialog-content">
          <el-select
            v-model="selectedDishIdsBatch"
            multiple
            placeholder="请选择要关联的菜品"
            style="width: 100%"
            filterable
            clearable
            collapse-tags
            collapse-tags-tooltip
          >
            <el-option
              v-for="dish in filteredAvailableDishes"
              :key="dish.id"
              :label="`${dish.name} - ¥${dish.price} ${dish.statusText}`"
              :value="dish.id"
            />
          </el-select>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showBatchAssociateDialog = false">取消</el-button>
            <el-button type="primary" @click="batchAssociateDishes">确定关联</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<style scoped>
/* 页面容器：暖白页面底 + 内容区自适应（§3.1；侧栏 --sidebar-width 240px） */
.menu-edit-container {
  box-sizing: border-box;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

.menu-edit-content {
  display: flex;
  flex-direction: column;
  gap: var(--card-gap-compact);
}

/* 卡片：L1 微质感由 AppCard 承担，merchant 内边距取 --card-padding-compact（§3.4、§4.2） */
.menu-info-section,
.dishes-section {
  padding: var(--card-padding-compact);
}

/* 区块标题从 h2 起（页头已占用 h1），字号 --text-h3 / 600（§3.2、§4.7） */
.section-title {
  margin: 0 0 var(--space-4);
  font-size: var(--text-h3);
  line-height: 24px;
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

/* 表单两列栅格：铺满卡片，收回右侧留白（§4.4） */
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4) var(--space-6);
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  min-width: 0;
}

.form-field--full {
  grid-column: 1 / -1;
}

/* 字段标签：统一线性图标 + 文字（§4.4、§4.9） */
.form-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  color: var(--ink-700);
}

.form-label .el-icon {
  color: var(--ink-400);
}

/* 必填星号取 --danger（§4.4） */
.form-required {
  color: var(--danger);
}

/* 控件铺满栅格列；焦点环统一 --brand-400（§4.6、§7.1） */
.form-field :deep(.el-input),
.form-field :deep(.el-select),
.form-field :deep(.el-time-picker),
.form-field :deep(.el-textarea) {
  width: 100%;
}

.form-field :deep(.el-input__wrapper),
.form-field :deep(.el-textarea__inner) {
  border-radius: var(--radius-sm);
  transition:
    border-color var(--duration-fast) var(--ease-out),
    box-shadow var(--duration-fast) var(--ease-out);
}

.form-field :deep(.el-input__wrapper:hover),
.form-field :deep(.el-textarea__inner:hover) {
  border-color: var(--brand-400);
}

.form-field :deep(.el-input__wrapper.is-focus),
.form-field :deep(.el-textarea__inner:focus) {
  border-color: var(--brand-500);
  box-shadow: 0 0 0 2px var(--brand-400);
}

/* 卡片头动作：次级 + 文字层级，避免第二个实心主按钮（§4.1） */
.dishes-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.dishes-list {
  display: flex;
  flex-direction: column;
}

/* 菜品行：L0 平面嵌在卡片内，用分割线表达，不加阴影（§3.4） */
.dish-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--line);
}

.dish-item:last-child {
  border-bottom: 0;
}

.dish-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}

.dish-name {
  font-size: var(--text-body);
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

.dish-price {
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
  color: var(--ink-700);
}

.status-tag {
  margin-left: var(--space-1);
}

.dish-actions {
  display: flex;
  flex-shrink: 0;
  gap: var(--space-2);
}

.status-btn {
  min-width: 60px;
}

/* 保存动作区：主按钮在前，次级选择在后（§4.1） */
.action-buttons {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.dialog-footer {
  text-align: right;
}

/* 弹层：统一圆角与描边（§3.4） */
:deep(.el-dialog) {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid var(--line);
}

:deep(.el-dialog__title) {
  color: var(--ink-900);
  font-weight: var(--weight-semibold);
}

:deep(.el-dialog__footer) {
  border-top: 1px solid var(--line);
}
</style>
