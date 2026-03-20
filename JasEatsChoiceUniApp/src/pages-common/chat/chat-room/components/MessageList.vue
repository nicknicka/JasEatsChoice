<!--
组件名称：MessageList
用途：聊天室消息列表容器
页面专用：聊天室页面使用
创建时间：2026-03-20
-->
<template>
  <scroll-view
    class="message-list"
    scroll-y
    :scroll-into-view="scrollIntoView"
    @scrolltoupper="onLoadMore"
  >
    <MessageBubble
      v-for="message in messages"
      :key="message.id"
      :message="message"
      :show-nickname="false"
      :id="'message-' + message.id"
      @image-tap="onImageTap"
      @dish-tap="onDishTap"
      @order-tap="onOrderTap"
      @longpress="onLongPress"
    />

    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore">
      <text v-if="loading">加载中...</text>
      <text v-else>下拉加载历史消息</text>
    </view>
  </scroll-view>
</template>

<script setup>
import MessageBubble from '@/components/business/chat/MessageBubble.vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  scrollIntoView: {
    type: String,
    default: ''
  },
  hasMore: {
    type: Boolean,
    default: true
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'load-more',
  'image-tap',
  'dish-tap',
  'order-tap',
  'longpress'
])

const onLoadMore = () => {
  emit('load-more')
}

const onImageTap = (url) => {
  emit('image-tap', url)
}

const onDishTap = (dishId) => {
  emit('dish-tap', dishId)
}

const onOrderTap = (orderId) => {
  emit('order-tap', orderId)
}

const onLongPress = (message) => {
  emit('longpress', message)
}
</script>

<style lang="scss" scoped>
.message-list {
  flex: 1;
  padding: 20rpx;
}

.load-more {
  text-align: center;
  padding: 20rpx;
  font-size: 24rpx;
  color: #999;
}
</style>
