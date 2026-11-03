const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { getAddress } = require('../utils/real-data')
const { TEST_API_BASE } = require('../utils/api')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')

const USER_ID = '3384650106421960'
// CI 不注入 AMAP_API_KEY，后端 /v1/location/cascader 按设计返回空列表。
// 这里使用从高德行政区接口读取到的真实层级，确保地址流程不依赖外部服务。
const SHANGHAI_CASCADER_DATA = [{
  value: '上海市',
  label: '上海市',
  children: [{
    value: '上海城区',
    label: '上海城区',
    children: [{ value: '徐汇区', label: '徐汇区' }]
  }]
}]

async function getAddressList() {
  const response = await fetch(`${TEST_API_BASE}/v1/addresses/user?userId=${USER_ID}`)
  const result = await response.json()
  if (!response.ok || !result?.success) throw new Error('读取地址列表失败')
  return result.data || []
}

async function findAddressByName(name) {
  return (await getAddressList()).find((item) => item.receiverName === name) || null
}

async function deleteAddressesByName(name) {
  const addresses = await getAddressList().catch(() => [])
  for (const address of addresses.filter((item) => item.receiverName === name)) {
    await fetch(`${TEST_API_BASE}/v1/users/${USER_ID}/addresses/${address.id}`, { method: 'DELETE' })
  }
}

async function selectShanghaiDistrict(page, dialog, district) {
  const cascader = dialog.locator('.el-cascader')
  const visibleNodes = page.locator('.el-cascader-node:visible')
  await expect(cascader).toBeVisible({ timeout: 10000 })
  await cascader.click()
  await visibleNodes.filter({ hasText: '上海市' }).first().click()
  await visibleNodes.filter({ hasText: '上海城区' }).first().click()
  await visibleNodes.filter({ hasText: district }).first().click()
}

async function getAiCapabilities() {
  const response = await fetch(`${TEST_API_BASE}/v1/ai/capabilities`)
  const result = await response.json()
  if (!response.ok || !result?.success || !result.data) {
    throw new Error('读取 AI 能力清单失败')
  }
  return result.data
}

async function stubShanghaiCascader(page) {
  await page.route('**/v1/location/cascader', (route) => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({
      success: true,
      code: '200',
      message: '成功',
      data: SHANGHAI_CASCADER_DATA
    })
  }))
}

test.describe('模块八：AI助手与地址管理', () => {

  test.beforeEach(async ({ page }) => { await loginAsUser(page) })

  test('8.1 AI对话', async ({ page }) => {
    markRunning('8.1')
    try {
      await page.goto('/user/home/ai')
      await expect(page.locator('.chat-messages')).toBeVisible({ timeout: 10000 })
      await expect(page.locator('.chat-message').first()).toBeVisible({ timeout: 10000 })
      markPassed('8.1', 'AI 聊天历史消息可加载')
    } catch (e) { markFailed('8.1', e.message.slice(0, 50)); throw e }
  })

  test('8.2 菜品识别接口契约与错误语义', async ({ page }) => {
    markRunning('8.2')
    try {
      const imageBuffer = require('fs').readFileSync(require('path').resolve(__dirname, '../../apps/miniapp/src/static/banner1.jpg'))
      const recognize = async (file) => {
        const response = await page.request.post('/api/v1/ai/dish-recognize', {
          multipart: { image: file }
        })
        return { response, body: await response.json() }
      }

      const capabilities = await getAiCapabilities()
      const jpeg = await recognize({
        name: 'banner1.jpg',
        mimeType: 'image/jpeg',
        buffer: imageBuffer
      })
      expect(jpeg.response.status()).toBe(200)
      if (capabilities.vision) {
        expect(jpeg.body.success).toBe(true)
        expect(jpeg.body.data).toMatchObject({
          isDish: true,
          nutritionEstimated: true
        })
        for (const key of ['name', 'calories', 'protein', 'fat', 'carbs', 'difficulty', 'preparationTime', 'ingredients', 'tags', 'confidence', 'nutritionScore', 'imageUrl']) {
          expect(jpeg.body.data).toHaveProperty(key)
        }
      } else {
        expect(jpeg.body.success).toBe(false)
        expect(jpeg.body.code).toBe('AI_VISION_NOT_CONFIGURED')
      }

      const disguisedText = await recognize({
        name: 'dish.jpg',
        mimeType: 'image/jpeg',
        buffer: Buffer.from('this is not an image')
      })
      expect(disguisedText.body.success).toBe(false)
      expect(disguisedText.body.code).toBe('415')
      expect(disguisedText.body.message).toContain('JPEG、PNG 或 WebP')

      const gif = await recognize({
        name: 'dish.gif',
        mimeType: 'image/gif',
        buffer: Buffer.from('GIF89a123456')
      })
      expect(gif.body.success).toBe(false)
      expect(gif.body.code).toBe('415')

      markPassed('8.2', '菜品识别接口契约、真实魔数与错误语义通过')
    } catch (e) { markFailed('8.2', e.message.slice(0, 50)); throw e }
  })

  test('8.2b 桌面端识别失败后可重试', async ({ page }) => {
    markRunning('8.2b')
    try {
      await page.route('**/v1/ai/capabilities', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            code: '200',
            message: '成功',
            data: { available: true, vision: true, textChat: true, streaming: true }
          })
        })
      })
      await page.goto('/user/home/ai')
      await expect(page.locator('#tab-btn-chat')).toHaveClass(/active/, { timeout: 10000 })
      await page.locator('#tab-btn-recognition').click()
      await expect(page.locator('#tab-btn-recognition')).toHaveClass(/active/)
      await expect(page.locator('.recognition-content-wrapper')).toBeVisible({ timeout: 10000 })

      await page.route('**/v1/ai/dish-recognize', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ success: false, code: '500', message: '识别失败，请稍后重试' })
        })
      })

      await page.locator('#dish-image-upload').setInputFiles(require('path').resolve(__dirname, '../../apps/miniapp/src/static/banner1.jpg'))
      await page.locator('.recognition-content-wrapper button.primary-btn').click()
      await expect(page.locator('.recognition-error')).toBeVisible({ timeout: 10000 })
      await expect(page.locator('.recognition-error')).toContainText('识别失败，请稍后重试')
      await expect(page.locator('.error-retry-btn')).toBeVisible()
      await expect(page.locator('.recognition-content-wrapper button.primary-btn')).toBeEnabled()
      markPassed('8.2b', '识别失败保留图片并展示重试入口')
    } catch (e) { markFailed('8.2b', e.message.slice(0, 50)); throw e }
  })

  test('8.2c 桌面端展示置信度、营养评分与估算提示', async ({ page }) => {
    markRunning('8.2c')
    try {
      await page.route('**/v1/ai/capabilities', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            code: '200',
            message: '成功',
            data: { available: true, vision: true, textChat: true, streaming: true }
          })
        })
      })
      await page.goto('/user/home/ai')
      await expect(page.locator('#tab-btn-chat')).toHaveClass(/active/, { timeout: 10000 })
      await page.locator('#tab-btn-recognition').click()
      await expect(page.locator('#tab-btn-recognition')).toHaveClass(/active/)
      await expect(page.locator('.recognition-content-wrapper')).toBeVisible({ timeout: 10000 })

      await page.route('**/v1/ai/dish-recognize', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            code: '200',
            message: '成功',
            data: {
              isDish: true,
              name: '番茄炒蛋',
              calories: 180,
              protein: 10,
              fat: 12,
              carbs: 8,
              difficulty: '简单',
              preparationTime: '15分钟',
              ingredients: ['番茄', '鸡蛋'],
              tags: ['家常菜'],
              confidence: 0.92,
              nutritionScore: 8,
              nutritionEstimated: true,
              imageUrl: 'http://localhost:8080/api/uploads/dish-recognition/test.jpg'
            }
          })
        })
      })

      await page.locator('#dish-image-upload').setInputFiles(require('path').resolve(__dirname, '../../apps/miniapp/src/static/banner1.jpg'))
      await page.locator('.recognition-content-wrapper button.primary-btn').click()
      await expect(page.locator('.confidence-chip')).toContainText('置信度 92%', { timeout: 10000 })
      await expect(page.locator('.nutrition-score-chip')).toContainText('营养评分 8/10')
      await expect(page.locator('.estimate-note')).toContainText('营养数据为估算值')
      markPassed('8.2c', '桌面端展示置信度、营养评分与估算提示')
    } catch (e) { markFailed('8.2c', e.message.slice(0, 50)); throw e }
  })

  test('8.2a 无视觉模型时菜品识别入口降级', async ({ page }) => {
    markRunning('8.2a')
    try {
      await page.goto('/user/home/ai')
      const capabilities = await getAiCapabilities()
      expect(capabilities.vision).toBe(false)
      await expect(page.locator('.status-badge')).not.toHaveText('检测中', { timeout: 10000 })
      const recognitionTab = page.locator('#tab-btn-recognition')
      await expect(recognitionTab).toBeDisabled()
      await expect(recognitionTab).toContainText('暂未开放')

      const response = await page.request.post('/api/v1/ai/dish-recognize', {
        multipart: {
          image: {
            name: 'banner1.jpg',
            mimeType: 'image/jpeg',
            buffer: require('fs').readFileSync(require('path').resolve(__dirname, '../../apps/miniapp/src/static/banner1.jpg'))
          }
        }
      })
      const body = await response.json()
      expect(response.status()).toBe(200)
      expect(body.success).toBe(false)
      expect(body.code).toBe('AI_VISION_NOT_CONFIGURED')
      expect(body.message).toContain('暂未开放')
      markPassed('8.2a', '无视觉模型时入口禁用且接口返回明确业务错误')
    } catch (e) { markFailed('8.2a', e.message.slice(0, 50)); throw e }
  })

  test('8.3a 无流式模型时食谱优化入口降级', async ({ page }) => {
    markRunning('8.3a')
    try {
      await page.goto('/user/home/ai')
      const capabilities = await getAiCapabilities()
      expect(capabilities.streaming).toBe(false)
      await expect(page.locator('.status-badge')).not.toHaveText('检测中', { timeout: 10000 })
      const recipeTab = page.locator('#tab-btn-recipe')
      await expect(recipeTab).toBeDisabled()
      await expect(recipeTab).toContainText('暂未开放')

      const response = await page.request.post('/api/v1/ai/recipe/stream', {
        data: { recipe: '番茄炒蛋：准备鸡蛋2个、番茄2个，调味后出锅。' },
        headers: { Accept: 'text/event-stream' }
      })
      expect(response.status()).toBe(200)
      const body = await response.text()
      expect(body).toContain('"error":true')
      expect(body).toContain('暂未开放')
      markPassed('8.3a', '无流式模型时入口禁用且 SSE 返回明确业务错误')
    } catch (e) { markFailed('8.3a', e.message.slice(0, 50)); throw e }
  })

  test('8.4 AI聊天历史', async ({ page }) => {
    markRunning('8.4')
    try {
      await page.goto('/user/home/ai')
      await expect(page.locator('.chat-messages')).toBeVisible({ timeout: 10000 })
      await expect(page.locator('.chat-message').first()).toBeVisible({ timeout: 10000 })
      markPassed('8.4', 'AI聊天历史消息可加载')
    } catch (e) { markFailed('8.4', e.message.slice(0, 50)); throw e }
  })

  test('8.5 地址列表', async ({ page }) => {
    markRunning('8.5')
    try {
      const address = await getAddress()
      await page.goto('/user/home/address')
      const card = page.locator('.address-card.default-address')
      await expect(card).toBeVisible({ timeout: 10000 })
      await expect(card.locator('.address-name')).toHaveText(address.receiverName)
      await expect(card.locator('.address-phone')).toHaveText(address.receiverPhone)
      await expect(card.locator('.address-full')).toContainText(address.province)
      await expect(card.locator('.address-full')).toContainText(address.city)
      await expect(card.locator('.address-full')).toContainText(address.detail)
      markPassed('8.5', `默认地址 ${address.id} 的字段真实展示`)
    } catch (e) { markFailed('8.5', e.message.slice(0, 50)); throw e }
  })

  test('8.6 添加地址', async ({ page }) => {
    markRunning('8.6')
    let createdName
    try {
      await stubShanghaiCascader(page)
      await page.goto('/user/home/address')
      await page.locator('.address-container > button', { hasText: '新增地址' }).click()
      const dialog = page.locator('.el-dialog', { hasText: '新增地址' })
      const suffix = Date.now().toString().slice(-8)
      createdName = `E2E 新增${suffix}`
      await dialog.locator('input[placeholder="请输入收件人姓名"]').fill(createdName)
      await dialog.locator('input[placeholder="请输入联系电话"]').fill('13800000001')
      await dialog.locator('input[placeholder="请输入详细地址"]').fill(`测试路 ${suffix} 号`)
      await selectShanghaiDistrict(page, dialog, '徐汇区')
      await dialog.getByRole('button', { name: '保存' }).click()
      const card = page.locator('.address-card', { hasText: createdName }).first()
      await expect(card).toBeVisible({ timeout: 10000 })
      await expect(card.locator('.address-full')).toContainText(`测试路 ${suffix} 号`)
      const created = await findAddressByName(createdName)
      expect(created).toBeTruthy()
      expect(created.province).toBe('上海市')
      expect(created.city).toBe('上海城区')
      expect(created.district).toBe('徐汇区')
      markPassed('8.6', `新增地址 ${created.id} 已真实落库并展示`)
    } finally {
      if (createdName) await deleteAddressesByName(createdName)
    }
  })

  test('8.7 编辑地址', async ({ page }) => {
    markRunning('8.7')
    let originalName
    let editedName
    try {
      await stubShanghaiCascader(page)
      await page.goto('/user/home/address')
      await page.locator('.address-container > button', { hasText: '新增地址' }).click()
      const addDialog = page.locator('.el-dialog', { hasText: '新增地址' })
      const suffix = Date.now().toString().slice(-8)
      originalName = `E2E 编辑${suffix}`
      editedName = `E2E 已编辑${suffix}`
      await addDialog.locator('input[placeholder="请输入收件人姓名"]').fill(originalName)
      await addDialog.locator('input[placeholder="请输入联系电话"]').fill('13800000001')
      await addDialog.locator('input[placeholder="请输入详细地址"]').fill(`编辑前 ${suffix} 号`)
      await selectShanghaiDistrict(page, addDialog, '徐汇区')
      await addDialog.getByRole('button', { name: '保存' }).click()
      const card = page.locator('.address-card', { hasText: originalName }).first()
      await expect(card).toBeVisible({ timeout: 10000 })
      await card.getByRole('button', { name: '编辑' }).click()
      const editDialog = page.locator('.el-dialog', { hasText: '编辑地址' })
      await expect(editDialog.locator('input[placeholder="请输入收件人姓名"]')).toHaveValue(originalName)
      await editDialog.locator('input[placeholder="请输入收件人姓名"]').fill(editedName)
      await editDialog.locator('input[placeholder="请输入详细地址"]').fill(`编辑后 ${suffix} 号`)
      await editDialog.getByRole('button', { name: '保存' }).click()
      const editedCard = page.locator('.address-card', { hasText: editedName }).first()
      await expect(editedCard).toBeVisible({ timeout: 10000 })
      await expect(editedCard.locator('.address-full')).toContainText(`编辑后 ${suffix} 号`)
      const edited = await findAddressByName(editedName)
      expect(edited).toBeTruthy()
      expect(edited.detail).toBe(`编辑后 ${suffix} 号`)
      expect(edited.province).toBe('上海市')
      expect(edited.city).toBe('上海城区')
      expect(edited.district).toBe('徐汇区')
      markPassed('8.7', `临时地址 ${edited.id} 编辑后字段真实更新`)
    } finally {
      if (editedName) await deleteAddressesByName(editedName)
      if (originalName) await deleteAddressesByName(originalName)
    }
  })

  test('8.8 设置默认地址', async ({ page }) => {
    markRunning('8.8')
    const originalDefault = await getAddress({ isDefault: true })
    const target = await getAddress({ isDefault: false })
    try {
      await page.goto('/user/home/address')
      const targetFullAddress = `${target.province} ${target.city} ${target.district} ${target.detail}`
      const targetCard = page.locator('.address-card').filter({
        has: page.locator('.address-full', { hasText: targetFullAddress }),
        hasText: '设置默认'
      })
      const setDefaultButton = targetCard.getByRole('button', { name: '设置默认' })
      await expect(setDefaultButton).toBeVisible({ timeout: 10000 })
      await setDefaultButton.click()
      await expect(page.locator('.address-card.default-address .address-full')).toContainText(targetFullAddress, { timeout: 10000 })
      const currentDefault = await getAddress({ addressId: target.id })
      expect([1, true]).toContain(currentDefault.isDefault)
      markPassed('8.8', `默认地址从 ${originalDefault.id} 切换为 ${target.id}`)
    } finally {
      const response = await page.request.put(`/api/v1/users/3384650106421960/addresses/${originalDefault.id}`, {
        data: {
          receiverName: originalDefault.receiverName,
          receiverPhone: originalDefault.receiverPhone,
          province: originalDefault.province,
          city: originalDefault.city,
          district: originalDefault.district,
          detail: originalDefault.detail,
          isDefault: 1,
          tag: originalDefault.tag || ''
        }
      })
      expect(response.ok()).toBeTruthy()
      const restoredDefault = await getAddress({ addressId: originalDefault.id })
      expect([1, true]).toContain(restoredDefault.isDefault)
    }
  })

  test.afterAll(() => { updateSummary() })
})
