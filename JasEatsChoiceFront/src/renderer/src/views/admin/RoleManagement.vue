<template>
  <div class="role-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>角色管理</h1>
      <p class="subtitle">管理系统角色和权限分配</p>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="handleCreate">新建角色</el-button>
    </div>

    <!-- 角色列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="roleList" v-loading="loading" stripe>
        <el-table-column prop="roleId" label="角色ID" width="100" />
        <el-table-column prop="roleName" label="角色名称" min-width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="180" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleAssignPermissions(row)">分配权限</el-button>
            <el-button type="warning" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)" v-if="row.roleCode !== 'SUPER_ADMIN'">删除</el-button>
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
      <el-form :model="roleForm" :rules="roleRules" ref="roleFormRef" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="roleForm.roleCode" placeholder="如：ADMIN, AUDITOR" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
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
        <el-button type="primary" @click="submitRole" :loading="submitting">确定</el-button>
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
        <el-button type="primary" @click="submitPermissions" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '@/utils/api'

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

// 权限树数据（模拟数据，实际应从API获取）
const permissionTree = ref([])

// 获取角色列表
const fetchRoleList = async () => {
  loading.value = true
  try {
    const response = await api.get('http://localhost:8080/api/admin/settings/roles', {
      params: { page: 1, pageSize: 100 }
    })

    if (response.data) {
      roleList.value = response.data.records || []
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

// 获取权限树
const fetchPermissionTree = async () => {
  try {
    // TODO: 调用实际的权限树API
    // const response = await api.get('http://localhost:8080/api/admin/settings/permissions/tree')
    // permissionTree.value = response.data

    // 临时使用模拟数据
    permissionTree.value = [
      {
        permissionId: 1,
        permissionName: '用户管理',
        children: [
          { permissionId: 11, permissionName: '用户列表' },
          { permissionId: 12, permissionName: '用户详情' },
          { permissionId: 13, permissionName: '修改状态' },
          { permissionId: 14, permissionName: '删除用户' }
        ]
      },
      {
        permissionId: 2,
        permissionName: '商家管理',
        children: [
          { permissionId: 21, permissionName: '商家列表' },
          { permissionId: 22, permissionName: '商家审核' },
          { permissionId: 23, permissionName: '修改状态' }
        ]
      }
    ]
  } catch (error) {
    console.error('获取权限树失败:', error)
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

// 删除角色
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色 "${row.roleName}" 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await api.delete(`http://localhost:8080/api/admin/settings/roles/${row.roleId}`)

    if (response.data?.success) {
      ElMessage.success('删除成功')
      fetchRoleList()
    } else {
      ElMessage.error(response.data?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除角色失败:', error)
      ElMessage.error('删除角色失败')
    }
  }
}

// 提交角色
const submitRole = async () => {
  if (!roleFormRef.value) return

  try {
    await roleFormRef.value.validate()
    submitting.value = true

    const response = isEdit.value
      ? await api.put(`http://localhost:8080/api/admin/settings/roles/${roleForm.roleId}`, roleForm)
      : await api.post('http://localhost:8080/api/admin/settings/roles', roleForm)

    if (response.data?.success) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchRoleList()
    } else {
      ElMessage.error(response.data?.message || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

// 分配权限
const handleAssignPermissions = async (row) => {
  try {
    // 获取角色已有的权限
    const response = await api.get(`http://localhost:8080/api/admin/settings/roles/${row.roleId}/permissions`)

    if (response.data?.success) {
      const permissionIds = response.data.permissionIds || []

      // 设置当前角色ID
      roleForm.roleId = row.roleId

      // 等待树加载完成后设置选中状态
      await fetchPermissionTree()

      // 设置选中的权限
      setTimeout(() => {
        permissionTreeRef.value?.setCheckedKeys(permissionIds)
      }, 100)

      permissionDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取角色权限失败:', error)
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

    const response = await api.post(
      `http://localhost:8080/api/admin/settings/roles/${roleForm.roleId}/permissions`,
      { permissionIds: allPermissionIds }
    )

    if (response.data?.success) {
      ElMessage.success('权限分配成功')
      permissionDialogVisible.value = false
    } else {
      ElMessage.error(response.data?.message || '分配失败')
    }
  } catch (error) {
    console.error('分配权限失败:', error)
    ElMessage.error('分配权限失败')
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

  .table-card {
    .permission-tree-wrapper {
      max-height: 400px;
      overflow-y: auto;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      padding: 10px;
    }
  }
}
</style>
