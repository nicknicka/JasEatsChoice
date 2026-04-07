<template>
  <div class="address-selector">
    <!-- ===== 已选地址展示 ===== -->
    <div class="address-card" v-if="selectedAddress" @click="openDialog">
      <div class="card-stamp" v-if="selectedAddress.isDefault">
        <span>默认</span>
      </div>
      <div class="card-body">
        <div class="card-letter">
          <div class="letter-edge"></div>
          <div class="letter-content">
            <div class="recipient-row">
              <span class="recipient-name">{{ selectedAddress.contactName }}</span>
              <span class="recipient-phone">{{ selectedAddress.contactPhone }}</span>
            </div>
            <div class="address-line">
              <svg class="pin-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                <circle cx="12" cy="10" r="3"/>
              </svg>
              <span class="address-text">{{ selectedAddress.fullAddress }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="card-actions-hint">
        <span>点击切换</span>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </div>
    </div>

    <!-- ===== 未选地址提示 ===== -->
    <div class="address-card empty-card" v-else @click="openDialog">
      <div class="empty-visual">
        <div class="envelope-icon">
          <div class="envelope-flap"></div>
          <div class="envelope-body">
            <div class="envelope-heart">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
      <div class="empty-text">
        <span class="empty-title">添加收货地址</span>
        <span class="empty-sub">美食送到你手中</span>
      </div>
      <div class="card-actions-hint">
        <span>去添加</span>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </div>
    </div>

    <!-- ===== 地址选择抽屉 ===== -->
    <el-drawer
      v-model="dialogVisible"
      title="选择收货地址"
      direction="rtl"
      size="420px"
      :show-close="false"
      @close="handleDialogClose"
    >
      <template #header>
        <div class="drawer-header">
          <h3 class="drawer-title">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
            收货地址
          </h3>
          <button class="drawer-close-btn" @click="dialogVisible = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
      </template>

      <div class="drawer-body">
        <!-- 地址列表 -->
        <div v-if="addressList.length > 0" class="address-list">
          <div
            v-for="(address, index) in addressList"
            :key="address.id"
            class="address-envelope"
            :class="{ selected: selectedAddressId === address.id }"
            :style="{ animationDelay: `${index * 0.06}s` }"
            @click="selectAddress(address)"
          >
            <!-- 左侧选中指示 -->
            <div class="envelope-indicator">
              <div class="indicator-dot"></div>
            </div>

            <!-- 地址内容 -->
            <div class="envelope-content">
              <div class="envelope-top">
                <span class="env-name">{{ address.contactName }}</span>
                <span class="env-phone">{{ address.contactPhone }}</span>
                <span class="env-badge" v-if="address.isDefault">默认</span>
              </div>
              <div class="envelope-address">{{ address.fullAddress }}</div>
            </div>

            <!-- 操作按钮 -->
            <div class="envelope-actions">
              <button class="env-action-btn" @click.stop="editAddress(address)" title="编辑">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
              </button>
              <button
                class="env-action-btn"
                v-if="!address.isDefault"
                @click.stop="setDefault(address)"
                title="设为默认"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
              </button>
              <button class="env-action-btn danger" @click.stop="deleteAddress(address)" title="删除">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
                  <polyline points="3 6 5 6 21 6"/>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-illustration">
            <div class="empty-house">
              <svg viewBox="0 0 80 80" fill="none">
                <path d="M40 15L10 40h10v25h15V50h10v15h15V40h10L40 15z" stroke="currentColor" stroke-width="2" fill="none" stroke-linejoin="round"/>
                <rect x="33" y="55" width="14" height="10" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/>
              </svg>
            </div>
          </div>
          <p class="empty-msg">还没有收货地址</p>
          <p class="empty-sub-msg">添加一个地址，让美食找到你</p>
          <button class="add-first-btn" @click="addAddress">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            添加地址
          </button>
        </div>

        <!-- 新增地址 -->
        <button class="add-address-fab" v-if="addressList.length > 0" @click="addAddress">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          新增地址
        </button>
      </div>

      <!-- 底部确认 -->
      <div class="drawer-footer" v-if="addressList.length > 0">
        <button class="confirm-btn" :disabled="!selectedAddressId" @click="confirmSelect">
          确认选择
        </button>
      </div>
    </el-drawer>

    <!-- ===== 地址编辑弹窗 ===== -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEditMode ? '编辑地址' : '添加新地址'"
      width="460px"
      @close="handleEditDialogClose"
      class="address-edit-dialog"
    >
      <div class="edit-form-wrapper">
        <el-form
          ref="addressFormRef"
          :model="addressForm"
          :rules="addressFormRules"
          label-width="0"
          class="compact-form"
        >
          <div class="form-row dual">
            <el-form-item prop="contactName" class="form-field">
              <div class="field-label">联系人</div>
              <el-input
                v-model="addressForm.contactName"
                placeholder="姓名"
                maxlength="20"
              />
            </el-form-item>
            <el-form-item prop="contactPhone" class="form-field">
              <div class="field-label">手机号</div>
              <el-input
                v-model="addressForm.contactPhone"
                placeholder="手机号码"
                maxlength="11"
              />
            </el-form-item>
          </div>

          <el-form-item prop="region" class="form-field">
            <div class="field-label">所在地区</div>
            <el-cascader
              v-model="addressForm.region"
              :options="regionOptions"
              :props="{ expandTrigger: 'hover' }"
              placeholder="选择省/市/区"
              style="width: 100%"
              @change="handleRegionChange"
            />
          </el-form-item>

          <el-form-item prop="detail" class="form-field">
            <div class="field-label">详细地址</div>
            <el-input
              v-model="addressForm.detail"
              type="textarea"
              :rows="3"
              placeholder="街道、楼栋号、单元室等"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item class="form-field switch-field">
            <div class="switch-row">
              <div>
                <div class="field-label">设为默认地址</div>
                <div class="switch-hint">每次下单自动使用此地址</div>
              </div>
              <el-switch v-model="addressForm.isDefault" />
            </div>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="edit-dialog-footer">
          <button class="btn-cancel" @click="editDialogVisible = false">取消</button>
          <button class="btn-save" @click="saveAddress" :disabled="saving">
            {{ saving ? '保存中...' : '保存地址' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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

const selectedAddressId = ref(props.modelValue?.id || '')
const selectedAddress = ref(props.modelValue || null)
const addressList = ref([])
const dialogVisible = ref(false)
const editDialogVisible = ref(false)
const isEditMode = ref(false)
const saving = ref(false)

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

watch(
  () => props.modelValue,
  (newVal) => {
    selectedAddress.value = newVal
    selectedAddressId.value = newVal?.id || ''
  }
)

const loadAddresses = async () => {
  if (userId.value <= 0) {
    ElMessage.warning('用户未登录')
    return
  }

  try {
    const response = await addressApi.getUserAddresses(userId.value)
    if (response.code === '200') {
      addressList.value = response.data || []
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

const openDialog = () => {
  loadAddresses()
  dialogVisible.value = true
}

const handleDialogClose = () => {
  selectedAddressId.value = selectedAddress.value?.id || ''
}

const selectAddress = (address) => {
  selectedAddressId.value = address.id
}

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
    .catch(() => {})
}

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

const handleEditDialogClose = () => {
  addressFormRef.value?.resetFields()
}

const handleRegionChange = (value) => {
  if (value && value.length === 3) {
    addressForm.value.province = value[0]
    addressForm.value.city = value[1]
    addressForm.value.district = value[2]
  }
}

const saveAddress = async () => {
  if (!addressFormRef.value) return

  await addressFormRef.value.validate(async (valid) => {
    if (!valid) return

    saving.value = true

    try {
      addressForm.value.fullAddress =
        `${addressForm.value.province}${addressForm.value.city}${addressForm.value.district}${addressForm.value.detail}`

      let response
      if (isEditMode.value) {
        response = await addressApi.updateAddress(addressForm.value.id, addressForm.value)
      } else {
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

onMounted(() => {
  if (userId.value > 0) {
    loadAddresses()
  }
})

defineExpose({
  loadAddresses,
  openDialog
})
</script>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

// ============================================
// 设计令牌 — 暖意明信片
// ============================================
@clay:            @nordic-accent;          // #D4845A 陶土
@clay-deep:       #C0724A;                // 更深的陶土
@clay-glow:       rgba(212, 132, 90, 0.12);
@clay-glow-strong:rgba(212, 132, 90, 0.25);
@sage:            @nordic-green;           // #7BAE7F 鼠尾草
@sage-deep:       @nordic-green-dark;      // #4a7a4d
@sage-glow:       rgba(123, 174, 127, 0.15);
@ink:             #1A1A1A;
@ink-mid:         #5A564F;
@ink-soft:        #8C8579;
@ink-faint:       #B5AFA6;
@cream:           #FBF9F5;
@cream-warm:      #F7F3ED;
@cream-deep:      #EDE8DF;
@paper:           #FFFFFF;
@paper-edge:      #F0ECE5;
@rule-line:       #E5E0D8;

// 动画曲线
@spring:          cubic-bezier(0.34, 1.56, 0.64, 1);
@smooth:          cubic-bezier(0.4, 0, 0.2, 1);
@ease-out:        cubic-bezier(0.0, 0, 0.2, 1);

// ============================================
// 主卡片 — 已选地址
// ============================================
.address-selector {
  position: relative;
}

.address-card {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 0;
  background: @paper;
  border-radius: 14px;
  border: 1.5px solid @paper-edge;
  cursor: pointer;
  transition: all 0.35s @smooth;
  position: relative;
  overflow: hidden;

  // 顶部装饰纹理线
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(
      90deg,
      @clay 0%,
      @clay 20%,
      transparent 20%,
      transparent 25%,
      @clay 25%,
      @clay 45%,
      transparent 45%,
      transparent 50%,
      @clay 50%,
      @clay 70%,
      transparent 70%,
      transparent 75%,
      @clay 75%,
      @clay 100%
    );
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    border-color: @clay;
    box-shadow:
      0 2px 8px @clay-glow,
      0 8px 24px rgba(0,0,0,0.04);
    transform: translateY(-1px);

    &::before {
      opacity: 1;
    }

    .card-actions-hint {
      opacity: 1;
      transform: translateX(0);
    }
  }

  &:active {
    transform: translateY(0);
    box-shadow: 0 1px 4px @clay-glow;
  }

  // 邮戳
  .card-stamp {
    width: 48px;
    height: 48px;
    flex-shrink: 0;
    margin: 14px 0 14px 14px;
    border: 2px dashed @sage;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    transform: rotate(-6deg);
    opacity: 0.85;

    span {
      font-size: 11px;
      font-weight: 800;
      color: @sage-deep;
      letter-spacing: 1px;
      text-transform: uppercase;
    }
  }

  // 主体
  .card-body {
    flex: 1;
    min-width: 0;
    padding: 16px 8px 16px 14px;
  }

  .card-letter {
    position: relative;

    .letter-edge {
      position: absolute;
      left: 0;
      top: 2px;
      bottom: 2px;
      width: 2px;
      background: @clay;
      border-radius: 2px;
      opacity: 0.4;
    }

    .letter-content {
      padding-left: 10px;
    }

    .recipient-row {
      display: flex;
      align-items: baseline;
      gap: 10px;
      margin-bottom: 5px;

      .recipient-name {
        font-size: 16px;
        font-weight: 700;
        color: @ink;
        letter-spacing: -0.3px;
      }

      .recipient-phone {
        font-size: 13px;
        color: @ink-mid;
        font-weight: 500;
        font-variant-numeric: tabular-nums;
      }
    }

    .address-line {
      display: flex;
      align-items: flex-start;
      gap: 6px;

      .pin-icon {
        width: 14px;
        height: 14px;
        color: @clay;
        flex-shrink: 0;
        margin-top: 2px;
      }

      .address-text {
        font-size: 13px;
        color: @ink-soft;
        line-height: 1.55;
      }
    }
  }

  // 右侧切换提示
  .card-actions-hint {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 3px;
    padding: 14px 16px 14px 8px;
    opacity: 0;
    transform: translateX(6px);
    transition: all 0.3s @smooth;

    span {
      font-size: 12px;
      color: @clay;
      font-weight: 500;
    }

    svg {
      width: 14px;
      height: 14px;
      color: @clay;
    }
  }
}

// ============================================
// 空地址卡片
// ============================================
.empty-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  background: @cream;
  border: 2px dashed @cream-deep;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.35s @smooth;

  &:hover {
    border-color: @clay;
    background: linear-gradient(135deg, #FFF9F2, #FFF3E8);
    box-shadow: 0 4px 16px @clay-glow;

    .envelope-icon {
      transform: translateY(-2px);
    }

    .card-actions-hint {
      opacity: 1;
      transform: translateX(0);
    }

    .empty-title {
      color: @clay-deep;
    }
  }

  .empty-visual {
    flex-shrink: 0;

    .envelope-icon {
      width: 48px;
      height: 40px;
      position: relative;
      transition: transform 0.3s @spring;

      .envelope-flap {
        position: absolute;
        top: 0;
        left: 4px;
        right: 4px;
        height: 0;
        border-left: 20px solid transparent;
        border-right: 20px solid transparent;
        border-top: 14px solid @clay;
        opacity: 0.25;
        z-index: 1;
      }

      .envelope-body {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 30px;
        background: @clay;
        opacity: 0.15;
        border-radius: 0 0 6px 6px;

        .envelope-heart {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 14px;
          height: 14px;
          color: @clay;
          opacity: 0.6;

          svg {
            width: 100%;
            height: 100%;
          }
        }
      }
    }
  }

  .empty-text {
    display: flex;
    flex-direction: column;
    gap: 2px;

    .empty-title {
      font-size: 15px;
      font-weight: 600;
      color: @ink-mid;
      transition: color 0.2s ease;
    }

    .empty-sub {
      font-size: 12px;
      color: @ink-soft;
    }
  }
}

// ============================================
// 抽屉样式
// ============================================
:deep(.el-drawer) {
  border-radius: 16px 0 0 16px;
  box-shadow: -8px 0 32px rgba(0,0,0,0.08);
  overflow: hidden;
  background: @cream;
}

:deep(.el-drawer__header) {
  margin: 0;
  padding: 0;
  border-bottom: none;
}

:deep(.el-drawer__body) {
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 抽屉头部
.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 20px 16px;
  background: @paper;
  border-bottom: 1px solid @rule-line;

  .drawer-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0;
    font-size: 17px;
    font-weight: 700;
    color: @ink;
    letter-spacing: -0.3px;

    svg {
      width: 20px;
      height: 20px;
      color: @clay;
    }
  }

  .drawer-close-btn {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    border: none;
    background: @cream-warm;
    color: @ink-soft;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;

    svg {
      width: 16px;
      height: 16px;
    }

    &:hover {
      background: @cream-deep;
      color: @ink;
    }
  }
}

// 抽屉主体
.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 16px 80px;

  // 自定义滚动条
  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: @cream-deep;
    border-radius: 2px;
  }
}

// ============================================
// 地址列表项 — 信封卡片
// ============================================
.address-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.address-envelope {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 12px 14px 14px;
  background: @paper;
  border: 1.5px solid @paper-edge;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s @smooth;
  position: relative;
  animation: envelope-in 0.35s @ease-out both;

  &:hover {
    border-color: fade(@clay, 40%);
    background: #FFFCF8;
    box-shadow: 0 2px 10px @clay-glow;

    .envelope-actions {
      opacity: 1;
    }
  }

  &.selected {
    border-color: @clay;
    background: linear-gradient(135deg, #FFFAF5, #FFF5EC);
    box-shadow:
      0 2px 10px @clay-glow,
      inset 0 0 0 1px rgba(212, 132, 90, 0.06);

    .indicator-dot {
      background: @clay;
      box-shadow: 0 0 0 3px @clay-glow-strong;

      &::after {
        opacity: 1;
        transform: scale(1);
      }
    }

    .env-name {
      color: @clay-deep;
    }
  }

  // 选中指示器
  .envelope-indicator {
    flex-shrink: 0;
    padding-top: 2px;

    .indicator-dot {
      width: 18px;
      height: 18px;
      border-radius: 50%;
      border: 2px solid @ink-faint;
      background: transparent;
      transition: all 0.25s @spring;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        width: 6px;
        height: 6px;
        background: #fff;
        border-radius: 50%;
        transform: translate(-50%, -50%) scale(0);
        opacity: 0;
        transition: all 0.25s @spring;
      }
    }
  }

  // 内容
  .envelope-content {
    flex: 1;
    min-width: 0;

    .envelope-top {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;

      .env-name {
        font-size: 14px;
        font-weight: 700;
        color: @ink;
        letter-spacing: -0.2px;
        transition: color 0.2s ease;
      }

      .env-phone {
        font-size: 12px;
        color: @ink-mid;
        font-weight: 500;
        font-variant-numeric: tabular-nums;
      }

      .env-badge {
        padding: 1px 8px;
        background: linear-gradient(135deg, @nordic-green-light, darken(@nordic-green-light, 3%));
        color: @sage-deep;
        font-size: 10px;
        font-weight: 700;
        border-radius: 4px;
        letter-spacing: 0.5px;
      }
    }

    .envelope-address {
      font-size: 12px;
      color: @ink-soft;
      line-height: 1.6;
    }
  }

  // 操作按钮
  .envelope-actions {
    flex-shrink: 0;
    display: flex;
    gap: 2px;
    opacity: 0;
    transition: opacity 0.2s ease;

    .env-action-btn {
      width: 28px;
      height: 28px;
      border-radius: 6px;
      border: none;
      background: transparent;
      color: @ink-soft;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;

      svg {
        width: 14px;
        height: 14px;
      }

      &:hover {
        background: @cream-deep;
        color: @ink;
      }

      &.danger:hover {
        background: @nordic-red-light;
        color: @nordic-red;
      }
    }
  }
}

// ============================================
// 空状态
// ============================================
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 24px;

  .empty-illustration {
    width: 80px;
    height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;
    color: @ink-faint;

    .empty-house svg {
      width: 60px;
      height: 60px;
    }
  }

  .empty-msg {
    font-size: 15px;
    font-weight: 600;
    color: @ink-mid;
    margin: 0 0 4px;
  }

  .empty-sub-msg {
    font-size: 12px;
    color: @ink-soft;
    margin: 0 0 20px;
  }

  .add-first-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 10px 24px;
    background: @clay;
    color: #fff;
    border: none;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.25s @smooth;

    svg {
      width: 16px;
      height: 16px;
    }

    &:hover {
      background: @clay-deep;
      transform: translateY(-1px);
      box-shadow: 0 4px 12px @clay-glow-strong;
    }
  }
}

// ============================================
// 添加地址浮动按钮
// ============================================
.add-address-fab {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px;
  margin-top: 12px;
  background: transparent;
  border: 2px dashed @cream-deep;
  border-radius: 10px;
  color: @clay;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s @smooth;

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    border-color: @clay;
    background: linear-gradient(135deg, #FFFAF5, #FFF5EC);
    color: @clay-deep;
  }
}

// ============================================
// 底部确认按钮
// ============================================
.drawer-footer {
  padding: 12px 16px 16px;
  background: @paper;
  border-top: 1px solid @rule-line;

  .confirm-btn {
    width: 100%;
    padding: 13px;
    background: linear-gradient(135deg, @clay, @clay-deep);
    color: #fff;
    border: none;
    border-radius: 12px;
    font-size: 15px;
    font-weight: 700;
    letter-spacing: 0.3px;
    cursor: pointer;
    transition: all 0.3s @smooth;
    box-shadow: 0 2px 10px @clay-glow;

    &:hover:not(:disabled) {
      box-shadow: 0 4px 18px @clay-glow-strong;
      transform: translateY(-1px);
    }

    &:active:not(:disabled) {
      transform: translateY(0);
    }

    &:disabled {
      background: @cream-deep;
      color: @ink-faint;
      box-shadow: none;
      cursor: not-allowed;
    }
  }
}

// ============================================
// 编辑弹窗
// ============================================
:deep(.address-edit-dialog) {
  .el-dialog {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 16px 48px rgba(0,0,0,0.1);
  }

  .el-dialog__header {
    padding: 20px 24px 16px;
    margin: 0;
    border-bottom: 1px solid @rule-line;
    background: @paper;

    .el-dialog__title {
      font-size: 17px;
      font-weight: 700;
      color: @ink;
      letter-spacing: -0.3px;
    }
  }

  .el-dialog__body {
    padding: 20px 24px;
    background: @cream;
  }

  .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid @rule-line;
    background: @paper;
  }
}

.edit-form-wrapper {
  .compact-form {
    .form-row.dual {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
    }

    .form-field {
      margin-bottom: 16px;

      .field-label {
        font-size: 12px;
        font-weight: 600;
        color: @ink-mid;
        margin-bottom: 6px;
        letter-spacing: 0.2px;
      }
    }

    .switch-field {
      .switch-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 12px 14px;
        background: @paper;
        border-radius: 10px;
        border: 1px solid @paper-edge;

        .switch-hint {
          font-size: 11px;
          color: @ink-soft;
          margin-top: 2px;
        }
      }
    }

    :deep(.el-input__wrapper),
    :deep(.el-textarea__inner) {
      border-radius: 8px;
      border: 1px solid @paper-edge;
      background: @paper;
      box-shadow: none;
      transition: all 0.2s ease;

      &:hover {
        border-color: @clay;
      }

      &.is-focus {
        border-color: @clay;
        box-shadow: 0 0 0 3px @clay-glow;
      }
    }

    :deep(.el-cascader) {
      width: 100%;

      .el-input__wrapper {
        border-radius: 8px;
      }
    }

    :deep(.el-switch.is-checked .el-switch__core) {
      background-color: @clay;
      border-color: @clay;
    }

    :deep(.el-form-item__error) {
      font-size: 11px;
      padding-top: 4px;
    }
  }
}

.edit-dialog-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;

  .btn-cancel {
    padding: 9px 20px;
    background: transparent;
    border: 1.5px solid @cream-deep;
    border-radius: 10px;
    color: @ink-mid;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      border-color: @ink-soft;
      color: @ink;
    }
  }

  .btn-save {
    padding: 9px 24px;
    background: linear-gradient(135deg, @clay, @clay-deep);
    border: none;
    border-radius: 10px;
    color: #fff;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.25s @smooth;
    box-shadow: 0 2px 8px @clay-glow;

    &:hover:not(:disabled) {
      box-shadow: 0 4px 14px @clay-glow-strong;
      transform: translateY(-1px);
    }

    &:disabled {
      background: @cream-deep;
      color: @ink-faint;
      box-shadow: none;
      cursor: not-allowed;
    }
  }
}

// ============================================
// 动画
// ============================================
@keyframes envelope-in {
  from {
    opacity: 0;
    transform: translateX(16px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>
