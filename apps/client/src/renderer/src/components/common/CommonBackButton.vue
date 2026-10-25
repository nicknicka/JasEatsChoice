<template>
  <el-button :type="type" :size="size" @click="handleClick" :class="className">
    <el-icon><ArrowLeft /></el-icon>
    {{ text }}
  </el-button>
</template>

<script setup>
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 组件属性
const props = defineProps({
  // 按钮类型
  type: {
    type: String,
    default: 'text'
  },
  // 按钮大小
  size: {
    type: String,
    default: 'large'
  },
  // 按钮文本
  text: {
    type: String,
    default: '返回'
  },
  // 自定义类名
  className: {
    type: String,
    default: 'back-btn'
  },
  // 是否使用路由返回还是浏览器返回
  useRouterBack: {
    type: Boolean,
    default: true
  }
})

// 按钮点击事件
const emit = defineEmits(['click'])

const handleClick = () => {
  if (props.useRouterBack) {
    try {
      router.go(-1)
    } catch (error) {
      console.error('路由返回失败:', error)
      ElMessage.error('返回失败，请手动返回')
    }
  }
  // 发出点击事件，允许父组件处理额外逻辑
  emit('click')
}
</script>

<style scoped lang="less">
// 默认样式
.back-btn {
  .el-button__text {
    margin-left: 5px;
  }
}
</style>
