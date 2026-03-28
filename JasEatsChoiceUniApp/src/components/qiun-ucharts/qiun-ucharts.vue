<template>
  <view class="qiun-ucharts-container">
    <canvas
      :canvas-id="canvasId"
      :id="canvasId"
      class="ucharts-canvas"
      :style="{ width: width + 'px', height: height + 'px' }"
    ></canvas>
    <view class="chart-placeholder" v-if="!chartData || !chartData.series">
      <text class="placeholder-text">图表功能开发中</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'line'
  },
  opts: {
    type: Object,
    default: () => ({})
  },
  chartData: {
    type: Object,
    default: () => ({})
  },
  canvas2d: {
    type: Boolean,
    default: true
  },
  canvasId: {
    type: String,
    default: 'ucharts-canvas'
  },
  width: {
    type: Number,
    default: 750
  },
  height: {
    type: Number,
    default: 500
  }
})

const emit = defineEmits(['getIndex', 'complete'])

const canvasContext = ref(null)

onMounted(() => {
  initChart()
})

watch(() => props.chartData, () => {
  initChart()
}, { deep: true })

const initChart = () => {
  if (!props.chartData || !props.chartData.series) {
    return
  }

  console.log('[qiun-ucharts] 图表初始化', {
    type: props.type,
    canvasId: props.canvasId,
    data: props.chartData
  })

  // TODO: 集成完整的图表库
  // 目前只显示占位符
  emit('complete', {
    canvasId: props.canvasId
  })
}
</script>

<style lang="scss" scoped>
.qiun-ucharts-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.ucharts-canvas {
  width: 100%;
  height: 100%;
}

.chart-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border-radius: 8rpx;

  .placeholder-text {
    font-size: 28rpx;
    color: #999;
  }
}
</style>
