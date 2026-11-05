<template>
  <canvas ref="canvasRef" class="weather-canvas-fx" aria-hidden="true"></canvas>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  theme: {
    type: String,
    default: 'default'
  }
})

const canvasRef = ref(null)

const DPR_MAX = 2
const REFERENCE_AREA = 360 * 240

// 每种天气的粒子规格：kind 决定运动方式，其余是取值范围
const PRESETS = {
  snow: {
    kind: 'flake',
    count: 82,
    radius: [0.7, 2],
    speed: [16, 46],
    drift: [4, 12],
    alpha: [0.45, 0.9],
    color: [255, 255, 255],
    push: 58
  },
  cold: {
    kind: 'flake',
    count: 46,
    radius: [0.7, 1.7],
    speed: [10, 26],
    drift: [3, 8],
    alpha: [0.3, 0.7],
    color: [255, 255, 255],
    push: 50
  },
  rain: {
    kind: 'streak',
    count: 36,
    length: [26, 58],
    speed: [420, 760],
    alpha: [0.26, 0.6],
    color: [226, 240, 255],
    slant: 0.24,
    push: 46
  },
  thunder: {
    kind: 'streak',
    count: 42,
    length: [24, 56],
    speed: [520, 900],
    alpha: [0.18, 0.42],
    color: [214, 208, 255],
    slant: 0.3,
    push: 50,
    flash: true
  },
  sunny: {
    kind: 'mote',
    count: 44,
    radius: [0.8, 2.4],
    speed: [8, 24],
    drift: [6, 18],
    alpha: [0.18, 0.5],
    color: [255, 232, 170],
    pull: 44,
    glow: [255, 236, 176]
  },
  hot: {
    kind: 'mote',
    count: 54,
    radius: [0.8, 2.6],
    speed: [10, 28],
    drift: [6, 20],
    alpha: [0.16, 0.46],
    color: [255, 196, 120],
    pull: 46,
    glow: [255, 178, 96]
  },
  cloudy: {
    kind: 'haze',
    count: 4,
    // 云团尺寸按卡片宽度取比例：卡片变大时云跟着变大，而不是数量变多
    radius: [0.13, 0.21],
    speed: [5, 14],
    alpha: [0.44, 0.62],
    color: [244, 249, 255],
    shade: [46, 72, 110],
    push: 84
  },
  default: {
    kind: 'none',
    count: 0
  }
}

const rand = (min, max) => min + Math.random() * (max - min)

let ctx = null
let host = null
let frameId = 0
let lastTime = 0
let cssWidth = 0
let cssHeight = 0
let particles = []
let spec = PRESETS.default
let observer = null
let motionQuery = null
let flash = 0
let flashTimer = 0

const pointer = { x: 0, y: 0, mixX: 0, mixY: 0, active: false }

const motionReduced = () => Boolean(motionQuery && motionQuery.matches)

function createParticle(fromEdge) {
  const particle = {
    x: rand(0, cssWidth),
    y: fromEdge ? rand(-cssHeight * 0.25, -10) : rand(0, cssHeight),
    vx: 0,
    vy: 0,
    phase: rand(0, Math.PI * 2),
    freq: rand(0.4, 1.1),
    alpha: rand(spec.alpha[0], spec.alpha[1]),
    r: 1,
    len: 0,
    weight: 1,
    driftAmp: 0
  }

  if (spec.kind === 'flake') {
    particle.r = rand(spec.radius[0], spec.radius[1])
    particle.vy = rand(spec.speed[0], spec.speed[1])
    particle.driftAmp = rand(spec.drift[0], spec.drift[1])
  } else if (spec.kind === 'streak') {
    particle.len = rand(spec.length[0], spec.length[1])
    particle.vy = rand(spec.speed[0], spec.speed[1])
    particle.weight = rand(0.7, 1.4)
    particle.alpha *=
      0.6 + ((particle.len - spec.length[0]) / (spec.length[1] - spec.length[0])) * 0.6
  } else if (spec.kind === 'mote') {
    particle.r = rand(spec.radius[0], spec.radius[1])
    particle.vy = -rand(spec.speed[0], spec.speed[1])
    particle.driftAmp = rand(spec.drift[0], spec.drift[1])
  } else if (spec.kind === 'haze') {
    particle.r = rand(spec.radius[0], spec.radius[1]) * cssWidth
    particle.vx = (Math.random() < 0.5 ? -1 : 1) * rand(spec.speed[0], spec.speed[1])
    // 一团云 = 底部宽基座 + 两三处凸起，左右再随机翻一下，避免每团云都是同一个形状
    const k = rand(0.86, 1.12)
    particle.flip = Math.random() < 0.5 ? -1 : 1
    particle.puffs = [
      { dx: -0.5, dy: 0.16, rx: 0.42, ry: 0.34 },
      { dx: 0.04, dy: -0.14, rx: 0.46, ry: 0.42 },
      { dx: 0.5, dy: 0.2, rx: 0.38, ry: 0.3 },
      { dx: 0, dy: 0.34, rx: 0.92, ry: 0.32 }
    ].map((puff) => ({ ...puff, rx: puff.rx * k, ry: puff.ry * k }))
  }

  return particle
}

function seed() {
  particles = []
  if (spec.kind === 'none' || cssWidth === 0 || cssHeight === 0) return
  const scale = Math.min(1.8, Math.max(0.5, (cssWidth * cssHeight) / REFERENCE_AREA))
  // 云团已经按卡片宽度缩放过，再按面积加数量只会糊成一片
  const total = spec.kind === 'haze' ? spec.count : Math.round(spec.count * scale)
  for (let i = 0; i < total; i += 1) {
    const particle = createParticle(false)
    if (spec.kind === 'haze') {
      // 横向均匀铺开，几团大云才不会挤在一起叠成一团白雾
      particle.x = ((i + rand(0.15, 0.85)) / total) * cssWidth
      particle.y = rand(-cssHeight * 0.05, cssHeight * 0.55)
    }
    particles.push(particle)
  }
}

function pushFromPointer(particle, dt) {
  if (!pointer.active || !spec.push) return
  const dx = particle.x - pointer.x
  const dy = particle.y - pointer.y
  const distance = Math.hypot(dx, dy)
  if (distance >= spec.push || distance < 0.001) return
  const force = (1 - distance / spec.push) * 88 * dt
  particle.x += (dx / distance) * force
  particle.y += (dy / distance) * force
}

function update(dt) {
  if (spec.kind === 'none') return

  pointer.mixX += (pointer.x - pointer.mixX) * Math.min(1, dt * 6)
  pointer.mixY += (pointer.y - pointer.mixY) * Math.min(1, dt * 6)

  for (const particle of particles) {
    particle.phase += dt * particle.freq

    if (spec.kind === 'flake') {
      particle.y += particle.vy * dt
      particle.x += Math.sin(particle.phase) * particle.driftAmp * dt
      pushFromPointer(particle, dt)
      if (particle.y - particle.r > cssHeight) Object.assign(particle, createParticle(true))
    } else if (spec.kind === 'streak') {
      particle.y += particle.vy * dt
      particle.x += particle.vy * spec.slant * dt
      pushFromPointer(particle, dt)
      if (particle.y - particle.len > cssHeight || particle.x - particle.len > cssWidth) {
        const fresh = createParticle(true)
        fresh.x = rand(-cssWidth * 0.1, cssWidth)
        Object.assign(particle, fresh)
      }
    } else if (spec.kind === 'mote') {
      particle.y += particle.vy * dt
      particle.x += Math.sin(particle.phase) * particle.driftAmp * dt
      if (pointer.active && spec.pull) {
        const dx = pointer.x - particle.x
        const dy = pointer.y - particle.y
        const distance = Math.hypot(dx, dy)
        if (distance < spec.pull && distance > 0.001) {
          const force = (1 - distance / spec.pull) * 26 * dt
          particle.x += (dx / distance) * force
          particle.y += (dy / distance) * force
        }
      }
      if (particle.y + particle.r < 0)
        Object.assign(particle, createParticle(false), { y: cssHeight + 6 })
    } else if (spec.kind === 'haze') {
      particle.x += particle.vx * dt
      particle.y += Math.sin(particle.phase) * 4 * dt
      pushFromPointer(particle, dt)
      if (particle.x - particle.r > cssWidth) particle.x = -particle.r
      if (particle.x + particle.r < 0) particle.x = cssWidth + particle.r
    }
  }
}

function drawGlow() {
  if (!spec.glow) return
  const [r, g, b] = spec.glow
  // 指针在卡片内时，光晕跟着指针走，形成「阳光追手」的互动
  const cx = pointer.active ? pointer.mixX : cssWidth * 0.82
  const cy = pointer.active ? pointer.mixY : cssHeight * 0.14
  const base = Math.max(cssWidth, cssHeight)
  const strength = pointer.active ? 1 : 0.7

  // 外层铺体积，内层收出更亮的核，单层只会像一块光斑
  const halos = [
    { radius: base * 0.62, alpha: 0.3 },
    { radius: base * 0.26, alpha: 0.34 }
  ]

  for (const halo of halos) {
    const gradient = ctx.createRadialGradient(cx, cy, 0, cx, cy, halo.radius)
    gradient.addColorStop(0, `rgba(${r}, ${g}, ${b}, ${halo.alpha * strength})`)
    gradient.addColorStop(1, `rgba(${r}, ${g}, ${b}, 0)`)
    ctx.fillStyle = gradient
    ctx.fillRect(0, 0, cssWidth, cssHeight)
  }
}

function draw() {
  if (!ctx) return
  ctx.clearRect(0, 0, cssWidth, cssHeight)
  if (spec.kind === 'none') return

  drawGlow()

  const [r, g, b] = spec.color
  for (const particle of particles) {
    if (spec.kind === 'streak') {
      // 尾迹用渐变收尾，比等宽实线更像真实的雨丝
      const tailX = particle.x - particle.len * spec.slant
      const tailY = particle.y - particle.len
      const trail = ctx.createLinearGradient(tailX, tailY, particle.x, particle.y)
      trail.addColorStop(0, `rgba(${r}, ${g}, ${b}, 0)`)
      trail.addColorStop(1, `rgba(${r}, ${g}, ${b}, ${particle.alpha})`)
      ctx.strokeStyle = trail
      ctx.lineWidth = particle.weight
      ctx.beginPath()
      ctx.moveTo(tailX, tailY)
      ctx.lineTo(particle.x, particle.y)
      ctx.stroke()
    } else if (spec.kind === 'haze') {
      // 云团：几个椭圆并成一条轮廓后一次性填充，边缘再收一点模糊——
      // 有形状、轮廓又不过分生硬，才不会糊成一团白雾，也不像贴纸
      const [sr, sg, sb] = spec.shade
      const rad = particle.r
      const dir = particle.flip || 1
      ctx.save()
      ctx.filter = `blur(${Math.max(2, rad * 0.05).toFixed(1)}px)`
      ctx.beginPath()
      for (const puff of particle.puffs) {
        ctx.ellipse(
          particle.x + rad * puff.dx * dir,
          particle.y + rad * puff.dy,
          rad * puff.rx,
          rad * puff.ry,
          0,
          0,
          Math.PI * 2
        )
      }
      // 顶部受光偏白、底部收进灰蓝，一个纵向渐变撑出体积
      const body = ctx.createLinearGradient(0, particle.y - rad * 0.72, 0, particle.y + rad * 0.78)
      body.addColorStop(0, `rgba(${r}, ${g}, ${b}, ${particle.alpha})`)
      body.addColorStop(0.5, `rgba(${r}, ${g}, ${b}, ${particle.alpha * 0.88})`)
      body.addColorStop(1, `rgba(${sr}, ${sg}, ${sb}, ${particle.alpha * 0.62})`)
      ctx.fillStyle = body
      ctx.fill()
      ctx.restore()
    } else {
      ctx.fillStyle = `rgba(${r}, ${g}, ${b}, ${particle.alpha})`
      ctx.beginPath()
      ctx.arc(particle.x, particle.y, particle.r, 0, Math.PI * 2)
      ctx.fill()
    }
  }

  if (flash > 0) {
    ctx.fillStyle = `rgba(232, 226, 255, ${flash})`
    ctx.fillRect(0, 0, cssWidth, cssHeight)
  }
}

function frame(now) {
  if (!lastTime) lastTime = now
  const dt = Math.min((now - lastTime) / 1000, 0.05)
  lastTime = now

  if (spec.flash) {
    flashTimer -= dt
    if (flashTimer <= 0) {
      flash = rand(0.18, 0.42)
      flashTimer = rand(2.6, 6.5)
    }
    flash = Math.max(0, flash - dt * 1.4)
  }

  update(dt)
  draw()
  frameId = requestAnimationFrame(frame)
}

function start() {
  if (frameId || motionReduced()) return
  lastTime = 0
  frameId = requestAnimationFrame(frame)
}

function stop() {
  if (!frameId) return
  cancelAnimationFrame(frameId)
  frameId = 0
}

function resize() {
  if (!canvasRef.value || !host) return
  // 用 clientWidth/clientHeight：卡片入场有 scale 动画，getBoundingClientRect 会量到缩放后的小尺寸
  const nextWidth = host.clientWidth
  const nextHeight = host.clientHeight
  if (nextWidth === 0 || nextHeight === 0) return

  cssWidth = nextWidth
  cssHeight = nextHeight

  const dpr = Math.min(DPR_MAX, window.devicePixelRatio || 1)
  const canvas = canvasRef.value
  canvas.width = Math.round(cssWidth * dpr)
  canvas.height = Math.round(cssHeight * dpr)
  canvas.style.width = `${cssWidth}px`
  canvas.style.height = `${cssHeight}px`

  ctx = canvas.getContext('2d')
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)

  pointer.mixX = cssWidth * 0.82
  pointer.mixY = cssHeight * 0.14

  seed()
  draw()
  if (!motionReduced()) start()
}

function applyTheme(theme) {
  spec = PRESETS[theme] || PRESETS.default
  flash = 0
  flashTimer = rand(1.2, 3.6)
  seed()
  draw()
  if (motionReduced()) stop()
  else start()
}

function onPointerMove(event) {
  if (!host) return
  const rect = host.getBoundingClientRect()
  pointer.x = event.clientX - rect.left
  pointer.y = event.clientY - rect.top
  pointer.active = true
}

function onPointerLeave() {
  pointer.active = false
}

function onMotionPreferenceChange() {
  if (motionReduced()) {
    stop()
    draw()
  } else {
    start()
  }
}

function onVisibilityChange() {
  if (document.hidden) {
    stop()
  } else if (!motionReduced()) {
    start()
  }
}

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas || !canvas.parentElement) return
  host = canvas.parentElement

  motionQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
  motionQuery.addEventListener('change', onMotionPreferenceChange)

  observer = new ResizeObserver(resize)
  observer.observe(host)

  host.addEventListener('pointermove', onPointerMove)
  host.addEventListener('pointerleave', onPointerLeave)
  document.addEventListener('visibilitychange', onVisibilityChange)

  applyTheme(props.theme)
})

onBeforeUnmount(() => {
  stop()
  if (observer) observer.disconnect()
  if (motionQuery) motionQuery.removeEventListener('change', onMotionPreferenceChange)
  if (host) {
    host.removeEventListener('pointermove', onPointerMove)
    host.removeEventListener('pointerleave', onPointerLeave)
  }
  document.removeEventListener('visibilitychange', onVisibilityChange)
})

watch(
  () => props.theme,
  (theme) => {
    if (host) applyTheme(theme)
  }
)
</script>

<style scoped>
.weather-canvas-fx {
  position: absolute;
  inset: 0;
  display: block;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}
</style>
