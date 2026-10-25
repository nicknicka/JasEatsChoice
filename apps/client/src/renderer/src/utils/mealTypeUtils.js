// 餐次类型转换工具

// 餐次类型选项配置
export const mealTypeOptions = [
  { value: 'breakfast', label: '早餐', icon: '🥣' },
  { value: 'lunch', label: '午餐', icon: '🍚' },
  { value: 'dinner', label: '晚餐', icon: '🍱' },
  { value: 'snack', label: '加餐', icon: '🍪' }
]

// 将英文餐次类型转换为中文
export const mealTypeToChinese = (mealType) => {
  const option = mealTypeOptions.find((opt) => opt.value === mealType)
  return option ? option.label : mealType
}

// 将中文餐次类型转换为英文
export const mealTypeToEnglish = (mealTypeName) => {
  const option = mealTypeOptions.find((opt) => opt.label === mealTypeName)
  return option ? option.value : mealTypeName.toLowerCase()
}

// 获取餐次类型对应的图标
export const getMealTypeIcon = (mealType) => {
  const option = mealTypeOptions.find((opt) => opt.value === mealType)
  return option ? option.icon : '🍴'
}
