<template>
  <div class="placeholder-container">
    <el-empty description="功能开发中...">
      <el-button type="primary" @click="goBack">返回</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="less">
.placeholder-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60vh;
}
</style>
