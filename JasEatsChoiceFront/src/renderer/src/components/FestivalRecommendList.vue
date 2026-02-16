<template>
  <div class="festival-recommend-list">
    <!-- 头部 -->
    <div class="list-header">
      <h2 class="list-title">节日推荐</h2>
      <div class="list-tabs">
        <el-button
          :type="activeTab === 'all' ? 'primary' : 'default'"
          size="small"
          @click="handleTabChange('all')"
        >
          全部
        </el-button>
        <el-button
          :type="activeTab === 'traditional' ? 'primary' : 'default'"
          size="small"
          @click="handleTabChange('TRADITIONAL')"
        >
          传统节日
        </el-button>
        <el-button
          :type="activeTab === 'western' ? 'primary' : 'default'"
          size="small"
          @click="handleTabChange('WESTERN')"
        >
          西方节日
        </el-button>
        <el-button
          :type="activeTab === 'seasonal' ? 'primary' : 'default'"
          size="small"
          @click="handleTabChange('SEASONAL')"
        >
          季节推荐
        </el-button>
      </div>
    </div>

    <!-- 推荐列表 -->
    <div v-loading="loading" class="recommend-content">
      <festival-recommend-card
        v-for="festival in festivals"
        :key="festival.festivalId"
        :festival="festival"
        @dish-click="handleDishClick"
        @add-to-cart="handleAddToCart"
        @feedback="handleSubmitFeedback"
      />

      <!-- 空状态 -->
      <el-empty v-if="!loading && festivals.length === 0" description="暂无节日推荐" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import FestivalRecommendCard from './FestivalRecommendCard.vue'
import festivalApi from '@/api/festival'
import { useCartStore } from '@/store/cartStore'

const loading = ref(false)
const activeTab = ref('all')
const festivals = ref([])

// 加载节日推荐
const loadFestivals = async () => {
  loading.value = true
  try {
    let response
    if (activeTab.value === 'all') {
      response = await festivalApi.getCurrentRecommendations()
    } else {
      // 需要根据节日类型获取推荐，这里暂时使用全部接口
      response = await festivalApi.getCurrentRecommendations()
    }

    if (response.code === 200) {
      festivals.value = response.data || []
    }
  } catch (error) {
    console.error('加载节日推荐失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 切换标签
const handleTabChange = (tab) => {
  activeTab.value = tab
  loadFestivals()
}

// 点击菜品
const handleDishClick = (dish) => {
  console.log('点击菜品:', dish)
  // 跳转到菜品详情页（跳转到商家详情页并显示菜品）
  router.push({
    path: '/user/home/merchant-detail',
    query: {
      id: dish.merchantId,
      dishId: dish.dishId
    }
  })
}

// 加入购物车
const handleAddToCart = async (dish) => {
  try {
    const cartStore = useCartStore()
    // 调用购物车store的添加方法
    await cartStore.addToCart({
      dishId: dish.dishId,
      merchantId: dish.merchantId,
      name: dish.dishName,
      price: dish.price,
      quantity: 1,
      image: dish.image
    })
    ElMessage.success(`已将 ${dish.dishName} 加入购物车`)
  } catch (error) {
    console.error('加入购物车失败:', error)
    ElMessage.error('加入购物车失败')
  }
}

// 提交反馈
const handleSubmitFeedback = async (data) => {
  try {
    await festivalApi.submitFeedback(data.recommendId, data)
  } catch (error) {
    console.error('提交反馈失败:', error)
  }
}

onMounted(() => {
  loadFestivals()
})
</script>

<style scoped>
.festival-recommend-list {
  padding: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-title {
  margin: 0;
  font-size: 1.714rem /* 原值: 24px */;
  font-weight: bold;
  color: #303133;
}

.list-tabs {
  display: flex;
  gap: 8px;
}

.recommend-content {
  min-height: 200px;
}
</style>
