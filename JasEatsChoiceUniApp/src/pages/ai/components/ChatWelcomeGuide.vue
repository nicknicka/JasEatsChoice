/**
 * ChatWelcomeGuide - 聊天空状态引导组件
 *
 * 功能：
 * - 新用户引导
 * - 功能介绍
 * - 快捷入口
 * - 友好的视觉设计
 *
 * @author Claude
 * @date 2026-03-31
 */

<template>
	<view class="welcome-guide">
		<!-- 头部图标和动画 -->
		<view class="guide-header">
			<text class="guide-icon">👋</text>
			<text class="guide-title">欢迎使用 AI 饮食助手</text>
			<text class="guide-subtitle">智能、健康、个性化的饮食建议</text>
		</view>

		<!-- 功能介绍 -->
		<view class="guide-features">
			<view class="feature-item" v-for="(feature, index) in features" :key="index">
				<view class="feature-icon">{{ feature.icon }}</view>
				<view class="feature-content">
					<text class="feature-title">{{ feature.title }}</text>
					<text class="feature-desc">{{ feature.desc }}</text>
				</view>
			</view>
		</view>

		<!-- 示例问题 -->
		<view class="guide-examples">
			<text class="examples-title">您可以问我：</text>
			<view class="example-list">
				<view
					class="example-item"
					v-for="(example, index) in examples"
					:key="index"
					@click="handleExampleClick(example)"
				>
					<text class="example-text">{{ example }}</text>
					<text class="example-icon">→</text>
				</view>
			</view>
		</view>

		<!-- 操作按钮 -->
		<view class="guide-actions">
			<button class="action-btn primary" @click="handleStartChat">
				<text class="btn-text">开始对话</text>
				<text class="btn-icon">💬</text>
			</button>
			<button class="action-btn secondary" @click="handleShowQuickQuestions">
				<text class="btn-text">查看快捷提问</text>
				<text class="btn-icon">⚡</text>
			</button>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'

/**
 * ChatWelcomeGuide 组件
 */
const emit = defineEmits(['start', 'showQuestions'])

/** 功能列表 */
const features = ref([
	{
		icon: '🍱',
		title: '推荐健康食谱',
		desc: '根据您的需求推荐合适的菜品'
	},
	{
		icon: '📊',
		title: '分析营养成分',
		desc: '详细分析食物的营养价值'
	},
	{
		icon: '🎯',
		title: '制定饮食计划',
		desc: '个性化的饮食建议和计划'
	}
])

/** 示例问题 */
const examples = ref([
	'推荐适合减肥的食谱',
	'今日卡路里摄入建议',
	'如何搭配营养均衡的饮食',
	'推荐低卡路里零食'
])

/**
 * 点击示例问题
 */
const handleExampleClick = (example) => {
	emit('start', example)
}

/**
 * 开始对话
 */
const handleStartChat = () => {
	emit('start')
}

/**
 * 显示快捷提问
 */
const handleShowQuickQuestions = () => {
	emit('showQuestions')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.welcome-guide {
	@include flex-center-column;
	padding: 80rpx $spacing-xl;
	text-align: center;
	animation: welcomeFadeIn $duration-slow ease-out;
}

@keyframes welcomeFadeIn {
	from {
		opacity: 0;
		transform: translateY(30rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

/* 头部 */
.guide-header {
	@include flex-center-column;
	margin-bottom: 60rpx;
}

.guide-icon {
	font-size: 140rpx;
	margin-bottom: $spacing-lg;
	display: block;
	animation: iconWave 2s ease-in-out infinite;
}

@keyframes iconWave {
	0%,
	100% {
		transform: rotate(0deg) scale(1);
	}
	25% {
		transform: rotate(-10deg) scale(1.1);
	}
	75% {
		transform: rotate(10deg) scale(1.1);
	}
}

.guide-title {
	display: block;
	font-size: 48rpx;
	font-weight: $font-weight-bold;
	color: $text-color-primary;
	margin-bottom: $spacing-sm;
}

.guide-subtitle {
	display: block;
	font-size: $font-size-base;
	color: $text-color-secondary;
	line-height: $line-height-lg;
}

/* 功能介绍 */
.guide-features {
	display: flex;
	flex-direction: column;
	gap: $spacing-lg;
	margin-bottom: 60rpx;
	width: 100%;
	max-width: 600rpx;
}

.feature-item {
	display: flex;
	align-items: center;
	gap: $spacing-md;
	padding: $spacing-lg;
	background: $bg-color-white;
	border-radius: $border-radius-lg;
	box-shadow: $box-shadow-sm;
	transition: $transition-base;

	&:active {
		transform: scale(0.98);
		box-shadow: $box-shadow-md;
	}
}

.feature-icon {
	font-size: 64rpx;
	flex-shrink: 0;
}

.feature-content {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: $spacing-xs;
	text-align: left;
}

.feature-title {
	font-size: $font-size-lg;
	font-weight: $font-weight-bold;
	color: $text-color-primary;
}

.feature-desc {
	font-size: $font-size-sm;
	color: $text-color-secondary;
	line-height: $line-height-base;
}

/* 示例问题 */
.guide-examples {
	width: 100%;
	max-width: 600rpx;
	margin-bottom: 60rpx;
}

.examples-title {
	display: block;
	font-size: $font-size-md;
	font-weight: $font-weight-medium;
	color: $text-color-primary;
	margin-bottom: $spacing-md;
}

.example-list {
	display: flex;
	flex-direction: column;
	gap: $spacing-sm;
}

.example-item {
	@include flex-between;
	align-items: center;
	padding: $spacing-md $spacing-lg;
	background: $primary-50;
	border: 1rpx solid $primary-200;
	border-radius: $border-radius-base;
	transition: $transition-base;

	&:active {
		transform: scale(0.98);
		background: $primary-100;
		border-color: $primary-400;
	}
}

.example-text {
	flex: 1;
	font-size: $font-size-base;
	color: $text-color-primary;
	text-align: left;
}

.example-icon {
	font-size: $font-size-xl;
	color: $primary-500;
	margin-left: $spacing-md;
}

/* 操作按钮 */
.guide-actions {
	display: flex;
	flex-direction: column;
	gap: $spacing-md;
	width: 100%;
	max-width: 500rpx;
}

.action-btn {
	@include flex-between;
	align-items: center;
	justify-content: center;
	gap: $spacing-sm;
	padding: $spacing-lg $spacing-xl;
	border-radius: $border-radius-lg;
	font-size: $font-size-lg;
	font-weight: $font-weight-medium;
	transition: $transition-base;
	border: none;
	outline: none;

	&::after {
		border: none;
	}

	&.primary {
		background: linear-gradient(135deg, $primary-500, $primary-800);
		color: $bg-color-white;
		box-shadow: $box-shadow-md;

		&:active {
			transform: scale(0.98);
			box-shadow: $box-shadow-sm;
		}
	}

	&.secondary {
		background: $bg-color-white;
		color: $primary-500;
		border: 2rpx solid $primary-500;

		&:active {
			background: $primary-50;
		}
	}
}

.btn-text {
	flex: 1;
	font-size: $font-size-lg;
}

.btn-icon {
	font-size: $font-size-xl;
}
</style>
