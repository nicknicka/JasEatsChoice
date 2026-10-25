/**
 * ChatWelcomeGuide - 聊天空状态引导组件
 *
 * 功能：
 * - 新用户引导
 * - 示例问题入口
 * - 轻量化欢迎说明
 *
 * @date 2026-03-31
 */

<template>
	<view class="welcome-guide">
		<view class="guide-header">
			<view class="status-badge">
				<view class="status-dot"></view>
				<text class="status-text">在线助手</text>
			</view>
			<text class="guide-title">想吃什么，直接问我</text>
			<text class="guide-subtitle">推荐菜品、分析热量、生成饮食建议，都可以在这里完成。</text>
		</view>

		<view class="guide-features">
			<view class="feature-item" v-for="(feature, index) in features" :key="index">
				<text class="feature-mark">{{ feature.mark }}</text>
				<text class="feature-title">{{ feature.title }}</text>
			</view>
		</view>

		<view class="guide-examples">
			<text class="examples-title">可以这样开始</text>
			<view class="example-list">
				<view
					class="example-item"
					v-for="(example, index) in examples"
					:key="index"
					@click="handleExampleClick(example)"
				>
					<text class="example-text">{{ example }}</text>
					<text class="example-arrow">></text>
				</view>
			</view>
		</view>

		<view class="guide-actions">
			<button class="action-btn primary" @click="handleStartChat">立即开始</button>
			<button class="action-btn secondary" @click="handleShowQuickQuestions">
				查看快捷问题
			</button>
		</view>
	</view>
</template>

<script setup>
const emit = defineEmits(['start', 'showQuestions'])

const features = [
	{ mark: '餐', title: '推荐点餐' },
	{ mark: '卡', title: '热量分析' },
	{ mark: '配', title: '饮食建议' }
]

const examples = [
	'推荐一份适合减脂的午餐',
	'今日卡路里摄入建议',
	'帮我搭配一份高蛋白晚餐'
]

const handleExampleClick = (example) => {
	emit('start', example)
}

const handleStartChat = () => {
	emit('start')
}

const handleShowQuickQuestions = () => {
	emit('showQuestions')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.welcome-guide {
	display: flex;
	flex-direction: column;
	gap: $spacing-xl;
	padding: $spacing-xl $spacing-lg calc($spacing-xl + env(safe-area-inset-bottom));
	box-sizing: border-box;
}

.guide-header {
	display: flex;
	flex-direction: column;
	align-items: flex-start;
	gap: $spacing-sm;
	text-align: left;
	padding: $spacing-lg;
	background: $bg-color-white;
}

.status-badge {
	display: inline-flex;
	align-items: center;
	gap: 10rpx;
	padding: 10rpx 18rpx;
	background: $success-50;
	border-radius: $border-radius-round;
}

.status-dot {
	width: 12rpx;
	height: 12rpx;
	border-radius: 50%;
	background: $success-color;
}

.status-text {
	font-size: $font-size-sm;
	color: #2f7d32;
	font-weight: $font-weight-medium;
}

.guide-title {
	display: block;
	font-size: 44rpx;
	font-weight: $font-weight-bold;
	color: $text-color-primary;
	line-height: 1.3;
}

.guide-subtitle {
	display: block;
	font-size: $font-size-base;
	color: $text-color-secondary;
	line-height: $line-height-base;
}

.guide-features {
	display: flex;
	flex-wrap: wrap;
	gap: $spacing-sm;
}

.feature-item {
	display: flex;
	align-items: center;
	gap: 10rpx;
	padding: 14rpx 20rpx;
	background: $primary-50;
	border: 1rpx solid $primary-200;
	border-radius: $border-radius-round;
}

.feature-mark {
	font-size: 22rpx;
	font-weight: $font-weight-bold;
	color: $primary-500;
	line-height: 1;
}

.feature-title {
	font-size: $font-size-sm;
	font-weight: $font-weight-medium;
	color: $text-color-primary;
}

.guide-examples {
	display: flex;
	flex-direction: column;
	gap: $spacing-md;
}

.examples-title {
	display: block;
	font-size: $font-size-sm;
	font-weight: $font-weight-bold;
	color: $text-color-primary;
	letter-spacing: 2rpx;
}

.example-list {
	display: flex;
	flex-direction: column;
	gap: $spacing-sm;
}

.example-item {
	@include flex-between;
	align-items: center;
	padding: $spacing-md;
	background: $bg-color-white;
	border-radius: $border-radius-md;
	box-shadow: $box-shadow-sm;
	transition: $transition-base;

	&:active {
		transform: scale(0.98);
		box-shadow: $box-shadow-md;
	}
}

.example-text {
	flex: 1;
	font-size: $font-size-sm;
	color: $text-color-primary;
	text-align: left;
	line-height: $line-height-base;
}

.example-arrow {
	font-size: $font-size-sm;
	color: $primary-500;
	font-weight: $font-weight-bold;
	line-height: 1;
}

.guide-actions {
	display: flex;
	gap: $spacing-sm;
}

.action-btn {
	flex: 1;
	@include flex-center;
	padding: $spacing-md 0;
	border-radius: $border-radius-round;
	font-size: $font-size-base;
	font-weight: $font-weight-medium;
	transition: $transition-base;
	border: none;
	outline: none;

	&::after {
		border: none;
	}

	&.primary {
		background: $primary-500;
		color: $bg-color-white;

		&:active {
			transform: scale(0.98);
		}
	}

	&.secondary {
		background: transparent;
		color: $primary-500;
		border: 2rpx solid $primary-500;

		&:active {
			background: $primary-50;
		}
	}
}
</style>
