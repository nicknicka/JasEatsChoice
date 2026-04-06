<template>
  <div class="page-container">
    <h1>${page}</h1>
    <p>This is the ${page} page.</p>
  </div>
</template>

<script setup>
// Add ${page} specific functionality here
</script>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.page-container {
  padding: @nordic-space-xl;
  background: @nordic-bg;
  min-height: 100%;
}
</style>
