<template>
	<view class="chat-message-list">
		<view v-if="isEmpty" class="empty-state">
			<view class="empty-icon">
				<text>聊</text>
			</view>
			<text class="empty-text">暂无消息，开始对话吧</text>
		</view>

		<scroll-view
			v-else
			class="message-scroll"
			scroll-y
			enhanced
			show-scrollbar="false"
			:scroll-into-view="scrollIntoView"
			:scroll-with-animation="true"
		>
			<view class="message-list">
				<view
					v-for="(msg, index) in messages"
					:key="msg.id"
					class="message-item"
					:id="'msg-' + index"
				>
					<ChatMessageItem :message="msg" />
				</view>
				<view class="message-list-bottom"></view>
			</view>
		</scroll-view>

		<view v-if="showLoadingMore" class="loading-more">
			<text class="loading-text">加载更多...</text>
		</view>
	</view>
</template>

<script setup>
import { computed } from 'vue'
import ChatMessageItem from './ChatMessageItem.vue'

/**
 * ChatMessageList - 聊天消息列表组件
 *
 * 功能：
 * - 使用 uni-list 实现虚拟滚动
 * - 支持大量消息时保持流畅
 * - 自动滚动到底部
 *

 * @date 2026-03-31
 */

const props = defineProps({
	/** 消息列表 */
	messages: {
		type: Array,
		default: () => []
	},
	/** 滚动位置 */
	scrollIntoView: {
		type: String,
		default: ''
	},
	/** 是否显示加载更多 */
	showLoadingMore: {
		type: Boolean,
		default: false
	}
})

/** 是否为空状态 */
const isEmpty = computed(() => {
	return !props.messages || props.messages.length === 0
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.chat-message-list {
	flex: 1;
	height: 100%;
	min-height: 0;
	width: 100%;
	display: flex;
	flex-direction: column;
	overflow: hidden;
}

.empty-state {
	@include flex-center-column;
	flex: 1;
	justify-content: center;
	align-items: center;
	padding: 120rpx $spacing-xl calc(120rpx + env(safe-area-inset-bottom));
	text-align: center;
}

.empty-icon {
	width: 96rpx;
	height: 96rpx;
	margin-bottom: $spacing-lg;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 50%;
	background: $bg-color-base;
	color: #d6d6d6;
	font-size: 48rpx;
	font-weight: $font-weight-bold;
}

.empty-text {
	font-size: $font-size-base;
	color: $text-color-secondary;
	display: block;
}

.message-scroll {
	flex: 1;
	height: 100%;
	min-height: 0;
	width: 100%;
	overflow: hidden;
}

.message-list {
	display: flex;
	flex-direction: column;
	padding: $spacing-lg $spacing-lg $spacing-md;
	box-sizing: border-box;
	min-height: 100%;
}

.message-item {
	padding: 0;
	margin: 0;
	background: transparent;
	border: none;

	&::after {
		display: none;
	}
}

.message-list-bottom {
	height: $spacing-lg;
	flex-shrink: 0;
}

.loading-more {
	@include flex-center;
	padding: $spacing-lg;
	background: $bg-color-white;
}

.loading-text {
	font-size: $font-size-sm;
	color: $text-color-secondary;
}
</style>
