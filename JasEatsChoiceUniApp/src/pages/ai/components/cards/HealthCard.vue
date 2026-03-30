<template>
	<view class="health-card">
		<view class="health-header">
			<text class="health-title">{{ data.title }}</text>
			<text v-if="data.subtitle" class="health-subtitle">{{ data.subtitle }}</text>
		</view>

		<view class="health-stats" v-if="data.stats && data.stats.length > 0">
			<view
				class="stat-item"
				v-for="(stat, index) in data.stats"
				:key="index"
			>
				<text class="stat-label">{{ stat.label }}</text>
				<text class="stat-value">{{ stat.value }}</text>
			</view>
		</view>

		<view class="health-suggestion" v-if="data.suggestion">
			<text class="suggestion-text">{{ data.suggestion }}</text>
		</view>
	</view>
</template>

<script setup>
defineProps({
	data: {
		type: Object,
		default: () => ({})
	}
})
</script>

<style lang="scss" scoped>
@import "@/styles/variables.scss";

.health-card {
	background: linear-gradient(135deg, $primary-50, #fff);
	border-radius: $border-radius-lg;
	padding: $spacing-lg;
	border: 1rpx solid $primary-200;
	box-shadow: $box-shadow-sm;
}

.health-header {
	margin-bottom: $spacing-md;
}

.health-title {
	font-size: $font-size-xl;
	font-weight: $font-weight-bold;
	color: $primary-500;
	display: block;
	margin-bottom: $spacing-xs;
}

.health-subtitle {
	font-size: $font-size-base;
	color: $text-color-secondary;
	display: block;
}

.health-stats {
	background: $bg-color-white;
	border-radius: $border-radius-base;
	padding: $spacing-md;
	margin-bottom: $spacing-md;
}

.stat-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: $spacing-sm 0;
	border-bottom: 1rpx solid $border-color-light;

	&:last-child {
		border-bottom: none;
	}
}

.stat-label {
	font-size: $font-size-base;
	color: $text-color-secondary;
	font-weight: $font-weight-medium;
}

.stat-value {
	font-size: $font-size-base;
	color: $text-color-primary;
	font-weight: $font-weight-bold;
	text-align: right;
	flex: 1;
	margin-left: $spacing-lg;
}

.health-suggestion {
	background: rgba(255, 107, 53, 0.05);
	border-radius: $border-radius-base;
	padding: $spacing-md;
	border-left: 4rpx solid $primary-500;
}

.suggestion-text {
	font-size: $font-size-base;
	color: $text-color-primary;
	line-height: $line-height-lg;
}
</style>
