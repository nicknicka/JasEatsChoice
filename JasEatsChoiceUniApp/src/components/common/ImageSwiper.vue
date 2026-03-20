<!--
组件名称：ImageSwiper
用途：图片轮播组件（支持预览）
复用情况：菜品详情、商家详情等
创建时间：2026-03-20
-->
<template>
  <view class="image-swiper-wrapper">
    <swiper
      class="image-swiper"
      :indicator-dots="images.length > 1"
      indicator-color="rgba(255,255,255,0.5)"
      indicator-active-color="#fff"
    >
      <swiper-item v-for="(image, index) in images" :key="index">
        <image class="swiper-image" :src="image" mode="aspectFill" @tap="previewImage(index)" />
      </swiper-item>
    </swiper>
  </view>
</template>

<script setup>
const props = defineProps({
  images: {
    type: Array,
    default: () => []
  }
})

const previewImage = (index) => {
  uni.previewImage({
    urls: props.images,
    current: index
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.image-swiper-wrapper {
  width: 100%;
  background: #fff;
}

.image-swiper {
  width: 100%;
  height: 750rpx;
}

.swiper-image {
  width: 100%;
  height: 100%;
}
</style>
