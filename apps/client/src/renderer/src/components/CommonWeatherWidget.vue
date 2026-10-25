<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../config/index.js'

// 对外暴露的属性和事件
const props = defineProps({
  // 城市名称
  city: {
    type: String,
    default: '北京'
  },
  // 用户ID（用于获取用户偏好）
  userId: {
    type: Number,
    default: 1
  }
})

const emit = defineEmits(['weather-updated', 'weather-error'])

// 天气相关状态
const isLoading = ref(false)
const weatherData = ref(null)
const weatherRecommendationEnabled = ref(true)

// 从 localStorage 获取用户天气推荐设置
const loadWeatherSettings = () => {
  const savedSettings = localStorage.getItem('userSettings')
  if (savedSettings) {
    const parsedSettings = JSON.parse(savedSettings)
    weatherRecommendationEnabled.value = parsedSettings.privacy?.weatherRecommendation !== false
  }
}

// 根据天气和时间生成推荐标签
const generateWeatherTags = () => {
  const tags = []

  if (!weatherData.value) return tags

  const { temperature, humidity } = weatherData.value

  // 天气维度推荐
  if (temperature > 30) {
    tags.push('冰饮', '凉菜', '轻食')
  } else if (temperature < 10) {
    tags.push('热饮', '热菜', '火锅')
  }

  if (humidity > 80) {
    tags.push('祛湿粥品', '清淡饮食')
  }

  return tags
}

// 获取当前天气数据
const getCurrentWeather = async (targetCity = props.city) => {
  if (!weatherRecommendationEnabled.value) return

  try {
    isLoading.value = true

    const response = await axios.get(API_CONFIG.baseURL + API_CONFIG.weather.current, {
      params: {
        city: targetCity || '北京'
      }
    })

    if (response.data && response.data.data) {
      weatherData.value = response.data.data

      // 生成天气标签
      const weatherTags = generateWeatherTags()

      // 对外发射天气数据和推荐标签
      emit('weather-updated', {
        weatherData: weatherData.value,
        weatherTags: weatherTags
      })
    }
  } catch (error) {
    console.error('获取天气数据失败:', error)
    ElMessage.error('天气推荐功能暂时不可用')
    emit('weather-error', error)
  } finally {
    isLoading.value = false
  }
}

// 组件挂载时加载天气设置并获取天气数据
onMounted(() => {
  loadWeatherSettings()
  getCurrentWeather()
})

// 监听城市变化，重新获取天气数据
watch(
  () => props.city,
  (newCity) => {
    if (newCity) {
      getCurrentWeather(newCity)
    }
  }
)

// 计算属性：天气推荐标签
const weatherTags = computed(() => generateWeatherTags())

// 暴露方法给父组件
defineExpose({
  getCurrentWeather,
  generateWeatherTags,
  weatherData,
  weatherRecommendationEnabled,
  loadWeatherSettings,
  weatherTags
})
</script>
