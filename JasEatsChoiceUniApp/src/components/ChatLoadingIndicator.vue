/**
 * ChatLoadingIndicator - 聊天加载状态指示器
 *
 * 功能：
 * - 显示不同的加载状态
 * - 连接中、思考中、流式输出
 * - 流畅的动画效果
 *
 * @author Claude
 * @date 2026-03-31
 */

<template>
	<view class="loading-indicator" :class="`state-${state}`">
		<!-- 连接中状态 -->
		<template v-if="state === 'connecting'">
			<view class="connecting-animation">
				<view class="connecting-dot"></view>
				<view class="connecting-dot"></view>
				<view class="connecting-dot"></view>
			</view>
			<text class="loading-text">连接中...</text>
		</template>

		<!-- 思考中状态 -->
		<template v-else-if="state === 'typing'">
			<view class="typing-indicator">
				<view class="typing-dot"></view>
				<view class="typing-dot"></view>
				<view class="typing-dot"></view>
			</view>
			<text class="loading-text">AI思考中...</text>
		</template>

		<!-- 流式输出状态 -->
		<template v-else-if="state === 'streaming'">
			<view class="streaming-indicator">
				<view class="streaming-dot"></view>
				<view class="streaming-wave"></view>
				<view class="streaming-dot"></view>
			</view>
			<text class="loading-text">正在输入...</text>
		</template>

		<!-- 默认加载状态 -->
		<template v-else>
			<view class="default-spinner">
				<view class="spinner"></view>
			</view>
			<text class="loading-text">加载中...</text>
		</template>
	</view>
</template>

<script setup>
import { computed } from 'vue'

/**
 * ChatLoadingIndicator 组件
 */
const props = defineProps({
	/** 加载状态 */
	state: {
		type: String,
		default: 'idle', // idle | connecting | typing | streaming
		validator: (value) => ['idle', 'connecting', 'typing', 'streaming'].includes(value)
	}
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.loading-indicator {
	@include flex-center-column;
	gap: $spacing-md;
	padding: $spacing-lg;
}

.loading-text {
	font-size: $font-size-sm;
	color: $text-color-secondary;
}

/* 连接中动画 */
.connecting-animation {
	@include flex-center;
	gap: $spacing-sm;
}

.connecting-dot {
	width: 12rpx;
	height: 12rpx;
	background: $info-color;
	border-radius: 50%;
	animation: connectingPulse 1.4s infinite ease-in-out;

	&:nth-child(1) {
		animation-delay: 0s;
	}

	&:nth-child(2) {
		animation-delay: 0.2s;
	}

	&:nth-child(3) {
		animation-delay: 0.4s;
	}
}

@keyframes connectingPulse {
	0%,
	80%,
	100% {
		transform: scale(0.6);
		opacity: 0.5;
	}
	40% {
		transform: scale(1);
		opacity: 1;
	}
}

/* 思考中动画 */
.typing-indicator {
	@include flex-center;
	gap: 8rpx;
}

.typing-dot {
	width: 12rpx;
	height: 12rpx;
	background: $primary-500;
	border-radius: 50%;
	animation: typingBounce $duration-typing infinite ease-in-out;

	&:nth-child(1) {
		animation-delay: -0.32s;
	}

	&:nth-child(2) {
		animation-delay: -0.16s;
	}
}

@keyframes typingBounce {
	0%,
	80%,
	100% {
		transform: scale(0.6);
		opacity: 0.5;
	}
	40% {
		transform: scale(1);
		opacity: 1;
	}
}

/* 流式输出动画 */
.streaming-indicator {
	@include flex-center;
	gap: $spacing-sm;
}

.streaming-dot {
	width: 8rpx;
	height: 8rpx;
	background: $success-color;
	border-radius: 50%;
	animation: streamingPulse 1.5s infinite ease-in-out;
}

.streaming-wave {
	width: 40rpx;
	height: 4rpx;
	background: linear-gradient(90deg, transparent, $success-color, transparent);
	border-radius: 2rpx;
	animation: waveMove 1.5s infinite ease-in-out;
}

.streaming-dot:last-child {
	animation-delay: 0.3s;
}

@keyframes streamingPulse {
	0%,
	100% {
		opacity: 0.3;
		transform: scale(0.8);
	}
	50% {
		opacity: 1;
		transform: scale(1.2);
	}
}

@keyframes waveMove {
	0% {
		transform: translateX(-20rpx);
		opacity: 0;
	}
	50% {
		opacity: 1;
	}
	100% {
		transform: translateX(20rpx);
		opacity: 0;
	}
}

/* 默认加载动画 */
.default-spinner {
	@include flex-center;
}

.spinner {
	width: 40rpx;
	height: 40rpx;
	border: 4rpx solid $border-color-light;
	border-top-color: $primary-500;
	border-radius: 50%;
	animation: spin 1s linear infinite;
}

@keyframes spin {
	to {
		transform: rotate(360deg);
	}
}
</style>
