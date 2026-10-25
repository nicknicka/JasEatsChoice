import { onMounted, ref, nextTick } from 'vue'

/**
 * 组件动画 Composable
 * 提供页面组件入场动画和交互动画的工具函数
 */
export function useAnimations() {
  /**
   * 在组件挂载时为元素添加动画类
   * @param {string} selector - CSS选择器
   * @param {string} animationClass - 动画类名
   * @param {number} delay - 延迟时间（毫秒）
   */
  const animateOnMount = (selector, animationClass = 'fade-in-up', delay = 0) => {
    onMounted(() => {
      setTimeout(() => {
        const elements = document.querySelectorAll(selector)
        elements.forEach((el) => {
          el.classList.add(animationClass)
        })
      }, delay)
    })
  }

  /**
   * 为列表项添加交错动画
   * @param {string} selector - CSS选择器
   * @param {number} baseDelay - 基础延迟时间（毫秒）
   */
  const animateListItems = (selector, baseDelay = 0) => {
    onMounted(() => {
      setTimeout(() => {
        const items = document.querySelectorAll(selector)
        items.forEach((item, index) => {
          setTimeout(() => {
            item.classList.add('fade-in-up')
          }, index * 100)
        })
      }, baseDelay)
    })
  }

  /**
   * 使用 Intersection Observer 实现滚动时触发动画
   * @param {string} selector - CSS选择器
   * @param {string} animationClass - 动画类名
   * @param {Object} options - IntersectionObserver 选项
   */
  const animateOnScroll = (
    selector,
    animationClass = 'fade-in-up',
    options = {
      threshold: 0.1,
      rootMargin: '0px 0px -50px 0px'
    }
  ) => {
    onMounted(() => {
      const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add(animationClass)
            observer.unobserve(entry.target)
          }
        })
      }, options)

      const elements = document.querySelectorAll(selector)
      elements.forEach((el) => {
        el.classList.add('animate-hidden')
        observer.observe(el)
      })
    })
  }

  /**
   * 为卡片组添加交错入场动画
   * @param {Ref} cards - 卡片数组的 ref
   * @param {string} animationClass - 动画类名
   */
  const animateCards = (cards, animationClass = 'card-animate') => {
    onMounted(() => {
      nextTick(() => {
        if (cards.value && Array.isArray(cards.value)) {
          cards.value.forEach((_, index) => {
            setTimeout(() => {
              const cardElements = document.querySelectorAll('.card-item')
              if (cardElements[index]) {
                cardElements[index].classList.add(animationClass)
              }
            }, index * 150)
          })
        }
      })
    })
  }

  /**
   * 触发元素的高亮闪烁效果
   * @param {string} selector - CSS选择器
   */
  const flashHighlight = (selector) => {
    const element = document.querySelector(selector)
    if (element) {
      element.classList.add('highlight-flash')
      setTimeout(() => {
        element.classList.remove('highlight-flash')
      }, 1000)
    }
  }

  /**
   * 为数字添加滚动动画效果
   * @param {Ref} value - 数字值的 ref
   * @param {number} duration - 动画持续时间（毫秒）
   */
  const animateNumber = (value, duration = 800) => {
    const displayValue = ref(0)

    onMounted(() => {
      if (typeof value.value === 'number') {
        const start = 0
        const end = value.value
        const increment = end / (duration / 16)
        let current = start

        const timer = setInterval(() => {
          current += increment
          if (current >= end) {
            displayValue.value = end
            clearInterval(timer)
          } else {
            displayValue.value = Math.floor(current)
          }
        }, 16)
      }
    })

    return displayValue
  }

  /**
   * 为多个元素批量添加动画
   * @param {Array} animations - 动画配置数组
   */
  const batchAnimate = (animations) => {
    onMounted(() => {
      animations.forEach(({ selector, animationClass = 'fade-in-up', delay = 0 }) => {
        setTimeout(() => {
          const elements = document.querySelectorAll(selector)
          elements.forEach((el) => {
            el.classList.add(animationClass)
          })
        }, delay)
      })
    })
  }

  /**
   * 创建悬停动画效果
   * @param {string} selector - CSS选择器
   */
  const addHoverEffect = (selector) => {
    onMounted(() => {
      const elements = document.querySelectorAll(selector)
      elements.forEach((el) => {
        el.classList.add('card-hover-effect')
      })
    })
  }

  /**
   * 创建骨架屏加载效果
   * @param {Ref} isLoading - 加载状态的 ref
   * @param {string} selector - 骨架屏元素的 CSS 选择器
   */
  const addSkeletonLoading = (isLoading, selector) => {
    onMounted(() => {
      if (isLoading.value) {
        const elements = document.querySelectorAll(selector)
        elements.forEach((el) => {
          el.classList.add('skeleton-animate')
        })
      }
    })
  }

  return {
    animateOnMount,
    animateListItems,
    animateOnScroll,
    animateCards,
    flashHighlight,
    animateNumber,
    batchAnimate,
    addHoverEffect,
    addSkeletonLoading
  }
}

/**
 * 交错动画生成器
 * 为数组中的每个元素生成延迟时间
 * @param {number} count - 元素数量
 * @param {number} interval - 间隔时间（毫秒）
 * @returns {Array} 延迟时间数组
 */
export function generateStaggerDelays(count, interval = 100) {
  return Array.from({ length: count }, (_, i) => i * interval)
}

/**
 * 随机动画选择器
 * 从一组动画中随机选择一个
 * @param {Array} animations - 动画类名数组
 * @returns {string} 随机选择的动画类名
 */
export function randomAnimation(
  animations = ['fade-in-up', 'slide-in-left', 'slide-in-right', 'scale-in', 'bounce-in']
) {
  return animations[Math.floor(Math.random() * animations.length)]
}
