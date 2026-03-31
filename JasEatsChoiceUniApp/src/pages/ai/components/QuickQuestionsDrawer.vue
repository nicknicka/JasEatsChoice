/**
 * QuickQuestionsDrawer - 快捷提问抽屉组件
 *
 * 功能：
 * - 底部抽屉式设计
 * - 不遮挡聊天内容
 * - 支持手势滑动关闭
 * - 流畅的动画效果
 *
 * @author Claude
 * @date 2026-03-31
 */

<template>
	<view class="quick-questions-drawer-container">
		<!-- 遮罩层 -->
		<view
			v-if="visible"
			class="drawer-overlay"
			:class="{ show: visible }"
			@click="handleClose"
		></view>

		<!-- 抽屉内容 -->
		<view
			class="drawer-content"
			:class="{ expanded: visible }"
			@touchstart="handleTouchStart"
			@touchmove="handleTouchMove"
			@touchend="handleTouchEnd"
		>
			<!-- 拖动指示器 -->
			<view class="drawer-indicator" @click="handleClose">
				<view class="indicator-bar"></view>
			</view>

			<!-- 标题栏 -->
			<view class="drawer-header">
				<text class="header-title">💬 快捷提问</text>
				<view class="close-btn" @click="handleClose">
					<text class="close-icon">✕</text>
				</view>
			</view>

			<!-- 问题列表 -->
			<scroll-view class="questions-list" scroll-y>
				<view
					class="question-item"
					v-for="(question, index) in questions"
					:key="index"
					@click="handleQuestionClick(question)"
				>
					<text class="question-text">{{ question }}</text>
					<text class="question-arrow">→</text>
				</view>
			</scroll-view>

			<!-- 底部提示 -->
			<view class="drawer-footer">
				<text class="footer-hint">下滑或点击遮罩关闭</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, watch } from 'vue'

/**
 * QuickQuestionsDrawer 组件
 */
const props = defineProps({
	/** 是否显示抽屉 */
	visible: {
		type: Boolean,
		default: false
	},
	/** 快捷提问列表 */
	questions: {
		type: Array,
		default: () => []
	}
})

const emit = defineEmits(['update:visible', 'select'])

/** 触摸起始位置 */
const touchStartY = ref(0)

/** 是否正在拖动 */
const isDragging = ref(false)

/**
 * 关闭抽屉
 */
const handleClose = () => {
	emit('update:visible', false)
}

/**
 * 选择问题
 */
const handleQuestionClick = (question) => {
	emit('select', question)
	handleClose()
}

/**
 * 触摸开始
 */
const handleTouchStart = (e) => {
	touchStartY.value = e.touches[0].clientY
	isDragging.value = true
}

/**
 * 触摸移动
 */
const handleTouchMove = (e) => {
	if (!isDragging.value || !props.visible) return

	const currentY = e.touches[0].clientY
	const deltaY = currentY - touchStartY.value

	// 如果向下滑动超过 50px，关闭抽屉
	if (deltaY > 50) {
		handleClose()
		isDragging.value = false
	}
}

/**
 * 触摸结束
 */
const handleTouchEnd = () => {
	isDragging.value = false
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.quick-questions-drawer-container {
	position: relative;
	width: 100%;
	height: 0;
}

/* 遮罩层 */
.drawer-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	opacity: 0;
	pointer-events: none;
	transition: opacity 0.3s ease;
	z-index: $z-index-modal;

	&.show {
		opacity: 1;
		pointer-events: auto;
	}
}

/* 抽屉内容 */
.drawer-content {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	max-height: 60vh;
	background: $bg-color-white;
	border-radius: 24rpx 24rpx 0 0;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.15);
	transform: translateY(100%);
	transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	z-index: $z-index-modal + 1;
	display: flex;
	flex-direction: column;

	&.expanded {
		transform: translateY(0);
	}
}

/* 拖动指示器 */
.drawer-indicator {
	@include flex-center;
	padding: $spacing-sm 0;
	background: $bg-color-white;
	border-radius: 24rpx 24rpx 0 0;
	position: sticky;
	top: 0;
	z-index: 1;
}

.indicator-bar {
	width: 80rpx;
	height: 8rpx;
	background: $border-color-dark;
	border-radius: 4rpx;
}

/* 标题栏 */
.drawer-header {
	@include flex-between;
	align-items: center;
	padding: $spacing-md $spacing-lg;
	border-bottom: 1rpx solid $border-color-light;
	background: $bg-color-white;
	position: sticky;
	top: 0;
	z-index: 1;
}

.header-title {
	font-size: $font-size-lg;
	font-weight: $font-weight-bold;
	color: $text-color-primary;
}

.close-btn {
	width: 48rpx;
	height: 48rpx;
	@include flex-center;
	background: $bg-color-base;
	border-radius: 50%;
	transition: $transition-fast;

	&:active {
		transform: scale(0.9);
		background: $bg-color-hover;
	}
}

.close-icon {
	font-size: 32rpx;
	color: $text-color-secondary;
	font-weight: bold;
}

/* 问题列表 */
.questions-list {
	flex: 1;
	padding: $spacing-md $spacing-lg;
	overflow-y: auto;
}

.question-item {
	@include flex-between;
	align-items: center;
	padding: $spacing-lg;
	background: $primary-50;
	border: 1rpx solid $primary-200;
	border-radius: $border-radius-base;
	margin-bottom: $spacing-sm;
	transition: $transition-base;

	&:last-child {
		margin-bottom: 0;
	}

	&:active {
		transform: scale(0.98);
		background: $primary-100;
		border-color: $primary-400;
		box-shadow: $box-shadow-md;
	}
}

.question-text {
	flex: 1;
	font-size: $font-size-base;
	color: $text-color-primary;
	line-height: $line-height-lg;
	font-weight: $font-weight-medium;
}

.question-arrow {
	font-size: $font-size-xl;
	color: $primary-500;
	margin-left: $spacing-md;
}

/* 底部提示 */
.drawer-footer {
	@include flex-center;
	padding: $spacing-sm;
	background: $bg-color-light;
	border-top: 1rpx solid $border-color-light;
}

.footer-hint {
	font-size: $font-size-xs;
	color: $text-color-secondary;
}
</style>
