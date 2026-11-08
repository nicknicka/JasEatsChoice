<template>
  <div class="role-management-container">
    <AppPageHeader title="角色管理" subtitle="管理系统角色和权限分配" :compact="true" />

    <!-- 角色列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-card-header">
          <h2>角色列表</h2>
          <el-button type="primary" :icon="Plus" @click="handleCreate">新建角色</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="roleList" stripe>
        <el-table-column prop="roleId" label="角色ID" width="72" />
        <el-table-column prop="roleName" label="角色名称" min-width="140" />
        <el-table-column prop="roleCode" label="角色编码" width="160" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column label="权限数量" width="80" align="center">
          <template #default="{ row }">
            <span class="permission-count">{{ row.permissionCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <StatusTag :status="row.status === 'ACTIVE' ? 'enabled' : 'disabled'" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            <span class="cell-time">{{ formatTime(row.createdAt) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <!-- 分配权限和编辑先弹对话框；删除等立即生效动作收进更多菜单 -->
            <RowActions
              :primary="{ text: '分配权限', onClick: () => handleAssignPermissions(row) }"
              :items="rowActions(row)"
              :max-visible="1"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑角色对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '新建角色'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model="roleForm.roleCode"
            placeholder="如：ADMIN, AUDITOR"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio label="ACTIVE">启用</el-radio>
            <el-radio label="DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="roleForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRole">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="分配权限"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="permission-tree-wrapper">
        <el-tree
          ref="permissionTreeRef"
          :data="permissionTree"
          :props="{ children: 'children', label: 'permissionName' }"
          node-key="permissionId"
          show-checkbox
          default-expand-all
        />
      </div>

      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPermissions">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import RowActions from '@/components/common/RowActions.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import { formatTime } from '@/utils/formatters'
import {
  getRoleList,
  createRole,
  updateRole,
  deleteRole,
  getPermissionTree,
  assignRolePermissions,
  getRolePermissions
} from '@/api/admin'

const loading = ref(false)
const roleList = ref([])
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const roleFormRef = ref(null)
const permissionTreeRef = ref(null)

const roleForm = reactive({
  roleId: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 'ACTIVE',
  sortOrder: 0
})

const roleRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线', trigger: 'blur' }
  ]
}

// 权限树数据
const permissionTree = ref([])

// 获取角色列表
const fetchRoleList = async () => {
  loading.value = true
  try {
    console.log('[角色管理] 获取角色列表')
    const response = await getRoleList({ page: 1, pageSize: 100 })

    console.log('[角色管理] API响应:', response)

    if (response && response.records) {
      roleList.value = response.records
    }
  } catch (error) {
    console.error('[角色管理] 获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 获取权限树
const fetchPermissionTree = async () => {
  try {
    console.log('[角色管理] 获取权限树')
    const response = await getPermissionTree()

    console.log('[角色管理] 权限树响应:', response)

    if (response && response.success) {
      permissionTree.value = response.data || []
    }
  } catch (error) {
    console.error('[角色管理] 获取权限树失败:', error)
    ElMessage.error('获取权限树失败')
  }
}

// 创建角色
const handleCreate = () => {
  isEdit.value = false
  Object.assign(roleForm, {
    roleId: null,
    roleName: '',
    roleCode: '',
    description: '',
    status: 'ACTIVE',
    sortOrder: 0
  })
  dialogVisible.value = true
}

// 编辑角色
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(roleForm, row)
  dialogVisible.value = true
}

// 行操作：编辑先弹对话框，删除为危险动作；超级管理员不显示删除
const rowActions = (row) => {
  const items = [{ text: '编辑', onClick: () => handleEdit(row) }]
  if (row.roleCode !== 'SUPER_ADMIN') {
    items.push({ text: '删除', danger: true, onClick: () => handleDelete(row) })
  }
  return items
}

// 删除角色：确认由 RowActions 的危险动作入口承担，避免叠加两层确认（§4.6）
const handleDelete = async (row) => {
  try {
    console.log('[角色管理] 删除角色:', row.roleId)
    const response = await deleteRole(row.roleId)

    if (response && response.success) {
      ElMessage.success('删除成功')
      fetchRoleList()
    } else {
      ElMessage.error(response?.message || '删除失败')
    }
  } catch (error) {
    console.error('[角色管理] 删除角色失败:', error)
    ElMessage.error('删除角色失败: ' + (error.message || '网络错误'))
  }
}

// 提交角色
const submitRole = async () => {
  if (!roleFormRef.value) return

  try {
    await roleFormRef.value.validate()
    submitting.value = true

    console.log('[角色管理] 提交角色:', isEdit.value, roleForm)

    const response = isEdit.value
      ? await updateRole(roleForm.roleId, roleForm)
      : await createRole(roleForm)

    if (response && response.success) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchRoleList()
    } else {
      ElMessage.error(response?.message || '操作失败')
    }
  } catch (error) {
    if (error !== false) {
      console.error('[角色管理] 提交失败:', error)
      ElMessage.error('操作失败: ' + (error.message || '网络错误'))
    }
  } finally {
    submitting.value = false
  }
}

// 分配权限
const handleAssignPermissions = async (row) => {
  try {
    console.log('[角色管理] 分配权限, 角色ID:', row.roleId)

    // 获取角色已有的权限
    const response = await getRolePermissions(row.roleId)

    if (response && response.success) {
      const permissionIds = response.permissionIds || []

      // 设置当前角色ID
      roleForm.roleId = row.roleId

      // 等待树加载完成后设置选中状态
      await fetchPermissionTree()

      // 设置选中的权限
      setTimeout(() => {
        permissionTreeRef.value?.setCheckedKeys(permissionIds)
      }, 100)

      permissionDialogVisible.value = true
    } else {
      ElMessage.error('获取角色权限失败')
    }
  } catch (error) {
    console.error('[角色管理] 获取角色权限失败:', error)
    ElMessage.error('获取角色权限失败')
  }
}

// 提交权限分配
const submitPermissions = async () => {
  try {
    submitting.value = true

    // 获取选中的权限ID（包括半选中的父节点）
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
    const allPermissionIds = [...checkedKeys, ...halfCheckedKeys]

    console.log('[角色管理] 提交权限分配:', allPermissionIds)

    const response = await assignRolePermissions(roleForm.roleId, {
      permissionIds: allPermissionIds
    })

    if (response && response.success) {
      ElMessage.success('权限分配成功')
      permissionDialogVisible.value = false
      fetchRoleList()
    } else {
      ElMessage.error(response?.message || '分配失败')
    }
  } catch (error) {
    console.error('[角色管理] 分配权限失败:', error)
    ElMessage.error('分配权限失败: ' + (error.message || '网络错误'))
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchRoleList()
})
</script>

<style scoped lang="less">
.role-management-container {
  /* 卡头标题从 h2 起，与列表页同源（§4.7） */
  .table-card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--space-4);

    h2 {
      margin: 0;
      color: var(--ink-900);
      font-size: var(--text-h3);
      font-weight: var(--weight-semibold);
    }
  }

  .permission-count {
    color: var(--ink-700);
    font-family: var(--font-num);
    font-variant-numeric: tabular-nums;
  }

  .cell-time {
    color: var(--ink-700);
    font-family: var(--font-num);
    font-variant-numeric: tabular-nums;
    white-space: nowrap;
  }

  .table-card {
    .permission-tree-wrapper {
      max-height: 400px;
      overflow-y: auto;
      border: 1px solid var(--line);
      border-radius: var(--radius-sm);
      padding: var(--space-2);
    }
  }
}
</style>
