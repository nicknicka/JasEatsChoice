import { createPinia } from 'pinia'

const pinia = createPinia()

// 导出所有 store 模块
export * from './modules/user'
export * from './modules/cart'
export * from './modules/location'
export * from './modules/merchant'

export default pinia
