<template>
  <div class="address-selector">
    <!-- 选中的地址显示 -->
    <div class="selected-address" v-if="selectedAddress" @click="openDialog">
      <div class="address-info">
        <div class="address-header">
          <el-icon><Location /></el-icon>
          <span class="address-tag" v-if="selectedAddress.isDefault">默认</span>
          <span class="contact-name">{{ selectedAddress.contactName }}</span>
          <span class="contact-phone">{{ selectedAddress.contactPhone }}</span>
        </div>
        <div class="address-detail">{{ selectedAddress.fullAddress }}</div>
      </div>
      <el-button type="text" @click.stop="openDialog">
        <el-icon><Edit /></el-icon>
        修改
      </el-button>
    </div>

    <!-- 未选择地址时的提示 -->
    <div class="no-address" v-else @click="openDialog">
      <el-icon><LocationInformation /></el-icon>
      <span class="hint-text">请选择收货地址</span>
      <el-button type="text">
        <el-icon><ArrowRight /></el-icon>
      </el-button>
    </div>

    <!-- 地址选择弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="选择收货地址"
      width="600px"
      @close="handleDialogClose"
    >
      <div class="address-list-container">
        <!-- 地址列表 -->
        <div v-if="addressList.length > 0" class="address-list">
          <div
            v-for="address in addressList"
            :key="address.id"
            class="address-item"
            :class="{ selected: selectedAddress?.id === address.id }"
            @click="selectAddress(address)"
          >
            <div class="address-item-content">
              <div class="address-item-header">
                <el-radio
                  v-model="selectedAddressId"
                  :label="address.id"
                  @change="selectAddress(address)"
                >
                  <span class="contact-name">{{ address.contactName }}</span>
                  <span class="contact-phone">{{ address.contactPhone }}</span>
                </el-radio>
                <el-tag v-if="address.isDefault" type="success" size="small" effect="plain">
                  默认
                </el-tag>
              </div>
              <div class="address-item-detail">{{ address.fullAddress }}</div>
            </div>
            <div class="address-item-actions">
              <el-button
                v-if="!address.isDefault"
                type="text"
                size="small"
                @click.stop="setDefault(address)"
              >
                设为默认
              </el-button>
              <el-button type="text" size="small" @click.stop="editAddress(address)">
                编辑
              </el-button>
              <el-button
                type="text"
                size="small"
                class="delete-btn"
                @click.stop="deleteAddress(address)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-address">
          <el-empty description="暂无收货地址">
            <el-button type="primary" @click="addAddress">添加新地址</el-button>
          </el-empty>
        </div>

        <!-- 新增地址按钮 -->
        <div class="add-address-btn" v-if="addressList.length > 0">
          <el-button type="primary" plain @click="addAddress" icon="Plus">
            添加新地址
          </el-button>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmSelect" :disabled="!selectedAddressId">
            确认选择
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 地址编辑弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEditMode ? '编辑地址' : '添加新地址'"
      width="500px"
      @close="handleEditDialogClose"
    >
      <el-form
        ref="addressFormRef"
        :model="addressForm"
        :rules="addressFormRules"
        label-width="80px"
      >
        <el-form-item label="联系人" prop="contactName">
          <el-input
            v-model="addressForm.contactName"
            placeholder="请输入联系人姓名"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="contactPhone">
          <el-input
            v-model="addressForm.contactPhone"
            placeholder="请输入手机号"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item label="地区" prop="district">
          <el-cascader
            v-model="addressForm.region"
            :options="regionOptions"
            :props="{ expandTrigger: 'hover' }"
            placeholder="请选择省/市/区"
            style="width: 100%"
            @change="handleRegionChange"
          />
        </el-form-item>

        <el-form-item label="详细地址" prop="detail">
          <el-input
            v-model="addressForm.detail"
            type="textarea"
            :rows="3"
            placeholder="请输入详细地址，如街道、楼栋号、单元室等"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="默认地址">
          <el-switch v-model="addressForm.isDefault" />
          <span class="form-tip">设为默认地址</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAddress" :loading="saving">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Location,
  LocationInformation,
  Edit,
  ArrowRight,
  Plus
} from '@element-plus/icons-vue'
import addressApi from '../../api/address'
import { useAuthStore } from '../../store/authStore'

const props = defineProps({
  modelValue: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const authStore = useAuthStore()
const userId = computed(() => parseInt(authStore.userId || '0', 10))

// 选中的地址ID
const selectedAddressId = ref(props.modelValue?.id || '')
// 选中的地址对象
const selectedAddress = ref(props.modelValue || null)
// 地址列表
const addressList = ref([])
// 弹窗显示状态
const dialogVisible = ref(false)
// 编辑弹窗显示状态
const editDialogVisible = ref(false)
// 是否编辑模式
const isEditMode = ref(false)
// 保存中状态
const saving = ref(false)

// 地址表单
const addressFormRef = ref(null)
const addressForm = ref({
  id: '',
  contactName: '',
  contactPhone: '',
  region: [],
  province: '',
  city: '',
  district: '',
  detail: '',
  fullAddress: '',
  isDefault: false
})

// 表单验证规则
const addressFormRules = {
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择地区', trigger: 'change' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在 5 到 200 个字符', trigger: 'blur' }
  ]
}

// 地区选项（简化版）
const regionOptions = ref([
  {
    value: '北京市',
    label: '北京市',
    children: [
      {
        value: '北京市',
        label: '北京市',
        children: [
          { value: '朝阳区', label: '朝阳区' },
          { value: '海淀区', label: '海淀区' },
          { value: '东城区', label: '东城区' },
          { value: '西城区', label: '西城区' },
          { value: '丰台区', label: '丰台区' },
          { value: '石景山区', label: '石景山区' }
        ]
      }
    ]
  },
  {
    value: '上海市',
    label: '上海市',
    children: [
      {
        value: '上海市',
        label: '上海市',
        children: [
          { value: '黄浦区', label: '黄浦区' },
          { value: '徐汇区', label: '徐汇区' },
          { value: '长宁区', label: '长宁区' },
          { value: '静安区', label: '静安区' },
          { value: '普陀区', label: '普陀区' },
          { value: '虹口区', label: '虹口区' }
        ]
      }
    ]
  },
  {
    value: '广东省',
    label: '广东省',
    children: [
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '天河区', label: '天河区' },
          { value: '越秀区', label: '越秀区' },
          { value: '海珠区', label: '海珠区' },
          { value: '荔湾区', label: '荔湾区' }
        ]
      },
      {
        value: '深圳市',
        label: '深圳市',
        children: [
          { value: '福田区', label: '福田区' },
          { value: '罗湖区', label: '罗湖区' },
          { value: '南山区', label: '南山区' },
          { value: '宝安区', label: '宝安区' }
        ]
      }
    ]
  }
])

// 监听 modelValue 变化
watch(
  () => props.modelValue,
  (newVal) => {
    selectedAddress.value = newVal
    selectedAddressId.value = newVal?.id || ''
  }
)

// 加载地址列表
const loadAddresses = async () => {
  if (userId.value <= 0) {
    ElMessage.warning('用户未登录')
    return
  }

  try {
    const response = await addressApi.getUserAddresses(userId.value)
    if (response.code === '200') {
      addressList.value = response.data || []

      // 如果没有选中的地址，且有默认地址，自动选中默认地址
      if (!selectedAddress.value && addressList.value.length > 0) {
        const defaultAddr = addressList.value.find(addr => addr.isDefault)
        if (defaultAddr) {
          selectAddress(defaultAddr)
        }
      }
    } else {
      ElMessage.error(response.message || '获取地址列表失败')
    }
  } catch (error) {
    console.error('加载地址列表失败:', error)
    ElMessage.error('加载地址列表失败')
  }
}

// 打开地址选择弹窗
const openDialog = () => {
  loadAddresses()
  dialogVisible.value = true
}

// 关闭地址选择弹窗
const handleDialogClose = () => {
  // 重置选择
  selectedAddressId.value = selectedAddress.value?.id || ''
}

// 选择地址
const selectAddress = (address) => {
  selectedAddressId.value = address.id
}

// 确认选择
const confirmSelect = () => {
  const address = addressList.value.find(addr => addr.id === selectedAddressId.value)
  if (address) {
    selectedAddress.value = address
    emit('update:modelValue', address)
    emit('change', address)
    dialogVisible.value = false
    ElMessage.success('地址已选择')
  }
}

// 设置默认地址
const setDefault = async (address) => {
  try {
    const response = await addressApi.setDefaultAddress(address.id, userId.value)
    if (response.code === '200') {
      ElMessage.success('已设为默认地址')
      loadAddresses()
    } else {
      ElMessage.error(response.message || '设置失败')
    }
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置默认地址失败')
  }
}

// 编辑地址
const editAddress = (address) => {
  isEditMode.value = true
  addressForm.value = {
    id: address.id,
    contactName: address.contactName,
    contactPhone: address.contactPhone,
    region: [address.province, address.city, address.district],
    province: address.province,
    city: address.city,
    district: address.district,
    detail: address.detail,
    fullAddress: address.fullAddress,
    isDefault: address.isDefault
  }
  editDialogVisible.value = true
}

// 删除地址
const deleteAddress = (address) => {
  ElMessageBox.confirm(
    `确定要删除该地址吗？${address.isDefault ? '这是默认地址。' : ''}`,
    '删除地址',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        const response = await addressApi.deleteAddress(address.id)
        if (response.code === '200') {
          ElMessage.success('删除成功')
          // 如果删除的是当前选中的地址，清空选择
          if (selectedAddress.value?.id === address.id) {
            selectedAddress.value = null
            selectedAddressId.value = ''
            emit('update:modelValue', null)
            emit('change', null)
          }
          loadAddresses()
        } else {
          ElMessage.error(response.message || '删除失败')
        }
      } catch (error) {
        console.error('删除地址失败:', error)
        ElMessage.error('删除地址失败')
      }
    })
    .catch(() => {
      // 用户取消删除
    })
}

// 添加新地址
const addAddress = () => {
  isEditMode.value = false
  addressForm.value = {
    id: '',
    contactName: '',
    contactPhone: '',
    region: [],
    province: '',
    city: '',
    district: '',
    detail: '',
    fullAddress: '',
    isDefault: false
  }
  editDialogVisible.value = true
}

// 关闭编辑弹窗
const handleEditDialogClose = () => {
  addressFormRef.value?.resetFields()
}

// 地区变化处理
const handleRegionChange = (value) => {
  if (value && value.length === 3) {
    addressForm.value.province = value[0]
    addressForm.value.city = value[1]
    addressForm.value.district = value[2]
  }
}

// 保存地址
const saveAddress = async () => {
  if (!addressFormRef.value) return

  await addressFormRef.value.validate(async (valid) => {
    if (!valid) return

    saving.value = true

    try {
      // 构建完整地址
      addressForm.value.fullAddress =
        `${addressForm.value.province}${addressForm.value.city}${addressForm.value.district}${addressForm.value.detail}`

      let response
      if (isEditMode.value) {
        // 更新地址
        response = await addressApi.updateAddress(addressForm.value.id, addressForm.value)
      } else {
        // 添加地址
        response = await addressApi.addAddress({
          userId: userId.value,
          ...addressForm.value
        })
      }

      if (response.code === '200') {
        ElMessage.success(isEditMode.value ? '修改成功' : '添加成功')
        editDialogVisible.value = false
        loadAddresses()
      } else {
        ElMessage.error(response.message || '保存失败')
      }
    } catch (error) {
      console.error('保存地址失败:', error)
      ElMessage.error('保存地址失败')
    } finally {
      saving.value = false
    }
  })
}

// 组件挂载时加载地址
onMounted(() => {
  if (userId.value > 0) {
    loadAddresses()
  }
})

// 暴露方法供父组件调用
defineExpose({
  loadAddresses,
  openDialog
})
</script>

<style scoped lang="less">
.address-selector {
  // 选中的地址显示
  .selected-address {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    background: #f8f9fa;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: #e9ecef;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .address-info {
      flex: 1;

      .address-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .el-icon {
          color: #67c23a;
          font-size: 1.286rem /* 原值: 18px */;
        }

        .address-tag {
          padding: 2px 8px;
          background: #67c23a;
          color: #fff;
          border-radius: 4px;
          font-size: 0.857rem /* 原值: 12px */;
        }

        .contact-name {
          font-weight: 600;
          color: #2c3e50;
          font-size: 1.071rem /* 原值: 15px */;
        }

        .contact-phone {
          color: #7f8c8d;
          font-size: 1rem /* 原值: 14px */;
        }
      }

      .address-detail {
        color: #5a6c7d;
        font-size: 1rem /* 原值: 14px */;
        line-height: 1.5;
        padding-left: 26px;
      }
    }
  }

  // 未选择地址提示
  .no-address {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: #fff8e1;
    border: 1px dashed #ffd54f;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: #ffecb3;
      border-color: #ffb300;
    }

    .el-icon {
      font-size: 1.429rem /* 原值: 20px */;
      color: #ffa000;
    }

    .hint-text {
      flex: 1;
      color: #f57c00;
      font-size: 1rem /* 原值: 14px */;
    }
  }
}

// 地址列表容器
.address-list-container {
  .address-list {
    max-height: 400px;
    overflow-y: auto;

    .address-item {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 16px;
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      margin-bottom: 12px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        border-color: #409eff;
        background: #f5f7fa;
      }

      &.selected {
        border-color: #409eff;
        background: rgba(64, 158, 255, 0.05);
      }

      &:last-child {
        margin-bottom: 0;
      }

      .address-item-content {
        flex: 1;

        .address-item-header {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 8px;

          .contact-name {
            font-weight: 600;
            color: #2c3e50;
            font-size: 1.071rem /* 原值: 15px */;
          }

          .contact-phone {
            color: #7f8c8d;
            font-size: 1rem /* 原值: 14px */;
          }
        }

        .address-item-detail {
          color: #5a6c7d;
          font-size: 1rem /* 原值: 14px */;
          line-height: 1.5;
          padding-left: 24px;
        }
      }

      .address-item-actions {
        display: flex;
        flex-direction: column;
        gap: 4px;

        .delete-btn {
          color: #f56c6c;

          &:hover {
            color: #f56c6c;
            background: rgba(245, 108, 108, 0.1);
          }
        }
      }
    }
  }

  .empty-address {
    padding: 40px 0;
    text-align: center;
  }

  .add-address-btn {
    margin-top: 16px;
    text-align: center;
  }
}

// 编辑弹窗样式
.form-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 0.929rem /* 原值: 13px */;
}
</style>
