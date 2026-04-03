<template>
  <div class="divider-element">
    <el-divider />
  </div>
</template>

<script setup>
defineProps({
  element: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.divider-element {
  width: 100%;
  padding: 4px 0;
}
</style>
