<script setup>
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import LoginTransitionOverlay from './components/LoginTransitionOverlay.vue'

const route = useRoute()

watch(
  () => route.path,
  () => {
    setTimeout(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }, 100)
  },
  { immediate: false }
)

onMounted(() => {
  // App 组件挂载时的初始化逻辑
})
</script>

<template>
  <div class="app-container">
    <router-view v-slot="{ Component }">
      <keep-alive>
        <component :is="Component"></component>
      </keep-alive>
    </router-view>
  </div>
  <!-- 登录过渡动画覆盖层 -->
  <LoginTransitionOverlay />
</template>

<style lang="less">
/* 移除 scoped，这是根容器样式 */
.app-container {
  height: 100vh;
  width: 100%;
  overflow: hidden; /* 防止出现双滚动条 */
}
</style>
