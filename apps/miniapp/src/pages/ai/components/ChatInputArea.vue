/**
 * ChatInputArea - 聊天输入区域组件
 *
 * 功能：
 * - 输入框
 * - 工具栏（表情、图片、清空）
 * - 发送按钮
 * - 已上传图片预览
 *

 * @date 2026-03-31
 */

<template>
	<view class="chat-input-area">
		<!-- 已上传图片预览 -->
		<view v-if="uploadedImages.length > 0" class="uploaded-images-preview">
			<view
				class="uploaded-image-item"
				v-for="(img, index) in uploadedImages"
				:key="index"
			>
				<image class="uploaded-image" :src="img.url" mode="aspectFill" />
				<view class="remove-image-btn" @click="$emit('removeImage', index)">
					<text class="remove-icon">×</text>
				</view>
			</view>
		</view>

		<!-- 表情面板 -->
		<view v-if="showEmojiPicker" class="emoji-panel">
			<view class="emoji-grid">
				<text
					class="emoji-item"
					v-for="emoji in commonEmojis"
					:key="emoji"
					@click="$emit('selectEmoji', emoji)"
				>
					{{ emoji }}
				</text>
			</view>
		</view>

		<!-- 工具栏 -->
		<view class="toolbar-row">
			<view class="toolbar-btn" @click="$emit('toggleEmoji')">
				<text class="toolbar-icon">表</text>
			</view>
			<view class="toolbar-btn" @click="$emit('chooseImage')">
				<text class="toolbar-icon">图</text>
			</view>
			<view class="toolbar-btn" @click="$emit('clearHistory')">
				<text class="toolbar-icon">清</text>
			</view>
			<view class="toolbar-btn" @click="$emit('toggleQuickQuestions')">
				<text class="toolbar-icon">问</text>
			</view>

			<!-- AI回复状态 -->
			<view v-if="isStreaming" class="action-row">
				<view class="streaming-status">
					<text class="status-dot">●</text>
					<text class="status-text">{{ loadingText }}</text>
				</view>
			</view>
		</view>

		<!-- 输入行 -->
		<view class="input-row">
			<input
				class="chat-input"
				type="text"
				:value="inputText"
				@input="$emit('update:inputText', $event.detail.value)"
				placeholder="输入您的饮食问题"
				:maxlength="500"
				@confirm="$emit('send')"
				confirm-type="send"
			/>

			<!-- 发送按钮 -->
			<view class="send-btn" :class="{ disabled: !canSend }" @click="handleSend">
				<view v-if="isStreaming" class="stop-icon" @click.stop="$emit('stop')">
					<text class="send-icon">停</text>
				</view>
				<text v-else class="send-icon">发</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed } from 'vue'

/**
 * ChatInputArea 组件
 */
const props = defineProps({
	/** 输入文本 */
	inputText: {
		type: String,
		default: ''
	},
	/** 是否正在流式传输 */
	isStreaming: {
		type: Boolean,
		default: false
	},
	/** 已上传的图片列表 */
	uploadedImages: {
		type: Array,
		default: () => []
	},
	/** 是否显示表情面板 */
	showEmojiPicker: {
		type: Boolean,
		default: false
	},
	/** 加载状态文本 */
	loadingText: {
		type: String,
		default: ''
	}
})

const emit = defineEmits([
	'update:inputText',
	'send',
	'stop',
	'toggleEmoji',
	'chooseImage',
	'clearHistory',
	'toggleQuickQuestions',
	'selectEmoji',
	'removeImage'
])

/** 是否可以发送 */
const canSend = computed(() => {
	return props.inputText.trim().length > 0
})

/**
 * 处理发送按钮点击
 */
const handleSend = () => {
	if (canSend.value) {
		emit('send')
	}
}
</script>

<style lang="scss" scoped>
@import '@/styles/mixins.scss';
@import '@/styles/variables.scss';

.chat-input-area {
	display: flex;
	flex-direction: column;
	background-color: $bg-color-white;
	padding: $spacing-sm $spacing-md;
	padding-bottom: calc(#{$spacing-sm} + env(safe-area-inset-bottom));
	border-top: 1rpx solid $border-color-light;
	flex-shrink: 0;
	width: 100%;
	height: auto;
	box-sizing: border-box;
}

.uploaded-images-preview {
	display: flex;
	flex-wrap: wrap;
	gap: $spacing-sm;
	padding: $spacing-sm 0;
	background: $bg-color-white;
}

.uploaded-image-item {
	position: relative;
	width: 160rpx;
	height: 160rpx;
	border-radius: $border-radius-base;
	overflow: hidden;
	box-shadow: $box-shadow-sm;
}

.uploaded-image {
	width: 100%;
	height: 100%;
}

.remove-image-btn {
	position: absolute;
	top: $spacing-xs;
	right: $spacing-xs;
	width: 40rpx;
	height: 40rpx;
	@include flex-center;
	background: rgba(0, 0, 0, 0.6);
	border-radius: 50%;
	transition: $transition-base;

	&:active {
		transform: scale(0.9);
		background: rgba(0, 0, 0, 0.8);
	}
}

.remove-icon {
	color: $bg-color-white;
	font-size: 28rpx;
	line-height: 1;
}

.emoji-panel {
	padding: $spacing-sm 0 $spacing-md;
	background: $bg-color-white;
	max-height: 300rpx;
	overflow-y: auto;
}

.emoji-grid {
	display: flex;
	flex-wrap: wrap;
	gap: $spacing-sm;
}

.emoji-item {
	font-size: 48rpx;
	padding: $spacing-xs;
	transition: $transition-base;

	&:active {
		transform: scale(1.2);
	}
}

.toolbar-row {
	display: flex;
	align-items: center;
	flex-wrap: wrap;
	gap: $spacing-sm;
	padding-bottom: $spacing-sm;
}

.toolbar-btn {
	width: 56rpx;
	height: 56rpx;
	@include flex-center;
	background: transparent;
	border-radius: 50%;
	transition: $transition-base;

	&:active {
		transform: scale(0.95);
		background: $primary-100;
	}
}

.toolbar-icon {
	font-size: $font-size-sm;
	color: $text-color-regular;
	font-weight: $font-weight-medium;
	line-height: 1;
}

.action-row {
	display: flex;
	align-items: center;
	margin-left: auto;
	padding: 0;
}

.streaming-status {
	@include flex-center;
	gap: $spacing-xs;
}

.status-dot {
	font-size: $font-size-xs;
	color: $primary-500;
	animation: pulse 1.5s infinite;
}

@keyframes pulse {
	0%,
	100% {
		opacity: 1;
	}
	50% {
		opacity: 0.3;
	}
}

.status-text {
	font-size: $font-size-sm;
	color: $text-color-secondary;
}

.input-row {
	display: flex;
	align-items: flex-end;
	gap: $spacing-sm;
}

.chat-input {
	flex: 1;
	height: $input-height-current;
	padding: 0 $spacing-lg;
	background-color: $bg-color-input;
	border-radius: $border-radius-round;
	font-size: $font-size-base;
	color: $text-color-primary;
	border: 2rpx solid transparent;
	transition: $transition-base;

	&:focus {
		border-color: $primary-500;
		background-color: $bg-color-white;
	}
}

.send-btn {
	width: 72rpx;
	height: 72rpx;
	@include flex-center;
	background: $primary-500;
	color: $bg-color-white;
	border-radius: 50%;
	transition: $transition-base;

	&:active {
		transform: scale(0.95);
	}

	&.disabled {
		background: $bg-color-base;
		color: $text-color-disabled;
	}
}

.stop-icon {
	@include flex-center;
	width: 100%;
	height: 100%;
}

.send-icon {
	font-size: $font-size-sm;
	color: $bg-color-white;
	font-weight: $font-weight-bold;
	line-height: 1;
	opacity: 1;
}

.send-btn.disabled .send-icon {
	opacity: 0.5;
}
</style>
