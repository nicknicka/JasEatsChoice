<template>
  <div class="permission-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>权限管理</h1>
      <p class="subtitle">管理系统权限和菜单</p>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="handleCreate">新建权限</el-button>
      <el-button :icon="Refresh" @click="fetchPermissionList">刷新</el-button>
    </div>

    <!-- 权限树表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="permissionList"
        v-loading="loading"
        row-key="permissionId"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        stripe
        default-expand-all
      >
        <el-table-column prop="permissionName" label="权限名称" min-width="200" />
        <el-table-column prop="permissionCode" label="权限编码" min-width="180" />
        <el-table-column label="资源类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getResourceTypeColor(row.resourceType)">
              {{ getResourceTypeText(row.resourceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="180" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="100" />
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
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" size="small" link @click="handleCreateChild(row)">添加子权限</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

const loading = ref(false)
const permissionList = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
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
  return isEdit.value ? '编辑权限' : '新建权限'
})

// 获取资源类型颜色
const getResourceTypeColor = (type) => {
  const colors = {
    'MENU': 'primary',
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
const fetchPermissionList = async () => {
  loading.value = true
  try {
    // TODO: 调用实际的权限树API
    // const response = await axios.get('http://localhost:8080/api/admin/settings/permissions/tree')

    // 临时使用模拟数据
    permissionList.value = [
      {
        permissionId: 1,
        permissionName: '控制台',
        permissionCode: 'admin:dashboard',
        resourceType: 'MENU',
        parentId: 0,
        path: '/admin/dashboard',
        icon: 'DataBoard',
        sortOrder: 1,
        status: 'ACTIVE',
        children: []
      },
      {
        permissionId: 2,
        permissionName: '用户管理',
        permissionCode: 'admin:user',
        resourceType: 'MENU',
        parentId: 0,
        path: '/admin/users',
        icon: 'User',
        sortOrder: 2,
        status: 'ACTIVE',
        children: [
          {
            permissionId: 21,
            permissionName: '用户列表',
            permissionCode: 'admin:user:list',
            resourceType: 'API',
            parentId: 2,
            sortOrder: 1,
            status: 'ACTIVE'
          },
          {
            permissionId: 22,
            permissionName: '用户详情',
            permissionCode: 'admin:user:detail',
            resourceType: 'API',
            parentId: 2,
            sortOrder: 2,
            status: 'ACTIVE'
          },
          {
            permissionId: 23,
            permissionName: '修改状态',
            permissionCode: 'admin:user:status',
            resourceType: 'BUTTON',
            parentId: 2,
            sortOrder: 3,
            status: 'ACTIVE'
          },
          {
            permissionId: 24,
            permissionName: '删除用户',
            permissionCode: 'admin:user:delete',
            resourceType: 'BUTTON',
            parentId: 2,
            sortOrder: 4,
            status: 'ACTIVE'
          }
        ]
      },
      {
        permissionId: 3,
        permissionName: '商家管理',
        permissionCode: 'admin:merchant',
        resourceType: 'MENU',
        parentId: 0,
        path: '/admin/merchants',
        icon: 'Shop',
        sortOrder: 3,
        status: 'ACTIVE',
        children: [
          {
            permissionId: 31,
            permissionName: '商家列表',
            permissionCode: 'admin:merchant:list',
            resourceType: 'API',
            parentId: 3,
            sortOrder: 1,
            status: 'ACTIVE'
          },
          {
            permissionId: 32,
            permissionName: '商家审核',
            permissionCode: 'admin:merchant:audit',
            resourceType: 'BUTTON',
            parentId: 3,
            sortOrder: 2,
            status: 'ACTIVE'
          }
        ]
      }
    ]

    // 构建父权限选项（用于下拉选择）
    parentPermissionOptions.value = buildParentOptions(permissionList.value)
  } catch (error) {
    console.error('获取权限列表失败:', error)
    ElMessage.error('获取权限列表失败')
  } finally {
    loading.value = false
  }
}

// 构建父权限选项（添加顶级选项）
const buildParentOptions = (list) => {
  return [
    { permissionId: 0, permissionName: '顶级权限', children: list }
  ]
}

// 创建权限
const handleCreate = () => {
  isEdit.value = false
  Object.assign(permissionForm, {
    permissionId: null,
    parentId: 0,
    permissionName: '',
    permissionCode: '',
    resourceType: 'MENU',
    path: '',
    icon: '',
    sortOrder: 0,
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

// 创建子权限
const handleCreateChild = (row) => {
  isEdit.value = false
  Object.assign(permissionForm, {
    permissionId: null,
    parentId: row.permissionId,
    permissionName: '',
    permissionCode: '',
    resourceType: 'BUTTON',
    path: '',
    icon: '',
    sortOrder: 0,
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

// 编辑权限
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(permissionForm, row)
  dialogVisible.value = true
}

// 删除权限
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除权限 "${row.permissionName}" 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用删除API
    ElMessage.success('删除成功')
    fetchPermissionList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除权限失败:', error)
      ElMessage.error('删除权限失败')
    }
  }
}

// 提交权限
const submitPermission = async () => {
  if (!permissionFormRef.value) return

  try {
    await permissionFormRef.value.validate()
    submitting.value = true

    // TODO: 调用实际的创建/更新API
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchPermissionList()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchPermissionList()
})
</script>

<style scoped lang="less">
.permission-management-container {
  .page-header {
    margin-bottom: 20px;

    h1 {
      font-size: 24px;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .subtitle {
      color: #909399;
      margin: 0;
      font-size: 14px;
    }
  }

  .toolbar {
    margin-bottom: 20px;
  }
}
</style>
