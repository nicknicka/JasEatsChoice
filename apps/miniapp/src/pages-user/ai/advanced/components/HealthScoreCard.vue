<!--
组件名称：HealthScoreCard
用途：健康评分卡片（含详细评分）
页面：AI高级分析页面
创建时间：2026-03-20
-->
<template>
  <view class="health-score">
    <CircularScore
      :score="healthScore"
      label="分"
      :details="scoreDetails"
    />
  </view>
</template>

<script setup>
import CircularScore from '@/components/common/CircularScore.vue'

const props = defineProps({
  healthScore: {
    type: Number,
    required: true
  },
  scoreDetails: {
    type: Array,
    default: () => []
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.health-score {
  // 样式由 CircularScore 组件处理
}
</style>
