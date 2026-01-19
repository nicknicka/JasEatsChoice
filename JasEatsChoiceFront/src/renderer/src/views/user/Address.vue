<template>
  <div class="address-container">
    <div class="header">
      <common-back-button />
      <h2>管理地址</h2>
    </div>

    <el-button type="primary" style="margin-bottom: 20px" @click="showAddDialog = true">
      <el-icon><Plus /></el-icon>
      新增地址
    </el-button>

    <div class="address-list">
      <el-card
        v-for="address in addresses"
        :key="address.id"
        class="address-card"
        :class="{ 'default-address': address.isDefault }"
      >
        <div class="address-header">
          <div class="address-name">{{ address.name }}</div>
          <div class="address-phone">{{ address.phone }}</div>
        </div>

        <div class="address-details">
          <div class="address-full">
            {{ address.province }} {{ address.city }} {{ address.district }} {{ address.street }}
          </div>
          <div class="address-tag" v-if="address.tag">{{ address.tag }}</div>
        </div>

        <div class="address-actions">
          <el-button type="text" size="small" @click="editAddress(address)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="text" size="small" danger @click="deleteAddress(address)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
          <el-button
            v-if="!address.isDefault"
            type="text"
            size="small"
            @click="setDefault(address)"
          >
            设置默认
          </el-button>
        </div>
      </el-card>

      <!-- 无数据显示 -->
      <div v-if="addresses.length === 0" class="empty-state">
        <el-icon class="empty-icon"><Location /></el-icon>
        <p>您还没有添加任何地址</p>
      </div>
    </div>

    <!-- 新增地址对话框 -->
    <el-dialog title="新增地址" v-model="showAddDialog" width="50%">
      <el-form ref="addressFormRef" :model="newAddress" label-width="80px" :rules="addressRules">
        <el-form-item label="收件人" prop="name">
          <el-input v-model="newAddress.name" placeholder="请输入收件人姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="newAddress.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="省/市/区" prop="province">
          <el-cascader
            v-model="newAddressCascader"
            :options="cascaderLocationData"
            placeholder="请选择或输入省/市/区"
            style="width: 100%"
            filterable
            @change="
              (value) => {
                if (value && value.length >= 1) {
                  newAddress.province = value[0]
                }
                if (value && value.length >= 2) {
                  newAddress.city = value[1]
                }
                if (value && value.length >= 3) {
                  newAddress.district = value[2]
                }
                // 如果选择被清空
                if (!value || value.length === 0) {
                  newAddress.province = ''
                  newAddress.city = ''
                  newAddress.district = ''
                }
              }
            "
          />
        </el-form-item>
        <el-form-item label="街道" prop="street">
          <el-input v-model="newAddress.street" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="newAddress.tag" placeholder="选择地址标签">
            <el-option label="家" value="家" />
            <el-option label="公司" value="公司" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="addAddress">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑地址对话框 -->
    <el-dialog title="编辑地址" v-model="showEditDialog" width="50%">
      <el-form ref="editFormRef" :model="editingAddress" label-width="80px" :rules="addressRules">
        <el-form-item label="收件人" prop="name">
          <el-input v-model="editingAddress.name" placeholder="请输入收件人姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="editingAddress.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="省/市/区" prop="province">
          <el-cascader
            v-model="editAddressCascader"
            :options="cascaderLocationData"
            placeholder="请选择或输入省/市/区"
            style="width: 100%"
            filterable
            @change="
              (value) => {
                if (value && value.length >= 1) {
                  editingAddress.province = value[0]
                }
                if (value && value.length >= 2) {
                  editingAddress.city = value[1]
                }
                if (value && value.length >= 3) {
                  editingAddress.district = value[2]
                }
                // 如果选择被清空
                if (!value || value.length === 0) {
                  editingAddress.province = ''
                  editingAddress.city = ''
                  editingAddress.district = ''
                }
              }
            "
          />
        </el-form-item>
        <el-form-item label="街道" prop="street">
          <el-input v-model="editingAddress.street" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="editingAddress.tag" placeholder="选择地址标签">
            <el-option label="家" value="家" />
            <el-option label="公司" value="公司" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="updateAddress">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import pinia from '../../store'
import { useAuthStore } from '../../store/authStore'

const authStore = useAuthStore(pinia)

import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, Delete, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'

// Cascader location data
const cascaderLocationData = ref([])

// Fetch cascader location data from API
const fetchCascaderData = () => {
  axios
    .get(`${API_CONFIG.baseURL}${API_CONFIG.location.cascaderData}`)
    .then((response) => {
      if (response.data && response.data.data) {
        cascaderLocationData.value = response.data.data // API返回的是对象，需要提取其中的data数组
      }
    })
    .catch((error) => {
      console.error('加载地址数据失败:', error)
    })
}

const router = useRouter()

// 地址列表
const addresses = ref([])

// 新增地址对话框
const showAddDialog = ref(false)

// 编辑地址对话框
const showEditDialog = ref(false)

// 新增地址表单数据
const newAddress = ref({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  street: '',
  tag: '',
  isDefault: false
})

// Cascader value for new address
const newAddressCascader = ref([])

// 编辑地址表单数据
const editingAddress = ref({})

// Cascader value for editing address
const editAddressCascader = ref([])

// 地址验证规则
const addressRules = ref({
  name: [{ required: true, message: '请输入收件人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入11位有效的手机号码', trigger: 'blur' } // 验证手机号格式
  ],
  province: [{ required: true, message: '请输入省/区地址', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: false, message: '请输入区/县', trigger: 'blur' }], // 区/县改为非必填，因为部分城市可能没有区级划分
  street: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
})

// 从后端加载地址数据和级联选择器数据
onMounted(async () => {
  // Fetch cascader location data first
  fetchCascaderData()

  try {
    // 从localStorage获取当前用户ID
    const userId = String(authStore.userId || 1) || 1

    // 调用后端API获取地址列表
    const response = await axios.get(`${API_CONFIG.baseURL}/v1/users/${userId}/addresses`)

    if (response.data && response.data.code === '200') {
      // 将后端返回的地址数据映射为前端需要的格式
      let mappedAddresses = response.data.data.map((address) => ({
        id: address.id,
        name: address.receiverName, // 后端字段: receiverName -> 前端字段: name
        phone: address.receiverPhone, // 后端字段: receiverPhone -> 前端字段: phone
        province: address.province,
        city: address.city,
        district: address.district,
        street: address.detail, // 后端字段: detail -> 前端字段: street
        tag: address.tag || '', // 后端可能没有tag字段，默认为空
        isDefault: address.isDefault === 1 // 后端字段: isDefault(0/1) -> 前端字段: isDefault(boolean)
      }))

      // 排序：默认地址置顶
      mappedAddresses.sort((a, b) => (b.isDefault ? 1 : 0) - (a.isDefault ? 1 : 0))

      // 如果没有默认地址，设置第一个为默认
      if (mappedAddresses.length > 0 && !mappedAddresses.some((addr) => addr.isDefault)) {
        mappedAddresses[0].isDefault = true
      }

      // 更新地址列表
      addresses.value = mappedAddresses
    } else {
      ElMessage.error('加载地址数据失败：' + (response.data?.message || '未知错误'))
    }
  } catch (error) {
    console.error('加载地址失败:', error)
    ElMessage.error('加载地址失败，请稍后重试')

    // 异常情况下，显示空地址列表
    addresses.value = []
  }
})

// 新增地址
const addAddress = async () => {
  try {
    // 从localStorage获取当前用户ID
    const userId = String(authStore.userId || 1) || 1

    // 映射前端表单数据为后端需要的格式
    const addressData = {
      receiverName: newAddress.value.name, // 前端字段: name -> 后端字段: receiverName
      receiverPhone: newAddress.value.phone, // 前端字段: phone -> 后端字段: receiverPhone
      province: newAddress.value.province,
      city: newAddress.value.city,
      district: newAddress.value.district,
      detail: newAddress.value.street, // 前端字段: street -> 后端字段: detail
      isDefault: newAddress.value.isDefault ? 1 : 0, // 前端: boolean -> 后端: 0/1
      tag: newAddress.value.tag // 前端: tag -> 后端: tag
    }

    // 调用后端API新增地址
    const response = await axios.post(
      `${API_CONFIG.baseURL}/v1/users/${userId}/addresses`,
      addressData
    )

    if (response.data && response.data.code === '200') {
      // 重新加载地址列表
      try {
        // 从localStorage获取当前用户ID
        const userId = String(authStore.userId || 1) || 1
        // 调用后端API获取地址列表
        const response = await axios.get(`${API_CONFIG.baseURL}/v1/users/${userId}/addresses`)

        if (response.data && response.data.code === '200') {
          // 将后端返回的地址数据映射为前端需要的格式
          addresses.value = response.data.data.map((address) => ({
            id: address.id,
            name: address.receiverName, // 后端字段: receiverName -> 前端字段: name
            phone: address.receiverPhone, // 后端字段: receiverPhone -> 前端字段: phone
            province: address.province,
            city: address.city,
            district: address.district,
            street: address.detail, // 后端字段: detail -> 前端字段: street
            tag: address.tag || '', // 后端可能没有tag字段，默认为空
            isDefault: address.isDefault === 1 // 后端字段: isDefault(0/1) -> 前端字段: isDefault(boolean)
          }))

          // 如果没有默认地址，设置第一个为默认
          if (addresses.value.length > 0 && !addresses.value.some((addr) => addr.isDefault)) {
            addresses.value[0].isDefault = true
          }
        }
      } catch (error) {
        console.error('重新加载地址失败:', error)
      }
      // 重置表单
      newAddress.value = {
        name: '',
        phone: '',
        province: '',
        city: '',
        district: '',
        street: '',
        tag: '',
        isDefault: false
      }
      // 关闭对话框
      showAddDialog.value = false
      // 提示成功
      ElMessage.success('地址已新增')
    } else {
      ElMessage.error('新增地址失败：' + (response.data?.message || '未知错误'))
    }
  } catch (error) {
    console.error('新增地址失败:', error)
    ElMessage.error('新增地址失败，请稍后重试')
  }
}

// 编辑地址
const editAddress = (address) => {
  // 复制地址信息到编辑表单
  editingAddress.value = JSON.parse(JSON.stringify(address))
  // 初始化级联选择器值
  editAddressCascader.value = [address.province, address.city, address.district]
  // 显示编辑对话框
  showEditDialog.value = true
}

// 更新地址
const updateAddress = async () => {
  try {
    // 从localStorage获取当前用户ID
    const userId = String(authStore.userId || 1) || 1

    // 映射前端表单数据为后端需要的格式
    const addressData = {
      receiverName: editingAddress.value.name, // 前端字段: name -> 后端字段: receiverName
      receiverPhone: editingAddress.value.phone, // 前端字段: phone -> 后端字段: receiverPhone
      province: editingAddress.value.province,
      city: editingAddress.value.city,
      district: editingAddress.value.district,
      detail: editingAddress.value.street, // 前端字段: street -> 后端字段: detail
      isDefault: editingAddress.value.isDefault ? 1 : 0, // 前端: boolean -> 后端: 0/1
      tag: editingAddress.value.tag // 前端: tag -> 后端: tag
    }

    // 调用后端API更新地址
    const response = await axios.put(
      `${API_CONFIG.baseURL}/v1/users/${userId}/addresses/${editingAddress.value.id}`,
      addressData
    )

    if (response.data && response.data.code === '200') {
      // 重新加载地址列表
      const reloadResponse = await axios.get(`${API_CONFIG.baseURL}/v1/users/${userId}/addresses`)
      if (reloadResponse.data && reloadResponse.data.code === '200') {
        addresses.value = reloadResponse.data.data.map((address) => ({
          id: address.id,
          name: address.receiverName,
          phone: address.receiverPhone,
          province: address.province,
          city: address.city,
          district: address.district,
          street: address.detail,
          tag: address.tag || '',
          isDefault: address.isDefault === 1
        }))
      }

      // 关闭对话框
      showEditDialog.value = false

      // 提示成功
      ElMessage.success('地址已更新')
    } else {
      ElMessage.error('更新地址失败：' + (response.data?.message || '未知错误'))
    }
  } catch (error) {
    console.error('更新地址失败:', error)
    ElMessage.error('更新地址失败，请稍后重试')
  }
}

// 删除地址
const deleteAddress = async (address) => {
  // 如果是默认地址，不能删除
  if (address.isDefault) {
    ElMessage.warning('默认地址不能删除')
    return
  }

  try {
    // 从localStorage获取当前用户ID
    const userId = String(authStore.userId || 1) || 1

    // 调用后端API删除地址
    const response = await axios.delete(
      `${API_CONFIG.baseURL}/v1/users/${userId}/addresses/${address.id}`
    )

    if (response.data && response.data.code === '200') {
      // 重新加载地址列表
      const reloadResponse = await axios.get(`${API_CONFIG.baseURL}/v1/users/${userId}/addresses`)
      if (reloadResponse.data && reloadResponse.data.code === '200') {
        addresses.value = reloadResponse.data.data.map((addr) => ({
          id: addr.id,
          name: addr.receiverName,
          phone: addr.receiverPhone,
          province: addr.province,
          city: addr.city,
          district: addr.district,
          street: addr.detail,
          tag: addr.tag || '',
          isDefault: addr.isDefault === 1
        }))
      }

      // 提示成功
      ElMessage.success('地址已删除')
    } else {
      ElMessage.error('删除地址失败：' + (response.data?.message || '未知错误'))
    }
  } catch (error) {
    console.error('删除地址失败:', error)
    ElMessage.error('删除地址失败，请稍后重试')
  }
}

// 设置默认地址
const setDefault = async (address) => {
  try {
    // 从localStorage获取当前用户ID
    const userId = String(authStore.userId || 1) || 1

    // 映射前端地址数据为后端需要的格式
    const addressData = {
      receiverName: address.name,
      receiverPhone: address.phone,
      province: address.province,
      city: address.city,
      district: address.district,
      detail: address.street,
      isDefault: 1, // 设置为默认地址（1表示是，0表示否）
      tag: address.tag || ''
    }

    // 调用后端API更新地址
    const response = await axios.put(
      `${API_CONFIG.baseURL}/v1/users/${userId}/addresses/${address.id}`,
      addressData
    )

    if (response.data && response.data.code === '200') {
      // 重新加载地址列表
      const reloadResponse = await axios.get(`${API_CONFIG.baseURL}/v1/users/${userId}/addresses`)
      if (reloadResponse.data && reloadResponse.data.code === '200') {
        addresses.value = reloadResponse.data.data.map((addr) => ({
          id: addr.id,
          name: addr.receiverName,
          phone: addr.receiverPhone,
          province: addr.province,
          city: addr.city,
          district: addr.district,
          street: addr.detail,
          tag: addr.tag || '',
          isDefault: addr.isDefault === 1
        }))
      }

      // 提示成功
      ElMessage.success('默认地址已设置')
    } else {
      ElMessage.error('设置默认地址失败：' + (response.data?.message || '未知错误'))
    }
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置默认地址失败，请稍后重试')
  }
}
</script>

<style scoped lang="less">
.address-container {
  padding: 20px 30px;
  background-color: #fafafa;
  min-height: 100vh;

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 28px;

    /* 移除自定义返回按钮样式，使用组件默认样式 */

    h2 {
      font-size: 28px;
      font-weight: 600;
      color: #2c3e50;
      margin: 0;
    }
  }

  .address-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
    gap: 20px;
    margin-bottom: 30px;

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  .address-card {
    position: relative;
    padding: 24px;
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    border: 1px solid #e8e8e8;
    border-left: 4px solid #409eff; /* 默认蓝色左侧边框 */

    &:hover {
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
      transform: translateY(-2px);
    }

    &.default-address {
      border-left: 4px solid #67c23a;
      border-color: #67c23a;

      &::before {
        content: '默认地址';
        position: absolute;
        top: 24px;
        right: 24px;
        color: #67c23a;
        font-size: 14px;
        font-weight: 500;
        padding: 2px 12px;
        background-color: rgba(103, 194, 58, 0.1);
        border-radius: 4px;
      }
    }

    .address-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 14px;

      .address-name {
        font-size: 18px;
        font-weight: 600;
        color: #2c3e50;
      }

      .address-phone {
        color: #333;
        font-size: 15px;
        font-weight: 500;
      }
    }

    .address-details {
      margin-bottom: 18px;
      line-height: 1.6;

      .address-full {
        font-size: 15px;
        color: #555;
        margin-bottom: 8px;
        white-space: pre-line;
      }

      .address-tag {
        display: inline-block;
        color: #409eff;
        background-color: rgba(64, 158, 255, 0.1);
        padding: 4px 12px;
        border-radius: 4px;
        font-size: 13px;
        font-weight: 500;
      }
    }

    .address-actions {
      text-align: right;

      .el-button {
        margin-left: 8px;
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 120px 20px;
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

    .empty-icon {
      font-size: 80px;
      color: #e6e6e6;
      margin-bottom: 24px;
    }

    p {
      font-size: 19px;
      color: #999;
      margin-bottom: 36px;
    }
  }
}
</style>
