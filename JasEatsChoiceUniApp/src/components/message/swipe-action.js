/**
 * 滑动操作 Mixin
 * 提供统一的滑动删除/操作功能
 */
export default {
  data() {
    return {
      // 滑动状态
      swipeStates: new Map()
    }
  },

  methods: {
    /**
     * 开始滑动
     */
    onSwipeStart(id, event) {
      this.swipeStates.set(id, {
        startX: event.changedTouches[0].pageX,
        startTime: Date.now()
      })
    },

    /**
     * 滑动中
     */
    onSwipeMove(id, event, maxWidth = 160) {
      const state = this.swipeStates.get(id)
      if (!state) return

      const currentX = event.changedTouches[0].pageX
      const deltaX = currentX - state.startX

      // 只允许向左滑动
      if (deltaX < 0) {
        const translateX = Math.max(deltaX, -maxWidth)
        this.updateItemTranslate(id, translateX)
      }
    },

    /**
     * 结束滑动
     */
    onSwipeEnd(id, event, callback) {
      const state = this.swipeStates.get(id)
      if (!state) return

      const endX = event.changedTouches[0].pageX
      const deltaX = endX - state.startX
      const deltaTime = Date.now() - state.startTime

      // 判断是否触发操作（滑动超过80px 或 快速滑动）
      const shouldTrigger = deltaX < -80 || (deltaX < -40 && deltaTime < 300)

      if (shouldTrigger && callback) {
        callback(id)
      } else {
        // 回弹
        this.updateItemTranslate(id, 0)
      }

      this.swipeStates.delete(id)
    },

    /**
     * 更新项的位移（子组件实现）
     */
    updateItemTranslate(id, translateX) {
      // 子组件需要实现此方法
      console.warn('updateItemTranslate method not implemented')
    },

    /**
     * 关闭所有滑动
     */
    closeAllSwipes() {
      this.swipeStates.clear()
    }
  }
}
