<script setup>
import { useLoginTransition } from '../composables/useLoginTransition'

const { isVisible, isFadingOut } = useLoginTransition()
</script>

<template>
  <Teleport to="body">
    <div
      v-if="isVisible"
      class="login-transition-overlay"
      :class="{ 'is-fading-out': isFadingOut }"
    >
      <div class="transition-content">
        <!-- 品牌标志动画 -->
        <div class="logo-animation">
          <svg
            class="brand-icon"
            width="56"
            height="56"
            viewBox="0 0 48 48"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M6 20C6 20 8 34 24 34C40 34 42 20 42 20"
              stroke="#E8825C"
              stroke-width="2.5"
              stroke-linecap="round"
              fill="none"
            />
            <path d="M4 20H44" stroke="#E8825C" stroke-width="2" stroke-linecap="round" opacity="0.6" />
            <path d="M20 16L30 4" stroke="#E8825C" stroke-width="2" stroke-linecap="round" opacity="0.8" />
            <path d="M24 16L36 6" stroke="#E8825C" stroke-width="2" stroke-linecap="round" opacity="0.8" />
            <path d="M18 38H30" stroke="#E8825C" stroke-width="2" stroke-linecap="round" opacity="0.4" />
          </svg>
        </div>

        <!-- 品牌名称 -->
        <h2 class="brand-title">佳食宜选</h2>

        <!-- 蒸汽动画 -->
        <div class="steam-container">
          <div class="steam steam-1"></div>
          <div class="steam steam-2"></div>
          <div class="steam steam-3"></div>
        </div>

        <!-- 加载提示 -->
        <p class="loading-text">
          <span class="dot-container">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </span>
          正在为您准备美食之旅
        </p>
      </div>
    </div>
  </Teleport>
</template>

<style scoped lang="less">
@accent: #E8825C;
@accent-warm: #F2784B;
@bg-dark: #1E1E2E;

.login-transition-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #FFF7F2 0%, #FFF0E8 40%, #FFE8DC 100%);
  opacity: 1;
  transition: opacity 0.6s cubic-bezier(0.4, 0, 0.2, 1);

  &.is-fading-out {
    opacity: 0;
    pointer-events: none;
  }
}

.transition-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

// 品牌图标 — 缩放呼吸动画
.logo-animation {
  animation: logo-pulse 2s ease-in-out infinite;
}

.brand-icon {
  filter: drop-shadow(0 4px 12px rgba(232, 130, 92, 0.3));
}

@keyframes logo-pulse {
  0%, 100% {
    transform: scale(1) translateY(0);
  }
  50% {
    transform: scale(1.08) translateY(-4px);
  }
}

// 品牌名称 — 渐入
.brand-title {
  font-size: 28px;
  font-weight: 700;
  color: #2C3E50;
  letter-spacing: 6px;
  margin: 0;
  animation: title-fade-in 0.8s ease-out 0.2s both;
}

@keyframes title-fade-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 蒸汽效果
.steam-container {
  position: relative;
  width: 40px;
  height: 30px;
  margin-top: -8px;
}

.steam {
  position: absolute;
  bottom: 0;
  width: 3px;
  height: 12px;
  background: linear-gradient(to top, rgba(232, 130, 92, 0.4), transparent);
  border-radius: 50%;
  animation: steam-rise 1.5s ease-out infinite;
}

.steam-1 {
  left: 8px;
  animation-delay: 0s;
}

.steam-2 {
  left: 18px;
  animation-delay: 0.3s;
}

.steam-3 {
  left: 28px;
  animation-delay: 0.6s;
}

@keyframes steam-rise {
  0% {
    opacity: 0.6;
    transform: translateY(0) scaleX(1);
  }
  50% {
    opacity: 0.3;
    transform: translateY(-10px) scaleX(1.5);
  }
  100% {
    opacity: 0;
    transform: translateY(-20px) scaleX(2);
  }
}

// 加载文字 — 跳动点
.loading-text {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #8E9AAF;
  letter-spacing: 2px;
  margin: 12px 0 0;
  animation: title-fade-in 0.8s ease-out 0.5s both;
}

.dot-container {
  display: inline-flex;
  gap: 4px;
  margin-right: 4px;
}

.dot {
  display: inline-block;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: @accent;
  animation: dot-bounce 1.4s ease-in-out infinite;

  &:nth-child(2) {
    animation-delay: 0.2s;
  }

  &:nth-child(3) {
    animation-delay: 0.4s;
  }
}

@keyframes dot-bounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
