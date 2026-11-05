<template>
  <div
    class="app-card"
    :class="[
      `app-card--level-${level}`,
      `app-card--padding-${padding}`,
      { 'app-card--interactive': interactive }
    ]"
  >
    <header v-if="$slots.header" class="app-card__header">
      <slot name="header" />
    </header>

    <div class="app-card__body">
      <slot />
    </div>

    <footer v-if="$slots.footer" class="app-card__footer">
      <slot name="footer" />
    </footer>
  </div>
</template>

<script setup>
// 契约冻结：level 0-3（默认 1）、interactive（默认 false）、padding none/sm/md/lg（默认 md）。
// 本仓库未装 @typescript-eslint/parser，lang="ts" 的 withDefaults 写法会打断 ESLint，
// 故用运行时声明 + 内联 default，行为与类型契约一致。
defineProps({
  // 质感层级：0 平面 / 1 微质感 / 2 浮起 / 3 玻璃
  level: {
    type: Number,
    default: 1,
    validator: (value) => [0, 1, 2, 3].includes(value)
  },
  // 仅 L2/L3 生效：hover 阴影升级与 1px 位移
  interactive: {
    type: Boolean,
    default: false
  },
  padding: {
    type: String,
    default: 'md',
    validator: (value) => ['none', 'sm', 'md', 'lg'].includes(value)
  }
})
</script>

<style scoped>
.app-card {
  box-sizing: border-box;
  border-radius: var(--radius-lg);
  background: var(--surface);
  color: var(--ink-900);
}

.app-card--padding-none {
  padding: 0;
}

.app-card--padding-sm {
  padding: 12px;
}

.app-card--padding-md {
  padding: 20px;
}

.app-card--padding-lg {
  padding: 24px;
}

/* L0 平面：纯色，无描边无阴影 */
.app-card--level-0 {
  border: 0;
  box-shadow: none;
}

/* L1 微质感：1px 描边 + 顶部内高光 */
.app-card--level-1 {
  border: 1px solid var(--line);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

/* L2 浮起：L1 + --shadow-sm */
.app-card--level-2 {
  border: 1px solid var(--line);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.6),
    var(--shadow-sm);
}

/* L3 玻璃：L2 + 半透明底 + 背景模糊，仅用于彩色/图像背景之上 */
.app-card--level-3 {
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.72);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.6),
    var(--shadow-sm);
  -webkit-backdrop-filter: blur(16px);
  backdrop-filter: blur(16px);
}

/* interactive 只在带阴影的 L2/L3 上补 hover 反馈 */
.app-card--level-2.app-card--interactive,
.app-card--level-3.app-card--interactive {
  cursor: pointer;
  transition:
    box-shadow var(--duration-fast) var(--ease-out),
    transform var(--duration-fast) var(--ease-out);
}

.app-card--level-2.app-card--interactive:hover,
.app-card--level-3.app-card--interactive:hover {
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.6),
    var(--shadow-md);
  transform: translateY(-1px);
}
</style>
