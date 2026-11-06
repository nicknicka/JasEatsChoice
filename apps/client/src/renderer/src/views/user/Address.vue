<template>
  <div class="address-container">
    <!-- 页头：标准版，标题 --text-h1 + 上下 --space-6；返回与主操作统一收进右侧动作区（§4.7） -->
    <AppPageHeader title="管理地址" class="address-header">
      <template #actions>
        <CommonBackButton type="default" size="default" />
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          新增地址
        </el-button>
      </template>
    </AppPageHeader>

    <div class="address-list">
      <el-card
        v-for="address in addresses"
        :key="address.id"
        class="address-card stagger-item"
        :class="{ 'default-address': address.isDefault }"
        shadow="never"
      >
        <div class="address-card__head">
          <div class="address-card__identity">
            <span class="address-name">{{ address.name }}</span>
            <span class="address-phone">{{ address.phone }}</span>
            <!-- 默认用明确的文字标签表达，取代绿/蓝边框（评审 P0） -->
            <span v-if="address.isDefault" class="address-default-tag">默认</span>
          </div>

          <div class="address-actions">
            <el-button type="text" @click="editAddress(address)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="text" danger @click="deleteAddress(address)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
            <el-button v-if="!address.isDefault" type="text" @click="setDefault(address)">
              设置默认
            </el-button>
          </div>
        </div>

        <div class="address-full">
          {{ address.province }} {{ address.city }} {{ address.district }} {{ address.street }}
        </div>

        <div v-if="address.tag" class="address-tag">{{ address.tag }}</div>
      </el-card>

      <!-- 无数据：图标 + 中文说明 + 替代动作（§4.5） -->
      <AppEmptyState
        v-if="addresses.length === 0"
        class="empty-state"
        :icon="Location"
        description="您还没有添加任何地址"
        action-text="新增地址"
        @action="showAddDialog = true"
      />
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
import { Edit, Delete, Plus, Location } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
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
/* 页面颜色、间距、圆角、字阶与动效全部取自 tokens.css（§3、§7.1），无裸色值。 */
.address-container {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-12);
  background: var(--canvas);
}

/* 页头动作区控件统一 40px（§3.4、§4.7）。 */
.address-header :deep(.el-button) {
  height: var(--control-height-md);
}

/* 地址卡栅格：单列 1200px 内两列，首屏至少露出两张完整卡片（§3.3、§4.2）。 */
.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(420px, 1fr));
  gap: var(--card-gap);

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

/* 地址卡统一 L1 微质感（§3.4）：--surface 底 + 1px --line 描边 + 顶部内高光，
   不再用彩色左边框表达状态（评审 P0、§4.2）。 */
.address-card {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
  transition:
    box-shadow var(--duration-fast) var(--ease-out),
    transform var(--duration-fast) var(--ease-out);

  :deep(.el-card__body) {
    padding: var(--card-padding);
  }

  &:hover {
    box-shadow: inset 0 1px 0 var(--surface-highlight), var(--shadow-sm);
    transform: translateY(-1px);
  }
}

.address-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-2);
}

.address-card__identity {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-1) var(--space-3);
  min-width: 0;
}

.address-name {
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

.address-phone {
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
  font-size: var(--text-body-sm);
  color: var(--ink-500);
}

/* 「默认」用明确的文字标签表达（评审 P0、§3.1），品牌浅底不抢占状态色。 */
.address-default-tag {
  display: inline-flex;
  align-items: center;
  padding: 1px var(--space-2);
  border-radius: var(--radius-full);
  background: var(--brand-50);
  color: var(--brand-700);
  font-size: var(--text-caption);
  line-height: 18px;
  font-weight: var(--weight-semibold);
}

.address-full {
  font-size: var(--text-body-sm);
  line-height: 20px;
  color: var(--ink-700);
}

/* 分类标签（家/公司/其他）用中性胶囊，不与品牌主色与状态色争语义（§3.1）。 */
.address-tag {
  display: inline-flex;
  align-items: center;
  margin-top: var(--space-2);
  padding: 1px var(--space-2);
  border-radius: var(--radius-full);
  background: var(--neutral-bg);
  color: var(--neutral);
  font-size: var(--text-caption);
  line-height: 18px;
}

/* 行内次级动作统一 32px（§3.4）。 */
.address-actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  gap: var(--space-1);

  :deep(.el-button) {
    height: var(--control-height-sm);
    margin-left: 0;
    font-size: var(--text-body-sm);
  }
}

.empty-state {
  grid-column: 1 / -1;
}
</style>
