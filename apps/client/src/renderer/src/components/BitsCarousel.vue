<template>
  <div
    ref="rootRef"
    class="bits-carousel"
    :class="{ 'is-dragging': dragging || wheeling }"
    :style="carouselStyle"
    tabindex="0"
    role="region"
    aria-roledescription="carousel"
    :aria-label="ariaLabel"
    @keydown="handleKeydown"
    @pointerdown="handlePointerDown"
    @pointermove="handlePointerMove"
    @pointerup="handlePointerEnd"
    @pointercancel="handlePointerEnd"
    @wheel="handleWheel"
    @click.capture="handleClickCapture"
  >
    <div class="bits-carousel__stage">
      <div
        v-for="(item, index) in items"
        :key="item?.id ?? index"
        :data-index="index"
        class="bits-carousel__card"
        :class="{ 'is-active': index === currentIndex }"
        :style="getCardStyle(index)"
        :aria-hidden="Math.abs(index - position) > 1.5 ? 'true' : undefined"
      >
        <slot :item="item" :index="index" :active="index === currentIndex" />
      </div>
    </div>

    <div v-if="arrows || indicator !== 'none'" class="bits-carousel__controls">
      <button
        v-if="arrows"
        class="bits-carousel__arrow"
        type="button"
        aria-label="上一张"
        :disabled="!loop && currentIndex === 0"
        @click.stop="prev"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="m15 18-6-6 6-6" />
        </svg>
      </button>

      <div v-if="indicator === 'bars'" class="bits-carousel__bars" role="tablist" aria-label="轮播进度">
        <button
          v-for="(_, index) in items"
          :key="index"
          class="bits-carousel__bar"
          type="button"
          role="tab"
          :aria-label="`第 ${index + 1} 张，共 ${items.length} 张`"
          :aria-selected="index === currentIndex"
          @click.stop="goTo(index)"
        >
          <span class="bits-carousel__bar-track">
            <span
              class="bits-carousel__bar-fill"
              :style="{ transform: `scaleX(${index === currentIndex ? 1 : 0})` }"
            />
          </span>
        </button>
      </div>

      <div v-else-if="indicator === 'dots'" class="bits-carousel__dots" role="tablist" aria-label="轮播进度">
        <button
          v-for="(_, index) in items"
          :key="index"
          class="bits-carousel__dot"
          :class="{ 'is-active': index === currentIndex }"
          type="button"
          role="tab"
          :aria-label="`第 ${index + 1} 张，共 ${items.length} 张`"
          :aria-selected="index === currentIndex"
          @click.stop="goTo(index)"
        />
      </div>

      <div v-else-if="indicator === 'counter'" class="bits-carousel__counter">
        <span>{{ currentIndex + 1 }}</span>
        <span>/</span>
        <span>{{ items.length }}</span>
      </div>

      <button
        v-if="arrows"
        class="bits-carousel__arrow"
        type="button"
        aria-label="下一张"
        :disabled="!loop && currentIndex === items.length - 1"
        @click.stop="next"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="m9 18 6-6-6-6" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  items: {
    type: Array,
    default: () => []
  },
  baseWidth: {
    type: Number,
    default: 320
  },
  aspectRatio: {
    type: Number,
    default: 0.94
  },
  gap: {
    type: Number,
    default: 16
  },
  radius: {
    type: Number,
    default: 16
  },
  indicator: {
    type: String,
    default: 'bars'
  },
  arrows: {
    type: Boolean,
    default: true
  },
  draggable: {
    type: Boolean,
    default: true
  },
  loop: {
    type: Boolean,
    default: false
  },
  initialIndex: {
    type: Number,
    default: 0
  },
  ariaLabel: {
    type: String,
    default: '卡片轮播'
  }
})

const emit = defineEmits(['change'])

const clamp = (value, min, max) => Math.min(max, Math.max(min, value))
const wrap = (value, size) => ((value % size) + size) % size
const nearest = (offset, size) => wrap(offset + size / 2, size) - size / 2

function clampIndex(index) {
  if (!props.items.length) return 0
  if (props.loop) return wrap(index, props.items.length)
  return clamp(index, 0, props.items.length - 1)
}

const rootRef = ref(null)
const rootWidth = ref(0)
const currentIndex = ref(clampIndex(props.initialIndex))
const dragOffset = ref(0)
const dragging = ref(false)
const wheeling = ref(false)

let resizeObserver = null
let wheelTimer = null
let wheelOffset = 0
let pointerId = null
let startX = 0
let lastX = 0
let lastTime = 0
let velocity = 0
let dragDistance = 0
let suppressClick = false

const count = computed(() => props.items.length)
const cardWidth = computed(() => {
  const available = rootWidth.value || props.baseWidth
  const maxWidth = Math.max(236, available * (available < 520 ? 0.82 : 0.62))
  return Math.min(props.baseWidth, maxWidth)
})
const cardHeight = computed(() => cardWidth.value / Math.max(props.aspectRatio, 0.5))
const step = computed(() => cardWidth.value + props.gap)
const position = computed(() => currentIndex.value + dragOffset.value / step.value)

const carouselStyle = computed(() => ({
  '--bits-card-width': `${cardWidth.value}px`,
  '--bits-card-height': `${cardHeight.value}px`,
  '--bits-card-radius': `${props.radius}px`,
  '--bits-card-step': `${step.value}px`
}))

function getOffset(index) {
  if (!count.value) return 0
  const rawOffset = index - position.value
  return props.loop && count.value > 1 ? nearest(rawOffset, count.value) : rawOffset
}

function getCardStyle(index) {
  const offset = getOffset(index)
  const distance = Math.abs(offset)
  const centerOffset = clamp(offset, -1.35, 1.35)
  const scale = 1 - Math.min(distance, 1) * 0.075
  const translateY = Math.min(distance, 1) * 8
  const rotateY = -centerOffset * 13
  const opacity = distance > 1.8 ? 0 : 1 - Math.max(0, distance - 0.9) * 0.35
  const brightness = 1 - Math.min(distance, 1) * 0.04
  const blur = distance > 1 ? Math.min((distance - 1) * 2.4, 1.2) : 0

  return {
    zIndex: 100 - Math.round(distance * 10),
    opacity,
    filter: `brightness(${brightness}) blur(${blur}px)`,
    transform: `translate3d(calc(-50% + ${centerOffset * step.value}px), ${translateY}px, 0) rotateY(${rotateY}deg) scale(${scale})`
  }
}

function goTo(index) {
  const nextIndex = clampIndex(index)
  if (nextIndex === currentIndex.value) return
  currentIndex.value = nextIndex
  emit('change', nextIndex)
}

function next() {
  if (props.loop && count.value > 1) {
    goTo(wrap(currentIndex.value + 1, count.value))
    return
  }
  goTo(Math.min(currentIndex.value + 1, count.value - 1))
}

function prev() {
  if (props.loop && count.value > 1) {
    goTo(wrap(currentIndex.value - 1, count.value))
    return
  }
  goTo(Math.max(currentIndex.value - 1, 0))
}

function handleKeydown(event) {
  if (event.key === 'ArrowRight') {
    event.preventDefault()
    next()
  }
  if (event.key === 'ArrowLeft') {
    event.preventDefault()
    prev()
  }
  if (event.key === 'Home') {
    event.preventDefault()
    goTo(0)
  }
  if (event.key === 'End') {
    event.preventDefault()
    goTo(count.value - 1)
  }
}

function handleWheel(event) {
  if (!props.draggable) return

  const horizontalDelta = Math.abs(event.deltaX)
  const verticalDelta = Math.abs(event.deltaY)

  if (horizontalDelta <= verticalDelta * 1.15) return

  event.preventDefault()
  wheeling.value = true
  wheelOffset += event.deltaX
  dragOffset.value = wheelOffset

  window.clearTimeout(wheelTimer)
  wheelTimer = window.setTimeout(() => {
    const projectedPosition = currentIndex.value + dragOffset.value / step.value
    const directionNudge = Math.sign(dragOffset.value) * 0.12
    const nextIndex = Math.round(projectedPosition + directionNudge)

    dragOffset.value = 0
    wheelOffset = 0
    wheeling.value = false
    goTo(nextIndex)
  }, 80)
}

function handlePointerDown(event) {
  if (!props.draggable || event.button !== 0 || event.target.closest('button')) return

  window.clearTimeout(wheelTimer)
  wheelOffset = 0
  wheeling.value = false
  pointerId = event.pointerId
  dragging.value = true
  startX = event.clientX
  lastX = event.clientX
  lastTime = performance.now()
  velocity = 0
  dragDistance = 0
  suppressClick = false
  rootRef.value?.setPointerCapture?.(pointerId)
}

function handlePointerMove(event) {
  if (!dragging.value || event.pointerId !== pointerId) return

  const deltaX = event.clientX - startX
  dragDistance = Math.max(dragDistance, Math.abs(deltaX))
  dragOffset.value = -deltaX

  const now = performance.now()
  const elapsed = Math.max(now - lastTime, 1)
  velocity = (event.clientX - lastX) / elapsed
  lastX = event.clientX
  lastTime = now

  if (dragDistance > 8) {
    suppressClick = true
    event.preventDefault()
  }
}

function handlePointerEnd(event) {
  if (!dragging.value || event.pointerId !== pointerId) return

  dragging.value = false
  rootRef.value?.releasePointerCapture?.(pointerId)
  pointerId = null

  const projectedPosition = position.value - velocity * 120
  dragOffset.value = 0
  goTo(Math.round(projectedPosition))

  window.setTimeout(() => {
    suppressClick = false
  }, 0)
}

function handleClickCapture(event) {
  if (event.target.closest('button')) return

  if (suppressClick || dragDistance > 8) {
    event.preventDefault()
    event.stopPropagation()
    return
  }

  const cardElement = event.target.closest('.bits-carousel__card')
  const cardIndex = cardElement ? Number(cardElement.dataset.index) : NaN
  if (Number.isFinite(cardIndex) && cardIndex !== currentIndex.value) {
    event.preventDefault()
    event.stopPropagation()
    goTo(cardIndex)
  }
}

function updateWidth() {
  rootWidth.value = rootRef.value?.getBoundingClientRect().width || 0
}

watch(
  () => props.items.length,
  () => {
    currentIndex.value = clampIndex(currentIndex.value)
  }
)

watch(
  () => props.initialIndex,
  (value) => {
    currentIndex.value = clampIndex(value)
  }
)

watch(
  () => props.loop,
  () => {
    currentIndex.value = clampIndex(currentIndex.value)
  }
)

onMounted(() => {
  updateWidth()
  resizeObserver = new ResizeObserver(updateWidth)
  if (rootRef.value) resizeObserver.observe(rootRef.value)
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  window.clearTimeout(wheelTimer)
})
</script>

<style scoped lang="less">
@import '../assets/css/nordic-theme.less';

.bits-carousel {
  width: 100%;
  min-width: 0;
  outline: none;
  touch-action: pan-y;
  user-select: none;
  -webkit-user-select: none;
}

.bits-carousel:focus-visible {
  box-shadow: 0 0 0 3px fade(@nordic-accent, 18%);
  border-radius: 18px;
}

.bits-carousel__stage {
  position: relative;
  width: calc(100% - clamp(32px, 6vw, 96px));
  height: calc(var(--bits-card-height) + 62px);
  margin: 0 auto;
  perspective: 1000px;
  perspective-origin: 50% 50%;
  -webkit-mask-image: linear-gradient(
    to right,
    transparent 0%,
    black 8%,
    black 92%,
    transparent 100%
  );
  mask-image: linear-gradient(
    to right,
    transparent 0%,
    black 8%,
    black 92%,
    transparent 100%
  );
}

.bits-carousel__card {
  position: absolute;
  top: 18px;
  left: 50%;
  width: var(--bits-card-width);
  height: var(--bits-card-height);
  border-radius: var(--bits-card-radius);
  transform-style: preserve-3d;
  transition:
    transform 520ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 320ms ease,
    filter 320ms ease;
  will-change: transform, opacity, filter;
}

.bits-carousel.is-dragging .bits-carousel__card {
  transition: none;
}

.bits-carousel__card.is-active {
  cursor: grab;
}

.bits-carousel.is-dragging .bits-carousel__card.is-active {
  cursor: grabbing;
}

.bits-carousel__controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 34px;
  margin-top: 6px;
}

.bits-carousel__arrow {
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  display: grid;
  place-items: center;
  padding: 0;
  color: var(--el-text-color-primary, @nordic-text);
  background: @nordic-surface;
  border: 1px solid @nordic-border;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 4px 12px fade(@nordic-text, 7%);
  transition:
    transform 180ms ease,
    color 180ms ease,
    border-color 180ms ease,
    background-color 180ms ease;
}

.bits-carousel__arrow:hover:not(:disabled) {
  color: @nordic-accent;
  background: @nordic-accent-light;
  border-color: fade(@nordic-accent, 35%);
  transform: translateY(-1px);
}

.bits-carousel__arrow:active:not(:disabled) {
  transform: scale(0.94);
}

.bits-carousel__arrow:disabled {
  opacity: 0.35;
  cursor: default;
}

.bits-carousel__arrow svg {
  width: 17px;
  height: 17px;
}

.bits-carousel__bars {
  width: min(220px, 42vw);
  display: flex;
  align-items: center;
  gap: 6px;
}

.bits-carousel__bar {
  flex: 1;
  height: 22px;
  padding: 0;
  background: transparent;
  border: 0;
  cursor: pointer;
}

.bits-carousel__bar-track {
  position: relative;
  width: 100%;
  height: 3px;
  display: block;
  overflow: hidden;
  background: @nordic-border;
  border-radius: 999px;
}

.bits-carousel__bar-fill {
  position: absolute;
  inset: 0;
  display: block;
  background: @nordic-accent;
  border-radius: inherit;
  transform: scaleX(0);
  transform-origin: left center;
  transition: transform 300ms ease;
}

.bits-carousel__dots {
  display: flex;
  align-items: center;
  gap: 2px;
}

.bits-carousel__dot {
  width: 18px;
  height: 18px;
  padding: 0;
  background: transparent;
  border: 0;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
}

.bits-carousel__dot::after {
  content: '';
  position: absolute;
  inset: 5px;
  background: @nordic-text-muted;
  border-radius: 50%;
  transition: transform 180ms ease, background-color 180ms ease;
}

.bits-carousel__dot.is-active::after {
  background: @nordic-accent;
  transform: scale(1.25);
}

.bits-carousel__counter {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: @nordic-text-muted;
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}
</style>
