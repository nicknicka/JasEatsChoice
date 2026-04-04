/**
 * 商家端AI聊天 Composable
 */
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../../../store/userStore'
import { MERCHANT_WELCOME_MESSAGE, MERCHANT_ERROR_MESSAGES, merchantLogger } from '../../../../config/merchantChatConfig'

// 消息存储（简单的内存存储，后续可改为持久化）
const messages = ref([])
const isLoading = ref(false)
const isStreaming = ref(false)

export function useMerchantAIChat() {
  const userStore = useUserStore()
  const chatContainerRef = ref(null)

  /**
   * 加载聊天历史
   */
  const loadMessages = async () => {
    // 如果没有消息，添加欢迎消息
    if (messages.value.length === 0) {
      messages.value = [
        {
          id: Date.now(),
          sender: 'ai',
          content: MERCHANT_WELCOME_MESSAGE,
          time: formatTime(new Date()),
          avatar: '🤖'
        }
      ]
    }
    merchantLogger.log('加载聊天历史', messages.value.length, '条消息')
  }

  /**
   * 发送消息
   */
  const sendMessage = async (content) => {
    if (!content || !content.trim()) {
      ElMessage.warning(MERCHANT_ERROR_MESSAGES.INPUT_EMPTY)
      return
    }

    // 添加用户消息
    const userMessage = {
      id: Date.now(),
      sender: 'user',
      content: content.trim(),
      time: formatTime(new Date()),
      avatar: '👤'
    }
    messages.value.push(userMessage)

    // 设置加载状态
    isLoading.value = true

    try {
      // 调用AI接口
      const response = await callMerchantAI(content.trim())

      // 添加AI回复
      const aiMessage = {
        id: Date.now() + 1,
        sender: 'ai',
        content: response,
        time: formatTime(new Date()),
        avatar: '🤖'
      }
      messages.value.push(aiMessage)

      // 滚动到底部
      await scrollToBottom()

    } catch (error) {
      merchantLogger.error('发送消息失败:', error)
      ElMessage.error(MERCHANT_ERROR_MESSAGES.SERVER_ERROR)

      // 添加错误提示消息
      messages.value.push({
        id: Date.now() + 1,
        sender: 'ai',
        content: `抱歉，${MERCHANT_ERROR_MESSAGES.SERVER_ERROR}`,
        time: formatTime(new Date()),
        avatar: '🤖',
        isError: true
      })
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 调用商家AI接口
   */
  const callMerchantAI = async (question) => {
    const merchantId = userStore.merchantId || localStorage.getItem('merchantId')

    // TODO: 替换为实际的API调用
    // 目前使用模拟响应
    return new Promise((resolve) => {
      setTimeout(() => {
        const responses = {
          '今日销售情况分析': generateSalesAnalysis(),
          '本周热销菜品有哪些？': generateTopDishes(),
          '帮我分析最近的差评原因': generateReviewAnalysis(),
          '明天应该备多少货？': generateStockSuggestion(),
          '如何提高客单价？': generateRevenueTips(),
          '最近有哪些菜品销量下滑？': generateDeclineAnalysis()
        }

        const response = responses[question] || generateDefaultResponse(question)
        resolve(response)
      }, 1500)
    })
  }

  /**
   * 生成销售分析
   */
  const generateSalesAnalysis = () => {
    return `📊 **今日销售分析**

**订单概览**
- 今日订单数：128 单
- 营业额：¥3,680
- 平均客单价：¥28.75

**销售趋势**
- 上午时段（6:00-11:00）：占比 35%
- 午餐时段（11:00-14:00）：占比 42%
- 下午时段（14:00-18:00）：占比 15%
- 晚餐时段（18:00-22:00）：占比 8%

**建议**
1. 晚餐时段订单较少，可考虑推出晚餐专属优惠
2. 热销菜品「红烧肉」库存充足，可继续推广
3. 建议增加下午茶套餐，提升下午时段销量`
  }

  /**
   * 生成热销菜品分析
   */
  const generateTopDishes = () => {
    return `🏆 **本周热销菜品排行**

| 排名 | 菜品名称 | 销量 | 环比变化 |
|------|----------|------|----------|
| 1 | 红烧肉 | 156份 | ↑ 12% |
| 2 | 宫保鸡丁 | 142份 | ↑ 8% |
| 3 | 鱼香肉丝 | 128份 | ↑ 5% |
| 4 | 麻婆豆腐 | 98份 | ↓ 3% |
| 5 | 糖醋排骨 | 86份 | ↑ 15% |

**分析建议**
- 「糖醋排骨」增长最快，可考虑作为主推菜品
- 「麻婆豆腐」销量下滑，建议检查口味或价格策略`
  }

  /**
   * 生成差评分析
   */
  const generateReviewAnalysis = () => {
    return `📝 **近期差评分析**

**差评统计（近30天）**
- 总评价数：89 条
- 差评数：6 条
- 差评率：6.7%

**主要问题分布**
1. **配送延迟**（3条）- 占比50%
   - 建议：优化出餐流程，预估更准确的配送时间

2. **菜品口味**（2条）- 占比33%
   - 建议：检查菜品制作标准，加强厨师培训

3. **分量不足**（1条）- 占比17%
   - 建议：标准化菜品分量，定期抽查

**改进建议**
- 建立差评快速响应机制
- 对重复出现的问题进行专项整改
- 主动联系差评用户，提供补偿方案`
  }

  /**
   * 生成备货建议
   */
  const generateStockSuggestion = () => {
    return `📦 **明日备货建议**

**基于历史数据分析**

| 食材 | 建议备货量 | 当前库存 | 状态 |
|------|------------|----------|------|
| 猪肉 | 15kg | 8kg | ⚠️ 需补充 |
| 鸡肉 | 12kg | 15kg | ✅ 充足 |
| 蔬菜 | 20kg | 5kg | ⚠️ 需补充 |
| 米饭 | 10kg | 12kg | ✅ 充足 |

**预测依据**
- 明日为周六，预计订单量增加 20%
- 天气晴朗，外卖需求可能上升
- 本周同期销售数据参考

**特别提醒**
- 猪肉和蔬菜库存较低，建议今日补货
- 可考虑增加特色菜品食材储备`
  }

  /**
   * 生成提高客单价建议
   */
  const generateRevenueTips = () => {
    return `💡 **提高客单价策略建议**

**1. 套餐组合优化**
- 推出「单人精选套餐」：主菜+小菜+饮料，定价35-45元
- 推出「双人分享套餐」：2主菜+2小菜+2饮料，定价68-88元
- 预计提升客单价 15-20%

**2. 加购推荐**
- 结账页面推荐小菜、饮料
- 设置「加X元换购」活动
- 预计提升客单价 8-12%

**3. 会员权益**
- 推出会员专属菜品
- 满减活动门槛设置略高于平均客单价
- 预计提升客单价 10-15%

**4. 时段促销**
- 下午茶时段推出「甜品+饮料」组合
- 晚餐时段推出「家庭套餐」
- 预计提升时段客单价 20%+

**实施建议**
优先级：套餐组合 > 加购推荐 > 会员权益 > 时段促销`
  }

  /**
   * 生成销量下滑分析
   */
  const generateDeclineAnalysis = () => {
    return `📉 **菜品销量下滑分析**

**下滑菜品统计**

| 菜品 | 本周销量 | 上周销量 | 变化幅度 |
|------|----------|----------|----------|
| 麻婆豆腐 | 98份 | 128份 | ↓ 23% |
| 酸辣土豆丝 | 56份 | 72份 | ↓ 22% |
| 蛋炒饭 | 45份 | 58份 | ↓ 22% |

**可能原因分析**
1. **季节因素**
   - 麻婆豆腐偏辣，夏季需求可能下降
   - 建议：推出清淡口味版本

2. **竞争因素**
   - 周边新开餐饮店分流客源
   - 建议：优化菜品口味，提升性价比

3. **价格因素**
   - 近期食材成本上涨，售价调整
   - 建议：推出优惠活动，恢复价格竞争力

**改进建议**
- 对下滑菜品进行口味测试
- 收集用户反馈，针对性改进
- 考虑推出限时优惠活动`
  }

  /**
   * 生成默认响应
   */
  const generateDefaultResponse = (question) => {
    return `感谢您的提问！关于「${question}」，我正在为您分析...

作为您的AI经营助手，我可以帮助您：
- 📊 分析销售数据和趋势
- 📝 生成评价回复建议
- 🍳 优化菜品描述
- 📦 提供备货建议

请告诉我您具体想了解哪方面的内容？`
  }

  /**
   * 清空聊天
   */
  const clearChat = () => {
    messages.value = [
      {
        id: Date.now(),
        sender: 'ai',
        content: MERCHANT_WELCOME_MESSAGE,
        time: formatTime(new Date()),
        avatar: '🤖'
      }
    ]
    ElMessage.success('聊天已清空')
  }

  /**
   * 滚动到底部
   */
  const scrollToBottom = async () => {
    await nextTick()
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  }

  /**
   * 格式化时间
   */
  const formatTime = (date) => {
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${hours}:${minutes}`
  }

  return {
    messages,
    isLoading,
    isStreaming,
    chatContainerRef,
    loadMessages,
    sendMessage,
    clearChat
  }
}
