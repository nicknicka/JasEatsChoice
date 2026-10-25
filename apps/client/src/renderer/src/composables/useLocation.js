import { ref, watch } from 'vue'
import { useCascaderLocationData } from './useCascaderLocationData'

export function useLocation() {
  const { cascaderData: cascaderLocationData, loadLocationData } = useCascaderLocationData()

  // Location dialog
  // 位置选择对话框
  const locationDialogVisible = ref(false)

  // Manual location selection
  // 手动选择的位置
  const manualLocation = ref([])

  // Fetch location data from API when dialog opens
  // 当对话框打开时从API获取地址数据
  const fetchLocationData = () => loadLocationData()

  // Watch dialog visibility and fetch location data when dialog opens
  // 监听对话框可见性并在对话框打开时获取位置数据
  watch(
    () => locationDialogVisible.value,
    (newValue) => {
      if (newValue) {
        fetchLocationData()
      }
    }
  )

  // Handle manual location selection from the cascader
  // 处理从级联选择器中手动选择位置
  const handleManualLocationSelect = (value) => {
    manualLocation.value = value
  }

  return {
    cascaderLocationData,
    locationDialogVisible,
    manualLocation,
    fetchLocationData,
    handleManualLocationSelect
  }
}
