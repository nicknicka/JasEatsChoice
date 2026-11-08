<template>
  <div class="permission-management-container">
    <!-- 页头：统一标题容器 + 右侧动作区（§4.7） -->
    <AppPageHeader title="权限管理" subtitle="管理系统权限和菜单" :compact="true">
      <template #actions>
        <el-button type="primary" :icon="ElIcons.Plus" @click="handleCreate">新建权限</el-button>
        <el-button :icon="ElIcons.Refresh" @click="fetchPermissionTree">刷新</el-button>
      </template>
    </AppPageHeader>

    <!-- 权限树表格 -->
    <el-card class="table-card" shadow="never">
      <!-- 空数据：紧凑中文空状态替换表体（§4.3、§5.5） -->
      <AppEmptyState
        v-if="!loading && permissionList.length === 0"
        compact
        icon="Lock"
        description="还没有权限数据，先新建一条权限试试"
        action-text="新建权限"
        @action="handleCreate"
      />

      <el-table
        v-else
        :data="permissionList"
        v-loading="loading"
        row-key="permissionId"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        stripe
        default-expand-all
      >
        <!-- 权限名称列固定，长表横向滚动时保留层级锚点（§4.3） -->
        <el-table-column prop="permissionName" label="权限名称" min-width="200" fixed="left" />
        <el-table-column prop="permissionCode" label="权限编码" min-width="180" />
        <el-table-column label="资源类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getResourceTypeColor(row.resourceType)">
              {{ getResourceTypeText(row.resourceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="180" show-overflow-tooltip />
        <!-- 图标列渲染真实线性图标，缺值给占位，不再展示纯文本图标名（§4.9） -->
        <el-table-column label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="resolveIcon(row.icon)" class="cell-icon" :title="row.icon">
              <component :is="resolveIcon(row.icon)" />
            </el-icon>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <!-- 行操作统一规则：删除这类破坏性动作收进「更多」；每行最多 2 个可见动作 -->
            <RowActions
              :primary="{ text: '编辑', onClick: () => handleEdit(row) }"
              :items="rowActions(row)"
              :max-visible="1"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑权限对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="permissionForm" :rules="permissionRules" ref="permissionFormRef" label-width="100px">
        <el-form-item label="上级权限" prop="parentId">
          <el-tree-select
            v-model="permissionForm.parentId"
            :data="parentPermissionOptions"
            :props="{ value: 'permissionId', label: 'permissionName', children: 'children' }"
            placeholder="选择上级权限（不选则为顶级权限）"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="permissionForm.permissionName" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="permissionCode">
          <el-input v-model="permissionForm.permissionCode" placeholder="如：admin:user:list" />
        </el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="permissionForm.resourceType" placeholder="请选择资源类型">
            <el-option label="菜单" value="MENU" />
            <el-option label="按钮" value="BUTTON" />
            <el-option label="接口" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="路由路径" prop="path" v-if="permissionForm.resourceType === 'MENU'">
          <el-input v-model="permissionForm.path" placeholder="如：/admin/users" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="permissionForm.icon" placeholder="图标名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="permissionForm.description" type="textarea" :rows="2" placeholder="请输入权限描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="permissionForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="permissionForm.status">
            <el-radio label="ACTIVE">启用</el-radio>
            <el-radio label="DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPermission" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as ElIcons from '@element-plus/icons-vue'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppEmptyState from '@/components/common/AppEmptyState.vue'
import RowActions from '@/components/common/RowActions.vue'
import { getPermissionTree, createPermission, updatePermission, deletePermission } from '@/api/admin'

const loading = ref(false)
const permissionList = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const isChild = ref(false)
const parentPermissionOptions = ref([])
const permissionFormRef = ref(null)

const permissionForm = reactive({
  permissionId: null,
  parentId: 0,
  permissionName: '',
  permissionCode: '',
  resourceType: 'MENU',
  path: '',
  icon: '',
  description: '',
  sortOrder: 0,
  status: 'ACTIVE'
})

const permissionRules = {
  permissionName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permissionCode: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { pattern: /^[a-z:_]+$/, message: '权限编码格式：模块:功能:操作', trigger: 'blur' }
  ],
  resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }]
}

const dialogTitle = computed(() => {
  if (isChild.value) return '添加子权限'
  return isEdit.value ? '编辑权限' : '新建权限'
})

// 获取资源类型颜色
const getResourceTypeColor = (type) => {
  // 资源类型是分类不是状态；菜单取信息蓝、接口取警示橙，保持评审认定的蓝/橙区分（§3.1）
  const colors = {
    'MENU': 'info',
    'BUTTON': 'success',
    'API': 'warning'
  }
  return colors[type] || 'info'
}

// 获取资源类型文本
const getResourceTypeText = (type) => {
  const texts = {
    'MENU': '菜单',
    'BUTTON': '按钮',
    'API': '接口'
  }
  return texts[type] || '未知'
}

// 获取权限列表（树形结构）
const fetchPermissionTree = async () => {
  loading.value = true
  try {
    console.log('[权限管理] 获取权限树')
    const response = await getPermissionTree()

    console.log('[权限管理] 权限树响应:', response)

    if (response && response.success) {
      permissionList.value = response.data || []
      parentPermissionOptions.value = response.data || []
    }
  } catch (error) {
    console.error('[权限管理] 获取权限树失败:', error)
    ElMessage.error('获取权限树失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 创建权限
const handleCreate = () => {
  isEdit.value = false
  isChild.value = false
  Object.assign(permissionForm, {
    permissionId: null,
    parentId: 0,
    permissionName: '',
    permissionCode: '',
    resourceType: 'MENU',
    path: '',
    icon: '',
    description: '',
    sortOrder: 0,
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

// 创建子权限
const handleCreateChild = (row) => {
  isEdit.value = false
  isChild.value = true
  Object.assign(permissionForm, {
    permissionId: null,
    parentId: row.permissionId,
    permissionName: '',
    permissionCode: '',
    resourceType: 'API',
    path: '',
    icon: '',
    description: '',
    sortOrder: 0,
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

// 编辑权限
const handleEdit = (row) => {
  isEdit.value = true
  isChild.value = false
  Object.assign(permissionForm, row)
  dialogVisible.value = true
}

// 删除权限：二次确认由 RowActions 的「更多」菜单承担，避免叠加两层确认（§4.6、行操作统一规则）
const handleDelete = async (row) => {
  try {
    // 检查是否有子权限
    if (row.children && row.children.length > 0) {
      ElMessage.warning('该权限下有子权限，无法删除')
      return
    }

    console.log('[权限管理] 删除权限:', row.permissionId)
    const response = await deletePermission(row.permissionId)

    if (response && response.success) {
      ElMessage.success('删除成功')
      fetchPermissionTree()
    } else {
      ElMessage.error(response?.message || '删除失败')
    }
  } catch (error) {
    console.error('[权限管理] 删除权限失败:', error)
    ElMessage.error('删除权限失败: ' + (error.message || '网络错误'))
  }
}

// 把权限的 icon 名解析为真实 Element Plus 线性图标；未注册的名字返回 null（§4.9）
const resolveIcon = (name) => (name && ElIcons[name]) || null

// 行操作：破坏性动作（删除）带 danger，由 RowActions 收进「更多」并二次确认
const rowActions = (row) => [
  { text: '添加子权限', onClick: () => handleCreateChild(row) },
  { text: '删除', danger: true, onClick: () => handleDelete(row) }
]

// 提交权限
const submitPermission = async () => {
  if (!permissionFormRef.value) return

  try {
    await permissionFormRef.value.validate()
    submitting.value = true

    console.log('[权限管理] 提交权限:', permissionForm)

    const response = isEdit.value
      ? await updatePermission(permissionForm.permissionId, permissionForm)
      : await createPermission(permissionForm)

    if (response && response.success) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchPermissionTree()
    } else {
      ElMessage.error(response?.message || '操作失败')
    }
  } catch (error) {
    if (error !== false) {
      console.error('[权限管理] 提交失败:', error)
      ElMessage.error('操作失败: ' + (error.message || '网络错误'))
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchPermissionTree()
})
</script>

<style scoped lang="less">
.permission-management-container {
  padding: var(--card-padding-compact) var(--page-gutter);
  background: var(--canvas);
  max-width: 1440px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;

  .table-card {
    /* 长表滚动时表头可辨：浅底色 + 中量字重（§4.3） */
    :deep(.el-table__header th) {
      background-color: var(--canvas-sunken);
      font-weight: var(--weight-semibold);
    }

    /* 长表行 hover 高亮，避免横向读串行（§3.5 高频微交互用 CSS transition） */
    :deep(.el-table__body tr:hover > td) {
      background-color: var(--canvas-sunken);
      transition: background-color var(--duration-fast) var(--ease-out);
    }

    /* 树展开/收起：过渡取 token，并放大命中区域 + hover 提示（§3.5） */
    :deep(.el-table__expand-icon) {
      transition: transform var(--duration-fast) var(--ease-out);
      padding: var(--space-2);
      margin: calc(var(--space-2) * -1);
      border-radius: var(--radius-sm);
    }

    :deep(.el-table__expand-icon:hover) {
      background-color: var(--canvas-sunken);
    }
  }

  /* 权限图标：中性色（§4.9） */
  .cell-icon {
    color: var(--ink-500);
    vertical-align: middle;
  }
}
</style>
