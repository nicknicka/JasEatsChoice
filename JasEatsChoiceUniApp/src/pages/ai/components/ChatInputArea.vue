/**
 * ChatInputArea - 聊天输入区域组件
 *
 * 功能：
 * - 输入框
 * - 工具栏（表情、图片、清空）
 * - 发送按钮
 * - 已上传图片预览
 *
 * @author Claude
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
				<text class="toolbar-icon">😊</text>
			</view>
			<view class="toolbar-btn" @click="$emit('chooseImage')">
				<text class="toolbar-icon">🖼️</text>
			</view>
			<view class="toolbar-btn" @click="$emit('clearHistory')">
				<text class="toolbar-icon">🗑️</text>
			</view>
			<view class="toolbar-btn" @click="$emit('toggleQuickQuestions')">
				<text class="toolbar-icon">💬</text>
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
				<text v-if="isStreaming" class="stop-icon" @click.stop="$emit('stop')">⏹️</text>
				<text v-else class="send-icon">➤</text>
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
@import '@/styles/variables.scss';

.chat-input-area {
	display: flex;
	flex-direction: column;
	background-color: $bg-color-white;
	padding: $spacing-md $spacing-lg;
	border-top: 1rpx solid $border-color-light;
	box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.04);
	flex-shrink: 0;
	width: 100%;
	min-height: 144rpx;
	height: auto;
}

.uploaded-images-preview {
	display: flex;
	flex-wrap: wrap;
	gap: $spacing-md;
	padding: $spacing-md $spacing-lg;
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
	font-size: 32rpx;
	font-weight: bold;
	line-height: 1;
}

.emoji-panel {
	padding: $spacing-md $spacing-lg;
	background: $bg-color-white;
	border-top: 1rpx solid $border-color-light;
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
	gap: $spacing-md;
	padding: $spacing-sm $spacing-lg;
	border-bottom: 1rpx solid $border-color-light;
}

.toolbar-btn {
	width: 48rpx;
	height: 48rpx;
	@include flex-center;
	background: $bg-color-base;
	border-radius: $border-radius-base;
	transition: $transition-base;

	&:active {
		transform: scale(0.95);
		background: $primary-100;
	}
}

.toolbar-icon {
	font-size: 32rpx;
}

.action-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: $spacing-md;
	padding: $spacing-sm 0;
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
	align-items: center;
	gap: $spacing-md;
}

.chat-input {
	flex: 1;
	max-width: 77%;
	height: $input-height-current;
	padding: 0 $spacing-md;
	margin: $spacing-sm 0;
	background-color: $bg-color-input;
	border-radius: 40rpx;
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
	width: 64rpx;
	height: 64rpx;
	@include flex-center;
	background: linear-gradient(135deg, $primary-500, $primary-800);
	color: $bg-color-white;
	border-radius: 50%;
	transition: $transition-base;
	box-shadow: $box-shadow-md;

	&:active {
		transform: scale(0.95);
		box-shadow: $box-shadow-sm;
	}

	&.disabled {
		opacity: 0.5;
		background: $bg-color-base;
	}
}

.send-icon {
	font-size: 28rpx;
	font-weight: $font-weight-bold;
}

.stop-icon {
	font-size: 32rpx;
}
</style>
