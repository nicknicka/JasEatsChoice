<!--
组件名称：NutritionChart
用途：营养素环形图展示
页面：AI高级分析页面
创建时间：2026-03-20
-->
<template>
  <view class="nutrition-chart">
    <canvas canvas-id="nutritionCanvas" id="nutritionCanvas" class="chart-canvas"></canvas>
    <view class="chart-center">
      <text class="total-calories">{{ calories }}</text>
      <text class="unit">kcal</text>
    </view>
  </view>
</template>

<script setup>
import { onMounted, watch } from 'vue'

const props = defineProps({
  calories: {
    type: [String, Number],
    default: '0'
  },
  nutritionData: {
    type: Array,
    default: () => []
  }
})

const drawChart = () => {
  const ctx = uni.createCanvasContext('nutritionCanvas')
  const centerX = 100
  const centerY = 100
  const radius = 80
  const lineWidth = 15

  let startAngle = -90

  props.nutritionData.forEach((item) => {
    const endAngle = startAngle + (item.percent / 100) * 360

    ctx.beginPath()
    ctx.arc(centerX, centerY, radius, startAngle * Math.PI / 180, endAngle * Math.PI / 180)
    ctx.setLineWidth(lineWidth)
    ctx.setStrokeStyle(item.color)
    ctx.stroke()

    startAngle = endAngle
  })

  ctx.draw()
}

onMounted(() => {
  drawChart()
})

watch(() => props.nutritionData, () => {
  drawChart()
}, { deep: true })
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.nutrition-chart {
  position: relative;
  width: 200px;
  height: 200px;
  margin: 40rpx auto;
}

.chart-canvas {
  width: 100%;
  height: 100%;
}

.chart-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.total-calories {
  font-size: 36px;
  font-weight: bold;
  color: #333;
  display: block;
}

.unit {
  font-size: 14px;
  color: #999;
}
</style>
