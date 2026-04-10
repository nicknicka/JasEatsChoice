<template>
  <transition name="fade-slide">
    <section
      v-if="show"
      class="quick-questions-panel-fixed"
      role="dialog"
      aria-label="快捷提问"
    >
      <header class="quick-questions-header">
        <div class="header-copy">
          <span class="eyebrow">快捷提问</span>
          <h3>点一下，直接开始问</h3>
          <p>把常见问题整理成卡片，减少输入成本，适合快速开启一次对话。</p>
        </div>

        <el-button
          class="close-button"
          :icon="Close"
          circle
          plain
          size="small"
          @click="$emit('close')"
        />
      </header>

      <div class="quick-questions-grid">
        <article
          v-for="category in categories"
          :key="category.id"
          class="question-category-card"
          :style="{ '--category-accent': category.accent || '#D4845A' }"
        >
          <div class="category-top">
            <div class="category-badge">{{ category.title }}</div>
            <span class="category-count">{{ category.questions.length }} 条</span>
          </div>

          <p class="category-description">{{ category.description }}</p>

          <div class="question-chip-list">
            <button
              v-for="question in category.questions"
              :key="question"
              type="button"
              class="question-chip"
              @click="$emit('select', question)"
            >
              {{ question }}
            </button>
          </div>
        </article>
      </div>

      <footer class="quick-questions-footer">
        <span>选中后会自动填入并发送，支持继续追问</span>
      </footer>
    </section>
  </transition>
</template>

<script setup>
import { Close } from '@element-plus/icons-vue'

defineProps({
  show: {
    type: Boolean,
    default: false
  },
  categories: {
    type: Array,
    default: () => []
  }
})

defineEmits(['close', 'select'])
</script>

<style scoped lang="less">
.quick-questions-panel-fixed {
  position: absolute;
  left: 0;
  right: 0;
  bottom: calc(100% + 10px);
  width: 100%;
  box-sizing: border-box;
  padding: 18px;
  border-radius: 20px;
  border: 1px solid rgba(212, 132, 90, 0.18);
  background:
    radial-gradient(circle at top right, rgba(123, 174, 127, 0.12), transparent 30%),
    linear-gradient(180deg, #fffdf9 0%, #faf6f0 100%);
  box-shadow: 0 18px 40px rgba(180, 140, 100, 0.18);
  backdrop-filter: blur(12px);
  z-index: 20;
}

.quick-questions-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 14px;

  .header-copy {
    min-width: 0;
  }

  .eyebrow {
    display: inline-flex;
    align-items: center;
    margin-bottom: 8px;
    padding: 4px 10px;
    border-radius: 999px;
    background: rgba(212, 132, 90, 0.12);
    color: #a85d39;
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 0.08em;
  }

  h3 {
    margin: 0;
    font-size: 18px;
    line-height: 1.2;
    color: #2d2926;
  }

  p {
    margin: 8px 0 0;
    color: #7a7168;
    font-size: 13px;
    line-height: 1.6;
    max-width: 46rem;
  }

  .close-button {
    flex-shrink: 0;
  }
}

.quick-questions-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.question-category-card {
  padding: 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(232, 228, 222, 0.9);
  box-shadow: 0 8px 24px rgba(180, 140, 100, 0.06);

  .category-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
  }

  .category-badge {
    display: inline-flex;
    align-items: center;
    padding: 6px 10px;
    border-radius: 999px;
    background: color-mix(in srgb, var(--category-accent) 14%, #ffffff);
    color: var(--category-accent);
    font-size: 13px;
    font-weight: 700;
  }

  .category-count {
    color: #9e9e9e;
    font-size: 12px;
    white-space: nowrap;
  }

  .category-description {
    margin: 0 0 12px;
    color: #7a7168;
    font-size: 12px;
    line-height: 1.6;
  }
}

.question-chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.question-chip {
  appearance: none;
  border: 1px solid rgba(212, 132, 90, 0.16);
  background: linear-gradient(180deg, #ffffff 0%, #fdf8f3 100%);
  color: #4d463f;
  padding: 9px 12px;
  border-radius: 999px;
  cursor: pointer;
  font-size: 13px;
  line-height: 1.4;
  text-align: left;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, color 0.2s ease;
  max-width: 100%;

  &:hover {
    border-color: var(--category-accent);
    color: var(--category-accent);
    transform: translateY(-1px);
    box-shadow: 0 8px 16px rgba(180, 140, 100, 0.12);
  }

  &:active {
    transform: translateY(0);
  }
}

.quick-questions-footer {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed rgba(232, 228, 222, 0.9);
  color: #9e9e9e;
  font-size: 12px;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.22s cubic-bezier(0.22, 1, 0.36, 1);
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.985);
}

@media (max-width: 960px) {
  .quick-questions-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .quick-questions-panel-fixed {
    padding: 14px;
    border-radius: 16px;
  }

  .quick-questions-header {
    margin-bottom: 12px;

    h3 {
      font-size: 16px;
    }
  }

  .question-category-card {
    padding: 12px;
  }

  .question-chip {
    width: 100%;
  }
}
</style>
