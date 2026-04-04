<template>
  <div class="divider-element">
    <div class="divider-line"></div>
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

.divider-line {
  height: 1px;
  background: #e8e8f8;
  border-radius: 1px;
}
</style>
