<template>
  <header class="app-page-header" :class="{ 'app-page-header--compact': props.compact }">
    <div class="app-page-header__main">
      <div class="app-page-header__titles">
        <div class="app-page-header__title-row">
          <h1 class="app-page-header__title">{{ props.title }}</h1>
          <div v-if="$slots.extra" class="app-page-header__extra">
            <slot name="extra" />
          </div>
        </div>
        <p v-if="props.subtitle" class="app-page-header__subtitle">{{ props.subtitle }}</p>
      </div>
      <div v-if="$slots.actions" class="app-page-header__actions">
        <slot name="actions" />
      </div>
    </div>
  </header>
</template>

<script setup>
// 契约冻结：title（必填）、subtitle（可选）、compact（默认 false，密集表格页用）；
// 具名 slot：actions（右侧主操作区）、extra（标题行内附加）。
// 本仓库未装 @typescript-eslint/parser，lang="ts" 的 withDefaults 写法会打断 ESLint，
// 故统一用运行时声明，行为与类型契约一致。
const props = defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  compact: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
/* 页头容器：页面底色 + 下边线，不引入背景色块（DESIGN-BASELINE §4.7）。 */
.app-page-header {
  box-sizing: border-box;
  padding-top: var(--space-6);
  padding-bottom: var(--space-6);
  margin-bottom: var(--space-6);
  border-bottom: 1px solid var(--line);
}

.app-page-header__main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
}

.app-page-header__titles {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  min-width: 0;
}

.app-page-header__title-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}

/* 正式版标题：--text-h1 / 600 / --ink-900（§3.2、§4.7）。 */
.app-page-header__title {
  margin: 0;
  font-size: var(--text-h1);
  line-height: 32px;
  font-weight: var(--weight-semibold);
  letter-spacing: var(--tracking-tight);
  color: var(--ink-900);
}

.app-page-header__extra {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  min-width: 0;
}

/* 副标题：--text-body-sm / --ink-500（§3.2、§4.7）。 */
.app-page-header__subtitle {
  margin: 0;
  font-size: var(--text-body-sm);
  line-height: 20px;
  color: var(--ink-500);
}

.app-page-header__actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: var(--space-3);
}

/* 紧凑版：标题降一档到 --text-h2，上下内边距降到 --space-4（§4.7）。 */
.app-page-header--compact {
  padding-top: var(--space-4);
  padding-bottom: var(--space-4);
  margin-bottom: var(--space-4);
}

.app-page-header--compact .app-page-header__title {
  font-size: var(--text-h2);
  line-height: 28px;
}

.app-page-header--compact .app-page-header__titles {
  gap: var(--space-1);
}
</style>
