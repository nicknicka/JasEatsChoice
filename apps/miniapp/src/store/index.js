import { createPinia } from 'pinia'
import { useUserStore } from './modules/user'
import { useCartStore } from './modules/cart'
import { useLocationStore } from './modules/location'
import { useMerchantStore } from './modules/merchant'

const pinia = createPinia()

// 显式导出所有 store 模块。
// 小程序编译链下 `export *` 偶发会让聚合出口的命名导出失效，
// 这里改为显式转发，避免运行时拿到的不是 store 工厂函数。
export {
  useUserStore,
  useCartStore,
  useLocationStore,
  useMerchantStore
}

export default pinia
