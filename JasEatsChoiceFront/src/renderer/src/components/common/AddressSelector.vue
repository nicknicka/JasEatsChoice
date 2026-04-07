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
      <div class="edit-trigger" @click.stop="openDialog">
        <el-icon :size="16"><Edit /></el-icon>
      </div>
    </div>

    <!-- 未选择地址时的提示 -->
    <div class="no-address" v-else @click="openDialog">
      <el-icon class="no-address-icon"><LocationInformation /></el-icon>
      <span class="hint-text">请选择收货地址</span>
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
@import '../../assets/css/nordic-theme.less';

// ===== 设计令牌 =====
@terracotta: @nordic-accent;
@terracotta-dark: @nordic-accent-dark;
@terracotta-glow: rgba(212, 132, 90, 0.15);
@sage: @nordic-green;
@sage-light: @nordic-green-light;
@ink: @nordic-text;
@ink-sec: @nordic-text-secondary;
@ink-muted: @nordic-text-muted;
@warm-bg: @nordic-bg;
@warm-surface: @nordic-surface;
@warm-border: @nordic-border;
@warm-divider: @nordic-divider;

.address-selector {
  // ===== 已选地址卡片 =====
  .selected-address {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 18px 20px;
    background: linear-gradient(135deg, #FDFBF8 0%, #FAF5EE 100%);
    border-radius: @nordic-radius-lg;
    border: 1.5px solid @warm-border;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    // 左侧陶土色装饰条
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: linear-gradient(180deg, @terracotta, @terracotta-dark);
      border-radius: 4px 0 0 4px;
    }

    &:hover {
      border-color: @terracotta;
      box-shadow: 0 4px 20px @terracotta-glow;
      transform: translateY(-1px);

      .edit-trigger {
        color: @terracotta;
        background: @nordic-accent-light;
      }
    }

    .address-info {
      flex: 1;
      padding-left: 8px;

      .address-header {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 8px;

        .el-icon {
          color: @terracotta;
          font-size: 18px;
          filter: drop-shadow(0 1px 2px @terracotta-glow);
        }

        .address-tag {
          padding: 2px 10px;
          background: linear-gradient(135deg, @sage, darken(@sage, 5%));
          color: #fff;
          border-radius: @nordic-radius-pill;
          font-size: @nordic-text-xs;
          font-weight: 600;
          letter-spacing: 0.5px;
          box-shadow: 0 2px 6px rgba(123, 174, 127, 0.3);
        }

        .contact-name {
          font-weight: 700;
          color: @ink;
          font-size: 15px;
          letter-spacing: -0.2px;
        }

        .contact-phone {
          color: @ink-sec;
          font-size: @nordic-text-base;
          font-weight: 500;
        }
      }

      .address-detail {
        color: @ink-sec;
        font-size: @nordic-text-base;
        line-height: 1.6;
        padding-left: 28px;
      }
    }

    .edit-trigger {
      flex-shrink: 0;
      width: 36px;
      height: 36px;
      border-radius: 10px;
      background: @warm-divider;
      display: flex;
      align-items: center;
      justify-content: center;
      color: @ink-muted;
      transition: all 0.2s ease;

      :deep(.el-button) {
        padding: 0;
        border: none;
        background: none;
        width: 100%;
        height: 100%;
        border-radius: 10px;
      }
    }
  }

  // ===== 未选择地址提示 =====
  .no-address {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 18px 20px;
    background: linear-gradient(135deg, #FFF9F2 0%, #FFF3E8 100%);
    border: 2px dashed @nordic-accent-light;
    border-radius: @nordic-radius-lg;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;

    // 右侧装饰箭头
    &::after {
      content: '';
      position: absolute;
      right: 16px;
      top: 50%;
      transform: translateY(-50%);
      width: 6px;
      height: 6px;
      border-right: 2px solid @terracotta;
      border-bottom: 2px solid @terracotta;
      transform: translateY(-50%) rotate(-45deg);
      opacity: 0.5;
      transition: all 0.2s ease;
    }

    &:hover {
      border-color: @terracotta;
      background: linear-gradient(135deg, #FFF5EB 0%, #FFEFE0 100%);
      box-shadow: 0 4px 16px @terracotta-glow;

      &::after {
        opacity: 1;
        right: 12px;
      }

      .no-address-icon {
        transform: scale(1.1);
      }

      .hint-text {
        color: @terracotta-dark;
      }
    }

    .no-address-icon,
    :deep(.el-icon) {
      font-size: 22px;
      color: @terracotta;
      transition: transform 0.2s ease;
    }

    .hint-text {
      flex: 1;
      color: @terracotta;
      font-size: @nordic-text-md;
      font-weight: 500;
      transition: color 0.2s ease;
    }
  }
}

// ===== 地址选择弹窗 =====
:deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12);
}

:deep(.el-dialog__header) {
  margin: 0;
  padding: 0;
  border-bottom: 1px solid @warm-border;
  background: linear-gradient(135deg, #FAF0E8 0%, #F4E6DE 100%);

  .el-dialog__title {
    font-family: 'Noto Serif SC', 'Georgia', serif;
    font-weight: 700;
    color: @ink;
    letter-spacing: -0.2px;
  }
}

:deep(.el-dialog__body) {
  padding: 16px 20px;
  background: #FDFBF8;
}

:deep(.el-dialog__footer) {
  padding: 12px 20px;
  border-top: 1px solid @warm-border;
  background: #FDFBF8;
}

// ===== 地址列表容器 =====
.address-list-container {
  .address-list {
    max-height: 400px;
    overflow-y: auto;
    padding-right: 4px;

    // 自定义滚动条
    &::-webkit-scrollbar {
      width: 5px;
    }
    &::-webkit-scrollbar-track {
      background: transparent;
    }
    &::-webkit-scrollbar-thumb {
      background: @warm-border;
      border-radius: 3px;

      &:hover {
        background: @ink-muted;
      }
    }

    .address-item {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 16px 18px;
      border: 1.5px solid @warm-border;
      border-left: 3px solid @warm-border;
      border-radius: @nordic-radius-md;
      margin-bottom: 10px;
      cursor: pointer;
      background: @warm-surface;
      transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;

      // 序号标识
      &:last-child {
        margin-bottom: 0;
      }

      &:hover {
        border-color: fade(@terracotta, 40%);
        border-left-color: @terracotta;
        background: #FFFAF5;
        box-shadow: 0 2px 12px @terracotta-glow;
        transform: translateX(2px);
      }

      &.selected {
        border-color: @terracotta;
        border-left-color: @terracotta;
        background: linear-gradient(135deg, #FFFAF5 0%, #FFF5EC 100%);
        box-shadow: 0 2px 12px @terracotta-glow;

        // 选中标记角标
        &::after {
          content: '';
          position: absolute;
          top: 0;
          right: 0;
          width: 0;
          height: 0;
          border-style: solid;
          border-width: 0 28px 28px 0;
          border-color: transparent @terracotta transparent transparent;
          border-radius: 0 10px 0 0;
        }

        &::before {
          content: '✓';
          position: absolute;
          top: 2px;
          right: 3px;
          color: #fff;
          font-size: 10px;
          font-weight: 700;
          z-index: 2;
        }

        .address-item-content {
          .address-item-header {
            .contact-name {
              color: @terracotta-dark;
            }
          }
        }
      }

      .address-item-content {
        flex: 1;

        .address-item-header {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 6px;

          // Element Plus Radio 覆盖
          :deep(.el-radio) {
            .el-radio__input {
              .el-radio__inner {
                border-color: @warm-border;

                &:hover {
                  border-color: @terracotta;
                }
              }

              &.is-checked {
                .el-radio__inner {
                  border-color: @terracotta;
                  background: @terracotta;
                  box-shadow: 0 2px 6px @terracotta-glow;
                }
              }
            }

            .el-radio__label {
              display: inline-flex;
              align-items: center;
              gap: 10px;
            }
          }

          .contact-name {
            font-weight: 700;
            color: @ink;
            font-size: 15px;
            letter-spacing: -0.2px;
            transition: color 0.2s ease;
          }

          .contact-phone {
            color: @ink-sec;
            font-size: @nordic-text-base;
            font-weight: 500;
          }

          // 默认标签覆盖
          :deep(.el-tag) {
            border-radius: @nordic-radius-pill;
            border: none;
            background: linear-gradient(135deg, @sage-light, darken(@sage-light, 3%));
            color: darken(@sage, 10%);
            font-weight: 600;
            font-size: @nordic-text-xs;
            padding: 0 10px;
          }
        }

        .address-item-detail {
          color: @ink-sec;
          font-size: @nordic-text-base;
          line-height: 1.6;
          padding-left: 24px;
        }
      }

      .address-item-actions {
        display: flex;
        flex-direction: column;
        gap: 2px;
        flex-shrink: 0;
        margin-left: 8px;

        :deep(.el-button) {
          font-size: @nordic-text-xs;
          padding: 4px 8px;
          border-radius: @nordic-radius-sm;
          transition: all 0.2s ease;

          &:hover {
            background: @warm-divider;
          }
        }

        .delete-btn {
          :deep(.el-button) {
            color: @nordic-red;

            &:hover {
              color: darken(@nordic-red, 10%);
              background: @nordic-red-light;
            }
          }
        }
      }
    }
  }

  // 空状态
  .empty-address {
    padding: 48px 20px;
    text-align: center;

    :deep(.el-empty) {
      .el-empty__description {
        color: @ink-muted;
        font-size: @nordic-text-md;
      }

      .el-empty__image {
        width: 80px;
        height: 80px;

        svg {
          fill: @warm-border;
        }
      }
    }
  }

  // 添加按钮
  .add-address-btn {
    margin-top: 14px;

    :deep(.el-button) {
      width: 100%;
      height: 44px;
      border-radius: @nordic-radius-md;
      border: 2px dashed @warm-border;
      background: transparent;
      color: @terracotta;
      font-weight: 600;
      font-size: @nordic-text-base;
      transition: all 0.25s ease;

      &:hover {
        border-color: @terracotta;
        background: linear-gradient(135deg, #FFFAF5 0%, #FFF5EC 100%);
        box-shadow: 0 2px 12px @terracotta-glow;
        color: @terracotta-dark;
      }
    }
  }
}

// ===== 底部按钮 =====
:deep(.dialog-footer) {
  display: flex;
  justify-content: flex-end;
  gap: 10px;

  .el-button {
    border-radius: 10px;
    font-weight: 600;
    min-width: 88px;
    height: 38px;
    transition: all 0.25s ease;
  }

  .el-button--default {
    border-color: @warm-border;
    color: @ink-sec;

    &:hover {
      border-color: @terracotta;
      color: @terracotta;
      background: #FFFAF5;
    }
  }

  .el-button--primary {
    background: linear-gradient(135deg, @terracotta, @terracotta-dark);
    border: none;
    box-shadow: 0 2px 8px @terracotta-glow;

    &:hover {
      box-shadow: 0 4px 16px rgba(212, 132, 90, 0.3);
      transform: translateY(-1px);
    }

    &.is-disabled {
      background: @warm-border;
      box-shadow: none;
      transform: none;
      cursor: not-allowed;
    }
  }
}

// ===== 编辑弹窗内表单 =====
.form-tip {
  margin-left: 12px;
  color: @ink-muted;
  font-size: @nordic-text-sm;
}

:deep(.el-form) {
  .el-form-item__label {
    color: @ink-sec;
    font-weight: 500;
  }

  .el-input__wrapper,
  .el-textarea__inner {
    border-radius: @nordic-radius-sm;
    transition: all 0.2s ease;

    &:hover {
      box-shadow: 0 0 0 1px @terracotta inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px @terracotta inset, 0 0 0 3px @terracotta-glow;
    }
  }

  .el-cascader {
    width: 100%;
  }

  .el-switch {
    &.is-checked .el-switch__core {
      background-color: @terracotta;
      border-color: @terracotta;
    }
  }
}

// ===== 入场动画 =====
@keyframes addr-card-in {
  from {
    opacity: 0;
    transform: translateY(8px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.address-list .address-item {
  animation: addr-card-in 0.3s ease both;

  &:nth-child(1) { animation-delay: 0.04s; }
  &:nth-child(2) { animation-delay: 0.08s; }
  &:nth-child(3) { animation-delay: 0.12s; }
  &:nth-child(4) { animation-delay: 0.16s; }
  &:nth-child(5) { animation-delay: 0.20s; }
}
</style>
