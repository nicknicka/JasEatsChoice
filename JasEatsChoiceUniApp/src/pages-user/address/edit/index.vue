<template>
  <view class="address-edit-container">
    <scroll-view class="scroll-container" scroll-y>
      <!-- 收货人信息 -->
      <view class="form-section card">
        <view class="section-title">收货人信息</view>

        <view class="form-item">
          <view class="form-label">收货人</view>
          <input
            class="form-input"
            v-model="formData.name"
            placeholder="请输入收货人姓名"
            :maxlength="20"
          />
        </view>

        <view class="form-item">
          <view class="form-label">手机号码</view>
          <input
            class="form-input"
            v-model="formData.phone"
            type="number"
            placeholder="请输入手机号码"
            :maxlength="11"
          />
        </view>
      </view>

      <!-- 收货地址 -->
      <view class="form-section card">
        <view class="section-title">收货地址</view>

        <view class="form-item clickable" @click="showRegionPicker">
          <view class="form-label">所在地区</view>
          <view class="form-value" :class="{ placeholder: !formData.regionText }">
            {{ formData.regionText || '请选择省/市/区' }}
          </view>
          <text class="form-arrow">→</text>
        </view>

        <view class="form-item">
          <view class="form-label">详细地址</view>
          <textarea
            class="form-textarea"
            v-model="formData.detail"
            placeholder="街道、楼牌号等详细信息"
            :maxlength="200"
            :auto-height="true"
          />
          <view class="form-count">{{ formData.detail.length }}/200</view>
        </view>
      </view>

      <!-- 地址标签 -->
      <view class="form-section card">
        <view class="section-title">
          <text>地址标签</text>
          <text class="section-tips">（选填）</text>
        </view>

        <view class="tags-grid">
          <view
            class="tag-item"
            :class="{ active: formData.tags.includes(tag) }"
            v-for="tag in defaultTags"
            :key="tag"
            @click="toggleTag(tag)"
          >
            {{ tag }}
          </view>
        </view>

        <view class="custom-tag" v-if="showCustomTag">
          <input
            class="custom-tag-input"
            v-model="customTagText"
            placeholder="输入自定义标签"
            :maxlength="10"
          />
          <button class="custom-tag-btn" @click="addCustomTag">确定</button>
        </view>

        <view class="add-custom-tag" @click="showCustomTag = true" v-else>
          <text class="add-icon">+</text>
          <text>添加自定义标签</text>
        </view>
      </view>

      <!-- 默认地址 -->
      <view class="form-section card">
        <view class="default-item">
          <view class="default-info">
            <view class="default-title">设为默认地址</view>
            <view class="default-tips">下次下单时会优先使用该地址</view>
          </view>
          <switch
            :checked="formData.isDefault"
            @change="toggleDefault"
            color="#FF6B35"
          />
        </view>
      </view>
    </scroll-view>

    <!-- 底部保存栏 -->
    <view class="bottom-bar">
      <button class="save-btn" @click="saveAddress" :disabled="!canSave">
        保存地址
      </button>
    </view>

    <!-- 地区选择器 -->
    <picker
      mode="region"
      :value="regionValue"
      @change="onRegionChange"
      v-if="showPicker"
    >
      <view></view>
    </picker>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { addressApi } from '@/api'

// Store
const userStore = useUserStore()

// 表单数据
const formData = ref({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  regionText: '',
  detail: '',
  tags: [],
  isDefault: false
})

// 默认标签
const defaultTags = ref(['家', '公司', '学校', '周末配送'])

// 自定义标签
const showCustomTag = ref(false)
const customTagText = ref('')

// 地区选择
const regionValue = ref([])
const showPicker = ref(false)

// 是否是编辑模式
const isEdit = ref(false)
const addressId = ref('')

// 表单验证
const canSave = computed(() => {
  return (
    formData.value.name.trim() &&
    formData.value.phone.trim() &&
    formData.value.province &&
    formData.value.city &&
    formData.value.district &&
    formData.value.detail.trim()
  )
})

/**
 * 切换标签
 */
const toggleTag = (tag) => {
  const index = formData.value.tags.indexOf(tag)
  if (index > -1) {
    formData.value.tags.splice(index, 1)
  } else {
    if (formData.value.tags.length < 3) {
      formData.value.tags.push(tag)
    } else {
      uni.showToast({
        title: '最多选择3个标签',
        icon: 'none'
      })
    }
  }
}

/**
 * 添加自定义标签
 */
const addCustomTag = () => {
  const tag = customTagText.value.trim()
  if (!tag) {
    uni.showToast({
      title: '请输入标签内容',
      icon: 'none'
    })
    return
  }

  if (formData.value.tags.includes(tag)) {
    uni.showToast({
      title: '标签已存在',
      icon: 'none'
    })
    return
  }

  if (formData.value.tags.length >= 3) {
    uni.showToast({
      title: '最多选择3个标签',
      icon: 'none'
    })
    return
  }

  formData.value.tags.push(tag)
  defaultTags.value.push(tag)
  customTagText.value = ''
  showCustomTag.value = false
}

/**
 * 显示地区选择器
 * 添加超时处理，默认30秒超时
 */
const showRegionPicker = () => {
  showPicker.value = true

  uni.showLoading({
    title: '请选择地址...'
  })

  // 创建超时定时器
  const timer = setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '选择地址超时',
      icon: 'none'
    })
  }, 30000)

  uni.chooseLocation({
    success: (res) => {
      clearTimeout(timer)
      uni.hideLoading()

      // 如果用户通过地图选择了位置，提取地区信息
      if (res.address) {
        // 简单的地址解析（实际项目中可能需要使用地图API的逆地理编码）
        const address = res.address
        // 这里可以根据实际情况解析省市区信息
        // 暂时使用原有逻辑，显示picker
        showPicker.value = true
      }
    },
    fail: (err) => {
      clearTimeout(timer)
      uni.hideLoading()

      // 用户取消选择或失败时，使用picker
      if (!err.errMsg || !err.errMsg.includes('cancel')) {
        console.error('选择地址失败:', err)
      }
      // 无论如何都显示picker作为备选方案
      showPicker.value = true
    }
  })
}

/**
 * 地区选择变化
 */
const onRegionChange = (e) => {
  const value = e.detail.value
  formData.value.province = value[0]
  formData.value.city = value[1]
  formData.value.district = value[2]
  formData.value.regionText = value.join(' ')
  regionValue.value = value
  showPicker.value = false
}

/**
 * 切换默认地址
 */
const toggleDefault = (e) => {
  formData.value.isDefault = e.detail.value
}

/**
 * 验证手机号
 */
const validatePhone = (phone) => {
  return /^1[3-9]\d{9}$/.test(phone)
}

/**
 * 保存地址
 */
const saveAddress = async () => {
  // 表单验证
  if (!formData.value.name.trim()) {
    uni.showToast({
      title: '请输入收货人姓名',
      icon: 'none'
    })
    return
  }

  if (!formData.value.phone.trim()) {
    uni.showToast({
      title: '请输入手机号码',
      icon: 'none'
    })
    return
  }

  if (!validatePhone(formData.value.phone)) {
    uni.showToast({
      title: '请输入正确的手机号码',
      icon: 'none'
    })
    return
  }

  if (!formData.value.province || !formData.value.city || !formData.value.district) {
    uni.showToast({
      title: '请选择所在地区',
      icon: 'none'
    })
    return
  }

  if (!formData.value.detail.trim()) {
    uni.showToast({
      title: '请输入详细地址',
      icon: 'none'
    })
    return
  }

  try {
    uni.showLoading({
      title: '保存中...'
    })

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    // 准备地址数据
    const addressData = {
      userId,
      receiverName: formData.value.name,
      receiverPhone: formData.value.phone,
      province: formData.value.province,
      city: formData.value.city,
      district: formData.value.district,
      detailAddress: formData.value.detail,
      isDefault: formData.value.isDefault,
      tags: formData.value.tags
    }

    if (isEdit.value) {
      // 更新地址
      await addressApi.update(addressId.value, addressData)
    } else {
      // 创建地址
      await addressApi.create(addressData)
    }

    uni.hideLoading()

    uni.showToast({
      title: isEdit.value ? '修改成功' : '添加成功',
      icon: 'success'
    })

    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    console.error('保存地址失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '保存失败，请重试',
      icon: 'none'
    })
  }
}

/**
 * 加载地址详情（编辑模式）
 */
const loadAddressDetail = async (id) => {
  try {
    uni.showLoading({
      title: '加载中...'
    })

    const res = await addressApi.getDetail(id)

    // 数据映射
    formData.value = {
      name: res.receiverName || res.name,
      phone: res.receiverPhone || res.phone,
      province: res.province || '',
      city: res.city || '',
      district: res.district || '',
      regionText: `${res.province || ''} ${res.city || ''} ${res.district || ''}`.trim(),
      detail: res.detailAddress || res.detail || '',
      tags: res.tags || [],
      isDefault: res.isDefault || false
    }

    // 更新地区选择器的值
    if (res.province && res.city && res.district) {
      regionValue.value = [res.province, res.city, res.district]
    }

    uni.hideLoading()
  } catch (error) {
    console.error('加载地址详情失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '加载失败，请重试',
      icon: 'none'
    })
  }
}

// 组件挂载
onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.data) {
    // 编辑模式
    isEdit.value = true
    const data = JSON.parse(decodeURIComponent(options.data))
    addressId.value = data.id
    formData.value = {
      name: data.name || '',
      phone: data.phone || '',
      province: data.province || '',
      city: data.city || '',
      district: data.district || '',
      regionText: `${data.province || ''} ${data.city || ''} ${data.district || ''}`,
      detail: data.detail || '',
      tags: data.tags || [],
      isDefault: data.isDefault || false
    }

    if (data.province && data.city && data.district) {
      regionValue.value = [data.province, data.city, data.district]
    }
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.address-edit-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 120rpx;
}

.scroll-container {
  height: calc(100vh - 120rpx);
}

.card {
  background-color: $bg-color-white;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-md;

  .section-tips {
    font-size: $font-size-sm;
    color: $text-color-secondary;
    font-weight: $font-weight-normal;
    margin-left: $spacing-xs;
  }
}

/* 表单项 */
.form-item {
  @include flex-between;
  align-items: flex-start;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color-lighter;

  &:last-child {
    border-bottom: none;
  }

  &.clickable {
    &:active {
      background-color: $bg-color-base;
    }
  }
}

.form-label {
  width: 160rpx;
  flex-shrink: 0;
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}

.form-input {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
  text-align: right;
}

.form-value {
  flex: 1;
  font-size: $font-size-base;
  text-align: right;

  &.placeholder {
    color: $text-color-placeholder;
  }
}

.form-arrow {
  flex-shrink: 0;
  margin-left: $spacing-sm;
  color: $text-color-secondary;
  font-size: $font-size-base;
}

.form-textarea {
  flex: 1;
  min-height: 120rpx;
  padding: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  font-size: $font-size-base;
  color: $text-color-primary;
  line-height: $line-height-lg;
  margin-top: $spacing-sm;
}

.form-count {
  text-align: right;
  font-size: $font-size-sm;
  color: $text-color-secondary;
  margin-top: $spacing-sm;
}

/* 标签选择 */
.tags-grid {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}

.tag-item {
  padding: $spacing-sm $spacing-md;
  font-size: $font-size-base;
  color: $text-color-regular;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  border: 1rpx solid $border-color-base;

  &.active {
    color: $primary-color;
    background-color: rgba(255, 107, 53, 0.1);
    border-color: $primary-color;
    font-weight: $font-weight-medium;
  }
}

.add-custom-tag {
  @include flex-center;
  gap: $spacing-xs;
  padding: $spacing-md;
  border: 1rpx dashed $border-color-base;
  border-radius: $border-radius-base;
  color: $text-color-secondary;
  font-size: $font-size-sm;

  .add-icon {
    font-size: $font-size-lg;
  }
}

.custom-tag {
  @include flex-center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
}

.custom-tag-input {
  flex: 1;
  height: 60rpx;
  padding: 0 $spacing-sm;
  background-color: $bg-color-white;
  border: 1rpx solid $border-color-base;
  border-radius: $border-radius-sm;
  font-size: $font-size-base;
}

.custom-tag-btn {
  padding: $spacing-sm $spacing-md;
  background-color: $primary-color;
  color: #fff;
  font-size: $font-size-sm;
  border-radius: $border-radius-sm;
  border: none;
}

/* 默认地址 */
.default-item {
  @include flex-between;
  align-items: center;
}

.default-info {
  flex: 1;
}

.default-title {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
  margin-bottom: $spacing-xs;
}

.default-tips {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 底部保存栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: $z-index-fixed;
  @include safe-area-bottom;
}

.save-btn {
  width: 100%;
  height: 88rpx;
  @include flex-center;
  background: linear-gradient(135deg, $primary-color, #FF8F61);
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-round;
  border: none;

  &:disabled {
    opacity: 0.6;
  }
}
</style>
