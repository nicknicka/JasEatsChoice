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
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import StatusTag from '../../components/common/StatusTag.vue'

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
const sortField = ref('updatedAt') // 默认按更新时间排序
const sortOrder = ref('desc') // 'asc' 或 'desc'

// 更新排序
const updateSorting = () => {
  filteredMenus.value.sort((a, b) => {
    // 获取排序字段的值，兼容 name 和 menuName 两种字段名
    const aValue = sortField.value === 'menuName' ? a.menuName || a.name || '' : a[sortField.value]
    const bValue = sortField.value === 'menuName' ? b.menuName || b.name || '' : b[sortField.value]

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

const fetchMenus = async () => {
  loading.value = true
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId
  if (!merchantId) {
    ElMessage.error('未检测到商家ID，请重新登录')
    loading.value = false
    return false
  }

  try {
    const response = await axios.get(
      `${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}`
    )
    if (!response.data?.success) {
      ElMessage.error(response.data?.message || '加载菜单失败')
      return false
    }

    menuList.value = response.data.data.map((menu) => ({
      ...menu,
      name: menu.name || menu.menuName || '',
      menuName: menu.menuName || menu.name || ''
    }))
    filteredMenus.value = [...menuList.value]
    updatePagination()
    return true
  } catch (error) {
    console.error('加载菜单失败:', error)
    ElMessage.error('加载菜单失败')
    return false
  } finally {
    loading.value = false
  }
}

// 页面加载时初始化
onMounted(() => {
  fetchMenus()
  window.scrollTo({ top: 0, behavior: 'smooth' })
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

const deleteMenus = async (menus) => {
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId
  if (!merchantId) {
    ElMessage.error('未检测到商家ID，请重新登录')
    return false
  }

  const menuIds = menus.map((menu) => menu.id)
  try {
    const response = await axios.post(`${API_CONFIG.baseURL}/v1/menus/menu/batch`, {
      menuIds,
      action: 'delete',
      merchantId
    })
    if (!response.data?.success) {
      ElMessage.error(response.data?.message || '删除菜单失败')
      return false
    }

    const refreshed = await fetchMenus()
    if (!refreshed) return false
    updateFilter()
    if (menuIds.some((id) => menuList.value.some((menu) => menu.id === id))) {
      ElMessage.error('删除后菜单仍然存在，请重试')
      return false
    }
    return true
  } catch (error) {
    console.error('删除菜单失败:', error)
    ElMessage.error('删除菜单失败')
    return false
  }
}

// 删除菜单
const deleteMenu = async (menu) => {
  try {
    await ElMessageBox.confirm('确定要删除该菜单？删除后不可恢复！', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'danger'
    })
  } catch {
    return
  }

  if (await deleteMenus([menu])) {
    ElMessage.success('菜单已删除')
  }
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
        .then(async () => {
          const menusToDelete = [...selectedMenus.value]
          if (await deleteMenus(menusToDelete)) {
            selectedMenus.value = []
            ElMessage.success('批量删除成功')
          }
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
      dayjs(menu.updatedAt).format('YYYY-MM-DD HH:mm:ss'),
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
    <!-- 页头：merchant 紧凑版，标题 --text-h2 + 上下 --space-4，返回在右侧动作区（§4.7） -->
    <AppPageHeader title="菜单管理" :compact="true">
      <template #actions>
        <el-button type="primary" @click="openAddMenuDialog">
          <el-icon><CirclePlus /></el-icon>
          <span>新增菜单</span>
        </el-button>
        <CommonBackButton type="default" />
      </template>
    </AppPageHeader>

    <!-- 工具条：搜索 + 状态分段 + 排序压缩成一行（评审 P0；控件高度取 --control-height-sm，§3.4） -->
    <div class="menu-toolbar">
      <el-input
        v-model="searchKeyword"
        class="menu-toolbar__search"
        placeholder="输入菜单名称/关键词搜索..."
        clearable
        @input="updateFilter"
      >
        <template #prefix>
          <el-icon class="menu-toolbar__search-icon"><Search /></el-icon>
        </template>
      </el-input>

      <el-radio-group
        v-model="activeStatusFilter"
        class="menu-toolbar__status"
        @change="updateFilter"
      >
        <el-radio-button
          v-for="status in ['all', 'online', 'draft', 'offline']"
          :key="status"
          :value="status"
        >
          {{ status === 'all' ? '全部菜单' : menuStatusMap[status].text }}
        </el-radio-button>
      </el-radio-group>

      <div class="menu-toolbar__sort">
        <el-select v-model="sortField" class="menu-toolbar__select" @change="updateFilter">
          <el-option label="更新时间" value="updatedAt" />
          <el-option label="菜单名称" value="menuName" />
        </el-select>
        <el-select
          v-model="sortOrder"
          class="menu-toolbar__select menu-toolbar__select--order"
          @change="updateFilter"
        >
          <el-option label="升序" value="asc" />
          <el-option label="降序" value="desc" />
        </el-select>
        <el-tooltip content="按所选字段升序或降序排列菜单" placement="top">
          <el-icon class="menu-toolbar__help"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
    </div>

    <!-- 批量操作条：选中后滑入，控件高度取 --control-height-sm（评审 P0/P2、§3.4） -->
    <transition name="menu-batch">
      <div v-if="selectedMenus.length > 0" class="menu-batch-bar">
        <label class="menu-batch-bar__all">
          <el-checkbox
            :indeterminate="getSelectAllState() === 1"
            :model-value="getSelectAllState() === 2"
            @change="toggleSelectAll"
          />
          <span>全选</span>
          <span class="menu-batch-bar__count">
            {{ selectedMenus.length }}/{{ filteredMenus.length }}
          </span>
        </label>

        <div class="menu-batch-bar__actions">
          <el-button @click="batchOperation('online')">
            <el-icon><CircleCheck /></el-icon>
            <span>批量上架</span>
          </el-button>
          <el-button @click="batchOperation('offline')">
            <el-icon><CircleClose /></el-icon>
            <span>批量下架</span>
          </el-button>
          <el-button type="danger" plain @click="batchOperation('delete')">
            <el-icon><Delete /></el-icon>
            <span>批量删除</span>
          </el-button>
          <el-button link @click="selectedMenus = []">取消选择</el-button>
        </div>
      </div>
    </transition>

    <!-- 菜单列表：每行一个精简摘要 + 行内动作（评审 P1 缩短菜单卡） -->
    <div class="menu-list">
      <transition-group name="list" tag="div" class="menu-list__items">
        <div
          v-for="menu in paginatedMenus"
          :key="menu.id"
          class="menu-item"
          :class="{ 'menu-item--selected': selectedMenus.includes(menu) }"
        >
          <el-checkbox
            class="menu-item__check"
            :model-value="selectedMenus.includes(menu)"
            @change="toggleMenuSelection(menu)"
          />

          <div class="menu-item__main">
            <div class="menu-item__head">
              <span class="menu-item__name">{{ menu.name || menu.menuName || '未命名菜单' }}</span>
              <StatusTag :status="menu.status" :text="menuStatusMap[menu.status].text" size="sm" />
              <span v-if="menu.category" class="menu-item__category">{{ menu.category }}</span>
            </div>
            <div class="menu-item__meta">
              <span class="menu-item__meta-item">
                <el-icon><Food /></el-icon>
                {{
                  Array.isArray(menu.dishes)
                    ? menu.dishes.length
                    : typeof menu.dishes === 'number'
                      ? menu.dishes
                      : 0
                }}
                菜品
              </span>
              <span class="menu-item__meta-item">
                <el-icon><Clock /></el-icon>
                更新时间：{{ dayjs(menu.updatedAt).format('YYYY-MM-DD HH:mm') }}
              </span>
              <span v-if="menu.autoOnline" class="menu-item__meta-item">
                <el-icon><Clock /></el-icon>
                自动上架：{{
                  menu.autoOnline.includes('T') || menu.autoOnline.length > 8
                    ? dayjs(menu.autoOnline).format('HH:mm')
                    : dayjs(menu.autoOnline, 'HH:mm:ss').format('HH:mm')
                }}
              </span>
              <span v-if="menu.autoOffline" class="menu-item__meta-item">
                <el-icon><Clock /></el-icon>
                自动下架：{{
                  menu.autoOffline.includes('T') || menu.autoOffline.length > 8
                    ? dayjs(menu.autoOffline).format('HH:mm')
                    : dayjs(menu.autoOffline, 'HH:mm:ss').format('HH:mm')
                }}
              </span>
            </div>
          </div>

          <div class="menu-item__actions">
            <el-button type="primary" link @click="editMenu(menu)">
              <el-icon><Edit /></el-icon>
              <span>编辑</span>
            </el-button>
            <el-button link @click="toggleMenuStatus(menu)">
              <el-icon v-if="menu.status === 'online'"><CircleClose /></el-icon>
              <el-icon v-else><CircleCheck /></el-icon>
              <span>{{ menu.status === 'online' ? '下架菜单' : '上架菜单' }}</span>
            </el-button>
            <el-button link @click="exportMenu(menu)">
              <el-icon><Download /></el-icon>
              <span>导出菜单</span>
            </el-button>
            <el-button type="danger" link @click="deleteMenu(menu)">
              <el-icon><Delete /></el-icon>
              <span>删除</span>
            </el-button>
          </div>
        </div>
      </transition-group>

      <!-- 空状态：图标 + 中文说明 + 替代动作（§4.5） -->
      <AppEmptyState
        v-if="!loading && filteredMenus.length === 0"
        class="menu-list__empty"
        :icon="Food"
        description="您还没有创建任何菜单~"
        action-text="新增菜单"
        @action="openAddMenuDialog"
      />

      <!-- 分页：全文中文文案，移到列表末尾（评审 P1） -->
      <div v-if="filteredMenus.length > 0" class="menu-pagination">
        <span class="menu-pagination__total">共 {{ filteredMenus.length }} 条</span>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          layout="prev, pager, next"
          :total="filteredMenus.length"
          @current-change="updatePagination"
        />
      </div>
    </div>

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

<style scoped>
/* 本页颜色、间距、圆角、字阶、动效全部取自 tokens.css 的 CSS 变量（§3、§7.1），无裸色值。 */
.menu-management-container {
  box-sizing: border-box;
  padding: 0 var(--space-6) var(--space-6);
}

/* 页头动作区控件高度取 --control-height-md（§3.4、§4.7） */
.menu-management-container :deep(.app-page-header__actions .el-button) {
  height: var(--control-height-md);
}

/* 工具条：单行承载搜索 + 状态分段 + 排序，L1 微质感（§3.4、§4.3） */
.menu-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3) var(--space-4);
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--card-gap-compact);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
}

.menu-toolbar__search {
  width: 240px;
  max-width: 100%;
}

.menu-toolbar__search-icon {
  color: var(--ink-400);
}

/* 工具条控件统一 --control-height-sm（§3.4） */
.menu-toolbar :deep(.el-input__wrapper),
.menu-toolbar :deep(.el-select__wrapper) {
  min-height: var(--control-height-sm);
  border-radius: var(--radius-sm);
}

.menu-toolbar__status :deep(.el-radio-button__inner) {
  height: var(--control-height-sm);
  padding: 0 var(--space-3);
  border-color: var(--line);
  background: var(--surface);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  box-shadow: none;
}

.menu-toolbar__status :deep(.el-radio-button__inner:hover) {
  color: var(--brand-600);
}

.menu-toolbar__status :deep(.el-radio-button.is-active .el-radio-button__inner) {
  border-color: var(--brand-500);
  background: var(--brand-500);
  color: var(--surface);
  box-shadow: none;
}

.menu-toolbar__sort {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-left: auto;
}

.menu-toolbar__select {
  width: 132px;
}

.menu-toolbar__select--order {
  width: 104px;
}

.menu-toolbar__help {
  color: var(--ink-400);
  cursor: help;
}

/* 批量操作条：选中后滑入（§3.5 状态切换 200ms，reduced-motion 由 token 兜底） */
.menu-batch-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-4);
  margin-bottom: var(--card-gap-compact);
  border: 1px solid var(--brand-200);
  border-radius: var(--radius-lg);
  background: var(--brand-50);
}

.menu-batch-bar__all {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--ink-700);
  font-size: var(--text-body-sm);
}

.menu-batch-bar__count {
  color: var(--ink-500);
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
}

.menu-batch-bar__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.menu-batch-bar__actions :deep(.el-button) {
  height: var(--control-height-sm);
}

.menu-batch-enter-active,
.menu-batch-leave-active {
  transition:
    opacity var(--duration-base) var(--ease-out),
    transform var(--duration-base) var(--ease-out);
}

.menu-batch-enter-from,
.menu-batch-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.menu-list {
  display: flex;
  flex-direction: column;
  gap: var(--card-gap-compact);
}

.menu-list__items {
  display: flex;
  flex-direction: column;
  gap: var(--card-gap-compact);
}

/* 菜单卡：L1 微质感摘要行，卡片内边距取 --card-padding-compact（§3.4） */
.menu-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--card-padding-compact);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
  transition:
    border-color var(--duration-fast) var(--ease-out),
    box-shadow var(--duration-fast) var(--ease-out);
}

.menu-item:hover {
  border-color: var(--line-strong);
  box-shadow:
    inset 0 1px 0 var(--surface-highlight),
    var(--shadow-sm);
}

/* 选中态由左边界 + 复选框共同表达，不再叠加浅色底（评审：等价值号收敛） */
.menu-item--selected {
  border-color: var(--brand-300);
  box-shadow:
    inset 3px 0 0 var(--brand-500),
    inset 0 1px 0 var(--surface-highlight);
}

.menu-item__check {
  flex-shrink: 0;
  margin-top: var(--space-1);
}

.menu-item__main {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: var(--space-2);
  min-width: 0;
}

.menu-item__head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  min-width: 0;
}

.menu-item__name {
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  line-height: 24px;
}

.menu-item__category {
  color: var(--ink-500);
  font-size: var(--text-caption);
  line-height: 18px;
}

.menu-item__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-1) var(--space-4);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.menu-item__meta-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  white-space: nowrap;
}

.menu-item__actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: var(--space-3);
  white-space: nowrap;
}

.menu-item__actions :deep(.el-button) {
  height: var(--control-height-sm);
  padding: 0;
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
}

.menu-item__actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.menu-list__empty {
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
}

/* 列表过渡：只动 transform / opacity（§3.5） */
.list-move,
.list-enter-active,
.list-leave-active {
  transition:
    opacity var(--duration-base) var(--ease-out),
    transform var(--duration-base) var(--ease-out);
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(4px);
}

.list-leave-active {
  position: absolute;
  width: 100%;
}

/* 分页：中文文案，置于列表末尾（评审 P1） */
.menu-pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-3);
}

.menu-pagination__total {
  color: var(--ink-500);
  font-family: var(--font-num);
  font-size: var(--text-body-sm);
  font-variant-numeric: tabular-nums;
}

/* 添加菜单对话框：只保留信息行布局，颜色交给全局 Element Plus 变量（§7.2） */
.add-menu-form .info-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

.add-menu-form .info-item:last-child {
  margin-bottom: 0;
}

.add-menu-form .info-label {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  gap: var(--space-1);
  width: 130px;
  color: var(--ink-500);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
}

/* 添加菜单对话框按钮组：保持原有的居中语义（§4.4 提交按钮） */
.dialog-footer {
  display: block;
  text-align: center;
}

.menu-management-container :deep(.el-dialog) {
  border-radius: var(--radius-lg);
}
</style>
