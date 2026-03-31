<template>
	<view class="chat-message-list">
		<!-- 空状态 -->
		<view v-if="isEmpty" class="empty-state">
			<text class="empty-icon">💬</text>
			<text class="empty-text">暂无消息，开始对话吧</text>
		</view>

		<!-- 消息列表（使用 uni-list 实现虚拟滚动） -->
		<uni-list
			v-else
			class="message-list"
			:scroll-into-view="scrollIntoView"
			:scroll-with-animation="true"
		>
			<uni-list-item
				v-for="(msg, index) in messages"
				:key="msg.id"
				class="message-item"
				:id="'msg-' + index"
			>
				<ChatMessageItem :message="msg" />
			</uni-list-item>
		</uni-list>

		<!-- 加载更多提示（可选） -->
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
 * @author Claude
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

.chat-message-list {
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
}

.empty-state {
	@include flex-center-column;
	align-items: center;
	padding: 120rpx $spacing-xl;
	text-align: center;
}

.empty-icon {
	font-size: 120rpx;
	margin-bottom: $spacing-lg;
	opacity: 0.3;
	display: block;
}

.empty-text {
	font-size: $font-size-base;
	color: $text-color-secondary;
	display: block;
}

.message-list {
	flex: 1;
	width: 100%;
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
