<script setup>
import { ref, onMounted, watch } from 'vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

// 扩展dayjs的相对时间功能
dayjs.extend(relativeTime)
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  ArrowUp,
  Edit,
  Delete,
  Download,
  Check,
  CirclePlus,
  CircleCheck,
  CircleClose,
  InfoFilled,
  Clock,
  Food,
  Document,
  Grid,
  Switch,
  Search
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'
// 导入authStore
import { useAuthStore } from '../../store/authStore'

const router = useRouter()
// 菜单状态映射
const menuStatusMap = {
  online: { text: '上架中', type: 'success' },
  draft: { text: '草稿', type: 'warning' },
  offline: { text: '下架中', type: 'danger' }
}

// 菜单数据
const menuList = ref([])

const loading = ref(false)
const searchKeyword = ref('')
const activeStatusFilter = ref('all')

// 筛选菜单
const filteredMenus = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const paginatedMenus = ref([])

// 排序相关
const sortField = ref('updateTime') // 默认按更新时间排序
const sortOrder = ref('desc') // 'asc' 或 'desc'

// 更新排序
const updateSorting = () => {
  filteredMenus.value.sort((a, b) => {
    // 获取排序字段的值，兼容 name 和 menuName 两种字段名
    const aValue = sortField.value === 'menuName' ? (a.menuName || a.name || '') : a[sortField.value]
    const bValue = sortField.value === 'menuName' ? (b.menuName || b.name || '') : b[sortField.value]

    if (aValue < bValue) {
      return sortOrder.value === 'asc' ? -1 : 1
    }
    if (aValue > bValue) {
      return sortOrder.value === 'asc' ? 1 : -1
    }
    return 0
  })
}

// 更新分页
const updatePagination = () => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  paginatedMenus.value = filteredMenus.value.slice(start, end)
}

// 页面加载时初始化
onMounted(() => {
  loading.value = true
  // 从authStore获取商家ID
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId
  if (!merchantId) {
    ElMessage.error('未检测到商家ID，请重新登录')
    return
  }
  // 从API获取菜单数据
  axios
    .get(`${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}`)
    .then((response) => {
      console.log('菜单数据响应:', response.data)
      if (response.data && response.data.success) {
        // 预处理菜单数据，统一字段名
        menuList.value = response.data.data.map((menu) => ({
          ...menu,
          // 如果没有 name 字段，使用 menuName 作为 name
          name: menu.name || menu.menuName || '',
          menuName: menu.menuName || menu.name || ''
        }))
        console.log('解析后的菜单列表:', menuList.value)
        console.log('第一个菜单数据:', menuList.value[0])
        filteredMenus.value = [...menuList.value] // 更新筛选后的菜单
        updatePagination() // 更新分页
      }
    })
    .catch((error) => {
      console.error('加载菜单失败:', error)
      ElMessage.error('加载菜单失败')
    })
    .finally(() => {
      loading.value = false
      // 滚动到页面顶部
      window.scrollTo({ top: 0, behavior: 'smooth' })
    })
})

// 更新筛选
const updateFilter = () => {
  filteredMenus.value = menuList.value.filter((menu) => {
    // 状态筛选
    if (activeStatusFilter.value !== 'all' && menu.status !== activeStatusFilter.value) {
      return false
    }

    // 搜索筛选
    if (searchKeyword.value) {
      const lowerCaseKeyword = searchKeyword.value.toLowerCase()
      const lowerCaseMenuName = (menu.name || menu.menuName || '').toLowerCase()
      if (!lowerCaseMenuName.includes(lowerCaseKeyword)) {
        return false
      }
    }

    return true
  })
  // 更新排序
  updateSorting()
  // 更新分页
  currentPage.value = 1 // 筛选后回到第一页
  updatePagination()
}

// 监听filteredMenus变化，确保全选状态正确更新
watch(
  () => filteredMenus.value,
  () => {
    // 如果过滤后的菜单数量减少，且当前选中的菜单数量等于过滤前的数量，那么需要调整选中的菜单
    if (selectedMenus.value.length > filteredMenus.value.length) {
      selectedMenus.value = selectedMenus.value.filter((menu) => filteredMenus.value.includes(menu))
    }
  },
  { deep: true }
)

// 切换状态并同步到后端
const toggleMenuStatus = (menu) => {
  let newStatus = menu.status === 'online' ? 'offline' : 'online'

  // 从authStore获取商家ID
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId

  axios
    .put(
      `${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}/${menu.id}`,
      { status: newStatus }
    )
    .then((response) => {
      if (response.data && response.data.success) {
        menu.status = newStatus
        updateFilter()
        ElMessage.success(`菜单已${menuStatusMap[newStatus].text}`)
      } else {
        ElMessage.error('更新菜单状态失败: ' + (response.data.message || '未知错误'))
      }
    })
    .catch((error) => {
      console.error('更新菜单状态失败:', error)
      ElMessage.error('更新菜单状态失败')
    })
}

// 编辑菜单
const editMenu = (menu) => {
  console.log('编辑菜单:', menu)
  // 导航到菜单编辑页面并传递菜单ID
  router.push({ path: '/merchant/home/menu-edit', query: { menuId: menu.id } })
}

// 删除菜单
const deleteMenu = (menu) => {
  ElMessageBox.confirm('确定要删除该菜单？删除后不可恢复！', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'danger'
  })
    .then(() => {
      const index = menuList.value.findIndex((item) => item.id === menu.id)
      if (index !== -1) {
        menuList.value.splice(index, 1)
        updateFilter()
        ElMessage.success('菜单已删除')
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
}

// 批量操作
const selectedMenus = ref([])

const batchOperation = (operation) => {
  if (selectedMenus.value.length === 0) {
    ElMessage.warning('请先选择菜单')
    return
  }

  switch (operation) {
    case 'online':
      selectedMenus.value.forEach((menu) => {
        menu.status = 'online'
      })
      ElMessage.success('批量上架成功')
      break
    case 'offline':
      selectedMenus.value.forEach((menu) => {
        menu.status = 'offline'
      })
      ElMessage.success('批量下架成功')
      break
    case 'delete':
      ElMessageBox.confirm('确定要删除选中的所有菜单？删除后不可恢复！', '警告', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'danger'
      })
        .then(() => {
          menuList.value = menuList.value.filter((menu) => !selectedMenus.value.includes(menu))
          selectedMenus.value = []
          updateFilter()
          ElMessage.success('批量删除成功')
        })
        .catch(() => {})
      return
  }

  updateFilter()
  selectedMenus.value = []
}

// 导出菜单为CSV格式
const exportMenu = (menu) => {
  // 构建CSV内容
  const csvContent = [
    // 表头
    ['菜单名称', '状态', '菜品数量', '更新时间', '自动上架时间', '自动下架时间'].join(','),
    // 数据行
    [
      menu.name,
      menuStatusMap[menu.status].text,
      Array.isArray(menu.dishes) ? menu.dishes.length : menu.dishes || 0,
      dayjs(menu.updateTime).format('YYYY-MM-DD HH:mm:ss'),
      menu.autoOnline
        ? menu.autoOnline.includes('T') || menu.autoOnline.length > 8
          ? dayjs(menu.autoOnline).format('HH:mm')
          : dayjs(menu.autoOnline, 'HH:mm:ss').format('HH:mm')
        : '',
      menu.autoOffline
        ? menu.autoOffline.includes('T') || menu.autoOffline.length > 8
          ? dayjs(menu.autoOffline).format('HH:mm')
          : dayjs(menu.autoOffline, 'HH:mm:ss').format('HH:mm')
        : ''
    ]
      .map((item) => `"${item}"`)
      .join(',') // 转义包含逗号或引号的内容
  ].join('\n')

  // 创建Blob对象
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })

  // 创建下载链接
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `${menu.name}_${dayjs().format('YYYYMMDDHHmmss')}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)

  ElMessage.success('菜单导出成功')
}

// 选择/取消选择单个菜单
const toggleMenuSelection = (menu) => {
  const index = selectedMenus.value.findIndex((item) => item.id === menu.id)

  if (index === -1) {
    selectedMenus.value.push(menu)
  } else {
    selectedMenus.value.splice(index, 1)
  }
}

// 新增菜单对话框
const addMenuDialogVisible = ref(false)

// 新菜单表单数据
const newMenu = ref({
  name: '',
  category: '',
  autoOnline: '',
  autoOffline: '',
  status: 'online'
})

// 打开添加菜单对话框
const openAddMenuDialog = () => {
  addMenuDialogVisible.value = true

  // 重置表单数据
  newMenu.value = {
    name: '',
    category: '',
    autoOnline: '',
    autoOffline: '',
    status: 'online'
  }
}

// 保存新菜单
const saveNewMenu = () => {
  // 简单的表单验证
  if (!newMenu.value.name.trim()) {
    ElMessage.warning('请填写菜单名称')
    return
  }

  // 从authStore获取商家ID
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId
  if (!merchantId) {
    ElMessage.error('未检测到商家ID，请重新登录')
    router.push('/merchant/login')
    return
  }

  // 准备请求参数
  const menuData = {
    name: newMenu.value.name,
    category: newMenu.value.category,
    status: newMenu.value.status,
    autoOnline: newMenu.value.autoOnline,
    autoOffline: newMenu.value.autoOffline
  }

  // 发送POST请求到后端保存菜单
  axios
    .post(
      `${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}`,
      menuData
    )
    .then((response) => {
      console.log('保存菜单响应:', response)
      if (response.data && response.data.success) {
        // 从响应中获取完整的菜单对象
        const savedMenu = response.data.data

        // 添加到菜单列表
        menuList.value.push(savedMenu)
        updateFilter()
        addMenuDialogVisible.value = false
        ElMessage.success('菜单已添加')
      } else {
        ElMessage.error(`保存菜单失败: ${response.data.message || '未知错误'}`)
      }
    })
    .catch((error) => {
      console.error('保存菜单失败:', error)
      ElMessage.error('保存菜单失败')
    })
}

// 检查全选状态：0=未选择，1=部分选择，2=全选
const getSelectAllState = () => {
  if (selectedMenus.value.length === 0) {
    return 0
  } else if (
    selectedMenus.value.length === filteredMenus.value.length &&
    filteredMenus.value.length > 0
  ) {
    return 2
  } else {
    return 1
  }
}

// 全选/取消全选
const toggleSelectAll = () => {
  const currentState = getSelectAllState()

  if (currentState === 2) {
    // 当前是全选状态，点击后取消全选
    selectedMenus.value = []
  } else {
    // 当前是未选或部分选择状态，点击后全选
    selectedMenus.value = [...filteredMenus.value]
  }

  // 触发Vue的响应式更新
  selectedMenus.value = [...selectedMenus.value]
}
</script>

<template>
  <div class="menu-management-container">
    <div class="menu-header">
      <div class="header-left">
        <h3 class="page-title">【菜单管理】</h3>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="输入菜单名称/关键词搜索..."
          style="min-width: 200px; max-width: 300px; width: auto; flex: 1; margin-right: 12px"
          clearable
          @input="updateFilter"
        >
          <template #prefix>
            <el-icon style="color: #909399"><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="openAddMenuDialog" class="add-button">
          <el-icon><CirclePlus /></el-icon>
          新增菜单
        </el-button>
      </div>
    </div>

    <!-- 筛选面板：整合状态筛选和排序 -->
    <div class="filter-panel">
      <div class="menu-filters">
        <div class="filter-section">
          <span class="filter-label">状态筛选：</span>
          <el-tag
            v-for="status in ['all', 'online', 'draft', 'offline']"
            :key="status"
            :type="activeStatusFilter === status ? 'primary' : 'info'"
            effect="plain"
            @click="
              () => {
                activeStatusFilter = status
                updateFilter()
              }
            "
            class="status-filter"
          >
            <el-icon v-if="status !== 'all' && status === 'online'"><CircleCheck /></el-icon>
            <el-icon v-if="status !== 'all' && status === 'draft'"><CirclePlus /></el-icon>
            <el-icon v-if="status !== 'all' && status === 'offline'"><CircleClose /></el-icon>
            {{ status === 'all' ? '全部菜单' : menuStatusMap[status].text }}
          </el-tag>
        </div>

        <div class="filter-section">
          <span class="filter-label">排序：</span>
          <el-select
            v-model="sortField"
            style="width: 120px; margin-right: 8px"
            @change="updateFilter"
          >
            <el-option label="更新时间" value="updateTime" />
            <el-option label="菜单名称" value="menuName" />
          </el-select>

          <el-select v-model="sortOrder" style="width: 100px" @change="updateFilter">
            <el-option label="升序" value="asc" />
            <el-option label="降序" value="desc" />
          </el-select>
          <el-tooltip content="按所选字段升序或降序排列菜单" placement="top">
            <el-icon style="color: #909399; margin-left: 4px; cursor: help"><InfoFilled /></el-icon>
          </el-tooltip>
        </div>
      </div>
    </div>

    <!-- 将批量操作和分页放在同一个容器中 -->
    <div class="batch-pagination-container" v-if="filteredMenus.length > 0">
      <div class="batch-actions">
        <span class="select-all">
          <el-checkbox
            :indeterminate="getSelectAllState() === 1"
            :model-value="getSelectAllState() === 2"
            @change="toggleSelectAll"
          />
          全选
          <span
            class="selected-count"
            :style="{ visibility: selectedMenus.length > 0 ? 'visible' : 'hidden' }"
          >
            ({{ selectedMenus.length }}/{{ filteredMenus.length }})
          </span>
        </span>

        <el-button
          type="success"
          size="small"
          @click="batchOperation('online')"
          :disabled="selectedMenus.length === 0"
          class="batch-btn"
        >
          <el-icon><CircleCheck /></el-icon>
          批量上架
        </el-button>

        <el-button
          type="warning"
          size="small"
          @click="batchOperation('offline')"
          :disabled="selectedMenus.length === 0"
          class="batch-btn"
        >
          <el-icon><CircleClose /></el-icon>
          批量下架
        </el-button>

        <el-button
          type="danger"
          size="small"
          @click="batchOperation('delete')"
          :disabled="selectedMenus.length === 0"
          class="batch-btn"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>

      <!-- 分页 -->
      <div class="menu-pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredMenus.length"
          @size-change="updatePagination"
          @current-change="updatePagination"
        />
      </div>
    </div>

    <div class="menu-list">
      <div class="menu-list-container">
        <div class="menu-item" v-for="menu in paginatedMenus" :key="menu.id">
          <div class="menu-selection">
            <el-checkbox
              :model-value="selectedMenus.includes(menu)"
              @change="toggleMenuSelection(menu)"
            />
          </div>

          <div class="menu-content">
            <div class="menu-info">
              <div class="menu-name">
                <span class="name">{{ menu.name || menu.menuName || '未命名菜单' }}</span>
                <!-- 菜单类型标签，不同类型不同样式 -->
                <el-tag
                  :type="
                    menu.category === '早餐'
                      ? 'success'
                      : menu.category === '午餐'
                        ? 'primary'
                        : menu.category === '晚餐'
                          ? 'warning'
                          : menu.category === '加餐'
                            ? 'info'
                            : 'info'
                  "
                  size="small"
                  style="margin-right: 8px"
                >
                  {{ menu.category }}
                </el-tag>
                <!-- 状态信息，参考状态筛选样式 -->
                <el-tag
                  :type="menuStatusMap[menu.status].type"
                  effect="plain"
                  class="menu-status-tag"
                >
                  <el-icon v-if="menu.status === 'online'"><CircleCheck /></el-icon>
                  <el-icon v-if="menu.status === 'draft'"><CirclePlus /></el-icon>
                  <el-icon v-if="menu.status === 'offline'"><CircleClose /></el-icon>
                  {{ menuStatusMap[menu.status].text }}
                </el-tag>
              </div>

              <!-- 菜品列表显示为标签，至多两行，超出省略并提示 -->
              <div class="menu-dishes">
                <el-tooltip
                  v-if="menu.dishes && Array.isArray(menu.dishes) && menu.dishes.length > 0"
                  :content="menu.dishes.map((dish) => dish.name || dish).join(', ')"
                  placement="top"
                >
                  <div class="dishes-tags-container">
                    <el-tag v-for="(dish, index) in menu.dishes" :key="index" size="small">
                      {{ dish.name || dish }}
                    </el-tag>
                  </div>
                </el-tooltip>
                <el-tooltip
                  v-else-if="menu.dishes && typeof menu.dishes === 'object' && menu.dishes.name"
                  :content="menu.dishes.name"
                  placement="top"
                >
                  <div class="dishes-tags-container">
                    <el-tag size="small">{{ menu.dishes.name }}</el-tag>
                  </div>
                </el-tooltip>
                <!-- 兼容旧数据结构（菜品数量） -->
                <div
                  v-else-if="menu.dishes && typeof menu.dishes === 'number'"
                  class="dishes-count"
                >
                  <el-icon><Food /></el-icon> {{ menu.dishes }} 菜品
                </div>
                <!-- 当 dishes 是空数组时显示 0 菜品 -->
                <div
                  v-else-if="menu.dishes && Array.isArray(menu.dishes) && menu.dishes.length === 0"
                  class="dishes-count"
                >
                  <el-icon><Food /></el-icon> 0 菜品
                </div>
                <!-- 无菜品时的显示 -->
                <div v-else class="dishes-count">
                  <el-icon><Food /></el-icon> 0 菜品
                </div>
              </div>

              <div class="menu-stats">
                <span class="update-time">
                  <el-icon><Clock /></el-icon>
                  更新时间：{{ dayjs(menu.updateTime).format('YYYY年MM月DD日 HH:mm:ss') }}
                </span>
              </div>

              <div class="auto-times">
                <span v-if="menu.autoOnline" class="auto-online">
                  <el-icon><Clock /></el-icon>
                  自动上架：{{
                    menu.autoOnline.includes('T') || menu.autoOnline.length > 8
                      ? dayjs(menu.autoOnline).format('HH:mm')
                      : dayjs(menu.autoOnline, 'HH:mm:ss').format('HH:mm')
                  }}
                </span>
                <span v-if="menu.autoOffline" class="auto-offline">
                  <el-icon><Clock /></el-icon>
                  自动下架：{{
                    menu.autoOffline.includes('T') || menu.autoOffline.length > 8
                      ? dayjs(menu.autoOffline).format('HH:mm')
                      : dayjs(menu.autoOffline, 'HH:mm:ss').format('HH:mm')
                  }}
                </span>
              </div>
            </div>

            <div class="menu-actions">
              <el-button
                :type="menu.status === 'online' ? 'warning' : 'success'"
                size="small"
                @click="toggleMenuStatus(menu)"
              >
                <el-icon v-if="menu.status === 'online'"><CircleClose /></el-icon>
                <el-icon v-else><CircleCheck /></el-icon>
                {{ menu.status === 'online' ? '下架菜单' : '上架菜单' }}
              </el-button>

              <el-button type="primary" size="small" @click="editMenu(menu)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>

              <el-button type="danger" size="small" @click="deleteMenu(menu)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>

              <el-button type="info" size="small" @click="exportMenu(menu)">
                <el-icon><Download /></el-icon>
                导出菜单
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空数据提示 -->
    <el-empty v-if="filteredMenus.length === 0" description="您还没有创建任何菜单~">
      <template #bottom>
        <el-button type="primary" size="small" @click="addMenuDialogVisible = true"
          >新增菜单</el-button
        >
      </template>
    </el-empty>

    <!-- 添加菜单对话框 -->
    <el-dialog v-model="addMenuDialogVisible" title="添加菜单" width="600px" top="10%">
      <div class="add-menu-form">
        <div class="info-item">
          <span class="info-label"
            ><el-icon><Document /></el-icon> 菜单名称</span
          >
          <el-input
            v-model="newMenu.name"
            placeholder="请输入菜单名称"
            style="width: 300px"
            clearable
          ></el-input>
        </div>
        <div class="info-item">
          <span class="info-label"
            ><el-icon><Grid /></el-icon> 菜单分类</span
          >
          <el-select
            v-model="newMenu.category"
            placeholder="选择菜单分类"
            style="width: 200px"
            clearable
          >
            <el-option label="早餐" value="早餐" />
            <el-option label="午餐" value="午餐" />
            <el-option label="晚餐" value="晚餐" />
            <el-option label="加餐" value="加餐" />
          </el-select>
        </div>
        <div class="info-item">
          <span class="info-label"
            ><el-icon><Clock /></el-icon> 自动上架时间</span
          >
          <el-time-picker
            v-model="newMenu.autoOnline"
            type="fixed-time"
            format="HH:mm:ss"
            value-format="HH:mm:ss"
            placeholder="选择自动上架时间"
            style="width: 200px"
          ></el-time-picker>
        </div>
        <div class="info-item">
          <span class="info-label"
            ><el-icon><Clock /></el-icon> 自动下架时间</span
          >
          <el-time-picker
            v-model="newMenu.autoOffline"
            type="fixed-time"
            format="HH:mm:ss"
            value-format="HH:mm:ss"
            placeholder="选择自动下架时间"
            style="width: 200px"
          ></el-time-picker>
        </div>
        <div class="info-item">
          <span class="info-label"
            ><el-icon><Switch /></el-icon> 菜单状态</span
          >
          <el-select
            v-model="newMenu.status"
            placeholder="选择菜单状态"
            style="width: 200px"
            clearable
          >
            <el-option label="上架中" value="online" />
            <el-option label="草稿" value="draft" />
            <el-option label="下架中" value="offline" />
          </el-select>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addMenuDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveNewMenu">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.menu-management-container {
  padding: 24px;
  background-color: #fafbfc;
  min-height: 100vh;

  /* 输入框样式 - 与菜品管理保持一致 */
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

  .menu-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding: 20px;
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);

    .page-title {
      font-size: 1.429rem /* 原值: 20px */;
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

  .filter-panel {
    background-color: #ffffff;
    padding: 20px 24px;
    border-radius: 12px;
    margin-bottom: 24px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    border: 1px solid #e9ecef;
  }

  .menu-filters {
    display: flex;
    flex-direction: column; /* 改为纵向排列，将状态筛选和排序分成两行 */
    gap: 16px; /* 两行之间的间距 */
    align-items: flex-start; /* 左对齐 */

    .filter-section {
      display: flex;
      align-items: center;
      gap: 12px;
      white-space: nowrap; /* 防止筛选标签换行 */

      .filter-label {
        font-weight: 500;
        white-space: nowrap; /* 确保标签文本不换行 */
        margin-right: 8px;
        color: #6c757d;
      }

      .status-filter {
        cursor: pointer;
        line-height: 28px; /* 增加行高确保图标和文字居中对齐 */
        white-space: nowrap !important; /* 强制标签内所有内容在同一行显示 */
        &:hover {
          opacity: 0.8;
        }
        // 确保标签内部元素也不换行
        :deep(.el-tag__content) {
          white-space: nowrap;
          display: inline-flex;
          align-items: center;
        }
      }
    }
  }

  .menu-list {
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

    .menu-item {
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

        .menu-actions .el-button {
          transform: translateY(0);
          opacity: 1;
        }
      }

      .menu-selection {
        margin-top: 4px;
        margin-right: 16px;
      }

      .menu-content {
        flex: 1;
        position: relative;
        z-index: 1;
        display: flex;
        flex-direction: column;

        .menu-info {
          .menu-name {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 16px;

            .name {
              font-size: 1.286rem /* 原值: 18px */;
              font-weight: 700;
              color: #2d3748;
            }
          }

          .menu-stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
            gap: 16px;
            margin-bottom: 20px;
            font-size: 1rem /* 原值: 14px */;

            .stat-item {
              display: flex;
              flex-direction: column;
              gap: 4px;

              .stat-label {
                color: #718096;
                font-size: 0.857rem /* 原值: 12px */;
                font-weight: 500;
              }

              .stat-value {
                color: #4a5568;
                font-size: 1rem /* 原值: 14px */;
                font-weight: 600;
              }
            }

            .dishes-count {
              color: #6c757d;
            }
          }

          /* 状态标签样式，与筛选部分一致 */
          .menu-status-tag {
            cursor: pointer;
            line-height: 28px;
            white-space: nowrap;
            display: inline-flex;
            align-items: center;

            :deep(.el-tag__content) {
              white-space: nowrap;
              display: inline-flex;
              align-items: center;
            }

            &:hover {
              opacity: 0.8;
            }
          }

          /* 菜品标签容器样式 */
          .menu-dishes {
            margin: 12px 0;

            .dishes-tags-container {
              display: flex;
              flex-wrap: wrap;
              gap: 8px;
              max-height: 56px; /* 两行标签的高度 */
              overflow: hidden;
              position: relative;
              padding-right: 20px;

              /* 超出时显示省略提示 */
              &::after {
                content: '...';
                position: absolute;
                right: 0;
                bottom: 8px;
                background: #ffffff;
                padding-left: 5px;
                font-size: 1rem /* 原值: 14px */;
                color: #adb5bd;
              }
            }
          }

          .auto-times {
            font-size: 0.929rem /* 原值: 13px */;
            color: #adb5bd;
            margin-bottom: 12px;
          }
        }

        .menu-actions {
          display: flex;
          flex-direction: row;
          gap: 10px;
          justify-content: flex-start;
          flex-wrap: wrap;
          margin-top: 12px;

          button {
            width: 90px;
            border-radius: 10px;
            font-weight: 500;
            transition: all 0.3s ease;
            border: none;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            transform: translateY(5px);
            opacity: 0.9;

            &:hover {
              transform: translateY(-1px);
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
            }

            &:active {
              transform: translateY(0);
            }

            &.btn-active {
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
  }

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
      white-space: nowrap; /* 防止全选文本换行 */
      min-width: 100px; /* 设置固定最小宽度，让批量操作按钮位置稳定 */

      .selected-count {
        font-size: 0.929rem /* 原值: 13px */;
        font-weight: 400;
        color: #909399;
      }
    }
  }

  // 分页容器样式
  .menu-pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
    padding: 16px;
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
    text-align: right;
  }

  // 新增按钮样式
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

  // 批量操作按钮样式
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

  /* 筛选面板小屏幕响应式调整 */
  @media (max-width: 767px) {
    .filter-panel {
      padding: 16px;
      overflow-x: auto; /* 小屏幕下允许横向滚动 */
    }

    .menu-filters {
      gap: 12px;
    }

    .filter-section {
      gap: 8px;
    }

    .filter-label {
      white-space: nowrap; /* 确保标签在小屏幕也不换行 */
    }
  }

  /* 将批量操作和分页分成两行显示 */
  .batch-pagination-container {
    margin-bottom: 20px;
    padding: 16px 20px;
    background-color: #ffffff;
    border-radius: 12px;
    border: 1px solid #f1f3f5;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);

    .batch-actions {
      justify-content: flex-start; /* 批量操作左对齐 */
    }

    .menu-pagination {
      text-align: center; /* 分页居中对齐 */
      margin-top: 12px;
    }
  }

  /* 对话框按钮样式 - 与添加菜品保持一致 */
  .dialog-footer {
    text-align: center;
    padding: 0 28px 24px;
  }

  :deep(.dialog-footer .el-button) {
    padding: 10px 28px;
    border-radius: 8px;
    font-weight: 500;
    font-size: 1rem /* 原值: 14px */;
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

  /* 对话框内容区域样式 - 与添加菜品保持一致 */
  :deep(.el-dialog) {
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }

  :deep(.el-dialog__header) {
    border-bottom: 2px solid rgba(102, 126, 234, 0.3);
    background: linear-gradient(135deg, rgba(230, 247, 255, 0.8) 0%, rgba(186, 231, 255, 0.8) 100%);
    padding: 24px 28px;
    border-radius: 12px 12px 0 0;
  }

  :deep(.el-dialog__title) {
    font-size: 1.429rem /* 原值: 20px */;
    font-weight: 600;
    color: #1890ff;
    background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  :deep(.el-dialog__body) {
    padding: 32px 28px;
    background-color: #fafbfc;
  }

  :deep(.el-dialog__footer) {
    border-top: 1px solid #f1f3f5;
    padding: 0 28px 24px;
    border-radius: 0 0 12px 12px;
    background-color: #ffffff;
  }

  /* 添加菜单表单样式 */
  .add-menu-form {
    .info-item {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 20px;

      .info-label {
        color: #555;
        width: 130px;
        font-weight: 500;
        font-size: 1rem /* 原值: 14px */;
      }

      /* 输入框、选择框、时间选择器悬浮效果优化 - 与添加菜品保持一致 */
      :deep(.el-input__wrapper),
      :deep(.el-select__wrapper),
      :deep(.el-time-picker .el-input__wrapper) {
        border-radius: 8px;
        border: 2px solid #e5e7eb;
        transition: all 0.3s ease;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

        &:hover {
          border-color: #91d5ff;
          box-shadow: 0 0 0 3px rgba(145, 213, 255, 0.1);
        }

        &.is-focus,
        &.is-focused {
          border-color: #40a9ff;
          box-shadow: 0 0 0 3px rgba(64, 169, 255, 0.15);
        }
      }
    }
  }
}

.auto-offline {
  margin-left: 20px;
}
</style>
