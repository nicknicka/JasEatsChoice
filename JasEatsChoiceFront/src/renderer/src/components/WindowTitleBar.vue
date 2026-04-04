<template>
  <div class="window-titlebar" :class="{ 'titlebar-dark': dark }">
    <!-- macOS 红绿灯按钮（左侧） -->
    <div v-if="isMac" class="titlebar-controls titlebar-left">
      <button class="control-btn mac-btn mac-close" @click="handleClose" title="关闭">
        <svg width="12" height="12" viewBox="0 0 12 12">
          <line x1="3.5" y1="3.5" x2="8.5" y2="8.5" stroke="currentColor" stroke-width="1.1" />
          <line x1="8.5" y1="3.5" x2="3.5" y2="8.5" stroke="currentColor" stroke-width="1.1" />
        </svg>
      </button>
      <button class="control-btn mac-btn mac-minimize" @click="handleMinimize" title="最小化">
        <svg width="12" height="12" viewBox="0 0 12 12">
          <line x1="3" y1="6" x2="9" y2="6" stroke="currentColor" stroke-width="1.1" />
        </svg>
      </button>
    </div>

    <!-- 可拖拽区域 -->
    <div class="titlebar-drag-area">
      <span v-if="title && !isMac" class="titlebar-title">{{ title }}</span>
    </div>

    <!-- Windows/Linux 按钮（右侧） -->
    <div v-if="!isMac" class="titlebar-controls titlebar-right">
      <button v-if="showMinimize" class="control-btn win-btn" @click="handleMinimize" title="最小化">
        <svg width="12" height="12" viewBox="0 0 12 12">
          <line x1="2" y1="6" x2="10" y2="6" stroke="currentColor" stroke-width="1" />
        </svg>
      </button>
      <button class="control-btn win-btn win-close" @click="handleClose" title="关闭">
        <svg width="12" height="12" viewBox="0 0 12 12">
          <line x1="2" y1="2" x2="10" y2="10" stroke="currentColor" stroke-width="1" />
          <line x1="10" y1="2" x2="2" y2="10" stroke="currentColor" stroke-width="1" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

defineProps({
  showMinimize: { type: Boolean, default: true },
  title: { type: String, default: '' },
  dark: { type: Boolean, default: false }
})

const isMac = computed(() => navigator.platform.toLowerCase().includes('mac'))

const handleClose = async () => {
  if (window.api?.window?.close) {
    await window.api.window.close()
  }
}

const handleMinimize = async () => {
  if (window.api?.window?.minimize) {
    await window.api.window.minimize()
  }
}
</script>

<style scoped lang="less">
.window-titlebar {
  width: 100%;
  height: 38px;
  display: flex;
  align-items: center;
  user-select: none;
  -webkit-app-region: drag;
  position: relative;
  z-index: 10;
  flex-shrink: 0;
}

.titlebar-drag-area {
  flex: 1;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.titlebar-title {
  font-size: 12px;
  font-weight: 500;
  opacity: 0.7;
  letter-spacing: 0.5px;
}

.titlebar-controls {
  display: flex;
  align-items: center;
  height: 100%;
  -webkit-app-region: no-drag;
}

.titlebar-left {
  padding-left: 12px;
  gap: 2px;
}

.titlebar-right {
  padding-right: 4px;
}

// macOS 红绿灯按钮
.mac-btn {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: default;
  transition: all 0.15s ease;
  position: relative;

  svg {
    opacity: 0;
    transition: opacity 0.15s;
    color: rgba(0, 0, 0, 0.6);
  }
}

.mac-close {
  background: #ff5f57;
  &:hover {
    background: #ff3b30;
    svg { opacity: 1; }
  }
}

.mac-minimize {
  background: #ffbd2e;
  &:hover {
    background: #f0a000;
    svg { opacity: 1; }
  }
}

// Windows/Linux 按钮
.win-btn {
  width: 46px;
  height: 100%;
  border: none;
  background: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.6);
  transition: all 0.15s ease;
  outline: none;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: rgba(255, 255, 255, 0.95);
  }
}

.win-close:hover {
  background: rgba(232, 72, 72, 0.85) !important;
  color: white !important;
}

// 暗色模式（主窗口使用时）
.titlebar-dark {
  .titlebar-title {
    color: rgba(0, 0, 0, 0.7);
  }

  .win-btn {
    color: rgba(0, 0, 0, 0.5);
    &:hover {
      background: rgba(0, 0, 0, 0.06);
      color: rgba(0, 0, 0, 0.85);
    }
  }

  .win-close:hover {
    background: rgba(232, 72, 72, 0.85) !important;
    color: white !important;
  }
}
</style>
