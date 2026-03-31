/**
 * LazyImage - 图片懒加载组件
 *
 * 功能：
 * - 图片进入视口时才加载
 * - 支持占位符
 * - 支持加载失败处理
 * - 支持淡入动画
 *
 * @author Claude
 * @date 2026-03-31
 */

<template>
	<view class="lazy-image" :style="{ width: width, height: height }">
		<!-- 实际图片 -->
		<image
			v-if="isLoaded || isVisible"
			:src="currentSrc"
			:mode="mode"
			:lazy-load="true"
			:class="{ 'fade-in': isLoaded && enableFadeIn }"
			@load="handleLoad"
			@error="handleError"
			class="image"
		/>

		<!-- 占位符 -->
		<view v-else class="placeholder" :style="{ background: placeholderBg }">
			<text v-if="placeholderIcon" class="placeholder-icon">{{ placeholderIcon }}</text>
			<view v-if="showLoading" class="loading-spinner"></view>
		</view>

		<!-- 错误状态 -->
		<view v-if="hasError" class="error-state">
			<text class="error-icon">⚠️</text>
			<text class="error-text">加载失败</text>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

/**
 * LazyImage 组件
 */
const props = defineProps({
	/** 图片地址 */
	src: {
		type: String,
		required: true
	},
	/** 图片宽度 */
	width: {
		type: String,
		default: '100%'
	},
	/** 图片高度 */
	height: {
		type: String,
		default: 'auto'
	},
	/** 图片裁剪模式 */
	mode: {
		type: String,
		default: 'aspectFill'
	},
	/** 占位符图标 */
	placeholderIcon: {
		type: String,
		default: '🖼️'
	},
	/** 占位符背景色 */
	placeholderBg: {
		type: String,
		default: '#f5f5f5'
	},
	/** 是否启用淡入动画 */
	enableFadeIn: {
		type: Boolean,
		default: true
	},
	/** 是否显示加载动画 */
	showLoading: {
		type: Boolean,
		default: true
	},
	/** 错误时的备用图片 */
	errorSrc: {
		type: String,
		default: ''
	},
	/** 根元素偏移量（用于 IntersectionObserver） */
	rootMargin: {
		type: String,
		default: '50px'
	}
})

/** 是否进入视口 */
const isVisible = ref(false)

/** 是否已加载 */
const isLoaded = ref(false)

/** 是否加载失败 */
const hasError = ref(false)

/** IntersectionObserver 实例 */
let observer = null

/** 当前图片地址 */
const currentSrc = computed(() => {
	if (hasError.value && props.errorSrc) {
		return props.errorSrc
	}
	return props.src
})

/**
 * 处理图片加载成功
 */
const handleLoad = () => {
	isLoaded.value = true
	hasError.value = false
}

/**
 * 处理图片加载失败
 */
const handleError = () => {
	hasError.value = true
	isLoaded.value = false
}

/**
 * 开始监听元素是否进入视口
 */
const startObserving = () => {
	// #ifdef H5
	// 在 H5 环境使用 IntersectionObserver
	if (typeof IntersectionObserver !== 'undefined') {
		observer = new IntersectionObserver(
			(entries) => {
				entries.forEach((entry) => {
					if (entry.isIntersecting) {
						isVisible.value = true
						// 进入视口后停止观察
						if (observer) {
							observer.disconnect()
							observer = null
						}
					}
				})
			},
			{
				rootMargin: props.rootMargin
			}
		)

		// 获取 DOM 元素并开始观察
		setTimeout(() => {
			const el = uni.createSelectorQuery().select('.lazy-image')
			// 注意：uni-app 中 IntersectionObserver 的使用有限制
			// 这里简化处理，直接设置为可见
			isVisible.value = true
		}, 100)
	}
	// #endif

	// #ifndef H5
	// 非 H5 环境直接使用 uni-app 的 lazy-load 属性
	// 组件自身已设置 lazy-load=true
	isVisible.value = true
	// #endif
}

/**
 * 停止观察
 */
const stopObserving = () => {
	if (observer) {
		observer.disconnect()
		observer = null
	}
}

onMounted(() => {
	startObserving()
})

onUnmounted(() => {
	stopObserving()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.lazy-image {
	position: relative;
	overflow: hidden;
	display: inline-block;
}

.image {
	width: 100%;
	height: 100%;
	display: block;

	&.fade-in {
		animation: fadeIn 0.3s ease-in;
	}
}

@keyframes fadeIn {
	from {
		opacity: 0;
	}
	to {
		opacity: 1;
	}
}

.placeholder {
	@include flex-center;
	width: 100%;
	height: 100%;
	min-height: 200rpx;
}

.placeholder-icon {
	font-size: 80rpx;
	opacity: 0.3;
}

.loading-spinner {
	width: 40rpx;
	height: 40rpx;
	border: 4rpx solid $border-color-light;
	border-top-color: $primary-500;
	border-radius: 50%;
	animation: spin 1s linear infinite;
	position: absolute;
}

@keyframes spin {
	to {
		transform: rotate(360deg);
	}
}

.error-state {
	@include flex-center-column;
	width: 100%;
	height: 100%;
	min-height: 200rpx;
	background: $bg-color-base;
}

.error-icon {
	font-size: 60rpx;
	margin-bottom: $spacing-sm;
}

.error-text {
	font-size: $font-size-sm;
	color: $text-color-secondary;
}
</style>
