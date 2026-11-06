<template>
  <section class="app-form-container" :class="`app-form-container--${variant}`">
    <AppCard>
      <header v-if="title || subtitle" class="app-form-container__header">
        <h2 v-if="title" class="app-form-container__title">{{ title }}</h2>
        <p v-if="subtitle" class="app-form-container__subtitle">{{ subtitle }}</p>
      </header>

      <div class="app-form-container__body">
        <slot />
      </div>

      <footer v-if="$slots.actions" class="app-form-container__actions">
        <slot name="actions" />
      </footer>
    </AppCard>
  </section>
</template>

<script setup>
// 契约冻结：variant auth（窄列 400px，登录/注册/找回/支付密码）| settings（限宽 720px，设置/资料类）。
// 宽度只取 §3.3 的 --form-width-auth / --form-width-settings 两档，不允许第三档。
// 本仓库未装 @typescript-eslint/parser，lang="ts" 的 withDefaults 写法会打断 ESLint，
// 故用运行时声明 + 内联 default，行为与类型契约一致。
import AppCard from './AppCard.vue'

defineProps({
  variant: {
    type: String,
    default: 'auth',
    validator: (value) => ['auth', 'settings'].includes(value)
  },
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.app-form-container {
  box-sizing: border-box;
  width: min(100%, var(--form-width-auth));
  margin-inline: auto;
}

/* 设置/资料类：限宽 720px，两栏由页面在 default slot 内排布 */
.app-form-container--settings {
  width: min(100%, var(--form-width-settings));
}

.app-form-container__header:not(:last-child) {
  margin-block-end: var(--space-6);
}

/* 页面主标题由 AppPageHeader 承担 h1，本组件标题属区块级，按 §4.7 从 h2 起 */
.app-form-container__title {
  margin: 0;
  font-size: var(--text-h2);
  line-height: 28px;
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

.app-form-container__subtitle {
  margin: var(--space-1) 0 0;
  font-size: var(--text-body-sm);
  line-height: 20px;
  color: var(--ink-500);
}

.app-form-container__body {
  min-width: 0;
}

.app-form-container__actions {
  display: flex;
  gap: var(--space-3);
}

/* 认证类：标签、输入框、主按钮同一列宽（§4.4），故动作区纵向铺满 */
.app-form-container--auth .app-form-container__actions {
  flex-direction: column;
  align-items: stretch;
}

/* 设置类：主操作靠右 */
.app-form-container--settings .app-form-container__actions {
  justify-content: flex-end;
}

.app-form-container__actions:not(:empty) {
  margin-block-start: var(--space-6);
}
</style>
