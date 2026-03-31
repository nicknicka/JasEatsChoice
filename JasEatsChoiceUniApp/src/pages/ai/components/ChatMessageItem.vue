<template>
	<view class="message-wrapper">
		<view class="message" :class="{ user: message.isUser }">
			<!-- AI头像 -->
			<view class="message-avatar" v-if="!message.isUser">
				<text class="avatar-icon">🤖</text>
			</view>

			<!-- 消息内容 -->
			<view class="message-content" :class="{ user: message.isUser }">
				<!-- AI消息：if-else 切换 -->
				<template v-if="!message.isUser">
					<!-- 情况1：内容为空，显示加载动画 -->
					<view v-if="!message.content" class="typing-indicator">
						<view class="typing-dot"></view>
						<view class="typing-dot"></view>
						<view class="typing-dot"></view>
					</view>
					<template v-else>
						<!-- 优先显示卡片 -->
						<view v-if="message.messageType && message.cardData" class="card-wrapper">
							<!-- 菜品列表卡片 -->
							<DishListCard
								v-if="message.messageType === 'dish_list_card'"
								:data="message.cardData"
								@action="handleCardAction"
							/>

							<!-- 订单列表卡片 -->
							<OrderListCard
								v-if="message.messageType === 'order_list_card'"
								:data="message.cardData"
								@action="handleCardAction"
							/>

							<!-- 收藏列表卡片 -->
							<FavoriteListCard
								v-if="message.messageType === 'favorite_list_card'"
								:data="message.cardData"
								@action="handleCardAction"
							/>

							<!-- 用户信息卡片 -->
							<UserInfoCard
								v-if="message.messageType === 'user_info_card'"
								:data="message.cardData"
								@action="handleCardAction"
							/>

							<!-- 健康建议卡片 -->
							<HealthCard
								v-if="message.messageType === 'health_card'"
								:data="message.cardData"
							/>
						</view>

						<!-- 文本内容 -->
						<text class="content-text">{{ message.content }}</text>
						<text class="message-time">{{ message.time }}</text>
					</template>
				</template>

				<!-- 用户消息：正常显示 -->
				<template v-else>
					<text class="content-text">{{ message.content }}</text>
					<text class="message-time">{{ message.time }}</text>
				</template>
			</view>

			<!-- 用户头像 -->
			<view class="message-avatar user" v-if="message.isUser">
				<image
					v-if="userInfo.avatar"
					class="avatar-image"
					:src="userInfo.avatar"
					mode="aspectFill"
					lazy-load
				/>
				<text v-else class="avatar-icon">👤</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store'
import DishListCard from './cards/DishListCard.vue'
import OrderListCard from './cards/OrderListCard.vue'
import FavoriteListCard from './cards/FavoriteListCard.vue'
import UserInfoCard from './cards/UserInfoCard.vue'
import HealthCard from './cards/HealthCard.vue'

/**
 * ChatMessageItem - 单条消息组件
 *
 * 功能：
 * - 渲染单条消息
 * - 支持AI和用户消息
 * - 支持卡片展示
 * - 图片懒加载
 *
 * @author Claude
 * @date 2026-03-31
 */

const props = defineProps({
	/** 消息对象 */
	message: {
		type: Object,
		required: true
	}
})

const emit = defineEmits(['action'])

// 用户信息
const userStore = useUserStore()
const userInfo = computed(() => ({
	avatar: userStore.userInfo?.avatar || ''
}))

/**
 * 处理卡片操作事件
 */
const handleCardAction = (event) => {
	console.log('🎯 卡片操作事件:', event)
	emit('action', event)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.message-wrapper {
	margin-bottom: $spacing-lg;
	animation: messageFadeIn $duration-base ease-out;
}

@keyframes messageFadeIn {
	from {
		opacity: 0;
		transform: translateY(10rpx) scale(0.98);
	}
	to {
		opacity: 1;
		transform: translateY(0) scale(1);
	}
}

.message {
	display: flex;
	align-items: flex-start;
	gap: $spacing-sm;
	width: 100%;

	// AI消息：靠左
	&:not(.user) {
		justify-content: flex-start;
	}

	// 用户消息：靠右
	&.user {
		flex-direction: row;
		justify-content: flex-end;
	}
}

/* 头像 */
.message-avatar {
	width: $avatar-size;
	max-width: $avatar-max-size;
	min-width: $avatar-min-size;
	height: $avatar-size;
	max-height: $avatar-max-size;
	min-height: $avatar-min-size;
	border-radius: 50%;
	@include flex-center;
	background: $primary-100;
	flex-shrink: 0;
	box-shadow: $box-shadow-sm;

	&.user {
		background: linear-gradient(135deg, $primary-500, $primary-800);
		box-shadow: $box-shadow-sm;
	}
}

.avatar-icon {
	font-size: 38rpx;
}

.avatar-image {
	width: 100%;
	height: 100%;
	border-radius: 50%;
}

/* 消息气泡 */
.message-content {
	/* ✅ 修复：减去左右边距(64rpx)，确保气泡不溢出屏幕 */
	max-width: calc(75vw - 64rpx);
	max-width: $message-bubble-max-width;
	min-width: $message-bubble-min-width;
	min-height: $message-bubble-min-height;
	padding: 16rpx 24rpx;
	background-color: $primary-100;
	border-radius: 24rpx;
	box-shadow: $box-shadow-sm;
	position: relative;
	transition: $transition-base;
	display: flex;
	flex-direction: column;

	&.user {
		background: $primary-500;
		color: $bg-color-white;
		box-shadow: $box-shadow-md;
		border-radius: 24rpx 24rpx 8rpx 24rpx;
	}

	&:not(.user) {
		background: $primary-100;
		color: $text-color-primary;
		border: 1rpx solid $primary-300;
		border-radius: 8rpx 24rpx 24rpx 24rpx;
	}

	&.typing {
		padding: $spacing-md $spacing-lg;
	}
}

.message-body {
	display: flex;
	flex-direction: column;
	gap: 4rpx;
}

.content-text {
	font-size: $font-size-base;
	line-height: 1.8;
	white-space: pre-wrap;
	word-break: break-word;
	font-weight: $font-weight-medium;
}

.message-time {
	display: block;
	font-size: 20rpx;
	color: rgba(0, 0, 0, 0.4);
	margin-top: 4rpx;
	text-align: right;
	opacity: 0.8;
	flex-shrink: 0;

	.user & {
		color: rgba(255, 255, 255, 0.7);
	}
}

/* 打字指示器 */
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

.card-wrapper {
	margin-bottom: $spacing-sm;
}
</style>
