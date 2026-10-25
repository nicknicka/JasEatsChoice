<!--
组件名称：NutritionDetails
用途：营养素详情列表（蛋白质、碳水、脂肪、膳食纤维）
页面：AI高级分析页面
创建时间：2026-03-20
-->
<template>
  <view class="nutrition-details">
    <ProgressBar
      v-for="item in nutritionList"
      :key="item.name"
      :label="item.name"
      :value="item.value"
      :target="item.target"
      :percent="item.percent"
      :color="item.color"
      unit="g"
    />
  </view>
</template>

<script setup>
import ProgressBar from '@/components/common/ProgressBar.vue'

const props = defineProps({
  nutritionList: {
    type: Array,
    default: () => []
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.nutrition-details {
  // 样式由 ProgressBar 组件处理
}
</style>
