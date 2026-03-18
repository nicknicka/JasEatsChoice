<template>
  <view class="performance-demo">
    <view class="demo-header">
      <text class="title">性能优化示例</text>
    </view>

    <!-- 性能指标监控 -->
    <view class="metrics-card">
      <view class="metric-item">
        <text class="metric-label">FPS</text>
        <text class="metric-value">{{ metrics.fps }}</text>
      </view>
      <view class="metric-item">
        <text class="metric-label">内存</text>
        <text class="metric-value">{{ metrics.memory.toFixed(2) }}MB</text>
      </view>
      <view class="metric-item">
        <text class="metric-label">加载时间</text>
        <text class="metric-value">{{ metrics.loadTime }}ms</text>
      </view>
    </view>

    <!-- 图片懒加载示例 -->
    <view class="section">
      <view class="section-title">图片懒加载</view>
      <scroll-view
        class="image-list"
        scroll-y
        @scroll="onImageScroll"
      >
        <view
          class="image-item"
          v-for="(image, index) in imageList"
          :key="index"
          :id="`image-${index}`"
        >
          <image
            class="lazy-image"
            :src="getLazyImageUrl(image.url)"
            mode="aspectFill"
            @load="onImageLoad(image.url)"
          ></image>
          <text class="image-title">{{ image.title }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 虚拟列表示例 -->
    <view class="section">
      <view class="section-title">虚拟列表 ({{ list.length }}条数据)</view>
      <scroll-view
        class="virtual-list-container"
        scroll-y
        :scroll-top="scrollTop"
        @scroll="onListScroll"
        :style="{ height: containerHeight + 'px' }"
      >
        <view
          class="virtual-list-content"
          :style="{ height: totalHeight + 'px', transform: `translateY(${offsetY}px)` }"
        >
          <view
            class="list-item"
            v-for="(item, index) in visibleList"
            :key="item.id"
            :style="{ height: itemHeight + 'px' }"
          >
            <text class="item-text">{{ item.text }}</text>
            <text class="item-index">#{{ range.start + index }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 无限滚动示例 -->
    <view class="section">
      <view class="section-title">无限滚动</view>
      <scroll-view
        class="infinite-list"
        scroll-y
        @scrolltolower="onLoadMore"
        lower-threshold="100"
      >
        <view
          class="infinite-item"
          v-for="item in infiniteList"
          :key="item.id"
        >
          <text class="item-text">{{ item.text }}</text>
        </view>

        <!-- 加载状态 -->
        <view class="load-more" v-if="!infiniteFinished">
          <uni-load-more
            v-if="infiniteLoading"
            status="loading"
            :content-text="{ contentdown: '上拉加载更多', contentrefresh: '加载中...', contentnomore: '没有更多了' }"
          ></uni-load-more>
        </view>

        <view class="no-more" v-if="infiniteFinished">
          <text class="no-more-text">没有更多了</text>
        </view>
      </scroll-view>
    </view>

    <!-- 防抖节流示例 -->
    <view class="section">
      <view class="section-title">防抖节流</view>
      <view class="input-demo">
        <input
          class="demo-input"
          type="text"
          placeholder="输入搜索内容（防抖）"
          @input="onSearchInput"
        />
        <text class="input-result">搜索次数: {{ searchCount }}</text>
      </view>

      <view class="button-demo">
        <button class="demo-btn" @click="onThrottleClick">节流点击 (点击次数: {{ clickCount }})</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  useLazyLoad,
  useVirtualList,
  useInfiniteScroll,
  usePullRefresh,
  useDebounce,
  useThrottle,
  usePerformanceMonitor
} from '@/utils/performance'

// 性能监控
const { metrics, recordLoadTime } = usePerformanceMonitor()

// 记录页面加载时间
onMounted(() => {
  recordLoadTime(Date.now())
})

// ========== 图片懒加载 ==========
const imageList = ref([])
const { loadedImages, getImageUrl, checkInView } = useLazyLoad()

// 生成测试图片数据
for (let i = 0; i < 20; i++) {
  imageList.value.push({
    url: `https://via.placeholder.com/400x300?text=Image+${i + 1}`,
    title: `图片 ${i + 1}`
  })
}

const getLazyImageUrl = (url) => {
  return getImageUrl(url)
}

const onImageLoad = (url) => {
  loadedImages.value.add(url)
}

const onImageScroll = useDebounce((e) => {
  // 检查所有图片是否需要加载
  imageList.value.forEach((image, index) => {
    const element = uni.createSelectorQuery().select(`#image-${index}`)
    if (element && checkInView(element)) {
      onImageLoad(image.url)
    }
  })
}, 100)

// ========== 虚拟列表 ==========
const list = ref([])
const itemHeight = 60
const visibleCount = 15

// 生成测试列表数据
for (let i = 0; i < 1000; i++) {
  list.value.push({
    id: i,
    text: `列表项 ${i + 1}`
  })
}

const {
  scrollTop,
  containerHeight,
  range,
  visibleList,
  totalHeight,
  offsetY,
  onScroll: onListScroll
} = useVirtualList(list, itemHeight, visibleCount)

// 设置容器高度
onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  containerHeight.value = systemInfo.windowHeight * 0.4
})

// ========== 无限滚动 ==========
const infiniteList = ref([])
let page = 1

const { loading: infiniteLoading, finished: infiniteFinished, loadMore, reset: resetInfinite } = useInfiniteScroll(async () => {
  // 模拟加载数据
  await new Promise(resolve => setTimeout(resolve, 1000))

  const newData = []
  for (let i = 0; i < 20; i++) {
    newData.push({
      id: infiniteList.value.length + i,
      text: `无限滚动项 ${infiniteList.value.length + i + 1}`
    })
  }

  infiniteList.value.push(...newData)
  page++

  // 模拟没有更多数据
  return page < 5
})

const onLoadMore = () => {
  loadMore()
}

// ========== 防抖节流 ==========
const searchCount = ref(0)
const clickCount = ref(0)

const onSearchInput = useDebounce((e) => {
  searchCount.value++
  console.log('搜索:', e.detail.value)
}, 500)

const onThrottleClick = useThrottle(() => {
  clickCount.value++
  uni.showToast({
    title: `点击了 ${clickCount.value} 次`,
    icon: 'none'
  })
}, 1000)
</script>

<style lang="scss" scoped>
.performance-demo {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);

  .demo-header {
    padding: 32rpx;
    background: #ffffff;
    margin-bottom: 24rpx;

    .title {
      font-size: 36rpx;
      font-weight: bold;
      color: #333333;
    }
  }

  .metrics-card {
    display: flex;
    justify-content: space-around;
    background: #ffffff;
    padding: 32rpx;
    margin: 0 32rpx 24rpx;
    border-radius: 16rpx;

    .metric-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16rpx;

      .metric-label {
        font-size: 26rpx;
        color: #999999;
      }

      .metric-value {
        font-size: 32rpx;
        font-weight: bold;
        color: #ff6b6b;
      }
    }
  }

  .section {
    background: #ffffff;
    margin: 0 32rpx 24rpx;
    border-radius: 16rpx;
    overflow: hidden;

    .section-title {
      padding: 24rpx 32rpx;
      font-size: 28rpx;
      font-weight: bold;
      color: #333333;
      border-bottom: 1rpx solid #f0f0f0;
    }

    .image-list {
      height: 400rpx;
      padding: 24rpx 32rpx;

      .image-item {
        margin-bottom: 24rpx;
        border-radius: 12rpx;
        overflow: hidden;
        background: #f5f5f5;

        .lazy-image {
          width: 100%;
          height: 300rpx;
        }

        .image-title {
          display: block;
          padding: 16rpx;
          font-size: 26rpx;
          color: #333333;
          text-align: center;
        }
      }
    }

    .virtual-list-container {
      padding: 24rpx 32rpx;

      .virtual-list-content {
        .list-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 0 24rpx;
          background: #f5f5f5;
          border-radius: 8rpx;
          margin-bottom: 16rpx;

          .item-text {
            font-size: 28rpx;
            color: #333333;
          }

          .item-index {
            font-size: 24rpx;
            color: #999999;
          }
        }
      }
    }

    .infinite-list {
      height: 600rpx;
      padding: 24rpx 32rpx;

      .infinite-item {
        padding: 24rpx;
        background: #f5f5f5;
        border-radius: 8rpx;
        margin-bottom: 16rpx;

        .item-text {
          font-size: 28rpx;
          color: #333333;
        }
      }

      .load-more {
        padding: 24rpx;
        text-align: center;
      }

      .no-more {
        padding: 24rpx;
        text-align: center;

        .no-more-text {
          font-size: 24rpx;
          color: #999999;
        }
      }
    }

    .input-demo {
      padding: 32rpx;

      .demo-input {
        width: 100%;
        height: 72rpx;
        padding: 0 24rpx;
        background: #f5f5f5;
        border-radius: 8rpx;
        font-size: 28rpx;
        margin-bottom: 24rpx;
      }

      .input-result {
        font-size: 26rpx;
        color: #666666;
      }
    }

    .button-demo {
      padding: 0 32rpx 32rpx;

      .demo-btn {
        width: 100%;
        height: 72rpx;
        background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
        color: #ffffff;
        border: none;
        border-radius: 8rpx;
        font-size: 28rpx;
      }
    }
  }
}
</style>
