<template>
  <div class="common-image-container" :style="containerStyle" :class="containerClass">
    <el-image
      v-if="src"
      :src="src"
      :fit="fit"
      :lazy="lazy"
      :preview-src-list="preview ? [src] : []"
      :preview-teleported="true"
      class="common-image"
      :class="{ 'is-clickable': preview || clickable }"
      @error="handleError"
      @load="handleLoad"
    >
      <!-- 加载中占位符 -->
      <template #placeholder>
        <div class="image-placeholder" :class="placeholderClass">
          <slot name="placeholder">
            <el-icon class="is-loading placeholder-icon" :size="iconSize">
              <Loading />
            </el-icon>
            <span v-if="showLoadingText" class="placeholder-text">加载中...</span>
          </slot>
        </div>
      </template>

      <!-- 加载失败占位符 -->
      <template #error>
        <div class="image-error" :class="placeholderClass">
          <slot name="error">
            <span class="error-icon">{{ errorIcon }}</span>
            <span v-if="showErrorText" class="error-text">{{ errorText }}</span>
          </slot>
        </div>
      </template>
    </el-image>

    <!-- 当没有 src 时直接显示占位符 -->
    <div v-else class="image-placeholder" :class="placeholderClass">
      <slot name="error">
        <span class="error-icon">{{ errorIcon }}</span>
        <span v-if="showErrorText" class="error-text">{{ errorText }}</span>
      </slot>
    </div>

    <!-- 开发模式：显示调试信息 -->
    <div v-if="isDevelopment && showDebug" class="debug-info">
      <small>
        <span v-if="!src" class="debug-warning">⚠️ 无src</span>
        <span v-if="hasError" class="debug-error">❌ 加载失败</span>
        <span v-if="isLoaded" class="debug-success">✅ 已加载</span>
      </small>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'

// 检测开发模式
const isDevelopment = computed(() => {
  return import.meta.env.MODE === 'development' ||
         import.meta.env.DEV ||
         window.location.hostname === 'localhost'
})

// Props 定义
const props = defineProps({
  // 图片地址
  src: {
    type: String,
    default: ''
  },
  // 容器宽度
  width: {
    type: [String, Number],
    default: '100%'
  },
  // 容器高度
  height: {
    type: [String, Number],
    default: '100%'
  },
  // 图片填充方式：fill, contain, cover, none, scale-down
  fit: {
    type: String,
    default: 'cover',
    validator: (value) => ['fill', 'contain', 'cover', 'none', 'scale-down'].includes(value)
  },
  // 是否懒加载
  lazy: {
    type: Boolean,
    default: true
  },
  // 是否支持点击预览
  preview: {
    type: Boolean,
    default: false
  },
  // 是否可点击（用于自定义点击事件）
  clickable: {
    type: Boolean,
    default: false
  },
  // 圆角大小
  radius: {
    type: [String, Number],
    default: '8px'
  },
  // 加载失败的图标（emoji或文字）
  errorIcon: {
    type: String,
    default: '🖼️'
  },
  // 加载失败的提示文字
  errorText: {
    type: String,
    default: '加载失败'
  },
  // 是否显示加载失败文字
  showErrorText: {
    type: Boolean,
    default: true
  },
  // 是否显示加载中文字
  showLoadingText: {
    type: Boolean,
    default: false
  },
  // 占位符图标大小
  iconSize: {
    type: [String, Number],
    default: 32
  },
  // 占位符背景色
  placeholderBg: {
    type: String,
    default: '#f5f7fa'
  },
  // 是否显示调试信息（仅开发模式）
  showDebug: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['load', 'error', 'click'])

// 状态管理
const hasError = ref(false)
const isLoaded = ref(false)

// 容器样式
const containerStyle = computed(() => ({
  width: typeof props.width === 'number' ? `${props.width}px` : props.width,
  height: typeof props.height === 'number' ? `${props.height}px` : props.height,
  borderRadius: typeof props.radius === 'number' ? `${props.radius}px` : props.radius
}))

// 容器类名
const containerClass = computed(() => ({
  'is-preview': props.preview,
  'is-clickable': props.clickable
}))

// 占位符类名
const placeholderClass = computed(() => ({
  'has-text': props.showLoadingText || props.showErrorText
}))

// 处理加载成功
const handleLoad = (e) => {
  hasError.value = false
  isLoaded.value = true
  emit('load', e)
}

// 处理加载失败
const handleError = (e) => {
  hasError.value = true
  isLoaded.value = false
  emit('error', e)
}

// 组件挂载时初始化
onMounted(() => {
  if (isDevelopment.value && props.showDebug) {
    console.log('🖼️ [CommonImage] 组件已挂载:', {
      src: props.src,
      width: props.width,
      height: props.height,
      fit: props.fit
    })
  }
})
</script>

<style scoped>
.common-image-container {
  position: relative;
  display: inline-block;
  overflow: hidden;
  background-color: v-bind(placeholderBg);
  transition: all 0.3s ease;
}

.common-image {
  width: 100%;
  height: 100%;
  display: block;
}

.common-image.is-clickable {
  cursor: pointer;
}

.common-image.is-clickable:hover {
  opacity: 0.9;
}

/* 占位符样式 */
.image-placeholder,
.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
  color: #909399;
}

.image-error {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
}

.placeholder-icon,
.error-icon {
  font-size: v-bind(iconSize);
  margin-bottom: 8px;
}

.error-icon {
  font-size: calc(v-bind(iconSize) * 1.5);
}

.placeholder-text,
.error-text {
  font-size: 14px;
  color: #909399;
}

.has-text .placeholder-icon,
.has-text .error-icon {
  margin-bottom: 4px;
}

/* 调试信息样式 */
.debug-info {
  position: absolute;
  top: 4px;
  left: 4px;
  padding: 2px 6px;
  background: rgba(0, 0, 0, 0.7);
  border-radius: 4px;
  z-index: 10;
  pointer-events: none;
}

.debug-info small {
  color: white;
  font-size: 10px;
}

.debug-warning {
  color: #e6a23c;
}

.debug-error {
  color: #f56c6c;
}

.debug-success {
  color: #67c23a;
}

/* Element Plus image 样式覆盖 */
.common-image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  transition: all 0.3s ease;
}

.common-image.is-clickable :deep(.el-image__inner):hover {
  transform: scale(1.02);
}
</style>
