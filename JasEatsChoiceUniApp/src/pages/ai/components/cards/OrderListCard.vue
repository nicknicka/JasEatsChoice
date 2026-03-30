<template>
	<view class="order-list-card">
		<!-- 卡片头部 -->
		<view class="card-header">
			<view class="header-title">
				<text class="icon">📋</text>
				<text class="title">订单列表</text>
			</view>
			<text class="header-summary" v-if="data.summary">{{ data.summary }}</text>
		</view>

		<!-- 卡片内容 -->
		<view class="card-content">
			<!-- 空状态 -->
			<view v-if="!orders || orders.length === 0" class="empty-state">
				<text class="empty-icon">📦</text>
				<text class="empty-text">暂无订单</text>
			</view>

			<!-- 订单列表 -->
			<view v-else class="order-list">
				<view
					v-for="(order, index) in orders"
					:key="order.orderId || order.orderNo || index"
					class="order-item"
					@tap="handleOrderTap(order)"
				>
					<!-- 订单头部 -->
					<view class="order-header">
						<view class="order-info">
							<text class="order-no">订单号: {{ order.orderNo || '---' }}</text>
							<view class="order-status" :class="'status-' + order.status">
								<text class="status-text">{{ getStatusText(order.status) }}</text>
							</view>
						</view>
						<text class="order-time">{{ formatTime(order.createTime) }}</text>
					</view>

					<!-- 订单内容（商家信息） -->
					<view class="order-content" v-if="order.merchant">
						<text class="merchant-name">{{ order.merchant.name || '商家' }}</text>
						<text class="dish-count">共{{ order.dishCount || 0 }}件商品</text>
					</view>

					<!-- 订单金额 -->
					<view class="order-footer">
						<text class="total-amount">¥{{ order.totalAmount || '0.00' }}</text>
						<view class="order-actions">
							<view
								class="action-btn primary"
								@tap.stop="handleViewDetail(order)"
							>
								<text class="action-text">查看详情</text>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
	data: {
		type: Object,
		default: () => ({})
	}
});

const emit = defineEmits(['action']);

// 订单列表
const orders = computed(() => {
	return props.data.orders || [];
});

// 获取订单状态文本
const getStatusText = (status) => {
	const statusMap = {
		pending: '待确认',
		confirmed: '已确认',
		preparing: '制作中',
		delivering: '配送中',
		completed: '已完成',
		cancelled: '已取消',
		refunded: '已退款'
	};
	return statusMap[status] || status;
};

// 格式化时间
const formatTime = (time) => {
	if (!time) return '';
	const date = new Date(time);
	const now = new Date();
	const diff = now - date;

	if (diff < 60000) {
		return '刚刚';
	} else if (diff < 3600000) {
		return Math.floor(diff / 60000) + '分钟前';
	} else if (diff < 86400000) {
		return Math.floor(diff / 3600000) + '小时前';
	} else {
		return date.toLocaleDateString();
	}
};

// 点击订单
const handleOrderTap = (order) => {
	console.log('点击订单:', order);
	handleViewDetail(order);
};

// 查看订单详情
const handleViewDetail = (order) => {
	console.log('查看订单详情:', order);
	emit('action', {
		type: 'view_detail',
		order: order
	});

	// 跳转到订单详情页
	uni.navigateTo({
		url: `/pages-user/order/progress?orderId=${order.orderId}`
	});
};
</script>

<style lang="scss" scoped>
@import "@/styles/variables.scss";

.order-list-card {
	background: $bg-color-white;
	border-radius: $border-radius-lg;
	overflow: hidden;
	box-shadow: $box-shadow-sm;
	margin: $spacing-md 0;
}

.card-header {
	padding: $spacing-lg;
	border-bottom: 1rpx solid $border-color-light;
	background: linear-gradient(135deg, $primary-50, $bg-color-white);
}

.header-title {
	display: flex;
	align-items: center;
	gap: $spacing-sm;
	margin-bottom: $spacing-xs;
}

.icon {
	font-size: 40rpx;
}

.title {
	font-size: $font-size-lg;
	font-weight: $font-weight-bold;
	color: $text-color-primary;
}

.header-summary {
	font-size: $font-size-sm;
	color: $text-color-secondary;
	margin-left: 52rpx;
}

.card-content {
	padding: $spacing-md;
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 80rpx $spacing-lg;
}

.empty-icon {
	font-size: 120rpx;
	margin-bottom: $spacing-lg;
	opacity: 0.5;
}

.empty-text {
	font-size: $font-size-base;
	color: $text-color-secondary;
}

.order-list {
	display: flex;
	flex-direction: column;
	gap: $spacing-md;
}

.order-item {
	padding: $spacing-md;
	background: $bg-color-base;
	border-radius: $border-radius-base;
	transition: $transition-base;

	&:active {
		background: $primary-50;
		transform: scale(0.98);
	}
}

.order-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: $spacing-sm;
}

.order-info {
	display: flex;
	align-items: center;
	gap: $spacing-sm;
	flex: 1;
	min-width: 0;
}

.order-no {
	font-size: $font-size-sm;
	color: $text-color-primary;
	font-weight: $font-weight-medium;
}

.order-status {
	flex-shrink: 0;
	padding: 4rpx $spacing-sm;
	border-radius: $border-radius-sm;

	&.status-pending,
	&.status-confirmed {
		background: $warning-100;
	}

	&.status-preparing,
	&.status-delivering {
		background: $primary-100;
	}

	&.status-completed {
		background: $success-100;
	}

	&.status-cancelled,
	&.status-refunded {
		background: $bg-color-light;
	}
}

.status-text {
	font-size: $font-size-xs;
	color: $text-color-secondary;
}

.order-time {
	font-size: $font-size-xs;
	color: $text-color-placeholder;
	flex-shrink: 0;
}

.order-content {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: $spacing-sm 0;
	margin-bottom: $spacing-sm;
	border-top: 1rpx solid $border-color-light;
	border-bottom: 1rpx solid $border-color-light;
}

.merchant-name {
	font-size: $font-size-base;
	color: $text-color-primary;
	font-weight: $font-weight-medium;
}

.dish-count {
	font-size: $font-size-sm;
	color: $text-color-secondary;
}

.order-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.total-amount {
	font-size: $font-size-xl;
	font-weight: $font-weight-bold;
	color: $danger-color;
}

.order-actions {
	display: flex;
	gap: $spacing-sm;
}

.action-btn {
	padding: $spacing-xs $spacing-md;
	border-radius: $border-radius-base;
	transition: $transition-base;

	&.primary {
		background: $primary-500;
	}

	&:active {
		opacity: 0.7;
		transform: scale(0.95);
	}
}

.action-text {
	font-size: $font-size-sm;
	color: $bg-color-white;
	font-weight: $font-weight-medium;
}
</style>
