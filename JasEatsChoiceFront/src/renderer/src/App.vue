<script setup>
// App.vue now acts as a root container for router views
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'

// 路由监听：路由切换时自动滚动到页面顶部
const route = useRoute()

watch(
  () => route.path,
  () => {
    // 路由变化时，延迟滚动到顶部
    setTimeout(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }, 100)
  },
  { immediate: false } // 不在首次加载时触发
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
</template>

<style lang="less">
/* 移除 scoped，这是根容器样式 */
.app-container {
  height: 100vh;
  width: 100%;
  overflow: hidden; /* 防止出现双滚动条 */
}
</style>
