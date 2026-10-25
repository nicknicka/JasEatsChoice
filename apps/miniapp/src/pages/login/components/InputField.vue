<template>
  <view class="input-field-wrapper">
    <view class="input-item" :class="{ 'captcha-item': !!captcha, 'autocomplete-item': showHistory }">
      <view class="input-icon">
        <uni-icons
          :type="resolvedIcon.type"
          :size="resolvedIcon.size"
          :color="resolvedIcon.color"
        ></uni-icons>
      </view>
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
      <view
        v-if="clearable && modelValue"
        class="clear-icon"
        @click.stop="handleClear"
      >
        <uni-icons type="closeempty" size="16" color="#8D8177"></uni-icons>
      </view>

      <!-- 历史记录下拉按钮 -->
      <view
        v-if="showHistory"
        class="dropdown-icon"
        @click.stop="toggleHistory"
      >
        <uni-icons type="bottom" size="14" color="#8D8177"></uni-icons>
      </view>

      <!-- 密码切换按钮 -->
      <view v-if="toggle" class="password-toggle" @click.stop="toggle.onClick">
        <uni-icons :type="toggle.icon" size="18" :color="toggle.color"></uni-icons>
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
        <view
          class="refresh-icon"
          @click="captcha.onRefresh"
        >
          <uni-icons type="refresh" size="18" color="#8D8177"></uni-icons>
        </view>
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
import { computed, ref, watch } from 'vue'

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

const resolvedIcon = computed(() => {
  const iconConfigMap = {
    phone: { type: 'phone', size: 18, color: '#C46739' },
    locked: { type: 'locked', size: 18, color: '#C46739' },
    checkmarkempty: { type: 'checkmarkempty', size: 18, color: '#C46739' }
  }

  return iconConfigMap[props.icon] || { type: 'compose', size: 18, color: '#C46739' }
})

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
  background: rgba(255, 250, 243, 0.9);
  border-radius: 50rpx;
  padding: 0 40rpx;
  height: 90rpx;
  border: 1rpx solid rgba(111, 141, 113, 0.14);
  box-shadow: 0 18rpx 34rpx rgba(92, 76, 58, 0.08);
  position: relative;
}

.input-icon {
  margin-right: 10rpx;
  display: flex;
  align-items: center;
}

.input-item .input-field {
  flex: 1;
  font-size: 28rpx;
  height: 100%;
  color: #3b332d;
}

.clear-icon {
  cursor: pointer;
  padding: 8rpx;
  font-size: 18px;
  color: #8d8177;
  transition: all 0.2s;
}

.clear-icon:active {
  transform: scale(0.9);
}

.dropdown-icon {
  cursor: pointer;
  padding: 10rpx;
  font-size: 16px;
  color: #8d8177;
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
  min-width: 180rpx;
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 28rpx;
  font-size: 24rpx;
  color: #c46739;
  background: rgba(214, 119, 71, 0.1);
  border: none;
  border-left: 1rpx solid rgba(111, 141, 113, 0.12);
  border-radius: 999rpx;
  font-weight: 600;
}

.code-btn:disabled {
  color: #a69a8f;
  background: rgba(183, 169, 153, 0.16);
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
  background-color: #f6efe5;
  border: 1rpx solid rgba(111, 141, 113, 0.12);
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
  color: #b55e34;
  background: rgba(255, 249, 244, 0.96);
  border-radius: 20rpx;
  margin-top: 12rpx;
  z-index: 10;
  animation: slideDown 0.2s ease-out;
  border: 1rpx solid rgba(214, 119, 71, 0.16);
  box-shadow: 0 12rpx 22rpx rgba(181, 94, 52, 0.1);
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
  background: rgba(255, 250, 244, 0.98);
  border-radius: 20rpx;
  border: 1rpx solid rgba(111, 141, 113, 0.12);
  box-shadow: 0 20rpx 36rpx rgba(92, 76, 58, 0.12);
  z-index: 100;
  max-height: 400rpx;
  overflow-y: auto;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25rpx 30rpx;
  border-bottom: 1rpx solid rgba(111, 141, 113, 0.08);
}

.history-item:last-child {
  border-bottom: none;
}

.history-phone {
  font-size: 28rpx;
  color: #3b332d;
}

.delete-icon {
  cursor: pointer;
  padding: 10rpx;
  font-size: 16px;
  color: #8d8177;
}
</style>
