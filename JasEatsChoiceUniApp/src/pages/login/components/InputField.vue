<template>
  <view class="input-field-wrapper">
    <view class="input-item" :class="{ 'captcha-item': !!captcha, 'autocomplete-item': showHistory }">
      <text class="input-icon">{{ iconMap[icon] || '📝' }}</text>
      <input
        class="input-field"
        :type="type"
        :password="isPassword && !showPassword"
        :value="modelValue"
        @input="handleInput"
        :placeholder="placeholder"
        :maxlength="maxlength"
        @blur="handleBlur"
        @focus="handleFocus"
      />

      <!-- 清除按钮 -->
      <text
        v-if="clearable && modelValue"
        class="clear-icon"
        @click.stop="handleClear"
      >✕</text>

      <!-- 历史记录下拉按钮 -->
      <text
        v-if="showHistory"
        class="dropdown-icon"
        @click.stop="toggleHistory"
      >▼</text>

      <!-- 密码切换按钮 -->
      <view v-if="toggle" class="password-toggle" @click.stop="toggle.onClick">
        <text class="eye-icon" :style="{ color: toggle.color }">
          {{ toggle.icon === 'eye-filled' ? '👁️' : '👁️‍🗨️' }}
        </text>
      </view>

      <!-- 验证码按钮 -->
      <button
        v-if="button"
        class="code-btn"
        :disabled="button.disabled"
        @click.stop="button.onClick"
      >
        {{ button.text }}
      </button>

      <!-- 验证码图片 -->
      <view v-if="captcha" class="captcha-wrapper">
        <image
          class="captcha-img"
          :src="captcha.image"
          mode="aspectFit"
          @click="captcha.onRefresh"
        />
        <text
          class="refresh-icon"
          @click="captcha.onRefresh"
        >🔄</text>
      </view>
    </view>

    <!-- 错误提示 -->
    <transition name="fade">
      <view v-if="error" class="input-error-tip">
        {{ error }}
      </view>
    </transition>

    <!-- 历史记录下拉列表 -->
    <view v-if="showHistoryList" class="history-list">
      <view
        v-for="item in historyItems"
        :key="item.phone"
        class="history-item"
        @click="selectHistory(item)"
      >
        <view class="history-phone">{{ item.phone }}</view>
        <text
          class="delete-icon"
          @click.stop="deleteHistory(item.phone)"
        >✕</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'

// Emoji 图标映射表
const iconMap = {
  phone: '📱',
  locked: '🔒',
  checkmarkempty: '✓',
  clear: '✕',
  down: '▼',
  eye: '👁️',
  'eye-filled': '👁️',
  refreshempty: '🔄'
}

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'text'
  },
  icon: {
    type: String,
    required: true
  },
  placeholder: {
    type: String,
    default: ''
  },
  maxlength: {
    type: Number,
    default: 20
  },
  error: {
    type: String,
    default: ''
  },
  clearable: {
    type: Boolean,
    default: false
  },
  showHistory: {
    type: Boolean,
    default: false
  },
  historyItems: {
    type: Array,
    default: () => []
  },
  button: {
    type: Object,
    default: null
  },
  captcha: {
    type: Object,
    default: null
  },
  toggle: {
    type: Object,
    default: null
  },
  // 新增：是否为密码输入框
  isPassword: {
    type: Boolean,
    default: false
  },
  // 新增：密码是否显示
  showPassword: {
    type: Boolean,
    default: false
  },
  // 新增：验证函数
  validateFn: {
    type: Function,
    default: null
  },
  // 新增：验证参数
  validateOptions: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'blur', 'focus', 'selectHistory', 'deleteHistory', 'validate'])

const showHistoryList = ref(false)

const handleInput = (e) => {
  const newValue = e.detail.value
  emit('update:modelValue', newValue)

  // 实时验证：如果输入内容且提供了验证函数，则触发验证
  if (newValue && props.validateFn) {
    emit('validate', newValue)
  }
}

const handleBlur = (e) => {
  emit('blur', e)
}

const handleFocus = (e) => {
  emit('focus', e)
  if (props.showHistory && props.historyItems.length > 0) {
    showHistoryList.value = true
  }
}

const handleClear = () => {
  emit('update:modelValue', '')
  // 清空时也触发验证（清除错误提示）
  emit('validate', '')
}

const toggleHistory = () => {
  showHistoryList.value = !showHistoryList.value
}

const selectHistory = (item) => {
  emit('selectHistory', item)
  showHistoryList.value = false
}

const deleteHistory = (phone) => {
  emit('deleteHistory', phone)
}

// 监听 modelValue 变化，当为空时隐藏历史记录
watch(() => props.modelValue, (newVal) => {
  if (!newVal) {
    showHistoryList.value = false
  }
})
</script>

<style scoped>
.input-field-wrapper {
  position: relative;
}

.input-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 50rpx;
  padding: 0 40rpx;
  height: 90rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
  position: relative;
}

.input-icon {
  font-size: 20px;
  margin-right: 10rpx;
  display: flex;
  align-items: center;
}

.input-item .input-field {
  flex: 1;
  font-size: 28rpx;
  height: 100%;
}

.clear-icon {
  cursor: pointer;
  padding: 8rpx;
  font-size: 18px;
  color: #999;
  transition: all 0.2s;
}

.clear-icon:active {
  transform: scale(0.9);
}

.dropdown-icon {
  cursor: pointer;
  padding: 10rpx;
  font-size: 16px;
  color: #999;
}

.password-toggle {
  cursor: pointer;
  padding: 10rpx;
  display: flex;
  align-items: center;
}

.eye-icon {
  font-size: 20px;
}

.code-btn {
  padding: 0 30rpx;
  font-size: 24rpx;
  color: #FF6B35;
  background: transparent;
  border: none;
  border-left: 1rpx solid #eee;
}

.code-btn:disabled {
  color: #999;
}

.captcha-item {
  padding-right: 180rpx;
}

.captcha-wrapper {
  position: absolute;
  right: 20rpx;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 10rpx;
  z-index: 10;
}

.captcha-img {
  width: 200rpx;
  height: 70rpx;
  border-radius: 10rpx;
  background-color: #f5f7fa;
}

.refresh-icon {
  cursor: pointer;
  padding: 5rpx;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 错误提示 */
.input-error-tip {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  padding: 12rpx 20rpx;
  font-size: 22rpx;
  color: #FF6B35;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20rpx;
  margin-top: 12rpx;
  z-index: 10;
  animation: slideDown 0.2s ease-out;
  box-shadow: 0 2rpx 10rpx rgba(255, 107, 53, 0.1);
}

/* 淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10rpx);
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 历史记录 */
.history-list {
  position: absolute;
  top: 100rpx;
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.15);
  z-index: 100;
  max-height: 400rpx;
  overflow-y: auto;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.history-item:last-child {
  border-bottom: none;
}

.history-phone {
  font-size: 28rpx;
  color: #333;
}

.delete-icon {
  cursor: pointer;
  padding: 10rpx;
  font-size: 16px;
  color: #999;
}
</style>
